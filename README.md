Mightycrawler - A queue based multithreaded web crawler written in Java
=======================================================================

Getting it
-----------

Warning: Mightycrawler is experimental. Some features are not fully implemented.

    git clone git://github.com/kristofd/mightycrawler.git
    cd mightycrawler
    mvn clean install assembly:single


Description
-----------

Mightycrawler is a multithreaded web crawler with reporting capabilities. The code is structured as a multi-stage queue system where downloading, parsing and storing content is done by separate thread pools. Various statistics about the site performance is gathered during crawling and put in a database. After completion, the database can be queried to produce various reports. The database is stored in RAM and is not persistent between runs.

**Warning!** Mightycrawler is indeed mighty and can generate a lot of requests in short time. Please do use the program properly.


Quick start
-----------

After downloading and building, edit `crawler.properties` to suit your needs.

Then run mightycrawler by typing

	java -jar mightycrawler-jar-with-dependencies.jar [myconfig.properties]


Configuration
-------------

All options for running mightycrawler are given in the `crawler.properties` file:

* `startURL`: Where to start crawling. Host names must end with a "/".

* `includeURL`: Restrict crawling to URLs matching this regex. Defaults to everything under startURL.

* `excludeURL`: Among the included URLs, exclude those matching this regex. Defaults to none (don't exclude any).

* `urlFile`: Visit all the URLs in this file (one URL per line). if provided, will override all of the above settings and turn off crawling.

* `extract`: Extract links from content types matching this regex. Defaults to none (if nothing is specified).

* `link`: Consinder content captured by this regex as links to resources. Defaults to none (if nothing is specified).

* `store`: Download and save to disk all content types matching this regex. Defaults to none (if nothing is specified).

* `userAgent`: The user agent the crawler reports to be.

* `defaultEncoding`: Use this encoding when storing content unless the content-type in the HTTP response header specifies otherwise.

* `proxyHost`: Host name to a proxy server. Defaults to not using any proxy (if nothing is specified).

* `proxyPort`: Port number to connect to the proxy, if any. Defaults to 8080 (if nothing is specified).

* `proxyUsername`: Username to authenticate against the proxy, if any. NOTE: Experimental.

* `proxyPassword`: Password to authenticate against the proxy, if any. NOTE: Experimental.

* `downloadThreads`: Number of threads when getting content from server.

* `parseThreads`: Number of threads for parsing content, ie scanning for URLs.

* `saveThreads`: Number of threads used for saving content to disk.

* `maxPages`: Stop after downloading this number of pages.

* `maxRecursion`: Stop when reaching this recursion level.

* `downloadDelay`: For each resource, pause of this number of seconds before downloading.

* `responseTimeout`: Wait this number of seconds for a server response before continuing.

* `crawlerTimeout`: Stop crawling if no new URLs are discovered within this number of seconds.

* `outputDirectory`: Where to put the downloaded web pages. MANDATORY.

* `reportDirectory`: Where to put download statistics. MANDATORY.

* `reportSQL`: SQL statements that are run against the crawler database after completion. Output is sent to the filename specified. Syntax: `SQL1@reportfile1.txt|SQL2@reportfile2.txt|...`


Tables in the crawler database
------------------------------

    DOWNLOADS
    ---------
    url VARCHAR(4095)
    http_code INTEGER default 0
    response_time INTEGER default 0
    downloaded_at DATETIME default NOW
    downladed BOOLEAN

    LINKS
    -----
    url_from VARCHAR(4095)
    url_to VARCHAR(4095)


TODO
-----------

* Proxy authentication support