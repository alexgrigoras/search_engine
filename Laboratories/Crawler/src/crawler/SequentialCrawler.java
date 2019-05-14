package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * Sequential Web Crawler for extracting html pages from a queue of URLs
 * @author alex_
 *
 */
public class SequentialCrawler {
    private static HashMap<String, String> _domanIp = new HashMap<>();

	/**
	 * Log data to console for a string
	 * @param msg
	 * @param newLine
	 */
	private static void log(String msg, boolean newLine) {
		if(newLine) {
			System.out.println(msg);
        }
		else {
			System.out.print(msg);
		}
	}
	
	/**
	 * Log data to console for an integer number
	 * @param number
	 * @param newLine
	 */
	private static void log(double number, boolean newLine) {
		StringBuilder sb = new StringBuilder();
		sb.append(number);
		if(newLine) {
			System.out.println(sb.toString());
        }
		else {
			System.out.print(sb.toString());
		}
	}
    
	/**
	 * Parse the links and remove the fragment
	 * @param str
	 * @return
	 */
	private static String parseLink(String str) {
		int i = 0;
		boolean setFlag = true;
		StringBuilder tempStr = new StringBuilder("");
		
		for(i=0; i<str.length() && setFlag;i++){
			if(str.charAt(i) != '#'){
				tempStr.append(str.charAt(i));
			}
			else 
			{
				setFlag = false;
			}
		}
		
		return tempStr.toString();
	}
	
    /**
     * Main function for Sequential Crawler
     * @param args
     */
    public static void main(String[] args) {
    	boolean logData = false;
    	
    	String startURL = "http://riweb.tibeica.com/crawl/";

        Queue<URLformatter> urlQueue = new LinkedList<>();
        try {
            int i = 0;

            urlQueue.add(new URLformatter(startURL));
            
            log("> Extragere resurse de la adresele: ", true);
            log(" -> " + startURL, true);
            
            // get the start time
    		long startTime = System.nanoTime();

            while (!urlQueue.isEmpty()) {
                try {                	
                    // get head of the queue to process
                	URLformatter processedURL = urlQueue.peek();
                    urlQueue.remove();

                    String fullDomainName = parseLink(processedURL.getFullDomainName());
                    String scheme = processedURL.getScheme();
                    String domain = processedURL.getDomain();
                    String localPath = processedURL.getLocalPath();
                    String page = processedURL.getResource();
                    String filesFolder = processedURL.getFilesFolder();
                    String processedFilesFolder = "files/text/";
                    
                    if(!domain.equals("riweb.tibeica.com")) {
                    	continue;
                    }

                    // check if domain was processed before
                    if (processedURL.wasProcessed())
                        continue;
                    String ipAddress;

                    if (!_domanIp.containsKey(domain)) {
                        DnsClient dnsClient = new DnsClient(domain, "8.8.8.8", 53, logData);
                        ipAddress = dnsClient.getIpAddres();
                        _domanIp.put(domain, ipAddress);
                    } else {
                        ipAddress = _domanIp.get(domain);
                    }

                    HTTPclient httpClient = new HTTPclient(fullDomainName, ipAddress, logData);
                    if (!_domanIp.containsKey(domain)) {
                        if (!httpClient.checkForRobosts()) {
                        	System.out.println("! Robots disallow -> " + fullDomainName);
                            continue;
                        }
                    }
                    if (!httpClient.sendRequest()) {
                    	System.out.println("! Request incomplete for " + fullDomainName);
                        continue;
                    }

                    // initialize jsoup
                    File htmlFile = new File(filesFolder + scheme + "/" + domain + localPath + "/" + page);
                    Document document = Jsoup.parse(htmlFile, null, "http://" + domain + localPath + "/");

                    // extract text to certain file
                    String text = document.body().text();

                    Files.createDirectories(Paths.get(processedFilesFolder + scheme + "/" + domain + localPath + "/"));
                    BufferedWriter writer = new BufferedWriter(new FileWriter(processedFilesFolder + scheme + "/" + domain + localPath + "/"
                            + page.replaceAll(".html", ".txt")));
                    writer.write(text);

                    writer.close();

                    // check for robots meta
                    Element robotsMeta = document.selectFirst("meta[name=robots]");
                    if (robotsMeta != null) {
                        String content = robotsMeta.attr("content");
                        if (content == null || content.contains("nofollow"))
                            continue;
                    }

                    // Extract link from currentPage
                    List<Element> links = document.select("a");
                    List<String> linksHref = new LinkedList<>();
                    for (Element e : links) {
                        String link = e.attr("abs:href");
                        if (!link.isEmpty()) {
                            if (!link.contains("https://")) {
                                URLformatter urlToAdd = new URLformatter(link);
                                if (!urlToAdd.wasProcessed())
                                    urlQueue.add(urlToAdd);
                            }
                        }

                    }
                    
                    i++;
                    
                    log(" -> " + fullDomainName, true);
                } catch (URISyntaxException e) {
                	log("! error on link -> " + e.getMessage(), true);
                }
            }
            
            log("> Fisiere procesate: " + i, true);
            
            long endTime = System.nanoTime();

    		// get difference of two nanoTime values
    		long timeElapsed = endTime - startTime;

    		log("> Timpul de executie in milisecunde : ", false);
    		log(timeElapsed / 1000000., true);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
