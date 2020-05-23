package eltech.DM2020.CSA.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.math.BigInteger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;

import eltech.DM2020.CSA.cmn.Message;
import eltech.DM2020.ElGamalCipher;
import eltech.DM2020.AES256;
import eltech.DM2020.PrimeNum;

public class ChatClient 
{
	final static int portNumber = 8001;
	private static String hostName = "";
	private static CCView view;
	private static ObjectInputStream cObjIn;
	private static ObjectOutputStream cObjOut;
	private static Socket socket;
	private static String username;
	public static HashMap<String, ChatWindow> chats;
	public static Queue<Message> msgQueue;

	private static String[] lastUpdateUsers;
	private static ArrayList<String> markedUsers;

	//=(
	public static final byte[] defaultA = (new BigInteger("1823736889973623579880975232092448518136755804545159225530451490153771957867772520543331295261452855466079816331852543338477704070493503735310826089169374151404140552428196601252038027259237108312729949671166501899167419327707322364341347356562526945432700549630933020914616184449973947983878498736614288850301067411903980595720077270623846455282723049967251979770873826771057807648256726363947151297251917781630610428698974751907252599618134451206973366362674495002152668900967767083488545818418809377814608854785368001385135071785182294144389920256105284450583857392322429857777215208191612229098989723658048980155045483753400727846392301311922887961147985622486409325618151954074924320885606017035195776136820332319045604670412010180708996703868483655127814464428443756436097413397213861642300820307922447546549320953632411321215403973136371043353510639837947295029609939972087940778741368867793716528409793369203603250004664895790559159856515461952614665059250513816466137006965139765707822963305008850003844405237041711215836000520650715887520071517853672094305028232989218314531105680881397689264757142294303192949379736294818725189538435840518952185047783508298128136894482180091545278301527668221869185156412903038352783445876453636918010747187831120629267012912531740944094309274542621038385166990422447208901963861011415349333205463634509557761303406754585208377251548661923326362332370301657811697513914966973366989527722638150177582705651091012911094387009714411945587788129877076899692792839238609413384883010808669579219372830112174342131146925768188804651451754119639257585066496026909181846869098202151755098428126188390211658944899294279634899335487532154022213206520568257087085625791547689471657972966897709783574344530116902599192995910191883641226357282639473340381011611065857840788883288456747232442563675666629748296680156416379903174277647260217947128850752432116500072773354198232479453709175157171830516896359980424303926845669754764919637242773820759691421487439190815069636036568032610134585415366439230487920433439240451037")).toByteArray();
	public static final byte[] defaultP = (new BigInteger("3059948265576697055654798349610816034604773243403863023150578718877729532684562987694453313878251451254283494498789726949726198487680738503410570538673840061074838237828562097741482642255867011465702352542767321887346917300678841123047258048923224244673142929764836199357635920344973852083877916404882114363785252975772502420212752565228142833749030552920866210733085036517974323136561548219958510339609615489690427986347201928110093355890261675208936231441192928362396921036037853750461824689953347443291989148450689440788626055682908121792437405760536324095016905767528384892425057751691127821608654060158624587234899229172972274894920727708428165610055056408921258926727374045455384517073828765227636867520573846350284270575312946488455672201969665194810217940596952487848914745672526688928094556035410117561527975627024144208162641454386784233929329243195346943911147852945575080772276881872266439305682037625291066048242838382853843982270242783995859946569757766242751613482773268355037590205809822230750516349041685444410989408139329748915517013914990825705636436399786144094950925447925212112913022891056376867425777853438325033086050666935160287355750561140897366979311788041683137796754312137382350856750444545769202049604256396500340448694840579500520465791049921195865076726687623329629786518395308080080322177838202242538506345732428718372839422202373657610730895830129848104390660485337116024743984720512029311510211609053290618334471178104289213538234025824478464573862444626679042328656261189700741711101167543670083867325779491907083703414325126357915173028377555675266530232252522717146330663921108479216380359720353278656264430380900243576985325092196172062092904887449402833588758116896920355720482387995700920644643770845128603121496344388833583766522101785062937368542666961702932321151817447829473644209507902622210472382365750501543436846610857115572931254726875013533420080655500445258703038240370981580041788752092497377248866914091232110154678533524779293070778856463226681028683030546280365688672870439605364625095483621973987")).toByteArray();
	/*
	ex:
	X1: 13284436701766055305681954123
	Y1: 149041773692437152830654473167934908345060900085807
	A1: 8814638583445620852072754611035792263801233986748
	P1: 150554329859844963904461537878343646900906702146443
	X2: 920256062478
	Y2: 124702246879575260435636564973056534668851922447160
	A2: 8814638583445620852072754611035792263801233986748
	P2: 150554329859844963904461537878343646900906702146443

	!ElEn 150554329859844963904461537878343646900906702146443 8814638583445620852072754611035792263801233986748 124702246879575260435636564973056534668851922447160 Ky2142151
	!ElDe 150554329859844963904461537878343646900906702146443 8814638583445620852072754611035792263801233986748 920256062478 327945734547098735949314580699281942231994981613961286214970315271074433672645130241607659536785959431788671823711931452998

	*/


