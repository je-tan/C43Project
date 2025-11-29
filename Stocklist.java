package cs.toronto.edu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Scanner;

public class Stocklist {
        public Stocklist() {
        }

        public void addStocklist(String username, String stocklist_name, boolean isPublic, Statement stmt) {
                try {
                String sql_add_stocklist = "INSERT INTO stocklist (stocklist_name, isPublic, owner) VALUES ('" + stocklist_name + "', " + isPublic +",'" + username + "') returning stocklist_id;";
                ResultSet rs = stmt.executeQuery(sql_add_stocklist);
                int stocklist_id = -1;
                if (rs.next()) {
                stocklist_id = rs.getInt("stocklist_id");
                System.out.println("Stocklist " + stocklist_name + " added.");
        }

                System.out.println("Stocklist " + stocklist_name + " added to stocklist table");
                }
                catch(SQLException e){
                        System.err.println("Failed to execute query: "+e.getMessage());
                }
        }


        public void getStocklists(String username, Statement stmt) {
                try {
                        String sql_get_stocklists = "SELECT stocklist_name FROM stocklist WHERE owner = '" + username + "';";
                        ResultSet rs = stmt.executeQuery(sql_get_stocklists);
                        System.out.println("Printing all stocklists belonging to user: "+username);
                        while (rs.next()) {
                                String stocklist = rs.getString("stocklist_name");
                                System.out.println(stocklist);
                        }
			sql_get_stocklists = "SELECT stocklist_name FROM shared_with JOIN stocklist ON stocklist.stocklist_id = shared_with.stocklist_id WHERE username = '" + username + "';";
                        rs = stmt.executeQuery(sql_get_stocklists);
                        System.out.println("\nPrinting all stocklists shared with user: "+username);
                        while (rs.next()) {
                                String stocklist = rs.getString("stocklist_name");
                                System.out.println(stocklist);
                        }
			sql_get_stocklists = "SELECT stocklist_name, owner FROM stocklist WHERE NOT owner = '" + username + "' AND isPublic = TRUE;";
                        System.out.println("\nPrinting all public stocklists");
                        rs = stmt.executeQuery(sql_get_stocklists);
                        while (rs.next()) {
                                String stocklist = rs.getString("stocklist_name");
				String owner = rs.getString("owner");
                                System.out.println(stocklist + " owned by " + owner);
                        }
			System.out.println();
                        return;
                } catch(SQLException e) {
                        System.err.println("Failed to execute get username query" + e.getMessage());
                        return;
                }
        }

        public void getStocklist(String username, String stocklist_name, Statement stmt, Statement stmt2) {

                try {
			String sql_get_stocklist = "SELECT * FROM stocklist_stock_holding WHERE stocklist_id = (SELECT stocklist_id FROM stocklist where owner= '" + username + "' AND stocklist_id IN (SELECT stocklist_id FROM stocklist WHERE stocklist_name = '" + stocklist_name + "'))";
		       	ResultSet rs = stmt.executeQuery(sql_get_stocklist);
			double total_value = 0;
                        while (rs.next()) {
                                String symbol = rs.getString("symbol");
                                int holding_amount = rs.getInt("holding_amount");

				String getHoldingValue = "SELECT close FROM stock WHERE symbol = '" + symbol + "' ORDER BY timestamp DESC LIMIT 1;";
                                ResultSet stock_value = stmt2.executeQuery(getHoldingValue);
                                if (!stock_value.next()) {
                                        continue;
                                }

				double s = stock_value.getDouble("close");
				double holding_value = s * holding_amount;
				total_value += holding_value;
			//	String sw_username = rs.getString("username");
                                System.out.println(symbol + "|" + holding_amount+ " | share value: " + s + "holding value: " + holding_value + "shared with: ");
                        }
			System.out.println("Total value of stocklist: " + total_value);
                        return;
                } catch(SQLException e) {
                        System.err.println("Failed to execute get password query" + e.getMessage());
                        return;
                }
        }

	public void deleteStocklist(String username, String stocklist_name, Statement stmt) {
		try {
			String sql_delete_stocklist = "DELETE FROM shared_with WHERE stocklist_id = (SELECT stocklist_id FROM stocklist WHERE owner = '" + username + "' AND stocklist_name = '" + stocklist_name + "');";
			stmt.executeUpdate(sql_delete_stocklist);
			sql_delete_stocklist = "DELETE FROM stocklist WHERE stocklist_name='" + stocklist_name + "' AND owner = '" + username + "';";
			stmt.executeUpdate(sql_delete_stocklist);

		} catch (SQLException e) {
                        System.err.println("Failed to execute get password query" + e.getMessage());
		}
		return;
	}
	
	public void shareStocklist(String username, String stocklist_name, Statement stmt) {
		try {
			String sql_share_stocklist = "INSERT INTO shared_with (stocklist_id, username) " +
				"SELECT stocklist_id, '" + username + "' FROM stocklist "+
				"WHERE stocklist_name = '" + stocklist_name + "';";
			stmt.executeUpdate(sql_share_stocklist);
		} catch (SQLException e) {
                        System.err.println("Failed to execute get password query" + e.getMessage());
		}
		return;
	}
	public void unshareStocklist(String username, String stocklist_name, Statement stmt) { 
		try {
			String sql_unshare_stocklist = "DELETE FROM shared_with WHERE username = '" + username + "' AND stocklist_id = (SELECT stocklist_id FROM ;";
			stmt.executeUpdate(sql_unshare_stocklist);
		} catch (SQLException e) {
			System.err.println("Failed to execute unsharestocklist query" + e.getMessage());
		}
		return;
	}

	public void addStocklistStockHolding(String username, String stocklist_name, String symbol, int holding_amount, Statement stmt) {
		try {
			String sql_get_stocklist_id = "SELECT stocklist_id FROM stocklist WHERE stocklist_name = '" + stocklist_name + "' AND owner = '" + username + "';";
                        ResultSet rs = stmt.executeQuery(sql_get_stocklist_id);
                        rs.next();
                        String sid = rs.getString("stocklist_id");
                        String sql_add_holding = "INSERT INTO stocklist_stock_holding (stocklist_id, symbol, holding_amount) VALUES ('" + sid + "', '" + symbol + "', " + holding_amount + ");";
                        stmt.executeUpdate(sql_add_holding);
                        System.out.println("Adding to stocklist " + stocklist_name +" " +  holding_amount + " shares of " + symbol);
                }
                catch (SQLException e) {
                        System.err.println("Failed to execute add portfolio stock holding query" + e.getMessage());
                }
                return;
	}
	public void removeStocklistStockHolding(String username, String stocklist_name, String symbol, Statement stmt) {
		try {
			String sql_get_stocklist_id = "SELECT stocklist_id FROM stocklist WHERE stocklist_name = '" + stocklist_name + "' AND owner = '" + username + "';";
                        ResultSet rs = stmt.executeQuery(sql_get_stocklist_id);
                        rs.next();
                        String sid = rs.getString("stocklist_id");

			String sql_delete_stocklist_stockholding = "DELETE FROM stocklist_stock_holding WHERE stocklist_id = '" + sid + "' AND symbol = '" + symbol + "';";
			stmt.executeUpdate(sql_delete_stocklist_stockholding);
			System.out.println("Removed " + symbol + " from " + stocklist_name);
		 } catch(SQLException e) {
			System.err.println("Error removing stocklist stock holding");
		 }
	}	

}
