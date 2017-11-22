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
package process.plugin;

import process.util.Constant;
import static process.util.Constant.CLASSIFYING;
import process.util.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.encog.mathutil.Equilateral;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;
import static process.util.Constant.TRAIN_FRACTION;
import static process.util.Constant.TRAIN_THRESHOLD;

/**
 * This class partially implements an neural process for the iris data.
 * @author Ron Coleman
 */
abstract public class AbstractIris implements INeuralProcess {
    // Controls certain debugging operations
    public static boolean DEBUGGING = false;

    // Defines the column data types in iris.csv
    public final static char[] DATA_TYPES = {
        Constant.TYPE_DECIMAL,  // sepal length
        Constant.TYPE_DECIMAL,  // sepal width
        Constant.TYPE_DECIMAL,  // petal length
        Constant.TYPE_DECIMAL,  // petal width
        Constant.TYPE_NOMINAL,  // iris classification
    };
    
    protected int trainStart = -1;
    protected int trainEnd = -1;
    protected int numTrainRows = -1;
    
    protected int testStart = -1;
    protected int testEnd = -1;
    protected int numTestRows = -1;
    
    // Training data set
    protected BasicMLDataSet trainingSet;
    
    // Neural network
    protected BasicNetwork network;
 
    // Using equilateral (as opposed to one-of-n) encoding
    protected Equilateral equilateral = null;
    
    // Nominal subtupes: setosa, versicolor, and virginica
    protected ArrayList<String> subtypes = null;
    
    // These are the normalized inputs in row-major format. That is,
    // the first dimension gives the column. For iris.csv rowMajorInputs[0] is the
    // sepal length col, rowMajorInputs[1] is the sepal width col, etc.
    protected double [][] rowMajorInputs = null;
    
    // These are the normalized inputs in col-major format. That is,
    // the first dimension gives the row. For iris.csv, each row contains an
    // observation together of sepal width, sepal length, petal width, and petal
    // length.
    protected double[][] inputs = null;
    
    // These are the normalized ideals in row-oriented form.
    // That is, every row contains an equilateral encoding of the nominal
    // subtype.
    protected double [][] ideals = null;
    
    // This contains the highs and lows for the respective column with string name.
    // The high is at index 0, and the low at index 1.
    protected HashMap<String,ArrayList<Double>> hilos = new HashMap<>();
    
    /**
     * Constructor
     * @param classifying Header column name
     * @param path File path of the CSV data
     */
    protected AbstractIris(String classifying, String path) {
        this(classifying,path,DATA_TYPES);
    }
    
