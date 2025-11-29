package cs.toronto.edu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Scanner;
import java.sql.Date;
import java.time.LocalDateTime;

public class Portfolio {
        public Portfolio() {
        }

	//should add to portfolio, ownership
        public void addPortfolio(String username, String portfolio_name, int cash_amount, Statement stmt) {
		try {
                String sql_add_portfolio = "INSERT INTO portfolio (portfolio_name, cash_amount, owner) VALUES ('" + portfolio_name + "', " + cash_amount + ", '" + username + "') returning portfolio_id;";
                ResultSet rs = stmt.executeQuery(sql_add_portfolio);
		int portfolio_id = 0;
		if (rs.next()) {
    		portfolio_id = rs.getInt("portfolio_id");
    		System.out.println("Portfolio " + portfolio_name + " added.");
	}

                System.out.println("Portfolio " + portfolio_name + " added to portfolio table");
		}
		catch(SQLException e){
			System.err.println("Failed to execute query: "+e.getMessage());
		}
        }


        public void getPortfolios(String username, Statement stmt) {
                try {
                        String sql_get_username = "SELECT * FROM portfolio WHERE owner = '" + username + "';";
                        ResultSet rs = stmt.executeQuery(sql_get_username);
			System.out.println("Printing all portfolios belonging to user: "+username);
			while (rs.next()) {
				String portfolio = rs.getString("portfolio_name");
				int cash = rs.getInt("cash_amount");
				System.out.println("Portfolio name: " + portfolio + "| cash amount: " + cash);
			}
                        return;
                } catch(SQLException e) {
                        System.err.println("Failed to execute get portfolios query" + e.getMessage());
                        return;
                }
        }

        public void getPortfolio(String username, String portfolio_name, Statement stmt, Statement stmt2) {

                try {
			getBalance(username, portfolio_name, stmt);
                        String sql_get_portfolio = "SELECT * FROM portfolio_stock_holding WHERE portfolio_id = (SELECT portfolio_id FROM portfolio where owner= '" + username + "' AND portfolio_id IN (SELECT portfolio_id FROM portfolio WHERE portfolio_name = '" + portfolio_name + "'))";
                        ResultSet rs = stmt.executeQuery(sql_get_portfolio);
			double total_value = 0;
			int holding_amount = 0;
			double s = 0;
			double holding_value = 0;
			double total_predicted_value = 0;
                        while (rs.next()) {
				String symbol = rs.getString("symbol");
				holding_amount = rs.getInt("holding_amount");
				String getHoldingValue = "SELECT close FROM stock WHERE symbol = '" + symbol + "' ORDER BY timestamp DESC LIMIT 1;";
				ResultSet stock_value = stmt2.executeQuery(getHoldingValue);
				if (!stock_value.next()) {
					continue;
				}
				s = stock_value.getDouble("close");
				holding_value = s * holding_amount;
				total_value += holding_value;
				total_predicted_value += predictStock(symbol, stmt2) * holding_amount;	
				System.out.println(symbol + "| holding amount: " + holding_amount + "| stock value: " + s + "| holding value: " + holding_value);
			}
			String sql_get_cash_amount = "SELECT cash_amount FROM portfolio WHERE portfolio_name = '" + portfolio_name + "' AND owner = '" + username + "';";
			rs = stmt.executeQuery(sql_get_cash_amount);
			rs.next();
			double cash = rs.getDouble("cash_amount");
			System.out.println("Total stock value: " + total_value + " Cash amount: " + cash + " Total: " + (cash + total_value));
			System.out.println("Predicted stock value in 1 year: " + total_predicted_value);
			return;
                } catch(SQLException e) {
                        System.err.println("Failed to execute get portfolio query" + e.getMessage());
                        return;
                }
        }

	public void deletePortfolio(String username, String portfolio_name, Statement stmt) {
		try {
			String sql_delete_portfolio = "DELETE FROM portfolio WHERE portfolio_name = '" + portfolio_name + "' AND owner = '" + username + "';";
			stmt.executeUpdate(sql_delete_portfolio);
			System.out.println("Deleted portfolio " + portfolio_name);
		} catch (SQLException e) {
			System.err.println("Failed to execute delete portfolio query");
		}
		return;
	}

