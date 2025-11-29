package cs.toronto.edu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Scanner;

public class Review {

	//** Made the assumption that we can uniquely identify a stocklist through stocklistname and stocklistowner as a user should not have more than one stocklist with the same name

	public void addReview(String username, String stocklist_name, String stocklist_owner, String review_content, Statement stmt) {
	    try {
	        // First, get the stocklist_id from the stocklist_name AND owner
	        String sql_get_stocklist_id = "SELECT stocklist_id FROM stocklist " +
	                                  "WHERE stocklist_name = '" + stocklist_name + "' " +
	                                  "AND owner = '" + stocklist_owner + "'";
	        ResultSet rs = stmt.executeQuery(sql_get_stocklist_id);
	        
	        int stocklistId = -1;
	        if (rs.next()) {
	            stocklistId = rs.getInt("stocklist_id");
	        } else {
	            System.out.println("Stocklist '" + stocklist_name + "' by owner '" + stocklist_owner + "' not found");
	            return;
	        }
	        
	        // Now check if the user already has a review for this stocklist
	        String sql_check_review = "SELECT r.review_id FROM review r " +
	                               "JOIN review_by rb ON r.review_id = rb.review_id " +
	                               "JOIN review_of ro ON r.review_id = ro.review_id " +
	                               "WHERE rb.username = '" + username + "' AND ro.stocklist_id = " + stocklistId;
	        rs = stmt.executeQuery(sql_check_review);
	        
	        if (rs.next()) {
	            // Update existing review
	            int existingReviewId = rs.getInt("review_id");
	            String sql_update = "UPDATE review SET review_content = '" + review_content + 
	                              "' WHERE review_id = " + existingReviewId;
	            stmt.executeUpdate(sql_update);
	            System.out.println("Review updated for stocklist: " + stocklist_name + " by " + stocklist_owner);
	        } else {
	            // Insert into review table and get the generated review_id
	            String sql_insert_review = "INSERT INTO review (review_content) VALUES ('" + 
	                                   review_content + "') RETURNING review_id";
	            rs = stmt.executeQuery(sql_insert_review);
	            
	            int newReviewId = -1;
	            if (rs.next()) {
	                newReviewId = rs.getInt("review_id");
	            }
	            
	            // Insert into review_by table
	            String sql_insert_reviewby = "INSERT INTO review_by (review_id, username) VALUES (" + 
	                                     newReviewId + ", '" + username + "')";
	            stmt.executeUpdate(sql_insert_reviewby);
	            
	            // Insert into review_of table
	            String sql_insert_reviewof = "INSERT INTO review_of (review_id, stocklist_id) VALUES (" + 
	                                     newReviewId + ", " + stocklistId + ")";
	            stmt.executeUpdate(sql_insert_reviewof);
	            
	            System.out.println("New review added for stocklist: " + stocklist_name + " by " + stocklist_owner);
	        }
	    } catch(SQLException e) {
	        System.out.println("Failed to execute query");
	    }
	}	
	//
	// Delete a review
	public void deleteReview(String user, String stocklist_name, String stocklist_owner, Statement stmt) {
		try {
			
	        String sql_get_stocklist_id = "SELECT stocklist_id FROM stocklist " +
	                                  "WHERE stocklist_name = '" + stocklist_name + "' " +
	                                  "AND owner = '" + stocklist_owner + "'";
	        ResultSet rs = stmt.executeQuery(sql_get_stocklist_id);
	        
	        int stocklistId = -1;
	        if (rs.next()) {
	            stocklistId = rs.getInt("stocklist_id");
	        } else {
	            System.out.println("Stocklist '" + stocklist_name + "' by owner '" + stocklist_owner + "' not found");
	            return;
	        }

		String sql_get_review_id = "SELECT rb.review_id FROM review_by rb JOIN review_of ro ON rb.review_id = ro.review_id WHERE rb.username = '" + user + "' AND ro.stocklist_id = '" + stocklistId + "';";
		rs = stmt.executeQuery(sql_get_review_id);
		
		int reviewId = -1;
		if (rs.next()) {
			reviewId = rs.getInt("review_id");
		} else {
			System.out.println("Review does not exist");
			return;
		}
		String sql_delete_review = "DELETE FROM review WHERE review_id = " + reviewId + ";";
		stmt.executeUpdate(sql_delete_review);
		System.out.println("Review deleted");
		} catch(SQLException e) {
			System.out.println("Error deleting review");
		}
	}
	// Get all reviews for a public stocklist
	public void getAllReviewsForStocklist(String stocklist_name, String stocklist_owner, Statement stmt) {
	    try {
	        String sql_get_all_reviews = "SELECT r.review_id, r.review_content, rb.username, sl.isPublic " +
	                    "FROM review r " +
	                    "JOIN review_by rb ON r.review_id = rb.review_id " +
	                    "JOIN review_of ro ON r.review_id = ro.review_id " +
	                    "JOIN stocklist sl ON ro.stocklist_id = sl.stocklist_id " +
	                    "WHERE sl.stocklist_name = '" + stocklist_name + "' " +
	                    "AND sl.owner = '" + stocklist_owner + "'";
	        
	        ResultSet rs = stmt.executeQuery(sql_get_all_reviews);
	        
	        System.out.println("All reviews for '" + stocklist_name + "' by " + stocklist_owner + ":");
	        boolean foundReviews = false;
	        boolean isPublic = false;
	        
	        while (rs.next()) {
	            if (!foundReviews) {
	                isPublic = rs.getBoolean("isPublic");
	                foundReviews = true;
	            }
	            
	            int reviewId = rs.getInt("review_id");
	            String content = rs.getString("review_content");
	            String reviewer = rs.getString("username");
	            
	            System.out.println("Review ID: " + reviewId);
	            System.out.println("Reviewer: " + reviewer);
	            System.out.println("Content: " + content);
	            System.out.println("---");
	        }
	        
	        if (!foundReviews) {
	            System.out.println("No reviews found for this stocklist");
	        }
	        
	    } catch(SQLException e) {
	        System.out.println("Failed to get all reviews for stocklist");
	    }
	}
	//
	// Get all reviews for a private stocklist that user can view 
	//
}
