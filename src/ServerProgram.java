import java.io.*;
import java.security.*;

import javax.net.ssl.*;

public class ServerProgram {
	
	public static final int MIN_USERNAME_LENGTH = 6;
	public static final int MIN_PASSWORD_LENGTH = 8;

	
	private UserMap userMap;
	private SSLContext sslContext;
	private KeyManagerFactory keyManagerFactory;
	private KeyStore keyStore;
	private char[] keyStorePassphrase;
	private SSLServerSocketFactory sslServerSocketFactory;
	private SSLServerSocket sslServerSocket;
	private SSLSocket sslNormalSocket;
	
	
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
			
		
		try {			
			
			sslContext = SSLContext.getInstance("SSL");		
			keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyStore = KeyStore.getInstance("JKS");
			
			keyStorePassphrase = "passphrase".toCharArray();
			keyStore.load(new FileInputStream("testkeys"), keyStorePassphrase);
			keyManagerFactory.init(keyStore, keyStorePassphrase);
			sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
			sslServerSocketFactory = sslContext.getServerSocketFactory();
			sslServerSocket = (SSLServerSocket)sslServerSocketFactory.createServerSocket(12345);
			System.out.println("Server Started!\nWaiting for clients...");
			while(true) {
				sslNormalSocket = (SSLSocket) sslServerSocket.accept();
				
				new CTC(sslNormalSocket, userMap, "Temp");
				System.out.println("Client joined the server!");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
}
