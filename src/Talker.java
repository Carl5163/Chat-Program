import java.io.*;
import java.net.*;

public class Talker {
	
	private BufferedReader in;
	private DataOutputStream out;
	private String talkingTo;
	private String talking;
	private Socket socket;

	
	public Talker(String ip, int port, String talking, String talkingTo) throws UnknownHostException, IOException {
		this(new Socket(ip, port), talking, talkingTo);
	}
	
	public Talker(Socket socket, String talking, String talkingTo) throws IOException {

		System.out.println("Connected!");
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new DataOutputStream(socket.getOutputStream());
		System.out.println("Got Streams!");
		this.talkingTo = talkingTo;
		this.talking = talking;
		this.socket = socket;
		
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
		return socket.getInetAddress().getHostAddress();
	}
	
}
