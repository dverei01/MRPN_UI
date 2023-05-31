package mrpnsim.application.ui;

import java.util.ArrayList;


import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import mrpnsim.application.ScrollArea;
import mrpnsim.application.editor.EditorArea;
import mrpnsim.application.model.Node;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.model.Transition;
import mrpnsim.application.simulator.SimulatorArea;



public class MovableNode extends EditableNode{

	private EventHandler  mContextDragOver;
    private EventHandler  mContextDragDropped;
	private Point2D mDragOffset = new Point2D(0.0, 0.0);
	
	private final MovableNode self;
	
	protected DataFormat dataformat;
	protected Point2D position;
	
	public Node myNode; // place or transition
	
	
	
	
	public MovableNode(MRPN mrpn, Node myNode, DataFormat dataformat) {
		super(mrpn);
		// TODO Auto-generated constructor stub
		self = this;

		this.myNode = myNode;
		
		this.dataformat = dataformat;
		buildNodeDragHandlers();
	}

	protected ArrayList<ArrowUI> arrows = new ArrayList<ArrowUI>();
	
	
	@Override
	public void rename(String newName) {
		
		if(myNode.getName().equals(newName))
			return;
		
		boolean nameHasChanged = myNode.setName(newName);
		if(!nameHasChanged) {
			//Popup for name taken
			editErrorPopup("Node (Transition or Place) already exists with the name '"+newName+"'");
			return;
		}

	}
	
	public Point2D getCenter() {
		double x = getLayoutX() + (getBoundsInLocal().getWidth() / 2);
		double y = getLayoutY() + (getBoundsInLocal().getHeight() / 2);
		Point2D center = new Point2D(x,y);
		return center;
	}
	
	public Point2D getIntersectionPoint(Point2D p3, Point2D p4) {
		return null;
	}
	
	protected void initArrow( EditorArea editor ) {
		ArrowUI arrow = new ArrowUI(mrpn);
		
		arrow.setSource(this);
		arrows.add(arrow);
		
		Point2D center = this.getCenter();
		arrow.setEndPoint(center.getX() , center.getY());
		arrow.update();
		
		//editor.addNode(arrow);
		editor.setCurrentArrow(arrow);
	}
	
	public ArrayList<ArrowUI> getArrows() {
		return arrows;
	}

	@Override
	protected void onLeftClick() {
		System.out.println("Left Clicked a Movable Node");
		
		
		if( mrpn.scrollArea instanceof SimulatorArea ) {
			buildNodeDragHandlers();
			return;
		}
		
		
		if( !(mrpn.scrollArea instanceof EditorArea) ) {
			return;
		}
		
		EditorArea editor = (EditorArea)mrpn.scrollArea;
		String tool = editor.getSelectedTool();
		
		switch(tool) {
		
		case "SELECT":
			buildNodeDragHandlers();
			break;
		
		case "ARROW": 
			
			ArrowUI currentArrow = editor.getCurrentArrow();
			if( currentArrow == null ) {
				initArrow(editor);
				
			}
			else {
				
				MovableNode source = currentArrow.source;
				
				if( source == this ) {
					System.out.println("Cannot create arrow with self");
					return;
				}
				
				if( source instanceof PlaceUI && this instanceof PlaceUI ) {
					System.out.println("Cannot link place with place");
					return;
				}
				if( source instanceof TransitionUI && this instanceof TransitionUI ) {
					System.out.println("Cannot link transition with transition");
					return;
				}
				
				MovableNode destination = this;
				currentArrow.setDestination(destination);
				currentArrow.update();
				
				
				currentArrow.myArrow = mrpn.addArrow(source.myNode, destination.myNode );
				
				arrows.add(currentArrow);
				editor.setCurrentArrow(null);
				
				currentArrow.disableMouseTransparent();
			
				editor.SomethingChanged();
				
			}
			
			break;
		}
		
	}
	
	protected void deleteAllArrows() {
		
		ArrowUI[] arrowArray = new ArrowUI[arrows.size()];
		arrows.toArray(arrowArray);
		for(int i=0; i<arrowArray.length; i++) {
			ArrowUI arrow = arrowArray[i];
			arrow.delete();
		}
	}
		
	protected void onDragOver() {
		
	}
	
public void buildNodeDragHandlers() {
		
	    self.setOnDragDetected ( new EventHandler <MouseEvent> () {

	        @Override
	        public void handle(MouseEvent event) {
	        	
	        	System.out.println("On drag detected event!");
	        	
	            getParent().setOnDragOver(null);
	            getParent().setOnDragDropped(null);

	            getParent().setOnDragOver (mContextDragOver);
	            getParent().setOnDragDropped (mContextDragDropped);

	            
	            
	        //begin drag ops
	            mDragOffset = new Point2D(event.getX(), event.getY());
	                    
	            relocateToPoint (new Point2D(event.getSceneX(), event.getSceneY()));
	            //relocateToPoint (mDragOffset);
	                    
	        ClipboardContent content = new ClipboardContent();
	            DragContainer container = new DragContainer();
	                    
	           // container.addData ("type", mType);
	            content.put(dataformat, container);
	                    
	        startDragAndDrop (TransferMode.ANY).setContent(content);                  
	                    
	        
	        	
	            event.consume();                    
	        }
	                
	    });  
	    
	    mContextDragOver = new EventHandler <DragEvent> () {

	        //dragover to handle node dragging in the right pane view
	        @Override
	        public void handle(DragEvent event) {       

	            event.acceptTransferModes(TransferMode.ANY);                
	            relocateToPoint(new Point2D( event.getSceneX(), event.getSceneY()));
	            //relocateToPoint(new Point2D( event.getX(), event.getY()));

	            for(ArrowUI arrow : arrows) {
	            	arrow.update();
	            }
	            
	            onDragOver();
	            
	            event.consume();
	        }
	    };
	    
	    
	  //dragdrop for node dragging
	    mContextDragDropped = new EventHandler <DragEvent> () {
	            
	        @Override
	        public void handle(DragEvent event) {

	        //getParent().setOnDragOver(null);
	        //getParent().setOnDragDropped(null);
	                        
	        event.setDropCompleted(true);
	        
	        //self.setOnDragDetected(null);
	             
	        
	        for(ArrowUI arrow : arrows) {
            	arrow.update();
            }
	        
	        event.consume();
	        
	        ScrollArea scrollArea = mrpn.scrollArea;
	        if(scrollArea instanceof EditorArea) {
	        	scrollArea.SomethingChanged();
	        }
	        
	        }
	    };
	    
//	}
	    
	    
	    
	}
	
	public void relocateToPoint (Point2D p) {

		//relocates the object to a point that has been converted to
		//scene coordinates
		Point2D localCoords = getParent().sceneToLocal(p);
		
		Point2D newPos = new Point2D(
				(localCoords.getX() - (getBoundsInLocal().getWidth() /2)),
				(localCoords.getY() - (getBoundsInLocal().getHeight() /2)) );
		
		setPosition(newPos);
	}

	public Point2D getPosition() {
		return position;
	}

	public void setPosition(Point2D newPos) {
		this.position = newPos;
		relocate ( (int) newPos.getX(), (int) newPos.getY() );
	}
	
	
}
