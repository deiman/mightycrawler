package no.bekk.mightycrawler;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class IncludeExcludeFilter {
	private Pattern includeFilter;
	private Pattern excludeFilter;
	
	public IncludeExcludeFilter(String include, String exclude) {
		includeFilter = Pattern.compile(include);
		excludeFilter = Pattern.compile(exclude);
	}
	
	public IncludeExcludeFilter(Collection<String> include, Collection<String> exclude) {
		this(StringUtils.join(include, "|"), StringUtils.join(exclude, "|"));
	}
	
	public boolean letsThrough(String item) {
		Matcher includeMatcher = includeFilter.matcher(item);
	    if (includeMatcher.matches()) {
	    	Matcher excludeMatcher = excludeFilter.matcher(item);
	    	if (!excludeMatcher.matches()) {
	    		return true;
	    	}
    	}
	    return false;
	}
}
