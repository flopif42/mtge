package mtgengine.mana;

import java.util.HashMap;

import mtgengine.mana.Mana.ManaType;

public class ManaPool {
	private HashMap<ManaType, Integer> _manas;
	
	public ManaPool() {
		_manas = new HashMap<ManaType, Integer>();
		this.empty();
	}
	
	/**
	 * 
	 * @param type
	 * @param number
	 */
	public void add(ManaType type, int number) {
		int oldValue;
		
		if (_manas.containsKey(type)) {
			oldValue = _manas.get(type);
		}
		else {
			oldValue = 0;
		}
			
		_manas.put(type, oldValue + number);
	}
	
	/**
	 * 
	 * @return
	 */
	public int getTotalMana() {
		int sum = 0;
		
		for (ManaType type : ManaType.values())
			sum += _manas.get(type);
		return sum;
	}
	
	public void empty() {
		for (ManaType mt : ManaType.values())
			_manas.put(mt, 0);
	}
	
	public String toString() {
		return String.format("%d|%d|%d|%d|%d|%d|",
				_manas.get(ManaType.WHITE),
				_manas.get(ManaType.BLUE),
				_manas.get(ManaType.BLACK),
				_manas.get(ManaType.RED),
				_manas.get(ManaType.GREEN),
				_manas.get(ManaType.COLORLESS));
	}
}
