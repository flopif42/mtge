package mtgengine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

import mtgengine.ability.ActivatedAbility;
import mtgengine.ability.Evergreen;
import mtgengine.ability.Protection;
import mtgengine.ability.StaticAbility;
import mtgengine.ability.TriggeredAbility;
import mtgengine.card.Card;
import mtgengine.card.Color;
import mtgengine.cost.ManaCost;
import mtgengine.effect.ContinuousEffect;
import mtgengine.type.CardType;
import mtgengine.type.CreatureType;
import mtgengine.type.Subtype;
import mtgengine.type.Supertype;

public class Xml {
	public static String CARD_DEFINITION_FILE = "cards.xml";
	public static String TOKEN_DEFINITION_FILE = "tokens.xml";
	public static String OUTPUT_FILE = "output.xml";
	public enum AbilityType { ACTIVATED, TRIGGERED, CONTINUOUS, EVERGREEN, STATIC };
	
	// XML tags
	// Meta : card and cards
	public static String XmlTag_Cards = "Cards";
	public static String XmlTag_Card = "Card";
	
	// Card name
	public static String XmlTag_Cardname = "Name";
	
	// Mana cost and converted mana cost
	public static String XmlTag_ManaCost = "ManaCost";
	
	// PT, power and toughness
	public static String XmlTag_PT = "PT";
	
	// Loyalty
	public static String XmlTag_Loyalty = "Loyalty";
	
	// Card types, subtypes and creature types
	public static String XmlTag_CardTypes = "Types";
	public static String XmlTag_CardType = "Type";
	public static String XmlTag_Subtypes = "Subtypes";
	public static String XmlTag_Subtype = "Subtype";
	public static String XmlTag_Supertypes = "Supertypes";
	public static String XmlTag_Supertype = "Supertype";
	public static String XmlTag_CreatureTypes = "CTypes";
	public static String XmlTag_CreatureType = "CType";
	
	// Abilities
	public static String XmlTag_Abilities = "Abilities";
	public static String XmlTag_Ability = "Ability";
	public static String XmlAttribute_Evergreen = AbilityType.EVERGREEN.toString();
	public static String XmlAttribute_StaticAbility = AbilityType.STATIC.toString();
	public static String XmlAttribute_ActivatedAbility = AbilityType.ACTIVATED.toString();
	public static String XmlAttribute_TriggeredAbility = AbilityType.TRIGGERED.toString();
	public static String XmlAttribute_ContinousEffect = AbilityType.CONTINUOUS.toString();
	
	// Effect
	public static String XmlTag_Effect = "Fx";
	
	// Protections
	public static String XmlTag_Protections = "Prots";
	public static String XmlTag_Protection = "Prot";
	
	// Token related stuff
	public static String XmlTag_Token = "Token";
	public static String XmlTag_TokenName = "Name";
	// Color (used only for tokens and color indicators)
	public static String XmlTag_Colors = "Colors";
	public static String XmlTag_Color = "Color";

	// Double faced cards
	public static String XmlTag_DayFace = "DayFace";
	public static String XmlTag_NightFace = "NightFace";
	
	/**
	 * Extract the specified vector of cards to an output file
	 * @param vector
	 * @param ouputFilename
	 */
	public static void dumpXml(Vector<Card> vector, String ouputFilename) {
		try {
			File f = new File(ouputFilename);
			if (!f.exists())
				f.createNewFile();
			
			FileOutputStream fos = new FileOutputStream(f);
			OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
			BufferedWriter bw = new BufferedWriter(osw);

			bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
			bw.write("<?xml-stylesheet type=\"text/xsl\" href=\"cards.xsl\"?>");
			bw.write("<"+XmlTag_Cards+">\r\n");

			for (MtgObject obj : vector)
				bw.write(Xml.toString((Card) obj) + "\r\n");
			
			bw.write("</"+XmlTag_Cards+">\r\n");
			bw.close();
		} catch (FileNotFoundException e1) {
			System.err.println("File not found.");
		} catch (IOException e) {
			System.err.println("Could not create file");
		}
	}
	
