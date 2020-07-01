package database_management;

import java.sql.SQLException;

public class StartDBManagement {
	public static void main(String[] args) throws SQLException {	
		String URL = "jdbc:mysql://localhost:3306/";
		String databaseName = "ExampleCompanyDB";
		String user = "root";
		String password = "305088155";
		
		DatabaseManagement company = new DatabaseManagement(URL, user, password, databaseName);
		String sqlCommand = "CREATE TABLE IF NOT EXISTS IOTEvent (" + 
				"`IOTEventID` INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
				"`serialNumber` varchar(16) NOT NULL,"  + 
				"`description` VARCHAR(255) NOT NULL," + 
				"`event_timestamp` TIMESTAMP NOT NULL)";				
		company.createTable(sqlCommand);

		sqlCommand = "INSERT INTO IOTEvent (serialNumber, description, event_timestamp) VALUES ('1233', 'im a description', '2020-04-23 15:56:09');";
		company.createRow(sqlCommand);
		company.createIOTEvent("2222|'im also a description'|'2020-04-23 15:56:20'");
		company.readRow("IOTEvent", "IOTEventID", '1');
		company.updateField("IOTEvent", "IOTEventID", 1,  "serialNumber", "1111");
		company.updateField("IOTEvent", "IOTEventID", 1,  2, "4444");

		System.out.println("read: "+ company.readRow("IOTEvent", "IOTEventID", "1"));
		System.out.println("read: "+ company.readRow("IOTEvent", "IOTEventID", "2"));
		
		System.out.println("field: " + company.readField("IOTEvent", "IOTEventID", 1, 4));
		System.out.println("field: " + company.readField("IOTEvent", "IOTEventID", 2, "event_timeStamp"));
		company.deleteRow("IOTEvent","serialNumber", "1111");
		
		sqlCommand = "DROP TABLE IF EXISTS IOTEvent";
		company.deleteTable(sqlCommand);
	}
}	
