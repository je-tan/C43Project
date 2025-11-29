CREATE TABLE users (
  username VARCHAR(20) PRIMARY KEY,
  password VARCHAR(20)
);

CREATE TABLE review (
  review_id SERIAL PRIMARY KEY,
  review_content VARCHAR(500)
);

CREATE TABLE review_by (
  review_id INT PRIMARY KEY,
  username VARCHAR(20),
  FOREIGN KEY (review_id) REFERENCES review(review_id)
	ON DELETE CASCADE,
  FOREIGN KEY (username) REFERENCES users(username)
	ON DELETE CASCADE
);

CREATE TABLE stocklist(
  stocklist_id SERIAL PRIMARY KEY,
  stocklist_name VARCHAR(100) NOT NULL,
  isPublic BOOLEAN,
  owner VARCHAR(100),
  FOREIGN KEY (owner) REFERENCES users(username)
);
CREATE TABLE review_of(
  review_id INT PRIMARY KEY,
  stocklist_id INT,
  FOREIGN KEY (review_id) REFERENCES review(review_id)
	ON DELETE CASCADE,
  FOREIGN KEY (stocklist_id) REFERENCES stocklist(stocklist_id)
	ON DELETE CASCADE
);


CREATE TABLE stock(
    timestamp DATE,
    open REAL,
    high REAL,
    low REAL,
    close REAL,
    volume INT,
    symbol VARCHAR(5),
    PRIMARY KEY (symbol, timestamp)
);

CREATE TABLE portfolio (
    portfolio_id SERIAL PRIMARY KEY,
    portfolio_name VARCHAR(100) NOT NULL,
    owner VARCHAR(100) NOT NULL,
    cash_amount REAL DEFAULT 0,
    FOREIGN KEY (owner) REFERENCES users(username)
);

CREATE TABLE portfolio_history (
    portfolio_id INT,
    timestamp TIMESTAMP,
    event VARCHAR(100),
    PRIMARY KEY (portfolio_id, timestamp),
    FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id)
);

CREATE TABLE portfolio_stock_holding (
    portfolio_id INT,
    symbol VARCHAR(5),
    holding_amount REAL,
    PRIMARY KEY (portfolio_id, symbol),
    FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id)
);
CREATE TABLE stocklist_stock_holding (
    stocklist_id INT,
    symbol VARCHAR(5),
    holding_amount REAL,
    PRIMARY KEY (stocklist_id, symbol),
    FOREIGN KEY (stocklist_id) REFERENCES stocklist(stocklist_id)
);

CREATE TABLE friend (
    sender_id VARCHAR(20),
    receiver_id VARCHAR(20),
    status VARCHAR(20) CHECK (status='pending' OR status='accepted' OR status='rejected'),
    time_last_request TIMESTAMP,
    PRIMARY KEY (sender_id, receiver_id),
    FOREIGN KEY (sender_id) REFERENCES users(username),
    FOREIGN KEY (receiver_id) REFERENCES users(username)
);

CREATE TABLE created_by (
    stocklist_id INT,
    username VARCHAR(20),
    PRIMARY KEY (stocklist_id, username),
    FOREIGN KEY (stocklist_id) REFERENCES stocklist(stocklist_id),
    FOREIGN KEY (username) REFERENCES users(username)
);

CREATE TABLE shared_with (
    stocklist_id INT,
    username VARCHAR(20),
    PRIMARY KEY (stocklist_id, username),
    FOREIGN KEY (stocklist_id) REFERENCES stocklist(stocklist_id),
    FOREIGN KEY (username) REFERENCES users(username)
);
