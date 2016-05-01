import java.io.*;
import java.util.*;

public class User {
	
	static int userCount = 0;
	
	private String username;
	private String password;

	private CTC ctc;

	private Vector<String> buddyList;
	private Vector<String> pendingBuddyRequests;
	
	public User(String username, String password, Map<String, User> userMap, CTC ctc) {
		this(username, password, userMap);
		this.ctc = ctc;
	}
	
	public User(String username, String password, Map<String, User> userMap) {
		this.username = username;
		this.password = password;
		buddyList = new Vector<String>();
		pendingBuddyRequests = new Vector<String>();
	}
	
	public User(DataInputStream dis, Map<String, User> userMap) throws IOException {
		username = dis.readUTF();
		password = dis.readUTF();
		buddyList = new Vector<String>();
		pendingBuddyRequests = new Vector<String>();
		int numBuddies = dis.readInt();
		for(int i = 0; i < numBuddies; i++) {
			buddyList.add(dis.readUTF());
		}
		int numRequests = dis.readInt();
		for(int i = 0; i < numRequests; i++) {
			pendingBuddyRequests.add(
					dis.readUTF());
		}
		userMap.put(username, this);
	}
	
	public void setCTC(CTC ctc) {
		this.ctc = ctc;		
	}
	
	public void write(DataOutputStream dos) {
		try {
			dos.writeUTF(username);
			dos.writeUTF(password);
			dos.writeInt(buddyList.size());
			for(String bud : buddyList) {
				dos.writeUTF(bud);
			}
			dos.writeInt(pendingBuddyRequests.size());
			for(String bud : pendingBuddyRequests) {
				dos.writeUTF(bud);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean validatePassword(String password) {
		return this.password.equals(password);
	}

	public Vector<String> getBuddyList() {
		return buddyList;
	}
	
	public Vector<String> getPendingRequests() {
		return pendingBuddyRequests;
	}

	public boolean isOnline() {
		return ctc != null;
	}

	public void send(String string) throws IOException {
		ctc.send(string);		
	}

	public String getUsername() {
		return username;
	}

	public int getStatus() {
		return ctc == null ? BuddyInfo.OFFLINE : BuddyInfo.ONLINE;
	}

	public void addPendingBuddy(String bi) {
		boolean bad = false;
		for(int i = 0; i < pendingBuddyRequests.size(); i++) {
			if(pendingBuddyRequests.get(i).equalsIgnoreCase(bi)) {
				bad = true;
			}
		}
		if(!bad) {
			pendingBuddyRequests.add(bi);
		}				
	}
	
	public void removePendingBuddy(String bi) {
		for(int i = 0; i < pendingBuddyRequests.size(); i++) {
			if(pendingBuddyRequests.get(i).equalsIgnoreCase(bi)) {
				pendingBuddyRequests.remove(i);
			}
		}
	}

	
}
