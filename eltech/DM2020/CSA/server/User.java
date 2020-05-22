package eltech.DM2020.CSA.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


//This has mostly been replaced by the CSHandler and the HashMap
//This class will most likely be removed if it's not necessary
public class User {

	String username; //unique identifier
	Socket clientSocket;
	String IP;
	ObjectInputStream objIn;
	ObjectOutputStream objOut;
	
	public User(Socket clientSocket, String username, ObjectInputStream objIn, ObjectOutputStream objOut){
		this.clientSocket = clientSocket;
		this.username = username;
		this.IP = clientSocket.getLocalAddress().getHostName();
		this.objIn = objIn;
		this.objOut = objOut;
	}
	
	public String getUsername(){
		return username;
	}
}
