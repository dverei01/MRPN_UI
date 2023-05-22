package mrpnsim.application.model;


import javafx.util.Pair;

public class LabelItem {

	private boolean token;
	private String  typeA;
	private String typeB;
	private String nameA;
	private String nameB;
	private int  idA;
	
	public boolean isToken() {
		return token;
	}
	public boolean isBond() {
		return !token;
	}
	public LabelItem(String A, String name, boolean flag) {
		this.token = true;
		this.typeA = A;
		this.nameA = name;
		int id = Integer.parseInt(name.substring(A.length()));
		this.idA = id;
	}	
	public LabelItem(String A) {
		this.token = true;
		this.nameA = A;
	}	
	public LabelItem(String A, String B) {
		this.token = false;
		this.nameA = A;
		this.nameB = B;
	}
	
	public String toString() {
		String temp ="";
		
		if(token)
			temp += typeA;
		if(!token)
			temp += "(" + typeA + "-" + typeB + ")";
		
		return temp;
	}
	
	public String getTokenType() {
		return typeA;
	}
	
	public String getTokenName() {
		return nameA;
	}
	public String getTokenNameB() {
		return nameB;
	}
	
	public int getIdA() {
		return idA;
	}
	
	public Pair<String,String> getBond(){
		return new Pair<String,String>(nameA,nameB);
	}
	
	public boolean hasType( String other ) {
		if( typeA == other )
			return true;
		if( typeB == other )
			return true;
		return false;
	}
	
	@Override
	public boolean equals(Object other) {
		
		if( other instanceof LabelItem ) {
			LabelItem otherLabel = (LabelItem)other;
			if( otherLabel.token != this.token)
				return false;
			
			if( otherLabel.token ) {
				return otherLabel.nameA.equals(this.nameA);
			}
			else {
				if( otherLabel.nameA.equals(this.nameA) && otherLabel.nameB.equals(this.nameB) )
					return true;
				if( otherLabel.nameA.equals(this.nameB) && otherLabel.nameB.equals(this.nameA) )
					return true;
				return false;
			}
		}
		
		return false;
	}
}