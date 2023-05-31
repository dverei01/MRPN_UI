package mrpnsim.application.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import mrpnsim.application.ScrollArea;
import mrpnsim.application.editor.EditorArea;
import mrpnsim.application.model.Arrow;
import mrpnsim.application.model.LabelItem;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.model.Token;
import mrpnsim.application.model.Transition;

public class ArrowUI extends EditableNode {

	MovableNode source;
	MovableNode destination;

	private Line line;
	private Line arrow1, arrow2, arrowBody;

	private static final double arrowLength = 10;
	private static final double arrowWidth = 4;

	protected TextFlow label;

	Line invisibleLine;
	// protected ArrayList<Token> tokens;
	// protected ArrayList<Token> negTokens;
	// protected ArrayList<Pair<Token, Token>> bonds;
	// protected ArrayList<Pair<Token, Token>> negBonds;

	public Arrow myArrow;

	private void init() {

		line = new Line();
		arrow1 = new Line();
		arrow2 = new Line();
		invisibleLine = new Line();
		invisibleLine.setStartX(line.getStartX());
		invisibleLine.setEndY(line.getEndY());
		invisibleLine.setEndX(line.getEndX());
		invisibleLine.setEndY(line.getEndY());

		invisibleLine.setStrokeWidth(2.0f);
		invisibleLine.setStroke(Color.TRANSPARENT);

		arrowBody = line;

		line.setMouseTransparent(true);
		arrow1.setMouseTransparent(true);
		arrow2.setMouseTransparent(true);

		invisibleLine.setMouseTransparent(true);

		invisibleLine.setOnMouseClicked(mouseClicked);

		group = new Group(line, arrow1, arrow2, invisibleLine);
		label = new TextFlow();
		group.getChildren().add(label);

		if (mrpn.scrollArea != null) {
			mrpn.scrollArea.addNode(this);
		}

	}

	public ArrowUI(MovableNode source, MovableNode destination) {
		super(source.mrpn);

		init();

		this.source = source;
		this.destination = destination;

		myArrow = mrpn.addArrow(source.myNode, destination.myNode);

		source.arrows.add(this);
		destination.arrows.add(this);
	}

	public ArrowUI(MRPN mrpn) {
		super(mrpn);
		init();
	}

	public Set<LabelItem> getSet() {
		return myArrow.getSet();
	}

	public Line getArrowBody() {
		return arrowBody;
	}

	void addTokenLabel(String token, String id) {
		Label l = new Label(id + ":" + token);

		label.getChildren().add(l);

		Label comma = new Label(",");
		label.getChildren().add(comma);
	}
	void addTokenLabel(String token) {
		Label l = new Label(token);

		label.getChildren().add(l);

		Label comma = new Label(",");
		label.getChildren().add(comma);
	}

	void addBondLabel(Pair<String, String> bond) {
		Label l = new Label(bond.getKey() + " - " + bond.getValue());

		label.getChildren().add(l);

		Label comma = new Label(",");
		label.getChildren().add(comma);

	}

	void createLabel() {
		label.getChildren().clear();

		Set<LabelItem> labelItemSet = getSet();
		for (LabelItem labelItem : labelItemSet) {
			if (labelItem.isToken()) {
				if(labelItem.getTokenType() == null)
					addTokenLabel(labelItem.getTokenName());
				else
					addTokenLabel(labelItem.getTokenType(), labelItem.getTokenType() + labelItem.getIdA());
			}
			else
				addBondLabel(labelItem.getBond());
		}

		int length = label.getChildren().size();
		if (length > 0)
			label.getChildren().remove(length - 1);
	}

	public void updateLabel() {
		createLabel();
	}

	public Node getNode() {
		return (Node) group;
	}

	public Group getGroup() {
		return group;
	}

	public Point2D getStartPoint() {
		// int index = arrowBody.size()-1;
		// Line line = arrowBody.get(index);
		Point2D point = new Point2D(line.getStartX(), line.getStartY());
		return point;
	}

	public Point2D getEndPoint() {
		// int index = arrowBody.size()-1;
		// Line line = arrowBody.get(index);
		Point2D point = new Point2D(line.getEndX(), line.getEndY());
		return point;
	}

	public void setStartPoint(double x, double y) {
		// int index = arrowBody.size()-1;
		// Line line = arrowBody.get(index);
		line.setStartX(x);
		line.setStartY(y);

		invisibleLine.setStartX(x);
		invisibleLine.setStartY(y);
	}

