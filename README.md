# MRPN_UI


    MRPN Graphical Editor: Add/Delete/Move/Edit places, transitions, tokens and arcs.
    Save/Load MRPNs: In XML format.
    Simulator Mode: Execute (enabled) transitions with Forward and Reverse execution modes.
    Reachability: Test if a marking is reachable from the initial marking.



    Install the Java Development Kit version 13 (jdk-13+)
    Download and Install Eclipse (https://www.eclipse.org/)
    Download and extract the Github project
    In Eclipse: Select "File>Import" and pick "General>Existing Workspace Into Project". Browse and select the extracted Github project.
    Download JavaFX Windows SDK from https://gluonhq.com/products/javafx/
    Extract JavaFX SDK and place the folder somewhere you can find it easily.
    In Eclipse: Right-Click the project name from the Package Explorer and select "Bulid Path>Add External Archives". Select all *.jar files from the lib folder of the extracted javafx sdk and press "Open".
    Open Run Configurations for MRPNApp, and set "Arguments>VM Arguments" to: --module-path (path to javafx sdk lib folder) --add-modules=javafx.controls,javafx.fxml
    Apply and Run
