package mrpnsim.application.simulator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javafx.util.Pair;
import mrpnsim.application.model.Arrow;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.model.Token;
import mrpnsim.application.model.Transition;


public class Execution {
	
	protected MRPN mrpn;
	protected ArrayList<Pair<Transition,ArrayList<ArrayList<Token>>>> enabledTransitions;
	protected ArrayList<Pair<Transition,ArrayList<ArrayList<Token>>>> enabledReverseTransitions;
	public Execution(MRPN mrpn) {
		this.mrpn = mrpn;
		enabledTransitions = new ArrayList<>();
		enabledReverseTransitions = new ArrayList<>();
		
	}
	
	public void setMRPN(MRPN mrpn) {
		this.mrpn = mrpn;
	}
	

}
