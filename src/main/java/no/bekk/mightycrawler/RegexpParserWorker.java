package no.bekk.mightycrawler;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RegexpParserWorker implements Callable<LinkHolder> {

	private static Pattern filter = Pattern.compile("href=\"(.*?)\"");
	private String page;
	private String url;
	
	static final Log log = LogFactory.getLog(RegexpParserWorker.class);
	
	public RegexpParserWorker(String page, String url) {
		this.page = page;
		this.url = url;
	}
	
	public LinkHolder call() {
		Collection<String> urlList = new HashSet<String>();
		Matcher hrefMatcher = filter.matcher(page);
		while (hrefMatcher.find()) {
			urlList.add(hrefMatcher.group(1));
		}
		return new LinkHolder(url, urlList);
	}	
}
