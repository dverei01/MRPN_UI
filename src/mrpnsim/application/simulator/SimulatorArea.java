package mrpnsim.application.simulator;

import java.io.File;
import java.util.ArrayList;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import mrpnsim.application.AutoCompleteListViewListener;
import mrpnsim.application.FileManager;
import mrpnsim.application.MRPNXMLReader;
import mrpnsim.application.MRPNXMLWriter;
import mrpnsim.application.ScrollArea;
import mrpnsim.application.model.Arrow;
import mrpnsim.application.model.Token;
import mrpnsim.application.model.Transition;
import mrpnsim.application.properties.Reachability;
import mrpnsim.application.ui.SelectableNode;
import mrpnsim.application.ui.TransitionUI;

public class SimulatorArea extends ScrollArea {

	protected String tempFileName = "TmpSimSave.xml";



	// metritis gia to history
	int count = 1;
	ReverseExecution reverse;
	ForwardExecution forward;

	@FXML
    private AnchorPane simulator_content, fx_simulatorScrollArea;

    @FXML
    private VBox fx_simulatorProperties;

    @FXML
    private Button refreshButton, forwardRunButton, reverseRunButton;

    @FXML
    private ListView<String> forwardList,  reverseList;
    @FXML
	private TextField forwardTextField,reverseTextField;
 
	
    public Tab simulatorTab;
	private TabPane tabPane;
	AutoCompleteListViewListener autoForward, autoReverse;
	
	EventHandler<MouseEvent> mouseClicked = new EventHandler<MouseEvent>() {
		@Override
		public void handle(final MouseEvent event) {

			System.out.println("Updating!");
			updateArrowsBonds();
			event.consume();
		}
	};

	public void updateForwardList() {
		
		autoForward.reset();
		
		//forwardList.getItems().clear();
		System.out.println("Enabled transitions:");
		
		
		long start_time = System.nanoTime();
		
		ArrayList<Pair<Transition, ArrayList<ArrayList<Token>>>> enabledTransitions =  forward.getEnabledTransitions();
		
		long end_time = System.nanoTime();
		long time = end_time - start_time;
		System.out.println("Get enabled (forward) transitions: " + time/1000000.0f + " ms");
		
		
		for (Pair<Transition, ArrayList<ArrayList<Token>>> enabled : enabledTransitions) {
			Transition t = enabled.getKey();
			ArrayList<ArrayList<Token>> matching = enabled.getValue();
			for(int i = 0; i < matching.size(); i++) {
				System.out.println(t.getName() + "(" + (i+1) +") ,");
				autoForward.add(t.getName() + "(" + (i+1) +")");
			}
			
			//forwardList.getItems().add(t.getName());
		}
		
		//autoForward.
	}

	public void updateReverseList() {
			//reverseList.getItems().clear();
			autoReverse.reset();
			
			
			long start_time = System.nanoTime();
			
			ArrayList<Pair<Transition, ArrayList<ArrayList<Token>>>> enabledTransitions =  reverse.getEnabledTransitions();
			
			long end_time = System.nanoTime();
			long time = end_time - start_time;
			System.out.println("Get enabled (reverse) transitions: " + time /1000000.0f + " ms");
			
			for (Pair<Transition, ArrayList<ArrayList<Token>>> enabled : enabledTransitions) {
				Transition t = enabled.getKey();
				ArrayList<ArrayList<Token>> matching = enabled.getValue();
				for(int i = 0; i < matching.size(); i++) {
					System.out.println(t.getName() + "(" + (i+1) +") ,");
					autoReverse.add(t.getName() + "(" + (i+1) +")");
				}
			}
	}

