package DynamicDuo.Study;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKNumber;
import com.github.mauricioaniche.ck.CKReport;

public class StudyUtils {

	public static boolean isTestClass(String className) {
		boolean test = false;
		String[] parts = className.toLowerCase().split("\\.");
		for (String part : parts) {
			test = test || part.endsWith("test");
		}
		return test;
	}

	public static boolean isMethod(String entityName) {
		// only a function namespace will contain brackets
		return entityName.contains("(") && entityName.contains(")");
	}

	public static String findTestPair(String prodName) {
		boolean main = false;
		String[] parts = prodName.toString().toLowerCase().split("\\\\");

		for (String part : parts) {
			main = main || part.equals("main");
		}

		StringBuilder path = new StringBuilder();
		if (main) {
			boolean notFound = true;

			for (String part : parts) {
				path.append(part);
				if (path.toString().endsWith("src") && fileExists(path.toString() + "\\test")) {
					path.append("\\test");
					notFound = false;
					break;
				}
				path.append("\\");
			}
			if (notFound) {
				return "no testfile found";
			}
		}
		String name = parts[parts.length - 1];
		name = name.substring(0, name.length() - 5);
		return findTestFile(path.toString(), name);
	}

	public static boolean fileExists(String path) {
		File f = new File(path);
		return f.exists();
	}

	private static String findTestFile(String path, String prodName) {
		String testPath = "no testfile found";
		try (Stream<Path> paths = Files.walk(Paths.get(path))) {
			Optional<Path> opt = paths
					.filter(p -> p.getFileName().toString().toLowerCase().equals(prodName + "test.java")).findFirst();

			if (opt.isPresent()) {
				testPath = opt.get().toString();
			}
			return testPath;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return testPath;
	}

	/**
	 * @param loc
	 *            Line of codes
	 * @param wmc
	 *            Weight method class
	 * @param hsv
	 *            HalsteadValue
	 * @return maintainabilityIndex
	 */
	public static int getMaintainabilityIndex(int loc, int wmc, double hsv) {
		return (int) Math
				.round(Math.max(0, (171 - (5.2 * Math.log(hsv)) - (0.23 * wmc) - (16.2 * Math.log(loc))) * 100 / 171));
	}

	public static CKNumber getMetricsForFile(String pathToFile) {
		CK ck = new CK();
		String path = StudyUtils.getPathToContainingFolder(pathToFile);
		String fileName = StudyUtils.getFileNameFromPath(pathToFile);
		System.out.println("CALCULATING CK FOR FILES ON: " + path);
		CKReport report = ck.calculate(path);
		return report.get(path.toString() + fileName);
	}

	public static String getPathToContainingFolder(String pathToFile) {
		StringBuilder path = new StringBuilder(StudyConstants.Repo_Path_Absolute + StudyConstants.Repo_Name + "\\");
		String[] parts = pathToFile.split("/", -1);
		parts[parts.length - 1] = "";
		for (String part : parts) {
			path.append(part).append("\\");
		}
		path.delete(path.length() - 2, path.length() - 1);
		return path.toString();
	}

	public static String getFileNameFromPath(String pathToFile) {
		String[] parts = pathToFile.split("/", -1);
		return parts[parts.length - 1];
	}

	public static String getAbsolutePathToFile(String pathToFile) {
		return getPathToContainingFolder(pathToFile) + getFileNameFromPath(pathToFile);
	}

	public static String getClassPathFromNameSpace(String namespace) {
		String reverseClassNamespace = new StringBuffer(namespace).reverse().toString().split(".", 1)[0];
		String classNameSpace = new StringBuffer(reverseClassNamespace).reverse().toString();
		return classNameSpace.replace('.', '/') + ".java";
	}

	public static String getClassPathFromRefactoringDescription(String description) {
		String[] refStringParts = description.split("in class ");// index 1
																	// should
																	// contain
																	// the class
																	// namespace
		if (refStringParts.length > 1) {
			return getClassPathFromNameSpace(refStringParts[1]);
		}
		return null;
	}

	public static String readFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, "UTF-8");
	}

}
