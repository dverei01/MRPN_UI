package mrpnsim.application.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import mrpnsim.application.model.Arrow;
import mrpnsim.application.model.LabelItem;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.model.Token;
import mrpnsim.application.model.Transition;
import mrpnsim.application.simulator.MRPNUtils;

public class ArcLabelUI extends AnchorPane {

	@FXML
	private Button okButton;

	// Tokens
	@FXML
	private ListView<String> listViewTokens;
	@FXML
	private ComboBox<String> tokenCombo;
	AutoCompleteComboBoxListener<String> autoTokenCombo;
	AutoCompleteComboBoxListener<String> autoComboToken1;
	AutoCompleteComboBoxListener<String> autoComboToken2;

	@FXML
	private Button addToken;
	@FXML
	private Button removeToken;

	// Bonds
	@FXML
	private ListView<String> listViewBonds;
	@FXML
	private ComboBox<String> comboToken1;
	@FXML
	private ComboBox<String> comboToken2;
	@FXML
	private Button addBond;
	@FXML
	private Button removeBond;

	private Stage stage;
	private MRPN mrpn;

	private ArrowUI arc;

	public ArcLabelUI(MRPN mrpn) {
		this.mrpn = mrpn;
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/popupForLabelArc.fxml"));

		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			Parent root = fxmlLoader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add("/popupForLabelArcStyling.css");
			stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Edit Label");
			stage.setScene(scene);

		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

	}

	public void showPopup(ArrowUI arc) {

		this.arc = arc;

		listViewTokens.getItems().clear();
		listViewBonds.getItems().clear();

		tokenCombo.setItems(FXCollections.observableArrayList(initTokenList()));
		comboToken1.setItems(FXCollections.observableArrayList(initTokenForBondList()));
		comboToken2.setItems(FXCollections.observableArrayList(initTokenForBondList()));

		autoTokenCombo = new AutoCompleteComboBoxListener<String>(tokenCombo);
		autoComboToken1 = new AutoCompleteComboBoxListener<String>(comboToken1);
		autoComboToken2 = new AutoCompleteComboBoxListener<String>(comboToken2);

		for (LabelItem item : arc.getSet()) {

			if (item.isToken()) {
				if (this.arc.getDestination().myNode instanceof Transition) 
					addToken(item.getTokenType());
				else
					addToken(item.getTokenName());
			}
			else {
				Pair<String, String> bond = item.getBond();
				addBond(bond.getKey(), bond.getValue());
			}
		}

		stage.show();

	}

	ArrayList<String> initTokenList() {
		ArrayList<String> typeList = new ArrayList<String>();
		ArrowUI arrow = this.arc;
		if (arrow.getDestination().myNode instanceof Transition) {
			for (String type : mrpn.getTypes())
				typeList.add(type);
		} else {
			Set<Arrow> arrowSet = mrpn.getNodeArrows(arrow.getSource().myNode);
			Arrow[] arrowArray = new Arrow[arrowSet.size()];
			arrowSet.toArray(arrowArray);
			for (Arrow a : arrowArray)
				if (a.getDestination() instanceof Transition) {
					ArrayList<LabelItem> tokens = a.getTokens();
					for (LabelItem token : tokens)
						typeList.add(token.getTokenName());
				}
		}
		return typeList;
	}

	ArrayList<String> initTokenForBondList() {
		ArrayList<String> typeList = new ArrayList<String>();
		ArrowUI arrow = this.arc;
		if (arrow.getDestination().myNode instanceof Transition) {
			ArrayList<LabelItem> tokens = arrow.myArrow.getTokens();
			for (LabelItem token : tokens)
				typeList.add(token.getTokenName());
		} else {
			Set<Arrow> arrowSet = mrpn.getNodeArrows(arrow.getSource().myNode);
			Arrow[] arrowArray = new Arrow[arrowSet.size()];
			arrowSet.toArray(arrowArray);
			for (Arrow a : arrowArray)
				if (a.getDestination() instanceof Transition) {
					ArrayList<LabelItem> tokens = a.getTokens();
					for (LabelItem token : tokens)
						typeList.add(token.getTokenName());
				}
		}
		return typeList;
	}

