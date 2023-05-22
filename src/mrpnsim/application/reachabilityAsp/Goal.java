package mrpnsim.application.reachabilityAsp;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class Goal {
//	public static String pathForFiles = "C:\\Users\\dvere\\eclipse-workspace\\test\\";

	String goalName = "goal";

	public Goal(int number) {
		goalName += number;

	}

	public String getGoal() {

		String goal = goalName + " :- ";
		return goal;
	}

	public static void main(String[] args) throws IOException {
//	try {
//		// Parse the XML file
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//		DocumentBuilder builder = factory.newDocumentBuilder();
//		// Document doc = builder.parse("Rafail_Temp.xml");
//		Document doc = builder.parse(pathForFiles + "TmpSimSave.xml");
//
////		Document doc = builder.parse("temp.xml");
//		// Traverse the XML document and print the tree
//		translator2 t = new translator2();
//		t.readXML(doc, 0);
//		t.printMrpnDetails();
//		t.();
//		t.translateToAsp();
////		System.out.println("Goal");
////		Document doc2 = builder.parse(pathForFiles + "temp.xml");
//////		Document doc = builder.parse("temp.xml");
////		// Traverse the XML document and print the tree
////		translator2 t2 = new translator2();
////		t2.readXML(doc2, 0);
////		t2.printMrpnDetails();
////		t2.readAndCopyLpCode();
////		t2.translateToAsp();
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
	}
}
