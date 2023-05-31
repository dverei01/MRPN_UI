package mrpnsim.application.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Pair;
import jfxtras.scene.layout.CircularPane;
import mrpnsim.application.editor.EditorArea;
import mrpnsim.application.editor.NameEditPopup;
import mrpnsim.application.model.Place;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.model.Token;



public class PlaceUI extends MovableNode {

	public static int counter = 0;
	//private String name;
	
	private VBox vbox;
	private Text label;
	
	private CircularPane circle;
	private GridPane grid;
	
	private ArrayList<TokenUI> tokens = new ArrayList<TokenUI>();
	
	public void rename(String newName) {
		super.rename(newName);
		
		this.name = myNode.getName();
		label.setText(this.name);
	}
	
	public PlaceUI(MRPN mrpn) {
		super(mrpn,mrpn.addPlace(),DragContainer.DragPlace);

		createPlace();
	     
	    circle.getChildren().add(grid);
		
		
	}
	
	public PlaceUI(MRPN mrpn, Point2D position, ArrayList<String> tokenList) {
		
		// Prosthese ena neo place sto MRPN mas
		super(mrpn,mrpn.addPlace(),DragContainer.DragPlace);
		
		Place place = (Place)myNode;
		
		this.position = position;
		
		
		createPlace();
		
		
		for(String tokenName: tokenList) {
			grid.setPadding(new Insets(2, 2, 2, 2));
			grid.setVgap(2); 
			grid.setHgap(2);
			TokenUI token = new TokenUI(mrpn,this);
			grid.add(token.getCircle(), 0, tokens.size());
			grid.add(token.getLabel(), 1, tokens.size());
			tokens.add(token);
		}
		
		circle.getChildren().add(grid);
		
		
	}
	
	private void createPlace() {
		
		vbox = new VBox();
		this.getChildren().add(vbox);
		
		// The name of the place
		label = new Text(myNode.name);
		vbox.getChildren().add(label);
		
		// The circle shape of the place
		circle = new CircularPane();
		vbox.getChildren().add(circle);
		
		// A grid pane that will contain tokens
		circle.setShowDebug(Color.BLACK);
		grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setPadding(new Insets(30, 30, 30, 30));
		grid.setVgap(30); 
		grid.setHgap(30);
		
    	if( mrpn.scrollArea != null ) {
    		mrpn.scrollArea.addNode( this );
    	}
	}
	

	
	
	public CircularPane getCircle() {
		return circle;
	}
	
	@Override
	public void refresh() {
		
		
		// Is the place removed?
		if( mrpn.getPlace(myNode.name) == null ) {
			delete();
			// TODO: cause everything to refresh
			System.err.println("Error Point 153");
			
			return;
		}
		
		boolean hasChanged = false;
		
		// Check if all tokens are contained
		TokenUI[] tokenArray = new TokenUI[tokens.size()];
		tokens.toArray(tokenArray);
		for( TokenUI tokenUI : tokenArray ) {
			
			// Check if the token was deleted
			Token token = mrpn.getToken(tokenUI.myToken.name);
			if( token == null ) {
				removeToken(tokenUI);
				hasChanged = true;
			}
			
			// Check if the token was moved
			else if( token.getPlace() != (Place)myNode ) {
				removeToken(tokenUI);
				hasChanged = true;
			}
		}
		
		// Check if we should add a new token
		Place place = (Place)myNode;
		Token[] myTokens = place.getTokens();
		for( Token myToken : myTokens ) {
			boolean contained = false;
			for( TokenUI tokenUI : tokens ) {
				if( myToken == tokenUI.myToken ) {
					contained = true;
					hasChanged = true;
					break;
				}
			}
			
			if(!contained) 
				addToken(myToken);
		}
		
		if( hasChanged ) {
			// TODO: refresh all other nodes!
		}
		
	}
	
	@Override
	public void delete() {
		
		// Remove place from the MRPN
		mrpn.removePlace((Place)myNode);
		
		// Remove this object from the view
		mrpn.scrollArea.removeNode( this );
		
		mrpn.scrollArea.refreshAll();
	}
	
