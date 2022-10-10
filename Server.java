import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class Server extends Node {
	static final int DEFAULT_PORT = 50001;
	static final int DEFAULT_CLIENT_DST_PORT = 50000;
	static final int DEFAULT_WORKER_DST_PORT = 50002;
	static final String[] WORKER_DNS = {"workerone", "workertwo", "workerthree"};
	static final String CLIENT_DNS = "client";
	static int WORKER_TO_PICK = 0;
	/*
	 *
	 */
	Server(int port) {
		try {
			socket= new DatagramSocket(port);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

	/**
	 * Assume that incoming packets contain a String and print the string.
	 */
	public void onReceipt(DatagramPacket packet) {
		try {
			System.out.println("Received packet");

			PacketContent content= PacketContent.fromDatagramPacket(packet);

			if (content.getType()==PacketContent.FILEINFO) {
				System.out.println("File name: " + ((FileInfoContent)content).getFileName());
				System.out.println("File size: " + ((FileInfoContent)content).getFileSize());

				DatagramPacket response;
				response= new AckPacketContent("OK - Server received file").toDatagramPacket();
				response.setSocketAddress(packet.getSocketAddress());
				socket.send(response);

				System.out.println("Sending packing to client");

				InetSocketAddress clientdst = new InetSocketAddress(CLIENT_DNS, DEFAULT_CLIENT_DST_PORT);
				packet.setSocketAddress(clientdst);
				socket.send(packet);
				System.out.println("Packet sent");

			}
			else if (content.getType()==PacketContent.FILEREQUEST)
			{
				System.out.println("REQUEST");
				System.out.println("File name: " + ((FileRequestContent)content).getFileName());

				InetSocketAddress workerdst = new InetSocketAddress(WORKER_DNS[WORKER_TO_PICK], DEFAULT_WORKER_DST_PORT);
				WORKER_TO_PICK++;
				if(WORKER_TO_PICK > 2)
					WORKER_TO_PICK = 0;

				packet.setSocketAddress(workerdst);
				socket.send(packet);
				System.out.println("Packet sent");
			}
			else if(content.getType()==PacketContent.ACKPACKET)
			{
				System.out.println(content.toString());
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}


	public synchronized void start() throws Exception {
		System.out.println("Waiting for contact");
		this.wait();
	}

	/*
	 *
	 */
	public static void main(String[] args) {
		try {
			(new Server(DEFAULT_PORT)).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
