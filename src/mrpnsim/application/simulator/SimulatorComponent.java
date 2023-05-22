package mrpnsim.application.simulator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class SimulatorComponent {

	public TabPane tabPane;
	public Tab simulatorTab;
	public ComboBox<String> reversibilityMode;
	public ListView<String> forwardList, reverseList;
	public Button forwardRunButton, reverseRunButton, refreshButton;
	
}
