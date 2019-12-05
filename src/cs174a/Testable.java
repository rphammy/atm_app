/*
 * CS174A Project Test Class.
 * Crated by Im YoungMin on October 29, 2019.
 */
package cs174a;                         // THE BASE PACKAGE FOR YOUR APP MUST BE THIS ONE.

interface Testable
{
	/////////////////////////////////////// Data types used across testing functions ///////////////////////////////////

	/**
	 * Account types.
	 */
	enum AccountType {
		STUDENT_CHECKING,
		INTEREST_CHECKING,
		SAVINGS,
		POCKET
	}

	/////////////////////////////////////// Functions for initializing your system /////////////////////////////////////

	/**
	 * Set up system, initialize any necessary variables, open connection to DB, etc.
	 * You MUST IMPLEMENT AT LEAST THIS FUNCTION, EVEN IF IT'S EMPTY.
	 * @return a string "r", where r = 0 for success, 1 for error.
	 */
	String initializeSystem();

	///////////////////////////////////////// Functions for testing your system ////////////////////////////////////////

	/**
	 * Destroy all of the tables in your DB.
	 * @return a string "r", where r = 0 for success, 1 for error.
	 */
	String dropTables();

	/**
	 * Create all of your tables in your DB.
	 * @return a string "r", where r = 0 for success, 1 for error.
	 */
	String createTables();

	/**
	 * Set system's date.
	 * @param year Valid 4-digit year, e.g. 2019.
	 * @param month Valid month, where 1: January, ..., 12: December.
	 * @param day Valid day, from 1 to 31, depending on the month (and if it's a leap year).
	 * @return a string "r yyyy-mm-dd", where r = 0 for success, 1 for error; and yyyy-mm-dd is the new system's date, e.g. 2012-09-16.
	 */
	String setDate( int year, int month, int day );

	/**
	 * Create a new checking or savings account.
	 * If customer is new, then their name and address should be provided.
	 * @param accountType New account's checking or savings type.
	 * @param id New account's ID.
	 * @param initialBalance Initial account balance.
	 * @param tin Account's owner Tax ID number - it may belong to an existing or new customer.
	 * @param name [Optional] If customer is new, this is the customer's name.
	 * @param address [Optional] If customer is new, this is the customer's address.
	 * @return a string "r aid type balance tin", where
	 *         r = 0 for success, 1 for error;
	 *         aid is the new account id;
	 *         type is the new account's type (see the enum codes above, e.g. INTEREST_CHECKING);
	 *         balance is the account's initial balance with 2 decimal places (e.g. 1000.34, as with %.2f); and
	 *         tin is the Tax ID of account's primary owner.
	 */
	String createCheckingSavingsAccount( AccountType accountType, String id, double initialBalance, String tin, String name, String address );

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
//	String createPocketAccount( String id, String linkedId, double initialTopUp, String tin );

	/**
	 * Create a new customer and link them to an existing checking or saving account.
	 * @param accountId Existing checking or saving account.
	 * @param tin New customer's Tax ID number.
	 * @param name New customer's name.
	 * @param address New customer's address.
	 * @return a string "r", where r = 0 for success, 1 for error.
	 */
	String createCustomer( String accountId, String tin, String name, String address );

	/**
	 * Deposit a given amount of dollars to an existing checking or savings account.
	 * @param accountId Account ID.
	 * @param amount Non-negative amount to deposit.
	 * @return a string "r old new" where
	 *         r = 0 for success, 1 for error;
	 *         old is the old account balance, with up to 2 decimal places (e.g. 1000.12, as with %.2f); and
	 *         new is the new account balance, with up to 2 decimal places.
	 */
	String deposit( String accountId, double amount );

	/**
	 * Show an account balance (regardless of type of account).
	 * @param accountId Account ID.
	 * @return a string "r balance", where
	 *         r = 0 for success, 1 for error; and
	 *         balance is the account balance, with up to 2 decimal places (e.g. with %.2f).
	 */
	String showBalance( String accountId );

