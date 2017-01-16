package helper.utilities;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ClassFinder {

	@SuppressWarnings("rawtypes")
	public List<Class> getDaoClasses() throws Exception {
		ArrayList<Class> classes = new ArrayList<>();
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		String pkg = "com.universeprojects.miniup.server.dao";
		Enumeration<URL> resources = contextClassLoader.getResources(pkg.replace(".", "/"));
		for (URL url = null; resources.hasMoreElements() && ((url = resources.nextElement()) != null);) {
			url.openConnection();
			checkDirectory(new File(URLDecoder.decode(url.getPath(),"UTF-8")), pkg, classes);
		}
		return classes;
	}

	@SuppressWarnings("rawtypes")
	private void checkDirectory(File directory, String pckgname, List<Class> classes) throws ClassNotFoundException {
		File tmpDirectory;

		if (directory.exists() && directory.isDirectory()) {
			final String[] files = directory.list();

			for (final String file : files) {
				if (file.endsWith(".class")) {
					try {
						String className = file.substring(0, file.length() - 6);
						classes.add((Class) Class.forName(pckgname + '.' + className));
					} catch (final NoClassDefFoundError e) {
						// do nothing. this class hasn't been found by the
						// loader, and we don't care.
					}
				} else if ((tmpDirectory = new File(directory, file)).isDirectory()) {
					checkDirectory(tmpDirectory, pckgname + "." + file, classes);
				}
			}
		}
	}

}
