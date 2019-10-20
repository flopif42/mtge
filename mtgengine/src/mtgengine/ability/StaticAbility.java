package mtgengine.ability;

import java.lang.reflect.Method;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mtgengine.CounterType;
import mtgengine.Game;
import mtgengine.Game.Response;
import mtgengine.action.SpellCast.Option;
import mtgengine.card.Card;
import mtgengine.cost.Cost;
import mtgengine.cost.ManaCost;
import mtgengine.effect.StaticAbilityEffect;
import mtgengine.type.Subtype;

public class StaticAbility {
	
	public static final String MORPH = "morph"; 
	public static final String MEGAMORPH = "megamorph";
	public static final String SUSPEND = "suspend";
	public static final String DEVOID = "devoid";
	public static final String SPECTACLE = "spectacle";
	
	private int _step = 1;
	private Card _source;
	private String _abilityName;
	private String _description;
	private String _parameter;
	private Cost _cost;
	private Vector<Subtype> _associatedBasicLandTypes; // some static abilities come with a pair of 
	                                                   // associated basic land types (i.e. some kind of dual lands)
	
	public StaticAbility(String name, Card source, Cost cost, String parameter) {
		_abilityName = name;
		_source = source;
		_parameter = parameter; // for example for Dredge, it's the number of cards to mill
		_cost = cost; // some static abilities have cost like Kicker or Awaken
	}

	public void setDescription(String desc) {
		_description = desc;
	}
	
	public String getParameter() {
		return _parameter;
	}
	
	public ManaCost getManaCost() {
		if (_cost == null)
			return null;
		return _cost.getManaCost();
	}
	
	public String getAbilityName() {
		return _abilityName;
	}

	public String getDescription() {
		return _description;
	}
	
	public Card getSource() {
		return _source;
	}
	
	public void addAssociatedSubtype(Subtype st) {
		if (_associatedBasicLandTypes == null)
			_associatedBasicLandTypes = new Vector<Subtype>();
		if (!_associatedBasicLandTypes.contains(st))
			_associatedBasicLandTypes.add(st);
	}
	
	public Subtype getAssociatedBasicLandType(int i) {
		return _associatedBasicLandTypes.get(i);
	}
	
	public boolean hasAssociatedSubyypes() {
		return !(_associatedBasicLandTypes == null);
	}

	// Effects that set the base P/T of a creature (i.e. Saproling Burst tokens, Tarmogoyf, ...)
	public String computeBasePT(Game g) {
		int power;
		int toughness;
		
		// Tarmogoyf
		if(_parameter.equals("tarmogoyf")) {
			int nbTypes = g.getTarmoBonusCount();
			power = nbTypes;
			toughness = nbTypes +1;
		}
		
		// Master of Etherium
		else if (_parameter.equals("masterOfEtherium")) {
			int nbArtifacts = g.getBattlefield().getArtifactsControlledBy(_source.getController(g)).size();
			power = nbArtifacts;
			toughness = nbArtifacts;
		}
		
		// serraAvatar
		else if (_parameter.equals("serraAvatar")) {
			int lifeTotal = _source.getController(g).getLife();
			power = lifeTotal;
			toughness = lifeTotal;
		}

		/* treefolkSeedlings */
		else if (_parameter.equals("treefolkSeedlings")) {
			int nbForests = 0;
			Vector<Card> permanents = g.getBattlefield().getPermanentsControlledBy(_source.getController(g));
			for (Card permanent : permanents) {
				if (permanent.hasSubtypeGlobal(g, Subtype.FOREST))
					nbForests++;
			}
			power = 2;
			toughness = nbForests; 
		}
		
		// Should not get here
		else {
			power = 0;
			toughness = 0;
		}
		return String.format("%d/%d", power, toughness); 
	}
	
	
//	public PowerToughness getBasePT(Game g) {
//		if (_abilityName.equals("base_PT")) {
//
//			// Saproling Burst tokens
//			if (_parameter.equals("saprolingBurst")) {
//				Card token = _source;
//				Card burst = token.getSource();
//				int nbCounters = burst.getNbCountersOfType(g, CounterType.FADE);
//				return new PowerToughness(nbCounters + "/" + nbCounters);
//			}
//			
//			
//			
//			
//		}
//		return null;
//	}
	
	/**
	 * Returns true if the ability does something AS the permanent enters the battlefield. (pseudo trigger, i.e. Shocklands, Iona, etc)
	 * @return
	 */
	public boolean isEntersBattlefield() {
		return _description.startsWith("As " + _source.getName() + " enters the battlefield");
	}

	public boolean preventCastSpell(Game g, Card spell) {
		/* serraAvenger */
		if (_abilityName.equals("serraAvenger")) {
			if (spell.getController(g).getTurn() <= 3)
				return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param g
	 * @param ability
	 * @return
	 */
	public Response doEffect(Game g) {
		Response ret = Response.Error;
		Method m;
		
		try {
			m = StaticAbilityEffect.class.getMethod(_abilityName, Game.class, StaticAbility.class);
			ret = (Game.Response) m.invoke(null, g, this);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return ret;
	}
	
	public int getStep() {
		return _step;
	}
	
	public void advanceStep() {
		_step++;
	}

	/**
	 * Put counters on permanent that enter the battlefield with counters, not using the stack.
	 * @param g
	 * @param card
	 */
	public void putCounters(Game g, Card card) {
		String str = "^.*(" + _source.getName() + "|This permanent|it|This creature" + ") enters the battlefield with (X|[a-z]+|[0-9]+) (.*) counter[s]? on it(.*)$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(_description);
		
		if (m.matches()) {
			String nbCounters_str = m.group(2);
			int nbCounters = 0;
			
			if (nbCounters_str.equals("X"))
				nbCounters = _source.getXValue();
			else 
			{
				try {
					nbCounters = Integer.parseInt(nbCounters_str);
				} catch (NumberFormatException e) {
					if (nbCounters_str.equals("a"))
						nbCounters = 1;
					else if (nbCounters_str.equals("two"))
						nbCounters = 2;
					else if (nbCounters_str.equals("three"))
						nbCounters = 3;
					else if (nbCounters_str.equals("four"))
						nbCounters = 4;
					else if (nbCounters_str.equals("ten"))
						nbCounters = 10;
					else {
						System.err.println(m.group(1) + " : nb counters UNKNOWN : " + m.group(2));
						System.exit(1);
					}
				}
			}
			
			// Some cards have a condition that needs to be met in order for the counters to be put
			if (_abilityName.equals("kavuTitan_counters") && (_source.getSpellCastUsed().getOption() != Option.CAST_WITH_KICKER))
				return;
			
			card.addCounter(g, CounterType.parse(m.group(3)), nbCounters);
		}
		
		if (_abilityName.equals("saga"))
			card.addCounter(g, CounterType.LORE, 1);
	}
}
