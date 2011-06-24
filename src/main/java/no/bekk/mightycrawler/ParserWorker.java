package no.bekk.mightycrawler;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

public class ParserWorker implements Callable<Resource> {

	private Resource res;
	private IncludeExcludeFilter linkFilter;
	
	static final Logger log = Logger.getLogger(ParserWorker.class);


	public ParserWorker(Resource res, IncludeExcludeFilter linkFilter) {
		this.res = res;
		this.linkFilter = linkFilter;
	}
	
	public Resource call() {
		res.urls = linkFilter.getMatches(res.content);
		return res;
	}	
}
