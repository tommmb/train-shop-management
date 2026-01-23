
CREATE TABLE UserAddresses (
    postcode VARCHAR(10) NOT NULL,
    house_number INT NOT NULL,
    county VARCHAR(100),
    city VARCHAR(100),
    road_name VARCHAR(255),
    PRIMARY KEY (postcode, house_number)
);

CREATE TABLE BankDetails (
    card_number VARCHAR(19) NOT NULL PRIMARY KEY,
    cvc VARCHAR(4),
    expiry_date VARCHAR(7),           
    cardholder_name VARCHAR(255),
    bank_card_name VARCHAR(100)       
);

CREATE TABLE Roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE  
);

CREATE TABLE Users (
    user_id SERIAL PRIMARY KEY,
    forename VARCHAR(100),
    surname VARCHAR(100),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,        
    salt VARCHAR(255) NOT NULL,            
    salt_aes VARCHAR(255),                 
    postcode VARCHAR(10),
    house_number INT,
    card_number VARCHAR(19),
    FOREIGN KEY (postcode, house_number) REFERENCES UserAddresses(postcode, house_number),
    FOREIGN KEY (card_number) REFERENCES BankDetails(card_number)
);


CREATE TABLE UserRoles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (role_id) REFERENCES Roles(role_id)
);


CREATE TABLE Products (
    product_id SERIAL PRIMARY KEY,
    product_name VARCHAR(255),
    brand_name VARCHAR(100),
    product_code VARCHAR(20) UNIQUE,
    manufacturer_code VARCHAR(50),
    price DECIMAL(10, 2),
    stock INT DEFAULT 0,
    size_ratio VARCHAR(20) 
);

CREATE TABLE Era (
    product_id INT NOT NULL PRIMARY KEY,
    start_era VARCHAR(20),              
    end_era VARCHAR(20),                
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);


CREATE TABLE Locomotives (
    product_id INT NOT NULL PRIMARY KEY,
    locomotive_model VARCHAR(50),       
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

CREATE TABLE Controllers (
    product_id INT NOT NULL PRIMARY KEY,
    signal_type VARCHAR(50),            
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

CREATE TABLE TrainSet (
    train_set_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL UNIQUE,
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

CREATE TABLE TrainSetProducts (
    train_set_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT DEFAULT 1,
    PRIMARY KEY (train_set_id, product_id),
    FOREIGN KEY (train_set_id) REFERENCES TrainSet(train_set_id),
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);


CREATE TABLE Orders (
    order_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    order_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING',
    total_cost DECIMAL(10, 2) DEFAULT 0.00,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

CREATE TABLE OrderLine (
    order_line_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT DEFAULT 1,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);