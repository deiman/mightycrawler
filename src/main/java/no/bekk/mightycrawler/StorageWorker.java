package no.bekk.mightycrawler;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class StorageWorker implements Callable<String> {

	private String page;
	private String url;

	private Configuration c;
	
	static final Logger log = Logger.getLogger(StorageWorker.class);

	public StorageWorker(Configuration c, String page, String url) {
		this.c = c;
		this.page = page;
		this.url = url;
	}
	
	public String call() {
		String outputFile = mapURLtoFileName(url); 
		String outputPath = StringUtils.substringBeforeLast(outputFile, "/");
		try {
			boolean created = new File(c.outputDirectory + outputPath).mkdirs();
			boolean exists = new File(c.outputDirectory + outputPath).exists();
			if (!exists) {
				log.error("Error creating output directory: " + c.outputDirectory + outputPath);
    			return "ERROR";
			}
			File f = new File(c.outputDirectory + outputFile);
			if (f.exists()) {
				log.error("Two different URLs map to the same file name. Stopping before overwriting existing file: " + c.outputDirectory + outputFile);
    			return "ERROR";				
			}
			FileUtils.writeStringToFile(f, toXML(url, page), c.defaultFileEncoding);
			log.debug("Wrote page at: " + url + " to file: " + c.outputDirectory + outputFile);
		} catch (IOException ioe) {
			log.error("Error saving url: " + url + " to disk, cause: " + ioe);
			return "ERROR";
		}
		return "OK";
	}

	public String mapURLtoFileName(String url) {
		// Extract host name
		String hostName = StringUtils.substringAfter(url, "://");
		hostName = hostName.substring(0, hostName.indexOf("/") + 1);

		// Append a unique file name based on the URL string
		// Note: The hashCodes of two different Strings (URLs) are not guaranteed to be different
		// TODO: Replace with perfect hashing
		String fileName = hostName + Integer.toHexString(url.hashCode()) + ".xml";
		return fileName;
	}
	
	public String toXML(String url, String page) {
		// Restart any CDATA blocks in case the input is XHTML
		page = page.replaceAll("]]>", "]]]]><![CDATA[>");

		// If the URL string is not already encoded, XML encode it
		// Works correctly if &'s will not appear in both raw and encoded variants in the URL
		if (!url.contains("&amp;")) {
			url = url.replaceAll("&", "&amp;");
		}
		
		return "<?xml version\"1.0\" encoding=\"utf-8\" ?>\n" + 
		"<resource>\n" +
		"<url>" + url + "</url>\n" +
		"<content><![CDATA[\n" + page + "\n]]></content>\n" +
		"</resource>";
	}
}
