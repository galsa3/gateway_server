package gatewayserver;

import java.sql.SQLException;

import org.json.JSONException;

public interface FactoryCommand {
	public String run(String data, DatabaseManagementInterface DbManagemaent) throws JSONException, SQLException;
}
