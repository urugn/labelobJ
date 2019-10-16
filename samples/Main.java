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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
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
public class Main extends Application implements LabelobScene {

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
     * Bottom pane
     */
    final HBox bottomPane = new HBox(2);

    final ImageView iView = new ImageView();
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

    ObjAnnotator objAnnotator;

    public static void main(String args[]) {
        launch(args);
    }
    private Stage primaryStage;
    private Paint oldStroke;
    private ObjLabel curObjLabel;

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
                    centerPane.widthProperty().add(iView.getFitWidth());
                    centerPane.heightProperty().add(iView.getFitHeight());
                    photos = imagesDir.listFiles(new SnapFilter(false));

                    newAnnotation();
                } else {
                    status("No images folder found in: " + dir);
                }
//        sPane.
            }
        });

        mPane.setTop(topPane);

        Label bLabel = new Label("::");
        bottomPane.getChildren().add(bLabel);

        bottomPane.getChildren().add(sLabel);

        mPane.setBottom(bottomPane);

        centerPane.setStyle("-fx-border-color: orange; -fx-alignment:CENTER;");

        centerPane.getChildren().add(iView);

        //TODO we could use the canvas in the future
        //centerPane.getChildren().add(objAnnotator);
        iView.setPreserveRatio(true);
        iView.setSmooth(true);
        iView.setFocusTraversable(true);

        iView.setOnMouseMoved((MouseEvent event) -> {
            //update cordinates
            sLabel.setText(" x:" + event.getX() + " y:" + event.getY());

//            ObjLabel rec = getObjLabel(event.getX(), event.getY());
//            if (rec != null) {
//                status("rec W:" + rec.getWidth());
//                oldStroke = rec.getStroke();
//                rec.setStroke(Color.KHAKI);
//                curObjLabel = rec;
//            }
//
//            if (rec == null) {
////                status("rec Null:");
//                if (curObjLabel != null && oldStroke != null) {
//                    curObjLabel.setStroke(oldStroke);
//                }
//            }
        });

        iView.setOnMouseExited((MouseEvent event) -> {
//            sLabel.setText("x:" + event.getX() + " y:" + event.getY());
        });

        iView.setOnMouseDragged((MouseEvent event) -> {

            xwidth = event.getX() - xstart;
            yheight = event.getY() - ystart;
            //visualize a rectangle being drawned
            //we get the last rectangle in the container and resize it
            if (drawRec && centerPane.getChildren().size() > 1) {
                //make sure to reserve the image at index 0
                ObjLabel rec = (ObjLabel) centerPane.getChildren().get(centerPane.getChildren().size() - 1);
                rec.setFill(new Color(Math.random(), Math.random(), Math.random(), 0.3));
                rec.setStroke(Color.RED);
                rec.setWidth(xwidth);
                rec.setHeight(yheight);
            }

            //update cordinates
            sLabel.setText(" x:" + event.getX() + " y:" + event.getY());
        });

        iView.setOnMousePressed((MouseEvent event) -> {
            xstart = event.getX();
            ystart = event.getY();
            System.out.println("xstart:" + xstart + ",ystart:" + ystart);

//            System.out.println("xend:" + xwidth + ",yend:" + yheight + " X:" + (xstart - (img.getWidth() / 2)));
            //we create a new rectangle object and add it to the center pane
            if (event.getButton() == event.getButton().PRIMARY) {

                curObjLabel = objAnnotator.newObjLabel((int) xstart, (int) ystart, (int) xwidth, (int) yheight);
                addObjLabel(curObjLabel);
                drawRec = true;
            }
        });

        iView.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                //use the secondary mouse to remove rectangles
                if (event.getButton() == event.getButton().SECONDARY) {
                    undo();
                    return;
                }

                //prompt annotation name
                Map opt = getObjectInfo();
                String name = opt.get("name") != null ? (String) opt.get("name") : "N/A";
                String pose = opt.get("pose") != null ? (String) opt.get("pose") : "N/A";
                int truncated = opt.get("truncated") != null ? (Integer) opt.get("truncated") : 0;
                int difficult = opt.get("difficult") != null ? (Integer) opt.get("difficult") : 0;

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
            iView.requestFocus();
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

        mScene.widthProperty().add(iView.fitWidthProperty());
        mScene.heightProperty().add(iView.fitHeightProperty());
        primaryStage.setTitle("LabelobJ");

        primaryStage.setScene(mScene);
        primaryStage.minWidthProperty().bind(mScene.heightProperty().multiply(2));
        primaryStage.minHeightProperty().bind(mScene.widthProperty().divide(2));

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
                undo();

            }
            return null;
        });

        Optional<Map<String, Object>> result = dialog.showAndWait();
        return result.get();
    }

    void undo() {

        int size = centerPane.getChildren().size();
        if (size > 1) {
            centerPane.getChildren().remove(centerPane.getChildren().size() - 1);
        }
        int recSize = objAnnotator.getObjLabels().size();
        if (recSize > 0) {
            objAnnotator.removeObject(objAnnotator.getObjLabels().get(recSize - 1));
        }

        //try and write immediately
        writeAnnotation();
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
        iView.addEventHandler(MouseEvent.MOUSE_MOVED, objLabel);

//        Rectangle objLabel = new Rectangle(x, y, width, height);
//        rec.setOnMouseMoved(event -> {
//            rec.setStroke(Color.HONEYDEW);
//            rec.setStrokeWidth(3d);
//
//        });
//
//        rec.setOnMouseExited(event -> {
//            rec.setStroke(Color.RED);
//            rec.setStrokeWidth(2d);
//
//        });
        objLabel.setFill(new Color(Math.random(), Math.random(), Math.random(), 0.3));//randomness for some effects
        objLabel.setStroke(Color.RED);
        objLabel.setStrokeWidth(2d);
        centerPane.getChildren().add(objLabel);
    }

