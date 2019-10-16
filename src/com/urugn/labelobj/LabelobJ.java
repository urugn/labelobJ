/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.urugn.labelobj;

import com.urugn.media.snap.SnapFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 *
 * @author UruGN
 */
public final class LabelobJ extends Application implements LabelobScene {

    double xstart = 0d, ystart = 0d, xwidth = 0d, yheight = 0d;
    boolean drawRec;

    /**
     * Top pane
     */
    final Pane topPane = new HBox(2);

    /**
     * Center pane
     */
    final Pane centerPane = new Pane();

    /**
     * Left pane
     */
    final VBox leftPane = new VBox();

    /**
     * Tree view to display the image information
     */
    final TreeView treeView = new TreeView();

    /**
     * Tree Item to display the file name
     *
     */
    final TreeItem filenameItem = new TreeItem();

    /**
     * Bottom pane
     */
    final HBox bottomPane = new HBox(2);

    LabelobCanvas objCanvas;
    File dir = null;

    /**
     * tack current image being processed
     */
    int fileIndex = 0;

    String images = "images";
    String annotations = "annotations";

    File imagesDir = null;
    File[] photos = null;

    final Label sLabel = new Label(":");

    ObjFrame _img = null;

    /**
     * variable to store selected label y max
     */
    private int selectXmax = 0;

    /**
     * Variable to store selected label x max
     */
    private int selectYmax = 0;
    private int selectXmin = 0;
    private int selectYmin = 0;

    private ContextMenu contextMenu;
    private String filename;

