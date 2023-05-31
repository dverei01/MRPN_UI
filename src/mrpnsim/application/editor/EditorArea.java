package mrpnsim.application.editor;


import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import jfxtras.animation.Timer;
import mrpnsim.application.MRPNApp;
import mrpnsim.application.ScrollArea;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.ui.ArcLabelUI;
import mrpnsim.application.ui.ArrowUI;
import mrpnsim.application.ui.BondUI;
import mrpnsim.application.ui.PlaceUI;
import mrpnsim.application.ui.SelectableNode;
import mrpnsim.application.ui.TokenUI;
import mrpnsim.application.ui.TransitionUI;

public class EditorArea extends ScrollArea {
	
	private EditorToolbox toolbox;
	
	private ArrowUI currentArrow = null;
	private BondUI currentBond = null;
	
	@FXML
    private AnchorPane editor_content;

    @FXML
    private VBox fx_editorButtons;

    @FXML
    private AnchorPane fx_editorScrollArea;
	
    
    protected ArcLabelUI arcLabel;
    
    
	@Override
	public void SomethingChanged() {
		super.SomethingChanged();
		MRPNApp.AddAsterisk();
	}
	
	@Override
	public void SavedChanges() {
		super.SavedChanges();
		MRPNApp.RemoveAsterisk();
	}
	
	
	
	
	public void setCurrentArrow(ArrowUI arrow) {
		this.currentArrow = arrow;
	}
	public ArrowUI getCurrentArrow() {
		return this.currentArrow;
	}
	
	public void setCurrentBond(BondUI bond) {
		this.currentBond = bond;
	}
	public BondUI getCurrentBond() {
		return this.currentBond;
	}
	

	
	public EditorArea() {
		super("/editor_tab.fxml");
		fx_scrollArea = fx_editorScrollArea;
		
		toolbox = new EditorToolbox(fx_editorButtons);
		
		this.fx_scrollArea.setOnMouseClicked(mouseClicked);
		this.fx_scrollArea.setOnMouseMoved(mouseMoved);
	
		arcLabel = new ArcLabelUI( mrpn);
		//createPerformanceTestMRPN(10);
	}

	
	public void showArcLabelEditor( ArrowUI arc ) {
		// TODO Auto-generated method stub
		arcLabel.showPopup(arc);
	}

	public void createPerformanceTestMRPN(int n) {
		
		TransitionUI t = addTransition( new Point2D(400,n*25));
		

		// Inputs
		for( int i=0; i<n; i++ ) {
			
			PlaceUI inPlace = addPlace( new Point2D(50,50+i*60));
			PlaceUI outPlace = addPlace( new Point2D(800,50+i*60));
		
			ArrowUI inArrow = new ArrowUI(inPlace, t);
			ArrowUI outArrow = new ArrowUI(t, outPlace);

			TokenUI tokenUI = inPlace.addToken();
			inArrow.myArrow.addToken(tokenUI.myToken.getName());
			outArrow.myArrow.addToken(tokenUI.myToken.getType());
			
			
			inArrow.updateLabel();
			inArrow.update();
			inArrow.disableMouseTransparent();
			
			outArrow.updateLabel();
			outArrow.update();
			outArrow.disableMouseTransparent();	
			
			
		}
		
		
		this.SomethingChanged();
		this.updateArrowsBonds();
		
	}
	
	
	public PlaceUI addPlace(Point2D pos) {
		
		PlaceUI placeUI = new PlaceUI(mrpn);
		//place.relocate(pos.getX()-20, pos.getY()-40);
		placeUI.setPosition(new Point2D(pos.getX()-20, pos.getY()-40));
		return placeUI;
	}
	
	public TransitionUI addTransition(Point2D pos) {
		TransitionUI transition = new TransitionUI(mrpn);
		//transition.relocate(pos.getX(), pos.getY());
		transition.setPosition(new Point2D(pos.getX(), pos.getY()));
		return transition;
	}
	
	/*
	public void deleteNode(SelectableNode node) {
		
		// Delete from MRPN
		if(node instanceof Place) {
			removeNode(node);
		}
		else if(node instanceof Transition){
			removeNode(node);
		}
		else if(node instanceof Token) {
			//mrpn.deleteToken((Token) node);
		}
		else if(node instanceof Bond) {
			Bond bond = (Bond)node;
			removeNode(bond.getGroup());
			return;
		}
		else if(node instanceof Arrow) {
			//mrpn.deleteArrow((Arrow) node);
			Arrow arrow = (Arrow)node;
			removeNode(arrow.getGroup());
			return;
		}
		
		
	}
	*/
	
