package mrpnsim.application.reachabilityAsp;

import java.util.*;
import java.util.ArrayList;

public class Mrpn {
	List<Place> placelist = new ArrayList<Place>();
	List<Transition> transitionlist = new ArrayList<Transition>();
	List<Bond> bondsList = new ArrayList<Bond>();

	public Mrpn(List<Place> placelist, List<Transition> transitionlist, List<Bond> bondsList) {
		this.placelist = placelist;
		this.transitionlist = transitionlist;
		this.bondsList = bondsList;
	}

	public List<Place> getPlacelist() {
		return placelist;
	}

	public void setPlacelist(List<Place> placelist) {
		this.placelist = placelist;
	}

	public List<Transition> getTransitionlist() {
		return transitionlist;
	}

	public void setTransitionlist(List<Transition> transitionlist) {
		this.transitionlist = transitionlist;
	}

	public List<Bond> getBondsList() {
		return bondsList;
	}

	public void setBondsList(List<Bond> bondsList) {
		this.bondsList = bondsList;
	}

}
