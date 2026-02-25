#!/usr/bin/env bash
#!/usr/bin/env bash

# Resolve the directory of this script
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Path to the client-app module
CLIENT_DIR="$ROOT_DIR/app"

# Build output directory
BUILD_DIR="$CLIENT_DIR/build"

# Classpath: thin JAR + all dependency JARs
CLASSPATH="$BUILD_DIR/libs/*:$BUILD_DIR/install/app/lib/*"

# Run the main class
exec java -cp "$CLASSPATH" com.cello.Server "$@"
