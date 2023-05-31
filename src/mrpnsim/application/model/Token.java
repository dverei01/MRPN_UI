package mrpnsim.application.model;

import java.util.HashSet;
import java.util.Set;

public class Token implements Comparable {

	public String name;
	public String type;
	public MRPN mrpn;
	public final Place initial_place;

	public Token(MRPN mrpn, Place initialPlace, String name, String type) {
		this.name = name;
		this.type = type;
		this.initial_place = initialPlace;
		this.mrpn = mrpn;
	}

	public Place getPlace() {
		Marking marking = mrpn.marking;
		Place place = marking.tokenIntoPlace.get(this);
		return place;
	}

	/**
	 * Find connected tokens recursively (for tokens that are not directly connected)
	 * 
	 * @param connected
	 */
	protected void getConnected(Set<Token> connected) {

		if (connected.contains(this))
			return;

		connected.add(this);

		Marking marking = mrpn.marking;
		Set<Token> connectedTokens = marking.tokenConnections.get(this);
		if( connectedTokens == null )
			return;
		
		for (Token connectedToken : connectedTokens)
			connectedToken.getConnected(connected);

	}

	public Token[] getConnected() {
		Set<Token> connected = new HashSet<Token>();
		this.getConnected(connected);
		
		Token[] tokenArray = new Token[connected.size()];
		connected.toArray(tokenArray);
		return tokenArray;
	}

	protected boolean isConnected(Set<Token> visited, Token other) {

		visited.add(this);

		Marking marking = mrpn.marking;
		Set<Token> connectedTokens = marking.tokenConnections.get(this);

		if( connectedTokens == null )
			return false;
		
		// Termatiki periptwsi ena.
		// Afou mesa sta connected tokens yparxei ekeino poy psaxnoume
		if (connectedTokens.contains(other)) {
			return true;
		}

		for (Token connectedToken : connectedTokens) {
			// Kane anadromi an den episkeptikes to sygkekrimeno token
			if (!visited.contains(connectedToken))
				return connectedToken.isConnected(visited, other);
		}

		return false;
	}

	public boolean isConnected(Token other) {
		Set<Token> visited = new HashSet<Token>();
		return isConnected(visited, other);
	}

	public void createBond(Token B) {

		mrpn.marking.setTokenConnection(this, B);

	}

	public void removeBond(Token B) {
		mrpn.marking.unsetTokenConnection(this, B);
	}

	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
		boolean flag = true;
		for(String t:mrpn.getTypes()) 
			if(t.equals(type))
				flag = false;
		if(flag)
			mrpn.types.add(type);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return type;
	}

	public Set<Token> getConnectedSet() {
		
		Set<Token> connected = new HashSet<Token>();
		this.getConnected(connected);
		
		return connected;
	}
	
    public Set<Token> getBonds() {
		
    	Marking marking = mrpn.marking;
		Set<Token> bonds = marking.tokenConnections.get(this);
		
		return bonds;
	}

	@Override
	public int compareTo(Object obj) {
		if( !(obj instanceof Token) )
			return 1;
		Token other = (Token) obj;
		return name.compareTo(other.name);
	}

}
