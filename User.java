package cs.toronto.edu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Scanner;

public class User {
	private String username;

	public User() {
		username = "";
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return this.username;
	}

	// Returns -1 if invalid username or password, 0 if found user 
	public int getUser(Statement stmt) {
		System.out.println("Enter Username: ");
		Scanner scan = new Scanner(System.in);
		String inputUsername = scan.next();
		if(!isValidUser(inputUsername, stmt)) {
			System.out.println("Invalid username");
			return -1;
		}
		System.out.println("Enter password: ");
		String inputPassword = scan.next();
		if(!isValidPassword(inputUsername, inputPassword, stmt)) {
			System.out.println("Invalid password");
			return -1;
		}
		setUsername(inputUsername);
		return 0;
	}

	public void addUser(String username, String password, Statement stmt) {
		try {
		String sql_add_user = "INSERT INTO users (username, password) " + "VALUES ('" + username + "', '" + password + "');";
		stmt.executeUpdate(sql_add_user);
		System.out.println("User " + username + "successfully added");	
		setUsername(username);
		} catch(SQLException e) {
			System.out.println("Failed to execute add user query" + e.getMessage());
		}
	}


	public boolean isValidUser(String username, Statement stmt) {
		try {
			String sql_get_username = "SELECT * FROM users WHERE username = '" + username + "' ;";
			ResultSet rs = stmt.executeQuery(sql_get_username);
			return rs.next();
		} catch(SQLException e) {
			System.err.println("Failed to execute get username query" + e.getMessage());
			return false;
		}
	}

	public boolean isValidPassword(String username, String password, Statement stmt) {
		try {
			String sql_get_password = "SELECT password FROM users WHERE username = '" + username + "' AND password = '" + password + "';";
			ResultSet rs = stmt.executeQuery(sql_get_password);
			return rs.next();
		} catch(SQLException e) {
			System.err.println("Failed to execute get password query" + e.getMessage());
			return false;
		}
	}
	
}	
