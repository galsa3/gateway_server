package gatewayserver;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

public class CompanyRegistrationCommand implements FactoryCommandModifier {

	@Override
	public void addToFactory() {
		CMDFactory<FactoryCommand, String, Object> commandFactory = CMDFactory.getFactory();
		commandFactory.add("COMPANY_REGISTRATION", (Object a) -> new CompanyRegistration());		
		System.out.println("added");
	}
	
	/**********************************************
	 * Company Registration
	 **********************************************/
	private class CompanyRegistration implements FactoryCommand {
		private static final String RESPONSE = "Company Registered!";

		@Override
		public String run(String data, DatabaseManagementInterface companyDatabase) throws JSONException, SQLException {
			JSONObject jsonObject = new JSONObject(data);
			companyDatabase.createTable(getSqlCommand(jsonObject));
			
			return RESPONSE;
		}
		
		private String getSqlCommand(JSONObject jsonObject) throws JSONException {
			return jsonObject.getString("sqlCommand");
		}
	}

	@Override
	public int getVersion() {
		return 6;		
	}
}
