package com.ericjeney.voice;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.StringTokenizer;



public class Team {
	public static ArrayList<Integer> teams = new ArrayList<Integer>();
	
	static {
		DatabaseQuery query = null;
		try {
			query = new DatabaseQuery("SELECT t_number FROM teams");
			ResultSet set = query.runQuery();
			if(set.first()) {
				teams.add(set.getInt(1));
				while(set.next()) {
					teams.add(set.getInt(1));
				}
			}
		}catch(Exception ex) {
			System.out.println("Couldn't Get Team List");
			ex.printStackTrace();
		}finally {
			if(query != null) query.close();
		}
	}
	
	public static void addComment(Integer number, String comment, String sender) {
		DatabaseQuery query = null;
		try {
			query = new DatabaseQuery("INSERT INTO comments VALUES (DEFAULT,?,?,?)");
			query.getStatement().setInt(1, number);
			query.getStatement().setString(2, comment);
			query.getStatement().setString(3, sender);
			query.runStatement();
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			if(query != null) query.close();
		}
	}
	
	public static void parseInformation(Integer number, String text, String sender) throws IOException {
		StringTokenizer token = new StringTokenizer(text);
		ArrayList<String> unknown = new ArrayList<String>();
		while(token.hasMoreTokens()) {
			String tok = token.nextToken();
			if(tok.startsWith("mt")) {
				updateMountain(number, tok.substring(3));
			}else if(tok.startsWith("mountain")) {
				updateMountain(number, tok.substring(9));
			}else if(tok.startsWith("disp")) {
				updateDispenser(number, tok.substring(5));
			}else if(tok.startsWith("dispenses") || tok.startsWith("dispenser")) {
				updateDispenser(number, tok.substring(10));
			}else if(tok.startsWith("def")) {
				updateDefense(number, tok.substring(4));
			}else if(tok.startsWith("off")) {
				updateOffense(number, tok.substring(4));
			}else if(tok.startsWith("defense")) {
				updateDefense(number, tok.substring(8));
			}else if(tok.startsWith("offense")) {
				updateOffense(number, tok.substring(8));
			}else {
				unknown.add(tok);
			}
		}
		
		if(unknown.size() > 0) {
			String total = "";
			for(String s : unknown) {
				total += "\"" + s + "\", ";
			}
			
			total = total.substring(0, total.length()-2);
			Main.getVoice().sendSMS(sender, "Unknown Parameters: " + total + ". Your other variables were still applied.");
		}
	}
	
	public static String gatherComments(Integer number) {
		DatabaseQuery query = null;
		String comments = "";
		try {
			query = new DatabaseQuery("SELECT numbers.name, comments.comment FROM comments LEFT JOIN numbers ON numbers.number = comments.sender WHERE comments.t_number = ?");
			query.getStatement().setInt(1, number);
			ResultSet set = query.runQuery();
			if(set.first()) {
				do {
					comments += (set.getString(1) + ": " + set.getString(2)) + "\n";
				}while(set.next());
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			if(query != null) query.close();
		}
		
		return comments;
	}
	
	private static void updateMountain(Integer number, String update) {
		DatabaseQuery query = null;
		try {
			query = new DatabaseQuery("UPDATE teams SET mountain = ? WHERE t_number = ?");
			query.getStatement().setString(1, update);
			query.getStatement().setInt(2, number);
			query.runStatement();
		}catch(Exception ex) {
			System.out.println("Error: " + number + " ---- " + update);
			ex.printStackTrace();
		}finally {
			if(query != null) query.close();
		}
	}
	
	private static void updateDispenser(Integer number, String update) {
		DatabaseQuery query = null;
		try {
			query = new DatabaseQuery("UPDATE teams SET dispenser = ? WHERE t_number = ?");
			query.getStatement().setString(1, update);
			query.getStatement().setInt(2, number);
			query.runStatement();
		}catch(Exception ex) {
			System.out.println("Error: " + number + " ---- " + update);
			ex.printStackTrace();
		}finally {
			if(query != null) query.close();
		}
	}
	
	private static void updateDefense(Integer number, String update) {
		DatabaseQuery query = null;
		try {
			query = new DatabaseQuery("UPDATE teams SET defense = ? WHERE t_number = ?");
			query.getStatement().setInt(1, Integer.parseInt(update));
			query.getStatement().setInt(2, number);
			query.runStatement();
		}catch(Exception ex) {
			System.out.println("Error: " + number + " ---- " + update);
			ex.printStackTrace();
		}finally {
			if(query != null) query.close();
		}
	}
	
	private static void updateOffense(Integer number, String update) {
		DatabaseQuery query = null;
		try {
			query = new DatabaseQuery("UPDATE teams SET offense = ? WHERE t_number = ?");
			query.getStatement().setInt(1, Integer.parseInt(update));
			query.getStatement().setInt(2, number);
			query.runStatement();
		}catch(Exception ex) {
			System.out.println("Error: " + number + " ---- " + update);
			ex.printStackTrace();
		}finally {
			if(query != null) query.close();
		}
	}
	
	public static void giveInformation(String sender, String number) throws IOException {
		Integer n = null;
		
		try {
			n = Integer.parseInt(number);
		}catch(Exception ex) {
			Main.getVoice().sendSMS(sender, "Invalid Team Number: " + number);
			return;
		}
		
		DatabaseQuery query = null;
		
		try {
			query = new DatabaseQuery("SELECT * FROM teams WHERE t_number = ?");
			query.getStatement().setInt(1, n);
			ResultSet set = query.runQuery();
			
			if(set.first()) {
				String ret = "About Team " + number + ": ";
				ret += "mt=" + set.getString("mountain") + " ";
				ret += "disp=" + set.getString("dispenser") + " ";
				int off = set.getInt("offense");
				int def = set.getInt("defense");
				ret += "def=" + (def >= 0? def : "Unknown") + " ";
				ret += "off=" + (off >= 0? off : "Unknown");
				Main.getVoice().sendSMS(sender, ret);
			}else {
				Main.getVoice().sendSMS(sender, "Couldn't Find Team " + number);
			}
		}catch(Exception ex) {
			System.out.println("Couldn't Retrieve Data for Team " + number);
			ex.printStackTrace();
		}finally {
			if(query != null) query.close();
		}
	}
}
