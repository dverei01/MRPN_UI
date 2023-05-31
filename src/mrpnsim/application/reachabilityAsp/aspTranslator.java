package mrpnsim.application.reachabilityAsp;

import java.util.ArrayList;

public class aspTranslator {
	ArrayList<Place> placeList = new ArrayList<Place>();
	ArrayList<Transition> transitionList = new ArrayList<Transition>();
	ArrayList<Arrow> arrowList = new ArrayList<Arrow>();
	ArrayList<Bond> totalBonds = new ArrayList<Bond>();
	protected ArrayList<String> code = new ArrayList<String>();

	public aspTranslator(ArrayList<Place> placeList, ArrayList<Transition> transitionList, ArrayList<Arrow> arrowList,
			ArrayList<Bond> totalBonds) {
		this.placeList = placeList;
		this.transitionList = transitionList;
		this.arrowList = arrowList;
		this.totalBonds = totalBonds;
	}

	public void produceTypes() {

		ArrayList<Token> tokens = new ArrayList<Token>();
		for (int i = 0; i < placeList.size(); i++) {
			for (int j = 0; j < placeList.get(i).getTokens().size(); j++)
				tokens.add(placeList.get(i).getTokens().get(j));
		}
		for (int i = 0; i < tokens.size(); i++) {
			code.add("type(" + tokens.get(i).getType() + "," + tokens.get(i).getId() + ").");
		}

		// code.add("\n");

	}

	private void producePtarc() {
		int i;
		ArrayList<Arrow> list = this.arrowList;

		for (i = 0; i < list.size(); i++) {
			// System.out.println(list.get(i).getSource().getId().charAt(0) + " " +
			// list.get(i).getSource().getId().charAt(0));

			if (list.get(i).getSource().getId().charAt(0) == 'p'
					&& list.get(i).getDestination().getId().charAt(0) == 't') {
				Label lab = list.get(i).getLabel();

				for (int j = 0; j < lab.getTokenList().size(); j++) {
					code.add("ptarc(" + list.get(i).getSource().getId() + "," + list.get(i).getDestination().getId()
							+ "," + lab.getTokenList().get(j).getId() + "," + lab.getTokenList().get(j).getType()
							+ ").");
					// }
				}
			}
		}
//		code.add("\n");

	}

	private void produceTparc() {
		int i;
		ArrayList<Arrow> list = this.arrowList;

		for (i = 0; i < list.size(); i++) {
//			System.out.println(
//					list.get(i).getSource().getId().charAt(0) + " " + list.get(i).getSource().getId().charAt(0));

			if (list.get(i).getSource().getId().charAt(0) == 't'
					&& list.get(i).getDestination().getId().charAt(0) == 'p') {
				Label lab = list.get(i).getLabel();

				for (int j = 0; j < lab.getTokenList().size(); j++) {

					code.add("tparc(" + list.get(i).getSource().getId() + "," + list.get(i).getDestination().getId()
							+ "," + lab.getTokenList().get(j).getId() + "," + lab.getTokenList().get(j).getType()
							+ ").");
					// }
				}
			}
		}
//		code.add("\n");
	}

	private void produceTparcb() {
		int i;
		ArrayList<Arrow> list = this.arrowList;

		for (i = 0; i < list.size(); i++) {
//			System.out.println(
//					list.get(i).getSource().getId().charAt(0) + " " + list.get(i).getSource().getId().charAt(0));

			if (list.get(i).getSource().getId().charAt(0) == 't'
					&& list.get(i).getDestination().getId().charAt(0) == 'p') {
				Label lab = list.get(i).getLabel();

				for (int j = 0; j < lab.getBondList().size(); j++) {
					ArrayList<Token> t = lab.getBondList().get(j).getTokens();
					Token temp = t.get(0);
					String codeToBeAdded = "tparcb(" + list.get(i).getSource().getId() + ","
							+ list.get(i).getDestination().getId(); 
					for (int z = 1; z < t.size(); z++) {
						codeToBeAdded += "," + temp.getId() + "," + t.get(z).getId() + ").";
						temp = t.get(z);
					}
					code.add(codeToBeAdded);
//					code.add("tparcb(" + list.get(i).getSource().getId() + "," + list.get(i).getDestination().getId()
//							+ "," + lab.getTokenList().get(j).getId() + "," + lab.getTokenList().get(j).getType()
//							+ ").");
//					// }
				}
			}
		}

	}

