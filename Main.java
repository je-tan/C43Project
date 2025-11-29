package cs.toronto.edu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {

		Connection conn = null;
		Statement stmt = null;
		Statement stmt2 = null;

		try {
			// 
			// Register the PostgreSQL driver
			//
			//
			Class.forName("org.postgresql.Driver");

			//
			// Connect to the database
			//
			//
			conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/c43db", "postgres", "postgres");
			System.out.println("Opened database successfully");

			//
			// Create a statement object
			//
			//
			stmt = conn.createStatement();
			stmt2 = conn.createStatement();

			//Going to set up command line interface for user here
			Scanner scanner = new Scanner(System.in);
			String user_input = "";
			boolean signed_in = false;
			//user authentication loop
			
			User user = new User();
			Portfolio portfolio = new Portfolio();
			Stocklist stocklist = new Stocklist();
			Friend friend = new Friend();
			Review review = new Review();
			Stock stock = new Stock();
			while(!user_input.equals("exit")) {
				if (!signed_in) {
					//User authentication, 1: signin, 2: register
					System.out.println("Enter 1 to signin, 2 to register");
					user_input = scanner.next();
					if(user_input.equals("1")) {
						if(user.getUser(stmt) == 0) signed_in = true;	
					} else if(user_input.equals("2")) {
						System.out.println("enter username ");
						String username = scanner.next();
						System.out.println("enter password ");
						String password = scanner.next();
						user.addUser(username, password, stmt);
					}
				}
				else{
					System.out.println("1. portfolio, 2. stock list, 3. friend requests, 4. Add stock entry, 5. View stock history, 6. predict stock, 'logout': logout");
					user_input = scanner.next();
					switch(user_input) {
						case "1":
							System.out.println("========Portfolio=======");
							portfolio.getPortfolios(user.getUsername(), stmt);
							System.out.println("1. Create new portfolio, 2. View portfolio, 3. Delete portfolio, 'logout': logout");
							user_input = scanner.next();
							switch(user_input) {
								case "1":
									System.out.println("Enter portfolio name: ");
									String portfolio_name = scanner.next();
									portfolio.addPortfolio(user.getUsername(), portfolio_name, 0, stmt);
								break;
								case "2":
									System.out.println("Enter portfolio name");
									String pname = scanner.next();
									portfolio.getPortfolio(user.getUsername(), pname, stmt, stmt2);
									System.out.println("1. add new stock holding, 2. withdraw cash, 3. deposit cash, 4. buy shares, 5. Sell shares 6. View recent portfolio history");
									user_input = scanner.next();
									double amount;
									switch(user_input) {
										case "1":
											System.out.println("Enter symbol");
											String symbol = scanner.next();
											System.out.println("Enter holding amount");
											int holding_amount = scanner.nextInt();
											portfolio.addPortfolioStockHolding(user.getUsername(), pname, symbol, holding_amount, stmt);
											break;
										case "2":
											System.out.println("How much would you like to withdraw?");
											amount = scanner.nextDouble();
											portfolio.withdraw(user.getUsername(), pname, amount, stmt);
											break;
										case "3":
											System.out.println("How much would you like to deposit?");
											amount = scanner.nextDouble();
											portfolio.deposit(user.getUsername(), pname, amount, stmt);
											break;
										case "4":
											System.out.println("Enter symbol of stock you would like to buy");
											String s = scanner.next();
											System.out.println("Enter how many shares you would like to buy");
											double shares = scanner.nextDouble();
											portfolio.buyStock(user.getUsername(), pname, s, shares, stmt);	
											break;
										case "5":
											System.out.println("Enter symbol of stock you would like to sell");
											String s2 = scanner.next();
											System.out.println("Enter how many shares you would like to sell");
											double shares2 = scanner.nextDouble();
											portfolio.sellStock(user.getUsername(), pname, s2, shares2, stmt);
											break;
										case "6":
											System.out.println(pname + " history:");
											portfolio.displayPortfolioHistory(user.getUsername(), pname, stmt);
											break;

									}
								break;
								case "3":
									System.out.println("Enter portfolio name");
									user_input = scanner.next();
									portfolio.deletePortfolio(user.getUsername(), user_input, stmt);
								break;
								case "logout":
									signed_in = false;
									user.setUsername("");
								break;
								default:
									System.out.println("Please enter a valid input");
							}
						break;
						case "2":
							System.out.println("========Stock List=======");
							stocklist.getStocklists(user.getUsername(), stmt);
							System.out.println("1. Create new stocklist, 2. View stocklist, 3. Delete stocklist, 'logout': logout");
							user_input = scanner.next();
							switch(user_input) {
								case "1":
									System.out.println("1. public, 2. private");
									user_input = scanner.next();
									boolean isPublic = true;
									if (user_input.equals("2")) {
										isPublic = false;
									}
									System.out.println("Enter a stocklist name:");
									user_input = scanner.next();
									stocklist.addStocklist(user.getUsername(), user_input, isPublic, stmt);
									break;
								case "2":
									System.out.println("Enter a stocklist name:");
									String stocklist_name = scanner.next();
									stocklist.getStocklist(user.getUsername(), stocklist_name, stmt, stmt2);
									System.out.println("1. share, 2. add review, 3. delete review, 4. view all reviews , 5. add stock holding, 6. remove stock holding, 7. quit");
									user_input = scanner.next();
									if (user_input.equals("1")) {
										System.out.println("Enter username to be shared with");
										user_input = scanner.next();
										stocklist.shareStocklist(user_input, stocklist_name, stmt);
									} else if(user_input.equals("2")) {
										System.out.println("stocklist owned by:");
										String owner = scanner.next();
										System.out.println("Review content:");
										scanner.nextLine();
										user_input = scanner.nextLine();
										review.addReview(user.getUsername(), stocklist_name, owner, user_input, stmt);
									} else if(user_input.equals("3")) {
										System.out.println("stocklist owned by:");
										String owner = scanner.next();
										System.out.println("Review written by:");
										user_input = scanner.next();
										review.deleteReview(user_input, stocklist_name, owner, stmt);
									} else if(user_input.equals("4")) {
										System.out.println("stocklist owned by:");
										String owner = scanner.next();
										review.getAllReviewsForStocklist(stocklist_name, owner, stmt);
									} else if(user_input.equals("5")) {
										System.out.println("Enter symbol");
                                                                                String symbol = scanner.next();
                                                                                System.out.println("Enter holding amount");
                                                                                int holding_amount = scanner.nextInt();
                                                                                stocklist.addStocklistStockHolding(user.getUsername(), stocklist_name, symbol, holding_amount, stmt);
									} else if(user_input.equals("6")) {
										System.out.println("Enter symbol");
										String symbol = scanner.next();
										stocklist.removeStocklistStockHolding(user.getUsername(), stocklist_name, symbol, stmt);
									}

									break;
								case "3":
									System.out.println("Enter a stocklist name:");
									user_input = scanner.next();
									stocklist.deleteStocklist(user.getUsername(), user_input, stmt);
									break;
								case "logout":
									signed_in = false;
									user.setUsername("");
								break;
								default:
							}
						break;
						case "3":
							System.out.println("========Friend Request========");
							System.out.println("1. Add friend, 2. View friends, 3. View friend requests, 4. Remove friend");
							user_input = scanner.next();
							switch(user_input) {
								case "1":
									System.out.println("Enter username");
									user_input = scanner.next();
									friend.sendFriendRequest(user.getUsername(), user_input, stmt);
								break;
								case "2":
									friend.getFriends(user.getUsername(), stmt);
								break;
								case "3":
									System.out.println("Friend Requests: ");
									friend.getIncomingFriendRequests(user.getUsername(), stmt);
									System.out.println("Select friend request by entering username:");
									String friend_request = scanner.next();
									if(!friend.existsFriendRequest(user.getUsername(), friend_request, stmt)) {
										System.out.println("friend request does not exist");
										break;
									}
									System.out.println("1. Accept, 2. Reject");
									user_input = scanner.next();
									switch(user_input) {
										case "1":
											//Accept friend request
											friend.acceptFriendRequest(user.getUsername(), friend_request, stmt);
										break;
										case "2":
											//Reject friend request
											friend.rejectFriendRequest(user.getUsername(), friend_request, stmt);
										break;
										default:
											System.out.println("Please enter a valid input");
									}
								break;
								case "4":
									System.out.println("Enter friend to remove");
									user_input = scanner.next();
									friend.removeFriend(user.getUsername(), user_input, stmt);
								default:
							}
						break;
						case "4":
							System.out.println("Enter Symbol");
							String s = scanner.next();
							System.out.println("Enter date (yyyy-mm-dd)");
							String d = scanner.next();
							System.out.println("Enter open");
							double o = scanner.nextDouble();
							System.out.println("Enter high");
							double h = scanner.nextDouble();
							System.out.println("Enter low");
							double l = scanner.nextDouble();
							System.out.println("Enter close");
							double c = scanner.nextDouble();
							System.out.println("Enter volume");
							int v = scanner.nextInt();
							stock.addStockEntry(java.sql.Date.valueOf(d), o, h, l, c, v, s, stmt);
						break;
						case "5":
							System.out.println("Enter Symbol");
							user_input = scanner.next();
							stock.viewStockHistory(user_input, stmt);

						break;
						case "6":
							System.out.println("Enter symbol");
							user_input = scanner.next();
                                                        stock.predictStock(user_input, stmt);
						break;
						case "logout":
							System.out.println("logging out..");
							signed_in = false;
							user.setUsername("");	
						break;
						default:
							System.out.println("Please enter a valid input");
					}

				}


			}


		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(1);
		} finally {
			try {
				if (stmt != null) stmt.close();
				if (conn != null) conn.close();
			
				System.out.println("Disconnected from the database");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}


	}
}
