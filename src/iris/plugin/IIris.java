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
public interface IIris {
    public void normalizeData();
    public void createTrainingData();
    public void createNetwork();
    public void trainNetwork();
    public void testNetwork();
}
