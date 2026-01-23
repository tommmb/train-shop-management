-- Test data for DatabaseOperations tests

-- Insert roles
INSERT INTO Roles (role_name) VALUES ('CUSTOMER');
INSERT INTO Roles (role_name) VALUES ('STAFF');
INSERT INTO Roles (role_name) VALUES ('MANAGER');

-- Insert test addresses
INSERT INTO UserAddresses (postcode, house_number, county, city, road_name) 
VALUES ('S1 1AA', 10, 'South Yorkshire', 'Sheffield', 'Main Street');

INSERT INTO UserAddresses (postcode, house_number, county, city, road_name) 
VALUES ('S2 2BB', 25, 'South Yorkshire', 'Sheffield', 'High Road');

-- Insert test bank details
INSERT INTO BankDetails (card_number, cvc, expiry_date, cardholder_name, bank_card_name)
VALUES ('1234567890123456', '123', '12/2028', 'John Doe', 'Visa');

-- Insert test users (password is 'password123' hashed - for testing only)
INSERT INTO Users (forename, surname, email, password, salt, postcode, house_number, card_number)
VALUES ('John', 'Doe', 'john.doe@example.com', 'hashedpassword123', 'salt123', 'S1 1AA', 10, '1234567890123456');

INSERT INTO Users (forename, surname, email, password, salt, postcode, house_number, card_number)
VALUES ('Jane', 'Smith', 'jane.smith@example.com', 'hashedpassword456', 'salt456', 'S2 2BB', 25, NULL);

-- Assign roles to users (John is CUSTOMER, Jane is STAFF)
INSERT INTO UserRoles (user_id, role_id) VALUES (1, 1);  -- John = CUSTOMER
INSERT INTO UserRoles (user_id, role_id) VALUES (2, 1);  -- Jane = CUSTOMER
INSERT INTO UserRoles (user_id, role_id) VALUES (2, 2);  -- Jane = STAFF

-- Insert test products
INSERT INTO Products (product_name, brand_name, product_code, manufacturer_code, price, stock, size_ratio)
VALUES ('Flying Scotsman', 'Hornby', 'L00001', 'HR-FS-001', 199.99, 5, 'OO');

INSERT INTO Products (product_name, brand_name, product_code, manufacturer_code, price, stock, size_ratio)
VALUES ('Standard Track Pack', 'Hornby', 'P00001', 'HR-TP-001', 29.99, 20, 'OO');

-- Insert locomotive data
INSERT INTO Locomotives (product_id, locomotive_model) VALUES (1, 'STEAM');

INSERT INTO Era (product_id, start_era, end_era) VALUES (1, 'ERA_3', 'ERA_5');

-- Insert test order for John
INSERT INTO Orders (user_id, order_date, status, total_cost)
VALUES (1, '2026-01-20', 'PENDING', 229.98);

INSERT INTO OrderLine (order_id, product_id, quantity) VALUES (1, 1, 1);
INSERT INTO OrderLine (order_id, product_id, quantity) VALUES (1, 2, 1);
