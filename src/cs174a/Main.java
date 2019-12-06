// WE LOVE GITHUB
package cs174a;                         // THE BASE PACKAGE FOR YOUR APP MUST BE THIS ONE.  But you may add subpackages.

// DO NOT REMOVE THIS IMPORT.
import cs174a.Testable.*;

/**
 * This is the class that launches your application.
 * DO NOT CHANGE ITS NAME.
 * DO NOT MOVE TO ANY OTHER (SUB)PACKAGE.
 * There's only one "main" method, it should be defined within this Main class, and its signature should not be changed.
 */
public class Main
{
	/**
	 * Program entry point.
	 * DO NOT CHANGE ITS NAME.
	 * DON'T CHANGE THE //!### TAGS EITHER.  If you delete them your program won't run our tests.
	 * No other function should be enclosed by the //!### tags.
	 */
	//!### COMENZAMOS
	public static void main( String[] args )
	{
		App app = new App();                        // We need the default constructor of your App implementation.  Make sure such
													// constructor exists.
		String r = app.initializeSystem();          // We'll always call this function before testing your system.
		if( r.equals( "0" ) )
		{
			// Our tests

//            r = app.dropTables();
//			System.out.println(r);
//
//            r = app.createTables();
//			System.out.println(r);
//
//			app.populateCustomerData();
//			app.populateAccountData();
//			app.populateOwnersData();
//			app.populatePocketData();
//
//			r = app.deposit("17431", 10);
//			System.out.println(r);
//
//			r = app.deposit("17431", 10);
//			System.out.println(r);

			r = app.createCheckingSavingsAccount(AccountType.INTEREST_CHECKING, "11111", 20.0, "361721022", "Alfred Hitchcock", "6667 El Colegio #40");
			System.out.println(r);


//			r = app.createTransaction("INTEREST_CHECKING", 10, "17431", "-1");
//			System.out.println(r);



//			r = app.createCheckingSavingsAccount( AccountType.INTEREST_CHECKING, "34567", 1234.56, "1928304", "Im YoungMing", "Known" );
//			System.out.println( r );
//
//			// Our tests
//			app.createCheckingSavingsAccount(AccountType.SAVINGS, "12345", 1500,"123456789","sasha","6835 Pasado Rd");
//			System.out.println( r );

		}
	}
	//!### FINALIZAMOS
}
