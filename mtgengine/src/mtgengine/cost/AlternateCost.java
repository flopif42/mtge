package mtgengine.cost;

import java.util.Vector;

import mtgengine.Game;
import mtgengine.Game.Response;
import mtgengine.Game.State;
import mtgengine.card.Card;
import mtgengine.card.Color;
import mtgengine.player.Player;
import mtgengine.type.Subtype;

public class AlternateCost {
	public enum DEFINITION {
		ForceOfWill,
		Daze,
		SnuffOut
	};
	
	private DEFINITION _def;
	private Card _card;
	private int _paymentStep = 1;
	private boolean _bPaymentOK = false;
	
	public AlternateCost(DEFINITION def, Card card) {
		_def = def;
		_card = card;
	}
	
	/**
	 * Returns true if the cost can be paid, false otherwise.
	 * @return
	 */
	public boolean canBePaid(Game g, Player p) {
		boolean bFound = false;
		switch (_def) {
		
		case ForceOfWill:
			// check player has at least one blue card in hand
			Vector<Card> cardsInHand = p.getHand().getCards();
			for (Card cardInHand : cardsInHand) {
				if (cardInHand.hasColor(Color.BLUE) && (cardInHand != _card)) {
					bFound = true;
					break;
				}
			}
			if (!bFound)
				return false;
			
			// check player has at least one life
			if (p.getLife() < 1)
				return false;
			break;
			
		case Daze:
			// check player controls at least one island
			Vector<Card> lands = g.getBattlefield().getLandsControlledBy(p);
			for (Card land : lands) {
				if (land.hasSubtypeGlobal(g, Subtype.ISLAND)) {
					bFound = true;
					break;
				}
			}
			if (!bFound)
				return false;
			
			break;
		case SnuffOut:
			// check player controls a swamp
			lands = g.getBattlefield().getLandsControlledBy(p);
			for (Card land : lands) {
				if (land.hasSubtypeGlobal(g, Subtype.SWAMP)) {
					bFound = true;
					break;
				}
			}
			if (!bFound)
				return false;
			
			// check player has at least 4 life
			if (p.getLife() < 4)
				return false;
			
			break;
			
		default:
			break;
		
		}
		return true;
	}

	/**
	 * Pay for the alternate cost.
	 * @param g
	 * @param controller
	 * @return
	 */
	public Response pay(Game g, Player controller) {
		switch (_def) {
		case ForceOfWill:
			if (_paymentStep == 1) {
				_paymentStep++;
				controller.setState(State.WaitChoiceForceOfWill);
				return Response.MoreStep;
			}
			
			if (_paymentStep == 2) {
				Card exiledCard = (Card) g.getChoices().get(0);
				g.move_HND_to_EXL(exiledCard);
				controller.loseLife(1);
				_bPaymentOK = true;
				return Response.OK;
			}
			
			break;
			
		case Daze:
			if (_paymentStep == 1) {
				_paymentStep++;
				controller.setState(State.WaitChoiceDaze);
				return Response.MoreStep;
			}
			
			if (_paymentStep == 2) {
				Card island = (Card) g.getChoices().get(0);
				g.move_BFD_to_HND(island);
				_bPaymentOK = true;
				return Response.OK;
			}
			
			break;
		
		case SnuffOut:
			controller.loseLife(4);
			_bPaymentOK = true;
			return Response.OK;
			
		default:
			System.out.println("TODO : write the paying code for " + _def.toString());
			break;
		}
		return Response.Error;
	}
	
	public boolean wasPaid() {
		return _bPaymentOK;
	}
}
