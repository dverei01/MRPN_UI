package mrpnsim.application.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import javafx.util.Pair;
import mrpnsim.application.model.Place;
import mrpnsim.application.model.MRPN;

import java.util.Set;

public class SearchMarking {
	protected MRPN mrpn;
	protected HashMap<String, Place> tokenIntoPlace = new HashMap<String, Place>();
	protected HashMap<String, Set<Pair<String,Boolean>>> tokenConnections = new HashMap<String, Set<Pair<String,Boolean>>>();
	
	public SearchMarking( MRPN mrpn, HashMap<String, String> inputTokensPlace, HashMap<String, Set<Pair<String,Boolean>>> inputTokenConnections ) {
		
		this.mrpn = mrpn;
		
		for (Map.Entry<String, String> entrySet : inputTokensPlace.entrySet()) {
			
			String placeName = entrySet.getValue();
			Place place = mrpn.getPlace(placeName);
			
			if( place != null ) 
				tokenIntoPlace.put(entrySet.getKey(), place);
		}
		
		for (Entry<String, Set<Pair<String,Boolean>>> entrySet :inputTokenConnections.entrySet()) {
			
			String token = entrySet.getKey();
			Set<Pair<String,Boolean>> oldConnections = entrySet.getValue();
			Set<Pair<String,Boolean>> newConnections = new HashSet<Pair<String,Boolean>>(oldConnections);
			tokenConnections.put(token, newConnections);
		}
		
	}
	
	public ArrayList<String> getTokens() {
		ArrayList<String> tokenArray = new ArrayList<>();
		for (Entry<String, Place> entrySet : tokenIntoPlace.entrySet()) 
				tokenArray.add(entrySet.getKey());	
		
		return tokenArray;
	}
	
	public ArrayList<String> getTokens(Place p) {
		ArrayList<String> tokenArray = new ArrayList<>();
		for (Entry<String, Place> entrySet : tokenIntoPlace.entrySet()) 
			if(entrySet.getValue().getName().equals(p.getName()))
				tokenArray.add(entrySet.getKey());	
		
		return tokenArray;
	}
	
	public ArrayList<Place> getPlaces() {
		ArrayList<Place> placeArray = new ArrayList<>();
		for (Entry<String, Place> entrySet : tokenIntoPlace.entrySet()) 
			if(!placeArray.contains(entrySet.getValue()))
				placeArray.add(entrySet.getValue());	
		
		return placeArray;
	}
	
	public HashMap<String, Set<Pair<String,Boolean>>> getΤokenConnections() {
		return new HashMap<String, Set<Pair<String,Boolean>>>(tokenConnections);
	}
	
	public Set<Pair<String,Boolean>> getConnectedTokens(String token) {
		return tokenConnections.get(token);
	}
	
	
}
