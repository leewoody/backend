%define __jar_repack %{nil}

Name:          open-xchange-saml-core
BuildArch:     noarch
#!BuildIgnore: post-build-checks
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
BuildRequires: open-xchange-core
%if 0%{?rhel_version} && 0%{?rhel_version} == 600
BuildRequires: java7-devel
%else
%if (0%{?suse_version} && 0%{?suse_version} >= 1210)
BuildRequires: java-1_7_0-openjdk-devel
%else
BuildRequires: java-devel >= 1.7.0
%endif
%endif
Version:       @OXVERSION@
%define        ox_release 27
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Core package to support SAML authentication
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
This package contains the core bundles to support SAML as authentication
mechanism. It must always be complemented by a custom implementation that
performs several deployment-specific tasks.

Authors:
--------
    Open-Xchange

%package -n open-xchange-saml
Group:         Applications/Productivity
Summary:       Meta package to install necessary components to support SAML authentication
Requires:      open-xchange-saml-core >= @OXVERSION@
Requires:      open-xchange-saml-backend

%description -n open-xchange-saml
Install this package and its dependencies will install the necessary components to support SAML authentication.

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%post
if [ ${1:-0} -eq 2 ]; then
    # only when updating
    . /opt/open-xchange/lib/oxfunctions.sh

    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    PFILE=/opt/open-xchange/etc/saml.properties

    # SoftwareChange_Request-2673
    ox_add_property com.openexchange.saml.enableAutoLogin false $PFILE

    # SoftwareChange_Request-3548
    ox_add_property com.openexchange.saml.allowUnsolicitedResponses true /opt/open-xchange/etc/saml.properties
fi

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/etc/
%config(noreplace) /opt/open-xchange/etc/*
%dir /opt/open-xchange/etc/hazelcast
%config(noreplace) /opt/open-xchange/etc/hazelcast/*
%dir /opt/open-xchange/templates
/opt/open-xchange/templates/*

%files -n open-xchange-saml
%defattr(-,root,root)

%changelog
* Thu Apr 12 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-04-12 (4674)
* Tue Apr 03 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-04-03 (4642)
* Fri Mar 23 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-03-26 (4619)
* Mon Mar 12 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-03-12 (4602)
* Mon Feb 26 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-02-26 (4583)
* Mon Jan 29 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-02-05 (4555)
* Mon Jan 15 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-01-22 (4538)
* Tue Jan 02 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-01-08 (4516)
* Fri Dec 08 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for Patch 2017-12-11 (4473)
* Thu Nov 16 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-11-20 (4441)
* Tue Nov 14 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-11-15 (4448)
* Wed Oct 25 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-10-30 (4415)
* Mon Oct 23 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-10-29 (4425)
* Mon Oct 16 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-10-16 (4394)
* Wed Sep 27 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-10-02 (4377)
* Thu Sep 21 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-09-22 (4373)
* Tue Sep 12 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-09-18 (4354)
* Fri Sep 01 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-09-04 (4328)
* Mon Aug 14 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-08-21 (4318)
* Tue Aug 01 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-08-07 (4304)
* Mon Jul 17 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-07-24 (4285)
* Mon Jul 03 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-07-10 (4257)
* Wed Jun 21 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-06-26 (4233)
* Tue Jun 06 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-06-08 (4180)
* Fri May 19 2017 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.8.4 release
* Thu May 04 2017 Marcus Klein <marcus.klein@open-xchange.com>
Second preview of 7.8.4 release
* Mon Apr 03 2017 Marcus Klein <marcus.klein@open-xchange.com>
First preview of 7.8.4 release
* Fri Nov 25 2016 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.8.3 release
* Thu Nov 24 2016 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.8.3 release
* Thu Nov 24 2016 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.8.4 release
* Tue Nov 15 2016 Marcus Klein <marcus.klein@open-xchange.com>
Third preview for 7.8.3 release
* Sat Oct 29 2016 Marcus Klein <marcus.klein@open-xchange.com>
Second preview for 7.8.3 release
* Fri Oct 14 2016 Marcus Klein <marcus.klein@open-xchange.com>
First preview 7.8.3 release
* Tue Sep 06 2016 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.8.3 release
* Tue Jul 12 2016 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.8.2 release
* Wed Jul 06 2016 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.8.2 release
* Wed Jun 29 2016 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.8.2 release
* Thu Jun 16 2016 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.8.2 release
* Wed Apr 06 2016 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.8.2 release
* Wed Mar 30 2016 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.8.1 release
* Fri Mar 25 2016 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.8.1 release
* Tue Mar 15 2016 Marcus Klein <marcus.klein@open-xchange.com>
Fifth preview for 7.8.1 release
* Fri Mar 04 2016 Marcus Klein <marcus.klein@open-xchange.com>
Fourth preview for 7.8.1 release
* Sat Feb 20 2016 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.8.1 release
* Wed Feb 03 2016 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.8.1 release
* Tue Jan 26 2016 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.8.1 release
* Tue Oct 20 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-10-26 (2813)
* Mon Oct 19 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-10-30 (2818)
* Mon Oct 19 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-10-26 (2812)
* Mon Oct 12 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-10-23 (2806)
* Thu Oct 08 2015 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.8.1
* Fri Oct 02 2015 Marcus Klein <marcus.klein@open-xchange.com>
Sixth candidate for 7.8.0 release
* Wed Sep 30 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-10-12 (2784)
* Fri Sep 25 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-09-28 (2767)
* Fri Sep 25 2015 Marcus Klein <marcus.klein@open-xchange.com>
Fith candidate for 7.8.0 release
* Fri Sep 18 2015 Marcus Klein <marcus.klein@open-xchange.com>
Fourth candidate for 7.8.0 release
* Tue Sep 08 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-09-14 (2732)
* Mon Sep 07 2015 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.8.0 release
* Wed Sep 02 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-09-01 (2726)
* Mon Aug 24 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-08-24 (2674)
* Fri Aug 21 2015 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.8.0 release
* Mon Aug 17 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-08-12 (2671)
* Thu Aug 06 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-08-17 (2666)
* Wed Aug 05 2015 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.8.0
* Tue Aug 04 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-08-10 (2655)
* Mon Aug 03 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-08-03 (2650)
* Thu Jul 23 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-07-27 (2626)
* Wed Jul 15 2015 Steffen Templin <steffen.templin@open-xchange.com>
Build for patch 2015-07-20 (2614)
* Tue Jul 14 2015 Steffen Templin <steffen.templin@open-xchange.com>
Prepare for 7.6.2 hotfix
* Tue Mar 24 2015 Steffen Templin <steffen.templin@open-xchange.com>
initial packaging for SAML core package
* Wed Nov 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.8.0 release
