package mtgengine.effect;

import java.util.Vector;

import mtgengine.ability.Emblem;
import mtgengine.ability.TriggeredAbility;
import mtgengine.ability.TriggeredAbilityFactory;
import mtgengine.ability.TriggeredAbility.Origin;
import mtgengine.card.Card;
import mtgengine.effect.ContinuousEffect.StopWhen;
import mtgengine.player.Player;

public class ContinuousEffectFactory {
	public static ContinuousEffect create(String name, ContinuousEffectSource source, Object additionalData) {
		ContinuousEffect continuousEffect = new ContinuousEffect(name, source);
		continuousEffect.setAdditionalData(additionalData);
		TriggeredAbility ta = null;
		
		/* Glorious Anthem */
		if (name.equals("gloriousAnthem"))
			continuousEffect.setDescription("Creatures you control get +1/+1.");

		/* loseAllAbilities */
		else if (name.equals(Effect.LOSE_ALL_ABILITIES))
			continuousEffect.setDescription("This permanent has lost all abilities.");
		
		/* pariah */
		else if (name.equals("pariah"))
			continuousEffect.setDescription("All damage that would be dealt to you is dealt to enchanted creature instead.");
		
		/* defensiveFormation */
		else if (name.equals("defensiveFormation"))
			continuousEffect.setDescription("Rather than the attacking player, you assign the combat damage of each creature attacking you. You can divide that creature's combat damage as you choose among any of the creatures blocking it. (Unimplemented)");
		
		/* darkestHour */
		else if (name.equals("darkestHour"))
			continuousEffect.setDescription("All creatures are black.");
		
		/* telepathy */
		else if (name.equals("telepathy"))
			continuousEffect.setDescription("Your opponents play with their hands revealed. <i>(Unimplemented)</i>");
		
		/* abundance */
		else if (name.equals("abundance"))
			continuousEffect.setDescription("If you would draw a card, you may instead choose land or nonland and reveal cards from the top of your library until you reveal a card of the chosen kind. Put that card into your hand and put all other cards revealed this way on the bottom of your library in any order.");
		
		/* absoluteGrace */
		else if (name.equals("absoluteGrace"))
			continuousEffect.setDescription("All creatures have protection from black.");
		
		/* absoluteLaw */
		else if (name.equals("absoluteLaw"))
			continuousEffect.setDescription("All creatures have protection from red.");
		
		/* yawgmothsWill_play */
		else if (name.equals("yawgmothsWill_play")) {
			continuousEffect.setDescription("You may play cards from your graveyard.");
			continuousEffect.setActiveZone(null);
			continuousEffect.setStop(StopWhen.END_OF_TURN);
		}
		
		/* yawgmothsWill_exile */
		else if (name.equals("yawgmothsWill_exile")) {
			continuousEffect.setDescription("If a card would be put into your graveyard from anywhere, exile that card instead.");
			continuousEffect.setActiveZone(null);
			continuousEffect.setStop(StopWhen.END_OF_TURN);
		}

		/* torrentialGearhulk_exile */
		else if (name.equals("torrentialGearhulk_exile")) {
			continuousEffect.setDescription("If " + ((Card) additionalData).getName() + " would be put into your graveyard this turn, exile it instead.");
			continuousEffect.setActiveZone(null);
			continuousEffect.setStop(StopWhen.END_OF_TURN);
		}
		
		/* exileUponDeath */
		else if (name.equals("exileUponDeath")) {
			continuousEffect.setDescription("If " + ((Card) additionalData).getName() + " would die this turn, exile it instead.");
			continuousEffect.setActiveZone(null);
			continuousEffect.setStop(StopWhen.END_OF_TURN);
		}

		/* lingeringMirage */
		else if (name.equals("lingeringMirage"))
			continuousEffect.setDescription("Enchanted land is an Island.");
		
		/* pacifism */
		else if (name.equals("pacifism"))
			continuousEffect.setDescription("Enchanted creature can't attack or block.");
		
		/* runeOfProtection_prevent */
		else if (name.equals("runeOfProtection_prevent")) {
			 // The additional data is the chosen source which damage will be prevented.
			continuousEffect.setDescription("The next time " + additionalData + " would deal damage to you this turn, prevent that damage.");
			continuousEffect.setActiveZone(null);
			continuousEffect.setStop(StopWhen.END_OF_TURN);
		}
		
		/* sanctumGuardian_prevent */
		else if (name.equals("sanctumGuardian_prevent")) {
			/*
			 * The additional data is a vector with 2 objects :
			 * object [0] is the target.
			 * object [1] is the chosen source which damage will be prevented.
			 */
			@SuppressWarnings("unchecked")
			Vector<Object> data = (Vector<Object>) additionalData;
			continuousEffect.setDescription("The next time " + data.get(1) + " would deal damage to " + data.get(0) + " this turn, prevent that damage.");
			continuousEffect.setActiveZone(null);
			continuousEffect.setStop(StopWhen.END_OF_TURN);
		}
		
		/* fog */
		else if (name.equals("fog")) {
			continuousEffect.setDescription("Prevent all combat damage that would be dealt this turn.");
			continuousEffect.setActiveZone(null);
			continuousEffect.setStop(StopWhen.END_OF_TURN);
		}
		
		/* falter_noBlock */
		else if (name.equals("falter_noBlock")) {
			continuousEffect.setDescription("Creatures without flying can't block.");
			continuousEffect.setActiveZone(null);
			continuousEffect.setStop(StopWhen.END_OF_TURN);
		}
		
		/* soulSculpted */
		else if (name.equals("soulSculpted"))
			continuousEffect.setDescription("This permanent is an enchantment and loses all abilities until a player casts a creature spell.");
		
		/* blanchwoodArmor */
		else if (name.equals("blanchwoodArmor"))
			continuousEffect.setDescription("Enchanted creature gets +1/+1 for each Forest you control.");
		
		/* werebear */
		else if (name.equals("werebear"))
			continuousEffect.setDescription("<i>Threshold</i> - Werebear gets +3/+3 as long as seven or more cards are in your graveyard.");
		
		/* deathsShadow */
		else if (name.equals("deathsShadow"))
			continuousEffect.setDescription("Death's Shadow gets -X/-X, where X is your life total.");
		
		/* despondency_pump */
		else if (name.equals("despondency_pump"))
			continuousEffect.setDescription("Enchanted creature gets -2/-0.");
		
		/* brilliantHalo_pump */
		else if (name.equals("brilliantHalo_pump"))
			continuousEffect.setDescription("Enchanted creature gets +1/+2.");
		
		/* bravado */
		else if (name.equals("bravado"))
			continuousEffect.setDescription("Enchanted creature gets +1/+1 for each other creature you control.");
		
		/* contamination_mana */
		else if (name.equals("contamination_mana"))
			continuousEffect.setDescription("If a land is tapped for mana, it produces Black instead of any other type and amount.");
		
		/* thaliaHereticCathar */
		else if (name.equals("thaliaHereticCathar"))
			continuousEffect.setDescription("Creatures and nonbasic lands your opponents control enter the battlefield tapped.");
		
		/* crosswinds */
		else if (name.equals("crosswinds"))
			continuousEffect.setDescription("Creatures with flying get -2/-0.");
		
		/* serrasEmbrace */
		else if (name.equals("serrasEmbrace"))
			continuousEffect.setDescription("Enchanted creature gets +2/+2 and has flying and vigilance.");
		
		/* zephidsEmbrace */
		else if (name.equals("zephidsEmbrace"))
			continuousEffect.setDescription("Enchanted creature gets +2/+2 and has flying and shroud. <i>(It can't be the target of spells or abilities.)</i>");
		
		/* vampiricEmbrace */
		else if (name.equals("vampiricEmbrace"))
			continuousEffect.setDescription("Enchanted creature gets +2/+2 and has flying.");
		
		/* shivsEmbrace */
		else if (name.equals("shivsEmbrace"))
			continuousEffect.setDescription("Enchanted creature gets +2/+2 and has flying.");
		
		/* sicken */
		else if (name.equals("sicken"))
			continuousEffect.setDescription("Enchanted creature gets -1/-1.");
		
		/* launch_boost */
		else if (name.equals("launch_boost"))
			continuousEffect.setDescription("Enchanted creature has flying.");

		/* reflexes */
		else if (name.equals("reflexes"))
			continuousEffect.setDescription("Enchanted creature has first strike. <i>(It deals combat damage before creatures without first strike.)</i>.");
		
		/* gaeasEmbrace_pump */
		else if (name.equals("gaeasEmbrace_pump"))
			continuousEffect.setDescription("Enchanted creature gets +3/+3 and has trample.");
		
		/* cloakOfMists */
		else if (name.equals("cloakOfMists"))
			continuousEffect.setDescription("Enchanted creature can't be blocked.");
		
		/* enchantPermanent */
		else if (name.equals("confiscate"))
			continuousEffect.setDescription("You control enchanted permanent.");
		
		/* wirecat */
		else if (name.equals("wirecat"))
			continuousEffect.setDescription("Wirecat can't attack or block if an enchantment is on the battlefield.");
		
		/* okk_noAttack */
		else if (name.equals("okk_noAttack"))
			continuousEffect.setDescription("Okk can't attack unless a creature with greater power also attacks.");
		/* okk_noBlock */
		else if (name.equals("okk_noBlock"))
			continuousEffect.setDescription("Okk can't block unless a creature with greater power also blocks.");

		/* veiledSerpent_cannotAttack */
		else if (name.equals("veiledSerpent_cannotAttack"))
			continuousEffect.setDescription("This creature can't attack unless defending player controls an Island.");
		
		/* phyrexianColossus_evasion */
		else if (name.equals("phyrexianColossus_evasion"))
			continuousEffect.setDescription("Phyrexian Colossus can't be blocked except by three or more creatures.");
		
		/* skipDrawStep */
		else if (name.equals("skipDrawStep"))
			continuousEffect.setDescription("Skip your draw step.");
		
		/* solitaryConfinement_shroud */
		else if (name.equals("solitaryConfinement_shroud"))
			continuousEffect.setDescription("You have shroud. <i>(You can't be the target of spells or abilities.)</i>");
		
		/* solitaryConfinement_undamageable */
		else if (name.equals("solitaryConfinement_undamageable"))
			continuousEffect.setDescription("Prevent all damage that would be dealt to you.");
		
		/* energyField_prevent */
		else if (name.equals("energyField_prevent"))
			continuousEffect.setDescription("Prevent all damage that would be dealt to you by sources you don't control.");
		
		/* worship */
		else if (name.equals("worship"))
			continuousEffect.setDescription("If you control a creature, damage that would reduce your life total to less than 1 reduces it to 1 instead.");
		
		/* sterlingGrove_shroud */
		else if (name.equals("sterlingGrove_shroud"))
			continuousEffect.setDescription("Other enchantments you control have shroud. <i>(They can't be the targets of spells or abilities.)</i>");
		
		/* fluctuator */
		else if (name.equals("fluctuator"))
			continuousEffect.setDescription("Cycling abilities you activate cost up to {2} less to activate.");
		
		/* endoskeleton_pump */
		else if (name.equals("endoskeleton_pump")) {
			Card target = (Card) additionalData;
			continuousEffect.setDescription(target.getName() + " gets +0/+3.");
		}
		
		/* maintainTapped */
		else if (name.equals("maintainTapped")) {
			String description, cardlist = "";
			Vector<Card> linkedCards;
			
			linkedCards = ((Card) source).getLinkedCards();
			if (linkedCards.size() == 1)
				description = linkedCards.get(0).getName() + " doesn't untap during its controller's untap step.";
			else {
				for (int i = 0; i < linkedCards.size(); i++) {
					cardlist += linkedCards.get(i).getName();
					if (i < (linkedCards.size()-1))
						cardlist += ", ";
				}
				description = cardlist + " don't untap during their controller's untap step.";
			}
			continuousEffect.setDescription(description);
		}
		
		/* backtoBasics */
		else if (name.equals("backtoBasics"))
			continuousEffect.setDescription("Nonbasic lands don't untap during their controllers' untap steps.");
		
		/* Bedlam */
		else if (name.equals("bedlam"))
			continuousEffect.setDescription("Creatures can't block.");
		
		/* exhaustion_noUntap */
		else if (name.equals("exhaustion_noUntap")) {
			Player target = (Player) additionalData;
			continuousEffect.setDescription("Creatures and lands " + target.getName() + " controls don't untap during his or her next untap step.");
			continuousEffect.setActiveZone(null);
			continuousEffect.setStop(StopWhen.TARGET_UNTAP);
		}
		
		/* eleshNorn_bonus */
		else if (name.equals("eleshNorn_bonus"))
			continuousEffect.setDescription("Other creatures you control get +2/+2.");

		/* eleshNorn_malus */
		else if (name.equals("eleshNorn_malus"))
			continuousEffect.setDescription("Creatures your opponents control get -2/-2.");
		
		/* melira_1 */
		else if (name.equals("melira_1"))
			continuousEffect.setDescription("You can't get poison counters.");
		/* melira_2 */
		else if (name.equals("melira_2"))
			continuousEffect.setDescription("Creatures you control can't have -1/-1 counters placed on them.");
		/* melira_3 */
		else if (name.equals("melira_3"))
			continuousEffect.setDescription("Creatures your opponents control lose infect. (unimplemented)");
		
		/* windingConstrictor_1 */
		else if (name.equals("windingConstrictor_1"))
			continuousEffect.setDescription("If one or more counters would be placed on an artifact or creature you control, that many plus one of each of those kinds of counters are placed on that permanent instead.");

		/* windingConstrictor_2 */
		else if (name.equals("windingConstrictor_2"))
			continuousEffect.setDescription("If you would get one or more counters, you get that many plus one of each of those kinds of counters instead.");
		
		/* signalPest_evasion */
		else if (name.equals("signalPest_evasion"))
			continuousEffect.setDescription("Signal Pest can't be blocked except by creatures with flying or reach.");
		
		/* denProtector_evasion */
		else if (name.equals("denProtector_evasion"))
			continuousEffect.setDescription("Creatures with power less than Den Protector's power can't block it.");
		
		/* treetopRangers_evasion */
		else if (name.equals("treetopRangers_evasion"))
			continuousEffect.setDescription("Treetop Rangers can't be blocked except by creatures with flying.");
		
		/* etchedChampion */
		else if (name.equals("etchedChampion"))
			continuousEffect.setDescription("<i>Metalcraft</i> — Etched Champion has protection from all colors as long as you control three or more artifacts.");
		
		/* tabernacle */
		else if (name.equals("tabernacle"))
			continuousEffect.setDescription("All creatures have \"At the beginning of your upkeep, destroy this creature unless you pay {1}.\"");
		
		/* retaliation */
		else if (name.equals("retaliation"))
			continuousEffect.setDescription("Creatures you control have \"Whenever this creature becomes blocked by a creature, this creature gets +1/+1 until end of turn.\"");
		
		/* glacialChasm_cantAttack */
		else if (name.equals("glacialChasm_cantAttack"))
			continuousEffect.setDescription("Creatures you control can't attack.");

		/* urzasArmor */
		else if (name.equals("urzasArmor"))
			continuousEffect.setDescription("If a source would deal damage to you, prevent 1 of that damage.");
		
		/* sulfuricVapors */
		else if (name.equals("sulfuricVapors"))
			continuousEffect.setDescription("If a red spell would deal damage to a permanent or player, it deals that much damage plus 1 to that permanent or player instead.");
		
		/* thaliaGuardianOfThraben */
		else if (name.equals("thaliaGuardianOfThraben"))
			continuousEffect.setDescription("Noncreature spells cost {1} more to cast.");
		
		/* glacialChasm_preventDamage */
		else if (name.equals("glacialChasm_preventDamage"))
			continuousEffect.setDescription("Prevent all damage that would be dealt to you.");
		
		/* fogBank */
		else if (name.equals("fogBank"))
			continuousEffect.setDescription("Prevent all combat damage that would be dealt to and dealt by Fog Bank.");
		
		/* crystallineSliver */
		else if (name.equals("crystallineSliver"))
			continuousEffect.setDescription("All Slivers have shroud.");
		
		/* muscleSliver */
		else if (name.equals("muscleSliver"))
			continuousEffect.setDescription("All Sliver creatures get +1/+1.");

		/* predatorySliver */
		else if (name.equals("predatorySliver"))
			continuousEffect.setDescription("Sliver creatures you control get +1/+1.");
		
		/* galeriderSliver */
		else if (name.equals("galeriderSliver"))
			continuousEffect.setDescription("Sliver creatures you control have flying.");
		
		/* meddlingMage */
		else if (name.equals("meddlingMage"))
			continuousEffect.setDescription("The named card can't be cast.");
		
		/* arcaneLaboratory */
		else if (name.equals("arcaneLaboratory"))
			continuousEffect.setDescription("Each player can't cast more than one spell each turn.");
		
		/* serraAvenger */
		else if (name.equals("serraAvenger"))
			continuousEffect.setDescription("You can't cast Serra Avenger during your first, second, or third turns of the game.");
		
		/* gaddockTeeg */
		else if (name.equals("gaddockTeeg_1"))
			continuousEffect.setDescription("Noncreature spells with converted mana cost 4 or greater can't be cast.");
		else if (name.equals("gaddockTeeg_2"))
			continuousEffect.setDescription("Noncreature spells with {X} in their mana costs can't be cast.");
		
		/* ionaShieldEmeria */
		else if (name.equals("ionaShieldEmeria"))
			continuousEffect.setDescription("Your opponents can't cast spells of the chosen color.");
		
		/* kalitas_putToken */
		else if (name.equals("kalitas_putToken"))
			continuousEffect.setDescription("If a nontoken creature an opponent controls would die, instead exile that card and create a 2/2 black Zombie creature token.");
		
		/* Narset Transcendent Emblem (opponents cannot cast noncreature spells) */
		else if (name.equals("narsetT_emblem"))
			continuousEffect.setDescription("Your opponents can't cast noncreature spells.");
		
		/* Arlinn, Embraced by the Moon (creatures have haste and tap : damage) */
		else if (name.equals("arlinnEBTM_emblem"))
			continuousEffect.setDescription("Creatures you control have haste and ‘{T}: This creature deals damage equal to its power to any target.'");
		
		/* knightOfTheReliquary_pump */
		else if (name.equals("knightOfTheReliquary_pump"))
			continuousEffect.setDescription("Knight of the Reliquary gets +1/+1 for each land card in your graveyard.");
		
		/* oathOfNissa_mana */
		else if (name.equals("oathOfNissa_mana"))
			continuousEffect.setDescription("You may spend mana as though it were mana of any color to cast planeswalker spells.");
		
		/* wildNacatl_mountain */
		else if (name.equals("wildNacatl_mountain"))
			continuousEffect.setDescription("Wild Nacatl gets +1/+1 as long as you control a Mountain.");
		
		/* wildNacatl_plains */
		else if (name.equals("wildNacatl_plains"))
			continuousEffect.setDescription("Wild Nacatl gets +1/+1 as long as you control a Plains.");
		
		/* inventorsApprentice */
		else if (name.equals("inventorsApprentice"))
			continuousEffect.setDescription("Inventor's Apprentice gets +1/+1 as long as you control an artifact.");
		
		/* sylvanAdvocate */
		else if (name.equals("sylvanAdvocate"))
			continuousEffect.setDescription("As long as you control six or more lands, Sylvan Advocate and land creatures you control get +2/+2.");
		
		/* krallenhordeHowler */
		else if (name.equals("krallenhordeHowler"))
			continuousEffect.setDescription("Creature spells you cast cost {1} less to cast.");
		
		/*Fires of Yavimaya */
		else if (name.equals("firesOfYavimaya"))
			continuousEffect.setDescription("Creatures you control have haste.");

		/* masterOfEtherium_pump */
		else if (name.equals("masterOfEtherium_pump"))
			continuousEffect.setDescription("Other artifact creatures you control get +1/+1.");	
		
		/* Exploration */
		else if (name.equals("exploration"))
			continuousEffect.setDescription("You may play an additional land on each of your turns.");
		
		/* Deranged Hermit */
		else if (name.equals("derangedHermit_boost"))
			continuousEffect.setDescription("Squirrel creatures get +1/+1.");

		/* opalescence */
		else if (name.equals("opalescence"))
			continuousEffect.setDescription("Each other non-Aura enchantment is a creature in addition to its other types and has base power and base toughness each equal to its converted mana cost.");
			
		/* basiliskCollar */
		else if (name.equals("basiliskCollar"))
			continuousEffect.setDescription("Equipped creature has deathtouch and lifelink.");
		
		/* cranialPlating */
		else if (name.equals("cranialPlating"))
			continuousEffect.setDescription("Equipped creature gets +1/+0 for each artifact you control.");	
		
		/* bonesplitter */
		else if (name.equals("bonesplitter"))
			continuousEffect.setDescription("Equipped creature gets +2/+0.");	
		
		/* batterskull */
		else if (name.equals("batterskull"))
			continuousEffect.setDescription("Equipped creature gets +4/+4 and has vigilance and lifelink.");	
		
		/* elspethKE_3 */
		else if (name.equals("elspethKE_3"))
			continuousEffect.setDescription("Artifacts, creatures, enchantments, and lands you control have indestructible.");
		
		/* lilianaDN_3 */
		else if (name.equals("lilianaDN_3"))
			continuousEffect.setDescription("Whenever a creature dies, return it to the battlefield under your control at the beginning of the next end step.");
		
		/* swordFireAndIce */
		else if (name.equals("swordFireAndIce"))
			continuousEffect.setDescription("Equipped creature gets +2/+2 and has protection from red and from blue.");
		
		/* swordFeastAndFamine */
		else if (name.equals("swordFeastAndFamine"))
			continuousEffect.setDescription("Equipped creature gets +2/+2 and has protection from black and from green.");
		
		/* swordBodyAndMind */
		else if (name.equals("swordBodyAndMind"))
			continuousEffect.setDescription("Equipped creature gets +2/+2 and has protection from green and from blue.");

		/* pendrellFlux */
		else if (name.equals("pendrellFlux"))
			continuousEffect.setDescription("Enchanted creature has \"At the beginning of your upkeep, sacrifice this creature unless you pay its mana cost.\"");
		
		/********************************************************************************************/
		/**                 Effects that grant one or more activated abilities                     **/
		/********************************************************************************************/
		
		/* hermeticStudy */
		else if (name.equals("hermeticStudy"))
			continuousEffect.setDescription("Enchanted creature has \"{T}: This creature deals 1 damage to any target.\"");
		
		/* citanulHierophants */
		else if (name.equals("citanulHierophants"))
			continuousEffect.setDescription("Creatures you control have \"{T}: Add {G}.\"");
		
		/* cryptolithRite */
		else if (name.equals("cryptolithRite"))
			continuousEffect.setDescription("Creatures you control have \"{T}: Add one mana of any color.\"");
		
		/* rishkarPeemaRenegade_mana */
		else if (name.equals("rishkarPeemaRenegade_mana"))
			continuousEffect.setDescription("Each creature you control with a counter on it has \"{T}: Add {G}.\"");
		
		/* hibernationSliver */
		else if (name.equals("hibernationSliver"))
			continuousEffect.setDescription("All Slivers have \"Pay 2 life: Return this permanent to its owner's hand.\"");
		
		/********************************************************************************************/
		/**                 Effects that generate a triggered ability                              **/
		/********************************************************************************************/
		
		/* chandraTOD_ultimate */
		else if (name.equals("chandraTOD_ultimate"))
			ta = TriggeredAbilityFactory.create("chandraTOD_ultimate_trigger", ((Emblem) source).getSource());

		/* Nissa, Vital Force emblem */
		else if (name.equals("nissaVF_emblem"))
			ta = TriggeredAbilityFactory.create("nissaVF_ultimate_trigger", ((Emblem) source).getSource());
		
		/* Nissa Vastwood Seer (landfall-like ability) */
		else if (name.equals("nissaVS_landfall"))
			ta = TriggeredAbilityFactory.create("nissaVS_transform", (Card) source);
		
		/* Tireless Tracker (landfall-like ability) */
		else if (name.equals("tirelessTracker_landfall"))
			ta = TriggeredAbilityFactory.create("tirelessTracker_investigate", (Card) source);

		/* catacombSifter_Scry */
		else if (name.equals("catacombSifter"))
			ta = TriggeredAbilityFactory.create("catacombSifter_scry", (Card) source);
	
		/* archangelAvacyn_creatureDies */
		else if (name.equals("archangelAvacyn_creatureDies")) 
			ta = TriggeredAbilityFactory.create("archangelAvacyn_creatureDies", (Card) source);
		
		/* grimHaruspex */
		else if (name.equals("grimHaruspex")) 
			ta = TriggeredAbilityFactory.create("grimHaruspex_draw", (Card) source);
		
		/* lilianaHeretical */
		else if (name.equals("lilianaHeretical")) 
			ta = TriggeredAbilityFactory.create("lilianaHeretical_transform", (Card) source);
		
		/* zulaportCutthroat */
		else if (name.equals("zulaportCutthroat"))
			ta = TriggeredAbilityFactory.create("zulaportCutthroat_drain", (Card) source);

		/* remembrance */
		else if (name.equals("remembrance"))
			ta = TriggeredAbilityFactory.create("remembrance_tutor", (Card) source);
		
		/* sporogenesis_makeToken */
		else if (name.equals("sporogenesis"))
			ta = TriggeredAbilityFactory.create("sporogenesis_makeToken", (Card) source);
		
		/* vampiricEmbrace_creatureDies */
		else if (name.equals("vampiricEmbrace_creatureDies"))
			ta = TriggeredAbilityFactory.create("vampiricEmbrace_addCounter", (Card) source);
		
		/* Fecundity */
		else if (name.equals("fecundity"))
			ta = TriggeredAbilityFactory.create("fecundity_draw", (Card) source);

		/* necropotence_discard */
		else if (name.equals("necropotence_discard"))
			ta = TriggeredAbilityFactory.create("necropotence_exile", (Card) source);
		
		/* diabolicServitude_return */
		else if (name.equals("diabolicServitude"))
			ta = TriggeredAbilityFactory.create("diabolicServitude_return", (Card) source);
		
		/* Bereavement */
		else if (name.equals("bereavement"))
			ta = TriggeredAbilityFactory.create("bereavement_discard", (Card) source);
		
		/* angelicChorus */
		else if (name.equals("angelicChorus"))
			ta = TriggeredAbilityFactory.create("angelicChorus_gainLife", (Card) source);
		
		/* taintedAEther */
		else if (name.equals("taintedAEther"))
			ta = TriggeredAbilityFactory.create("taintedAEther_sac", (Card) source);
		
		/* scald */
		else if (name.equals("scald"))
			ta = TriggeredAbilityFactory.create("scald_damage", (Card) source);
		
		/* Lifeline */
		else if (name.equals("lifeline"))
			ta = TriggeredAbilityFactory.create("lifeline_trigger", (Card) source);
		
		/* lifeline_delayed */
		else if (name.equals("lifeline_delayed")) {
			ta = TriggeredAbilityFactory.create("lifeline_reanimate", (Card) source);
			ta.setAdditionalData(additionalData);
			continuousEffect.setActiveZone(null);
		}
		
		/* Liability */
		else if (name.equals("liability")) 
			ta = TriggeredAbilityFactory.create("liability_loseLife", (Card) source);
		
		/* energyField_sac */
		else if (name.equals("energyField"))
			ta = TriggeredAbilityFactory.create("energyField_sac", (Card) source);
		
		/* planarVoid */
		else if (name.equals("planarVoid"))
			ta = TriggeredAbilityFactory.create("planarVoid_exile", (Card) source);
		
		// For delayed triggered abilities, make sure to specify NULL as the active zone
		
		/* necropotence_delayedTrigger */
		else if (name.equals("necropotence_delayedTrigger")) {
			ta = TriggeredAbilityFactory.create("necropotence_putCardInHand", (Card) source);
			continuousEffect.setActiveZone(null);
		}
		
		/* geistOfSaintTraft */
		else if (name.equals("geistOfSaintTraft")) {
			ta = TriggeredAbilityFactory.create("geistOfSaintTraft_exile", (Card) source);
			continuousEffect.setActiveZone(null);
		}
		
		/* mishrasBauble */
		else if (name.equals("mishrasBauble")) {
			ta = TriggeredAbilityFactory.create("mishrasBauble_draw", (Card) source);
			continuousEffect.setActiveZone(null);
		}
		
		/* flickerwisp_delayedTrigger */
		else if (name.equals("flickerwisp_delayedTrigger")) {
			ta = TriggeredAbilityFactory.create("flickerwisp_return", (Card) source);
			continuousEffect.setActiveZone(null);
		}
		
		/* wallOfJunk_return */
		else if (name.equals("wallOfJunk_return")) {
			ta = TriggeredAbilityFactory.create("wallOfJunk_return", (Card) source);
			continuousEffect.setActiveZone(null);
		}
		
		/* vebulid_destroy */
		else if (name.equals("vebulid_destroy")) {
			ta = TriggeredAbilityFactory.create("vebulid_destroy", (Card) source);
			continuousEffect.setActiveZone(null);
		}
		
		/* nahiriUltimate */
		else if (name.equals("nahiriUltimate")) {
			ta = TriggeredAbilityFactory.create("nahiriUltimate_bounce", (Card) source);
			continuousEffect.setActiveZone(null);
		}
		
		/* waylay_delayed */
		else if (name.equals("waylay_delayed")) {
			ta = TriggeredAbilityFactory.create("waylay_exile", (Card) source);
			continuousEffect.setActiveZone(null);
		}
		
		/* chandraF_1_delayed */
		else if (name.equals("chandraF_1_delayed")) {
			ta = TriggeredAbilityFactory.create("chandraF_exileTokens", (Card) source);
			continuousEffect.setActiveZone(null);
		}
		
		/* sneakAttack_delayed */
		else if (name.equals("sneakAttack_delayed")) {
			ta = TriggeredAbilityFactory.create("sneakAttack_sacCreature", (Card) source);
			continuousEffect.setActiveZone(null);
		}
		
		/* rebound */
		else if (name.equals("rebound")) {
			ta = TriggeredAbilityFactory.create("rebound_cast", (Card) source);
			continuousEffect.setActiveZone(null);
		}
		
		/* narset rebound */
		else if (name.equals("narset_rebound")) {
			ta = TriggeredAbilityFactory.create("narset_rebound", (Card) source);
			continuousEffect.setActiveZone(null);
		}
		
		/* rallyTheAncestors */
		else if (name.equals("rallyTheAncestors")) {
			ta = TriggeredAbilityFactory.create("rallyTheAncestors_exile", (Card) source);
			continuousEffect.setActiveZone(null);
		}
		
		/* archangelAvacyn_transform */
		else if (name.equals("archangelAvacyn_transform")) {
			ta = TriggeredAbilityFactory.create(name, (Card) source);
			continuousEffect.setActiveZone(null);
		}
		
		else {
			System.err.println("Continuous effect error : " + name);
			System.exit(0);
		}

		if (ta != null) {
			continuousEffect.setDescription(ta.getDescription());
			continuousEffect.addTriggeredEffect(ta);
			ta.setOrigin(Origin.CONTINUOUS_EFFECT);			
		}
		
		return continuousEffect;
	}
	
	public static ContinuousEffect create(String name, ContinuousEffectSource source) {
		return create(name, source, null);
	}
}
