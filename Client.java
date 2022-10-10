import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

/**
 *
 * Client class
 *
 * An instance accepts user input
 *
 */
public class Client extends Node {
	static final int DEFAULT_SRC_PORT = 50000;
	static final int DEFAULT_DST_PORT = 50001;
	static final String DEFAULT_DST_NODE = "server";
	InetSocketAddress dstAddress;

	/**
	 * Constructor
	 *
	 * Attempts to create socket at given port and create an InetSocketAddress for the destinations
	 */
	Client(String dstHost, int dstPort, int srcPort) {
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

			if (content.getType()==PacketContent.FILEINFO) {
				System.out.println("File name: " + ((FileInfoContent)content).getFileName());
				System.out.println("File size: " + ((FileInfoContent)content).getFileSize());

				DatagramPacket response;
				response= new AckPacketContent("OK - Client received file").toDatagramPacket();
				response.setSocketAddress(packet.getSocketAddress());
				socket.send(response);
				this.notify();
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}


	/**
	 * Sender Method
	 *
	 */
	public synchronized void start() throws Exception {
		String fname;
		String fnametwo;
		FileRequestContent frequest;
		FileRequestContent frequesttwo;

		DatagramPacket packet= null;
		DatagramPacket packettwo= null;

		fname= "message.txt";//terminal.readString("Name of file: ");
		fnametwo= "secondmessage.txt";

		frequest = new FileRequestContent(fname);

		System.out.println("Sending request for file message.txt");

		packet= frequest.toDatagramPacket();
		packet.setSocketAddress(dstAddress);
		socket.send(packet);
		System.out.println("First request sent");
		this.wait();

		frequesttwo = new FileRequestContent(fnametwo);

		System.out.println("Sending request for file secondmessage.txt");
		packettwo= frequesttwo.toDatagramPacket();
		packettwo.setSocketAddress(dstAddress);
		socket.send(packettwo);
		System.out.println("Second request sent");
		this.wait();

	}


	/**
	 * Test method
	 *
	 * Sends a packet to a given address
	 */
	public static void main(String[] args) {
		try {
			(new Client(DEFAULT_DST_NODE, DEFAULT_DST_PORT, DEFAULT_SRC_PORT)).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