	public void setEndPoint(double x, double y) {
		// int index = arrowBody.size()-1;
		// Line line = arrowBody.get(index);
		line.setEndX(x);
		line.setEndY(y);

		invisibleLine.setEndX(x);
		invisibleLine.setEndY(y);
	}

	double findAngleBetweenTwoPoints(Point2D p1, Point2D p2) {
		double x1 = p1.getX();
		double y1 = p1.getY();
		double x2 = p2.getX();
		double y2 = p2.getY();
		double deltaX = x2 - x1;
		double deltaY = y2 - y1;
		double rad = Math.atan2(deltaY, deltaX);
		double deg = rad * (180 / Math.PI);
		return deg;
	}

	private void updateArrowHead() {

		Point2D end = getEndPoint();
		double ex = end.getX();
		double ey = end.getY();

		Point2D start = getStartPoint();
		double sx = start.getX();
		double sy = start.getY();

		Point2D midpoint = start.midpoint(end);

		arrow1.setEndX(ex);
		arrow1.setEndY(ey);
		arrow2.setEndX(ex);
		arrow2.setEndY(ey);

		if (ex == sx && ey == sy) {
			// arrow parts of length 0
			arrow1.setStartX(ex);
			arrow1.setStartY(ey);
			arrow2.setStartX(ex);
			arrow2.setStartY(ey);
		} else {
			double factor = arrowLength / Math.hypot(sx - ex, sy - ey);
			double factorO = arrowWidth / Math.hypot(sx - ex, sy - ey);

			// part in direction of main line
			double dx = (sx - ex) * factor;
			double dy = (sy - ey) * factor;

			// part ortogonal to main line
			double ox = (sx - ex) * factorO;
			double oy = (sy - ey) * factorO;

			arrow1.setStartX(ex + dx - oy);
			arrow1.setStartY(ey + dy + ox);
			arrow2.setStartX(ex + dx + oy);
			arrow2.setStartY(ey + dy - ox);

			this.label.setLayoutX(midpoint.getX());
			this.label.setLayoutY(midpoint.getY());
			// this.label.setText("Labelx̅");
			/*
			 * String str = "Labelx̅"; char[] ch = str.toCharArray(); for(int i = 0;
			 * i<ch.length;i++) { if(i==ch.length-1) System.out.println((int)ch[i]); else
			 * System.out.println(ch[i]); }
			 */
		}

	}

	public void update() {

		Point2D sourcePos = new Point2D(source.getLayoutX(), source.getLayoutY());

		Point2D start = source.getCenter();

		Point2D end = getEndPoint();
		if (destination != null) {
			end = destination.getCenter();
		}

		ArrayList<ArrowUI> arrows = source.getArrows();
		for (ArrowUI other : arrows) {
			if (other != this) {
				if (other.source == destination && other.destination == source) {
					// System.out.println("self loop");
					Point2D temp = end.subtract(start);
					Point2D point = new Point2D(1, 0);
					double angle = temp.angle(point);
					angle = angle * Math.PI / 180;
					Point2D offset;
					if (source instanceof PlaceUI) {
						if (temp.getY() < 0)
							angle = 2 * Math.PI - angle;
						offset = new Point2D(10 * Math.sin(angle), -10 * Math.cos(angle));

					} else {
						if (temp.getY() > 0)
							angle = 2 * Math.PI - angle;
						angle += Math.PI;
						offset = new Point2D(10 * Math.sin(angle), 10 * Math.cos(angle));
					}
					start = start.add(offset);
					end = end.add(offset);

					break;
				}
			}
		}

		// Find intersection
		Point2D actualStart = source.getIntersectionPoint(start, end);
		if (actualStart == null)
			actualStart = start;

		this.setStartPoint(actualStart.getX(), actualStart.getY());

		Point2D actualEnd = end;
		if (destination != null) {
			actualEnd = destination.getIntersectionPoint(start, end);
			if (actualEnd == null)
				actualEnd = end;
		}

		this.setEndPoint(actualEnd.getX(), actualEnd.getY());

		// Update the arrow head
		updateArrowHead();
	}

	public void setSource(MovableNode newSource) {
		source = newSource;
	}

