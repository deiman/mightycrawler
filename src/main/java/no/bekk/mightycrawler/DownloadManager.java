package no.bekk.mightycrawler;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class DownloadManager extends Thread {

	private HttpClient httpClient;
	private ClientConnectionManager cm;

	private ExecutorService workerService = null;
	private CompletionService<Resource> completionService = null;
	
	private Configuration c;
	private Report r;
	private StorageManager s;
	private ParserManager p;
	
	public int urlsDownloaded;
	public int queueSize;
	
	static final Log log = LogFactory.getLog(DownloadManager.class);

	public DownloadManager(Configuration c, Report r, StorageManager s) {
		this.c = c;
		this.r = r;
		this.s = s;

		HttpParams params = new BasicHttpParams();

		ConnManagerParams.setMaxTotalConnections(params, c.downloadThreads);
		ConnPerRouteBean cpr = new ConnPerRouteBean(c.downloadThreads);
		ConnManagerParams.setMaxConnectionsPerRoute(params, cpr);
		ConnManagerParams.setTimeout(params, 5000);
		
		HttpProtocolParams.setUserAgent(params, c.userAgent);
        
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), c.httpPort));
        
		cm = new ThreadSafeClientConnManager(params, schemeRegistry);
		httpClient = new DefaultHttpClient(cm, params);
				
		workerService = Executors.newFixedThreadPool(c.downloadThreads);
		completionService = new ExecutorCompletionService<Resource>(workerService);
	}
	
	public void setParserManager(ParserManager p) {
		this.p = p;
	}
	
	public void addToQueue(String url) {
		completionService.submit(new DownloadWorker(httpClient, url, c));
		log.debug("Added downloading of " + url + " to queue.");
		queueSize++;
	}

	public void addToQueue(Collection<String> URLs) {
		for (String url : URLs) {
			addToQueue(url);
		}
	}
	
	public boolean isTerminated() {
		return workerService.isTerminated();
	}
	
	public void run() {
		while (!workerService.isShutdown()) {
			try {
				Future<Resource> result = completionService.poll(c.crawlerTimeout, TimeUnit.SECONDS);
				if (result != null) {
					Resource res = result.get();
					log.debug("Has processed URL: " + res.url);	
					r.registerDownload(res);
					if (res.hasContent()) {
						s.addToQueue(res);
						p.addToQueue(res);
						urlsDownloaded++;
					}
					queueSize--;
				} else {
					log.info("Stopping, reached crawlerTimeout.");
					List<Runnable> queuedTasks = workerService.shutdownNow();
					log.debug("Cancelling " + queuedTasks.size() + " downloads.");					
				}
			} catch (RejectedExecutionException ree) {
	        	// This exception is harmless here. Do nothing.
			} catch (Exception e) {
				log.error("Error waiting for download workers: " + e);
			}
			
			if (urlsDownloaded == c.maxPages) {
				log.info("Stopping, reached maxPages.");
				List<Runnable> queuedTasks = workerService.shutdownNow();
				log.debug("Cancelling " + queuedTasks.size() + " downloads.");
			}
		}
		
		cm.shutdown();
		log.debug("DownloadManager has shut down.");
	}	
}
