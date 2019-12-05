package cs174a;                                             // THE BASE PACKAGE FOR YOUR APP MUST BE THIS ONE.  But you may add subpackages.

// You may have as many imports as you need.
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Properties;
import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.OracleConnection;
import sun.nio.cs.ext.EUC_CN;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.transform.Result;

/**
 * The most important class for your application.
 * DO NOT CHANGE ITS SIGNATURE.
 */
public class App implements Testable {
	private OracleConnection _connection;                   // Example connection object to your DB.
	private Date sysDate;
	private String bankName;
	private int transactionId;

	/**
	 * Default constructor.
	 * DO NOT REMOVE.
	 */
	App() {
		sysDate = new Date(2011, 3, 1);
		transactionId=0;
		bankName="bank of Nuts";
	}

	/**
	 * This is an example access operation to the DB.
	 */
	void exampleAccessToDB() {
		// Statement and ResultSet are AutoCloseable and closed automatically.
		try (Statement statement = _connection.createStatement()) {
			try (ResultSet resultSet = statement.executeQuery("select owner, table_name from all_tables")) {
				while (resultSet.next())
					System.out.println(resultSet.getString(1) + " " + resultSet.getString(2) + " ");
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	////////////////////////////// Implement all of the methods given in the interface /////////////////////////////////
	// Check the Testable.java interface for the function signatures and descriptions.

	@Override
	public String initializeSystem() {
		// Some constants to connect to your DB.
		final String DB_URL = "jdbc:oracle:thin:@cs174a.cs.ucsb.edu:1521/orcl";
		final String DB_USER = "c##romanova";
		final String DB_PASSWORD = "4477352";

		// Initialize your system.  Probably setting up the DB connection.
		Properties info = new Properties();
		info.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME, DB_USER);
		info.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, DB_PASSWORD);
		info.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");

		try {
			OracleDataSource ods = new OracleDataSource();
			ods.setURL(DB_URL);
			ods.setConnectionProperties(info);
			_connection = (OracleConnection) ods.getConnection();

			// Get the JDBC driver name and version.
			DatabaseMetaData dbmd = _connection.getMetaData();
			System.out.println("Driver Name: " + dbmd.getDriverName());
			System.out.println("Driver Version: " + dbmd.getDriverVersion());

			// Print some connection properties.
			System.out.println("Default Row Prefetch Value is: " + _connection.getDefaultRowPrefetch());
			System.out.println("Database Username is: " + _connection.getUserName());
			System.out.println();

			return "0";
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			return "1";
		}
	}

	@Override
	public String dropTables() {
		Statement stmt;
		try {
			String[] dropStatements = new String[]{"DROP TABLE Pocket", "DROP TABLE TwoSided", "DROP TABLE Owners", "DROP TABLE WriteCheck", "DROP TABLE Transactions", "DROP TABLE Accounts", "DROP TABLE Customers"};
			for (String dropStatement : dropStatements) {
				stmt = _connection.createStatement();
				stmt.executeUpdate(dropStatement);
				// System.out.print("dropping table...");
			}
			return "0";
		}catch(SQLException er) {
			System.err.println(er.getMessage());
			return "1";
		}
	}

	/**
	 * Create all of your tables in your DB.
	 * @return a string "r", where r = 0 for success, 1 for error.
	 */
	@Override
	public String createTables() {
		final String CREATE_CUSTOMERS=
				"CREATE TABLE Customers ("
						+"cname CHAR(100),"
						+ "address CHAR(200),"
						+ "pinKey CHAR(4),"
						+ "taxid INT NOT NULL,"
						+ "PRIMARY KEY (taxid))";

		final String CREATE_ACCOUNTS=
				"CREATE TABLE Accounts ("
						+ "atype CHAR(50),"
						+ "status CHAR(10),"
						+ "bankname CHAR(50),"
						+ "balance FLOAT,"
						+ "interest FLOAT,"
						+ "aid INT NOT NULL,"
						+ "taxid INT,"
						+ "PRIMARY KEY (aid),"
						+ "FOREIGN KEY (taxid) REFERENCES Customers ON DELETE CASCADE)";

		final String CREATE_OWNERS=
				"CREATE TABLE Owners ("
						+ "aid INTEGER NOT NULL,"
						+ "taxid INTEGER NOT NULL,"
						+ "PRIMARY KEY (aid, taxid),"
						+ "FOREIGN KEY (aid) REFERENCES Accounts ON DELETE CASCADE,"
						+ "FOREIGN KEY (taxid) REFERENCES Customers ON DELETE CASCADE)";

		final String CREATE_TRANSACTIONS=
				"CREATE TABLE Transactions ("
						+ "ttype CHAR(50),"
						+ "amount FLOAT,"
						+ "tdate DATE,"
						+ "tid INT,"
						+ "aid INT,"
						+ "PRIMARY KEY (tid),"
						+ "FOREIGN KEY (aid) REFERENCES Accounts ON DELETE CASCADE)";

		final String CREATE_TWOSIDED=
				"CREATE TABLE TwoSided (" +
						"aid INT NOT NULL," +
						"tid INT NOT NULL," +
						"PRIMARY KEY (aid,tid)," +
						"FOREIGN KEY (aid) REFERENCES Accounts ON DELETE CASCADE," +
						"FOREIGN KEY (tid) REFERENCES Transactions)";

		final String CREATE_WRITECHECK=
				"CREATE TABLE WriteCheck ("
						+ "checkno INT NOT NULL,"
						+ "tid INT NOT NULL,"
						+ "PRIMARY KEY (checkno),"
						+ "FOREIGN KEY (tid) REFERENCES Transactions)";

		final String CREATE_POCKET=
				"CREATE TABLE Pocket ("
						+ "aid INT,"
						+ "aid2 INT,"
						+ "PRIMARY KEY (aid, aid2),"
						+ "FOREIGN KEY (aid) REFERENCES Accounts ON DELETE CASCADE,"
						+ "FOREIGN KEY (aid2) REFERENCES Accounts ON DELETE CASCADE)";

		try{
			Statement stmnt = _connection.createStatement();
			stmnt.executeUpdate(CREATE_CUSTOMERS);
			stmnt.executeUpdate(CREATE_ACCOUNTS);
			stmnt.executeUpdate(CREATE_OWNERS);
			stmnt.executeUpdate(CREATE_TRANSACTIONS);
			stmnt.executeUpdate(CREATE_TWOSIDED);
			stmnt.executeUpdate(CREATE_WRITECHECK);
			stmnt.executeUpdate(CREATE_POCKET);
			return "0";
		}
		catch(SQLException e) {
			System.err.println( e.getMessage() );
			return "1";
		}
	}


	/**
	 * Create a new customer and link them to an existing checking or saving account.
	 * @param accountId Existing checking or saving account.
	 * @param tin New customer's Tax ID number.
	 * @param name New customer's name.
	 * @param address New customer's address.
	 * @return a string "r", where r = 0 for success, 1 for error.
	 */
	@Override
	public String createCustomer( String accountId, String tin, String name, String address ){
		final String INSERT_CUSTOMER =
				"INSERT INTO Customers(taxid, cname, address, pinkey)"
				+ "\nVALUES("
				+ Integer.parseInt(tin) + ","
				+ "'" + name + "'" + ","
				+ "'" + address + "'" + ","
				+ "NULL)";

		try{
			//insert customer data
			System.out.println(INSERT_CUSTOMER);
			Statement stmnt = _connection.createStatement();

			stmnt.executeUpdate(INSERT_CUSTOMER);
			return "0";
		}
		catch(SQLException er) {
			System.err.println(er.getMessage());
			return "1";
		}
	}

	/**
	 * Set system's date.
	 *
	 * @param year  Valid 4-digit year, e.g. 2019.
	 * @param month Valid month, where 1: January, ..., 12: December.
	 * @param day   Valid day, from 1 to 31, depending on the month (and if it's a leap year).
	 * @return a string "r yyyy-mm-dd", where r = 0 for success, 1 for error; and yyyy-mm-dd is the new system's date, e.g. 2012-09-16.
	 */
	@Override
	public String setDate(int year, int month, int day) {
		String newDate = year + "-" + month + "-" + day;
		try {
			sysDate = new Date(year, month, day);
			return "0 " + newDate;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return "1 " + newDate;
		}
	}



	/**
	 * Move a specified amount of money from the linked checking/savings account to the pocket account.
	 * @param accountId Pocket account ID.
	 * @param amount Non-negative amount to top up.
	 * @return a string "r linkedNewBalance pocketNewBalance", where
	 *         r = 0 for success, 1 for error;
	 *         linkedNewBalance is the new balance of linked account, with up to 2 decimal places (e.g. with %.2f); and
	 *         pocketNewBalance is the new balance of the pocket account.
	 */
	@Override
	public String topUp( String accountId, double amount ) {
		Statement stmt = null;
		final String LINKED_ACCOUNT_QUERY =
				"SELECT aid2 "
				+ "FROM Pocket "
				+ " WHERE aid=" + accountId;

		final String LINKED_BALANCE_QUERY =
				"SELECT A.balance\n" +
				"FROM accounts A\n" +
				"WHERE A.aid = \n" +
				"    (SELECT P.aid2\n" +
				"    FROM pocket P\n" +
				"    WHERE P.aid =" + accountId +
				")\n";

		final String POCKET_BALANCE_QUERY =
				"SELECT A.balance\n" +
						"FROM Accounts A\n" +
						"WHERE A.aid = " + accountId;
 
		final String LINKED_BALANCE_UPDATE;
		final String POCKED_BALANCE_UPDATE;

		double pocketBalance = 0;
		double linkedBalance = 0;
		double newPocketBalance = 0;
		double newLinkedBalance = 0;




		try {
			//query for the corresponding linked account using pocket account in pocket table
			stmt = _connection.createStatement();
			int test = 0;
			ResultSet r = stmt.executeQuery(LINKED_ACCOUNT_QUERY);
			if(r.next() == false) {
				System.out.print("sorry :(");
			}
			while(r.next()) {
				System.out.println("hi");
				test = r.getInt(1);
				System.out.println("id: " + test);
			}

			ResultSet rs = stmt.executeQuery(POCKET_BALANCE_QUERY);
			System.out.println(POCKET_BALANCE_QUERY);
			while(rs.next()) {
				pocketBalance = rs.getDouble("balance");
				System.out.println("pocket balance: " + pocketBalance);
			}

			rs = stmt.executeQuery(LINKED_BALANCE_QUERY);
			while(rs.next()) {
				linkedBalance = rs.getDouble("balance");
				System.out.print("linked balance: " + linkedBalance);
			}


			newPocketBalance = pocketBalance + amount;
			newLinkedBalance = linkedBalance - amount;


			//then update the corresponding balance for that checking/savings row in accounts table
			String UPDATE_LINKED = "UPDATE Accounts " +
					"SET balance = " + newLinkedBalance +
					" WHERE aid = ";
			String UPDATE_POCKET = "UPDATE Accounts " +
					"SET balance = " + newLinkedBalance +
					" WHERE aid = ";
//			rs = stmt.executeUpdate();
//			rs = stmt.executeUpdate(");


			return "0 " + pocketBalance + linkedBalance;
		} catch (SQLException e) {
			return "1" + e;

		}
	}

	/**
	 * Create a new checking or savings account.
	 * If customer is new, then their name and address should be provided.
	 *
	 * @param accountType    New account's checking or savings type.
	 * @param id             New account's ID.
	 * @param initialBalance Initial account balance.
	 * @param tin            Account's owner Tax ID number - it may belong to an existing or new customer.
	 * @param name           [Optional] If customer is new, this is the customer's name.
	 * @param address        [Optional] If customer is new, this is the customer's address.
	 * @return a string "r aid type balance tin", where
	 * r = 0 for success, 1 for error;
	 * aid is the new account id;
	 * type is the new account's type (see the enum codes above, e.g. INTEREST_CHECKING);
	 * balance is the account's initial balance with 2 decimal places (e.g. 1000.34, as with %.2f); and
	 * tin is the Tax ID of account's primary owner.
	 */
	@Override
	public String createCheckingSavingsAccount(AccountType accountType, String id, double initialBalance, String tin, String name, String address) {
		// check if customer exists
		double interest=0.0;
		Statement stmt;
		ResultSet rs;
		String customerLookupQuery = "SELECT * FROM Customers C WHERE C.taxid=" + tin;
		if(accountType==Testable.AccountType.STUDENT_CHECKING || accountType== AccountType.INTEREST_CHECKING) {
			interest=3.0;
		} else if(accountType==AccountType.SAVINGS) {
			interest=4.8;
		}
		String createAccountQuery = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid) VALUES"+
									"('"+accountType+"','open','"+bankName+"',"+initialBalance+","+interest+","+id+","+tin+")";
		// create customer if customer does not exist
		try {
			stmt = _connection.createStatement();
			rs = stmt.executeQuery(customerLookupQuery);
			if (!rs.next()) {
				createCustomer(id,tin,name,address);
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			return "1 " + id + " " + accountType + " " + initialBalance + " " + tin;
		}
		// create account
		try {
			stmt = _connection.createStatement();
			stmt.executeUpdate(createAccountQuery);
		}catch(SQLException e){
			System.err.print(e.getMessage());
			return "1 " + id + " " + accountType + " " + initialBalance + " " + tin;
		}
		return "0"+ id + " " + accountType + " " + initialBalance + " " + tin;
	}

	/**
	 * Deposit a given amount of dollars to an existing checking or savings account.
	 * @param accountId Account ID.
	 * @param amount Non-negative amount to deposit.
	 * @return a string "r old new" where
	 *         r = 0 for success, 1 for error;
	 *         old is the old account balance, with up to 2 decimal places (e.g. 1000.12, as with %.2f); and
	 *         new is the new account balance, with up to 2 decimal places.
	 */
	@Override
	public String deposit( String accountId, double amount ) {
		// DO WE NEED TO CHECK ACCOUNT TYPE?
		Statement stmt;
		String oldBalance;
		String newBalance;
		oldBalance = getAccountBalance(accountId);
		editAccountBalance(accountId, amount);
		createTransaction("deposit",amount,accountId,"-1");
		newBalance = getAccountBalance(accountId);
		return "0 "+oldBalance+" "+newBalance;
	}

	/**
	 * Move a specified amount of money from one pocket account to another pocket account.
	 * @param from Source pocket account ID.
	 * @param to Destination pocket account ID.
	 * @param amount Non-negative amount to pay.
	 * @return a string "r fromNewBalance toNewBalance", where
	 *         r = 0 for success, 1 for error.
	 *         fromNewBalance is the new balance of the source pocket account, with up to 2 decimal places (e.g. with %.2f); and
	 *         toNewBalance is the new balance of destination pocket account, with up to 2 decimal places.
	 */
	@Override
	public String payFriend( String from, String to, double amount ) {
		// check that from, to are pocket accounts
		String fromQuery = "SELECT A.atype FROM Accounts A WHERE A.aid="+from;
		String toQuery = "SELECT A.atype FROM Accounts A WHERE A.aid="+to;
		String firstTransaction = "SELECT * FROM Transaction T WHERE T.aid="+from;
		double fromNewBalance=0;
		double toNewBalance=0;
		Statement stmt;
		ResultSet rs;
		try{
			// check that both accounts are pocket accounts
			stmt=_connection.createStatement();
			rs = stmt.executeQuery(fromQuery);
			if(!rs.next() || rs.getString("atype")!="POCKET") {
				System.out.print("Error: both accounts must be existing Pocket accounts");
				return "1 "+fromNewBalance+" "+toNewBalance;
			}
			rs = stmt.executeQuery(toQuery);
			if(!rs.next() || rs.getString("atype")!="POCKET") {
				System.out.print("Error: both accounts must be existing Pocket accounts");
				return "1 "+fromNewBalance+" "+toNewBalance;
			}
			// the first transaction of the month incurs a $5 fee
			rs = stmt.executeQuery(firstTransaction);
			if(!rs.next()) {
				amount = amount-5.0;
			}
			// edit account balances
			editAccountBalance(from, amount*-1);
			editAccountBalance(to, amount);
			// create Transaction
			int fromId = 0;
			int toId = 0;
			createTransaction("pocket", amount, from, to);
		}catch(SQLException e) {
			System.err.print(e.getMessage());
			return "1 "+fromNewBalance+" "+toNewBalance;
		}
		return "0 "+fromNewBalance+" "+toNewBalance;
	}

	/**
	 * Add Transactions entry and TwoSided entry (if needed) to db
	 * @param ttype type of transaction
	 * @param amount dollar amount
	 * @param aid account id initiating transaction
	 * @param aid2 for two sided transactions, "-1" otherwise
	 * @return a string "r", where r=0 for success, 1 for error
	 */
	@Override
	public String createTransaction(String ttype, double amount, String aid,String aid2) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String currentDate = formatter.format(java.time.LocalDate.now());
		String transactionQuery = "INSERT INTO Transactions(ttype, amount,tdate,tid,aid) VALUES ("
									+ ttype + ","
									+ amount + ","
									+ ",DATE '" + currentDate + "',"
									+ transactionId + ","
								    + aid + ")";
		Statement stmt;
		try {
			stmt = _connection.createStatement();
			stmt.executeQuery(transactionQuery);
			if(aid2!="-1") {
				String twoSidedQuery = "INSERT INTO TwoSided(aid, tid) VALUES ("
						+ aid2 + ","
						+ transactionId + ")";

				stmt.executeQuery(twoSidedQuery);
			}
			transactionId++;
			return "0";
		}catch(SQLException e){
			System.err.print(e.getMessage());
			return "1";
		}
	}

