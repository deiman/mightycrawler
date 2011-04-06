package no.bekk.mightycrawler;

import java.util.Collection;

public class LinkHolder {

	String url;
	Collection<String> urls;
	
	public LinkHolder(String url, Collection<String> urls) {
		this.url = url;
		this.urls = urls;
	}
	
}
