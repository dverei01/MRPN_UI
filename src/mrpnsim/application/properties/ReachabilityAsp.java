package mrpnsim.application.properties;

import javafx.util.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.TextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.File; // Import the File class
import java.io.IOException; // Import the IOException class to handle errors
import java.io.FileWriter; // Import the FileWriter class

import org.controlsfx.control.ToggleSwitch;
import org.w3c.dom.Document;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import mrpnsim.application.AutoCompleteComboBoxListener;
import mrpnsim.application.model.Marking;
import mrpnsim.application.model.Place;
import mrpnsim.application.model.Bond;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.model.Token;
import mrpnsim.application.simulator.ForwardExecution;
import mrpnsim.application.simulator.ReverseExecution;
import mrpnsim.application.ui.BondUI;
import mrpnsim.application.ui.PlaceUI;
import mrpnsim.application.ui.TokenUI;
import mrpnsim.application.ScrollArea;
import mrpnsim.application.reachabilityAsp.RunPython;
import mrpnsim.application.reachabilityAsp.debug;
import mrpnsim.application.reachabilityAsp.translator2;
import mrpnsim.application.MRPNXMLWriter;
import mrpnsim.application.MRPNXMLReader;

public class ReachabilityAsp extends AnchorPane {

	private Stage stage;

	@FXML
	private Button searchButton;
	@FXML
	private ListView<String> listViewTokens;
	@FXML
	private ComboBox<String> tokenCombo;
	@FXML
	private Button addToken;
	@FXML
	private Button removeToken;
	@FXML
	private ListView<String> listViewBonds;
	@FXML
	private ComboBox<String> comboToken1;
	@FXML
	private ComboBox<String> comboToken2;
	@FXML
	private ToggleSwitch onlyBond;
	@FXML
	private Button addBond;
	@FXML
	private Button removeBond;
	@FXML
	private ComboBox<String> placeCombo;
	@FXML
	private Button closeButton, applyButton;
	@FXML
	private AnchorPane result;

	AutoCompleteComboBoxListener<String> autoTokenCombo;
	AutoCompleteComboBoxListener<String> autoComboToken1;
	AutoCompleteComboBoxListener<String> autoComboToken2;
	AutoCompleteComboBoxListener<String> autoPlaceCombo;

	private ArrayList<String> holdsSteps = new ArrayList<String>();
	private int time = 10;
	private MRPN mrpn;
	private boolean applyFlag = false;
	private ArrayList<Edge> path;
	private int holdsStepsCounter = 1;
	private boolean initalIsFinalFlag = false;
	private boolean UNSAT = false;

	public ReachabilityAsp(MRPN mrpn) {
		System.out.println("Selected Popup for Asp");
		this.mrpn = mrpn;
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/popupForAspReachability.fxml"));

		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			Parent root = fxmlLoader.load();
			Scene scene = new Scene(root);
			stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Check Reachability with Asp Property");
			stage.setScene(scene);

		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		addToken.setOnAction(clicked);
		removeToken.setOnAction(clicked);
		addBond.setOnAction(clicked);
		removeBond.setOnAction(clicked);
		searchButton.setOnAction(clicked);
		closeButton.setOnAction(clicked);
		applyButton.setOnAction(clicked);
	}

	public EventHandler<ActionEvent> clickAction = new EventHandler<ActionEvent>() {
		@Override
		public void handle(final ActionEvent event) {
			tokenCombo.setItems(FXCollections.observableArrayList(initTokenList()));
			comboToken1.setItems(FXCollections.observableArrayList(initTokenList()));
			comboToken2.setItems(FXCollections.observableArrayList(initTokenList()));
			placeCombo.setItems(FXCollections.observableArrayList(initPlaceList()));

			autoTokenCombo = new AutoCompleteComboBoxListener<String>(tokenCombo);
			autoComboToken1 = new AutoCompleteComboBoxListener<String>(comboToken1);
			autoComboToken2 = new AutoCompleteComboBoxListener<String>(comboToken2);
			autoPlaceCombo = new AutoCompleteComboBoxListener<String>(placeCombo);
			stage.show();
		}
	};

	ArrayList<String> initTokenList() {
		ArrayList<String> tokenList = new ArrayList<String>();
		String t[] = mrpn.getTypes();
		ArrayList<String> types = new ArrayList<>();
		int typeCounter[] = new int[t.length];
		for (int i = 0; i < t.length; i++) {
			typeCounter[i] = 1;
			types.add(t[i]);
		}

		for (Token token : mrpn.getTokens()) {
			tokenList.add(token.getType() + "(" + typeCounter[types.indexOf(token.getType())] + ")");
			typeCounter[types.indexOf(token.getType())]++;
		}
		return tokenList;
	}

	ArrayList<String> initPlaceList() {
		ArrayList<String> placeList = new ArrayList<String>();
		for (Place place : mrpn.getPlaces())
			placeList.add(place.getName());
		return placeList;
	}

