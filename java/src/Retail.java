/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */

import java.sql.*;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Vector;
import java.io.PushbackInputStream;
import java.util.*; 
import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;




/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Retail {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));


   public static String globalType;
   public static String globalID;
   public static String globalLat;
   public static String globalLong;

   /**
    * Creates a new instance of Retail shop
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Retail(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Retail

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public static double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Retail.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Retail esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Retail object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Retail (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Stores within 30 miles");
                System.out.println("2. View Product List");
                System.out.println("3. Place a Order");
                System.out.println("4. View 5 recent orders");

                //the following functionalities basically used by managers
                System.out.println("5. Update Product");
                System.out.println("6. View 5 recent Product Updates Info");
                System.out.println("7. View 5 Popular Items");
                System.out.println("8. View 5 Popular Customers");
                System.out.println("9. Place Product Supply Request to Warehouse");
		System.out.println("10. Update Users");
		System.out.println("11. Update Product");
	
                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewStores(esql); break;
                   case 2: viewProducts(esql); break;
                   case 3: placeOrder(esql); break;
                   case 4: viewRecentOrders(esql); break;
                   case 5: updateProduct(esql); break;
                   case 6: viewRecentUpdates(esql); break;
                   case 7: viewPopularProducts(esql); break;
                   case 8: viewPopularCustomers(esql); break;
                   case 9: placeProductSupplyRequests(esql); break;
		   case 10: updateUserAdmin(esql); break;
		   case 11: updateProductAdmin(esql);break;

                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(Retail esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();
         System.out.print("\tEnter latitude: ");   
         String latitude = in.readLine();       //enter lat value between [0.0, 100.0]
         System.out.print("\tEnter longitude: ");  //enter long value between [0.0, 100.0]
         String longitude = in.readLine();
         
         String type="Customer";

			String query = String.format("INSERT INTO USERS (name, password, latitude, longitude, type) VALUES ('%s','%s', %s, %s,'%s')", name, password, latitude, longitude, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Retail esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE name = '%s' AND password = '%s'", name, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0){
		
		query = String.format("Select u.userID, u.latitude, u.longitude FROM Users u WHERE name = '%s' AND password = '%s'", name, password);
		List<List<String>> extract = esql.executeQueryAndReturnResult(query);
		String userid = extract.get(0).get(0);
		globalID = userid;
		globalLat = extract.get(0).get(1);
		globalLong = extract.get(0).get(2);
		
		/*
		System.out.print("\tUsers lat: ");
		System.out.print(globalLat);
		System.out.println();
		System.out.print("\tUsers long: ");		
		System.out.print(globalLong);
		System.out.println();
		*/

		return name;
	 }
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

   /* view stores within 30 miles of users lat/long */

	public static void viewStores(Retail esql) {
		try{

	  	/* QUERY EXAMPLE TO GRAB SOMETHING FROM USER GIVEN 'globalID'
		String fuckthis = String.format("SELECT u.name FROM Users u WHERE userID = %s", globalID);

		List<List<String>> myreturn = esql.executeQueryAndReturnResult(fuckthis);
		
		String namesies = myreturn.get(0).get(0);
		System.out.print("\tName of current user found was: ");
		System.out.print(namesies);
		System.out.print("\n");
		Name 
		*/

			double lat1 = Double.parseDouble(globalLat);
			double long1 = Double.parseDouble(globalLong);

			//gets all the store information into a hand List List
			String query = "SELECT * FROM Store";
			List<List<String>> extract = esql.executeQueryAndReturnResult(query);

			//need to do a loop
			
			System.out.print("\tResult:\n");

			for(int i = 0; i < extract.size(); ++i){
	
				double tempLat = Double.parseDouble(extract.get(i).get(2));
				double tempLong = Double.parseDouble(extract.get(i).get(3));

				double gLat = Double.parseDouble(globalLat);
				double gLong = Double.parseDouble(globalLong);

				double test = calculateDistance(gLat, gLong, tempLat, tempLong);
				
				if(test < 30){

					System.out.print("\tStore name: ");
					System.out.print(extract.get(i).get(1));
					System.out.println();
					System.out.print("\tStore ID: ");
					System.out.print(extract.get(i).get(0));
					System.out.println();
					System.out.println();

				}

			}

	
		}
         
      catch(Exception e){
         System.err.println (e.getMessage ());
         
      }
	}

   public static void viewProducts(Retail esql) {

	try{
	String saccquery="select storeID from store";
	esql.executeQueryAndPrintResult(saccquery);
	List<List<String>> s=esql.executeQueryAndReturnResult(saccquery);
	System.out.print("\tEnter Store ID to view products: ");
	String input = in.readLine().trim();
	boolean sacc=false;
	while(!sacc){
		for(int i=0;i<s.size();i++){
			if(input.equals(s.get(i).get(0).trim())){
				sacc=true;
			}
		}
		if(!sacc){
			System.out.print("\tStoreID does not exist. Enter Store ID to view Products: ");
			input=in.readLine().trim();
		}
	}
	String query = "select productName, numberOfUnits, pricePerUnit ";
	String q1 = "from Product where storeID = ";
	
	query = query + q1 + input;
	
	List<List<String>> extract = esql.executeQueryAndReturnResult(query);

	for(int i = 0; i < extract.size(); ++i){

		System.out.print("\tProduct name: ");
		System.out.print(extract.get(i).get(0));
		System.out.println();	

		System.out.print("\tNumber of available units: ");
		System.out.print(extract.get(i).get(1));
		System.out.println();

		System.out.print("\tPrice per unit: ");
		System.out.print(extract.get(i).get(2));
		System.out.println();
		System.out.println();

	} 
	}
	catch(Exception e){
		System.err.println (e.getMessage ());
	}
   }
   public static void placeOrder(Retail esql) {
	try{
		
		Vector<String> available = new Vector<String>();

		double lat1 = Double.parseDouble(globalLat);
		double long1 = Double.parseDouble(globalLong);

		String query = "SELECT * FROM Store";
		List<List<String>> extract = esql.executeQueryAndReturnResult(query);

		System.out.println("\tAvailable stores in your area: ");

		for(int i = 0; i < extract.size(); ++i){
	
			double tempLat = Double.parseDouble(extract.get(i).get(2));
			double tempLong = Double.parseDouble(extract.get(i).get(3));

			double gLat = Double.parseDouble(globalLat);
			double gLong = Double.parseDouble(globalLong);

			double test = calculateDistance(gLat, gLong, tempLat, tempLong);

			if(test < 30){

				String temp = extract.get(i).get(0);
				available.add(temp);
				System.out.println('\t');
				System.out.print('\t'+extract.get(i).get(0));
			}

		}

		System.out.println();
		System.out.println();

		boolean accepted = false;
		String input = "";


		while(!accepted){

			
			System.out.print("\tEnter storeID for desired store pickup: ");
			input = in.readLine();
			

			for(int i = 0; i < available.size(); ++i){

				if(input.equals(available.get(i))){
					accepted = true;
				}

			}

			if(!accepted){
				System.out.print("\tStore not within 30 miles, please try again.");
				System.out.println();
			}
		}

		System.out.println("\tAvailable units at given store: ");
		System.out.println();
			
		query = "select productName, numberOfUnits, pricePerUnit ";
		String q1 = "from Product where storeID = ";
	
		query = query + q1 + input;
		String savedLoc = input;	

		extract = esql.executeQueryAndReturnResult(query);

		for(int i = 0; i < extract.size(); ++i){

			System.out.print("\tProduct name: ");
			System.out.print(extract.get(i).get(0));
			System.out.println();	

			System.out.print("\tNumber of available units: ");
			System.out.print(extract.get(i).get(1));
			System.out.println();

			System.out.print("\tPrice per unit: ");
			System.out.print(extract.get(i).get(2));
			System.out.println();
			System.out.println();

		} 

		boolean paccepted = false;
		boolean eaccepted = false;		

		int uorder = 0;		
		String porder = "";


		while(!paccepted ||  !eaccepted){

			System.out.print("\tEnter product name you wish to order: ");
			porder = in.readLine().trim();
			System.out.println();
				
			System.out.print("\tEnter number of units you wish to order: ");
			boolean isnum=false;
			while(!isnum){
				try{
				uorder = Integer.parseInt(in.readLine());
				isnum=true;
				}catch(NumberFormatException e){
				System.out.print("Please enter an integer amount for units you wish to order: ");
				}
			}
			String expectedpname = "";
			int expectedamount = 0;


			for(int i = 0; i < extract.size(); ++i){

				expectedpname = extract.get(i).get(0).trim();

				expectedamount = Integer.parseInt(extract.get(i).get(1).trim());
					
				if(expectedpname.equals(porder)){
					paccepted = true;
					if(expectedamount >= uorder){
						eaccepted = true;
					}
				}
				


			}

			if(!paccepted){
				System.out.println("\tProduct name not available please try again.");
			}
			else if(!eaccepted){
				System.out.println("\tNumber of units not available please try again.");	
				paccepted = false;
					
			}

		}


	Timestamp timeat = new Timestamp(System.currentTimeMillis());
	String mytime = timeat.toString();
	java.util.Date date =new java.util.Date();


	String newOrder = String.format("insert into orders (ordernumber, customerID, storeID, productName, unitsOrdered, OrderTime) VALUES (DEFAULT, %s, %s, '%s', %d, '%s');", globalID, savedLoc, porder, uorder, date);




	esql.executeUpdate(newOrder);   

	query = "update product set numberofUnits=numberofUnits-";
	Integer u = new Integer(uorder);
	String uO =u.toString();
	q1=" where storeID=";
	String p=savedLoc.toString();
	String p1=" and productName=";
	String p2= porder.toString();
	String quote="'";
	query= query+quote+uO+quote+q1+quote+p+quote+p1+quote+p2+quote;
	esql.executeUpdate(query);

	String give_order = "select ordernumber from orders where customerid = '" + globalID + "'";

	List<List<String>> quick = esql.executeQueryAndReturnResult(give_order);
	String for_customer = quick.get(quick.size()-1).get(0);
	
	String message = "\tOrder number " + for_customer + " has been successfully placed!";

	System.out.println(message);
      
	}
	catch(Exception e){
		System.err.println (e.getMessage ());
	}

	
   
}
   public static  void viewRecentOrders(Retail esql) {
	 try{
        String query = "select o.storeID, s.name,o.productName,o.unitsOrdered, o.orderTime ";
        String q1 = "from orders o, store s  where customerID = ";
	String q2= " and s.storeID=o.storeID order by orderTime desc limit 5";
	String q="'";
        query = query + q1 +q+ globalID+q+ q2;

        List<List<String>> extract = esql.executeQueryAndReturnResult(query);

        for(int i = 0; i < extract.size(); ++i){

                System.out.print("\tStoreID: ");
                System.out.print(extract.get(i).get(0));
                System.out.println();

		System.out.print("\tStore Name: ");
                System.out.print(extract.get(i).get(1));
                System.out.println();

                System.out.print("\tProduct Name: ");
                System.out.print(extract.get(i).get(2));
                System.out.println();

                System.out.print("\tUnits Ordered: ");
                System.out.print(extract.get(i).get(3));
                System.out.println();

		System.out.print("\torderTime: ");
                System.out.print(extract.get(i).get(4));
                System.out.println();
                System.out.println();

        }
	//System.out.println(extract);
        }
        catch(Exception e){
                System.err.println (e.getMessage ());
        }
   }
   public static void updateProductAdmin(Retail esql) {
	try{	
		
		String q="select * from product order by storeID";
		List<List<String>>extract=esql.executeQueryAndReturnResult(q);
		System.out.println("\tProduct List\n");
		boolean uidacc=false;
                 boolean infoacc=false;
		int expectedSid=0;
		String expectedpname="";
		int expectedNum=0;
		for(int i=0; i<extract.size();i++){
			System.out.print("\tStoreID: ");
                	System.out.print(extract.get(i).get(0));
                	System.out.print(", ");
                
                	System.out.print("\tProduct Name: ");
                	System.out.print(extract.get(i).get(1));
                	System.out.print(", ");
                
                	System.out.print("\tNumber of Units: ");
                	System.out.print(extract.get(i).get(2));
                	System.out.print(", ");

                	System.out.print("\tPrice Per Unit: ");
                	System.out.print(extract.get(i).get(3));
	                System.out.println();
		}
	String updating="";
	String pname="";
	int sid=0;
	int numUp = 0;
	boolean innernumUp=false;
	boolean innersid=false;
	boolean innerpname=false;		
		while(!innersid){
			System.out.print("\tStoreID of Store where you wish to Update Products: ");
				try{
					 expectedSid=Integer.parseInt(in.readLine().trim());	
					
					for(int i=0;i<extract.size();i++){
						sid=Integer.parseInt(extract.get(i).get(0).trim());
						if(expectedSid==sid){
							innersid=true;
						}
					}
					if(!innersid){
						System.out.println("\tThat is not an acceptable StoreID");
					}
				}catch(NumberFormatException e){
                        		System.out.println("\tThat is not an acceptable StoreID.") ;
               			}
		}
		//also add a 3. to update number of units and 4. to update price per unit
		//also call productUpdates function after 1,2,3,4 are run

		updating = "\t1. Add Product \n\t 2. Delete Product /n/t 3. Edit an existing product: \n\t Enter 1 or 2 or 3: ";
                System.out.print(updating);
		String productCheck = "select p.storeID,p.productName, p.numberOfUnits, p.pricePerUnit from product p, store s where p.storeid = s.storeid and s.storeid = '";
                productCheck = productCheck + expectedSid + "'";
                List<List<String>> productlists = esql.executeQueryAndReturnResult(productCheck);
		while(!innernumUp){
			try{
				numUp=Integer.parseInt(in.readLine().trim());
				if(numUp==1||numUp==2){
					innernumUp=true;
				}	
			}catch(NumberFormatException e){
			}
			if(!innernumUp){
				System.out.print("\tPlease Enter 1 (to Add Product) or 2 (to Delete Product): ");
			}	
		}
		
                if(numUp==1){
			System.out.println("\tWhat is the Product Name you are adding: ");
			pname=in.readLine().trim();
			boolean bnumU=false;
			boolean bprice=false;
			int numU=0;
			int price=0;
			while(!bnumU){
				System.out.print("\tWhat is the amount of units?: ");
				try{
					numU=Integer.parseInt(in.readLine().trim());
					bnumU=true;
				}catch(NumberFormatException e){
					System.out.println("\tThat is not an Integer, Try Again");
				}
			}
			while(!bprice){
                                System.out.print("\tWhat is the Price per Unit?: ");
                                try{
                                        price=Integer.parseInt(in.readLine().trim());
                                        bprice=true;
                                }catch(NumberFormatException e){
                                        System.out.println("\tThat is not an Integer, Try Again");
                                }
                        }
			String Add=String.format("insert into product (storeID, productName, numberOfUnits,pricePerUnit) VALUES (%d,'%s',%d,%d);",expectedSid, pname,numU,price);
			esql.executeUpdate(Add);
			esql.executeQueryAndPrintResult(productCheck);
			//add new product here
		}else if (numUp == 2){
			boolean bpname=false;
			 pname="";
			while(!bpname){
				System.out.println("\tWhat is the Product (name) that you are deleting: ");
				pname=in.readLine().trim();
				for(int i=0;i<productlists.size();i++){
					if(pname.equals(productlists.get(i).get(1).trim())){
						bpname=true;
					}
				}
				if(!bpname){
					System.out.println("\tThat Product Name does not exist at the store you are trying to delete from. Try Again.");
				}
			}
			String del= "delete from product where storeID='"+expectedSid+"' and productName='"+pname+"'";
			esql.executeUpdate(del);
			 esql.executeQueryAndPrintResult(productCheck);	
		}
		
		else if(numUp == 3){
			
			//productlist
			
			for(int i = 0; i < productlist; ++i){
				System.out.print("Product Name: ");
				System.out.println(productlist.get(i).get(1));
				
				System.out.print("Number of units: ");
				System.out.println(productlist.get(i).get(2));
				
				System.out.print("Price per unit: ");
				System.out.println(productlist.get(i).get(3));
			}
			
			System.out.println("\tWhich product which you like to make edits to? \n\t Enter product name: ");
			String selection = in.readLine().trim();
			
			boolean exists = false;
			int existsAt = 0;
			while(!exists){
				
			for(int i = 0; i < productlist.size(); ++i){
			
				if(productlist.get(i).get(1).equals(selection)){
					exists = true;
					existsAt = i;
				}
				
			}
				
				if(!exists){
				
					System.out.println("\tError: Product name did not match inventory, please try again. \n\tEnter product name: ");
					selection = in.readLine().trim();
					
				}
				
			}
			
			System.out.println("\tPlease enter which attribute you wish to edit:");
			System.out.println("\t1: Product name \n\t2: Number of Units \n\t3: Cost per Unit \n\tAttribute: ");
			
			boolean isAtt = false;
			int trythis = 0;
			
			while(!isAtt){
				
				try{
					trythis = Integer.parseInt(in.readLine().trim());
					if(trythis > 0 && trythis < 4){
						isAtt = true;
					}
					
					if(!isAtt){
					
						System.out.println("\tInput did not match 1 - 3, please try again: ");
						
					}
				}
				catch(NumberFormatException e){
					System.out.println("\tThat is not an Integer, Try Again");
				}
			}
			
			String chocie = "";
			if(trythis == 1) { choice = "productname";}
			else if(trythis == 2) {choice = "numberofunits";}
			else if(trythis == 3) {choice = "priceperunit";}
			else{exit(55)};
			     
			System.out.println("\tEnter value you wish to change it to: ");
			String updateTo;
			int updateTonNum; //= Integer.parseInt(updateTo);
			
			if(trythis == 1){
				
				updateTo = in.readLine().trim();
				String updateTime = "update product set " + choice + " = " + "'"+updateTo + "'"+" where storeID = '" + expectedSid + "'"+" and productName = '"+ selection+ "'";
				esql.executeUpdate(updateTime);
				
				String returnResult = "select * from product";
				esql.exectueQueryAndPrintResult(returnResult);
				
			}
			
			else if(trythis == 2 || trythis == 3){
			
				
				boolean isCorrect = false;
				while(!isCorrect){
				
					try{
						updateTonNum = Integer.parseInt(updateTo);
						isAtt = true;
					}
					catch(NumberFormatException e){
						System.out.println("\tThat is not an Integer, Try Again");
					}
				}
				
				String updateTime = "update product set " + choice + " = " + "'"+updateToNum + "'"+" where storeID = '" + expectedSid + "'"+" and productName = '"+ selection+ "'";
				esql.executeUpdate(updateTime);
				
				String returnResult = "select * from product";
				esql.exectueQueryAndPrintResult(returnResult);
				
			}
			
			
			System.out.println("\tUpdate successful, woohoo!!");
			
		}
		else{	
			exit(30);
		}



	System.out.println("\tUpdate successful!!! Woohoo!!!");

	}catch(Exception e){
		System.err.println (e.getMessage());
	}
   }

   public static void updateUserAdmin(Retail esql){
	try{
	    String q="select * from users order by userID";
		List<List<String>>extract=esql.executeQueryAndReturnResult(q);
		System.out.println("\tUser List\n");
		boolean uidacc=false;
                 boolean infoacc=false;
		int expecteduid=0;
		String expectedpname="";
		int expectedNum=0;
		for(int i=0; i<extract.size();i++){
			System.out.print("\tUserID: ");
                	System.out.print(extract.get(i).get(0));
                	System.out.print(", ");
                
                	System.out.print("\tName: ");
                	System.out.print(extract.get(i).get(1));
                	System.out.print(", ");
                
                	System.out.print("\tPassword: ");
                	System.out.print(extract.get(i).get(2));
                	System.out.print(", ");

                	System.out.print("\tLatitude: ");
                	System.out.print(extract.get(i).get(3));
			System.out.print(", ");
			
			System.out.print("\tLongitude: ");
                	System.out.print(extract.get(i).get(3));
			System.out.print(", ");
			
			System.out.print("\tUser Type: ");
                	System.out.print(extract.get(i).get(3));
	                System.out.println();
		}
	String updating="";
	int numUp = 0;
	boolean bnumUp=false;
		//3 update user->what update? 1. name 2. password 3. lat 4. long 5. type
	System.out.print("\t1. Add User\n\t2. Delete User\n\tEnter 1 (add user) or 2 (delete user): ");
	while(!bnumUp){
		try{
			numUp=Integer.parseInt(in.readLine().trim());
			if(numUp==1 ||numUp==2){
				bnumUp=true;
			}else{
				System.out.print("\tEnter 1 (add user) or 2 (delete user): ");	
			}
		}catch(NumberFormatException e){
			System.out.print("\tEnter 1 (add user) or 2 (delete user): ");	
		}
	}
	if(numUp==1){
		System.out.print("\tEnter name of User you wish to add: ");
		String uname=in.readLine().trim();
		System.out.print("\tEnter password for User you wish to add: ");
		String pass=in.readLine().trim();
		boolean blat=false;
		int lat=0;
		int lon=0;
		boolean blong=false;
		System.out.print("\tWhat is their latitude: ");
		while(!blat){
			try{
				lat=Integer.parseInt(in.readLine().trim());
				blat=true;
			}catch(NumberFormatException e){
				System.out.print("\tThat is not a number. What is their latitude: ");
			}
		}
		System.out.print("\tWhat is their longitude: ");
		while(!blong){
			try{
				lon=Integer.parseInt(in.readLine().trim());
				blong=true;
			}catch(NumberFormatException e){
				System.out.print("\tThat is not a number. What is their longitude: ");
			}
		}
		
		//we assume they are type customer cause we cant add stores and there is only one admin, not necessarily
		//might need to make sure cant enter empty char or space
		//admin can do manager things, but for all stores-prof says in piazza-not stated in pdf?
		//admin can remove and add users and products
		//do we default userid or an we make it any number we want->delete old manager and add new user with that id as manager?
		String cust="customer";
		String Add=String.format("insert into users (userID, name, password,latitude,longitude,type) VALUES (DEFAULT,'%s','%s',%d,%d,'%s');",uname, pass,lat,lon,cust);
		esql.executeUpdate(Add);
		esql.executeQueryAndPrintResult(extract);
		
	}else{
		System.out.print("\tEnter name of UserID of User you'd like to delete: ");
		int uid=0;
		while(!uidacc){
			try{
				int uid=Integer.parseInt(in.readLine().trim());
				for(int i=0; i<extract.size();i++){
					if(uid==Integer.parseInt(extract.get(i).get(0).trim(0)){
						uidacc=true;	
					}else{
						System.out.print("Please Enter an existing UserID: ");	
					}
				}
			}catch(NumberFormatException e){
				System.out.print("\tPlease Enter a Number for UserID: ");	
			}
				
		}
		String del= "delete from users where UserID='"+uid+"';
		esql.executeUpdate(del);
		esql.executeQueryAndPrintResult(extract);
					   
	}
	System.out.println("\tUpdate successful!!! Woohoo!!!");

	}catch(Exception e){
		System.err.println(e.getMessage());
	}
   }

   public static void updateProduct(Retail esql){
	try{
		String q="select p.storeID, p.productName, p.numberOfUnits,p.pricePerUnit from product p, store s where s.managerID=";
		String q1=" and s.storeId=p.storeID order by p.storeID";
		q=q+"'"+globalID+"'"+q1;
		List<List<String>>extract=esql.executeQueryAndReturnResult(q);
		System.out.println("\tProduct List\n");
		for(int i=0; i<extract.size();i++){
                        System.out.print("\tStoreID: ");
                        System.out.print(extract.get(i).get(0));
                        System.out.print(", ");

                        System.out.print("\tProduct Name: ");
                        System.out.print(extract.get(i).get(1));
                        System.out.print(", ");

                        System.out.print("\tNumber of Units: ");
                        System.out.print(extract.get(i).get(2));
                        System.out.print(", ");

                        System.out.print("\tPrice Per Unit: ");
                        System.out.print(extract.get(i).get(3));
                        System.out.println();
                }

		boolean store_exists = false;
		String store_change = "";


		while(!store_exists){

			System.out.print("\tEnter the storeID which you wish to make updates to: ");
			store_change = in.readLine().trim();


			for(int i = 0; i < extract.size(); ++i){

				String temp = extract.get(i).get(0).trim();

				if(temp.equals(store_change)){
					store_exists = true;
				}

			}

			if(!store_exists){
				System.out.println("\tStore not found, please try again.");
			}

		}

		
		int sid=Integer.parseInt(store_change);
		System.out.print("\tProduct Name of Product you wish to update: ");
                String pname=in.readLine().trim();
		boolean pacc=false;
		while(!pacc){
			for(int i=0;i<extract.size();i++){
				String expectedPname =extract.get(i).get(1).trim();	
				if(expectedPname.equals(pname)){
					pacc=true;
				}	
			}
			if(!pacc){
			for(int i=0; i<extract.size();i++){
                        System.out.print("\tStoreID: ");
                        System.out.print(extract.get(i).get(0));
                        System.out.print(", ");

                        System.out.print("\tProduct Name: ");
                        System.out.print(extract.get(i).get(1));
                        System.out.print(", ");

                        System.out.print("\tNumber of Units: ");
                        System.out.print(extract.get(i).get(2));
                        System.out.print(", ");

                        System.out.print("\tPrice Per Unit: ");
                        System.out.print(extract.get(i).get(3));
                        System.out.println();
                }
			System.out.print("\tProduct does not exist at your store. Enter an existing Product Name at your Store: ");
			pname=in.readLine().trim();
			}
		}
	
		System.out.print("\t1. Number of Units\n\t2. Price Per Unit\n\n\tWhat would you like to update? (Enter 1 or 2): ");
		 
		int nu=0;
		boolean notacc=false;	
		while(!notacc){
			try{
				int numUp=Integer.parseInt(in.readLine());	
				if(numUp>0 && numUp<=2){
                	        	nu=numUp;
					notacc = true;
                        	}else{
					System.out.print("Please Select 1 or 2 to update Number of Units or Price: ");
				}
			}catch(NumberFormatException e){
				System.out.print("\tPlease Enter 1 or 2 to Update Number of Units or Price: ");
			}
		}
		String param = "";
		if(nu == 1)
			param = "numberOfUnits";
		else 
			param="pricePerUnit";
				   
		System.out.print("\tWhat would you like to change this field to?: ");
		
				   //might break cause int
		boolean upacc=false;
		int toUpdate=0;
		 while(!upacc){
			try{
				 toUpdate = Integer.parseInt(in.readLine());
				if(toUpdate>0){
				upacc=true;
				}else{
				System.out.print("\tnot acceptable input. Try again: ");
				}
				
			
			}catch(NumberFormatException e){
				System.out.print("\tPlease Enter an integer: ");
			}
		}
		String updateTime = "update product set " + param + " = " + "'"+toUpdate + "'"+" where storeID = '" + sid + "'"+" and productName = '"+ pname+ "'";
		esql.executeUpdate(updateTime);	
		System.out.println("first update to products");
				   
		Timestamp timeat = new Timestamp(System.currentTimeMillis());
		String mytime = timeat.toString();
		java.util.Date date =new java.util.Date();


		String newOrder = String.format("insert into productUpdates (updatenumber, managerID, storeID, productName, updatedOn) VALUES (DEFAULT, %s, %s, '%s','%s');", globalID, sid, pname, date);
		esql.executeUpdate(newOrder);   
		System.out.println("second update to productUpdate");

		
				   
	}catch(Exception e){
		System.err.println (e.getMessage());
	}
   }
   public static void viewRecentUpdates(Retail esql) {
	try{
		String hold="select s.storeID from user u, store s where s.managerID= "+globalID;
                List<List<String>>extract=esql.executeQueryAndReturnResult(hold);
		for(int i=0; i<extract.size();i++){
		String st=extract.get(i).get(0).trim();
		String q = "select pu.updateNumber, pu.managerID, pu.storeID, pu.productName, pu.updatedOn from productupdates pu, store s, orders o,users u where s.managerid = '";
		String q2 = "' and '";
		String q4 ="'= s.storeid and pu.managerid=u.userID group by pu.updateNumber order by count(pu.updatedOn) desc limit 5";
		String q3 = q + globalID + q2+st+q4;
		esql.executeQueryAndPrintResult(q3);
		}


	}catch(Exception e){
		System.err.println (e.getMessage());
	}
   }
   public static void viewPopularProducts(Retail esql) {
	try{
	
		//listlist<string> extract = select count(productName) from products group by productname
		
		String q = "select o.productname, count(o.productname) from store s, orders o where s.managerid = '";
		String q2 = "' and o.storeid = s.storeid group by productname order by count(o.productname) desc limit 5";
		String q3 = q + globalID + q2;

		esql.executeQueryAndPrintResult(q3);	
	
		


	}catch(Exception e){
		System.err.println(e.getMessage());
	}
   }
   public static void viewPopularCustomers(Retail esql) {
   	try{
	
		//listlist<string> extract = select count(customerID) from products group by productname
		
		String q = "select u.name, count(o.customerID) from store s, orders o,users u where s.managerid = '";
		String q2 = "' and o.storeid = s.storeid and o.customerID=u.userID group by u.name order by count(o.customerID) desc limit 5";
		String q3 = q + globalID + q2;

		esql.executeQueryAndPrintResult(q3);	
	
		


	}catch(Exception e){
		System.err.println(e.getMessage());
	}
   }
   public static void placeProductSupplyRequests(Retail esql) {
   	
	try{
		String hold="select p.storeID, p.productName, p.numberOfUnits, p.pricePerUnit from user u, store s, product p where s.managerID= "+globalID;
		hold=hold+"and p.storeID=s.storeID order by p.storeID";
        	List<List<String>>extract=esql.executeQueryAndReturnResult(hold);
		for(int i = 0; i < extract.size(); ++i){

                System.out.print("\tStoreID: ");
                System.out.print(extract.get(i).get(0));
                System.out.println();
		
		System.out.print("\tProduct Name: ");
                System.out.print(extract.get(i).get(1));
                System.out.println();

                System.out.print("\tNumber of available units: ");
                System.out.print(extract.get(i).get(2));
                System.out.println();

                System.out.print("\tPrice per unit: ");
                System.out.print(extract.get(i).get(3));
                System.out.println();
                System.out.println();

                }
		
		System.out.print("\tEnter Store ID to Order Product for: ");
		String sid = in.readLine().trim();
		boolean sidacc=false;
		while(!sidacc){
			for(int i=0;i<extract.size();i++){
				if(sid.equals(extract.get(i).get(0).trim())){
					sidacc=true;	
				}
			}
			if(!sidacc){
				System.out.println("\tYou are either not a manager of the store you entered or the store you entered does not exist.");
				System.out.print("\tEnter Store ID to Order Product for: ");
				sid = in.readLine().trim();
			}
		}	
		String query = "select productName, numberOfUnits, pricePerUnit ";
		String q1 = "from Product where storeID = ";
	
		query = query + q1 + sid;
	
		extract = esql.executeQueryAndReturnResult(query);

		for(int i = 0; i < extract.size(); ++i){

		System.out.print("\tProduct name: ");
		System.out.print(extract.get(i).get(0));
		System.out.println();	

		System.out.print("\tNumber of available units: ");
		System.out.print(extract.get(i).get(1));
		System.out.println();

		System.out.print("\tPrice per unit: ");
		System.out.print(extract.get(i).get(2));
		System.out.println();
		System.out.println();

		} 
		//
		System.out.print("\tEnter Product Name you would like to Order Units for: ");
		String pname = in.readLine().trim();
		boolean pacc=false;
		int ogunits=0;
		while(!pacc){
			for(int i=0;i<extract.size();i++){
				if(pname.equals(extract.get(i).get(0).trim())){
					ogunits=Integer.parseInt(extract.get(i).get(1).trim());
					pacc=true;	
				}
			}
			if(!pacc){
				System.out.print("\tProduct Does Not Exist. Enter Product Name to Order: ");
				pname = in.readLine().trim();
			}
		}	
		System.out.println();
		System.out.println("\tWarehouse List\n");
		String compare= "select * from warehouse w";
		extract=esql.executeQueryAndReturnResult(compare);
		for(int i = 0; i < extract.size(); ++i){

		System.out.print("\tWarehouseID: ");
		System.out.print(extract.get(i).get(0));
		System.out.println();	

		System.out.print("\tarea: ");
		System.out.print(extract.get(i).get(1));
		System.out.println();

		System.out.print("\tLatitude: ");
		System.out.print(extract.get(i).get(2));
		System.out.println();
			
		System.out.print("\tLongitude: ");
		System.out.print(extract.get(i).get(3));
		System.out.println();
		System.out.println();

		} 
		System.out.print("\tEnter WarehouseID you would like to Request Supply From: ");
		String ware = in.readLine().trim();
		boolean wareacc=false;
		while(!wareacc){
			for(int i=0;i<extract.size();i++){
				if(ware.equals(extract.get(i).get(0).trim())){
					wareacc=true;	
				}
			}
			if(!wareacc){
				System.out.print("\tWarehouse Does not exist. Enter Warehouse ID to Order Product from: ");
				ware = in.readLine().trim();
			}
		}
		System.out.print("\tHow many more Units would you like to Request?: ");
		boolean uacc=false;
		int units=0;
		while(!uacc){
			try{
			units=Integer.parseInt(in.readLine().trim());
			uacc=true;
			}catch(NumberFormatException e){
			System.out.print("\tThat is not a valid unit amount. Enter the amount of units you would like to request: ");
			uacc=false;
			}
		}
		int req=units;
		units=units+ogunits;
		String updateTime = "update product set  numberOfUnits = " + units +" where storeID = '" + sid + "'"+" and productName = '"+ pname+ "'";
		esql.executeUpdate(updateTime);	
		System.out.println("first update to products");
		
		String newOrder = String.format("insert into productSupplyRequests (requestNumber, managerID, warehouseID, storeID, productName, unitsRequested) VALUES (DEFAULT, %s, %s, '%s','%s', %d);", globalID, ware, sid, pname, req);
		esql.executeUpdate(newOrder);   
		System.out.println("second update to productSupplyRequests");
		
		String work="select * from productSupplyRequests";
		esql.executeQueryAndPrintResult(work);		
		
	}catch(Exception e){
		System.err.println (e.getMessage ());
	}
   
	
   }
//end Retail
}
