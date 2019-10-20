package mtgengine.gui;

public class UIFrame {
	public int width;
	public int height;
	public int x;
	public int y;

	public void setDimensions(int w, int h) {
		width = w;
		height = h;
	}

	public void setLocation(int lx, int ly) {
		x = lx;
		y = ly;
	}
}
