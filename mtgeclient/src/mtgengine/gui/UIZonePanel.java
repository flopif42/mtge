package mtgengine.gui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;

import mtgengine.Game;

public class UIZonePanel extends UIPanel {
	private static final long serialVersionUID = -3567635225986584440L;

	private Board _board;
	private UIZone _zone;
	
	public UIZonePanel(Board board) {
		_board = board;
	}
	
	public void setZone(UIZone zone) {
		_zone = zone;
	}
	
	public void doCardClick(UICard cardClicked) {
		super.doCardClick(cardClicked);
		_board._app.setLastPanelClicked(this);
		
		if ((this.getClass() == UIZonePanel.class) && (this == _board._libraryPanel)) {
			_board.closeLibPanel(this);
		}
	}
	
	void clearZone() {
		_zone.clear();
	}

	public UICard getCardAtPosition(int x, int y) {
		int i = _zone.getCards().size();
		while (i > 0) {
			UICard card = _zone.getCards().get(i-1);
			if (card.hit(x, y))
				return card;
			i--;
		}

		return null;
	}
	
	public void doButtonClick(UIButton clickedButton) {
	 	_app.send(Game.COMMAND_PERFORM_ACTION + " " + clickedButton.getSourceId() + " " + clickedButton.getActionId());
	 	_app.setAutoPassPriority(true);
	}
	
	public void paintComponent(Graphics g) {
		g.setColor(Color.gray);
		g.fillRect(0, 0, getWidth(), getHeight());
		int nbCards = _zone.size();
		int x = 0;
		int y = 0;
		int iCol = 0;
		int nbCol = getWidth() / (Board.CARD_WIDTH + Board.CARD_SPACING);

		for (int i = 0; i < nbCards; i++) {
			_board.drawCard(g, _zone.get(i), x, y);
			iCol++;
			x += Board.CARD_WIDTH + Board.CARD_SPACING;
			if (iCol % nbCol == 0) {
				x = 0;
				y += 30;
			}
		}
		if (_cardInfoRequest != null)
			drawCardInfo(g);
		
		// activated abilities buttons
		if (_actionButtons.size() > 0) {
			UICard lastCardClicked = _app.getLastCardClicked();
			drawActivatedAbilitiesButtons(g, lastCardClicked.x + Board.CARD_WIDTH, lastCardClicked.y);
		}
	}
	
	public void close() {
		this.getParent().setVisible(false);
		((JFrame)this.getParent()).dispose();
	}

	@Override
	protected UIManaChoice getManaChoiceAtPosition(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
