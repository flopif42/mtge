package mtgengine.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import mtgengine.Game;
import mtgengine.Game.Phase;
import mtgengine.Game.State;
import mtgengine.Game.Step;

public class Board extends UIPanel  {
	private HashMap<String, BufferedImage> _cacheImages = new HashMap<String, BufferedImage>();

	public Object lock = new Object();
	public boolean bReadyToCompute = false;
	public boolean bInitializationOK = false;
	private boolean _bDebugMode = false;
	public boolean bDisplayImages = true;

	private static final long serialVersionUID = -857936816172803660L;
	static final String CARD_IMAGES_PATH = "E:\\Work\\mtge\\MTGE Client\\card_images\\";
	static final String CLIENT_IMAGES_PATH = "images/";
	private static final String IMAGE_URL = "http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=";

	final static int ID_STATUS_DEBUGMODE        = 0;
	final static int ID_STATUS_TURN             = 1;
	final static int ID_STATUS_PHASE            = 2;
	final static int ID_STATUS_STEP             = 3;
	final static int ID_STATUS_STATE            = 4;
	final static int ID_STATUS_FIRST_PLAYER     = 5;

	/* Large screen */	
	//	static final int WINDOW_WIDTH = 1280;
	//	static final int WINDOW_HEIGHT = 1120;
	//	static final int CARD_WIDTH = 119;
	//	static final int CARD_HEIGHT = 171;

	/* Small screen */
	static final int WINDOW_WIDTH = 1280;
	static final int WINDOW_HEIGHT = 1000;
	static final int CARD_WIDTH = 97;
	static final int CARD_HEIGHT = 140;

	static final int CARD_SPACING = 5;
	static final int BUTTON_MAX_WIDTH = 150;
	private final static int INTERFACE_BUTTON_WIDTH = 80;
	private final static int STATUS_BUTTON_WIDTH = 171;
	private static final int STATUS_BUTTON_HEIGHT = 80;
	static final int ZONE_SPACING = 10; // minimum space between each zone
	static final int ZONE_OUTLINE = 1; // space between the zone borders and the cards inside the zone

	private BufferedImage _wallpaper;
	private String _targetor;
	private int _xValue = 1;

	public static HashMap<Integer, Vector<Integer>> _auras = new HashMap<Integer, Vector<Integer>>();

	// Mana choice related stuff
	private static final int MANACHOICE_SYMBOL_WIDTH = 40;
	private static final int MANACHOICE_SYMBOL_HEIGHT = 40;
	private static final int MANACHOICE_COMBINATION_SPACING = 12;
	private String _manaChoices[];


	public static void addAura(int hostID, int auraID) {
		Vector<Integer> auras;

		if (!_auras.containsKey(hostID)) {
			auras = new Vector<Integer>();
			auras.add(auraID);
			_auras.put(hostID, auras);
		}
		else {
			auras = _auras.get(hostID);
			auras.add(auraID);
		}
	}

	private Vector<UIManaChoice> _manaChoicesButtons = new Vector<UIManaChoice>();

	private Vector<HoldPriority> _holdPrioritiesSettings = new Vector<HoldPriority>();

	/* Players */
	private Vector<UIPlayer> _players = new Vector<UIPlayer>();

	/* Zones */
	private Vector<UIZone> _zones;

	private UIZone _library;
	private UIZone _libManipulation;
	private UIZone _oppHand;
	public static UIZone _possibleActions;
	UIZone _stack;

	/* Player data */
	private int _idPlayer;
	private int _idOpponent;
	private int _idActivePlayer;
	private Phase _phase;
	private Step _step;

	public Vector<Integer> _highlights = new Vector<Integer>();

	// attacking creatures
	public Vector<Integer> _undeclaredPotentialAttacker = new Vector<Integer>();
	public Vector<Integer> _declaredPotentialAttacker = new Vector<Integer>();
	public Vector<Integer> _blockableAttacker = new Vector<Integer>();
	private HashMap<Integer, Integer> _attacks = new HashMap<Integer, Integer>();

	// attack recipients
	public Vector<Integer> _attackableRecipient = new Vector<Integer>();

	// blocking creatures
	public Vector<Integer> _undeclaredPotentialBlocker = new Vector<Integer>();
	public Vector<Integer> _declaredPotentialBlocker = new Vector<Integer>();
	private HashMap<Integer, Integer> _blocks = new HashMap<Integer, Integer>();

	/* Buttons */
	private UIButton _btnPassPriority;
	private UIButton _btnYes;
	private UIButton _btnNo;
	private UIButton _btnDone;
	private UIButton _btnPlus;
	private UIButton _btnMinus;
	private UIButton _btnXValue;
	private UIFrame _statusFrame;

	int _turn;
	int _nextTurn;

	boolean _bF4_activated = false;
	boolean _bF2_activated = false;

	private static Vector<UICard> _allCards;

	private String _response;



	private UIZonePanel _libManipulationPanel = null;
	private UIZonePanel _oppHandPanel = null;
	UIZonePanel _libraryPanel;

	private UIButton createButton(String label) {
		UIButton newButton = new UIButton(label);
		_UIButtons.add(newButton);
		return newButton;
	}

	private void drawStatus(Graphics g) {
		drawStatusFrame(g, BORDER_MARGIN, BUTTON_SPACING + _btnXValue.y + _btnXValue.height, STATUS_BUTTON_WIDTH, STATUS_BUTTON_HEIGHT, TooltipColor);
		drawPlayerFrame(g, findPlayerByID(_idOpponent), BORDER_MARGIN);
		drawPlayerFrame(g, findPlayerByID(_idPlayer), _statusFrame.y + _statusFrame.height + BUTTON_SPACING);
	}

	private String computeManaPoolString(UIPlayer player) {
		int nbMana;
		String ret = "";

		for (String manaType : player.getManaPool().keySet()) {
			if ((nbMana = player.getManaAmount(manaType)) > 0)
				ret += String.format("{%s}:%d ", manaType, nbMana);
		}
		return ret;
	}

	private void drawPlayerFrame(Graphics g, UIPlayer player, int yPos) {
		// Player button with name and life total
		Color buttonColor;
		UIButton playerBtn = player.getNameButton();
		if ((_app.getGameState() == State.PromptTargets) && (_highlights.contains(playerBtn.ID)))
			buttonColor = Color.YELLOW;
		else if ((_app.getGameState() == State.WaitChooseRecipientToAttack) && (_attackableRecipient.contains(playerBtn.ID)))
			buttonColor = Color.YELLOW;
		else
			buttonColor = Color.WHITE;
		drawButton(g, playerBtn, _statusFrame.x, yPos, 112, buttonColor);
		if (player.isMonarch())
			drawButton(g, new UIButton("|^^^|"), _statusFrame.x + playerBtn.width + BUTTON_SPACING, yPos, 50, Color.WHITE);
		yPos += playerBtn.height + BUTTON_SPACING;

		int xPos = _statusFrame.x;

		// Mana pool
		String manaPoolString = computeManaPoolString(player);
		if (manaPoolString.length() > 0) {
			UIButton manaPoolButton = new UIButton(manaPoolString);
			drawButton(g, manaPoolButton, xPos, yPos, 176, Color.WHITE);
			yPos += playerBtn.height + BUTTON_SPACING;
		}

		// zone buttons with number of card (library, hand, graveyard, exile)
		UIButton button = new UIButton("LIB : " + player.getLibSize());
		drawButton(g, button, xPos, yPos, 50, Color.WHITE);
		xPos += 62;

		button = new UIButton("HND : " + player.getHandSize());
		drawButton(g, button, xPos, yPos, 50, Color.WHITE);
		xPos = _statusFrame.x;
		yPos += playerBtn.height + BUTTON_SPACING;

		UIButton graveButton = new UIButton("GYD : " + player.getGraveyard().size());
		drawButton(g, graveButton, xPos, yPos, 50, Color.WHITE);
		xPos += 62;

		UIButton exileButton = new UIButton("EXL : " + player.getExile().size());
		drawButton(g, exileButton, xPos, yPos, 50, Color.WHITE);
		xPos += 62;

		UIButton commandButton = new UIButton("CMD : " + player.getCommand().size());
		drawButton(g, commandButton, xPos, yPos, 50, Color.WHITE);

		player.setButtons(graveButton, exileButton, commandButton);
	}

	private void initButtons() {
		// Button containers
		// Buttons
		_btnPassPriority = createButton("OK (F2)");
		_btnYes = createButton("Yes");
		_btnNo = createButton("No");
		_btnMinus = createButton("-");
		_btnPlus = createButton("+");
		_btnDone = createButton("Done");
		_btnXValue = createButton("X = 1");

		// Status window
		_statusFrame = new UIFrame();
	}

	private void initZones() {
		_library = new UIZone("My library");
		_libManipulation = new UIZone("Library manipulation");
		_oppHand = new UIZone("Opponent's hand");
		_possibleActions = new UIZone("Activated abilities");
		_stack = new UIZone("Stack");
		_zones = new Vector<UIZone>();
		_zones.add(_library);
		_zones.add(_stack);
		_zones.add(_possibleActions);
	}

	public void setHoldPrioritySetting(Step step, boolean my, boolean his) {
		getHPSetting(step).set(my, his);
	}

	private void initHoldPrioritySettings() {
		for (Step step : Game.Step.values()) {
			if (step != Step.Untap)
				_holdPrioritiesSettings.add(new HoldPriority(step, false, false));
		}

		// Priority hold settings are initialized here
		setHoldPrioritySetting(Step.Main, true, false);
		setHoldPrioritySetting(Step.End, false, true);
	}

