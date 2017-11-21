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
package process.util;

/**
 * This class contains most of the constants needed for the iris data.
 * @author Ron.Coleman
 */
public class Constant { 
    //// THESE DATA MAY NEED TO CHANGE FOR DIFFERENT DATA SETS ////
    
    // Header column title in iris.csv we're classifying
    public final static String CLASSIFYING = "iris"; 
    
    // Training data set indexes in iris.csv
    public final static int TRAINING_START = 0;
    public final static int TRAINING_END = 119;
    public final static int NUM_TRAINING_ROWS = TRAINING_END - TRAINING_START + 1;
    
    // Test data set indexes in iris.csv
    public final static int TEST_START = TRAINING_END + 1;
    public final static int TEST_END = 150;
    public final static int NUM_TEST_ROWS = TRAINING_END - TRAINING_START + 1;
    
    // Number of neurons in the (one) hidden layer
    public final static int NUM_HIDDEN = 4;

    //// THESE DATA PROBABLY WONT CHANGE FOR DIFFERENT DATA SETS ////  
    
    // Training error threshold
    public final static double TRAINING_THRESHOLD = 0.01;
    
    public final static char TYPE_DECIMAL = 'D';
    public final static char TYPE_NOMINAL = 'N';
    public final static char TYPE_SKIP = '-';  
}