	void addToken() {
		String selectedToken = tokenCombo.getValue();
		String selectedPlace = placeCombo.getValue();
		if (selectedToken == null || selectedPlace == null)
			return;
		if (selectedToken.indexOf('(') == -1)
			return;

		String type = selectedToken.substring(0, selectedToken.indexOf('('));
		if (!mrpn.hasType(type) || !mrpn.hasNode(selectedPlace))
			return;

		boolean contain = false;
		for (String line : listViewTokens.getItems()) {
			String temp[] = line.split(" ");
			if (selectedToken.equals(temp[0])) {
				contain = true;
				break;
			}
		}

		if (!contain)
			listViewTokens.getItems().add(selectedToken + " " + "\u2208" + " M(" + selectedPlace + ")");

	}

	void addBond() {
		String selectedToken1 = comboToken1.getValue();
		String selectedToken2 = comboToken2.getValue();
		if (selectedToken1 == null || selectedToken2 == null)
			return;
		if (selectedToken1.indexOf('(') == -1 || selectedToken2.indexOf('(') == -1)
			return;

		String type1 = selectedToken1.substring(0, selectedToken1.indexOf('('));
		String type2 = selectedToken2.substring(0, selectedToken2.indexOf('('));
		if (!mrpn.hasType(type1) || !mrpn.hasType(type2))
			return;

		if (!selectedToken1.equals(selectedToken2)) {
			boolean contain = false;
			for (String line : listViewBonds.getItems()) {
				String temp[] = line.split(" - ");
				if (selectedToken1.equals(temp[0]) && selectedToken2.equals(temp[1])) {
					contain = true;
					break;
				}
				if (selectedToken1.equals(temp[1]) && selectedToken2.equals(temp[0])) {
					contain = true;
					break;
				}
			}

			if (!contain) {
				boolean token1ToPlace = false;
				boolean token2ToPlace = false;
				for (String line : listViewTokens.getItems()) {
					String temp[] = line.split(" ");
					if (selectedToken1.equals(temp[0]))
						token1ToPlace = true;
					if (selectedToken2.equals(temp[0]))
						token2ToPlace = true;
				}
				if (token1ToPlace && token2ToPlace)
					listViewBonds.getItems().add(selectedToken1 + " - " + selectedToken2);
//				else if (token1ToPlace && token2ToPlace)
//					listViewBonds.getItems().add(selectedToken1 + " - " + selectedToken2);
			}
		}

	}

	void delete(ListView<String> listView) {
		String selectedToken = listView.getSelectionModel().getSelectedItem();
		if (selectedToken != null) {
			listView.getItems().remove(selectedToken);
		}
	}

	HashMap<String, String> getTokens() {
		HashMap<String, String> tokensIntoPlace = new HashMap<>();
		for (String line : listViewTokens.getItems()) {
			String temp[] = line.split(" ");
			String token = temp[0];
			String place = temp[2].substring(2, temp[2].length() - 1);
			tokensIntoPlace.put(token, place);
		}

		return tokensIntoPlace;
	}

	HashMap<String, Set<Pair<String, Boolean>>> getBonds() {
		HashMap<String, Set<Pair<String, Boolean>>> tokenConnections = new HashMap<>();
		for (String line : listViewBonds.getItems()) {
			String temp[] = line.split(" ");
			boolean noBond = false;
			if (temp.length > 3)
				noBond = true;
			if (!tokenConnections.containsKey(temp[0])) {
				Set<Pair<String, Boolean>> con = new HashSet<>();
				Pair<String, Boolean> bond = new Pair<String, Boolean>(temp[2], noBond);
				con.add(bond);
				tokenConnections.put(temp[0], con);
			} else {
				Set<Pair<String, Boolean>> con = tokenConnections.get(temp[0]);
				Pair<String, Boolean> bond = new Pair<String, Boolean>(temp[2], noBond);
				con.add(bond);
				tokenConnections.replace(temp[0], con);
			}
			if (!tokenConnections.containsKey(temp[2])) {
				Set<Pair<String, Boolean>> con = new HashSet<>();
				Pair<String, Boolean> bond = new Pair<String, Boolean>(temp[0], noBond);
				con.add(bond);
				tokenConnections.put(temp[2], con);
			} else {
				Set<Pair<String, Boolean>> con = tokenConnections.get(temp[2]);
				Pair<String, Boolean> bond = new Pair<String, Boolean>(temp[0], noBond);
				con.add(bond);
				tokenConnections.replace(temp[2], con);
			}

		}

		return tokenConnections;
	}

