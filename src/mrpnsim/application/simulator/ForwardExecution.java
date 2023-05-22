package mrpnsim.application.simulator;

import java.util.ArrayList;
import java.util.Set;
import javafx.util.Pair;
import mrpnsim.application.model.Arrow;
import mrpnsim.application.model.Bond;
import mrpnsim.application.model.LabelItem;
import mrpnsim.application.model.Place;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.model.Token;
import mrpnsim.application.model.Transition;

public class ForwardExecution extends Execution {

	private ArrayList<ArrayList<String>> labels = new ArrayList<ArrayList<String>>();
	MRPNUtils utils;

	public ForwardExecution(MRPN mrpn) {
		super(mrpn);
		utils = new MRPNUtils(mrpn);
	}

	private boolean areMarked(ArrayList<LabelItem> tokenList, ArrayList<LabelItem> bondList, Place place,
			ArrayList<ArrayList<Token>> matching, ArrayList<String> label) {
		if (!place.containsTokens(tokenList))
			return false;

		ArrayList<String> labelNames = new ArrayList<String>();
		ArrayList<String> types = new ArrayList<String>();
		ArrayList<ArrayList<String>> bonds = new ArrayList<ArrayList<String>>();
		ArrayList<String> tokenNames = new ArrayList<String>();

		for (int i = 0; i < tokenList.size(); i++) {
			labelNames.add(tokenList.get(i).getTokenName());
			label.add(tokenList.get(i).getTokenName());
			types.add(tokenList.get(i).getTokenType());
			bonds.add(new ArrayList<String>());
			for (LabelItem bond : bondList)
				if (bond.getTokenName().equals(labelNames.get(i)))
					bonds.get(i).add(bond.getTokenNameB());
				else if (bond.getTokenNameB().equals(labelNames.get(i)))
					bonds.get(i).add(bond.getTokenName());
		}
		
		return areMarked(labelNames, types, bonds, tokenNames, place, matching, 0);
	}

	private boolean areMarked(ArrayList<String> labelNames, ArrayList<String> types, ArrayList<ArrayList<String>> bonds,
			ArrayList<String> tokenNames, Place place, ArrayList<ArrayList<Token>> matching, int i) {
		if (labelNames.size() == i) {
			ArrayList<Token> match = new ArrayList<>();
			//System.out.println("Matches");
			for (String token : tokenNames) {
				match.add(mrpn.getToken(token));
				
				//System.out.print(mrpn.getToken(token).getType() +":"+token+":"+mrpn.getToken(token).getName()+"\t");
			}
			//System.out.println();
			matching.add(match);
			return true;
		}

		Token[] tokensIntoPlace = place.getTokens();
		boolean flag1 = false;
		ArrayList<Token> selected = new ArrayList<>();
		for (Token token : tokensIntoPlace) {
			boolean flag2 = true;
			if (token.getType().equals(types.get(i)) && !tokenNames.contains(token.getName())) {
				for (String labelBond : bonds.get(i)) {
					int j = labelNames.indexOf(labelBond);
					if (tokenNames.size() > j) {
						flag2 = false;
						if (mrpn.hasBond(token.getName(), tokenNames.get(j)))
							flag2 = true;
						if (!flag2)
							break;
					}
				}
				if (flag2) {
					ArrayList<String> labelBonds = bonds.get(i);
					for (int j = 0; j < tokenNames.size(); j++)
						if (mrpn.hasBond(token.getName(), tokenNames.get(j))
								&& !labelBonds.contains(labelNames.get(j))) {
							flag2 = false;
							break;
						}
					if (flag2) {
						for (int j = 0; j < selected.size(); j++)
							if (utils.tokenComparison(token, selected.get(j))) {
								flag2 = false;
								break;
							}
					}
					if (flag2) {
						tokenNames.add(token.getName());
						selected.add(token);
						if (areMarked(labelNames, types, bonds, tokenNames, place, matching, i + 1))
							flag1 = true;
						tokenNames.remove(tokenNames.size() - 1);
					}

				}
			}
		}
		return flag1;
	}

	private ArrayList<Arrow> getOutgoingArrows(Transition transition) {
		ArrayList<Arrow> outgoing_arrows = new ArrayList<>();
		Arrow[] arrows = transition.getArrows();
		for (Arrow arrow : arrows) {
			if (arrow.getSource() == transition) {
				outgoing_arrows.add(arrow);
			}
		}
		return outgoing_arrows;
	}