	public void addPortfolioStockHolding(String username, String portfolio_name, String symbol, int holding_amount, Statement stmt) {
		try {
			double balance = getBalance(username, portfolio_name, stmt);
			//get price of stock
			String getHoldingValue = "SELECT close FROM stock WHERE symbol = '" + symbol + "' ORDER BY timestamp DESC LIMIT 1;";
			ResultSet stock_value = stmt.executeQuery(getHoldingValue);
			if (!stock_value.next()) {
				System.out.println("something went wrong :(");
				return;
			}
			double value = stock_value.getDouble("close");
			if (balance - ( value * holding_amount ) < 0 ) {
				System.out.println("Wanted to purchase " + holding_amount + " shares of " + symbol + " worth " + (value * holding_amount) + " but only have " + balance + "cash. Failed to add stock holdings");
				return;
			}
			String sql_get_portfolio_id = "SELECT portfolio_id FROM portfolio WHERE portfolio_name = '" + portfolio_name + "' AND owner = '" + username + "';";
                        ResultSet rs = stmt.executeQuery(sql_get_portfolio_id);
                        rs.next();
                        String pid = rs.getString("portfolio_id");
                        String sql_add_holding = "INSERT INTO portfolio_stock_holding (portfolio_id, symbol, holding_amount) VALUES ('" + pid + "', '" + symbol + "', 0);";
                        stmt.executeUpdate(sql_add_holding);
                        System.out.println("Adding to portfolio " + portfolio_name +" " +  holding_amount + " shares of " + symbol);
			buyStock(username, portfolio_name, symbol, holding_amount, stmt);
		}
		catch (SQLException e) {
			System.err.println("Failed to execute add portfolio stock holding query" + e.getMessage());
		}
		return;
	}

	private double getBalance(String username, String portfolio_name, Statement stmt) {
		try {
			String sql_get_balance = "SELECT cash_amount FROM portfolio WHERE owner = '" + username + "' AND portfolio_name = '" + portfolio_name + "';";
			ResultSet rs = stmt.executeQuery(sql_get_balance);
			rs.next();
			double balance = rs.getDouble("cash_amount");
			System.out.println("Balance: " + balance);	
			return balance;
		}catch (SQLException e) {
			System.err.println("Failed to execute get balance query" + e.getMessage());
			return -1;
		}
	}

	private double getBalanceNoPrint(String username, String portfolio_name, Statement stmt) {
		try {
			String sql_get_balance = "SELECT cash_amount FROM portfolio WHERE owner = '" + username + "' AND portfolio_name = '" + portfolio_name + "';";
			ResultSet rs = stmt.executeQuery(sql_get_balance);
			rs.next();
			double balance = rs.getDouble("cash_amount");
			return balance;
		}catch (SQLException e) {
			System.err.println("Failed to execute get balance query" + e.getMessage());
			return -1;
		}
	}

	// Withdraw from cash account
	public void withdraw(String username, String portfolio_name, double amount, Statement stmt) {
		try {
			double balance = getBalanceNoPrint(username, portfolio_name, stmt);
			if (balance == -1) {
				System.out.println("failed to get balance. cannot withdraw");
				return;
			}
			if ( balance < amount ) {
				System.out.println("Not enough money, cannot withdraw");
			}
			else{
				double new_balance = balance - amount;
				String sql_withdraw = "UPDATE portfolio SET cash_amount = " + new_balance + " WHERE owner = '" + username + "' AND portfolio_name = '" + portfolio_name + "';";
				stmt.executeUpdate(sql_withdraw);
				System.out.println("Withdrew $" + amount + " from " + portfolio_name + " new balance: " + new_balance);
				// add to transaction history
				String transaction_info = "Withdrew $ " + amount + " New balance: " + new_balance;
				addPortfolioHistory(username, portfolio_name, transaction_info, stmt);
			}
		} catch (SQLException e) {
			System.err.println("Failed to withdraw " + e.getMessage());
		}
	}

