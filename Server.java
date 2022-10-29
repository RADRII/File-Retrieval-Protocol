import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.*;

public class Server extends Node {
	static final int DEFAULT_PORT = 50001;
	static final int DEFAULT_CLIENT_DST_PORT = 50000;
	static final int DEFAULT_WORKER_DST_PORT = 50002;
	static final String DEFAULT_CLIENT_DNS = "client";
	static int WORKER_TO_PICK = 0;

	private HashMap<InetSocketAddress, String> requests = new HashMap<>();
	private ArrayList<InetSocketAddress> workers = new ArrayList<InetSocketAddress>();
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
				System.out.println("FILE CONTENT RECEIVED");
				System.out.println("File name: " + ((FileInfoContent)content).getFileName());
				System.out.println("File size: " + ((FileInfoContent)content).getFileSize());

				System.out.println("Sending ACK to Worker");

				DatagramPacket response;
				response= new AckPacketContent("OK - Server received file").toDatagramPacket();
				response.setSocketAddress(packet.getSocketAddress());
				socket.send(response);

				System.out.println("Packet sent");
				System.out.println("Sending file to client");

				InetSocketAddress clientdst = getDestinationFromFileName(((FileInfoContent)content).getFileName());
				if(clientdst == null)
				{
					throw new Exception("ERROR FILE NOT REQUESTED "+(((FileInfoContent)content).getFileName()));
				}

				packet.setSocketAddress(clientdst);
				socket.send(packet);
				System.out.println("Packet sent");

			}
			else if (content.getType()==PacketContent.FILEREQUEST)
			{
				if(workers.size() <= 0)
				{
					throw new Exception("ERROR SERVER HAS NO WORKERS");
				}

				System.out.println("FILE REQUEST: from " + packet.getSocketAddress());
				System.out.println("File name: " + ((FileRequestContent)content).getFileName());

				requests.put((InetSocketAddress) packet.getSocketAddress(), ((FileRequestContent)content).getFileName());

				if(WORKER_TO_PICK > workers.size() - 1)
					WORKER_TO_PICK = 0;
				InetSocketAddress workerdst = workers.get(WORKER_TO_PICK);
				WORKER_TO_PICK++;
				if(WORKER_TO_PICK > workers.size() - 1)
					WORKER_TO_PICK = 0;

				packet.setSocketAddress(workerdst);
				socket.send(packet);
				System.out.println("Packet sent to: " + workerdst);
			}
			else if(content.getType()==PacketContent.ACKPACKET)
			{
				System.out.println(content.toString());
				requests.remove(packet.getSocketAddress());
			}
			else if(content.getType()==PacketContent.CONNECTREQUEST)
			{
				System.out.println(content.toString());
				if(((ConnectContent) content).getPacketInfo().equals("CONNECT"))
				{
					workers.add((InetSocketAddress) packet.getSocketAddress());

					System.out.println("Connected - Sending ACK packet");
					DatagramPacket response;
					response= new AckPacketContent("OK - Connected to Server").toDatagramPacket();
					response.setSocketAddress(packet.getSocketAddress());
					socket.send(response);
				}
				else
				{
					if(workers.contains(packet.getSocketAddress()))
					{
						workers.remove(packet.getSocketAddress());
						System.out.println("REMOVED: " + packet.getSocketAddress());
					}
					else
					{
						throw new Exception("ERROR DISCONNECT NOT CONNECTED "+ (packet.getSocketAddress()));
					}
				}
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}

	private synchronized InetSocketAddress getDestinationFromFileName (String name)
	{
		for (Map.Entry<InetSocketAddress,String> entry : requests.entrySet()) {
			String requestFile = entry.getValue();

			if(requestFile.equals(name))
				return entry.getKey();
		}
		return null;
	}

	public synchronized void start() throws Exception {
		System.out.println("Waiting for contact");
		this.wait();
	}
	/* METHODS FOR TESTING REMOVE AT END */
	private synchronized void printHashMap ()
	{
		System.out.println("MAP");
		for (Map.Entry<InetSocketAddress,String> entry : requests.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
		return;
	}

	/*
	 * MAIN
	 */
	public static void main(String[] args) {
		try {
			(new Server(DEFAULT_PORT)).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
