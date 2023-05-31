package mrpnsim.application.properties;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import mrpnsim.application.model.Marking;
import mrpnsim.application.model.Transition;
import mrpnsim.application.simulator.ForwardExecution;
import mrpnsim.application.simulator.ReverseExecution;

public class Node {

	public Marking marking;
	public Set<Edge> children = new HashSet<Edge>();
	public Edge fromParent;
	public int depth;
	
	public Node(Marking marking, int depth) {
		this.marking = marking;
		this.depth = depth;
	}

	@Override
	public int hashCode() {
		return marking.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return marking.equals(obj);
	}
	
	public String toString() {
		return marking.toString();
	}
	
}