	// Deposit to cash account
	public void deposit(String username, String portfolio_name, double amount, Statement stmt) {
		try {
			double balance = getBalanceNoPrint(username, portfolio_name, stmt);
			double new_balance = balance + amount;
			String sql_deposit = "UPDATE portfolio SET cash_amount = " + new_balance + " WHERE owner = '" + username + "' AND portfolio_name = '" + portfolio_name + "';";
			stmt.executeUpdate(sql_deposit);
			System.out.println("Deposited $" + amount + " to " + portfolio_name + " new balance: " + new_balance);
			// add to transaction history
                        String transaction_info = "Deposited $ " + amount + " New balance: " + new_balance;
                        addPortfolioHistory(username, portfolio_name, transaction_info, stmt);
		}
		catch (SQLException e) {
			System.err.println("Failed to deposit " + e.getMessage());
		}
	}
	// Get the portfolio id
	public int getPortfolioId(String username, String portfolio_name, Statement stmt) {
		try {
		String sql_portfolio_id = "SELECT portfolio_id FROM portfolio WHERE owner = '" + username + "' AND portfolio_name = '" + portfolio_name + "';";
                ResultSet rs = stmt.executeQuery(sql_portfolio_id);
                int portfolio_id = -1;
                if(rs.next()) {
			portfolio_id = rs.getInt("portfolio_id");
                }
		return portfolio_id;}
		catch (SQLException e) {
			System.err.println("failed to get portfolio id " + e.getMessage());
		}
		return -1;
	}

