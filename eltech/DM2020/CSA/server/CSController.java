package eltech.DM2020.CSA.server;

//The CSController interacts with the CSView, may be expanded as functionality
//on CSView is added or required.
public class CSController {

	private CSView view;
	
	public CSController(){}
	
	public void setCSView(CSView view){
		this.view = view;
	}
	
	//Writes messages to the View's message window
	public void writeMsg(String string)
	{
		System.out.println();
		//view.serverMsgWindow.append(">>> " + string + "\n");
	}
}
