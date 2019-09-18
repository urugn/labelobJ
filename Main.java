/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.urugn.labelobj;

import java.io.File;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
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
    int fileIndex = 0;

    public static void main(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        //initial image
        ObjectFrame img = new ObjectFrame("file:///developer/projects/ai/urugn-labelobj-1.0/frame.png");
//        iView.setImage(img);
        BorderPane mPane = new BorderPane();
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

        fBtn.setOnAction((ActionEvent event) -> {
            System.out.println("Load image location");
            dir = showFileDialog(primaryStage);
            System.out.println("Loaded location: " + dir);
            if (dir != null) {
                //set the director
                tField.setText(dir.getAbsolutePath());
                
                //load and init the first image
                iView.setImage(img);
        centerPane.widthProperty().add( iView.getFitWidth());
        centerPane.heightProperty().add(iView.getFitHeight() );
//        sPane.
            }
        });

        mPane.setTop(topPane);

        
        Label bLabel = new Label("::");
        bottomPane.getChildren().add(bLabel);

        Label sLabel = new Label(":");
        bottomPane.getChildren().add(sLabel);

        mPane.setBottom(bottomPane);


        centerPane.setStyle("-fx-border-color: orange; -fx-alignment:CENTER;");

        centerPane.getChildren().add(iView);

        iView.setOnMouseMoved((MouseEvent event) -> {
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

        });

        iView.setOnMousePressed((MouseEvent event) -> {
            xstart = event.getX();
            ystart = event.getY();
            System.out.println("xstart:" + xstart + ",ystart:" + ystart);

//            System.out.println("xend:" + xwidth + ",yend:" + yheight + " X:" + (xstart - (img.getWidth() / 2)));
            //we create a new rectangle object and add it to the center pane
            if (event.getButton() == event.getButton().PRIMARY) {
                Rectangle rec = new Rectangle(xstart, ystart, xwidth, yheight);
                rec.setFill(new Color(Math.random(), Math.random(), Math.random(), 0.3));
                rec.setStroke(Color.RED);
                centerPane.getChildren().add(rec);
                drawRec = true;
            }
        });

        iView.setOnMouseReleased((MouseEvent event) -> {

            //use the secondary mouse to undo
            if (event.getButton() == event.getButton().SECONDARY) {
                int size = centerPane.getChildren().size();
                if (size > 1) {
                    centerPane.getChildren().remove(centerPane.getChildren().size() - 1);
                }
                return;
            }

            //write annotation
            
            
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
        Scene mScene = new Scene(mPane);
        mScene.widthProperty().add(iView.fitWidthProperty());
        mScene.heightProperty().add(iView.fitHeightProperty());

        primaryStage.setScene(mScene);
        primaryStage.setWidth(iView.getFitWidth());
        primaryStage.setHeight(iView.getFitHeight());

        primaryStage.show();
    }

    private File showFileDialog(Stage _mainStage) {

        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("Open Directory");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File chosenDir = fileChooser.showDialog(_mainStage);
        return chosenDir;
    }
}

