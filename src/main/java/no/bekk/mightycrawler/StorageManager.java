package no.bekk.mightycrawler;

import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StorageManager extends Thread {
	
	private ExecutorService workerService = null;
	private CompletionService<String> completionService = null;
	private Configuration c;
	
	public int queueSize;

	static final Log log = LogFactory.getLog(StorageManager.class);

	public StorageManager(Configuration c) {		
		this.c = c;
		workerService = Executors.newFixedThreadPool(c.saveThreads);
		completionService = new ExecutorCompletionService<String>(workerService);
	}
	
	public void addToQueue(Resource res) {
		completionService.submit(new StorageWorker(c, res.content, res.url));
		log.debug("Added storing of " + res.url + " to queue.");
		queueSize++;
	}

	public void run() {
		while (!workerService.isShutdown()) {
			try {
				Future<String> result = completionService.take();
				String status = result.get();
				log.debug("Done saving to disk. Status: " + status);
				queueSize--;
			} catch (RejectedExecutionException ree) {
	        	// This exception is harmless here. Do nothing.
			} catch (InterruptedException e) {
				log.debug("StorageManager is shutting down.");
				List<Runnable> queuedTasks = workerService.shutdownNow();
				log.debug("Cancelling " + queuedTasks.size() + " storage tasks.");
			} catch (Exception e) {
				log.error("Error getting storage worker results.");
			}
		}
		log.debug("StorageManager has shut down.");
	}
}
