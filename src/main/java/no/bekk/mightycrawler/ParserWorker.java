package no.bekk.mightycrawler;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ParserWorker implements Callable<Resource> {

	private Resource res;
	private IncludeExcludeFilter linkFilter;
	
	static final Log log = LogFactory.getLog(ParserWorker.class);
	
	public ParserWorker(Resource res, IncludeExcludeFilter linkFilter) {
		this.res = res;
		this.linkFilter = linkFilter;
	}
	
	public Resource call() {
		res.urls = linkFilter.getMatches(res.content);
		return res;
	}	
}
