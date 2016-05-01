import java.io.*;
import java.net.*;

public class CTC implements Runnable {
	
	private Talker talker;
	private UserMap userMap;
	private User parent;
	
	public CTC(Socket socket, UserMap userMap, String username) {
		try {
			talker = new Talker(socket, "Central Server", username);
			this.userMap = userMap;
			new Thread(this).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			handleLoginOrRegister();
			
			while(true) {
				String cmd = talker.recieve();
				if(cmd.startsWith("BUDDY_REQ")) {
					String reqUsername = cmd.split("\\s+")[1];
					User user = userMap.get(reqUsername);
					if(user == null) {
						talker.send("USER_NOT_FOUND " + reqUsername);
					} else {
						if(user.isOnline()) {
							if(reqUsername.equals(parent.getUsername())) {
								user.send("BUT_THAT_IS_YOU " + parent.getUsername());
							} else {
								user.send("BUDDY_REQ " + parent.getUsername());
							}
						} else {
							talker.send("USER_OFFLINE " + reqUsername);
						}
					}
				} else if(cmd.startsWith("BUDDY_DENIED")) {
					String uname = cmd.split("\\s+")[1];
					User user = userMap.get(uname);
					if(user != null) {
						if(user.isOnline()) {
							user.send("BUDDY_DENIED " + parent.getUsername());
						}
					}
				} else if(cmd.startsWith("BUDDY_ACCEPTED")) {
					String uname = cmd.split("\\s+")[1];
					User user = userMap.get(uname);
					if(user != null) {
						if(user.isOnline()) {
							user.send("BUDDY_ACCEPTED " + parent.getUsername());				
						}
						user.getBuddyList().add(parent.getUsername());
						userMap.get(parent.getUsername()).getBuddyList().add(user.getUsername());
						userMap.write();	
						
					}					
				} else if(cmd.startsWith("REMOVE_BUDDY")) {
					String uname = cmd.split("\\s+")[1];
					User user = userMap.get(uname);
					if(user != null) {
						if(user.isOnline()) {
							user.send("REMOVE_BUDDY " + parent.getUsername());				
						}
						user.getBuddyList().remove(parent.getUsername());
						userMap.get(parent.getUsername()).getBuddyList().remove(user.getUsername());
						userMap.write();			
					}					
				} else if(cmd.startsWith("CHAT_MESSAGE")) {
					String uname = cmd.split("\\s+")[1];
					String timestamp = cmd.split("\\s+")[2];
					String message = cmd.substring("CHAT_MESSAGE ".length() + uname.length()+1 + timestamp.length()+1);
					User user = userMap.get(uname);
					if(user != null) {
						if(user.isOnline()) {
							user.send("CHAT_MESSAGE " + parent.getUsername() + " " + timestamp + " " + message);
						}
					}					
				} else if(cmd.startsWith("FILE_SEND_REQUEST")) {
					String uname = cmd.split("\\s+")[1];
					String fileName = cmd.split("\\s+")[2];
					String size = cmd.split("\\s+")[3];
					String id = cmd.split("\\s+")[4];
					User user = userMap.get(uname);
					if(user != null) {
						if(user.isOnline()) {
							user.send("FILE_SEND_REQUEST " + parent.getUsername() + " " + fileName + " " + size + " " + id);
						}
					}					
				} else if(cmd.startsWith("FILE_SEND_ACCEPTED")) {
					String uname = cmd.split("\\s+")[1];
					String id = cmd.split("\\s+")[2];
					String port = cmd.split("\\s+")[3];
					User user = userMap.get(uname);
					if(user != null) {
						if(user.isOnline()) {
							user.send("FILE_SEND_ACCEPTED " + parent.getUsername() + " " + id + " " + talker.getIP() + " " + port);
						}
					}					
				}
			}
		} catch (IOException e) {
			if(parent != null) {
				if(!parent.getUsername().equals("Temp")) {
					parent.setCTC(null);
					User user = userMap.get(parent.getUsername());
					for(String buddyName : user.getBuddyList()) {
						if(userMap.get(buddyName) != null) {
							if(userMap.get(buddyName).isOnline()) {
								try {
									userMap.get(buddyName).send("STATUS_CHANGE " + parent.getUsername() + " " + BuddyInfo.OFFLINE);
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
		
		
		
	}

	private void handleLoginOrRegister() throws IOException {

		String command, user, pass;
		boolean userBad = false;
		boolean passBad = false;
		command = talker.recieve();
		user = talker.recieve().substring(5);
		pass = talker.recieve().substring(5);
		if(command.startsWith("REGISTER")) {
			System.out.println("Successful Attempting to register user with Username: " + user + ", Password: " + pass);
			String badCredentialsReport = "";
			if(user.split("\\s+").length > 1) {
				userBad = true;
				badCredentialsReport = badCredentialsReport.concat("INTERNAL_SPACES\n");
			} else {
				badCredentialsReport = badCredentialsReport.concat("+OK1\n");
			}
			if(user.length() < ServerProgram.MIN_USERNAME_LENGTH) {
				userBad = true;
				badCredentialsReport = badCredentialsReport.concat("SHORT_USER " + ServerProgram.MIN_USERNAME_LENGTH + "\n");
			} else {
				badCredentialsReport = badCredentialsReport.concat("+OK2\n");
			}
			if(pass.length() < ServerProgram.MIN_PASSWORD_LENGTH) {
				passBad = true;
				badCredentialsReport = badCredentialsReport.concat("SHORT_PASS " + ServerProgram.MIN_PASSWORD_LENGTH + "\n");
			} else {
				badCredentialsReport = badCredentialsReport.concat("+OK3\n");
			}
			if(userBad || passBad) {
				talker.send("+ERR MALFORMED_CREDENTIALS");
				talker.send(Boolean.toString(userBad));
				talker.send(Boolean.toString(passBad));
				talker.send(badCredentialsReport);
				throw new IOException("Bad credentials. Username Bad: " + userBad + ", Password Bad: " + passBad);
			}
			User maybeOldUser = userMap.get(user);
			if(maybeOldUser != null) {
				talker.send("+ERR USER_EXISTS");
				throw new IOException("User already exists.");
			} else {
				talker.setName(user);
				System.out.println("Successful registration! User created with Username: " + user + ", Password: " + pass);
				parent = new User(user, pass, userMap, this);
				userMap.put(user, parent);
				userMap.write();
				talker.send("+OK");
			}
		} else if(command.startsWith("LOGIN")) {
			System.out.println("Attempting to login user with Username: " + user + ", Password: " + pass);
			User maybeOldUser = userMap.get(user);
			if(maybeOldUser == null) {
				talker.send("+ERR VALIDATION_FAILED");
				throw new IOException("User does not exist.");
			} else {
				if(maybeOldUser.validatePassword(pass)) {
					if(maybeOldUser.isOnline()) {
						talker.send("+ERR USER_ONLINE");						
					} else {
						talker.setName(user);
						talker.send("+OK Login successful!");
						maybeOldUser.setCTC(this);	
						parent = maybeOldUser;
						talker.send("BUDDY_LIST");
						talker.send(Integer.toString(maybeOldUser.getBuddyList().size()));
						for(String buddyName : maybeOldUser.getBuddyList()) {
							talker.send(buddyName);
							if(userMap.get(buddyName) != null) {
								talker.send(Integer.toString(userMap.get(buddyName).getStatus()));
							}
						}
						for(String buddyName : maybeOldUser.getBuddyList()) {
							if(userMap.get(buddyName) != null) {
								try {
									if(userMap.get(buddyName).isOnline()) {
										userMap.get(buddyName).send("STATUS_CHANGE " + maybeOldUser.getUsername() + " " + BuddyInfo.ONLINE);
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				} else {
					talker.send("+ERR VALIDATION_FAILED");
					throw new IOException("Password is incorrect.");							
				}
			}
					
		}
		
	}

	public void send(String string) throws IOException {
		talker.send(string);
	}

}
