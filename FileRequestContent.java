import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Class for packet content that represents a request for file information
 *
 */
public class FileRequestContent extends PacketContent {
    String filename;

    /**
	 * Constructor that takes in information about a file.
	 * @param filename Initial filename.
	 */
	FileRequestContent(String filename) {
		type= FILEREQUEST;
		this.filename = filename;
	}

    /**
	 * Constructs an object out of a datagram packet.
	 * @param packet Packet that contains information about a file.
	 */
    protected FileRequestContent(ObjectInputStream oin) {
		try {
			type= FILEREQUEST;
			filename= oin.readUTF();
		}
		catch(Exception e) {e.printStackTrace();}
	}

    /**
	 * Writes the content into an ObjectOutputStream
	 *
	 */
	protected void toObjectOutputStream(ObjectOutputStream oout) {
		try {
			oout.writeUTF(filename);
		}
		catch(Exception e) {e.printStackTrace();}
	}

    /**
	 * Returns the content of the packet as String.
	 *
	 * @return Returns the content of the packet as String.
	 */
	public String toString() {
		return "REQUEST: " + filename ;
	}
    
    /**
	 * Returns the file name contained in the packet.
	 *
	 * @return Returns the file name contained in the packet.
	 */
	public String getFileName() {
		return filename;
	}
}