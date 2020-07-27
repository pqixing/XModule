package com.pqixing.intellij.ui;

import java.awt.*;
import javax.swing.JFrame;

/**
 * Méthodes statiques pour récupérer les informations d'un écran.
 *
 * @author Jean-Claude Stritt
 * @version 1.0 / 24.2.2009
 */
public class ScreenInfo {

    /**
     * Permet de récupérer le numéro de l'écran par rapport à la fenêtre affichée.
     * @return le numéro 1, 2, ... (ID) de l'écran
     */
    public static int getScreenID( Component jf ) {
        int scrID = 1;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        for (int i = 0; i < gd.length; i++) {
            GraphicsConfiguration gc = gd[i].getDefaultConfiguration();
            Rectangle r = gc.getBounds();
            if (r.contains(jf.getLocation())) {
                scrID = i+1;
            }
        }
        return scrID;
    }

    /**
     * Permet de récupérer la dimension (largeur, hauteur) en px d'un écran spécifié.
     * @param scrID --> le n° d'écran
     * @return la dimension (largeur, hauteur) en pixels de l'écran spécifié
     */
    public static Dimension getScreenDimension( int scrID ) {
        Dimension d = new Dimension(0, 0);
        if (scrID > 0) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            DisplayMode mode = ge.getScreenDevices()[scrID - 1].getDisplayMode();
            d.setSize(mode.getWidth(), mode.getHeight());
        }
        return d;
    }

    /**
     * Permet de récupérer la largeur en pixels d'un écran spécifié.
     * @param scrID --> le n° d'écran
     * @return la largeur en px de l'écran spécifié
     */
    public static int getScreenWidth( int scrID ) {
        Dimension d = getScreenDimension(scrID);
        return d.width;
    }

    /**
     * Permet de récupérer la hauteur en pixels d'un écran spécifié.
     * @param scrID --> le n° d'écran
     * @return la hauteur en px de l'écran spécifié
     */
    public static int getScreenHeight( int scrID ) {
        Dimension d = getScreenDimension(scrID);
        return d.height;
    }

}