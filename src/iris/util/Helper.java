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
package iris.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

/**
 * This class implements convenience methods and data structures.
 * @author Ron.Coleman
 */
public class Helper {
    // Toggle this for helper-specific debuggins
    public static boolean DEBUGGING = false;
    
    // All the data, decimal and nominal
    public static HashMap<String, ArrayList> data = new HashMap<>();
    
    // All the respective hearders
    public static ArrayList<String> headers = new ArrayList<>();
    
    //
    static HashMap<String,ArrayList<Integer>> oneofn = new HashMap<>();
    
    static public int rowCount = -1;
    
    // Universe of types
    static char[] universe = null;

    /**
     * Loads the data from a CSV file.
     * Assumes first row is the header row.
     * @param path File path
     * @param types Types of data, D, N, or -
     * @throws Exception 
     */
    public static void loadCsv(String path, char[] types) throws Exception {
        Helper.loadCsv(path, types, new Random(0));
    }
    
    /**
     * Loads the data from a CSV file.
     * Assumes first row is the header row.
     * @param path Path to the file.
     * @param types Types of data in each column: D decimal, N nominal, - skip
     * @param ran Random number generator to shuffle the rows
     * @throws FileNotFoundException
     * @throws IOException
     * @throws Exception 
     */
    public static void loadCsv(String path, char[] types, Random ran)
            throws FileNotFoundException, IOException, Exception {
        // Clear the old data
        universe = types;
        
        data.clear();
        
        headers.clear();
        
        oneofn.clear();
        
        // Preload the data--note this works well enough only for small
        // datasets.
        ArrayList<String> lines = preload(path, ran);


        // Process each row in turn, assuming the first row is the header.
        for(int row=0; row < lines.size(); row++) {
            if(DEBUGGING)
                System.out.println(row+": "+lines.get(row));
            
            String[] fields = lines.get(row).split(",");

            // Handle empty rows
            if (fields.length == 0) {
                continue;
            }

            // Validate fields and ontology length match
            if (fields.length != types.length) {
                throw new Exception("fields mismatch row " + row);
            }

            // Assumes row zero is a header row
            if (row == 0) {
                for (int col = 0; col < fields.length; col++) {
                    String title = fields[col];

                    headers.add(title);

                    data.put(title, new ArrayList<>());
                }
            } else {
                // Go through each field and convert it according to its type
                for (int col = 0; col < fields.length; col++) {
                    String title = headers.get(col);
                    
                    switch (types[col]) {
                        // This column is decimal data
                        case Constant.TYPE_DECIMAL:                            
                            double d = Double.parseDouble(fields[col]);

                            ArrayList colDoubles = data.get(title);

                            colDoubles.add(d);
                            break;
                            
                        // This column is nominal data
                        case Constant.TYPE_NOMINAL:
                            String s = fields[col];

                            ArrayList colNominals = data.get(title);

                            colNominals.add(s);
                            break;
                            
                        // Skip this column
                        case Constant.TYPE_SKIP:
                            break;
                            
                        default:
                            throw new Exception("bad type '"+ types[col] +"' row "+row);
                    }
                }
            }

        }
    }
    
    /**
     * Gets the subtypes for
     * @return 
     */
    public static ArrayList<String> getNominalSubtypes() {
        if(oneofn.isEmpty())
            oneofn = encodeOneOfN();
        
        Object[] keys = oneofn.keySet().toArray();

        ArrayList<String> subtypes = new ArrayList<>();
        for(Object key: keys) {
            subtypes.add((String)key);
        }
        
        return subtypes;
    }
    
    /**
     * Gets the number of subtypes for the nominal.
     * @return Number of subtypes
     */
    public static int getNominalSubtypeCount() {
        if(!oneofn.isEmpty())
            oneofn = encodeOneOfN();
        
        return getNominalSubtypes().size();
    }
    
    /**
     * Gets an encoded hash map of nominal types and their 1-of-n values as 1 or -1.
     * Assumes there's exactly one nominal type.
     * @return Hash map of nominal and its 1-of-n encoding
     */
    public static HashMap<String, ArrayList<Integer>> encodeOneOfN() {
        if(!oneofn.isEmpty())
            return oneofn;
        
        // Locate the nominal, if there is one.
        int index = -1;

        for (int k = 0; k < universe.length; k++) {
            if (universe[k] == Constant.TYPE_NOMINAL) {
                index = k;
                break;
            }
        }

        if(index == -1)
            return null;

        // This is the hash map we return
        oneofn = new HashMap<>();

        // Use this title to retrieve the nominal column
        String title = Helper.headers.get(index);

        ArrayList<String> nominals = Helper.data.get(title);

        // Count the number of unique nominal values
        HashMap<String, Integer> counter = new HashMap<>();

        for (String nominal : nominals) {
            int count = counter.getOrDefault(nominal, 0);

            counter.put(nominal, count + 1);
        }

        int numberNominals = counter.size();
        
        // Encode each nominal
        for(int j = 0; j < numberNominals; j++) {
            ArrayList<Integer> variable = new ArrayList<>();

            for (int k = 0; k < numberNominals; k++) {
                if (k == j) {
                    variable.add(1);
                } else {
                    variable.add(-1);
                }
            }
            
            // Store the encoding in the hash map
            Object[] keys = counter.keySet().toArray();
            
            oneofn.put((String) keys[j], variable);
        }
        
        return oneofn;
    }
    
    /**
     * Pre-loads the data into memory buffer and if necessary, randomizes it.
     * @param path Path to the file
     * @param randomized Whether or not to randomize the buffer
     * @return Buffer of lines
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private static ArrayList<String> preload(String path, Random ran) throws FileNotFoundException, IOException {
        // All line are store here
        ArrayList<String> lines = new ArrayList<>();
        
        // Open the file
        BufferedReader br = new BufferedReader(new FileReader(path));
        
        int row = 0;
        
        String firstLine = null;
        
        do {
            // Gets a line and adds it to the buffer of lines
            String line = br.readLine();
            
            // If we get to the end, return the buffer
            if(line == null) {
                rowCount = lines.size();
                
                Collections.shuffle(lines, ran);
                
                lines.add(0, firstLine);
              
                return lines;
            }
            
            if(row == 0)
                firstLine = line;
            else
                lines.add(line);
            
            row++;
        } while(true);
    }
    
    /**
     * Gets the title for a column.
     * @param col
     * @return Title
     */
    public static String getTitle(int col) {
        assert(col >= 0 && col < headers.size());
        
        return headers.get(col);
    }
}