	private String toStringForBonds(HashMap<String, Set<Pair<String, Boolean>>> tokenConnections) {
		StringBuilder mapAsString = new StringBuilder("{\n");
		for (String key : tokenConnections.keySet()) {
			mapAsString.append("").append(key).append("=[");
			Set<Pair<String, Boolean>> set = tokenConnections.get(key);
			for (Pair<String, Boolean> pair : set) {
				mapAsString.append(pair.getKey()).append(", ");
			}
			if (!set.isEmpty()) {
				mapAsString.setLength(mapAsString.length() - 2); // Remove the last comma and space
			}
			mapAsString.append("]\n");
		}
		mapAsString.append("}");
		return mapAsString.toString().replace("(", "").replace(")", "");
	}

	private String getAspCodeForTokens(String mapAsString, String bonds) {

		System.out.println(bonds);
//		mapAsString.replace("{", "").replace("}", "");
		String[] assignments = mapAsString.split(",\\s*");
		String output = "";

//		for(int i = 0 ;i<assignments.length;i++)
//			System.out.println("Test"+assignments[i]);
		for (String assignment : assignments) {
			String[] parts = assignment.split("=");
			String variable = parts[0];
			String value = parts[1];
			String index = variable.substring(variable.indexOf("(") + 1, variable.indexOf(")"));
			String name = variable.substring(0, variable.indexOf("("));
			// String nameOfToken = ""
			if (!bonds.contains(name.concat(index))) {
//				Place p[] = mrpn.getPlaces();
//				for (int i = 0; i < p.length; i++) {
//					Token t[] = p[i].getTokens();
//					for(int j = 0 ; j<t.length;j++)
//						System.out.println("Name + Type:"+t[j].name +"   " +t[j].type);

				// System.out.println(mrpn.getTokens()[0].name + "Type" +
				// mrpn.getTokens()[0].type);

				output += "holds(" + value + "," + name + index + "," + "TS" + ").\n";
				// }
			}
		}
		return output;
	}

	public static String mergeAdjacentCommas(String input) {
		StringBuilder mergedString = new StringBuilder();
		String[] lines = input.split("\\r?\\n");

		for (String line : lines) {
			StringBuilder mergedLine = new StringBuilder();
			int i = 0;

			while (i < line.length()) {
				char currentChar = line.charAt(i);
				mergedLine.append(currentChar);

				if (currentChar == ',') {
					while (i < line.length() - 1 && line.charAt(i + 1) == ',') {
						i++;
					}
				}
				i++;
			}

			mergedString.append(mergedLine).append(System.lineSeparator());
		}

		return mergedString.toString();
	}

	private String modifyTransitionsForTokens(String input) {
		String[] lines = input.split("\\r?\\n"); // Split the input into lines

		HashMap<Character, String> typeMap = new HashMap<>(); // Store variable types

		StringBuilder output = new StringBuilder(); // Modified output

		// Process each line
		for (String line : lines) {
			// If the line starts with "type", extract variable and type information
			if (line.startsWith("type")) {
				String[] parts = line.split("[(),]+");
				String type = parts[2];
				String variable = parts[3];
				typeMap.put(variable.charAt(0), type);
			}
			// If the line starts with "ptarc" or "tparc" (excluding "ptarcb" and "tparcb"),
			// modify the line
			else if ((line.startsWith("ptarc") || line.startsWith("tparc")) && !line.startsWith("ptarcb")
					&& !line.startsWith("tparcb")) {
				String[] parts = line.split("[(),]+");
				String variable = parts[4];
				String type = typeMap.get(variable.charAt(0));
				if (type != null) {
					line = line.replace(parts[5], type);
					line = line.replace(parts[6], String.valueOf(variable.charAt(0)));
				}
			}
			// Append the line (modified or original) to the output
			output.append(line).append("\n");
		}

		return output.toString(); // Return the modified output

	}

	public static String modifyTransitionsForBonds(String inputString) {
		// Splitting the string into lines
		String[] lines = inputString.split("\\r?\\n");
		System.out.println(lines);
		// Modifying each line
		StringBuilder modifiedString = new StringBuilder();
		for (String line : lines) {
			if (line.startsWith("tparcb(") || line.startsWith("ptarcb(")) {
				String[] elements = line.split(",");
				System.out.println(line);
				elements[2] = elements[2] + "," + getOnlyLetters(elements[2]);
				elements[3] = elements[3].substring(0, elements[3].length() - 2) + "," + getOnlyLetters(elements[3])
						+ ").";
				modifiedString.append(String.join(",", elements)).append("\n");
			} else {
				modifiedString.append(line).append("\n");
			}
		}

		return modifiedString.toString();
	}

	public static String getOnlyLetters(String inputString) {
		// Regular expression pattern to match letters
		Pattern pattern = Pattern.compile("[a-zA-Z]+");
		Matcher matcher = pattern.matcher(inputString);

		StringBuilder letters = new StringBuilder();
		while (matcher.find()) {
			letters.append(matcher.group());
		}

		return letters.toString();
	}

