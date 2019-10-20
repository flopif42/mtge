package mtgengine.card;

import java.io.File;
import java.util.HashMap;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import mtgengine.Xml;
import mtgengine.ability.ActivatedAbilityFactory;
import mtgengine.ability.Evergreen;
import mtgengine.ability.Protection;
import mtgengine.ability.SpellAbilityFactory;
import mtgengine.ability.StaticAbilityFactory;
import mtgengine.ability.TriggeredAbilityFactory;
import mtgengine.cost.Cost;
import mtgengine.cost.ManaCost;
import mtgengine.effect.ContinuousEffectFactory;
import mtgengine.type.CardType;
import mtgengine.type.CreatureType;
import mtgengine.type.Subtype;
import mtgengine.type.Supertype;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CardFactory {
	// Card definitions
	private static HashMap<String, Element> _cardDefinitions = new HashMap<String, Element>();
	private static Vector<String> _cardnames = new Vector<String>();
	
	/**
	 * Load card definition file in memory
	 */
	public static void loadCardDefinition(String filename) {
		File cardfile = new File(filename);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document definitionDocument = dBuilder.parse(cardfile);
			NodeList card_NodeList = definitionDocument.getElementsByTagName(Xml.XmlTag_Card);
			Element card_Element;
			String cardname;
			int nbCards = card_NodeList.getLength();
			int iCard;
			
			// Loop on cards and store their definition in the Hashmap
			for (iCard = 0; iCard < nbCards; iCard++)
			{
				card_Element = (Element) card_NodeList.item(iCard);
				if (card_Element.getElementsByTagName(Xml.XmlTag_Cardname).getLength() > 0)
				{
					cardname = card_Element.getElementsByTagName(Xml.XmlTag_Cardname).item(0).getTextContent();
					if (_cardDefinitions.containsKey(cardname)) {
						System.err.println("This card definition has already been loaded : " + cardname + ".");
						System.exit(1);
					}
					else {
						_cardDefinitions.put(cardname, card_Element);
						_cardnames.add(cardname);
					}
				}
			}
			
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
	public static String getRandomCard() {
		return _cardnames.get((new Random()).nextInt(_cardnames.size()));
	}
	
	/**
	 * 
	 * @param card_name
	 * @return
	 */
	public static Card create(String card_name) {
		// Card characteristics
		Card c;
		String cardName;
		String manaCost = null;
		boolean bXspell = false;
		int multiverseID;
		Vector<CardType> cardtypes = new Vector<CardType>();
		Vector<Color> colorIndicators = new Vector<Color>();
		Vector<Subtype> subtypes = new Vector<Subtype>();
		Vector<Supertype> supertypes = new Vector<Supertype>();
		Vector<CreatureType> creaturetypes = new Vector<CreatureType>();

		Element card_Element = _cardDefinitions.get(card_name);
		if (card_Element == null) {
			System.out.println("Error : unknown card : " + card_name + ".");
			return null;
		}
		
		// Card Name ************************************************************************************************************
		if (card_Element.getElementsByTagName(Xml.XmlTag_Cardname).getLength() == 0) {
			System.out.println("Error : no cardname element.");
			return null;
		}

		cardName = card_Element.getElementsByTagName(Xml.XmlTag_Cardname).item(0).getTextContent();
		
		// Multiverse ID ********************************************************************************************************
		if (card_Element.getAttribute("id") == null) {
			System.out.println("Loading card [" + cardName + "] error : no id attribute.");
			return null;
		}
		multiverseID = Integer.parseInt(card_Element.getAttribute("id"));
		
		// Color indicator(s) if any (this is used by tokens and cards that dont have mana cost but an indicator) ******************************************************************************************************
		if (card_Element.getElementsByTagName(Xml.XmlTag_Colors).getLength() == 1) {
			try {
				Element color_Element = (Element) card_Element.getElementsByTagName(Xml.XmlTag_Colors).item(0);
				NodeList color_NodeList = color_Element.getElementsByTagName(Xml.XmlTag_Color);
				for (int j = 0; j < color_NodeList.getLength(); j++)
					colorIndicators.add(Color.valueOf(color_Element.getElementsByTagName(Xml.XmlTag_Color).item(j).getTextContent()));
			} catch (IllegalArgumentException e1) {
				System.out.println("Loading card [" + cardName + "] error : Invalid color.");
				return null;
			}
		}
		
		// Card type(s) *********************************************************************************************************
		if (card_Element.getElementsByTagName(Xml.XmlTag_CardTypes).getLength() == 0) {
			System.out.println("Loading card [" + cardName + "] error : no cardtypes element.");
			return null;
		}
		Element cardtype_Element = (Element) card_Element.getElementsByTagName(Xml.XmlTag_CardTypes).item(0);
		NodeList cardtype_NodeList = cardtype_Element.getElementsByTagName(Xml.XmlTag_CardType);
		if (cardtype_NodeList.getLength() == 0) {
			System.out.println("Loading card [" + cardName + "] error : no cardtype element.");
			return null;
		}
		try {
			for (int j = 0; j < cardtype_NodeList.getLength(); j++)
				cardtypes.add(CardType.valueOf(cardtype_Element.getElementsByTagName(Xml.XmlTag_CardType).item(j).getTextContent()));
		} catch (IllegalArgumentException e) {
			System.out.println("Loading card [" + cardName + "] error : Invalid card type.");
			return null;
		}

		// Creature types (if any) **********************************************************************************************
		if (card_Element.getElementsByTagName(Xml.XmlTag_CreatureTypes).getLength() == 1) {
			Element creaturetype_Element = (Element) card_Element.getElementsByTagName(Xml.XmlTag_CreatureTypes).item(0);
			try {
				for (int j = 0; j < creaturetype_Element.getElementsByTagName(Xml.XmlTag_CreatureType).getLength(); j++)
					creaturetypes.add(CreatureType.valueOf(creaturetype_Element.getElementsByTagName(Xml.XmlTag_CreatureType).item(j).getTextContent()));
			} catch (IllegalArgumentException e) {
				System.out.println("Loading card [" + cardName + "] error : Invalid creature type.");
				return null;
			}
		}
		
		// Card subtypes (basic land types mostly) if any ***********************************************************************
		if (card_Element.getElementsByTagName(Xml.XmlTag_Subtypes).getLength() > 0) {
			Element subtype_Element = (Element) card_Element.getElementsByTagName(Xml.XmlTag_Subtypes).item(0);
			NodeList subtype_NodeList = subtype_Element.getElementsByTagName(Xml.XmlTag_Subtype);
			try {
				for (int j = 0; j < subtype_NodeList.getLength(); j++)
					subtypes.add(Subtype.valueOf(subtype_Element.getElementsByTagName(Xml.XmlTag_Subtype).item(j).getTextContent()));
			} catch (IllegalArgumentException e) {
				System.out.println("Loading card [" + cardName + "] error : Invalid card subtype.");
				return null;
			}
		}
		
		// Card supertypes (legendary, snow) if any *****************************************************************************
		if (card_Element.getElementsByTagName(Xml.XmlTag_Supertypes).getLength() > 0) {
			Element supertype_Element = (Element) card_Element.getElementsByTagName(Xml.XmlTag_Supertypes).item(0);
			NodeList supertype_NodeList = supertype_Element.getElementsByTagName(Xml.XmlTag_Supertype);
			try {
				for (int j = 0; j < supertype_NodeList.getLength(); j++)
					supertypes.add(Supertype.valueOf(supertype_Element.getElementsByTagName(Xml.XmlTag_Supertype).item(j).getTextContent()));
			} catch (IllegalArgumentException e) {
				System.out.println("Loading card [" + cardName + "] error : Invalid card supertype.");
				return null;
			}
		}
		
		// Mana cost if any (for example Lands have no mana cost) ***************************************************************
		if (card_Element.getElementsByTagName(Xml.XmlTag_ManaCost).getLength() == 1) {
			manaCost = card_Element.getElementsByTagName(Xml.XmlTag_ManaCost).item(0).getTextContent();
			if (manaCost.contains("{X}"))
				bXspell = true;
		}
		
		// Everything is OK, create the card
		ManaCost cardManaCost = null;
		if (manaCost != null)
			cardManaCost = new ManaCost(manaCost);
		c = new Card(cardName, multiverseID, /*cmc, */cardManaCost);
		c.setCardTypes(cardtypes);
		c.setSubtypes(subtypes);
		c.setSuperTypes(supertypes);
		c.setCreatureTypes(creaturetypes);
		c.setColorIndicators(colorIndicators);
		c.generatePerformableActions();
		if (bXspell)
			c.requiresXValue();
		
		// Tokens ***************************************************************************************************************
		if (card_Element.getElementsByTagName(Xml.XmlTag_Token).getLength() > 0) {
			Element e = (Element) card_Element.getElementsByTagName(Xml.XmlTag_Token).item(0);
			NodeList nl = e.getElementsByTagName(Xml.XmlTag_TokenName);
			if (nl.getLength() == 1) {
				c.setTokenName(e.getElementsByTagName(Xml.XmlTag_TokenName).item(0).getTextContent());
			}
			else {
				System.out.println("Loading card [" + cardName + "] error : Token name not specified.");
				return null;
			}
		}

		// Double faced cards (Innistrad werewolves) ****************************************************************************
		if (card_Element.getElementsByTagName(Xml.XmlTag_DayFace).getLength() > 0)
			c.setDayFaceCard(card_Element.getElementsByTagName(Xml.XmlTag_DayFace).item(0).getTextContent());
		if (card_Element.getElementsByTagName(Xml.XmlTag_NightFace).getLength() > 0)
			c.setNightFaceCard(card_Element.getElementsByTagName(Xml.XmlTag_NightFace).item(0).getTextContent());
		
		// Protections if any ***************************************************************************************************
		if (card_Element.getElementsByTagName(Xml.XmlTag_Protections).getLength() == 1) {
			Element prot_Element = (Element) card_Element.getElementsByTagName(Xml.XmlTag_Protections).item(0);
			NodeList prot_NodeList = prot_Element.getElementsByTagName(Xml.XmlTag_Protection);
			for (int j = 0; j < prot_NodeList.getLength(); j++) {
				try {
					String protTXT = prot_Element.getElementsByTagName(Xml.XmlTag_Protection).item(j).getTextContent();
					try {
						Color color = Color.valueOf(protTXT);
						c.setProtectionFrom(color);
					} catch (IllegalArgumentException e1) {
						try {
							Protection prot = Protection.valueOf(protTXT);
							c.setProtectionFrom(prot);
						} catch (IllegalArgumentException e2) {
							System.err.println(e2);
						}
					}
				} catch(IllegalArgumentException e) {
					System.out.println("Loading card [" + cardName + "] error : Invalid color specified in protection.");
					return null;
				}
			}
		}
		
		// Power and toughness (if applicable) **********************************************************************************
		if (card_Element.getElementsByTagName(Xml.XmlTag_PT).getLength() == 1)
			c.setPrintedPT(card_Element.getElementsByTagName(Xml.XmlTag_PT).item(0).getTextContent());

		// Loyalty (if applicable) **********************************************************************************************
		if (card_Element.getElementsByTagName(Xml.XmlTag_Loyalty).getLength() == 1)
			c.setLoyalty(Integer.parseInt(card_Element.getElementsByTagName(Xml.XmlTag_Loyalty).item(0).getTextContent()));
		
		// Spell effect (instants and sorceries only) ***************************************************************************
		if (card_Element.getElementsByTagName(Xml.XmlTag_Effect).getLength() == 1)
			c.setSpellEffect(SpellAbilityFactory.create(card_Element.getElementsByTagName(Xml.XmlTag_Effect).item(0).getTextContent(), c));
		
		// Abilities if any *****************************************************************************************************
		if (card_Element.getElementsByTagName(Xml.XmlTag_Abilities).getLength() == 1)
		{
			String abilityTXT = null;
			Element ability_Element = null;
			Xml.AbilityType abilityType = null;
			String parameter = null;
			Cost abilityCost = null;
			
			Element abilities_Element = (Element) card_Element.getElementsByTagName(Xml.XmlTag_Abilities).item(0);
			NodeList abilities_NodeList = abilities_Element.getElementsByTagName(Xml.XmlTag_Ability);
			for (int j = 0; j < abilities_NodeList.getLength(); j++)
			{
				ability_Element = (Element) abilities_Element.getElementsByTagName(Xml.XmlTag_Ability).item(j);
				// Look for ability attibute named "type"
				if (ability_Element.getAttribute("type").equals("")) {
					System.out.println("Loading card [" + cardName + "] error : an ability has no type attribute.");
					return null;
				}
				
				try {
					abilityType = Xml.AbilityType.valueOf(ability_Element.getAttribute("type"));
					String strCost = ability_Element.getAttribute("cost");
					
					if (abilityType != Xml.AbilityType.ACTIVATED) {
						if (strCost.equals(""))
							abilityCost = null;
						else {
							abilityCost = new Cost(new ManaCost(strCost));
						}
					}
					parameter = ability_Element.getAttribute("parameter");
					if (parameter.equals(""))
						parameter = null;
				} catch (IllegalArgumentException e) {
					System.out.println("Loading card [" + cardName + "] error : " + ability_Element.getAttribute("type") + " : no such type."); // should never get here
					return null;
				}
				abilityTXT = abilities_Element.getElementsByTagName(Xml.XmlTag_Ability).item(j).getTextContent();
				
				switch (abilityType) {
				case STATIC:
					if ((!ability_Element.getAttribute("subtype1").equals("")) && (!ability_Element.getAttribute("subtype2").equals(""))) {
						String attribute = ability_Element.getAttribute("subtype1");
						Subtype subtype1 = Subtype.valueOf(attribute.toUpperCase());
						Subtype subtype2 = Subtype.valueOf(ability_Element.getAttribute("subtype2").toUpperCase());
						c.addStaticAbility(StaticAbilityFactory.create(abilityTXT, c, subtype1, subtype2));
					}
					else {
						c.addStaticAbility(StaticAbilityFactory.create(abilityTXT, c, abilityCost, parameter));
					}
					break;
				
				case ACTIVATED:
					c.addActivatedAbility(ActivatedAbilityFactory.create(abilityTXT, c, parameter));
					break;
					
				case CONTINUOUS:
					c.addContinuousEffect(ContinuousEffectFactory.create(abilityTXT, c));
					break;
					
				case EVERGREEN:
					try {
						c.setEvergreen(Evergreen.valueOf(abilityTXT));
					} catch (IllegalArgumentException e) {
						System.out.println("Loading card [" + cardName + "] error : " + abilityTXT + " : no such Evergreen."); // should never get here
						return null;
					}
					break;
					
				case TRIGGERED:
					c.addTriggeredAbility(TriggeredAbilityFactory.create(abilityTXT, c, abilityCost, parameter));
					break;
					
				default:
					System.out.println("Loading card [" + cardName + "] error : " + abilityTXT + " : no such type."); // should never get here
					return null;
				}
			}
		}

		// Add the ability to produce mana granted from basic land subtypes
		if ((c != null) && c.isLandCard()) {
			if (c.hasSubtypePrinted(Subtype.PLAINS))
				c.addActivatedAbility(ActivatedAbilityFactory.create("plains", c));				
			if (c.hasSubtypePrinted(Subtype.ISLAND))
				c.addActivatedAbility(ActivatedAbilityFactory.create("island", c));
			if (c.hasSubtypePrinted(Subtype.SWAMP))
				c.addActivatedAbility(ActivatedAbilityFactory.create("swamp", c));
			if (c.hasSubtypePrinted(Subtype.MOUNTAIN))
				c.addActivatedAbility(ActivatedAbilityFactory.create("mountain", c));
			if (c.hasSubtypePrinted(Subtype.FOREST))
				c.addActivatedAbility(ActivatedAbilityFactory.create("forest", c));
		}
		
		return c;
	}
	
	/**
	 * Returns a vector with all cards loaded from the Card Definition
	 * @return
	 */
	public static Vector<Card> extractCardsFromDefinition() {
		Vector<Card> cards = new Vector<Card>();
		SortedSet<String> keys = new TreeSet<String>(_cardDefinitions.keySet());
		for (String cardname : keys) {
			System.out.println("card name : " + cardname);
			cards.add(CardFactory.create(cardname));
		}
		return cards;
	}
};
