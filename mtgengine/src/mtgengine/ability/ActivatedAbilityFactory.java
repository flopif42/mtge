package mtgengine.ability;

import mtgengine.Target.Category;
import mtgengine.TargetRequirement.Cardinality;
import mtgengine.card.Card;
import mtgengine.effect.Effect;
import mtgengine.zone.Zone.Name;

public class ActivatedAbilityFactory {
	public static ActivatedAbility create(String name, Card source) {
		return create(name, source, null);
	}
	
	public static ActivatedAbility create(String name, Card source, String parameter) {
		ActivatedAbility ability = new ActivatedAbility(name, source, parameter);
		
		/* Cycling */
		if (name.equals("cycling"))
			ability.initialize(Effect.DRAW_A_CARD, "Cycling " + parameter.toString() + " <i>(" + parameter.toString() + ", Discard this card: Draw a card.)</i>", Name.Hand);
		
		/* Equip */
		else if (name.equals("equip")) {
			ability.initialize(name, "Equip " + parameter.toString() + " <i>(" + parameter.toString() + ": Attach to target creature you control. Equip only as a sorcery.)</i>");
			ability.addTargetRequirement(Category.CreatureYouControl);
			ability.setSorcerySpeed();
		}
	
		/* Crew */
		else if (name.equals("crew"))
			ability.initialize(Effect.CREW, "Crew " + parameter + " <i>(Tap any number of creatures you control with total power " + parameter + " or more: This Vehicle becomes an artifact creature until end of turn.)</i>");
		
		/* clue_draw */
		else if (name.equals("clue_draw"))
			ability.initialize(Effect.DRAW_A_CARD, "{2}, Sacrifice this artifact: Draw a card.");
		
		/* whetstone */
		else if (name.equals("whetstone"))
			ability.initialize(name, "{3}: Each player puts the top two cards of his or her library into his or her graveyard.");
		
		/* voltaicKey */
		else if (name.equals("voltaicKey")) {
			ability.initialize(name, "{1}, {T}: Untap target artifact.");
			ability.addTargetRequirement(Category.Artifact);
		}
		
		/* lurkingEvil */
		else if (name.equals("lurkingEvil"))
			ability.initialize(name, "Pay half your life rounded up: Lurking Evil becomes a 4/4 Horror creature with flying.");
		
		/* rainOfFilth_sac */
		else if (name.equals("rainOfFilth_sac"))
			ability.initialize(name, "Sacrifice this land: Add {B}.");
		
		/* jitte_activation */
		else if (name.equals("jitte_activation")) {
			ability.initialize(name, "Remove a charge counter from Umezawa's Jitte: Choose one —§"
					+ "• Equipped creature gets +2/+2 until end of turn.§"
					+ "• Target creature gets -1/-1 until end of turn.§"
					+ "• You gain 2 life.");
			ability.setModal(3);
			ability.addTargetRequirementMode(2, Category.Creature, Cardinality.ONE);
		}
		
		/* arlinn_tapToDamage */
		else if (name.equals("arlinn_tapToDamage")) {
			ability.initialize(name, "{T}: This creature deals damage equal to its power to any target.");
			ability.addTargetRequirement(Category.AnyTarget);
		}
		
		/* hibernationSliver_return */
		else if (name.equals("hibernationSliver_return"))
			ability.initialize(name, "Pay 2 life: Return this permanent to its owner's hand.");
		
		/* duskwatchRecruiter */
		else if (name.equals("duskwatchRecruiter"))
			ability.initialize(name, "{2}{G}: Look at the top three cards of your library. You may reveal a creature card from among them and put it into your hand. Put the rest on the bottom of your library in any order.");
		
		/* wanderingFumarole_switch */
		else if (name.equals("wanderingFumarole_switch")) {
			ability.initialize(name, "{0}: Switch this creature's power and toughness until end of turn.");
		}
		
		/* blinkmoth_pump */
		else if (name.equals("blinkmoth_pump")) {
			ability.initialize(name, "{1}, {T}: Target Blinkmoth creature gets +1/+1 until end of turn.");
			ability.addTargetRequirement(Category.BlinkmothCreature);
			
		}
		
		/* piaNalaar_pump */
		else if (name.equals("piaNalaar_pump")) {
			ability.initialize(name, "{1}{R}: Target artifact creature gets +1/+0 until end of turn.");
			ability.addTargetRequirement(Category.ArtifactCreature);
		}
		
		/* piaNalaar_cantBlock */
		else if (name.equals("piaNalaar_cantBlock")) {
			ability.initialize(name, "{1}, Sacrifice an artifact: Target creature can't block this turn.");
			ability.addTargetRequirement(Category.Creature);
			
		}
		
		/* cranialPlating_attach */
		else if (name.equals("cranialPlating_attach")) {
			ability.initialize(name, "{B}{B}: Attach Cranial Plating to target creature you control.");
			ability.addTargetRequirement(Category.CreatureYouControl);
		}
		
		/* sealOfRemoval */
		else if (name.equals("sealOfRemoval")) {
			ability.initialize("unsummon", "Sacrifice Seal of Removal: Return target creature to its owner's hand.");
			ability.addTargetRequirement(Category.Creature);
			
		}
		
		/* sealOfCleansing */
		else if (name.equals("sealOfCleansing")) {
			ability.initialize(Effect.DESTROY_TARGET_CAN_REGEN, "Sacrifice Seal of Cleansing: Destroy target artifact or enchantment.");
			ability.addTargetRequirement(Category.ArtifactOrEnchantment);
			
		}
		
		/* mishrasBauble */
		else if (name.equals("mishrasBauble")) {
			ability.initialize(name, "{T}, Sacrifice Mishra's Bauble: Look at the top card of target player's library. Draw a card at the beginning of the next turn's upkeep.");
			ability.addTargetRequirement(Category.Player);
			
		}
		
		/* faithHealer */
		else if (name.equals("faithHealer")) {
			ability.initialize(name, "Sacrifice an enchantment: You gain life equal to the sacrificed enchantment's converted mana cost.");
			
		}
		
		/* elvishLyrist */
		else if (name.equals("elvishLyrist")) {
			ability.initialize(Effect.DESTROY_TARGET_CAN_REGEN, "{G}, {T}, Sacrifice Elvish Lyrist: Destroy target enchantment.");
			ability.addTargetRequirement(Category.Enchantment);
			
		}
		
		/* qasaliPridemage */
		else if (name.equals("qasaliPridemage")) {
			ability.initialize(Effect.DESTROY_TARGET_CAN_REGEN, "{1}, Sacrifice Qasali Pridemage: Destroy target artifact or enchantment.");
			ability.addTargetRequirement(Category.ArtifactOrEnchantment);
			
		}
		
		/* necropotence_draw */
		else if (name.equals("necropotence_pseudoDraw")) {
			ability.initialize(name, "Pay 1 life: Exile the top card of your library face down. Put that card into your hand at the beginning of your next end step.");
			
		}
		
		/* longtuskCub_counter */
		else if (name.equals("longtuskCub_counter")) {
			ability.initialize(Effect.PUT_PLUS_ONE_COUNTER_ON_THIS, "Pay {E}{E}: Put a +1/+1 counter on Longtusk Cub.");
			
		}
		
		/* shardPhoenix_sac */
		else if (name.equals("shardPhoenix_sac")) {
			ability.initialize(name, "Sacrifice Shard Phoenix: Shard Phoenix deals 2 damage to each creature without flying.");
			
		}
		
		/* shardPhoenix_return */
		else if (name.equals("shardPhoenix_return")) {
			ability.initialize(Effect.RETURN_TO_HAND, "{R}{R}{R}: Return Shard Phoenix from your graveyard to your hand. Activate this ability only during your upkeep.", Name.Graveyard);
			ability.setOnlyActivableDuringUpkeep();
		}
		
		/* scrapheapScrounger */
		else if (name.equals("scrapheapScrounger")) {
			ability.initialize("netherSpirit", "{1}{B}, Exile another creature card from your graveyard: Return Scrapheap Scrounger from your graveyard to the battlefield.", Name.Graveyard);
			
		}
		
		/* batterskull_return */
		else if (name.equals("batterskull_return")) {
			ability.initialize(name, "{3}: Return Batterskull to its owner's hand.");
		}
		
		/* coralhelmGuide */
		else if (name.equals("coralhelmGuide")) {
			ability.initialize(name, "{4}{U}: Target creature can't be blocked this turn.");
			ability.addTargetRequirement(Category.Creature);
		}
		
		/* loamDryad */
		else if (name.equals("loamDryad")) {
			ability.initialize(Effect.ADD_MANA, "{T}, Tap an untapped creature you control: Add one mana of any color.");
			
			
		}
		
		/* moxOpal */
		else if (name.equals("moxOpal")) {
			ability.initialize(Effect.ADD_MANA, "<i>Metalcraft</i> — {T}: Add one mana of any color. Activate this ability only if you control three or more artifacts.");
			
			ability.setOnlyActivableWithMetalcraft();
			
		}
		
		/* volrathsStronghold */
		else if (name.equals("volrathsStronghold")) {
			ability.initialize(name, "{1}{B}, {T}: Put target creature card from your graveyard on top of your library.");
			ability.addTargetRequirement(Category.CreatureCardInYourGraveyard);
			
		}
		
		/* attunement */
		else if (name.equals("attunement")) {
			ability.initialize(name, "Return Attunement to its owner's hand: Draw three cards, then discard four cards.");
			
		}
		
		/* recurringNightmare */
		else if (name.equals("recurringNightmare")) {
			ability.initialize(name, "Sacrifice a creature, Return Recurring Nightmare to its owner's hand: Return target creature card"
					+ " from your graveyard to the battlefield. Activate this ability only any time you could cast a sorcery.");
			ability.addTargetRequirement(Category.CreatureCardInYourGraveyard);
			ability.setSorcerySpeed();
			
			
		}
		
		/* moggFanatic */
		else if (name.equals("moggFanatic")) {
			ability.initialize(name, "Sacrifice Mogg Fanatic: Mogg Fanatic deals 1 damage to any target.");
			ability.addTargetRequirement(Category.AnyTarget);
			
		}
		
		/* yavimayaElder_draw */
		else if (name.equals("yavimayaElder_draw")) {
			ability.initialize(Effect.DRAW_A_CARD, "{2}, Sacrifice Yavimaya Elder: Draw a card.");
			
		}
		
		/* motherOfRunes */
		else if (name.equals("motherOfRunes")) {
			ability.initialize(name, "{T}: Target creature you control gains protection from the color of your choice until end of turn.");
			ability.addTargetRequirement(Category.CreatureYouControl);
			
		}
		
		/* mindOverMatter */
		else if (name.equals("mindOverMatter")) {
			ability.initialize(name, "Discard a card: You may tap or untap target artifact, creature, or land.");
			ability.addTargetRequirement(Category.ArtifactOrCreatureOrLand);
			
		}
		
		/* selflessSpirit */
		else if (name.equals("selflessSpirit")) {
			ability.initialize(name, "Sacrifice Selfless Spirit: Creatures you control gain indestructible until end of turn.");
			
		}
		
		/* loxodonHierarch_regen */
		else if (name.equals("loxodonHierarch_regen")) {
			ability.initialize(name, "{G}{W}, Sacrifice Loxodon Hierarch: Regenerate each creature you control.");
			
		}
		
		/* vampireHexmage */
		else if (name.equals("vampireHexmage")) {
			ability.initialize(name, "Sacrifice Vampire Hexmage: Remove all counters from target permanent.");
			ability.addTargetRequirement(Category.Permanent);
			
		}
		
		/* goblinLegionnaire_shock */
		else if (name.equals("goblinLegionnaire_shock")) {
			ability.initialize(Effect.DEAL_2_DAMAGE_TO_TARGET, "{R}, Sacrifice Goblin Legionnaire: Goblin Legionnaire deals 2 damage to any target.");
			ability.addTargetRequirement(Category.AnyTarget);
			
		}
		/* goblinLegionnaire_prevent */
		else if (name.equals("goblinLegionnaire_prevent")) {
			ability.initialize(name, "{W}, Sacrifice Goblin Legionnaire: Prevent the next 2 damage that would be dealt to any target this turn.");
			ability.addTargetRequirement(Category.AnyTarget);
			
		}
		
		/* sanctumGuardian */
		else if (name.equals("sanctumGuardian")) {
			ability.initialize(name, "Sacrifice Sanctum Guardian: The next time a source of your choice would deal damage to any target this turn, prevent that damage.");
			ability.addTargetRequirement(Category.AnyTarget);
			
		}
		
		/* RoPArtifacts */
		else if (name.equals("RoPArtifacts"))
			ability.initialize(name, "{W}: The next time an artifact source of your choice would deal damage to you this turn, prevent that damage.");
		
		/* RoPLands */
		else if (name.equals("RoPLands"))
			ability.initialize(name, "{W}: The next time a land source of your choice would deal damage to you this turn, prevent that damage.");
		
		/* RoPBlack */
		else if (name.equals("RoPBlack"))
			ability.initialize(name, "{W}: The next time a black source of your choice would deal damage to you this turn, prevent that damage.");

		/* RoPBlue */
		else if (name.equals("RoPBlue"))
			ability.initialize(name, "{W}: The next time a blue source of your choice would deal damage to you this turn, prevent that damage.");

		/* RoPGreen */
		else if (name.equals("RoPGreen"))
			ability.initialize(name, "{W}: The next time a green source of your choice would deal damage to you this turn, prevent that damage.");

		/* RoPRed */
		else if (name.equals("RoPRed"))
			ability.initialize(name, "{W}: The next time a red source of your choice would deal damage to you this turn, prevent that damage.");

		/* RoPWhite */
		else if (name.equals("RoPWhite"))
			ability.initialize(name, "{W}: The next time a white source of your choice would deal damage to you this turn, prevent that damage.");

		/* sanctumCustodian */
		else if (name.equals("sanctumCustodian")) {
			ability.initialize("goblinLegionnaire_prevent", "{T}: Prevent the next 2 damage that would be dealt to any target this turn.");
			ability.addTargetRequirement(Category.AnyTarget);
			
		}
		
		/* savageKnuckleblade_pump */
		else if (name.equals("savageKnuckleblade_pump")) {
			ability.initialize(name, "{2}{G}: Savage Knuckleblade gets +2/+2 until end of turn. Activate this ability only once each turn.");
			ability.setOncePerTurn();
		}
		/* savageKnuckleblade_bounce */
		else if (name.equals("savageKnuckleblade_bounce")) {
			ability.initialize(name, "{2}{U}: Return Savage Knuckleblade to its owner's hand.");
		}
		/* savageKnuckleblade_haste */
		else if (name.equals("savageKnuckleblade_haste")) {
			ability.initialize(name, "{R}: Savage Knuckleblade gains haste until end of turn");
		}
		
		/* steelOverseer */
		else if (name.equals("steelOverseer")) {
			ability.initialize(name, "{T}: Put a +1/+1 counter on each artifact creature you control.");
			
		}
		
		/* spikes_transfer */
		else if (name.equals("spikes_transfer")) {
			ability.initialize(name, "{2}, Remove a +1/+1 counter from this creature: Put a +1/+1 counter on target creature.");
			ability.addTargetRequirement(Category.Creature);
			
		}
		
		/* spikeFeeder */
		else if (name.equals("spikeFeeder")) {
			ability.initialize(name, "Remove a +1/+1 counter from Spike Feeder: You gain 2 life.");
			
		}
		
		/* eldraziDisplacer */
		else if (name.equals("eldraziDisplacer")) {
			ability.initialize(name, "{2}{C}: Exile another target creature, then return it to the battlefield tapped under its owner's control.");
			ability.addTargetRequirement(Category.AnotherCreature);
		}
		
		/* Hammer of Bogardan */
		else if (name.equals("hammerOfBogardan_return")) {
			ability.initialize(Effect.RETURN_TO_HAND, "{2}{R}{R}{R}: Return Hammer of Bogardan from your graveyard to your hand. Activate this ability only during your upkeep.", Name.Graveyard);
			ability.setOnlyActivableDuringUpkeep();
		}
		
		/* hangarbackWalker */
		else if (name.equals("hangarbackWalker")) {
			ability.initialize(Effect.PUT_PLUS_ONE_COUNTER_ON_THIS, "{1}, {T}: Put a +1/+1 counter on Hangarback Walker.");
			
		}
		
		/* walkingBallista_addCounter */
		else if (name.equals("walkingBallista_addCounter"))
			ability.initialize(Effect.PUT_PLUS_ONE_COUNTER_ON_THIS, "{4}: Put a +1/+1 counter on Walking Ballista.");
		
		/* walkingBallista_damage */
		else if (name.equals("walkingBallista_damage")) {
			ability.initialize(Effect.DEAL_1_DAMAGE_TO_TARGET, "Remove a +1/+1 counter from Walking Ballista: It deals 1 damage to any target.");
			ability.addTargetRequirement(Category.AnyTarget);
			
		}
		
		/* perniciousDeed */
		else if (name.equals("perniciousDeed")) {
			ability.initialize(name, "{X}, Sacrifice Pernicious Deed: Destroy each artifact, creature, and enchantment with converted mana cost X or less.");
			ability.requiresXValue();
			
		}
		
		/* mishrasHelix */
		else if (name.equals("mishrasHelix")) {
			ability.initialize(name, "{X}, {T}: Tap X target lands.");
			ability.addTargetRequirement(Category.Land, Cardinality.X);
			ability.requiresXValue();
			
		}
		
		/* aethersphereHarvester_lifelink */
		else if (name.equals("aethersphereHarvester_lifelink")) {
			ability.initialize(name, "Pay {E}: Aethersphere Harvester gains lifelink until end of turn.");
			
		}
		
//		/* heartOfKiran_alternateCrew */
//		else if (name.equals("heartOfKiran_alternateCrew")) {
//			ability.initialize(Effect.CREW, "You may remove a loyalty counter from a planeswalker you control rather than pay Heart of Kiran's crew cost.");
//			
//		}
		
		/* cinderElemental */
		else if (name.equals("cinderElemental")) {
			ability.initialize(name, "{X}{R}, {T}, Sacrifice Cinder Elemental: Cinder Elemental deals X damage to any target.");
			ability.addTargetRequirement(Category.AnyTarget);
			ability.requiresXValue();
			
		}
		
		/* phyrexianPlaguelord_1 */
		else if (name.equals("phyrexianPlaguelord_1")) {
			ability.initialize(name, "{T}, Sacrifice Phyrexian Plaguelord: Target creature gets -4/-4 until end of turn.");
			ability.addTargetRequirement(Category.Creature);
			
		}
		/* phyrexianPlaguelord_2 */
		else if (name.equals("phyrexianPlaguelord_2")) {
			ability.initialize(name, "Sacrifice a creature: Target creature gets -1/-1 until end of turn.");
			ability.addTargetRequirement(Category.Creature);
			
		}
		
		/* phyrexianProcessor_createToken */
		else if (name.equals("phyrexianProcessor_createToken")) {
			ability.initialize(name, "{4}, {T}: Create an X/X black Minion creature token, where X is the life paid as Phyrexian Processor entered the battlefield.");
			
		}
		
		/* sliverQueen */
		else if (name.equals("sliverQueen")) {
			ability.initialize(name, "{2}: Create a 1/1 colorless Sliver creature token.");
		}
		
		/* pitTrap */
		else if (name.equals("pitTrap")) {
			ability.initialize(Effect.DESTROY_TARGET_CANNOT_REGEN, "{2}, {T}, Sacrifice Pit Trap: Destroy target attacking creature without flying. It can't be regenerated.");
			ability.addTargetRequirement(Category.AttackingCreatureWithoutFlying);
			
		}
		
		/* goblinTrenches */
		else if (name.equals("goblinTrenches")) {
			ability.initialize(name, "{2}, Sacrifice a land: Create two 1/1 red and white Goblin Soldier creature tokens.");
			
		}
		
		/* meloku */
		else if (name.equals("meloku")) {
			ability.initialize(name, "{1}, Return a land you control to its owner's hand: Create a 1/1 blue Illusion creature token with flying.");
			
		}
		
		/* Saproling Burst */
		else if (name.equals("saprolingBurst_makeToken")) {
			ability.initialize(name, "Remove a fade counter from Saproling Burst: Put a green Saproling creature token "
					+ "onto the battlefield. It has \"This creature's power and toughness are each equal to the number of fade counters on "
					+ "Saproling Burst.\"");
			
		}
		
		/* parallaxWave_exile */
		else if (name.equals("parallaxWave_exile")) {
			ability.initialize(name, "Remove a fade counter from Parallax Wave: Exile target creature.");
			ability.addTargetRequirement(Category.Creature);
			
		}
		
		/* parallaxTide_exile */
		else if (name.equals("parallaxTide_exile")) {
			ability.initialize(name, "Remove a fade counter from Parallax Tide: Exile target land.");
			ability.addTargetRequirement(Category.Land);
			
		}
		
		/* ancientHydra */
		else if (name.equals("ancientHydra")) {
			ability.initialize(Effect.DEAL_1_DAMAGE_TO_TARGET, "{1}, Remove a fade counter from Ancient Hydra: Ancient Hydra deals 1 damage to any target.");
			ability.addTargetRequirement(Category.AnyTarget);
			
		}
		
		/* Archivist */
		else if (name.equals("archivist")) {
			ability.initialize(Effect.DRAW_A_CARD, "{T}: Draw a card.");
			
			
		}
		
		/* clawsOfGix */
		else if (name.equals("clawsOfGix")) {
			ability.initialize(name, "{1}, Sacrifice a permanent: You gain 1 life.");
			
		}
		
		/* crystalChimes */
		else if (name.equals("crystalChimes")) {
			ability.initialize(name, "{3}, {T}, Sacrifice Crystal Chimes: Return all enchantment cards from your graveyard to your hand.");
			
		}
		
		/* dragonBlood */
		else if (name.equals("dragonBlood")) {
			ability.initialize("spikes_transfer", "{3}, {T}: Put a +1/+1 counter on target creature.");
			ability.addTargetRequirement(Category.Creature);
			
		}
		
		/* citanulFlute */
		else if (name.equals("citanulFlute")) {
			ability.initialize(name, "{X}, {T}: Search your library for a creature card with converted mana cost X or less, reveal it, and put it into your hand. Then shuffle your library.");
			ability.requiresXValue();
			
		}
		
		/* faunaShaman */
		else if (name.equals("faunaShaman")) {
			ability.initialize(name, "{G}, {T}, Discard a creature card: Search your library for a creature card, reveal it, and put it into your hand. Then shuffle your library.");
			
		}
		
		/* evolvingWilds */
		else if (name.equals("evolvingWilds")) {
			ability.initialize(name, "{T}, Sacrifice Evolving Wilds: Search your library for a basic land card and put it onto the battlefield tapped. "
					+ "Then shuffle your library.");
			
		}
		
		/* knightOfTheReliquary_fetch */
		else if (name.equals("knightOfTheReliquary_fetch")) {
			ability.initialize(name, "{T}, Sacrifice a Forest or Plains: Search your library for a land card, put it onto the battlefield, then shuffle your library.");
			
		}
		
		/* fetchPlainsIsland */
		else if (name.equals("fetchPlainsIsland")) {
			ability.initialize("fetchLand", "{T}, Pay 1 life, Sacrifice Flooded Strand: Search your library for a Plains or Island card and put it onto the battlefield. Then shuffle your library.");
			
		}
		/* fetchIslandSwamp */
		else if (name.equals("fetchIslandSwamp")) {
			ability.initialize("fetchLand", "{T}, Pay 1 life, Sacrifice Polluted Delta: Search your library for an Island or Swamp card and put it onto the battlefield. Then shuffle your library.");
			
		}
		/* fetchSwampMountain */
		else if (name.equals("fetchSwampMountain")) {
			ability.initialize("fetchLand", "{T}, Pay 1 life, Sacrifice Bloodstained Mire: Search your library for a Swamp or Mountain card and put it onto the battlefield. Then shuffle your library.");
			
		}
		/* fetchMountainForest */
		else if (name.equals("fetchMountainForest")) {
			ability.initialize("fetchLand", "{T}, Pay 1 life, Sacrifice Wooded Foothills: Search your library for a Mountain or Forest card and put it onto the battlefield. Then shuffle your library.");
			
		}
		/* fetchForestPlains */
		else if (name.equals("fetchForestPlains")) {
			ability.initialize("fetchLand", "{T}, Pay 1 life, Sacrifice Windswept Heath: Search your library for a Forest or Plains card and put it onto the battlefield. Then shuffle your library.");
			
		}
		/* fetchPlainsSwamp */
		else if (name.equals("fetchPlainsSwamp")) {
			ability.initialize("fetchLand", "{T}, Pay 1 life, Sacrifice Marsh Flats: Search your library for a Plains or Swamp card and put it onto the battlefield. Then shuffle your library.");
			
		}
		/* fetchSwampForest */
		else if (name.equals("fetchSwampForest")) {
			ability.initialize("fetchLand", "{T}, Pay 1 life, Sacrifice Verdant Catacombs: Search your library for an Swamp or Forest card and put it onto the battlefield. Then shuffle your library.");
			
		}
		/* fetchForestIsland */
		else if (name.equals("fetchForestIsland")) {
			ability.initialize("fetchLand", "{T}, Pay 1 life, Sacrifice Misty Rainforest: Search your library for a Forest or Island card and put it onto the battlefield. Then shuffle your library.");
			
		}
		/* fetchIslandMountain */
		else if (name.equals("fetchIslandMountain")) {
			ability.initialize("fetchLand", "{T}, Pay 1 life, Sacrifice Scalding Tarn: Search your library for an Island or Mountain card and put it onto the battlefield. Then shuffle your library.");
			
		}
		/* fetchMountainPlains */
		else if (name.equals("fetchMountainPlains")) {
			ability.initialize("fetchLand", "{T}, Pay 1 life, Sacrifice Arid Mesa: Search your library for a Mountain or Plains card and put it onto the battlefield. Then shuffle your library.");
			
		}
		
		/* skirgeFamiliar */
		else if (name.equals("skirgeFamiliar"))
			ability.initialize(name, "Discard a card: Add {B}.");
		
		/* silentAttendant */
		else if (name.equals("silentAttendant"))
			ability.initialize(name, "{T}: You gain 1 life.");
		
		/* shivanHellkite */
		else if (name.equals("shivanHellkite")) {
			ability.initialize(Effect.DEAL_1_DAMAGE_TO_TARGET, "{1}{R}: Shivan Hellkite deals 1 damage to any target.");
			ability.addTargetRequirement(Category.AnyTarget);
		}
		
		/* Ping */
		else if (name.equals("ping")) { 
			ability.initialize(Effect.DEAL_1_DAMAGE_TO_TARGET, "{T}: " + source.getName() + " deals 1 damage to any target.");
			ability.addTargetRequirement(Category.AnyTarget);
			
		}
		
		/* songstitcher */
		else if (name.equals("songstitcher")) {
			ability.initialize(name, "{1}{W}: Prevent all combat damage that would be dealt this turn by target attacking creature with flying.");
			ability.addTargetRequirement(Category.AttackingCreatureWithFlying);
		}
		
		/* opalAcrolith_petrify */
		else if (name.equals("opalAcrolith_petrify"))
			ability.initialize(name, "{0}: Opal Acrolith becomes an enchantment.");
		
		/* soulSculptor */
		else if (name.equals("soulSculptor")) {
			ability.initialize(name, "{1}{W}, {T}: Target creature becomes an enchantment and loses all abilities until a player casts a creature spell.");
			ability.addTargetRequirement(Category.Creature);
		}
		
		/* figureOfDestiny_1 */
		else if (name.equals("figureOfDestiny_1"))
			ability.initialize(name, "{r/w}: Figure of Destiny becomes a Kithkin Spirit with base power and toughness 2/2.");
		/* figureOfDestiny_2 */
		else if (name.equals("figureOfDestiny_2"))
			ability.initialize(name, "{r/w}{r/w}{r/w}: If Figure of Destiny is a Spirit, it becomes a Kithkin Spirit Warrior with base power and toughness 4/4.");
		/* figureOfDestiny_3 */
		else if (name.equals("figureOfDestiny_3"))
			ability.initialize(name, "{r/w}{r/w}{r/w}{r/w}{r/w}{r/w}: If Figure of Destiny is a Warrior, it becomes a Kithkin Spirit Warrior Avatar with base power and toughness 8/8, flying, and first strike.");
		
		/* skyshroudElf */
		else if (name.equals("skyshroudElf"))
			ability.initialize(name, "{1}: Add {R} or {W}.");
		
		/* nobleHierarch */
		else if (name.equals("nobleHierarch"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {G}, {W}, or {U}.");
		
		/* lotusPetal */
		else if (name.equals("lotusPetal"))
			ability.initialize(Effect.ADD_MANA, "{T}, Sacrifice Lotus Petal: Add one mana of any color.");
		
		/* addAnyMana */
		else if (name.equals("addAnyMana"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add one mana of any color.");
		
		/* tendoIceBridge */
		else if (name.equals("tendoIceBridge"))
			ability.initialize(Effect.ADD_MANA, "{T}, Remove a charge counter from Tendo Ice Bridge: Add one mana of any color.");
		
		/* aetherHub_mana */
		else if (name.equals("aetherHub_mana"))
			ability.initialize(Effect.ADD_MANA, "{T}, Pay {E}: Add one mana of any color.");
		
		/* gemstoneMine */
		else if (name.equals("gemstoneMine"))
			ability.initialize(Effect.ADD_MANA, "{T}, Remove a mining counter from Gemstone Mine: Add one mana of any color. If there are no mining counters on Gemstone Mine, sacrifice it.");
		
		/* grandColiseum */
		else if (name.equals("grandColiseum"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add one mana of any color. Grand Coliseum deals 1 damage to you.");
		
		/* gavonyTownship */
		else if (name.equals("gavonyTownship"))
			ability.initialize(name, "{2}{G}{W}, {T}: Put a +1/+1 counter on each creature you control.");
		
		/* fireAnts */
		else if (name.equals("fireAnts"))
			ability.initialize(name, "{T}: Fire Ants deals 1 damage to each other creature without flying.");
		
		/* phyrexianColossus_untap */
		else if (name.equals("phyrexianColossus_untap"))
			ability.initialize(Effect.UNTAP_THIS, "Pay 8 life: Untap Phyrexian Colossus.");
		
		/* horeshoeCrab */
		else if (name.equals("horeshoeCrab"))
			ability.initialize(Effect.UNTAP_THIS, "{U}: Untap Horseshoe Crab.");
		
		/* mobileFort */
		else if (name.equals("mobileFort")) {
			ability.initialize(name, "{3}: Mobile Fort gets +3/-1 until end of turn and can attack this turn as though it didn't have defender. Activate this ability only once each turn.");
			ability.setOncePerTurn();
		}
		
		/* hoppingAutomaton */
		else if (name.equals("hoppingAutomaton"))
			ability.initialize(name, "{0}: Hopping Automaton gets -1/-1 and gains flying until end of turn.");
		
		/* pestilence_damage */
		else if (name.equals("pestilence_damage"))
			ability.initialize(Effect.PESTILENCE, "{B}: Pestilence deals 1 damage to each creature and each player.");
		
		/* thrashingWumpus */
		else if (name.equals("thrashingWumpus"))
			ability.initialize(Effect.PESTILENCE, "{B}: Thrashing Wumpus deals 1 damage to each creature and each player.");
		
		/* wirewoodSymbiote */
		else if (name.equals("wirewoodSymbiote")) {
			ability.initialize(name, "Return an Elf you control to its owner's hand: Untap target creature. Activate this ability only once each turn.");
			ability.addTargetRequirement(Category.Creature);
			ability.setOncePerTurn();
			
		}
		
		/* wallOfRoots */
		else if (name.equals("wallOfRoots")) {
			ability.initialize(Effect.ADD_MANA, "Put a -0/-1 counter on Wall of Roots: Add {G}. Activate this ability only once each turn.");
			ability.setOncePerTurn();
			
		}
		
		/* questingPheldda_1 */
		else if (name.equals("questingPheldda_1")) {
			ability.initialize(name, "{G}: Questing Phelddagrif gets +1/+1 until end of turn. Target opponent creates a 1/1 green Hippo creature token.");
			ability.addTargetRequirement(Category.Opponent);
		}
		else if (name.equals("questingPheldda_2")) {
			ability.initialize(name, "{W}: Questing Phelddagrif gains protection from black and from red until end of turn. Target opponent gains 2 life.");
			ability.addTargetRequirement(Category.Opponent);
		}
		else if (name.equals("questingPheldda_3")) {
			ability.initialize(name, "{U}: Questing Phelddagrif gains flying until end of turn. Target opponent may draw a card.");
			ability.addTargetRequirement(Category.Opponent);
		}

		/* sterlingGrove_tutor */
		else if (name.equals("sterlingGrove_tutor")) {
			ability.initialize(name, "{1}, Sacrifice Sterling Grove: Search your library for an enchantment card and reveal that card. Shuffle your library, then put the card on top of it.");
			
		}
		
		/* Morphling */
		else if (name.equals("morphling_1"))
			ability.initialize(Effect.UNTAP_THIS, "{U}: Untap Morphling.");
		else if (name.equals("morphling_2"))
			ability.initialize(name, "{U}: Morphling gains flying until end of turn.");
		else if (name.equals("morphling_3"))
			ability.initialize(name, "{U}: Morphling gains shroud until end of turn.");
		else if (name.equals("morphling_4"))
			ability.initialize(name, "{1}: Morphling gets +1/-1 until end of turn.");
		else if (name.equals("morphling_5"))
			ability.initialize(name, "{1}: Morphling gets -1/+1 until end of turn.");
		
		/* chimericStaff */
		else if (name.equals("chimericStaff")) {
			ability.initialize(name, "{X}: Chimeric Staff becomes an X/X Construct artifact creature until end of turn.");
			ability.requiresXValue();
		}
		
		/* blinkmoth_animate */
		else if (name.equals("blinkmoth_animate"))
			ability.initialize(name, "{1}: Blinkmoth Nexus becomes a 1/1 Blinkmoth artifact creature with flying until end of turn. It's still a land.");

		/* inkmoth_animate */
		else if (name.equals("inkmoth_animate"))
			ability.initialize(name, "{1}: Inkmoth Nexus becomes a 1/1 Blinkmoth artifact creature with flying and infect until end of turn. It's still a land.");

		/* mutavault */
		else if (name.equals("mutavault"))
			ability.initialize(name, "{1}: Mutavault becomes a 2/2 creature with all creature types until end of turn. It's still a land.");
		
		/* forbiddingWatchtower */
		else if (name.equals("forbiddingWatchtower"))
			ability.initialize(name, "{1}{W}: Forbidding Watchtower becomes a 1/5 white Soldier creature until end of turn. It's still a land.");
		
		/* faerieConclave */
		else if (name.equals("faerieConclave"))
			ability.initialize(name, "{1}{U}: Faerie Conclave becomes a 2/1 blue Faerie creature with flying until end of turn. It's still a land.");
		
		/* spawningPool */
		else if (name.equals("spawningPool"))
			ability.initialize(name, "{1}{B}: Spawning Pool becomes a 1/1 black Skeleton creature with \"{B}: Regenerate this creature\" until end of turn. It's still a land.");
		
		/* ghituEncampment */
		else if (name.equals("ghituEncampment")) {
			ability.initialize(name, "{1}{R}: Ghitu Encampment becomes a 2/1 red Warrior creature with first strike until end of turn. It's still a land.");
		}
		
		/* treetopVillage */
		else if (name.equals("treetopVillage")) {
			ability.initialize("treetopVillage", "{1}{G}: Treetop Village becomes a 3/3 green Ape creature with trample until end of turn. It's still a land.");
		}
		
		/* shamblingVent */
		else if (name.equals("shamblingVent")) {
			ability.initialize(name, "{1}{W}{B}: Shambling Vent becomes a 2/3 white and black Elemental creature with lifelink until end of turn. It's still a land.");
		}
		
		/* hissingQuagmire */
		else if (name.equals("hissingQuagmire")) {
			ability.initialize(name, "{1}{B}{G}: Hissing Quagmire becomes a 2/2 black and green Elemental creature with deathtouch until end of turn. It's still a land.");
		}

		/* wanderingFumarole */
		else if (name.equals("wanderingFumarole")) {
			ability.initialize(name, "{2}{U}{R}: Until end of turn Wandering Fumarole becomes a 1/4 blue and red Elemental creature with \"{0}: Switch this creature's power and toughness until end of turn.\" It's still a land.");
		}
		
		/* lumberingFalls */
		else if (name.equals("lumberingFalls")) {
			ability.initialize(name, "{2}{G}{U}: Lumbering Falls becomes a 3/3 green and blue Elemental creature with hexproof until end of turn. It's still a land.");
		}
		
		/* needleSpires */
		else if (name.equals("needleSpires")) {
			ability.initialize(name, "{2}{R}{W}: Needle Spires becomes a 2/1 red and white Elemental creature with double strike until end of turn. It's still a land.");
		}
		
		/* celestialColonnade */
		else if (name.equals("celestialColonnade")) {
			ability.initialize(name, "{3}{W}{U}: Until end of turn Celestial Colonnade becomes a 4/4 white and blue Elemental creature with flying and vigilance. It's still a land.");
		}
		
		/* creepingTarPit */
		else if (name.equals("creepingTarPit")) {
			ability.initialize(name, "{1}{U}{B}: Creeping Tar Pit becomes a 3/2 blue and black Elemental creature until end of turn and can't be blocked this turn. It's still a land.");
		}
		
		/* lavaclawReaches */
		else if (name.equals("lavaclawReaches")) {
			ability.initialize(name, "{1}{B}{R}: Until end of turn Lavaclaw Reaches becomes a 2/2 black and red Elemental creature with \"{X}: This creature gets +X/+0 until end of turn.\" It's still a land.");
		}
		
		/* ragingRavine */
		else if (name.equals("ragingRavine")) {
			ability.initialize(name, "{2}{R}{G}: Until end of turn Raging Ravine becomes a 3/3 red and green Elemental creature with \"Whenever this creature attacks, put a +1/+1 counter on it.\" It's still a land.");
		}
		
		/* stirringWildwood */
		else if (name.equals("stirringWildwood")) {
			ability.initialize(name, "{1}{G}{W}: Until end of turn Stirring Wildwood becomes a 3/4 green and white Elemental creature with reach. It's still a land.");
		}
		
		/* opposition */
		else if (name.equals("opposition")) {
			ability.initialize(Effect.TAP_TARGET, "Tap an untapped creature you control: Tap target artifact, creature, or land.");
			ability.addTargetRequirement(Category.ArtifactOrCreatureOrLand);
			
		}
		
		/* aetherVial_put */
		else if (name.equals("aetherVial_put")) {
			ability.initialize(name, "{T}: You may put a creature card with converted mana cost equal to the number of charge counters on Aether Vial from your hand onto the battlefield.");
			ability.setOptional();
			
		}
		
		/* horizonCanopy_mana */
		else if (name.equals("horizonCanopy_mana"))
			ability.initialize(Effect.ADD_MANA, "{T}, Pay 1 life: Add {G} or {W}.");
		
		/* horizonCanopy_sac */
		else if (name.equals("horizonCanopy_sac"))
			ability.initialize(Effect.DRAW_A_CARD, "{1}, {T}, Sacrifice Horizon Canopy: Draw a card.");
		
		/* Stoneforge Mystic */
		else if (name.equals("stoneforgeMystic_put")) {
			ability.initialize(name, "{1}{W}, {T}: You may put an Equipment card from your hand onto the battlefield.");
			ability.setOptional();
			
		}
		
		/* noRestfortheWicked */
		else if (name.equals("noRestfortheWicked"))
			ability.initialize(name, "Sacrifice No Rest for the Wicked: Return to your hand all creature cards in your graveyard that were put there from the battlefield this turn.");
		
		/* midsummerRevel_sac */
		else if (name.equals("midsummerRevel_sac"))
			ability.initialize(name, "{G}, Sacrifice Midsummer Revel: Create X 3/3 green Beast creature tokens, where X is the number of verse counters on Midsummer Revel.");
		
		/* recantation_sac */
		else if (name.equals("recantation_sac")) {
			ability.initialize(name, "{U}, Sacrifice Recantation: Return up to X target permanents to their owners' hands, where X is the number of verse counters on Recantation.");
			ability.addTargetRequirement(Category.Permanent, Cardinality.UP_TO_X);
			ability.requiresXValue();
			
		}
		
		/* vileRequiem_sac */
		else if (name.equals("vileRequiem_sac")) {
			ability.initialize(name, "{1}{B}, Sacrifice Vile Requiem: Destroy up to X target nonblack creatures, where X is the number of verse counters on Vile Requiem. They can't be regenerated.");
			ability.addTargetRequirement(Category.NonBlackCreature, Cardinality.UP_TO_X);
			ability.requiresXValue();
			
		}
		
		/* warDance_sac */
		else if (name.equals("warDance_sac")) {
			ability.initialize(name, "Sacrifice War Dance: Target creature gets +X/+X until end of turn, where X is the number of verse counters on War Dance.");
			ability.addTargetRequirement(Category.Creature);
			
		}
		
		/* rumblingCrescendo_sac */
		else if (name.equals("rumblingCrescendo_sac")) {
			ability.initialize(name, "{R}, Sacrifice Rumbling Crescendo: Destroy up to X target lands, where X is the number of verse counters on Rumbling Crescendo.");
			ability.addTargetRequirement(Category.Land, Cardinality.UP_TO_X);
			ability.requiresXValue();
			
		}
		
		/* barrinsCodex_sac */
		else if (name.equals("barrinsCodex_sac"))
			ability.initialize(name, "{4}, {T}, Sacrifice Barrin's Codex: Draw X cards, where X is the number of page counters on Barrin's Codex.");
		
		/* serrasHymn_sac */
		else if (name.equals("serrasHymn_sac")) {
			ability.initialize(name, "Sacrifice Serra's Hymn: Prevent the next X damage that would be dealt this turn to any number of target creatures and/or players, divided as you choose, where X is the number of verse counters on Serra's Hymn.");
			ability.addTargetRequirement(Category.AnyTarget);
		}
		
		/* serrasLiturgy_sac */
		else if (name.equals("serrasLiturgy_sac")) {
			ability.initialize(name, "{W}, Sacrifice Serra's Liturgy: Destroy up to X target artifacts and/or enchantments, where X is the number of verse counters on Serra's Liturgy.");
			ability.addTargetRequirement(Category.ArtifactOrEnchantment, Cardinality.UP_TO_X);
			ability.requiresXValue();
		}
		
		/* liltingRefrain_sac */
		else if (name.equals("liltingRefrain_sac")) {
			ability.initialize(name, "Sacrifice Lilting Refrain: Counter target spell unless its controller pays {X}, where X is the number of verse counters on Lilting Refrain.");
			ability.addTargetRequirement(Category.Spell);
		}
		
		/* douse */
		else if (name.equals("douse")) {
			ability.initialize(Effect.COUNTER_TARGET_STACKOBJECT, "{1}{U}: Counter target red spell.");
			ability.addTargetRequirement(Category.RedSpell);
		}
		
		/* discordantDirge_sac */
		else if (name.equals("discordantDirge_sac")) {
			ability.initialize(name, "{B}, Sacrifice Discordant Dirge: Look at target opponent's hand and choose up to X cards from it, where X is the number of verse counters on Discordant Dirge. That player discards those cards.");
			ability.addTargetRequirement(Category.Opponent);
		}
		
		/* torchSong_sac */
		else if (name.equals("torchSong_sac")) {
			ability.initialize(name, "{2}{R}, Sacrifice Torch Song: Torch Song deals X damage to any target, where X is the number of verse counters on Torch Song.");
			ability.addTargetRequirement(Category.AnyTarget);
		}
		
		/* metrognome */
		else if (name.equals("metrognome"))
			ability.initialize(name, "{4}, {T}: Create a 1/1 colorless Gnome artifact creature token.");
		
		/* lotusBlossom_sac */
		else if (name.equals("lotusBlossom_sac"))
			ability.initialize(name, "{T}, Sacrifice Lotus Blossom: Add X mana of any one color, where X is the number of petal counters on Lotus Blossom.");
		
		/* disruptiveStudent */
		else if (name.equals("disruptiveStudent")) {
			ability.initialize(name, "{T}: Counter target spell unless its controller pays {1}.");
			ability.addTargetRequirement(Category.Spell);
		}
		
		/* copperGnomes */
		else if (name.equals("copperGnomes")) {
			ability.initialize(name, "{4}, Sacrifice Copper Gnomes: You may put an artifact card from your hand onto the battlefield.");
			ability.setOptional();
		}

		/* momirVig */
		else if (name.equals("momirVig")) {
			ability.initialize(name, "{X}, Discard a card: Create a token that's a copy of a creature card with converted mana cost X chosen at random. Activate this ability only any time you could cast a sorcery and only once each turn.");
			ability.requiresXValue();
			ability.setOncePerTurn();
			ability.setSorcerySpeed();
		}
		
		/* visceraSeer */
		else if (name.equals("visceraSeer")) {
			ability.initialize(name, "Sacrifice a creature: Scry 1.");
			
		}
		
		/* witchEngine */
		else if (name.equals("witchEngine")) {
			ability.initialize(Effect.ADD_MANA, "{T}: Add {B}{B}{B}{B}. Target opponent gains control of Witch Engine.");
			ability.addTargetRequirement(Category.Opponent);
			
		}
		
		/* wizardMentor */
		else if (name.equals("wizardMentor")) {
			ability.initialize(name, "{T}: Return Wizard Mentor and target creature you control to their owner's hand.");
			ability.addTargetRequirement(Category.CreatureYouControl);
			
		}
		
		/* westernPaladin */
		else if (name.equals("westernPaladin")) {
			ability.initialize(Effect.DESTROY_TARGET_CAN_REGEN, "{B}{B}, {T}: Destroy target white creature.");
			ability.addTargetRequirement(Category.WhiteCreature);
			
		}
		
		/* easternPaladin */
		else if (name.equals("easternPaladin")) {
			ability.initialize(Effect.DESTROY_TARGET_CAN_REGEN, "{B}{B}, {T}: Destroy target green creature.");
			ability.addTargetRequirement(Category.GreenCreature);
			
		}
		
		/* intrepidHero */
		else if (name.equals("intrepidHero")) {
			ability.initialize(Effect.DESTROY_TARGET_CAN_REGEN, "{T}: Destroy target creature with power 4 or greater.");
			ability.addTargetRequirement(Category.CreatureWithPower4orGreater);
			
		}

		/* karn_animate */
		else if (name.equals("karn_animate")) {
			ability.initialize(name, "{1}: Target noncreature artifact becomes an artifact creature with power and toughness each equal to its converted mana cost until end of turn.");
			ability.addTargetRequirement(Category.NonCreatureArtifact);
		}
		
		/* Visara */
		else if (name.equals("visara")) {
			ability.initialize(Effect.DESTROY_TARGET_CANNOT_REGEN, "{T}: Destroy target creature. It can't be regenerated.");
			ability.addTargetRequirement(Category.Creature);
			
		}

		/* doggedHunter */
		else if (name.equals("doggedHunter")) {
			ability.initialize(Effect.DESTROY_TARGET_CAN_REGEN, "{T}: Destroy target creature token.");
			ability.addTargetRequirement(Category.TokenCreature);
			
		}
		
		/* noblePanther */
		else if (name.equals("noblePanther")) {
			ability.initialize(name, "{1}: Noble Panther gains first strike until end of turn.");
		}
		
		/* Pump (~this gets +1/+1 until end of turn) */
		else if (name.equals("pump"))
			ability.initialize(name, parameter + ": " + ability.getSource().getName() + " gets +1/+1 until end of turn.");
		
		/* endoskeleton */
		else if (name.equals("endoskeleton")) {
			ability.initialize(name, "{2}, {T}: Target creature gets +0/+3 for as long as Endoskeleton remains tapped.");
			ability.addTargetRequirement(Category.Creature);
			
		}
		
		/* manaLeech */
		else if (name.equals("manaLeech")) {
			ability.initialize(name, "{T}: Tap target land. It doesn't untap during its controller's untap step for as long as Mana Leech remains tapped.");
			ability.addTargetRequirement(Category.Land);
			
		}
		
		/* sandSquid */
		else if (name.equals("sandSquid")) {
			ability.initialize(name, "{T}: Tap target creature. That creature doesn't untap during its controller's untap step for as long as Sand Squid remains tapped.");
			ability.addTargetRequirement(Category.Creature);
			
		}
		
		/* lavaclawReaches_pump */
		else if (name.equals("lavaclawReaches_pump")) {
			ability.initialize(name, "{X}: This creature gets +X/+0 until end of turn.");
			ability.requiresXValue();
		}
		
		/* sneakAttack */
		else if (name.equals("sneakAttack")) {
			ability.initialize(name, "{R}: You may put a creature card from your hand onto the battlefield. That creature gains haste. Sacrifice the creature at the beginning of the next end step.");
			ability.setOptional();
		}
		
		/* fieryMantle_pump */
		else if (name.equals("fieryMantle_pump"))
			ability.initialize(name, "{R}: Enchanted creature gets +1/+0 until end of turn.");
		
		/* firebreathing */
		else if (name.equals("firebreathing"))
			ability.initialize(name, "{R}: " + ability.getSource().getName() + " gets +1/+0 until end of turn.");
		
		/* firesOfYavimaya_pump */
		else if (name.equals("firesOfYavimaya_pump")) {
			ability.initialize(name, "Sacrifice Fires of Yavimaya: Target creature gets +2/+2 until end of turn.");
			ability.addTargetRequirement(Category.Creature);
			
		}
		
		/* temporalAperture */
		else if (name.equals("temporalAperture")) {
			ability.initialize(name, "{5}, {T}: Shuffle your library, then reveal the top card. Until end of turn, for as long as that card remains on top of your library, play with the top card of your library revealed and you may play that card without paying its mana cost.");
			
		}
		
		/* griselbrand */
		else if (name.equals("griselbrand")) {
			ability.initialize(name, "Pay 7 life: Draw seven cards.");
			
		}
		
		/* mazeOfIth */
		else if (name.equals("mazeOfIth")) {
			ability.initialize(name, "{T}: Untap target attacking creature. Prevent all combat damage that would be dealt to and dealt by that creature this turn.");
			ability.addTargetRequirement(Category.AttackingCreature);
			
		}
		
		/* angelicPage */
		else if (name.equals("angelicPage")) {
			ability.initialize(name, "{T}: Target attacking or blocking creature gets +1/+1 until end of turn.");
			ability.addTargetRequirement(Category.AttackingOrBlockingCreature);
			
		}
		
		/* elvishHerder */
		else if (name.equals("elvishHerder")) {
			ability.initialize(name, "{G}: Target creature gains trample until end of turn.");
			ability.addTargetRequirement(Category.Creature);
		}
		
		/* eliteArchers */
		else if (name.equals("eliteArchers")) {
			ability.initialize(name, "{T}: Elite Archers deals 3 damage to target attacking or blocking creature.");
			ability.addTargetRequirement(Category.AttackingOrBlockingCreature);
			
		}
		
		/* carrionBeetles */
		else if (name.equals("carrionBeetles")) {
			ability.initialize(name, "{2}{B}, {T}: Exile up to three target cards from a single graveyard.");
			ability.addTargetRequirement(Category.CardInAnyGraveyard, Cardinality.UP_TO_THREE);
			
		}
		
		/* bloodVassal */
		else if (name.equals("bloodVassal")) {
			ability.initialize(name, "Sacrifice Blood Vassal: Add {B}{B}.");
			
			
		}
		
		/* barrin */
		else if (name.equals("barrin")) {
			ability.initialize("unsummon", "{2}, Sacrifice a permanent: Return target creature to its owner's hand.");
			ability.addTargetRequirement(Category.Creature);
			
		}
		
		/* dynavoltTower_damage */
		else if (name.equals("dynavoltTower_damage")) {
			ability.initialize(Effect.DEAL_3_DAMAGE_TO_TARGET, "{T}, Pay {E}{E}{E}{E}{E}: Dynavolt Tower deals 3 damage to any target.");
			ability.addTargetRequirement(Category.AnyTarget);
			
		}
		
		/* shieldedAetherThief_draw */
		else if (name.equals("shieldedAetherThief_draw")) {
			ability.initialize(Effect.DRAW_A_CARD, "{T}, Pay {E}{E}{E}: Draw a card.");
			
		}
		
		/* argothianElder */
		else if (name.equals("argothianElder")) {
			ability.initialize(name, "{T}: Untap two target lands.");
			ability.addTargetRequirement(Category.Land, Cardinality.TWO);
			
		}
		
		/* Yawgmoth's Bargain */
		else if (name.equals("yawgmothsBargain")) {
			ability.initialize(Effect.DRAW_A_CARD, "Pay 1 life: Draw a card.");
			
		}

		/* azoriusGuildmage_tap */
		else if (name.equals("azoriusGuildmage_tap")) {
			ability.initialize(Effect.TAP_TARGET, "{2}{W}: Tap target creature.");
			ability.addTargetRequirement(Category.Creature);
		}
		
		/* azoriusGuildmage_counter */
		else if (name.equals("azoriusGuildmage_counter")) {
			ability.initialize(Effect.COUNTER_TARGET_STACKOBJECT, "{2}{U}: Counter target activated ability.");
			ability.addTargetRequirement(Category.ActivatedAbility);
		}
		
		/* Stormscape Apprentice */
		else if (name.equals("stormscapeApprentice_tap")) {
			ability.initialize(Effect.TAP_TARGET, "{W}, {T}: Tap target creature.");
			ability.addTargetRequirement(Category.Creature);
			
		}
		else if (name.equals("stormscapeApprentice_loselife")) {
			ability.initialize(name, "{B}, {T}: Target player loses 1 life.");
			ability.addTargetRequirement(Category.Player);
			
		}
		
		/* libraryOfAlexandria */
		else if (name.equals("libraryOfAlexandria_draw")) {
			ability.initialize(Effect.DRAW_A_CARD, "{T}: Draw a card. Activate this ability only if you have exactly seven cards in hand.");
			
		}
		
		/* spireOfIndustry */
		else if (name.equals("spireOfIndustry")) {
			ability.initialize(Effect.ADD_MANA, "{T}, Pay 1 life: Add one mana of any color. Activate this ability only if you control an artifact.");
			
		}
		
		/* karakas */
		else if (name.equals("karakas")) {
			ability.initialize(name, "{T}: Return target legendary creature to its owner's hand.");
			ability.addTargetRequirement(Category.LegendaryCreature);
			
		}
		
		/* rishadanPort */
		else if (name.equals("rishadanPort")) {
			ability.initialize(Effect.TAP_TARGET, "{1}, {T}: Tap target land.");
			ability.addTargetRequirement(Category.Land);
			
		}
		
		/* eldraziSacForMana */
		else if (name.equals("eldraziSacForMana")) {
			ability.initialize(Effect.ADD_MANA, "Sacrifice this creature: Add {C}.");
			
		}
		
		// Filter lands
		/* mysticGate */
		else if (name.equals("mysticGate")) {
			ability.initialize(Effect.ADD_MANA, "{w/u}, {T}: Add {W}{W}, {W}{U}, or {U}{U}.");
			
		}
		/* sunkenRuins */
		else if (name.equals("sunkenRuins")) {
			ability.initialize(Effect.ADD_MANA, "{u/b}, {T}: Add {U}{U}, {U}{B}, or {B}{B}.");
			
		}
		/* gravenCairns */
		else if (name.equals("gravenCairns")) {
			ability.initialize(Effect.ADD_MANA, "{b/r}, {T}: Add {B}{B}, {B}{R}, or {R}{R}.");
			
		}
		/* fireLitThicket */
		else if (name.equals("fireLitThicket")) {
			ability.initialize(Effect.ADD_MANA, "{r/g}, {T}: Add {R}{R}, {R}{G}, or {G}{G}.");
			
		}
		/* woodedBastion */
		else if (name.equals("woodedBastion")) {
			ability.initialize(Effect.ADD_MANA, "{g/w}, {T}: Add {G}{G}, {G}{W}, or {W}{W}.");
			
		}
		/* fetidHeath */
		else if (name.equals("fetidHeath")) {
			ability.initialize(Effect.ADD_MANA, "{w/b}, {T}: Add {W}{W}, {W}{B}, or {B}{B}.");
			
		}
		/* twilightMire */
		else if (name.equals("twilightMire")) {
			ability.initialize(Effect.ADD_MANA, "{b/g}, {T}: Add {B}{B}, {B}{G}, or {G}{G}.");
			
		}
		/* floodedGrove */
		else if (name.equals("floodedGrove")) {
			ability.initialize(Effect.ADD_MANA, "{g/u}, {T}: Add {G}{G}, {G}{U}, or {U}{U}.");
			
		}
		/* cascadeBluffs */
		else if (name.equals("cascadeBluffs")) {
			ability.initialize(Effect.ADD_MANA, "{u/r}, {T}: Add {U}{U}, {U}{R}, or {R}{R}.");
			
		}
		/* ruggedPrairie */
		else if (name.equals("ruggedPrairie")) {
			ability.initialize(Effect.ADD_MANA, "{r/w}, {T}: Add {R}{R}, {R}{W}, or {W}{W}.");
			
		}
		
		/* dustBowl */
		else if (name.equals("dustBowl")) {
			ability.initialize(Effect.DESTROY_TARGET_CAN_REGEN, "{3}, {T}, Sacrifice a land: Destroy target nonbasic land.");
			ability.addTargetRequirement(Category.NonbasicLand);
			
			
		}
		
		/* wasteland */
		else if (name.equals("wasteland")) {
			ability.initialize(Effect.DESTROY_TARGET_CAN_REGEN, "{T}, Sacrifice Wasteland: Destroy target nonbasic land.");
			ability.addTargetRequirement(Category.NonbasicLand);
			
			
		}
		
		/* wornPowerstone */
		else if (name.equals("wornPowerstone")) {
			ability.initialize(Effect.ADD_MANA, "{T}: Add {C}{C}.");
			
		}
		
		/* manaVault_addMana */
		else if (name.equals("manaVault_addMana")) {
			ability.initialize(Effect.ADD_MANA, "{T}: Add {C}{C}{C}.");
			
		}
		
		/* workhorse_ */
		else if (name.equals("workhorse_mana")) {
			ability.initialize(Effect.ADD_MANA, "Remove a +1/+1 counter from Workhorse: Add {C}.");
			
		}
		
		/* ancientTomb */
		else if (name.equals("ancientTomb")) {
			ability.initialize(Effect.ADD_MANA, "{T}: Add {C}{C}. Ancient Tomb deals 2 damage to you.");
			
		}
		
		/* arcboundRavager_sac */
		else if (name.equals("arcboundRavager_sac")) {
			ability.initialize(name, "Sacrifice an artifact: Put a +1/+1 counter on Arcbound Ravager.");
			
		}
		
		/* serrasSantcum */
		else if (name.equals("serrasSantcum")) {
			ability.initialize(Effect.ADD_MANA, "{T}: Add {W} for each enchantment you control.");
			
		}
		
		/* priestOfTitania */
		else if (name.equals("priestOfTitania")) {
			ability.initialize(Effect.ADD_MANA, "{T}: Add {G} for each Elf on the battlefield.");
			
		}
		
		/* gaeasCradle */
		else if (name.equals("gaeasCradle")) {
			ability.initialize(Effect.ADD_MANA, "{T}: Add {G} for each creature you control.");
			
		}
		
		/* tolarianAcademy */
		else if (name.equals("tolarianAcademy")) {
			ability.initialize(Effect.ADD_MANA, "{T}: Add {U} for each artifact you control.");
			
		}
		
		/* kalitas_sac */
		else if (name.equals("kalitas_sac")) {
			ability.initialize(name, "{2}{B}, Sacrifice another Vampire or Zombie: Put two +1/+1 counters on Kalitas, Traitor of Ghet.");
			
		}
		
		/* siegeGang_sac */
		else if (name.equals("siegeGang_sac")) {
			ability.initialize(name, "{1}{R}, Sacrifice a Goblin: Siege-Gang Commander deals 2 damage to any target.");
			ability.addTargetRequirement(Category.AnyTarget);
			
		}
		
		/* falkenrathAristocrat */
		else if (name.equals("falkenrathAristocrat")) {
			ability.initialize(name, "Sacrifice a creature: Falkenrath Aristocrat gains indestructible until end of turn. If the sacrificed "
					+ "creature was a Human, put a +1/+1 counter on Falkenrath Aristocrat.");
			
		}
		
		/* phyrexianGhoul */
		else if (name.equals("phyrexianGhoul")) {
			ability.initialize("nantukoHusk", "Sacrifice a creature: Phyrexian Ghoul gets +2/+2 until end of turn.");
			
		}
		
		/* nantukoHusk */
		else if (name.equals("nantukoHusk")) {
			ability.initialize(name, "Sacrifice a creature: Nantuko Husk gets +2/+2 until end of turn.");
			
		}
		
		/* shivanGorge */
		else if (name.equals("shivanGorge")) {
			ability.initialize(name, "{2}{R}, {T}: Shivan Gorge deals 1 damage to each opponent.");
			
		}
		
		/* phyrexianTower */
		else if (name.equals("phyrexianTower")) {
			ability.initialize(name, "{T}, Sacrifice a creature: Add {B}{B}.");
			
			
		}
		
		/* scavengingOoze */
		else if (name.equals("scavengingOoze")) {
			ability.initialize("scavengingOoze", "{G}: Exile target card from a graveyard. If it was a creature card, put a +1/+1 counter on Scavenging Ooze and you gain 1 life.");
			ability.addTargetRequirement(Category.CardInAnyGraveyard);
		}
		
		/* Deathrite Shaman */
		else if (name.equals("drs_1")) {
			ability.initialize(name, "{T}: Exile target land card from a graveyard. Add one mana of any color.");
			ability.addTargetRequirement(Category.LandCardInAnyGraveyard);
			
		}
		else if (name.equals("drs_2")) {
			ability.initialize(name, "{B}, {T}: Exile target instant or sorcery card from a graveyard. Each opponent loses 2 life.");
			ability.addTargetRequirement(Category.SorceryOrInstantCardInAnyGraveyard);
			
		}
		else if (name.equals("drs_3")) {
			ability.initialize(name, "{G}, {T}: Exile target creature card from a graveyard. You gain 2 life.");
			ability.addTargetRequirement(Category.CreatureCardInAnyGraveyard);
			
		}

		/* darkDepths_removeCounter */
		else if (name.equals("darkDepths_removeCounter"))
			ability.initialize(name, "{3}: Remove an ice counter from Dark Depths.");
		
		/* masticore_ping */
		else if (name.equals("masticore_ping")) {
			ability.initialize(Effect.DEAL_1_DAMAGE_TO_TARGET, "{2}: Masticore deals 1 damage to target creature.");
			ability.addTargetRequirement(Category.Creature);
		}
		
		/* masticore_regen */
		else if (name.equals("masticore_regen"))
			ability.initialize("regenerate", "{2}: Regenerate Masticore.");
		
		/* fortitude_regen */
		else if (name.equals("fortitude_regen")) {
			ability.initialize(name, "Sacrifice a Forest: Regenerate enchanted creature.");
			
		}
		
		/* greaterGood */
		else if (name.equals("greaterGood")) {
			ability.initialize(name, "Sacrifice a creature: Draw cards equal to the sacrificed creature's power, then discard three cards.");
			
		}
		
		/* gaeasEmbrace_regen */
		else if (name.equals("gaeasEmbrace_regen"))
			ability.initialize(name, "{G}: Regenerate enchanted creature.");
		
		/* viashinoSandswimmer */
		else if (name.equals("viashinoSandswimmer"))
			ability.initialize(name, "{R}: Flip a coin. If you win the flip, return Viashino Sandswimmer to its owner's hand. If you lose the flip, sacrifice Viashino Sandswimmer.");
		
		/* spinedFluke_regen */
		else if (name.equals("spinedFluke_regen"))
			ability.initialize("regenerate", "{B}: Regenerate Spined Fluke.");
		
		/* unworthyDead_regen */
		else if (name.equals("unworthyDead_regen"))
			ability.initialize("regenerate", "{B}: Regenerate Unworthy Dead.");
		
		/* childOfGaea_regen */
		else if (name.equals("childOfGaea_regen"))
			ability.initialize("regenerate", "{1}{G}: Regenerate Child of Gaea.");
		
		/* sanguineGuard */
		else if (name.equals("sanguineGuard"))
			ability.initialize("regenerate", "{1}{B}: Regenerate Sanguine Guard.");
		
		/* albinoTroll_regen */
		else if (name.equals("albinoTroll_regen"))
			ability.initialize("regenerate", "{1}{G}: Regenerate Albino Troll.");
		
		/* spectralLynx_regen */
		else if (name.equals("spectralLynx_regen"))
			ability.initialize("regenerate", "{B}: Regenerate Spectral Lynx.");
		
		/* spawningPool_regen */
		else if (name.equals("spawningPool_regen"))
			ability.initialize("regenerate", "{B}: Regenerate this creature.");
		
		/* willOTheWisp_regen */
		else if (name.equals("willOTheWisp_regen"))
			ability.initialize("regenerate", "{B}: Regenerate Will-o'-the-Wisp.");
		
		/* Nissa, Voice of Zendikar */
		else if (name.equals("nissaVOZ_1")) {
			ability.initialize(name, "+1: Create a 0/1 green Plant creature token.");
			
		}
		else if (name.equals("nissaVOZ_2")) {
			ability.initialize(name, "-2: Put a +1/+1 counter on each creature you control.");
			
		}
		else if (name.equals("nissaVOZ_3")) {
			ability.initialize(name, "-7: You gain X life and draw X cards, where X is the number of lands you control.");
			
		}
		
		/* Arlinn Kord */
		else if (name.equals("arlinnKord_1")) {
			ability.initialize(name, "+1: Until end of turn, up to one target creature gets +2/+2 and gains vigilance and haste.");
			ability.addTargetRequirement(Category.Creature, Cardinality.UP_TO_ONE);
			
		}
		else if (name.equals("arlinnKord_2")) {
			ability.initialize(name, "0: Create a 2/2 green Wolf creature token. Transform Arlinn Kord.");
			
		}
		/* Arlinn, Embraced by the Moon */
		else if (name.equals("arlinnEBTM_1")) {
			ability.initialize(name, "+1: Creatures you control get +1/+1 and gain trample until end of turn.");
			
		}
		else if (name.equals("arlinnEBTM_2")) {
			ability.initialize(name, "-1: Arlinn, Embraced by the Moon deals 3 damage to any target. Transform Arlinn, Embraced by the Moon.");
			ability.addTargetRequirement(Category.AnyTarget);
			
		}
		else if (name.equals("arlinnEBTM_3")) {
			ability.initialize(name, "-6: You get an emblem with \"Creatures you control have haste and ‘{T}: This creature deals damage equal to its power to any target.'\"");
			
		}
		
		/* Sorin, Grim Nemesis */
		else if (name.equals("sorinGN_1")) {
			ability.initialize(name, "+1: Reveal the top card of your library and put that card into your hand. Each opponent loses life equal to its converted mana cost.");
			
		}
		else if (name.equals("sorinGN_2")) {
			ability.initialize(name, "-X: Sorin, Grim Nemesis deals X damage to target creature or planeswalker and you gain X life.");
			ability.requiresXValue();
			ability.addTargetRequirement(Category.CreatureOrPlaneswalker);
			
		}
		else if (name.equals("sorinGN_3")) {
			ability.initialize(name, "-9: Create a number of 1/1 black Vampire Knight creature tokens with lifelink equal to the highest life total among all players.");
			
		}
		
		/* Sorin, Solemn Visitor */
		else if (name.equals("sorinSV_1")) {
			ability.initialize(name, "+1: Until your next turn, creatures you control get +1/+0 and gain lifelink.");
			
		}
		else if (name.equals("sorinSV_2")) {
			ability.initialize(name, "-2: Create a 2/2 black Vampire creature token with flying.");
			
		}
		else if (name.equals("sorinSV_3")) {
			ability.initialize(name, "-6: You get an emblem with \"At the beginning of each opponent's upkeep, that player sacrifices a creature.\"");
			
		}
		
		/* Nissa, Vital Force */
		else if (name.equals("nissaVF_1")) {
			ability.initialize(name, "+1: Untap target land you control. Until your next turn, it becomes a 5/5 Elemental creature with haste. It's still a land.");
			ability.addTargetRequirement(Category.LandYouControl);
			
		}
		else if (name.equals("nissaVF_2")) {
			ability.initialize(Effect.REGROWTH, "-3: Return target permanent card from your graveyard to your hand.");
			ability.addTargetRequirement(Category.PermanentCardInYourGraveyard);
			
		}
		else if (name.equals("nissaVF_3")) {
			ability.initialize(name, "-6: You get an emblem with \"Whenever a land enters the battlefield under your control, you may draw a card.\"");
			
		}
		
		/* Chandra, Torch of Defiance */
		else if (name.equals("chandraTOD_1")) {
			ability.initialize(name, "+1: Exile the top card of your library. You may cast that card. If you don't, Chandra, Torch of Defiance deals 2 damage to each opponent.");
			
		}
		else if (name.equals("chandraTOD_2")) {
			ability.initialize(name, "+1: Add {R}{R}.");
			
		}
		else if (name.equals("chandraTOD_3")) {
			ability.initialize(name, "-3: Chandra, Torch of Defiance deals 4 damage to target creature.");
			ability.addTargetRequirement(Category.Creature);
			
		}
		else if (name.equals("chandraTOD_4")) {
			ability.initialize(name, "-7: You get an emblem with \"Whenever you cast a spell, this emblem deals 5 damage to any target.\"");
			
		}
		
		/* Gideon, Ally of Zendikar */
		else if (name.equals("gideonAOZ_1")) {
			ability.initialize(name, "+1: Until end of turn, Gideon, Ally of Zendikar becomes a 5/5 Human Soldier Ally creature with indestructible that's still a planeswalker. Prevent all damage that would be dealt to him this turn.");
			
		}
		else if (name.equals("gideonAOZ_2")) {
			ability.initialize(name, "0: Create a 2/2 white Knight Ally creature token.");
			
		}
		else if (name.equals("gideonAOZ_3")) {
			ability.initialize(name, "-4: You get an emblem with \"Creatures you control get +1/+1.\"");
			
		}
		
		/* Elspeth, Knight-Errant */
		else if (name.equals("elspethKE_1")) {
			ability.initialize(name, "+1: Create a 1/1 white Soldier creature token.");
			
		}
		else if (name.equals("elspethKE_2")) {
			ability.initialize(name, "+1: Target creature gets +3/+3 and gains flying until end of turn.");
			ability.addTargetRequirement(Category.Creature);
			
		}
		else if (name.equals("elspethKE_3")) {
			ability.initialize(name, "-8: You get an emblem with \"Artifacts, creatures, enchantments, and lands you control have indestructible.\"");
			
		}
		
		/* Jace, Telepath Unbound */
		else if (name.equals("jaceTU_1")) {
			ability.initialize(name, "+1: Up to one target creature gets -2/-0 until your next turn.");
			ability.addTargetRequirement(Category.Creature, Cardinality.UP_TO_ONE);
			
		}
		else if (name.equals("jaceTU_2")) {
			ability.initialize("jaceVP_2", "-3: You may cast target instant or sorcery card from your graveyard this turn. If that card would be put into your graveyard this turn, exile it instead.");
			
		}
		else if (name.equals("jaceTU_3")) {
			ability.initialize("jaceVP_3", "-9: You get an emblem with \"Whenever you cast a spell, target opponent puts the top five cards of his or her library into his or her graveyard.\"");
			
		}
		
		/* Nissa, Sage Animist */
		else if (name.equals("nissaSA_1")) {
			ability.initialize(name, "+1: Reveal the top card of your library. If it's a land card, put it onto the battlefield. Otherwise, put it into your hand.");
			
		}
		else if (name.equals("nissaSA_2")) {
			ability.initialize(name, "-2: Create a legendary 4/4 green Elemental creature token named Ashaya, the Awoken World.");
			
		}
		else if (name.equals("nissaSA_3")) {
			ability.initialize(name, "-7: Untap up to six target lands. They become 6/6 Elemental creatures. They're still lands.");
			
		}
		
		/* Jace, Unraveler of Secrets */
		else if (name.equals("jaceUOS_1")) {
			ability.initialize(name, "+1: Scry 1, then draw a card.");
			
		}
		else if (name.equals("jaceUOS_2")) {
			ability.initialize(name, "-2: Return target creature to its owner's hand.");
			ability.addTargetRequirement(Category.Creature);
			
		}
		else if (name.equals("jaceUOS_3")) {
			ability.initialize(name, "-8: You get an emblem with \"Whenever an opponent casts his or her first spell each turn, counter that spell.\"");
			
		}
		
		/* Ob Nixilis Reignited */
		else if (name.equals("nixilisR_1")) {
			ability.initialize(name, "+1: You draw a card and you lose 1 life.");
			
		}
		else if (name.equals("nixilisR_2")) {
			ability.initialize(Effect.DESTROY_TARGET_CAN_REGEN, "-3: Destroy target creature.");
			ability.addTargetRequirement(Category.Creature);
			
		}
		else if (name.equals("nixilisR_3")) {
			ability.initialize(name, "-8: Target opponent gets an emblem with \"Whenever a player draws a card, you lose 2 life.\"");
			ability.addTargetRequirement(Category.Player);
			
		}
		
		/* Nahiri, the Harbinger */
		else if (name.equals("nahiriTH_1")) {
			ability.initialize(name, "+2: You may discard a card. If you do, draw a card.");
			
			ability.setOptional();
		}
		else if (name.equals("nahiriTH_2")) {
			ability.initialize(name, "-2: Exile target enchantment, tapped artifact, or tapped creature.");
			
			ability.addTargetRequirement(Category.EnchantmentTappedArtifactOrTappedCreature);
		}
		else if (name.equals("nahiriTH_3")) {
			ability.initialize(name, "-8: Search your library for an artifact or creature card, put it onto the battlefield, then shuffle your library. It gains haste. Return it to your hand at the beginning of the next end step.");
			
		}
		
		/* Chandra, Flamecaller */
		else if (name.equals("chandraF_1")) {
			ability.initialize(name, "+1: Create two 3/1 red Elemental creature tokens with haste. Exile them at the beginning of the next end step.");
			
		}
		else if (name.equals("chandraF_2")) {
			ability.initialize(name, "0: Discard all the cards in your hand, then draw that many cards plus one.");
			
		}
		else if (name.equals("chandraF_3")) {
			ability.initialize(name, "-X: Chandra, Flamecaller deals X damage to each creature.");
			
			ability.requiresXValue();
		}
		
		/* Narset Transcendent */
		else if (name.equals("narsetT_1")) {
			ability.initialize(name, "+1: Look at the top card of your library. If it's a noncreature, nonland card, you may reveal it and put it into your hand.");
			
		}
		else if (name.equals("narsetT_2")) {
			ability.initialize(name, "-2: When you cast your next instant or sorcery spell from your hand this turn, it gains rebound.");
			
		}
		else if (name.equals("narsetT_3")) {
			ability.initialize(name, "-9: You get an emblem with \"Your opponents can't cast noncreature spells.\"");
			
		}
		
		/* Garruk Wildspeaker */
		else if (name.equals("garrukW_1")) {
			ability.initialize(name, "+1: Untap two target lands.");
			
			ability.addTargetRequirement(Category.Land, Cardinality.TWO);
		}
		else if (name.equals("garrukW_2")) {
			ability.initialize(name, "-1: Create a 3/3 green Beast creature token.");
			
		}
		else if (name.equals("garrukW_3")) {
			ability.initialize(name, "-4: Creatures you control get +3/+3 and gain trample until end of turn.");
			
		}
		
		/* Liliana, Defiant Necromancer */
		else if (name.equals("lilianaDN_1")) {
			ability.initialize("lilianaOTV_1", "+2: Each player discards a card.");
			
		}
		else if (name.equals("lilianaDN_2")) {
			ability.initialize(name, "-X: Return target nonlegendary creature card with converted mana cost X from your graveyard to the battlefield.");
			ability.addTargetRequirement(Category.NonLgdryCreaCardInYourGydWithCMCX);
			ability.requiresXValue();
			
		}
		else if (name.equals("lilianaDN_3")) {
			ability.initialize(name, "-8: You get an emblem with \"Whenever a creature dies, return it to the battlefield under your control at the beginning of the next end step.\"");
			
		}
		
		/* Liliana of the Veil */
		else if (name.equals("lilianaOTV_1")) {
			ability.initialize("lilianaOTV_1", "+1: Each player discards a card.");
			
		}
		else if (name.equals("lilianaOTV_2")) {
			ability.initialize("lilianaOTV_2", "-2: Target player sacrifices a creature.");
			ability.addTargetRequirement(Category.Player);
			
		}
		else if (name.equals("lilianaOTV_3")) {
			ability.initialize("lilianaOTV_3", "-6: Separate all permanents target player controls into two piles. That player sacrifices all permanents in the pile of his or her choice.");
			
		}
		
		/* groveOfTheBurnwillows */
		else if (name.equals("groveOfTheBurnwillows"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {R} or {G}. Each opponent gains 1 life.");
		
		/* Khans of Tarkir tri-lands */
		else if (name.equals("frontierBivouac"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {G}, {U}, or {R}.");
		else if (name.equals("mysticMonastery"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {U}, {R}, or {W}.");
		else if (name.equals("nomadOutpost"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {R}, {W}, or {B}.");
		else if (name.equals("opulentPalace"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {B}, {G}, or {U}.");
		else if (name.equals("sandsteppeCitadel"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {W}, {B}, or {G}.");
		
		/* Mana producing abilities that require to tap */
		else if (name.equals("addWhiteMana"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {W}.");
		else if (name.equals("addBlueMana"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {U}.");
		else if (name.equals("addBlackMana"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {B}.");
		else if (name.equals("addRedMana"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {R}.");
		else if (name.equals("addGreenMana"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {G}.");
		else if (name.equals("colorlessMana"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {C}.");
		
		/* Implicit activated abilities inherited from basic land types */
		else if (name.equals("plains")) {
			ability.initialize(Effect.ADD_MANA, "{T}: Add {W}.");
			ability.setImplied();
		}
		else if (name.equals("island")) {
			ability.initialize(Effect.ADD_MANA, "{T}: Add {U}.");
			ability.setImplied();
		}
		else if (name.equals("swamp")) {
			ability.initialize(Effect.ADD_MANA, "{T}: Add {B}.");
			ability.setImplied();
		}
		else if (name.equals("mountain")) {
			ability.initialize(Effect.ADD_MANA, "{T}: Add {R}.");
			ability.setImplied();
		}
		else if (name.equals("forest")) {
			ability.initialize(Effect.ADD_MANA, "{T}: Add {G}.");
			ability.setImplied();
		}
		
		/* dual lands that do not have basic land types */
		else if (name.equals("whiteOrBlueMana"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {W} or {U}.");
		else if (name.equals("blueOrBlackMana"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {U} or {B}.");
		else if (name.equals("blackOrRedMana"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {B} or {R}.");
		else if (name.equals("redOrGreenMana"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {R} or {G}.");
		else if (name.equals("greenOrWhiteMana"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {G} or {W}.");
		else if (name.equals("whiteOrBlackMana"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {W} or {B}.");
		else if (name.equals("blackOrGreenMana"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {B} or {G}.");
		else if (name.equals("greenOrBlueMana"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {G} or {U}.");
		else if (name.equals("blueOrRedMana"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {U} or {R}.");
		else if (name.equals("redOrWhiteMana"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {R} or {W}.");
		
		/* pain lands */
		else if (name.equals("adarkarWastes"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {W} or {U}. Adarkar Wastes deals 1 damage to you.");
		else if (name.equals("undergroundRiver"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {U} or {B}. Underground River deals 1 damage to you.");
		else if (name.equals("sulfurousSprings"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {B} or {R}. Sulfurous Springs deals 1 damage to you.");
		else if (name.equals("karplusanForest"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {R} or {G}. Karplusan Forest deals 1 damage to you.");
		else if (name.equals("brushland"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {G} or {W}. Brushland deals 1 damage to you.");
		else if (name.equals("cavesOfKoilos"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {W} or {B}. Caves of Koilos deals 1 damage to you.");
		else if (name.equals("llanowarWastes"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {B} or {G}. Llanowar Wastes deals 1 damage to you.");
		else if (name.equals("yavimayaCoast"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {G} or {U}. Yavimaya Coast deals 1 damage to you.");
		else if (name.equals("shivanReef"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {U} or {R}. Shivan Reef deals 1 damage to you.");
		else if (name.equals("battlefieldForge"))
			ability.initialize(Effect.ADD_MANA, "{T}: Add {R} or {W}. Battlefield Forge deals 1 damage to you.");
		
		/* springleafDrum */
		else if (name.equals("springleafDrum"))
			ability.initialize(Effect.ADD_MANA, "{T}, Tap an untapped creature you control: Add one mana of any color.");
		
		/* westvaleAbbey_make_token */
		else if (name.equals("westvaleAbbey_make_token"))
			ability.initialize(name, "{5}, {T}, Pay 1 life: Create a 1/1 white and black Human Cleric creature token.");
		
		/* westvaleAbbey_transform */
		else if (name.equals("westvaleAbbey_transform"))
			ability.initialize(name, "{5}, {T}, Sacrifice five creatures: Transform Westvale Abbey, then untap it.");
		
		/* thespiansStage */
		else if (name.equals("thespiansStage")) {
			ability.initialize(name, "{2}, {T}: Thespian's Stage becomes a copy of target land and gains this ability.");
			ability.addTargetRequirement(Category.Land);
		}
		
		/* Jace, Vryn Prodigy */
		else if (name.equals("jaceVrynProdigy"))
			ability.initialize(name, "{T}: Draw a card, then discard a card. If there are five or more cards in your graveyard, exile Jace, Vryn's Prodigy, then return him to the battlefield transformed under his owner's control.");

		/* Transform */
		else if (name.equals("transform"))
			ability.initialize(name, "(0): Transform this.");
		
		else {
			System.err.println("Could not create ability : " + name);
			System.exit(0);
		}
		
		return ability;
	}
}