	private void producePtarcb() {
		int i;
		ArrayList<Arrow> list = this.arrowList;

		for (i = 0; i < list.size(); i++) {
//			System.out.println(
//					list.get(i).getSource().getId().charAt(0) + " " + list.get(i).getSource().getId().charAt(0));

			if (list.get(i).getSource().getId().charAt(0) == 'p'
					&& list.get(i).getDestination().getId().charAt(0) == 't') {
				Label lab = list.get(i).getLabel();

				for (int j = 0; j < lab.getBondList().size(); j++) {
					ArrayList<Token> t = lab.getBondList().get(j).getTokens();
					Token temp = t.get(0);
					String codeToBeAdded = "ptarcb(" + list.get(i).getSource().getId() + ","
							+ list.get(i).getDestination().getId();
					for (int z = 1; z < t.size(); z++) {
						codeToBeAdded += "," + temp.getId() + "," + t.get(z).getId() + ").";
						temp = t.get(z);
					}
					code.add(codeToBeAdded);
//					code.add("tparcb(" + list.get(i).getSource().getId() + "," + list.get(i).getDestination().getId()
//							+ "," + lab.getTokenList().get(j).getId() + "," + lab.getTokenList().get(j).getType()
//							+ ").");
//					// }
				}
			}
		}

	}

	public void produceHolds() {
		for (int i = 0; i < this.placeList.size(); i++) {
			ArrayList<Token> l = this.placeList.get(i).getTokens();
			for (int j = 0; j < l.size(); j++) {
				code.add("holds(" + this.placeList.get(i).getName() + "," + l.get(j).getId() + ",0).");
			}
		}
		// System.out.println(code);
//		code.add("\n");

	}

	public String getPlaceFromId(String id) {
		for (int i = 0; i < this.placeList.size(); i++)
			for (int j = 0; j < this.placeList.get(i).getTokens().size(); j++) {
				// System.out.println(this.placeList.get(i).getTokens().get(j).getId().replaceAll("\\s",
				// ""));
				// System.out.println(this.placeList.get(i).getTokens().get(j).getId().replaceAll("\\s",
				// ""));
				if (this.placeList.get(i).getTokens().get(j).getId().replaceAll("\\s", "").equals(id))
					return this.placeList.get(i).getName();
			}
		return "None";
	}

	public void produceHoldBonds() {
		String codeToAdd = "";
		for (int i = 0; i < this.totalBonds.size(); i++) {
			// System.out.println(this.totalBonds.get(i).getA().getId());
			String s = this.getPlaceFromId(this.totalBonds.get(i).getA().getId());
//			for (int j = 0; j < this.placeList.size(); j++) {
//				if(this.placeList.get(j).getName().equals(s))
			codeToAdd = "holdsbonds(" + s + "," + this.totalBonds.get(i).getA().getId() + ",";

			// codeToAdd = "holdbonds(" + this.placeList.get(i).getName() + "," +
			// this.totalBonds.get(i).getA() + ",";

			codeToAdd += this.totalBonds.get(i).getB().getId();
			codeToAdd += ",0).";
			System.out.println(codeToAdd);
			findAndReplace(this.totalBonds.get(i).getA());
			findAndReplace(this.totalBonds.get(i).getB());
			code.add(codeToAdd);
		}
	}

	private void findAndReplace(Token a) {
		for (int i = 0; i < code.size(); i++)
			if (code.get(i).contains("holds(") && code.get(i).contains(a.getId()))
				code.remove(i);
	}

	public String produceAsp() {
		System.out.println(this.totalBonds.size());
		this.produceHolds();
		this.produceTypes();
		this.producePtarc();
		this.produceTparc();
		this.produceTparcb();
		this.producePtarcb();
		this.produceHoldBonds();
		char temp = ' ';
		String result = "";
		for (int i = 0; i < code.size(); i++) {
			if (temp != code.get(i).charAt(0)) {
				result += '\n';
				temp = code.get(i).charAt(0);
			}
			result += code.get(i) + '\n';
			// System.out.println(code.get(i).replaceAll("\\s", ""));
		}
		return result;
	}
}
