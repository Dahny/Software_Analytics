package DynamicDuo.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CsvFileWriter {
	
	public static void writeLineToCsv(String pathToCsv, String content) throws IOException {
		Path csvPath = Paths.get(pathToCsv);
		if(Files.notExists(csvPath, LinkOption.NOFOLLOW_LINKS)) {
			Files.createFile(csvPath);
		}
		Files.write(csvPath, (content+"\n").getBytes(), StandardOpenOption.APPEND);
	}
}
