/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.urugn.labelobj;

import javafx.scene.Cursor;

/**
 *
 * @author UruGN
 */
interface LabelobScene {

    public void addLabel();

    public void removeLabel();

    public void setCursor(Cursor curser);

    public void highlightLabel(ObjLabel objLabel);

    ObjLabel getSelectedLabel();

    /**
     * In case we may need some extra width in this scene
     * @return 
     */
    public double extraHeight();
    
    /**
     * In case we may need some extra height in this scene
     * @return 
     */
    public double extraWidth();
}
