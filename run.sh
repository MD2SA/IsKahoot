#!/bin/bash

SRC_DIR="src"
OUT_DIR="out"
LIB_DIR="libs"   #  libs: e.g., gson-2.10.1.jar

mkdir -p "$OUT_DIR"

# Build the classpath from jars
CP=""
for jar in "$LIB_DIR"/*.jar; do
    CP="$CP:$jar"
done
CP="${CP#:}"  # remove the initial ":"

echo "Compiling all Java files..."

# Compile all .java files, including jars in the classpath
find "$SRC_DIR" -name "*.java" > sources.txt
javac -cp "$CP" -d "$OUT_DIR" @sources.txt
rm sources.txt

if [ $? -ne 0 ]; then
    echo "Compilation errors occurred."
    exit 1
fi

echo "Compilation finished."


# If the user passed the class on the command line
if [ -n "$1" ]; then
    CLASS_NAME="$1"
    shift
    PROGRAM_ARGS="$@"
else
    # List only classes that have "Main" in the name
    AVAILABLE_CLASSES=$(find "$OUT_DIR" -name "*Main*.class" | sed "s|$OUT_DIR/||;s|/|.|g;s|.class$||")

    echo "Available classes (with 'Main' in the name):"

    if [ -z "$AVAILABLE_CLASSES" ]; then
        echo "No classes with 'Main' in the name were found."
        AVAILABLE_CLASSES=$(find "$OUT_DIR" -name "*.class" | sed "s|$OUT_DIR/||;s|/|.|g;s|.class$||")
    fi

    echo "$AVAILABLE_CLASSES"

    read -rp "Enter the fully qualified class name to run: " CLASS_NAME
    read -rp "Enter program arguments (or leave empty): " PROGRAM_ARGS
fi

# Run the Java class
java -cp "$OUT_DIR:$CP" "$CLASS_NAME" $PROGRAM_ARGS
