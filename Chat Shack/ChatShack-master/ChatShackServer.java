
/**
 * The ChatShackServer implements the class made chatroom protocol, and 
 * allows clients to connect with a join command. Incoming messages from connections are handled in the InComingConnection class.
 * Outgoing messages are handled by the broadcast thread in the outGoingConnection class
 */

import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.Vector;
import java.util.HashMap;

public class ChatShackServer {
    public static final int DEFAULT_PORT = 1337;

    // construct a thread pool for concurrency
    private static final Executor exec = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        ServerSocket sock = null;
        // Vector that holds messages, accessible by all instances of InComingConnection and the single instance of OutGoingConneciton
        Vector<String> messageList = new Vector<String>();
        // Hashmap that stores usernames to output streams
        HashMap<String, BufferedOutputStream> users = new HashMap<String, BufferedOutputStream>();

        try {
            // establish the socket
            sock = new ServerSocket(DEFAULT_PORT);
            //establish the OutGoingConnection object. This is the broadcast thread responsible for forwarding messages to clients via the vector
            Runnable outgoing = new OutGoingConnection(messageList, users);
            exec.execute(outgoing);

            while (true) {
                //now listen for connections and service the connection in a separate thread
                Runnable task = new InComingConnection(sock.accept(), messageList, users);
                exec.execute(task);
            }
        } 
        catch (IOException ioe) {
            System.err.println(ioe);
        } 
        finally {
            if (sock != null)
                sock.close();
        }
    }
}
