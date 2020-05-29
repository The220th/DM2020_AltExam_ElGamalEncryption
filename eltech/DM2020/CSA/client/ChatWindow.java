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
	private boolean mutedOtherMode;

	private byte[] ElContactKey;
	
	public ChatWindow(final String recipient, JFrame viewFrame)
	{
		currentMode = 0;
		elCipher = null;
		aesCipher = null;
		ElContactKey = null;
		mutedOtherMode = false;

		System.out.println("Created new chat window with " + recipient);
		
		this.recipient = recipient;
		sdf = new SimpleDateFormat("HH:mm:ss");
		
		//Create frame, set size, center, and set exit on close
		frame = new JFrame("Chat with " + recipient);
		frame.setSize(600, 400);
		//frame.setSize(600, 400);
		//frame.setResizable(false);
		frame.setResizable(true);
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
				if(!tempMsg.substring(0, 1).equals("!"))
					tempMsg = "[mode " + currentMode + "] " + tempMsg;
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
		if( !recipient.equals("All") && tempMsg.length() > 0 && !tempMsg.substring(0, 1).equals("!") )
			chatWindow.append(ChatClient.getUsername() + " [" + sdf.format(time) + "]: " + tempMsg + "\n");
		
		if (recipient.equals("All") &&  (currentMode == ChatWindow.ElMode || currentMode == ChatWindow.aesMode ))
		{
			chatWindow.append(ChatClient.getUsername() + " [" + sdf.format(time) + "]: " + tempMsg + "\n");
		}
		ChatClient.sendMessage(recipient, message, Message.MESSAGE);
	}
	
	public void receiveMessage(String sender, String message)
	{
		Date time = new Date();
		chatWindow.append(sender + " [" + sdf.format(time) + "]: " + message + "\n");
	}

	public void receiveMessage(String sender, byte[] message)
	{
		Date time = new Date();
		message = MsgLogicReceive(message);
		if(message != null)
		{
			if( !sender.equals( ChatClient.getUsername() ) || currentMode == ChatWindow.NoCryptMode)
				chatWindow.append(sender + " [" + sdf.format(time) + "]: " + new String(message) + "\n");
			//chatWindow.append(sender + " [" + sdf.format(time) + "]: " + new String(message) + "\n");
		}
	}


	private byte[] MsgLogicSend(String tempMsg)
	{
		ElGamalCipher buffEl;
		boolean SyntaxisProblem = false;
		byte[] buff;
		/*
			Все команды начинаются с "!":
			help - вывести подсказки
			roll [число] - сгенерировать число от 0 до [число]
			muteMode - (не принимать)/(принимать) сообщения, которые были зашифрованы другим способом
			сlear (иди cls) - очистить поле с сообщениями

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
			ElGetContactY - получить публичный ключ того, кому вы отсылаете сообщения

			Sign [msg] - подписать сообщение
			Verify [msg] [other Y] - проверить подпись сообщения от пользователя, чей публичный ключ [other Y]
			SignKey [key] [msg] - подписать ключ
			VerifyKey [key] [other Y] - проверить подпись ключа. [other Y] - это публичный ключ того, кто подписывал

			initAES [key] - инициализировать шифровальщик, использующий алгоритм AES-256
			initAES - инициализировать шифровальщик, использующий алгоритм AES-256. Ключ сгенерируется сам
			AESgetKey - получает ключ, с помощью которого шифруются сообщения

			CryptoSwitch [число] (или [строка]). [число] такое (или [строка] такая), что:
			0 (off или none) - все входящие и исходящие сообщения передаются без шифрования.
			1 (ElGamal или el) - все входящие и исходящие сообщения шифруются с помощью алгоритма Ель-Гамаля
			2 (AES или AES256, или AES-256) - все входящие и исходящие сообщения шифруются с помощью алгоритма AES-256
		*/
		if(tempMsg.length() > 0)
		{
			if(!tempMsg.substring(0, 1).equals("!"))
			{
				switch(currentMode)
				{
					case ChatWindow.NoCryptMode:
						byte[] resMsgNonCrypt = tempMsg.getBytes();
						return addModeToMsg(resMsgNonCrypt, (byte)currentMode );
					case ChatWindow.ElMode:
						byte[] resMsgEl = elCipher.encrypt(tempMsg.getBytes(), ElContactKey);
						return addModeToMsg(resMsgEl, (byte)currentMode );
					case ChatWindow.aesMode:
						byte[] resMsgAES = aesCipher.makeAES256_withSalt(tempMsg.getBytes(), AES256.ifENCRYPT);
						return addModeToMsg(resMsgAES, (byte)currentMode );
					default:
						System.out.println("Failed successfully in switch Mode. My congratulations. And how did u do this?!\n");
						break;
				}
			}
		}
		else
			return tempMsg.getBytes();

		
		byte[] res = null; /*именно res отправится и, если команда, то ничего не выведется*/

		//tempMsg = tempMsg.replaceAll("\n", " ");
		//String[] cm = tempMsg.split(" ");
		String[] cm = tempMsg.split("\\s+|\\n+");
		chatWindow.append("\n\tОтвет запроса ( " + cm[0] + " ): \n");
		try
		{
			switch(cm[0].toLowerCase())
			{
				case "!help":
					res = null;
					chatWindow.append( ChatClient.help() );
					break;
				case "!roll":
					String numRoll = cm[1].replaceAll("[^0-9]", "");
					BigInteger a = PrimeNum.rndBigInteger(BigInteger.ZERO, new BigInteger(  numRoll  ));
					res = ("_User " + ChatClient.getUsername() + " rolls [0; " + numRoll + ") " + a.toString()).getBytes();
					chatWindow.append("Вы заролили [0; " + numRoll + "): " + a.toString() + "\n");
					break;
				case "!mutemode": // muteMode
					mutedOtherMode = !mutedOtherMode;
					chatWindow.append("Все сообщения, зашифрованные другим спобом теперь " + (mutedOtherMode?"приниматься не будут":"будут приниматься") + "\n");
					break;
				case "!clear": // clear
				case "!cls": // cls
					chatWindow.setText("");
					break;
				case "!initel": // initEl [P] [A] [X] [Y] или initEl [P] [A], или initEl
					if(cm.length == 5)
						elCipher = new ElGamalCipher((new BigInteger(cm[1])).toByteArray(), (new BigInteger(cm[2])).toByteArray(), (new BigInteger(cm[3])).toByteArray(), (new BigInteger(cm[4])).toByteArray());
					else if(cm.length == 3)
						elCipher = new ElGamalCipher((new BigInteger(cm[1])).toByteArray(), (new BigInteger(cm[2])).toByteArray());
					else if(cm.length == 1)
						elCipher = new ElGamalCipher();
					else
						SyntaxisProblem = true;
					if(!SyntaxisProblem) chatWindow.append("\tПроинициализировано\n");
					break;
				case "!initeldefault": // initEldefault [X] [Y] или initEldefault
					if(cm.length == 3)
						elCipher = new ElGamalCipher(ChatClient.getdefaultP(), ChatClient.getDefaultA(), (new BigInteger(cm[1])).toByteArray(), (new BigInteger(cm[2])).toByteArray());
					else if(cm.length == 1)
						elCipher = new ElGamalCipher(ChatClient.getdefaultP(), ChatClient.getDefaultA());
					else
						SyntaxisProblem = true;
					if(!SyntaxisProblem) chatWindow.append("\tПроинициализировано\n");
					break;
				case "!initcontact": // initContact [other Y]
					if(cm.length == 2)
						ElContactKey = (new BigInteger(cm[1])).toByteArray();
					else
						SyntaxisProblem = true;
					if(!SyntaxisProblem) chatWindow.append("\tПроинициализировано\n");
					break;
				case "!initaes": // initAES [key] или initAES
					if(cm.length == 2)
						aesCipher = new AES256( PrimeNum.NumToBytes(new BigInteger(cm[1])) );
					else if(cm.length == 1)
						aesCipher = new AES256();
					else
						SyntaxisProblem = true;
					if(!SyntaxisProblem) chatWindow.append("\tПроинициализировано\n");
					break;
				case "!aesgetkey": // AESgetKey
					if(aesCipher != null)
						chatWindow.append( PrimeNum.BytesToNum(aesCipher.getKey()) + "\n");
					else
						chatWindow.append("\t \"AES шифровальшик\" ещё не проинициализирован. Используйте команду AESgetKey\n");
					break;
				case "!elgety": // ElGetY
					if(elCipher != null)
						chatWindow.append( new BigInteger(elCipher.getY()) + "\n" );
					else
						chatWindow.append("\t Ключ ещё не проинициализирован. Используйте команды initEl или initEldefault\n");
					break;
				case "!elgetx": // ElGetX
					if(elCipher != null)
						chatWindow.append( new BigInteger(elCipher.getX()) + "\n" );
					else
						chatWindow.append("\t Ключ ещё не проинициализирован. Используйте команды initEl или initEldefault\n");
					break;
				case "!elgetp": // ElGetP
					if(elCipher != null)
						chatWindow.append( new BigInteger(elCipher.getP()) + "\n" );
					else
						chatWindow.append("\t Ключ ещё не проинициализирован. Используйте команды initEl или initEldefault\n");
					break;
				case "!elgeta": // ElGetA
					if(elCipher != null)
						chatWindow.append( new BigInteger(elCipher.getA()) + "\n" );
					else
						chatWindow.append("\t Ключ ещё не проинициализирован. Используйте команды initEl или initEldefault\n");
					break;
				case "!elgetcontacty": // ElGetContactY
					if(ElContactKey != null)
						chatWindow.append( new BigInteger(ElContactKey) + "\n" );
					else
						chatWindow.append("\t Ключ ещё не проинициализирован. Используйте команду initContact\n");
					break;
				case "!220":
				case "!220th":
				case "!the220th":
					System.out.println("DA-DA 9!");
					break;
				case "!sign": // Sign [msg]
					if(cm.length < 2){ SyntaxisProblem = true; break; }
					cm[1] = unionString(cm, 1, cm.length);
					if(elCipher != null)
						chatWindow.append(  PrimeNum.BytesToNum(elCipher.signMessage( (cm[1]).getBytes() ) )  + "\n" );
					else
						chatWindow.append("\t \"Шифровальшик\" ещё не проинициализирован. Используйте команды initEl или initEldefault\n");
					break;
				case "!verify": // Verify [msg] [other Y]
					if(cm.length != 3){ SyntaxisProblem = true; break; }
					if(elCipher != null)
						chatWindow.append( new String ( elCipher.verifyMessage(PrimeNum.NumToBytes( new BigInteger(cm[1]) ), (new BigInteger(cm[2])).toByteArray()) )  + "\n" );
					else
						chatWindow.append("\t \"Шифровальшик\" ещё не проинициализирован. Используйте команды initEl или initEldefault\n");
					break;
				case "!signkey": // SignKey [key] [msg]
					if(cm.length < 3){ SyntaxisProblem = true; break; }
					cm[2] = unionString(cm, 2, cm.length);
					if(elCipher != null)
						chatWindow.append( PrimeNum.BytesToNum( elCipher.signKey((new BigInteger(cm[1]).toByteArray()), (cm[2]).getBytes()) )  + "\n" );
					else
						chatWindow.append("\t \"Шифровальшик\" ещё не проинициализирован. Используйте команды initEl или initEldefault\n");
					break;
				case "!verifykey": // VerifyKey [key] [other Y]
					if(cm.length != 3){ SyntaxisProblem = true; break; }
					if(elCipher != null)
					{
						byte[][] KeyAndMsgByte = elCipher.verifyKey( PrimeNum.NumToBytes( new BigInteger(cm[1]) ), (new BigInteger(cm[2])).toByteArray() );
						chatWindow.append("Описание: " + new String(KeyAndMsgByte[1]) + "\nКлюч:\n" + new BigInteger(KeyAndMsgByte[0]) + "\n" );
					}
					else
						chatWindow.append("\t \"Шифровальшик\" ещё не проинициализирован. Используйте команды initEl или initEldefault\n");
					break;
				case "!elen": // ElEn [P] [A] [other Y] [msg](строка)
					if(cm.length < 5){ SyntaxisProblem = true; break; }
					cm[4] = unionString(cm, 4, cm.length);
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
					if(cm.length < 5) { SyntaxisProblem = true; break; }
					cm[4] = unionString(cm, 4, cm.length);
					buffEl = new ElGamalCipher((new BigInteger(cm[1])).toByteArray(), (new BigInteger(cm[2])).toByteArray(), (new BigInteger(cm[3])).toByteArray(), (new BigInteger(cm[3])).toByteArray());
					chatWindow.append( PrimeNum.BytesToNum(  buffEl.signMessage( cm[4].getBytes() )   ) + "\n");
					break;
				case "!elverify": // ElVerify [P] [A] [other Y] [msg](число)
					if(cm.length != 5) { SyntaxisProblem = true; break; }
					buff = ElGamalCipher.verifyMessage(PrimeNum.NumToBytes(new BigInteger(cm[4])), (new BigInteger(cm[3])).toByteArray(), (new BigInteger(cm[1])).toByteArray(), (new BigInteger(cm[2])).toByteArray());
					chatWindow.append( new String(buff) + "\n");
					break;
				case "!elsignkey": // ElSignKey [P] [A] [X] [key] [msg]
					if(cm.length < 6) { SyntaxisProblem = true; break; }
					cm[5] = unionString(cm, 5, cm.length);
					buffEl = new ElGamalCipher((new BigInteger(cm[1])).toByteArray(), (new BigInteger(cm[2])).toByteArray(), (new BigInteger(cm[3])).toByteArray(), (new BigInteger(cm[3])).toByteArray());
					buff = buffEl.signKey((new BigInteger(cm[4])).toByteArray(), (cm[5]).getBytes());
					chatWindow.append( PrimeNum.BytesToNum(buff) + "\n" );
					break;
				case "!elverifykey": // ElVerifyKey [P] [A] [other Y] [key]
					if(cm.length != 5) { SyntaxisProblem = true; break; }
					byte[][] VerKey = ElGamalCipher.verifyKey(PrimeNum.NumToBytes(new BigInteger (cm[4])), (new BigInteger(cm[3])).toByteArray(), (new BigInteger(cm[1])).toByteArray(), (new BigInteger(cm[2])).toByteArray());
					chatWindow.append("Описание: " + new String(VerKey[1]) + "\nКлюч:\n" + new BigInteger(VerKey[0]) + "\n" );
					break;
				case "!cryptoswitch": // CryptoSwitch [что-то]
				/*
					0 (off или none) - все входящие и исходящие сообщения передаются без шифрования.
					1 (ElGamal или el) - все входящие и исходящие сообщения шифруются с помощью алгоритма Ель-Гамаля
					2 (AES или AES256, или AES-256) - все входящие и исходящие сообщения шифруются с помощью алгоритма AES-256
				*/
					if(cm.length != 2) { SyntaxisProblem = true; break; }
					if( (cm[1]).toLowerCase().equals("0") || (cm[1]).toLowerCase().equals("off") || (cm[1]).toLowerCase().equals("none") )
					{
						currentMode = ChatWindow.NoCryptMode;
						chatWindow.append("Шифрование отключено\n");
					}
					else if( (cm[1]).toLowerCase().equals("1") || (cm[1]).toLowerCase().equals("elgamal") || (cm[1]).toLowerCase().equals("el") )
					{
						if(elCipher != null && ElContactKey != null)
						{
							currentMode = ChatWindow.ElMode;
							chatWindow.append("Теперь все сообщения отправленные и входящие будут автоматически шифроваться с помощью алгорима Ель-Гамаля в соотвествии с выставленными настройками\n");
						}
						else
							chatWindow.append("\t \"Шифровальшик\" или публичный ключ получателя ещё не проинициализированы. Используйте команды initEl, initEldefault или initContact\n");
					}
					else if( (cm[1]).toLowerCase().equals("2") || (cm[1]).toLowerCase().equals("aes") || (cm[1]).toLowerCase().equals("aes256") || (cm[1]).toLowerCase().equals("aes-256") )
					{
						if(aesCipher != null)
						{
							currentMode = ChatWindow.aesMode;
							chatWindow.append("Теперь все сообщения отправленные и входящие будут автоматически шифроваться с помощью алгорима AES-256 в соотвествии с выставленными настройками\n");
						}
						else
							chatWindow.append("\t \"AES-256 шифровальшик\" ещё не проинициализирован. Используйте команду AESgetKey\n");
					}
					else
						SyntaxisProblem = true;
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
		byte[] msg = null;
		byte[][] buff = checkMsgMode(tempMsg);
		byte mode = buff[0][0];
		if(mode != (byte)currentMode)
		{
			if(mutedOtherMode)
				msg = null;
			else
				msg = buff[1];
		}
		else
		{
			switch(currentMode)
			{
				case ChatWindow.NoCryptMode:
					msg = buff[1];
					break;
				case ChatWindow.ElMode:
					try
					{
						msg = elCipher.decrypt( buff[1] );
					}
					catch(Exception e)
					{
						System.out.println("In [mode 1] received msg not for you");
					}
					break;
				case ChatWindow.aesMode:
					try
					{
						msg = aesCipher.makeAES256_withSalt(buff[1], AES256.ifDECRYPT);
					}
					catch(Exception e)
					{
						System.out.println("In [mode 2] received msg not for you");
					}
					break;
				default:
					msg = "Установлен неправильный mode".getBytes();
					break;
			}
		}
		return msg;
	}

	/**
	 * Прикрепряет к сообщению текущий mode
	 * 
	 * @param msg - исходное сообщение
	 * @param mode - текущий mode шифрования
	 * @return mode + исходное сообщение
	 */
	public static byte[] addModeToMsg(byte[] msg, byte mode)
	{
		byte[] res = new byte[msg.length + 1];
		
		for(int i = 0; i < msg.length; i++)
			res[i+1] = msg[i];
		res[0] = mode;
		return res;
	}

	/**
	 * Достаёт из сообщения текущий mode и изменят сообщение как было
	 * 
	 * @param msg - mode + исходное сообщение
	 * @return res[0][0] - mode; res[1] - исходное сообщение, без mode
	 */
	public static byte[][] checkMsgMode(byte[] msg)
	{
		byte[][] res = new byte[2][];
		res[1] = new byte[msg.length-1];
		res[0] = new byte[1];
		for(int i = 0; i < (res[1]).length; i++)
			res[1][i] = msg[i+1];
		res[0][0] = msg[0];
		return res;
	}

	/**
	 * Объединяет строки из cm в одну, имеющих индекс [i; j), через пробел. Если i >= j, то вернёт только строку по индексу j, если i или j больше, чем cm.length, то вернёт cm[0]
	 * 
	 * @param cm - массив строк, где некоторые нужно объединить
	 * @param i - начиная с этого индекса
	 * @param j - заканчивая этим индексом (не включая его)
	 * @return объединённую строку
	 */
	public static String unionString(String[] cm, int i, int j)
	{
		if(i >= cm.length || j > cm.length)
			return cm[0];
		if(i >= j)
			return cm[j];
		int li;
		StringBuilder sb = new StringBuilder();
		for(li = i; li < j; li++)
			sb.append(cm[li] + " ");
		return sb.toString();
	}
}
