package gatewayserver;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

public class IOTRegistrationCommand implements FactoryCommandModifier {

	@Override
	public void addToFactory() {
		CMDFactory<FactoryCommand, String, Object> commandFactory = CMDFactory.getFactory();
		commandFactory.add("IOT_USER_REGISTRATION", (Object a) -> new IOTRegistration());		
		System.out.println("added");
	}

	private class IOTRegistration implements FactoryCommand {
		private static final String RESPONSE = "IOT Registered!";
		
		@Override
		public String run(String data, DatabaseManagementInterface companyDatabase) throws JSONException, SQLException {
			JSONObject jsonObject = new JSONObject(data);
			companyDatabase.createRow(getSqlCommand(jsonObject));			
			return RESPONSE;
		}
		
		private String getSqlCommand(JSONObject jsonObject) throws JSONException {
			return jsonObject.getString("sqlCommand");
		}		
	}

	@Override
	public int getVersion() {
		return 0;
		// TODO Auto-generated method stub
		
	}

}
