package mtgengine;

import mtgengine.Target.Category;

public class TargetRequirement {
	public enum Cardinality {
		ONE, TWO, THREE,
		ONE_OR_TWO,
		UP_TO_ONE, UP_TO_TWO, UP_TO_THREE, UP_TO_FOUR, UP_TO_X,
		X
	} ;
	
	private Category _category;
	private Cardinality _cardinality;
	
	public TargetRequirement(Category targetCategory, Cardinality cardinality) {
		_category = targetCategory;
		_cardinality = cardinality;
	}
	
	public Category getCategory() {
		return _category;
	}
	
	public Cardinality getCardinality() {
		return _cardinality;
	}
}