	private String getAspCodeForBonds(String mapAsString, String tokens) {
		tokens = tokens.replaceAll("\\(", "").replaceAll("\\)", "");
		System.out.println("___________________________________________________________" + tokens);
		// Remove curly braces and split into key-value pairs
		String[] pairs = tokens.replace("{", "").replace("}", "").split(", ");

		// Create a HashMap to store the values
		HashMap<String, String> map = new HashMap<>();

		// Iterate over the pairs and add them to the map
		for (String pair : pairs) {
			String[] parts = pair.split("=");
			String key = parts[0].trim();
			String value = parts[1].trim();
			map.put(key, value);
		}

		String[] lines = mapAsString.split("\\r?\\n");
		ArrayList<String> result = new ArrayList<>();
		Set<String> unique = new HashSet<>();

		for (String line : lines) {
			if (line.trim().isEmpty() || !line.contains("=")) {
				continue; // skip empty lines and lines without an equal sign
			}
			String[] parts = line.split("=");
			String lhs = parts[0];
			String[] rhs = parts[1].replaceAll("[\\[\\]]", "").split(", ");

			for (String bond : rhs) {
				String place = "";
				System.out.println("__________________________");
				System.out.println("place1" + map.get("A(1)"));
				String place1 = map.get(lhs);
				String place2 = map.get(bond);
				if (place1.equals(place2))
					place = place1;
				else
					break;
				String current = String.format("holdsbonds(%s,%s,%s,TS).", place, lhs, bond);
				if (!unique.contains(current)) {
					result.add(current);
					unique.add(current);
				}

				if (!bond.matches("\\d+\\.?\\d*")) {
					current = String.format("holdsbonds(%s,%s,%s,TS).", place, bond, lhs);
					if (!unique.contains(current)) {
						result.add(current);
						unique.add(current);
					}
				}
			}
		}

		// Remove duplicates
		ArrayList<String> distinct = new ArrayList<>(new HashSet<>(result));
		result.clear();

		// Keep only the first occurrence of reverse pairs
		Set<String> reversePairs = new HashSet<>();
		for (String line : distinct) {
			String[] parts = line.split(",");
			String lhs = parts[1].trim();
			String rhs = parts[2].trim();
			String current = String.format("holdsbonds(%s,%s,%s,TS)", map.get(lhs), lhs, rhs);

			if (reversePairs.contains(current)) {
				continue;
			} else if (line.startsWith("holdsbonds(" + map.get(lhs) + "," + lhs)) {
				reversePairs.add(String.format("holdsbonds(%s,%s,%s,TS)", map.get(lhs), rhs, lhs));
				result.add(line);
			} else {
				reversePairs.add(current);
				result.add(line);
			}
		}
		String goalResult = String.join(" ", result).replaceAll(" ", ".\n");
		return mergeAdjacentCommas(goalResult);
	}

	private String produceGoal(String tokens, String bonds) {
		return this.getAspCodeForTokens(tokens, bonds) + this.getAspCodeForBonds(bonds, tokens);
	}

	public static String removeLast(String str, String search, String replace) {
		int pos = str.lastIndexOf(search);

		if (pos > -1) {
			return str.substring(0, pos) + replace + str.substring(pos + search.length(), str.length());
		}

		return str;
	}

	public static boolean haveSameStartBeforeLastComma(String s1, String s2) {
		int index1 = s1.lastIndexOf(",");
		int index2 = s2.lastIndexOf(",");

		// If either string doesn't contain a comma, return false
		if (index1 == -1 || index2 == -1) {
			return false;
		}

		String substr1 = s1.substring(0, index1);
		String substr2 = s2.substring(0, index2);

		return substr1.equals(substr2);
	}

