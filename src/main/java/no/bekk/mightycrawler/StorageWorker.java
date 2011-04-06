package no.bekk.mightycrawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StorageWorker implements Callable<String> {

	private String page;
	private String url;

	private Configuration c;
	
	static final Log log = LogFactory.getLog(StorageWorker.class);

	public StorageWorker(Configuration c, String page, String url) {
		this.c = c;
		this.page = page;
		this.url = url;
	}
	
	public String call() {
		// TODO: read max url length from configuration (update report sql also)
		if (url.length() < 200) {
			
			String fullPath = mapURLtoFullPath(url); 
			String directory = StringUtils.substringBeforeLast(fullPath, "/");

			FileWriter fw = null;
			try {
				boolean created = new File(c.outputDirectory + directory).mkdirs();
    			fw = new FileWriter(c.outputDirectory + fullPath);
    			fw.write(page);
    			log.debug("Wrote page at: " + url + " to file: " + c.outputDirectory + fullPath);
			} catch (IOException ioe) {
				log.error("Error saving url: " + url + " to disk, cause: " + ioe);
    			return "ERROR";
			} finally {
				try {
					if (fw != null) fw.close();				
				} catch (IOException ioe) {
					log.error("Error closing file writer: " + c.outputDirectory + fullPath + ", cause: " + ioe);
				}
			}
		}
		return "OK";
	}
	
	public String mapURLtoFullPath(String url) {
		String fileName = StringUtils.substringAfter(url, "://");

		// On Windows platform replace illegal file name characters
		if (c.isWindows) {
			fileName = fileName.replaceAll(":", "+");
			fileName = fileName.replaceAll("\\?", "%3F");
		}

		String lastPart = StringUtils.substringAfterLast(fileName, "/");
		if (!lastPart.contains(".")) {
			// if not clearly a file, assume lastPart is a directory to avoid overwrites - lastPart might be a substring of a longer url. 
			// example:
			// 1) /section
			// 2) /section/test.html
			// if lastPart is instead regarded to be a file, creating the directory "section" (when saving content in url 2) would fail.
			//
			// RISK: this means /section is saved under section/index.html. What if the url /section/index.html turns up later?
			fileName = fileName + "/";
		}
		
		if (fileName.endsWith("/")) {
			fileName = fileName + c.defaultPage;
		}

		return fileName;
	}
}