//    ObjLabel getObjLabel(double x, double y) {
//        ObjLabel _label = null;
//        for (int i = 1; i < centerPane.getChildren().size(); i++) {
//            ObjLabel label = (ObjLabel) centerPane.getChildren().get(i);
//            double recX = label.getX();
//            double recY = label.getY();
//            double recW = label.getWidth();
//            double recH = label.getHeight();
//            boolean inX = false;
//            boolean inY = false;
//            if (x >= recX && recX + recW >= x) {
//                status(label.getName() + " xMax:" + (recX + recW));
//                inX = true;
//                if (y >= recY && recY + recH >= y) {
//                    status(label.getName() + " yMax:" + (recY + recH));
//                    inY = true;
//                    _label.setStroke(Color.KHAKI);
//                }
//            }
//
//            if (inX && inY) {
//
//                status(label.getName() + " xMax:" + (recX + recW) + " : " + label.getName() + " yMax:" + (recY + recH));
//                _label = label;
//                break;
//            }
//            if ((x >= rec.getX() && rec.getX() + rec.getWidth() >= x)
//                    && (y >= rec.getY() && rec.getY() + rec.getHeight() >= y)) {
//                status("rec width:" + rec.getWidth());
//                _rec = rec;
//                rec.setStroke(Color.KHAKI);
//            }
//        }
//        return _label;
//    }
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
                rh.parse(objAnnotator, doc);
            }

            //redraw the boxes
            ArrayList<ObjLabel> objLabels = objAnnotator.getObjLabels();

            status("Parsed: " + objLabels.size() + " objects for " + getFilename());
            objLabels.forEach((objLabel) -> {
//                addRectangle(objLabel.getXmin(),
//                        objLabel.getYmin(),
//                        objLabel.getXmax() - objLabel.getXmin(),
//                        objLabel.getYmax() - objLabel.getYmin());
                addObjLabel(objLabel);

            });

        } catch (Throwable t) {
            t.printStackTrace();
        }
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
//        ObjLabel objLabel = objAnnotator.newObjLabel();
        curObjLabel.setObjectInfo(name, pose, truncated, difficult)
                .setBoundingBox(xmin, ymin, xmax, ymax);

        //try and write immediately
        writeAnnotation();
    }

    void writeAnnotation() {

        status("Save annotation to file..");

        //prepare annotation file
        String annotFile = getAnnotationFile();

        Document doc = objAnnotator.build();
        try {
            Source input = new DOMSource(doc);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult(new File(annotFile));

            transformer.transform(input, output);
            status("Wrote Annotation to file..");
        } catch (TransformerException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates a new annotation for a new image
     */
    void newAnnotation() {

        //remove all rectangles
        for (int i = 1; i < centerPane.getChildren().size(); i++) {
            centerPane.getChildren().remove(i);
        }

        if (photos != null && photos.length > 0) {
            status("Image: " + photos[fileIndex]);
            String image = getImage(fileIndex);
            if (image != null && !image.isEmpty()) {

                _img = new ObjFrame(image);
                iView.setImage(_img);
                iView.setPreserveRatio(true);
                iView.setSmooth(true);

                primaryStage.minWidthProperty().bind(iView.fitWidthProperty());
                primaryStage.minHeightProperty().bind(iView.fitHeightProperty());
                primaryStage.setWidth(_img.getWidth() + 50);
                primaryStage.setHeight(_img.getHeight() + 100);
//                            primaryStage.setMinWidth(iView.getFitWidth());
//                            primaryStage.setMinHeight(iView.getFitHeight());

                String fileName = getFilename();

                objAnnotator = new ObjAnnotator(Main.this);
                objAnnotator.setSourceInfo("THE VOC2007 DB", "PASCAL VOC2007", "flickr", "12121212")
                        .setOwnerInfo("myflickerId", "Tony NGurU")
                        .setImageInfo("images", fileName, 0)
                        .setSizeInfo((int) Math.round(_img.getWidth()), (int) Math.round(_img.getWidth()), 3);

                objAnnotator.init();

                primaryStage.setTitle("LabelobJ - " + fileName);

                //parse existing xml
                parseXml();
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
    }

    @Override
    public void drawLabel() {
    }

    @Override
    public void highlightLabel(ObjLabel objLabel, boolean select) {
    }

    @Override
    public ObjLabel getSelectedLabel() {
        return curObjLabel;
    }
}
