PACKAGE='shimelinux'
VERSION='1.2.0'
DEPENDS='openjdk-21-jre, libayatana-appindicator3-1'
SECTION='java'
PRIORITY='optional'
ARCHITECTURE='all'
MAINTAINER='Bujju (https://github.com/BujjuIsABee)'
DESCRIPTION='An unofficial Linux port of Shimeji-ee Desktop Pet'

cd ../

# Get installed size
files=(
  'shimelinux.sh'
  'shimelinux.desktop'
  'icon.svg'
  "build/libs/${PACKAGE}-${VERSION}.jar"
  'LICENSE'
  'LICENSE-ORIGINAL'
)

total_size = 0
for file in "${files[@]}"; do
  size=$(wc -c < "$file")
  total_size=$((total_size+size))
done

INSTALLED_SIZE=$((total_size / 1024))

mkdir -p distribution/build/${PACKAGE}_${VERSION}_${ARCHITECTURE}/DEBIAN
cat > distribution/build/${PACKAGE}_${VERSION}_${ARCHITECTURE}/DEBIAN/control << EOF
Package: $PACKAGE
Version: $VERSION
Depends: $DEPENDS
Section: $SECTION
Priority: $PRIORITY
Architecture: $ARCHITECTURE
Installed-Size: $INSTALLED_SIZE
Maintainer: $MAINTAINER
Description: $DESCRIPTION
EOF

mkdir -p distribution/build/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/bin
cp shimelinux.sh distribution/build/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/bin/shimelinux
chmod +x distribution/build/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/bin/shimelinux
mkdir -p distribution/build/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/applications
cp shimelinux.desktop distribution/build/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/applications/shimelinux.desktop
mkdir -p distribution/build/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/icons/hicolor/scalable/apps
cp icon.svg distribution/build/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/icons/hicolor/scalable/apps/shimelinux.svg
mkdir -p distribution/build/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/java
cp build/libs/${PACKAGE}-${VERSION}.jar distribution/build/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/java/shimelinux.jar
mkdir -p distribution/build/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/licenses/shimelinux
cp LICENSE distribution/build/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/licenses/shimelinux/LICENSE
cp LICENSE-ORIGINAL distribution/build/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/licenses/shimelinux/LICENSE-ORIGINAL

dpkg-deb --build --root-owner-group distribution/build/${PACKAGE}_${VERSION}_${ARCHITECTURE}
rm -rf distribution/build/${PACKAGE}_${VERSION}_${ARCHITECTURE}
