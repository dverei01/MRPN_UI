package mrpnsim.application.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Transition extends Node {


	public Transition(MRPN mrpn, String name) {
		super(mrpn,name);
		
		
	}

	public void addToHistory(){
		 mrpn.marking.history.put(this,  mrpn.marking.history.get(this)+1);
	}
	
	public Integer getHistory(){
		 return mrpn.marking.history.get(this);
	}
	
	public void removeFromHistory(){
		 mrpn.marking.history.put(this,  mrpn.marking.history.get(this)-1);
	}
	
	
	@Override
	public boolean setName(String newName) {
		
		String oldName = this.name;
		
		if(!super.setName(newName))
			return false;
		
		// Fix hashmap
		mrpn.transitions.remove(oldName);
		mrpn.transitions.put(newName, this);
		
		return true;
	}
	
	
	
}
