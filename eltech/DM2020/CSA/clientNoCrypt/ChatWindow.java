package eltech.DM2020.CSA.clientNoCrypt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import eltech.DM2020.CSA.cmn.Message;

public class ChatWindow {

	private JFrame frame;
	private Container contentPane;
	private JPanel botPanel;
	private Border border;
	private JButton sendButton;
	//private JButton quitButton;
	private JScrollPane msgScroll, chatScroll;
	private JTextArea msgWindow, chatWindow;
	@SuppressWarnings("unused")
	private String recipient;
	
	private SimpleDateFormat sdf;
	
	public ChatWindow(final String recipient, JFrame viewFrame){
		System.out.println("Created new chat window with " + recipient);
		
		this.recipient = recipient;
		sdf = new SimpleDateFormat("HH:mm:ss");
		
		//Create frame, set size, center, and set exit on close
		frame = new JFrame("Chat with " + recipient);
		frame.setSize(600, 400);
		frame.setResizable(false);
		frame.setLocationRelativeTo(viewFrame);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//Create Border
		border = BorderFactory.createLineBorder(Color.gray);
		
		//Create Components
		chatWindow = new JTextArea();
		chatWindow.setEditable(false);
		chatScroll = new JScrollPane(chatWindow);
		chatScroll.setBorder(border);
		
		sendButton = new JButton("Send");
		sendButton.setPreferredSize(new Dimension(100, 100));

		//quitButton = new JButton("Quit");
		//quitButton.setPreferredSize(new Dimension(100, 50));
		
		msgWindow = new JTextArea();
		msgScroll = new JScrollPane(msgWindow);
		msgScroll.setPreferredSize(new Dimension(500, 200));
		msgScroll.setBorder(border);
		botPanel = new JPanel();
		botPanel.setLayout(new BorderLayout());
		botPanel.add(msgScroll, BorderLayout.WEST);
		botPanel.add(sendButton);
		//botPanel.add(quitButton);
		botPanel.setBorder(border);
		
		//Set up pane
		contentPane = frame.getContentPane();
		contentPane.setLayout(new GridLayout(2, 1));
		contentPane.add(chatScroll);
		contentPane.add(botPanel);
		
		frame.setContentPane(contentPane);
		frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				System.out.println("Used cross in chat");
				ChatClient.refreshMarkedList_rm(recipient);
			}
		});
		frame.setVisible(true);
		
		sendButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String tempMsg = msgWindow.getText();
				msgWindow.setText("");
				sendMessage(recipient, tempMsg);
			}});
	}
	
	public void sendMessage(String recipient, String message)
	{
		Date time = new Date();
		if(!recipient.equals("All"))
			chatWindow.append(ChatClient.getUsername() + " [" + sdf.format(time) 
				+ "]: " + message + "\n");
		ChatClient.sendMessage(recipient, message, Message.MESSAGE);
	}
	
	public void receiveMessage(String sender, String message){
		Date time = new Date();
		chatWindow.append(sender + " [" + sdf.format(time) + "]: " 
				+ message + "\n");
	}
}
