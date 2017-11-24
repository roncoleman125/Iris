/*
 Copyright (c) Ron Coleman

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package process;

import java.util.logging.Level;
import java.util.logging.Logger;
import process.plugin.INeuralProcess;


/**
 * This class runs the Iris model ANN.
 * @author Ron Coleman
 */
public class Main {    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Load the processor
        INeuralProcess process = loadProcess();
        
        // Normalize the data
        process.normalizeData();
        
        // From the normalized data, create the training data
        process.createTrainingData();
        
        // Build the ANN
        process.createNetwork();
        
        // Using the training data, train the network
        process.trainNetwork();
        
        // Using the test data, test the network
        process.testNetwork();
    }

    /**
     * Loads the process plug-in.
     * @return Process plug-in
     */
    private static INeuralProcess loadProcess() {
        // Check the properties for the iris plugin
        String className = System.getProperty("process.plugin");

        // If there is no iris in the properties, use the default iris
        if (className == null) {
            className = "process.plugin.DefaultIris";
        }

        Class<INeuralProcess> clazz;

        try {
            clazz = (Class<INeuralProcess>) Class.forName(className);

            INeuralProcess iris = clazz.newInstance();

            return iris;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        // If we get here, a proper plugin was not found.
        return null;
    }
}
