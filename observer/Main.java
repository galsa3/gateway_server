package observer;

public class Main {

	public static void main(String[] args) {
		
		Subject subject = new Subject();
		Observer<Integer> observer = new Observer<>();
		
		observer.regitser(subject);
		subject.updateAll();
		observer.unregitser();
		subject.updateAll();
	}

}