	// This will be used to record stocks bought and sold (including changing the cash and stock holdings)
	public void addPortfolioHistory(String username, String portfolio_name, String event, Statement stmt) {
		try {
			int portfolio_id = getPortfolioId(username, portfolio_name, stmt);
			String sql_insert_event = "INSERT INTO portfolio_history (portfolio_id, timestamp, event) VALUES (" + portfolio_id + ", '" + LocalDateTime.now() + "', '" + event + "');";
		        stmt.executeUpdate(sql_insert_event);
		} catch (SQLException e) {
			System.err.println("Failed to add portfolio history" + e.getMessage());
		}
		return;
	}	
	// This will print the 10 most recent transactions for the portfolio (deposit cash, withdraw cash, bought stock, sold stock, added new stock)
	public void displayPortfolioHistory(String username, String portfolio_name, Statement stmt) {
		try {
			int portfolio_id = getPortfolioId(username, portfolio_name, stmt);
			String sql_get_history = "SELECT event, timestamp FROM portfolio_history WHERE portfolio_id = " + portfolio_id + " ORDER BY timestamp DESC LIMIT 10;";
			ResultSet rs = stmt.executeQuery(sql_get_history);
			while(rs.next()) {
				System.out.println(rs.getString("event") + " " + rs.getString("timestamp"));
			}
		} catch (SQLException e) {
			System.err.println("Failed to display portfolio history" + e.getMessage());
		}
	}
	// Sell shares of a stock (money will be deposited to cash account)
	public void sellStock(String username, String portfolio_name, String symbol, double amount_to_sell, Statement stmt) {
		try {
			int portfolio_id = getPortfolioId(username, portfolio_name, stmt);
			// check that user has that amount of shares
			String sql_get_shares = "SELECT holding_amount FROM portfolio_stock_holding WHERE portfolio_id = " + portfolio_id + " AND symbol = '" + symbol + "';";
			ResultSet rs = stmt.executeQuery(sql_get_shares);
			double shares = -1;
			if(rs.next()) {
				shares = rs.getDouble("holding_amount");	
			}
			if(amount_to_sell > shares) {
				System.out.println("Not enough shares, only have " + shares + " shares of " + symbol);
				return;
			}
			//Update the holding amount for the stock, get the latest closing price for stock to calculate the cash amount, deposit cash amount to cash account
			String sql_update_holding_amount = "UPDATE portfolio_stock_holding SET holding_amount = " + (shares-amount_to_sell) + " WHERE portfolio_id = " + portfolio_id + " AND symbol = '" + symbol + "';";
			stmt.executeUpdate(sql_update_holding_amount);
			String sql_get_stock_price = "SELECT close FROM stock WHERE symbol = '" + symbol + "' ORDER BY timestamp DESC LIMIT 1";
			ResultSet rs2 = stmt.executeQuery(sql_get_stock_price);
			if (!rs2.next()) {
				return;
			}
			double stock_price = rs2.getDouble("close");
			double cash_amount = stock_price * amount_to_sell;
			deposit(username, portfolio_name, cash_amount, stmt);
			//add transaction to portfolio history
			String transaction_info = "Sold " + amount_to_sell + " shares of " + symbol + " for a total of $ " + cash_amount;
			addPortfolioHistory(username, portfolio_name, transaction_info, stmt); 
		} catch (SQLException e) {
			System.err.println("Failed to sell stock" + e.getMessage());
		}
	}
	// Buy shares of a stock (money will be taken from cash account)
	public void buyStock(String username, String portfolio_name, String symbol, double shares_to_buy, Statement stmt) {
		try {
			// Check that user has enough in cash account to buy shares_to_buy shares
			int portfolio_id = getPortfolioId(username, portfolio_name, stmt);
			double cash_amount = getBalanceNoPrint(username, portfolio_name, stmt);
			String sql_get_stock_price = "SELECT close FROM stock WHERE symbol = '" + symbol + "' ORDER BY timestamp DESC LIMIT 1";
                        ResultSet rs = stmt.executeQuery(sql_get_stock_price);
			if (!rs.next()) {
				return;
			}
			double stock_price = rs.getDouble("close");
			double total_amount = shares_to_buy * stock_price;
			if(total_amount > cash_amount) {
				System.out.println("Not enough cash in cash account to buy " + shares_to_buy + " shares of " + symbol);
				return;
			}
			// Increase the portfolio's stock holding of this stock by shares_to_buy and update cash_amount in portfolio
			String sql_get_shares = "SELECT holding_amount FROM portfolio_stock_holding WHERE portfolio_id = " + portfolio_id + " AND symbol = '" + symbol + "';";
			ResultSet rs2 = stmt.executeQuery(sql_get_shares);
                        double shares = -1;
                        if(rs2.next()) {
                                shares = rs2.getDouble("holding_amount");
                        }
			String sql_update_holding_amount = "UPDATE portfolio_stock_holding SET holding_amount = " + (shares+shares_to_buy) + " WHERE portfolio_id = " + portfolio_id + " AND symbol = '" + symbol + "';";
			stmt.executeUpdate(sql_update_holding_amount);
			withdraw(username, portfolio_name, total_amount, stmt);
			//add transaction to portfolio history
                        String transaction_info = "Bought " + shares_to_buy + " shares of " + symbol + " for a total of $ " + total_amount;
                        addPortfolioHistory(username, portfolio_name, transaction_info, stmt);
		} catch (SQLException e) {
			System.err.println("Failed to sell stock" + e.getMessage());
		}

	}

	 public double predictStock(String symbol, Statement stmt) {
                try {
                        String sql_old_price = "SELECT close FROM stock WHERE symbol = '" + symbol + "' AND timestamp >=  (Select MAX(timestamp) -  INTERVAL '1 year' FROM stock WHERE symbol = '" + symbol + "') ORDER BY timestamp ASC LIMIT 1;";
                        ResultSet rs = stmt.executeQuery(sql_old_price);
                        if (!rs.next()) {
                                System.out.println("something went wrong");
                                return -1;
                        }
                        double old = rs.getDouble("close");

                        String sql_curr_price = "SELECT close FROM stock WHERE symbol = '" + symbol + "' ORDER BY timestamp DESC LIMIT 10;";
                        rs = stmt.executeQuery(sql_curr_price);
                        if (!rs.next()) {
                                System.out.println("something went wrong");
                                return -1;
                        }
                        double nw = rs.getDouble("close");
                        return (((nw - old) / old) + 1) * nw;


                }
                catch (SQLException e) {
                        System.err.println("Failed to predict stock price " + e.getMessage());
                }
                return -1;
        }

	
}
