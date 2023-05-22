package mrpnsim.application.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Pair;
import mrpnsim.application.editor.EditorArea;
import mrpnsim.application.model.MRPN;

public class SelectableNode extends AnchorPane {

	public static SelectableNode currentSelected;
	protected SelectableNode self;

	protected MRPN mrpn;

	protected Group group;
	
	public SelectableNode(MRPN mrpn) {
		super();

		this.mrpn = mrpn;

		// onMousePressed();
		this.setOnMouseClicked(mouseClicked);
		self = this;
	}


	public Node getNode() {
		return this;
	}
	
	public MRPN getMRPN() {
		return mrpn;
	}
	
	protected void removeHightlight() {
		getStyleClass().remove("selectedNode");
	}

	protected void setHightlight() {
		getStyleClass().add("selectedNode");
	}

	// We call refresh to check if the node should be removed
	public void refresh() {
		
	}

	// Deletes the MRPN component and removes the UI from view
	public void delete() {
		System.out.println("Delete Node (at SelectableNode)");

		// Remove from MRPN list
		// mrpn.delete(this);

		// Remove as UI component

	}

	protected void onLeftClick() {
		System.out.println("Left Mouse Click");
	}

	protected void onRightClick() {
		System.out.println("Right Mouse Click");
	}

	protected void onDoubleClick() {
		System.out.println("Double Mouse Click");

	}


	public List<Pair<String, Object>> getDataList() {
		ArrayList<Pair<String, Object>> data = new ArrayList<Pair<String, Object>>();
		return data;
	}

	// Kaleitai opote allazei to MRPN apo to simulation
	public void update() {
		
	}
	
	protected EventHandler<MouseEvent> mouseClicked = new EventHandler<MouseEvent>() {
		@Override
		public void handle(final MouseEvent event) {

			if(mrpn.scrollArea == null) {
				event.consume();
				return;
			}
			
			
			if (event.getClickCount() == 2)
				onDoubleClick();
			
			System.out.println(event.getButton());
			if (event.getButton() == MouseButton.PRIMARY) {

				onLeftClick();

				if ((mrpn.scrollArea instanceof EditorArea)) {
						
					EditorArea editor = (EditorArea) mrpn.scrollArea;
					String tool = editor.getSelectedTool();

					System.out.println(tool);
					if (tool.equals("SELECT") || tool.equals("DELETE")) {
						if (currentSelected != null)
							currentSelected.removeHightlight();
						self.setHightlight();
						currentSelected = self;
					}
					
					if(tool.equals("DELETE"))
						currentSelected.delete();
				}

			}

			event.consume();
		}
	};

}
