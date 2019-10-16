/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.urugn.labelobj;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author UruGN
 */
public class ObjAnnotator extends Canvas {

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
    private boolean owner;
    private int width;
    private int height;
    private int depth;

    Document doc = null;
    private final ArrayList<ObjLabel> objLabels;
    private LabelobScene scene;

    public ObjAnnotator(LabelobScene scene) {
        this.scene = scene;
        this.objLabels = new ArrayList();
        try {
            DocumentBuilderFactory docBF = DocumentBuilderFactory.newInstance();
            doc = docBF.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ObjAnnotator.class.getName()).log(Level.SEVERE, null, ex);
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
    public ObjAnnotator setImageInfo(String folder, String filename, int segmented) {
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
    public ObjAnnotator setSourceInfo(String database, String annotation, String image, String flickerId) {
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
    public ObjAnnotator setOwnerInfo(String ownerFlickerId, String ownerName) {
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
    public ObjAnnotator setSizeInfo(int width, int height, int depth) {
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

    public ObjAnnotator removeObject(ObjLabel objLabel) {
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
        widthNode.appendChild(doc.createTextNode(Integer.toString(width)));
        sizeNode.appendChild(widthNode);

        x += "<height>" + height + "</height>";
        Element heightNode = doc.createElement("height");
        heightNode.appendChild(doc.createTextNode(Integer.toString(height)));
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
