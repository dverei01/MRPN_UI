package mrpnsim.application.model;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mrpnsim.application.ScrollArea;

public class MRPN {

	public ScrollArea scrollArea;

	Marking initialMarking = new Marking(this);
	Marking marking = new Marking(initialMarking);

	Map<String, Place> places = new HashMap<>();
	Map<String, Transition> transitions = new HashMap<>();

	Map<String, Token> tokens = new HashMap<>();
	Map<Node, Set<Arrow>> nodeArrows = new HashMap<Node, Set<Arrow>>();
	
	ArrayList<String> types = new ArrayList<>();

	boolean firstToken = false;
	protected int[] counters = new int[5];

	

	
	public MRPN() {
		for (int i = 0; i < counters.length; i++)
			counters[i] = 1;
	}
	public MRPN(ScrollArea scrollArea) {
		
		this.scrollArea = scrollArea;
		for (int i = 0; i < counters.length; i++)
			counters[i] = 1;
	}
	
	
	public ScrollArea getScrollArea() {
		return scrollArea;
	}
	
	public Marking getMarking() {
		return marking;
	}

	// MRPN edit mode methods
	protected <T> String findName(String prefix, int index, Map<String, T> map) {
		String name = prefix + counters[index];
		counters[index]++;

		while (map.containsKey(name)) {
			name = prefix + counters[index];
			counters[index]++;
		}

		return name;
	}

	public Place addPlace() {

		String name = findName("p", 0, places);

		Place place = new Place(this, name);
		places.put(name, place);

		return place;
	}

	
	public boolean hasNode( String name ) {
		Place place = places.get(name);
		Transition transition = transitions.get(name);
		if( place == null && transition == null )
			return false;
		return true;
	}
	
	public boolean hasToken( String name ) {
		Token token = tokens.get(name);
		if( token == null )
			return false;
		return true;
	}
	
	public boolean hasType( String type ) {
		
		return types.contains(type);
	}
	
	public boolean hasBond( String source, String destination ) {
		Token A = tokens.get(source);
		Token B = tokens.get(destination);
		
		if( A==null || B==null ) 
			return false;
		
		if(!marking.tokenConnections.containsKey(A))
			return false;
		
		Set<Token> connections = marking.tokenConnections.get(A);
		if( !connections.contains(B) )
			return false;
		
		return true;
	}
	
	public Place getPlace(String name) {
		Place place = places.get(name);
		return place;
	}

	public Transition getTransition(String name) {
		Transition transition = transitions.get(name);
		return transition;
	}

	
	public void removePlace(Place place) {

		places.remove(place.name);

		place.disconnectArrows();
		
		Token[] tokens = place.getTokens();
		for( Token token : tokens ) {
			removeToken(token);
		}

	}

	public void removeTransition(Transition transition) {

		transitions.remove(transition.name);

		transition.disconnectArrows();
		
		initialMarking.removeTransition(transition);
		marking = new Marking(initialMarking);
	}

	public Token addToken(Place place) {

		String name = findName("b", 1, tokens);

		Token token = new Token(this, place, name, "A");
		if(!firstToken) {
			firstToken = true;
			types.add("A");
		}
			
		tokens.put(name, token);

		initialMarking.setTokenPlace(token, place);
		marking = new Marking(initialMarking);

		return token;
	}
	
	public Token addToken(Place place,String name) {

		Token token = new Token(this, place, name, "A");
		if(!firstToken) {
			firstToken = true;
			types.add("A");
		}
			
		tokens.put(name, token);

		initialMarking.setTokenPlace(token, place);
		marking = new Marking(initialMarking);

		return token;
	}
	
	public Token addToken(Place place,String name, String type) {

		Token token = new Token(this, place, name, type);
		if(!firstToken) {
			firstToken = true;
			types.add("A");
		}
			
		tokens.put(name, token);

		initialMarking.setTokenPlace(token, place);
		marking = new Marking(initialMarking);

		return token;
	}
	

	public Token getToken(String name) {
		Token token = tokens.get(name);
		return token;
	}

	public void removeToken(Token token) {
		if (tokens.containsKey(token.name)) {
			tokens.remove(token.name);

			initialMarking.removeToken(token);
			marking = new Marking(initialMarking);
		}
	}

	public Transition addTransition() {

		String name = findName("t", 2, transitions);

		Transition transition = new Transition(this, name);
		transitions.put(name, transition);

		initialMarking.history.put(transition, 0);
		marking = new Marking(initialMarking);
		
		return transition;
	}
	
	public Transition addTransition(String name) {
		Transition transition = new Transition(this, name);
		transitions.put(name, transition);
		return transition;
	}

	public Arrow addArrow(Node source, Node destination) {
		Arrow arrow;

		if (!nodeArrows.containsKey(source)) {
			Set<Arrow> arrowSet = new HashSet<Arrow>();
			arrow = new Arrow(source, destination);
			arrowSet.add(arrow);
			nodeArrows.put(source, arrowSet);
		} else {
			Set<Arrow> arrowSet = nodeArrows.get(source);
			arrow = new Arrow(source, destination);
			arrowSet.add(arrow);
		}
		
		if (!nodeArrows.containsKey(destination)) {
			Set<Arrow> arrowSet = new HashSet<Arrow>();
			arrowSet.add(arrow);
			nodeArrows.put(destination, arrowSet);
		} else {
			Set<Arrow> arrowSet = nodeArrows.get(destination);
			arrowSet.add(arrow);
		}

		return arrow;
	}