	/****
	 * 
	 * Here I have to change the search method in order to go to asp translator I
	 * have to read TmpPropSave
	 * 
	 * @throws IOException
	 */
	ArrayList<Edge> search() throws IOException {
		time = this.mrpn.getPlaces().length * this.mrpn.getTransitions().length;
		HashMap<String, String> tokensIntoPlace = getTokens();// {A(1)=p1}
		String tokensToString = tokensIntoPlace.toString();

		System.out.println("aaa" + tokensToString);
		if (tokensIntoPlace.isEmpty()) {
			result.getChildren().clear();
			result.getChildren().add(new Label("Satisfiable situation. You did not insert a token as goal though."));

			return null;
		}
		String aspCodeForTokens = getAspCodeForTokens(tokensToString.substring(1, tokensToString.length() - 1),
				this.toStringForBonds(getBonds()));
		System.out.println("AspFORTOKENS" + aspCodeForTokens);
		HashMap<String, Set<Pair<String, Boolean>>> tokenConnections = getBonds();
		String bondsToString = this.toStringForBonds(tokenConnections);
		String aspCodeForBonds = getAspCodeForBonds(bondsToString, tokensToString);
		SearchMarking searchMarking = new SearchMarking(mrpn, tokensIntoPlace, tokenConnections);
		String goalAspCode = this.produceGoal(tokensToString.substring(1, tokensToString.length() - 1), bondsToString);
		System.out.println(this.produceGoal(tokensToString.substring(1, tokensToString.length() - 1), bondsToString));
		String translated = "";
		try {
			// Parse the XML file
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse("TmpPropSave.xml");
			translator2 t = new translator2();
			t.readXML(doc, 0);
			// t.printMrpnDetails();
//			t.readAndCopyLpCode();

			translated = t.translateToAsp();
			translated = modifyTransitionsForTokens(translated);
			translated = modifyTransitionsForBonds(translated);
			System.out.println(translated);

		} catch (Exception e) {
			e.printStackTrace();
		}

		goalAspCode = fixUiIssue(translated, goalAspCode);

		File scenarioFile = new File("MRPN_Scenario.lp");
		FileWriter myWriter = new FileWriter("MRPN_Scenario.lp");
		System.out.println(translated);
		myWriter.write(translated.toLowerCase());
		System.out.println("Translated" + translated);
		myWriter.close();
//
//		String goalAsString = goalAspCode;

		try {
			FileWriter writer = new FileWriter("MRPN_Scenario.lp", true);
			writer.write("time(0.." + time + ").\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("DEBUG");

//		
//		goalAspCode 

		try {
			File goalFile = new File("goal.lp");
			FileWriter writer = new FileWriter(goalFile);
			System.out.println(goalAspCode);
			// writer.write(goalAspCode.toLowerCase());
			String goal = goalAspCode.replace(".", ",");

			goal = "goal:- " + goal + " ";
			// goal = goal.substring(0, goal.length()-3) + "." +
			// goal.substring(goal.length()+1);
			goal = goal + "\n:- not goal.";
			goal = removeLast(goal, ",", ",TS<" + time + ".");
			goal = mergeAdjacentCommas(goal.toLowerCase().replace("ts", "TS"));
			writer.write(goal);
			System.out.println(goal);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String clingoResponse = RunPython.run("python", "resources/test_work.py", "clingo.exe", "MRPN_Scenario.lp",
				"aspCode.lp", "goal.lp", "2");

		goalAspCode = goalAspCode.toLowerCase();
		if (!clingoResponse.contains("UNSATISFIABLE")) {

			String moves = clingoResponse.substring(clingoResponse.indexOf("Answer: ") + 9,
					clingoResponse.indexOf("SATISFIABLE"));
			System.out.println(goalAspCode + "aaaaaaaaaaaaaaaaaaaa");
			String goalLines[] = goalAspCode.split("\\.");

			String mainMoves2 = "";

			String temp = "";
			String substring = "";
			ArrayList<String> gLines = new ArrayList<String>();
			for (int i = 0; i < goalLines.length; i++)
				if (goalLines[i].length() > 3)
					gLines.add(goalLines[i]);
			int goalCounter = 0;

			for (int i = 0; i < gLines.size(); i++) {

				System.out.println("SO WE CHECK FOR" + gLines.get(i));

				temp = extractSubstringUpToLastOccurrence(moves, gLines.get(i));
				if (temp != null) {
					if (temp.length() > substring.length()) {
						goalCounter++;
						substring = temp;
					}
					if (goalCounter == gLines.size())
						break;
				}
			}
			System.out.println(goalCounter + " askmwelknmewlk" + gLines.size());
			mainMoves2 = substring;

			System.out.println(moves);
			// ArrayList<String> gLines = new ArrayList<String>();
			for (int i = 0; i < goalLines.length; i++) {
				System.out.println(goalLines[i]);
				if (goalLines[i].length() > 3) {
					gLines.add(goalLines[i]);
					System.out.println(gLines.get(i) + "AAAAAAAAAAAAAAAAAAA");
				}
			}

			boolean exists[] = new boolean[gLines.size()];
			for (int i = 0; i < exists.length; i++) {
				exists[i] = false;

			}

			String mainMoves = "";
			boolean flag = false;

			System.out.println("PPPPPPPPPPPPP" + moves);

			System.out.println("Here ARE THE MAIN MOVES" + "\n" + mainMoves2);
			if (mainMoves2.indexOf(',') != -1) {
				String lastTimestamp = mainMoves2.substring(mainMoves2.lastIndexOf(',') + 1,
						mainMoves2.lastIndexOf(')'));
				time = Integer.parseInt(lastTimestamp);
				System.out.println("OK");
				int startOfFires = clingoResponse.indexOf("->Moves type Fires:");
				int endOfFires = clingoResponse.indexOf("->Moves type Satisfied:");
				String fires = clingoResponse.substring(startOfFires, endOfFires);
				// System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + fires);
				fires = fires.substring(fires.indexOf(':') + 1);

				// I have to ask for this line
				// if (fires.contains("firesb"))
				// fires = fires.substring(0, fires.indexOf("firesb"));
				String labelResult = "";

				int startOfHolds = clingoResponse.indexOf("->Moves type Holds:");
				int endOfHolds = clingoResponse.indexOf("->Moves type Enabled:");
				String holds = clingoResponse.substring(startOfHolds, endOfHolds);

				int startOfHoldsBonds = clingoResponse.indexOf("->Moves type Holds_Bonds:");
				String holdsbonds = clingoResponse.substring(startOfHoldsBonds);

				String holdsArr[] = null, holdsbondsArr[] = null;

				if (holdsbonds.contains("holdsbonds")) {
					holdsbonds = holdsbonds.substring(holdsbonds.indexOf(':') + 1);
					holdsbondsArr = holdsbonds.split(">>");
				}

				if (holds.contains("holds")) {
					holds = holds.substring(holds.indexOf(':') + 1);
					holdsArr = holds.split(">>");
				}
				System.out.println("aaaaaaaaaaaaaaaadslkndjojwl.");
				if (holdsArr != null)
					for (int i = 0; i < holdsArr.length; i++) {
						if (Integer.parseInt(holdsArr[i].split(" ")[2]) <= Integer.parseInt(lastTimestamp)) {
							holdsSteps.add(holdsArr[i].split(" ")[0]);
							System.out.println(holdsArr[i].split(" ")[0]);
						}
					}
				if (holdsbondsArr != null)
					for (int i = 0; i < holdsbondsArr.length; i++) {
						if (Integer.parseInt(holdsbondsArr[i].split(" ")[2]) <= Integer.parseInt(lastTimestamp)) {
							holdsSteps.add(holdsbondsArr[i].split(" ")[0]);
							System.out.println(holdsbondsArr[i].split(" ")[0]);
						}
					}
				sortHoldsList();
				for (int i = 0; i < holdsSteps.size(); i++)
					System.out.println(holdsSteps.get(i) + "ldsmkdfm");
				String str = "";
				str = filterMovesByTimeStamp(holds, lastTimestamp);
				System.out.println(fires);
				int timeForFire = Integer.parseInt(lastTimestamp) - 1;
				str = filterMovesByTimeStamp(fires, "" + timeForFire);
				System.out.println(str);
				System.out.println(holdsbonds);

				System.out.println("ONLY THE HOLDS I NEED" + str);
				fires = filterMovesByTimeStamp(fires, "" + timeForFire);
				if (fires.contains(">>")) {
					String fireLines[] = fires.split(">>");
					// System.out.println(clingoResponse);

					for (int i = 0; i < fireLines.length; i++)
						System.out.println(fireLines[i]);
					labelResult = getLabelResult(fireLines);

				} else if (fires.length() > 2) {
					String fireLines[] = new String[1];
					fireLines[0] = fires;
					// System.out.println(clingoResponse);

					for (int i = 0; i < fireLines.length; i++)
						System.out.println(fireLines[i]);
					labelResult = getLabelResult(fireLines);

				}
				result.getChildren().clear();
				result.getChildren().add(new Label("Path Found: " + labelResult + " End."));
				if (labelResult.length() == 0) {
					initalIsFinalFlag = true;
				}
			} else {

				result.getChildren().clear();
				result.getChildren().add(new Label("Satisfiable situation but Clingo cannot find the path"));
				String stepsFromGoal[] = goalAspCode.trim().split("\\.");
				System.out.println(goalAspCode);
				for (int i = 0; i < stepsFromGoal.length; i++) {
					System.out.println("SAT Situation" + stepsFromGoal[i] + goalAspCode);
					holdsSteps.add(stepsFromGoal[i].substring(0, stepsFromGoal[i].lastIndexOf(',') + 1) + "1)");
					System.out.println(holdsSteps.get(holdsSteps.size() - 1));
				}

			}
		} else

		{
			result.getChildren().clear();
			result.getChildren().add(new Label("No Path Found"));
			UNSAT = true;
		}

		return path;

	}

	private void sortHoldsList() {
		holdsSteps.sort((s1, s2) -> {
			String lastNumStr1 = s1.substring(s1.lastIndexOf(",") + 1, s1.lastIndexOf(")"));
			String lastNumStr2 = s2.substring(s2.lastIndexOf(",") + 1, s2.lastIndexOf(")"));
			int lastNum1 = Integer.parseInt(lastNumStr1);
			int lastNum2 = Integer.parseInt(lastNumStr2);
			return Integer.compare(lastNum1, lastNum2);
		});
	}

	private String filterMovesByTimeStamp(String input, String threshold) {
		String[] holds = input.split(">>");
		StringBuilder sb = new StringBuilder();
		for (String h : holds) {
			if (h.lastIndexOf(",") != -1) {
				int lastIndex = h.lastIndexOf(",");
				int lastDigit = Integer.parseInt(h.substring(lastIndex + 1, h.indexOf(')')));
				int thresholdValue = Integer.parseInt(threshold);
				if (lastDigit <= thresholdValue) {
					sb.append(h.trim()).append(" >> ");
				}
			}
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 4, sb.length()); // remove the last ">> "
		}
		return sb.toString();
	}

	public String extractSubstringUpToLastOccurrence(String input, String searchString) {
		int lastIndex = input.indexOf(searchString.substring(0, searchString.lastIndexOf(',')));
		if (lastIndex != -1) {
			return input.substring(0, lastIndex + searchString.length() - 1);
		} else {
			return null;
		}
	}

	private String fixUiIssue(String translated, String goalAspCode) {

		String types = getOnlyTypesNumbered(translated);
		System.out.println("TYPESSSSSSSS" + types);
		String[] lines = types.split("\n");
		ArrayList<String> type1List = new ArrayList<>();
		ArrayList<String> type2List = new ArrayList<>();

		for (String line : lines) {
			if (line.contains("type")) {
				String[] parts = line.split(",");
				String type1 = parts[0].substring(parts[0].indexOf("(") + 1);
				String type2 = parts[1].substring(0, parts[1].indexOf(")"));
				type1List.add(type1);
				type2List.add(type2);
				System.out.println(type1 + " and " + type2);

			}
		}

		String[] type1Array = type1List.toArray(new String[type1List.size()]);
		String[] type2Array = type2List.toArray(new String[type2List.size()]);

		String[] goalLines = goalAspCode.split("\n");
		String result = "";

		for (int i = 0; i < goalLines.length; i++) {
			if (goalLines[i].contains("holdsbonds")) {
				int firstCommaIndex = goalLines[i].indexOf(',');
				int secondCommaIndex = goalLines[i].indexOf(',', firstCommaIndex + 1);

				String firstArg = goalLines[i].substring(firstCommaIndex + 1, secondCommaIndex);
				String secondArg = goalLines[i].substring(secondCommaIndex + 1,
						goalLines[i].indexOf(',', secondCommaIndex + 1));

				for (int j = 0; j < type1Array.length; j++) {
					System.out.println(firstArg + "dojsdnkjfnekfjernk" + type1Array[j]);
					System.out.println(secondArg + "dojsdnkjfnekfjernk" + type1Array[j]);

					if (firstArg.equals(type1Array[j])) {
						goalLines[i] = goalLines[i].replaceAll(firstArg, type2Array[j]);
						System.out.println(goalLines[i]);

					} else if (secondArg.equals(type1Array[j])) {
						goalLines[i] = goalLines[i].replaceAll(secondArg, type2Array[j]);
						System.out.println(goalLines[i]);
					}
				}

			} else if (goalLines[i].contains("holds")) {
				System.out.println(goalLines[i]);
				int firstComma = goalLines[i].indexOf(',');
				int secondComma = goalLines[i].indexOf(',', firstComma + 1);
				String argument = goalLines[i].substring(firstComma + 1, secondComma);

				for (int j = 0; j < type1Array.length; j++) {
					System.out.println(argument + "dojsdnkjfnekfjernk" + type1Array[j]);
					if (argument.equals(type1Array[j])) {
						System.out.println("Change");
						goalLines[i] = goalLines[i].replaceAll(argument, type2Array[j]);
					}
				}

			}
			System.out.println("LINE 500000");

			System.out.println(goalLines[i]);
			result += goalLines[i] + "\n";
		}

		return result;
	}

	private String getOnlyTypesNumbered(String input) {
		String[] lines = input.split("\n");
		String prevType = null;
		int counter = 1;
		StringBuilder result = new StringBuilder();

		for (String line : lines) {
			if (line.contains("type")) {
				String[] parts = line.split(",");
				String currentType = parts[0].substring(parts[0].indexOf("(") + 1);

				if (currentType.equals(prevType)) {
					counter++;
				} else {
					counter = 1;
				}

				prevType = currentType;

				result.append(line.substring(0, line.indexOf("(") + 1)).append(currentType).append(counter).append(",")
						.append(parts[1]).append("\r\n");
			}
		}

		return result.toString();
	}

	private String getLabelResult(String[] fireLines) {
		String labelRes = "";
		for (int i = 0; i < fireLines.length; i++) {
			if (fireLines[i].contains("firesb"))
				labelRes += fireLines[i].substring(fireLines[i].indexOf('('), fireLines[i].indexOf(',')) + ",backward)";
			else if (fireLines[i].contains("fires"))
				labelRes += fireLines[i].substring(fireLines[i].indexOf('('), fireLines[i].indexOf(',')) + ",forward)";
			labelRes += "->";
			if (i % 5 == 4) {
				labelRes += "\n";
			}
		}
		return labelRes;

	}

	void addBond(Token tokenA, Token tokenB, String nameOfPlace) {
		PlaceUI u = new PlaceUI(mrpn);
		TokenUI t1Ui = new TokenUI(mrpn, u, tokenA);
		TokenUI t2Ui = new TokenUI(mrpn, u, tokenB);

		BondUI bondUi = new BondUI(t1Ui, t2Ui);
		mrpn.moveToken(mrpn.getPlace(nameOfPlace), tokenA);

		mrpn.scrollArea.refreshAll();

	}

	public String[] parseFunctionHolds(String functionCall, int numArgs) {
		// Split the function call at the opening and closing parentheses
		String funcParts[] = functionCall.split("\\(")[1].split("\\)")[0].split(",");

		// Select the first numArgs elements from the array of function arguments
		String argArray[] = Arrays.copyOfRange(funcParts, 0, numArgs);

		return argArray;
	}

	public String[] parseFunctionHoldsBonds(String functionCall, int n) {
		// Split the function call at the opening and closing parentheses
		String funcParts[] = functionCall.split("\\(")[1].split("\\)")[0].split(",");

		// Select the first n elements from the array of function arguments
		String argArray[] = Arrays.copyOfRange(funcParts, 0, n);

		return argArray;
	}

	void apply() {
		if (!UNSAT) {
			if (!initalIsFinalFlag || holdsSteps.size() == 0) {
				ScrollArea scr = mrpn.getScrollArea();
				if (applyFlag)
					holdsStepsCounter = time;
				Place places[] = mrpn.getPlaces();
				Token t[] = mrpn.getTokens();
				String tokenTypes[] = new String[t.length];
				boolean moved[] = new boolean[t.length];
				Place tokenPlaceBeforeApply[] = new Place[t.length];
				for (int i = 0; i < t.length; i++) {
					moved[i] = false;
					tokenPlaceBeforeApply[i] = t[i].getPlace();
					tokenTypes[i] = t[i].getType();
				}
				for (int i = 0; i < t.length; i++) {
					String p = t[i].getPlace().name;
					mrpn.removeToken(t[i]);
					System.out.println(t[i].getName() + " - " + t[i].getType() + " - " + p);
				}
				mrpn.scrollArea.refreshAll();

				for (int i = 0; i < holdsSteps.size(); i++) {
					if (holdsSteps.get(i).indexOf(',') != -1) {
						if (holdsSteps.get(i)
								.substring(holdsSteps.get(i).lastIndexOf(',') + 1, holdsSteps.get(i).indexOf(')'))
								.equals("" + holdsStepsCounter)) {
							if (holdsSteps.get(i).contains("holdsbonds")) {
								String s[] = parseFunctionHoldsBonds(holdsSteps.get(i), 3);// to do

								for (int j = 0; j < t.length; j++)
									if (t[j].name.equals(s[1])) {
										for (int k = 0; k < t.length; k++) {
											if (t[k].name.equals(s[2]))
												for (int l = 0; l < places.length; l++)
													if (places[l].name.equals(s[0]))
														addBond(t[j], t[k], s[0]);
										}
									}

							} else if (holdsSteps.get(i).contains("holds")) {
								String s[] = parseFunctionHolds(holdsSteps.get(i), 2);
//					System.out.println(mrpn.getToken("b1"));

								for (int j = 0; j < t.length; j++) {
									if (t[j].name.equals(s[1])) {
										Token t1 = new Token(mrpn, mrpn.getPlace(s[0]), s[1], t[j].type);
										mrpn.addToken(mrpn.getPlace(s[0]), t1.name, t1.type);

									}
								}
							}
						}
					}
					mrpn.scrollArea.refreshAll();
					if (time == holdsStepsCounter - 1) {
						holdsStepsCounter--;
						applyFlag = true;

					}
					if (mrpn.getTokens().length == 0) {
						mrpn.scrollArea = scr;
						mrpn.scrollArea.refreshAll();

					}

				}

				holdsStepsCounter++;
			}

			System.out.println("apply mrpn!");
		} else {
			System.out.println("Unsatisfiable Situation No Apply Effect");
		}
	}

	private EventHandler<ActionEvent> clicked = new EventHandler<ActionEvent>() {
		@Override
		public void handle(final ActionEvent event) {
			Button selectedButton = (Button) event.getSource();
			if (selectedButton.getId().equals("closeButton"))
				stage.close();
			else if (selectedButton.getId().equals("addToken"))
				addToken();
			else if (selectedButton.getId().equals("addBond"))
				addBond();
			else if (selectedButton.getId().equals("removeToken"))
				delete(listViewTokens);
			else if (selectedButton.getId().equals("removeBond"))
				delete(listViewBonds);
			else if (selectedButton.getId().equals("searchButton"))
				try {
					path = search();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else if (applyButton.getId().equals("applyButton"))
				apply();
		}
	};

}
