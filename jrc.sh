#!/usr/bin/env bash
# =============================================================================
# jrc - Java Run & Compile
# Script to compile and run local Java projects.
# You must run it from the project root directory (the parent of src/).
#
# - Compiles sources from src/
# - Outputs .class files to out/
# - Automatically includes jars from libs/
# - Lets you select the main class by name or number
#
# Author : grok & MD2SA
# Dependencies: bash ≥4, java, javac
# =============================================================================

set -euo pipefail                # Safety: exit on error, undefined var, pipe fail
IFS=$'\n\t'                      # Better field splitting

# ----------------------------- Configuration ---------------------------------
SRC_DIR="src"                    # Source directory
OUT_DIR="out"                    # Compiled classes output directory
LIB_DIR="libs"                   # Directory containing external .jar libraries

# -------------------------------- Colors ------------------------------------
# Only enable colors when output is a terminal (nice for pipes/redirections)
if [[ -t 1 ]]; then
    GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'
    BLUE='\033[0;34m';  PURPLE='\033[0;35m'; CYAN='\033[0;36m'
    BOLD='\033[1m';     NC='\033[0m'        # No Color
else
    GREEN='' YELLOW='' RED='' BLUE='' PURPLE='' CYAN='' BOLD='' NC=''
fi

# ----------------------------- Logging functions ----------------------------
log_info()    { echo -e "${BLUE}ℹ${NC} $*"; }
log_warn()    { echo -e "${YELLOW}⚠${NC} $*"; }
log_success() { echo -e "${GREEN}✔${NC} $*"; }
log_error()   { echo -e "${RED}✖${NC} $*" >&2; }

# ---------------------------------- Help ------------------------------------
usage() {
    cat << EOF
jrc — Compile and run Java projects with style
Usage: jrc [options] [number|class] [arguments...]

Options:
  -r, --no-compile   Skip compilation (use existing .class files)
  -h, --help         Show this help message

Examples:
  jrc                   → lists classes and asks which one to run
  jrc 3                 → runs the 3rd class in the list
  jrc HelloWorld        → runs HelloWorld (exact or partial match)
  jrc MyApp arg1 arg2   → runs MyApp with arguments
  jrc -r 1              → runs without recompiling

Tip: Use quotes for arguments with spaces, e.g.: jrc Greeting "Hello World" 42
EOF
    exit 0
}

# --------------------------- Argument parsing ------------------------------
NO_COMPILE=false

while [[ $# -gt 0 ]]; do
    case "$1" in
        -h|--help)          usage ;;
        -r|--no-compile)    NO_COMPILE=true; shift ;;
        *)                  break ;;   # stop on first non-option argument
    esac
done

# --------------------------- Prepare environment ----------------------------
mkdir -p "$OUT_DIR"

