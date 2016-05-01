import java.io.*;
import java.net.*;

public class ServerProgram {
	
	private UserMap userMap;
	public static final int MIN_USERNAME_LENGTH = 6;
	public static final int MIN_PASSWORD_LENGTH = 8;
	
	public static void main(String[] args) {
		
		new ServerProgram();
		
	}
	
	public ServerProgram() {
		
		userMap = new UserMap("UserList.dat");	
		
		if(new File("UserList.dat").exists()) {
			try {
				DataInputStream dis = new DataInputStream(new FileInputStream("UserList.dat"));
				int numUsers = dis.readInt();
				for(int i = 0; i < numUsers; i++) {
					new User(dis, userMap);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
		System.out.println("Server Started!\nWaiting for clients...");
		
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(12354);
			while(true) {
				Socket regSocket;
				regSocket = serverSocket.accept();
				
				new CTC(regSocket, userMap, "Temp");
				System.out.println("Client joined the server!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
