/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.urugn.labelobj;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author UruGN
 */
public class ObjLabel implements EventHandler<MouseEvent> {

    public static final String POSE_NONE = "none";
    public static final String POSE_FRONT = "front";
    public static final String POSE_BACK = "back";
    public static final String POSE_LEFT = "left";
    public static final String POSE_RIGHT = "right";

    private int xmin;
    LabelobScene scene;

    private double x;
    private double y;
    private double width;
    private double height;
    private Paint stroke;
    private double strokeWidth;
    private Paint fill;
    private Paint initStroke;

    private final TreeItem nameItem = new TreeItem();

    private int ymin;
    private int xmax;
    private int ymax;
    private String name;
    private String pose;
    private int truncated;
    private int difficult;
    private boolean highlighted;

    /**
     * Avoids repeated builds
     */
    private boolean built = false;

    private Document doc = null;

    ObjLabel(LabelobScene scene, Document doc) {
        this.scene = scene;
        this.doc = doc;

    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public void setXmax(double xmax) {
        setWidth(xmax - xmin);
    }

    public void setYmax(double ymax) {
        setHeight(ymax - ymin);
    }

    public int getXmin() {
        return xmin;
    }

    public int getYmin() {
        return ymin;
    }

    public int getXmax() {
        return xmax;
    }

    public int getYmax() {
        return ymax;
    }

    public String getName() {
        return name;
    }

    public String getPose() {
        return pose;
    }

    public int getTruncated() {
        return truncated;
    }

    public int getDifficult() {
        return difficult;
    }

    public Document getDoc() {
        return doc;
    }

    /**
     *
     *
     * @return
     */
    public String declareObject() {
        return "<object>";
    }

    /**
     *
     * @param s
     * @return
     */
    public String closeObject(String s) {
        return s += "</object>";
    }

    TreeItem getInfoItem() {

        ArrayList<TreeItem> partialInfo = new ArrayList<>();
        TreeItem poseItem = new TreeItem("Pose: " + pose);
        partialInfo.add(poseItem);
        TreeItem truncatedItem = new TreeItem("Trancated: " + truncated);
        partialInfo.add(truncatedItem);
        TreeItem difficultItem = new TreeItem("Difficult: " + difficult);
        partialInfo.add(difficultItem);

        ArrayList<TreeItem> bndboxInfo = new ArrayList<>();
        TreeItem xItem = new TreeItem("X: " + xmin);
        bndboxInfo.add(xItem);
        TreeItem yItem = new TreeItem("Y: " + ymin);
        bndboxInfo.add(yItem);
        TreeItem widthItem = new TreeItem("Width: " + width);
        bndboxInfo.add(widthItem);
        TreeItem heightItem = new TreeItem("Height: " + height);
        bndboxInfo.add(heightItem);

        TreeItem bndItem = new TreeItem("BndBox");
        bndItem.getChildren().addAll(bndboxInfo);

        partialInfo.add(bndItem);

        nameItem.setValue(ObjLabel.this);
        nameItem.getChildren().addAll(partialInfo);

        return nameItem;
    }

    TreeItem getTreeItem() {
        return nameItem;
    }

    /**
     *
     * @param name
     * @param pose
     * @param truncated
     * @param difficult
     * @return
     */
    public ObjLabel setObjectInfo(String name, String pose, int truncated, int difficult) {
        this.name = name;
        this.pose = pose;
        this.truncated = truncated;
        this.difficult = difficult;

        return this;

    }

    /**
     *
     * @param xmin
     * @param ymin
     * @param xmax
     * @param ymax
     * @return
     */
    public ObjLabel setBoundingBox(int xmin, int ymin, int xmax, int ymax) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
        setX(xmin);
        setY(ymin);
        setWidth(getXmax() - getXmin());
        setHeight(getYmax() - getYmin());
        return this;

    }

    public void build() {
        if (built) {
            return;
        }

        String x = declareObject();
        Element objectNode = doc.createElement("object");
        doc.getDocumentElement().appendChild(objectNode);

        x += "<name" + name + "</name>";
        Element nameNode = doc.createElement("name");
        nameNode.appendChild(doc.createTextNode(name));
        objectNode.appendChild(nameNode);

        x += "<pose>" + pose + "</pose>";
        Element poseNode = doc.createElement("pose");
        poseNode.appendChild(doc.createTextNode(pose));
        objectNode.appendChild(poseNode);

        x += "<truncated>" + truncated + "</truncated>";
        Element truncatedNode = doc.createElement("truncated");
        truncatedNode.appendChild(doc.createTextNode(Integer.toString(truncated)));
        objectNode.appendChild(truncatedNode);

        x += "<difficult>" + difficult + "</difficult>";
        Element difficultNode = doc.createElement("difficult");
        difficultNode.appendChild(doc.createTextNode(Integer.toString(difficult)));
        objectNode.appendChild(difficultNode);

        x += "<bndbox>";
        Element bndboxNode = doc.createElement("bndbox");
        objectNode.appendChild(bndboxNode);

        x += "<xmin" + xmin + "</xmin>";
        Element xminNode = doc.createElement("xmin");
        xminNode.appendChild(doc.createTextNode(Integer.toString(xmin)));
        bndboxNode.appendChild(xminNode);

        x += "<ymin>" + ymin + "</ymin>";
        Element yminNode = doc.createElement("ymin");
        yminNode.appendChild(doc.createTextNode(Integer.toString(ymin)));
        bndboxNode.appendChild(yminNode);

        x += "<xmax>" + xmax + "</xmax>";
        Element xmaxNode = doc.createElement("xmax");
        xmaxNode.appendChild(doc.createTextNode(Integer.toString(xmax)));
        bndboxNode.appendChild(xmaxNode);

        x += "<ymax>" + ymax + "</ymax>";
        Element ymaxNode = doc.createElement("ymax");
        ymaxNode.appendChild(doc.createTextNode(Integer.toString(ymax)));
        bndboxNode.appendChild(ymaxNode);

        x += "</bndbox>";
        x = closeObject(x);
        built = true;
//        return objectNode;
    }

    public boolean isBuilt() {
        return built;
    }

    @Override
    public void handle(MouseEvent mEvent) {

        int mX = (int) Math.round(mEvent.getX());
        int mY = (int) Math.round(mEvent.getY());

        int recX = (int) Math.round(getX());
        int recY = (int) Math.round(getY());
        int recW = (int) Math.round(getWidth());
        int recH = (int) Math.round(getHeight());

        if (mX >= recX && (recX + recW) >= mX && mY >= recY && (recY + recH) >= mY) {

            System.out.println("Mouse over " + getName());
            nameItem.setExpanded(true);
            scene.highlightLabel(this);
            setHighlighted(true);
//            setStroke(Color.KHAKI);

        } else {
            nameItem.setExpanded(false);
            setHighlighted(false);
        }
    }

    public ObjLabel setX(double x) {
        this.x = x;
        return ObjLabel.this;
    }

    public double getX() {
        return x;
    }

    public ObjLabel setY(double y) {
        this.y = y;
        return ObjLabel.this;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Paint getStroke() {
        return stroke;
    }

    public void setStroke(Paint stroke) {
        if (initStroke == null) {//set this only once
            initStroke = stroke;
        }
        this.stroke = stroke;
    }

    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }

    public Paint getFill() {
        return fill;
    }

    public void setFill(Paint fill) {
        this.fill = fill;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public Paint initStroke() {
        return initStroke;
    }

    @Override
    public String toString() {

        return getName();
    }

}
