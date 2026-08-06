NAME='shimelinux'
VERSION='1.2.0'
RELEASE='1%{?dist}'
SUMMARY='An unofficial Linux port of Shimeji-ee Desktop Pet'
BUILD_ARCH='noarch'
LICENSE='BSD-3-CLAUSE'
SOURCE0='%{name}-%{version}.tar.gz'
REQUIRES=('bash' 'java >= 21' 'libappindicator-gtk3')

cd ../

mkdir -p ~/rpmbuild/{BUILD,RPMS,SOURCES,SPECS,SRPMS}
cat > ~/rpmbuild/SPECS/${NAME}.spec << EOF
Name:           $NAME
Version:        $VERSION
Release:        $RELEASE
Summary:        $SUMMARY
BuildArch:      $BUILD_ARCH

License:        $LICENSE
Source0:        $SOURCE0

$(for r in "${REQUIRES[@]}"; do echo "Requires:       $r"; done)

%description
$SUMMARY

%prep
%setup -q

%install
rm -rf \$RPM_BUILD_ROOT
mkdir -p \$RPM_BUILD_ROOT/%{_bindir}
mkdir -p \$RPM_BUILD_ROOT/%{_datadir}/java
mkdir -p \$RPM_BUILD_ROOT/%{_datadir}/icons/hicolor/scalable/apps
mkdir -p \$RPM_BUILD_ROOT/%{_datadir}/applications

cp %{name}.sh \$RPM_BUILD_ROOT/%{_bindir}/%{name}
cp %{name}-%{version}.jar \$RPM_BUILD_ROOT/%{_datadir}/java/%{name}.jar
cp icon.svg \$RPM_BUILD_ROOT/%{_datadir}/icons/hicolor/scalable/apps/shimelinux.svg
cp %{name}.desktop \$RPM_BUILD_ROOT/%{_datadir}/applications/
chmod +x \$RPM_BUILD_ROOT/%{_bindir}/%{name}

%clean
rm -rf \$RPM_BUILD_ROOT

%files
%{_bindir}/%{name}
%{_datadir}/java/%{name}.jar
%{_datadir}/icons/hicolor/scalable/apps/%{name}.svg
%{_datadir}/applications/%{name}.desktop
%license LICENSE
%license LICENSE-ORIGINAL
EOF

mkdir -p ~/rpmbuild/SOURCES/${NAME}-${VERSION}
cp shimelinux.sh ~/rpmbuild/SOURCES/${NAME}-${VERSION}/shimelinux.sh
cp shimelinux.desktop ~/rpmbuild/SOURCES/${NAME}-${VERSION}/shimelinux.desktop
cp icon.svg ~/rpmbuild/SOURCES/${NAME}-${VERSION}/icon.svg
cp build/libs/${NAME}-${VERSION}.jar ~/rpmbuild/SOURCES/${NAME}-${VERSION}/${NAME}-${VERSION}.jar
cp LICENSE ~/rpmbuild/SOURCES/${NAME}-${VERSION}/LICENSE
cp LICENSE-ORIGINAL ~/rpmbuild/SOURCES/${NAME}-${VERSION}/LICENSE-ORIGINAL
tar -czf ~/rpmbuild/SOURCES/${NAME}-${VERSION}.tar.gz -C ~/rpmbuild/SOURCES ${NAME}-${VERSION}

rpmbuild -bb ~/rpmbuild/SPECS/shimelinux.spec
mkdir -p distribution/build
mv ~/rpmbuild/RPMS/${BUILD_ARCH}/*.rpm distribution/build
rm -rf ~/rpmbuild
