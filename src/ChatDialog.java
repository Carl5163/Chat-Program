import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


@SuppressWarnings("serial")
public class ChatDialog extends JDialog implements ActionListener, DocumentListener , DropTargetListener{
	
	private JTextPane tpMain;
	private JTextField tfMain;
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
	
	public ChatDialog(String myName, String talkingTo, JFrame parent, CTS cts) {
		
		super(parent, "Chat with " + talkingTo, false);
		
		fileList = new Vector<File>();
		this.hisName = talkingTo;
		this.myName = myName;
		this.cts = cts;
		lastMessageTime = System.currentTimeMillis();
		JPanel panel = new JPanel(new BorderLayout());
		
		tpMain = new JTextPane();
		tpMain.setEditable(false);
		DropTarget target = new DropTarget(tpMain, this);
		JScrollPane scroll = new JScrollPane(tpMain);
		panel.add(scroll, BorderLayout.CENTER);
		doc = tpMain.getStyledDocument();
		

		myFormat = new SimpleAttributeSet();
		StyleConstants.setForeground(myFormat, Color.GRAY);		

		hisFormat = new SimpleAttributeSet();
		StyleConstants.setForeground(hisFormat, Color.LIGHT_GRAY);
		

		sysFormat = new SimpleAttributeSet();
		StyleConstants.setForeground(hisFormat, Color.BLACK);
		
		tfMain = new JTextField();
		tfMain.getDocument().addDocumentListener(this);
		
		btnSend = new JButton("Send");
		btnSend.setActionCommand("SEND");
		btnSend.addActionListener(this);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(tfMain, BorderLayout.CENTER);
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
					String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(lastMessageTime));
					String time = new SimpleDateFormat("hh:mm:ss a").format(new Date(lastMessageTime));
					doc.insertString(doc.getLength(), "Last message: " + date + ", " + time + "\n", sysFormat);					
				}
				doc.insertString(doc.getLength(), myName + ": " + tfMain.getText().trim() + "\n", myFormat);
				lastMessageTime = curTime;
				cts.send("CHAT_MESSAGE " + hisName + " " + System.currentTimeMillis() + " " + tfMain.getText().trim());
				tfMain.setText("");
			} catch (BadLocationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	@Override
	public void insertUpdate(DocumentEvent arg0) {
		btnSend.setEnabled(tfMain.getText().trim().length() > 0);		
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
			long curTime = System.currentTimeMillis();
			if(curTime - lastMessageTime > 60000) {
				String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(lastMessageTime));
				String time = new SimpleDateFormat("hh:mm:ss a").format(new Date(lastMessageTime));
				doc.insertString(doc.getLength(), "Last message: " + date + ", " + time + "\n", sysFormat);
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
					
					java.util.List list = (java.util.List) tr.getTransferData(flavors[i]);
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
}
