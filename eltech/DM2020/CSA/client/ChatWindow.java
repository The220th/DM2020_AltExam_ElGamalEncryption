package eltech.DM2020.CSA.client;

import java.math.BigInteger;

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
import eltech.DM2020.ElGamalCipher;
import eltech.DM2020.AES256;
import eltech.DM2020.PrimeNum;

public class ChatWindow 
{

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

	private ElGamalCipher elCipher;
	private AES256 aesCipher;

	private static final int NoCryptMode = 0;
	private static final int ElMode = 1;
	private static final int aesMode = 2;
	private int currentMode;

	private byte[] ElKey;
	private byte[] aesKey;
	
	public ChatWindow(final String recipient, JFrame viewFrame)
	{
		currentMode = 0;
		elCipher = null;
		aesCipher = null;
		ElKey = null;
		aesKey = null;

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
				byte[] msg = MsgLogicSend(tempMsg);
				msgWindow.setText("");
				sendMessage(recipient, tempMsg, msg);
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

	public void sendMessage(String recipient, String tempMsg, byte[] message)
	{
		Date time = new Date();
		if(!recipient.equals("All") && tempMsg.length() > 0 && !tempMsg.substring(0, 1).equals("!"))
			chatWindow.append(ChatClient.getUsername() + " [" + sdf.format(time) 
				+ "]: " + tempMsg + "\n");
		ChatClient.sendMessage(recipient, message, Message.MESSAGE);
	}
	
	public void receiveMessage(String sender, String message)
	{
		Date time = new Date();
		chatWindow.append(sender + " [" + sdf.format(time) + "]: " 
				+ message + "\n");
	}

	public void receiveMessage(String sender, byte[] message)
	{
		Date time = new Date();
		message = MsgLogicReceive(message);
		chatWindow.append(sender + " [" + sdf.format(time) + "]: " 
				+ new String(message) + "\n");
	}


	private byte[] MsgLogicSend(String tempMsg)
	{
		/*
			Команды начинаются с "!":
			help - вывести подсказки
			roll [число] - сгенерировать число от 0 до [число]

		*/
		if(tempMsg.length() > 0)
		{
			if(!tempMsg.substring(0, 1).equals("!"))
				return tempMsg.getBytes();
		}
		else
			return tempMsg.getBytes();

		
		byte[] res; /*именно res отправится, а если команда, то ничего не выведется*/

		tempMsg = tempMsg.replaceAll("\n", "");
		String[] cm = tempMsg.split(" ");
		chatWindow.append("\n\tОтвет запроса: \n");
		switch(cm[0])
		{
			case "!roll":
				BigInteger a = PrimeNum.rndBigInteger(BigInteger.ZERO, new BigInteger(  cm[1].replaceAll("[^0-9]", "")  ));
				res = ("Пользователь " + ChatClient.getUsername() + " зароллил " + a.toString()).getBytes();
				chatWindow.append("Вы заролили: " + a.toString() + "\n");
				break;
			default:
				chatWindow.append("Syntaxis error\n");
				res = null;
				break;
		}
		return res;
	}

	private byte[] MsgLogicReceive(byte[] tempMsg)
	{
		byte[] msg;
		switch(currentMode)
		{
			case 0:
				msg = tempMsg;
				break;
			case 1:
				msg = "Coming soon".getBytes();
				break;
			case 2:
				msg = "Coming soon".getBytes();
				break;
			default:
				msg = "Установлен неправильный mode".getBytes();
				break;
		}
		return msg;
	}
}
