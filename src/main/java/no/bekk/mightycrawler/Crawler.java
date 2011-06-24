package no.bekk.mightycrawler;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class Crawler {

	private Configuration config = null;
	private Report report = null;

	private StorageManager storage = null;
	private DownloadManager download = null;
	private ParserManager parse = null;
	
	static final Logger log = Logger.getLogger(Crawler.class);
	
	public void init(String propertiesFile) {
		
		DOMConfigurator.configure("log4j.xml");
		
		config = new Configuration(propertiesFile);
		report = new Report();
		
		storage = new StorageManager(config);
		download = new DownloadManager(config, report, storage);
		parse = new ParserManager(config, download, report);
		
		try {
			FileUtils.deleteDirectory(new File(config.outputDirectory));
			FileUtils.deleteDirectory(new File(config.reportDirectory));

			new File(config.outputDirectory).mkdirs();
			new File(config.reportDirectory).mkdirs();
		} catch (IOException ioe) {
			log.error("Could not empty directory: " + ioe);
		}
	}
	
	public void start() {
		log.info("Crawling starts...");

		download.setParserManager(parse);
		
		parse.start();
		storage.start();
 		download.start(); 		

 		Resource res = new Resource(config.startURL);
 		res.recursionLevel = 0;
 		download.addToQueue(res);

 		while (!download.isTerminated()) {
 			try {
				Thread.sleep(10000);
			} catch (Exception e) {
	 			log.error("Main thread sleep interrupted.");
			}
			log.info("Downloaded resources: " + download.urlsDownloaded);
 			log.info("Download queue size: " + download.getQueueSize());
 			log.info("Parse queue size: " + parse.getQueueSize());
 			log.info("Storage queue size: " + storage.getQueueSize());
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