	public void setDestination(MovableNode newDestination) {
		destination = newDestination;
	}

	public MovableNode getSource() {
		return source;
	}

	public MovableNode getDestination() {
		return destination;
	}

	public void disableMouseTransparent() {
		this.arrowBody.setMouseTransparent(false);
		// for(Line line:arrowBody)
		// line.setMouseTransparent(false);
		this.arrow1.setMouseTransparent(false);
		this.arrow2.setMouseTransparent(false);
		this.invisibleLine.setMouseTransparent(false);
		setMouseTransparent(false);
	}

	protected void onDoubleClick() {
		if (mrpn.scrollArea instanceof EditorArea) {
			EditorArea editor = (EditorArea) mrpn.getScrollArea();
			editor.showArcLabelEditor(this);
		}

	}

	protected void onLeftClick() {
		System.out.println("Left Mouse Click arrow!");
	}

	protected void removeHightlight() {
		line.getStyleClass().remove("selectedArrow");
		// for(Line line:arrowBody)
		// line.getStyleClass().remove("selectedArrow");
		arrow1.getStyleClass().remove("selectedArrow");
		arrow2.getStyleClass().remove("selectedArrow");
	}

	protected void setHightlight() {
		line.getStyleClass().add("selectedArrow");
		// for(Line line:arrowBody)
		// line.getStyleClass().add("selectedArrow");
		arrow1.getStyleClass().add("selectedArrow");
		arrow2.getStyleClass().add("selectedArrow");
	}

	@Override
	public List<Pair<String, Object>> getDataList() {
		ArrayList<Pair<String, Object>> data = new ArrayList<Pair<String, Object>>();

		this.source.name = this.source.myNode.getName();
		this.destination.name = this.destination.myNode.getName();

		data.add(new Pair<String, Object>("source", this.source.name));
		data.add(new Pair<String, Object>("destination", this.destination.name));

		ArrayList<Pair<String, Object>> labelList = new ArrayList<Pair<String, Object>>();

		ArrayList<Pair<String, Object>> tokenList = new ArrayList<Pair<String, Object>>();
		ArrayList<LabelItem> tokens = myArrow.getTokens();
		for (LabelItem token : tokens) {
			ArrayList<Pair<String,String>> tokenData = new ArrayList<Pair<String,String>>();
			tokenData.add(new Pair<String,String>("id",token.getTokenName()));
			if(this.destination instanceof TransitionUI)
				tokenData.add(new Pair<String,String>("type",token.getTokenType()));
			tokenList.add( new Pair<String,Object>("token",tokenData));
		}
		labelList.add(new Pair<String, Object>("tokens", tokenList));


		ArrayList<Pair<String, Object>> bondList = new ArrayList<Pair<String, Object>>();
		ArrayList<LabelItem> bonds = myArrow.getBonds();
		for (LabelItem bond : bonds) {
			ArrayList<Pair<String, String>> tempList = new ArrayList<Pair<String, String>>();
			tempList.add(new Pair<String, String>("token", bond.getTokenName()));
			tempList.add(new Pair<String, String>("token", bond.getTokenNameB()));

			bondList.add(new Pair<String, Object>("bond", tempList));
		}
		labelList.add(new Pair<String, Object>("bonds", bondList));


		data.add(new Pair<String, Object>("label", labelList));

		return data;
	}

	@Override
	public void refresh() {

		// Check if source and destinations still exist
		if (!mrpn.hasNode(source.myNode.getName())) {
			deleteArrow();
			return;
		}
		if (!mrpn.hasNode(destination.myNode.getName())) {
			deleteArrow();
			return;
		}

	}

	public void deleteArrow() {
		source.arrows.remove(this);
		destination.arrows.remove(this);

		// Remove UI from scroll area
		//mrpn.scrollArea.removeNode(this);
		mrpn.scrollArea.removeNode(getGroup());

		// Remove from mrpn list
		mrpn.removeArrow(source.myNode, destination.myNode);

		// Do not refresh all!
	}
	
	@Override
	public void delete() {
		source.arrows.remove(this);
		destination.arrows.remove(this);

		// Remove UI from scroll area
		mrpn.scrollArea.removeNode(this);
		//mrpn.scrollArea.removeNode(getGroup());

		// Remove from mrpn list
		mrpn.removeArrow(source.myNode, destination.myNode);

		// Do not refresh all!
	}

}
