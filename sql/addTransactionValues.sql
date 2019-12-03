INSERT INTO Transactions(ttype, amount, tdate, tid, aid) 
VALUES ('deposit',1200,DATE '2011-03-01',1,17431);

INSERT INTO Transactions(ttype, amount, tdate, tid, aid) 
VALUES ('deposit',21000,DATE '2011-03-01',2,54321);

INSERT INTO Transactions(ttype, amount, tdate, tid, aid) 
VALUES('deposit',1200,DATE '2011-03-01',3,12121);

INSERT INTO Transactions(ttype, amount, tdate, tid, aid) 
VALUES('deposit',15000,DATE '2011-03-01',4,41725);

INSERT INTO Transactions(ttype, amount, tdate, tid, aid) 
VALUES('deposit',2000000,DATE '2011-03-01',5,93156);

INSERT INTO Transactions(ttype, amount, tdate, tid, aid) 
VALUES('top-up',50,DATE '2011-03-01',6,53027);

INSERT INTO Transactions(aid,tid) 
VALUES(12121,6);

INSERT INTO Transactions(ttype, amount, tdate, tid, aid)
VALUES('deposit',1289, DATE '2011-03-01',7,43942);

INSERT INTO Transactions(ttype, amount, tdate, tid, aid) 
VALUES('deposit',34000,DATE '2011-03-01',8,29107);

INSERT INTO Transactions(ttype, amount, tdate, tid, aid) 
VALUES('deposit',2300,DATE '2011-03-01',9,19023);

INSERT INTO Transactions(ttype, amount, tdate, tid, aid) VALUES
('top-up',20,DATE '2011,03-01',10,60413);

Insert Into Twosided(Aid, Tid) 
Values(43942,10);
INSERT INTO Transactions(ttype, amount, tdate, tid, aid) 
VALUES('deposit',1000,DATE '2011-03-01',11,32156);

INSERT INTO Transactions(ttype, amount, tdate, tid, aid) 
VALUES('deposit',8456,DATE '2011-03-01',12,76543);

INSERT INTO Transactions(ttype, amount, tdate, tid, aid) 
VALUES('top-up',30,DATE '2011-03-01',13,43947);

INSERT INTO TwoSided(aid, tid) 
VALUES(29107,13);

INSERT INTO Transactions(ttype, amount, tdate, tid, aid) 
VALUES('top-up',100,DATE '2011-03-01',14,67521);

INSERT INTO TwoSided(aid, tid) 
VALUES(19023,14);
