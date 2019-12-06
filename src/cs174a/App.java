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
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Date;

import javax.xml.transform.Result;

/**
 * The most important class for your application.
 * DO NOT CHANGE ITS SIGNATURE.
 */
public class App implements Testable {
	private OracleConnection _connection;                   // Example connection object to your DB.
	public Date sysDate;
	private String bankName;
	private int transactionId;
	private boolean addedInterest;
	public String currentCustomerTid;

	/**
	 * Default constructor.
	 * DO NOT REMOVE.
	 */
	App() {
		sysDate = new Date(2011, 3, 1);
		transactionId=0;
		bankName="bank of Nuts";
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

	//good
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
	//good
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

	// good
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
				+ tin + ","
				+ "'" + name + "'" + ","
				+ "'" + address + "'" + ","
				+ "1717)";

		try{
			//insert customer data
			Statement stmnt = _connection.createStatement();
			stmnt.executeUpdate(INSERT_CUSTOMER);
			return "0";
		}
		catch(SQLException er) {
			System.err.println(er.getMessage());
			return "1";
		}
	}

	//good
	public String createOwner(String id, String tin) {
		final String INSERT_OWNERS = "INSERT INTO Owners(aid, taxid)\n" +
				"VALUES(" + id + "," +  tin + ")";
		try {
			Statement stmt = _connection.createStatement();
			stmt.executeUpdate(INSERT_OWNERS);
			return "0";
		}
		catch(SQLException er) {
			System.err.println(er.getMessage());
			return "1";
		}
	}