	/*
	@Override
	public void delete() {
		
		// Delete all tokens
		TokenUI[] tokenArray = new TokenUI[tokens.size()];
	    tokens.toArray(tokenArray);
		for(int i=0; i<tokenArray.length; i++) {
			TokenUI tokenUI = tokenArray[i];
			
			mrpn.removeToken(tokenUI.myToken);
			
			tokens.remove(tokenUI);
			removeToken(tokenUI);
		}
		
		// Delete all arrows
		deleteAllArrows();
		
		System.out.println(mrpn.scrollArea.test());
		
		mrpn.removePlace((Place)myNode);
		
		// Delete self
		mrpn.scrollArea.removeNode( this );
		//EditorArea.singleton.deleteNode(this);
	}
	
	public void moveToken(TokenUI token) {
		
		PlaceUI previousPlace = token.getPlace();
		if(previousPlace == this)
			return;
		
		previousPlace.removeTokenWithoutLabels(token);
		addToken(token);
		token.setPlace(this);
		
		//prepei na metaferthoun kai ta tokens pou einai syndemena me to token
		BondUI[] myBonds = token.getBonds();
		for(int i=0; i<myBonds.length; i++) {
			BondUI bond = myBonds[i];
			TokenUI tokenA = bond.getSource();
			TokenUI tokenB = bond.getDestination();
			if(token == tokenA)
				moveToken(tokenB);
			if(token == tokenB)
				moveToken(tokenA);
		}
		
	}
	*/
	
	/*
	private void removeTokenWithoutLabels(TokenUI token) {

		// Remove from list
		tokens.remove(token);
		
		
		
		// Remove from grid
		grid.getChildren().remove(token.getCircle());
		grid.getChildren().remove(token.getLabel());
		
		// Clear grid, and put tokens back again
		grid.getChildren().clear();
		for(int i=0; i<tokens.size(); i++) {
			grid.add(tokens.get(i).getCircle(), 0, i);
			grid.add(tokens.get(i).getLabel(), 1, i);
			
		}
		// If grid is empty 
		if( tokens.size() == 0) {
			grid.setPadding(new Insets(30, 30, 30, 30));
			grid.setVgap(30); 
			grid.setHgap(30);			
			return;
		}
		
	}
	
	protected void removeToken(TokenUI token) {
		
		removeTokenWithoutLabels(token);
		token.removeAllBonds();
		// Remove all labels that contain token!
		for( ArrowUI arrow : mrpn.arrows ) {
			 
			Set<LabelItem> labelItems = arrow.getSet();
			LabelItem[] labelArray = new LabelItem[labelItems.size()];
			labelItems.toArray( labelArray );
			for( int i=0; i<labelArray.length; i++) {
				LabelItem item = labelArray[i];
				if( item.hasToken(token) )
					labelItems.remove(item);
			}
			arrow.updateLabel();
		}
		
		
		mrpn.tokens.remove(token);
		mrpn.scrollArea.updateArrowsBonds();
		
	}
	*/
	
	
	public void removeToken( TokenUI tokenUI ) {
		// Remove from list
		tokens.remove(tokenUI);

		// Remove from grid
		grid.getChildren().remove(tokenUI.getCircle());
		grid.getChildren().remove(tokenUI.getLabel());

		// Clear grid, and put tokens back again
		grid.getChildren().clear();
		for (int i = 0; i < tokens.size(); i++) {
			grid.add(tokens.get(i).getCircle(), 0, i);
			grid.add(tokens.get(i).getLabel(), 1, i);

		}
		// If grid is empty
		if (tokens.size() == 0) {
			grid.setPadding(new Insets(30, 30, 30, 30));
			grid.setVgap(30);
			grid.setHgap(30);
			return;
		}
	}
	
