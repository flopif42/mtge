package mtgengine.server;

import java.util.Vector;

import mtgengine.Game;
import mtgengine.HighlightCandidates;
import mtgengine.Game.Answer;
import mtgengine.Game.PermanentCategory;
import mtgengine.Game.Phase;
import mtgengine.Game.Response;
import mtgengine.Game.State;
import mtgengine.card.Color;
import mtgengine.card.UntapOptional;

public class Interpreter {
	private Game _g;
	private Vector<Integer> _choices = null;
	private int _iTarget = 0;
	private int _idPlayer;
	private int _idOpponent;
	private boolean _bFirstResponse = true;
	
	public Interpreter(Game g, int idPlayer) {
		_g = g;		
		_idPlayer = idPlayer;
		_idOpponent = _g.findPlayerByID(idPlayer).getOpponent().getID();
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	public Response computeQuery(String query) {
		String[] args = query.split(" ");
		Response response = Response.ErrorComputeQuery;
		
		int iCommand = 0;
		_idPlayer = Integer.parseInt(args[0]);
		Game.State state = _g.getState(_idPlayer);
		
		try {
			iCommand = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			return Response.ErrorInvalidCommand;
		}
		
		if (iCommand == Game.COMMAND_GET_CARD_INFO) {
			if (args.length != 3)
				response = Response.ErrorInvalidCommand;
			else {
				try {
					int iCard = Integer.parseInt(args[2]);
					return _g.requestCardInfo(_idPlayer, iCard);
				} catch (NumberFormatException e) {
					response = Response.ErrorInvalidCommand;
				}
			}
		}
		
		switch (state)
		{
		case WaitingForOpponent:
			response = Response.ErrorWaitingForOpponent;
			break;
		
		case Ready:
			/* ask for available actions player can perform with this card */
			if (iCommand == Game.COMMAND_GET_ACTIONS)
			{
				if (args.length != 3)
					response = Response.ErrorInvalidNumberOfArguments;
				else
				{
					try {
						int iCard = Integer.parseInt(args[2]);
						response = _g.requestActions(iCard);
					} catch (NumberFormatException e) {
						response = Response.ErrorInvalidCommand;
					}
				}
			}
			/* Cast a spell, activate an activated ability or take a special action */
			else if (iCommand == Game.COMMAND_PERFORM_ACTION) {
				if (args.length == 4)
				{
					try {
						int idCard = Integer.parseInt(args[2]);
						int idAction = Integer.parseInt(args[3]);
						response = _g.performAction(idCard, idAction);
					} catch (NumberFormatException e) {
						response = Response.ErrorInvalidCommand;
					}
				}
				else
				{
					response = Response.ErrorInvalidNumberOfArguments;
				}
			}
			/* pass priority */
			else if (iCommand == Game.COMMAND_PASS_PRIORITY)
			{
				response = _g.passPriority(_idPlayer);
			}
			/* toggle debug mode */
			else if (iCommand == Game.COMMAND_TOGGLE_DEBUG_MODE) {
				response = _g.toggleDebugMode();
			}
			else
			{
				response = Response.ErrorInvalidCommand;
			}
			break;
			
		case PromptDoYouWantToUntap:
			switch(iCommand) {
			
			// Answer to the question : Do you want to untap <card> ?
			case Game.COMMAND_YES:
				_g.setOptionalUntap(UntapOptional.State.Untap);
				break;
				
			case Game.COMMAND_NO:
				_g.setOptionalUntap(UntapOptional.State.LeaveTapped);
				break;
				
			default:
				break;
			}
			break;
			
		case PromptMulligan:
			switch(iCommand) {
			
			// Answer to the question : Do you want to keep this hand ?
			case Game.COMMAND_YES:
				_g.setKeepHand(_idPlayer, true);
				break;
				
			case Game.COMMAND_NO:
				_g.setKeepHand(_idPlayer, false);
				break;
				
			default:
				break;
			}
			response = _g.doMulligans();
			if (response == Response.Mulliganing_B)
				response = _g.doMulligans();
			break;
			
		case PromptCatastrophe_PermanentType:
		case WaitChoiceTurnabout:
		case WaitChoiceHarnessedLightning:
		case WaitChooseColor:
			response = _g.assignIntegerResolution(iCommand);
			break;
			
		case WaitChooseManaCombination:
			_g.assignManaChoice(iCommand);
			response = Response.OKholdPriority;
			break;

		case WaitChooseTriggeredManaAbilityColor:
			response = _g.assignTriggeredManaAbilityColor(iCommand);
			break;
			
		case PromptChooseColorStaticETB:
			response = _g.assignColorStaticETB(Color.intToColor(iCommand));
			break;
			
		case PromptPayLifeStaticETB:
			response = _g.assignIntegerStaticETB(iCommand);
			break;

		case PromptXValue:
			response = _g.assignXValue(iCommand);
			break;
			
		case PromptMode:
			response = _g.assignMode(iCommand);
			break;
			
		case PromptDoYouWantToDrawACard:
		case PromptDoYouWantPutTheCardInYourHand:
		case PromptDoYouWantPutInGraveyard:
		case PromptTurnabout_doYouWantToTap:
		case PromptDoYouWantToUseTheAbility:
		case PromptDoYouWantToSacrificeALand:
		case PromptDoYouWantToShuffle:
		case PromptCastWithoutPayingManaCost:
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
		case PromptPayPunishingFire:
		case PromptPayUpkeepCost:
		case PromptPayEchoCost:
		case PromptPayCumulativeUpkeep:
			if (iCommand == Game.COMMAND_YES)
				response = _g.setAnswer(Answer.Yes);
			else if (iCommand == Game.COMMAND_NO)
				response = _g.setAnswer(Answer.No);
			else
				response = Response.ErrorInvalidCommand;
			break;
			
		case WaitDeclareAttackers:
			if (iCommand == Game.COMMAND_DONE)
				response = _g.validateDeclareAttackers();
			else
				response = _g.toggleDeclareAttackers(iCommand);
			break;
			
		case WaitReorderBlockers:
			if (iCommand == Game.COMMAND_DONE)
				response = _g.validateBlockersOrder();
			else
				response = _g.reorderBlocker(iCommand);
			break;
			
		case WaitDeclareBlockers:
			if (iCommand == Game.COMMAND_DONE) // client pressed "Done"
				response = _g.validateDeclareBlockers();
			else
				response = _g.toggleDeclareBlockers(iCommand);
			break;
		
		case WaitChooseCreatureToBlock:
			if (iCommand == Game.COMMAND_NO) // client pressed "Cancel" = cancel all blocker decisions
				_g.resetBlockersStep();
			else
				response = _g.assignCreatureToBlock(iCommand);
			break;
			
		case WaitChooseRecipientToAttack:
			if (iCommand == Game.COMMAND_NO) // client pressed "Cancel" = cancel all attacking decisions
				_g.resetAttackersStep();
			else
				response = _g.assignRecipientToAttack(iCommand);
			break;
			
		case PromptDredge:
			if (_choices == null)
				_choices = new Vector<Integer>();
			if (iCommand != Game.COMMAND_DONE)
				_choices.add(iCommand);
			response = _g.assignDredgeResponse(_idPlayer, _choices);
			_choices = null;
			break;

		case WaitChooseReplaceDraw:
			if (_choices == null)
				_choices = new Vector<Integer>();
			_choices.add(iCommand);
			response = _g.assignReplaceDraw(_idPlayer, _choices);
			_choices = null;
			break;

			
		case PromptAbundance_CardType:
			if (iCommand == Game.COMMAND_YES)
				response = _g.assignAbundanceResponse(_idPlayer, Answer.Yes); // land
			else if (iCommand == Game.COMMAND_NO)
				response = _g.assignAbundanceResponse(_idPlayer, Answer.No); // nonland
			else
				response = Response.ErrorInvalidCommand;
			break;
	
		case PromptApplyReplaceDraw:
			if (iCommand == Game.COMMAND_YES)
				response = _g.assignReplaceDrawResponse(_idPlayer, Answer.Yes);
			else if (iCommand == Game.COMMAND_NO)
				response = _g.assignReplaceDrawResponse(_idPlayer, Answer.No);
			else
				response = Response.ErrorInvalidCommand;
			break;
			
		case PromptPayForShockland:
			if (iCommand == Game.COMMAND_YES)
				response = _g.assignShocklandResponse(Answer.Yes);
			else if (iCommand == Game.COMMAND_NO)
				response = _g.assignShocklandResponse(Answer.No);
			else
				response = Response.ErrorInvalidCommand;
			break;
		
		case PromptRevealPlainsOrIsland:
		case PromptRevealIslandOrSwamp:
		case PromptRevealSwampOrMountain:
		case PromptRevealMountainOrForest:
		case PromptRevealForestOrPlains:
			if (_choices == null)
				_choices = new Vector<Integer>();
			if (iCommand != Game.COMMAND_DONE)
				_choices.add(iCommand);
			response = _g.assignRevealLandResponse(_idPlayer, _choices);
			_choices = null;
			break;
			
		case WaitChoicePutInHand:
		case WaitChoicePutBottomLib:
		case WaitChoicePutTopLib:
			if (_choices == null)
				_choices = new Vector<Integer>();
			_choices.add(iCommand);
			response = _g.setChoice(_idPlayer, _choices);
			_choices = null;
			break;
			
		case PromptHost:
			response = _g.assignHost(iCommand);
			break;
			
		case PromptTargets:
		case PromptTargetsOrDone:
			int nbTarget = _g.getNbTarget();
			if (_choices == null)
				_choices = new Vector<Integer>();
			_choices.add(iCommand);
			_iTarget++;
			if ((_iTarget == nbTarget) || (iCommand == Game.COMMAND_DONE)) {
				response = _g.assignTarget(_choices);
				_choices = null;
				_iTarget = 0;
			}
			else
				response = Response.OK;
			break;
			
		case WaitChoiceMoxDiamond:
		case WaitChoiceChordOfCalling:
		case WaitChoiceCitanulFlute:
		case WaitChoiceGreenSunZenith:
		case WaitChoiceArtifactOrCreatureCard:
		case WaitChoiceLandOrCreatureCard:
		case WaitChoiceGoblinCard:
		case WaitChoiceArtifactOrEnchantmentCard:
		case WaitChoiceEnchantment:
		case WaitChoiceEnchantmentCardWithCCM3orLess:
		case WaitChoiceCreatureCardWithToughness2orLess:
		case WaitChoiceInstantOrSorceryCard:
		case WaitChoiceCreatureCard:
		case WaitChoiceRemembrance:
		case WaitChoiceStoneforgeMystic_search:
		case WaitChoiceStoneforgeMystic_put:
		case WaitChoiceSneakAttack:
		case WaitChoiceAcademyResearchers:
		case WaitChoiceCopperGnomes:
		case WaitChoiceGoblinLackey:
		case WaitChoiceAetherVial_put:
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
		case WaitChoiceCollectedCompany:
		case WaitChoiceOathOfNissa:
		case WaitChoicePutCreatureCardInHand:
		case WaitchoiceCardInGraveyard:
		case WaitChoiceScryPutBottom:
		case WaitChoiceLookTop:
		case LookPlayersHand:
		case WaitChoiceDuress:
		case WaitChoiceThoughtseize:
		case WaitChoiceReprocess:
		case WaitChoiceShowAndTell:
		case WaitUntapLand:
			_choices = new Vector<Integer>();
			
			/* do not add if entry is -1 */
			if (iCommand != Game.COMMAND_DONE)					
				_choices.add(iCommand);
			response = _g.setChoice(_idPlayer, _choices);
			_choices = null;
			break;
			
		case WaitChoiceDaze:
		case WaitChoiceForceOfWill:
			_choices = new Vector<Integer>();
			_choices.add(iCommand);
			response = _g.setAlternateCostChoice(_idPlayer, _choices);
			_choices = null;
			break;
			
		case WaitChoiceTutor:
		case WaitChoiceExhume:
		case WaitChoiceCreature:
		case WaitChoicePurgingScythe:
		case WaitChoiceEnchantmentAlteration:
		case WaitSacrificeCreature:
		case WaitSacrificeCreatureOrLand:
		case WaitSacrificeCreatureOrPlaneswalker:
		case WaitSacrificeLand:
		case WaitSacrificeEnchantment:
		case WaitSacrificePermanent:
		case WaitReturnPermanent: 
		case WaitDiscard:
		case WaitBrainstorm:
		case WaitExileForIchorid:
		case WaitChoiceDamageSource:
		case WaitChoiceDamageArtifactSource:
		case WaitChoiceDamageLandSource:
		case WaitChoiceDamageWhiteSource:
		case WaitChoiceDamageBlueSource:
		case WaitChoiceDamageBlackSource:
		case WaitChoiceDamageRedSource:
		case WaitChoiceDamageGreenSource:
		case WaitChoiceCoercion:
			_choices = new Vector<Integer>();
			_choices.add(iCommand);
			response = _g.setChoice(_idPlayer, _choices);
			_choices = null;
			break;

		case WaitPayCostDiscardACreatureCard:
		case WaitPayCostDiscardACard:
		case WaitPayCostSacrificeCreature:
		case WaitPayCostSacrificeGoblin:
		case WaitPayCostSacrificeAnotherVampireOrZombie:
		case WaitPayCostSacrificeArtifact:
		case WaitPayCostSacrificeLand:
		case WaitPayCostSacrificePermanent:
		case WaitPayCostSacrificeEnchantment:
		case WaitPayCostSacrificeForestOrPlains:
		case WaitPayCostSacrificeForest:
		case WaitPayCostReturnLand:
		case WaitHeartOfKiran:
		case WaitPayCostReturnElf:
		case WaitPayCostTapUntappedCreature:
		case WaitPayCostCrew:
		case WaitPayCostExileAnotherCreatureCardFromGyd:
			_choices = new Vector<Integer>();
			_choices.add(iCommand);
			response = _g.setChoicePayCosts(_idPlayer, _choices);
			_choices = null;
			break;
			
		case WaitDiscardEOT:
			response = _g.discardEOT(iCommand);
			break;
			
		default:
			break;
		}
		return response;
	}
	
	/**
	 * 
	 * @param response
	 * @return
	 */
	public String computeResponse(Response response) {
		String output = "";
		State state = _g.getState(_idPlayer);
		
		Vector<String> strings = new Vector<String>();
		strings.add(response.toString());
		output += Packet.print("Response", strings);
		strings.clear();
		strings.add(String.format("%d", _idPlayer));
		output += Packet.print("ID", strings);
		// send data relative to both players
		if (_bFirstResponse) {
			output += Packet.print("Players", _g.dumpPlayers());
			_bFirstResponse = false;
		}

		// when a player requests Card Info
		if ((_g.getRequestCardInfo_player() != -1) && (_idPlayer == _g.getRequestCardInfo_player())) {
			output += Packet.print("CardInfo", _g.dumpCardInfo());
		}
		
		output += Packet.print("Stack", _g.dumpStack());
		output += Packet.print("Status", _g.dumpStatus(_idPlayer));
		
		// send data relative to this player
		output += Packet.print("Hand#" + _idPlayer, _g.dumpHandforPlayer(_idPlayer));
		output += Packet.print("Graveyard#" + _idPlayer, _g.dumpGraveyardforPlayer(_idPlayer));
		output += Packet.print("Exile#" + _idPlayer, _g.dumpExileforPlayer(_idPlayer));
		output += Packet.print("Command#" + _idPlayer, _g.dumpCommandForPlayer(_idPlayer));
		output += Packet.print("Lands#" + _idPlayer, _g.dumpPermanentsforPlayer(_idPlayer, PermanentCategory.Lands)); 
		output += Packet.print("Creatures#" + _idPlayer, _g.dumpPermanentsforPlayer(_idPlayer, PermanentCategory.Creatures));
		output += Packet.print("Other#" + _idPlayer, _g.dumpPermanentsforPlayer(_idPlayer, PermanentCategory.Other));
		
		// send data relative to opponent
		output += Packet.print("Graveyard#" + _idOpponent, _g.dumpGraveyardforPlayer(_idOpponent));
		output += Packet.print("Exile#" + _idOpponent, _g.dumpExileforPlayer(_idOpponent));
		output += Packet.print("Command#" + _idOpponent, _g.dumpCommandForPlayer(_idOpponent));
		output += Packet.print("Lands#" + _idOpponent, _g.dumpPermanentsforPlayer(_idOpponent, PermanentCategory.Lands)); 
		output += Packet.print("Creatures#" + _idOpponent, _g.dumpPermanentsforPlayer(_idOpponent, PermanentCategory.Creatures));
		output += Packet.print("Other#" + _idOpponent, _g.dumpPermanentsforPlayer(_idOpponent, PermanentCategory.Other));
		
		// if it's the combat phase, send info related to attacking creatures
		if (_g.getPhase() == Phase.Combat)
		{
			output += Packet.print("Attacking", _g.dumpDeclaredAttackers());
			output += Packet.print("Blocking", _g.dumpDeclaredBlocks());
		}
		
		// counters must be computed after other data
		if (!_g.dumpCounters().isEmpty())
			output += Packet.print("Counters", _g.dumpCounters());
		
		switch (state)
		{
		case PromptApplyReplaceDraw:
		case PromptPayForShockland:
		case PromptDoYouWantToUseTheAbility:
		case PromptDoYouWantToDrawACard:
		case PromptCastWithoutPayingManaCost:
		case PromptDoYouWantToSacrificeALand:
		case PromptDoYouWantToShuffle:
		case WaitDiscardEOT:
		case WaitDiscard:
		case WaitBrainstorm:
		case WaitingForOpponent:
		case PromptMulligan:
		case PromptXValue:
		case PromptPayLifeStaticETB:
		case PromptChooseColorStaticETB:
		case PromptMode:
		case WaitChoiceTurnabout:
		case WaitChoiceHarnessedLightning:
		case WaitChooseColor:
		case WaitChooseTriggeredManaAbilityColor:
		case PromptCatastrophe_PermanentType:
		case PromptAbundance_CardType:
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
		case PromptPayPunishingFire:
		case PromptPayUpkeepCost:
		case PromptPayEchoCost:
		case PromptPayCumulativeUpkeep:
			break;

		case WaitChooseManaCombination:
			output += Packet.print("ManaChoices", _g.dumpManaChoices());
			break;
			
		case Ready:
			if (response == Response.PrintActions)
				output += Packet.print("Actions", _g.dumpActions());
			break;

		/* Candidates in zones other than the library */
		case WaitExileForIchorid:
		case WaitPayCostCrew:
		case WaitPayCostTapUntappedCreature:
		case WaitPayCostSacrificeCreature:
		case WaitPayCostSacrificeGoblin:
		case WaitPayCostSacrificeAnotherVampireOrZombie:
		case WaitPayCostSacrificeArtifact:
		case WaitPayCostSacrificePermanent:
		case WaitPayCostSacrificeEnchantment:
		case WaitSacrificeCreature:
		case WaitSacrificeCreatureOrLand:
		case WaitSacrificeCreatureOrPlaneswalker:
		case WaitSacrificeEnchantment:
		case WaitSacrificeLand:
		case WaitUntapLand:
		case WaitSacrificePermanent:
		case WaitReturnPermanent:
		case WaitPayCostSacrificeLand:
		case WaitPayCostSacrificeForestOrPlains:
		case WaitPayCostSacrificeForest:
		case WaitPayCostReturnLand:
		case WaitPayCostReturnElf:
		case WaitChoiceStoneforgeMystic_put:
		case WaitChoiceSneakAttack:
		case WaitChoiceCopperGnomes:
		case WaitChoiceGoblinLackey:
		case WaitChoiceAcademyResearchers:
		case WaitChoiceAetherVial_put:
		case WaitChoiceExhume:
		case WaitChoiceCreature:
		case WaitChoicePurgingScythe:
		case WaitChoiceEnchantmentAlteration:
		case WaitChoiceMoxDiamond:
		case PromptDoYouWantToUntap:
		case PromptTurnabout_doYouWantToTap:
		case PromptDredge:
		case PromptRevealPlainsOrIsland:
		case PromptRevealIslandOrSwamp:
		case PromptRevealSwampOrMountain:
		case PromptRevealMountainOrForest:
		case PromptRevealForestOrPlains:
		case WaitChooseReplaceDraw:
		case WaitPayCostDiscardACreatureCard:
		case WaitPayCostDiscardACard:
		case WaitChoiceDamageSource:
		case WaitChoiceDamageArtifactSource:
		case WaitChoiceDamageLandSource:
		case WaitChoiceDamageWhiteSource:
		case WaitChoiceDamageBlueSource:
		case WaitChoiceDamageBlackSource:
		case WaitChoiceDamageRedSource:
		case WaitChoiceDamageGreenSource:
		case WaitchoiceCardInGraveyard:
		case WaitChoiceReprocess:
		case WaitChoiceShowAndTell:
		case WaitChoiceForceOfWill:
		case WaitChoiceDaze:
		case WaitHeartOfKiran:
		case WaitPayCostExileAnotherCreatureCardFromGyd:
			output += Packet.print("Highlight", HighlightCandidates.dump(_g, _idPlayer, state));
			break;
		
		/* Candidates in the top X cards of the library */
		case WaitChoiceCollectedCompany:
		case WaitChoicePutCreatureCardInHand:
		case WaitChoiceOathOfNissa:
			output += Packet.print("LibManip", _g.dumpLibraryManipulation(_idPlayer));
			output += Packet.print("Highlight", HighlightCandidates.dump(_g, _idPlayer, state));
			break;

		/* Candidates in the entire library */
		case WaitChoiceIslandSwamp:
		case WaitChoiceSylvanScrying:
		case WaitChoicePlainsIsland:
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
		case WaitChoiceStoneforgeMystic_search:
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
			output += Packet.print("Library", _g.dumpLibraryForPlayer(_idPlayer));
			output += Packet.print("Highlight", HighlightCandidates.dump(_g, _idPlayer, state));
			break;
			
		case LookPlayersHand:
			output += Packet.print("OpponentHand", _g.dumpOpponentHandforPlayer(_idPlayer));
			break;
			
		case WaitChoiceCoercion:
		case WaitChoiceDuress:
		case WaitChoiceThoughtseize:
			output += Packet.print("OpponentHand", _g.dumpOpponentHandforPlayer(_idPlayer));
			output += Packet.print("Highlight", HighlightCandidates.dump(_g, _idPlayer, state));
			break;
			
		/* No highlight for these states because the player can pick any card */
		case WaitChoiceScryPutBottom:
		case WaitChoicePutInHand:
		case WaitChoicePutBottomLib:
		case WaitChoicePutTopLib:
		case PromptDoYouWantPutTheCardInYourHand:
		case PromptDoYouWantPutInGraveyard:
		case WaitChoiceLookTop:
			output += Packet.print("LibManip", _g.dumpLibraryManipulation(_idPlayer));
			break;

		case PromptHost:
			output += Packet.print("Highlight", _g.dumpAvailableHosts());
			break;
			
		case PromptTargets:
		case PromptTargetsOrDone:
			output += Packet.print("Highlight", _g.dumpAvailableTargets(_iTarget));
			break;
			
		case WaitChoiceTutor:
			output += Packet.print("Library", _g.dumpLibraryForPlayer(_idPlayer));
			break;

		case WaitDeclareAttackers:
			if (_g.dumpAvailableAttackers().size() > 0)
				output += Packet.print("Attackers", _g.dumpAvailableAttackers());
			break;
			
		case WaitDeclareBlockers:
			if (_g.dumpAvailableBlockers().size() > 0)
				output += Packet.print("Blockers", _g.dumpAvailableBlockers());
			break;
			
		case WaitChooseCreatureToBlock:
			if (_g.dumpAvailableCreaturesToBlock().size() > 0)
				output += Packet.print("Blockables", _g.dumpAvailableCreaturesToBlock());
			break;
			
		case WaitChooseRecipientToAttack:
			if (_g.dumpAvailableRecipientsToAttack().size() > 0)
				output += Packet.print("Attackables", _g.dumpAvailableRecipientsToAttack());
			break;
		
		case WaitReorderBlockers:
			if (_g.dumpBlockersOrder().size() > 0) {
				output += Packet.print("BlockOrder", _g.dumpBlockersOrder());
				output += Packet.print("Blocks", _g.dumpDeclaredBlocks());
			}
			break;
			
		default:
			output = "Error in Interpreter.computeResponse()";
			break;
		}
		output += "\r\n";
		return output;
	}
}
