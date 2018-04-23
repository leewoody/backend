%define __jar_repack %{nil}

Name:          open-xchange-filestore-s3
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
Summary:       Filestore implementation storing files in a S3 API compatible storage
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
This package installs the OSGi bundles needed for storing files in a S3 API compatible storage.

Authors:
--------
    Open-Xchange

%prep
%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%post
. /opt/open-xchange/lib/oxfunctions.sh
ox_update_permissions /opt/open-xchange/etc/filestore-s3.properties root:open-xchange 640
if [ ${1:-0} -eq 2 ]; then
    # only when updating
    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'
    PFILE=/opt/open-xchange/etc/filestore-s3.properties

    # SoftwareChange_Request-2061
    ox_add_property com.openexchange.filestore.s3.[filestoreID].bucketName "" $PFILE
    ox_add_property com.openexchange.filestore.s3.[filestoreID].pathStyleAccess true $PFILE

    # SoftwareChange_Request-2581
    ox_add_property com.openexchange.filestore.s3.[filestoreID].signerOverride S3SignerType $PFILE

    # SoftwareChange_Request-3477
    ox_add_property com.openexchange.filestore.s3.[filestoreID].chunkSize 5MB $PFILE
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
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/filestore-s3.properties
%config(noreplace) /opt/open-xchange/etc/*

%changelog
* Thu Apr 12 2018 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2018-04-12 (4674)
* Tue Apr 03 2018 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2018-04-03 (4642)
* Fri Mar 23 2018 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2018-03-26 (4619)
* Mon Mar 12 2018 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2018-03-12 (4602)
* Mon Feb 26 2018 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2018-02-26 (4583)
* Mon Jan 29 2018 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2018-02-05 (4555)
* Mon Jan 15 2018 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2018-01-22 (4538)
* Tue Jan 02 2018 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2018-01-08 (4516)
* Fri Dec 08 2017 Markus Wagner <markus.wagner@open-xchange.com>
Build for Patch 2017-12-11 (4473)
* Thu Nov 16 2017 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2017-11-20 (4441)
* Tue Nov 14 2017 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2017-11-15 (4448)
* Wed Oct 25 2017 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2017-10-30 (4415)
* Mon Oct 23 2017 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2017-10-29 (4425)
* Mon Oct 16 2017 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2017-10-16 (4394)
* Wed Sep 27 2017 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2017-10-02 (4377)
* Thu Sep 21 2017 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2017-09-22 (4373)
* Tue Sep 12 2017 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2017-09-18 (4354)
* Fri Sep 01 2017 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2017-09-04 (4328)
* Mon Aug 14 2017 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2017-08-21 (4318)
* Tue Aug 01 2017 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2017-08-07 (4304)
* Mon Jul 17 2017 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2017-07-24 (4285)
* Mon Jul 03 2017 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2017-07-10 (4257)
* Wed Jun 21 2017 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2017-06-26 (4233)
* Tue Jun 06 2017 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2017-06-08 (4180)
* Fri May 19 2017 Markus Wagner <markus.wagner@open-xchange.com>
First candidate for 7.8.4 release
* Thu May 04 2017 Markus Wagner <markus.wagner@open-xchange.com>
Second preview of 7.8.4 release
* Mon Apr 03 2017 Markus Wagner <markus.wagner@open-xchange.com>
First preview of 7.8.4 release
* Fri Nov 25 2016 Markus Wagner <markus.wagner@open-xchange.com>
Second release candidate for 7.8.3 release
* Thu Nov 24 2016 Markus Wagner <markus.wagner@open-xchange.com>
First release candidate for 7.8.3 release
* Thu Nov 24 2016 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.8.4 release
* Tue Nov 15 2016 Markus Wagner <markus.wagner@open-xchange.com>
Third preview for 7.8.3 release
* Sat Oct 29 2016 Markus Wagner <markus.wagner@open-xchange.com>
Second preview for 7.8.3 release
* Fri Oct 14 2016 Markus Wagner <markus.wagner@open-xchange.com>
First preview 7.8.3 release
* Tue Sep 06 2016 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.8.3 release
* Tue Jul 12 2016 Markus Wagner <markus.wagner@open-xchange.com>
Second candidate for 7.8.2 release
* Wed Jul 06 2016 Markus Wagner <markus.wagner@open-xchange.com>
First candidate for 7.8.2 release
* Wed Jun 29 2016 Markus Wagner <markus.wagner@open-xchange.com>
Second candidate for 7.8.2 release
* Thu Jun 16 2016 Markus Wagner <markus.wagner@open-xchange.com>
First candidate for 7.8.2 release
* Wed Apr 06 2016 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.8.2 release
* Wed Mar 30 2016 Markus Wagner <markus.wagner@open-xchange.com>
Second candidate for 7.8.1 release
* Fri Mar 25 2016 Markus Wagner <markus.wagner@open-xchange.com>
First candidate for 7.8.1 release
* Tue Mar 15 2016 Markus Wagner <markus.wagner@open-xchange.com>
Fifth preview for 7.8.1 release
* Fri Mar 04 2016 Markus Wagner <markus.wagner@open-xchange.com>
Fourth preview for 7.8.1 release
* Sat Feb 20 2016 Markus Wagner <markus.wagner@open-xchange.com>
Third candidate for 7.8.1 release
* Wed Feb 03 2016 Markus Wagner <markus.wagner@open-xchange.com>
Second candidate for 7.8.1 release
* Tue Jan 26 2016 Markus Wagner <markus.wagner@open-xchange.com>
First candidate for 7.8.1 release
* Tue Oct 20 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-10-26 (2813)
* Mon Oct 19 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-10-30 (2818)
* Mon Oct 19 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-10-26 (2812)
* Mon Oct 12 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-10-23 (2806)
* Thu Oct 08 2015 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.8.1
* Fri Oct 02 2015 Markus Wagner <markus.wagner@open-xchange.com>
Sixth candidate for 7.8.0 release
* Wed Sep 30 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-10-12 (2784)
* Fri Sep 25 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-09-28 (2767)
* Fri Sep 25 2015 Markus Wagner <markus.wagner@open-xchange.com>
Fith candidate for 7.8.0 release
* Fri Sep 18 2015 Markus Wagner <markus.wagner@open-xchange.com>
Fourth candidate for 7.8.0 release
* Tue Sep 08 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-09-14 (2732)
* Mon Sep 07 2015 Markus Wagner <markus.wagner@open-xchange.com>
Third candidate for 7.8.0 release
* Wed Sep 02 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-09-01 (2726)
* Mon Aug 24 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-08-24 (2674)
* Fri Aug 21 2015 Markus Wagner <markus.wagner@open-xchange.com>
Second candidate for 7.8.0 release
* Mon Aug 17 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-08-12 (2671)
* Thu Aug 06 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-08-17 (2666)
* Wed Aug 05 2015 Markus Wagner <markus.wagner@open-xchange.com>
First release candidate for 7.8.0
* Tue Aug 04 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-08-10 (2655)
* Mon Aug 03 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-08-03 (2650)
* Thu Jul 23 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-07-27 (2626)
* Wed Jul 15 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-07-20 (2614)
* Fri Jul 03 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-07-10
* Fri Jul 03 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-07-02 (2611)
* Fri Jul 03 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-06-29 (2578)
* Fri Jul 03 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-06-29 (2542)
* Wed Jun 24 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-06-29 (2569)
* Wed Jun 24 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-06-26 (2573)
* Wed Jun 10 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-06-08 (2539)
* Wed Jun 10 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-06-08 (2540)
* Mon May 18 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-05-26 (2521)
* Fri May 15 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-05-15 (2529)
* Fri May 08 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-05-12 (2478)
* Thu Apr 30 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-05-04 (2496)
* Thu Apr 30 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-05-04 (2497)
* Tue Apr 28 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-05-04 (2505)
* Fri Apr 24 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-09-09 (2495)
* Tue Apr 14 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-04-13 (2473)
* Wed Apr 08 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-04-13 (2474)
* Tue Apr 07 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2013-04-09 (2486)
* Thu Mar 26 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-03-30 (2459)
* Mon Mar 23 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-03-20
* Tue Mar 17 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-03-18
* Fri Mar 13 2015 Markus Wagner <markus.wagner@open-xchange.com>
Twelfth candidate for 7.6.2 release
* Fri Mar 06 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-03-16
* Fri Mar 06 2015 Markus Wagner <markus.wagner@open-xchange.com>
Eleventh candidate for 7.6.2 release
* Wed Mar 04 2015 Markus Wagner <markus.wagner@open-xchange.com>
Tenth candidate for 7.6.2 release
* Tue Mar 03 2015 Markus Wagner <markus.wagner@open-xchange.com>
Nineth candidate for 7.6.2 release
* Thu Feb 26 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-02-23
* Tue Feb 24 2015 Markus Wagner <markus.wagner@open-xchange.com>
Eighth candidate for 7.6.2 release
* Mon Feb 23 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-02-25
* Thu Feb 12 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-02-23
* Thu Feb 12 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-02-23
* Wed Feb 11 2015 Markus Wagner <markus.wagner@open-xchange.com>
Seventh candidate for 7.6.2 release
* Fri Feb 06 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-02-10
* Fri Feb 06 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-02-09
* Fri Jan 30 2015 Markus Wagner <markus.wagner@open-xchange.com>
Sixth candidate for 7.6.2 release
* Wed Jan 28 2015 Markus Wagner <markus.wagner@open-xchange.com>
Fifth candidate for 7.6.2 release
* Mon Jan 26 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-27
* Mon Jan 26 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-01-26
* Wed Jan 21 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-01-29
* Mon Jan 12 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-01-09
* Wed Jan 07 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-01-12
* Mon Jan 05 2015 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-01-12
* Tue Dec 30 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2015-01-12
* Tue Dec 16 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-12-10
* Fri Dec 12 2014 Markus Wagner <markus.wagner@open-xchange.com>
Fourth candidate for 7.6.2 release
* Mon Dec 08 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-12-15
* Mon Dec 08 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-12-10
* Mon Dec 08 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-12-15
* Fri Dec 05 2014 Markus Wagner <markus.wagner@open-xchange.com>
Third candidate for 7.6.2 release
* Thu Dec 04 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-12-09
* Tue Dec 02 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-12-03
* Tue Nov 25 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-12-01
* Mon Nov 24 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-12-01
* Mon Nov 24 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-12-01
* Fri Nov 21 2014 Markus Wagner <markus.wagner@open-xchange.com>
Second candidate for 7.6.2 release
* Thu Nov 20 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-12-01
* Wed Nov 19 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-11-21
* Tue Nov 18 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-11-20
* Mon Nov 10 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-11-17
* Mon Nov 10 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-11-17
* Mon Nov 10 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-11-17
* Wed Nov 05 2014 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.8.0 release
* Tue Nov 04 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-11-10
* Fri Oct 31 2014 Markus Wagner <markus.wagner@open-xchange.com>
First candidate for 7.6.2 release
* Tue Oct 28 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-11-03
* Mon Oct 27 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-30
* Fri Oct 24 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-11-04
* Fri Oct 24 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-11-03
* Fri Oct 24 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-22
* Fri Oct 17 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-24
* Tue Oct 14 2014 Markus Wagner <markus.wagner@open-xchange.com>
Fifth candidate for 7.6.1 release
* Fri Oct 10 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-20
* Fri Oct 10 2014 Markus Wagner <markus.wagner@open-xchange.com>
Fourth candidate for 7.6.1 release
* Fri Oct 10 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-20
* Thu Oct 09 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-13
* Tue Oct 07 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-09
* Tue Oct 07 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-09
* Tue Oct 07 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-10
* Thu Oct 02 2014 Markus Wagner <markus.wagner@open-xchange.com>
Third release candidate for 7.6.1
* Tue Sep 30 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-06
* Fri Sep 26 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-09-29
* Fri Sep 26 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-06
* Tue Sep 23 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-10-02
* Thu Sep 18 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-09-23
* Wed Sep 17 2014 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.6.2 release
* Tue Sep 16 2014 Markus Wagner <markus.wagner@open-xchange.com>
Second release candidate for 7.6.1
* Mon Sep 08 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-09-15
* Mon Sep 08 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-09-15
* Fri Sep 05 2014 Markus Wagner <markus.wagner@open-xchange.com>
First release candidate for 7.6.1
* Thu Aug 21 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-08-25
* Wed Aug 20 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-08-25
* Mon Aug 18 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-08-25
* Wed Aug 13 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-08-15
* Tue Aug 05 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-08-06
* Mon Aug 04 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-08-11
* Mon Aug 04 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-08-11
* Mon Jul 28 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-30
* Mon Jul 21 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-28
* Tue Jul 15 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-21
* Mon Jul 14 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-24
* Thu Jul 10 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-15
* Mon Jul 07 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-14
* Mon Jul 07 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-07
* Tue Jul 01 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-07-07
* Thu Jun 26 2014 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.6.1
* Mon Jun 23 2014 Markus Wagner <markus.wagner@open-xchange.com>
Seventh candidate for 7.6.0 release
* Fri Jun 20 2014 Markus Wagner <markus.wagner@open-xchange.com>
Sixth release candidate for 7.6.0
* Wed Jun 18 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-06-30
* Fri Jun 13 2014 Markus Wagner <markus.wagner@open-xchange.com>
Fifth release candidate for 7.6.0
* Fri Jun 13 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-06-23
* Thu Jun 05 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-06-16
* Fri May 30 2014 Markus Wagner <markus.wagner@open-xchange.com>
Fourth release candidate for 7.6.0
* Thu May 22 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-05-26
* Fri May 16 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-05-26
* Fri May 16 2014 Markus Wagner <markus.wagner@open-xchange.com>
Third release candidate for 7.6.0
* Wed May 07 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-05-05
* Mon May 05 2014 Markus Wagner <markus.wagner@open-xchange.com>
Second release candidate for 7.6.0
* Fri Apr 25 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-04-29
* Tue Apr 15 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-04-22
* Fri Apr 11 2014 Markus Wagner <markus.wagner@open-xchange.com>
First release candidate for 7.6.0
* Thu Apr 10 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-04-11
* Thu Apr 03 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-04-07
* Mon Mar 31 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-03-31
* Wed Mar 19 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-03-21
* Mon Mar 17 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-03-24
* Thu Mar 13 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-03-13
* Mon Mar 10 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2013-03-12
* Fri Mar 07 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2013-03-07
* Tue Mar 04 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2013-03-05
* Tue Feb 25 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-03-10
* Tue Feb 25 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-02-26
* Fri Feb 21 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-02-28
* Fri Feb 21 2014 Markus Wagner <markus.wagner@open-xchange.com>
Build for patch 2014-02-26
* Wed Feb 12 2014 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.6.0
* Fri Feb 07 2014 Markus Wagner <markus.wagner@open-xchange.com>
Sixth release candidate for 7.4.2
* Thu Feb 06 2014 Markus Wagner <markus.wagner@open-xchange.com>
Fifth release candidate for 7.4.2
* Tue Feb 04 2014 Markus Wagner <markus.wagner@open-xchange.com>
Fourth release candidate for 7.4.2
* Wed Dec 18 2013 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.4.2
* Thu Oct 10 2013 Markus Wagner <markus.wagner@open-xchange.com>
First sprint increment for 7.4.0 release
* Mon Oct 07 2013 Markus Wagner <markus.wagner@open-xchange.com>
prepare for 7.4.1
* Tue Jul 16 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.4.0
* Mon Apr 15 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.4.0
* Tue Apr 02 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second candidate for 7.2.0 release
* Tue Mar 26 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First release candidate for 7.2.0
* Fri Mar 15 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.2.0
* Tue Sep 11 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Initial release
