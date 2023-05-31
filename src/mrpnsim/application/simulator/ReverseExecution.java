package mrpnsim.application.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javafx.scene.control.ListView;
import javafx.util.Pair;
import mrpnsim.application.model.Arrow;
import mrpnsim.application.model.Bond;
import mrpnsim.application.model.LabelItem;
import mrpnsim.application.model.Node;
import mrpnsim.application.model.Place;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.model.Token;
import mrpnsim.application.model.Transition;

public class ReverseExecution extends Execution {

	private ArrayList<ArrayList<String>> labels = new ArrayList<ArrayList<String>>();
	MRPNUtils utils;

	public ReverseExecution(MRPN mrpn) {
		super(mrpn);
		utils = new MRPNUtils(mrpn);
	}

	private boolean areMarked(ArrayList<LabelItem> tokenList, ArrayList<LabelItem> bondList, Transition t, Place place,
			ArrayList<ArrayList<Token>> matching, ArrayList<String> labelArray) {
		Set<LabelItem> incoming = utils.pre(t);
		Map<String, String> types = new HashMap<String, String>();
		for (LabelItem label : incoming)
			if (label.isToken())
				types.put(label.getTokenName(), label.getTokenType());

		if (!place.containsTokens(tokenList, types))
			return false;

		ArrayList<String> labelNames = new ArrayList<String>();
		ArrayList<ArrayList<String>> bonds = new ArrayList<ArrayList<String>>();
		ArrayList<String> tokenNames = new ArrayList<String>();

		for (int i = 0; i < bondList.size(); i++) {
			LabelItem bond = bondList.get(i);
			if (!labelNames.contains(bond.getTokenName())) {
				labelNames.add(bond.getTokenName());
				labelArray.add(bond.getTokenName());
				bonds.add(new ArrayList<String>());
			}
			bonds.get(labelNames.indexOf(bond.getTokenName())).add(bond.getTokenNameB());
			if (!labelNames.contains(bond.getTokenNameB())) {
				labelNames.add(bond.getTokenNameB());
				labelArray.add(bond.getTokenNameB());
				bonds.add(new ArrayList<String>());
			}
			bonds.get(labelNames.indexOf(bond.getTokenNameB())).add(bond.getTokenName());
		}

		for (int i = 0; i < tokenList.size(); i++) {
			if (!labelNames.contains(tokenList.get(i).getTokenName())) {
				labelNames.add(tokenList.get(i).getTokenName());
				labelArray.add(tokenList.get(i).getTokenName());
				bonds.add(new ArrayList<String>());
			}
		}

		return areMarked(labelNames, types, bonds, tokenNames, place, matching, 0);
	}

