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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ParserManager extends Thread {

	private ExecutorService workerService = null;
	private CompletionService<Resource> completionService = null;
	private URLManager urlManager;
	private IncludeExcludeFilter linkFilter = null;
	
	private DownloadManager d;
	private Report r;
	
	static final Log log = LogFactory.getLog(ParserManager.class);

	public ParserManager(Configuration c, DownloadManager d, Report r) {
		this.d = d;
		this.r = r;

		linkFilter = c.linkFilter;
		urlManager = new URLManager(c.crawlFilter);
		workerService = Executors.newFixedThreadPool(c.parseThreads);
		completionService = new ExecutorCompletionService<Resource>(workerService);
	}

	public void addToQueue(Resource res) {
		completionService.submit(new ParserWorker(res, linkFilter));
		log.debug("Added parsing of " + res.url + " to queue.");
	}

	public int getQueueSize() {
		return ((ThreadPoolExecutor) workerService).getQueue().size();
	}

	
	public void run() {
		while (!workerService.isShutdown()) {
			try {
				Future<Resource> result = completionService.take();
			
				Resource res = result.get();			
				log.debug("Done parsing " + res.url);

				Collection<String> newURLs = urlManager.updateQueues(res);
				d.addToQueue(newURLs, res.recursionLevel+1);
				r.registerOutboundLinks(res.url, newURLs);
			} catch (RejectedExecutionException ree) {
	        	// This exception is harmless here. Do nothing.
			} catch (InterruptedException e) {
				log.debug("ParserManager is shutting down.");
				List<Runnable> queuedTasks = workerService.shutdownNow();
				log.debug("Cancelling " + queuedTasks.size() + " parsing tasks.");
			} catch (Exception e) {
				log.error("Error waiting on parser workers: " + e);
			}
		}
		log.debug("ParseManager has shut down.");
	}
}
