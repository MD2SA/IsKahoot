#!/bin/bash

SRC_DIR="src"
OUT_DIR="out"
LIB_DIR="libs"   # coloque aqui todos os jars que seu projeto usa, ex: gson-2.10.1.jar

# Cria a pasta de saída
mkdir -p "$OUT_DIR"

# Monta o classpath dos jars
CP=""
for jar in "$LIB_DIR"/*.jar; do
    CP="$CP:$jar"
done
CP="${CP#:}"  # remove o ":" inicial

echo "Compilando todos os arquivos Java..."

# Compila todos os arquivos de src, incluindo os jars no classpath
find "$SRC_DIR" -name "*.java" > sources.txt
javac -cp "$CP" -d "$OUT_DIR" @sources.txt
rm sources.txt

if [ $? -ne 0 ]; then
    echo "Houveram erros na compilação."
    exit 1
fi

echo "Compilação concluída."

# Se o usuário passou a classe na linha de comando
if [ -n "$1" ]; then
    CLASS_NAME="$1"
    shift
    PROGRAM_ARGS="$@"
else
    # Modo interativo
    echo "Classes disponíveis:"
    find "$OUT_DIR" -name "*.class" | sed "s|$OUT_DIR/||;s|/|.|g;s|.class$||"

    read -rp "Digite o nome completo da classe que deseja executar: " CLASS_NAME
    read -rp "Digite os argumentos do programa (ou vazio): " PROGRAM_ARGS
fi

# Executa
java -cp "$OUT_DIR:$CP" "$CLASS_NAME" $PROGRAM_ARGS