    /**
     * Constructor
     * @param classifying Header column name
     * @param path File path of the CSV data
     * @param columnTypes Column types
     * @see process.util.Constant
     */
    protected AbstractIris(String classifying, String path, char[] columnTypes) {
        try {
            // Load the csv file into memory
            Helper.loadCsv(path, columnTypes);
            
            // Compute the data indexes
            this.trainStart = 0;
            this.trainEnd = (int) (Helper.rowCount * TRAIN_FRACTION + 0.5) - 1;
            this.numTrainRows = this.trainEnd - this.trainStart + 1;

            this.testStart = this.trainEnd + 1;
            this.testEnd = Helper.rowCount - 1;
            this.numTestRows = this.testEnd - this.testStart + 1;            
            
            // Get the number of subtypes of the nominal data
            this.subtypes = Helper.getNominalSubtypes();
        
            // Define the equilateral over [-1, 1].
            this.equilateral = new Equilateral(subtypes.size(), -1, 1); 
            
            
            
        } catch (Exception ex) {
            Logger.getLogger(AbstractIris.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Normalizes the data into inputs and ideals
     */
    @Override
    public void normalizeData() {
        hilos.clear();
        
        ArrayList<String> headers = Helper.headers;
        
        rowMajorInputs = new double[headers.size()-1][];
        
        for(int index=0; index < headers.size(); index++) {
            String header = headers.get(index);
            
            if(!headers.get(index).equals(CLASSIFYING)) {
                double[] normalized = normalizeInputs(header);
                rowMajorInputs[index] = normalized;
            }
            else {
                ideals = normalizeIdeals(header);
            }
        }
    }
    /**
     * Gets the normalized encodings for the column with string name in header.
     * @param header String name of the column, e.g., "setosa".
     * @return 1D array: normalize encoding in range [-1, 1]
     */    
    protected double[] normalizeInputs(String header) {
        double[] denormalized = new double[numTrainRows];

        ArrayList decimals = Helper.data.get(header);

        double max = Double.MIN_VALUE;
        double min = Double.MAX_EXPONENT;
        
        for (int index=trainStart; index <= trainEnd; index++) {
            double decimal = (Double) decimals.get(index);
            
            denormalized[index] = decimal;
            
            if(decimal > max)
                max = decimal;
            
            if(decimal < min)
                min = decimal;
        } 
        
        // Save range data we need later when normalizing test data
        ArrayList<Double> maxmins = new ArrayList<>();
        maxmins.add(max);
        maxmins.add(min);
        hilos.put(header, maxmins);

        // This buffer holds the normalized data
        double[] normalized = new double[numTrainRows];
        
        // Use normalization object where we can specify the denormalized
        // high and low and the normalized high and low.
        NormalizedField norm =
                new NormalizedField(NormalizationAction.Normalize, null, max, min, 1, -1);
        
        // Normalize the data
        for(int index=0; index < denormalized.length; index++)
            normalized[index] = norm.normalize(denormalized[index]);

        return normalized;
    }
    
    /**
     * Gets the normalized encodings for the column with string name in header
     * @param header String name of the column, e.g., "iris".
     * @return 2D array: [A][B] where A=all rows, B=n-1 dimension array
     */
    protected double[][] normalizeIdeals(String header) { 
        double[][] denormalized = new double[numTrainRows][];

        // This is the entire column of nominal data
        ArrayList nominals = Helper.data.get(header);
        
        // This will be "setosa," "versicolor," and "virginic" for iris data
        subtypes = Helper.getNominalSubtypes();
               
        // Get the encodings for the subtype
        for(int i=trainStart; i <= trainEnd; i++) {
            // Translate the subtype to an number for the equilateral coding
            String nominal = (String) nominals.get(i);
            
            boolean translated = false;
            
            for(int j=0; j < subtypes.size(); j++) {
                String name = subtypes.get(j);
                
                if(nominal.equals(name)) {
                    // This will be an n-1 dimensional array for n subtypes.
                    denormalized[i] = this.equilateral.encode(j);
                    
                    translated = true;
                    break;
                }
            }
            
            // If we didn't translate the nominal, something is wrong
            assert(translated);
        }
        
        return denormalized;
    }

    /**
     * Creates the training data.
     */
    @Override
    public void createTrainingData() {
        assert(rowMajorInputs != null && rowMajorInputs.length != 0 && ideals != null && ideals.length != 0);
        
        // Transpose the row-major input matrix into col-major form
        int numCols = Helper.headers.size() - 1;
        inputs = new double[numTrainRows][numCols];
        
        for(int row=0; row < numTrainRows; row++) {
            for(int col=0; col < numCols; col++) {
                inputs[row][col] = rowMajorInputs[col][row];
            }
        }
        
        trainingSet = new BasicMLDataSet(inputs, ideals);
    }
    
    /**
     * Trains the network.
     */
    @Override
    public void trainNetwork() {
        assert(network != null && trainingSet != null);
        
        // Gets a backpropagation algorithm for training
        final ResilientPropagation train = new ResilientPropagation(network, trainingSet);

        // Training the network until the error drop below our threshold
        int epoch = 1;

        do {
            train.iteration();
            
            System.out.println("Epoch #" + epoch + " Error:" + train.getError());
            
            epoch++;
        } while (train.getError() > TRAIN_THRESHOLD);
        
        train.finishTraining();        
    }
    
    /**
     * Creates the network.
     */
    @Override
    abstract public void createNetwork();
    
    /**
     * Tests the network.
     */
    @Override
    abstract public void testNetwork();
}
