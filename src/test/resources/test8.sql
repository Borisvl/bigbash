CREATE TABLE realtimelogging (actiondate DATE,
appdomain INT,
sku TEXT,
simplesku TEXT,
customerId TEXT,
sessioneId TEXT,
action TEXT,
salecount INT,
price INT,
orderId TEXT,
cookieId TEXT,
shobabtest TEXT,
internalReferer TEXT) ;
 
--SELECT group_concat(DISTINCT substr(sku, 0, length(sku)-2)) FROM realtimelogging 
--GROUP BY cookieId having count(*) > 5 AND count(*) < 100;