	public void addToken(Token token) {
		grid.setPadding(new Insets(2, 2, 2, 2));
		grid.setVgap(2); 
		grid.setHgap(2);
		
		TokenUI tokenUI = new TokenUI(mrpn, this, token);
		
		grid.add(tokenUI.getCircle(), 0, tokens.size());
		grid.add(tokenUI.getLabel(), 1, tokens.size());
		
		tokens.add(tokenUI);
		
		mrpn.scrollArea.updateArrowsBonds();
	}
	
	
	public TokenUI addToken() {
		
		grid.setPadding(new Insets(2, 2, 2, 2));
		grid.setVgap(2); 
		grid.setHgap(2);
		
		TokenUI token = new TokenUI(mrpn,this);
		
		grid.add(token.getCircle(), 0, tokens.size());
		grid.add(token.getLabel(), 1, tokens.size());
		
		tokens.add(token);

		mrpn.scrollArea.updateArrowsBonds();		
		return token;
	}
	
	public TokenUI addToken(String name) {
		
		grid.setPadding(new Insets(2, 2, 2, 2));
		grid.setVgap(2); 
		grid.setHgap(2);
		
		TokenUI token = new TokenUI(mrpn,this,name);
		
		grid.add(token.getCircle(), 0, tokens.size());
		grid.add(token.getLabel(), 1, tokens.size());
		
		tokens.add(token);

		mrpn.scrollArea.updateArrowsBonds();		
		return token;
	}
	
	protected void onDoubleClick() {
		if( mrpn.scrollArea instanceof EditorArea )
			new NameEditPopup("Place",this);
		
	}
	
	@Override
	public void onLeftClick() {
		super.onLeftClick();
		
		if(!(mrpn.scrollArea instanceof EditorArea ))
			return;
		
		EditorArea editor = (EditorArea)mrpn.scrollArea;
		String tool = editor.getSelectedTool();
		
		switch(tool) {
		case "TOKEN": 
			addToken();
			editor.SomethingChanged();
			
			break;
		}
		
	}
	
	
	/*
	@Override
	public void relocate(double x, double y) {
		Point2D local = new Point2D(circle.getLayoutX(),circle.getLayoutY());
		double width = circle.getWidth();
		double height = circle.getHeight();
		
		super.relocate(x+width/2 + local.getX(), y+height/2 + local.getY());
	}*/
	public int getIndex(TokenUI token) {
		return tokens.indexOf(token);
	}
	
	public Point2D getRelativeCoordinates() {
		Point2D local = new Point2D(circle.getLayoutX(),circle.getLayoutY());
		double width = circle.getWidth();
		double height = circle.getHeight();
		System.out.println(width);
		return new Point2D(local.getX()+width/2, local.getY()+height/2);
	}
	
	@Override
	public Point2D getCenter() {
		Point2D global = new Point2D(this.getLayoutX(),this.getLayoutY());
		Point2D local = new Point2D(circle.getLayoutX(),circle.getLayoutY());
		double width = circle.getWidth();
		double height = circle.getHeight();
		
		Point2D start = new Point2D( global.getX() + local.getX(), global.getY() + local.getY() );
		
		Point2D center = new Point2D(start.getX() + width/2, start.getY() + height/2);
		return center;
	}
	
	@Override
	protected void setHightlight(/*boolean active*/) {
		//if(this.isSelected)
			circle.getStyleClass().add("selectedPlace");
	}
	
	@Override
	protected void removeHightlight() {
		circle.getStyleClass().remove("selectedPlace");
	}
	
	private void updateBonds() {
		for(TokenUI token : tokens) {
			for(BondUI bond : token.bondsUI) {
				bond.update();
			}
		}
	}
	

