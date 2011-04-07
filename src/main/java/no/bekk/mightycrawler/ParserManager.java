package no.bekk.mightycrawler;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ParserManager extends Thread {

	private ExecutorService workerService = null;
	private CompletionService<LinkHolder> completionService = null;
	private URLManager urlManager;
	
	private DownloadManager d;
	private Report r;
	
	public int queueSize;
	
	static final Log log = LogFactory.getLog(ParserManager.class);

	public ParserManager(Configuration c, DownloadManager d, Report r) {
		this.d = d;
		this.r = r;

		urlManager = new URLManager(c.crawlFilter);
		workerService = Executors.newFixedThreadPool(c.parseThreads);
		completionService = new ExecutorCompletionService<LinkHolder>(workerService);
	}

	public void addToQueue(Resource res) {
		completionService.submit(new RegexpParserWorker(res.content, res.url));
		log.debug("Added parsing of " + res.url + " to queue.");
		queueSize++;
	}

	public void run() {
		while (!workerService.isShutdown()) {
			try {
				Future<LinkHolder> result = completionService.take();
			
				LinkHolder l = result.get();			
				log.debug("Done parsing " + l.url);

				Collection<String> newURLs = urlManager.updateQueues(l);
				d.addToQueue(newURLs);
				r.registerOutboundLinks(l.url, newURLs);
				queueSize--;
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
