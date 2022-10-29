import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Class for packet content that represents acknowledgements
 *
 */
public class ConnectContent extends PacketContent {

	String info;

	/**
	 * Constructor that takes in information about a file.
	 * @param filename Initial filename.
	 * @param size Size of filename.
	 */
	ConnectContent(String info) {
		type= CONNECTREQUEST;
		this.info = info;
	}

	/**
	 * Constructs an object out of a datagram packet.
	 * @param packet Packet that contains information about a file.
	 */
	protected ConnectContent(ObjectInputStream oin) {
		try {
			type= CONNECTREQUEST;
			info= oin.readUTF();
		}
		catch(Exception e) {e.printStackTrace();}
	}

	/**
	 * Writes the content into an ObjectOutputStream
	 *
	 */
	protected void toObjectOutputStream(ObjectOutputStream oout) {
		try {
			oout.writeUTF(info);
		}
		catch(Exception e) {e.printStackTrace();}
	}



	/**
	 * Returns the content of the packet as String.
	 *
	 * @return Returns the content of the packet as String.
	 */
	public String toString() {
		return "CONNECT OR DISCONNECT REQUEST: " + info;
	}

	/**
	 * Returns the info contained in the packet.
	 *
	 * @return Returns the info contained in the packet.
	 */
	public String getPacketInfo() {
		return info;
	}
}
