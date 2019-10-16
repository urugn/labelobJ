/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.urugn.labelobj;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author UruGN
 */
public class ObjLabelHandler extends DefaultHandler {

    JsonObjectBuilder annotationBuilder;
    JsonArrayBuilder objectsArray;

    private Writer out;
    private boolean verbose = true;

    public ObjLabelHandler() {
        this.annotationBuilder = Json.createObjectBuilder();
        this.objectsArray = Json.createArrayBuilder();

        try {
            out = new OutputStreamWriter(System.out, "UTF8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ObjLabelHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    JsonObjectBuilder jsonObjectToBuilder(JsonObject jo) {
        JsonObjectBuilder job = Json.createObjectBuilder();

        jo.entrySet().forEach((entry) -> {
            job.add(entry.getKey(), entry.getValue());
        });

        return job;
    }

    JsonArrayBuilder jsonArrayToBuilder(JsonArray jo) {
        JsonArrayBuilder job = Json.createArrayBuilder();

        jo.forEach((entry) -> {
            job.add(entry);
        });

        return job;
    }

    LabelobCanvas parse(LabelobCanvas objAnnot, Document doc) throws SAXException {

        Element annotationEl = (Element) doc.getElementsByTagName("annotation").item(0);

        Element folderEl = (Element) annotationEl.getElementsByTagName("folder").item(0);
        annotationBuilder.add(folderEl.getNodeName(), folderEl.getTextContent());

        Element filenameEl = (Element) annotationEl.getElementsByTagName("filename").item(0);
        annotationBuilder.add(filenameEl.getNodeName(), filenameEl.getTextContent());

        Element segmentedEl = (Element) annotationEl.getElementsByTagName("segmented").item(0);
        annotationBuilder.add(segmentedEl.getNodeName(), Integer.parseInt(segmentedEl.getTextContent()));

        //source
        addJsonObject(annotationEl, "source");

        //owner
        addJsonObject(annotationEl, "owner");

        //size
        addJsonObject(annotationEl, "size");

        //objects
        JsonObject annotationObj = annotationBuilder.build();
        NodeList objectList = annotationEl.getElementsByTagName("object");
//        addJsonArray(parentObj, objectEl, "bndbox");
        JsonArray objectArray = annotationObj.getJsonArray("objects");
        if (objectArray == null) {
            objectArray = Json.createArrayBuilder().build();
        }
        JsonObjectBuilder childrenBuilder = Json.createObjectBuilder();
        for (int o = 0; o < objectList.getLength(); o++) {
            Node objectNode = objectList.item(o);
            NodeList childNodes = objectNode.getChildNodes();
            for (int temp = 0; temp < childNodes.getLength(); temp++) {
                Node nNode = childNodes.item(temp);
                System.out.println("\nCurrent Element :" + nNode.getNodeName());

//                Element el = (Element) nNode;
                if (nNode.getNodeName().equals("bndbox")) {

                    childrenBuilder = addBndbox(childrenBuilder.build(), (Element) objectNode, "bndbox");
                } else {
                    if (nNode.getTextContent() != null) {
//                    annotationBuilder.add(nNode.getNodeName(), el.getTextContent());

                        childrenBuilder.add(nNode.getNodeName(), nNode.getTextContent());
                    }
                }
            }
            JsonArrayBuilder arrayB = jsonArrayToBuilder(objectArray);
            arrayB.add(childrenBuilder);
            objectArray = arrayB.build();
        }
        annotationBuilder = jsonObjectToBuilder(annotationObj);
        annotationBuilder.add("objects", objectArray);

        JsonObject jsonObject = annotationBuilder.build();
        nl();
        emit("START JSON");
        nl();
        StringWriter stringWriter = new StringWriter();

        //Write the Json object.
        try ( JsonWriter writer = Json.createWriter(stringWriter)) {
            //Write the Json object.
            writer.writeObject(jsonObject);

            //Done! Close the writer to free up all the resources.
        }

//        nl();
//        //System.out.println(encode(stringWriter.getBuffer().toString()));
        emit(stringWriter.getBuffer().toString());
//
        nl();
        emit("END JSON");
//        try {
//            out.flush();
//        } catch (IOException e) {
//            throw new SAXException("I/O error", e);
//        }
        String folder = jsonObject.getString("folder");
        String filename = jsonObject.getString("filename");
        int segmented = jsonObject.getInt("segmented");
        objAnnot.setImageInfo(folder, filename, segmented);

        JsonObject srcObj = jsonObject.getJsonObject("source");
        String database = srcObj.getString("database");
        String annotation = srcObj.getString("annotation");
        String image = srcObj.getString("image");
        String flickerId = srcObj.getString("flickerid");
        objAnnot.setSourceInfo(database, annotation, image, flickerId);

        JsonObject ownerObj = jsonObject.getJsonObject("owner");
        String flickerid = ownerObj.getString("flickerid");
        String ownerName = ownerObj.getString("name");
        objAnnot.setOwnerInfo(flickerid, ownerName);

        JsonObject sizeObj = jsonObject.getJsonObject("size");
        int width = Integer.parseInt(sizeObj.getString("width"));
        int height = Integer.parseInt(sizeObj.getString("height"));
        int depth = Integer.parseInt(sizeObj.getString("depth"));
        objAnnot.setSizeInfo(width, height, depth);

        JsonArray objArray = jsonObject.getJsonArray("objects");
        for (int i = 0; i < objArray.size(); i++) {
            JsonObject objectObj = objArray.getJsonObject(i);

            String objectName = objectObj.getString("name");
            String pose = objectObj.getString("pose");
            int truncated = Integer.parseInt(objectObj.getString("truncated"));
            int difficult = Integer.parseInt(objectObj.getString("difficult"));

            ObjLabel objLabel = objAnnot.newObjLabel();

            //initial colors
            objLabel.setFill(new Color(Math.random(), Math.random(), Math.random(), 0.3));
            objLabel.setStroke(Color.RED);
            objLabel.setStrokeWidth(2d);

            objLabel.setObjectInfo(objectName, pose, truncated, difficult);
            JsonObject bndboxObj = objectObj.getJsonObject("bndbox");
            int xmin = bndboxObj.getInt("xmin");
            int ymin = bndboxObj.getInt("ymin");
            int xmax = bndboxObj.getInt("xmax");
            int ymax = bndboxObj.getInt("ymax");
            objLabel.setBoundingBox(xmin, ymin, xmax, ymax);
        }

        return objAnnot;
    }

    private void emit(String s)
            throws SAXException {
        if (!verbose) {
            return;
        }

        try {
            out.write(s);
            out.flush();
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    // Start a new line
    // and indent the next line appropriately
    private void nl()
            throws SAXException {
        String lineEnd = System.getProperty("line.separator");
        try {
            out.write(lineEnd);
//            for (int i = 0; i < indentLevel; i++) {
//                out.write(indentString);
//            }
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    void addJsonObject(Element parentEl, String childTag) {
        JsonObject annotationObj = annotationBuilder.build();
        NodeList nodeLists = parentEl.getElementsByTagName(childTag);
        Node childEl = nodeLists.item(0);
        NodeList childNodes = childEl.getChildNodes();

        JsonObject sizeObj = annotationObj.getJsonObject(childTag);
        if (sizeObj == null) {
            sizeObj = Json.createObjectBuilder().build();
        }
        JsonObjectBuilder childrenBuilder = jsonObjectToBuilder(sizeObj);
        for (int temp = 0; temp < childNodes.getLength(); temp++) {
            Node nNode = childNodes.item(temp);
            System.out.println("\nCurrent Element :" + nNode.getNodeName());

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) nNode;
                if (el.getTextContent() != null) {
//                    annotationBuilder.add(nNode.getNodeName(), el.getTextContent());

                    childrenBuilder.add(el.getNodeName(), el.getTextContent());
                }
            }
        }
        annotationBuilder = jsonObjectToBuilder(annotationObj);
        annotationBuilder.add(childTag, childrenBuilder.build());

    }

    JsonObjectBuilder addBndbox(JsonObject objectObj, Element parentEl, String childTag) {
        NodeList nodeLists = parentEl.getElementsByTagName(childTag);
        Node childEl = nodeLists.item(0);
        NodeList childNodes = childEl.getChildNodes();

        JsonObject sizeObj = objectObj.getJsonObject(childTag);
        if (sizeObj == null) {
            sizeObj = Json.createObjectBuilder().build();
        }
        JsonObjectBuilder childrenBuilder = jsonObjectToBuilder(sizeObj);
        for (int temp = 0; temp < childNodes.getLength(); temp++) {
            Node nNode = childNodes.item(temp);
            System.out.println("\nCurrent Element :" + nNode.getNodeName());

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) nNode;
                if (el.getTextContent() != null) {
//                    annotationBuilder.add(nNode.getNodeName(), el.getTextContent());

                    childrenBuilder.add(el.getNodeName(), Integer.parseInt(el.getTextContent()));
                }
            }
        }
        JsonObjectBuilder objectBuilder = jsonObjectToBuilder(objectObj);
        objectBuilder.add(childTag, childrenBuilder.build());
        return objectBuilder;
    }

    void addJsonArray(JsonObject parentObj, Element parentEl, String childTag) {
//        JsonObject parentObj = annotationBuilder.build();
        NodeList nodeLists = parentEl.getElementsByTagName(childTag);
        JsonArrayBuilder jsonArrayB = Json.createArrayBuilder();
        for (int n = 0; n < nodeLists.getLength(); n++) {
            Node childEl = nodeLists.item(n);
            NodeList childNodes = childEl.getChildNodes();

            JsonObject sizeObj = parentObj.getJsonObject(childTag);
            if (sizeObj == null) {
                sizeObj = Json.createObjectBuilder().build();
            }
            JsonObjectBuilder childrenBuilder = jsonObjectToBuilder(sizeObj);
            for (int temp = 0; temp < childNodes.getLength(); temp++) {
                Node nNode = childNodes.item(temp);
                System.out.println("\nCurrent Element :" + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) nNode;
                    if (el.getTextContent() != null) {
//                    annotationBuilder.add(nNode.getNodeName(), el.getTextContent());

                        childrenBuilder.add(el.getNodeName(), el.getTextContent());
                    }
                }
                jsonArrayB.add(childrenBuilder);
            }
        }
        annotationBuilder = jsonObjectToBuilder(parentObj);
        annotationBuilder.add(childTag + "s", jsonArrayB);
    }
}
