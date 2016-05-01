import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileReceiver implements Runnable {

	private File destination;
	private int port;
	private static int nextPort = 3000;
	private byte[] buffer;
	private long fileSize;
	private DataInputStream in;
	private DataOutputStream fileOut;
	private DataOutputStream out;
	
	public FileReceiver(File file, long size) {
		buffer = new byte[128];
		fileSize = size;
		destination = file;
		port = nextPort;
		nextPort++;
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			Socket socket = serverSocket.accept();
			
			fileOut = new DataOutputStream(new FileOutputStream(destination));
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
			System.out.println("Successfully made direct connection and got streams.");
			
			int bytesRead = 0;
			
			while(bytesRead < fileSize) {			
				bytesRead += in.read(buffer);
				fileOut.write(buffer);
				buffer = new byte[128];	
				System.out.printf("Recieved %d/%d bytes\n", bytesRead, fileSize);
			}
			fileOut.flush();
			fileOut.close();
			
			out.writeUTF("DONE\n");
			
			System.out.println("Finished receiving file!");
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getPort() {
		return port;
	}
	
}
