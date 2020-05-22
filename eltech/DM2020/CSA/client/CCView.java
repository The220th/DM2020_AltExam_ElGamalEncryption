package eltech.DM2020.CSA.client;

import java.util.ArrayList;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;

import eltech.DM2020.CSA.cmn.Message;

@SuppressWarnings("unused")
public class CCView {

	private JFrame frame;
	private Container contentPane;
	private Border border;
	private DefaultListModel<String> model;
	@SuppressWarnings("rawtypes")
	private JList userList;
	
	public CCView(){
		
		//Create frame, set size, center, and set exit on close
		frame = new JFrame("Chat Client");
		frame.setSize(250, 500);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Create Border
		border = BorderFactory.createLineBorder(Color.gray);
		
		//Set up pane
		contentPane = frame.getContentPane();
		
		//Create userList
		model = new DefaultListModel<String>();
		userList = new JList<String>(model);
		userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		userList.setBorder(border);
		
		contentPane.add(userList);
		
		frame.setContentPane(contentPane);
		frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				System.out.println("Used cross");
				ChatClient.shutdown();
				System.exit(0);
			}
		});
		frame.setVisible(true);
		
		LoginDialog loginDialog = new LoginDialog(frame);
		loginDialog.setLocationRelativeTo(frame);
		loginDialog.setVisible(true);
		
		frame.setTitle(ChatClient.getUsername() + "'s Chat Client");
		
		userList.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
		    	if(e.getClickCount()==2){
					String recipient = (String) userList.getSelectedValue();
					recipient = recipient.replace("*", "");
		    		ChatWindow chatWindow = new ChatWindow(recipient, frame);
					ChatClient.chats.put(recipient, chatWindow);
		        }
		    }
		});
	}
	
	public void updateUsers(String[] userArray, ArrayList markedUsers)
	{
		int i;
		String buffS;

		model.clear();

		i = 0;
		if( markedUsers.contains("All") )
			model.add(i, "All*");
		else
			model.add(i, "All");

		for(i = 0; i < userArray.length; i++)
		{
			buffS = userArray[i];
			if(markedUsers.contains(buffS))
				buffS += "*";
			model.add(i+1, buffS);
		}
	}

	public void newChat(Message message) {
		ChatWindow chatWindow = new ChatWindow(message.getSender(), frame);
		ChatClient.chats.put(message.getSender(), chatWindow);
	}
}
