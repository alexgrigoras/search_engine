package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import sun.rmi.runtime.Log;

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

public class SequentialCrawler {
    private static HashMap<String, String> _domanIp = new HashMap<>();

    /**
     * Main function for Sequential Crawler
     * @param args
     */
    public static void main(String[] args) {
    	
    	boolean logData = false;

        Queue<URLformatter> urlQueue = new LinkedList<>();
        try {
            int i = 0;

            urlQueue.add(new URLformatter("http://riweb.tibeica.com/crawl/"));

            while (!urlQueue.isEmpty()) {
                try {
                    // get head of the queue to process
                	URLformatter processedURL = urlQueue.peek();
                    urlQueue.remove();

                    String fullDomainName = processedURL.get_fullDomainName();
                    String scheme = processedURL.get_scheme();
                    String domain = processedURL.get_domain();
                    String localPath = processedURL.get_localPath();
                    String page = processedURL.get_page();
                    String filesFolder = processedURL.get_filesFolder();
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
                    
                    System.out.println("> Fisiere procesate: " + (++i) + " -> " + fullDomainName);
                } catch (URISyntaxException e) {
                	System.out.println("! error on link -> " + e.getMessage());
                }
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
