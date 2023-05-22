package mrpnsim.application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javafx.geometry.Point2D;
import javafx.util.Pair;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.ui.ArrowUI;
import mrpnsim.application.ui.BondUI;
import mrpnsim.application.ui.MovableNode;
import mrpnsim.application.ui.PlaceUI;
import mrpnsim.application.ui.SelectableNode;
import mrpnsim.application.ui.TokenUI;
import mrpnsim.application.ui.TransitionUI;

public class MRPNXMLReader {

	private static Node parseNode(Element parent, int index, String tag) {
		return parent.getElementsByTagName(tag).item(index);
	}

	private static NodeList parseNodeList(Element parent, String tag) {
		return parent.getElementsByTagName(tag);
	}

	private static String parseText(Element parent, int index, String tag) {
		return parseNode(parent, index, tag).getTextContent();
	}

	private static double parseDouble(Element parent, int index, String tag) {
		String str = parseText(parent, index, tag);
		return Double.parseDouble(str);
	}

	private static MRPN readFile(MRPN mrpn, File file) throws Exception {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(file);
		doc.getDocumentElement().normalize();
		// System.out.println("Root element :" +
		// doc.getDocumentElement().getNodeName());

		// gia na ginei 2 fores to sxima
		double epipleonY = 0;

		Map<String, MovableNode> movableNodes = new HashMap<String, MovableNode>();
		Map<String, TokenUI> tokenNodes = new HashMap<String, TokenUI>();

		// Read and create places
		NodeList placeList = doc.getElementsByTagName("place");
		for (int i = 0; i < placeList.getLength(); i++) {
			Node nNode = placeList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				String placeName = parseText(eElement, 0, "name")/* +"_"+counter */;
				Double x = parseDouble(eElement, 0, "x");
				Double y = parseDouble(eElement, 0, "y");
				/*
				 * if(counter!=1) y+=epipleonY;
				 */
				Point2D position = new Point2D(x, y);
				ArrayList<String> tokenNames = new ArrayList<>();

				if (movableNodes.containsKey(placeName)) {
					System.err.println("Error! There is already a place with the name '" + placeName + "'");
					throw new Exception("Duplicate place name error");
				}

				PlaceUI placeUI = new PlaceUI(mrpn);
				placeUI.setPosition(position);
				placeUI.rename(placeName);

				movableNodes.put(placeName, placeUI);

				Node tokens = parseNode(eElement, 0, "tokens");
				if (tokens.getNodeType() == Node.ELEMENT_NODE) {
					Element eTokens = (Element) tokens;
					NodeList tokenList = parseNodeList(eTokens, "token");
					for (int j = 0; j < tokenList.getLength(); j++) {

						Node nToken = tokenList.item(j);
						if (nToken.getNodeType() == Node.ELEMENT_NODE) {
							Element tElement = (Element) nToken;
							String tokenId = parseText(tElement, 0, "id");
							String tokenType = parseText(tElement, 0, "type");

							if (tokenNodes.containsKey(tokenId)) {
								System.err.println("Error! There is already a token with the symbol '" + tokenId + "'");
								throw new Exception("Duplicate token symbol error");
							}

							TokenUI tokenUI = placeUI.addToken(tokenId);
							tokenUI.rename(tokenId);
							tokenUI.changeType(tokenType);
							tokenNodes.put(tokenId, tokenUI);
						}
					}
				}

			}

		}

		NodeList transitionList = doc.getElementsByTagName("transition");
		// System.out.println("----------------------------");

