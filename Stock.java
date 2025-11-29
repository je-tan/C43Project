package cs.toronto.edu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.Scanner;

public class Stock {

	public Stock() {
	}

        public void addStockEntry(Date date, double open, double high, double low, double close, int volume, String symbol, Statement stmt) {
                try {
                        String sql_add_stock_entry = "INSERT INTO stock (timestamp, open, high, low, close, volume, symbol) VALUES ('" + date + "'," + open + ", " + high + ", " + low + ", " + close + ", " + volume + ", '" + symbol + "');";
                        stmt.executeUpdate(sql_add_stock_entry);
                        System.out.println("Added new entry to stock table");
                } catch (SQLException e) {
                        System.err.println("Failed to execute add stock history query" + e.getMessage());
                }
        }	

	public void viewStockHistory(String symbol, Statement stmt) {
		try {
			String sql_get_stock_history = "SELECT close, timestamp FROM stock WHERE symbol = '" + symbol + "' ORDER BY timestamp DESC LIMIT 10;";
			ResultSet rs = stmt.executeQuery(sql_get_stock_history);
			double close = 0;
			java.sql.Date date;
			while (rs.next()) {
				close = rs.getDouble("close");
				date = rs.getDate("timestamp");
				System.out.println("Date: " + date + " Price: " + close);
			}

		}
		catch (SQLException e) {
			System.err.println("Failed to get stock history" + e.getMessage());
		}
		return;
	}

        public void predictStock(String symbol, Statement stmt) {
                try {
                        String sql_old_price = "SELECT close FROM stock WHERE symbol = '" + symbol + "' AND timestamp >=  (Select MAX(timestamp) -  INTERVAL '1 year' FROM stock WHERE symbol = '" + symbol + "') ORDER BY timestamp ASC LIMIT 1;";
                        ResultSet rs = stmt.executeQuery(sql_old_price);
			if (!rs.next()) {
				System.out.println("something went wrong");
				return;
			}
                        double old = rs.getDouble("close");

			String sql_curr_price = "SELECT close FROM stock WHERE symbol = '" + symbol + "' ORDER BY timestamp DESC LIMIT 10;";
                        rs = stmt.executeQuery(sql_curr_price);
			if (!rs.next()) {
				System.out.println("something went wrong");
				return;
			}
                        double nw = rs.getDouble("close");
			double estimated_price = (((nw - old) / old) + 1) * nw;
			System.out.println("old: " + old + " new: " + nw);
			System.out.println("Estimated value of " + symbol + " in 1 year is: " + estimated_price);
			

                }
                catch (SQLException e) {
                        System.err.println("Failed to predict stock price " + e.getMessage());
                }
                return;
        }

}	
