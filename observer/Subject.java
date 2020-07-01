package observer;

public class Subject {
	
	private Dispatcher<Integer> dispatcher = new Dispatcher<>();
	private Integer param = 4;
	
	public void register(Callback<Integer> callback) {
		dispatcher.register(callback);
	}
	
	public void unregister(Callback<Integer> callback) {
		dispatcher.unregister(callback);
	}
	
	public void updateAll() {
		dispatcher.updateAll(param);
	}
	
	public void stopUpdate() {
		dispatcher.stopUpdate();
	}	
}