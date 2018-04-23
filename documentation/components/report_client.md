---
title: OX-Report Client
---

# Introduction
**Generating a report every month** is mandatory in order to have access to the maintenance updates available in the updates directory on software.open-xchange.com.

**You have been blocked already?**

**Don't panic**, you can still access <https://software.open-xchange.com/OX6/stable>, 
because that is open for everyone. So install the Report Client from stable instead 
of updates and once you're done, update to the latest version.

The Open-Xchange Report Client extension of the Open-Xchange Server
enables you to generate and send usage reports of your environment to
Open-Xchange. The report will contain information of how many contexts
and users have been created in the given Open-Xchange environment. This
article will guide you through the installation of the Open-Xchange
Report Client. It describes the setup of the software extension itself,
and which additional configurations need to be done to execute this
extension.

You will find further information at the Open-Xchange Frequent Asked Questions (FAQ) 

- [English](https://sdb.open-xchange.com/faq/70)
- [German](https://sdb.open-xchange.com/category/1/71)

# Installation on OX App Suite

## Debian GNU/Linux 7.0

Add the following entry to <code>/etc/apt/sources.list.d/open-xchange.list</code> if not already present:


 	deb https://software.open-xchange.com/products/appsuite/stable/backend/DebianWheezy/ /

	# if you have a valid maintenance subscription, please uncomment the 
	# following and add the ldb account data to the url so that the most recent
	# packages get installed
	# deb https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/DebianWheezy/ /

and run

	$ apt-get update 
	$ apt-get install open-xchange-report-client

## Debian GNU/Linux 8.0

Add the following entry to <code>/etc/apt/sources.list.d/open-xchange.list</code> if not already present:

 	deb https://software.open-xchange.com/products/appsuite/stable/backend/DebianJessie/ /
 	
	# if you have a valid maintenance subscription, please uncomment the 
	# following and add the ldb account data to the url so that the most recent
	# packages get installed
	# deb https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/DebianJessie/ /
	
and run

	$ apt-get update
	$ apt-get install open-xchange-report-client

## SUSE Linux Enterprise Server 11

Add the package repository using <code>zypper</code> if not already present:

	$ zypper ar https://software.open-xchange.com/products/appsuite/stable/backend/SLES11 ox

If you have a valid maintenance subscription, please run the following command and add the ldb account data to the url so that
the most recent packages get installed:

	$ zypper ar https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/SLES11 ox-updates
	
and run

	$ zypper ref
	$ zypper in open-xchange-report-client

## SUSE Linux Enterprise Server 12

Add the package repository using <code>zypper</code> if not already present:

	$ zypper ar https://software.open-xchange.com/products/appsuite/stable/backend/SLE_12 ox
	
If you have a valid maintenance subscription, please run the following command and add the ldb account data to the url so that the most recent packages get installed:
 
	$ zypper ar https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/appsuite/stable/backend/updates/SLES11 ox-updates

and run

	$ zypper ref
	$ zypper in open-xchange-report-client

## RedHat Enterprise Linux 6

Start a console and create a software repository file if not already present:

	$ vim /etc/yum.repos.d/ox.repo

	[ox]
	name=Open-Xchange
	baseurl=https://software.open-xchange.com/products/appsuite/stable/backend/RHEL6/	gpgkey=https://software.open-xchange.com/oxbuildkey.pub
	enabled=1
	gpgcheck=1
	metadata_expire=0m

	# if you have a valid maintenance subscription, please uncomment the 
	# following and add the ldb account data to the url so that the most recent
	# packages get installed
	# [ox-updates]
	# name=Open-Xchange Updates
	# baseurl=https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/RHEL6/
	# gpgkey=https://software.open-xchange.com/oxbuildkey.pub
	# enabled=1
	# gpgcheck=1
	# metadata_expire=0m

and run

	$ yum update
	$ yum install open-xchange-report-client
	
## RedHat Enterprise Linux 7

Start a console and create a software repository file if not already present:

	$ vim /etc/yum.repos.d/ox.repo

	[ox]
	name=Open-Xchange
	baseurl=https://software.open-xchange.com/products/appsuite/stable/backend/RHEL7/
	gpgkey=https://software.open-xchange.com/oxbuildkey.pub
	enabled=1
	gpgcheck=1
	metadata_expire=0m

	# if you have a valid maintenance subscription, please uncomment the 
	# following and add the ldb account data to the url so that the most recent
	# packages get installed
	# [ox-updates]
	# name=Open-Xchange Updates
	# baseurl=https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/RHEL7/
	# gpgkey=https://software.open-xchange.com/oxbuildkey.pub
	# enabled=1
	# gpgcheck=1
	# metadata_expire=0m

and run

	$ yum update
	$ yum install open-xchange-report-client

## CentOS 6

Start a console and create a software repository file if not already present:

	$ vim /etc/yum.repos.d/ox.repo

	[ox]
	name=Open-Xchange
	baseurl=https://software.open-xchange.com/products/appsuite/stable/backend/RHEL6/	gpgkey=https://software.open-xchange.com/oxbuildkey.pub
	enabled=1
	gpgcheck=1
	metadata_expire=0m

	# if you have a valid maintenance subscription, please uncomment the 
	# following and add the ldb account data to the url so that the most recent
	# packages get installed
	# [ox-updates]
	# name=Open-Xchange Updates
	# baseurl=https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/RHEL6/
	# gpgkey=https://software.open-xchange.com/oxbuildkey.pub
	# enabled=1
	# gpgcheck=1
	# metadata_expire=0m

and run

	$ yum update
	$ yum install open-xchange-report-client

## CentOS 7

Start a console and create a software repository file if not already present:

	$ vim /etc/yum.repos.d/ox.repo

	[ox]
	name=Open-Xchange
	baseurl=https://software.open-xchange.com/products/appsuite/stable/backend/RHEL7/
	gpgkey=https://software.open-xchange.com/oxbuildkey.pub
	enabled=1
	gpgcheck=1
	metadata_expire=0m

	# if you have a valid maintenance subscription, please uncomment the 
	# following and add the ldb account data to the url so that the most recent
	# packages get installed
	# [ox-updates]
	# name=Open-Xchange Updates
	# baseurl=https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/RHEL7/
	# gpgkey=https://software.open-xchange.com/oxbuildkey.pub
	# enabled=1
	# gpgcheck=1
	# metadata_expire=0m

and run

	$ yum update
	$ yum install open-xchange-report-client

# Configuration

A requirement to execute the Open-Xchange Report Client is to have your
Open-Xchange license key stored in one specific file on your
Open-Xchange installation. The latest version of our
[installation guide](Main\_Page\#Installation\_based\_on\_packages) 
documentation will automatically enable you to store your
license key on disk by using a new oxinstaller command.

If you want to use the report client on an already installed environment
you need to store your license key manually on disk. To do so create and
edit the following file on your Open-Xchange server:

	$ vim /opt/open-xchange/etc/common/licensekeys.properties

or on 6.22 or newer

	$ vim /opt/open-xchange/etc/licensekeys.properties

	com.openexchange.licensekey.1=PUT\_YOUR\_OPEN-XCHANGE\_LICENSE\_KEY\_HERE

If you are behind a firewall and the report client needs to be
configured using a HTTP proxy, please edit file:

	$ vim /opt/open-xchange/etc/groupware/reportclient.properties

or on 6.22 or newer

	$ vim /opt/open-xchange/etc/reportclient.properties

After editing this file accordingly to your proxy needs, try to start
the report client again.

# Using the Report tool

Now as the package has been correctly installed and you provided your
license key in the according properties file your are ready to launch
the report client to generate a report. By default (if no option is
given) the report client will display and send the generated report to
an Open-Xchange service on activation.open-xchange.com. Note that only
data that is displayed in the console will be transfered to Open-Xchange

	activation.open-xchange.com:443

## Report kinds

In general there are two kinds of reports. Since 7.8.0 the default
report has got the appsuite style format. If you would like to generate
and display/save/send the formerly used style you have to add the option
"-o" to every known parameter combination.

## Display data usage report

	$ /opt/open-xchange/sbin/report -d

If you want to know which data would be transfered, execute the report
client with the option "-d" (display\_only). If this option is given to
the report client no data will be send:

## Send data usage report

	$ /opt/open-xchange/sbin/report -s

If you don't want to have the report displayed in the console (which
might be the case for automated executions of the report client) execute
the report client with the option -s (send\_only). Now no report will be
displayed after the report has been sent to activation.open-xchange.com.

## Report performance and storage
With version 7.8.3 new properties are introduced.

* [com.openexchange.report.appsuite.fileStorage](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.report.appsuite.fileStorage)
 
 Describes the storage path for all report relevant data. Saving a report will place a JSON-Version of the report in that folder.

* [com.openexchange.report.appsuite.maxChunkSize](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.report.appsuite.maxChunkSize=200)
	
 This property enables the client to store parts of the report on hard drive to keep memory usage small. A chunk is a CapabilitySet.
 The stored parts are combined into a single .report file and then deleted when the report is finished. The .report file is not deleted automatically.

* [com.openexchange.report.appsuite.maxThreadPoolSize](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.report.appsuite.maxThreadPoolSize)
	
 The report will use multithreading for faster processing. Therefore the user can edit the threadpoolsize by editing this property value. Each thread is processing the needed values from a schema. If the threadpool is smaller then the schemas in the database, the threads are queued.

* [com.openexchange.report.appsuite.threadPriority](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.report.appsuite.threadPriority)
	
 This property determines the used threads priotity. It can range from 1 (lowest) to 10 (highest).



## Available options

	$ /opt/open-xchange/sbin/report -h

lists all available options:

	Usage: report 
	 -h,--help                                        Prints a help text          
    --environment                                     Show info about commandline environment
    --nonl                                            Remove all newlines (\n) from output
    --responsetimeout <responsetimeout>               response timeout in seconds for reading response from the backend (default 0s; infinite)
    -H,--host <host>                                  specifies the host          
    -T,--timeout <timeout>                            timeout in seconds for the connection creation to the backend (default 15s)
    -J,--jmxauthuser <jmxauthuser>                    jmx username (required when jmx authentication enabled)
    -P,--jmxauthpassword <jmxauthpassword>            jmx username (required when jmx authentication enabled)
    -s,--sendonly                                     Send report without displaying it (Disables default)
    -d,--displayonly                                  Display report without sending it (Disables default)
    -c,--csv                                          Show output as CSV          
    -a,--advancedreport                               Run an advanced report (could take some time with a lot of contexts)
    -f,--savereport                                   Save the report as JSON String instead of sending it
    -b,--showaccesscombination <showaccesscombination>  Show access combination for bitmask
    -e,--run-appsuite-report                          Schedule an appsuite style report. Will print out the reports UUID or, if a report is being generated, the UUID of the pending report
    -t,--report-type <report-type>                    The type of the report to run. Leave this off for the 'default' report. 'Known reports next to 'default': 'extended'
    --inspect-appsuite-reports                     	  Prints information about currently running reports
    --cancel-appsuite-reports                      	  Cancels pending reports     
    -g,--get-appsuite-report                          Retrieve the report that was generated, can (and should) be combined with the options for sending, displaying or saving the report
    -x,--run-and-deliver-report                       Create a new report and send it immediately. Note: This command will run until the report is finished, and that could take a while. Can (and should) be combined with the options for sending, displaying or saving the report 
    -o,--run-and-deliver-old-report                   Run old report type. Used to have a backward compatibility.
    -S,--timeframe-start <timeframe-start>            Set the starting date of the timeframe in format: dd.mm.yyyy
    -E,--timeframe-end <timeframe-end>                Set the ending date of the timeframe in format: dd.mm.yyyy. If start date is set and this parameter not, the current Date is taken as timeframe end.

## Known report types in 7.8.2

**default**

- The output equals the reports in appsuite style. Additionally a beginning and ending timeframe can be set. This report type will be generated, if no other report type is set with option <code>-t</code>.

**extended**

- The extended output adds drive metrics, client logins, quota and context metrics for each capability-set and the whole deployment.

## Explanation of the appsuite report console output

### Report-type: default

This report type will be generated even without a selected type.
<pre>
 $ /opt/open-xchange/sbin/report -d
Starting the Open-Xchange report client. Note that the report generation may take a little while.

11:31:30, a7ee832bb74b41f9bf5f7451ba0994fc: 0/7 (0,00 %) 
UUID: a7ee832bb74b41f9bf5f7451ba0994fc
Type: default
Total time: 47 milliseconds
Avg. time per context: 6 milliseconds
Report was finished: Tue Jun 07 11:31:30 CEST 2016

------ report -------
{
  "configs" : {
    "com.openexchange.mail.adminMailLoginEnabled" : "false",
    "com.openexchange.report.appsuite.ReportService" : "LocalReportService"
  },
  "total" : {
    "timeframe" : {
      "start" : 1433669490406,
      "end" : 1465291890406
    },
    "users-disabled" : 1,
    "Context-users-max" : 4,
    "guests" : 1,
    "links" : 2,
    "contexts" : 7,
    "Context-users-min" : 1,
    "Context-users-avg" : 2,
    "users" : 18,
    "contexts-disabled" : 1,
    "report-format" : "appsuite-short"
  },
  "clientlogincount" : {
    "appsuite" : "3",
    "olox2" : "0",
    "caldav" : "0",
    "usm-eas" : "0",
    "mobileapp" : "0",
    "ox6" : "0",
    "carddav" : "0"
  },
  "macdetail" : {
    "capabilitySets" : [ {
      "client-list" : {
        "open-xchange-appsuite" : 5,
        "123" : 1,
        "olox2" : 1,
        "mobileapp" : 1,
        "OpenXchange.HTTPClient.OXAddIn" : 2,
        "com.openexchange.mobileapp" : 3
      },
      "capabilities" : [ "active_sync", "autologin", "boxcom", "caldav", "calendar", "carddav", "client-onboarding", "collect_email_addresses", "conflict_handling", "contacts", "delegate_tasks", "dev", "document_preview", "drive", "edit_group", "edit_public_folders", "edit_resource", "emclient", "filestore", "freebusy", "gab", "google", "groupware", "guard", "guard-drive", "guard-mail", "ical", "infostore", "invite_guests", "messenger", "messenger-group", "mobility", "msliveconnect", "multiple_mail_accounts", "oauth", "olox20", "participants_dialog", "pim", "pop3", "portal", "presenter", "publication", "read_create_shared_folders", "remote_presenter", "rt", "search", "share_links", "share_mail_attachments", "spreadsheet", "subscription", "tasks", "testoauthservice", "text", "twitter", "unified-mailbox", "usm", "vcard", "webdav", "webdav_xml", "webmail" ],
      "admin" : 0,
      "contexts" : 6,
      "total" : 11,
      "Context-users-max" : 3,
      "quota" : 1048576,
      "guests" : 1,
      "disabled" : 1,
      "links" : 2,
      "Context-users-min" : 1,
      "Context-users-avg" : 1
    }, {
      "client-list" : {
        "open-xchange-appsuite" : 2
      },
      "total" : 7,
      "capabilities" : [ "active_sync", "autologin", "boxcom", "caldav", "calendar", "carddav", "client-onboarding", "collect_email_addresses", "conflict_handling", "contacts", "delegate_tasks", "dev", "document_preview", "drive", "edit_group", "edit_public_folders", "edit_resource", "emclient", "filestore", "freebusy", "gab", "google", "groupware", "guard", "guard-drive", "guard-mail", "ical", "infostore", "invite_guests", "messenger", "messenger-group", "mobility", "msliveconnect", "multiple_mail_accounts", "oauth", "olox20", "participants_dialog", "pim", "pop3", "portal", "presenter", "publication", "read_create_shared_folders", "remote_presenter", "rt", "search", "share_links", "share_mail_attachments", "spreadsheet", "subscription", "tasks", "testoauthservice", "text", "twitter", "unified-mailbox", "usm", "vcard", "webdav", "webdav_xml" ],
      "Context-users-max" : 1,
      "quota" : 1048576,
      "guests" : 0,
      "admin" : 7,
      "disabled" : 0,
      "links" : 0,
      "contexts" : 7,
      "Context-users-min" : 1,
      "Context-users-avg" : 1
    } ]
  },
  "clientlogincountyear" : {
    "appsuite" : "7",
    "olox2" : "2",
    "caldav" : "0",
    "usm-eas" : "0",
    "mobileapp" : "3",
    "ox6" : "0",
    "carddav" : "0"
  },
  "uuid" : "a7ee832bb74b41f9bf5f7451ba0994fc",
  "reportType" : "default",
  "timestamps" : {
    "start" : 1465291890406,
    "stop" : 1465291890453
  },
  "version" : {
    "version" : "7.8.2-Rev0",
    "buildDate" : "develop"
  }
}
------ end -------
</pre>

**macdetail:**

- detailed information about existing module access combinations and
    its usage 
    
**total:**

- accumulated user and disabled users, guests, links (anonymous share), the reports timeframe, if not set via arguments the last year is considered and context metrics (min, max, average, disabled and total)

**clientlogincountyear:**

- number of client logins for the last year (1 year back from current
    date)

**clientlogincount:**

- number of client logins for the last month

**Additional Information:** Both login count values respect only the last client login of a user with a distinct client like olox2 or appsuite. If a user logs in with olox2 and appsuite during the last 30 days, he will be considered in both counts for the clients and also in both timeframes (clientlogincountyear & clientlogincount). If his last login with a client is further away in the past but still in the timeframe of the last year (365 days in the past), he will only be considered in the yearly counter (clientlogincountyear) and not in the last monthly counter (clientlogincount).

**uuid:**

- a unique id for the report

**reportType:**

- given name for that report

**timestamps:**

- timestamps of the start and end time of the report

**versions:**

- version and build date of the server 

**configs:**

- server configuration (currently only for setting 'com.openexchange.mail.adminMailLoginEnabled')
- What service generated this report, hazelcast or local.

**capabilitySets**

- **client-list**: What clients have been used by the users and how often in the considered timeframe
- **context-users-...**:context metrics (min, max, average, disabled and total)
- **disabled**: the number of disabled users
- **links**: number of links created by users
- **guests**: number of guests, invited by users
- **total**: total number of users
- **admin**: total number of admins
- **contexts**: total number of contexts
- **quota**: cumulated quota in byte



### Report-type: extended

<pre>
Starting the Open-Xchange report client. Note that the report generation may take a little while.

UUID: e48819025b5043a580b1f8d0d2ac2f2f
Type: extended
Total time: 198 milliseconds
Avg. time per context: 28 milliseconds
Report was finished: Tue Jun 07 11:46:05 CEST 2016

------ report -------
{
  "configs" : {
    "com.openexchange.mail.adminMailLoginEnabled" : "false",
    "com.openexchange.report.appsuite.ReportService" : "LocalReportService"
  },
  "total" : {
    "timeframe" : {
      "start" : 1433670365550,
      "end" : 1465292765550
    },
    "users-disabled" : 1,
    "Context-users-max" : 4,
    "guests" : 1,
    "links" : 2,
    "contexts" : 7,
    "Context-users-min" : 1,
    "Context-users-avg" : 2,
    "drive-total" : {
      "file-size-min" : 141,
      "file-size-max" : 30018,
      "file-size-avg" : 9060,
      "file-size-total" : 81547,
      "storage-use-min" : 4708,
      "storage-use-max" : 35568,
      "storage-use-avg" : 20386,
      "storage-use-total" : 81547,
      "file-count-overall-min" : 1,
      "file-count-overall-max" : 6,
      "file-count-overall-avg" : 2,
      "file-count-overall-total" : 9,
      "file-count-in-timerange-min" : 1,
      "file-count-in-timerange-max" : 6,
      "file-count-in-timerange-avg" : 2,
      "file-count-in-timerange-total" : 9,
      "external-storages-min" : 0,
      "external-storages-max" : 1,
      "external-storages-avg" : 1,
      "external-storages-total" : 1,
      "external-storages-users" : 1,
      "distinct-files-total" : 8,
      "quota-usage-percent-min" : 0,
      "quota-usage-percent-avg" : 1,
      "quota-usage-percent-max" : 6,
      "users" : 4,
      "mime-type-text/xml" : 2,
      "mime-type-application/octet-stream" : 3,
      "mime-type-text/html" : 1,
      "mime-type-text/plain" : 3
    },
    "users" : 18,
    "contexts-disabled" : 1,
    "report-format" : "appsuite-short"
  },
  "macdetail" : {
    "capabilitySets" : [ {
      "drive-user" : {
        "file-size-min" : 141,
        "file-size-max" : 30018,
        "file-size-avg" : 8786,
        "file-size-total" : 70294,
        "storage-use-min" : 4708,
        "storage-use-max" : 35568,
        "storage-use-avg" : 23431,
        "storage-use-total" : 70294,
        "file-count-overall-min" : 1,
        "file-count-overall-max" : 6,
        "file-count-overall-avg" : 2,
        "file-count-overall-total" : 8,
        "file-count-in-timerange-min" : 1,
        "file-count-in-timerange-max" : 6,
        "file-count-in-timerange-avg" : 2,
        "file-count-in-timerange-total" : 8,
        "external-storages-min" : 1,
        "external-storages-max" : 1,
        "external-storages-avg" : 1,
        "external-storages-total" : 1,
        "external-storages-users" : 1,
        "distinct-files-total" : 7,
        "quota-usage-percent-min" : 0,
        "quota-usage-percent-avg" : 1,
        "quota-usage-percent-max" : 6,
        "users" : 3
      },
      "client-list" : {
        "open-xchange-appsuite" : 5,
        "123" : 1,
        "olox2" : 1,
        "mobileapp" : 1,
        "OpenXchange.HTTPClient.OXAddIn" : 2,
        "com.openexchange.mobileapp" : 3
      },
      "capabilities" : [ "active_sync", "autologin", "boxcom", "caldav", "calendar", "carddav", "client-onboarding", "collect_email_addresses", "conflict_handling", "contacts", "delegate_tasks", "dev", "document_preview", "drive", "edit_group", "edit_public_folders", "edit_resource", "emclient", "filestore", "freebusy", "gab", "google", "groupware", "guard", "guard-drive", "guard-mail", "ical", "infostore", "invite_guests", "messenger", "messenger-group", "mobility", "msliveconnect", "multiple_mail_accounts", "oauth", "olox20", "participants_dialog", "pim", "pop3", "portal", "presenter", "publication", "read_create_shared_folders", "remote_presenter", "rt", "search", "share_links", "share_mail_attachments", "spreadsheet", "subscription", "tasks", "testoauthservice", "text", "twitter", "unified-mailbox", "usm", "vcard", "webdav", "webdav_xml", "webmail" ],
      "admin" : 0,
      "contexts" : 6,
      "drive-overall" : {
        "mime-type-application/octet-stream" : 3,
        "mime-type-text/html" : 1,
        "mime-type-text/plain" : 3,
        "mime-type-text/xml" : 1
      },
      "total" : 11,
      "Context-users-max" : 3,
      "quota" : 1048576,
      "guests" : 1,
      "disabled" : 1,
      "links" : 2,
      "Context-users-min" : 1,
      "Context-users-avg" : 1
    }, {
      "drive-user" : {
        "file-size-min" : 11253,
        "file-size-max" : 11253,
        "file-size-avg" : 11253,
        "file-size-total" : 11253,
        "storage-use-min" : 11253,
        "storage-use-max" : 11253,
        "storage-use-avg" : 11253,
        "storage-use-total" : 11253,
        "file-count-overall-min" : 1,
        "file-count-overall-max" : 1,
        "file-count-overall-avg" : 1,
        "file-count-overall-total" : 1,
        "file-count-in-timerange-min" : 1,
        "file-count-in-timerange-max" : 1,
        "file-count-in-timerange-avg" : 1,
        "file-count-in-timerange-total" : 1,
        "external-storages-min" : 0,
        "external-storages-max" : 0,
        "external-storages-avg" : 0,
        "external-storages-total" : 0,
        "external-storages-users" : 0,
        "distinct-files-total" : 1,
        "quota-usage-percent-min" : 0,
        "quota-usage-percent-avg" : 1,
        "quota-usage-percent-max" : 6,
        "users" : 1
      },
      "client-list" : {
        "open-xchange-appsuite" : 2
      },
      "capabilities" : [ "active_sync", "autologin", "boxcom", "caldav", "calendar", "carddav", "client-onboarding", "collect_email_addresses", "conflict_handling", "contacts", "delegate_tasks", "dev", "document_preview", "drive", "edit_group", "edit_public_folders", "edit_resource", "emclient", "filestore", "freebusy", "gab", "google", "groupware", "guard", "guard-drive", "guard-mail", "ical", "infostore", "invite_guests", "messenger", "messenger-group", "mobility", "msliveconnect", "multiple_mail_accounts", "oauth", "olox20", "participants_dialog", "pim", "pop3", "portal", "presenter", "publication", "read_create_shared_folders", "remote_presenter", "rt", "search", "share_links", "share_mail_attachments", "spreadsheet", "subscription", "tasks", "testoauthservice", "text", "twitter", "unified-mailbox", "usm", "vcard", "webdav", "webdav_xml" ],
      "admin" : 7,
      "contexts" : 7,
      "drive-overall" : {
        "mime-type-text/xml" : 1
      },
      "total" : 7,
      "Context-users-max" : 1,
      "quota" : 1048576,
      "guests" : 0,
      "disabled" : 0,
      "links" : 0,
      "Context-users-min" : 1,
      "Context-users-avg" : 1
    } ]
  },
  "uuid" : "e48819025b5043a580b1f8d0d2ac2f2f",
  "reportType" : "extended",
  "timestamps" : {
    "start" : 1465292765550,
    "stop" : 1465292765748
  },
  "version" : {
    "version" : "7.8.2-Rev0",
    "buildDate" : "develop"
  }
}
------ end -------
</pre>

Because this report simply enhances the default reports information, only the added components are described.

**total**

* **drive-total** (on deployment level)
	*  **file-size-...**: file-size values
	*  **storage-use-...**: storage usage values
	*  **file-count-overall-...**: overall file count values
	*  **file-count-in-timerange-...**: file count values inside the considered timeframe
	*  **external-storages-...**: exernal storage usage values
	*  **distinct-files-total**: how many files are on this deployment, disregarding the versions
	*  **quota-usage-percent-...**: how much quota is used in percent values
	*  **users**: overall drive users
	*  **ime-type-...**: What mime-types and how many of them are there

**capabilitySets**

* **drive-user**: The drive metrics for this capability set users
	*  **file-size-...**: file-size values
	*  **storage-use-...**: storage usage values
	*  **file-count-overall-...**: overall file count values
	*  **file-count-in-timerange-...**: file count values inside the considered timeframe
	*  **external-storages-...**: exernal storage usage values
	*  **distinct-files-total**: how many files are on this deployment, disregarding the versions
	*  **quota-usage-percent-...**: how much quota is used in percent values
	*  **users** : overall drive users

* **drive-overall**: All file mime-types and their amount.
   	
## Explanation of the old console output

	$ /opt/open-xchange/sbin/report -o -d 
	Starting the Open-Xchange report client. Note that the report generation may take a little while.

	module version\
	admin 6.20.5 Rev1 groupware 6.20.5 Rev1

	contexts users guests links 
	5 		 19    22     10

	mac 	  count adm disabled 
	268435455 6 	1 	0
	237044501 48    0 	0
	5 		  2 	2 	0

	key 										value 
	com.openexchange.mail.adminMailLoginEnabled true

	usmeas olox2 mobileapp carddav caldav 
	1 	   0 	 0 		   0 	   0

	usmeasyear olox2year mobileappyear carddavyear caldavyear 
	4 		   12 		 7 			   11 		   10

**contexts:**

- total number of contexts

**users:**

- total number of users

**guests:**

- total number of guests

**links:**

- total number of links shared to anonymous users

**mac:**

- decimal value of the module access. For conversion decimal to human readable permission please use report -b as described below

**count:**

- amount of users with this module access

**adm:**

- amount of admin users with this specific module access

**disabled:**

- amount of users that are disabled

**key:**

- key of the configuration property

**value:**

- value of the configuration property

The last rows show the amount of users that did login to Open-Xchange
within the ''last 30 days'' and ''the last year'' using the specified
OXtender/service.

If you want to know the access permissions of a specific <tt>mac</tt>,
run e.g.

	$ /opt/open-xchange/sbin/report -b 237044501

**usmeas:**

- using mobile phone via active sync (OXtender for Business Mobility) within the last 30 days

**olox2:**

- using OXtender 2 for Microsoft Outlook within the last 30 days

**mobileapp:**

- using Open-Xchange Mobile Web Interface within the last 30 days

**carddav/caldav:**

- using the CalDAV/CardDAV feature within the last 30 days

**usmeasyear:**

- using mobile phone via active sync (OXtender for Business Mobility) within the last year

**olox2:**

- using OXtender 2 for Microsoft Outlook within the last year

**mobileapp:**

- using Open-Xchange Mobile Web Interface within the last year

**carddav/caldav:**

- using the CalDAV/CardDAV feature within the last year

**Example:** a value of 7 below olox2 means, that within the last 30 days, 7 different users used the OXtender 2 for Microsoft Outlook to connect to Open-Xchange.

# Automatic reports

Creating a cron entry which will automatically execute the report client
once a week saves a lot of work. To create this cron entry execute:

	$ vim /etc/cron.d/open-xchange\_report

	0 3 \* \* 7 open-xchange /opt/open-xchange/sbin/report -s -x 1> /dev/null 2>&1
