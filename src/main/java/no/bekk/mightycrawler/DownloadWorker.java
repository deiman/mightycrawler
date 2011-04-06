package no.bekk.mightycrawler;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class DownloadWorker implements Callable<Resource> {
	
	private HttpClient httpClient;
	private HttpContext context;
	private String url;

	private Configuration c;
	
	static final Log log = LogFactory.getLog(DownloadWorker.class);
	
    public DownloadWorker(HttpClient httpClient, String url, Configuration c) {
        this.httpClient = httpClient;
        this.context = new BasicHttpContext();
        this.url = url;
        this.c = c;
    }
    
    public Resource call() {
    	Resource res = new Resource(url);
    	HttpGet httpGet = new HttpGet(url);
        try {
        	log.debug("Fetching " + url + ", delay = " + c.downloadDelay + " seconds.");
        	Thread.sleep(c.downloadDelay * 1000);
	    	
			long startTime = System.currentTimeMillis();
			HttpResponse response = httpClient.execute(httpGet, context);
	    	res.responseTime = System.currentTimeMillis() - startTime;
			res.timeStamp = parseTimestamp(response.getFirstHeader("Date").getValue());
	    	
        	HttpEntity entity = response.getEntity();
        	res.responseCode = response.getStatusLine().getStatusCode();
			res.encoding = c.defaultEncoding;
			
    		// TODO: handle redirects
        	if (entity != null) {        		
        		if (res.responseCode == HttpStatus.SC_OK) {
        			if (entity.getContentType() != null) {
        				res.contentType = StringUtils.substringBefore(entity.getContentType().getValue(), ";");
        			}
        			if (entity.getContentEncoding() != null) {
        				res.encoding = entity.getContentEncoding().getValue();
        			}
	    			handleContent(entity, res);
				} else {
					log.debug("Not fetching page at " + url + ", response code was " + res.responseCode);
				}
	        	entity.consumeContent();
            }
        } catch (InterruptedException ie) {
        	// Thread interruption is harmless here. Do nothing.
        } catch (Exception e) {
            httpGet.abort();
            log.error("Error fetching page at : " + url + ", " + e);
        }
        return res;
    }

    public void handleContent(HttpEntity entity, Resource res) {
    	// TODO: handle link extraction (HTML, Flash, JS) vs content download (HTML)
    	
    	if (c.linkExtractionFilter.letsThrough(res.contentType)) {
	    	log.debug("Content-type matched extraction filter. Processing html at " + url + " with encoding " + res.encoding);
	    	try {
	    		res.content = EntityUtils.toString(entity, res.encoding);
	    	} catch (Exception e) {
	    		log.error(e);
	    	}
		}
		if (c.binaryFilter.letsThrough(res.contentType)) {
	    	log.debug("Content-type matched binary file filter. Adding " + url + " to list of URLs to index later.");
	    	addURLToFile(c.binariesFile, url);
		}
	}

	public void addURLToFile(String fileName, String url) {
		log.info("Added url " + url + " to binaries file.");
		try {
			FileWriter fw = new FileWriter(fileName, true);
			fw.write(url + "\n");
			fw.close();
		} catch (Exception e) {
			log.error("Could not append url " + url + " to file " + fileName + ": " + e);
		}
	}    
    
	public Date parseTimestamp(String headerValue) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		Date d;
		try {
			d = sdf.parse(headerValue);
		} catch (Exception e) {
			log.warn("Could not parse date from response: " + headerValue + ", using current time instead.");
			d = new Date();
		}
		return d;
	}

}
