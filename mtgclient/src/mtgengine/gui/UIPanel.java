package mtgengine.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JPanel;

import mtgengine.Game;

public abstract class UIPanel extends JPanel {
	static final int AURA_Y_OFFSET = 20;
	static final int AURA_X_OFFSET = 20;
	
	static final int BUTTON_SPACING = 3;
	static final int BUTTON_MARGIN = 4;
	static final int BORDER_MARGIN = 20; // minimum space between any zone and the window borders
	
	protected static final Color TooltipColor = new Color(255, 247, 178);
	private final static int ACTIVATED_ABILITY_BUTTON_WIDTH = 171;
	
	// Card info related data
	protected UICard _cardInfoRequest;
	protected Vector<UIButton> _actionButtons;
	protected Vector<UIButton> _UIButtons;
	
	protected MtgeClient _app;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8763428553223971278L;

	protected abstract void doButtonClick(UIButton button);
	protected abstract UICard getCardAtPosition(int x, int y);
	protected abstract UIManaChoice getManaChoiceAtPosition(int x, int y);
	
	public UIPanel() {
		_actionButtons = new Vector<UIButton>();
		_UIButtons = new Vector<UIButton>();
	}
	
	/**
	 * 
	 * @param app
	 */
	public void setApp(MtgeClient app) {
		_app = app;
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	protected UIButton getClickedButton(int x, int y) {
		x += getX();
		y += getY();
		
		Vector<UIButton> allButtons = new Vector<UIButton>();
		allButtons.addAll(_UIButtons);
		allButtons.addAll(_actionButtons);
		
		for (UIButton button : allButtons) {
			if ((x >= button.x) && (x <= button.x + button.width) && (y >= button.y) && (y <= button.y + button.height)) {
				if (button.isEnabled())
					return button;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param text
	 * @param width
	 * @param fm
	 * @return
	 */
	private String multiLineCut(String text, int width, Graphics g) {
		int CRLF;
		int iSemicolon = text.indexOf(";");
		int iParagraph = text.indexOf("§");
		
		if ((iSemicolon != -1) && (iParagraph != -1))
			CRLF = Math.min(iSemicolon, iParagraph);
		else if (iSemicolon == -1)
			CRLF = iParagraph;
		else if (iParagraph == -1)
			CRLF = iSemicolon;
		else CRLF = -1;
		
		if (CRLF != -1)
			text = text.substring(0, CRLF);

		int lastSpace;

		while (Board.getTextWidth(g, text) > width) {
			lastSpace = text.lastIndexOf(" ");
			if (lastSpace != -1)
				text = text.substring(0, lastSpace);
			else
				return null;
		}			
		return text;
	}
	
	/**
	 * 
	 * @param text
	 * @param width
	 * @param fm
	 * @return
	 */
	protected Vector<String> multiLine(String text, int width, Graphics g) {
		Vector<String> ret = new Vector<String>();
		String str;
		
		do {
			str = multiLineCut(text, width, g);
			if (str != null)
			{
				if (str.length()>0)
					ret.add(str);
				if (text.equals(str))
					str = null;
				else
					text = text.substring(str.length()+1);
			}
		} while (str != null);
		return ret;
	}
	
	/**
	 * 
	 * @param g
	 */
	protected void drawCardInfo(Graphics g) {
		String cardInfo = _app.getCardInfo();
		if (cardInfo == null)
			return;
			
		int xOffset = 30;
		int x = Math.max(_cardInfoRequest.x - xOffset, 5);
		int y = _cardInfoRequest.y + Board.CARD_HEIGHT/4;
		
		int indexColon = cardInfo.indexOf(";");
		String manaCost = cardInfo.substring(0, indexColon);
		cardInfo = cardInfo.substring(indexColon + 1);
		cardInfo = cardInfo.replace("MorphCreature", "(no name)");
		UIButton temp = new UIButton(cardInfo);
		Point p = drawButton(g, temp, x, y, Board.CARD_WIDTH + (xOffset*2), TooltipColor);
		if (!manaCost.equals("null"))
			Board.drawManaCost(g, manaCost, p.x + (Board.CARD_WIDTH + (xOffset*2) - 10), p.y + 4);
	}
	
	/**
	 *  used to force HEIGHT of a button rather than have it computed
	 * @param g
	 * @param button
	 * @param lx
	 * @param ly
	 * @param width
	 * @param height
	 * @param fillColor
	 */
	private void drawButton(Graphics g, UIButton button, int lx, int ly, int width, int height, Color fillColor) {
		Color textColor;
		
		if (button.isEnabled())
			textColor = Color.BLACK;
		else
			textColor = Color.LIGHT_GRAY;

		Vector<String> text = multiLine(button.getLabel(), width, g);
		
		int fontHeight = g.getFontMetrics().getHeight();
		
		int w = width + BUTTON_MARGIN * 2;
		int h = height + BUTTON_MARGIN * 2;
		button.setDimensions(w, h);
		
		int x = lx;
		int y = ly;
		button.setLocation(x, y);
		
		// Draw button back
		g.setColor(fillColor);
		g.fillRect(x, y, w, h);
		
		// Draw button frame
		g.setColor(Color.BLACK);
		g.drawRect(x, y, w, h);
		
		// Draw text
		g.setColor(textColor);
		
		for (String line : text) {
			y += fontHeight;
			Board.drawText(g, line, x + BUTTON_MARGIN,  y);
		}
	}
	
	/**
	 * 
	 * @param g
	 * @param button
	 */
	protected Point drawButton(Graphics g, UIButton button, int lx, int ly, int width, Color fillColor) {
		Vector<String> text = multiLine(button.getLabel(), width, g);
		int fontHeight = g.getFontMetrics().getHeight();
		int height = fontHeight * text.size();
		
		if (ly + height > getHeight())
			ly = (getHeight() - height) - BORDER_MARGIN;
		
		if (lx + width > getWidth())
			lx = (getWidth() - width) - BORDER_MARGIN;
		
		drawButton(g, button, lx, ly, width, height, fillColor);
		return new Point(lx, ly);
	}
	
	/**
	 * 
	 * @param g
	 * @param x
	 * @param y
	 */
	protected void drawActivatedAbilitiesButtons(Graphics g, int x, int y) {
		if (x + ACTIVATED_ABILITY_BUTTON_WIDTH > getWidth()) {
			x = getWidth() - ACTIVATED_ABILITY_BUTTON_WIDTH;
		}
		for (UIButton btnAbility : _actionButtons) {
			drawButton(g, btnAbility, x, y, ACTIVATED_ABILITY_BUTTON_WIDTH, TooltipColor);
			y += btnAbility.height + BUTTON_SPACING;
		}		
	}
	
	/**
	 * 
	 * @param cardClicked
	 */
	public void doCardClick(UICard cardClicked) {
		_app.setLastCardClicked(cardClicked);
		int idCard = cardClicked.getIdCard(); 
		switch (_app.getGameState()) {
		case Ready:
			_app.send(Game.COMMAND_GET_ACTIONS + " " + idCard);
			break;

		default:
			_app.send(idCard);
			break;
		}
		System.out.println("Clicked card : " + cardClicked.getName() + " with ID=" + cardClicked.getIdCard());
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param mouseButtonClicked
	 */
	public void computeMouseClick(int x, int y, int mouseButtonClicked) {
		switch (mouseButtonClicked) {
		case MouseEvent.BUTTON1: // left button click
			// check if an UI button was clicked, if not, check if a card was clicked
			UIButton button = getClickedButton(x, y);
			if (button != null) {
				doButtonClick(button);
			}
			else {
				UIManaChoice mc = getManaChoiceAtPosition(x, y);
				if (mc != null) {
					_app.send(mc.getNumber());
				}
				else {
					UICard card = getCardAtPosition(x, y);
					if (card != null)
						doCardClick(card);
				}
			}

			if (_actionButtons.size() > 0) {
				_actionButtons.clear();
				repaint();
			}
			break;

		case MouseEvent.BUTTON2: // middle button (wheel) click
			break;
			
		default:
			break;
		}
	}
	
	/**
	 * 
	 * @param possibleActions
	 * @param sourceId
	 */
	public void buildActionButtons(UIZone possibleActions, int sourceId) {
		// Possible actions
		UIButton btnAaction;
		UICard action;
		
		for (int i = 0; i < possibleActions.size(); i++) {
			action = possibleActions.get(i);
			btnAaction = new UIButton(action.getRulesText());
			btnAaction.setActionData(sourceId, action.getIdCard());
			_actionButtons.add(btnAaction);
		}

	}
}
