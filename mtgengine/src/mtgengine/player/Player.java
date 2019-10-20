package mtgengine.player;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mtgengine.CounterType;
import mtgengine.EffectCallback;
import mtgengine.Game;
import mtgengine.MtgObject;
import mtgengine.StackObject;
import mtgengine.Game.PermanentCategory;
import mtgengine.Game.Response;
import mtgengine.Game.State;
import mtgengine.ability.Emblem;
import mtgengine.ability.Evergreen;
import mtgengine.ability.StaticAbility;
import mtgengine.card.Card;
import mtgengine.card.UntapOptional;
import mtgengine.damage.DamageSource;
import mtgengine.damage.Damageable;
import mtgengine.effect.ContinuousEffect;
import mtgengine.mana.Mana.ManaType;
import mtgengine.mana.ManaPool;
import mtgengine.modifier.Modifier;
import mtgengine.modifier.SpellCastingModifier;
import mtgengine.type.Supertype;
import mtgengine.type.CardType;
import mtgengine.type.Subtype;
import mtgengine.zone.Command;
import mtgengine.zone.Exile;
import mtgengine.zone.Graveyard;
import mtgengine.zone.Hand;
import mtgengine.zone.Library;

public class Player extends MtgObject implements Damageable {
	public final static int KEEP_HAND_UNKNOWN = 0;
	public final static int KEEP_HAND_YES = 1;
	public final static int KEEP_HAND_NO = -1;
	
	private Game _g;
	
	// player zones
	private Library _library;
	private Hand _hand;
	private Graveyard _graveyard;
	private Exile _exile;
	private Command _command;
	
	// player data
	private HashMap<CounterType, Integer> _counters;
	private int _life = 20;
	private int _damagePrevention = 0;
	private int _turn = 0;
	private int _nbLandsPlayed = 0;
	private Player _opponent;
	private State _state;
	private int _keepHand = KEEP_HAND_UNKNOWN;
	private int _nbSpellsCastThisTurn;
	private int _nbSpellsCastLastTurn;
	private int _nbCreatureSpellsCastThisTurn;
	private int _nbPlaneswalkersDeployed = 0;
	private boolean _bDealtDamageThisTurn;
	private boolean _bLostLifeThisTurn;
	private boolean _bRevolt = false;
	
	// mana pool
	private ManaPool _manaPool;
	
	private ArrayList<EffectCallback> _effectCallbacks = new ArrayList<EffectCallback>();   // effect callbacks are delayed triggered (or pseudo-triggered)
	                                                                                        // effects that must be fired when certain event occurs (i.e. when
	                                                                                        // a player becomes the Monarch)
	
	/**
	 * Default constructor
	 * @param g
	 * @param name
	 */
	public Player(Game g, String name, String deck) {
		super(name);
		_g = g;
		_library = new Library(g, this);
		_hand = new Hand(g, this);
		_graveyard = new Graveyard(g, this);
		_exile = new Exile(g, this);
		_command = new Command(g, this);
		_counters = new HashMap<CounterType, Integer>();
		_counters.put(CounterType.POISON, 0);
		_counters.put(CounterType.ENERGY, 0);
		
		_manaPool = new ManaPool();
		
		loadDeck(deck);
	}
	
	public void addMana(ManaType type, int number) {
		_manaPool.add(type, number);
	}
	
	public void emptyManaPool() {
		_manaPool.empty();
	}
	
	public int getLife() {
		return _life;
	}
	
	public int getNbCounters(CounterType t) {
		return _counters.get(t);
	}
	
	@Override
	public String getSystemName() {
		return _name + "#" + _id;
	}
	