	private boolean areMarked(ArrayList<String> labelNames, Map<String, String> types,
			ArrayList<ArrayList<String>> bonds, ArrayList<String> tokenNames, Place place,
			ArrayList<ArrayList<Token>> matching, int i) {
		if (labelNames.size() == i) {
			ArrayList<Token> match = new ArrayList<>();
			for (String token : tokenNames)
				match.add(mrpn.getToken(token));

			matching.add(match);
			return true;
		}

		Token[] tokensIntoPlace = place.getTokens();
		boolean flag1 = false;
		ArrayList<Token> selected = new ArrayList<>();
		for (Token token : tokensIntoPlace) {
			boolean flag2 = true;
			if (token.getType().equals(types.get(labelNames.get(i))) && !tokenNames.contains(token.getName())) {
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

	public ArrayList<Pair<Transition, ArrayList<ArrayList<Token>>>> getEnabledTransitions() {
		enabledReverseTransitions.clear();
		labels.clear();

		for (Transition transition : mrpn.getTransitions()) {
			if (transition.getHistory() == 0)
				continue;
			boolean isEnabled = true;
			ArrayList<ArrayList<Token>> matching = new ArrayList<>();
			Arrow[] arrows = transition.getArrows();
			ArrayList<String> label = new ArrayList<>();
			for (Arrow arrow : arrows) {
				if (arrow.getSource() == transition && (arrow.getTokens().size() != 0 || arrow.getBonds().size() != 0)) {
					// outgoing arrow

					ArrayList<ArrayList<Token>> tempMatching = new ArrayList<>();
					Place place = (Place) arrow.getDestination();
					ArrayList<LabelItem> tokens = arrow.getTokens();
					ArrayList<LabelItem> bonds = arrow.getBonds();
					
					if (!areMarked(tokens, bonds, transition, place, tempMatching, label)) {
						//System.out.println("Failed Definition 4.1");
						isEnabled = false;
						break;
					}

					ArrayList<ArrayList<Token>> matching2 = new ArrayList<>();
					for (int i = 0; i < matching.size(); i++) {
						for (int j = 0; j < tempMatching.size(); j++) {
							boolean flag = true;
							for (int z = 0; z < tempMatching.get(j).size(); z++)
								if (matching.get(i).contains(tempMatching.get(j).get(z))) {
									flag = false;
									break;
								}
							if (flag) {
								ArrayList<Token> temp = (ArrayList<Token>) matching.get(i).clone();
								temp.addAll(tempMatching.get(j));
								matching2.add(temp);
							}
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
				enabledReverseTransitions.add(allMatchings);
				labels.add(label);
			}
		}

		return enabledReverseTransitions;
	}

	protected void removeDeletedBonds(Transition t, ArrayList<Token> tokenList, ArrayList<String> label) {
		ArrayList<Arrow> incomingArrows = new ArrayList<>();
		ArrayList<Arrow> outgoingArrows = new ArrayList<>();
		for (Arrow arrow : t.getArrows()) {
			if (arrow.getSource() == t)
				outgoingArrows.add(arrow);
			if (arrow.getDestination() == t)
				incomingArrows.add(arrow);
		}

		// koita3e an dimiourgithike desmos me tin ektelesi tou transition
		ArrayList<LabelItem> bondsToDestroy = new ArrayList<>();
		MRPNUtils utils = new MRPNUtils(mrpn);
		for (Arrow outgoingArrow : outgoingArrows) {
			Set<LabelItem> outgoingLabel = outgoingArrow.getSet();

			Set<LabelItem> outgoingBonds = utils.getBondsFrom(outgoingLabel);
			for (LabelItem outgoingBond : outgoingBonds) {
				boolean bondHasExisted = false;
				for (Arrow incomingArrow : incomingArrows) {
					Set<LabelItem> incomingLabel = incomingArrow.getSet();
					Set<LabelItem> incomingBonds = utils.getBondsFrom(incomingLabel);
					for (LabelItem incomingBond : incomingBonds) {
						if (outgoingBond.getTokenName().equals(incomingBond.getTokenName())
								&& outgoingBond.getTokenNameB().equals(incomingBond.getTokenNameB())) {
							bondHasExisted = true;
							break;
						}
						if (outgoingBond.getTokenNameB().equals(incomingBond.getTokenName())
								&& outgoingBond.getTokenName().equals(incomingBond.getTokenNameB())) {
							bondHasExisted = true;
							break;
						}
					}
					if (bondHasExisted)
						break;
				}
				if (!bondHasExisted)
					bondsToDestroy.add(outgoingBond);
			}

		}

		// diagra4e tous desmous pou dimiourgithikan apo tin ektelesi tou transition
		for (LabelItem bond : bondsToDestroy) {
			Pair<String, String> pair = bond.getBond();
			String tokenA = pair.getKey();
			String tokenB = pair.getValue();
			for (Bond mrpnBond : mrpn.getBonds()) {
				Token source = mrpnBond.getSource();
				Token destination = mrpnBond.getDestination();
				if (tokenList.get(label.indexOf(tokenA)).getName().equals(source.getName())
						&& tokenList.get(label.indexOf(tokenB)).getName().equals(destination.getName())) {
					source.removeBond(destination);
					// tokenA.removeBond(mrpnBond);
					break;
				}
				if (tokenList.get(label.indexOf(tokenA)).getName().equals(destination.getName())
						&& tokenList.get(label.indexOf(tokenB)).getName().equals(source.getName())) {
					source.removeBond(destination);
					// tokenA.removeBond(mrpnBond);
					break;
				}

			}
		}
	}

	/**
	 * elegxw kata poso dimiourgithikan desmoi me tin forward ektelesi tou
	 * transition(diladi o desmos ypirxe mono sto outgoing arrow tou transition kai
	 * den ypirxe se kanena apo tan incoming arrows tou transition). An ontws
	 * dimiourgithikan desmoi me tin ektelesi tou transition tote tha prepei na tous
	 * katastre4w kai na metakinsw ola ta tokens/bonds pou yparxoun sta incoming
	 * arrows tou transition sta antistoixa incoming places.
	 * 
	 * @param t
	 */
	public void fireTransition(Transition t, int num) {
		ArrayList<Token> tokenList = null;
		int c = 0;
		for (Pair<Transition, ArrayList<ArrayList<Token>>> enabled : enabledReverseTransitions) {
			if (enabled.getKey().getName().equals(t.getName())) {
				tokenList = enabled.getValue().get(num - 1);
				break;
			}
			c++;
		}
		
		ArrayList<String> label = labels.get(c);
		removeDeletedBonds(t, tokenList,label);

		ArrayList<Arrow> incomingArrows = new ArrayList<>();
		for (Arrow arrow : t.getArrows()) {
			if (arrow.getDestination() == t)
				incomingArrows.add(arrow);
		}

		// metakina ta tokens pou briskontai sta incoming arrows sta antistoixa incoming
		// places
		for (Arrow incomingArrow : incomingArrows) {
			Set<LabelItem> incomingLabel = incomingArrow.getSet();
			Place incomingPlace = (Place) incomingArrow.getSource();

			for (LabelItem item : incomingLabel) {
				if (item.isToken()) {
					String token = item.getTokenName();
					incomingPlace.moveToken(tokenList.get(label.indexOf(token)));
				}
			}

			for (LabelItem item : incomingLabel) {
				if (item.isBond()) {
					Pair<String, String> bond = item.getBond();
					String tokenA = bond.getKey();
					String tokenB = bond.getValue();

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
		t.removeFromHistory();
	}

}
