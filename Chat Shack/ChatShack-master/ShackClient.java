/**
 * this is the reader thread of the client 
 */

import java.net.*;
import java.io.*;
import javax.swing.*;

public class ShackClient implements Runnable
{
	private Socket server;
	private BufferedInputStream fromServer;
	bigG screen;

	public ShackClient(Socket server, bigG screen) {
		this.server = server;
		this.screen = screen;
	}
	public void run() {

		try {
			fromServer = new BufferedInputStream(server.getInputStream());

			while (true) {
				byte[] buffer = new byte[4096];
				String msgIn = "";
				int numBytes = fromServer.read(buffer);
				msgIn += new String(buffer, 0, numBytes);
				msgIn = msgIn.trim();
				System.out.println(msgIn);

				String[] first = msgIn.split("\r\n");
				String[] second = first[0].split("\\|");
				
				String username = "";
				String content = "";

				if (second[0].equals("STAT")){
					if (second[1].equals("421")){
						content = "receiver of private message does not exist";
						username = second[0];
						// dat = "";
						screen.updatecontent(username, content);
					}
				}
				else if (!(second[1].equals(bigG.ourusername))){

					screen.updateusers(second[0], second[1]);
					if (second[0].equals("PVMG")){
						username = second[1]+ " to you";
						content = first[1];
					}
					if (second[0].equals("LEAV")){
						username = "server";
						content = second[1] + " left the chat";
					}
					if (second[0].equals("JOIN")){
						username = "server";
						content = second[1] + " joined the chat";
					}
					if (second[0].equals("BDMG")){
						username = second[1];
						content = first[1];
					}
					screen.updatecontent(username, content);
				}
			}
		}
		catch (IOException ioe) { System.out.println(ioe); }

	}
}
