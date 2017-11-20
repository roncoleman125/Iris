/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package iris.plugin;

/**
 *
 * @author Ron.Coleman
 */
public class DefaultIris extends AbstractIris {

    public DefaultIris() {
        this("iris","iris.csv");
    }
    
    public DefaultIris(String classifying, String path) {
        super(classifying, path);
    }

    @Override
    public void createNetwork() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void testNetwork() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
