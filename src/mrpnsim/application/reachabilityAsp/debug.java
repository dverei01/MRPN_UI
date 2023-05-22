package mrpnsim.application.reachabilityAsp;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class debug {

	public static void main(String[] args) {
		moveToken("p2", "b1", "p1","TmpPropSave.xml");
	}

	public static void moveToken(String sourcePlaceName, String tokenId, String destPlaceName, String fileName) {
	    String tokenType = "A";
		try {
	        // Load the XML data from a file
	        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(fileName));

	        // Find the source place element with the given name
	        NodeList sourcePlaceList = doc.getElementsByTagName("place");
	        Element sourcePlaceElem = null;
	        for (int i = 0; i < sourcePlaceList.getLength(); i++) {
	            Element currPlaceElem = (Element) sourcePlaceList.item(i);
	            String name = currPlaceElem.getElementsByTagName("name").item(0).getTextContent();
	            if (name.equals(sourcePlaceName)) {
	                sourcePlaceElem = currPlaceElem;
	                break;
	            }
	        }
	        if (sourcePlaceElem == null) {
	            System.out.println("No source place found with name '" + sourcePlaceName + "'.");
	            return;
	        }

	        // Find the destination place element with the given name
	        NodeList destPlaceList = doc.getElementsByTagName("place");
	        Element destPlaceElem = null;
	        for (int i = 0; i < destPlaceList.getLength(); i++) {
	            Element currPlaceElem = (Element) destPlaceList.item(i);
	            String name = currPlaceElem.getElementsByTagName("name").item(0).getTextContent();
	            if (name.equals(destPlaceName)) {
	                destPlaceElem = currPlaceElem;
	                break;
	            }
	        }
	        if (destPlaceElem == null) {
	            System.out.println("No destination place found with name '" + destPlaceName + "'.");
	            return;
	        }

	        // Find the token element with the given ID and type in the source place
	        NodeList tokenList = sourcePlaceElem.getElementsByTagName("token");
	        Element tokenElem = null;
	        for (int i = 0; i < tokenList.getLength(); i++) {
	            Element currTokenElem = (Element) tokenList.item(i);
	            String id = currTokenElem.getElementsByTagName("id").item(0).getTextContent();
	            String type = currTokenElem.getElementsByTagName("type").item(0).getTextContent();
	            if (id.equals(tokenId) && type.equals(tokenType)) {
	                tokenElem = currTokenElem;
	                break;
	            }
	        }
	        if (tokenElem == null) {
	            System.out.println("No token found with ID '" + tokenId + "' and type '" + tokenType +
	                    "' in place '" + sourcePlaceName + "'.");
	            return;
	        }

	        // Create a new token element in the destination place
	        Element newTokenElem = doc.createElement("token");
	        Element newIdElem = doc.createElement("id");
	        newIdElem.setTextContent(tokenId);
	        Element newTypeElem = doc.createElement("type");
	        newTypeElem.setTextContent(tokenType);
	        newTokenElem.appendChild(newIdElem);
	        newTokenElem.appendChild(newTypeElem);
	        NodeList destTokenList = destPlaceElem.getElementsByTagName("tokens");
	        if (destTokenList.getLength() == 0) {
	            Element newTokensElem = doc.createElement("tokens");
	            newTokensElem.appendChild(newTokenElem);
	            destPlaceElem.appendChild(newTokensElem);
	        } else {
	            Element destTokensElem = (Element) destTokenList.item(0);
	            destTokensElem.appendChild(newTokenElem);
	        }

	        // Remove the token element from the source place
	     // Remove only the appropriate token element from the source place
	        NodeList tokensList = sourcePlaceElem.getElementsByTagName("tokens");
	        if (tokensList.getLength() > 0) {
	            Element tokensElem = (Element) tokensList.item(0);
	            tokenList = tokensElem.getElementsByTagName("token");
	            for (int i = 0; i < tokenList.getLength(); i++) {
	                Element currTokenElem = (Element) tokenList.item(i);
	                String id = currTokenElem.getElementsByTagName("id").item(0).getTextContent();
	                if (id.equals(tokenId)) {
	                    tokensElem.removeChild(currTokenElem);
	                    break;
	                }
	            }
	        }

	        // Save the modified XML data back to the file
	        Transformer transformer = TransformerFactory.newInstance().newTransformer();
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        DOMSource source = new DOMSource(doc);
	        StreamResult result = new StreamResult(new File(fileName));
	        transformer.transform(source, result);
			System.out.println("Token with ID '" + tokenId + "' moved from place '" + sourcePlaceName + "' to place '"
					+ destPlaceName + "'.");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