	public ArrayList<Pair<Transition, ArrayList<ArrayList<Token>>>> getEnabledTransitions() {
		enabledTransitions.clear();
		labels.clear();

		for (Transition transition : mrpn.getTransitions()) {
			boolean isEnabled = true;
			ArrayList<ArrayList<Token>> matching = new ArrayList<>();
			Arrow[] arrows = transition.getArrows();
			ArrayList<String> label = new ArrayList<>();
			for (Arrow arrow : arrows) {
				if (arrow.getDestination() == transition && arrow.getTokens().size() != 0) {
					// incoming arrow

					ArrayList<ArrayList<Token>> tempMatching = new ArrayList<>();
					Place place = (Place) arrow.getSource();
					ArrayList<LabelItem> tokens = arrow.getTokens();
					ArrayList<LabelItem> bonds = arrow.getBonds();

					if (!areMarked(tokens, bonds, place, tempMatching, label)) {
						isEnabled = false;
						break;
					}

					ArrayList<ArrayList<Token>> matching2 = new ArrayList<>();
					for (int i = 0; i < matching.size(); i++) {
						for (int j = 0; j < tempMatching.size(); j++) {
							ArrayList<Token> temp = (ArrayList<Token>) matching.get(i).clone();
							temp.addAll(tempMatching.get(j));
							matching2.add(temp);
						}
					}
					if (matching.size() == 0)
						matching = tempMatching;
					else
						matching = matching2;

				}
			}
			if (isEnabled) {
				Pair<Transition, ArrayList<ArrayList<Token>>> allMatchings = new Pair<>(transition, matching);
				enabledTransitions.add(allMatchings);
				labels.add(label);
			}
		}

		return enabledTransitions;
	}

	/**
	 * Gia kathe outgoing arrow tou transition metakinw ta antistoixa tokens kai
	 * bonds pou yparxoun sto outgoing arrow sto antistoixo outgoing place. Akomi
	 * e3etazw kata poso ypirxe proigoumenws o desmos pou anagrafetai sto outgoing
	 * arrow sta antistoixa tokens kai an den ypirxe tote prosthetw bond sta tokens.
	 * Telos, enimerwnw tin history list tou transition pou pirwdwtithike.
	 * 
	 * @param t:     to transition pou tha ektelestei
	 * @param count: o arithmos pou tha prosthethei stin history list tou transition
	 *               pou deixnei me poia seira ektelestike to transition
	 */
	public void fireTransition(Transition t, int num) {
		ArrayList<Token> tokenList = null;
		int c = 0;
		for (Pair<Transition, ArrayList<ArrayList<Token>>> enabled : enabledTransitions) {
			if (enabled.getKey().getName().equals(t.getName())) {
				tokenList = enabled.getValue().get(num - 1);
				break;
			}
			c++;
		}
		ArrayList<String> label = labels.get(c);
		
		ArrayList<Arrow> outgoingArrows = getOutgoingArrows(t);
		removeDeletedBonds(t, tokenList, label);

		for (Arrow outgoingArrow : outgoingArrows) {
			Set<LabelItem> outgoingLabel = outgoingArrow.getSet();
			Place outgoingPlace = (Place) outgoingArrow.getDestination();

			for (LabelItem item : outgoingLabel) {
				if (item.isToken()) {
					String token = item.getTokenName();
					outgoingPlace.moveToken(tokenList.get(label.indexOf(token)));
				}

				if (item.isBond()) {
					Pair<String, String> bond = item.getBond();
					String tokenA = bond.getKey();
					String tokenB = bond.getValue();
					outgoingPlace.moveToken(tokenList.get(label.indexOf(tokenA)));
					outgoingPlace.moveToken(tokenList.get(label.indexOf(tokenB)));

					Boolean isExistBond = false;
					for (Bond existedBond : mrpn.getBonds()) {

						if (existedBond.getSource() == tokenList.get(label.indexOf(tokenA))
								&& existedBond.getDestination() == tokenList.get(label.indexOf(tokenB))) {
							isExistBond = true;
							break;
						}
						if (existedBond.getSource() == tokenList.get(label.indexOf(tokenB))
								&& existedBond.getDestination() == tokenList.get(label.indexOf(tokenA))) {
							isExistBond = true;
							break;
						}

					}

					if (!isExistBond) {
						tokenList.get(label.indexOf(tokenA)).createBond(tokenList.get(label.indexOf(tokenB)));
					}
				}
			}
		}

		t.addToHistory();

		// t.updateHistory();

	}

	private void removeDeletedBonds(Transition t, ArrayList<Token> tokenList, ArrayList<String> label) {
		Arrow[] arrows = t.getArrows();
		ArrayList<Arrow> outgoingArrows = getOutgoingArrows(t);
		for (Arrow arrow : arrows) {
			if (arrow.getDestination() == t && arrow.getTokens().size() != 0) {
				ArrayList<LabelItem> bonds = arrow.getBonds();
				for (LabelItem bond : bonds) {
					boolean flag = false;
					for (Arrow outgoingArrow : outgoingArrows) {
						Set<LabelItem> outgoingLabel = outgoingArrow.getSet();
						for (LabelItem item : outgoingLabel) {
							if (item.isBond()) {
								if (item.getTokenName().equals(bond.getTokenName())
										&& item.getTokenNameB().equals(bond.getTokenNameB()))
									flag = true;
								else if (item.getTokenName().equals(bond.getTokenNameB())
										&& item.getTokenNameB().equals(bond.getTokenName()))
									flag = true;
							}
							if (flag)
								break;
						}
						if (flag)
							break;
					}
					if (!flag)
						tokenList.get(label.indexOf(bond.getTokenName()))
								.removeBond(tokenList.get(label.indexOf(bond.getTokenNameB())));
				}

			}
		}
	}

}
