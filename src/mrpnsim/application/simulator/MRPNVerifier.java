package mrpnsim.application.simulator;

import java.util.ArrayList;
import java.util.Set;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import mrpnsim.application.model.LabelItem;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.model.Transition;


public class MRPNVerifier {
	
	static MRPN mrpn;
	
	public MRPNVerifier(MRPN mrpn) {
		this.mrpn = mrpn;
	}
	
	public static void setMRPN(MRPN mrpn) {
		MRPNVerifier.mrpn = mrpn;
	}
	
	public static MRPN getMRPN() {
		return mrpn;
	}
	
	public static EventHandler<ActionEvent> clicked = new EventHandler<ActionEvent>() {
	    @Override
	    public void handle(final ActionEvent event) {
	    	Alert alert = new Alert(Alert.AlertType.ERROR);
	        alert.setTitle("Confirmation message");
	        try {
	        	if(MRPNVerifier.verify()) {
		    		alert.setHeaderText("Successful Validation!");
		    		FontAwesomeIconView iconSuccess = new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE);
		    		iconSuccess.setGlyphSize(35);
		    		iconSuccess.setFill(Color.LAWNGREEN);
		    		alert.getDialogPane().setGraphic(iconSuccess);
		            alert.show();
		    	}
	        }catch (IllegalArgumentException ex) {
	        	alert.setHeaderText("Parsing Error");
	    		System.out.println("The MRPN is not well-formed.");
	    		System.out.println(ex.getMessage());
	        	alert.setContentText("The MRPN is not well-formed. "+ex.getMessage());
	        	alert.show();
	        }
	    	
	    	event.consume();
	    }
	};
	
	
	public static boolean verify() {
		
		
		Transition[] transitions = mrpn.getTransitions();
		MRPNUtils u = new MRPNUtils( MRPNVerifier.mrpn );
		
		for( Transition transition : transitions ) {
			Set<LabelItem> pret = u.pre(transition);
			Set<LabelItem> postt = u.post(transition);
			
			
			// Definition 2.1
			{
				Set<String> tokensLeft = u.getTokensFrom(pret);
				Set<String> tokensRight = u.getTokensFrom(postt);
				if( !u.compare(tokensLeft, tokensRight) ) {
					throw new IllegalArgumentException("Error in transition "+transition.getName());
					//System.out.println("Definition 2.1 failed at transition "+transition.getName());
					//return false;
				}
			}
			
			// Definition 2.2
			{
				ArrayList<Set<LabelItem>> outLabels = u.outLabels(transition);
				for( int i=0; i<outLabels.size(); i++ ) {
					for( int j=i+1; j<outLabels.size(); j++ ) {
						
						Set<LabelItem> setA = outLabels.get(i);
						Set<LabelItem> setB = outLabels.get(j);
						
						Set<LabelItem> intersection = u.intersection(setA, setB);
						
						if(!intersection.isEmpty()) {
							throw new IllegalArgumentException("Error in transition "+transition.getName());
							//System.out.println("Definition 2.3 failed at transition "+transition.getName());
							//return false;
						}
						
					}
				}
			}
		}
		
		
		return true;
	}
	

}
