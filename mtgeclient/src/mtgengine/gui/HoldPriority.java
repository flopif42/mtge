package mtgengine.gui;

import mtgengine.Game.Step;

public class HoldPriority {
	private Step _step;
	private boolean _myTurn;
	private boolean _hisTurn;
	
	public HoldPriority(Step step, boolean my, boolean his) {
		_step = step;
		_myTurn = my;
		_hisTurn = his;
	}
	
	public Step getStep() {
		return _step;
	}

	public boolean getMy() {
		return _myTurn;
	}
	
	public boolean getHis() {
		return _hisTurn;
	}

	public void set(boolean my, boolean his) {
		_myTurn = my;
		_hisTurn = his;
	}
	
	/*
	public String toString() {
		return _step.name() + ":" + _myTurn + ";" + _hisTurn;
	}
	*/
}
