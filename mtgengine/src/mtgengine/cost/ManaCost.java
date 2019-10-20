package mtgengine.cost;

import java.util.HashMap;

public class ManaCost {
	public enum Symbol { Generic, X,
		White, Blue, Black, Red, Green,
		Hybrid_wu, Hybrid_ub, Hybrid_br, Hybrid_rg, Hybrid_gw,
		Hybrid_wb, Hybrid_bg, Hybrid_gu, Hybrid_ur, Hybrid_rw,
		Hybrid_W, Hybrid_U, Hybrid_B, Hybrid_R, Hybrid_G,
		Phyrexian_W, Phyrexian_U, Phyrexian_B, Phyrexian_R, Phyrexian_G,
		};

		private HashMap<Symbol, Integer> _manaCost;
		private static HashMap<Symbol, String> _manaStringDefinition = new HashMap<ManaCost.Symbol, String>();

		public static void initialize() {
			_manaStringDefinition.put(Symbol.X, "{X}");
			
			_manaStringDefinition.put(Symbol.White, "{W}");
			_manaStringDefinition.put(Symbol.Blue,  "{U}");
			_manaStringDefinition.put(Symbol.Black, "{B}");
			_manaStringDefinition.put(Symbol.Red,   "{R}");
			_manaStringDefinition.put(Symbol.Green, "{G}");
			
			_manaStringDefinition.put(Symbol.Hybrid_wu, "{w/u}");
			_manaStringDefinition.put(Symbol.Hybrid_ub, "{u/b}");
			_manaStringDefinition.put(Symbol.Hybrid_br, "{b/r}");
			_manaStringDefinition.put(Symbol.Hybrid_rg, "{r/g}");
			_manaStringDefinition.put(Symbol.Hybrid_gw, "{g/w}");
			
			_manaStringDefinition.put(Symbol.Hybrid_wb, "{w/b}");
			_manaStringDefinition.put(Symbol.Hybrid_bg, "{b/g}");
			_manaStringDefinition.put(Symbol.Hybrid_gu, "{g/u}");
			_manaStringDefinition.put(Symbol.Hybrid_ur, "{u/r}");
			_manaStringDefinition.put(Symbol.Hybrid_rw, "{r/w}");
			
			_manaStringDefinition.put(Symbol.Phyrexian_W, "{w/p}");
			_manaStringDefinition.put(Symbol.Phyrexian_U, "{u/p}");
			_manaStringDefinition.put(Symbol.Phyrexian_B, "{b/p}");
			_manaStringDefinition.put(Symbol.Phyrexian_R, "{r/p}");
			_manaStringDefinition.put(Symbol.Phyrexian_G, "{g/p}");
			
			_manaStringDefinition.put(Symbol.Hybrid_W, "{2/w}");
			_manaStringDefinition.put(Symbol.Hybrid_U, "{2/u}");
			_manaStringDefinition.put(Symbol.Hybrid_B, "{2/b}");
			_manaStringDefinition.put(Symbol.Hybrid_R, "{2/r}");
			_manaStringDefinition.put(Symbol.Hybrid_G, "{2/g}");
		}
		
		public static boolean isValid(String manaCost) {
			if (manaCost.equals(""))
				return false;

			char c;
			boolean bInsideBracket = false;
			
			for (int i = 0; i < manaCost.length(); i++) {
				c = manaCost.charAt(i);
				switch (c) {
				case '{' :
					bInsideBracket = true;
					break;
					
				case '}' :
					bInsideBracket = false;
					break;
				
				case 'w':
				case 'u':
				case 'b':
				case 'r':
				case 'g':
				case 'W':
				case 'U':
				case 'B':
				case 'R':
				case 'G':
				case 'C':
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case 'X':
					if (bInsideBracket == false)
						return false;
					break;

				case '/':
					String hybridSymbol;
					hybridSymbol = manaCost.substring(i-1, i+2);
					
					switch (hybridSymbol) {
					//-------- Ally color hybrid ---------------------------------------------
					case "w/u":
					case "u/b":
					case "b/r":
					case "r/g":
					case "g/w":
				    //-------- Ennemy color hybrid ---------------------------------------------
					case "w/b":
					case "b/g":
					case "g/u":
					case "u/r":
					case "r/w":
				    //------- Phyrexian mana ----------------------------------------------
					case "w/p":
					case "u/p":
					case "b/p":
					case "r/p":
					case "g/p":
					//------- Monocolor hybrid mana (i.e. Spectral Procession) ----------------------------------------------
					case "2/w":
					case "2/u":
					case "2/b":
					case "2/r":
					case "2/g":
						break;

					// incorrect hybrid mana
					default:
						return false;
					}
					break;
					
				// incorrect symbol
				default:
					return false;
				}
			}
			return true;
		}
		
