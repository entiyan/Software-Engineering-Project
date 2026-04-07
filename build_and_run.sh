#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# build_and_run.sh  —  Compile and run the JDM Patient Management System
# Requires: JDK 17+ (javac + java)
# ─────────────────────────────────────────────────────────────────────────────
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$SCRIPT_DIR/src/main/java"
OUT_DIR="$SCRIPT_DIR/out"
DATA_DIR="$SCRIPT_DIR/data"
JAR="$SCRIPT_DIR/jdm.jar"

echo "=== Compiling JDM Application ==="
mkdir -p "$OUT_DIR"
find "$SRC_DIR" -name "*.java" > /tmp/sources.txt
javac --release 17 -d "$OUT_DIR" @/tmp/sources.txt
echo "Compilation successful."

echo "=== Packaging JAR ==="
echo "Main-Class: jdm.Main" > /tmp/manifest.txt
jar cfm "$JAR" /tmp/manifest.txt -C "$OUT_DIR" .
echo "JAR created: $JAR"

echo "=== Running Application ==="
echo "Data directory: $DATA_DIR"
echo ""
java -jar "$JAR" "$DATA_DIR"
