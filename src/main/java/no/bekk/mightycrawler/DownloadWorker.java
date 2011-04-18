package no.bekk.mightycrawler;

import java.io.FileWriter;
import java.net.SocketTimeoutException;
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
	private Resource res;

	private Configuration c;
	
	static final Log log = LogFactory.getLog(DownloadWorker.class);
	
    public DownloadWorker(HttpClient httpClient, Resource res, Configuration c) {
        this.httpClient = httpClient;
        this.context = new BasicHttpContext();
        this.res = res;
        this.c = c;
    }
    
    public Resource call() {
    	HttpGet httpGet = new HttpGet(res.url);
        try {
        	log.debug("Fetching " + res.url + ", delay = " + c.downloadDelay + " seconds.");
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
					log.debug("Not fetching page at " + res.url + ", response code was " + res.responseCode);
				}
	        	EntityUtils.consume(entity);
            }
        } catch (InterruptedException ie) {
        	// Thread interruption is harmless here. Do nothing.
        } catch (SocketTimeoutException ste) {
            httpGet.abort();
            res.responseCode = HttpStatus.SC_REQUEST_TIMEOUT;
            log.warn("Timeout (" + c.responseTimeout + " seconds) reached when requesting: " + res.url + ", " + ste);
        } catch (Exception e) {
            httpGet.abort();
            log.error("Error fetching page at : " + res.url + ", " + e);
        }
        return res;
    }

    public void handleContent(HttpEntity entity, Resource res) {
    	res.doStore = c.storeFilter.letsThrough(res.contentType);
    	res.doExtract = c.extractFilter.letsThrough(res.contentType);
    	
    	if (res.doStore || res.doExtract) {
	    	try {
	    		res.content = EntityUtils.toString(entity, res.encoding);
	    	} catch (Exception e) {
	    		log.error(e);
	    	}
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
