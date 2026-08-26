#!/usr/bin/env bash
# Генерирует Java-классы protobuf/gRPC из src/main/proto каждого сервиса и копирует
# их прямо в src/main/java (в пакет из java_package в .proto).
#
# ПОЧЕМУ РУЧНОЙ СКРИПТ, А НЕ protobuf-maven-plugin ПРИ mvn compile:
# путь проекта содержит кириллицу (C:\Users\Антон\...), а protobuf-maven-plugin на
# Windows ломает передачу такого пути в protoc.exe (аргументы с кириллицей бьются в
# "?????" на границе Java ProcessBuilder -> нативный exe, sun.jnu.encoding на Windows
# не переопределяется через -D). Обходной путь: гонять protoc из каталога без кириллицы
# в аргументах (сам protoc.exe и вывод — во временную ASCII-only директорию), а потом
# обычным cp переносить результат в src/main/java — cp у Git Bash/MSYS с кириллицей
# работает нормально, проблема именно в протоковском argv.
#
# Запускать вручную после любого изменения .proto файлов.
set -euo pipefail

TOOLS_DIR="/c/tools"
PROTOC="$TOOLS_DIR/protoc.exe"
GRPC_PLUGIN="$TOOLS_DIR/protoc-gen-grpc-java.exe"

if [ ! -x "$PROTOC" ] || [ ! -x "$GRPC_PLUGIN" ]; then
  echo "protoc/protoc-gen-grpc-java не найдены в $TOOLS_DIR — сначала скопируйте их туда из ~/.m2 (см. память проекта)." >&2
  exit 1
fi

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TMP_OUT="/tmp/profkom_protoc_gen"

for SERVICE_DIR in "$ROOT_DIR"/*_service; do
  PROTO_DIR="$SERVICE_DIR/src/main/proto"
  [ -d "$PROTO_DIR" ] || continue

  SERVICE_NAME="$(basename "$SERVICE_DIR")"
  echo "== $SERVICE_NAME =="

  rm -rf "$TMP_OUT"
  mkdir -p "$TMP_OUT/java" "$TMP_OUT/grpc"

  ( cd "$PROTO_DIR" && for PROTO_FILE in *.proto; do
      "$PROTOC" \
        --java_out="$TMP_OUT/java" \
        --plugin=protoc-gen-grpc-java="$GRPC_PLUGIN" \
        --grpc-java_out="$TMP_OUT/grpc" \
        "$PROTO_FILE"
      echo "  сгенерирован $PROTO_FILE"
  done )

  # копируем сгенерированные .java поверх src/main/java (структура пакетов уже верная)
  cp -r "$TMP_OUT/java/"* "$SERVICE_DIR/src/main/java/"
  cp -r "$TMP_OUT/grpc/"* "$SERVICE_DIR/src/main/java/"
done

rm -rf "$TMP_OUT"
echo "Готово."