	public SimulatorArea(TabPane tabPane, Tab simulatorTab) {
		super("/simulator_tab.fxml");
		
		autoForward = new AutoCompleteListViewListener(forwardTextField, forwardList);
		autoReverse = new AutoCompleteListViewListener(reverseTextField, reverseList);
		
		this.fx_scrollArea = fx_simulatorScrollArea;
		this.tabPane = tabPane;
		this.simulatorTab = simulatorTab;
		this.fx_scrollArea.setOnMouseClicked(mouseClicked);
		

		refreshButton.setOnAction(event2 -> {

			long refreshTime_start = System.nanoTime();
			
			// Reopen file
			this.clearAll();
			count = 1;
			
			
			long openTime_start = System.nanoTime();
			// Now open it with the file manager
			File file = new File(tempFileName);
			mrpn = MRPNXMLReader.read(this, file);
			long openTime_end = System.nanoTime();
			long openTime = openTime_end - openTime_start;
			System.out.println("MRPN Open time : "+openTime /1000000.0f + " ms");
			
			
			this.updateArrowsBonds();
			
			// forward = new ForwardExecution(simulatorMRPN, forwardList);
			forward.setMRPN(mrpn);
			reverse.setMRPN(mrpn);
			updateForwardList();
			updateReverseList();			
			
			count = 1;
			
			
			mrpn.toFile("simulatorMRPN.txt");

			this.refreshAll();
			

			long refreshTime_end = System.nanoTime();
			long refreshTime = refreshTime_end - refreshTime_start;
			System.out.println("UI refresh time: " + refreshTime /1000000.0f + " ms");
		});

		
		forwardRunButton.setOnAction(event2 -> {
			long forwardRunTime_start = System.nanoTime();
			
			String selectedTransitionString = forwardList.getSelectionModel().getSelectedItem();
			String selectedTransitionName = selectedTransitionString.substring(0, selectedTransitionString.lastIndexOf('('));
			int selectedTransitionNum = Integer.parseInt(selectedTransitionString.substring(selectedTransitionString.lastIndexOf('(')+1,selectedTransitionString.length()-1));
			Transition selectedTransition = null;
			
			for (Transition t : mrpn.getTransitions()) {
				if (t.getName().equals(selectedTransitionName)) {
					selectedTransition = t;
					break;
				}
			}
			if (selectedTransition != null) {
				
				
				long start_time = System.nanoTime();
				
				forward.fireTransition(selectedTransition, selectedTransitionNum);
				
				long end_time = System.nanoTime();
				long time = end_time - start_time;
				System.out.println("Fired transition (forward): " + time /1000000.0f + " ms");
				
				
				updateArrowsBonds();
				count++;
				
				//long test_start = System.nanoTime();
				this.refreshAll();
				//long test_end = System.nanoTime();
				//long test_time = test_end - test_start;
				//System.out.println("refresh All delay: " + test_time /1000000.0f + " ms");
				
				mrpn.toFile("simulatorMRPN.txt");

				
				updateForwardList();
				updateReverseList();
			}
			
			System.out.println("History Count: "+count);

			long forwardRunTime_end = System.nanoTime();
			long forwardRunTime = forwardRunTime_end - forwardRunTime_start;
			System.out.println("UI forward run button time: " + forwardRunTime /1000000.0f + " ms");
			
		});
		
		
		reverseRunButton.setOnAction(event3 -> {

			long reverseRunTime_start = System.nanoTime();
			String selectedTransitionString = reverseList.getSelectionModel().getSelectedItem();
			String selectedTransitionName = selectedTransitionString.substring(0, selectedTransitionString.lastIndexOf('('));
			int selectedTransitionNum = Integer.parseInt(selectedTransitionString.substring(selectedTransitionString.lastIndexOf('(')+1,selectedTransitionString.length()-1));
			Transition selectedTransition = null;
			
			for (Transition t : mrpn.getTransitions()) {
				if (t.getName().equals(selectedTransitionName)) {
					selectedTransition = t;
					break;
				}
			}
			if (selectedTransition != null) {
				
				
				long start_time = System.nanoTime();
				
				reverse.fireTransition(selectedTransition, selectedTransitionNum);
				
				long end_time = System.nanoTime();
				long time = end_time - start_time;
				System.out.println("Fired transition (reverse): " + time /1000000.0f + " ms");
				
				
				count--;
				
				mrpn.toFile("simulatorMRPN.txt");
				
				updateForwardList();
				updateReverseList();
				this.refreshAll();
			}
			
			System.out.println("History Count: "+count);
			
			long reverseRunTime_end = System.nanoTime();
			long reverseRunTime = reverseRunTime_end - reverseRunTime_start;
			System.out.println("UI reverse run button time: "+reverseRunTime /1000000.0f + " ms");
			
		});
		
		
		simulatorTab.setOnSelectionChanged(event -> {

			
			if (simulatorTab.isSelected()) {

				System.out.println("Simulator Tab is Selected");
				
				try {
					
					long simulatorTabTime_start = System.nanoTime();
					
					long verifyTime_start = System.nanoTime();
					if (MRPNVerifier.verify()) {
						long verifyTime_end = System.nanoTime();
						long verifyTime  = verifyTime_end - verifyTime_start;
						System.out.println("MRPN Verify time : "+verifyTime /1000000.0f + " ms");
						
						//System.out.println("The mrpn is well-formed!");

						// Save current MRPN (with UI) in a temporary file
						long saveTime_start = System.nanoTime();
						
						File file = new File(tempFileName);
						MRPNXMLWriter.write(MRPNVerifier.mrpn, file);

						long saveTime_end = System.nanoTime();
						long saveTime = saveTime_end - saveTime_start;
						System.out.println("MRPN Save time : "+saveTime /1000000.0f + " ms");
						
						// Clear everything in the simulator area
						this.clearAll();
						count = 1;
						long openTime_start = System.nanoTime();
						// Now open it with the file manager
						mrpn = MRPNXMLReader.read(this, file);
						long openTime_end = System.nanoTime();
						long openTime = openTime_end - openTime_start;
						System.out.println("MRPN Open time : "+openTime /1000000.0f + " ms");
						
						
						// Update line positions
						this.updateArrowsBonds();

						forward = new ForwardExecution(mrpn);
						reverse = new ReverseExecution(mrpn);

						updateForwardList();
						updateReverseList();
						
						count = 1;
						
					
						long simulatorTabTime_end = System.nanoTime();
						long simulatorTabTime = simulatorTabTime_end - simulatorTabTime_start;
						System.out.println("UI simulator tab time: "+simulatorTabTime /1000000.0f + " ms");
					
					}
					
					
					
				}catch (IllegalArgumentException ex) {
					

					System.out.println("The mrpn isn't well-formed..");
					// switch to editor tab
					tabPane.getSelectionModel().select(0);
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText("Parsing Error");
					alert.setContentText(
								"The MRPN is not well-formed. "+ ex.getMessage());
					alert.showAndWait();

					
				}
				
				

			}
		});
	}

}