	public ChatClient() {
		
		markedUsers = new ArrayList<String>();

		view = new CCView();
		try {
			connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		msgQueue = new LinkedList<Message>();
		chats = new HashMap<String, ChatWindow>();
		
		Listener chatListener = new Listener();
		chatListener.start();
	}
	
	public static void connect() throws IOException{
		try {
			socket = new Socket(hostName, portNumber);
			
			//Debugging
			System.out.println("Connected to server.");

			//Set up the streams
			cObjOut = new ObjectOutputStream(socket.getOutputStream());
			cObjIn = new ObjectInputStream(socket.getInputStream());

			//Try logging into the server with username provided in dialog box
			cObjOut.writeObject(new Message(null, null, username, Message.LOGIN));
			cObjOut.flush();

			//Login confirmation is a String object from server
			Message confirmation;

			confirmation = (Message) cObjIn.readObject();

			if(confirmation.getData().equals("SUCCESS"))
			{
				System.out.println("Connection successful.");
				
				//Initial update of users
				Message users = (Message) cObjIn.readObject();
				lastUpdateUsers = (String[]) users.getData();
				System.out.println("Now online: " + Arrays.asList(lastUpdateUsers));
				view.updateUsers(lastUpdateUsers.clone(), markedUsers);
			}
			else if(confirmation.getData().equals("")){
				System.out.println("Username is blank.  Try connecting again with a username");
			}	
			else{
				System.out.println("Connection failed.  Username may not be unique or is blank.  Try again.");
			}
		} catch (ClassNotFoundException e) {
			System.err.println("Wrong class returned.");
			e.printStackTrace();
		} catch (UnknownHostException e) {
			System.err.println("Host " + hostName + " and Port " + portNumber
					+ " could not be resolved.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Unable to establish I/O connection with " +
					hostName + " and Port " + portNumber);
			e.printStackTrace();
		}
	}	
	
	//Parse Message -- determine the type of Message and act accordingly
	public void parseMessage(Message message){
		try {
			switch (message.getType())
			{
				case Message.MESSAGE: 
						receiveMessage(message);
						break;
				case Message.TOALL:
					{
						receiveMessage_toAll(message);
						break;
					}
				case Message.UPDATE_USERS: 
					{
						lastUpdateUsers = (String[]) message.getData();
						view.updateUsers(lastUpdateUsers, markedUsers);
						break;
					}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Receive message and route to all 
	public void receiveMessage_toAll(Message message)
	{
		//System.out.println(chats);
		if( chats.containsKey("All") )
		{
			chats.get( "All" ).receiveMessage(message.getSender(), message.getMsg());
		}
		else
		{
			markedUsers.add("All");
			view.updateUsers( lastUpdateUsers, markedUsers);
		}
	}
	
	//Receive message and route to correct ChatWindow
	public void receiveMessage(Message message){
		//System.out.println(chats);
		if(chats.containsKey(message.getSender()))
		{
			chats.get(message.getSender()).receiveMessage(message.getSender(), message.getMsg());
		}
		else
		{
			markedUsers.add(message.getSender());
			view.updateUsers( lastUpdateUsers, markedUsers);
		}
	}
	
	//Send Message
	public static void sendMessage(String recipient, String message, int type)
	{
		if(recipient != null && recipient.toLowerCase().equals("all"))
			type = Message.TOALL;
		Message msg = new Message(recipient, username, message, type);
		if(markedUsers.contains(recipient))
		{
			markedUsers.remove(recipient);
			view.updateUsers(lastUpdateUsers, markedUsers);
		}
		try 
		{
			cObjOut.writeObject(msg);
			cObjOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Send byte Message
	public static void sendMessage(String recipient, byte[] message, int type)
	{
		if(recipient != null && recipient.toLowerCase().equals("all"))
			type = Message.TOALL;
		Message msg = new Message(recipient, username, message, type);
		if(markedUsers.contains(recipient))
		{
			markedUsers.remove(recipient);
			view.updateUsers(lastUpdateUsers, markedUsers);
		}
		try 
		{
			cObjOut.writeObject(msg);
			cObjOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void setUsername(String user){
		username = user;
	}

	public static void refreshMarkedList_rm(String user)
	{
		if(chats.containsKey( user ))
			chats.remove(user);
		if(markedUsers.contains(user))
			markedUsers.remove(user);
		view.updateUsers(lastUpdateUsers, markedUsers);
	}
	
	public static void setIP(String IP)
	{
		hostName = IP;
	}
	
	public static String getUsername()
	{
		return username;
	}
	
	public static void shutdown(){
		//TODO - upon closing, send logout message that closes streams, removes user from
		sendMessage(null, "LOGOUT", Message.LOGOUT);
	}
	
	public static void main(String[] args) throws IOException{
		@SuppressWarnings("unused")
		ChatClient client = new ChatClient();
	}

	public static byte[] getDefaultA()
	{
		return ChatClient.defaultA;
	}

	public static byte[] getdefaultP()
	{
		return ChatClient.defaultP;
	}

	public static String help()
	{
		String answer = "";
		String line;
		try (BufferedReader inFile = new BufferedReader(new InputStreamReader( new FileInputStream("ReadMe.txt"), "UTF-8")))
		{
			while ( (line = inFile.readLine()) != null )
				answer += line + "\n";
		}
		catch(Throwable t)
		{
			System.out.println(t);
		}
		return answer;
	}
	
	//Update 5/5/2015: Create member class as helper for ChatClient.  This 
	//class will contain the Thread listening on the Port and will have access 
	//to all the ChatClient's incoming data.  The Listener will poll a message 
	//queue.  Many synchronization issues can effectively be solved by
	//synchronizing this member class's run method and passing all Message objects
	//through it.
	public class Listener extends Thread {

		public synchronized void run(){
			try{
				
				while(true)
				{
					//Probably need a lock here
					msgQueue.add((Message)cObjIn.readObject());
					parseMessage(msgQueue.poll());
				}
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}