package crawler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 * DNS client for getting the IP address of a URL address
 * @author alex_
 *
 */
public class DnsClient {
    private String _domain;						// URL address
    private String _ipAddr;						// DNS Server IP address
	private int _dnsServerPort;					// DNS Server Port
	private boolean _logData; 					// Log data flag

	/**
	 * Class constructor
	 * @param _domain: URL address
	 * @param _ipAddr: DNS Server IP address
	 * @param _dnsServerPort: DNS Server Port
	 */
    public DnsClient(String domain, String ipAddr, int dnsServerPort, boolean logData) {
        _domain = domain;
        _ipAddr = ipAddr;
        _dnsServerPort = dnsServerPort;
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
     * Returns the IP address of a URL
     * @return IP address: the IP address of the site
     * @throws IOException: exception thrown by the function
     */
    public String getIpAddres() throws IOException {
        InetAddress ipAddress = InetAddress.getByName(_ipAddr);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        /*
         * Build a DNS Request Frame
         */        
        dos.writeShort(0x1234);			// Identifier: 16-bit  
        dos.writeShort(0x0100);			// Query Flags -> only RD = 1
        dos.writeShort(0x0001);			// Question Count: Specifies the number of questions in the Question section of the message.
        dos.writeShort(0x0000);			// Answer Record Count: Specifies the number of resource records in the Answer section of the message.
        dos.writeShort(0x0000);			// Authority Record Count: Specifies the number of resource records in the Authority section of the message. (“NS” stands for “name server”)
        dos.writeShort(0x0000);	        // Additional Record Count: Specifies the number of resource records in the Additional section of the message.
        // Question name
        String[] domainParts = _domain.split("\\.");
        log("> URL: \"" + _domain + "\" are " + domainParts.length + " parti: ", false);
        for (int i = 0; i<domainParts.length; i++) {
            log("[" + domainParts[i] + "]", false);
            if(i<domainParts.length - 1) {
            	log(", ", false);
            }
            byte[] domainBytes = domainParts[i].getBytes("UTF-8");
            dos.writeByte(domainBytes.length);
            dos.write(domainBytes);
        }
        dos.writeByte(0x00);        	// Last byte = 0      
        dos.writeShort(0x0001);			// QType 0x01 = A (Host Request)
        dos.writeShort(0x0001);			// QClass 0x01 = IN

        byte[] dnsFrame = baos.toByteArray();
        log("\n\n> Trimitere " + dnsFrame.length + " bytes:", true);
        log("\t", false);
        for (int i=0; i< dnsFrame.length; i++) {
            log("0x" + String.format("%x", dnsFrame[i]) + " ", false);
        }

        /*
         * Send DNS Request Frame
         */
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket dnsReqPacket = new DatagramPacket(dnsFrame, dnsFrame.length, ipAddress, _dnsServerPort);
        socket.send(dnsReqPacket);

        /*
         * Await response from DNS server
         */
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        socket.close();
        
        log("\n\n< Primire " + packet.getLength() + " bytes:", true);
        log("\t", false);
        for (int i = 0; i < packet.getLength(); i++) {
            log("0x" + String.format("%x", buf[i]) + " ", false);
        }
        log("\n", true);
        
        /*
         * Display record message
         */
        log("< Campuri mesaj primit: ", true);
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(buf));
        log("\tIdentifier: 0x" + String.format("%x", din.readShort()), true);
        log("\tFlags and codes: 0x" + String.format("%x", din.readShort()), true);
        log("\tQuestion count: 0x" + String.format("%x", din.readShort()), true);
        log("\tAnswer Record Count: 0x" + String.format("%x", din.readShort()), true);
        log("\tName Server (Authority Record) Count: 0x" + String.format("%x", din.readShort()), true);
        log("\tAdditional Record Count: 0x" + String.format("%x", din.readShort()), true);
        log("\tQuestion name: ", false);
        int recLen = 0;
        while ((recLen = din.readByte()) > 0) {
            byte[] record = new byte[recLen];
            for (int i = 0; i < recLen; i++) {
                record[i] = din.readByte();
            }
            log(new String(record, "UTF-8") + " ", false);
        }
        log("\n\tQType: 0x" + String.format("%x", din.readShort()), true);
        log("\tQClass: 0x" + String.format("%x", din.readShort()), true);
        log("\tName: 0x" + String.format("%x", din.readShort()), true);
        log("\tType: 0x" + String.format("%x", din.readShort()), true);
        log("\tClass: 0x" + String.format("%x", din.readShort()), true);
        log("\tTTL: 0x" + String.format("%x", din.readInt()), true);
        short addrLen = din.readShort();
        log("\tRDLenght: 0x" + String.format("%x", addrLen), true);
        log("\tRData: ", false);
        StringBuilder ipAddres = new StringBuilder();
        for (int i = addrLen - 4; i < addrLen; i++ ) {
        	int byteRead = din.readByte() & 0xFF;
            log("" + String.format("%d", byteRead), false);
            ipAddres.append(String.format("%d", byteRead));
            if(i < addrLen - 1) {
            	log(".", false);
            	ipAddres.append(".");
            }
        }
        log("\n", true);
        
        return ipAddres.toString();
    }
	
    /**
     * Main function
     * @param args: arguments from the command line
     */
	public static void main(String[] args) {
		String domain = "http://riweb.tibeica.com/crawl/";
		String ipAddress = "8.8.8.8";
		int dnsServPort = 53;
		boolean logDataFlag = true;
		
		DnsClient dnsClient = new DnsClient(domain, ipAddress, dnsServPort, logDataFlag);
		
        try {
			ipAddress = dnsClient.getIpAddres();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        System.out.println("> Adresa IP a domeniului: " + ipAddress);
	}

}
