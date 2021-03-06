
%define name		trousers
%define version		0.3.7
%define release		1

%ifarch ppc64 x86_64 ia64 s390x
%define arch64 1
%define packages64 cairo-devel-64bit, glitz-devel-64bit, fontconfig-devel-64bit, freetype2-devel-64bit, xorg-x11-devel-64bit, libpng-devel-64bit
%define pkgconfig_path /opt/gnome/lib64/pkgconfig:/usr/lib64/pkgconfig
%endif

# RPM specfile for the trousers project

Name:		%{name}
Summary:	Implementation of the TCG's Software Stack v1.1 Specification
Version:	%{version}
Release:	%{release}
License:	CPL
Group:		Productivity/Security
Source:		%{name}-%{version}.tar.gz
Url:		http://www.sf.net/projects/trousers
BuildRoot:	%{_tmppath}/%{name}-%{version}-root
PreReq:		/usr/sbin/groupadd /usr/sbin/useradd /bin/chown
Requires:	gtk+ >= 2.0, openssl
BuildRequires:	gtk+ >= 2.0, openssl %{?arch64:,%{packages64}}

%description
TrouSerS is an implementation of the Trusted Computing Group's Software Stack
(TSS) specification. You can use TrouSerS to write applications that make use
of your TPM hardware. TPM hardware can create, store and use RSA keys
securely (without ever being exposed in memory), verify a platform's software
state using cryptographic hashes and more.

%package	devel
Summary:	TrouSerS header files and documentation
Group:		Productivity/Security
Requires:	trousers

%description	devel
Header files and man pages for use in creating Trusted Computing enabled
applications.

%prep
%setup

%build
%{?arch64:export PKG_CONFIG_PATH=%{pkgconfig_path}:$PKG_CONFIG_PATH}
./configure --prefix=/usr --libdir=%{_libdir}
make

%clean
[ "${RPM_BUILD_ROOT}" != "/" ] && [ -d ${RPM_BUILD_ROOT} ] && rm -rf ${RPM_BUILD_ROOT};

%pre
# add group tss
/usr/sbin/groupadd tss || {
	RC=$?
	case $RC in
		9) # group 'tss' already exists
			;;
		*) # some other error; fail
			echo "Couldn't create group 'tss'. Exiting."
			exit $RC;;
	esac
}
# add user tss
/usr/sbin/useradd -r tss || {
	RC=$?
	case $RC in
		9) # user 'tss' already exists
			;;
		*) # some other error; fail
			echo "Couldn't create user 'tss'. Exiting."
			exit $RC;;
	esac
}

%post
# create the default location for the persistent store files
if test -e %{_localstatedir}/tpm; then
	mkdir -p %{_localstatedir}/tpm
	/bin/chown tss:tss %{_localstatedir}/tpm
	/bin/chmod 1777 %{_localstatedir}/tpm
fi

# chown the daemon
/bin/chown tss:tss %{_sbindir}/tcsd

/sbin/ldconfig

%install
# This line keeps build machines from being affected
[ "${RPM_BUILD_ROOT}" != "/" ] && [ -d ${RPM_BUILD_ROOT} ] && rm -rf ${RPM_BUILD_ROOT};
mkdir -p ${RPM_BUILD_ROOT}
make install DESTDIR=${RPM_BUILD_ROOT}

%postun
/sbin/ldconfig
/usr/sbin/userdel tss
/usr/sbin/groupdel tss

# The files for the base package, 'trousers'
%files
%doc README AUTHORS
%attr(755, tss, tss) %{_sbindir}/tcsd
%{_libdir}/libtspi.la
%{_libdir}/libtspi.so*
%{_libdir}/libtddl.a
%config %attr(600, tss, tss) %{_sysconfdir}/tcsd.conf
%{_mandir}/man5/*
%{_mandir}/man8/*

# The files to be used by developers, 'trousers-devel'
%files		devel
%{_includedir}/tss/*.h
%{_includedir}/trousers/*.h
%{_mandir}/man3/Tspi_*

