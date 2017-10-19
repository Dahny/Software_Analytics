package DynamicDuo.Study;

public class StudyConstants {
	public static final String Repo_Url = "https://github.com/elastic/elasticsearch.git"; //"https://github.com/tuplejump/MapDB.git"
	public static final String Repo_Name = "elasticsearch";
	
	public static final String Repo_Path_Absolute = "C:\\Temp\\"; // C:/temp/...
	public static final String Repo_Path_Relative = "\\Temp";
	
	public static final char CSV_Delimiter = ',';
	
	public static final String CSV_ALL_REFACTORS_Path = "../"+StudyConstants.Repo_Name+"-all-test-refactors.csv";
	public static final String CSV_Monthly_Metrics = String.format("../%s-%s.csv", StudyConstants.Repo_Name, "monthly-metrics");
	public static final String CSV_Refactors = String.format("../%s-%s.csv", StudyConstants.Repo_Name, "refactors");
	public static final String CSV_Metrics = String.format("../%s-%s.csv", StudyConstants.Repo_Name, "metrics");
	public static final String CSV_Class_Pairs = String.format("../%s-%s.csv", StudyConstants.Repo_Name, "class-pairs");
	
	public static final int Commits_To_Skip_In_Delayed_Checker = 10;
	public static final int Versions_To_Check_In_Delayed_Checker = 5;
}
