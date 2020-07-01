package observer;

import java.util.ArrayList;
import java.util.List;

public class Dispatcher<T> {
	
	private List<Callback<T>> callbackList = new ArrayList<>();
	
	public void register(Callback<T> callback) {
		callbackList.add(callback);
		callback.setDispatcher(this);
	}
	
	public void unregister(Callback<T> callback) {
		callbackList.remove(callback);
	}
	
	public void updateAll(T param) {
		for (Callback<T> callback : callbackList) {
			callback.update(param);
		}
	}
	
	public void stopUpdate() {
		for (Callback<T> callback : callbackList) {
			callback.stopUpdate();
		}
	}	
}