	/**
	 * Generate list of closed accounts.
	 * @return a string "r id1 id2 ... idn", where
	 *         r = 0 for success, 1 for error; and
	 *         id1 id2 ... idn is a list of space-separated closed account IDs.
	 */
	@Override
	public String listClosedAccounts() {
		String query = "SELECT A.aid " +
				       "FROM Accounts A " +
					   "WHERE A.status='closed'";
		String ids = "";
		Statement stmt;
		ResultSet rs;
		try {
			stmt = _connection.createStatement();
			rs = stmt.executeQuery(query);
			while(rs.next()){
				ids += " ";
				ids += rs.getString("aid");
			}
			return "0" + ids;
		} catch(SQLException e) {
			System.err.print(e.getMessage());
			return "1" + ids;
		}
	}


	/**
	 * Add or subtract a given amount from account balance
	 * close account if balance <=.01 but >0
	 * fail if balance <0 after transaction
	 * @param aid account id
	 * @param amount negative to subtract, positive to add
	 * @return a string "r", where r=0 if success, 1 for error, -1 for failed transaction
	 */
	@Override
	public String editAccountBalance(String aid,double amount) {
		Statement stmt;
		ResultSet rs;
		String findAccountQuery = "SELECT A.balance FROM Accounts A WHERE A.aid=" + aid;
		String updateAccount = "";
		double balance = 0.0;
		// find account
		try {
			stmt = _connection.createStatement();
			rs = stmt.executeQuery(findAccountQuery);
			balance = rs.getInt("balance");
			balance += amount;
			if (balance < 0) {
				return "-1";
			}
			// close account, update balance
			if (balance <= .01) {
				updateAccount = "UPDATE Accounts " +
						"SET balance = " + balance +
						", status = 'closed'" +
						" WHERE aid = " + aid;
				stmt.executeUpdate(updateAccount);
			}
			if (balance > .01) {
				updateAccount = "UPDATE Accounts " +
						"Set balance = " + balance +
						" WHERE aid = " + aid;
				stmt.executeUpdate(updateAccount);
			}
		} catch (SQLException e) {
			System.err.print(e.getMessage());
			return "1";
		}
		return "0";
	}

