package no.bekk.mightycrawler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Crawler {

	private Configuration config = null;
	private Report report = null;

	private StorageManager storage = null;
	private DownloadManager download = null;
	private ParserManager parse = null;
	
	static final Log log = LogFactory.getLog(Crawler.class);
	
	public void init(String propertiesFile) {
		config = new Configuration(propertiesFile);
		report = new Report();
		
		storage = new StorageManager(config);
		download = new DownloadManager(config, report, storage);
		parse = new ParserManager(config, download, report);
	}
	
	
	public void start() {
		log.info("Crawling starts...");

		download.setParserManager(parse);
		
		parse.start();
		storage.start();
 		download.start(); 		

 		download.addToQueue(config.startURL);

 		while (!download.isTerminated()) {
 			try {
				Thread.sleep(10000);
			} catch (Exception e) {
	 			log.error("Main thread sleep interrupted.");
			}
			log.info("Downloaded URLs: " + download.urlsDownloaded);
 			log.info("Download queue size: " + download.queueSize);
 			log.info("Parse queue size: " + parse.queueSize);
 			log.info("Storage queue size: " + storage.queueSize);
 		}
 		
 		try {
	 		download.join();
	 		
	 		parse.interrupt();
	 		parse.join();
	 		
	 		storage.interrupt();
	 		storage.join();
 		} catch (Exception e) {
 			log.debug("Error waiting for threads to stop: " + e);
 		}

 		report.createReport(config.reportDirectory, config.reportSQL);
		report.shutDown();

		log.info("Crawling finished.");
	}
	
	public static void main(String[] args) {
		Crawler c = new Crawler();

		String defaultFile = "crawler.properties";
		if (args.length == 1) {
			defaultFile = args[0];
		}
		
		c.init(defaultFile);
		c.start();
	}
	
}
