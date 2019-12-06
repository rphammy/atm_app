package cs174a;                         // THE BASE PACKAGE FOR YOUR APP MUST BE THIS ONE.  But you may add subpackages.

// DO NOT REMOVE THIS IMPORT.
import cs174a.Testable.*;
import java.util.Scanner;

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
			Scanner scanner = new Scanner(System.in);
			boolean runApp = true;
			while(runApp) {

				System.out.println("---------HOME PAGE---------\n" +
							       "Choose one of the following:\n" +
							       "0 Bank Teller\n" +
								   "1 Customer ATM\n" +
						           "2 Set System Date\n" +
								   "3 Exit Program");

				String initialChoice = scanner.nextLine();
				switch(initialChoice) {

					// bank teller
					case "0":
						boolean bankTelling = true;
						while(bankTelling) {
							System.out.println("Choose one of the following:\n" +
									"0 Enter Check Transaction\n" +
									"1 Generate Monthly Statement\n" +
									"2 List Closed Accounts\n" +
									"3 Generate Government DTER\n" +
									"4 Customer Report\n" +
									"5 Add interest\n" +
									"6 Create Account\n" +
									"7 Delete Closed Accounts and Customers\n" +
									"8 Delete Transactions\n" +
									"9 GO BACK TO HOME PAGE");
							String bankTellerChoice = scanner.nextLine();
							switch (bankTellerChoice) {
								case "1":
									break;
								case "2":
									break;
								case "3":
									break;
								case "4":
									break;
								case "5":
									break;
								case "6":
									break;
								case "7":
									break;
								case "8":
									break;
								case "9":
									bankTelling=false;
									break;
								default:
									System.out.println("Must choose between choices 0-9\n");
									break;
							}
						}
						break;

					// customer atm
					case "1":
						boolean usingATM = true;
						while(usingATM) {
							System.out.println("Choose one of the following:\n" +
									"0 Deposit\n" +
									"1 Top-Up\n" +
									"2 Withdrawal\n" +
									"3 Purchase\n" +
									"4 Transfer\n" +
									"5 Collect\n" +
									"6 Pay-Friend\n" +
									"7 Wire\n" +
									"8 LOG OUT AND GO BACK TO HOME PAGE");
							String customerChoice = scanner.nextLine();
							switch (customerChoice) {
								case "0":
									break;
								case "1":
									break;
								case "2":
									break;
								case "3":
									break;
								case "4":
									break;
								case "5":
									break;
								case "6":
									break;
								case "7":
									break;
								case "8":
									usingATM = false;
									break;
								default:
									System.out.println("Must choose between choices 0-8\n");
									break;
							}
						}
						break;

					// set sys date
					case "2":
						System.out.println("Enter the year(yyyy), month(mm), and day(dd) on separate lines");
						int year = Integer.parseInt(scanner.nextLine());
						int month = Integer.parseInt(scanner.nextLine());
						int day = Integer.parseInt(scanner.nextLine());
						app.setDate(year, month, day);
						break;

					// exit program
					case "3":
						runApp=false;
						break;

					default:
						System.out.print("Must choose between choices 0-3\n");
						break;
				}
			}

			// Our tests



		}
	}
	//!### FINALIZAMOS
}
