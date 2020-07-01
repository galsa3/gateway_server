package gatewayserver;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

public class IOTUpdateCommand implements FactoryCommandModifier {

	@Override
	public void addToFactory() {
		CMDFactory<FactoryCommand, String, Object> commandFactory = CMDFactory.getFactory();
		commandFactory.add("IOT_UPDATE", (Object a) -> new IOTUpdate());		
		System.out.println("added");
	}
	/**********************************************
	 * IOT Update
	 **********************************************/
	private class IOTUpdate implements FactoryCommand {
		private static final String RESPONSE = "IOT Updated!";

		@Override
		public String run(String data, DatabaseManagementInterface companyDatabase) throws JSONException, SQLException {
			JSONObject jsonObject = new JSONObject(data);
			companyDatabase.createIOTEvent(getRawData(jsonObject));

			return RESPONSE;
		}			
		
		private String getRawData(JSONObject jsonObject) throws JSONException {
			return jsonObject.getString("rawData");
		}
	}
	@Override
	public int getVersion() {
		return 0;
		// TODO Auto-generated method stub
		
	}
}
