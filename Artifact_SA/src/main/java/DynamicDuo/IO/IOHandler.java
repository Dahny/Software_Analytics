package DynamicDuo.IO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class IOHandler {
	
	public static void writeLineToCsv(String pathToCsv, String content) throws IOException {
		Path csvPath = Paths.get(pathToCsv);
		if(Files.notExists(csvPath, LinkOption.NOFOLLOW_LINKS)) {
			Files.createFile(csvPath);
		}
		Files.write(csvPath, (content+"\n").getBytes(), StandardOpenOption.APPEND);
	}
	
	public static void readLines(String pathToCsv, LineHandler lineHandler) throws IOException {
		Path csvPath = Paths.get(pathToCsv);
		if(Files.notExists(csvPath, LinkOption.NOFOLLOW_LINKS)) {
			return;
		}
		for(String line : Files.readAllLines(csvPath)){
			lineHandler.handle(line);
		}
	}
}
