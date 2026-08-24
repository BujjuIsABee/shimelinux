PACKAGE='shimelinux'
VERSION='1.2.2'
DEPENDS='openjdk-21-jre, libayatana-appindicator3-1'
SECTION='java'
PRIORITY='optional'
ARCHITECTURE='all'
MAINTAINER='Bujju (https://github.com/BujjuIsABee)'
DESCRIPTION='An unofficial Linux port of Shimeji-ee Desktop Pet'

files=(
  'shimelinux.sh'
  'shimelinux.desktop'
  'icon.svg'
  "build/libs/${PACKAGE}-${VERSION}.jar"
  'LICENSE'
  'LICENSE-ORIGINAL'
)

mkdir -p ~/debbuild/${PACKAGE}_${VERSION}_${ARCHITECTURE}/DEBIAN
cat > ~/debbuild/${PACKAGE}_${VERSION}_${ARCHITECTURE}/DEBIAN/control << EOF
Package: $PACKAGE
Version: $VERSION
Depends: $DEPENDS
Section: $SECTION
Priority: $PRIORITY
Architecture: $ARCHITECTURE
Installed-Size: $(du -ck "${files[@]}" | tail -n 1 | awk '{print $1}')
Maintainer: $MAINTAINER
Description: $DESCRIPTION
EOF

mkdir -p ~/debbuild/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/bin
cp shimelinux.sh ~/debbuild/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/bin/shimelinux
chmod +x ~/debbuild/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/bin/shimelinux
mkdir -p ~/debbuild/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/applications
cp shimelinux.desktop ~/debbuild/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/applications/shimelinux.desktop
mkdir -p ~/debbuild/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/icons/hicolor/scalable/apps
cp icon.svg ~/debbuild/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/icons/hicolor/scalable/apps/shimelinux.svg
mkdir -p ~/debbuild/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/java
cp build/libs/${PACKAGE}-${VERSION}.jar ~/debbuild/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/java/shimelinux.jar
mkdir -p ~/debbuild/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/licenses/shimelinux
cp LICENSE ~/debbuild/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/licenses/shimelinux/LICENSE
cp LICENSE-ORIGINAL ~/debbuild/${PACKAGE}_${VERSION}_${ARCHITECTURE}/usr/share/licenses/shimelinux/LICENSE-ORIGINAL

dpkg-deb --build --root-owner-group ~/debbuild/${PACKAGE}_${VERSION}_${ARCHITECTURE}
mv ~/debbuild/${PACKAGE}_${VERSION}_${ARCHITECTURE}.deb .
rm -rf ~/debbuild
