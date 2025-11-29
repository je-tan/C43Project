package cs.toronto.edu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.Scanner;

public class Friend {

	// Get all friends of user "username"
	public void getFriends(String username, Statement stmt) {
		try {
			String sql_get_friends = "(SELECT receiver_id AS friend_id FROM friend WHERE sender_id = '" + username + "' AND status = 'accepted') UNION (SELECT sender_id AS friend_id FROM friend WHERE receiver_id = '" + username + "' AND status = 'accepted')";
			ResultSet rs = stmt.executeQuery(sql_get_friends);
			System.out.println("Friends: ");
			while(rs.next()) {
				System.out.println(rs.getString("friend_id"));
			}

		} catch(SQLException e) {
			System.out.println("Failed to execute get friends query" + e.getMessage());
		}
	}

	public void sendFriendRequest(String sender, String receiver, Statement stmt) {
		try {
			//First query to see if receiver is valid and if sender and receiver are friends already
			String sql_get_receiver = "SELECT * FROM users WHERE username = '" + receiver + "';";
			ResultSet rs = stmt.executeQuery(sql_get_receiver);
			if(!rs.next()) {
				System.out.println(receiver + " is not a valid user");
				return;
			}
			String sql_are_friends = "SELECT * FROM friend WHERE (sender_id = '" + sender + "' AND receiver_id = '" + receiver + "' AND status = 'accepted') OR (sender_id = '" + receiver + "' AND receiver_id = '" + sender + "' AND status = 'accepted');"; 
			rs = stmt.executeQuery(sql_are_friends);
			if(rs.next()) {
				System.out.println("Already friends");
				return;
			}
			//Second query to see if sender has already sent a request and its pending
			String sql_already_sent_request = "SELECT * FROM friend WHERE sender_id = '" + sender + "' AND receiver_id = '" + receiver + "' AND status = 'pending';";
			rs = stmt.executeQuery(sql_already_sent_request);
			if(rs.next()) {
				System.out.println("Request already sent. Currently pending");
				return;
			}
			//Third query to see if receiver has rejected the request and it has been more than 5 minutes after rejection
			String sql_rejected_request = "SELECT * FROM friend WHERE sender_id = '" + sender + "' AND receiver_id = '" + receiver + "' AND status = 'rejected';";
			rs = stmt.executeQuery(sql_rejected_request);
			Timestamp oldTimeStamp;
			Timestamp currentTimeStamp = new Timestamp(System.currentTimeMillis());
			if(rs.next()) {	
				oldTimeStamp = rs.getTimestamp("time_last_request");
				long timeDifference = currentTimeStamp.getTime() - oldTimeStamp.getTime();
				if(timeDifference <= 5 * 60 * 1000) {
					System.out.println("It has not been 5 minutes since request was rejected. Please try again later");
					return;
				}
				//If here then it has been more than 5 minutes since request was rejected, so can send another one
				String sql_update_rejected_request = "UPDATE friend SET status = 'pending', time_last_request = '" + currentTimeStamp + "' WHERE sender_id = '" + sender + "' AND receiver_id = '" + receiver + "';";
				stmt.executeUpdate(sql_update_rejected_request);
				System.out.println("sent friend request");
				return;
			}
			String sql_send_friend_request = "INSERT INTO friend (sender_id, receiver_id, status, time_last_request) VALUES ('" + sender + "', '" + receiver + "', 'pending', '" + currentTimeStamp + "');";

			stmt.executeUpdate(sql_send_friend_request);
			System.out.println("sent friend request");
		} catch(SQLException e) {
			System.out.println("Failed to execute add user query" + e.getMessage());
		}
	}
	// Get all incoming friend requests for user "username"
	public void getIncomingFriendRequests(String username, Statement stmt) {
		try {
			String sql_get_requests = "SELECT sender_id FROM friend WHERE receiver_id = '" + username + "' AND status = 'pending';";
			ResultSet rs = stmt.executeQuery(sql_get_requests);
			while(rs.next()) {
				System.out.println(rs.getString("sender_id"));
			}
		} catch(SQLException e) {
			System.out.println("Failed to execute get incoming friend request query");
		}
	}
	// Returns true if friend request exists, false otherwise
	public boolean existsFriendRequest(String username, String friend_request, Statement stmt) {
		try {
			String sql_get_request = "SELECT * FROM friend where receiver_id = '" + username + "' AND sender_id = '" + friend_request + "' AND status = 'pending';";
			ResultSet rs = stmt.executeQuery(sql_get_request);
			if(rs.next()) return true;
			return false;	
		} catch(SQLException e) {
			System.out.println("Failed to execute get friend request query");
			return false;
		}
	}
	// Accept friend request
	public void acceptFriendRequest(String username, String friend_request, Statement stmt) {
		try {
			String sql_accept_request = "UPDATE friend SET status = 'accepted' WHERE receiver_id = '" + username + "' AND sender_id = '" + friend_request + "';";
			stmt.executeUpdate(sql_accept_request);
			System.out.println("Friend request accepted");
			// If other user also sent a friend request, want to delete it as they are now friends
			String sql_delete_request = "DELETE FROM friend WHERE sender_id = '" + username + "' AND receiver_id = '" + friend_request + "';";
			stmt.executeUpdate(sql_delete_request);
		} catch(SQLException e) {
			System.out.println("Failed to execute accept friend request update query");
		}
	}

	// Reject friend request
	public void rejectFriendRequest(String username, String friend_request, Statement stmt) {
		try {
			String sql_reject_request = "UPDATE friend SET status = 'rejected' WHERE receiver_id = '" + username + "' AND sender_id = '" + friend_request + "';";
			stmt.executeUpdate(sql_reject_request);
			System.out.println("Friend request rejected");
		} catch(SQLException e) {
			System.out.println("Failed to execute reject friend request update query");
		}
	}
	// Remove friend
	public void removeFriend(String username, String friend, Statement stmt) {
		try {
			// First check that they are friends
			String sql_are_friends = "(SELECT sender_id AS friend_id FROM friend WHERE sender_id = '" + username + "' AND receiver_id = '" + friend + "') UNION (SELECT receiver_id AS friend_id FROM friend WHERE receiver_id = '" + username + "' AND sender_id = '" + friend + "');";  
			ResultSet rs = stmt.executeQuery(sql_are_friends);
			if(!rs.next()) {
				System.out.println("No such friend exists");
				return;
			}
			String sql_remove_friend = "DELETE FROM friend WHERE sender_id = '" + username + "' AND receiver_id = '" + friend + "';";
		       	stmt.executeUpdate(sql_remove_friend);
			sql_remove_friend = "DELETE FROM friend WHERE receiver_id = '" + username + "' AND sender_id = '" + friend + "';";
			stmt.executeUpdate(sql_remove_friend);	

		} catch(SQLException e) {
			System.out.println("failed to delete friend");
		}
	}

}	
