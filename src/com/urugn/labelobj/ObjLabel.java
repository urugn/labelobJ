/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.urugn.labelobj;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author UruGN
 */
public class ObjLabel {

    public static final String POSE_NONE = "none";
    public static final String POSE_FRONT = "front";
    public static final String POSE_BACK = "back";
    public static final String POSE_LEFT = "left";
    public static final String POSE_RIGHT = "right";

    private int xmin;

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
    private int ymin;
    private int xmax;
    private int ymax;
    private String name;
    private String pose;
    private int truncated;
    private int difficult;

    /**
     * Avoids repeated builds
     */
    private boolean built = false;

    private Document doc = null;

    public ObjLabel(Document doc) {
//        try {
//            DocumentBuilderFactory docBF = DocumentBuilderFactory.newInstance();
//            this.doc = docBF.newDocumentBuilder().newDocument();
//        } catch (ParserConfigurationException ex) {
//            Logger.getLogger(ObjAnnotator.class.getName()).log(Level.SEVERE, null, ex);
//        }
        this.doc = doc;
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

}
