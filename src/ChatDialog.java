import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

@SuppressWarnings("serial")
public class ChatDialog extends JDialog implements ActionListener, DocumentListener , DropTargetListener, FocusListener, KeyListener{
	
	private JTextPane tpMain;
	private JTextArea taMain;
	private JButton btnSend;
	private String hisName;
	private String myName;
	private StyledDocument doc;
	private SimpleAttributeSet myFormat;
	private SimpleAttributeSet hisFormat;
	private SimpleAttributeSet sysFormat;
	private CTS cts;
	private long lastMessageTime;
	private Vector<File> fileList;
	private int numUnseenMsgs = 0;
	private Runnable ding;
	private String title;
	
	public ChatDialog(String myName, String talkingTo, JFrame parent, CTS cts) {
		
		super(parent, false);
		title = "Chat with " + talkingTo;
		setTitle(title);
		addFocusListener(this);
		ding = (Runnable) Toolkit.getDefaultToolkit().getDesktopProperty("win.sound.exclamation");
		fileList = new Vector<File>();
		this.hisName = talkingTo;
		this.myName = myName;
		this.cts = cts;
		lastMessageTime = System.currentTimeMillis();
		JPanel panel = new JPanel(new BorderLayout());
		
		tpMain = new JTextPane();
		tpMain.setEditable(false);
		new DropTarget(tpMain, this);
		JScrollPane scroll = new JScrollPane(tpMain);
		panel.add(scroll, BorderLayout.CENTER);
		doc = tpMain.getStyledDocument();
		

		myFormat = new SimpleAttributeSet();
		StyleConstants.setForeground(myFormat, Color.BLUE);		

		hisFormat = new SimpleAttributeSet();
		StyleConstants.setForeground(hisFormat, new Color(39,125,44));
		

		sysFormat = new SimpleAttributeSet();
		StyleConstants.setForeground(sysFormat, Color.BLACK);

		
		taMain = new JTextArea();
		taMain.setWrapStyleWord(true);
		taMain.setLineWrap(true);
		taMain.getDocument().addDocumentListener(this);
		taMain.addKeyListener(this);
		
		scroll = new JScrollPane(taMain);
		scroll.setPreferredSize(new Dimension(getWidth(),50));
		
		btnSend = new JButton("Send");
		btnSend.setActionCommand("SEND");
		btnSend.addActionListener(this);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(scroll, BorderLayout.CENTER);
		bottomPanel.add(btnSend, BorderLayout.EAST);
		panel.add(bottomPanel, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

		getContentPane().add(panel);
		setSize(400,400);
		setLocationRelativeTo(parent);
		getRootPane().setDefaultButton(btnSend);
		setVisible(true);
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();
		if(cmd.equals("SEND")) {
		    try {
				long curTime = System.currentTimeMillis();
				if(curTime - lastMessageTime > 60000) {
					String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date(lastMessageTime));
					String time = new SimpleDateFormat("hh:mm:ss a").format(new Date(lastMessageTime));
					doc.insertString(doc.getLength(), "System: Last message: " + date + ", " + time + "\n", sysFormat);					
				}
				doc.insertString(doc.getLength(), myName + ": " + taMain.getText().trim() + "\n", myFormat);
				lastMessageTime = curTime;
				cts.send("CHAT_MESSAGE " + hisName + " " + System.currentTimeMillis() + " " + taMain.getText().trim());
				taMain.setText("");
			} catch (BadLocationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	@Override
	public void insertUpdate(DocumentEvent arg0) {
		btnSend.setEnabled(taMain.getText().trim().length() > 0);		
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		insertUpdate(arg0);
		
	}
	
	@Override
	public void changedUpdate(DocumentEvent arg0) {}

	public void newChatMessage(String timestamp, String message) {
		try {
			if(!isVisible()) {
				setVisible(true);
			}
			if(!hasFocus()) {
				numUnseenMsgs++;
				if(ding != null) {
					ding.run();
				}
				setTitle(title + "(" + numUnseenMsgs + ")");
				setVisible(true);
			}
			long curTime = System.currentTimeMillis();
			if(curTime - lastMessageTime > 60000) {
				String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date(lastMessageTime));
				String time = new SimpleDateFormat("hh:mm:ss a").format(new Date(lastMessageTime));
				doc.insertString(doc.getLength(), "System: Last message: " + date + ", " + time + "\n", sysFormat);
			}
			lastMessageTime = curTime;
			doc.insertString(doc.getLength(), hisName + ": " + message + "\n", hisFormat);
			
		} catch (BadLocationException e) {
			e.printStackTrace();
		}	
	}

	@Override
	public void dragEnter(DropTargetDragEvent arg0) {
		dragOver(arg0);
	}

	@Override
	public void dragExit(DropTargetEvent arg0) {
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		
		try {
			
			Transferable tr = dtde.getTransferable();
			DataFlavor[] flavors = tr.getTransferDataFlavors();
			
			for(int i = 0; i < flavors.length; i++) {
				  
				if (flavors[i].isFlavorJavaFileListType()) {
				    	  
					dtde.acceptDrag(DnDConstants.ACTION_COPY);
										
					@SuppressWarnings("unchecked")
					java.util.List<File> list = (java.util.List<File>) tr.getTransferData(flavors[i]);
					if(list.size() > 1) {
						dtde.rejectDrag();
					}
					File file = (File)list.get(0);
					if(file.isDirectory()) {
						dtde.rejectDrag();
					}
					      	
				}
			    
			}
			      	      
		} catch (Exception e) {
			
			e.printStackTrace();
			dtde.rejectDrag();
		  
		}
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {

		try {
			
			Transferable tr = dtde.getTransferable();
			DataFlavor[] flavors = tr.getTransferDataFlavors();
			
			for(int i = 0; i < flavors.length; i++) {
				  
				if (flavors[i].isFlavorJavaFileListType()) {
				    	  
					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					
					@SuppressWarnings("rawtypes")
					java.util.List list = (java.util.List) tr.getTransferData(flavors[i]);
					if(list.size() > 1) {
						dtde.rejectDrop();
					}
					      	
					File file = (File)list.get(0);
					if(file.isDirectory()) {
						dtde.rejectDrop();
					}
					
					if(JOptionPane.showConfirmDialog(this, "Are you sure you want to send the file \"" + file.getName() + " (" + file.length() + " bytes)\" to " + hisName + "?" , "Send File", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
						fileList.add(file);
						cts.sendFile(file, hisName, fileList.indexOf(file));
					}
					      
					dtde.dropComplete(true);
				}
			    
			}
			      	      
		} catch (Exception e) {
			
			e.printStackTrace();
			dtde.rejectDrop();
		  
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent arg0) {}

	public int newFileSendRequest(String fileName, String size) {

		if(!isVisible()) {
			setVisible(true);
		}
		
		return JOptionPane.showConfirmDialog(this, hisName + " would like to send you the file \"" + fileName + " (" + size + " bytes)\" Would you like to accept it?" , "Send File", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			
	}

	public void newFileSendAccepted(String uname, int id, String ip, int port) {
		try {
			if(!isVisible()) {
				setVisible(true);
			}
			File f = fileList.get(id);
			new FileSender(f, f.length(), ip, port);
		} catch(ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}

	public void buddyQuit(String hisName) {
		try {
			doc.insertString(doc.getLength(),"System: " + hisName + " just went offline.\n",  sysFormat);
			btnSend.setEnabled(false);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	public void buddyReturned(String hisName) {
		try {
			doc.insertString(doc.getLength(),"System: " + hisName + " has come back online.\n",  sysFormat);
			btnSend.setEnabled(true);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
		System.out.println("focusGained");
		numUnseenMsgs = 0;
		setTitle(title);
	}

	@Override
	public void focusLost(FocusEvent e) {
		System.out.println("focusLost");
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
			arg0.consume();
			if(arg0.isControlDown()) {
				taMain.setText(taMain.getText() + "\n");
			} else {
				btnSend.doClick();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}
}
