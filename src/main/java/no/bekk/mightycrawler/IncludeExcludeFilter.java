package no.bekk.mightycrawler;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IncludeExcludeFilter {
	private Pattern includeFilter;
	private Pattern excludeFilter;
	
	// TODO: Collapse include/exclude-patterns
	public IncludeExcludeFilter(String include, String exclude) {
		includeFilter = Pattern.compile(include);
		excludeFilter = Pattern.compile(exclude);
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
	
	public Collection<String> getMatches(String content) {
		Collection<String> matchList = new HashSet<String>();
		Matcher matcher = includeFilter.matcher(content);
		while (matcher.find()) {
			int i=1;
			while (i <= matcher.groupCount()) {
				if (matcher.group(i) != null) {
					matchList.add(matcher.group(i));				
				}
				i++;
			}
		}
		return matchList;
	}
}