    private final TextField ownerField = new TextField();
    private final TextField annotationField = new TextField();
    private final TextField databaseField = new TextField();

//    ObjAnnotator objCanvas;
    public LabelobJ() {
        objCanvas = new LabelobCanvas(LabelobJ.this);
        contextMenu = new ContextMenu();
        contextMenu.getItems().add(new MenuItem("Move"));

        contextMenu.getItems().add(new MenuItem("Resize"));
        MenuItem remove = new MenuItem("Remove");
        remove.addEventHandler(ActionEvent.ACTION, (ActionEvent event) -> {
            removeLabel();
        });
        contextMenu.getItems().add(remove);

        //tree staff
        filename = getFilename();
        filenameItem.setExpanded(true);

        //clicks on tree nodes
        treeView.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            Node node = event.getPickResult().getIntersectedNode();
//            status("Treeview clicked: " + node);
            if (node instanceof Text || node instanceof TreeCell) {
                Object obj = ((TreeItem) treeView.getSelectionModel().getSelectedItem()).getValue();
                if (obj instanceof ObjLabel) {
                    highlightLabel((ObjLabel) obj);
                    highlight(true);
                }
            }
        });

        //update image infomation
        ownerField.textProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println("textfield changed from " + oldValue + " to " + newValue);
            objCanvas.setOwnerName(newValue);
        });

        databaseField.textProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println("textfield changed from " + oldValue + " to " + newValue);
            objCanvas.setDatabase(newValue);
        });
        
        annotationField.textProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println("textfield changed from " + oldValue + " to " + newValue);
            objCanvas.setAnnotation(newValue);
        });
    }

    private Stage primaryStage;
    private Paint oldStroke;

    /**
     * We keep our currently highlighted label here upon mouse movements
     */
    private ObjLabel curObjLabel;
    /**
     * We will also need to keep track of the last label highlighted before we
     * switch to a new highlight. So we create another variable of ObjLabel
     */
    private ObjLabel preObjLabel;

    public static void main(String args[]) {
        launch(args);
    }

    String getImage(int index) {
        if (photos != null && photos.length > 0) {
            return "file://" + photos[index].getAbsolutePath();
        }

        return "";
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        //initial image
//        ObjFrame img = new ObjFrame("file:///developer/projects/ai/urugn-labelobj-1.0/frame.png");
//        iView.setImage(img);
        BorderPane mPane = new BorderPane();
        Scene mScene = new Scene(mPane);
//        primaryStage.addEventHandler((WindowEvent event) ->{
//            
//        System.out.println(event);
//        
//        }, (WindowEvent handler) ->{
//        System.out.println(handler);
//        });

        Button fBtn = new Button("Directory");
        topPane.getChildren().add(fBtn);

        TextField tField = new TextField();
        topPane.getChildren().add(tField);

        //load directori that contains images folder
        fBtn.setOnAction((ActionEvent event) -> {
            status("Load images location");
            dir = showFileDialog(primaryStage);
            status("Loading location: " + dir.getAbsolutePath() + File.separator + images);
            if (dir != null) {
                imagesDir = new File(dir.getAbsolutePath() + File.separator + images);
                status("Found images folder: " + imagesDir);
                if (imagesDir.exists()) {
                    //set the director
                    tField.setText(dir.getAbsolutePath());

                    //load and init the first image
                    centerPane.widthProperty().add(objCanvas.getFitWidth());
                    centerPane.heightProperty().add(objCanvas.getFitHeight());
                    photos = imagesDir.listFiles(new SnapFilter(false));

                    newAnnotation();
                } else {
                    status("No images folder found in: " + dir);
                }
//        sPane.
            }
        });

        mPane.setTop(topPane);

        //work on the tree
//        filenameItem.setValue(getFilename());
        treeView.setRoot(filenameItem);
        leftPane.getChildren().add(treeView);

        //add some controls
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ownerField.setPromptText("Owner");
        ownerField.setText("Urugn Code Systems");
        grid.add(new Label("Owner:"), 0, 0);
        grid.add(ownerField, 1, 0);

        annotationField.setPromptText("Annotation");
        annotationField.setText("PASCAL VOC2007");
        grid.add(new Label("Annotation:"), 0, 1);
        grid.add(annotationField, 1, 1);

        databaseField.setPromptText("Annotation");
        databaseField.setText("THE VOC2007 DB");
        grid.add(new Label("Database:"), 0, 2);
        grid.add(databaseField, 1, 2);
        
        //segmentation
        CheckBox segCheck = new CheckBox("Segmented");
        segCheck.setDisable(true);//TODO segmentation pending
        grid.add(segCheck, 0, 3, 2, 1);

        final Separator sepHor = new Separator();
        sepHor.setValignment(VPos.CENTER);
        grid.add(sepHor, 0, 4, 2, 1);

        ToggleGroup group = new ToggleGroup();
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {

                if (group.getSelectedToggle() != null) {
                    System.out.println("Toggle:" + group.getSelectedToggle().getUserData().toString());
                    // Do something here with the userData of newly selected radioButton

                }

            }
        });

        //currently ony bounding boxes are supported
        RadioButton rb1 = new RadioButton("Bound Box");
        rb1.setUserData("RadioButton1");
        rb1.setToggleGroup(group);
        rb1.setSelected(true);
        grid.add(rb1, 0, 5);

        //TODO
        RadioButton rb2 = new RadioButton("Polygon");
        rb2.setDisable(true);//will  re enable after we support drawing polygon
        rb2.setUserData("Polygon");
        rb2.setToggleGroup(group);
        grid.add(rb2, 1, 5);

        //add the grid to lower left
        leftPane.getChildren().add(grid);

        mPane.setLeft(leftPane);

        Label bLabel = new Label("::");
        bottomPane.getChildren().add(bLabel);

        bottomPane.getChildren().add(sLabel);

        mPane.setBottom(bottomPane);

        centerPane.setStyle("-fx-border-color: orange; -fx-alignment:CENTER;");

        centerPane.getChildren().add(objCanvas);

        //TODO we could use the canvas in the future
        //centerPane.getChildren().add(objCanvas);
        objCanvas.setPreserveRatio(true);
        objCanvas.setSmooth(true);
        objCanvas.setFocusTraversable(true);

        objCanvas.setOnMouseMoved((MouseEvent event) -> {
            //update cordinates
            sLabel.setText(" x:" + event.getX() + " y:" + event.getY());

            double mX = event.getX();
            double mY = event.getY();
            highlight(isHighlighted(mX, mY));
//            if (isHighlighted(mX, mY)) {
//                objCanvas.setCursor(Cursor.HAND);
//                if (curObjLabel != null) {
//                    curObjLabel.setStroke(Color.KHAKI);
//                    oldStroke = curObjLabel.initStroke();
//                }
//
//                //make sure do de hightlight the previous label if over bound
//                if (preObjLabel != null) {
//                    preObjLabel.setStroke(preObjLabel.initStroke());
//                }
//            } else {
//                objCanvas.setCursor(Cursor.DEFAULT);
//                if (curObjLabel != null) {
//                    curObjLabel.setStroke(oldStroke);
//                }
//
//                //make sure to de hightlight the previous label
//                if (preObjLabel != null) {
//                    preObjLabel.setStroke(preObjLabel.initStroke());
//                }
//            }

            repaintCanvas();
        });

        objCanvas.setOnMouseExited((MouseEvent event) -> {
//            sLabel.setText("x:" + event.getX() + " y:" + event.getY());
        });

        objCanvas.setOnMouseDragged((MouseEvent event) -> {

            xwidth = event.getX() - xstart;
            yheight = event.getY() - ystart;
            //visualize a rectangle being drawned
            //we get the last rectangle in the container and resize it
            if (drawRec) {
                //make sure to reserve the image at index 0
//                ObjLabel rec = (ObjLabel) centerPane.getChildren().get(centerPane.getChildren().size() - 1);

                curObjLabel.setFill(new Color(Math.random(), Math.random(), Math.random(), 0.3));
                curObjLabel.setStroke(Color.RED);
                curObjLabel.setXmax(xstart + xwidth);
                curObjLabel.setYmax(ystart + yheight);

//                objCanvas.drawLabel(curObjLabel);
                //update scene by removing redrawing the label and repainting the whole canvas
                objCanvas.repaint(curObjLabel, xstart,
                        ystart,
                        xwidth,
                        yheight);

                sLabel.setText("drawing x:" + event.getX() + " y:" + event.getY());
            } else {

                //update cordinates
                sLabel.setText(" x:" + event.getX() + " y:" + event.getY());
            }
        });

        objCanvas.setOnMousePressed((MouseEvent event) -> {
            xstart = event.getX();
            ystart = event.getY();
            System.out.println("xstart:" + xstart + ",ystart:" + ystart);

//            System.out.println("xend:" + xwidth + ",yend:" + yheight + " X:" + (xstart - (img.getWidth() / 2)));
            //we create a new rectangle object and add it to the center pane
            if (event.getButton() == event.getButton().PRIMARY) {

                curObjLabel = objCanvas.newObjLabel((int) xstart, (int) ystart, (int) xwidth, (int) yheight);

                //initial colors
                curObjLabel.setFill(new Color(Math.random(), Math.random(), Math.random(), 0.3));
                curObjLabel.setStroke(Color.RED);
                curObjLabel.setStrokeWidth(2d);

                addObjLabel(curObjLabel);
                drawRec = true;

//                redraw();
            }
        });

        objCanvas.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                //use the secondary mouse to remove rectangles
                if (event.getButton() == event.getButton().SECONDARY) {
                    if (isHighlighted(event.getX(), event.getY())) {

                        //context menu shows relative the main screen
                        //so we need to get the x and y co-ordinates of the
                        //primary stage and add them to our mouse co-ordinates
                        contextMenu.show(objCanvas, event.getSceneX() + primaryStage.getX(),
                                event.getSceneY() + primaryStage.getY());
                    }
//                    undo();
                    return;
                }

                //prompt annotation name
                Map opt = getObjectInfo();
                String name = opt.get("name") != null ? (String) opt.get("name") : "N/A";
                String pose = opt.get("pose") != null ? (String) opt.get("pose") : "N/A";
                int truncated = opt.get("truncated") != null ? (Integer) opt.get("truncated") : 0;
                int difficult = opt.get("difficult") != null ? (Integer) opt.get("difficult") : 0;

                //update
                repaintCanvas();

                labelObject(name, pose, truncated, difficult);

                //clear size to make room for the next rectangle
                xwidth = 0;
                yheight = 0;

                drawRec = false;
            }
        });
        mPane.setCenter(centerPane);
