package jarloader;

import java.io.IOException;
import java.util.List;

public class Main {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		List<Class<?>> listClasses = JarLoader.load(
				"FactoryCommand", "/home/gal/Downloads/jar/companyRegistration.jar");
		
		System.out.println(listClasses.size());
		listClasses.forEach(System.out::println);
	}
}
