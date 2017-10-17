package DynamicDuo.Study;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class StudyUtils {
	
	public static boolean isTestClass(String className) {
		boolean test = false;
		String[] parts = className.toLowerCase().split("\\.");
		for (String part : parts) {
			test = test || part.endsWith("test");
		}
		return test;
	}
	
	public static void writeLineToCsv(String pathToCsv, String content) throws IOException {
		Path csvPath = Paths.get(pathToCsv);
		if(Files.notExists(csvPath, LinkOption.NOFOLLOW_LINKS)) {
			Files.createFile(csvPath);
		}
		Files.write(csvPath, (content+"\n").getBytes(), StandardOpenOption.APPEND);
	}
	
	public static boolean isMethod(String entityName) {
		//only a function namespace will contain brackets
		return entityName.contains("(") && entityName.contains(")");
	}
}
