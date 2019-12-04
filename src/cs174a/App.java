package cs174a;                                             // THE BASE PACKAGE FOR YOUR APP MUST BE THIS ONE.  But you may add subpackages.

// You may have as many imports as you need.
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.OracleConnection;

/**
 * The most important class for your application.
 * DO NOT CHANGE ITS SIGNATURE.
 */
public class App implements Testable
{
	private OracleConnection _connection;                   // Example connection object to your DB.

	/**
	 * Default constructor.
	 * DO NOT REMOVE.
	 */
	App()
	{
		// TODO: Any actions you need.
	}

	/**
	 * This is an example access operation to the DB.
	 */
	void exampleAccessToDB()
	{
		// Statement and ResultSet are AutoCloseable and closed automatically.
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select owner, table_name from all_tables" ) )
			{
				while( resultSet.next() )
					System.out.println( resultSet.getString( 1 ) + " " + resultSet.getString( 2 ) + " " );
			}
		}
		catch( SQLException e )
		{
			System.err.println( e.getMessage() );
		}
	}

	////////////////////////////// Implement all of the methods given in the interface /////////////////////////////////
	// Check the Testable.java interface for the function signatures and descriptions.

	@Override
	public String initializeSystem()
	{
		// Some constants to connect to your DB.
		final String DB_URL = "jdbc:oracle:thin:@cs174a.cs.ucsb.edu:1521/orcl";
		final String DB_USER = "c##romanova";
		final String DB_PASSWORD = "4477352";

		// Initialize your system.  Probably setting up the DB connection.
		Properties info = new Properties();
		info.put( OracleConnection.CONNECTION_PROPERTY_USER_NAME, DB_USER );
		info.put( OracleConnection.CONNECTION_PROPERTY_PASSWORD, DB_PASSWORD );
		info.put( OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20" );

		try
		{
			OracleDataSource ods = new OracleDataSource();
			ods.setURL( DB_URL );
			ods.setConnectionProperties( info );
			_connection = (OracleConnection) ods.getConnection();

			// Get the JDBC driver name and version.
			DatabaseMetaData dbmd = _connection.getMetaData();
			System.out.println( "Driver Name: " + dbmd.getDriverName() );
			System.out.println( "Driver Version: " + dbmd.getDriverVersion() );

			// Print some connection properties.
			System.out.println( "Default Row Prefetch Value is: " + _connection.getDefaultRowPrefetch() );
			System.out.println( "Database Username is: " + _connection.getUserName() );
			System.out.println();

			return "0";
		}
		catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
	}

	@Override
	public String dropTables() {
		Statement stmt = null;
		try{
			String[] dropStatements = new String[]{"DROP TABLE Pocket","DROP TABLE TwoSided","DROP TABLE Owners","DROP TABLE WriteCheck","DROP TABLE Transactions","DROP TABLE Accounts","DROP TABLE Customers"};
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
	 * Example of one of the testable functions.
	 */
	@Override
	public String listClosedAccounts()
	{
		return "0 it works!";
	}

	/**
	 * Another example.
	 */
	@Override
	public String createCheckingSavingsAccount( AccountType accountType, String id, double initialBalance, String tin, String name, String address )
	{
		return "0 " + id + " " + accountType + " " + initialBalance + " " + tin;
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
				+ "taxid INT NOT NULL,"
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
}
