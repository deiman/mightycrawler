package no.bekk.mightycrawler;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;

public class Resource {
	public String url = "";
	public String content = "";

	public String contentType = "";
	public int responseCode;
	public String encoding = "";
	
	public Date timeStamp = null;
	public long responseTime;

//	public Date expires = new Date();
//	public String ETag = "";
//	public Date lastModified = new Date();

	public boolean doStore = false;
	public boolean doExtract = false;
	
	public int recursionLevel = 0;
	public Collection<String> urls = new LinkedHashSet<String>();
	
	public Resource(String url) {
		this.url = url;
	}
}