	/**
	 * Move a specified amount of money from the linked checking/savings account to the pocket account.
	 * @param accountId Pocket account ID.
	 * @param amount Non-negative amount to top up.
	 * @return a string "r linkedNewBalance pocketNewBalance", where
	 *         r = 0 for success, 1 for error;
	 *         linkedNewBalance is the new balance of linked account, with up to 2 decimal places (e.g. with %.2f); and
	 *         pocketNewBalance is the new balance of the pocket account.
	 */
	String topUp( String accountId, double amount );

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
     String payFriend( String from, String to, double amount );

	/**
	 * Generate list of closed accounts.
	 * @return a string "r id1 id2 ... idn", where
	 *         r = 0 for success, 1 for error; and
	 *         id1 id2 ... idn is a list of space-separated closed account IDs.
	 */
	String listClosedAccounts();

	///////////////////////////////////////// ADDITIONAL FUNCTIONS /////////////////////////////////////////////////////

	/**
	 * Subtract to the checking or savings account balance
	 * @return a string r = "0" for success, "1" for error
	 */
	String withdrawal(String aid, double amount);

	/**
	 * Subtract money from the pocket account balance
	 * @return a string r="0" for success, "1" for error
	 */
	String purchase(String aid, double amount);

	/**
	 * Subtract money from one account w/ aid and add it to another account w/ aid2.
	 * A transfer can only occur between two accounts that have at least one
	 * owner in common. If the transfer was requested by a customer, she or he
	 * must be an owner of both accounts. Amount < $2000.
	 * @param aid checking or savings account
	 * @param aid2 checking or savings account
	 * @param amount amount to be transferred
	 * @return a string r="0" for success, "1" for error
	 */
	String transfer(String aid, String aid2, double amount);

	/**
	 * Move amount of money from account w/ aid back to linked account w/ aid2.
	 * There is a 3% fee for this action.
	 * @param aid pocket account
	 * @param aid2 linked checking or savings account
	 * @param amount amount to be collected (incurs 3% fee)
	 * @return a string r="0" for success, "1" for error
	 */
	String collect(String aid, String aid2, double amount);

	/**
	 * subtract money from account w aid and add it to another. The customer that
	 * requests this action must be an owner of account w aid. There is a 2% fee
	 * for this action.
	 * @param aid savings or checking account
	 * @param aid2 another checking or savings account
	 * @param amount amount to be collected (incurs a 3% fee)
	 * @return a string r="0" for success, "1" for error
	 */
	String wire(String aid, String aid2, double amount);

	/**
	 * Subtract money from the checking account w aid. Associated with a check
	 * is a check number.
	 * @param aid checking account
	 * @param amount amount
	 * @return a string r="0" for success, "1" for error
	 */
	String writeCheck(String aid, double amount);

	/**
	 * Add money to the checking or savings account. The amount added is the
	 * monthly interest rate times the average daily balance for the month.
	 * Interest is added at the end of each month.
	 * @return a string r="0" for success, "1" for error
	 */
	String accrueInterest();


	/////////////////////////////////////////// HELPER FUNCTIONS ///////////////////////////////////////////////////////
	/**
	 * Add transaction to db
	 * @param ttype type of transaction
	 * @param amount dollar amount
	 * @param aid account id initiating transaction
	 * @param aid2 aid2 for two sided transactions, "-1" otherwise
	 * @return a string "r", where r=0 for success, 1 for error
	 */
	String createTransaction(String ttype,double amount,String aid,String aid2);

	/**
	 * Add or subtract a given amount from account balance
	 * @param aid account id
	 * @param amount negative to subtract, positive to add
	 * @return a string "r", where r=0 for success, 1 for error
	 */
	String editAccountBalance(String aid,double amount);

	/**
	 * Get current balance of account
	 * @param aid account id
	 * @return account balance as a string, or "-1" for error
	 */
	String getAccountBalance(String aid);

	/**
	 * get account type
	 * @return AccountType as a string, or "1" for error
	 */
	String getAccountType(String aid);

}
