Mightycrawler - A queue based multithreaded web crawler written in Java
=======================================================================

Getting it
-----------

Warning: Mightycrawler is experimental. Some features are not fully implemented.

    git clone git://github.com/kristofd/mightycrawler.git
    cd mightycrawler
    mvn clean package


Description
-----------

Mightycrawler is a multithreaded web crawler with reporting capabilities. The code is structured as a multi-stage queue system where downloading, parsing and storing content is done by separate thread pools. Various statistics about the site performance is gathered during crawling and put in a database. After completion, the database can be queried to produce various reports. The database is stored in RAM and is not persistent between runs.

**Warning!** Mightycrawler is indeed mighty and can generate a lot of requests in short time. Please do use the program properly.


Quick start
-----------

After downloading and building, edit crawler.properties to suit your needs.

Then run mightycrawler by typing

	java -jar mightycrawler.jar [myconfig.properties]


Configuration
-------------

All options for running mightycrawler are given in the crawler.properties file:

* startURL: Where to start crawling. URL must end with a "/".

* includeURL: Restrict crawling and downloading to URLs matching this regex. Defaults to everything under startURL.

* excludeURL: Among the included URLs, exclude any matching this regex. Defaults to none (don't exclude any).

* extract: Restrict link extraction to content types matching this regex. Defaults to none (if nothing is specified).

* link: Consinder content matching this regex as a link to a resource. Defaults to none (if nothing is specified).

* store: Restrict downloading to content types matching this regex. Defaults to none (if nothing is specified).

* userAgent: The user agent the crawler identifies itself as.

* defaultPage: When storing pages to disk, URLs ending with "/" are appended with the file name given here. The URL would else map to a directory name - making storage impossible.

* defaultEncoding: Use this encoding to store content unless content-type is specified in the HTTP response header.

* downloadThreads: Number of threads for downloading.

* parseThreads: Number of threads for parsing content, looking for URLs.

* saveThreads: Number of threads used for saving content to disk.

* maxPages: Stop after downloading this number of pages.

* maxRecursion: Stop when reaching this recursion level.

* downloadDelay: Each download thread waits this number of seconds before it starts.

* crawlerTimeout: Stop crawling if no new URLs are discovered within this number of seconds.

* outputDirectory: Where to put the downloaded web pages. MANDATORY.

* reportDirectory: Where to put download statistics. MANDATORY.

* reportSQL: SQL statements that are run against the crawler database after completion. Output is sent to the filename specified. Syntax: SQL1@reportfile1.txt|SQL2@reportfile2.txt|....


Tables in the crawler database
------------------------------

DOWNLOADS
url VARCHAR(4095) | http_code INTEGER default 0 | response_time INTEGER default 0 | downloaded_at DATETIME default NOW | downladed BOOLEAN

LINKS
url_from VARCHAR(4095) | url_to VARCHAR(4095)


TODO
-----------

* Improve and simplify logging and log configuration
* Proxy support