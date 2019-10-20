package mtgengine.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.Timer;
import javax.swing.event.MouseInputAdapter;

import mtgengine.Game;

public class MtgeGuiMouseAdapter extends MouseInputAdapter {
	private UIPanel _panel;
	private Timer _timer;
	private UICard _cardHovered;
	private UICard _lastHovered = null;
	
	public MtgeGuiMouseAdapter(UIPanel panel) {
		_panel = panel;
		_timer = new Timer(600, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				_timer.stop();
				if (_panel._actionButtons.size() == 0)
				{
					_panel._app.send(Game.COMMAND_GET_CARD_INFO + " " + _cardHovered.getIdCard());
					_panel._cardInfoRequest = _cardHovered;
					if (_panel.getClass() == UIZonePanel.class) {
						_panel.repaint();
					}	
				}
			}
		});
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		
		_cardHovered = _panel.getCardAtPosition(x, y);
		if (_cardHovered != null)
		{
			if (_lastHovered != _cardHovered) {
				_lastHovered = _cardHovered;
				if (_panel._actionButtons.size() == 0)
					_timer.start();
			}
		}
		else
		{
			_lastHovered = null;
			_cardHovered = null;
			_panel._cardInfoRequest = null;
			_timer.stop();
			_panel.repaint();
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		_panel.computeMouseClick(x, y, e.getButton());
		
		_lastHovered = null;
		_cardHovered = null;
		_panel._cardInfoRequest = null;
		_timer.stop();
		_panel.repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {}

	@Override
	public void mouseReleased(MouseEvent arg0) {}
}
