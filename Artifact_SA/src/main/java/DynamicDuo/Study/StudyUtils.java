package DynamicDuo.Study;

public class StudyUtils {
	
	public static boolean isTestClass(String className) {
		boolean test = false;
		String[] parts = className.toLowerCase().split("\\.");
		for (String part : parts) {
			test = test || part.endsWith("test");
		}
		return test;
	}
}
