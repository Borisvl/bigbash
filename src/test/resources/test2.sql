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

Select * from realtimelogging join brands on realtimelogging.sku = brands.sku where brands.sku REGEXP '^HI12.*' or brands.sku='JU621K000-B11';