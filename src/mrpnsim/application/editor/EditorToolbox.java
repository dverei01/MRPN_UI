package mrpnsim.application.editor;

import java.util.ArrayList;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class EditorToolbox {

	private VBox fx_editorButtons;
	
	protected ToolboxButton selectedButton = null;
	private ArrayList<ToolboxButton>myButtons;
	
	
	public String getSelected() {
		if(selectedButton==null)
			return "";
		return selectedButton.toString();
	}
	
	public EditorToolbox(VBox fx_editorButtons) {
		this.fx_editorButtons = fx_editorButtons;
		
		myButtons = new ArrayList<>();
		
		for( Node node : fx_editorButtons.getChildren() ) {
			if( node instanceof Button ) {
				Button fx_button = (Button)node;
				ToolboxButton toolboxButton = new ToolboxButton(this,fx_button);
				myButtons.add(toolboxButton);
			}
		}
		
	}
	
}
