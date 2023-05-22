package mrpnsim.application.reachabilityAsp;

public class Transition {
	String name;
	double x;
	double y;

	public Transition(String name, double x, double y) {
		this.name = name.substring(0);
		this.x = x;
		this.y = y;
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
		return "Name: " + this.getName() + " x: " + this.getX() + " y: " + this.getY();
	}
}
