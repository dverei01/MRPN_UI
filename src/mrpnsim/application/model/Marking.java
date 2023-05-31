package mrpnsim.application.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Marking {

	protected MRPN mrpn;
	protected HashMap<Token, Place> tokenIntoPlace = new HashMap<Token, Place>();
	
	protected HashMap<Token, Set<Token>> tokenConnections = new HashMap<Token, Set<Token>>();
	
	protected HashMap<Transition, Integer> history = new HashMap<Transition, Integer>();
	
	
	/*
	public Marking(HashMap<String, String> tokenIntoPlace, HashMap<String, Set<String>> tokenConnections) {
		this.tokenIntoPlace = new HashMap<Token, Place>(tokenIntoPlace);
		this.tokenConnections = new HashMap<String, Set<String>>(tokenConnections);	
	}
	*/
	public Marking(MRPN mrpn) {
		this.mrpn = mrpn;
	}
	public Marking( Marking original ) {
		
		this.mrpn = original.mrpn;
		tokenIntoPlace = new HashMap<Token, Place>(original.tokenIntoPlace);
		tokenConnections = new HashMap<Token, Set<Token>>();
		for (Map.Entry<Token, Set<Token>> entrySet : original.tokenConnections.entrySet()) {
			
			Token token = entrySet.getKey();
			Set<Token> oldConnections = entrySet.getValue();
			Set<Token> newConnections = new HashSet<Token>(oldConnections);
			tokenConnections.put(token, newConnections);
		}
		
		history = new HashMap<Transition, Integer>();
		for (Map.Entry<Transition, Integer> entrySet : original.history.entrySet()) {
			
			Transition transition = entrySet.getKey();
			Integer oldNum = entrySet.getValue();
			history.put(transition, oldNum);
		}
	}
	
	public Marking( MRPN mrpn, HashMap<String, String> inputTokensPlace, HashMap<String, Set<String>> inputTokenConnections ) {
		
		this.mrpn = mrpn;
		
		// Add our tokens into a place
		for (Map.Entry<String, String> entrySet : inputTokensPlace.entrySet()) {
			
			String tokenName = entrySet.getKey();
			String placeName = entrySet.getValue();
			
			Token token = mrpn.getToken(tokenName);
			Place place = mrpn.getPlace(placeName);
			
			if( token != null && place != null ) 
				setTokenPlace(token,place);
		}
		
		// Create our token bonds
		for (Map.Entry<String, Set<String>> entrySet : inputTokenConnections.entrySet()) {
			
			String tokenName = entrySet.getKey();
			Set<String> tokenSetNames = entrySet.getValue();
			
			Token token = mrpn.getToken(tokenName);
			if( token != null ) {
				for( String connectionName : tokenSetNames ) {
					Token connection = mrpn.getToken(connectionName);
					if(connection !=null) 
						setTokenConnection(token,connection);
				}
			}
			
		}
		
	}
	
	public ArrayList<Token> getTokens(Place p) {
		ArrayList<Token> tokenArray = new ArrayList<Token>();
		for (Entry<Token, Place> entrySet : tokenIntoPlace.entrySet()) 
			if(entrySet.getValue().getName().equals(p.getName()))
				tokenArray.add(entrySet.getKey());	
		
		return tokenArray;
	}
	
	public boolean equals(Object obj) {
		if( this == obj )
			return true;
		
		if(!(obj instanceof Marking) ) 
			return false;
		
		Marking other = (Marking)obj;
		if( !this.subsetOf(other))
			return false;
		if( !other.subsetOf(this) )
			return false;
		
		return true;
	}
	
	protected String markingToString() {
		StringBuilder sb = new StringBuilder();
		
		Token[] tokens = mrpn.getTokens();
		Arrays.sort( tokens );
		
		for( Token token : tokens ) {
			Place place = tokenIntoPlace.get(token);
			sb.append(token.name);
			sb.append("->");
			sb.append(place.name);
			sb.append(",");
		}
		
		for( Token token : tokens ) {
			Set<Token> connections = tokenConnections.get(token);
			if(connections == null)
				continue;
			
			Token[] connectionArray = new Token[connections.size()];
			connections.toArray(connectionArray);
			Arrays.sort(connectionArray);
			
			for( Token other : connectionArray ) {
				sb.append(token.name);
				sb.append("-");
				sb.append(other.name);
				sb.append(",");
			}
		}
		
		return sb.toString();
	}
	
	protected String historyToString() {
		StringBuilder sb = new StringBuilder();
		
		for (Map.Entry<Transition, Integer> entrySet : history.entrySet()) {
		
			Transition transition = entrySet.getKey();
			Integer transitionHistory = entrySet.getValue();
			
			sb.append(transition.name);
			sb.append(transitionHistory);
		}
		
		return sb.toString();
	}
	
	public String toString() {
		return markingToString() + "," + historyToString();
	}
	
	public int hashCode() {
		String hashString = markingToString();
		return hashString.hashCode();
	}
	
	public void removeToken(Token token) {
		
		// Remove all token references
		tokenIntoPlace.remove(token);
	
		// Remove all token connections
		{
			tokenConnections.remove(token);
			Set<Token> keySet = tokenConnections.keySet();
			Token[] keyArray = new Token[keySet.size()];
			keySet.toArray(keyArray);
			for( Token key : keyArray ) {
				Set<Token> connections = tokenConnections.get(key);
				connections.remove(token);
				if( connections.isEmpty() ) 
					tokenConnections.remove(key);
			}
		}

		
	}
	
	public void removeTransition(Transition transition) {
		history.remove(transition);
	}
	
	public void setTokenPlace(Token token, Place place) {
		tokenIntoPlace.put(token, place);
	}
	
	public void setTokenConnection(Token A, Token B) {
		if( tokenConnections.containsKey(A) ) {
			Set<Token> tokenSet = tokenConnections.get(A);
			tokenSet.add(B);
		}
		else {
			Set<Token> tokenSet = new HashSet<Token>();
			tokenSet.add(B);
			tokenConnections.put(A,tokenSet);
		}
		
		// Do the other direction as well
		if( tokenConnections.containsKey(B) ) {
			Set<Token> tokenSet = tokenConnections.get(B);
			tokenSet.add(A);
		}
		else {
			Set<Token> tokenSet = new HashSet<Token>();
			tokenSet.add(A);
			tokenConnections.put(B,tokenSet);
		}
		
	}
	

	
	public void unsetTokenConnection(Token A, Token B) {
		
		if( tokenConnections.containsKey(A) ) {
			Set<Token> tokenSet = tokenConnections.get(A);
			if(tokenSet.contains(B))
				tokenSet.remove(B);
			if(tokenSet.isEmpty())
				tokenConnections.remove(A);
		}

		if( tokenConnections.containsKey(B) ) {
			Set<Token> tokenSet = tokenConnections.get(B);
			if(tokenSet.contains(A))
				tokenSet.remove(A);
			if(tokenSet.isEmpty())
				tokenConnections.remove(B);
		}
		
	}
	
	public boolean subsetOf(Marking otherMarking) {
		for (Map.Entry<Token, Place> entry : tokenIntoPlace.entrySet()) {
			if(otherMarking.tokenIntoPlace.containsKey(entry.getKey())) {
				Place otherPlace = otherMarking.tokenIntoPlace.get(entry.getKey());
				if(!entry.getValue().equals(otherPlace)) 
					return false;
			}
			else {
				return false;
			}
		}
		
		for(Map.Entry<Token, Set<Token>> entry : tokenConnections.entrySet()) {
			Token key = entry.getKey();
			Set<Token> value = entry.getValue();
			
			if(otherMarking.tokenConnections.containsKey(key)) {
				Set<Token> otherValue = otherMarking.tokenConnections.get(key);
				for(Token token: value) {
					if(!otherValue.contains(token))
						return false;
				}
			}
			else return false;
		}
		return true;
	}
	
}
