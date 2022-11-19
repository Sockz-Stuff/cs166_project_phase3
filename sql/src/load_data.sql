COPY Users
FROM '/extra/mjone032/cs166/final_project/data/users.csv'
WITH DELIMITER ',' CSV HEADER;
ALTER SEQUENCE users_userID_seq RESTART 101;

COPY Store
FROM '/extra/mjone032/cs166/final_project/data/stores.csv'
WITH DELIMITER ',' CSV HEADER;

COPY Product
FROM '/extra/mjone032/cs166/final_project/data/products.csv'
WITH DELIMITER ',' CSV HEADER;

COPY Warehouse
FROM '/extra/mjone032/cs166/final_project/data/warehouse.csv'
WITH DELIMITER ',' CSV HEADER;

COPY Orders
FROM '/extra/mjone032/cs166/final_project/data/orders.csv'
WITH DELIMITER ',' CSV HEADER;
ALTER SEQUENCE orders_orderNumber_seq RESTART 501;


COPY ProductSupplyRequests
FROM '/extra/mjone032/cs166/final_project/data/productSupplyRequests.csv'
WITH DELIMITER ',' CSV HEADER;
ALTER SEQUENCE productsupplyrequests_requestNumber_seq RESTART 11;

COPY ProductUpdates
FROM '/extra/mjone032/cs166/final_project/data/productUpdates.csv'
WITH DELIMITER ',' CSV HEADER;
ALTER SEQUENCE productupdates_updateNumber_seq RESTART 51;
