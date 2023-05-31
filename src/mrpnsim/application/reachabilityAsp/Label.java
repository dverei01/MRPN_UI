package mrpnsim.application.reachabilityAsp;

import java.util.ArrayList;
import java.util.List;

public class Label {
	ArrayList<Token> tokenList = new ArrayList<Token>();
	ArrayList<Token> negTokenList = new ArrayList<Token>();
	ArrayList<Bond> bondList = new ArrayList<Bond>();
	ArrayList<Bond> negBondList = new ArrayList<Bond>();

//	public Label(ArrayList<Token> tokenList) {
//		this(tokenList, new ArrayList<Token>(), new ArrayList<Bond>(), new ArrayList<Bond>());
//	}

	public Label(ArrayList<Token> tokenList, ArrayList<Token> negTokenList, ArrayList<Bond> bondList,
			ArrayList<Bond> negBondList) {
		this.tokenList = tokenList;
		this.negTokenList = negTokenList;
		this.bondList = bondList;
		this.negBondList = negBondList;
	}
//	public boolean isExist(Bond b, ArrayList<Bond> bondList) {
//		
//		
//		
//	}

	public String toString() {
		String s = "Tokenlist: [ ";
		for (int i = 0; i < this.tokenList.size(); i++) {
			s += this.tokenList.get(i).toString() + " , ";
		}

		s += "] <-> Negative tokenlist: [ ";
		for (int i = 0; i < this.negTokenList.size(); i++) {
			s += this.negTokenList.get(i).toString() + " , ";
		}

		s += "] <-> Bondlist: [ ";
		for (int i = 0; i < this.bondList.size(); i++) {
			s += this.bondList.get(i).toString() + " , ";
		}
		s += "] <-> Negative bondlist: [ ";
		for (int i = 0; i < this.negBondList.size(); i++) {
			s += this.negBondList.get(i).toString() + " , ";
		}

		s += "]";

		return s;

	}

	public Label(ArrayList<Bond> bondList) {
		this(new ArrayList<Token>(), new ArrayList<Token>(), bondList, new ArrayList<Bond>());
	}
	public Label(ArrayList<Token> tokenList, ArrayList<Bond> bondList) {
		this(tokenList, new ArrayList<Token>(), bondList, new ArrayList<Bond>());
	}
	public ArrayList<Token> getTokenList() {
		return tokenList;
	}

	public void setTokenList(ArrayList<Token> tokenList) {
		this.tokenList = tokenList;
	}

	public ArrayList<Token> getNegTokenList() {
		return negTokenList;
	}

	public void setNegTokenList(ArrayList<Token> negTokenList) {
		this.negTokenList = negTokenList;
	}

	public ArrayList<Bond> getBondList() {
		return bondList;
	}

	public void setBondList(ArrayList<Bond> bondList) {
		this.bondList = bondList;
	}

	public ArrayList<Bond> getNegBondList() {
		return negBondList;
	}

	public void setNegBondList(ArrayList<Bond> negBondList) {
		this.negBondList = negBondList;
	}
}
