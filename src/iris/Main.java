/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package iris;

import iris.plugin.IIris;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class runs the Iris ANN.
 * @author Ron Coleman
 */
public class Main {    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        IIris iris = loadIris();
        
        iris.normalizeData();
        
        iris.createTrainingData();
        
        iris.createNetwork();
        
        iris.trainNetwork();
        
        iris.testNetwork();
    }

    public static IIris loadIris() {
        String className = System.getProperty("iris.plugin");

        if (className == null) {
            className = "iris.plugin.DefaultIris";
        }

        Class<IIris> clazz;

        try {
            clazz = (Class<IIris>) Class.forName(className);

            IIris iris = clazz.newInstance();

            return iris;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
