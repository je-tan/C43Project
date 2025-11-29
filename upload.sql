COPY Stock(timestamp, open, high,
low, close, volume, symbol) FROM '/data/SP500History.csv' DELIMITER ','
CSV HEADER;
