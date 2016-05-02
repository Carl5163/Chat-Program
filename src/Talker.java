import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.*;

public class Talker {
	
	private BufferedReader in;
	private DataOutputStream out;
	private String talkingTo;
	private String talking;
	
	private SSLSocketFactory sslSocketFactory;
	private SSLContext sslContext;
	private KeyManagerFactory keyManagerFactory;
	private KeyStore keyStore;
	private char[] keyStorePassphrase;
	
	private SSLSocket sslNormalSocket;
	
	public Talker(String ip, int port, String talking, String talkingTo) throws UnknownHostException, IOException, SSLException{
		
		
		try {
			System.setProperty("javax.net.ssl.trustStore", "samplecacerts");
			System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
			
			sslContext = SSLContext.getInstance("SSL");
			keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyStore = KeyStore.getInstance("JKS");
			
			keyStorePassphrase = "passphrase".toCharArray();
			keyStore.load(new FileInputStream("testkeys"), keyStorePassphrase);
			
			keyManagerFactory.init(keyStore, keyStorePassphrase);
			sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
			
			sslSocketFactory = sslContext.getSocketFactory();
			sslNormalSocket = (SSLSocket)sslSocketFactory.createSocket(ip, port);
			sslNormalSocket.startHandshake();
			
			System.out.println("Connected!");
			in = new BufferedReader(new InputStreamReader(sslNormalSocket.getInputStream()));
			out = new DataOutputStream(sslNormalSocket.getOutputStream());
			System.out.println("Got Streams!");
			this.talkingTo = talkingTo;
			this.talking = talking;
		} catch(NoSuchAlgorithmException | CertificateException | KeyStoreException | UnrecoverableKeyException | KeyManagementException e){
			e.printStackTrace();
			throw new SSLException("Could not connect to server: Could not create a secure connection.");
		}
	}
	
	public Talker(SSLSocket socket, String talking, String talkingTo) throws IOException {

		System.out.println("Connected!");
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new DataOutputStream(socket.getOutputStream());
		System.out.println("Got Streams!");
		this.talkingTo = talkingTo;
		this.talking = talking;
		this.sslNormalSocket = socket;
		
	}
	
	public void send(String msg) throws IOException {
		
		if(!msg.endsWith("\n")) {
			msg = msg + "\n";
		}
		
		out.writeBytes(msg);
		String msgToPrint = msg.endsWith("\n") ? msg.substring(0, msg.length()-1) : msg;
		System.out.println(talking + "    >>>>    " + msgToPrint + "    >>>>    " + talkingTo);		
	}
	
	public String recieve() throws IOException {
		String msg = in.readLine();
		String msgToPrint = msg.endsWith("\n") ? msg.substring(0, msg.length()-1) : msg;
		System.out.println(talking + "    <<<<    " + msgToPrint + "    <<<<    " + talkingTo);
		return msg;
	}

	public void setName(String user) {
		talkingTo = user;		
	}

	public String getIP() {
		return sslNormalSocket.getInetAddress().getHostAddress();
	}
	
}