	private void loadDeck(String filename) {
		Path file = new File("Decks/" + filename).toPath();
		try {
			InputStream is = Files.newInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			String line = null;
			while ((line = br.readLine()) != null) {
				 Pattern p = Pattern.compile("^([0-9]*)\\s*(.*)$");
				 if (line.trim().isEmpty() || line.trim().startsWith("--"))
					 continue;
				 Matcher m = p.matcher(line);
				 if (m.matches()) {
					 String nb = m.group(1);
					 if (nb.equals(""))
						 nb = "1";
					 for (int i = 0; i < Integer.parseInt(nb); i++) {
						 _library.add(m.group(2));
					 }
				 }
			}
		} catch (NoSuchFileException e) {
			System.err.println("Could not open file : " + filename + " : file does not exist.");
			System.exit(1);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		_hand.add();
	}
	
	public Response shuffle() {
		_library.shuffle();
		return Response.OK;
	}
	
	public Response discardAllCardsInHand() {
		while (_hand.size() > 0)
			_g.discard((Card)_hand.getObjectAt(0));
		return Response.OK;
	}
	
	public MtgObject findObjectById(int idObject) {
		MtgObject ret = null;
		if ((ret = _hand.getObjectByID(idObject)) != null)
			return ret;
		if ((ret = _graveyard.getObjectByID(idObject)) != null)
			return ret;
		if ((ret = _library.getObjectByID(idObject)) != null)
			return ret;
		if ((ret = _exile.getObjectByID(idObject)) != null)
			return ret;
		if ((ret = _command.getObjectByID(idObject)) != null)
			return ret;
		return null;
	}
	
	public Vector<MtgObject> getLibraryContent() {
		if (!_library.isSearchable())
			return null;
		Vector<MtgObject> content = new Vector<MtgObject>();
		content.addAll(_library.getObjects());
		return content;
	}
	
	public int getHandSize() {
		return _hand.size();
	}

	public Graveyard getGraveyard() {
		return _graveyard;
	}
	
	public Exile getExile() {
		return _exile;
	}

	public Hand getHand() {
		return _hand;
	}
	
	public Command getCommand() {
		return _command;
	}
	
	public Library getLibrary() {
		return _library;
	}

	public boolean isLibrarySearchable() {
		return _library.isSearchable();
	}

	public void setLibrarySearchable(boolean value) {
		_library.setSearchable(value);
		
	}

	public void setNbLandsPlayed(int i) {
		_nbLandsPlayed = i;
	}

	public int getNbLandsPlayed() {
		return _nbLandsPlayed;
	}
	
	public void setOpponent(Player opponent) {
		_opponent = opponent;
	}
	
	public Player getOpponent() {
		return _opponent;
	}

	public Vector<String> dumpPermanents(PermanentCategory cat) {
		Vector<String> ret = new Vector<String>();
		String label;
		Card card;
		
		for (MtgObject obj : _g.getBattlefield().getObjectsControlledBy(_id))
		{
			card = (Card) obj;
			label = null;

			if (card.hasHost())
				continue;
			
			if (card.isLand(_g) && (cat == PermanentCategory.Lands) && !card.isCreature(_g))
			{
				label = card.getSystemName();
			}
			else if (card.isCreature(_g) && (cat == PermanentCategory.Creatures))
			{
				label = card.getSystemName() + "[" + card.getPower(_g) + "/" + card.getToughness(_g) + "]";
			}
			else if (!card.isLand(_g) && !card.isCreature(_g) && (cat == PermanentCategory.Other))
			{
				label = card.getSystemName();
			}
			else
			{
				continue;
			}				
			
			if (label != null)
			{
				if (card.isTapped())
					label += "^";
				ret.add(0, label);
			}
		}
		return ret;
	}
	
	public Vector<String> dumpPermanentsAttached(PermanentCategory cat) {
		Vector<String> ret = new Vector<String>();
		String label;
		Card card;
		
		for (MtgObject obj : _g.getBattlefield().getObjectsControlledBy(_id))
		{
			card = (Card) obj;
			label = null;

			if (!card.hasHost())
				continue;
			
			if (!card.isLand(_g) && !card.isCreature(_g) && (cat == PermanentCategory.Other))
			{
				label = card.getSystemName() + "->" + card.getHost().getID();
			}
			else
			{
				continue;
			}				
			
			if (label != null)
			{
				if (card.isTapped())
					label += "^";
				ret.add(0, label);
			}
		}
		return ret;
	}

	public void setState(State state) {
		_state = state;
		if (_state != State.WaitingForOpponent)
			_opponent.setState(State.WaitingForOpponent);
	}
	
	public State getState() {
		return _state;
	}

	/**
	 * Checks if this player controls a Planeswalker (for attack declaration purposes)
	 * @return true or false according to yes or no he controls a Planeswalker
	 */
	public boolean controlsAPlaneswalker() {
		for (Card permanent : _g.getBattlefield().getObjectsControlledBy(_id)) {
			if (permanent.isPlaneswalkerCard())
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if this player controls an Artifact
	 * @return true or false according to yes or no he controls an Artifact
	 */
	public boolean controlsAnArtifact() {
		for (Card permanent : _g.getBattlefield().getObjectsControlledBy(_id)) {
			if (permanent.isArtifactCard())
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if this player controls a basic land of a certain basic land subtype
	 * @return true or false
	 */
	public boolean controlsABasicLandType(Subtype basicLandSubtype) {
		for (Card permanent : _g.getBattlefield().getObjectsControlledBy(_id)) {
			if (permanent.hasSubtypeGlobal(_g, basicLandSubtype))
				return true;
		}
		return false;
	}

	public int getTurn() {
		return _turn;
	}

	public int getNbPlaneswalkerDeployedThisTurn() {
		return _nbPlaneswalkersDeployed;
	}
	
	private Response untapPermanents() {
		Vector<Card> permanents = _g.getBattlefield().getPermanentsControlledBy(this);
		boolean bUntap;
		
		for (Card permanent : permanents) {
			bUntap = false;
			
			// Permanent is tapped
			if (permanent.isTapped())
			{
				// Untapping this permanent is optional
				if (permanent.isUntapOptional()) {
					
					// The choice has not yet been made 
					if (!permanent.isUntapOptionalAnswered()) {
						_g.promptUntap(permanent);
						return Response.MoreStep;
					}
					else { // The choice has been made
						if (permanent.getUntapOptionalChoice() == UntapOptional.State.Untap)
							bUntap = true;
					}
				}
				else if (!permanent.hasEvergreenGlobal(Evergreen.NOUNTAP, _g)) // Permanent untaps normally
					bUntap = true;	
				
				// Check for effects preventing it from untapping
				Vector<ContinuousEffect> continuousEffects = _g.getContinuousEffects();
				for (ContinuousEffect ce : continuousEffects) {
					if (!ce.doesUntapDuringUntapStep(_g, permanent)) {
						bUntap = false;
						break;
					}
				}

				// If all is OK, untap the permanent
				if (bUntap)
					permanent.untap(_g);
			}
		}
		return Response.OK;
	}
	
	public Response newTurn() {
		Response ret = Response.OK;
		_nbLandsPlayed = 0;
		_nbSpellsCastLastTurn = _nbSpellsCastThisTurn;
		_nbSpellsCastThisTurn = 0;
		_nbCreatureSpellsCastThisTurn = 0;
		_nbPlaneswalkersDeployed = 0;
		_bDealtDamageThisTurn = false;
		_bLostLifeThisTurn = false;
		_bRevolt = false;
		
		for (Card permanent : _g.getBattlefield().getPermanentsControlledBy(this))
			permanent.newTurn(_g);
		
		if (this == _g.getActivePlayer())
			ret = untapPermanents();
		return ret;
	}

	public void incrementTurn() {
		_turn ++;
	}
	
	public void setTurn(int i) {
		_turn = i;
	}

	public String getName() {
		return _name;
	}
	
	public void takeMulligan() {
		int handSize = getHandSize();
		
		if (handSize == 0) {
			_keepHand = KEEP_HAND_YES;
			return;
		}
			
		Card card;
		int i = handSize;
		while (i > 0) {
			card = (Card) _hand.getObjectAt(0);
			_g.move_HND_to_TOPLIB(card);
			i--;
		}
		shuffle();
		if (handSize > 1) {
			_g.drawCards(this, handSize - 1);
			_keepHand = KEEP_HAND_UNKNOWN;
		}
		else
			_keepHand = KEEP_HAND_YES;
	}
	
	public void setKeepHand(boolean value) {
		_keepHand = (value ? KEEP_HAND_YES : KEEP_HAND_NO);
	}
	
	public int getKeepHand() {
		return _keepHand;
	}

	public void gainLife(int lifeGained) {
		_life += lifeGained;
		_g.queueGainLifeEffects(this, lifeGained);
	}

	public void addCounter(CounterType t, int number) {
		// check for effects that modify the number of counters received
		Vector<ContinuousEffect> effects = _g.getContinuousEffects();
		for (ContinuousEffect effect : effects)
			number = effect.modifyNbCounters(_g, this, t, number);
		
		// Add the counters
		if (number > 0)
			_counters.put(t, _counters.get(t) + number);
	}
	
	@Override
	public boolean isUndamageable(Game g) {
		// If the player is Undamageable (i.e. under Solitary Confinement), do nothing.
		// Look for continuous effects that grant Undamageability to the player
		Vector<ContinuousEffect> effects = g.getContinuousEffects();
		for (ContinuousEffect ce : effects) {
			if (ce.grantsEvergreen(Evergreen.UNDAMAGEABLE, this, g))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns the amount of damage dealt after computing prevention
	 */
	public int isDealtDamage(DamageSource source, int damageDealt, Game g) {
		// subtract damage preventeed
		int actualDamage = Math.max(damageDealt - _damagePrevention, 0);

		// subtract the amount of damage to the current prevention
		_damagePrevention = Math.max(_damagePrevention - damageDealt, 0);
		
		// check if the source of the damage has infect and if so, put poison counters on the player
		if ((source instanceof Card) && (((Card) source).hasEvergreenGlobal(Evergreen.INFECT, _g)))
			addCounter(CounterType.POISON, actualDamage);
		else {
			int lifeToLose = actualDamage;

			// Check for effects that change the amount of life that should be lost (i.e. Worship)
			Vector<ContinuousEffect> effects = _g.getContinuousEffects();
			for (ContinuousEffect ce : effects) {
				if (ce.doesWorshipEffect(_g, this) && (lifeToLose >= _life) )
					lifeToLose = _life - 1;
			}
			this.loseLife(lifeToLose); // lose life
		}
		_bDealtDamageThisTurn = true;
		return actualDamage;
	}
	
	public boolean wasDealtDamageThisTurn() {
		return _bDealtDamageThisTurn;
	}
	
	public void setRevolt() {
		_bRevolt = true;
	}
	
	public void loseLife(int lifeLost) {
		// if the amount is 0 or less, do nothing
		if (lifeLost <= 0)
			return;
		
		_life -= lifeLost;
		_bLostLifeThisTurn = true;
	}
	
	public boolean hasLostLifeThisTurn() {
		return _bLostLifeThisTurn;
	}
	
	public void addEmblem(Emblem e) {
		_command.addObject(e);
	}

	@Override
	public boolean isTargetableBy(Game g, StackObject so) {
		// Look for continuous effects that grant Shroud to the player
		Vector<ContinuousEffect> effects = g.getContinuousEffects();
		for (ContinuousEffect ce : effects) {
			if (ce.grantsEvergreen(Evergreen.SHROUD, this, g))
				return false;
		}
		return true;
	}
	
	public void addDamagePrevention(int nb) {
		_damagePrevention += nb;
	}

	public void incrementNbCreatureSpellsCastThisTurn() {
		_nbCreatureSpellsCastThisTurn++;
	}

	public int getNbCreatureSpellsCastThisTurn() {
		return _nbCreatureSpellsCastThisTurn;
	}
	
	public void incrementNbSpellsCastThisTurn() {
		_nbSpellsCastThisTurn++;
	}
	
	public int getNbSpellsCastThisTurn() {
		return _nbSpellsCastThisTurn;
	}
	
	public int getNbSpellsCastLastTurn() {
		return _nbSpellsCastLastTurn;
	}

	/**
	 * Returns the number of lands controlled by this player.
	 * @param g
	 * @return
	 */
	public int getNbLandsControlled() {
		return _g.getBattlefield().getLandsControlledBy(this).size();
	}
	

	/**
	 * This method returns the number of basic land types amont lands a player controls.
	 * Used by Ordered Migration for example
	 * @param p
	 * @return
	 */
	public int getDomainCount() {
		int domainCount = 0;
		Vector<Card> lands;
		boolean bPlains = false;
		boolean bIsland = false;
		boolean bSwamp = false;
		boolean bMountain = false;
		boolean bForest = false;
		
		lands = _g.getBattlefield().getLandsControlledBy(this);
		for (Card land : lands)
		{
			if (land.hasSubtypeGlobal(_g, Subtype.PLAINS)) bPlains = true;
			if (land.hasSubtypeGlobal(_g, Subtype.ISLAND)) bIsland = true;
			if (land.hasSubtypeGlobal(_g, Subtype.SWAMP)) bSwamp = true;
			if (land.hasSubtypeGlobal(_g, Subtype.MOUNTAIN)) bMountain = true;
			if (land.hasSubtypeGlobal(_g, Subtype.FOREST)) bForest = true;
		}
		if (bPlains) domainCount++;
		if (bIsland) domainCount++;
		if (bSwamp) domainCount++;
		if (bMountain) domainCount++;
		if (bForest) domainCount++;
		return domainCount;
	}

	/**
	 * Returns true if the player is under the continuous effect of Yawgmoth's Will
	 * allowing him to play cards from his graveyard.
	 * @return
	 */
	public boolean isUnderYawgmothsWill() {
		for (ContinuousEffect ce : _g.getContinuousEffects()) {
			if (ce.canPlayCardFromGraveyard(_g, this))
				return true;
		}
		return false;
	}

	
	/**
	 * Returns the number of creatures controlled by this player.
	 * @param g
	 * @return
	 */
	public int getNbCreaturesControlled() {
		return _g.getBattlefield().getCreaturesControlledBy(this).size();
	}

	/**
	 * Returns the number of enchantments controlled by this player.
	 * @param g
	 * @return
	 */
	public int getNbEnchantmentsControlled() {
		return _g.getBattlefield().getEnchantmentsControlledBy(this).size();
	}

	/**
	 * Returns the number of artifacts controlled by this player.
	 * @param g
	 * @return
	 */
	public int getNbArtifactsControlled() {
		return _g.getBattlefield().getArtifactsControlledBy(this).size();
	}
	
	/**
	 * Returns the number of basic lands controlled by this player.
	 * @return
	 */
	public int getNbBasicLandsControlled() {
		int nbBasics = 0;
		Vector<Card> lands = _g.getBattlefield().getLandsControlledBy(this);
		for (Card land : lands) {
			if (land.hasSupertype(Supertype.BASIC))
				nbBasics++;
		}
		return nbBasics;
	}

	/**
	 * Returns the number of lands controlled by this player, excluding the one in the parameter.
	 * @param card The land card that must not be counted.
	 * @return
	 */
	public int getNbOtherLands(Card card) {
		int nbLands = 0;
		
		Vector<Card> lands = _g.getBattlefield().getLandsControlledBy(this);
		for (Card land : lands) {
			if (land != card)
				nbLands++;
		}
		return nbLands;
	}

	
	public boolean canCastSpell(Card spell) {
		// Check modifiers on the Player himself
		for (Modifier mod : _modifiers) {
			if (mod instanceof SpellCastingModifier) {
				SpellCastingModifier scMod = (SpellCastingModifier) mod;
				
				if (scMod.isForbidden(spell))
					return false;
			}
		}
		
		// Check modifiers coming from Continuous effects
		Vector<ContinuousEffect> effects = _g.getContinuousEffects();
		for (ContinuousEffect effect : effects) {
			if (effect.isSpellForbidden(this, spell, _g)) {
				return false;
			}
		}
		
		// Check static abilities on the card
		Vector<StaticAbility> abilities = spell.getStaticAbilities();
		for (StaticAbility sa : abilities) {
			if (sa.preventCastSpell(_g, spell))
				return false;
		}
		return true;
	}

	public void endUntilYNTEffects(Player activePlayer) {
		Iterator<Modifier> it = _modifiers.iterator();
		while (it.hasNext()) {
			Modifier mod = (Modifier)it.next();
			if ((mod.getDuration() == Modifier.Duration.UNTIL_YOUR_NEXT_TURN) && (mod.getSource().getController(_g) == activePlayer))
				it.remove();
		}
	}
	
	@Override
	public String toString() {
		return _name;
	}

	public void deployedPlaneswalker() {
		_nbPlaneswalkersDeployed++;
	}

	public int getNbPermanentsControlled() {
		return _g.getBattlefield().getPermanentsControlledBy(this).size();
	}
	
	public ArrayList<EffectCallback> getEffectCallbacks() {
		return _effectCallbacks;
	}

	public void addEffectCallback(String methodName, Object parameter) {
		_effectCallbacks.add(new EffectCallback(methodName, parameter));
	}
	
	public void removeEffectCallback(EffectCallback ecb) {
		_effectCallbacks.remove(ecb);
	}

	public void payEnergy(int number) {
		_counters.put(CounterType.ENERGY, _counters.get(CounterType.ENERGY) - number);
	}

	public ManaPool getManaPool() {
		return _manaPool;
	}
	
	/**
	 * 
	 * @param p
	 * @return
	 */
	public String dumpStatus() {
		int bActivePlayer = (this == _g._activePlayer) ? 1 : 0;
		int bMonarch = (_g.getTheMonarch() == this) ? 1 : 0;
		
		String status = String.format("%d|%d|%d|%d|%d|%d|%d|%d|%s",
				_id,           						// 0. ID player
				bActivePlayer, 						// 1. Active / Non active (1 or 0)
				_life,         						// 2. Life total
				_counters.get(CounterType.POISON),	// 3. Poison counters
				_counters.get(CounterType.ENERGY),  // 4. Energy counters
				bMonarch,							// 5. Monarch (1 or 0)
				_library.size(),					// 6. Library size
				_hand.size(),					// 7. Hand size
				_manaPool
				);
		return status;
	}
	
	public boolean controls2LegendaryPermanentsWithSameName() {
		boolean ret = false;
		
		// look for legendary permanents
		Vector<Card> allPermanents = _g.getBattlefield().getPermanentsControlledBy(this);
		Vector<String> legPermanents = new Vector<>();
		for (Card permanent : allPermanents) {
			if (permanent.hasSupertype(Supertype.LEGENDARY)) {
				if (legPermanents.contains(permanent.getName()))
					return true;
				else
					legPermanents.add(permanent.getName());
			}
		}
		return ret;
	}
	
	/**
	 * Returns true if the player has the buff, false otherwise.
	 * @param buff
	 * @return
	 */
	public boolean hasBuff(PlayerBuff buff) {
		switch(buff) {
		
		// Delirium
		case Delirium:
			return (_graveyard.getNbCardtypes() >= 4);

		// Revolt
		case Revolt:
			return _bRevolt;

		// Metalcraft
		case Metalcraft:
			return (_g.getBattlefield().getArtifactsControlledBy(this).size() >= 3);
		
		// Spell Mastery
		case SpellMastery:
			int nbInstantSorceries = 0;
			for (Card c : _graveyard.getCards()) {
				if (c.isSorceryCard() || c.isInstantCard())
					nbInstantSorceries++;
			}
			return (nbInstantSorceries >= 2);
			
		// Threshold
		case Threshold:
			return (_graveyard.size() >= 7);
			
		default:
			break;
		
		}
		return false;
	}

	public int getNbPermanentsControlledOfType(CardType ct) {
		return _g.getBattlefield().getNumberOfControlledBy(ct, this);
	}
	
	public int getNbPermanentsControlledOfSubtype(Subtype st) {
		return _g.getBattlefield().getNumberOfControlledBy(st, this);
	}
}
