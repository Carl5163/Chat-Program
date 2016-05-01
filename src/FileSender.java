import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class FileSender implements Runnable {

	private File source;
	private String ip;
	private int port;
	private BufferedReader in;
	private DataInputStream fileIn;
	private DataOutputStream out;
	private byte[] buffer;
	private long fileSize;
	
	public FileSender(File file, long l, String ip, int port) {
		source = file;
		buffer = new byte[128];
		fileSize = l;
		this.ip = ip;
		this.port = port;
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		try {
			Socket socket = new Socket(ip, port);
			fileIn = new DataInputStream(new FileInputStream(source));
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());
			System.out.println("Successfully made direct connection and got streams.");
			
			int bytesRead = fileIn.read(buffer);
			int totalBytesSent = bytesRead;			

			while(bytesRead != -1) {			
				out.write(buffer);
				buffer = new byte[128];	
				System.out.printf("Sent %d/%d bytes\n", totalBytesSent, fileSize);
				bytesRead = fileIn.read(buffer);
				totalBytesSent += bytesRead;
			}
			out.flush();
			fileIn.close();
			
			System.out.println("Waiting on completion");
			in.readLine();			
			System.out.println("Finished sending file!");
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
