package no.bekk.mightycrawler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class Configuration {

	public Collection<String> startURLs = new ArrayList<String>();
	public String includeCrawl = "";
	public String excludeCrawl = "";

	public boolean crawlingEnabled = true;
	
	public String extract = "";
	public String link = "";
	public String store = "";
	public String collect = "";

	public String userAgent = "";
	public String defaultHTTPEncoding = "";
	public String defaultFileEncoding = "";
	public int httpPort = 80;
	
	public String proxyHost = "";
	public int proxyPort;
	public String proxyUsername = "";
	public String proxyPassword = "";
	
	public int downloadThreads;
	public int parseThreads;
	public int saveThreads;

	public int maxPages;
	public int maxRecursion;
	public int downloadDelay;
	public int responseTimeout;
	public int crawlerTimeout;

	public String listFile = "";
	public String outputDirectory = "";
	public String reportDirectory = "";

	public Collection<String> reportSQL = new ArrayList<String>();

	public IncludeExcludeFilter crawlFilter;
	public IncludeExcludeFilter extractFilter;
	public IncludeExcludeFilter linkFilter;
	public IncludeExcludeFilter storeFilter;
	
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
		
			String start = p.getProperty("startURLs", "");
			startURLs = Arrays.asList(start.split("\\|"));

			String urlFile = p.getProperty("urlFile", "");
			File f = new File(urlFile);
			if (f.exists()) {
				startURLs = FileUtils.readLines(f);				
				crawlingEnabled = false;
			}

			String first = startURLs.iterator().next();
			int port = new URL(first).getPort();
			if (port != -1) {
				httpPort = port;
			}
	 
	 		String[] inc = start.split("\\|");
			String defaultIncludes = StringUtils.join(inc, ".*|") + ".*";
			if (!crawlingEnabled) {
				defaultIncludes = "";
			}
			includeCrawl = p.getProperty("includeCrawl", defaultIncludes);
			excludeCrawl = p.getProperty("excludeCrawl", "");

			extract = p.getProperty("extract", "");
			link =  p.getProperty("link", "");
			store = p.getProperty("store", "");
			collect = p.getProperty("collect", "");
			
			userAgent = p.getProperty("userAgent", "");
			defaultHTTPEncoding = p.getProperty("defaultHTTPEncoding", "UTF-8");
			defaultFileEncoding = p.getProperty("defaultFileEncoding", "UTF-8");

			proxyHost = p.getProperty("proxyHost", "");

			String pPort = p.getProperty("proxyPort", "");
			if ("".equals(pPort)) {
				pPort = "8080";
			}
			proxyPort = Integer.parseInt(pPort);
			proxyUsername = p.getProperty("proxyUsername", "");
			proxyPassword = p.getProperty("proxyPassword", "");

			downloadThreads = Integer.parseInt(p.getProperty("downloadThreads", "1"));
			parseThreads = Integer.parseInt(p.getProperty("parseThreads", "1"));
			saveThreads = Integer.parseInt(p.getProperty("saveThreads", "1"));
				
			maxPages = Integer.parseInt(p.getProperty("maxPages", "1"));
			maxRecursion = Integer.parseInt(p.getProperty("maxRecursion", "1"));
			downloadDelay = Integer.parseInt(p.getProperty("downloadDelay", "5"));		
			responseTimeout = Integer.parseInt(p.getProperty("responseTimeout", "10"));		
			crawlerTimeout = Integer.parseInt(p.getProperty("crawlerTimeout", "30"));

			listFile = p.getProperty("listFile", "");
			outputDirectory = p.getProperty("outputDirectory", System.getProperty("java.io.tmpdir"));
			reportDirectory = p.getProperty("reportDirectory", System.getProperty("java.io.tmpdir"));

			String sql = p.getProperty("reportSQL", "");
			if (sql.length() == 0) {
				reportSQL = new ArrayList<String>();
			} else {
				reportSQL = Arrays.asList(sql.split("\\|"));
			}			
			
			crawlFilter = new IncludeExcludeFilter(includeCrawl, excludeCrawl);
			extractFilter = new IncludeExcludeFilter(extract, "");
			linkFilter = new IncludeExcludeFilter(link, "");
			storeFilter = new IncludeExcludeFilter(store, "");
			
		} catch (IOException ioe) {
			System.err.println("\nError reading configuration file: " + ioe);
			System.err.println("Aborting.");
			System.exit(1);
		} catch (NumberFormatException nfe) {
			log.error("Error reading configuration value: " + nfe);
		} finally {
			try {
				if (fr != null) fr.close();
			} catch (IOException ioe) {
				log.error("Error closing configuration file: " + ioe.getMessage());
			}
		}
	}	
}