	@Override
	public List<Pair<String,Object>> getDataList(){
		ArrayList<Pair<String,Object>> data = new ArrayList<Pair<String,Object>>();
		
		this.name = this.myNode.getName();
		data.add( new Pair<String, Object>("name", this.name ));
		double x = getPosition().getX();
		double y = getPosition().getY();
		data.add( new Pair<String, Object>("x", Double.toString(x) ));
		data.add( new Pair<String, Object>("y", Double.toString(y) ));
		
		ArrayList< Pair<String,Object> > tokenList = new ArrayList<Pair<String,Object>>();
		for( TokenUI tokenUI : tokens ) {
			ArrayList<Pair<String,String>> tokenData = new ArrayList<Pair<String,String>>();
			tokenData.add(new Pair<String,String>("id",tokenUI.myToken.name));
			tokenData.add(new Pair<String,String>("type",tokenUI.myToken.getType()));
			tokenList.add( new Pair<String,Object>("token",tokenData));
		}
		
		data.add( new Pair<String,Object>("tokens",tokenList));
		
		return data;
	}
	
	
	@Override
	protected void onDragOver() {
		updateBonds();
	}
	
	@Override
	public Point2D getIntersectionPoint(Point2D p1, Point2D p2) {
		
		Point2D center = getCenter();
		double r = circle.getWidth()/2;
		List<Point2D> intersectionPoints = PlaceUI.getCircleLineIntersectionPoint(p1, p2, center, r);
		
		if(intersectionPoints.isEmpty())
			return null;
		
		if(intersectionPoints.size() == 1)
			return intersectionPoints.get(0);
		
		Point2D A = new Point2D(p1.getX(),p1.getY());
		Point2D B = new Point2D(p2.getX(),p2.getY());
		Point2D AB = B.subtract(A); // B - A
		
		Point2D C = new Point2D(intersectionPoints.get(0).getX(),intersectionPoints.get(0).getY());
		Point2D D = new Point2D(intersectionPoints.get(1).getX(),intersectionPoints.get(1).getY());
		
		Point2D AC = C.subtract(A); // C - A
		Point2D AD = D.subtract(A); // D - A
		
		// Elegxos: An to intersection point 1 (C) einai mesa sto eu8igramo tmima p1 -> p2
		if( AC.dotProduct(AB) < 0 )
			return intersectionPoints.get(1);
		if( AC.magnitude() > AB.magnitude() )
			return intersectionPoints.get(1);
		
		// Elegxos: An to intersection point 2 (C) einai mesa sto eu8igramo tmima p1 -> p2
		if( AD.dotProduct(AB) < 0 )
			return intersectionPoints.get(0);
		if( AD.magnitude() > AB.magnitude() )
			return intersectionPoints.get(0);
			
		// Kai ta dio einai mesa sto eu8igramo tmima.
		// Ara epilegoume to intersection point pou einai pio konta sto p1
		if( AC.magnitude() < AD.magnitude() )
			return intersectionPoints.get(0);
		else
			return intersectionPoints.get(1);
	}
	
	
	public static List<Point2D> getCircleLineIntersectionPoint(Point2D pointA,
			Point2D pointB, Point2D center, double radius) {
        double baX = pointB.getX() - pointA.getX();
        double baY = pointB.getY() - pointA.getY();
        double caX = center.getX() - pointA.getX();
        double caY = center.getY() - pointA.getY();

        double a = baX * baX + baY * baY;
        double bBy2 = baX * caX + baY * caY;
        double c = caX * caX + caY * caY - radius * radius;

        double pBy2 = bBy2 / a;
        double q = c / a;

        double disc = pBy2 * pBy2 - q;
        if (disc < 0) {
            return Collections.emptyList();
        }
        // if disc == 0 ... dealt with later
        double tmpSqrt = Math.sqrt(disc);
        double abScalingFactor1 = -pBy2 + tmpSqrt;
        double abScalingFactor2 = -pBy2 - tmpSqrt;
        

        Point2D p1 = new Point2D(pointA.getX() - baX * abScalingFactor1, pointA.getY()- baY * abScalingFactor1);
        if (disc == 0) { // abScalingFactor1 == abScalingFactor2
            return Collections.singletonList(p1);
        }
        Point2D p2 = new Point2D(pointA.getX() - baX * abScalingFactor2, pointA.getY()- baY * abScalingFactor2);
        return Arrays.asList(p1, p2);
    }


	public ArrayList<TokenUI> getTokens() {
		return tokens;
	}
	
}
