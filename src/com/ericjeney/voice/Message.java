package com.ericjeney.voice;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;



public class Message {	
	private String sender;
	private String text;
	private Calendar time;
	
	public Message(String sender, String text, String t) {
		this.sender = sender;
		this.text = text.trim();
		
		time = Calendar.getInstance();
		
		int hour = Integer.parseInt(t.substring(0,t.indexOf(':')));
		if(t.contains("PM")) {
			time.set(Calendar.AM_PM, Calendar.PM);
		}else {
			time.set(Calendar.AM_PM, Calendar.AM);
		}
		
		t = t.substring(t.indexOf(':')+1);
		
		int minute = Integer.parseInt(t.substring(0,t.indexOf(' ')));
		
		time.set(Calendar.SECOND, 0);
		time.set(Calendar.MILLISECOND, 0);
		
		time.set(Calendar.HOUR, hour);
		time.set(Calendar.MINUTE, minute);
	}
	
	public long getTime() {
		return time.getTimeInMillis();
	}
	
	public boolean equals(Object o) {
		if(o instanceof Message) {
			Message m = (Message) o;
			
			if(sender.equals(m.sender) && text.equals(m.text) && getTime() == m.getTime()) {
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}
	}

	public void parseMessage() throws IOException, SQLException {
		String given = getGivenName();
		
		if(text.toLowerCase().equals("help")) {
			Main.getVoice().sendSMS(sender, "Set #### mt=y/n disp=h/m/l def=0-10 off=0-10\n\nGet ####\n\n#### Moves across mountain in autonomous");
		}else if(text.toLowerCase().startsWith("name:")) {
			String name = text.substring(5).trim();
			updateName(name);
		}else if(text.toLowerCase().startsWith("set")){
			String text2 = text.substring(3).trim();
			String number = "";
			for(int i = 0; i < text2.length(); i++) {
				if(Character.isDigit(text2.charAt(i))) {
					number += text2.charAt(i);
				}else {
					break;
				}
			}
			
			if(number.length() > 0) {
				if(!Team.teams.contains(Integer.parseInt(number))) {
					Main.getVoice().sendSMS(sender, "Invalid Team Number: " + number);
				}else {
					Team.parseInformation(Integer.parseInt(number), text2.substring(number.length()).trim(), sender);
				}
			}
		}else if(text.toLowerCase().startsWith("get")) {
			if(text.contains("email")) {
				Team.sendEMail(sender, text.substring(3).trim().substring(5).trim());
			}else {
				Team.giveInformation(sender, text.substring(3).trim());
			}
		}else {
			String number = "";
			for(int i = 0; i < text.length(); i++) {
				if(Character.isDigit(text.charAt(i))) {
					number += text.charAt(i);
				}else {
					break;
				}
			}
			
			if(number.length() > 0) {
				Team.addComment(Integer.parseInt(number), text.substring(number.length()).trim(), sender);
			}else if(!given.equals("Unknown")){
				Main.getVoice().sendSMS(sender, "Message Not Understood");
			}
		}
	}
	
	private String getGivenName() throws SQLException, IOException {
		DatabaseQuery query = new DatabaseQuery("SELECT * FROM numbers WHERE number = ?");
		query.getStatement().setString(1, sender);
		ResultSet set = query.runQuery();
		
		String givenName = "Unknown";
		if(set.first()) {
			givenName = set.getString("name");
		}else {
			DatabaseQuery query2 = null;
			try {
				query2 = new DatabaseQuery("INSERT INTO numbers VALUES(?,?)");
				query2.getStatement().setString(1, sender);
				query2.getStatement().setString(2, "Unknown");
				query2.runStatement();
			}catch(Exception ex) {
				ex.printStackTrace();
			}finally {
				if(query2 != null) query2.close();
			}
			
			Main.getVoice().sendSMS(sender, "Your number is not in our system.  Please reply: \"Name: FIRST_NAME\".");
		}
		
		query.close();
		
		return givenName;
	}
	
	private void updateName(String name) {
		DatabaseQuery query = null, query2 = null;
		
		name = name.split(" ")[0];
		
		try {
			query = new DatabaseQuery("INSERT INTO numbers VALUES (?,?)");
			query.getStatement().setString(1, sender);
			query.getStatement().setString(2, name);
			query.runStatement();
			Main.getVoice().sendSMS(sender, "Successfully Added Name.  Thank You, " + name + ".");
		}catch(Exception ex) {
			try {
				query2 = new DatabaseQuery("UPDATE numbers SET name = ? WHERE number = ?");
				query2.getStatement().setString(1, name);
				query2.getStatement().setString(2, sender);
				query2.runStatement();
				
				Main.getVoice().sendSMS(sender, "Successfully Updated Name.  Thank You, " + name + ".");
			}catch(Exception e) {
				System.out.println("Oop2");
			}finally {
				if(query2 != null) query2.close();
			}
		}finally {
			if(query != null) query.close();
		}
	}
}
