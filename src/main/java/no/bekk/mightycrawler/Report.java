package no.bekk.mightycrawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Report {

	static final transient Log log = LogFactory.getLog(Report.class);

	private Driver hsqldbDriver = null;
	private String connectionString = "jdbc:hsqldb:mem:db";

	public Report() {
		try {
			hsqldbDriver = (Driver) Class.forName("org.hsqldb.jdbcDriver").newInstance();
			DriverManager.registerDriver(hsqldbDriver);

			write("DROP TABLE downloads IF EXISTS");
			write("DROP TABLE links IF EXISTS");
			write("CREATE TABLE downloads ( url VARCHAR(4095), http_code INTEGER default 0, response_time INTEGER default 0, downloaded_at DATETIME default NOW, downloaded BOOLEAN)");
			write("CREATE TABLE links ( url_from VARCHAR(4095), url_to VARCHAR(4095))");
		} catch (Exception e) {
			log.error("Error setting up database: " + e.getMessage());
		}

	}

	public void registerDownload(Resource res) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timeString = sdf.format(res.timeStamp);
		// TODO: Escaping
		write("INSERT INTO downloads (url, http_code, response_time, downloaded_at, downloaded) values ('" 
				+ res.url + "', " + res.responseCode + ", " + res.responseTime + ", '" + timeString + "', " + res.hasContent() + ")");
	}

	public void registerOutboundLinks(String url, Collection<String> outlinks) {
		for (String l : outlinks) {
			// TODO: Escaping
			write("INSERT INTO links (url_from, url_to) values ('" + url + "', '" + l + "' )");
		}
	}
	
	private BufferedWriter createReportStream(String logDir, String fileName) {
		BufferedWriter out = null;
		try {
			boolean created = new File(logDir).mkdirs();
			out = new BufferedWriter(new FileWriter(logDir + fileName));
		} catch (Exception e) {
			log.error("Error creating report file: " + e.getMessage());
		}
		return out;
	}

	private void printReport(BufferedWriter out, Collection<LinkedHashMap<String, String>> c) {
		try {
			for (LinkedHashMap<String, String> h : c) {
				Set<Entry<String, String>> entries = h.entrySet();
				for (Entry<String, String> e: entries) {
	    			out.write(e.getValue() + " ");
				}
				out.write("\n");
			}
			out.close();
		} catch (Exception e) {
			log.error("Error creating report file: " + e.getMessage());			
		}
	}
	
	public void createReport(String logDir, Collection<String> reportList) {
		for (String report : reportList) {
			String sql = report.split("@")[0];
			String fileName = report.split("@")[1];
			printReport(createReportStream(logDir, fileName), read(sql));
		}
	}

	public Collection<LinkedHashMap<String, String>> read(String sql){
		Collection<LinkedHashMap<String, String>> a = new ArrayList<LinkedHashMap<String, String>>();
		Connection c = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			c = DriverManager.getConnection(connectionString);
			st = c.createStatement();
	        rs = st.executeQuery(sql);

	        int columns = 0;
	        ResultSetMetaData r = null;

	        try {
		        r = rs.getMetaData();
				columns = r.getColumnCount();
			} catch (Exception e) {
				log.error("ResultSet for sql: " + sql + " has no metadata.");
			}

			while (rs != null && rs.next()) {
				LinkedHashMap<String, String> l = new LinkedHashMap<String, String>();
				for (int i=1; i<=columns; i++) {
					l.put(r.getColumnName(i), "" + rs.getObject(i));
				}
				a.add(l);
			}
		} catch (Exception e) {
			log.error("Read error: " + e.getMessage());
			log.error("SQL: " + sql);
		} finally {
	        try {
	        	if (rs != null) {
	        		rs.close();	        		
	        	}
	        } catch (Exception e) {
				log.error("ResultSet close error: " + e.getMessage());
	        } finally {
	        	try {
		        	if (st != null) {
		        		st.close();
		        	}
		        } catch (Exception e) {
					log.error("Statement close error: " + e.getMessage());
		        } finally {
		        	try {
			        	if (c != null) {
			        		c.close();	        		
			        	}
			        } catch (Exception e) {
						log.error("Connection close error: " + e.getMessage());
			        }
		        }
	        }
		}
		return a;
	}

	
	public void write(String sql){
		Connection c = null;
		Statement st = null;
		try {
			c = DriverManager.getConnection(connectionString);
			st = c.createStatement();
	        if (st.executeUpdate(sql) == -1) {
	        	log.error("DB error executing sql: " + sql);
	        }
		} catch (Exception e) {
			log.error("Write error: " + e.getMessage());
			log.error("SQL: " + sql);
		} finally {
	        try {
	        	if (st != null) {
	        		st.close();
	        	}
	        } catch (Exception e) {
				log.error("Statement close error: " + e.getMessage());
	        } finally {
	        	try {
		        	if (c != null) {
		        		c.close();	        		
		        	}
		        } catch (Exception e) {
					log.error("Connection close error: " + e.getMessage());
		        }
	        }
		}
	}
	
	public void shutDown(){
		write("SHUTDOWN");
		try {
			DriverManager.deregisterDriver(hsqldbDriver);
        } catch (Exception e) {
			log.error("Could not deregister hsqldb driver: " + e.getMessage());
        }
	}
}
