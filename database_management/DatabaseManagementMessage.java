package database_management;

import java.io.Serializable;

public class DatabaseManagementMessage implements Message<ActionTypeKey, Object[]> , Serializable{
	private static final long serialVersionUID = 1L;
	private ActionTypeKey key;
	private Object[] data;
	
	public DatabaseManagementMessage (ActionTypeKey key, Object[] data) {
		this.key = key;
		this.data = data;
	}
	
	@Override
	public ActionTypeKey getKey() {
		return key;
	}
	
	@Override
	public Object[] getData() {
		return data;
	}
	
	@Override
	public String toString() {
		return " " + data;
	}

	@Override
	public void setKey(ActionTypeKey key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setData(Object[] data) {
		// TODO Auto-generated method stub
		
	}	
}