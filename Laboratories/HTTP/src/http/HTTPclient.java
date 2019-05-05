package http;

import javax.xml.crypto.URIReferenceException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URISyntaxException;

public class HTTPclient {
	private String ipAddress;
    private URLformatter urlFormatter;
    private int nrOfRedirect = 0;
    private StringBuilder httpRequest;
    
    public HTTPclient(URLformatter _urlFormatter, String _ipAdress) {
    	ipAddress = _ipAdress;
    	urlFormatter = _urlFormatter;
    }
    
    private void buildHttpRequest(boolean isRobots) {
        httpRequest = new StringBuilder();
        // first line
        httpRequest.append("GET ");
        if (isRobots)
            httpRequest.append("/robots.txt");
        else
            httpRequest.append(urlFormatter.get_localPathStr());
        httpRequest.append(" HTTP/1.1\r\n");

        // second line
        httpRequest.append("Host: ");
        httpRequest.append(urlFormatter.get_domain());
        httpRequest.append("\r\n");

        // third line
        httpRequest.append("User-Agent: CLIENTRIW\r\n");

        // forth line
        httpRequest.append("Connection: close\r\n");

        // fifth line
        //_httpRequest.append("If-Modified-Since: "); // TO DO

        // sixth line
        httpRequest.append("\r\n");
    }

    public boolean checkForRobots() throws IOException {
        buildHttpRequest(true);
        Socket socket = new Socket(InetAddress.getByName(ipAddress), urlFormatter.get_port());

        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.print(httpRequest);
        pw.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains("Disallow:"))
                if (line.contains(urlFormatter.get_localPathStr()))
                    return false;
        }
        return true;
    }

    public boolean sendRequest() throws IOException {
        buildHttpRequest(false);
        Socket socket = new Socket(InetAddress.getByName(ipAddress), urlFormatter.get_port());

        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.print(httpRequest);
        pw.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String t = br.readLine();
        try {
            if (t.contains("HTTP/1.1 301 Moved Permanently")) {
                nrOfRedirect++;
                if (nrOfRedirect > 6) {
                    throw new Exception("Too many redirects!");
                }
                t = br.readLine();
                if (t.contains("Location")) {
                    final String separator = "://";
                    int index = 0;
                    StringBuilder newLocation = new StringBuilder();
                    boolean flag = false;
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
                throw new Exception("Error request!");
            } else {
                boolean flag = false;
                urlFormatter.buildFolderPath();
                File output = new File(urlFormatter.get_domain() + urlFormatter.get_localPath() + "/" + urlFormatter.get_page());
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

	public static void main(String[] args) {
		String url = "http://tuiasi.ro";	//"http://riweb.tibeica.com/crawl";
		String ipAddress = "81.180.223.1";
		
		URLformatter processedURL;
		try {
			processedURL = new URLformatter(url);

			HTTPclient httpClient = new HTTPclient(processedURL, ipAddress);
			
			try {
				if (!httpClient.sendRequest()) {
					System.out.println("> Request sent successfully");
				}
				else {
					System.out.println("> Invalid request");
				}
			} catch (IOException e) {
				System.out.println("! Invalid request");
			}
                
			
		} catch (URISyntaxException e) {
			System.out.println("! Invalid HTTP client");
		}
	}

}
