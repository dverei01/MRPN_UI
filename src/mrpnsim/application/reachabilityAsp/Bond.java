package mrpnsim.application.reachabilityAsp;

import java.util.ArrayList;

public class Bond {

	protected Token A = null;
	protected Token B = null;

	public Bond(Token A, Token B) {
		this.A = A;
		this.B = B;
	}

	public Token getA() {
		return A;
	}

	public Token getB() {
		return B;
	}

	public ArrayList<Token> getTokens() {
		ArrayList<Token> tokens = new ArrayList<Token>();
		if (A != null)
			tokens.add(A);
		if (B != null)
			tokens.add(B);

		return tokens;
	}

	public String toString() {
		return A.toString() + "<-> " + B.toString();

	}

	public boolean equals(Bond b) {
		return this.A.equals(b.A) && this.B.equals(b.B);
	}
	
}
