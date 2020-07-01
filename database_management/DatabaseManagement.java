package database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import gatewayserver.DatabaseManagementInterface;

public class DatabaseManagement implements DatabaseManagementInterface {	
	private static final String INSERT_IOT_EVENT = "INSERT INTO IOTEvent (serial_number, description, event_timestamp) VALUES (";
	private static final String CREATE_DB = "CREATE SCHEMA IF NOT EXISTS ";
	private static final String SELECT_ALL = "SELECT * FROM ";
	private static final String DELETE_FROM = "DELETE FROM ";
	private static final String UPDATE = "UPDATE ";
	private static final String WHERE = " WHERE ";
	private static final String SPLIT_BY = "[|]";
	private static final String END_LINE = ");";
	private static final String SET = " SET ";
	private static final String EQUAL = " = ";
	private static final String COMMA = ", ";
	private static final int SERIAL_NUMBER_INDEX = 0;
	private static final int DESCRIPTION_INDEX = 1;
	private static final int TIMESTAMP_INDEX = 2;
	
	private final String databaseName;
	private final String password;
	private final String user;
	private final String URL;
	
	public DatabaseManagement(String URL, String user, String password, String databaseName) throws SQLException {		
		 this.databaseName = databaseName;			
		 this.password  = password;		 
		 this.user = user;
		 this.URL = URL;
		 createDB();	
	}
	
	public void createTable(String sqlCommand) throws SQLException {
		executeUpdate(sqlCommand);
	}
	
	public void deleteTable(String tableName) throws SQLException {	
		executeUpdate("DROP TABLE IF EXISTS "+tableName);
	}
	
	public void createRow(String sqlCommand) throws SQLException {
		executeUpdate(sqlCommand);
	}
		
	public List<Object> readRow(String tableName, String columnName, Object rowKey) throws SQLException {		
		Connection connection = getConnection();
		ResultSet rowResultSet = executeQuery(connection, tableName,  columnName, rowKey);
		List<Object> resultList = new ArrayList<>();
		resultList = copyResultSetToList(rowResultSet, resultList);			
		connection.close();
		
		return resultList;
	}

	public void deleteRow(String tableName, String primaryKeyColumnName, Object primaryKey) throws SQLException {
        String sqlCommand = DELETE_FROM + tableName + WHERE + primaryKeyColumnName + EQUAL + primaryKey;
		executeUpdate(sqlCommand);
	}
	
	public Object readField(String tableName, String primaryKeyColumnName, Object rowKey, String columnName) throws SQLException {
		Connection connection = getConnection();
		ResultSet result = executeQuery(connection, tableName,  primaryKeyColumnName, rowKey);		
		Object field = result.getObject(columnName);
		connection.close();
		
		return field;
	}

	public Object readField(String tableName, String primaryKeyColumnName, Object rowKey, int columnIndex) throws SQLException {
		return readRow(tableName, primaryKeyColumnName, rowKey).get(columnIndex - 1);
	}
	
	public void updateField(String tableName, String primaryKeyColumnName, Object primaryKey, String ColumnName, Object newValue) throws SQLException {
		String sqlCommand = UPDATE + tableName + SET + ColumnName + EQUAL + newValue + WHERE + primaryKeyColumnName + EQUAL + primaryKey;
		executeUpdate(sqlCommand);
	}
	
	public void updateField(String tableName, String primaryKeyColumnName, Object primaryKey, int columnIndex, Object newValue) throws SQLException {
		Connection connection = getConnection();
		ResultSet result = executeQuery(connection, tableName, primaryKeyColumnName, primaryKey);
		result.updateObject(columnIndex, newValue);
		result.updateRow();
		connection.close();
	}

	public void createIOTEvent(String rawData) throws SQLException { 
		String[] tokens = rawData.split(SPLIT_BY);		
		String sqlCommand = INSERT_IOT_EVENT + tokens[SERIAL_NUMBER_INDEX] + COMMA + 
							tokens[DESCRIPTION_INDEX] + COMMA + tokens[TIMESTAMP_INDEX]+ END_LINE;
		executeUpdate(sqlCommand);
	}
	
	private void createDB() throws SQLException {
		Connection connection = DriverManager.getConnection(URL, user, password);
		Statement statement = connection.createStatement();
		statement.executeUpdate(CREATE_DB + databaseName);
		connection.close();
	}	
	
	private void executeUpdate(String sqlCommand) throws SQLException {
		Connection connection = getConnection();
		Statement statement = connection.createStatement();
		statement.executeUpdate(sqlCommand);
		connection.close();		
	}
	
	private ResultSet executeQuery(Connection connection, String tableName, String primaryKeyColumnName, Object rowKey) throws SQLException {
		String sqlCommand = SELECT_ALL + tableName + WHERE + primaryKeyColumnName + EQUAL + rowKey;
		Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet result = statement.executeQuery(sqlCommand);
		result.next();
		
		return result;	
	}
	
	private Connection getConnection() throws SQLException {	
		Connection connection = DriverManager.getConnection(URL + databaseName, user, password);
		System.out.println("connected to DB: " + databaseName);		
		
		return connection;			
	}

	private List<Object> copyResultSetToList(ResultSet rowResultSet, List<Object> resultList) throws SQLException {
		for(int currentColomn = 1; currentColomn <= rowResultSet.getMetaData().getColumnCount(); ++currentColomn) {
			resultList.add(rowResultSet.getObject(currentColomn));
		}
		
		return resultList;
	}
}
