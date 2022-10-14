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

	Terminal workerTerminal;

    /**
	 * Constructor
	 *
	 * Attempts to create socket at given port and create an InetSocketAddress for the destinations
	 */
	Worker(Terminal t, String dstHost, int dstPort, int srcPort) {
		try {
			workerTerminal = t;
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
			workerTerminal.println("Received packet");

			PacketContent content= PacketContent.fromDatagramPacket(packet);

			if (content.getType()==PacketContent.FILEREQUEST) {
                String fname = ((FileRequestContent)content).getFileName();
				workerTerminal.println("REQUEST - File name: " + fname);

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
                    throw new Exception("ERROR: Problem with File Access:"+fname);
                }
                workerTerminal.println("File size: " + buffer.length);

                fcontent= new FileInfoContent(fname, size);

                workerTerminal.println("Sending file content w/ name & length"); // Send packet with file name and length
                filepacket= fcontent.toDatagramPacket();
                filepacket.setSocketAddress(dstAddress);
                socket.send(filepacket);
                workerTerminal.println("Packet sent");
				fin.close();
			}
            else if (content.getType()==PacketContent.ACKPACKET) {
                workerTerminal.println(content.toString());
				this.notify();
            }
		}
		catch(Exception e) {e.printStackTrace();}
	}

    public synchronized void start() throws Exception {
		workerTerminal.println("Connecting to Server");

		DatagramPacket connect= new ConnectContent("CONNECT").toDatagramPacket();
		connect.setSocketAddress(dstAddress);
		socket.send(connect);
		this.wait();

		workerTerminal.println("Worker Online, type 'quit' to shutdown, anything else to open the worker,");
		workerTerminal.println("Or wait 10 seconds.");
		while (true) 
		{
			String userInput = workerTerminal.read("Type quit Here");
			if(userInput != null && userInput.equals("quit"))
			{
				workerTerminal.println("Sending Disconnect request to server");
				DatagramPacket disconnect;
				disconnect= new ConnectContent("DISCONNECT").toDatagramPacket();
				disconnect.setSocketAddress(dstAddress);
				socket.send(disconnect);
				System.exit(0);
			}
			this.wait();
			workerTerminal.println("Action Completed, quit worker now?");
		}
	}

    public static void main(String[] args) {
		try {
			Terminal workerTerminal = new Terminal("Worker");
			(new Worker(workerTerminal, DEFAULT_DST_NODE, DEFAULT_DST_PORT, DEFAULT_SRC_PORT)).start();
			workerTerminal.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