	/**
	 * Get current balance of account
	 * @param aid account id
	 * @return account balance as a string, or "-1" for error
	 */
	@Override
	public String getAccountBalance(String aid) {
		Statement stmt;
		ResultSet rs;
		String getBalance = "SELECT A.balance FROM Accounts A WHERE A.aid="+aid;
		double balance = 0.0;
		try {
			stmt = _connection.createStatement();
			rs = stmt.executeQuery(getBalance);
			balance = rs.getInt("balance");
			return(Double.toString(balance));
		} catch(SQLException e) {
			System.err.print(e.getMessage());
			return "-1";
		}
	}

	// HELPER FUNCTION TO CHECK IF TRANSACTION ARE LEGAL?
	@Override
	public String checkAccountType(Testable.AccountType aType) {
		return "0";
	}

	public void populateCustomerData(){
		String alfred = "INSERT INTO Customers (taxid, cname, address, pinkey) \n" +
				"VALUES (361721022, 'Alfred Hitchcock', '6667 El Colegio #40', '1234')";

		String billy = "INSERT INTO Customers (taxid, cname, address, pinkey) \n" +
				"VALUES (231403227, 'Billy Clinton','5777 Hollister' ,'1468')";

		String cindy = "INSERT INTO Customers (taxid, cname, address, pinkey) \n" +
				"VALUES (412231856, 'Cindy Laugher', '7000 Hollister', '3764')";

		String david = "INSERT INTO Customers (taxid, cname, address, pinkey) \n" +
				"VALUES (207843218, 'David Copperfill','1357 State St', '8582')";

		String elizabeth = "INSERT INTO Customers (taxid, cname, address, pinkey) \n" +
				"VALUES (122219876, 'Elizabeth Sailor', '4321 State St', '3856')";

		String fatal = "INSERT INTO Customers (taxid, cname, address, pinkey) \n" +
				"VALUES (401605312, 'Fatal Castro','3756 La Cumbre Plaza' ,'8193')";

		String george = "INSERT INTO Customers (taxid, cname, address, pinkey) \n" +
				"VALUES (201674933, 'George Brush', '5346 Foothill Av', '9824')";

		String hurryson = "INSERT INTO Customers (taxid, cname, address, pinkey) \n" +
				"VALUES (212431965, 'Hurryson Ford', '678 State St', '3532')";

		String ivan = "INSERT INTO Customers (taxid, cname, address, pinkey) \n" +
				"VALUES (322175130, 'Ivan Lendme','1235 Johnson Dr', '8471')";

		String joe = "INSERT INTO Customers (taxid, cname, address, pinkey) \n" +
				"VALUES (344151573, 'Joe Pepsi','3210 State St' ,'3692')";

		String kelvin = "INSERT INTO Customers (taxid, cname, address, pinkey) \n" +
				"VALUES (209378521, 'Kelvin Costner','Santa Cruz #3579', '4659')";

		String li = "INSERT INTO Customers (taxid, cname, address, pinkey) \n" +
				"VALUES (212116070, 'Li Kung', '2 People''s Rd Beijing', '9173')";

		String magic = "INSERT INTO Customers (taxid, cname, address, pinkey) \n" +
				"VALUES (188212217, 'Magic Jordon','3852 Court Rd', '7351')";

		String nam = "INSERT INTO Customers (taxid, cname, address, pinkey) \n" +
				"VALUES (203491209, 'Nam-Hoi Chung', '1997 People''s St HK', '5340')";

		String olive = "INSERT INTO Customers (taxid, cname, address, pinkey) \n" +
				"VALUES (210389768, 'Olive Stoner', '6689 El Colegio #151', '8452')";

		String pit = "INSERT INTO Customers (taxid, cname, address, pinkey) \n" +
				"VALUES (400651982, 'Pit Wilson', '911 State St' ,'1821')";
		Statement stmt;
		try{
			stmt = _connection.createStatement();

			stmt.executeUpdate(alfred);
			stmt.executeUpdate(billy);
			stmt.executeUpdate(cindy);
			stmt.executeUpdate(david);
			stmt.executeUpdate(elizabeth);
			stmt.executeUpdate(fatal);
			stmt.executeUpdate(george);
			stmt.executeUpdate(hurryson);
			stmt.executeUpdate(ivan);
			stmt.executeUpdate(joe);
			stmt.executeUpdate(kelvin);
			stmt.executeUpdate(li);
			stmt.executeUpdate(magic);
			stmt.executeUpdate(nam);
			stmt.executeUpdate(olive);
			stmt.executeUpdate(pit);

			System.out.println("Customers Populated");
		}

		catch(SQLException e) {
			System.out.println(e);
		}
	}