	public String getSelectedTool() {
		return toolbox.getSelected();
	}
	
	private void onLeftClick(Point2D mousePos) {
		
		String tool = toolbox.getSelected();
		switch(tool) {
			
		case "PLACE": addPlace(mousePos); SomethingChanged(); break;
		case "TRANSITION": addTransition(mousePos); SomethingChanged(); break;
		case "SELECT":
			//mrpn.disableMouseTransparentBond();
			//System.out.println("mpike");
			break;
		};
		
	}
	
	public void setMRPN(MRPN mrpn) {
		super.setMRPN(mrpn);
		arcLabel = new ArcLabelUI( mrpn);
	}
	
	EventHandler<MouseEvent> mouseClicked = new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(final MouseEvent event) {
	    	
	    	Point2D mousePos = new Point2D( event.getX(), event.getY() );
	    	
	    	if( event.getButton() == MouseButton.PRIMARY )
	    		onLeftClick(mousePos);
	    	
	    	System.out.println(event.getClickCount());
	    	
	    	System.out.println("Selected button: "+toolbox.getSelected());
	        event.consume();
	    }
	};
	
	
	/*EventHandler<MouseEvent> mousePressed = new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(final MouseEvent event) {
	    	
	    	Point2D mousePos = new Point2D( event.getX(), event.getY() );
	    	String tool = toolbox.getSelected();
	    	switch(tool) {
	    	case "ARROW": 
				if(currentArrow != null) {
					ArrayList<Line> arrowBody = currentArrow.getArrowBody();
					int index = arrowBody.size() - 1;
					Line last_line = arrowBody.get(index);		
					Line line = new Line();
					line.setStartX(last_line.getEndX());
					line.setStartY(last_line.getEndY());
					line.setEndX(mousePos.getX());
					line.setEndY(mousePos.getY());
					arrowBody.add(line);
					Group group = currentArrow.getGroup();
					group.getChildren().add(line);
					
					
				}
				break;
	    	}
	        event.consume();
	    }
	};*/
	
	EventHandler<MouseEvent> mouseMoved = new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(final MouseEvent event) {
	    	
	    	//System.out.println("Mouse moved");
	    	Point2D mousePos = new Point2D( event.getX(), event.getY() );
	    	
	    	String tool = toolbox.getSelected();
	    	switch(tool) {
	    	case "ARROW":
	    		//System.out.println("Arrow tool");
	    		if( currentArrow != null ) {
	    			//System.out.println("Current arrow");
	    			/*ArrayList<Line> arrowBody = currentArrow.getArrowBody();
					int index = arrowBody.size() - 1;
					Line last_line = arrowBody.get(index);
					last_line.setEndX(mousePos.getX());
					last_line.setEndY(mousePos.getY());*/
	    			currentArrow.setEndPoint( mousePos.getX(), mousePos.getY());
	    			currentArrow.update();
	    			SomethingChanged();
	    		}
	    		
	    		break;
	    		
	    	case "BOND":
	    		
	    		if(currentBond != null) {
	    			currentBond.setEndPoint( mousePos.getX(), mousePos.getY());
	    			currentBond.update();
	    			SomethingChanged();
	    		}
	    		break;
	    	
	    	}
	    }
	};	
	
	public EventHandler<KeyEvent> keyClicked = new EventHandler<KeyEvent>() {
	    @Override
	    public void handle(final KeyEvent keyEvent) {
	    	String keyCode = keyEvent.getCode().toString();
	    	if(keyCode.equals("DELETE")) {
	    		//deleteKeyPressed = true;
	    		System.out.println(keyCode);
	    		
	    		SelectableNode selectedNode = SelectableNode.currentSelected;
	    		selectedNode.delete();
	    		SomethingChanged();
	    		//deleteNode(selectedNode);
	    		
	    		//if(node!=null)
	    		//System.out.println("selected: "+node.hashCode());
	    	}
	    	
	    	
	    	keyEvent.consume();
	    }
	};



	
}