	void addToken(String tokenType) {
		String selectedToken = tokenType;

		// if (!mrpn.hasType(tokenType))
		// return;
		if (this.arc.getDestination().myNode instanceof Transition) {
			if (selectedToken != null) {
				listViewTokens.getItems().add(selectedToken);
			}
		} else if (!listViewTokens.getItems().contains(selectedToken)) {
			if (selectedToken != null) {
				List<String> list_items = listViewBonds.getItems();
				for (String item : list_items) {
					if (item.substring(0, item.indexOf('-')-1).equals(selectedToken) || item.substring(item.indexOf('-')+2,item.length()).equals(selectedToken))
						return;

				}
				listViewTokens.getItems().add(selectedToken);
			}
		}
	}

	void addToken() {
		addToken(tokenCombo.getValue());
		ArrowUI arrow = this.arc;
		if (arrow.getDestination().myNode instanceof Transition) {
			String type = tokenCombo.getValue();
			ArrayList<Integer> count = new ArrayList<>();
			if (arrow.getDestination().myNode instanceof Transition) {
				Set<Arrow> arrowSet = mrpn.getNodeArrows(arrow.getDestination().myNode);
				Arrow[] arrowArray = new Arrow[arrowSet.size()];
				arrowSet.toArray(arrowArray);
				for (Arrow a : arrowArray)
					if (a.getDestination() instanceof Transition) {
						ArrayList<LabelItem> tokens = a.getTokens();
						for (LabelItem token : tokens)
							if (token.getTokenType().equals(type))
								count.add(token.getIdA());  
					}
			}
			int i = 1;
			do {
				if (!count.contains(i) && !comboToken1.getItems().contains(type + i)) {
					break;
				}
				i++;
			} while (true);
			comboToken1.getItems().add(type + i);
			comboToken2.getItems().add(type + i);
			autoComboToken1 = new AutoCompleteComboBoxListener<String>(comboToken1);
			autoComboToken2 = new AutoCompleteComboBoxListener<String>(comboToken2);
		}
	}

	void delete(ListView<String> listView) {
		String selectedToken = listView.getSelectionModel().getSelectedItem();
		if (selectedToken != null) {
			listView.getItems().remove(selectedToken);

		}
	}

	void addBond(String tokenA, String tokenB) {
		// if (!mrpn.hasToken(tokenA) || !mrpn.hasToken(tokenB))
		// return;

		String selectedToken1 = tokenA;
		String selectedToken2 = tokenB;

		List<String> list_items = listViewBonds.getItems();
		if (selectedToken1 != null && selectedToken2 != null && !selectedToken1.equals(selectedToken2) 
				&& !listViewTokens.getItems().contains(selectedToken1) && !listViewTokens.getItems().contains(selectedToken2)) {
			for (String item : list_items) {
				if (item.indexOf(selectedToken1) != -1 && item.indexOf(selectedToken2) != -1)
					return;

			}
			listViewBonds.getItems().add(selectedToken1 + " - " + selectedToken2);
		}

	}

	void addBond() {
		String selectedToken1 = comboToken1.getValue();
		String selectedToken2 = comboToken2.getValue();
		addBond(selectedToken1, selectedToken2);
	}

