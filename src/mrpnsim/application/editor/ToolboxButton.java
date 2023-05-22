package mrpnsim.application.editor;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

public class ToolboxButton {

	private EditorToolbox toolbox;
	private Button fx_button;
	private ToolboxButton self;
	
	
	public ToolboxButton( EditorToolbox toolbox, Button fx_button ) {
	
		self = this;
		this.toolbox = toolbox;
		this.fx_button = fx_button;
		this.fx_button.setOnAction(myHandler);
	}
	
	protected void addStyleClass(String styleClass) {
		fx_button.getStyleClass().add(styleClass);
	}
	protected void removeStyleClass(String styleClass) {
		fx_button.getStyleClass().remove(styleClass);
	}
	
	private EventHandler<ActionEvent> myHandler = new EventHandler<ActionEvent>() {
	    @Override
	    public void handle(final ActionEvent event) {
			//previous selected button
			if(toolbox.selectedButton!=null)
				toolbox.selectedButton.removeStyleClass("selectedButton");
			
			//System.out.println("Self: "+self);
			toolbox.selectedButton = self;
			toolbox.selectedButton.addStyleClass("selectedButton");
	    	
	        event.consume();
	    }
	};

	@Override
	public String toString() {
		return fx_button.getText();
	}
	
}
