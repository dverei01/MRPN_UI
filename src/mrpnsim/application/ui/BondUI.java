package mrpnsim.application.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.QuadCurve;
import javafx.util.Pair;
import mrpnsim.application.editor.EditorArea;
import mrpnsim.application.model.Bond;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.model.Token;

public class BondUI extends SelectableNode {

	private Arc arc;

	private TokenUI A, B;

	private Point2D endPoint;

	private void init() {
		endPoint = new Point2D(0, 0);

		arc = new Arc();
		// arc.setMouseTransparent(true);
		arc.setStroke(Color.BLACK);
		arc.setStrokeWidth(2);
		// arc.setFill(Color.TRANSPARENT);

		arc.setType(ArcType.OPEN);
		arc.getStyleClass().add("bond");
		arc.setOnMouseClicked(mouseClicked);

		group = new Group(arc);	
		
		
    	if( mrpn.scrollArea != null ) {
    		mrpn.scrollArea.addNode( this );
    	}
	}
	

	
	public BondUI(Bond existingBond) {
		super(existingBond.getSource().mrpn);
		init();
		
		// Find UI components of tokens
		Token tokenA = existingBond.getSource();
		Token tokenB = existingBond.getDestination();
		
		this.A = mrpn.scrollArea.findToken(tokenA);
		this.B = mrpn.scrollArea.findToken(tokenB);
		
		this.A.bondsUI.add(this);
		this.B.bondsUI.add(this);
	}
	
	public BondUI(TokenUI tokenA, TokenUI tokenB) {
		super(tokenA.mrpn);
		init();
		
		this.A = tokenA;
		this.B = tokenB;
		
		tokenA.bondsUI.add(this);
		tokenB.bondsUI.add(this);
		
		mrpn.addBondFromMRPN(A.myToken, B.myToken);
		
	}
	
	public BondUI(MRPN mrpn) {
		super(mrpn);
		init();
	}

	public Arc getArc() {
		return arc;
	}

	public void setSource(TokenUI A) {
		this.A = A;
	}

	public TokenUI getSource() {
		return A;
	}

	public Node getNode() {
		return (Node) group;
	}
	
	public void setDestination(TokenUI B) {
		this.B = B;
	}

	public TokenUI getDestination() {
		return B;
	}

	public void setEndPoint(double x, double y) {
		this.endPoint = new Point2D(x, y);
	}

	public void update() {

		if (A == null)
			return;

		Point2D start = A.getCenter();
		// Point2D start = new Point2D( A.getLayoutX() , A.getLayoutY() );
		double indexA = A.getIndex();

		Point2D end = new Point2D(start.getX(), endPoint.getY());
		double indexB = 0;
		double div = 22.0;
		double distance = Math.abs(end.getY() - start.getY()) / div;
		
		if (B != null) {

			indexB = B.getIndex();
			distance = Math.abs(indexB - indexA);
			// end = new Point2D( B.getLayoutX(), B.getLayoutY() );
			end = B.getCenter();
		}
		System.out.println("DISTANCE: "+distance);
		
		Point2D center = start.midpoint(end);
		update(center.getX(), center.getY(), distance);

		System.out.println("Bond " + this + ", Pos: " + center + ", Distance: " + distance);

		/*
		 * EditorArea.singleton.dbgCircleA.relocate(start.getX(), start.getY());
		 * 
		 * EditorArea.singleton.dbgCircleB.relocate(end.getX(), end.getY());
		 * 
		 * EditorArea.singleton.dbgCircleC.relocate(center.getX(), center.getY());
		 */
	}

	@Override
	public String toString() {
		String str = "(";
		if (A != null)
			str += A.getName();
		str += " - ";
		if (B != null)
			str += B.getName();
		str += ")";
		return str;
	}

	private void update(double centerX, double centerY, double distance) {
		// Setting the properties of the arc
		double factor = 0.11 * distance;
		//double factor = 0.093 * distance;

		arc.setCenterX(centerX);
		arc.setCenterY(centerY);
		arc.setRadiusX(100.0f * factor);
		arc.setRadiusY(100.0f * factor);
		arc.setStartAngle(90.0f);
		arc.setLength(180.0f);
	}

	public Group getGroup() {
		return group;
	}

	protected void onLeftClick() {
		super.onLeftClick();
		System.out.println("SELECTED BOND ON LEFT CLICK");

		if (!(mrpn.scrollArea instanceof EditorArea)) {
			return;
		}

		EditorArea editor = (EditorArea) mrpn.scrollArea;
		String tool = editor.getSelectedTool();
		if (tool.equals("SELECT"))
			setHightlight();
	}

	@Override
	public List<Pair<String, Object>> getDataList() {
		ArrayList<Pair<String, Object>> data = new ArrayList<Pair<String, Object>>();

		ArrayList<Pair<String, String>> tokenList = new ArrayList<Pair<String, String>>();
		tokenList.add(new Pair<String, String>("token", A.name));
		tokenList.add(new Pair<String, String>("token", B.name));

		data.add(new Pair<String, Object>("bond", tokenList));
		return data;
	}

	@Override
	protected void removeHightlight() {
		// arc.getStyleClass().remove("selectedBond");
		arc.setStroke(Color.rgb(88, 112, 203));
	}

	@Override
	protected void setHightlight() {
		// arc.getStyleClass().add("selectedBond");
		arc.setStroke(Color.rgb(88, 112, 203));
	}

	@Override
	public void refresh() {
	
		// Check if the MRPN still has the bond A-B
		if( !mrpn.hasBond( A.getName(), B.getName())) {
			remove();
			return;
		}
		
		// Also check if the UI components exist
		if( mrpn.scrollArea != null ) {
			if(!mrpn.scrollArea.hasNode(A) ||  !mrpn.scrollArea.hasNode(B) ) {
				remove();
				return;
			}
		}
		
		
	}
	
	public void remove() {
		A.removeBond(this);
		B.removeBond(this);
    	if( mrpn.scrollArea != null ) {
    		mrpn.scrollArea.removeNode( this );
    	}
	}
	
	@Override
	public void delete() {
		A.deleteBond(this);
		B.deleteBond(this);
    	if( mrpn.scrollArea != null ) {
    		mrpn.scrollArea.removeNode( this );
    	}
	}

}
