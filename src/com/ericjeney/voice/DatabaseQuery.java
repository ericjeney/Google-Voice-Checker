package com.ericjeney.voice;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseQuery {
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private Connection conn;
	private PreparedStatement statement;
	private ResultSet set = null;
	
	public DatabaseQuery(String state) throws SQLException {
		conn = DriverManager.getConnection("jdbc:mysql://localhost/saywatt?user=root&password=SayWatt");
		statement = conn.prepareStatement(state);
	}
	
	public PreparedStatement getStatement() {
		return statement;
	}
	
	public ResultSet runQuery() throws SQLException {
		set = statement.executeQuery();
		return set;
	}
	
	public boolean runStatement() throws SQLException {
		return statement.execute();
	}
	
	public void close() {
		if(set != null) {
			try { set.close(); }catch(Exception ex) {}
		}
		
		try{ statement.close(); }catch(Exception ex) {}
		try{ conn.close(); }catch(Exception ex) {}
	}
	
}
