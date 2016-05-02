import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.GroupLayout.*;
import javax.swing.event.*;
import javax.swing.text.*;

@SuppressWarnings("serial")
public class ClientProgram extends JFrame implements ActionListener, DocumentListener, MouseListener {

	private JPanel centerPanel;
	private CTS cts;
	private JTextField tfUsername;
	private JTextField tfAddBuddy;
	private Document docAddBuddy;
	private JPasswordField tfPassword;
	private JLabel lblUsername;
	private JLabel lblPassword;
	private JPanel bottomPanel;
	private JPanel buddyPanel;
	private JTextArea taErrors;
	private JTable tblBuddies;
	private JButton btnAddBuddy;
	private MyTableModel tableModel;
	private Map<String, BuddyInfo> buddyList;
	private Map<String, ChatDialog> chatWindowMap;
	private String myName;

	public static void main(String[] args) {
		try {
			new ClientProgram();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public ClientProgram() throws IOException {
		layoutFrame();
		buddyList = new HashMap<String, BuddyInfo>();
		chatWindowMap = new HashMap<String, ChatDialog>();
		cts = new CTS("127.0.0.1", 12345, buddyList, tableModel, this);
		setVisible(true);
	}

	private void layoutFrame() {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 500);
		setLocationRelativeTo(null);
		centerPanel = makeLoginPanel();
		getContentPane().add(centerPanel, BorderLayout.NORTH);
		bottomPanel = new JPanel(new BorderLayout());
		taErrors = new JTextArea();
		taErrors.setForeground(Color.RED);
		taErrors.setEditable(false);
		taErrors.setBackground(this.getBackground());
		taErrors.setWrapStyleWord(true);
		taErrors.setLineWrap(true);
		bottomPanel.add(taErrors);
		buddyPanel = makeBuddyPanel();
		getContentPane().add(bottomPanel, BorderLayout.CENTER);
	}

	private JPanel makeLoginPanel() {

		JPanel panel = new JPanel(new BorderLayout());

		   GroupLayout layout = new GroupLayout(panel);
		   panel.setLayout(layout);

		   layout.setAutoCreateGaps(true);
		   layout.setAutoCreateContainerGaps(true);

		   GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();

		   lblUsername = new JLabel("Username: ");
		   lblPassword = new JLabel("Password: ");
		   tfUsername = new JTextField();
		   tfPassword = new JPasswordField();
		   JButton btnLogin = new JButton("Login");
		   btnLogin.setActionCommand("LOGIN");
		   btnLogin.addActionListener(this);
		   getRootPane().setDefaultButton(btnLogin);
		   JButton btnRegister = new JButton("Register");
		   btnRegister.setActionCommand("REGISTER");
		   btnRegister.addActionListener(this);

		   hGroup.addGroup(layout.createParallelGroup().
		            addComponent(lblUsername).addComponent(lblPassword).addComponent(btnLogin));
		   hGroup.addGroup(layout.createParallelGroup().
		            addComponent(tfUsername).addComponent(tfPassword).addComponent(btnRegister));
		   layout.setHorizontalGroup(hGroup);

		   GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

		   vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
		            addComponent(lblUsername).addComponent(tfUsername));
		   vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
		            addComponent(lblPassword).addComponent(tfPassword));
		   vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
		            addComponent(btnLogin).addComponent(btnRegister));
		   layout.setVerticalGroup(vGroup);

		return panel;
	}
	
	private JPanel makeBuddyPanel() {

		
		JPanel panel = new JPanel(new BorderLayout());

		tableModel = new MyTableModel();
		tblBuddies = new JTable(tableModel);
		tblBuddies.addMouseListener(this);
				
		JScrollPane scrollPane = new JScrollPane(tblBuddies);
		panel.add(scrollPane, BorderLayout.CENTER);
		
		JPanel bPanel = new JPanel(new BorderLayout());
		tfAddBuddy = new JTextField();
		docAddBuddy = tfAddBuddy.getDocument();
		docAddBuddy.addDocumentListener(this);
		bPanel.add(tfAddBuddy, BorderLayout.NORTH);
		btnAddBuddy = new JButton("Add Buddy");
		btnAddBuddy.setEnabled(false);
		btnAddBuddy.setActionCommand("ADD_BUDDY");
		btnAddBuddy.addActionListener(this);
		bPanel.add(btnAddBuddy, BorderLayout.SOUTH);
		panel.add(bPanel, BorderLayout.SOUTH);		
		
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		String command = ae.getActionCommand();

		try {
			if(command.equals("REGISTER")) {
				taErrors.setText("");
				connect("REGISTER");
			} else if(command.equals("LOGIN")) {
				taErrors.setText("");
				connect("LOGIN");
			} else if(command.equals("ADD_BUDDY")) {
				cts.addBuddy(tfAddBuddy.getText().trim());
				tfAddBuddy.setText("");
			} else if(command.equals("REMOVE_BUDDY")) {
				cts.removeBuddy(tfAddBuddy.getText().trim());
				tfAddBuddy.setText("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void connect(String command) throws UnknownHostException, IOException, RegisterFailedException {
		try {
			lblUsername.setForeground(Color.BLACK);
			lblPassword.setForeground(Color.BLACK);
			try {
				System.out.println("sup");
				cts.connect(command, tfUsername.getText().trim(), new String(tfPassword.getPassword()).trim());
			} catch(ConnectException | SSLException e) {
				e.printStackTrace();
				System.out.println("YO");
				taErrors.setText("Error: \n * " + e.getMessage());
				return;
			}
			myName = tfUsername.getText().trim();
			centerPanel.setVisible(false);
			bottomPanel.setVisible(false);
			getRootPane().setDefaultButton(btnAddBuddy);
			getContentPane().remove(centerPanel);
			getContentPane().remove(bottomPanel);
			getContentPane().add(buddyPanel, BorderLayout.CENTER);
			setTitle(myName);
			repaint();
		} catch(LoginFailedException e) {
			taErrors.setText(taErrors.getText() + " * " + e.getMessage());
		} catch(RegisterFailedException e) {
			taErrors.setText("Error:\n");
			if(e.isUserBad()) {
				lblUsername.setForeground(Color.RED);
				if(!e.getReportOn(RegisterFailedException.INTERNAL_SPACES_USER).startsWith("+OK")) {
					taErrors.setText(taErrors.getText() + " * Usernames cannot contain spaces.\n");
				}
				if(!e.getReportOn(RegisterFailedException.SHORT_USER).startsWith("+OK")) {
					taErrors.setText(taErrors.getText() + " * Usernames must be at least " + Integer.parseInt(e.getReportOn(RegisterFailedException.SHORT_USER).split("\\s+")[1]) + " characters long.\n");
				}
			}
			if(e.isPassBad()) {
				lblPassword.setForeground(Color.RED);
				if(!e.getReportOn(RegisterFailedException.SHORT_PASS).startsWith("+OK")) {
					taErrors.setText(taErrors.getText() + " * Passwords must be at least " + Integer.parseInt(e.getReportOn(RegisterFailedException.SHORT_PASS).split("\\s+")[1]) + " characters long.\n");
				}
			}
			
			if(e.getMessage().startsWith("USER_EXISTS")) {
				taErrors.setText("Error: \n * Unable to register. \n Reason: \n * That username is already taken.");
			}
		}

	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		if(arg0.getDocument() == docAddBuddy) {
			btnAddBuddy.setEnabled((tfAddBuddy.getText().trim().length() > 0));
			if(tfAddBuddy.getText().trim().length() > 0) {
				btnAddBuddy.setText("Add Buddy");
				btnAddBuddy.setActionCommand("ADD_BUDDY");
				for(BuddyInfo info : buddyList.values()) {
					if(tfAddBuddy.getText().trim().toUpperCase().equals(info.name.trim().toUpperCase())) {
						btnAddBuddy.setText("Remove Buddy");
						btnAddBuddy.setActionCommand("REMOVE_BUDDY");
					}
				}
			}
		}
	}
	
	@Override
	public void removeUpdate(DocumentEvent arg0) {
		insertUpdate(arg0);
	}
	
	@Override
	public void changedUpdate(DocumentEvent arg0) {}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			int[] rows = tblBuddies.getSelectedRows();
			for(int i = 0; i < rows.length; i++) {
				String hisName = (String) tableModel.getValueAt(tblBuddies.convertRowIndexToModel(rows[i]), 0);
				if(buddyList.get(hisName).status == BuddyInfo.ONLINE) {
					if(chatWindowMap.get(hisName) == null) {
						ChatDialog dialog = new ChatDialog(myName, hisName, this, cts);
						chatWindowMap.put(hisName, dialog);
					} else {
						chatWindowMap.get(hisName).requestFocus();
						chatWindowMap.get(hisName).setVisible(true);
					}
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}

	public void newChatMessage(String hisName, String timestamp, String message) {
		ChatDialog dialog = chatWindowMap.get(hisName);
		if(dialog == null) {
			dialog = new ChatDialog(myName, hisName, this, cts);
			chatWindowMap.put(hisName, dialog);
		}
		dialog.newChatMessage(timestamp, message);
	}

	public void buddyQuit(String hisName) {
		ChatDialog dialog = chatWindowMap.get(hisName);
		if(dialog != null) {
			dialog.buddyQuit(hisName);
		}
	}
	

	public void buddyReturned(String hisName) {
		ChatDialog dialog = chatWindowMap.get(hisName);
		if(dialog != null) {
			dialog.buddyReturned(hisName);
		}
	}

	public int newFileSendRequest(String uname, String fileName, String size) {

		for(BuddyInfo bud : buddyList.values()) {
			if(bud.name.equalsIgnoreCase(uname)) {
				ChatDialog dialog = chatWindowMap.get(uname);
				if(dialog == null) {
					dialog = new ChatDialog(myName, uname, this, cts);
					chatWindowMap.put(uname, dialog);
				}
				return dialog.newFileSendRequest(fileName, size);
			}
		}
		
		return JOptionPane.NO_OPTION;
		
	}

	public void newFileSendAccepted(String uname, String id, String ip, int port) {
		for(BuddyInfo bud : buddyList.values()) {
			System.out.println(bud.name);
			if(bud.name.equalsIgnoreCase(uname)) {
				ChatDialog dialog = chatWindowMap.get(uname);
				if(dialog == null) {
					dialog = new ChatDialog(myName, uname, this, cts);
					chatWindowMap.put(uname, dialog);
				}
				dialog.newFileSendAccepted(uname, Integer.parseInt(id), ip, port);
			}
		}	
	}

	public void restart() {
		dispose();
		try {
			new ClientProgram();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
