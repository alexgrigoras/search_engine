package crawler;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URISyntaxException;

/**
 * Enum for selecting the request type:
 * Resource - get the html page
 * Robots - get the robots.txt file
 * @author alex_
 *
 */
enum RequestType {
	Resource,
	Robots
}

/**
 * HTTP client class for sending requests to specified URL
 * @author alex_
 * 
 */
public class HTTPclient {
	private String _ipAddress;
	private String _userAgent;
    private URLformatter _urlFormatter;
    private int _nrOfRedirect = 0;
    private StringBuilder _httpRequest;
    private boolean _logData;
    
    /**
     * Class constructor
     * @param _url: url address of site
     * @param _ipAddress: ip address of site from dns
     */
    public HTTPclient(String url, String ipAddress, String userAgent, boolean logData) {
    	_ipAddress = ipAddress;
    	_userAgent = userAgent;

		try {
			_urlFormatter = new URLformatter(url);
		} catch (URISyntaxException e) {
			System.out.println(e.getMessage());
		}
		
		_logData = logData;
    }
    
    /**
	 * Log data to console for a string
	 * @param msg: message to be written on the console
	 * @param newLine: if true newline is added to the end; else nothing happens
	 */
	private void log(String msg, boolean newLine) {
		if(_logData) {
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
	private void buildRequest(RequestType type) {
        _httpRequest = new StringBuilder();

        _httpRequest.append("GET ");
        
        if (type == RequestType.Resource)
        {
        	_httpRequest.append(_urlFormatter.getLocalPathStr() + " ");	
        }
        else if (type == RequestType.Robots)
        {
        	_httpRequest.append("/robots.txt ");
        }        
        
        _httpRequest.append("HTTP/1.1\r\n");

        _httpRequest.append("Host: ");
        _httpRequest.append(_urlFormatter.getDomain());
        _httpRequest.append("\r\n");

        _httpRequest.append("User-Agent: " + _userAgent + "\r\n");

        _httpRequest.append("Connection: close\r\n");

        _httpRequest.append("\r\n");
        
        log("> Building request:\n" + _httpRequest.toString(), true);
    }

    /**
     * Send the request to the specified resource
     * @return true for success or false for fail
     * @throws IOException
     */
    public boolean sendRequest() throws IOException {
    	buildRequest(RequestType.Resource);
        Socket socket = new Socket(_ipAddress, _urlFormatter.getPort());
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        
        pw.print(_httpRequest);
        pw.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String t = br.readLine();
        
        log("> Response: " + t, true);
        
        try {
            if (t.contains("HTTP/1.1 301 Moved Permanently")) {
            	
            	log("> Response code: 301", true);
            	
                _nrOfRedirect++;
                if (_nrOfRedirect > 6) {
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
                        throw new Exception("> Invalid Location Header!");
                    }

                    _urlFormatter.setDomain(newLocation.toString());
                    return sendRequest();
                }  
            } else if (!t.contains("HTTP/1.1 200 OK")) {
            	
            	log("> Response code: not 200", true);
            	
                throw new Exception("Error request!");
            } else {
            	
            	log("> Response code: 200", true);
            	
                boolean flag = false;
                _urlFormatter.buildFolderPath();
                
                File output = new File(_urlFormatter.getFilesFolder() + _urlFormatter.getScheme() + _urlFormatter.getDomain() + _urlFormatter.getLocalPath() + "/" + _urlFormatter.getResource());
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
            writer.write(_httpRequest.toString());

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
     * Check for robots exclusion protocol
     * @return true if robots allow crawling or false if not
     * @throws IOException
     */
    public boolean checkForRobots() throws IOException {
    	buildRequest(RequestType.Robots);    	
        Socket socket = new Socket(InetAddress.getByName(_ipAddress), _urlFormatter.getPort());
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        
        pw.print(_httpRequest);
        pw.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        String line;
        
    	boolean currentCrawler = false;
        
        while ((line = br.readLine()) != null) {
        	if (line.contains("User-agent:")) {
        		if(_userAgent.equals(line.substring(line.lastIndexOf(" ")+1))) {
        			currentCrawler = true;
        		}
        		else {
        			currentCrawler = false;
        		}
        	}
            if (line.contains("Disallow:")) {
            	if(currentCrawler) {
            		if (line.contains("/"))
            			return false;
	                if (line.contains(_urlFormatter.getLocalPathStr()))
	                    return false;
            	}
            }
        }
        return true;
    }
    
    /**
     * Main function for testing
     * @param args: arguments from command line
     */
	public static void main(String[] args) {
		String url = "http://riweb.tibeica.com/modpython.html";
		String ipAddress = "67.207.88.228";
		String userAgent = "RIWEB_CRAWLER";
		boolean logData = true;
		
		HTTPclient client = new HTTPclient(url, ipAddress, userAgent, logData);
		
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