	public void populateAccountData() {
		final String a = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('student-checking','open','San Francisco',0,3.0,17431,344151573)";
		final String b = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('student-checking','open','Los Angeles',0,3.0,54321,212431965)";
		final String c = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('Student-checking','open','Goleta',0,3.0,12121,207843218)";
		final String d = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('Interest-checking','open','Los Angeles',0,3.0,41725,201674933)";
		final String e = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('Interest-checking','open','Santa Barbara',0,3.0,76543,212116070)";
		final String f = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('Interest-checking','open','Goleta',0,3.0,93156,209378521)";
		final String g = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('Savings','open','Santa Barbara',0,4.8,43942,361721022)";
		final String h = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('Savings','open','Los Angeles',0,4.8,29107,209378521)";
		final String i = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('Savings','open','San Francisco',0,4.8,19023,412231856)";
		final String j = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('Savings','open','Goleta',0,4.8,32156,188212217)";
		final String k = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('Pocket','open','Goleta',0,0,53027,207843218)";
		final String l = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('Pocket','open','Isla Vista',0,0,43947,212116070)";
		final String m = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('Pocket','open','Santa Cruz',0,0,60413,400651982)";
		final String n = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('Pocket','open','Santa Barbara',0,0,67521,401605312)";

		Statement stmt;
		try {
			stmt = _connection.createStatement();

			stmt.executeUpdate(a);
			stmt.executeUpdate(b);
			stmt.executeUpdate(c);
			stmt.executeUpdate(d);
			stmt.executeUpdate(e);
			stmt.executeUpdate(f);
			stmt.executeUpdate(g);
			stmt.executeUpdate(h);
			stmt.executeUpdate(i);
			stmt.executeUpdate(j);
			stmt.executeUpdate(k);
			stmt.executeUpdate(l);
			stmt.executeUpdate(m);
			stmt.executeUpdate(n);

			System.out.println("Accounts Populated");
		}
		catch (SQLException ex){
			System.out.println(ex);
		}
	}

