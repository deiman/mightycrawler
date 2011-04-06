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

Mightycrawler is a multithreaded web crawler with reporting capabilities. The code is structured as a multi-stage queue system where downloading, parsing and storing content is done by separate thread pools. Various statistics about the site performance is gathered during crawling and put in a RAM database. After completion, the database can be queried to produce various reports. The database is not persistent between runs.

Warning! Mightycrawler is indeed mighty and can generate a lot of requests in short time. Please do use the program properly.


Quick start
-----------

After downloading and building, edit crawler.properties to suit your needs.

Then run mightycrawler by typing

	java -jar mightycrawler.jar


Configuration
-------------

All options for running mightycrawler are given in the crawler.properties file:

* startURL: Where to start crawling. URL must end with a "/".

* includeURL: Restrict crawling and downloading to URLs matching this regex. Defaults to everything under startURL.

* excludeURL: Among the included URLs, exclude any matching this regex. Defaults to none (don't exclude any).

* includeLinkExtraction: Restrict link extraction to content types matching this regex. Defaults to none (if nothing is specified).

* excludeLinkExtraction: Among the included content types, do not perform link extraction on content types matching this regex. Defaults to none (don't exclude any).

* includeContent: Restrict downloading to content types matching this regex. Defaults to none (if nothing is specified).

* excludeContent: Among the included content types, exclude any matching this regex. defaults to none (don't exclude any).

* includeBinaryFile: Content types matching this regex pattern are not downloaded and not parsed - instead the URLs are gathered in a separate file for possible later processing. Only URLs matching the link extraction filter will be considered. Also, only links that are found inside href attributes will be discovered.

* excludeBinaryFile: Among the included content types, exclude any matching this regex. Defaults to none (don't exclude any).

* binariesFile: URLs to binary files will be gathered in this file. If the name is blank no file will be generated.

* userAgent: The user agent the crawler identifies itself as.

* defaultPage: When storing pages to disk, URLs ending with "/" are appended with the file name given here. The URL would else map to a directory name - making storage impossible.

* defaultEncoding: Use this encoding to store content unless content-type is specified in the HTTP response header.

* downloadThreads: Number of threads for downloading.

* parseThreads: Number of threads for parsing content, looking for URLs.

* saveThreads: Number of threads used for saving content to disk.

* maxPages: Stop after downloading this number of pages.

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


Todo
----
* Implement "stop when recursion level N is reached" 
* Improve link extraction, now only looks at href attribute values