# Build classpath: output directory + every .jar in libs/
build_classpath() {
    local cp="$OUT_DIR"
    shopt -s nullglob                     # makes globs return nothing if no match
    for jar in "$LIB_DIR"/*.jar; do
        cp="$cp:$jar"
    done
    shopt -u nullglob
    echo "$cp"
}

CLASSPATH=$(build_classpath)

# ------------------------------- Compile -----------------------------------
compile() {
    log_info "Compiling sources in '$SRC_DIR'..."

    # Find all .java files and compile them in one javac call (faster)
    local sources
    mapfile -t sources < <(find "$SRC_DIR" -name "*.java" 2>/dev/null)

    if [[ ${#sources[@]} -eq 0 ]]; then
        log_warn "No .java files found in '$SRC_DIR'."
        return 0
    fi

    if javac -cp "$CLASSPATH" -d "$OUT_DIR" "${sources[@]}"; then
        log_success "Compilation successful!"
    else
        log_error "Compilation failed."
        exit 1
    fi
}

if ! $NO_COMPILE; then
    compile
else
    log_warn "Compilation skipped (-r / --no-compile)"
fi

# ------------------------ Discover classes with main -----------------------
discover_main_classes() {
    local -a candidates=()

    # Prefer classes that contain "Main" in their name (most projects follow this)
    mapfile -t candidates < <(find "$OUT_DIR" -name "*Main*.class" -type f 2>/dev/null | sort)

    # Fallback: use all compiled classes
    if [[ ${#candidates[@]} -eq 0 ]]; then
        mapfile -t candidates < <(find "$OUT_DIR" -name "*.class" -type f 2>/dev/null | sort)
    fi

    # Convert file paths to fully qualified class names
    local -a classes=()
    for file in "${candidates[@]}"; do
        local class="${file#$OUT_DIR/}"      # remove output dir prefix
        class="${class%.class}"              # remove .class extension
        class="${class//\//.}"               # replace / with .
        classes+=("$class")
    done

    printf '%s\n' "${classes[@]}"
}

mapfile -t MAIN_CLASSES < <(discover_main_classes)

if [[ ${#MAIN_CLASSES[@]} -eq 0 ]]; then
    log_error "No compiled classes found in '$OUT_DIR'."
    exit 1
fi

# --------------------------- Class resolution -----------------------------
resolve_class() {
    local input="$1"
    local -a matches=()

    # 1. Exact match
    for c in "${MAIN_CLASSES[@]}"; do
        if [[ "$c" == "$input" ]]; then
            echo "$c"
            return 0
        fi
    done

    # 2. By number (1-based index)
    if [[ "$input" =~ ^[0-9]+$ ]] && (( input >= 1 && input <= ${#MAIN_CLASSES[@]} )); then
        echo "${MAIN_CLASSES[$((input-1))]}"
        return 0
    fi

    # 3. Partial match (case-sensitive)
    for c in "${MAIN_CLASSES[@]}"; do
        [[ "$c" == *"$input"* ]] && matches+=("$c")
    done

    case ${#matches[@]} in
        0)
            log_error "Class not found: $input"
            exit 1
            ;;
        1)
            echo "${matches[0]}"
            ;;
        *)
            log_info "Multiple classes found:"
            for i in "${!matches[@]}"; do
                echo " $((i+1))) ${matches[i]}"
            done
            read -rp "Choose the number: " choice
            if [[ "$choice" =~ ^[0-9]+$ ]] && (( choice >= 1 && choice <= ${#matches[@]} )); then
                echo "${matches[$((choice-1))]}"
            else
                log_error "Invalid choice."
                exit 1
            fi
            ;;
    esac
}

# Prompt user for program arguments (with proper quoting support)
prompt_for_args() {
    echo -e "${BLUE}Program arguments (use double quotes for spaces, or leave empty):${NC}"
    echo
    read -r arg_line
    [[ -z "$arg_line" ]] && { PROGRAM_ARGS=(); return; }

    # Safely evaluate the input line as an array (handles quoting)
    local args
    if eval "args=($arg_line)" 2>/dev/null; then
        PROGRAM_ARGS=("${args[@]}")
    else
        log_error "Invalid arguments! Use double quotes for values containing spaces."
        prompt_for_args  # retry
    fi
}

# ----------------------------- Final selection -----------------------------
if [[ -n "${1:-}" ]]; then
    SELECTED_CLASS=$(resolve_class "$1")
    shift
    PROGRAM_ARGS=("$@")                     # remaining args are program args
else
    echo -e "${BLUE}${BOLD}Available classes with main method:${NC}"
    for i in "${!MAIN_CLASSES[@]}"; do
        printf " ${PURPLE}%3d)${NC} %s\n" $((i+1)) "${MAIN_CLASSES[i]}"
    done
    echo
    read -rp "Enter the number or class name: " choice
    SELECTED_CLASS=$(resolve_class "$choice")
    prompt_for_args
fi

# Final sanity check: ensure the .class file actually exists
if [[ ! -f "$OUT_DIR/${SELECTED_CLASS//.//}.class" ]]; then
    log_error "Class file not found for: $SELECTED_CLASS"
    exit 1
fi

# -------------------------------- Run --------------------------------------
echo
echo -e "${GREEN}${BOLD}Running Java program${NC}"
echo -e " ${YELLOW}Class:${NC}      ${PURPLE}${SELECTED_CLASS}${NC}"
echo -e " ${YELLOW}Classpath:${NC} ${CYAN}${CLASSPATH}${NC}"

if [[ ${#PROGRAM_ARGS[@]} -eq 0 ]]; then
    echo -e " ${YELLOW}Arguments:${NC} ${RED}(none)${NC}"
else
    echo -e " ${YELLOW}Arguments:${NC}"
    for i in "${!PROGRAM_ARGS[@]}"; do
        if [[ $i -eq $((${#PROGRAM_ARGS[@]}-1)) ]]; then
            printf " ${BLUE}└─${NC} %s\n" "${PROGRAM_ARGS[i]}"
        else
            printf " ${BLUE}├─${NC} %s\n" "${PROGRAM_ARGS[i]}"
        fi
    done
fi
echo

# Execute the Java program
exec java -cp "$CLASSPATH" "$SELECTED_CLASS" "${PROGRAM_ARGS[@]}"
