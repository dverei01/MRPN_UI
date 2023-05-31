package mrpnsim.application.model;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Place extends Node {

	
	public Place(MRPN mrpn, String name) {
		super(mrpn,name);
	}

	private Set<Token> getTokenSet(){
		Marking marking = mrpn.marking;
		
		Set<Token> tokenSet  = new HashSet<Token>();
		for( Map.Entry<Token,Place> entrySet : marking.tokenIntoPlace.entrySet()) {
			Place place = entrySet.getValue();
			Token token = entrySet.getKey();
			if( this == place ) 
				tokenSet.add(token);
		}
		
		return tokenSet;
	}
	
	public boolean containsToken(Token token) {
		Set<Token> tokenSet  = getTokenSet();
		return tokenSet.contains(token);
	}
	
	public boolean containsTokens(ArrayList<LabelItem> tokenList) {
		Token[] tokenSet  = getTokens();
		boolean[] tokenFlag = new boolean[tokenSet.length];
		for(int i = 0; i < tokenSet.length; i++)
			tokenFlag[i] = true;
		
		for(int i = 0; i < tokenList.size(); i++) {
			boolean flag = false;
			for(int j = 0; j < tokenSet.length; j++) 
				if(tokenFlag[j] && tokenList.get(i).getTokenType().equals(tokenSet[j].getType())) {
					tokenFlag[j] = false;
					flag = true;
					break;
				}
			if(!flag)
				return false;
		}
		return true;
	}
	
	public boolean containsTokens(ArrayList<LabelItem> tokenList, Map<String, String> types) {
		Token[] tokenSet  = getTokens();
		boolean[] tokenFlag = new boolean[tokenSet.length];
		for(int i = 0; i < tokenSet.length; i++)
			tokenFlag[i] = true;
		
		for(int i = 0; i < tokenList.size(); i++) {
			boolean flag = false;
			for(int j = 0; j < tokenSet.length; j++) 
				if(tokenFlag[j] && types.get(tokenList.get(i).getTokenName()).equals(tokenSet[j].getType())) {
					tokenFlag[j] = false;
					flag = true;
					break;
				}
			if(!flag)
				return false;
		}
		return true;
	}
	
	public Token[] getTokens(){

		Set<Token> tokenSet  = getTokenSet();
		Token[] tokens = new Token[tokenSet.size()];
		tokenSet.toArray(tokens);
		return tokens;
	}
	
	@Override
	public boolean setName(String newName) {
		
		String oldName = this.name;
		
		if(!super.setName(newName))
			return false;
		
		// Fix hashmap
		mrpn.places.remove(oldName);
		mrpn.places.put(newName, this);
		
		return true;
	}
	
	public void moveToken(Token token) {
		mrpn.moveToken(this, token);
	}
	
}
