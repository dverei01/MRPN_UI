package mrpnsim.application;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javafx.util.Pair;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.ui.ArrowUI;
import mrpnsim.application.ui.BondUI;
import mrpnsim.application.ui.PlaceUI;
import mrpnsim.application.ui.TransitionUI;

public class MRPNXMLWriter {

	private static Document doc;

	private static Element appendChild(Element parent, String childName) {
		Element child = doc.createElement(childName);
		parent.appendChild(child);
		return child;
	}
	
	private static void appendChildWithText(Element parent, String childName,String childText) {
		Element child = doc.createElement(childName);
		child.appendChild(doc.createTextNode(childText));
		parent.appendChild(child);
	}
	
	
	private static void appendList(Element parent, List< ? > list) {
		for( Object item : list ) {
			
			if( item instanceof Pair<?,?> ) {
				Object key = ((Pair)item).getKey();
				Object value = ((Pair) item).getValue();
				
				if( value instanceof List<?> ) {
					Element childElement = appendChild(parent, key.toString());
					appendList(childElement, (List<?>) value );
				}
				else {
					//System.out.println("key: "+key +", value: "+value);
					appendChildWithText(parent, key.toString(), value.toString());
				}
			}
			
		}
	}

	
	public static void write(MRPN mrpn, File file) {
		
		if( mrpn.scrollArea == null ) {
			System.err.println("Error. No scroll area attached to MRPN. Cannot save UI data.");
			return;
		}
		
		ScrollArea scrollArea = mrpn.scrollArea;
		
		PlaceUI[] places = scrollArea.getPlaces();
		BondUI[] bonds = scrollArea.getBonds();
		TransitionUI[] transitions = scrollArea.getTransitions();
		ArrowUI[] arrows = scrollArea.getArrows();
		
		 try {
	         DocumentBuilderFactory dbFactory =
	         DocumentBuilderFactory.newInstance();
	         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	         doc = dBuilder.newDocument();
	         
	         // root element
	         Element rootElement =  doc.createElement("mrpn");
	         doc.appendChild(rootElement);
	         
	         Element placesElement = appendChild(rootElement, "places");
	         for(PlaceUI place: places) {
	        	 Element placeElement = appendChild(placesElement, "place");
	        	 appendList(placeElement, place.getDataList());
	         }
	           
	         Element transitionsElement = appendChild(rootElement, "transitions");
	         for(TransitionUI transition: transitions) {
	        	 Element transitionElement = appendChild(transitionsElement, "transition");
	        	 appendList(transitionElement, transition.getDataList());
	         }
	         
	         Element arrowsElement = appendChild(rootElement, "arrows");
	         for(ArrowUI arrow: arrows) {
	        	 if(!scrollArea.hasNode(arrow.getSource()))
	        		 continue;
	        	 if(!scrollArea.hasNode(arrow.getDestination()))
	        		 continue;
	        	 Element arrowElement = appendChild(arrowsElement, "arrow");
	        	 appendList(arrowElement,arrow.getDataList());
	         }
	         
	         Element bondsElement = appendChild(rootElement, "totalBonds");
	         for(BondUI bond: bonds) {
	        	 //Element bondElement = appendChild(bondsElement, "bond");
	        	 appendList(bondsElement, bond.getDataList());
	         }
	         

	         // write the content into xml file
	         TransformerFactory transformerFactory = TransformerFactory.newInstance();
	         Transformer transformer = transformerFactory.newTransformer();
	         DOMSource source = new DOMSource(doc);
	         StreamResult result = new StreamResult(file);
	         transformer.transform(source, result);
	         
	         // Output to console for testing
	         //StreamResult consoleResult = new StreamResult(System.out);
	         //transformer.transform(source, consoleResult);
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
		 
	}
	
}
