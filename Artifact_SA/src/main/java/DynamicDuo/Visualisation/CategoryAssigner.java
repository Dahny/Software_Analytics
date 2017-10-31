package DynamicDuo.Visualisation;

public class CategoryAssigner {
	
	public static Category getCategory(String categoryName, int val) {
		switch(categoryName) {
			case "WMC": return getWMCCategory(val);
			case "NOM": return getNOMCategory(val);
			case "NOF": return getNOFCategory(val);
			case "LOC": return getLOCCategory(val);
			default:
				System.err.println("error: invalid category " + categoryName);
				return null;
		}
	}
	
	private static Category getWMCCategory(int val) {
		Category cat = Category.HIGH;
		if(val < 46) cat = Category.MEDIUM;
		if(val < 26) cat = Category.LOW;
		if(val < 17) cat = Category.VERY_LOW;
		return cat;
	}
	
	private static Category getNOMCategory(int val) {
		Category cat = Category.HIGH;
		if(val < 20) cat = Category.MEDIUM;
		if(val < 13) cat = Category.LOW;
		if(val < 9) cat = Category.VERY_LOW;
		return cat;
	}
	
	private static Category getNOFCategory(int val) {
		Category cat = Category.HIGH;
		if(val < 9) cat = Category.MEDIUM;
		if(val < 5) cat = Category.LOW;
		if(val < 3) cat = Category.VERY_LOW;
		return cat;
	}
	
	private static Category getLOCCategory(int val) {
		Category cat = Category.HIGH;
		if(val < 281) cat = Category.MEDIUM;
		if(val < 173) cat = Category.LOW;
		if(val < 126) cat = Category.VERY_LOW;
		return cat;
	}
}
