package no.bekk.mightycrawler;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class URLManager {

	private LinkedHashSet<String> urlsToVisit = new LinkedHashSet<String>();
	private LinkedHashSet<String> urlsVisited = new LinkedHashSet<String>();

	private IncludeExcludeFilter urlFilter;
	
	static final Log log = LogFactory.getLog(URLManager.class);

	public URLManager(IncludeExcludeFilter f) {
		urlFilter = f;
	}

	public Collection<String> updateQueues(LinkHolder l) {
		markURLAsVisited(l.url);

		Collection<String> newURLs = l.urls;
		newURLs = normalizeURLs(newURLs, l.url);
		newURLs = removeKnownURLs(newURLs);
		newURLs = filterURLs(newURLs);
		log.info("Page: " + l.url + ", urls added to queue: " + newURLs.size());

		addNewURLs(newURLs);
		log.info("Urls visited: " + urlsVisited.size());
		log.info("Urls to visit: " + urlsToVisit.size());
		
		return newURLs;
	}
	
	public Collection<String> removeKnownURLs(Collection<String> newUrls) {
		newUrls.removeAll(urlsVisited);
		newUrls.removeAll(urlsToVisit);
		return newUrls;
	}

	public void markURLAsVisited(String url) {
		urlsToVisit.remove(url);
		urlsVisited.add(url);
	}

	public void addNewURLs(Collection<String> newUrls) {
		urlsToVisit.addAll(newUrls);
	}

	public Collection<String> filterURLs(Collection<String> urlList) {
		Collection<String> filteredURLs = new HashSet<String>();
		for (String u : urlList) {
			if (urlFilter.letsThrough(u)) {
				filteredURLs.add(u);
			}
		}
		return filteredURLs;
	}

	public Collection<String> normalizeURLs(Collection<String> urlList, String baseUrl) {
		Collection<String> normalizedURLs = new HashSet<String>();
		for (String u : urlList) {
			normalizedURLs.add(normalize(u, baseUrl));
		}
		return normalizedURLs;
	}

	public String normalize(String url, String baseUrl) {
		url = url.trim();
		url = StringUtils.substringBefore(url, " ");

		// Needed?
		url = url.replaceAll("&#38;", "&");
		url = url.replaceAll("&amp;", "&");

		// Remove page anchor links - still the same page
		url = StringUtils.substringBeforeLast(url, "#");

		// Remove sessionids - still the same page
		url = StringUtils.substringBeforeLast(url, ";jsessionid");
		
		String absoluteURL = "";
		if (url.length() == 0) {
			// if url is empty at this point the normalized version is the baseurl itself
			return baseUrl;
		} else {
			try {
				URI base = new URI(baseUrl);
				URI fullUrl = base.resolve(url);

				absoluteURL = fullUrl.toString();
				String query = fullUrl.getRawQuery();
				if (query != null) {
					String beforeQuery = StringUtils.substringBefore(absoluteURL, query);
					absoluteURL = beforeQuery + alphabetizeQuery(query);
				}
//				log.debug("Normalized url: " + url + " to: " + absoluteURL);
			} catch (Exception e) {
				log.error("Could not normalize url: " + url + ", base url: " + baseUrl + ", error: " + e);
			}
		}
		return absoluteURL;
	}
	
	public String alphabetizeQuery(String queryMap) {
		String[] vars = queryMap.split("&");
		Arrays.sort(vars);
		return StringUtils.join(vars, "&");
	}
}
