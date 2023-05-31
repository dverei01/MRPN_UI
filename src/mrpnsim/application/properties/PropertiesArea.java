package mrpnsim.application.properties;

import java.io.File;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mrpnsim.application.MRPNXMLReader;
import mrpnsim.application.MRPNXMLWriter;
import mrpnsim.application.ScrollArea;
import mrpnsim.application.model.Transition;
import mrpnsim.application.simulator.Execution;
import mrpnsim.application.simulator.ForwardExecution;
import mrpnsim.application.simulator.MRPNVerifier;
import mrpnsim.application.simulator.ReverseExecution;


public class PropertiesArea extends ScrollArea{
	
	
	protected String tempFileName = "TmpPropSave.xml";
	
	protected TabPane tabPane;
	protected Tab propertiesTab;
	
	@FXML
    private AnchorPane properties_content, fx_propertiesScrollArea;

    @FXML
    private VBox fx_vboxProperties;

    @FXML
    private Button refreshButton, reachabilityButton, reachabilityAspButton;


	ReverseExecution reverse;
	ForwardExecution forward;
    
	public PropertiesArea(TabPane tabPane, Tab propertiesTab) {
		super("/properties_tab.fxml");
		
		this.tabPane = tabPane;
		this.propertiesTab = propertiesTab;
		
		this.fx_scrollArea = fx_propertiesScrollArea;
		this.fx_scrollArea.setOnMouseClicked(mouseClicked);


		refreshButton.setOnAction(event2 -> {

			// Reopen file
			this.clearAll();
			File file = new File(tempFileName);
			mrpn = MRPNXMLReader.read(this, file);

			this.updateArrowsBonds();
			
			mrpn.toFile("propertiesMRPN.txt");
			
			this.refreshAll();
			
			Reachability reachability = new Reachability(mrpn);
			reachabilityButton.setOnAction(reachability.clickAction);
			ReachabilityAsp reachabilityAsp = new ReachabilityAsp(mrpn);
			reachabilityAspButton.setOnAction(reachabilityAsp.clickAction);
		});
		
		
		propertiesTab.setOnSelectionChanged(event -> {

			if (propertiesTab.isSelected()) {

				System.out.println("Properties Tab is Selected");
				try {
					if (MRPNVerifier.verify()) {

						System.out.println("The mrpn is well-formed!");

						// Save current MRPN (with UI) in a temporary file
						File file = new File(tempFileName);
						MRPNXMLWriter.write(MRPNVerifier.getMRPN(), file);

						// Clear everything in the simulator area
						this.clearAll();
						mrpn = MRPNXMLReader.read(this, file);

						// Update line positions
						this.updateArrowsBonds();
						
						
						//reversibilityMode.getSelectionModel().clearSelection();

						Reachability reachability = new Reachability(mrpn);
						reachabilityButton.setOnAction(reachability.clickAction);
						ReachabilityAsp reachabilityAsp = new ReachabilityAsp(mrpn);
						reachabilityAspButton.setOnAction(reachabilityAsp.clickAction);
						System.out.println("aaaa");
						
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
	
	EventHandler<MouseEvent> mouseClicked = new EventHandler<MouseEvent>() {
		@Override
		public void handle(final MouseEvent event) {

			System.out.println("Updating!");
			updateArrowsBonds();
			event.consume();
		}
	};
	
	
	

}
