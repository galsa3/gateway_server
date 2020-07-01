package observer;

import java.util.function.Consumer;

public class Observer<T> {
	
	private Callback<Integer> callback;	
	
	public void regitser(Subject subject) {		
		Consumer<Integer> updateCallback = (a) -> System.out.println(a);
		Consumer<Integer> stopUpdateCallback = (a) -> System.out.println("bye");	
		
		callback = new Callback<>(updateCallback, stopUpdateCallback);
		
		subject.register(callback);
	}
	
	public void unregitser() {
		callback.getDispatcher().unregister(callback);
	}	
	
	public void unregitser(Subject subject) {
		subject.unregister(callback);
	}	
}