	public Board() {
		super();
		initButtons();
		initZones();
		initHoldPrioritySettings();

		_allCards = new Vector<UICard>();

		File wallpaperFile = new File(CLIENT_IMAGES_PATH + "wallpaper.jpg");
		try {
			_wallpaper = ImageIO.read(wallpaperFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void drawManaCost(Graphics g, String manaCost, int x, int y) {
		int size = 15;
		int indexStart;
		int indexEnd;
		String symbol;

		indexStart = manaCost.lastIndexOf("{");
		while (indexStart != -1) {
			indexEnd = manaCost.lastIndexOf("}");
			symbol = manaCost.substring(indexStart, indexEnd+1);
			g.drawImage(ManaSymbols.getImage(symbol), x, y, size, size, null);
			x -= size;
			manaCost = manaCost.substring(0, indexStart);
			indexStart = manaCost.lastIndexOf("{");
		}
	}

//	private static int myDrawString(Graphics g, String text, int x, int y) {
//		Font currentFont = g.getFont();
//		int width;
//		String regularText;
//		String italicsText;
//
//		int italicsTagOpen = text.indexOf("<i>");
//		int italicsTagClose = text.indexOf("</i>");
//		if (italicsTagOpen != -1)
//		{
//			if (italicsTagClose != -1) {
//				regularText = text.substring(0, italicsTagOpen);
//				italicsText = text.substring(italicsTagOpen + 3, italicsTagClose);
//			}
//			else
//			{
//				regularText = text.substring(0, italicsTagOpen);
//				italicsText = text.substring(italicsTagOpen + 3);
//			}
//			g.drawString(regularText, x,  y);
//			g.setFont(new Font(currentFont.getFontName(), Font.ITALIC, currentFont.getSize()));
//			g.drawString(italicsText, x + g.getFontMetrics().stringWidth(regularText),  y);
//			width = g.getFontMetrics().stringWidth(italicsText) + g.getFontMetrics().stringWidth(regularText);
//			if (italicsTagClose != -1)
//				g.setFont(new Font(currentFont.getFontName(), Font.PLAIN, currentFont.getSize()));
//		}
//		else if (italicsTagClose != -1)
//		{
//			italicsText = text.substring(0, italicsTagClose);
//			regularText = text.substring(italicsTagClose + 4);
//			g.drawString(italicsText, x,  y);
//			g.setFont(new Font(currentFont.getFontName(), Font.PLAIN, currentFont.getSize()));
//			g.drawString(regularText, x + g.getFontMetrics().stringWidth(italicsText),  y);
//			width = g.getFontMetrics().stringWidth(regularText) + g.getFontMetrics().stringWidth(italicsText);
//		}
//		else {
//			g.drawString(text, x,  y);
//			width = g.getFontMetrics().stringWidth(text);
//		}
//		return width;
//	}
	
	private static void setFontItalic(Graphics g) {
		Font currentFont = g.getFont();
		g.setFont(new Font(currentFont.getFontName(), Font.ITALIC, currentFont.getSize()));
	}

	private static void setFontRegular(Graphics g) {
		Font currentFont = g.getFont();
		g.setFont(new Font(currentFont.getFontName(), Font.PLAIN, currentFont.getSize()));
	}
	
	// Case A : open italic tag : YES, close italic tag : YES
	private static int myDrawStringA(Graphics g, String text, int x, int y) {
		String regularTextBefore, regularTextAfter, italicsText;
		int width;

		int italicsTagOpen = text.indexOf("<i>");
		int italicsTagClose = text.indexOf("</i>");

		regularTextBefore = text.substring(0, italicsTagOpen);
		italicsText = text.substring(italicsTagOpen + 3, italicsTagClose);
		regularTextAfter = text.substring(italicsTagClose + 4);

		// 1. draw the normal regular text before the italic text
		g.drawString(regularTextBefore, x,  y);
		width = g.getFontMetrics().stringWidth(regularTextBefore);
		
		// 2. change police to italics and draw the italic text
		setFontItalic(g);
		g.drawString(italicsText, x + width,  y);
		width += g.getFontMetrics().stringWidth(italicsText);
		
		// 3. change police to regular and draw the regular text after the italic text
		setFontRegular(g);
		g.drawString(regularTextAfter, x + width,  y);
		width += g.getFontMetrics().stringWidth(regularTextAfter);

		return width;
	}
	
	// Case B : open italic tag : YES, close italic tag : NO
	private static int myDrawStringB(Graphics g, String text, int x, int y) {
		String regularText, italicsText;

		int italicsTagOpen = text.indexOf("<i>");
		regularText = text.substring(0, italicsTagOpen);
		italicsText = text.substring(italicsTagOpen + 3);

		// 1. draw the normal regular text before the italic text
		g.drawString(regularText, x,  y);
		
		// 2. change police to italics and draw the italic text
		setFontItalic(g);
		g.drawString(italicsText, x + g.getFontMetrics().stringWidth(regularText),  y);
		
		return g.getFontMetrics().stringWidth(italicsText) + g.getFontMetrics().stringWidth(regularText);
	}
	
	// Case C : open italic tag : NO, close italic tag : YES
	private static int myDrawStringC(Graphics g, String text, int x, int y) {
		String regularText, italicsText;

		int italicsTagClose = text.indexOf("</i>");
		italicsText = text.substring(0, italicsTagClose);
		regularText = text.substring(italicsTagClose + 4);
		
		// 1. draw the italic text
		g.drawString(italicsText, x,  y);
		
		// 2. change police to regular and draw the regular text after the italic text
		setFontRegular(g);
		g.drawString(regularText, x + g.getFontMetrics().stringWidth(italicsText),  y);
		
		return g.getFontMetrics().stringWidth(regularText) + g.getFontMetrics().stringWidth(italicsText);
	}
	
	// Case D : open italic tag : NO, close italic tag : NO
	private static int myDrawStringD(Graphics g, String text, int x, int y) {
		g.drawString(text, x,  y);
		return g.getFontMetrics().stringWidth(text);
	}
	
	private static int myDrawString(Graphics g, String text, int x, int y) {
		int italicsTagOpen = text.indexOf("<i>");
		int italicsTagClose = text.indexOf("</i>");
		
		// there is an opening and a closing italic tag : case A
		if ((italicsTagOpen != -1) && (italicsTagClose != -1))
			return myDrawStringA(g, text, x, y);
		
		// there is an opening and no closing italic tag : case B
		if ((italicsTagOpen != -1) && (italicsTagClose == -1))
			return myDrawStringB(g, text, x, y);

		// there is no opening and a closing italic tag : case C
		if ((italicsTagOpen == -1) && (italicsTagClose != -1))
			return myDrawStringC(g, text, x, y);

		// there is no opening and no closing italic tag : case D
		if ((italicsTagOpen == -1) && (italicsTagClose == -1))
			return myDrawStringD(g, text, x, y);
		
		return 0; // should never get here
	}

	public static void drawText(Graphics g, String text, int x, int y) {
		int skip;
		int width;
		int i = text.indexOf("{");
		String data;

		while (i != -1) {
			skip = 3;
			String text1 = text.substring(0, i);
			x += myDrawString(g, text1, x,  y);
			data = text.substring(i, i + skip);
			if (data.startsWith("{1") && !data.equals("{1}")) { // This deals with (10) to (16) symbols
				skip = 4;
				data = text.substring(i, i + skip);
			}
			if (data.charAt(2) == '/') { // This deals with hybrid mana i.e. (b/g) for Deathrite Shaman
				skip = 5;
				data = text.substring(i, i + skip); 
			}

			if (ManaSymbols.contains(data)) {
				BufferedImage img = ManaSymbols.getImage(data);
				g.drawImage(img, x, y - (g.getFontMetrics().getAscent()), img.getWidth(), img.getHeight(), null);
				width = img.getWidth();
			}
			else {
				String subtext = text.substring(i, i + skip);
				myDrawString(g, subtext, x, y);
				width = g.getFontMetrics().stringWidth(subtext);
			}
			x += width;
			text = text.substring(i + skip);
			i = text.indexOf("{");
		}
		myDrawString(g, text, x,  y);
	}

	public static int getTextWidth(Graphics g, String text) {
		text = text.replace("<i>", "");
		text = text.replace("</i>", "");
		int skip;
		int width;
		int i = text.indexOf("{");
		String data;
		int length = 0;

		while (i != -1) {
			skip = 3;
			String text1 = text.substring(0, i);
			length += g.getFontMetrics().stringWidth(text1);

			data = text.substring(i, i + skip);
			if (data.startsWith("{1") && !data.equals("{1}")) { // This deals with (10) to (16) symbols
				skip = 4;
				data = text.substring(i, i + skip);
			}
			if (data.charAt(2) == '/') { // This deals with hybrid mana i.e. (b/g) for Deathrite Shaman
				skip = 5;
				data = text.substring(i, i + skip); 
			}

			if (ManaSymbols.contains(data))
				width = ManaSymbols.getImage(data).getWidth();
			else {
				String subtext = text.substring(i, i + skip);
				width = g.getFontMetrics().stringWidth(subtext);
			}
			length += width;
			text = text.substring(i + skip);
			i = text.indexOf("{");
		}
		length += g.getFontMetrics().stringWidth(text);
		return length;
	}

	private void drawStatusFrame(Graphics g, int lx, int ly, int width, int height, Color fillColor) {
		Color textColor = Color.BLACK;

		Vector<String> text = new Vector<String>();

		text.add("Turn " + _turn + " (" + getActivePlayerName() + ")");
		text.add(_phase + "/" + _step);
		text.addAll(multiLine(computeState(), width, g));

		int fontHeight = g.getFontMetrics().getHeight();

		int w = width + BUTTON_MARGIN * 2;
		int h = height + BUTTON_MARGIN * 2;
		_statusFrame.setDimensions(w, h);

		int x = lx;
		int y = ly;
		_statusFrame.setLocation(x, y);

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
			drawText(g, line, x + BUTTON_MARGIN,  y);
		}
	}

	private void drawButtons(Graphics g, int start_xPos, int start_yPos) {
		int x = start_xPos;
		int y = start_yPos;

		for (UIButton button : _UIButtons)
		{
			drawButton(g, button, x, y, INTERFACE_BUTTON_WIDTH, Color.WHITE);
			if (button == _btnYes) // to chain "No" button
				x += _btnYes.width + BUTTON_SPACING;
			else if (button == _btnMinus) // to chain "Next" button
				x += _btnMinus.width + BUTTON_SPACING;
			else if (button == _btnDone) // to chain "Expand Graveyard" button
				x += _btnDone.width + BUTTON_SPACING;
			else
			{
				y += button.height + BUTTON_SPACING;
				x = start_xPos;
			}
		}
	}


	/**
	 * Download the image from website
	 * @param imageID
	 */
	private static void downloadImage(String cardname, int imageID) {
		Image img;
		URL url;

		try {
			url = new URL(IMAGE_URL + imageID + "&type=card");
			
			img = ImageIO.read(url);
			ImageIO.write((RenderedImage) img, "png", new File(CARD_IMAGES_PATH + cardname + ".png"));
		} catch (MalformedURLException e) {
		} catch (IOException e) {
			System.err.println("Error with this name : " + cardname);
		}
	}

	private void drawBackground(Graphics g) {
		if (bDisplayImages) 
			g.drawImage(_wallpaper, 0, 0, getWidth(), getHeight(), null);
		else
		{
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		if (_bDebugMode) {
			g.setColor(Color.MAGENTA);
			g.drawRect(0, 0, getWidth()-1, getHeight()-1);
		}
	}

	public void drawCardFrame(Graphics g, String cardname, int x, int y, boolean bTapped)
	{
		String text;
		int w;
		int h;

		if (bTapped) {
			h = CARD_WIDTH;
			w = CARD_HEIGHT;			
		}
		else {
			w = CARD_WIDTH;
			h = CARD_HEIGHT;
		}

		g.setColor(Color.WHITE);
		g.fillRect(x, y, w, h);
		g.setColor(Color.BLACK);
		g.drawRect(x, y, w, h);
		if (cardname.length() > 15)
			text = cardname.substring(0, 15) + "...";
		else
			text = cardname;
		g.drawString(text, x + 2, y + 11);

	}

	/**
	 * 
	 * @param g
	 * @param imageID
	 * @param xPos
	 * @param yPos
	 * @param width
	 * @param height
	 */
	public void drawCard(Graphics g, UICard card, int xPos, int yPos) {
		String path;
		File imageFile;
		int x, y, w, h;
		BufferedImage img = null;
		String cardname = card.getName();
		int imageID = card.getImageID();

		if (imageID == -1) { // Card is a token
			path = CLIENT_IMAGES_PATH + "tokens/" + cardname + ".png";
			imageFile = new File(path);
			if (!imageFile.exists())
				imageFile = new File(CLIENT_IMAGES_PATH + "tokens/_no_image.png"); // token image not dowloaded yet, use "no_image" pic
		}
		else if (imageID == -2) { // 2/2 face down MORPH creature
			path = CLIENT_IMAGES_PATH + "tokens/COLORLESS_MORPH_22.png";
			imageFile = new File(path);
		}
		else { // card is not a token
			path = CARD_IMAGES_PATH + cardname + ".png";
			imageFile = new File(path);
			if (!imageFile.exists())
				downloadImage(cardname, imageID);
		}

		// load the image in cache
		if (!_cacheImages.containsKey(cardname))
		{
			try {
				img = ImageIO.read(imageFile);
				_cacheImages.put(cardname, img);
			} catch (IOException e) {
				System.out.println("IO error with file : " + cardname);
			}	
		}
		img = _cacheImages.get(cardname);

		x = xPos;

		if (card.isTapped()) {
			w = CARD_HEIGHT;
			h = CARD_WIDTH;
			y = yPos + CARD_HEIGHT - CARD_WIDTH;
			AffineTransform at = AffineTransform.getTranslateInstance(x + CARD_HEIGHT, y);
			at.rotate(Math.PI/2);
			double Xratio = (double)CARD_WIDTH / (double)img.getWidth();
			double Yratio = (double)CARD_HEIGHT / (double)img.getHeight();
			at.scale(Xratio, Yratio);
			Graphics2D g2d = (Graphics2D) g;
			if (bDisplayImages)
				g2d.drawImage(img, at, null);
		}
		else
		{
			h = CARD_HEIGHT;
			w = CARD_WIDTH;
			y = yPos;
			if (bDisplayImages)
				g.drawImage(img, x, y, w, h, null);
		}
		if (!bDisplayImages)
			drawCardFrame(g, card.getName(), x, y, card.isTapped());

		// set card size and coordinates for click detection
		card.x = x;
		card.y = y;
		card.width = w;
		card.height = h;

		// DEBUG draw card coords
		if (_bDebugMode) {
			g.setColor(Color.CYAN);
			g.drawString("x = " + x + "; y = " + y, x, y);
		}

		// Draw power/toughness if it'a a creature
		String pt = card.getPowerToughness();
		if (pt != null)
		{
			int ptboxWidth = g.getFontMetrics().stringWidth(pt);
			int ptboxHeight = g.getFontMetrics().getHeight();
			int ptboxX;
			int ptboxY;
			if (card.isTapped()) {
				ptboxX = xPos + 9;
				ptboxY = yPos + CARD_HEIGHT - ptboxHeight - 6;
			}
			else {
				ptboxX = xPos + CARD_WIDTH - ptboxWidth - 9;
				ptboxY = yPos + CARD_HEIGHT - ptboxHeight - 6;
			}

			g.setColor(TooltipColor);
			g.fillRect(ptboxX-4, ptboxY-1, ptboxWidth+8, ptboxHeight+2);
			g.setColor(Color.LIGHT_GRAY);
			g.drawRect(ptboxX-4, ptboxY-1, ptboxWidth+8, ptboxHeight+2);
			g.setColor(Color.BLACK);
			g.drawString(pt, ptboxX, ptboxY + ptboxHeight - 3);
		}

		// Draw "Attacking" if the creature is attacking
		if (_attacks.containsKey(new Integer(card.getIdCard()))) {
			setCombatLabels(g, card, "Attacking", xPos, yPos);
		}

		// Draw "Blocking" if the creature is blocking
		if (_blocks.containsKey(new Integer(card.getIdCard()))) {
			int blockOrder = card.getBlockOrder();
			String text = "Blocking";
			if (blockOrder != 0)
				text += " #" + blockOrder;
			setCombatLabels(g, card, text , xPos, yPos);
		}

		// Draw counters
		if (card.getCounters().size() > 0) {
			int xCou;
			int yCou;

			if (card.isTapped()) { 
				xCou = xPos + 10;
				yCou = yPos + 60;
			}
			else {
				xCou = xPos + 10;
				yCou = yPos + 10;				
			}
			try {
				Vector<String> counters = card.getCounters();
				for (String counter : counters) {
					UIButton btn = new UIButton(counter);
					drawButton(g, btn, xCou, yCou, g.getFontMetrics().stringWidth(counter), Color.ORANGE);
					yCou += btn.height;
				}
			} catch (ConcurrentModificationException e) {
				System.err.println("Error in Board.drawCard()");
			}

		}

		// Setting card border color
		Color borderColor = new Color(0f,0f,0f,0f);
		Integer idCard = new Integer(card.getIdCard());

		if (_highlights.contains(idCard))
			borderColor = Color.YELLOW;

		if (_app.getGameState() == State.WaitDeclareAttackers)
		{
			if (_declaredPotentialAttacker.contains(idCard))
				borderColor = Color.RED;
			else if (_undeclaredPotentialAttacker.contains(idCard))
				borderColor = Color.YELLOW;
		}

		if (_app.getGameState() == State.WaitDeclareBlockers)
		{
			if (_declaredPotentialBlocker.contains(idCard))
				borderColor = Color.GREEN;
			else if (_undeclaredPotentialBlocker.contains(idCard))
				borderColor = Color.YELLOW;
		}

		if (_app.getGameState() == State.WaitChooseCreatureToBlock)
		{
			if (_blockableAttacker.contains(idCard))
				borderColor = Color.YELLOW;
		}

		if (_app.getGameState() == State.WaitChooseRecipientToAttack)
		{
			if (_attackableRecipient.contains(idCard))
				borderColor = Color.YELLOW;
		}

		if (((_app.getGameState() == State.WaitDiscardEOT) ||
				(_app.getGameState() == State.WaitDiscard) ||
				(_app.getGameState() == State.WaitBrainstorm)) &&
				(card.getZone() == findPlayerByID(_idPlayer).getHand()))
			borderColor = Color.CYAN;
		g.setColor(borderColor);
		g.drawRect(x, y, w, h);

		addToAllCards(card);

		// draw tooltip like box for triggered and activated abilities
		if (card.getRulesText() != null) {
			// draw a filter
			g.setColor(new Color(1, 0, 0, 0.2f));
			g.fillRect(xPos, yPos, CARD_WIDTH, CARD_HEIGHT);

			UIButton temp = new UIButton(card.getRulesText());
			drawButton(g, temp, xPos, yPos + CARD_HEIGHT/2, CARD_WIDTH-6, TooltipColor);
		}
	}

	private void setCombatLabels(Graphics g, UICard card, String text, int xPos, int yPos) {
		int ptboxWidth = g.getFontMetrics().stringWidth(text);
		int ptboxHeight = g.getFontMetrics().getHeight();
		int ptboxX;
		int ptboxY;
		if (card.isTapped()) {
			ptboxX = xPos + (CARD_HEIGHT - ptboxWidth)/2;
			ptboxY = yPos + 90;	
		}
		else {
			ptboxX = xPos + (CARD_WIDTH - ptboxWidth)/2;
			ptboxY = yPos + 50;
		}

		g.setColor(Color.WHITE);
		g.fillRect(ptboxX-4, ptboxY-1, ptboxWidth+8, ptboxHeight+2);
		g.setColor(Color.LIGHT_GRAY);
		g.drawRect(ptboxX-4, ptboxY-1, ptboxWidth+8, ptboxHeight+2);
		g.setColor(Color.RED);
		g.drawString(text, ptboxX, ptboxY + ptboxHeight - 3);
	}

	private void addToAllCards(UICard card) {
		int i = 0;
		while (i < _allCards.size()) {
			if (_allCards.get(i).getIdCard() == card.getIdCard()) {
				_allCards.remove(i);
				i--;
			}
			i++;
		}
		_allCards.add(card);
	}

	private void drawLibrary(Graphics g, int yPos) {
		if (_libraryPanel == null) {
			_libraryPanel = createZonePanel(_library, -1, -1, true);
		}
	}

	private void drawStack(Graphics g, int yPos) {
		int nbCards = _stack.size();
		if (nbCards == 0)
			return;

		int xPos = (getWidth() - (CARD_WIDTH * nbCards)) / 2;		

		g.setColor(new Color(0.0f, 0.0f, 0.8f, 0.3f));
		g.fillRect(xPos - ZONE_SPACING,
				yPos - ZONE_SPACING,
				(nbCards * (CARD_WIDTH + CARD_SPACING) - CARD_SPACING) + ZONE_SPACING * 2,
				CARD_HEIGHT + ZONE_SPACING * 2);

		// draw cards
		for (int i = 0; i < nbCards; i++) {
			UICard c = _stack.get(i);
			drawCard(g, c, xPos, yPos);
			xPos += (CARD_WIDTH + CARD_SPACING);
		}
	}
	private void drawUIZone(Graphics g, UIZone zone, int xPos, int yPos) {
		int maxWidth = getWidth() - (xPos - ZONE_OUTLINE) - BORDER_MARGIN;

		drawUIZone(g, zone, xPos, yPos, maxWidth);
	}

	private void drawUIZone(Graphics g, UIZone zone, int xPos, int yPos, int maxWidth) {
		if (zone == null)
			return;

		zone.x = xPos - ZONE_OUTLINE;
		zone.y = yPos - ZONE_OUTLINE;
		zone.width = maxWidth;
		zone.height = CARD_HEIGHT + AURA_Y_OFFSET + ZONE_OUTLINE * 2;

		// draw zone frame
		if (_bDebugMode)
		{
			g.setColor(new Color(0.5f, 0.5f, 0.5f, 0.85f));
			g.fillRect(zone.x, zone.y, zone.width, zone.height);
			g.setColor(Color.BLACK);
			g.drawRect(zone.x, zone.y, zone.width, zone.height);	
		}


		int nbCards = zone.size() - zone.getNbAuras();

		if (nbCards == 0)
			return;

		int xOffset;
		boolean bStretch = false;
		// zone is smaller than cards to draw : stretch is required
		int s = computeZoneSize(zone);
		if ((nbCards != 1) && (s > maxWidth)) {
			bStretch = true;
			xOffset = (maxWidth - (zone.get(0).isTapped() ? CARD_HEIGHT : CARD_WIDTH)) / (nbCards - 1);
		}
		else
			xOffset = CARD_WIDTH + CARD_SPACING;

		yPos += AURA_Y_OFFSET;

		// draw cards
		for (int i = 0; i < zone.size(); i++) {
			UICard card = zone.get(i);
			int xOffsetAura = 0;

			if (card.getHostID() != 0)
				continue;

			// Auras and equipments
			int nbAuras = card.getAurasID().size();
			if (nbAuras > 0) {
				xOffsetAura = nbAuras * AURA_X_OFFSET;
				int xAura = xPos + (nbAuras * AURA_X_OFFSET);
				int yAura = yPos - AURA_Y_OFFSET;;
				Vector<Integer> auraIDs = card.getAurasID();

				for (int j = 0; j < nbAuras; j++) {
					drawCard(g, findCardById(auraIDs.get(j)), xAura, yAura);
					xAura -= AURA_X_OFFSET;
				}
			}
			drawCard(g, card, xPos, yPos);
			if (card.isTapped())
				xOffsetAura = 0;
			xPos += xOffset + xOffsetAura;
			if (!bStretch && card.isTapped())
				xPos += (CARD_HEIGHT - CARD_WIDTH);
		}
	}

	private boolean isLibraryVisible() {
		if ((_app == null) || (_app.getGameState() == null))
			return false;

		boolean ret;

		switch (_app.getGameState()) {
		case WaitChoiceChordOfCalling:
		case WaitChoiceCitanulFlute:
		case WaitChoiceGreenSunZenith:
		case WaitChoiceCreatureCard:
		case WaitChoiceRemembrance:
		case WaitChoiceGoblinCard:
		case WaitChoiceLandOrCreatureCard:
		case WaitChoiceArtifactOrCreatureCard:
		case WaitChoiceArtifactOrEnchantmentCard:
		case WaitChoiceEnchantment:
		case WaitChoiceEnchantmentCardWithCCM3orLess:
		case WaitChoiceCreatureCardWithToughness2orLess:
		case WaitChoiceInstantOrSorceryCard:
		case WaitChoiceStoneforgeMystic_search:
		case WaitChoiceSylvanScrying:
		case WaitChoicePlainsIsland:
		case WaitChoiceIslandSwamp:
		case WaitChoiceSwampMountain:
		case WaitChoiceMountainForest:
		case WaitChoiceForestPlains:
		case WaitChoicePlainsSwamp:
		case WaitChoiceSwampForest:
		case WaitChoiceForestIsland:
		case WaitChoiceIslandMountain:
		case WaitChoiceMountainPlains:
		case WaitChoiceBasicForestCard:
		case WaitChoiceForestCard:
		case WaitChoiceBasicLand:
		case WaitChoiceTutor:
			ret = true;
			break;

		default:
			ret = false;
			break;
		}
		return ret;
	}

	/**
	 * 
	 * @param g
	 * @param color
	 * @param xA
	 * @param yA
	 * @param xb
	 * @param yb
	 */
	private void drawArrow(Graphics g, Color color, int xa, int ya, int xb, int yb) {
		int arrowHeadHeight = 20;
		int arrowHeadWidth = 15;
		Double arrowLength = Math.sqrt(Math.pow(xb - xa, 2) + Math.pow(ya - yb, 2));
		Double adjacent;
		Double angle;
		int xB = xa;
		int yB = ya - arrowLength.intValue();

		// Case 1 (North East)
		if (xa <= xb && ya >= yb) {
			adjacent = (double) (ya - yb);
			angle = Math.acos(adjacent / arrowLength);
		}
		// Case 2 (South East)
		else if (xa <= xb && ya <= yb) {
			adjacent = (double) (xb - xa);
			angle = Math.acos(adjacent / arrowLength) + Math.PI/2;
		}
		// Case 3 (South West)
		else if (xa >= xb && ya <= yb) {
			adjacent = (double) (xa - xb);
			angle = -(Math.PI/2 + Math.acos(adjacent / arrowLength));
		}
		// Case 4 (North West)
		else if (xa >= xb && ya >= yb) {
			adjacent = (double) (ya - yb);
			angle = Math.acos(adjacent / arrowLength) * -1;
		}
		else
			return;

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(color);
		g2.rotate(angle, xa, ya);
		g2.setStroke(new BasicStroke(4));
		g2.drawLine(xa, ya, xB, yB + arrowHeadHeight); // arrow line

		Polygon triangle = new Polygon(); // arrow head
		triangle.addPoint(xB, yB);
		triangle.addPoint(xB - arrowHeadWidth/2, yB + arrowHeadHeight);
		triangle.addPoint(xB + arrowHeadWidth/2, yB + arrowHeadHeight);
		g2.setStroke(new BasicStroke(1));
		g2.fillPolygon(triangle);
	}

	/**
	 * Draw block arrows
	 * @param g
	 */
	private void drawBlockArrows(Graphics g) {
		Iterator<Entry<Integer, Integer>> it = _blocks.entrySet().iterator();
		Entry<Integer, Integer> pair;
		Integer idBlocking, idBlocked;
		Color arrowColor =  new Color(0.0f, 1.0f, 0.0f, 0.7f); // Green with 70% alpha

		while (it.hasNext()) {
			pair = (Entry<Integer, Integer>) it.next();
			idBlocking = (Integer) pair.getKey();
			idBlocked = (Integer) pair.getValue();

			if (idBlocked != -1) {
				UICard blocker = findCardById(idBlocking);
				UICard blocked = findCardById(idBlocked);
				int x1 = blocker.x + blocker.width / 2;
				int y1 = blocker.y + blocker.height / 2;
				int x2 = blocked.x + blocked.width / 2;
				int y2 = blocked.y + blocked.height /2;
				drawArrow(g, arrowColor, x1, y1, x2, y2);
			}
		}
	}

	/**
	 * Draw attack arrows
	 * @param g
	 */
	private void drawAttackArrows(Graphics g) {
		Iterator<Entry<Integer, Integer>> it = _attacks.entrySet().iterator();
		Entry<Integer, Integer> pair;
		Integer idattacking, idRecipient;
		Color arrowColor = new Color(1.0f, 0.0f, 0.0f, 0.7f); // Red with 70% alpha

		while (it.hasNext()) {
			pair = (Entry<Integer, Integer>) it.next();
			idattacking = (Integer) pair.getKey();
			idRecipient = (Integer) pair.getValue();

			if (idRecipient != -1) {
				Point recipient = null;
				UICard attacker = findCardById(idattacking);
				UIPlayer player = findPlayerByID(idRecipient);

				if (player != null) { // recipient is a player
					UIButton playerButton = player.getNameButton();
					if (playerButton != null)
						recipient = new Point(playerButton.x + playerButton.width / 2, playerButton.y + playerButton.height / 2);
				}
				else { // recipient is a Planeswalker
					UICard planeswalker = findCardById(idRecipient);
					if (planeswalker != null)
						recipient = new Point(planeswalker.x + planeswalker.width / 2, planeswalker.y + planeswalker.height / 2);
				}

				// If recipient UI element was found, draw the arrow
				if (recipient != null) {
					int x1 = attacker.x + attacker.width / 2;
					int y1 = attacker.y + attacker.height / 2;
					int x2 = recipient.x;
					int y2 = recipient.y;

					if (_bDebugMode) {
						g.setColor(Color.MAGENTA);
						g.drawString("x1 = " + x1 + "; y1 = " + y1, x1, y1);
						g.drawString("x2 = " + x2 + "; y2 = " + y2, x2, y2);
					}
					drawArrow(g, arrowColor, x1, y1, x2, y2);
				}
			}
		}
	}

	private int computeZoneSize(UIZone zone) {
		int size = CARD_SPACING * (zone.size() - zone.getNbAuras());
		for (UICard c : zone.getCards()) {
			if (c.getHostID() != 0)
				continue;

			if (c.isTapped())
				size += CARD_HEIGHT;
			else
				size += CARD_WIDTH;
			size += CARD_SPACING;
		}
		return size;
	}

	/**
	 * 
	 */
	public void paintComponent(Graphics g) {
		synchronized (lock) {
			if (!bInitializationOK)
				return;
			bReadyToCompute = false;

			UIPlayer me = findPlayerByID(_idPlayer);
			UIPlayer opponent = findPlayerByID(_idOpponent);

			drawBackground(g);

			// draw buttons
			drawButtons(g, BORDER_MARGIN, BORDER_MARGIN + CARD_HEIGHT + ZONE_SPACING + 60);

			// draw status frame including player life frames
			drawStatus(g);

			// draw battlefield and player hand
			int xZones = _statusFrame.x + _statusFrame.width + ZONE_SPACING;
			int yZone = getHeight() - (CARD_HEIGHT + AURA_Y_OFFSET + BORDER_MARGIN);

			// player
			drawUIZone(g, me.getHand(), xZones, yZone);
			yZone -= CARD_HEIGHT + AURA_Y_OFFSET + ZONE_SPACING;
			drawUIZone(g, me.getLands(), xZones, yZone);
			yZone -= CARD_HEIGHT + AURA_Y_OFFSET + ZONE_SPACING;
			int maxWidth = (getWidth() - (xZones - ZONE_OUTLINE) - BORDER_MARGIN) - (computeZoneSize(me.getOtherPermanents()));
			drawUIZone(g, me.getCreatures(), xZones, yZone, maxWidth);
			drawUIZone(g, me.getOtherPermanents(), me.getCreatures().x + me.getCreatures().width + ZONE_SPACING, yZone);

			// opponent
			yZone = BORDER_MARGIN;
			drawUIZone(g, opponent.getLands(), xZones, yZone);
			yZone += CARD_HEIGHT + AURA_Y_OFFSET + ZONE_SPACING;
			maxWidth = (getWidth() - (xZones - ZONE_OUTLINE) - BORDER_MARGIN) - (computeZoneSize(opponent.getOtherPermanents()));
			drawUIZone(g, opponent.getCreatures(), xZones, yZone, maxWidth);
			drawUIZone(g, opponent.getOtherPermanents(), opponent.getCreatures().x + opponent.getCreatures().width + ZONE_SPACING, yZone);

			// draw stack
			drawStack(g, ((getHeight() - (BORDER_MARGIN + CARD_HEIGHT)) / 2) - (CARD_HEIGHT / 2));

			// *** optional things to draw ***
			// activated abilities buttons
			if (_actionButtons.size() > 0) {
				Graphics gLocal = g;
				UICard lastCardClicked = _app.getLastCardClicked();
				drawActivatedAbilitiesButtons(gLocal, lastCardClicked.x, lastCardClicked.y + CARD_HEIGHT/2);
			}

			// library
			if (isLibraryVisible())
				drawLibrary(g, ((getHeight() - (BORDER_MARGIN + CARD_HEIGHT)) / 2) - (CARD_HEIGHT / 2));

			if (_libManipulation.size() > 0) {
				if (_libManipulationPanel == null)
					_libManipulationPanel = createZonePanel(_libManipulation, -1, -1, true);
			}

			if (_oppHand.size() > 0) {
				if (_oppHandPanel == null)
					_oppHandPanel = createZonePanel(_oppHand, -1, -1, true);
			}

			// Draw arrows indicating attacks (attacker -> recipient)
			drawAttackArrows(g);

			// Draw arrows indicating blocks (blocker -> attacker)
			drawBlockArrows(g);

			// Draw card info if applicable
			if (_cardInfoRequest != null)
				drawCardInfo(g);

			// Draw mana choices (i.e. Adarkar Wastes)
			if (_app._manaChoices != null)
				drawManaChoices(g);

			// Mana color picker (i.e. Mother of Runes)
			if ((_app.getGameState() == State.WaitChooseColor) ||
					(_app.getGameState() == State.WaitChooseTriggeredManaAbilityColor) ||
					(_app.getGameState() == State.PromptChooseColorStaticETB))
				drawPentacle(g);

			bReadyToCompute = true;
			lock.notify();
		}
	}

	private void drawPentacle(Graphics g) {
		int radius = 65;
		int x, y;
		double angle;
		int numNodes = 5;
		int xOffset = 80;
		int yOffset = 630;

		angle = (2 * Math.PI) / numNodes;

		String symbol;

		_manaChoicesButtons.clear();
		for (int i = 0; i < numNodes; i++) {
			if (i == 0)
				g.setColor(Color.RED);
			else
				g.setColor(Color.PINK);
			x = (int) (radius * Math.cos(-Math.PI/2 + (angle * i)));
			y = (int) (radius * Math.sin(-Math.PI/2 + (angle * i)));

			switch(i) {
			case 0:
				symbol = "[WHITE]";
				break;

			case 1:
				symbol = "[BLUE]";
				break;

			case 2:
				symbol = "[BLACK]";
				break;

			case 3:
				symbol = "[RED]";
				break;

			default:
				symbol = "[GREEN]";
				break;

			}
			drawManaCombi(g, x + xOffset, y + yOffset, symbol);
			_manaChoicesButtons.add(new UIManaChoice(i + 1, x + xOffset, y + yOffset, (MANACHOICE_SYMBOL_WIDTH)+2, MANACHOICE_SYMBOL_HEIGHT+2));
		}
	}

	/**
	 * 
	 * @return
	 */
	private int countNbSymbols() {
		int nbSymbols = 0;

		String manaSymbols[];
		_manaChoices = _app._manaChoices.split(";");
		for (int iChoice = 0; iChoice < _manaChoices.length; iChoice++) {
			manaSymbols = _manaChoices[iChoice].split(",");
			nbSymbols += manaSymbols.length;
		}
		return nbSymbols;
	}



	/**
	 * 
	 * @param g
	 * @param x
	 * @param y
	 * @param combi
	 * @return The number of symbols drawn
	 */
	private int drawManaCombi(Graphics g, int x, int y, String combi) {
		combi = combi.substring(1, combi.length()-1);
		String manaSymbols[] = combi.split(",");
		int nbSymbols = manaSymbols.length;
		String mana;
		String manaLetter;
		int xStr, yStr, strWidth, strHeight;

		Font currentFont = g.getFont();
		g.setFont(new Font(currentFont.getFontName(), Font.BOLD, 20));
		FontMetrics fm = g.getFontMetrics();

		// draw combination frame
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(x-1, y-1, (nbSymbols * MANACHOICE_SYMBOL_WIDTH)+2, MANACHOICE_SYMBOL_HEIGHT+2);
		g.setColor(Color.GRAY);
		g.drawRect(x-1, y-1, (nbSymbols * MANACHOICE_SYMBOL_WIDTH)+2, MANACHOICE_SYMBOL_HEIGHT+2);

		// draw each mana symbol
		for (int iSymbol = 0; iSymbol < nbSymbols; iSymbol++) {
			mana = manaSymbols[iSymbol].trim();

			// draw a circle of the color of the mana
			g.setColor(colorParse(mana));
			g.fillOval(x, y, MANACHOICE_SYMBOL_WIDTH, MANACHOICE_SYMBOL_HEIGHT);
			g.setColor(Color.BLACK);
			g.drawOval(x, y, MANACHOICE_SYMBOL_WIDTH, MANACHOICE_SYMBOL_HEIGHT);

			// Draw the mana color letter (W U B R G) in the center
			manaLetter = getLetter(mana);
			strWidth = fm.stringWidth(manaLetter);
			strHeight = fm.getHeight();

			xStr = x + (MANACHOICE_SYMBOL_WIDTH - strWidth) / 2;
			yStr = y + (MANACHOICE_SYMBOL_HEIGHT - strHeight) / 2;

			g.drawString(manaLetter, xStr, yStr + strHeight - 5);
			x += MANACHOICE_SYMBOL_WIDTH;
		}
		g.setFont(currentFont);
		return nbSymbols;
	}

	public String getLetter(String manaColor) {
		if (manaColor.equals("WHITE"))
			return "W";
		if (manaColor.equals("BLUE"))
			return "U";
		if (manaColor.equals("BLACK"))
			return "B";
		if (manaColor.equals("RED"))
			return "R";
		if (manaColor.equals("GREEN"))
			return "G";
		return null;
	}

	public static Color colorParse(String input) {
		if (input.equals("WHITE"))
			return Color.decode("#FFFBD5");
		if (input.equals("BLUE"))
			return Color.decode("#AAE0FA");
		if (input.equals("BLACK"))
			return Color.decode("#CBC2BF");
		if (input.equals("RED"))
			return Color.decode("#DE8166");
		if (input.equals("GREEN"))
			return Color.decode("#9BD3AE");
		return null;
	}

	/**
	 * 
	 * @param g
	 */
	public void drawManaChoices(Graphics g) {
		int nbTotalSymbols = countNbSymbols();
		_manaChoices = _app._manaChoices.split(";");
		_manaChoicesButtons.clear();
		int nbChoices = _manaChoices.length;
		int xFrame, yFrame, wFrame, hFrame;
		int xCombi, yCombi;
		int nbSymbolsDrawn;

		// Draw frame
		wFrame = (MANACHOICE_SYMBOL_WIDTH * nbTotalSymbols) + (MANACHOICE_COMBINATION_SPACING * (nbChoices-1));
		hFrame = MANACHOICE_SYMBOL_HEIGHT;
		xFrame = (this.getWidth() - wFrame) / 2;
		yFrame = (this.getHeight() - hFrame) / 2;
		//g.setColor(Color.BLACK);
		//g.drawRect(xFrame - 2, yFrame - 2, wFrame + 4, hFrame + 4);

		// Draw each choice combination
		xCombi = xFrame;
		yCombi = yFrame;

		for (int iChoice = 0; iChoice < nbChoices; iChoice++) {
			nbSymbolsDrawn = drawManaCombi(g, xCombi, yCombi, _manaChoices[iChoice]);
			_manaChoicesButtons.add(new UIManaChoice(iChoice + 1,
					xCombi, // X
					yCombi, // Y
					(_manaChoices[iChoice].split(",").length * MANACHOICE_SYMBOL_WIDTH)+2, // Width
					MANACHOICE_SYMBOL_HEIGHT+2)); // Height
			xCombi += (MANACHOICE_SYMBOL_WIDTH * nbSymbolsDrawn) + MANACHOICE_COMBINATION_SPACING;
		}
	}

	/**
	 * 
	 * @param input
	 */
	public void updateCounters(String input) {
		input = input.substring(input.indexOf("=") + 1);
		if (input.length() == 0)
			return;

		String cards[] = input.split(";");
		String counters[];
		String counterType;
		int indexStar;
		UICard c;

		// for each card
		for (int i = 0; i < cards.length; i++)
		{
			if (cards[i].length() > 0)
			{
				counters = cards[i].split("\\|");
				c = getPermanentByID(Integer.parseInt(counters[0].substring(1)));  // substring(1) is to ignore first character (a # in front of ID)
				c.getCounters().clear();

				// for each counter type
				for (int j = 1; j < counters.length; j++) {
					indexStar = counters[j].indexOf("*");
					counterType = counters[j].substring(0, indexStar);
					c.setCounters(counterType, Integer.parseInt(counters[j].substring(indexStar + 1)));
				}
			}
		}
	}

	public void updateUIprivateZone(String input, int idPlayer) {
		int idxEqualSign = input.indexOf("=");
		UIZone zone;
		UIPlayer player = findPlayerByID(idPlayer);

		if (input.startsWith("Hand"))
			zone = player.getHand();
		else if (input.startsWith("Graveyard")) {
			zone = player.getGraveyard();
			if (player.getGraveyardPanel() != null) {
				player.getGraveyardPanel().setZone(zone);
				player.getGraveyardPanel().repaint();
			}
		}
		else if (input.startsWith("Exile")) {
			zone = player.getExile();
			if (player.getExilePanel() != null) {
				player.getExilePanel().setZone(zone);
				player.getExilePanel().repaint();
			}
		}
		else if (input.startsWith("Command")) {
			zone = player.getCommand();
			if (player.getCommandPanel() != null) {
				player.getCommandPanel().setZone(zone);
				player.getCommandPanel().repaint();
			}
		}
		else if (input.startsWith("Lands"))
			zone = player.getLands();
		else if (input.startsWith("Creatures"))
			zone = player.getCreatures();
		else // if (input.startsWith("Other"))
			zone = player.getOtherPermanents();

		zone.load(input.substring(idxEqualSign + 1));	
	}

	public void updateUIpublicZone(String input, UIZone zone) {
		int idxEqualSign = input.indexOf("=");
		zone.load(input.substring(idxEqualSign + 1));	
	}

	public void updateLibrary(String input) {
		String[] zones = input.split("\\|");
		_library.load(zones[0]);
	}

	public void closeOppHandPanel() {
		if (_oppHandPanel == null)
			return;
		_oppHand.clear();
		JFrame frame = (JFrame) SwingUtilities.getWindowAncestor((UIZonePanel) _oppHandPanel);
		frame.setVisible(false);
		frame.dispose();
		_oppHandPanel = null;		
	}

	public void closeUpdateLibPanel() {
		if (_libManipulationPanel == null)
			return;
		_libManipulation.clear();
		JFrame frame = (JFrame) SwingUtilities.getWindowAncestor((UIZonePanel) _libManipulationPanel);
		frame.setVisible(false);
		frame.dispose();
		_libManipulationPanel = null;		
	}

	public void updateLibManipulation(String input) {
		String[] zones = input.split("\\|");
		_libManipulation.load(zones[0]);
		if (_libManipulationPanel != null)
			_libManipulationPanel.repaint();
	}

	public void updateOpponentHand(String input) {
		String[] zones = input.split("\\|");
		_oppHand.load(zones[0]);
		if (_oppHandPanel != null)
			_oppHandPanel.repaint();
	}

	private String computeState() {
		String description;

		switch (_app.getGameState()) {
		case PromptMulligan:
			description = "Do you want to keep this hand ?";
			break;

		case PromptDoYouWantToUntap:
			description = "Do you want to untap this permanent ?";
			break;

		case PromptCastWithoutPayingManaCost:
			description = "Do you want to cast that card without paying its mana cost ?";
			break;

		case PromptDoYouWantToUseTheAbility:
			description = "Do you want to use the ability ?";
			break;

		case PromptDoYouWantToDrawACard:
			description = "Do you want to draw a card ?";
			break;

		case WaitChoiceHarnessedLightning:
			description = "How many {E} do you want to pay ?";
			break;

		case WaitChoiceTurnabout:
			description = "Select a permanent type (1=Artifact, 2=Creature, 3=Land).";
			break;

		case WaitChooseTriggeredManaAbilityColor:
		case WaitChooseColor:
		case PromptChooseColorStaticETB:
			description = "Choose a color.";
			break;

		case PromptAbundance_CardType:
			description = "Choose Land ? (Yes=Land, No=Nonland)";
			break;

		case PromptCatastrophe_PermanentType:
			description = "Select permanents to destroy (1=Lands, 2=Creatures).";
			break;

		case PromptTurnabout_doYouWantToTap:
			description = "Tap or untap the permanents (Yes=Tap, No=Untap) ?";
			break;
			
		case PromptDoYouWantPutInGraveyard:
			description = "Do you want to put the card in the graveyard (No = leave it on top) ?";
			break;

		case PromptDoYouWantPutTheCardInYourHand:
			description = "Do you want to reveal and put the card in your hand ?";
			break;

		case PromptDoYouWantToShuffle:
			description = "Do you want to shuffle your library ?";
			break;

		case PromptDoYouWantToSacrificeALand:
			description = "Do you want to sacrifice a land ?";
			break;

		case WaitingForOpponent:
			description = "Waiting for opponent...";
			break;

		case Ready:
			description = "Ready.";
			break;

		case PromptXValue:
			description = "Choose a value for X.";
			break;

		case PromptMode:
			description = "Choose a mode.";
			break;

		case PromptTargetsOrDone:
			description = "Choose a target for " + _targetor + " or click Done.";
			break;

		case PromptTargets:
			description = "Choose a target for " + _targetor + ".";
			break;

		case PromptHost:
			description = "Choose a permanent to attach this Aura.";
			break;

		case WaitChoiceRemembrance:
			description = "Choose a card with the same name.";
			break;

		case WaitChoiceCreatureCard:
			description = "Choose a creature card.";
			break;

		case WaitChoiceGoblinCard:
			description = "Choose a Goblin card.";
			break;

		case WaitChoiceCreatureCardWithToughness2orLess:
			description = "Choose a creature card with toughness 2 or less.";
			break;

		case WaitChoiceArtifactOrCreatureCard:
			description = "Choose an artifact or creature card.";
			break;

		case WaitChoiceLandOrCreatureCard:
			description = "Choose a land or creature card.";
			break;

		case WaitChoiceArtifactOrEnchantmentCard:
			description = "Choose an artifact or enchantment card.";
			break;

		case WaitChoiceDamageArtifactSource:
			description = "Choose an artifact source.";
			break;

		case WaitChoiceDamageLandSource:
			description = "Choose an land source.";
			break;

		case WaitChoiceDamageWhiteSource:
			description = "Choose white a source.";
			break;
		case WaitChoiceDamageBlueSource:
			description = "Choose blue a source.";
			break;
		case WaitChoiceDamageBlackSource:
			description = "Choose a black source.";
			break;
		case WaitChoiceDamageRedSource:
			description = "Choose a red source.";
			break;
		case WaitChoiceDamageGreenSource:
			description = "Choose a green source.";
			break;

		case WaitChoiceDamageSource:
			description = "Choose a damage source.";
			break;

		case WaitChoiceEnchantment:
			description = "Choose an enchantment card.";
			break;

		case WaitChoiceEnchantmentCardWithCCM3orLess:
			description = "Choose an enchantment card with converted mana cost 3 or less.";
			break;

		case WaitChoiceInstantOrSorceryCard:
			description = "Choose an instant or sorcery card.";
			break;

		case WaitChoiceChordOfCalling:
		case WaitChoiceCitanulFlute:
			description = "Choose a creature card with converted mana cost X or less.";
			break;

		case WaitChoiceGreenSunZenith:
			description = "Choose a green creature card with converted mana cost X or less.";
			break;

		case WaitChoiceStoneforgeMystic_search:
		case WaitChoiceStoneforgeMystic_put:
			description = "Choose an Equipment card.";
			break;

		case WaitChoiceCopperGnomes:
			description = "Choose an artifact card.";
			break;

		case WaitChoiceGoblinLackey:
			description = "Choose a Goblin permanent card.";
			break;

		case WaitChoiceAcademyResearchers:
			description = "Choose an Aura card that can enchant Academy Researchers.";
			break;

		case WaitChoiceAetherVial_put:
			description = "Choose a creature card with converted mana cost equal to X.";
			break;

		case WaitChoiceMoxDiamond:
			description = "You may discard a land card.";
			break;

			/* Shadows Over Innistrad "reveal" lands */
		case PromptRevealPlainsOrIsland:
			description = "You may reveal a Plains or Island card.";
			break;
		case PromptRevealIslandOrSwamp:
			description = "You may reveal an Island or Swamp card.";
			break;
		case PromptRevealSwampOrMountain:
			description = "You may reveal a Swamp or Mountain card.";
			break;
		case PromptRevealMountainOrForest:
			description = "You may reveal a Mountain or Forest card.";
			break;
		case PromptRevealForestOrPlains:
			description = "You may reveal a Forest or Plains card.";
			break;

		case PromptPayEchoCost:
			description = "Do you want to pay the echo cost ?";
			break;

		case PromptPayCumulativeUpkeep:
			description = "Do you want to pay the cumulative upkeep cost ?";
			break;

		case PromptPayUpkeepCost:
			description = "Do you want to pay the upkeep cost ?";
			break;

		case PromptPayPunishingFire:
			description = "Do you want to pay {R} ?";
			break;

		case PromptPay_1life:
			description = "Do you want to pay 1 life ?";
			break;

		case PromptPay_2life:
			description = "Do you want to pay 2 life ?";
			break;

		case PromptPayChildOfGaea:
			description = "Do you want to pay {G}{G} ?";
			break;

		case PromptPayDriftingDjinn:
			description = "Do you want to pay {1}{U} ?";
			break;

		case PromptDiscardToPay:
			description = "Do you want to discard a card ?";
			break;

		case PromptSacrificeEnchantment:
			description = "Do you want to sacrifice an enchantment ?";
			break;

		case PromptPay_1mana:
			description = "Do you want to pay {1} ?";
			break;

		case PromptPay_2mana:
			description = "Do you want to pay {2} ?";
			break;

		case PromptPay_3mana:
			description = "Do you want to pay {3} ?";
			break;

		case PromptPay_4mana:
			description = "Do you want to pay {4} ?";
			break;

		case PromptPay_Xmana:
			description = "Do you want to pay {X} ?";
			break;

		case PromptPayLifeStaticETB:
			description = "How much life do you want to pay ?";
			break;

		case WaitChoiceForceOfWill:
			description = "Exile a blue card from your hand.";
			break;

		case WaitChoiceDaze:
			description = "Return an Island to your hand.";
			break;

		case PromptPayForShockland:
			description = "Do you want to pay 2 life ?";
			break;

		case PromptApplyReplaceDraw:
			description = "Do you want to apply the replacement effect instead of the normal draw ?";
			break;

		case PromptDredge:
			description = "Choose a Dredge card in your graveyard or click Done.";
			break;

		case WaitChoicePutInHand:
			description = "Choose a card to put into your hand.";
			break;

		case WaitChoiceOathOfNissa:
			description = "Choose creature, land, or planeswalker card.";
			break;

		case WaitChoicePutCreatureCardInHand:
			description = "Choose a creature card to put into your hand.";
			break;
		case WaitchoiceCardInGraveyard:
			description = "Choose a card in your graveyard or click Done.";
			break;

		case WaitChoiceCollectedCompany:
			description = "Choose a creature card with converted mana cost 3 or less.";
			break;

		case WaitChoicePutBottomLib:
			description = "Put a card on the bottom of your library.";
			break;

		case WaitChoiceScryPutBottom:
			description = "Put any number of cards on the bottom of your library.";
			break;

		case WaitChoiceShowAndTell:
			description = "Choose an an artifact, creature, enchantment or land card or click Done.";
			break;

		case WaitChoiceReprocess:
			description = "Sacrifice an artifact, creature or land or click Done.";
			break;

		case WaitChoiceCoercion:
		case WaitChoiceDuress:
		case WaitChoiceThoughtseize:
			description = "Choose a card to be discarded.";
			break;

		case LookPlayersHand:
			description = "Click Done when you are done looking at the player's hand.";
			break;

		case WaitChoiceLookTop:
			description = "Click Done when you are done looking at the top card.";
			break;

		case WaitChoicePutTopLib:
			description = "Put a card on the top of your library.";
			break;

		case WaitChoiceSylvanScrying:
			description = "Choose a land card.";
			break;

			// Ally fetch lands
		case WaitChoicePlainsIsland:
			description = "Choose a Plains or Island card.";
			break;
		case WaitChoiceIslandSwamp:
			description = "Choose an Island or Swamp card.";
			break;
		case WaitChoiceSwampMountain:
			description = "Choose a Swamp or Mountain card.";
			break;
		case WaitChoiceMountainForest:
			description = "Choose a Mountain or Forest card.";
			break;
		case WaitChoiceForestPlains:
			description = "Choose a Forest or Plains card.";
			break;
			// Ennemy fetch lands
		case WaitChoicePlainsSwamp:
			description = "Choose a Plains or Swamp card.";
			break;
		case WaitChoiceSwampForest:
			description = "Choose a Swamp or Forest card.";
			break;
		case WaitChoiceForestIsland:
			description = "Choose a Forest or Island card.";
			break;
		case WaitChoiceIslandMountain:
			description = "Choose an Island or Mountain card.";
			break;
		case WaitChoiceMountainPlains:
			description = "Choose a Mountain or Plains card.";
			break;

		case WaitChoiceBasicForestCard:
			description = "Choose a basic Forest card.";
			break;

		case WaitChoiceForestCard:
			description = "Choose a Forest card.";
			break;

		case WaitChoiceBasicLand:
			description = "Choose a basic land card.";
			break;

		case WaitChoiceTutor:
			description = "Choose a card.";
			break;

		case WaitDiscardEOT:
			description = "Discard down to your maximum hand size.";
			break;

		case WaitSacrificePermanent:
			description = "Sacrifice a permanent.";
			break;

		case WaitReturnPermanent:
			description = "Choose a permanent to return to its owner's hand.";
			break;

		case WaitUntapLand:
			description = "Choose a land to untap or click Done.";
			break;

		case WaitChoiceExhume:
			description = "Choose a creature card in your graveyard";
			break;

		case WaitChoiceEnchantmentAlteration:
			description = "Choose a new host for the Aura.";
			break;

		case WaitChoiceCreature:
			description = "Choose a creature you control.";
			break;

		case WaitChoicePurgingScythe:
			description = "Choose a creature with least toughness.";
			break;

		case WaitPayCostSacrificeAnotherVampireOrZombie:
			description = "Sacrifice another Vampire or Zombie.";
			break;

		case WaitPayCostExileAnotherCreatureCardFromGyd:
			description = "Exile a creature card from your graveyard.";
			break;

		case WaitPayCostSacrificeGoblin:
			description = "Sacrifice a Goblin.";
			break;

		case WaitSacrificeCreatureOrLand:
			description = "Sacrifice a creature or land.";
			break;

		case WaitSacrificeCreatureOrPlaneswalker:
			description = "Sacrifice a creature or planeswalker.";
			break;
			
		case WaitPayCostSacrificeCreature:
		case WaitSacrificeCreature:
			description = "Sacrifice a creature.";
			break;

		case WaitSacrificeLand:
			description = "Sacrifice a land.";
			break;

		case WaitPayCostSacrificeEnchantment:
		case WaitSacrificeEnchantment:
			description = "Sacrifice an enchantment.";
			break;

		case WaitPayCostSacrificeArtifact:
			description = "Sacrifice an artifact.";
			break;

		case WaitPayCostSacrificePermanent:
			description = "Sacrifice a permanent.";
			break;

		case WaitPayCostSacrificeLand:
			description = "Sacrifice a land.";
			break;

		case WaitPayCostSacrificeForestOrPlains:
			description = "Sacrifice a Forest or Plains.";
			break;

		case WaitPayCostSacrificeForest:
			description = "Sacrifice a Forest.";
			break;

		case WaitPayCostReturnLand:
			description = "Return a land you control to its owner's hand.";
			break;

		case WaitPayCostReturnElf:
			description = "Return an Elf you control to its owner's hand.";
			break;

		case WaitHeartOfKiran:
			description = "Remove a loyalty counter from which Planeswalker ?";
			break;

		case WaitPayCostTapUntappedCreature:
		case WaitPayCostCrew:
			description = "Tap an untapped creature you control.";
			break;

		case WaitExileForIchorid:
			description = "Exile another black creature card from your graveyard.";
			break;

		case WaitDiscard:
			description = "Discard a card from your hand.";
			break;

		case WaitBrainstorm:
			description = "Put a card from your hand on top of your library.";
			break;

		case WaitChoiceSneakAttack:
			description = "Choose a creature card from your hand.";
			break;

		case WaitPayCostDiscardACreatureCard:
			description = "Discard a creature card from your hand.";
			break;

		case WaitPayCostDiscardACard:
			description = "Discard a card from your hand.";
			break;

		case WaitDeclareAttackers:
			description = "Declare attacking creature(s).";
			break;

		case WaitDeclareBlockers:
			description = "Declare blocking creature(s).";
			break;

		case WaitChooseCreatureToBlock:
			description = "Choose a creature to block.";
			break;

		case WaitChooseRecipientToAttack:
			description = "Choose a player or a planeswalker to attack.";
			break;

		case WaitReorderBlockers:
			description = "Rearrange the order of blocking creatures.";
			break;

		case WaitChooseReplaceDraw:
			description = "Choose a replacement effect to apply.";
			break;

		case WaitChooseManaCombination:
			description = "Choose a mana type or combination.";
			break;

		default:
			description = "Invalid state";
			break;
		}
		return description;
	}

	public void updatePlayers(String input) {
		int index_pound;
		int idPlayer;
		String playerName;

		input = input.substring(input.indexOf("=")+1);
		String players[] = input.split(";");

		for (int i = 0; i < players.length; i++) {
			index_pound = players[i].indexOf("#");
			idPlayer = Integer.parseInt(players[i].substring(index_pound + 1));
			if (idPlayer != _idPlayer)
				_idOpponent = idPlayer;
			playerName = players[i].substring(0, index_pound);
			_players.add(new UIPlayer(idPlayer, playerName));
			_zones.addAll(_players.get(i).getZones());
		}
	}

	public void updateStatus(String input) {
		if (!_highlights.isEmpty())
			_highlights.clear();
		input = input.substring(input.indexOf("=")+1);
		String info[] = input.split(";");

		_bDebugMode = Boolean.parseBoolean(info[ID_STATUS_DEBUGMODE]);
		if (_bDebugMode)
			_app.setTitle("MTGE client *** DEBUG MODE ***");
		else
			_app.setTitle("MTGE client");
		_turn = Integer.parseInt(info[ID_STATUS_TURN]);
		_phase = Phase.valueOf(info[ID_STATUS_PHASE]);
		_step = Step.valueOf(info[ID_STATUS_STEP]);

		if (_phase == Phase.PostMain)
		{
			_attacks.clear();
			_blocks.clear();
		}
		else if (_step == Step.BegCombat) {
			_declaredPotentialAttacker.clear();
			_undeclaredPotentialAttacker.clear();
			_declaredPotentialBlocker.clear();
			_undeclaredPotentialBlocker.clear();
		}

		int idxColon = info[ID_STATUS_STATE].indexOf(":");
		if (idxColon == -1)
			_app.setGameState(State.valueOf(info[ID_STATUS_STATE]));
		else
		{
			_app.setGameState(State.valueOf(info[ID_STATUS_STATE].substring(0, idxColon)));
			_targetor = info[ID_STATUS_STATE].substring(idxColon + 1);
		}

		// update players life totals and active player
		for (int i = ID_STATUS_FIRST_PLAYER; i < info.length; i++) {
			String data = info[i];
			int id;

			String[] playerInfo = data.split("\\|");

			id = Integer.parseInt(playerInfo[0]); // player ID
			if (playerInfo[1].equals("1"))
				_idActivePlayer = id;                // active player ? (1 = yes, 0 = no)
			int life = Integer.parseInt(playerInfo[2]); // life total
			int poison = Integer.parseInt(playerInfo[3]); // poison counters
			int energy = Integer.parseInt(playerInfo[4]); // energy counters {E}
			int monarch = Integer.parseInt(playerInfo[5]); // Monarch ? (1 = yes, 0 = no)
			int libSize = Integer.parseInt(playerInfo[6]); // number of cards in the library
			int handSize = Integer.parseInt(playerInfo[7]); // number of cards in hand

			UIPlayer player = findPlayerByID(id);

			/* Player mana pool */
			player.setMana("W", Integer.parseInt(playerInfo[8]));      // White mana
			player.setMana("U", Integer.parseInt(playerInfo[9]));      // Blue mana
			player.setMana("B", Integer.parseInt(playerInfo[10]));     // Black mana
			player.setMana("R", Integer.parseInt(playerInfo[11]));     // Red mana
			player.setMana("G", Integer.parseInt(playerInfo[12]));     // Green mana
			player.setMana("C", Integer.parseInt(playerInfo[13]));     // Colorless mana

			player.setLife(life);
			player.setLibSize(libSize);
			player.setHandSize(handSize);
			player.getNameButton().ID = id;

			String name_caption = player.getName() + " " + player.getLife();
			if (monarch == 1)
				player.setMonarch(true);
			else
				player.setMonarch(false);
			if (poison > 0)
				name_caption = name_caption + " " + poison + "P"; 
			if (energy > 0)
				name_caption = name_caption + " " + energy + "E";
			player.getNameButton().setLabel(name_caption);
		}

		/* initialize button clickability */
		_btnPassPriority.setEnabled(false);
		_btnYes.setEnabled(false);
		_btnNo.setEnabled(false);
		_btnDone.setEnabled(false);

		_btnPlus.setEnabled(false);
		_btnMinus.setEnabled(false);

		_btnXValue.setEnabled(false);
		_btnXValue.setLabel("X = " + _xValue);

		switch (_app.getGameState()) {
		case WaitDiscard:
		case WaitPayCostDiscardACard:
		case WaitPayCostDiscardACreatureCard:
			if (findPlayerByID(_idPlayer).getHand().getCards().size() == 0)
				_btnDone.setEnabled(true);
			break;

			// Enable DONE button
		case WaitDeclareAttackers:
		case WaitDeclareBlockers:
		case WaitReorderBlockers:
		case WaitChoiceLookTop:
		case LookPlayersHand:
		case WaitChoiceSylvanScrying:
		case WaitChoiceBasicForestCard:
		case WaitChoiceForestCard:
		case WaitChoiceBasicLand:
		case WaitChoicePlainsIsland:
		case WaitChoiceIslandSwamp:
		case WaitChoiceSwampMountain:
		case WaitChoiceMountainForest:
		case WaitChoiceForestPlains:
		case WaitChoicePlainsSwamp:
		case WaitChoiceSwampForest:
		case WaitChoiceForestIsland:
		case WaitChoiceIslandMountain:
		case WaitChoiceMountainPlains:
		case WaitChoiceStoneforgeMystic_search:
		case WaitChoiceStoneforgeMystic_put:
		case WaitChoiceSneakAttack:
		case WaitChoiceCopperGnomes:
		case WaitChoiceGoblinLackey:
		case WaitChoiceAcademyResearchers:
		case WaitChoiceAetherVial_put:
		case WaitChoiceMoxDiamond:
		case PromptDredge:
		case PromptRevealPlainsOrIsland:
		case PromptRevealIslandOrSwamp:
		case PromptRevealSwampOrMountain:
		case PromptRevealMountainOrForest:
		case PromptRevealForestOrPlains:
		case WaitChoiceChordOfCalling:
		case WaitChoiceCitanulFlute:
		case WaitChoiceGreenSunZenith:
		case WaitChoiceCreatureCard:
		case WaitChoiceRemembrance:
		case WaitChoiceGoblinCard:
		case WaitChoiceLandOrCreatureCard:
		case WaitChoiceArtifactOrCreatureCard:
		case WaitChoiceArtifactOrEnchantmentCard:
		case WaitChoiceEnchantment:
		case WaitChoiceEnchantmentCardWithCCM3orLess:
		case WaitChoiceInstantOrSorceryCard:
		case WaitchoiceCardInGraveyard:
		case WaitChoiceScryPutBottom:
		case WaitChoiceCollectedCompany:
		case WaitChoicePutCreatureCardInHand:
		case WaitChoiceOathOfNissa:
		case WaitExileForIchorid:
		case PromptTargetsOrDone:
		case WaitUntapLand:
		case WaitChoiceThoughtseize:
		case WaitChoiceDuress:
		case WaitChoiceReprocess:
		case WaitChoiceShowAndTell:
		case WaitChoiceCreatureCardWithToughness2orLess:
			_btnDone.setEnabled(true);
			break;

			//Enable YES and NO buttons
		case PromptDoYouWantToDrawACard:
		case PromptDoYouWantToUseTheAbility:
		case PromptDoYouWantToShuffle:
		case PromptCastWithoutPayingManaCost:
		case PromptDoYouWantPutTheCardInYourHand:
		case PromptDoYouWantPutInGraveyard:
		case PromptDoYouWantToSacrificeALand:
		case PromptPayPunishingFire:
		case PromptMulligan:
		case PromptDoYouWantToUntap:
		case PromptTurnabout_doYouWantToTap:
		case PromptPayForShockland:
		case PromptPay_1life:
		case PromptPay_2life:
		case PromptPayChildOfGaea:
		case PromptPayDriftingDjinn:
		case PromptDiscardToPay:
		case PromptSacrificeEnchantment:
		case PromptPay_1mana:
		case PromptPay_2mana:
		case PromptPay_3mana:
		case PromptPay_4mana:
		case PromptPay_Xmana:
		case PromptPayUpkeepCost:
		case PromptPayEchoCost:
		case PromptPayCumulativeUpkeep:
		case PromptApplyReplaceDraw:
		case PromptAbundance_CardType:
			_btnYes.setEnabled(true);
			_btnNo.setEnabled(true);
			break;

			// Enable -/+ and DONE buttons
		case PromptPayLifeStaticETB:
		case PromptXValue:
		case PromptMode:
		case WaitChoiceTurnabout:
		case WaitChoiceHarnessedLightning:
		case PromptCatastrophe_PermanentType:
			_btnPlus.setEnabled(true);
			_btnMinus.setEnabled(true);
			_btnXValue.setEnabled(true);
			_btnDone.setEnabled(true);
			break;

			// Nothing to do
		case WaitChoiceForceOfWill:
		case WaitChoiceDaze:
		case WaitChoiceTutor:
		case WaitChoiceDamageSource:
		case WaitChoiceDamageArtifactSource:
		case WaitChoiceDamageLandSource:
		case WaitChoiceDamageWhiteSource:
		case WaitChoiceDamageBlueSource:
		case WaitChoiceDamageBlackSource:
		case WaitChoiceDamageRedSource:
		case WaitChoiceDamageGreenSource:
		case WaitChoiceCoercion:
		case WaitChooseReplaceDraw:
		case WaitChooseColor:
		case PromptChooseColorStaticETB:
		case WaitChooseTriggeredManaAbilityColor:
		case WaitChooseManaCombination:
		case WaitChooseCreatureToBlock:
		case WaitChooseRecipientToAttack:
			break;

		case Ready:
			_btnPassPriority.setEnabled(true);
			if (_response.equals("OKholdPriority") || _response.startsWith("Error"))
				_app.setAutoPassPriority(false);
			if (!_bDebugMode) {
				if (_app.isAutoPassPriority())
					_app.send(Game.COMMAND_PASS_PRIORITY);
			}
			break;

		case WaitingForOpponent:
			_app.setAutoPassPriority(false);
			break;

		default:
			break;
		}
	}

	private String getActivePlayerName() {
		String ret;
		try {
			ret = findPlayerByID(_idActivePlayer).getName();
		} catch (NullPointerException e) {
			ret = "error_active_player_name";
		}

		return ret;
	}

	public UIPlayer findPlayerByID(int id) {
		for (UIPlayer p : _players) {
			if (p.getId() == id)
				return p;
		}
		return null;
	}

	private HoldPriority getHPSetting(Step step) {
		for (HoldPriority hp : _holdPrioritiesSettings) {
			if (hp.getStep() == step)
				return hp;
		}
		return null;
	}

	public void doFkeys() {
		boolean bDisable = false;

		/* Simulate click of "Done" button when no creatures can be declared as attacker */
		if ((_app.getGameState() == State.WaitDeclareAttackers) && _undeclaredPotentialAttacker.isEmpty() && _declaredPotentialAttacker.isEmpty())
			_app.send(Game.COMMAND_DONE);

		/* Simulate click of "Done" button when no creatures can be declared as blocker */
		if ((_app.getGameState() == State.WaitDeclareBlockers) && _undeclaredPotentialBlocker.isEmpty() && _declaredPotentialBlocker.isEmpty())
			_app.send(Game.COMMAND_DONE);

		if (_app.getGameState() != State.Ready)
			return;

		if (_bF4_activated)
		{
			if (_stack.size() > 0)
				bDisable = true;

			if ((_idActivePlayer == _idPlayer) && (getHPSetting(_step).getMy() == true)) {
				bDisable = true;
			}

			if ((_idActivePlayer != _idPlayer) && (getHPSetting(_step).getHis() == true)) {
				bDisable = true;
			}

			if (bDisable)
				_bF4_activated = false;
			else
				_app.send(Game.COMMAND_PASS_PRIORITY);
		}
		/*
		if (_bF6_activated)
		{
			if ((_turn == _nextTurn) && _phaseStep.equals("PreMain"))
				_bF6_activated = false;
			else
				_app.send(PASS_PRIORITY);
		}
		 */
	}

	private UIZonePanel createZonePanel(UIZone zone, int x, int y, boolean bNoCloseButton) {
		int nbCards = zone.getCards().size();
		if (nbCards == 0)
			return null;
		JFrame frame = new JFrame();

		int preferredWidth = (nbCards * (CARD_WIDTH + CARD_SPACING)) + AURA_X_OFFSET;
		if (preferredWidth > 1024)
			preferredWidth = 1024;

		int nbCardsPerRow = preferredWidth / (CARD_WIDTH + CARD_SPACING);

		int preferredHeight = AURA_Y_OFFSET + CARD_HEIGHT + (((nbCards / nbCardsPerRow)+1) * 30);

		frame.setSize(preferredWidth, preferredHeight);
		frame.setAlwaysOnTop(true);
		frame.setTitle(zone.getName());
		if ((x == -1) || (y == -1)) {
			x = _app.getX() + (_app.getWidth() - frame.getWidth()) / 2;
			y = _app.getY() + (_app.getHeight() - frame.getHeight()) / 2;
		}
		else
		{
			x += _app.getX();
			y += _app.getY();
		}
		frame.setLocation(x, y);

		UIZonePanel panel = new UIZonePanel(this);
		panel.setApp(_app);
		panel.addMouseListener(new MtgeGuiMouseAdapter(panel));
		panel.addMouseMotionListener(new MtgeGuiMouseAdapter(panel));
		panel.setVisible(true);
		panel.setBackground(Color.PINK);
		panel.setZone(zone);
		frame.add(panel);
		if (bNoCloseButton)
			frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setVisible(true);
		return panel;
	}

	public UICard getPermanentByID(int idCard) {
		UIPlayer me = findPlayerByID(_idPlayer);
		for (UIZone zone : _zones)
		{
			if ((zone != me.getHand()) && (zone != _library) && (zone != me.getGraveyard()))
				for (UICard c : zone.getCards()) {
					if (c.getIdCard() == idCard)
						return c;
				}
		}
		return null;
	}

	public void updateCardInfo(String input) {
		input = input.substring(input.indexOf("=")+1);
		_app.setCardInfo(input);
	}

	public void updateHighlight(String input) {
		input = input.substring(input.indexOf("=")+1);
		String highlights[] = input.split(";");
		_highlights.clear();
		for (String highlight : highlights)
			if (highlight.length() > 0)
				_highlights.addElement(Integer.parseInt(highlight));
	}

	/**
	 * 
	 * @param input
	 */
	public void updateAttacking(String input) {
		int idxArrow;
		int idAttackingCreature;
		int idRecipient;
		String attackEntries[];

		input = input.substring(input.indexOf("=")+1);
		attackEntries = input.split(";");
		_attacks.clear();
		for (String attackEntry : attackEntries)
		{
			if (attackEntry.length() > 0)
			{
				idxArrow = attackEntry.indexOf("->");
				if (idxArrow != -1)
				{
					idAttackingCreature =  Integer.parseInt(attackEntry.substring(0, idxArrow));
					idRecipient =  Integer.parseInt(attackEntry.substring(idxArrow + 2));
				}
				else
				{
					idAttackingCreature =  Integer.parseInt(attackEntry);
					idRecipient = -1;
				}
				_attacks.put(idAttackingCreature, idRecipient);
			}
		}
	}

	/**
	 * 
	 * @param input
	 */
	public void updateBlocks(String input) {
		int idxArrow;
		int idBlockingCreature;
		int idBlockedCreature;
		String blockEntries[];

		input = input.substring(input.indexOf("=")+1);
		blockEntries = input.split(";");
		_blocks.clear();		
		for (String blockEntry : blockEntries)
		{
			if (blockEntry.length() > 0)
			{
				idxArrow = blockEntry.indexOf("->");
				if (idxArrow != -1)
				{
					idBlockingCreature =  Integer.parseInt(blockEntry.substring(0, idxArrow));
					idBlockedCreature =  Integer.parseInt(blockEntry.substring(idxArrow+2));
				}
				else
				{
					idBlockingCreature = Integer.parseInt(blockEntry);
					idBlockedCreature = -1;
				}
				_blocks.put(idBlockingCreature, idBlockedCreature);
			}
		}
	}

	public void updateManaChoices(String input) {
		input = input.substring(input.indexOf("=")+1);
		_app.setManaChoices(input);
	}

	public void updateAtkDecl(String input) {
		input = input.substring(input.indexOf("=")+1);
		String potentialAttackers[] = input.split(";");
		//_attacks.clear();
		_undeclaredPotentialAttacker.clear();
		_declaredPotentialAttacker.clear();
		for (String potentialAttacker : potentialAttackers)
		{
			int iSymbolDeclared = potentialAttacker.length() - 1;
			String symbolDeclared = potentialAttacker.substring(iSymbolDeclared);
			int id =  Integer.parseInt(potentialAttacker.substring(0, iSymbolDeclared));
			if (symbolDeclared.compareTo("!") == 0)
				_declaredPotentialAttacker.addElement(id);
			else if (symbolDeclared.compareTo("?") == 0)
				_undeclaredPotentialAttacker.addElement(id);
		}
	}

	public void updateBlockables(String input) {
		input = input.substring(input.indexOf("=")+1);
		String blockableAttackers[] = input.split(";");
		_blockableAttacker.clear();

		for (String blockableAttacker : blockableAttackers)
		{
			int id =  Integer.parseInt(blockableAttacker);
			_blockableAttacker.addElement(id);
		}
	}

	public void updateAttackables(String input) {
		input = input.substring(input.indexOf("=")+1);
		String attackables[] = input.split(";");
		_attackableRecipient.clear();

		for (String attackable : attackables)
			_attackableRecipient.addElement(Integer.parseInt(attackable));
	}

	public void updateBloDecl(String input) {
		input = input.substring(input.indexOf("=")+1);
		String potentialBlockers[] = input.split(";");
		_undeclaredPotentialBlocker.clear();
		_declaredPotentialBlocker.clear();
		for (String potentialBlocker : potentialBlockers)
		{
			int iSymbolDeclared = potentialBlocker.length() - 1;
			String symbolDeclared = potentialBlocker.substring(iSymbolDeclared);
			int id =  Integer.parseInt(potentialBlocker.substring(0, iSymbolDeclared));
			if (symbolDeclared.compareTo("!") == 0)
				_declaredPotentialBlocker.addElement(id);
			else if (symbolDeclared.compareTo("?") == 0)
				_undeclaredPotentialBlocker.addElement(id);
		}
	}

	public UICard findCardById(int idCard) {
		Vector<UICard> cards;
		for (UIZone zone : _zones) {
			if (zone != _possibleActions) {
				cards = zone.getCards();
				for (UICard card : cards) {
					if (card.getIdCard() == idCard)
						return card;
				}	
			}
		}
		return null;
	}

	public void updateID(String packet) {
		packet = packet.substring(packet.indexOf("=")+1);
		String id[] = packet.split(";");
		_idPlayer = Integer.parseInt(id[0]);
	}

	public void updateBlockOrder(String packet) {
		packet = packet.substring(packet.indexOf("=")+1);
		String entries[] = packet.split(";");

		for (String entry : entries) {
			if (entry.length() > 0) {
				String creatures[] = entry.split(",");
				for (int iBlocker = 0; iBlocker < creatures.length; iBlocker++) {
					int blockerID = Integer.parseInt(creatures[iBlocker]);
					UICard blocker = findCardById(blockerID);
					blocker.setBlockOrder(iBlocker + 1); 
				}
			}
		}
	}

	public void activateF4() {
		_bF4_activated = true;
	}

	public Vector<HoldPriority> getHoldPrioritiesSettings() {
		return _holdPrioritiesSettings;
	}

	public void setResponse(String val) {
		_response = val;
	}

	public void closeLibPanel(Component cpn) {
		JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(cpn);
		frame.setVisible(false);
		frame.dispose();
		_libraryPanel.clearZone();
		_libraryPanel = null;
	}

	public void doButtonClick(UIButton clickedButton) {
		UIPlayer me = findPlayerByID(_idPlayer);
		UIPlayer opponent = findPlayerByID(_idOpponent);

		// for targeting players
		for (UIPlayer player : _players) {
			UIButton p = player.getNameButton();
			if ((clickedButton.ID == p.ID) && ((_app.getGameState() == State.PromptTargets) || (_app.getGameState() == State.WaitChooseRecipientToAttack)))
				_app.send(p.ID);
		}

		if ((clickedButton == _btnPassPriority) && (_app.getGameState() == State.Ready))
		{
			_app.F2pressed();
		}
		else if (clickedButton == _btnYes)
			_app.send(Game.COMMAND_YES);
		else if (clickedButton == _btnNo)
			_app.send(Game.COMMAND_NO);
		else if (clickedButton == _btnDone)
		{
			switch (_app.getGameState()) {
			case PromptXValue:
			case PromptMode:
			case WaitChoiceTurnabout:
			case PromptCatastrophe_PermanentType:
			case PromptPayLifeStaticETB:
			case WaitChoiceHarnessedLightning:
				_app.send(_xValue);
				break;

			default:
				if (_libraryPanel != null)
					closeLibPanel(_libraryPanel);
				_app.send(Game.COMMAND_DONE);
				break;
			}
		}
		else if (clickedButton == _btnPlus) {
			switch (_app.getGameState()) {
			case PromptXValue:
			case PromptMode:
			case WaitChoiceTurnabout:
			case PromptCatastrophe_PermanentType:
			case WaitChoiceHarnessedLightning:
			case PromptPayLifeStaticETB:
				_xValue++;
				_btnXValue.setLabel("X = " + _xValue);
				break;

			default:
				break;
			}
			repaint();
		}
		else if (clickedButton == _btnMinus) {
			switch (_app.getGameState()) {
			case PromptXValue:
			case PromptMode:
			case WaitChoiceTurnabout:
			case PromptCatastrophe_PermanentType:
			case WaitChoiceHarnessedLightning:
			case PromptPayLifeStaticETB:
				if (_xValue != 0)
					_xValue--;
				_btnXValue.setLabel("X = " + _xValue);
				break;

			default:
				break;
			}
			repaint();
		}
		// my panels
		else if (clickedButton == me.getGraveyardButton())
			me.setGraveyardPanel(createZonePanel(me.getGraveyard(), clickedButton.x, clickedButton.y, false));
		else if (clickedButton == me.getExileButton())
			me.setExilePanel(createZonePanel(me.getExile(), clickedButton.x, clickedButton.y, false));
		else if (clickedButton == me.getCommandButton())
			me.setCommandPanel(createZonePanel(me.getCommand(), clickedButton.x, clickedButton.y, false));

		// my opponent's panels
		else if (clickedButton == opponent.getGraveyardButton())
			opponent.setGraveyardPanel(createZonePanel(opponent.getGraveyard(), clickedButton.x, clickedButton.y, false));
		else if (clickedButton == opponent.getExileButton())
			opponent.setExilePanel(createZonePanel(opponent.getExile(), clickedButton.x, clickedButton.y, false));
		else if (clickedButton == opponent.getCommandButton())
			opponent.setCommandPanel(createZonePanel(opponent.getCommand(), clickedButton.x, clickedButton.y, false));

		else if (_actionButtons.contains(clickedButton))
		{
			if (clickedButton.getActionId() != -1)
				_app.send(Game.COMMAND_PERFORM_ACTION + " " + clickedButton.getSourceId() + " " + clickedButton.getActionId());
			_app.setAutoPassPriority(true);
		}
	}

	public void doCardClick(UICard cardClicked) {
		super.doCardClick(cardClicked);
		_app.setLastPanelClicked(this);
	}

	public UIManaChoice getManaChoiceAtPosition(int x, int y) {
		for (UIManaChoice umc : _manaChoicesButtons) {
			if (umc.hit(x, y))
				return umc;
		}
		return null;
	}

	public UICard getCardAtPosition(int x, int y) {
		for (UIZone zone : _zones) {

			if (zone.getName().contains("graveyard") || zone.getName().contains("command") || zone.getName().contains("exile"))
				continue;

			if ((zone == _library) && !isLibraryVisible())
				continue;

			int i = zone.getCards().size();
			while (i > 0) {
				UICard card = zone.getCards().get(i-1);
				if ((zone != _possibleActions) && (card.hit(x, y)))
				{
					if (card.getHostID() != 0) {
						UICard host = findCardById(card.getHostID());
						if (host.hit(x, y))
							return host;
					}
					return card;
				}
				i--;
			}
		}
		return null;
	}

	protected UIButton getClickedButton(int x, int y) {
		x += getX();
		y += getY();
		UIButton ret = super.getClickedButton(x, y);

		if (ret != null)
			return ret;

		for (UIPlayer player : _players) {
			for (UIButton button : player.getButtons()) {
				if ((x >= button.x) && (x <= button.x + button.width) && (y >= button.y) && (y <= button.y + button.height))
				{
					if (button.isEnabled())
						return button;
				}	
			}
		}
		return null;
	}

	public void updateActions(String input) {
		String[] zones = input.split("\\|");
		int sourceId;
		_possibleActions.load(zones[0]);

		if (_possibleActions.size() == 0) {
			System.out.println("no action available");
			return;
		}

		UICard c = _possibleActions.get(0);
		sourceId = _possibleActions.get(0).getSourceId();

		// if the only possible action is to cast the spell or play the land card, play it
		if ((_possibleActions.size() == 1) && (c.getRulesText().equals("Play land") || c.getRulesText().equals("Cast"))) {
			_app.send(Game.COMMAND_PERFORM_ACTION + " " + sourceId + " " + c.getIdCard());
			_app.setAutoPassPriority(true);
			return;
		}

		_app.getLastPanelClicked().buildActionButtons(_possibleActions, sourceId);
	}

	public void clearCounters() {
		try {
			for (UICard c : _allCards)
				c.clearCounters();
		} catch (ConcurrentModificationException e) {
			System.err.println("Error in Board.clearCounters()");
		}
	}

	public void clearAuras() {
		_auras = new HashMap<Integer, Vector<Integer>>();
	}

	public void updateAuras() {
		UICard host;
		Vector<Integer> aurasID;

		for (int hostID : _auras.keySet()) {
			host = this.findCardById(hostID);
			aurasID = _auras.get(hostID);
			for (int auraID : aurasID) {
				host.addAuraID(auraID);
			}
		}
	}
}
