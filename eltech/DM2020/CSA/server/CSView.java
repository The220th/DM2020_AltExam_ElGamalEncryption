package eltech.DM2020.CSA.server;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;

public class CSView {

	private JFrame frame;
	private Container contentPane;
	private Border border;
	private JList<String> userList;
	private DefaultListModel<String> model;
	private JScrollPane scrollPane;
	public JTextArea serverMsgWindow;
	@SuppressWarnings("unused")
	private CSController controller;
	
	public CSView(final CSController controller){
		this.controller = controller;	//Constructor dependency injection
		
		//Create frame, set size, center, and set exit on close
		frame = new JFrame("Chat Server");
		frame.setSize(450, 600);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Create Border
		border = BorderFactory.createLineBorder(Color.gray);
		
		//Create the Swing components
		model = new DefaultListModel<String>();
		userList = new JList<String>(model);
		userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		userList.setBorder(border);
		
		//Server message window with scroll bar
		serverMsgWindow = new JTextArea(10, 30);
		serverMsgWindow.setEditable(false);
		scrollPane = new JScrollPane(serverMsgWindow);
		scrollPane.setBorder(border);
		
		contentPane = frame.getContentPane();
		contentPane.setLayout(new GridLayout(2, 1));
		
		contentPane.add(userList);
		contentPane.add(scrollPane);
		
		frame.setContentPane(contentPane);
		frame.setVisible(true);
	}
	
	public void updateUsers(String[] userArray){

		model.clear();
		
		for(int i = 0; i < userArray.length; i++){
			model.add(i, userArray[i]);
		}
	}
}