		for (int i = 0; i < transitionList.getLength(); i++) {
			Node nNode = transitionList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				String transitionName = parseText(eElement, 0, "name")/* +"_"+counter */;
				Double x = parseDouble(eElement, 0, "x");
				Double y = parseDouble(eElement, 0, "y");
				/*
				 * if(counter!=1) y+=epipleonY;
				 */
				Point2D position = new Point2D(x, y);

				if (movableNodes.containsKey(transitionName)) {
					System.err.println("Error! There is already a node with the name '" + transitionName + "'");
					throw new Exception("Duplicate node name error");
				}

				TransitionUI transitionUI = new TransitionUI(mrpn);
				transitionUI.setPosition(position);
				transitionUI.rename(transitionName);

				movableNodes.put(transitionName, transitionUI);

			}
		}

		NodeList arrowList = doc.getElementsByTagName("arrow");
		for (int i = 0; i < arrowList.getLength(); i++) {
			Node nNode = arrowList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				String sourceName = parseText(eElement, 0, "source")/* +"_"+counter */;
				String destinationName = parseText(eElement, 0, "destination")/* +"_"+counter */;

				MovableNode source = movableNodes.get(sourceName);
				MovableNode destination = movableNodes.get(destinationName);

				if (source == null) {
					System.err.println("Error! No node with the name '" + sourceName + "'");
					throw new Exception("Non-existent source error");
				}
				if (destination == null) {
					System.err.println("Error! No node with the name '" + destinationName + "'");
					throw new Exception("Non-existent destination error");
				}
				if (source instanceof PlaceUI && destination instanceof PlaceUI) {
					System.err.println("Error! Cannot connect place (" + sourceName + ") with another place ("
							+ destinationName + ")");
					throw new Exception("Place connect place error");
				}
				if (source instanceof TransitionUI && destination instanceof TransitionUI) {
					System.err.println("Error! Cannot connect transition (" + sourceName + ") with another transition ("
							+ destinationName + ")");
					throw new Exception("Transition connect transition error");
				}

				ArrowUI arrow = new ArrowUI(source, destination);

				// No we add the label items
				Node tokens = parseNode(eElement, 0, "tokens");
				if (tokens.getNodeType() == Node.ELEMENT_NODE) {
					Element tokenElement = (Element) tokens;
					NodeList tokenList = parseNodeList(tokenElement, "token");
					for (int j = 0; j < tokenList.getLength(); j++) {
						if (destination instanceof TransitionUI) {
							Node nToken = tokenList.item(j);
							if (nToken.getNodeType() == Node.ELEMENT_NODE) {
								Element tElement = (Element) nToken;

								String tokenId = parseText(tElement, 0, "id");
								String tokenType = parseText(tElement, 0, "type");

								arrow.myArrow.addToken(tokenType, tokenId);
							}
						} else {
							Node nToken = tokenList.item(j);
							if (nToken.getNodeType() == Node.ELEMENT_NODE) {
								Element tElement = (Element) nToken;

								String tokenId = parseText(tElement, 0, "id");

								arrow.myArrow.addToken(tokenId);
							}
						}
					}
				}

				Node bonds = parseNode(eElement, 0, "bonds");
				if (bonds.getNodeType() == Node.ELEMENT_NODE) {
					NodeList bondList = parseNodeList((Element) bonds, "bond");
					for (int j = 0; j < bondList.getLength(); j++) {
						Element bond = (Element) bondList.item(j);
						String tokenNameA = parseText(bond, 0, "token")/* +"_"+counter */;
						String tokenNameB = parseText(bond, 1, "token")/* +"_"+counter */;

						arrow.myArrow.addBond(tokenNameA, tokenNameB);
					}
				}

				arrow.updateLabel();
				arrow.update();
				arrow.disableMouseTransparent();

			}

		}

		Element bonds = (Element) doc.getElementsByTagName("totalBonds").item(0);
		NodeList bondList = parseNodeList(bonds, "bond");
		for (int i = 0; i < bondList.getLength(); i++) {
			Element bond = (Element) bondList.item(i);
			String tokenNameA = parseText(bond, 0, "token")/* +"_"+counter */;
			String tokenNameB = parseText(bond, 1, "token")/* +"_"+counter */;

			TokenUI tokenA = tokenNodes.get(tokenNameA);
			if (tokenA == null)
				continue;
			TokenUI tokenB = tokenNodes.get(tokenNameB);
			if (tokenB == null)
				continue;

			BondUI newBond = new BondUI(tokenA, tokenB);

		}

		return mrpn;
	}

	public static MRPN read(ScrollArea scrollArea, File file) {

		MRPN mrpn = new MRPN(scrollArea);

		try {
			readFile(mrpn, file);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mrpn;
	}

}
