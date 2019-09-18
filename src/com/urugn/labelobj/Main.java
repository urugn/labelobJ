/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.urugn.labelobj;

import com.urugn.media.snap.SnapFilter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
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
public class Main extends Application {

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
     * tack current image being prcessed
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

        iView.setPreserveRatio(true);
        iView.setSmooth(true);
        iView.setFocusTraversable(true);

        iView.setOnMouseMoved((MouseEvent event) -> {
            //update cordinates
            sLabel.setText(" x:" + event.getX() + " y:" + event.getY());
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
                //make sure to reseve the image at index 0
                Rectangle rec = (Rectangle) centerPane.getChildren().get(centerPane.getChildren().size() - 1);
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

                addRectangle(xstart, ystart, xwidth, yheight);
                drawRec = true;
            }
        });

        iView.setOnMouseReleased((MouseEvent event) -> {

            //use the secondary mouse to remove rectangles
            if (event.getButton() == event.getButton().SECONDARY) {
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
                return;
            }

            //prompt annotation name
            labelObject();

            //clear size to make room for the next rectangle
            xwidth = 0;
            yheight = 0;

            drawRec = false;

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
        primaryStage.setTitle("ObjLabel");

        primaryStage.setScene(mScene);
        primaryStage.minWidthProperty().bind(mScene.heightProperty().multiply(2));
        primaryStage.minHeightProperty().bind(mScene.widthProperty().divide(2));

        primaryStage.show();
    }

    /**
     * Method to add the rectangle.
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    void addRectangle(double x, double y, double width, double height) {
        Rectangle rec = new Rectangle(x, y, width, height);
        rec.setFill(new Color(Math.random(), Math.random(), Math.random(), 0.3));//randomness for some effects
        rec.setStroke(Color.RED);
        rec.setStrokeWidth(2d);
        centerPane.getChildren().add(rec);
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
                rh.parse(objAnnotator, doc);
            }

            //redraw the boxes
            ArrayList<ObjLabel> objLabels = objAnnotator.getObjLabels();
            objLabels.forEach((objLabel) -> {
                addRectangle(objLabel.getXmin(),
                        objLabel.getYmin(),
                        objLabel.getXmax() - objLabel.getXmin(),
                        objLabel.getYmax() - objLabel.getYmin());

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
    void labelObject() {
        status("Prompting object details..");
        int xmin = (int) Math.round(xstart);
        int ymin = (int) Math.round(ystart);
        int xmax = (int) Math.round(xstart + xwidth);
        int ymax = (int) Math.round(ystart + yheight);

        //initiate a new object in the current image
        ObjLabel objLabel = objAnnotator.newObjLabel();
        objLabel.setObjectInfo("fingerprint", ObjLabel.POSE_NONE, 0, 0)
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

                String fileName = (photos != null && photos.length > 0) ? photos[fileIndex].getName() : "noimage";

                objAnnotator = new ObjAnnotator();
                objAnnotator.setSourceInfo("THE VOC2007 DB", "PASCAL VOC2007", "flickr", "12121212")
                        .setOwnerInfo("myflickerId", "Tony NGurU")
                        .setImageInfo("images", fileName, 0)
                        .setSizeInfo((int) Math.round(_img.getWidth()), (int) Math.round(_img.getWidth()), 3);

                objAnnotator.init();

                //parse existing xml
                parseXml();
            } else {
                status("No images found: " + imagesDir);
            }

            //TODO render the annotations if any
        }

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
}
