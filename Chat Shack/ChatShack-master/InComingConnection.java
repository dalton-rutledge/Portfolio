/**
 * This InComingConnection class contains all of the INCOMING message logic for the ChatShackServer Application
 * Incoming messages are stored in the Vector 'messageList' which is passed in as a parameter to individual connections
 * Valid incoming messages are stored in the Vector 'messageList'
 * When a successful join message is received, the thread will add that user and socket outgoing data stream to the userConnections Hashmap
 * All other parts of the thread will check to make sure a valid usrname has been made (only possible through the join command in the protocol)
 * Dalton Rutledge
 */

import java.net.*;
import java.io.*;
import java.util.Vector;
import java.util.HashMap;

public class InComingConnection implements Runnable
{
	private Socket	client;
	public static final int BUFFER_SIZE = 4096;

	private Vector<String> messageList;
	private HashMap<String, BufferedOutputStream> userConnections;
	private String usrname;
	
	public InComingConnection(Socket client, Vector<String> messageList, HashMap<String, BufferedOutputStream> userConnections) {
		this.client = client;
		this.messageList = messageList;
		this.userConnections = userConnections;
		this.usrname = "";
	}
    /*
     * This method runs in a separate thread.
     */	
	public void run() { 
		
		// declaring data some streams
		BufferedInputStream  fromClient = null;
		BufferedOutputStream toClient = null;

		//all necessary error messages
		String good200 = "STAT|200\r\n";
		String error400 = "STAT|400\r\n";
		String error420 = "STAT|420\r\n";
		String error421 = "STAT|421\r\n";

		try {
			//Open data streams
			fromClient = new BufferedInputStream(client.getInputStream());
			toClient = new BufferedOutputStream(client.getOutputStream());

			/*
			 * While-loop that reads messages from the client 
			 * Parse incoming messages according to the protocol
			 * Handle joins first to set username and update hashmap
			 * Then move on to all other commands
			 * Make sure to update the vector with all relevant messages
			 */

			boolean connected = true;
			while(connected){	
				toClient.flush();
				byte[] buffer = new byte[BUFFER_SIZE];
				//recieve and trim the client string
				String msgIn = "";
				int numBytes = fromClient.read(buffer);
				msgIn += new String(buffer, 0, numBytes);
				msgIn = msgIn.trim();

				//Sort out message pieces:
				int firstBar = msgIn.indexOf("|");
				int secondBar = msgIn.indexOf("|", firstBar+ 1);
				int thirdBar = msgIn.indexOf("|", secondBar + 1);

				String command = msgIn.substring(0, firstBar);
				String requestedUserName = msgIn.substring(firstBar + 1, secondBar); 
				String destination = msgIn.substring(secondBar + 1, thirdBar);


				// Handle the JOIN command
				if(command.equals("JOIN")){
					boolean goodName = true; 
					//check if the requested username is already taken
					if(userConnections.containsKey(requestedUserName)){
						goodName = false;
					}

					//check for bad characters in the username
					//if(requestedUserName.contains("awdadw"))

					//check to see if the username is too long
					if(requestedUserName.length()>15){
						goodName = false;
					}

					//if the username is valid, add the message to the list, add the user to the hashmap, and write back a STAT|200
					if(goodName){
						userConnections.put(requestedUserName, toClient);
						messageList.add(msgIn);
						this.usrname = requestedUserName;
						byte[] errorBytes = new byte[BUFFER_SIZE];
						errorBytes = good200.getBytes();
						int errLen = errorBytes.length;
						toClient.write(errorBytes, 0, errLen);
					}
					//if the username is bad, write back a STAT|420
					else {
						byte[] errorBytes = new byte[BUFFER_SIZE];
						errorBytes = error420.getBytes();
						int errLen = errorBytes.length;
						toClient.write(errorBytes, 0, errLen);
					}
				}
				//handle other messages only if this thread has a set username
				else if(!(this.usrname.equals(""))){
					//handle private messages
					if(command.equals("PVMG")){
						//if that user exists, add the message to the list and send 200
						if(userConnections.containsKey(destination)){
							messageList.add(msgIn);
							byte[] errorBytes = new byte[BUFFER_SIZE];
							errorBytes = good200.getBytes();
							int errLen = errorBytes.length;
							toClient.write(errorBytes, 0, errLen);
						}
						//otherwise send back an error 421
						else{
							byte[] errorBytes = new byte[BUFFER_SIZE];
							errorBytes = error421.getBytes();
							int errLen = errorBytes.length;
							toClient.write(errorBytes, 0, errLen);
						}
					}
					//handle the LEAV command by adding in the message to the vector, sending back a STAT|200, and closing the conneciton
					else if(command.equals("LEAV")){
						messageList.add(msgIn);
						byte[] errorBytes = new byte[BUFFER_SIZE];
						errorBytes = good200.getBytes();
						int errLen = errorBytes.length;
						toClient.write(errorBytes, 0, errLen);
						connected = false;
						userConnections.remove(this.usrname);
					}
					//handle the broadcast message by adding in the message to the list and sending back the STAT|200
					else{
						messageList.add(msgIn);
						byte[] errorBytes = new byte[BUFFER_SIZE];
						errorBytes = good200.getBytes();
						int errLen = errorBytes.length;
						toClient.write(errorBytes, 0, errLen);
					}
				}

				//otherwise the message is invalid and we send a STAT|400
				else{
					byte[] errorBytes = new byte[BUFFER_SIZE];
					errorBytes = error400.getBytes();
					int errLen = errorBytes.length;
					toClient.write(errorBytes, 0, errLen);
				}
			}

			//close streams
			if (fromClient != null)
			fromClient.close();
			if (toClient != null)
			toClient.close();
		}
		catch (java.io.IOException ioe) {
			System.err.println(ioe);
		}
}
}