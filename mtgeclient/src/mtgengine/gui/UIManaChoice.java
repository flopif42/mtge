package mtgengine.gui;

public class UIManaChoice {
	private int _number;
	private int _x;
	private int _y;
	private int _w;
	private int _h;
	
	public UIManaChoice(int number, int x, int y, int w, int h) {
		_number = number;
		_x = x;
		_y = y;
		_w = w;
		_h = h;
	}
	
	public int getNumber() {
		return _number;
	}
	
	public int getX() {
		return _x;
	}
	
	public int getY() {
		return _y;
	}
	
	public int getW() {
		return _w;
	}
	
	public int getH() {
		return _h;
	}
	
	public boolean hit(int lx, int ly) {
		if ((lx >= _x) && (lx <= _x + _w) && (ly >= _y) && (ly <= _y + _h))
			return true;
		return false;
	}

}
