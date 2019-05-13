package crawler;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URISyntaxException;

/**
 * HTTP client class for sending requests to specified URL
 *
 */
public class HTTPclient {
	private String ipAddress;
    private URLformatter urlFormatter;
    private int nrOfRedirect = 0;
    private StringBuilder httpRequest;
    private boolean logData;
    
    /**
     * Class constructor
     * @param _url: url address of site
     * @param _ipAddress: ip address of site from dns
     */
    public HTTPclient(String _url, String _ipAddress, boolean _logData) {
    	ipAddress = _ipAddress;

		try {
			urlFormatter = new URLformatter(_url);
		} catch (URISyntaxException e) {
			System.out.println(e.getMessage());
		}
		
		logData = _logData;
    }
    
    /**
	 * Log data to console for a string
	 * @param msg: message to be written on the console
	 * @param newLine: if true newline is added to the end; else nothing happens
	 */
	private void log(String msg, boolean newLine) {
		if(logData) {
			if(newLine) {
				System.out.println(msg);
	        }
			else {
				System.out.print(msg);
			}
		}
	}
    
	/**
	 * Build the HTTP request with method, URL and headers
	 */
    private void buildRequest() {
        httpRequest = new StringBuilder();

        httpRequest.append("GET ");
        httpRequest.append(urlFormatter.get_localPathStr() + " ");
        httpRequest.append("HTTP/1.1\r\n");

        httpRequest.append("Host: ");
        httpRequest.append(urlFormatter.get_domain());
        httpRequest.append("\r\n");

        httpRequest.append("User-Agent: CLIENT RIW\r\n");

        httpRequest.append("Connection: close\r\n");

        httpRequest.append("\r\n");
        
        log("> Building request:\n" + httpRequest.toString(), true);
    }

    /**
     * Send the request to the specified resource
     * @return true for success or false for fail
     * @throws IOException
     */
    public boolean sendRequest() throws IOException {
        buildRequest();
        Socket socket = new Socket(ipAddress, urlFormatter.get_port());

        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.print(httpRequest);
        pw.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String t = br.readLine();
        
        log("> Response: " + t, true);
        
        try {
            if (t.contains("HTTP/1.1 301 Moved Permanently")) {
            	
            	log("> Response code: 301", true);
            	
                nrOfRedirect++;
                if (nrOfRedirect > 6) {
                    throw new Exception("Too many redirects!");
                }
                t = br.readLine();
                if (t.contains("Location")) {
                    final String separator = "://";
                    int index = 0;
                    StringBuilder newLocation = new StringBuilder();
                    for (Character c : t.toCharArray()) {
                        if (index > separator.length()) {
                            if (c.equals('/'))
                                break;
                            newLocation.append(c);
                        }

                        if (c == separator.charAt(index))
                            index++;
                        else
                            index = 0;
                    }

                    if (index < separator.length()) {
                        throw new Exception("Invalid Location Header!");
                    }

                    urlFormatter.set_domain(newLocation.toString());
                    return sendRequest();
                }  
            } else if (!t.contains("HTTP/1.1 200 OK")) {
            	
            	log("> Response code: not 200", true);
            	
                throw new Exception("Error request!");
            } else {
            	
            	log("> Response code: 200", true);
            	
                boolean flag = false;
                urlFormatter.buildFolderPath();
                
                File output = new File(urlFormatter.get_filesFolder() + urlFormatter.get_scheme() + urlFormatter.get_domain() + urlFormatter.get_localPath() + "/" + urlFormatter.get_page());
                if (!output.exists())
                    output.createNewFile();

                BufferedWriter writer = new BufferedWriter(new FileWriter(output));
                while ((t = br.readLine()) != null) {
                    if (t.trim().isEmpty())
                        flag = true;
                    if (flag)
                        writer.write(t + "\r\n");
                }
                writer.close();
            }
            br.close();

        } catch (Exception e) {
        	
        	log("> Errors written in error.txt", true);
        	
            File output = new File("error.txt");
            if (!output.exists())
                output.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            writer.write(e.getMessage() + "\r\n\r\n");
            writer.write(httpRequest.toString());

            writer.write(t + "\r\n");
            while ((t = br.readLine()) != null) {
                if (t.trim().isEmpty())
                    break;
                writer.write(t + "\r\n");
            }
            writer.close();
            
            return false;
        }
        
        return true;
    }

    /**
     * Check for robots exclusion protocol - NOT IMPLEMENTED YET
     * @return true if robots allow crawling or false if not
     * @throws IOException
     */
    public boolean checkForRobosts() throws IOException {
    	return true;
    }
    
    /**
     * Main function for testing
     * @param args: arguments from command line
     */
	public static void main(String[] args) {
		String url = "http://riweb.tibeica.com/crawl/inst-prerequisites.html";
		String ipAddress = "67.207.88.228";
		boolean logData = true;
		
		HTTPclient client = new HTTPclient(url, ipAddress, logData);
		
		client.log("> Making request for [" + url + "] with address [" + ipAddress + "]", true);
		
		try {
			if (client.sendRequest()) {
				System.out.println("> Request completed successfully");
			}
			else {
				System.out.println("> Request incomplete");
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

}