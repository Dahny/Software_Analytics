package DynamicDuo.Models;

public class ClassPairModel {
	private String testClass;
	private String productionClass;
	
	public ClassPairModel(String test, String production) {
		setTestClass(test);
		setProductionClass(production);
	}

	public String getTestClass() {
		return testClass;
	}

	public void setTestClass(String testClass) {
		this.testClass = testClass;
	}

	public String getProductionClass() {
		return productionClass;
	}

	public void setProductionClass(String productionClass) {
		this.productionClass = productionClass;
	}
}
