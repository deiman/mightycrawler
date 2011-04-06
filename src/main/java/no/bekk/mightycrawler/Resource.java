package no.bekk.mightycrawler;

import java.util.Date;

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
		
	public Resource(String url) {
		this.url = url;
	}
		
	public boolean hasContent() {
		return (content != null && content.length() > 0);
	}
}


