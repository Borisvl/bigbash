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
 
CREATE TABLE brands (sku TEXT,
brandcode TEXT) ;
 
CREATE TABLE brandnames (brandcode TEXT,
brandname TEXT) ;

SELECT brandcode, count(*), sum(price) FROM realtimelogging 
HASH LEFT JOIN brands on realtimelogging.sku = brands.sku 
GROUP BY brandcode order by count(*) DESC LIMIT 5;