	// epistrefi to telefteo id gia to token type
	private int typeCount(String type) {
		ArrayList<Integer> count = new ArrayList<>();
		ArrowUI arrow = this.arc;
		if (arrow.getDestination().myNode instanceof Transition) {
			Set<Arrow> arrowSet = mrpn.getNodeArrows(arrow.getDestination().myNode);
			Arrow[] arrowArray = new Arrow[arrowSet.size()];
			arrowSet.toArray(arrowArray);
			for (Arrow a : arrowArray)
				if (a.getDestination() instanceof Transition) {
					ArrayList<LabelItem> tokens = a.getTokens();
					for (LabelItem token : tokens)
						if (token.getTokenType().equals(type))
							count.add(token.getIdA());
				}
		}
		for (int i = 1; i <= count.size() + 1; i++)
			if (!count.contains(i))
				return i;
		return 0;
	}

	void initArcLists() {

		// Krata antigrafo gia sygrisi meta
		Set<LabelItem> prevItems = new HashSet<LabelItem>();
		prevItems.addAll(arc.getSet());

		arc.getSet().clear();

		List<String> list_items = listViewTokens.getItems();

		for (String tokenType : list_items) {
			if (this.arc.getDestination().myNode instanceof Transition) {
				for (int i = 0; i < mrpn.getTypes().length; i++) {
					String type = mrpn.getTypes()[i];

					if (type.equals(tokenType)) {
						int count = typeCount(type);
						arc.myArrow.addToken(type, type+count);
						// arc.tokens.add(token);
						break;
					}
				}
			} else {
				arc.myArrow.addToken(tokenType);
			}
		}

		list_items = listViewBonds.getItems();
		for (String bond : list_items) {
			String tokens[] = bond.split(" - ");
			String token1 = tokens[0];
			String token2 = tokens[1];
			Pair<String, String> pair = new Pair<>(token1, token2);
			arc.myArrow.addBond(pair);
			// arc.bonds.add(pair);
		}

		// Kane elegxo. An exei ala3ei ta label items
		MRPNUtils utils = new MRPNUtils(arc.mrpn);
		boolean areSame = utils.compareLabelItems(prevItems, arc.getSet());
		if (!areSame) {
			System.out.println("Label item on arc has changed");
			arc.mrpn.scrollArea.SomethingChanged();
		}

	}

	void addTokenLabel(String token, String id) {
		Label l = null;
		if (this.arc.getDestination().myNode instanceof Transition) 
			l = new Label(id + ":" + token);
		else
			l = new Label(id);
		arc.label.getChildren().add(l);

		Label comma = new Label(",");
		arc.label.getChildren().add(comma);
	}

	void addBondLabel(Pair<String, String> bond) {
		Label l = new Label(bond.getKey() + " - " + bond.getValue());
		arc.label.getChildren().add(l);

		Label comma = new Label(",");
		arc.label.getChildren().add(comma);

	}

	void createLabel() {
		arc.label.getChildren().clear();

		Set<LabelItem> labelItemSet = arc.getSet();
		for (LabelItem labelItem : labelItemSet) {
			if (labelItem.isToken())
				addTokenLabel(labelItem.getTokenType(), labelItem.getTokenName());
			else
				addBondLabel(labelItem.getBond());
		}

		int length = arc.label.getChildren().size();
		if (length > 0)
			arc.label.getChildren().remove(length - 1);
	}

	@FXML
	private void initialize() {
		addToken.setOnAction(clicked);
		removeToken.setOnAction(clicked);
		addBond.setOnAction(clicked);
		removeBond.setOnAction(clicked);
		okButton.setOnAction(clicked);
	}

	public EventHandler<ActionEvent> clicked = new EventHandler<ActionEvent>() {
		@Override
		public void handle(final ActionEvent event) {
			Button selectedButton = (Button) event.getSource();
			if (selectedButton.getId().equals("addToken"))
				addToken();
			else if (selectedButton.getId().equals("removeToken"))
				delete(listViewTokens);
			else if (selectedButton.getId().equals("removeBond"))
				delete(listViewBonds);
			else if (selectedButton.getId().equals("addBond"))
				addBond();
			else if (selectedButton.getId().equals("okButton")) {
				initArcLists();
				createLabel();
				stage.close();
			}

		}

	};

}
