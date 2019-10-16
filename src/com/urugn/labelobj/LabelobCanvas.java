/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.urugn.labelobj;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableNumberValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author UruGN
 */
public class LabelobCanvas extends Canvas implements Runnable {

    GraphicsContext g2d;

    private double width = 0;
    private double height = 0;
    ObjFrame _img;

    String namespaceURI = "", qualifiedName = "", doctype = "";
    private String folder;
    private String filename;
    private int segmented;
    private String database;
    private String annotation;
    private String image;
    private String flickerId;
    private boolean hasSource;
    private String ownerFlickerId;
    private String ownerName;

    public int getSegmented() {
        return segmented;
    }

    public void setSegmented(int segmented) {
        this.segmented = segmented;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    private boolean owner;
//    private int width;
//    private int height;
    private int depth;

    Document doc = null;
    private ArrayList<ObjLabel> objLabels;
    private LabelobScene scene;

    public LabelobCanvas(LabelobScene scene) {
        super(800, 600);
        g2d = getGraphicsContext2D();
        this.scene = scene;
        this.objLabels = new ArrayList();
        try {
            DocumentBuilderFactory docBF = DocumentBuilderFactory.newInstance();
            doc = docBF.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(LabelobCanvas.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void setImage(ObjFrame _img) {
        this._img = _img;
        g2d.fill();

        if (_img != null) {
            width = _img.getWidth();
            height = _img.getHeight();
            setWidth(width);
            setHeight(height);

            g2d.drawImage(_img, 0, 0);

            objLabels.forEach((objLabel) -> {
                drawLabel(objLabel);

            });
        }
//        widthProperty().bind(getFitWidth());
//        heightProperty().bind(getFitHeight());

    }

    @Override
    public boolean isResizable() {
        return true;
    }

    void setPreserveRatio(boolean b) {
    }

    void setSmooth(boolean b) {
    }

    ObservableNumberValue getFitWidth() {
        final DoubleProperty w = new SimpleDoubleProperty(null, "width", width);
        return new DoubleBinding() {

            {
                super.bind(w);
            }

            @Override
            protected double computeValue() {
                return w.get() + scene.extraWidth();//220 to cover the leftpane width
            }

        };
    }

    ObservableNumberValue getFitHeight() {
        final DoubleProperty h = new SimpleDoubleProperty(null, "height", height);
        return new DoubleBinding() {

            {
                super.bind(h);
            }

            @Override
            protected double computeValue() {
                return h.get();
            }

        };
    }

//    ObservableNumberValue fitWidthProperty() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    ObservableNumberValue fitHeightProperty() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    /**
     * Add a label
     *
     * @param objLabel
     */
    void drawLabel(ObjLabel objLabel) {
        g2d.setFill(objLabel.getFill());
        g2d.setStroke(objLabel.getStroke());
        g2d.setLineWidth(objLabel.getStrokeWidth());

        g2d.strokeRoundRect(objLabel.getXmin(),
                objLabel.getYmin(),
                objLabel.getXmax() - objLabel.getXmin(),
                objLabel.getYmax() - objLabel.getYmin(), 5, 5);

        g2d.fillRect(objLabel.getXmin(),
                objLabel.getYmin(),
                objLabel.getXmax() - objLabel.getXmin(),
                objLabel.getYmax() - objLabel.getYmin());

        //add the info on the label
        g2d.setLineWidth(1d);
        Text txt = new Text();
        Font font = g2d.getFont();
        txt.setFont(font);
        txt.setText(objLabel.getName());
        double txtW = txt.getLayoutBounds().getWidth();
        double txtH = txt.getLayoutBounds().getHeight();
        double leftPadding = 3;
        double rightPadding = 3;
        
        //make sure the text is visible incase the labe is too near to image width
        double txtX = objLabel.getXmin() + leftPadding + txtW > _img.getWidth() ?
                objLabel.getXmin() - (objLabel.getXmin() + leftPadding + txtW - _img.getWidth())  :
                objLabel.getXmin();
        
        //clear the rect where the text will be placed
        g2d.clearRect(txtX,
                objLabel.getYmin() - 7,
                txtW + rightPadding,
                txtH);

        //fill the rect with the new rectangle
        g2d.fillRect(txtX,
                objLabel.getYmin() - 7,
                txtW + rightPadding,//leave atleast one pixel after the width
                txtH);
        //draw the rectangle line 
        g2d.strokeRect(txtX,
                objLabel.getYmin() - 7,
                txtW + rightPadding,//leave atleast three pixel after the width
                txtH);
        g2d.setStroke(objLabel.isHighlighted() ? Color.BLACK : objLabel.getStroke());//make sure the text is visible upon highlighting
        g2d.strokeText(objLabel.getName(), txtX, objLabel.getYmin() + 5, objLabel.getWidth());
    }

    /**
     * Draw a label
     *
     * @param objLabel
     */
//    void drawLabel(ObjLabel objLabel) {
//        //clear the previous drawing
//        g2d.clearRect(prev[0], prev[1], prev[2], prev[3]);
//
//        //add this label
//        addLabel(objLabel);
//
//        //store it for update
//        prev[0] = objLabel.getXmin();
//        prev[1] = objLabel.getYmin();
//        prev[2] = objLabel.getWidth();
//        prev[3] = objLabel.getHeight();
//
//    }
    void removeLabel(ObjLabel objLabel) {
        g2d.clearRect(objLabel.getXmin() - 1,
                objLabel.getYmin() - 1,
                (objLabel.getXmax() - objLabel.getXmin()) + 1,
                (objLabel.getYmax() - objLabel.getYmin()) + 1);

    }

    void repaint() {
        g2d.clearRect(0, 0, 0, 0);
        g2d.fill();
    }

    /**
     * Repaint the canvas with all the labels
     *
     * @param objLabel
     * @param xstart
     * @param ystart
     * @param xwidth
     * @param yheight
     */
    void repaint(ObjLabel objLabel, double xstart,
            double ystart,
            double xwidth,
            double yheight) {
//        GraphicsContext g2d = objCanvas.getGraphicsContext2D();
//                gc.lineTo(event.getX(), event.getY());
//                gc.stroke();
        g2d.setFill(new Color(Math.random(), Math.random(), Math.random(), 0.3));
        g2d.setStroke(objLabel.getStroke());
        g2d.setLineWidth(objLabel.getStrokeWidth());

        g2d.clearRect(xstart,
                ystart,
                xwidth,
                yheight);

        g2d.drawImage(_img, 0, 0);
//                ArrayList<ObjLabel> objLabels = getObjLabels();

//                status("Parsed: " + objLabels.size() + " objects for " + getFilename());
        objLabels.forEach((_objLabel) -> {
            drawLabel(_objLabel);

        });

        g2d.fillRect(xstart,
                ystart,
                xwidth,
                yheight);

        g2d.strokeRect(xstart,
                ystart,
                xwidth,
                yheight);

    }

    /**
     * Repaints this canvas
     */
    @Override
    public void run() {

        while (true) {
            repaint();

            setImage(_img);
//        ArrayList<ObjLabel> objLabels = getObjLabels();

//            objLabels.forEach((objLabel) -> {
//                drawLabel(objLabel);
//
//            });
            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                Logger.getLogger(LabelobCanvas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public String startXml() {

        String x = "";
        x += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        return x;
    }

    /**
     *
     * @param s
     * @return
     */
    public String declareAnnotation(String s) {
        return s += "<annotation>";
    }

    /**
     *
     * @param s
     * @return
     */
    public String closeAnnotation(String s) {
        return s += "</annotation>";
    }

    /**
     *
     * @param folder
     * @param filename
     * @param segmented
     * @return
     */
    public LabelobCanvas setImageInfo(String folder, String filename, int segmented) {
        this.folder = folder;
        this.filename = filename;
        this.segmented = segmented;
        return this;

    }

    /**
     *
     * @param database
     * @param annotation
     * @param image
     * @param flickerId
     * @param x
     * @return
     */
    public LabelobCanvas setSourceInfo(String database, String annotation, String image, String flickerId) {
        this.database = database;
        this.annotation = annotation;
        this.image = image;
        this.flickerId = flickerId;
        hasSource = database != null;
        return this;

    }

    /**
     *
     * @param ownerFlickerId
     * @param ownerName
     * @return
     */
    public LabelobCanvas setOwnerInfo(String ownerFlickerId, String ownerName) {
        this.ownerFlickerId = ownerFlickerId;
        this.ownerName = ownerName;
        this.owner = ownerName != null && !ownerName.isEmpty();
        return this;

    }

    /**
     *
     * @param width
     * @param height
     * @param depth
     * @param x
     * @return
     */
    public LabelobCanvas setSizeInfo(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        return this;

    }

    public ObjLabel newObjLabel() {
        ObjLabel objLabel = new ObjLabel(scene, doc);
        objLabels.add(objLabel);
        return objLabel;
    }

    public ObjLabel newObjLabel(int x, int y, int height, int width) {
        ObjLabel objLabel = newObjLabel();
        objLabel.setBoundingBox(x, y, width, height);
        return objLabel;
    }

    public LabelobCanvas removeObject(ObjLabel objLabel) {
        objLabels.remove(objLabel);
        return this;
    }

    public Document build() {

        //loop objects
        objLabels.forEach((objLabel) -> {
            objLabel.build();
        });

        return doc;
    }

    public void init() {
        String x = startXml();
        NodeList nL = doc.getElementsByTagName("annotation");
        if (nL != null && nL.getLength() > 0) {
            doc.removeChild(doc.getElementsByTagName("annotation").item(0));
        }
        objLabels = new ArrayList<>();

        Element annotationNode = doc.createElement("annotation");
        doc.appendChild(annotationNode);

        x = declareAnnotation(x);

        x += "<folder>" + folder + "</folder>";
        Element folderNode = doc.createElement("folder");
        folderNode.appendChild(doc.createTextNode(folder));
        annotationNode.appendChild(folderNode);

        x += "<filename>" + filename + "</filename>";
        Element filenameNode = doc.createElement("filename");
        filenameNode.appendChild(doc.createTextNode(filename));
        annotationNode.appendChild(filenameNode);

        x += "<segmented>" + segmented + "</segmented>";
        Element segmentedNode = doc.createElement("segmented");
        segmentedNode.appendChild(doc.createTextNode(Integer.toString(segmented)));
        annotationNode.appendChild(segmentedNode);

        if (hasSource) {
            x += "<source>";
            Element sourceNode = doc.createElement("source");
//        sourceNode.appendChild(doc.createTextNode(folder);
            annotationNode.appendChild(sourceNode);

            x += "<database>" + database + "</database>";
            Element databaseNode = doc.createElement("database");
            databaseNode.appendChild(doc.createTextNode(database));
            sourceNode.appendChild(databaseNode);

            x += "<annotation>" + annotation + "</annotation>";
            Element srcAnnotationNode = doc.createElement("annotation");
            srcAnnotationNode.appendChild(doc.createTextNode(annotation));
            sourceNode.appendChild(srcAnnotationNode);

            x += "<image>" + image + "</image>";
            Element imageNode = doc.createElement("image");
            imageNode.appendChild(doc.createTextNode(image));
            sourceNode.appendChild(imageNode);

            x += "<flickerid>" + image + "</flickerid>";
            Element flickeridNode = doc.createElement("flickerid");
            flickeridNode.appendChild(doc.createTextNode(image));
            sourceNode.appendChild(flickeridNode);
            x += "</source>";
        }

        if (owner) {
            x += "<owner>";
            Element ownerNode = doc.createElement("owner");
            annotationNode.appendChild(ownerNode);

            x += "<flickerid>" + ownerFlickerId + "</flickerid>";
            Element flickeridNode = doc.createElement("flickerid");
            flickeridNode.appendChild(doc.createTextNode(ownerFlickerId));
            ownerNode.appendChild(flickeridNode);

            x += "<name>" + ownerName + "</name>";

            Element ownerNameNode = doc.createElement("name");
            ownerNameNode.appendChild(doc.createTextNode(ownerName));
            ownerNode.appendChild(ownerNameNode);
            x += "</owner>";
        }

        x += "<size>";
        Element sizeNode = doc.createElement("size");
        annotationNode.appendChild(sizeNode);

        x += "<width>" + width + "</width>";
        Element widthNode = doc.createElement("width");
        widthNode.appendChild(doc.createTextNode(Integer.toString((int) Math.round(width))));
        sizeNode.appendChild(widthNode);

        x += "<height>" + height + "</height>";
        Element heightNode = doc.createElement("height");
        heightNode.appendChild(doc.createTextNode(Integer.toString((int) Math.round(height))));
        sizeNode.appendChild(heightNode);

        x += "<depth>" + depth + "</depth>";
        Element depthNode = doc.createElement("depth");
        depthNode.appendChild(doc.createTextNode(Integer.toString(depth)));
        sizeNode.appendChild(depthNode);
        x += "</size>";
    }

    public void setDocument(Document doc) {
        this.doc = doc;
    }

    Document getDocument() {
        return doc;
    }

    public ArrayList<ObjLabel> getObjLabels() {
        return objLabels;
    }
}