		public ManaCost(String manaCost) {
			_manaCost = new HashMap<Symbol, Integer>();
			char c;
			
			for (int i = 0; i < manaCost.length(); i++) {
				c = manaCost.charAt(i);
				switch (c) {
				case '{' :
				case '}' :
					break;
				
				case 'W':
					addManaType(Symbol.White);
					break;
				
				case 'U':
					addManaType(Symbol.Blue);
					break;
				
				case 'B':
					addManaType(Symbol.Black);
					break;
				
				case 'R':
					addManaType(Symbol.Red);
					break;
				
				case 'G':
					addManaType(Symbol.Green);
					break;
				
				case '0':
				case '1':
				case '2':
					if (manaCost.charAt(i + 1) == '/')
						break;
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					int value = c - '0';
					
					while (manaCost.charAt(i + 1) != '}') {
						i = i + 1;
						value = (value * 10) + (manaCost.charAt(i) - '0');
					}
					
					_manaCost.put(Symbol.Generic, value);
					break;
					
				case 'X':
					addManaType(Symbol.X);
					break;
				
				case '/':
					String hybridSymbol;
					hybridSymbol = manaCost.substring(i-1, i+2);
					
					switch (hybridSymbol) {
					//-------- Ally color hybrid ---------------------------------------------
					case "w/u":
						addManaType(Symbol.Hybrid_wu);
						break;

					case "u/b":
						addManaType(Symbol.Hybrid_ub);
						break;
						
					case "b/r":
						addManaType(Symbol.Hybrid_br);
						break;
						
					case "r/g":
						addManaType(Symbol.Hybrid_rg);
						break;
						
					case "g/w":
						addManaType(Symbol.Hybrid_gw);
						break;
						
				    //-------- Ennemy color hybrid ---------------------------------------------
					case "w/b":
						addManaType(Symbol.Hybrid_wb);
						break;
						
					case "b/g":
						addManaType(Symbol.Hybrid_bg);
						break;
						
					case "g/u":
						addManaType(Symbol.Hybrid_gu);
						break;
						
					case "u/r":
						addManaType(Symbol.Hybrid_ur);
						break;
					
					case "r/w":
						addManaType(Symbol.Hybrid_rw);
						break;
					
				    //------- Phyrexian mana ----------------------------------------------
					case "w/p":
						addManaType(Symbol.Phyrexian_W);
						break;
						
					case "u/p":
						addManaType(Symbol.Phyrexian_U);
						break;
						
					case "b/p":
						addManaType(Symbol.Phyrexian_B);
						break;
						
					case "r/p":
						addManaType(Symbol.Phyrexian_R);
						break;
					
					case "g/p":
						addManaType(Symbol.Phyrexian_G);
						break;
						
					//------- Monocolor hybrid mana (i.e. Spectral Procession) ----------------------------------------------
					case "2/w":
						addManaType(Symbol.Hybrid_W);
						break;
						
					case "2/u":
						addManaType(Symbol.Hybrid_U);
						break;
						
					case "2/b":
						addManaType(Symbol.Hybrid_B);
						break;
						
					case "2/r":
						addManaType(Symbol.Hybrid_R);
						break;
					
					case "2/g":
						addManaType(Symbol.Hybrid_G);
						break;
					
					default:
						//System.err.println("Unkown hybrid symbol : " + hybridSymbol);
						break;
					}
					break;
					
				default:
					break;
					
				}
			}
		}

		private void addManaType(Symbol manaType) {
			if (_manaCost.containsKey(manaType))
				_manaCost.put(manaType, _manaCost.get(manaType) + 1);
			else
				_manaCost.put(manaType, 1);	
		}
		
		public String toString() {
			String strManaCost = "";
			
			if (_manaCost.containsKey(Symbol.Generic))
				strManaCost += "{" + _manaCost.get(Symbol.Generic) + "}";
			
			for (Symbol manaType : _manaCost.keySet()) {
				if (manaType != Symbol.Generic) {
					for (int i = 0; i < _manaCost.get(manaType); i++)
						strManaCost += _manaStringDefinition.get(manaType);
				}
			}
			return strManaCost;
		}

		public int getNumberOfX() {
			return _manaCost.get(Symbol.X);
		}

		public boolean hasPhyrexianMana() {
			if (_manaCost.containsKey(Symbol.Phyrexian_W) || _manaCost.containsKey(Symbol.Phyrexian_U) ||
				_manaCost.containsKey(Symbol.Phyrexian_B) || _manaCost.containsKey(Symbol.Phyrexian_R) ||
				_manaCost.containsKey(Symbol.Phyrexian_G)) 
				return true;
			return false;
		}

		public boolean contains(Symbol manaSymbol) {
			return _manaCost.containsKey(manaSymbol);
		}

		public boolean hasMonocolorHybridMana() {
			if (_manaCost.containsKey(Symbol.Hybrid_W) || _manaCost.containsKey(Symbol.Hybrid_U) ||
				_manaCost.containsKey(Symbol.Hybrid_B) || _manaCost.containsKey(Symbol.Hybrid_R) ||
				_manaCost.containsKey(Symbol.Hybrid_G)) 
				return true;
			return false;
		}

		public int computeCMC() {
			int cmc =0 ;
			
			// Generic mana
			if (_manaCost.containsKey(Symbol.Generic))
				cmc += _manaCost.get(Symbol.Generic);
			
			// Other symbols
			for (Symbol symbol : _manaCost.keySet()) {
				switch (symbol) {
				
				// ignore X when calculating the CMC
				case Generic:
				case X:
					break;
					
				// monocolored hybrid mana count for 2 each (i.e. Spectral Procession)
				case Hybrid_W:
				case Hybrid_U:
				case Hybrid_B:
				case Hybrid_R:
				case Hybrid_G:
					cmc += (2 * _manaCost.get(symbol));
					break;
					
				// all other symbols count for 1 each
				default:
					cmc += (_manaCost.get(symbol));
					break;
				}
			}
			return cmc;
		}
}

