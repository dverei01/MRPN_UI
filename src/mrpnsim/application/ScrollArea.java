package mrpnsim.application;

import java.io.IOException;
import java.util.ArrayList;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import jfxtras.animation.Timer;
import mrpnsim.application.model.Bond;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.model.Token;
import mrpnsim.application.ui.ArrowUI;
import mrpnsim.application.ui.BondUI;
import mrpnsim.application.ui.PlaceUI;
import mrpnsim.application.ui.SelectableNode;
import mrpnsim.application.ui.TokenUI;
import mrpnsim.application.ui.TransitionUI;

public class ScrollArea extends AnchorPane {

	protected boolean changed = false;
	
	protected boolean savedOnce = false;
	
	
	protected AnchorPane fx_scrollArea;
	protected MRPN mrpn;
	
	protected Timer updateTimer;
	
	protected ArrayList<PlaceUI> places = new ArrayList<PlaceUI>();
	protected ArrayList<TransitionUI> transitions = new ArrayList<TransitionUI>();
	protected ArrayList<ArrowUI> arrows = new ArrayList<ArrowUI>();
	protected ArrayList<BondUI> bonds = new ArrayList<BondUI>();
	
	
	public PlaceUI[] getPlaces() {
		PlaceUI[] placesArray = new PlaceUI[places.size()];
		places.toArray(placesArray);
		return placesArray;
	}
	
	public TransitionUI[] getTransitions() {
		TransitionUI[] transitionArray = new TransitionUI[transitions.size()];
		transitions.toArray(transitionArray);
		return transitionArray;
	}
	
	public ArrowUI[] getArrows() {
		ArrowUI[] arrowArray = new ArrowUI[arrows.size()];
		arrows.toArray(arrowArray);
		return arrowArray;
	}
	
	public BondUI[] getBonds() {
		BondUI[] bondArray = new BondUI[bonds.size()];
		bonds.toArray(bondArray);
		return bondArray;
	}
	
	
	public void SomethingChanged() {
		changed = true;
	}
	
	public void NewFile() {
		changed = false;
		savedOnce = false;
	}
	
	public void SavedChanges() {
		changed = false;
		savedOnce = true;
	}
	
	
	public void clearAll() {
		fx_scrollArea.getChildren().clear();
		clearLists();
	}
	
	public String test() {
		return fx_scrollArea.getChildren().toString();
	}
	
	public AnchorPane getfxScrollArea() {
		return fx_scrollArea;
	}
	
	public ScrollArea(String fxmlFile) {

		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource(fxmlFile)
				);
	
		//System.out.println("Count");
		
		
		fxmlLoader.setRoot(this); 
		fxmlLoader.setController(this);
		
		try { 
			fxmlLoader.load();
        
		} catch (IOException exception) {
		    throw new RuntimeException(exception);
		}
		
		
		
		
		mrpn = new MRPN(this);
		
		// Updates arrows and bonds after a few miliseconds
		updateTimer = new Timer(new Runnable() {
			@Override
			public void run() {
				
				//System.out.println("Updating arrows and bonds");
	    		for(ArrowUI arrow : arrows )
	    			arrow.update();
	    		for(BondUI bond : bonds )
	    			bond.update();
	    		updateTimer.stop();
			}
		});
		updateTimer.setDelay(Duration.millis(100.0));
	}
	
	public void addNode(Node node) {
		
		if( node instanceof SelectableNode ) {
			SelectableNode selectableNode = (SelectableNode)node;
			
			if( node instanceof PlaceUI ) 
				places.add((PlaceUI)node);
			else if( node instanceof TransitionUI ) 
				transitions.add((TransitionUI)node);
			else if( node instanceof BondUI ) 
				bonds.add((BondUI)node);
			else if( node instanceof ArrowUI ) 
				arrows.add((ArrowUI)node);
			
			if( selectableNode.getNode() != null ) {
				fx_scrollArea.getChildren().add(selectableNode.getNode());
			}
		}
		else {
			fx_scrollArea.getChildren().add(node);
		}
		
	}
	
	public boolean hasNode(Node node) {
		return fx_scrollArea.getChildren().contains(node);
	}
	
	public void removeNode(Node node) {
		
		if( node instanceof SelectableNode ) {
			SelectableNode selectableNode = (SelectableNode)node;
			
			if( node instanceof PlaceUI ) 
				places.remove((PlaceUI)node);
			else if( node instanceof TransitionUI ) 
				transitions.remove((TransitionUI)node);
			else if( node instanceof BondUI ) 
				bonds.remove((BondUI)node);
			else if( node instanceof ArrowUI ) 
				arrows.remove((ArrowUI)node);
			
			if( selectableNode.getNode() != null ) {
				fx_scrollArea.getChildren().remove(selectableNode.getNode());
			}
		}
		else {
			fx_scrollArea.getChildren().remove(node);
		}
		
		
	}
	
	
	public TokenUI findToken(Token token) {
		for( PlaceUI place : places ) {
			
			for( TokenUI tokenUI : place.getTokens() ) {
				
				if( tokenUI.myToken == token )
					return tokenUI;
			}
			
		}
		return null;
	}
	
	protected void refreshBonds() {
		// Use array to avoid java.util.ConcurrentModificationException 
		BondUI[] bondArray = new BondUI[bonds.size()];
		bonds.toArray(bondArray);
		for( BondUI bondUI : bondArray )
			bondUI.refresh();
	}
	
	public void refreshAll() {
	
		
		for( PlaceUI place : places )
			place.refresh();
		
		for( TransitionUI transition : transitions )
			transition.refresh();
		
		for( ArrowUI arrow : arrows ) {
			arrow.refresh();
		}
		
		
		refreshBonds();
		
		// Add bonds that dont exist
		for( Bond bond : mrpn.getBonds() ) {
			boolean exists = false;
			for( BondUI bondUI : bonds ) {
				Token A = bondUI.getSource().myToken;
				Token B = bondUI.getDestination().myToken;
				if( (bond.getSource() == A && bond.getDestination() == B) ||
						(bond.getSource() == B && bond.getDestination() == A) ) {
					
					exists = true;
					break;
				}
			}
			
			if(!exists) {
				BondUI newBond = new BondUI(bond);
				//System.out.println(newBond.toString());
			}
			
		}
		
		
		
		updateArrowsBonds();
	}
	
	
	
	public void updateArrowsBonds() {
		updateTimer.restart();
	}
	
	public MRPN getMRPN() {
		return mrpn;
	}

	public void clearLists() {
		places.clear();
		transitions.clear();
		arrows.clear();
		bonds.clear();
	}
	
	public void setMRPN(MRPN mrpn) {
		// TODO Auto-generated method stub
		this.mrpn = mrpn;
	}
	
	public void loadFXML(String fxmlFile) {

		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource(fxmlFile)
				);
	
		fxmlLoader.setRoot(this); 
		fxmlLoader.setController(this);
		
		try { 
			fxmlLoader.load();
        
		} catch (IOException exception) {
		    throw new RuntimeException(exception);
		}
		
	}
	
	
	
}