	/**
	 * Set system's date.
	 * @param year  Valid 4-digit year, e.g. 2019.
	 * @param month Valid month, where 1: January, ..., 12: December.
	 * @param day   Valid day, from 1 to 31, depending on the month (and if it's a leap year).
	 * @return a string "r yyyy-mm-dd", where r = 0 for success, 1 for error; and yyyy-mm-dd is the new system's date, e.g. 2012-09-16.
	 */
	//good
	@Override
	public String setDate(int year, int month, int day) {
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
		String stringDate = "";
		try {
			sysDate = new Date(year, month, day);
			stringDate = DATE_FORMAT.format(sysDate);
			return "0 " + stringDate;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return "1 " + stringDate;
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
	//good
	@Override
	public String topUp( String accountId, double amount ) {
		Statement stmt;
		final String LINKED_ACCOUNT_QUERY =
				"SELECT aid2 "
				+ "FROM Pocket "
				+ " WHERE aid=" + accountId;
		double newPocketBalance = 0;
		double newLinkedBalance = 0;
		String linkedId = "";
		try {
			//query for the corresponding linked account using pocket account in pocket table
			stmt = _connection.createStatement();

			//get linked aid
			ResultSet rs = stmt.executeQuery(LINKED_ACCOUNT_QUERY);

			while(rs.next()) {
				linkedId = Integer.toString(rs.getInt("aid2"));
			}
			//update balances
			String r = editAccountBalance(accountId, amount);
			if(r.equals("1")){
				System.out.println("Cannot edit balance");
				return "1 " +  newLinkedBalance + " " +  newPocketBalance;
			}
			String s = editAccountBalance(linkedId, amount*-1);
			if(s.equals("1")){
				System.out.println("Cannot edit balance");
				return "1 " +  newLinkedBalance + " " +  newPocketBalance;
			}
			// create Transaction
			createTransaction("top-up",amount,linkedId,accountId);
			// return
			newPocketBalance = Double.parseDouble(getAccountBalance(accountId));
			newLinkedBalance = Double.parseDouble(getAccountBalance(linkedId));
			return "0 " + newLinkedBalance + " " +  newPocketBalance;
		} catch (SQLException e) {
			System.out.println(e);
			return "1 " +  newLinkedBalance + " " +  newPocketBalance;
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
	//good
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

		//createOwners entry
		String r = createOwner(id, tin);
		if(r.equals("1")){
			return "1 " + id + " " + accountType + " " + initialBalance + " " + tin;
		}
		return "0"+ id + " " + accountType + " " + initialBalance + " " + tin;
	}

	/**
	 * Create a new pocket account.
	 * @param id New account's ID.
	 * @param linkedId Linked savings or checking account ID.
	 * @param initialTopUp Initial balance to be deducted from linked account and deposited into new pocket account.
	 * @param tin Existing customer's Tax ID number.  He/She will become the new pocket account's owner.
	 * @return a string "r aid type balance tin", where
	 *         r = 0 for success, 1 for error;
	 *         aid is the new account id;
	 *         type is the new account's type (see the enum codes above);
	 *         balance is the account's initial balance with up to 2 decimal places (e.g. 1000.12, as with %.2f); and
	 *         tin is the Tax ID of account's primary owner.
	 */
	//good
	@Override
	public String createPocketAccount( String id, String linkedId, double initialTopUp, String tin ) {
		String INSERT_ACCOUNTS = "INSERT INTO Accounts(atype, status,bankname,balance,interest,aid,taxid) VALUES ("+
									 "'POCKET','open',"+ "'" + bankName+"'" + ",0.0,0.0,"+id+","+tin+")";
		String INSERT_POCKET = "INSERT INTO Pocket(aid, aid2) \n" +
				"VALUES("+ id +"," + linkedId + ")";

		Statement stmt;
		String balance = "";
		try{
			stmt =  _connection.createStatement();
			stmt.executeUpdate(INSERT_ACCOUNTS);
			stmt.executeUpdate(INSERT_POCKET);

			String r = topUp(id, initialTopUp);
			balance = getAccountBalance(id);
			return "0 " + id + " POCKET " + balance + " " + tin;
		}
		catch(SQLException e){
			System.err.print(e.getMessage());
			return "0 " + id + " POCKET " + balance + " " + tin;
		}
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
	//good
	@Override
	public String deposit( String accountId, double amount ) {
		String t = getAccountType(accountId);
		// check account type
		if(t=="POCKET") {
			System.out.print("Transaction not valid on Pocket account");
			return "1";
		}
		// perform deposit
		Statement stmt;
		String oldBalance = getAccountBalance(accountId);
		String newBalance = oldBalance;
		String r = editAccountBalance(accountId, amount);

		if(r.equals("1"))
			return "1 " + oldBalance + " " + newBalance;

		createTransaction("deposit", amount, accountId,"-1");

		newBalance = getAccountBalance(accountId);
		return "0 " + oldBalance + " " + newBalance;
	}

	/**
	 * Show an account balance (regardless of type of account).
	 * @param accountId Account ID.
	 * @return a string "r balance", where
	 *         r = 0 for success, 1 for error; and
	 *         balance is the account balance, with up to 2 decimal places (e.g. with %.2f).
	 */
	//good
	@Override
	public String showBalance( String accountId ) {
		String balance = getAccountBalance(accountId);
		if(balance.equals("1")) {
			return "1";
		} else {
			return "0 "+balance;
		}
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
	//good
	@Override
	public String payFriend( String from, String to, double amount ) {
		// check that from, to are pocket accounts
		String FROM_TYPE_QUERY = "SELECT A.atype FROM Accounts A WHERE A.aid="+from;
		String TO_TYPE_QUERY = "SELECT A.atype FROM Accounts A WHERE A.aid="+to;
		String fromNewBalance=getAccountBalance(from);
		String toNewBalance=getAccountBalance(to);
		Statement stmt;
		ResultSet rs;
		try{
			// check that both accounts are pocket accounts
			stmt=_connection.createStatement();
			rs = stmt.executeQuery(FROM_TYPE_QUERY);
			if(!rs.next() || !rs.getString("atype").trim().equals("POCKET")) {
				System.out.println("Error: both accounts must be existing Pocket accounts");
				return "1 "+fromNewBalance+" "+toNewBalance;
			}

			rs = stmt.executeQuery(TO_TYPE_QUERY);
			if(!rs.next() || !rs.getString("atype").trim().equals("POCKET")) {
				System.out.println("Error: both accounts must be existing Pocket accounts");
				return "1 "+fromNewBalance+" "+toNewBalance;
			}

			// edit account balances
			String r = editAccountBalance(from, amount*-1);
			if(r.equals("1")) {
				System.out.print("Error: insufficient funds in account.");
				return "1 "+fromNewBalance+" "+toNewBalance;
			}
			editAccountBalance(to, amount);

			fromNewBalance = getAccountBalance(from);
			toNewBalance = getAccountBalance(to);

			// create Transaction
			createTransaction("pay-friend", amount, from, to);
			return "0 "+fromNewBalance+" "+toNewBalance;
		}catch(SQLException e) {
			System.err.print(e.getMessage());
			return "1 "+fromNewBalance+" "+toNewBalance;
		}
	}

	/**
	 * Generate list of closed accounts.
	 * @return a string "r id1 id2 ... idn", where
	 *         r = 0 for success, 1 for error; and
	 *         id1 id2 ... idn is a list of space-separated closed account IDs.
	 */
	@Override
	//good
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

	///////////////////////////////////// Additional Transaction Functions /////////////////////////////////////////////
	/**
	 * Subtract to the checking or savings account balance
	 * @return a string r = "0" for success, "1" for error
	 */
	//good
	public String withdrawal(String aid, double amount) {
		if(getAccountType(aid).equals("POCKET") || getAccountType(aid).equals("1")) {
			return "1";
		}
		String r = editAccountBalance(aid, amount*-1);
		if(r.equals("1")) {
			System.out.print("Error: insufficient funds in account.");
			return "1";
		}
		createTransaction("withdrawal",amount*-1,aid,"-1");
		return "0";
	}

	/**
	 * Subtract money from the pocket account balance
	 * @return a string r="0" for success, "1" for error
	 */
	//good
	public String purchase(String aid, double amount) {
		String t = getAccountType(aid);
		if(!t.equals("POCKET")) return "1";
		editAccountBalance(aid,amount*-1);
		createTransaction("purchase",amount*-1,aid,"-1");
		return "0";
	}

	/**
	 * Subtract money from one account w/ aid and add it to another account w/ aid2.
	 * A transfer can only occur between two accounts that have at least one
	 * owner in common. If the transfer was requested by a customer, she or he
	 * must be an owner of both accounts. Amount < $2000.
	 * @param aid checking or savings account
	 * @param aid2 checking or savings account
	 * @param amount amount to be transferred
	 *
	 * @return a string r="0" for success, "1" for error
	 */
	//good
	public String transfer(String aid, String aid2, double amount, boolean customer) {
		// amount must be less than $2000
		if(amount>2000) {
			System.out.print("Error: cannot transfer more than $2000 in one transaction.");
			return "1";
		}
		// both accounts must be checking/savings accounts
		if(getAccountType(aid).equals("POCKET") || getAccountType(aid2).equals("POCKET")) {
			System.out.print("Error: cannot perform a transfer with a pocket account.");
			return "1";
		}
		// check that accounts have at least one owner in common
		String CUSTOMER_AT_LEAST_ONE_OWNER = "SELECT DISTINCT O.taxid " +
				       "FROM Owners O, Owners O2 " +
				       "WHERE O.aid=" + aid +
				 	   " AND O2.aid=" + aid2 +
				       " AND O.taxid = O2.taxid" +
					   " AND O.taxid=" + currentCustomerTid;

		String AT_LEAST_ONE_OWNER
				=
				"SELECT DISTINCT O.taxid " +
				"FROM Owners O, Owners O2 " +
				"WHERE O.aid=" + aid +
				" AND O2.aid=" + aid2 +
				" AND O.taxid = O2.taxid";

		Statement stmt;
		ResultSet rs;
		try {
			stmt=_connection.createStatement();
			if(customer){
				rs = stmt.executeQuery(CUSTOMER_AT_LEAST_ONE_OWNER);
				if(!rs.next()) {
					System.out.print("Error: must be owner of both accounts and customer must be owner to perform a transfer.");
					return "1";
				}
			} else {
				rs = stmt.executeQuery(AT_LEAST_ONE_OWNER);
				if(!rs.next()) {
					System.out.print("Error: must be owner of both accounts to perform a transfer.");
					return "1";
				}
			}
		} catch(SQLException e) {
			System.err.print(e.getMessage());
			return "1";
		}
		// perform transfer
		editAccountBalance(aid, amount*-1);
		editAccountBalance(aid2, amount);
		createTransaction("transfer",amount,aid,aid2);
		return "0";
	}

	// NEEDS TESTING
	/**
	 * Move amount of money from account w/ aid back to linked account w/ aid2.
	 * There is a 3% fee for this action.
	 * @param aid pocket account
	 * @param aid2 linked checking or savings account
	 * @param amount amount to be collected (incurs 3% fee)
	 * @return a string r="0" for success, "1" for error
	 */
	//collect
	public String collect(String aid, String aid2, double amount) {
		Statement stmt;
		ResultSet rs;
		double fromBalance;
		double fee;
		String checkLink = "SELECT P.aid FROM Pocket P WHERE P.aid="+aid+" AND P.aid2="+aid2;
		// check account types
		if(!getAccountType(aid).equals("POCKET") || getAccountType(aid2).equals("POCKET")) {
			System.out.print("Error: transaction must be between a pocket account and it's linked checking or savings account. ");
			return "1";
		}
		try {
			// check that accounts are linked
			stmt = _connection.createStatement();
			rs = stmt.executeQuery(checkLink);
			if(!rs.next()) {
				System.out.print("Error: transaction must be between a pocket account and it's linked checking or savings account. ");
				return "1";
			}
		} catch(SQLException e) {
			System.err.print(e.getMessage());
			return "-1";
		}
		// update account balances and create transaction
		fee = 0.03 * amount;
		editAccountBalance(aid,  - amount - fee);

		String r = getAccountBalance(aid);
		System.out.println("53027: " + r);

		editAccountBalance(aid2, amount);
		createTransaction("collect",amount,aid,aid2);
		return "0";
	}

	// NEEDS TESTING
	/**
	 * subtract money from account w aid and add it to another. The customer that
	 * requests this action must be an owner of account w aid. There is a 2% fee
	 * for this action.
	 * @param aid savings or checking account
	 * @param aid2 another checking or savings account
	 * @param amount amount to be collected (incurs a 3% fee)
	 * @return a string r="0" for success, "1" for error
	 */
	public String wire(String aid, String aid2, double amount) {
		// check that customer is owner of account with aid
		String query = "SELECT O.taxid " +
				"FROM Owners O " +
				"WHERE O.aid=" + aid +
				" AND O.taxid =" + currentCustomerTid;
		Statement stmt;
		ResultSet rs;
		try {
			stmt=_connection.createStatement();
			rs = stmt.executeQuery(query);
			if(!rs.next()) {
				System.out.print("Error: must be owner of account to wire money.");
				return "1";
			}
		} catch(SQLException e) {
			System.err.print(e.getMessage());
			return "1";
		}
		// check account types
		if(getAccountType(aid)=="POCKET" || getAccountType(aid2)=="POCKET") {
			System.out.print("Error: transaction must be between checking/savings accounts. ");
			return "1";
		}
		// update account balances and create transaction
		editAccountBalance(aid, -1*(amount+amount*0.2));
		editAccountBalance(aid2, amount);
		createTransaction("wire",amount,aid,aid2);
		return "0";
	}

	// NEEDS TESTING
	/**
	 * Subtract money from the checking account w aid. Associated with a check
	 * is a check number.
	 * @param aid checking account
	 * @param amount amount
	 * @return a string r="0" for success, "1" for error
	 */
	public String writeCheck(String aid, double amount) {
		// check accountTypes
		if(getAccountType(aid)!="STUDENT_CHECKING" && getAccountType(aid)!="INTEREST_CHECKING") {
			System.out.print("Error: checks can only be written from checking accounts\n");
			return "1";
		}
		// edit account balance, create transaction
		editAccountBalance(aid, amount);
		createTransaction("writeCheck",amount,aid,"-1");
		// update WriteCheck table
		String update = "INSERT INTO WriteCheck(checkno, tid) VALUES ("+transactionId+","+transactionId+")";
		Statement stmt;
		try{
			stmt=_connection.createStatement();
			stmt.executeUpdate(update);
		}catch(SQLException e){
			System.err.print(e.getMessage());
			return "1";
		}
		return "0";
	}

	// NEEDS TESTING
	/**
	 * Add money to the checking or savings account. The amount added is the
	 * monthly interest rate times the average daily balance for the month.
	 * Interest is added at the end of each month.
	 * @return a string r="0" for success, "1" for error
	 */
	public String accrueInterest(String aid) {
		double balance = Double.valueOf(getAccountBalance(aid));
		double interest = getInterest(aid);
		balance += balance*interest;
		String update = "UPDATE Accounts" +
						"SET balance=" + balance +
						" WHERE aid=" + aid;
		Statement stmt;
		try {
			stmt=_connection.createStatement();
			stmt.executeUpdate(update);
		} catch(SQLException e) {
			System.err.print(e.getMessage());
			return "1";
		}
		return "0";
	}

	////////////////////////// Additional Bank Teller Functions ////////////////////////////////////////////////////////

	// NEEDS TESTING
	/**
	 * Submit a check transaction for an account
	 * @return a string r="0" for success, "1" for error
	 */
	public String enterCheckTransaction(String aid, double amount) {
		String r = writeCheck(aid, amount);
		if(r.equals("0")) return "0";
		return "1";
	}

	// RLLY SHOULD B TESTED :0
	/**
	 * Given a customer, do the following for each account she owns (including closed accounts):
	 * (1) List names and addresses of all owners of the account.
	 * (2) generate a list of all transactions which have occurred in the last month.
	 * (3) List initial and final account balance.
	 * (4) If the sum of the balances of the accounts of which the customer is the primary owner
	 *     exceeds $100,000, include a warning that the limit of insurance has been reached.
	 * @param taxId customer's taxId
	 * @return a string r="0" for success, "1" for error
	 */
	public String generateMonthlyStatement(String taxId) {
		System.out.println("Generating monthly statement...");
		// Find each account associated with aid
		String query = "SELECT A.aid FROM Accounts A WHERE A.aid IN (SELECT O.aid FROM Owners O WHERE O.taxid="+taxId;
		Statement stmt;
		ResultSet rs;
		ResultSet coOwners;
		ResultSet transactions;
		ResultSet initBal;
		String currentAid;
		double totalFunds = 0.0;
		double initialBalance=0.0;
		try {
			stmt=_connection.createStatement();
			rs=stmt.executeQuery(query);
			while(rs.next()) {
				currentAid=Integer.toString(rs.getInt("aid"));
				System.out.println("ACCOUNT "+currentAid);
				// (1) List names and addresses of all owners
				System.out.println("  Co-owners, Co-owner's Address");
				query = "SELECT C.cname, C.address FROM Customers C WHERE C.taxid IN (SELECT O.taxid FROM Owners O WHERE O.aid="+currentAid+")";
				coOwners =stmt.executeQuery(query);
				while(coOwners.next()) {
					System.out.println("    "+coOwners.getString("cname")+coOwners.getString("address"));
				}
				// (2) List all transactions
				query = "SELECT T.amount,T.ttype, T.date FROM Transactions T WHERE T.aid="+currentAid + "ORDER BY T.date ASC";
				System.out.println("  Transactions (date   type   $amount");
				transactions = stmt.executeQuery(query);
				while(transactions.next()) {
					System.out.println("    " + transactions.getDate("date") +
											"   " + transactions.getString("ttype") +
											"    $" + transactions.getDouble("amount"));
				}
				// (3) List initial and final account balance
				query = "SELECT T.amount,T.ttype, T.date" +
						" FROM Transactions T " +
						"WHERE (T.aid="+currentAid + ") AND " +
							  "(T.date= (SELECT MIN(T2.date) FROM Transactions T2)";
				initBal = stmt.executeQuery(query);
				while(initBal.next()){
					initialBalance = initBal.getDouble("amount");
				}
				System.out.println("  Initial balance");
				System.out.println("    " + initialBalance);
				System.out.println("  Final balance");
				System.out.println("    " + getAccountBalance(rs.getString("aid")));
				System.out.println();
				totalFunds+=Integer.parseInt(getAccountBalance(rs.getString("aid")));
			}
			if(totalFunds>100000){
				System.out.println("WARNING: You have reached the limit of insurance");
			}
		}catch(SQLException e){
			System.out.print(e.getMessage());
			return "1";
		}
		return "0";
	}

	// NEEDS TESTING
	/**
	 * Generate a list of all customers which have a sum of deposits, transfers, and wires
	 * during the current month, over all owned accounts (active or closed) of over $10,000.
	 * @return a string r="0" for success, "1" for error
	 */
	public String generateDTER() {
		String query ="";
		// SELECT C.taxid
		Statement stmt;
		ResultSet rs;
		try{
			stmt=_connection.createStatement();
			rs = stmt.executeQuery(query);
		}catch(SQLException e){
			System.err.print(e.getMessage());
			return "1";
		}
		return "0";
	}

	// NEEDS TESTING
	/**
	 * Generate a list of all accounts associated with a particular customer and indicate
	 * whether the accounts are open or closed
	 * @return a string r="0" for success, "1" for error
	 */
	public String generateCustomerReport(String aid) {
		String customerReport = "";
		String query = "SELECT A.aid, A.status" +
					   "FROM Accounts A" +
					   "WHERE A.aid IN" +
					   "(SELECT O.aid FROM Owners O WHERE O.aid="+aid+")";
		Statement stmt;
		ResultSet rs;
		int accId;
		String status;
		try{
			stmt=_connection.createStatement();
			rs=stmt.executeQuery(query);
			while(!rs.next()) {
				accId = rs.getInt("aid");
				status = rs.getString("status");
				customerReport+= accId+"         "+status+"\n";
			}
		}catch(SQLException e){
			System.err.print(e.getMessage());
			return "0";
		}
		if(customerReport.equals("")) {
			System.out.print("This customer owns no accounts");
			return "1";
		}
		customerReport = "account ID    status\n"+customerReport;
		System.out.print(customerReport);
		return "1";
	}

	// NEEDS TESTING
	/**
	 * For all open accounts, add the appropriate amount of monthly interest to the balance.
	 * If interest has already been added for the month, report a warning.
	 * @return a string r="0" for success, "1" for error/warning
	 */
	public String addInterest() {
		if(addedInterest) {
			System.out.print("Error: Interest has already been added this month");
			return "1";
		}
		String query = "SELECT A.aid FROM Accounts A WHERE A.status='open' AND A.atype<>'POCKET'";
		Statement stmt;
		ResultSet rs;
		int aid;
		try{
			stmt = _connection.createStatement();
			rs=stmt.executeQuery(query);
			while(!rs.next()) {
				aid = rs.getInt("aid");
				accrueInterest(Integer.toString(aid));
			}
		}catch(SQLException e){
			System.err.print(e.getMessage());
			return "1";
		}
		return "0";
	}

	// NEEDS TESTING
	/**
	 * delete closed accounts and remove all customers who do not own any accounts
	 * @return a string r="0" for success, "1" for error/warning
	 */
	public String deleteClosedAccounts(){
		// delete closed accounts
		String deletion = "DELETE FROM Accounts WHERE A.status='closed'";
		Statement stmt;
		try{
			stmt=_connection.createStatement();
			stmt.executeUpdate(deletion);
		}catch(SQLException e){
			System.err.print(e.getMessage());
			return "1";
		}
		// find customers who do not own any accounts
		ResultSet rs;
		String query = "SELECT C.taxid FROM Customers C WHERE C.taxid NOT IN (SELECT O.taxid FROM Owners O)";
		try{
			stmt=_connection.createStatement();
			rs=stmt.executeQuery(query);
			while(!rs.next()) {
				deletion = "DELETE FROM Customers C WHERE C.taxid="+Integer.toString(rs.getInt("taxid"));
				stmt.executeUpdate(deletion);
			}
		}catch(SQLException e){
			System.err.print(e.getMessage());
			return "1";
		}
		return "0";
	}

	// NEEDS TESTING
	/**
	 * delete the list of transactions from each of the accounts in preparation of
	 * a new month of processing.
	 * @return a string r="0" for success, "1" for error/warning
	 */
	public String deleteTransactions() {
		String update = "DELETE FROM Transactions";
		Statement stmt;
		try{
			stmt=_connection.createStatement();
			stmt.executeUpdate(update);
			transactionId=1;
			return "0";
		}catch(SQLException e){
			System.err.print(e.getMessage());
			return "1";
		}
	}

	///////////////////////////////////// Helper Functions /////////////////////////////////////////////////////////////


	/**
	 * Add Transactions entry and TwoSided entry (if needed) to db
	 * @param ttype type of transaction
	 * @param amount dollar amount
	 * @param aid account id initiating transaction
	 * @param aid2 for two sided transactions, "-1" otherwise
	 * @return a string "r", where r=0 for success, 1 for error
	 */
	//good
	public String createTransaction(String ttype, double amount, String aid, String aid2) {
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

		String transactionUpdate = "INSERT INTO Transactions(ttype, amount,tdate,tid,aid) VALUES ("
									+ "'" + ttype + "'" + ","
									+ amount + ","
									+ "DATE '" + DATE_FORMAT.format(sysDate) + "',"
									+ transactionId + ","
								    + aid + ")";

		String FIRST_TRANSACTION = "SELECT * FROM Transactions T WHERE T.aid="+aid;
		double feeBalance;
		double pocketBalance;
		Statement stmt;
		ResultSet rs;
		try {
			stmt = _connection.createStatement();

			// if a pocketaccountthe first transaction of the month incurs a $5 fee
			if(getAccountType(aid).equals("POCKET")){
				rs = stmt.executeQuery(FIRST_TRANSACTION);
				if(!rs.next()){
					pocketBalance = Double.parseDouble(getAccountBalance(aid));
					feeBalance = pocketBalance-5.0;
					String UPDATE_FEE = "UPDATE Accounts " +
							"SET balance = " + feeBalance +
							" WHERE aid = " + aid;
					stmt.executeUpdate(UPDATE_FEE);
				}
			}

			stmt.executeUpdate(transactionUpdate);

			//two sided transaction
			if(aid2!="-1") {
				String twoSidedUpdate = "INSERT INTO TwoSided(aid, tid) VALUES ("
						+ aid2 + ","
						+ transactionId + ")";

				stmt.executeUpdate(twoSidedUpdate);
			}
			transactionId++;
			return "0";
		}catch(SQLException e){
			System.err.print(e.getMessage());
			return "1";
		}
	}

	/**
	 * Add or subtract a given amount from account balance
	 * close account if balance <=.01 but >0
	 * fail if balance <0 after transaction
	 * @param aid account id
	 * @param amount negative to subtract, positive to add
	 * @return a string "r", where r=0 if success, 1 for error
	 */
	//good
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
			while(rs.next()){
				balance = rs.getDouble("balance");
			}
			balance += amount;
			if (balance < 0) {
				return "1";
			}
			// close account, update balance
			else if (balance <= .01) {
				updateAccount =
						"UPDATE Accounts " +
						"SET balance = " + balance + ", status = 'closed'" +
						" WHERE aid = " + aid;
				stmt.executeUpdate(updateAccount);
			}
			else if (balance > .01) {
				updateAccount =
						"UPDATE Accounts " +
						"SET balance = " + balance +
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
	 * @return account balance as a string, or "1" for error
	 */
	//good
	public String getAccountBalance(String aid) {
		Statement stmt;
		ResultSet rs;
		String getBalance = "SELECT A.balance FROM Accounts A WHERE A.aid="+aid;
		double balance = 0.0;
		try {
			stmt = _connection.createStatement();
			rs = stmt.executeQuery(getBalance);
			while(rs.next()) {
				balance = rs.getDouble("balance");
			}
			return(Double.toString(balance));
		} catch(SQLException e) {
			System.err.print(e.getMessage());
			return "1";
		}
	}

	/**
	 * get account type
	 * @return AccountType as a string, or "1" for error
	 */
	//good
	public String getAccountType(String aid) {
		Statement stmt;
		ResultSet rs;
		String query = "SELECT A.atype FROM Accounts A WHERE A.aid="+aid;
		String accountType = "";
		try{
			stmt = _connection.createStatement();
			rs = stmt.executeQuery(query);
			while(rs.next()){
				accountType = rs.getString("atype").trim();
			}
			return accountType;
		}catch(SQLException e){
			System.err.print(e.getMessage());
			return "1";
		}
	}

	// NOT TESTED
	/**
	 * get interest rate for a given account
	 * @return a double r = interest if success, 0.0 otherwise
	 */
	public Double getInterest(String aid) {
		Statement stmt;
		ResultSet rs;
		String query = "SELECT A.interest FROM Accounts A WHERE A.aid="+aid;
		double interest = 0.0;
		try{
			stmt=_connection.createStatement();
			rs=stmt.executeQuery(query);
			while(rs.next()) {
				interest = rs.getDouble("interest");
			}
		}catch(SQLException e){
				System.err.print(e.getMessage());
				return 0.0;
		}
		return interest;
	}

	///////////////////////////////////Populate Functions///////////////////////////////////////////////////////////////

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
				"VALUES ('STUDENT_CHECKING','open','San Francisco',0,3.0,17431,344151573)";
		final String b = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('STUDENT_CHECKING','open','Los Angeles',0,3.0,54321,212431965)";
		final String c = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('STUDENT_CHECKING','open','Goleta',0,3.0,12121,207843218)";
		final String d = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('INTEREST_CHECKING','open','Los Angeles',0,3.0,41725,201674933)";
		final String e = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('INTEREST_CHECKING','open','Santa Barbara',0,3.0,76543,212116070)";
		final String f = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('INTEREST_CHECKING','open','Goleta',0,3.0,93156,209378521)";
		final String g = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('SAVINGS','open','Santa Barbara',0,4.8,43942,361721022)";
		final String h = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('SAVINGS','open','Los Angeles',0,4.8,29107,209378521)";
		final String i = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('SAVINGS','open','San Francisco',0,4.8,19023,412231856)";
		final String j = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('SAVINGS','open','Goleta',0,4.8,32156,188212217)";
		final String k = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('POCKET','open','Goleta',0,0,53027,207843218)";
		final String l = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('POCKET','open','Isla Vista',0,0,43947,212116070)";
		final String m = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('POCKET','open','Santa Cruz',0,0,60413,400651982)";
		final String n = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid)\n" +
				"VALUES ('POCKET','open','Santa Barbara',0,0,67521,401605312)";

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

	//aid is the pocket
	//aid2 is the linked account
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
