package mrpnsim.application.reachabilityAsp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class translator {
	public static String pathForFiles = "C:\\Users\\dvere\\eclipse-workspace\\test\\";

	static String s1 = "";
	static String id = "";
	static String type = "";
	static String nameOfPlaces = "";
	static Place p;
	static String name = "";
	static String source = "";
	static String destination = "";
	static double x = 0, y = 0;
	static Token t;
	static Transition tr;
	static boolean placesFlag = false;
	static boolean tokenBondFlag = false;
	static boolean arrowsFlag = false;
	static boolean arrowBondsFlag = false;
	static boolean transitionsFlag = false;
	static ArrayList<Place> placeList = new ArrayList<Place>();
	static ArrayList<Transition> transitionList = new ArrayList<Transition>();
	static ArrayList<Arrow> arrowList = new ArrayList<Arrow>();
	static ArrayList<Bond> totalBonds = new ArrayList<Bond>();
	static int arrowsCounter = 0;
	static ArrayList<Token> arrowTokens = new ArrayList<Token>();
	static ArrayList<Bond> arrowBonds = new ArrayList<Bond>();
	static int arrowBondCounter = 0;
	static int arrowCnt = 0;
	static boolean totalBondsFlag = false;
	static ArrayList<Token> bondTokens = new ArrayList<Token>();

	public static void main(String[] args) throws IOException {
		try {
			// Parse the XML file
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			// Document doc = builder.parse("Rafail_Temp.xml");
			Document doc = builder.parse("TmpPropSave.xml");

//			Document doc = builder.parse("temp.xml");
			// Traverse the XML document and print the tree
			readXML(doc, 0);
			printMrpnDetails();
			readAndCopyLpCode();
			translateToAsp();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void translateToAsp() {
		aspTranslator a = new aspTranslator(placeList, transitionList, arrowList, totalBonds);
		a.produceAsp();
		// System.out.println(a.translateToAsp);//metafrasi s
	}

	public static String readLpFile(String code) throws FileNotFoundException {
		File file = new File(code);
		Scanner input = new Scanner(file);
		String s = "";
		// Read file
		while (input.hasNextLine()) {
			String line = input.nextLine();
			s += line + '\n';
		}

		// Close file
		input.close();
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(pathForFiles + "aspCode.lp", true);
			BufferedWriter writer = new BufferedWriter(fileWriter);
			writer.write(s);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

	public static void readAndCopyLpCode() {
		try {
			String code = readLpFile(pathForFiles + "MRPN.lp");
//			System.out.println(code);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static ArrayList<String> getStringSplited(String input) {
		ArrayList<String> parts = new ArrayList<String>();
		String str = "";
		boolean firstTimeFlag = true;
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (!Character.isDigit(c)) {

				if (!firstTimeFlag)
					parts.add(str);
				firstTimeFlag = false;
				str = "" + input.charAt(i);
			} else {
				str += input.charAt(i);
			}
		}

		return parts;
	}

	public static void printMrpnDetails() {
		int i = 0;
		String s = "Places:" + '\n';
		for (i = 0; i < placeList.size(); i++) {
			s += placeList.get(i).toString() + '\n';
		}
		s += "Transitions:" + '\n';
		for (i = 0; i < transitionList.size(); i++) {
			s += transitionList.get(i).toString() + '\n';
		}
		s += "Arrows:" + '\n';
		for (i = 0; i < arrowList.size(); i++) {
			s += arrowList.get(i).toString() + '\n';
		}
		s += "Total Bonds:" + '\n';
		//System.out.println(totalBonds);
		for (i = 0; i < totalBonds.size(); i++) {
			if (totalBonds.get(i) != null)
				s += totalBonds.get(i).toString() + '\n';
		}
		//System.out.println(s);
	}

	public static void readXML(Node node, int level) {
		// Print the node name and text content
		StringBuilder indent = new StringBuilder();
		for (int i = 0; i < level; i++) {
			indent.append("  ");
		}
		String s = "";
		s += indent + node.getNodeName() + ": " + node.getTextContent();
		//System.out.println(s);
		if (node.getNodeName().equals("places")) {
			placesFlag = true;
			transitionsFlag = false;
			arrowsFlag = false;
			arrowBondsFlag = false;
			totalBondsFlag = false;
		} else if (node.getNodeName().equals("name")) {
			name = node.getTextContent();
		} else if (node.getNodeName().equals("x")) {
			x = Double.parseDouble(node.getTextContent());
		} else if (node.getNodeName().equals("y")) {
			y = Double.parseDouble(node.getTextContent());
			if (transitionsFlag) {
				tr = new Transition(name, x, y);
				transitionList.add(tr);
				//System.out.println(tr.toString());
			}
		} else if (node.getNodeName().equals("transitions")) {
			transitionsFlag = true;
			placesFlag = false;
			arrowsFlag = false;
			arrowBondsFlag = false;
			totalBondsFlag = false;
		} else if (placesFlag && node.getNodeName().equals("tokens")) {
			if (node.getTextContent().length() > 1) {
				t = new Token(node.getTextContent().substring(0, node.getTextContent().length() - 1),
						node.getTextContent().charAt(node.getTextContent().length() - 1));
			} else {
				t = null;
			}

			///////////////////////////////////////////////////////////////////////////

			// PREPEI NA KAMW KATI GIA NA EXEI ENA PLACE PARAPANO POU ENA TOKEN
			// EPISIS ME TA BONDS
			///////////////////////////////////////////////////////////////////////
			p = new Place(name, x, y);
			p.addToken(t);
			placeList.add(p);
		} else if (node.getNodeName().equals("arrows")) {
			arrowsFlag = true;
			transitionsFlag = false;
			placesFlag = false;
			arrowBondsFlag = false;
			totalBondsFlag = false;
		} // Nomizw kalitera na dw ti tiponei to xml kai na pairnw ta arrows ana 2
			// xaraktires
		else if (node.getNodeName().equals("arrow") && arrowsFlag) {
			arrowBondsFlag = false;
			String arrowContent = node.getTextContent();
			ArrayList<String> parts = new ArrayList<String>();
			//System.out.println(arrowContent);
			parts = getStringSplited(arrowContent);

			source = parts.get(0);
//			System.out.println(parts.get(0) + "-----" + parts.get(1));
			//System.out.println(parts);
			destination = parts.get(1);
		} else if (node.getNodeName().equals("token") && arrowsFlag && !arrowBondsFlag && !totalBondsFlag) {
			arrowBondsFlag = false;
			String tokenContent = node.getTextContent();
			// System.out.println("aaaaaaa" + tokenContent);
			Token t;
			if (tokenContent.length() % 2 == 1)
				t = new Token(tokenContent.substring(0, 2));
			// arrowTokens.add(new Token(tokenContent.substring(0, 2)));// Itan 0,1 elegxw
			// to xana an ekame diafora
			else
				t = new Token(tokenContent.substring(0));
			// arrowTokens.add(new Token(tokenContent.substring(0)));
			arrowTokens.add(t);
			if (!Character.isDigit(tokenContent.charAt(tokenContent.length() - 1)))
				t.setType(tokenContent.charAt(tokenContent.length() - 1));
			else {/////////////////////////////////////////////////////////////
				t.setType(tokenContent.charAt(0));// just for testings
			} //////////////////////////////////////////////////////////////////
		} else if (node.getNodeName().equals("bond") && totalBondsFlag) {
			tokenBondFlag = true;
			if (bondTokens.size() != 0) {
//				System.out.println("aaaaa" + bondTokens.size());
				totalBonds.add(new Bond(bondTokens.get(0), bondTokens.get(1)));
				totalBonds = new ArrayList<Bond>();
			}
		} else if (node.getNodeName().equals("token") && tokenBondFlag) {
			bondTokens.add(new Token(node.getTextContent()));

//			System.out.println("aa" + node.getTextContent());
			if (bondTokens.size() > 1) {
				Bond b = null;
				b = new Bond(bondTokens.get(0), bondTokens.get(1));

				totalBonds.add(b);
				bondTokens = new ArrayList<Token>();
			}
		} else if (node.getNodeName().equals("bonds")) {
			if (arrowsFlag) {
				arrowBondsFlag = true;
				String bondsContent = node.getTextContent();
				arrowBondCounter = bondsContent.length() / 4; // every bond has 2 tokens of two-length string as name
				arrowBonds.clear();
				for (int i = 0, start = 0; i < arrowBondCounter; i++, start += 4) {
					arrowBonds.add(new Bond(new Token(bondsContent.substring(start, start + 2)),
							new Token(bondsContent.substring(start + 2, start + 4))));
				}
				Label label = new Label(arrowTokens, arrowBonds);
				arrowTokens = new ArrayList<Token>();
				arrowBonds = new ArrayList<Bond>();

				Arrow arr = new Arrow(new Token(source), new Token(destination), label);
				arrowList.add(arr);
			}
		} else if (node.getNodeName().equals("totalBonds")) {
			totalBondsFlag = true;
		} else if (node.getNodeName().equals("token") && totalBondsFlag) {

//			bondTokens.add(new Token(node.getTextContent()));
		}

		// Print the child nodes recursively
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			readXML(childNodes.item(i), level + 1);
		}
		s1 += s;
	}
}
