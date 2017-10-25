package DynamicDuo.RefactoringUtils;

import java.io.IOException;

import org.eclipse.jdt.core.compiler.InvalidInputException;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import DynamicDuo.IO.IOHandler;
import DynamicDuo.IO.LineHandler;
import DynamicDuo.Study.StudyConstants;

public class ProductionClassPairRepository {
	
	private static ProductionClassPairRepository instance;
	
	private BiMap<String, String> classPairs;
	
	private ProductionClassPairRepository() {
		classPairs = HashBiMap.create();
		initializeClassPairs();
	}
	
	public static synchronized ProductionClassPairRepository getInstance() {
		if(instance == null) {
			instance = new ProductionClassPairRepository();
		}
		return instance;
	}
	
	public boolean testClassExists(String testClass) {
		return classPairs.containsKey(testClass.toLowerCase());
	}
	
	public String getPairedProductionClass(String testClass) {
		return classPairs.get(testClass.toLowerCase());
	}
	
	public String getPairedTestClass(String prodClass) {
		return classPairs.inverse().get(prodClass.toLowerCase());
	}

	public boolean productionClassExists(String prodClass) {
		return classPairs.containsValue(prodClass.toLowerCase());
	}
	
	private void initializeClassPairs() {
		try {
			IOHandler.readLines(StudyConstants.CSV_Class_Pairs, new LineHandler() {
				@Override
				public void handle(String line) {
					String[] classPair = line.split(","); //0 = commithash, 1 = production class, 2 = test class
					if(classPair.length < 3) {
						System.err.println("csv file contains invalid data");
						return;
					}
					if(!(classPairs.containsKey(classPair[2].toLowerCase()) || classPairs.containsValue(classPair[1].toLowerCase()))) {
						classPairs.put(classPair[2].toLowerCase(), classPair[1].toLowerCase());
					}
				}
			});
		} catch (IOException e) {
			//error reading the file..
			e.printStackTrace();
			System.exit(1); //stop because we cannot continue without this class properly initialized.
		}
	}
}
