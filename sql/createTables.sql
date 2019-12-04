/*
CREATE TABLE Customers (
    cname CHAR(100),
    address CHAR(200),
    pinKey CHAR(4),
    taxid INT NOT NULL,
    PRIMARY KEY (taxid)
);*/


/*
CREATE TABLE Accounts ( 
    atype CHAR(50),
    status CHAR(10),
    bankname CHAR(50),
    balance FLOAT,
    interest FLOAT,
    aid INT NOT NULL, 
    taxid INT,
    PRIMARY KEY (aid),
    FOREIGN KEY (taxid) REFERENCES Customers ON DELETE CASCADE
);
*/

/*
CREATE TABLE Owners (
    aid INTEGER NOT NULL,
    taxid INTEGER NOT NULL,
    PRIMARY KEY (aid, taxid),
    FOREIGN KEY (aid) REFERENCES Accounts ON DELETE CASCADE,
    FOREIGN KEY (taxid) REFERENCES Customers ON DELETE CASCADE
);
*/

/*
CREATE TABLE Transactions (
    ttype CHAR(50),
    amount FLOAT,
    tdate DATE, 
    tid INT,
    aid INT,
    PRIMARY KEY (tid),
    FOREIGN KEY (aid) REFERENCES Accounts ON DELETE CASCADE
);
*/


CREATE TABLE TwoSided (
    aid INT NOT NULL,
    tid INT NOT NULL,
    PRIMARY KEY (aid,tid),
    FOREIGN KEY (aid) REFERENCES Accounts ON DELETE CASCADE,
    FOREIGN KEY (tid) REFERENCES Transactions
);


/*
CREATE TABLE WriteCheck (
    checkno INT NOT NULL,
    tid INT NOT NULL,
    PRIMARY KEY (checkno),
    FOREIGN KEY (tid) REFERENCES Transactions
);
*/

/*
CREATE TABLE Pocket (
    aid INT,
    aid2 INT,
    PRIMARY KEY (aid, aid2),
    FOREIGN KEY (aid) REFERENCES Accounts ON DELETE CASCADE,
    FOREIGN KEY (aid2) REFERENCES Accounts ON DELETE CASCADE
);
*/





    

    
    
