import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.io.File;
import java.io.FileInputStream;

public class Worker extends Node
{
    static final int DEFAULT_SRC_PORT = 50002;
	static final int DEFAULT_DST_PORT = 50001;
    static final String DEFAULT_DST_NODE = "server";
    InetSocketAddress dstAddress;

    /**
	 * Constructor
	 *
	 * Attempts to create socket at given port and create an InetSocketAddress for the destinations
	 */
	Worker(String dstHost, int dstPort, int srcPort) {
		try {
			dstAddress= new InetSocketAddress(dstHost, dstPort);
			socket= new DatagramSocket(srcPort);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}
    
    /**
	 * Assume that incoming packets contain a String and print the string.
	 */
	public synchronized void onReceipt(DatagramPacket packet) {
		try {
			System.out.println("Received packet");

			PacketContent content= PacketContent.fromDatagramPacket(packet);

			if (content.getType()==PacketContent.FILEREQUEST) {
                String fname = ((FileRequestContent)content).getFileName();
				System.out.println("File name: " + fname);

                File file= null;
                FileInputStream fin= null;
                FileInfoContent fcontent;
                int size;
                byte[] buffer= null;
                DatagramPacket filepacket= null;

                file = new File(fname);	// Reserve buffer for length of file and read file
                buffer= new byte[(int) file.length()];
		        fin= new FileInputStream(file);
		        size= fin.read(buffer);
                if (size==-1) {
                    fin.close();
                    throw new Exception("Problem with File Access:"+fname);
                }
                System.out.println("File size: " + buffer.length);

                fcontent= new FileInfoContent(fname, size);

                System.out.println("Sending file content w/ name & length"); // Send packet with file name and length
                filepacket= fcontent.toDatagramPacket();
                filepacket.setSocketAddress(dstAddress);
                socket.send(filepacket);
                System.out.println("Packet sent");
				fin.close();
			}
            else if (content.getType()==PacketContent.ACKPACKET) {
                System.out.println(content.toString());
            }
		}
		catch(Exception e) {e.printStackTrace();}
	}

    public synchronized void start() throws Exception {
		System.out.println("Waiting for contact");
		this.wait();
	}

    public static void main(String[] args) {
		try {
			(new Worker(DEFAULT_DST_NODE, DEFAULT_DST_PORT, DEFAULT_SRC_PORT)).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
