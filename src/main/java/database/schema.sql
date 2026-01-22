==================== USER MANAGEMENT TABLES ====================

-- Stores user addresses (referenced by Users)
CREATE TABLE UserAddresses (
    postcode VARCHAR(10) NOT NULL,
    house_number INT NOT NULL,
    county VARCHAR(100),
    city VARCHAR(100),
    road_name VARCHAR(255),
    PRIMARY KEY (postcode, house_number)
);

-- Stores bank/payment card details
CREATE TABLE BankDetails (
    card_number VARCHAR(19) NOT NULL PRIMARY KEY,
    cvc VARCHAR(4),
    expiry_date VARCHAR(7),           -- Format: MM/YYYY or similar
    cardholder_name VARCHAR(255),
    bank_card_name VARCHAR(100)       -- e.g., Visa, Mastercard
);

-- Stores user roles (CUSTOMER, STAFF, MANAGER)
CREATE TABLE Roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE  -- 'CUSTOMER', 'STAFF', 'MANAGER'
);

-- Main users table
CREATE TABLE Users (
    user_id SERIAL PRIMARY KEY,
    forename VARCHAR(100),
    surname VARCHAR(100),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,        -- SHA hashed password
    salt VARCHAR(255) NOT NULL,            -- Salt for password hashing
    salt_aes VARCHAR(255),                 -- Salt for AES encryption (Base64 encoded)
    postcode VARCHAR(10),
    house_number INT,
    card_number VARCHAR(19),
    FOREIGN KEY (postcode, house_number) REFERENCES UserAddresses(postcode, house_number),
    FOREIGN KEY (card_number) REFERENCES BankDetails(card_number)
);

-- Junction table for User-Role many-to-many relationship
CREATE TABLE UserRoles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (role_id) REFERENCES Roles(role_id)
);

==================== PRODUCT TABLES ====================

-- Main products table (base table for all product types)
-- Product codes follow patterns:
--   R##### = Track Piece
--   C##### = Controller
--   L##### = Locomotive
--   S##### = Rolling Stock
--   M##### = Train Set
--   P##### = Track Pack
CREATE TABLE Products (
    product_id SERIAL PRIMARY KEY,
    product_name VARCHAR(255),
    brand_name VARCHAR(100),
    product_code VARCHAR(20) UNIQUE,
    manufacturer_code VARCHAR(50),
    price DECIMAL(10, 2),
    stock INT DEFAULT 0,
    size_ratio VARCHAR(20)              -- SizeRatio enum: e.g., 'OO', 'N', 'TT', etc.
);

-- Era information for Rolling Stock and Locomotives
CREATE TABLE Era (
    product_id INT NOT NULL PRIMARY KEY,
    start_era VARCHAR(20),              -- Era enum values
    end_era VARCHAR(20),                -- Era enum values
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

-- Locomotive-specific information
CREATE TABLE Locomotives (
    product_id INT NOT NULL PRIMARY KEY,
    locomotive_model VARCHAR(50),       -- LocomotiveModel enum
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

-- Controller-specific information
CREATE TABLE Controllers (
    product_id INT NOT NULL PRIMARY KEY,
    signal_type VARCHAR(50),            -- SignalType enum (e.g., 'DCC', 'ANALOGUE')
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

-- Train Set information (links Products that are train sets)
CREATE TABLE TrainSet (
    train_set_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL UNIQUE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

-- Junction table for Train Set contents (products contained in a train set)
CREATE TABLE TrainSetProducts (
    train_set_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT DEFAULT 1,
    PRIMARY KEY (train_set_id, product_id),
    FOREIGN KEY (train_set_id) REFERENCES TrainSet(train_set_id),
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

==================== ORDER TABLES ====================

-- Orders table
-- Status values: 'PENDING', 'CONFIRMED', 'FULFILLED', 'CANCELLED'
CREATE TABLE Orders (
    order_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    order_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING',
    total_cost DECIMAL(10, 2) DEFAULT 0.00,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Order line items
CREATE TABLE OrderLine (
    order_line_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT DEFAULT 1,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

==================== INITIAL DATA - Roles ====================

INSERT INTO Roles (role_name) VALUES ('CUSTOMER');
INSERT INTO Roles (role_name) VALUES ('STAFF');
INSERT INTO Roles (role_name) VALUES ('MANAGER');
