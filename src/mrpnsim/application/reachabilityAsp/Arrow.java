package mrpnsim.application.reachabilityAsp;


public class Arrow {
	Token source;
	Token destination;
	Label label;

	public Arrow(Token source, Token destination, Label label) {
		this.source = source;
		this.destination = destination;
		this.label = label;
	}
	public String toString() {
		return this.source.getId() + "->" + this.destination.getId() + " " + this.label.toString();
	}

	public Token getSource() {
		return source;
	}

	public void setSource(Token source) {
		this.source = source;
	}

	public Token getDestination() {
		return destination;
	}

	public void setDestination(Token destination) {
		this.destination = destination;
	}

	public Label getLabel() {
		return label;
	}

	public void setLabel(Label label) {
		this.label = label;
	}

}