	public void populatePocketData() {
		final String a = "INSERT INTO Pocket(aid, aid2) \n" +
				"VALUES(53027, 12121)";
		final String b = "INSERT INTO Pocket(aid, aid2)\n" +
				"VALUES(43947, 29107)";
		final String c = "INSERT INTO Pocket(aid, aid2) \n" +
				"VALUES(60413, 43942)";
		final String d = "INSERT INTO Pocket(aid, aid2) \n" +
				"VALUES(67521, 19023)";

		Statement stmt;

		try{
			stmt = _connection.createStatement();
			stmt.executeUpdate(a);
			stmt.executeUpdate(b);
			stmt.executeUpdate(c);
			stmt.executeUpdate(d);

			System.out.println("Pockets Populated");
		}
		catch(SQLException ex) {
			System.out.println(ex);
		}
	}

	public void populateOwnersData() {
		final String a = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(17431, 344151573)";
		final String b = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(17431, 412231856)";
		final String c = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(17431, 322175130)";
		final String d = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(54321, 212431965)";
		final String e = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(54321, 412231856 )";
		final String f = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(54321, 122219876)";
		final String g = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(54321, 203491209)";
		final String h = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(12121, 207843218)";
		final String i = "INSERT INTO Owners(aid,taxid)\n" +
				"VALUES(41725, 201674933)";
		final String j = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(41725,401605312)";
		final String k = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(41725,231403227)";
		final String l = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(76543,212116070)";
		final String m = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(76543,188212217)";
		final String n = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(93156,209378521)";
		final String o = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(93156,188212217)";
		final String p = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(93156,210389768)";
		final String q = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(93156,122219876)";
		final String r =  "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(93156,203491209)";
		final String s = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(43942,361721022)";
		final String t = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(43942,400651982)";
		final String u = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(43942,212431965)";
		final String v = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(43942,322175130)";
		final String w = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(29107,209378521)";
		final String x = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(29107, 212116070)";
		final String y = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(29107, 210389768)";
		final String z = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(19023, 412231856)";
		final String aa = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(19023, 201674933)";
		final String bb = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(19023, 401605312)";
		final String cc = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(32156, 188212217 )";
		final String dd = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(32156, 207843218)";
		final String ee = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(32156, 122219876)";
		final String ff = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(32156, 344151573)";
		final String gg = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(32156,203491209)";
		final String hh = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(32156,210389768)";
		final String ii = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(53027, 207843218)";
		final String jj = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(43947,212116070)";
		final String kk = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(60413,400651982)";
		final String ll = "INSERT INTO Owners(aid,taxid) \n" +
				"VALUES(67521,401605312)";

		Statement stmt;

		try{
			stmt = _connection.createStatement();
			stmt.executeUpdate(a);
			stmt.executeUpdate(b);
			stmt.executeUpdate(c);
			stmt.executeUpdate(d);
			stmt.executeUpdate(e);
			stmt.executeUpdate(f);
			stmt.executeUpdate(g);
			stmt.executeUpdate(h);
			stmt.executeUpdate(i);
			stmt.executeUpdate(j);
			stmt.executeUpdate(k);
			stmt.executeUpdate(l);
			stmt.executeUpdate(m);
			stmt.executeUpdate(n);
			stmt.executeUpdate(o);
			stmt.executeUpdate(p);
			stmt.executeUpdate(q);
			stmt.executeUpdate(r);
			stmt.executeUpdate(s);
			stmt.executeUpdate(t);
			stmt.executeUpdate(u);
			stmt.executeUpdate(v);
			stmt.executeUpdate(w);
			stmt.executeUpdate(x);
			stmt.executeUpdate(y);
			stmt.executeUpdate(z);
			stmt.executeUpdate(aa);
			stmt.executeUpdate(bb);
			stmt.executeUpdate(cc);
			stmt.executeUpdate(dd);
			stmt.executeUpdate(ee);
			stmt.executeUpdate(gg);
			stmt.executeUpdate(hh);
			stmt.executeUpdate(ii);
			stmt.executeUpdate(jj);
			stmt.executeUpdate(kk);
			stmt.executeUpdate(ll);

			System.out.println("Owners Populated");
		}
		catch(SQLException ex) {
			System.out.println(ex);
		}
	}

}
