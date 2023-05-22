package mrpnsim.application;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AutoCompleteListViewListener {
	
	 ObservableList<String> entries = FXCollections.observableArrayList();    
	    ListView list;
	    TextField txt;
	 
	   public AutoCompleteListViewListener(TextField txt, ListView list) {
		   this.txt = txt;
		   this.list = list;
		   this.txt.setPromptText("Search");
	        txt.textProperty().addListener(
		            new ChangeListener() {
		                public void changed(ObservableValue observable, 
		                                    Object oldVal, Object newVal) {
		                    handleSearchByKey((String)oldVal, (String)newVal);
		                }
		            });
	        
	   }
	     
	   public void add( String item ) {
	        entries.add(item);
	        list.setItems( entries );
	   }
	   public void reset() {
		   entries.clear();
		   list.setItems( entries );
	   }
	 
	    public void handleSearchByKey(String oldVal, String newVal) {
	        // If the number of characters in the text box is less than last time
	        // it must be because the user pressed delete
	        if ( oldVal != null && (newVal.length() < oldVal.length()) ) {
	            // Restore the lists original set of entries 
	            // and start from the beginning
	            list.setItems( entries );
	        }
	         
	        // Break out all of the parts of the search text 
	        // by splitting on white space
	        String[] parts = newVal.toUpperCase().split(" ");
	 
	        // Filter out the entries that don't contain the entered text
	        ObservableList<String> subentries = FXCollections.observableArrayList();
	        for ( Object entry: list.getItems() ) {
	            boolean match = true;
	            String entryText = (String)entry;
	            for ( String part: parts ) {
	                // The entry needs to contain all portions of the
	                // search string *but* in any order
	                if ( ! entryText.toUpperCase().contains(part) ) {
	                    match = false;
	                    break;
	                }
	            }
	 
	            if ( match ) {
	                subentries.add(entryText);
	            }
	        }
	        list.setItems(subentries);
	    }

}
