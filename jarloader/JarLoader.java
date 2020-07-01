package jarloader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarLoader {
	private final static String DOT_CLASS = ".class";
	private final static String BACKSLASH = "/";
	private static final String FILE = "file://";
	private final static String DOT = ".";
	
	public static List<Class<?>> load(String interfaceName, String path) throws IOException, ClassNotFoundException {
		List<Class<?>> listClasses = new ArrayList<>();
		JarFile jarFile = new JarFile(path);
		Enumeration<JarEntry> entries = jarFile.entries();
		URLClassLoader classLoader = new URLClassLoader(new URL[] {new URL(FILE + path)});
		
		while(entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			
			if(entry.getName().endsWith(DOT_CLASS) && !entry.isDirectory()) {
				Class<?> currentClass = classLoader.loadClass(getClassName(entry));
				Class<?>[] interfaces = currentClass.getInterfaces();
				
				for(Class<?> currentInterface : interfaces) {
					if(currentInterface.getSimpleName().equals(interfaceName)) {
						listClasses.add(currentClass);
					}
				}
			}
		}
		jarFile.close();
		classLoader.close();
		return listClasses;
	}

	private static String getClassName(JarEntry entry) {
		String className = entry.getName().replaceAll(BACKSLASH, DOT);
		String myClass = className.substring(0, className.lastIndexOf(DOT));
		System.out.println("myClass "+myClass);
		return myClass;
	}
}
