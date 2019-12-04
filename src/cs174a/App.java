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

/**
 * The most important class for your application.
 * DO NOT CHANGE ITS SIGNATURE.
 */
public class App implements Testable {
	private OracleConnection _connection;                   // Example connection object to your DB.
	Date sysDate;
	String bankName = "Bank of Nuts";

	/**
	 * Default constructor.
	 * DO NOT REMOVE.
	 */
	App() {
		sysDate = new Date(2011, 3, 1);
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
			//inset customer data
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
		String newDate = year + "-" + month + "-" + day
		try {
			sysDate = new Date(year, month, day);
			return "0" + newDate;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return "1" + newDate;
		}
	}

	@Override
	public String listClosedAccounts() {
		return "0 it works!";
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
		Statement stmt;
		String customerLookupQuery = "SELECT * FROM Customers C WHERE C.taxid=" + tin;
		String createAccountQuery = "INSERT INTO Accounts(atype,status,bankname,balance,interest,aid,taxid) VALUES"+
									"('"+AccountType+"','open','"+bankName+"',"+initialBalance+""
		try {
			stmt = _connection.createStatement();
			ResultSet rs = stmt.executeQuery(customerLookupQuery);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			return "1 " + id + " " + accountType + " " + initialBalance + " " + tin;
		}
		// customer does not exist
		if (rs.next() == false) {
			createCustomer(id,tin,name,address);
			try {
				stmt = _connection.createStatement();
				stmt.executeUpdate(createAccountQuery);
			}catch(SQLException e){
				System.err.print(e.getMessage());
				return "1 " + id + " " + accountType + " " + initialBalance + " " + tin;
			}
		// customer does exist
		} else {

		}
			//Read more: https://javarevisited.blogspot.com/2016/10/how-to-check-if-resultset-is-empty-in-Java-JDBC.html#ixzz676IBHOax

			return "0 " + id + " " + accountType + " " + initialBalance + " " + tin;
	}
}