	public Set<Arrow> getNodeArrows(Node node) {
		if (nodeArrows.containsKey(node)) 
			return nodeArrows.get(node);
		return null;
	}
	
	public void removeArrow(Node source, Node destination) {

		if (nodeArrows.containsKey(source)) {

			Set<Arrow> arrowSet = nodeArrows.get(source);

			Arrow[] arrowArray = new Arrow[arrowSet.size()];
			arrowSet.toArray(arrowArray);
			for( Arrow arrow : arrowArray ) 
				if( arrow.destination == destination ) {
					arrowSet.remove(arrow);
				}
			
			if (arrowSet.isEmpty())
				nodeArrows.remove(source);

		}
		
		if (nodeArrows.containsKey(destination)) {

			Set<Arrow> arrowSet = nodeArrows.get(destination);

			Arrow[] arrowArray = new Arrow[arrowSet.size()];
			arrowSet.toArray(arrowArray);
			for( Arrow arrow : arrowArray ) 
				if( arrow.source == source && arrow.destination == destination ) {
					arrowSet.remove(arrow);
			}
			
			if (arrowSet.isEmpty())
				nodeArrows.remove(destination);

		}
	}

	// MRPN getter functions
	public Transition[] getTransitions() {
		Transition[] transitionArray = new Transition[transitions.size()];
		int count = 0;
		for (Map.Entry<String, Transition> entrySet : transitions.entrySet()) {
			transitionArray[count] = entrySet.getValue();
			count++;
		}
		return transitionArray;
	}

	public Place[] getPlaces() {
		Place[] placeArray = new Place[places.size()];
		int count = 0;
		for (Map.Entry<String, Place> entrySet : places.entrySet()) {
			placeArray[count] = entrySet.getValue();
			count++;
		}
		return placeArray;
	}

	public Bond[] getBonds() {

		ArrayList<Bond> bondList = new ArrayList<Bond>();

		for (Map.Entry<Token, Set<Token>> entrySet : marking.tokenConnections.entrySet()) {

			Token tokenA = entrySet.getKey();
			Set<Token> tokenSet = entrySet.getValue();
			for (Token tokenB : tokenSet) {
				bondList.add(new Bond(tokenA, tokenB));
			}
		}

		Bond[] bonds = new Bond[bondList.size()];
		bondList.toArray(bonds);
		return bonds;
	}

	public Token[] getTokens() {
		Token[] tokenArray = new Token[tokens.size()];
		int count = 0;
		for (Map.Entry<String, Token> entrySet : tokens.entrySet()) {
			tokenArray[count] = entrySet.getValue();
			count++;
		}
		return tokenArray;
	}
	
	public String[] getTypes() {
		String[] typeArray = new String[types.size()];
		int count = 0;
		for (String type:types) {
			typeArray[count] = type;
			count++;
		}
		return typeArray;
	}

	// MRPN simulation mode methods

	public void moveToken(Place target, Token token) {

		Place previousPlace = token.getPlace();
		if (previousPlace == target)
			return;

		marking.setTokenPlace(token, target);

		// prepei na metaferthoun kai ta tokens pou einai syndemena me to token
		Token[] connectedTokens = token.getConnected();
		for (Token connectedToken : connectedTokens) {
			moveToken(target, connectedToken);
		}

	}


	// MUST NOT BE CALLED DURING SIMULATION!!
	public void addBondFromMRPN(Token A, Token B) {

		initialMarking.setTokenConnection(A, B);
		marking = new Marking(initialMarking);

	}

	public void removeBondFromMRPN(Token A, Token B) {
		initialMarking.unsetTokenConnection(A, B);
		marking = new Marking(initialMarking);

	}

	
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("places: {");
		Place[] places = getPlaces();
		for( Place place : places )
			sb.append(place + ", ");
		sb.append("}\n");
		
		sb.append("transitions: {");
		Transition[] transitions = getTransitions();
		for( Transition transition : transitions )
			sb.append(transition + ", ");
		sb.append("}\n");	
		
		sb.append("tokens: {\n");
		Token[] tokens = getTokens();
		for( Token token : tokens ) {
			
			sb.append("  "+token + " in " + token.getPlace() + ",\n");
			
		}
		sb.append("}\n");	
		
		
		sb.append("bonds : {\n");
		Bond[] bonds = getBonds();
		for( Bond bond : bonds ) {
			sb.append("  "+bond.A + " - " + bond.B + ",\n");
		}
		sb.append("}\n");	
		
		sb.append("arcs : {\n");
		for(Place place : places ) {
			Arrow[] arrows = place.getArrows();
			for( Arrow arrow : arrows ) {
				sb.append("  "+arrow.source + " -> " + arrow.destination + " {");
				sb.append(arrow.items.toString());
				sb.append(" },\n");
			}
			
		}
		for(Transition transition : transitions ) {
			Arrow[] arrows = transition.getArrows();
			for( Arrow arrow : arrows ) {
				sb.append("  "+arrow.source + " -> " + arrow.destination + " {");
				sb.append(arrow.items.toString());
				sb.append(" },\n");
			}
			
		}
		sb.append("}\n");	
		
		return sb.toString();
	}
	
	
	public void toFile(String filename) {
		
		try {
			PrintWriter pw = new PrintWriter(filename);
			String data = this.toString();
			pw.print(data);
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public void setMarking(Marking newMarking) {
		marking = newMarking;
		
	}
	
}
