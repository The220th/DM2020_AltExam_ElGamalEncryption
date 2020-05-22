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
				if(msg != null)
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
		ElGamalCipher buffEl;
		boolean SyntaxisProblem = false;
		byte[] buff;
		/*
			Команды начинаются с "!":
			help - вывести подсказки
			roll [число] - сгенерировать число от 0 до [число]

			ElEn [P] [A] [other Y] [msg](строка) - зашифровать сообщение с помощью алгоритма Эль-Гамаля
			ElDe [P] [A] [X] [msg](число) - расшифровать сообщение с помощью алгоритма Эль-Гамаля
			ElSign [P] [A] [X] [msg] - подписать сообщение с помощью алгоритма Эль-Гамаля
			ElVerify [P] [A] [other Y] [msg] - проверить подпись сообщения от пользователя, у которого публичный ключ [other Y], поле вычитов [P] и генератор [A]
			ElSignKey [P] [A] [X] [key] [msg] - подписать ключ
			ElVerifyKey [P] [A] [other Y] [key] - проверить подпись ключа. [other Y] - это публичный ключ того, кто подписывал

			initEl [P] [A] [X] [Y] - инициализировать шифровальщик, использующий алгоритм Эль Гамаля
			initEl [P] [A] - инициализировать шифровальщик, использующий алгоритм Эль Гамаля. При этом X и Y сгенерируются сами
			initEldefault - инициализировать шифровальщик, использующий алгоритм Эль Гамаля. При этом в качестве P и A будут взяты стандартные значения, X и Y сгенерируются сами
			initEldefault [X] [Y] - инициализировать шифровальщик, использующий алгоритм Эль Гамаля. При этом в качестве P и A будут взяты стандартные значения
			initEl - инициализировать шифровальщик, использующий алгоритм Эль Гамаля. При этом P, A, X, Y сгенерируются сами. На это уйдёт какое-то время. Возможно даже несколько минут. Программа не будет отвечать во время генерации
			initContact [other Y] - инициализировать публичный ключ того, кому вы собираетесь отправлять сообщения
			
			ElGetY - получить ваш публичный ключ 
			ElGetX - получить ваш секретный ключ 
			ElGetP - получить ваше поле вычитов P
			ElGetA - получить ваш генератор поля вычитов A

			Sign [msg] - подписать сообщение
			Verify [msg] - проверить подпись сообщения
			Verify [msg] [other Y] - проверить подпись сообщения от пользователя, чей публичный ключ [other Y]
			SignKey [key] [msg] - подписать ключ
			VerifyKey [key] [other Y] - проверить подпись ключа. [other Y] - это публичный ключ того, кто подписывал

			initAES [key] - инициализировать шифровальщик, использующий алгоритм AES-256
			initAES - инициализировать шифровальщик, использующий алгоритм AES-256. Ключ сгенерируется сам
			AESgetKey - получает ключ, с помощью которого шифруются сообщения

			CryptoSwitch [число] (или [строка]). [число] такое (или [строка] такая), что:
			0 (off или none) - все входящие и исходящие сообщения передаются без шифрования.
			1 (ElGamal) - все входящие и исходящие сообщения шифруются с помощью алгоритма Ель-Гамаля
			2 (*AES*) - все входящие и исходящие сообщения шифруются с помощью алгоритма AES-256
		*/
		if(tempMsg.length() > 0)
		{
			if(!tempMsg.substring(0, 1).equals("!"))
				return tempMsg.getBytes();
		}
		else
			return tempMsg.getBytes();

		
		byte[] res = null; /*именно res отправится и, если команда, то ничего не выведется*/

		tempMsg = tempMsg.replaceAll("\n", " ");
		String[] cm = tempMsg.split(" ");
		chatWindow.append("\n\tОтвет запроса: \n");
		try
		{
			switch(cm[0].toLowerCase())
			{
				case "!help":
					res = null;
					chatWindow.append("Тут будет помощь. Ага\n");
					break;
				case "!roll":
					BigInteger a = PrimeNum.rndBigInteger(BigInteger.ZERO, new BigInteger(  cm[1].replaceAll("[^0-9]", "")  ));
					res = ("Пользователь " + ChatClient.getUsername() + " зароллил " + a.toString()).getBytes();
					chatWindow.append("Вы заролили: " + a.toString() + "\n");
					break;
				case "!elen": // ElEn [P] [A] [other Y] [msg](строка)
					if(cm.length != 5){ SyntaxisProblem = true; break; }
					buffEl = new ElGamalCipher((new BigInteger(cm[1])).toByteArray(), (new BigInteger(cm[2])).toByteArray(), (new BigInteger(cm[3])).toByteArray(), (new BigInteger(cm[3])).toByteArray());
					chatWindow.append( PrimeNum.BytesToNum( buffEl.encrypt( cm[4].getBytes(), buffEl.getY() ) ).toString() + "\n");
					res = null;
					buffEl = null;
					break;
				case "!elde": // ElDe [P] [A] [X] [msg](число)
					if(cm.length != 5){ SyntaxisProblem = true; break; }
					buffEl = new ElGamalCipher((new BigInteger(cm[1])).toByteArray(), (new BigInteger(cm[2])).toByteArray(), (new BigInteger(cm[3])).toByteArray(), (new BigInteger(cm[3])).toByteArray());
					chatWindow.append( new String( buffEl.decrypt( PrimeNum.NumToBytes( new BigInteger(cm[4]) ) ) ) + "\n");
					res = null;
					buffEl = null;
					break;
				case "!elsign": // ElSign [P] [A] [X] [msg]
					if(cm.length != 5) { SyntaxisProblem = true; break; }
					buffEl = new ElGamalCipher((new BigInteger(cm[1])).toByteArray(), (new BigInteger(cm[2])).toByteArray(), (new BigInteger(cm[3])).toByteArray(), (new BigInteger(cm[3])).toByteArray());
					chatWindow.append( PrimeNum.BytesToNum(  buffEl.signMessage( cm[4].getBytes() )   ) + "\n");
					break;
				case "!elverify": // ElVerify [P] [A] [other Y] [msg](число)
					if(cm.length != 5) { SyntaxisProblem = true; break; }
					buff = ElGamalCipher.verifyMessage(PrimeNum.NumToBytes(new BigInteger(cm[4])), (new BigInteger(cm[3])).toByteArray(), (new BigInteger(cm[1])).toByteArray(), (new BigInteger(cm[2])).toByteArray());
					chatWindow.append( new String(buff) + "\n");
					break;
				case "!elsignkey": // ElSignKey [P] [A] [X] [key] [msg]
					if(cm.length != 6) { SyntaxisProblem = true; break; }
					buffEl = new ElGamalCipher((new BigInteger(cm[1])).toByteArray(), (new BigInteger(cm[2])).toByteArray(), (new BigInteger(cm[3])).toByteArray(), (new BigInteger(cm[3])).toByteArray());
					buff = buffEl.signKey((new BigInteger(cm[4])).toByteArray(), (cm[5]).getBytes());
					chatWindow.append( PrimeNum.BytesToNum(buff) + "\n" );
					break;
				case "!elverifykey": // ElVerifyKey [P] [A] [other Y] [key]
					if(cm.length != 5) { SyntaxisProblem = true; break; }
					byte[][] VerKey = ElGamalCipher.verifyKey(PrimeNum.NumToBytes(new BigInteger (cm[4])), (new BigInteger(cm[3])).toByteArray(), (new BigInteger(cm[1])).toByteArray(), (new BigInteger(cm[2])).toByteArray());
					chatWindow.append("Описание: " + new String(VerKey[1]) + "\nКлюч:\n" + new BigInteger(VerKey[0]) + "\n" );
					break;
				default:
					chatWindow.append("Syntaxis error. Попробуйте ввести \"!help\"\n");
					res = null;
					break;
			}
			if(SyntaxisProblem)
				chatWindow.append("Syntaxis error. Попробуйте ввести \"!help\". Также проверьте, что вы не поставили пробелы, где не следовало\n");
		}
		catch(Throwable t)
		{
			chatWindow.append("Syntaxis error. Попробуйте ввести \"!help\"\n");
			t.printStackTrace();
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
