package mrpnsim.application.model;

public class Bond {

	protected Token A;
	protected Token B;
	
	public Bond(Token A, Token B) {
		this.A = A;
		this.B = B;
	}

	public Token getSource() {
		return A;
	}
	public Token getDestination() {
		return B;
	}
	
}
