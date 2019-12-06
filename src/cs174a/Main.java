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

		app.dropTables();
		app.createTables();
		app.populateCustomerData();
		app.populateAccountData();
		app.populateOwnersData();
		app.populatePocketData();

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
							String accountId = "";
							String taxId = "";
							switch (bankTellerChoice) {
								// writeCheck
								case "0":
									System.out.println("To enter a check transaction, enter checking account number and amount on check on separate lines:");
									accountId = scanner.nextLine();

									double amount = Double.parseDouble(scanner.nextLine());
									r = app.writeCheck(accountId, amount);
									if (r.equals("1")) {
										System.out.println("Check transaction failed");
									} else {
										System.out.println("Successfully entered transaction");
									}
									break;
								// genMonthlyStatement
								case "1":
									System.out.println("To generate monthly report, enter customer tax id:");
									taxId = scanner.nextLine();
									app.generateMonthlyStatement(taxId);
									break;
								// listClosedAccounts
								case "2":
									System.out.println("Account IDs of closed accounts");
									String str[] = app.listClosedAccounts().split(" ");
									if (str[0].equals("0")) {
										for (int i = 1; i < str.length; i++) {
											System.out.println(str[i]);
										}
									} else {
										System.out.println("Listing closed accounts failed");
									}
									break;
								// genGovDTER
								case "3":
									System.out.println("Not implemented yet :(");
									break;
								// genCustomerReport
								case "4":
									System.out.println("To generate customer report, enter tax id:");
									taxId = scanner.nextLine();
									app.generateCustomerReport(taxId);
									break;
								// addInterest
								case "5":
									r = app.addInterest();
									if(r.equals("0")) {
										System.out.println("Interest added.");
									}
									break;
								// createAccount
								case "6":
									boolean choosingType = true;
									System.out.println("Choose account type to create:\n" +
											"0 Student Checking\n" +
											"1 Interest Checking\n" +
											"2 Savings\n" +
											"3 Pocket");
									String choice = scanner.nextLine();
									String id;
									String linkedId = "";
									double initialTopUp;
									double initialBalance;
									String tin;
									String name = "";
									String address = "";
									AccountType aType = null;
									switch (choice) {
										case "0":
											aType = AccountType.STUDENT_CHECKING;
											break;
										case "1":
											aType = AccountType.INTEREST_CHECKING;
											break;
										case "2":
											aType = AccountType.SAVINGS;
											break;
										case "3":
											aType = AccountType.POCKET;
											break;
										default:
											break;
									}
									if (aType != AccountType.POCKET) {
										System.out.println("Enter new account ID:");
										id = scanner.nextLine();
										System.out.println("Enter initial balance:");
										initialBalance = Double.parseDouble(scanner.nextLine());
										System.out.println("Enter customer taxid:");
										tin = scanner.nextLine();
										System.out.println("For existing customer, press enter to create account," +
												"For new customer, enter name, address on separate lines:");
										name = scanner.nextLine();
										address = scanner.nextLine();
										app.createCheckingSavingsAccount(aType, id, initialBalance, tin, name, address);
									} else if (aType.equals(AccountType.POCKET)) {
										System.out.println("Enter new pocket account ID:");
										id = scanner.nextLine();
										System.out.println("Enter checking/savings account ID to link to:");
										linkedId = scanner.nextLine();
										System.out.println("Enter initial top-up amount:");
										initialTopUp = Double.parseDouble(scanner.nextLine());
										System.out.println("Enter customer taxid:");
										tin = scanner.nextLine();
										app.createPocketAccount(id, linkedId, initialTopUp, tin);
									}
									break;
								// delete closed accounts
								case "7":
									System.out.println("deleting closed accounts...");
									r = app.deleteClosedAccounts();
									if (r.equals("0")) {
										System.out.println("closed accounts deleted.");
									}
									break;
								// delete transactions
								case "8":
									// NEED to check last day of month
									System.out.println("deleting last month's transactions...");
									r=app.deleteTransactions();
									if(r.equals("0")) {
										System.out.println("last month's transactions deleted.");
									}
									break;
								// go to home page
								case "9":
									bankTelling = false;
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
						boolean loggedIn = false;
						int logInPIN = 0;
						while (usingATM) {
							if(!loggedIn) {
								System.out.println("Enter your PIN to continue:");
								logInPIN = Integer.parseInt(scanner.nextLine());
								if(app.verifyPin(logInPIN)) {
									loggedIn=true;
								} else {
									System.out.println("Incorrect PIN");
									usingATM = false;
								}
							}
							if(loggedIn) {
								System.out.println("Choose one of the following:\n" +
										"0 Deposit\n" +
										"1 Top-Up\n" +
										"2 Withdrawal\n" +
										"3 Purchase\n" +
										"4 Transfer\n" +
										"5 Collect\n" +
										"6 Pay-Friend\n" +
										"7 Wire\n" +
										"8 Set Pin\n" +
										"9 LOG OUT AND GO BACK TO HOME PAGE");
								String customerChoice = scanner.nextLine();
								String accountId = "";
								String accountId2 = "";
								double amount = 0.0;
								switch (customerChoice) {
									// deposit
									case "0":
										System.out.println("Enter account id for deposit:");
										accountId = scanner.nextLine();
										System.out.println("Enter amount to deposit:");
										amount = Double.parseDouble(scanner.nextLine());
										app.deposit(accountId, amount);
										break;
									// top-up
									case "1":
										System.out.println("Enter account id for top-up:");
										accountId = scanner.nextLine();
										System.out.println("Enter amount to top-up:");
										amount = Double.parseDouble(scanner.nextLine());
										app.topUp(accountId, amount);
										break;
									// withdrawal
									case "2":
										System.out.println("Enter account id for withdrawal:");
										accountId = scanner.nextLine();
										System.out.println("Enter amount to withdraw:");
										amount = Double.parseDouble(scanner.nextLine());
										app.withdrawal(accountId, amount);
										break;
									// purchase
									case "3":
										System.out.println("Enter account id for purchase:");
										accountId = scanner.nextLine();
										System.out.println("Enter purchase amount:");
										amount = Double.parseDouble(scanner.nextLine());
										app.topUp(accountId, amount);
										break;
									// transfer
									case "4":
										System.out.println("Enter account id to transfer from:");
										accountId = scanner.nextLine();
										System.out.println("Enter account id to transfer to:");
										accountId2 = scanner.nextLine();
										System.out.println("Enter amount to transfer:");
										amount = Double.parseDouble(scanner.nextLine());
										app.transfer(accountId, accountId2, amount, true);
										break;
									// collect
									case "5":
										System.out.println("Enter account id to collect from:");
										accountId = scanner.nextLine();
										System.out.println("Enter account id to transfer collected funds to:");
										accountId2 = scanner.nextLine();
										System.out.println("Enter amount to collect:");
										amount = Double.parseDouble(scanner.nextLine());
										app.collect(accountId, accountId2, amount);
										break;
									// pay-friend
									case "6":
										System.out.println("Enter account id to pay from:");
										accountId = scanner.nextLine();
										System.out.println("Enter account id to pay:");
										accountId2 = scanner.nextLine();
										System.out.println("Enter amount to pay:");
										amount = Double.parseDouble(scanner.nextLine());
										app.payFriend(accountId, accountId2, amount);
										break;
									// wire
									case "7":
										System.out.println("Enter account id to wire from:");
										accountId = scanner.nextLine();
										System.out.println("Enter account id to wire:");
										accountId2 = scanner.nextLine();
										System.out.println("Enter amount to wire:");
										amount = Double.parseDouble(scanner.nextLine());
										app.wire(accountId, accountId2, amount);
										break;
									// set PIN
									case "8":
										int oldPin;
										int newPin;
										System.out.println("Enter old pin: ");
										oldPin = Integer.parseInt(scanner.nextLine());
										System.out.println("Enter new pin: ");
										newPin = Integer.parseInt(scanner.nextLine());
										app.setPin(oldPin, newPin);
										// go to home page
									case "9":
										usingATM = false;
										loggedIn = false;
										break;
									default:
										System.out.println("Must choose between choices 0-9\n");
										break;
								}
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
						runApp = false;
						break;

					default:
						System.out.print("Must choose between choices 0-3\n");
						break;
				}
			}
		}

	}
}
	//!### FINALIZAMOS

