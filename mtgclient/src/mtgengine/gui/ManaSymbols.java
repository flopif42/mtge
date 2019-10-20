package mtgengine.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class ManaSymbols {
	private static HashMap<String, BufferedImage> _manaSymbols;
	
	public static BufferedImage getImage(String symbol) {
		return _manaSymbols.get(symbol);
	}
	
	public static void initialize() {
		_manaSymbols = new HashMap<String, BufferedImage>();
		
		try {
			// single color mana symbols
			_manaSymbols.put("{W}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/white.jpg")));
			_manaSymbols.put("{U}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/blue.jpg")));
			_manaSymbols.put("{B}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/black.jpg")));
			_manaSymbols.put("{R}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/red.jpg")));
			_manaSymbols.put("{G}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/green.jpg")));
			
			// hybrid color mana symbols
			// Ally
			_manaSymbols.put("{w/u}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/hybrid_wu.jpg")));
			_manaSymbols.put("{u/b}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/hybrid_ub.jpg")));
			_manaSymbols.put("{b/r}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/hybrid_br.jpg")));
			_manaSymbols.put("{r/g}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/hybrid_rg.jpg")));
			_manaSymbols.put("{g/w}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/hybrid_gw.jpg")));
			// Enemy
			_manaSymbols.put("{w/b}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/hybrid_wb.jpg")));
			_manaSymbols.put("{b/g}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/hybrid_bg.jpg")));
			_manaSymbols.put("{g/u}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/hybrid_gu.jpg")));
			_manaSymbols.put("{u/r}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/hybrid_ur.jpg")));
			_manaSymbols.put("{r/w}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/hybrid_rw.jpg")));
			
			// Monocolor hybrid symbols  (i.e. Spectral Procession)
			_manaSymbols.put("{2/w}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/hybrid_w.jpg")));
			_manaSymbols.put("{2/u}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/hybrid_u.jpg")));
			_manaSymbols.put("{2/b}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/hybrid_b.jpg")));
			_manaSymbols.put("{2/r}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/hybrid_r.jpg")));
			_manaSymbols.put("{2/g}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/hybrid_g.jpg")));
			
			// phyrexian mana symbols
			_manaSymbols.put("{w/p}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/phyrexian_w.jpg")));
			_manaSymbols.put("{u/p}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/phyrexian_u.jpg")));
			_manaSymbols.put("{b/p}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/phyrexian_b.jpg")));
			_manaSymbols.put("{r/p}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/phyrexian_r.jpg")));
			_manaSymbols.put("{g/p}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/phyrexian_g.jpg")));
			
			// colorless mana symbol
			_manaSymbols.put("{C}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/colorless.jpg")));
			_manaSymbols.put("{X}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/x.jpg")));
			
			// energy symbol
			_manaSymbols.put("{E}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/energy.jpg")));
			
			// tap symbol
			_manaSymbols.put("{T}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/tap.jpg")));
			
			// generic mana symbols (numbers)
			_manaSymbols.put("{0}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/zero.jpg")));
			_manaSymbols.put("{1}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/one.jpg")));
			_manaSymbols.put("{2}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/two.jpg")));
			_manaSymbols.put("{3}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/three.jpg")));
			_manaSymbols.put("{4}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/four.jpg")));
			_manaSymbols.put("{5}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/five.jpg")));
			_manaSymbols.put("{6}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/six.jpg")));
			_manaSymbols.put("{7}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/seven.jpg")));
			_manaSymbols.put("{8}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/eight.jpg")));
			_manaSymbols.put("{9}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/nine.jpg")));
			_manaSymbols.put("{10}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/ten.jpg")));
			_manaSymbols.put("{11}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/eleven.jpg")));
			_manaSymbols.put("{12}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/twelve.jpg")));
			_manaSymbols.put("{15}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/fifteen.jpg")));
			_manaSymbols.put("{16}", ImageIO.read(new File(Board.CLIENT_IMAGES_PATH + "/mana symbols/sixteen.jpg")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean contains(String data) {
		return _manaSymbols.containsKey(data);
	}
}
