package mrpnsim.application.reachabilityAsp;

public class Token {
	String id = "aaaa";
	char type = '_';

	public Token(String id) {
		this.id = id;
	}

	public Token(String id, char c) {
		this.id = id;
		this.type = c;
	}

	public boolean equals(Token t) {
		return this.id.equals(t.id) && this.type == t.type;
	}

	public Token(Token t) {
		this.id = t.id.substring(0);
		this.type = t.type;
	}

	public String getId() {
		return id;
	}

	public boolean hasType() {
		return this.type != '_';
	}

	public void setId(String id) {
		this.id = id.substring(0);
	}

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = type;
	}

	public String toString() {
		String s = "";
		if (this != null)
			s += "Id: " + this.getId() + " ";
		if (this.hasType())
			s += "Type: " + this.getType();
		return s;
	}
}
