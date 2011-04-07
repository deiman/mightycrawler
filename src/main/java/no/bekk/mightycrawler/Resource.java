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

	public boolean doStore = false;
	public boolean doCollect = false;
	public boolean doParse = false;
	
	public Resource(String url) {
		this.url = url;
	}
}


