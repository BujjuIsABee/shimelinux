NAME='shimelinux'
VERSION='1.2.1'
RELEASE='1%{?dist}'
SUMMARY='An unofficial Linux port of Shimeji-ee Desktop Pet'
BUILD_ARCH='noarch'
LICENSE='BSD-3-CLAUSE'
SOURCE0='%{name}-%{version}.tar.gz'
REQUIRES=('bash' 'java >= 21' 'libappindicator-gtk3')

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
rm -rf \%{buildroot}
mkdir -p \%{buildroot}/%{_bindir}
mkdir -p \%{buildroot}/%{_datadir}/java
mkdir -p \%{buildroot}/%{_datadir}/icons/hicolor/scalable/apps
mkdir -p \%{buildroot}/%{_datadir}/applications

cp %{name}.sh \%{buildroot}/%{_bindir}/%{name}
cp %{name}-%{version}.jar \%{buildroot}/%{_datadir}/java/%{name}.jar
cp icon.svg \%{buildroot}/%{_datadir}/icons/hicolor/scalable/apps/shimelinux.svg
cp %{name}.desktop \%{buildroot}/%{_datadir}/applications/
chmod +x \%{buildroot}/%{_bindir}/%{name}

%clean
rm -rf \%{buildroot}

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
mv ~/rpmbuild/RPMS/${BUILD_ARCH}/*.rpm .
rm -rf ~/rpmbuild
