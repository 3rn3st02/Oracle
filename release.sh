
#!/bin/bash

VERSION=$1

if [ -z "$VERSION" ]; then
  echo "❌ Uso: ./release.sh <version>"
  echo "Ejemplo: ./release.sh 1.2"
  exit 1
fi

TAG="APK_V$VERSION"

echo "🚀 Iniciando release $TAG..."

# 1. Asegurar que estamos en android-client
git checkout android-client || exit

# 2. Guardar cambios
echo "💾 Guardando cambios..."
git add .
git commit -m "release(android): preparación APK V$VERSION" || true

# 3. Push
git push origin android-client

# 4. Crear tag
echo "🏷️ Creando tag $TAG..."
git tag -a $TAG -m "APK V$VERSION release automatizada"

git push origin $TAG

# 5. Build APK
echo "📦 Generando APK..."
cd android/app-oraculo || exit

./gradlew assembleDebug || exit

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

if [ ! -f "$APK_PATH" ]; then
  echo "❌ ERROR: No se encontró el APK"
  exit 1
fi

cd ../../..

# 6. Copiar APK a releases
DEST_DIR="releases/v$VERSION"
mkdir -p $DEST_DIR

cp android/app-oraculo/$APK_PATH $DEST_DIR/

APK_FILE="$DEST_DIR/app-debug.apk"

# 7. Crear GitHub Release
echo "🌐 Creando GitHub Release..."

gh release create $TAG $APK_FILE \
  --title "APK V$VERSION" \
  --notes "Release automático APK V$VERSION

Características:
- App Android funcional
- Arquitectura cliente-servidor preparada
- Integración API en progreso

Generado automáticamente 🚀"

echo "✅ Release $TAG completado"
