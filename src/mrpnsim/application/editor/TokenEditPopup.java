package mrpnsim.application.editor;

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mrpnsim.application.ScrollArea;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.ui.EditableNode;
import mrpnsim.application.ui.PlaceUI;
import mrpnsim.application.ui.SelectableNode;
import mrpnsim.application.ui.TokenUI;
import mrpnsim.application.ui.TransitionUI;

public class TokenEditPopup extends AnchorPane {

	@FXML
	private AnchorPane root_pane;

	@FXML
	private Label titleLabel;

	@FXML
	private TextField typeText;

	@FXML
	private Button editButton;

	private TokenUI token;
	Stage stage;

	public TokenEditPopup(String titleLabel, TokenUI token) {
		this.token = token;
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/popupForToken.fxml"));

		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			// Parent root = FXMLLoader.load(getClass().getResource("/popupWindow.fxml"));
			Parent root = fxmlLoader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add("/style.css");
			stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Edit");
			stage.setScene(scene);
			this.titleLabel.setText(titleLabel);
			this.setOnKeyPressed(keyClicked);

			editButton.setOnAction(event -> typeEntered());

			stage.show();

		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

	}

	@FXML
	private void initialize() {

	}

	protected void typeEntered() {
		if (typeText.getText().length() > 0) {	
			MRPN mrpn = token.getMRPN();
			ScrollArea scrollArea = mrpn.getScrollArea();
			if (scrollArea instanceof EditorArea) {
				scrollArea.SomethingChanged();
			}
			token.changeType(typeText.getText());
		} 
		stage.close();
	}

	public EventHandler<KeyEvent> keyClicked = new EventHandler<KeyEvent>() {
		@Override
		public void handle(final KeyEvent keyEvent) {

			String keyCode = keyEvent.getCode().toString();
			if (keyCode.equals("ENTER")) {
				System.out.println(keyCode);
				typeEntered();
			}

			keyEvent.consume();
		}
	};

}
