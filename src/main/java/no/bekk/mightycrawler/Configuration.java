package no.bekk.mightycrawler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Configuration {

	public String startURL = "";
	public String includeCrawl = "";
	public String excludeCrawl = "";

	public String includeStore = "";
	public String excludeStore = "";
	
	public String includeExtractLink = "";
	public String excludeExtractLink = "";

	public String includeCollectURL = "";
	public String excludeCollectURL = "";

	public String userAgent = "";
	public String defaultPage = "";	
	public String defaultEncoding = "";
	public int httpPort = 80;
	
	public int downloadThreads;
	public int parseThreads;
	public int saveThreads;
	
	public boolean isWindows = File.separator.equals("\\");

	public int maxPages;
	public int downloadDelay;
	public int downloadTimeout;
	public int crawlerTimeout;

	public String listFile = "";
	public String outputDirectory = "";
	public String reportDirectory = "";

	public Collection<String> reportSQL = new ArrayList<String>();

	public IncludeExcludeFilter crawlFilter;
	public IncludeExcludeFilter storeFilter;
	public IncludeExcludeFilter extractLinkFilter;
	public IncludeExcludeFilter collectURLFilter;
	
	static final Logger log = Logger.getLogger(Configuration.class);
		
	public Configuration(String filename) {
		init(filename);
	}
		
	public void init(String fileName) {
		Properties p = new Properties();
		FileReader fr = null;
		try {
			fr = new FileReader(fileName);			
			p.load(fr);
		
			startURL = p.getProperty("startURL");
			int port = new URL(startURL).getPort();
			if (port != -1) {
				httpPort = port;
			}
				
			includeCrawl = p.getProperty("includeCrawl", startURL + "(.*)");
			excludeCrawl = p.getProperty("excludeCrawl", "");

			includeStore = p.getProperty("includeStore");
			excludeStore = p.getProperty("excludeStore", "");
			
			includeExtractLink = p.getProperty("includeExtractLink");
			excludeExtractLink = p.getProperty("excludeExtractLink", "");

			includeCollectURL= p.getProperty("includeCollectURL");
			excludeCollectURL = p.getProperty("excludeCollectURL", "");
			
			userAgent = p.getProperty("userAgent", "");
			defaultPage = p.getProperty("defaultPage", "index.html");
			defaultEncoding = p.getProperty("defaultEncoding", "UTF-8");

			downloadThreads = Integer.parseInt(p.getProperty("downloadThreads", "1"));
			parseThreads = Integer.parseInt(p.getProperty("parseThreads", "1"));
			saveThreads = Integer.parseInt(p.getProperty("saveThreads", "1"));
				
			maxPages = Integer.parseInt(p.getProperty("maxPages", "1"));
			downloadDelay = Integer.parseInt(p.getProperty("downloadDelay", "5"));		
			downloadTimeout = Integer.parseInt(p.getProperty("downloadTimeout", "10"));		
			crawlerTimeout = Integer.parseInt(p.getProperty("crawlerTimeout", "30"));

			listFile = p.getProperty("listFile", "");
			outputDirectory = p.getProperty("outputDirectory", System.getProperty("java.io.tmpdir"));
			reportDirectory = p.getProperty("reportDirectory", System.getProperty("java.io.tmpdir"));
			
			reportSQL = Arrays.asList(p.getProperty("reportSQL").split("\\|"));
			
			crawlFilter = new IncludeExcludeFilter(includeCrawl, excludeCrawl);
			storeFilter = new IncludeExcludeFilter(includeStore, excludeStore);	
			extractLinkFilter = new IncludeExcludeFilter(includeExtractLink, excludeExtractLink);	
			collectURLFilter = new IncludeExcludeFilter(includeCollectURL, excludeCollectURL);
			
		} catch (IOException ioe) {
			System.err.println("\nError reading configuration file: " + ioe.getMessage());
			System.err.println("Aborting.");
			System.exit(1);
		} catch (NumberFormatException nfe) {
			log.error("Error reading configuration value: " + nfe.getMessage());
		} finally {
			try {
				if (fr != null) fr.close();
			} catch (IOException ioe) {
				log.error("Error closing configuration file: " + ioe.getMessage());
			}
		}
	}	
}

