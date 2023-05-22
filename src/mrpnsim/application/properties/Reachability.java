package mrpnsim.application.properties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.controlsfx.control.ToggleSwitch;
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
import mrpnsim.application.model.MRPN;
import mrpnsim.application.model.Token;
import mrpnsim.application.simulator.ForwardExecution;
import mrpnsim.application.simulator.ReverseExecution;

public class Reachability extends AnchorPane {

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

	private MRPN mrpn;
	private ArrayList<Edge> path;

	public Reachability(MRPN mrpn) {
		this.mrpn = mrpn;
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/popupForReachability.fxml"));

		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			Parent root = fxmlLoader.load();
			Scene scene = new Scene(root);
			stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Check Reachability with Property");
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
			tokenList.add(token.getType() + "(" + typeCounter[types.indexOf(token.getType())]+")");
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
		boolean noOtherBonds = onlyBond.isSelected();
		if (selectedToken1 == null || selectedToken2 == null)
			return;
		if (selectedToken1.indexOf('(') == -1 || selectedToken2.indexOf('(') == -1 )
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
				for(String line : listViewTokens.getItems()) {
					String temp[] = line.split(" ");
					if(selectedToken1.equals(temp[0]))
						token1ToPlace = true;
					if(selectedToken2.equals(temp[0]))
						token2ToPlace = true;
				}
				if(token1ToPlace && token2ToPlace && noOtherBonds)
					listViewBonds.getItems().add(selectedToken1 + " - " + selectedToken2 + " (strengthened bond)");
				else if(token1ToPlace && token2ToPlace)
					listViewBonds.getItems().add(selectedToken1 + " - " + selectedToken2);
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

	HashMap<String, Set<Pair<String,Boolean>>> getBonds() {
		HashMap<String, Set<Pair<String,Boolean>>> tokenConnections = new HashMap<>();
		for (String line : listViewBonds.getItems()) {
			String temp[] = line.split(" ");
			boolean noBond = false;
			if(temp.length > 3)
				noBond = true;
			if (!tokenConnections.containsKey(temp[0])) {
				Set<Pair<String,Boolean>> con = new HashSet<>();
				Pair<String,Boolean> bond = new Pair<String,Boolean>(temp[2],noBond);
				con.add(bond);
				tokenConnections.put(temp[0], con);
			} else {
				Set<Pair<String,Boolean>> con = tokenConnections.get(temp[0]);
				Pair<String,Boolean> bond = new Pair<String,Boolean>(temp[2],noBond);
				con.add(bond);
				tokenConnections.replace(temp[0], con);
			}
			if (!tokenConnections.containsKey(temp[2])) {
				Set<Pair<String,Boolean>> con = new HashSet<>();
				Pair<String,Boolean> bond = new Pair<String,Boolean>(temp[0],noBond);
				con.add(bond);
				tokenConnections.put(temp[2], con);
			} else {
				Set<Pair<String,Boolean>> con = tokenConnections.get(temp[2]);
				Pair<String,Boolean> bond = new Pair<String,Boolean>(temp[0],noBond);
				con.add(bond);
				tokenConnections.replace(temp[2], con);
			}

		}

		return tokenConnections;
	}

	ArrayList<Edge> search() {
		HashMap<String, String> tokensIntoPlace = getTokens();
		HashMap<String, Set<Pair<String,Boolean>>> tokenConnections = getBonds();
		SearchMarking searchMarking = new SearchMarking(mrpn, tokensIntoPlace, tokenConnections);
		// System.out.println("Marking: "+searchMarking);

		ForwardExecution forward = new ForwardExecution(mrpn);
		ReverseExecution reverse = new ReverseExecution(mrpn);

		Search rs = new Search(mrpn, forward, reverse);
		ArrayList<Edge> path = rs.search(searchMarking);
		result.getChildren().clear();
		if (path != null) {

			StringBuilder sb = new StringBuilder();
			for (Edge edge : path) {
				sb.append("(");
				sb.append(edge.t);
				sb.append(",");
				if (edge.ex instanceof ForwardExecution)
					sb.append("forward");
				if (edge.ex instanceof ReverseExecution)
					sb.append("reverse");
				sb.append(")");
				sb.append(" -> ");
			}
			sb.append("End");

			result.getChildren().add(new Label("Path Found: " + sb.toString()));
			System.out.println(sb.toString());

		} else
			result.getChildren().add(new Label("No Path Found"));

		return path;

	}

	void apply() {
		System.out.println("apply mrpn!");
		if (path != null) {
			Edge lastEdge = path.get(path.size() - 1);
			Marking newMarking = lastEdge.destination.marking;
			mrpn.setMarking(newMarking);
			mrpn.scrollArea.refreshAll();
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
				path = search();
			else if (applyButton.getId().equals("applyButton"))
				apply();
		}
	};

}
