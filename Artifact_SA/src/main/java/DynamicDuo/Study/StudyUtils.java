package DynamicDuo.Study;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public class StudyUtils {

	public static boolean isTestClass(String className) {
		boolean test = false;
		String[] parts = className.toLowerCase().split("\\.");
		for (String part : parts) {
			System.out.println(part);
			test = test || part.endsWith("test");
		}
		return test;
	}

	public static String findTestPair(String prodName) {
		boolean main = false;
		String[] parts = prodName.toString().toLowerCase().split("\\\\");

		for (String part : parts) {
			System.out.println(part);
			main = main || part.equals("main");
		}

		StringBuilder path = new StringBuilder();
		if (main) {
			for (String part : parts) {
				path.append(part);
				if (path.toString().endsWith("src")) {
					path.append("\\test");
					break;
				}
				path.append("\\");
			}
		}
		String name = parts[parts.length - 1];
		name = name.substring(0, name.length() - 5);
		return findTestFile(path.toString(), name);
	}

	public static String findTestFile(String path, String prodName) {
		
		try (Stream<Path> paths = Files.walk(Paths.get(path))) {
			Optional<Path> opt = paths
					.filter(p -> p.getFileName().toString().toLowerCase().equals(prodName + "test.java")).findFirst();
			String testPath = "no testfile found";
			
			if (opt.isPresent()) {
				testPath = opt.get().toString();
			}
			return testPath;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "no testfile found";
	}

	// public static String findTestFile(File[] files, String prodName, String
	// testName, Boolean test) {
	// if(test){
	// return testName;
	// }
	// for (File file : files) {
	// if (!file.isDirectory()) {
	// System.out.println("Directory: " + file.getName());
	// System.out.println("DIRECTORY: " + file.getAbsolutePath());
	//
	// findTestFile(file.listFiles(), prodName, testName, test);
	// } else {
	// System.out.println("File: " + file.getName());
	// System.out.println(file.getName().toLowerCase() + " --->>> " + prodName +
	// "test.java");
	// if(file.getName().toLowerCase().equals(prodName + "test.java")){
	// System.out.println("WAARROOOMMM");
	// findTestFile(file.listFiles(), prodName, file.getAbsolutePath(), true);
	// }
	// }
	// }
	// return null;
	// }

	public static void main(String arg[]) {
		System.out.println(findTestPair(
				"C:\\temp\\sonarqube\\sonar-core\\src\\main\\java\\org\\sonar\\api\\database\\configuration\\Property.java"));

	}
}
