package no.bekk.mightycrawler;

public class LogEntry {
	
	public String url;
	public String page;
	public int httpCode;
	public long responseTime;
	public long downloadTime;
	
	//TODO: Replace with static status DOWNLOADED/ERROR/REDIRECT
	public boolean downloaded;
	
	public LogEntry() {
		downloaded = false;
	}

	public LogEntry(String url, String page, int httpCode, long responseTime, long downloadTime, boolean downloaded) {
		this.url = url;
		this.page = page;
		this.httpCode = httpCode;
		this.responseTime = responseTime;
		this.downloadTime = downloadTime;
		this.downloaded = downloaded;
	}

}
