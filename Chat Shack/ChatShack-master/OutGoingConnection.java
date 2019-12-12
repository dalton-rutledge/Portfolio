/**
 * This OutGoingConnection class contains all of the OUTGOING message logic for the ChatShackServer Application
 * Outgoing messages are popped from the Vector 'messageList' which is passed in as a parameter to this class when the server starts
 * Outgoing messages are sent to the correct location using each message's individual destination field, and the HashMap of userconnections 
 * (which is also passed in as instance data from the server) which stores dataoutputsteams from sockets 
 * Dalton Rutledge
 */



import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.net.*;
import java.io.*;

public class OutGoingConnection implements Runnable
{
    public static final int BUFFER_SIZE = 4096;
    private HashMap<String, BufferedOutputStream> userConnections;
    private Vector<String> messageList; 

    public OutGoingConnection(Vector<String> messages, HashMap<String, BufferedOutputStream> usersConnections){
        this.messageList = messages;
        this.userConnections = usersConnections;    
    }

    public void run() {
        while (true) {
            // sleep for 1/10th of a second
            try { Thread.sleep(100); } catch (InterruptedException ignore) { }


            /**
             * check if there are any messages in the Vector. If so, remove them
             * and broadcast the messages to the correct locations 
             **/

            while(!(messageList.isEmpty())){
                System.out.println("entered the loop");
                //pop the front most message off the list (because messages get appended, so the front one was sent first)
                String curMsg = messageList.remove(0);

                System.out.println(curMsg);

                //prepare the message to be sent
                byte[] msgBytes = new byte[BUFFER_SIZE];
                msgBytes = curMsg.getBytes();
                int msgLen = msgBytes.length;

                //Sort out message pieces:
				int firstBar = curMsg.indexOf("|");
				int secondBar = curMsg.indexOf("|", firstBar+ 1);
				int thirdBar = curMsg.indexOf("|", secondBar + 1);

				String command = curMsg.substring(0, firstBar);
                String destination = curMsg.substring(secondBar + 1, thirdBar);
                
                // handle all broadcast style messages
                if(command.equals("JOIN") || command.equals("BDMG") || command.equals("LEAV")){
                    for (BufferedOutputStream toClient : userConnections.values()) {
                        try{
                        System.out.println("writing message");
                        toClient.write(msgBytes, 0, msgLen);
                        toClient.flush();
                        System.out.println("writen message");
                        }
                        catch (IOException ioe) {
                            System.err.println(ioe);
                        } 
                    }
                }
                // handle private messages
                else{
                    BufferedOutputStream pvt = userConnections.get(destination);
                    try{
                        pvt.write(msgBytes, 0, msgLen);
                        pvt.flush();
                    }
                    catch (IOException ioe) {
                        System.err.println(ioe);
                    } 
                }
            }
        }
    }
} 