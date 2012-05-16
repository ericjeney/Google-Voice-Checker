package com.ericjeney.voice;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;

import com.techventus.server.voice.Voice;


public class Main {
	public static final String USERNAME = "INSERT USERNAME HERE";
	public static final String PASSWORD = "INSERT PASSWORD HERE";
	
	private static ArrayList<Message> messageList = new ArrayList<Message>();
	private static Voice voice;
	
	public static void readMultipleLines(BufferedReader reader, int count) throws IOException {
		for(int i = 0; i < count; i++) {
			reader.readLine();
		}
	}
	
	public static Voice getVoice() {
		return voice;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException, SQLException {
		voice = new Voice(USERNAME, PASSWORD);
		
		System.out.println();
		System.out.println();
		
		System.setOut(new OutputOverride());
		
		Message lastMessage = null;
		boolean firstRun = true;
		
		while(true) {
			String inbox = voice.getSMS();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(inbox.getBytes())));
			
			Message newestMessage = null;
			for(String line = reader.readLine(); line != null; line = reader.readLine()) {
				if(line.contains("<div class=\"gc-message-sms-row\">")) {
					readMultipleLines(reader, 3);
					String number = reader.readLine().trim();
					number = number.substring(0, number.length()-1);
					
					readMultipleLines(reader, 3);
					String message = reader.readLine();
					message = message.trim();
					message = message.substring(message.indexOf('>')+1);
					while(!message.contains("</span")) {
						message += reader.readLine().trim();
					}
					message = message.substring(0, message.indexOf('<'));
					
					if(message.contains("sent you an SMS from Gmail.")) {
						continue; // Skip GMail notification.
					}
					
					readMultipleLines(reader, 2);
					
					String time = reader.readLine().trim();
					while(!time.contains("AM") && !time.contains("PM")) {
						time = reader.readLine().trim();
					}
					
					// Must be sent by the daemon
					if(number.contains("Me")) continue;
					
					String fullMessage = number + ": " + message;
					
					Message m = new Message(number, message, time);
					
					if(!messageList.contains(m)) {
						messageList.add(m);
						
						if(!firstRun) {
							System.out.println(m.getTime() + "  --  " + fullMessage);
							m.parseMessage();
						}
					}
					
					if(newestMessage == null || m.getTime() > newestMessage.getTime()) {
						newestMessage = m;
					}
				}
			}
			
			lastMessage = newestMessage;
			firstRun = false;
			Thread.sleep(5000);
		}
	}
}
