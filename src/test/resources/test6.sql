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
 
Select appdomain, count(*), sum(price) from realtimelogging where price <> '-' and sku REGEXP 'AD1.*' group by appdomain;