	/**
	 * Helper function that will dump the specified element between two tags of the specified tagname.
	 * @param element
	 * @param tagname
	 * @return
	 */
	private static String xmlTag(String element, String tagname) {
		return "<" + tagname + ">" + element + "</" + tagname + ">";
	}
	
	/**
	 * Returns the XML form of a card
	 * @param card
	 * @return
	 */
	public static String toString(Card card) {
		// opening tag
		String xml = "<"+XmlTag_Card+" id=\"" + card.getImageID() + "\">";

		// Card name
		String cardname = card.getName();
		if (cardname == null)
			cardname = Card.COLORLESS_MORPH_22;
		xml += xmlTag(cardname, XmlTag_Cardname);
		
		// Double faced cards
		if (card.hasNightFace())
			xml += xmlTag(card.getNightFaceCard(), XmlTag_NightFace);
		if (card.hasDayFace())
			xml += xmlTag(card.getDayFaceCard(), XmlTag_DayFace);
		
		// Mana cost
		ManaCost mc = card.getManaCost();
		if (mc != null) {
			String manaCost = card.getManaCost().toString();
			xml += xmlTag(manaCost, XmlTag_ManaCost);	
		}
		else {
			if (card.isColored()) {
				xml += "<"+XmlTag_Colors+">";
				for (Color col : Color.values()) {
					if (card.hasColor(col))
						xml += xmlTag(col.name(), XmlTag_Color);
				}
				xml += "</"+XmlTag_Colors+">";
			}
		}
		
		// Card type(s) (artifact, creature, etc.)
		xml += "<"+XmlTag_CardTypes+">";
		for (CardType ct : card.getCardTypes())
			xml += xmlTag(ct.toString().toUpperCase(), XmlTag_CardType);
		xml += "</"+XmlTag_CardTypes+">";

		// Card subtype(s) (basic land types mostly)
		if (card.getSubtypes().size() > 0) {
			xml += "<"+XmlTag_Subtypes+">";
			for (Subtype st : card.getSubtypes())
				xml += xmlTag(st.toString().toUpperCase(), XmlTag_Subtype);
			xml += "</"+XmlTag_Subtypes+">";
		}
		
		// Card supertype(s) (Legendary, Snow, etc.)
		if (card.getSupertypes().size() > 0) {
			xml += "<"+XmlTag_Supertypes+">";
			for (Supertype st : card.getSupertypes())
				xml += xmlTag(st.toString().toUpperCase(), XmlTag_Supertype);
			xml += "</"+XmlTag_Supertypes+">";
		}
		
		// Creature types (Angel, Goblin, etc.)
		if (card.getCreatureTypes().size() > 0) {
			xml += "<"+XmlTag_CreatureTypes+">";
			for (CreatureType creatureType : card.getCreatureTypes())
				xml += xmlTag(creatureType.toString(), XmlTag_CreatureType);
			xml += "</"+XmlTag_CreatureTypes+">";
		}
		
		// Power and toughness
		if (card.isCreatureCard() || card.hasSubtypePrinted(Subtype.VEHICLE))
			xml += xmlTag(card.getPrintedPT(), XmlTag_PT);
		
		// Loyalty
		if (card.isPlaneswalkerCard())
			xml += xmlTag(Integer.toString(card.getPrintedLoyalty()), XmlTag_Loyalty);

		// Spell effect in case of instants and sorceries
		if (card.getSpellEffect() != null)
			xml += xmlTag(card.getSpellEffect().getAbilityName(), XmlTag_Effect);
		
		// Abilities (includes Triggered, Activated, Continuous effects and Evergreen)
		if (card.hasAbilities()) {
			xml += "<"+XmlTag_Abilities+">";
			// Evergreen abilities (flying, first strike, etc.)
			if (card.getPrintedEvergreen().size() > 0) {
				for (Evergreen ev : card.getPrintedEvergreen())
					xml += "<"+XmlTag_Ability+" type=\""+XmlAttribute_Evergreen+"\">" + ev.toString() + "</"+XmlTag_Ability+">";
			}
			// Continuous effects
			if (card.getPrintedContinuousEffects().size() > 0) {
				for (ContinuousEffect ce : card.getPrintedContinuousEffects())
					xml += "<"+XmlTag_Ability+" type=\""+XmlAttribute_ContinousEffect+"\">" + ce.getAbilityName() + "</"+XmlTag_Ability+">";
			}
			// Static abilities
			if (card.getPrintedStaticAbilities().size() > 0) {
				for (StaticAbility sa : card.getPrintedStaticAbilities()) {
					xml += "<"+XmlTag_Ability+" type=\""+XmlAttribute_StaticAbility+"\"";
					if (sa.getParameter() != null)
						xml += " parameter=\""+ sa.getParameter() +"\"";
					if (sa.getManaCost() != null)
						xml += " cost=\""+ sa.getManaCost() +"\"";
					if (sa.hasAssociatedSubyypes()) {
						xml += " subtype1=\""+ sa.getAssociatedBasicLandType(0) +"\"";	
						xml += " subtype2=\""+ sa.getAssociatedBasicLandType(1) +"\"";
					}
					xml += ">" + sa.getAbilityName() + "</"+XmlTag_Ability+">";					
				}
			}
			// Triggered abilities
			if (card.getPrintedTriggeredAbilities().size() > 0) {
				for (TriggeredAbility ta : card.getPrintedTriggeredAbilities()) {
					if (!ta.isSubAbility()) {
						xml += "<"+XmlTag_Ability+" type=\""+XmlAttribute_TriggeredAbility+"\"";
						if (ta.getManaCost() != null)
							xml += " cost=\""+ ta.getManaCost() +"\"";
						if (ta.getParameter() != null)
							xml += " parameter=\""+ ta.getParameter() +"\"";
						xml += ">" + ta.getAbilityName() + "</"+XmlTag_Ability+">";
					}
				}
			}
			// Activated abilities
			if (card.getPrintedActivatedAbilities().size() > 0) {
				for (ActivatedAbility aa : card.getPrintedActivatedAbilities()) {
					if (!aa.isImplied()) {
						xml += "<"+XmlTag_Ability+" type=\""+XmlAttribute_ActivatedAbility+"\"";
						if (aa.getParameter() != null)
							xml += " parameter=\""+ aa.getParameter() +"\"";
//						if (aa.getCost() != null)
//							xml += " cost=\""+ aa.getCost().toString() +"\"";
						xml += ">" + aa.getAbilityName() + "</"+XmlTag_Ability+">";
					}
				}
			}
			xml += "</"+XmlTag_Abilities+">";
		}
		
		// Protections
		if (card.hasProtections()) {
			xml += "<"+XmlTag_Protections+">";
			for (Object quality : card.getPrintedProtections()) {
				String protTXT = null;
				if (quality instanceof Color)
					protTXT = quality.toString().toUpperCase();
				else if (quality instanceof Protection)
					protTXT = ((Protection) quality).name();
				else {
					System.err.println("Invalid protection : " + quality.toString());
					System.exit(0);
				}
				xml += xmlTag(protTXT, XmlTag_Protection);
			}
			xml += "</"+XmlTag_Protections+">";
		}
		
		// Token related stuff
		if (card.isToken()) {
			xml += "<"+XmlTag_Token+">";
			xml += xmlTag(card.getDisplayName(), XmlTag_TokenName);
			xml += "</"+XmlTag_Token+">";
		}
		
		// closing tag
		xml += "</"+XmlTag_Card+">";
		return xml;
	}
}