//        iView.fitWidthProperty().bind(sPane.widthProperty() );
//        iView.fitHeightProperty().bind(sPane.heightProperty() );

//        iView.setOnMouseDragOver((MouseEvent event) -> {
//
//            Rectangle rec = new Rectangle(xstart, ystart, event.getX(), event.getY());
//            sPane.getChildren().add(rec);
//        });
        mScene.setOnKeyReleased((KeyEvent event) -> {
            objCanvas.requestFocus();
            status("Key detected:" + event.getCharacter());
            if (photos != null) {
                if (event.getCode() == KeyCode.RIGHT) {

                    if (fileIndex < photos.length - 1) {
                        fileIndex++;
                    } else if (fileIndex == photos.length - 1) {
                        fileIndex = 0;
                    }
                }
                if (event.getCode() == KeyCode.LEFT) {
                    if (fileIndex - 1 >= 0) {
                        fileIndex--;
                    } else if (fileIndex == 0) {
                        fileIndex = photos.length - 1;
                    }
                }
                newAnnotation();
            }

        });

        mScene.widthProperty().add(objCanvas.getFitWidth());
        mScene.heightProperty().add(objCanvas.getFitHeight());
        primaryStage.setTitle("LabelobJ");

        primaryStage.setScene(mScene);
        primaryStage.minWidthProperty().bind(mScene.widthProperty());
        primaryStage.minHeightProperty().bind(mScene.heightProperty());

        primaryStage.show();
    }

    Map<String, Object> getObjectInfo() {
        status("getObjectInfo..");

        // Create the custom dialog.
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Object Info");
        dialog.setHeaderText("Object data parameters.");

// Set the icon (must be included in the project).
//        dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString()));
// Set the button types.
        ButtonType writeButtonType = new ButtonType("Write", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(writeButtonType, ButtonType.CANCEL);

// Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);

//        TextField poseField = new TextField();
//        poseField.setPromptText("Pose");
        ObservableList<String> options
                = FXCollections.observableArrayList(
                        ObjLabel.POSE_NONE,
                        ObjLabel.POSE_LEFT,
                        ObjLabel.POSE_RIGHT,
                        ObjLabel.POSE_FRONT,
                        ObjLabel.POSE_BACK
                );
        final ComboBox poseComboBox = new ComboBox(options);
        poseComboBox.setValue(ObjLabel.POSE_NONE);

        grid.add(new Label("Pose:"), 0, 1);
        grid.add(poseComboBox, 1, 1);

        TextField truncatedField = new TextField();
        truncatedField.setPromptText("Truncated");
        truncatedField.setText("0");
        grid.add(new Label("Truncated:"), 0, 2);
        grid.add(truncatedField, 1, 2);

        TextField difficultField = new TextField();
        difficultField.setPromptText("Difficult");
        difficultField.setText("0");
        grid.add(new Label("Difficult:"), 0, 3);
        grid.add(difficultField, 1, 3);
// Enable/Disable login button depending on whether a username was entered.
        Node writeButton = dialog.getDialogPane().lookupButton(writeButtonType);
        writeButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            writeButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

// Request focus on the name field by default.
        Platform.runLater(() -> nameField.requestFocus());

// Convert the result to key-value-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == writeButtonType) {
                Map<String, Object> op = new HashMap<>();
                op.put("name", nameField.getText());
                op.put("pose", poseComboBox.getValue());
                op.put("truncated", truncatedField.getText().isEmpty() ? 0 : Integer.parseInt(truncatedField.getText()));
                op.put("difficult", difficultField.getText().isEmpty() ? 0 : Integer.parseInt(difficultField.getText()));

                return op;
            }

            if (dialogButton == ButtonType.CANCEL) {
                removeLabel();//remove just currently added label

            }
            return null;
        });
        dialog.setX(primaryStage.getX() + 100);
        dialog.setY(primaryStage.getY() + 100);

        Optional<Map<String, Object>> result = dialog.showAndWait();
        return result.get();
    }

    /**
     * Called to highligh current label and dehighlight the previous highlighted
     * label
     *
     * @param highlighted
     */
    public void highlight(boolean highlighted) {

        if (highlighted) {
            objCanvas.setCursor(Cursor.HAND);
            if (curObjLabel != null) {
                curObjLabel.setStroke(Color.KHAKI);
                curObjLabel.setHighlighted(highlighted);
                oldStroke = curObjLabel.initStroke();
            }

            //make sure do de hightlight the previous label if over bound
            if (preObjLabel != null) {
                preObjLabel.setStroke(preObjLabel.initStroke());
                preObjLabel.setHighlighted(false);
                preObjLabel.getTreeItem().setExpanded(false);
            }
        } else {
            objCanvas.setCursor(Cursor.DEFAULT);
            if (curObjLabel != null) {
                curObjLabel.setStroke(oldStroke);
                curObjLabel.setHighlighted(highlighted);
            }

            //make sure to de hightlight the previous label
            if (preObjLabel != null) {
                preObjLabel.setStroke(preObjLabel.initStroke());
                preObjLabel.setHighlighted(highlighted);
            }
        }
    }

    boolean isHighlighted(double mX, double mY) {
        return mX >= selectXmin && selectXmax >= mX && mY >= selectYmin && selectYmax >= mY;
    }

    /**
     * Method to add the rectangle.
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    void addObjLabel(ObjLabel objLabel) {

        //make sure the Object gets notified of mouse movements
        objCanvas.addEventHandler(MouseEvent.MOUSE_MOVED, objLabel);
        objCanvas.drawLabel(objLabel);
    }

    /**
     * Parse xml annotation
     *
     * @param xmlFile
     */
    void parseXml() {

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            // Set up output stream
            // Parse the input
//            SAXParser saxParser = factory.newSAXParser();
            ObjLabelHandler rh = new ObjLabelHandler();

            File xmlFile = new File(getAnnotationFile());
            if (xmlFile.exists()) {
//                saxParser.parse(xmlFile, rh);
                Document doc = dBuilder.parse(xmlFile);
                doc.getDocumentElement().normalize();
                rh.parse(objCanvas, doc);
            }

            //add the listeners
//            ArrayList<ObjLabel> objLabels = objCanvas.getObjLabels();
//
//            status("Parsed: " + objLabels.size() + " objects for " + getFilename());
//            objLabels.forEach((objLabel) -> {
//                objCanvas.addEventHandler(MouseEvent.MOUSE_MOVED, objLabel);
//            });
            //redraw the boxes
            repaintCanvas();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * This redraws the scene after modifications
     */
    void repaintCanvas() {
        objCanvas.repaint();

        objCanvas.setImage(_img);
        ArrayList<ObjLabel> objLabels = objCanvas.getObjLabels();

        status("Parsed: " + objLabels.size() + " objects for " + getFilename());
        objLabels.forEach((objLabel) -> {
            addObjLabel(objLabel);

        });
    }

    private File showFileDialog(Stage _mainStage) {

        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("Open Directory");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File chosenDir = fileChooser.showDialog(_mainStage);
        return chosenDir;
    }

    /**
     * Label object upon drawing the bounding box
     */
    void labelObject(String name, String pose, int truncated, int difficult) {
        status("Prompting object details..");
        int xmin = (int) Math.round(xstart);
        int ymin = (int) Math.round(ystart);
        int xmax = (int) Math.round(xstart + xwidth);
        int ymax = (int) Math.round(ystart + yheight);

        //initiate a new object in the current image
//        ObjLabel objLabel = objCanvas.newObjLabel();
        curObjLabel.setObjectInfo(name, pose, truncated, difficult)
                .setBoundingBox(xmin, ymin, xmax, ymax);

        //try and write immediately
        writeAnnotation();
    }

    void writeAnnotation() {

        status("Save annotation to file..");

        //prepare annotation file
        String annotFile = getAnnotationFile();

        Document doc = objCanvas.build();
        try {
            Source input = new DOMSource(doc);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult(new File(annotFile));

            transformer.transform(input, output);

            status("Wrote Annotation to file..");

            //reload
            newAnnotation();
        } catch (TransformerException ex) {
            Logger.getLogger(LabelobJ.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates a new annotation for a new image
     */
    void newAnnotation() {

        //start by resetting variables
        selectXmin = 0;
        selectXmax = 0;
        selectYmin = 0;
        selectYmax = 0;

        preObjLabel = null;
        curObjLabel = null;

        filenameItem.getChildren().clear();

        //remove all handlers
        objCanvas.getObjLabels().forEach((oL) -> {
            objCanvas.removeEventHandler(MouseEvent.MOUSE_MOVED, oL);
        });

        if (photos != null && photos.length > 0) {
            status("Image: " + photos[fileIndex]);
            String image = getImage(fileIndex);
            if (image != null && !image.isEmpty()) {

                _img = new ObjFrame(image);
                objCanvas.setImage(_img);
                objCanvas.setPreserveRatio(true);
                objCanvas.setSmooth(true);

                primaryStage.minWidthProperty().bind(objCanvas.getFitWidth());
                primaryStage.minHeightProperty().bind(objCanvas.getFitHeight());
                primaryStage.setWidth(_img.getWidth() + 100);
                primaryStage.setHeight(_img.getHeight() + 100);
//                            primaryStage.setMinWidth(iView.getFitWidth());
//                            primaryStage.setMinHeight(iView.getFitHeight());

                filename = getFilename();
                filenameItem.setValue(filename);

//                objCanvas = new LabelobCanvas(LabelobJ.this);
                objCanvas.setSourceInfo(databaseField.getText().isEmpty() ? "THE VOC2007 DB" : databaseField.getText(),
                        annotationField.getText().isEmpty() ? "PASCAL VOC2007" : annotationField.getText(),
                        "flickr",
                        "12121212")
                        .setOwnerInfo("myflickerId", ownerField.getText().isEmpty() ? "UruGN Code Systems" : ownerField.getText())
                        .setImageInfo("images", filename, 0)
                        .setSizeInfo((int) Math.round(_img.getWidth()), (int) Math.round(_img.getWidth()), 3);

                objCanvas.init();

                primaryStage.setTitle("LabelobJ - " + filename);

                //parse existing xml
                parseXml();

                //populate the tree
                objCanvas.getObjLabels().forEach((oL) -> {
                    filenameItem.getChildren().add(oL.getInfoItem());
                });
            } else {
                status("No images found: " + imagesDir);
            }

            //TODO render the annotations if any
        }

    }

    public String getFilename() {
        return (photos != null && photos.length > 0) ? photos[fileIndex].getName() : "noimage";
    }

    /**
     * Prepare the annotation file
     *
     * @return
     */
    String getAnnotationFile() {
        File imgFile = photos[fileIndex];
        String parentDir = imgFile.getParent();
        String fileName = imgFile.getName();
        String annotationName = fileName.replace(fileName.substring(fileName.lastIndexOf('.') + 1), "xml");
        File annotationDir = new File(parentDir.replace("images", "annotations"));
        if (!annotationDir.exists()) {
            annotationDir.mkdirs();//create dir if none
        }
        return annotationDir.getAbsolutePath() + File.separator + annotationName;
    }

    void status(String s) {
        sLabel.setText(s);

        System.out.println(s);
    }

    @Override
    public void addLabel() {
    }

    @Override
    public void removeLabel() {
        if (curObjLabel == null) {
            status("No Object selected!");
            return;
        }

        objCanvas = objCanvas.removeObject(curObjLabel);
        objCanvas.removeLabel(curObjLabel);

        //unregister this label from handling mouse events
        objCanvas.removeEventHandler(MouseEvent.MOUSE_MOVED, curObjLabel);
        curObjLabel = null;

        int recSize = objCanvas.getObjLabels().size();

        //redraw
        repaintCanvas();

        //try and write immediately
        writeAnnotation();

    }

    @Override
    public void setCursor(Cursor cursor) {
        objCanvas.setCursor(cursor);
    }

    /**
     * Selects the Object label.Basically we just set it to be the curObjLabel
     *
     * @param objLabel The ObjectLabel to highlight or not highlight
     * @param hightlight Mark the ObjLabel as highlighted or not
     * @param select
     */
    @Override
    public void highlightLabel(ObjLabel objLabel) {
        selectXmin = objLabel.getXmin();
        selectYmin = objLabel.getYmin();
        selectXmax = objLabel.getXmax();
        selectYmax = objLabel.getYmax();

        //make sure we keep track of this current ObjLabel 
        //in the history timeline one object in the past
        if (curObjLabel != objLabel) {
            preObjLabel = curObjLabel;
        }
        curObjLabel = objLabel;
        treeView.getSelectionModel().select(objLabel.getTreeItem());

//            oldStroke = curObjLabel.getStroke();
        status("Current label: " + curObjLabel.getName());

        repaintCanvas();
    }

    @Override
    public ObjLabel getSelectedLabel() {
        return curObjLabel;
    }

    /**
     * Observable filename
     */
    ObservableStringValue getCurrentFilename() {
        final StringProperty fName = new SimpleStringProperty(null, "filename", filename);
        return new StringBinding() {

            {
                super.bind(fName);
            }

            @Override
            protected String computeValue() {
                return fName.get();
            }

        };
    }

    @Override
    public double extraWidth() {
        return leftPane.getWidth();
    }

    @Override
    public double extraHeight() {
        return leftPane.getHeight();
    }
}
