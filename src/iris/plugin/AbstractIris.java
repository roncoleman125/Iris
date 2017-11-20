/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package iris.plugin;

import iris.util.Helper;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.encog.mathutil.Equilateral;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;

/**
 * This class runs the Iris ANN.
 * @author Ron Coleman
 */
abstract public class AbstractIris implements IIris {
    public static boolean DEBUGGING = false;
    
    public final static String IRIS = "iris";
    
    public final static int TRAINING_START = 0;
    public final static int TRAINING_END = 119;
    public final static int TESTING_START = TRAINING_END + 1;
    public final static int TESTING_END = 150;
    
    public final static int NUM_TRAINING_ROWS = TRAINING_END - TRAINING_START + 1;
    
    public final static char[] DATA_TYPES = {
        Helper.TYPE_DECIMAL,
        Helper.TYPE_DECIMAL,
        Helper.TYPE_DECIMAL,
        Helper.TYPE_DECIMAL,
        Helper.TYPE_NOMINAL,
    };
    
    protected BasicMLDataSet trainingSet;
    protected BasicNetwork network;
    protected int NUM_HIDDEN = 4; 
    
    protected Equilateral equilateral = null;
    
    protected ArrayList<String> subtypes = null;
    
    protected double [][] ideals = null;
    protected double [][] inputs = null;
    
    protected AbstractIris(String classifying, String path) {
        this(classifying,path,DATA_TYPES);
    }
    
    protected AbstractIris(String classifying, String path, char[] columnTypes) {
        try {
            Helper.loadCsv(path, columnTypes);
            
            this.subtypes = Helper.getNominalSubtypes();
        
            this.equilateral = new Equilateral(subtypes.size(),-1,1); 
            
        } catch (Exception ex) {
            Logger.getLogger(AbstractIris.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void normalizeData() {
        ArrayList<String> headers = Helper.headers;
        
        inputs = new double[headers.size()-1][];
        
        for(int index=0; index < headers.size(); index++) {
            String header = headers.get(index);
            
            if(!headers.get(index).equals(IRIS)) {
                double[] normalized = normalizeInputs(header);
                inputs[index] = normalized;
            }
            else {
                ideals = normalizeIdeals(header);
            }
        }
    }
    
    protected double[] normalizeInputs(String header) {
        double[] denormalized = new double[NUM_TRAINING_ROWS];

        ArrayList decimals = Helper.data.get(header);

        double max = Double.MIN_VALUE;
        double min = Double.MAX_EXPONENT;
        
        for (int index=TRAINING_START; index < TRAINING_END; index++) {
            double decimal = (Double) decimals.get(index);
            
            denormalized[index] = decimal;
            
            if(decimal > max)
                max = decimal;
            
            if(decimal < min)
                min = decimal;
        } 
        
//        NormalizeArray norm = new NormalizeArray();
//
//        norm.setNormalizedHigh(max);
//        norm.setNormalizedLow(min);
//        
//        return /*DEBUGGING ? denormalized :*/ norm.process(denormalized); 

        double[] normalized = new double[NUM_TRAINING_ROWS];
        
        NormalizedField norm = new NormalizedField(NormalizationAction.Normalize,null,max,min,1,-1);
        
        for(int index=0; index < denormalized.length; index++)
            normalized[index] = norm.normalize(denormalized[index]);

        return normalized;
    }
    
    protected double[][] normalizeIdeals(String header) { 
        double[][] denormalized = new double[NUM_TRAINING_ROWS][];

        ArrayList nominals = Helper.data.get(header);
        
        subtypes = Helper.getNominalSubtypes();
               
        for(int i=TRAINING_START; i < TRAINING_END; i++) {
            String nominal = (String) nominals.get(i);
            
            boolean encoded = false;
            for(int j=0; j < subtypes.size(); j++) {
                String name = subtypes.get(j);
                
                if(nominal.equals(name)) {
                    denormalized[i] = this.equilateral.encode(j);
                    
                    encoded = true;
                    break;
                }
            }
            
            assert(encoded == true);
        }
        
        return denormalized;
    }

    @Override
    public void createTrainingData() {
        assert(inputs != null);
        assert(inputs.length != 0);
        assert(ideals != null);
        assert(ideals.length != 0);
        
        trainingSet = new BasicMLDataSet(inputs, ideals);
    }
    
    @Override
    public void trainNetwork() {
        final ResilientPropagation train = new ResilientPropagation(network, trainingSet);

        int epoch = 1;

        do {
            train.iteration();
            
            System.out.println("Epoch #" + epoch + " Error:" + train.getError());
            
            epoch++;
        } while (train.getError() > 0.01);
        
        train.finishTraining();        
    }
    
    @Override
    abstract public void createNetwork();
    
    @Override
    abstract public void testNetwork();
}
