package no.bekk.mightycrawler;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.log4j.Logger;

public class DownloadManager extends Thread {

	private DefaultHttpClient httpClient;
	private ThreadSafeClientConnManager cm;

	private ExecutorService workerService = null;
	private CompletionService<Resource> completionService = null;
	
	private Configuration c;
	private Report r;
	private StorageManager s;
	private ParserManager p;
	
	public int urlsDownloaded;
	public int recursionLevel;
	
	static final Logger log = Logger.getLogger(DownloadManager.class);

	public DownloadManager(Configuration c, Report r, StorageManager s) {
		this.c = c;
		this.r = r;
		this.s = s;

		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setSoTimeout(params, c.responseTimeout * 1000);
		HttpProtocolParams.setUserAgent(params, c.userAgent);
        
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", c.httpPort, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
        
		cm = new ThreadSafeClientConnManager(schemeRegistry);
		cm.setDefaultMaxPerRoute(c.downloadThreads);
		cm.setMaxTotal(c.downloadThreads);

		httpClient = new DefaultHttpClient(cm, params);

		if (!"".equals(c.proxyHost)) {
			HttpHost proxy = new HttpHost(c.proxyHost, c.proxyPort);
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

    		if (!"".equals(c.proxyUsername)) {
    			AuthScope as = new AuthScope(c.proxyHost, c.proxyPort);
    			UsernamePasswordCredentials upc = new UsernamePasswordCredentials(c.proxyUsername, c.proxyPassword);
    			httpClient.getCredentialsProvider().setCredentials(as, upc);
    		}
		}
				
		workerService = Executors.newFixedThreadPool(c.downloadThreads);
		completionService = new ExecutorCompletionService<Resource>(workerService);
	}
	
	public void setParserManager(ParserManager p) {
		this.p = p;
	}
	
	public void addToQueue(Resource res) {
		completionService.submit(new DownloadWorker(httpClient, res, c));
		log.debug("Added downloading of " + res.url + " to queue.");
	}

	public void addToQueue(Collection<String> URLs, int recursionLevel) {
		for (String url : URLs) {
			Resource res = new Resource(url);
			res.recursionLevel = recursionLevel;
			addToQueue(res);
		}
	}
	
	public int getQueueSize() {
		return ((ThreadPoolExecutor) workerService).getQueue().size();
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
					log.info("Recursion level: " + res.recursionLevel);
					
					r.registerDownload(res);
					if (res.doStore) {
						urlsDownloaded++;
						s.addToQueue(res);
					}
					if (res.doExtract) {
						p.addToQueue(res);
					}
					recursionLevel = res.recursionLevel;
				} else {
					log.info("Queue size was: " + getQueueSize());
					log.info("Stopping, no download completed within " + c.crawlerTimeout + " seconds.");
					List<Runnable> queuedTasks = workerService.shutdownNow();
					log.info("Cancelling " + queuedTasks.size() + " downloads.");
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

			if (recursionLevel > c.maxRecursion) {
				log.info("Stopping, reached maxRecursion.");
				List<Runnable> queuedTasks = workerService.shutdownNow();
				log.debug("Cancelling " + queuedTasks.size() + " downloads.");
			}
		}
		
		cm.shutdown();
		log.debug("DownloadManager has shut down.");
	}	
}
