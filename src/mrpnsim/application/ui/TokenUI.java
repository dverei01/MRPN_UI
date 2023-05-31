package mrpnsim.application.ui;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import mrpnsim.application.editor.EditorArea;
import mrpnsim.application.editor.TokenEditPopup;
import mrpnsim.application.model.Place;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.model.Token;

public class TokenUI extends EditableNode {


	private Text label;
	private Circle circle;
	
	private PlaceUI placeUI;
	public Token myToken;
	

	protected ArrayList<BondUI> bondsUI = new ArrayList<BondUI>();
	
	public TokenUI( MRPN mrpn, PlaceUI placeUI, Token token) {
		super(mrpn);
		//super(5,5,5);
		circle = new Circle(5,5,5);
		circle.getOnMouseClicked(); 
		this.myToken = token;
		this.placeUI = placeUI;
		
		this.name = myToken.getName();
		String tokenType = myToken.getType();
		label = new Text(tokenType);
		
		circle.setOnMouseClicked(mouseClicked);
	}
	
	public TokenUI( MRPN mrpn, PlaceUI placeUI,String name) {
		super(mrpn);
		//super(5,5,5);
		circle = new Circle(5,5,5);
		circle.getOnMouseClicked();
		
		Place place = (Place)placeUI.myNode;
		this.myToken = mrpn.addToken(place,name);
		
		
		this.placeUI = placeUI;
		
		String tokenType = myToken.getType();
		this.name = myToken.getName();
		label = new Text(tokenType);
		
		circle.setOnMouseClicked(mouseClicked);
	    //Text tokenId = new Text(tokenID);
	}
	
	public TokenUI( MRPN mrpn, PlaceUI placeUI) {
		super(mrpn);
		//super(5,5,5);
		circle = new Circle(5,5,5);
		circle.getOnMouseClicked();
		
		Place place = (Place)placeUI.myNode;
		this.myToken = mrpn.addToken(place);
		
		
		this.placeUI = placeUI;
		
		String tokenType = myToken.getType();
		this.name = myToken.getName();
		label = new Text(tokenType);
		
		circle.setOnMouseClicked(mouseClicked);
	    //Text tokenId = new Text(tokenID);
	}
	

	public PlaceUI getPlace() {
		return placeUI;
	}
	
	public void setPlace(PlaceUI placeUI) {
		this.placeUI = placeUI;
	}
	
	@Override
	public String toString() {
		return myToken.getName();
	}

	public String getName() {
		return myToken.getName();
	}
	
	public Circle getCircle() {
		return circle;
	}

	public Text getLabel() {
		return label;
	}
	
	public int getIndex() {
		return placeUI.getIndex(this);
	}
	
	public void rename(String newName) {
		myToken.setName(newName);
	}
	
	public void changeType(String newType) {
		myToken.setType(newType);
		String tokenType = myToken.getType();
		label.setText(tokenType);
	}

	
	private void initBond(EditorArea editor) {
		BondUI bond = new BondUI(this.mrpn);
		bond.setSource(this);
		
		Point2D endPoint = getCenter();
		bond.setEndPoint( endPoint.getX(), endPoint.getY());
		bond.update();
		
		//editor.addNode(bond.getGroup());
		editor.setCurrentBond(bond);
	}
	
	public Point2D getCenter() {
		//Bounds bounds = circle.localToScene(circle.getBoundsInLocal());
		//return new Point2D( bounds.getCenterX(), bounds.getCenterY() );
		
		Point2D globalPosition = new Point2D(0,0);
		
		Node node = circle;
		
		AnchorPane scrollArea = mrpn.scrollArea.getfxScrollArea();
		while( node != scrollArea ) {
			if(node == null)
				break;
			
			Point2D localPos = new Point2D( node.getLayoutX(), node.getLayoutY() );
			globalPosition = globalPosition.add(localPos);
			
			node = node.getParent();
		}	
		
		globalPosition = globalPosition.add( circle.getRadius(), circle.getRadius() );
		
		return globalPosition;
	}
	
	protected void onDoubleClick() {
		if( mrpn.scrollArea instanceof EditorArea )
			new TokenEditPopup("Token",this);
		
	}
	
	
	@Override
	protected void onLeftClick() {
		super.onLeftClick();
		
		
		if(!(mrpn.scrollArea instanceof EditorArea))
			return;
		
		System.out.println("Left Clicked a Token Node");
		//System.out.println("Token Center: "+getCenter());
		
		
		EditorArea editor = (EditorArea)mrpn.scrollArea;
		String tool = editor.getSelectedTool();
		
		switch(tool) {
		
		case "BOND":
			
			BondUI currentBond = editor.getCurrentBond();
			if( currentBond == null ) {
				initBond(editor);
			}
			else {
				
				TokenUI source = currentBond.getSource();
				
				if( source == this ) {
					System.out.println("Cannot create bond with self");
					return;
				}
				
				if( source.placeUI != this.placeUI ) {
					System.out.println("Cannot create bond with token in different place");
					return;
				}
				
				TokenUI destination = this;	
				currentBond.setDestination(destination);
				currentBond.update();
				
				bondsUI.add(currentBond);
				destination.bondsUI.add(currentBond);
				
				
				mrpn.addBondFromMRPN( source.myToken, destination.myToken );
				
				editor.setCurrentBond(null);
			}
			
			
			break;
			
		}
	}
	
	protected void removeAllBonds() {
		System.out.println("Removing all bonds");
		
		// Cannot remove while iterating on a list (Concurrent modification exception)
		// So we need to create an array first!
		BondUI[] bondArray = new BondUI[bondsUI.size()];
		bondsUI.toArray(bondArray);
		for(int i=0; i<bondArray.length; i++) {
			BondUI bond = bondArray[i];
			removeBond(bond);
		}

	}
	
	
	public void removeBond(BondUI bond) {
		TokenUI source = bond.getSource();
		TokenUI destination = bond.getDestination();
		
		// Remove from lists
		source.bondsUI.remove(bond);
		destination.bondsUI.remove(bond);
	}
	
	public void deleteBond(BondUI bond) {
		
		TokenUI source = bond.getSource();
		TokenUI destination = bond.getDestination();
		
		// Remove from MRPN
		mrpn.removeBondFromMRPN(source.myToken, destination.myToken);
		
		// Remove from lists
		source.bondsUI.remove(bond);
		destination.bondsUI.remove(bond);
		
	}
	
	
	protected void removeHightlight() {
		circle.getStyleClass().remove("selectedToken");
	}
	
	protected void setHightlight() {
		circle.getStyleClass().add("selectedToken");
	}
	
	@Override
	public void delete() {
		System.out.println("Deleting Token from Place!");
		placeUI.removeToken(this);
		mrpn.removeToken(myToken);
	}
	
}
