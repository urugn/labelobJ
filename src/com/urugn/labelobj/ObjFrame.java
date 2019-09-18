/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.urugn.labelobj;

import javafx.scene.image.Image;

/**
 *
 * @author UruGN
 */
public class ObjFrame extends Image{
    
    
    public ObjFrame(String url) {
        super(url);
    }

    public ObjFrame( String url, boolean backgroundLoading) {
        super(url, backgroundLoading);
    }
    
    
    
}
