import java.io.*;
import java.net.*;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.swing.*;

public class CTS implements Runnable {
		
	private Talker talker;
	private String ip;
	private int port;
	private Map<String, BuddyInfo> buddyList;
	private MyTableModel tableModel;
	private ClientProgram parent;
	private JFileChooser fileChooser;
	
	public CTS(String ip, int port, Map<String, BuddyInfo> buddyList, MyTableModel tableModel, ClientProgram parent) throws IOException {
		this.ip = ip;
		this.port = port;
		this.buddyList = buddyList;
		this.tableModel = tableModel;
		this.parent = parent;
		fileChooser = new JFileChooser();
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				String cmd = talker.recieve();
				if(cmd.startsWith("BUDDY_LIST")) {
					int numBuddies = Integer.parseInt(talker.recieve());
					for(int i = 0; i < numBuddies; i++) {
						String name = talker.recieve();
						int status = Integer.parseInt(talker.recieve());
						buddyList.put(name, new BuddyInfo(name, status));
					}					
					updateBuddyList();
				} else if(cmd.startsWith("BUDDY_REQ")) {
					String buddyRequester = cmd.split("\\s+")[1];
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							int ret = JOptionPane.showConfirmDialog(parent, "The user \"" + buddyRequester + "\" would like to be your buddy! Do you accept?", "New Buddy Request", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							if(ret == JOptionPane.YES_OPTION) {
								try {
									talker.send("BUDDY_ACCEPTED " + buddyRequester);
									buddyList.put(buddyRequester, new BuddyInfo(buddyRequester, BuddyInfo.ONLINE));
									updateBuddyList();
								} catch (IOException e) {
									e.printStackTrace();
								}
							} else {
								try {
									talker.send("BUDDY_DENIED " + buddyRequester);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					});
					
				} else if(cmd.startsWith("USER_NOT_FOUND")) {
					String notFoundUsername = cmd.split("\\s+")[1];
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JOptionPane.showMessageDialog(parent, "The user \"" + notFoundUsername + "\" doesn't exist.", "Could not send buddy request.", JOptionPane.ERROR_MESSAGE);
						}
					});
				} else if(cmd.startsWith("USER_OFFLINE")) {
					String notFoundUsername = cmd.split("\\s+")[1];
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JOptionPane.showMessageDialog(parent, "The user \"" + notFoundUsername + "\" is offline.", "Could not contact user.", JOptionPane.ERROR_MESSAGE);
						}
					});
				} else if(cmd.startsWith("BUDDY_DENIED")) {
					String uname = cmd.split("\\s+")[1];
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JOptionPane.showMessageDialog(parent, "The user \"" + uname + "\" doesn't like you enough to be your buddy. \nSorry. :'(", "Bummer...", JOptionPane.INFORMATION_MESSAGE);
						}
					});
				} else if(cmd.startsWith("BUDDY_ACCEPTED")) {
					String uname = cmd.split("\\s+")[1];
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JOptionPane.showMessageDialog(parent, "The user \"" + uname + "\" has accepted your buddy request!", "Congratulations!", JOptionPane.INFORMATION_MESSAGE);
							buddyList.put(uname, new BuddyInfo(uname, BuddyInfo.ONLINE));
							updateBuddyList();
						}
					});
				} else if(cmd.startsWith("BUT_THAT_IS_YOU")) {
					String uname = cmd.split("\\s+")[1];
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JOptionPane.showOptionDialog(parent, "The user \"" + uname + ".....\" is you. \nWhy? Why would you do that?", "Really...?", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"I won't do that again."}, "default");
						}
					});
				} else if(cmd.startsWith("REMOVE_BUDDY")) {
					String uname = cmd.split("\\s+")[1];
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JOptionPane.showMessageDialog(parent, "The user \"" + uname + "\" no longer wishes to be your buddy.", "Congratulations!", JOptionPane.INFORMATION_MESSAGE);
							buddyList.remove(uname);
							updateBuddyList();
						}
					});
				} else if(cmd.startsWith("STATUS_CHANGE")) {
					String uname = cmd.split("\\s+")[1];
					buddyList.get(uname).status = Integer.parseInt(cmd.split("\\s+")[2]);
					if(buddyList.get(uname).status == BuddyInfo.OFFLINE) {
						parent.buddyQuit(uname);
					} else {
						parent.buddyReturned(uname);						
					}
					updateBuddyList();
				} else if(cmd.startsWith("CHAT_MESSAGE")) {
					String uname = cmd.split("\\s+")[1];
					String timestamp = cmd.split("\\s+")[2];
					String message = cmd.substring("CHAT_MESSAGE ".length() + uname.length()+1 + timestamp.length()+1);
					//BuddyInfo messageTo = buddyList.get(uname);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							parent.newChatMessage(uname, timestamp, message);
						}						
					});
				} else if(cmd.startsWith("FILE_SEND_REQUEST")) {
					String uname = cmd.split("\\s+")[1];
					String fileName = cmd.split("\\s+")[2];
					String size = cmd.split("\\s+")[3];
					String id = cmd.split("\\s+")[4];
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							if(parent.newFileSendRequest(uname, fileName, size) == JOptionPane.YES_OPTION) {
								try {
									String suggestedName = fileName.substring(0, fileName.lastIndexOf('.'));
									String extension = fileName.substring(fileName.lastIndexOf('.'));
									int index = 1;
									if(new File(fileChooser.getCurrentDirectory() + "\\" + suggestedName + extension).exists()) {
										suggestedName = suggestedName + "(" + index + ")";
										while(new File(fileChooser.getCurrentDirectory() + "\\" + suggestedName + extension).exists()) {
											suggestedName = suggestedName.substring(0, suggestedName.length()-3);
										}
									}
									
									fileChooser.setSelectedFile(new File(suggestedName + extension));
									if(fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
										FileReceiver fr = new FileReceiver(fileChooser.getSelectedFile(), Long.parseLong(size));
										talker.send("FILE_SEND_ACCEPTED " + uname + " " + id + " " + fr.getPort());
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					});
				} else if(cmd.startsWith("FILE_SEND_ACCEPTED")) {
					System.out.println("BEFORE SPIT");
					String uname = cmd.split("\\s+")[1];
					String id = cmd.split("\\s+")[2];
					String ip = cmd.split("\\s+")[3];
					System.out.println("Almost after split");
					String port = cmd.split("\\s+")[4];
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							parent.newFileSendAccepted(uname, id, ip, Integer.parseInt(port));
						}
					});
				}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(parent, "You have lost connection or the server has shut down.", "Connection lost.", JOptionPane.ERROR_MESSAGE);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					parent.restart();
				}				
			});
		}
	}
	
	private void updateBuddyList() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				tableModel.clear();
				for(BuddyInfo bi : buddyList.values()) {
					tableModel.add(bi);
				}
			}
		});
	}

	public void connect(String command, String username, String password) throws UnknownHostException, 
																				 IOException, 
																				 LoginFailedException,
																				 RegisterFailedException, SSLException {

		try {
			talker = new Talker(ip, port, username, "Central Server");
			talker.send(command);
			talker.send("USER " + username);
			talker.send("PASS " + password);
		} catch(ConnectException e) {
			String msg = "Could not connect to server: Server did not respond.";
			throw new ConnectException(msg);
		}
		String msg = talker.recieve();
		if(msg.startsWith("+OK")) {
			new Thread(this).start();
		} else {
			if(command.equals("LOGIN")) {
				msg = msg.substring(5);
				if(msg.startsWith("USER_ONLINE")) {
					msg = "Login Failed: You are already logged in elsewhere.";
				} else {
					msg = "Login Failed: Username or password is incorrect.";
				}
				throw new LoginFailedException(msg);
			} else if(command.equals("REGISTER")) {
				msg = msg.substring(5);
				boolean userBad = false, passBad = false;
				String[] report = new String[3];
				if(!msg.startsWith("USER_EXISTS")) {
					userBad = Boolean.parseBoolean(talker.recieve());
					passBad = Boolean.parseBoolean(talker.recieve());
					for(int i = 0; i < 3; i++) {
						report[i] = talker.recieve();
					}
				}
				throw new RegisterFailedException(msg, userBad, passBad, report);
			}
		}
	}
	
	public void send(String message) throws IOException {
		talker.send(message);
	}

	public void addBuddy(String name) throws IOException {
		talker.send("BUDDY_REQ " + name);
	}

	public void removeBuddy(String name) throws IOException {
		buddyList.remove(name);
		talker.send("REMOVE_BUDDY " + name);	
		updateBuddyList();
	}

	public void sendFile(File file, String hisName, int index) throws IOException {
		talker.send("FILE_SEND_REQUEST " + hisName + " " + file.getName() + " " + file.length() + " " + index);		
	}

	public void buddyAccepted(String buddyRequester) {
		try {
			talker.send("BUDDY_ACCEPTED " + buddyRequester);
			buddyList.put(buddyRequester, new BuddyInfo(buddyRequester, BuddyInfo.ONLINE));
			updateBuddyList();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void buddyDeleted(String buddyRequester) {
		try {
			talker.send("BUDDY_DENIED " + buddyRequester);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
