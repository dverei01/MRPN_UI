package mrpnsim.application.properties;

import mrpnsim.application.model.Transition;
import mrpnsim.application.simulator.Execution;

public class Edge {

	public Node source;
	public Node destination;
	public Transition t;
	public Execution ex;
	
	public Edge(Node source, Node destination, 
			Transition t, Execution ex) {
		super();
		this.source = source;
		this.destination = destination;
		this.t = t;
		this.ex = ex;
	}
	
	
}





