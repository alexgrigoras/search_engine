package dns;

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
    private String domain;						// URL address
    private String ipAddr;						// DNS Server IP address
	private int dnsServerPort;					// DNS Server Port

	/**
	 * Class constructor
	 * @param _domain: URL address
	 * @param _ipAddr: DNS Server IP address
	 * @param _dnsServerPort: DNS Server Port
	 */
    public DnsClient(String _domain, String _ipAddr, int _dnsServerPort) {
        domain = _domain;
        ipAddr = _ipAddr;
        dnsServerPort = _dnsServerPort;
    }
    
    /**
     * Returns the IP address of a URL
     * @return IP address
     * @throws IOException
     */
    public String getIpAddres() throws IOException {
        InetAddress ipAddress = InetAddress.getByName(ipAddr);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        /*
         * Build a DNS Request Frame
         */        
        dos.writeShort(0x028d);			// Identifier: 16-bit  
        dos.writeShort(0x0100);			// Query Flags -> only RD = 1
        dos.writeShort(0x0001);			// Question Count: Specifies the number of questions in the Question section of the message.
        dos.writeShort(0x0000);			// Answer Record Count: Specifies the number of resource records in the Answer section of the message.
        dos.writeShort(0x0000);			// Authority Record Count: Specifies the number of resource records in the Authority section of the message. (“NS” stands for “name server”)
        dos.writeShort(0x0000);	        // Additional Record Count: Specifies the number of resource records in the Additional section of the message.
        // Question name
        String[] domainParts = domain.split("\\.");
        System.out.print("URL: " + domain + " are " + domainParts.length + " parti: ");
        for (int i = 0; i<domainParts.length; i++) {
            System.out.print("[" + domainParts[i] + "] ");
            byte[] domainBytes = domainParts[i].getBytes("UTF-8");
            dos.writeByte(domainBytes.length);
            dos.write(domainBytes);
        }
        dos.writeByte(0x00);        	// Last byte = 0      
        dos.writeShort(0x0001);			// QType 0x01 = A (Host Request)
        dos.writeShort(0x0001);			// QClass 0x01 = IN

        byte[] dnsFrame = baos.toByteArray();
        System.out.println("\n\nTrimitere " + dnsFrame.length + " bytes:");
        System.out.print("\t");
        for (int i=0; i< dnsFrame.length; i++) {
            System.out.print("0x" + String.format("%x", dnsFrame[i]) + " " );
        }

        /*
         * Send DNS Request Frame
         */
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket dnsReqPacket = new DatagramPacket(dnsFrame, dnsFrame.length, ipAddress, dnsServerPort);
        socket.send(dnsReqPacket);

        /*
         * Await response from DNS server
         */
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        socket.close();
        
        System.out.println("\n\nPrimire " + packet.getLength() + " bytes:");
        System.out.print("\t");
        for (int i = 0; i < packet.getLength(); i++) {
            System.out.print("0x" + String.format("%x", buf[i]) + " ");
        }
        System.out.println("\n");
        
        /*
         * Display record message
         */
        System.out.println("Campuri mesaj primit: ");
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(buf));
        System.out.println("\tIdentifier: 0x" + String.format("%x", din.readShort()));
        System.out.println("\tFlags and codes: 0x" + String.format("%x", din.readShort()));
        System.out.println("\tQuestion count: 0x" + String.format("%x", din.readShort()));
        System.out.println("\tAnswer Record Count: 0x" + String.format("%x", din.readShort()));
        System.out.println("\tName Server (Authority Record) Count: 0x" + String.format("%x", din.readShort()));
        System.out.println("\tAdditional Record Count: 0x" + String.format("%x", din.readShort()));
        System.out.print("\tQuestion name: ");
        int recLen = 0;
        while ((recLen = din.readByte()) > 0) {
            byte[] record = new byte[recLen];
            for (int i = 0; i < recLen; i++) {
                record[i] = din.readByte();
            }
            System.out.print(new String(record, "UTF-8") + " ");
        }
        System.out.println("\n\tQType: 0x" + String.format("%x", din.readShort()));
        System.out.println("\tQClass: 0x" + String.format("%x", din.readShort()));
        System.out.println("\tName: 0x" + String.format("%x", din.readShort()));
        System.out.println("\tType: 0x" + String.format("%x", din.readShort()));
        System.out.println("\tClass: 0x" + String.format("%x", din.readShort()));
        System.out.println("\tTTL: 0x" + String.format("%x", din.readInt()));
        short addrLen = din.readShort();
        System.out.println("\tRDLenght: 0x" + String.format("%x", addrLen));
        System.out.print("\tRData: ");
        StringBuilder ipAddres = new StringBuilder();
        for (int i = 0; i < addrLen; i++ ) {
        	int byteRead = din.readByte() & 0xFF;
            System.out.print("" + String.format("%d", byteRead));
            ipAddres.append(String.format("%d", byteRead));
            if(i < addrLen - 1) {
            	System.out.print(".");
            	ipAddres.append(".");
            }
        }
        System.out.println();
        
        return ipAddres.toString();
    }
	
    /**
     * Main function
     * @param args: arguments from the command line
     */
	public static void main(String[] args) {
		String domain = "www.google.com";
		String ipAddress = "8.8.8.8";
		int dnsServPort = 53;
		
		DnsClient dnsClient = new DnsClient(domain, ipAddress, dnsServPort);
		
        try {
			ipAddress = dnsClient.getIpAddres();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        System.out.println("\nAdresa IP a domeniului: " + ipAddress);
	}

}
