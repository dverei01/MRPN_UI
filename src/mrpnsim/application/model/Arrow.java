package mrpnsim.application.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javafx.util.Pair;

public class Arrow {

	Node source;
	Node destination;

	Set<LabelItem> items = new HashSet<LabelItem>();

	public Arrow(Node source, Node destination) {
		this.source = source;
		this.destination = destination;
	}
	
	public Node getSource() {
		return source;
	}
	
	public Node getDestination() {
		return destination;
	}
	
	public Set<LabelItem> getSet() {
		return items;
	}

	public void addToken(String type, String id) {
		// tokens.add(token);
		items.add(new LabelItem(type, id, true));
	}
	
	// gia arrows apo transition se place
	public void addToken(String name) {
		// tokens.add(token);
		items.add(new LabelItem(name));
	}

	public void addBond(String A, String B) {
		// bonds.add( new Pair<Token,Token>(A,B) );
		items.add(new LabelItem(A, B));
	}

	public void addBond(Pair<String, String> bond) {
		// bonds.add( new Pair<Token,Token>(A,B) );
		items.add(new LabelItem(bond.getKey(), bond.getValue()));
	}
	
	public ArrayList<LabelItem> getTokens() {
		ArrayList<LabelItem> tokens = new ArrayList<LabelItem>();
		for (LabelItem item : items) {
			if (item.isToken())
				tokens.add(item);
		}
		return tokens;
	}
	
	
	public ArrayList<LabelItem> getBonds() {
		ArrayList<LabelItem> bonds = new ArrayList<LabelItem>();
		for (LabelItem item : items) {
			if (item.isBond())
				bonds.add(item);
		}
		return bonds;
	}

}
