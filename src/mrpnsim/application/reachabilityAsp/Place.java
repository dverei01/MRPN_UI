package mrpnsim.application.reachabilityAsp;

import java.util.ArrayList;

public class Place {
	String name;
	double x;
	double y;
	ArrayList<Token> tokens;

	public Place(String name, double x, double y) {
		this.name = name.substring(0);
		this.x = x;
		this.y = y;
		this.tokens = new ArrayList<Token>();
	}

	public ArrayList<Token> getTokens() {
		return tokens;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.substring(0);
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public String toString() {
		if (tokens.size() != 0) {
			String s = "Name: " + this.getName() + " x: " + this.getX() + " y: " + this.getY();
			for (int i = 0; i < tokens.size(); i++) {
				s += " " + tokens.get(i).toString();
			}
			return s;
		} else
			return "Name: " + this.getName() + " x: " + this.getX() + " y: " + this.getY();
	}

	public void addToken(Token t) {
		if (!(t == null)) {
			this.tokens.add(new Token(t.id, t.type));
		}
//		else
//			token = null;

	}

}