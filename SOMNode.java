package SOM;

import java.util.ArrayList;
import java.util.List;


public class SOMNode {
	
	SOMVector weights;
	
	int				  dimension;
	
	
	@SuppressWarnings("unchecked")
	public SOMNode(int d)
	{
		//friends,follower,page,groups,pictures,status
		int[] constrain={5000,50000,1000,1000,50000,5000};
		int[] mul	   ={10000,100000,10000,10000,100000,10000};
		
		dimension = d;
		weights = new SOMVector();
		
		for (int x=0; x<dimension ; x++) 
		{
			weights.addElement(new Double( (int)(Math.random()*mul[x])%constrain[x]));
		}
	}
	
	public double distance(SOMData a)
	{
		return weights.euclideanDist(a.weights);
	}
	
	public void adjustWeights(SOMVector input, double learningRate, double distanceFalloff)
	{
		double wt, vw;
		for (int w=0; w<weights.size(); w++) 
		{
			wt = ((Double)weights.elementAt(w)).doubleValue();
			vw = ((Double)input.elementAt(w)).doubleValue();
			wt += distanceFalloff * learningRate * (vw - wt);
			weights.setElementAt(new Double(wt), w);
		}
	}
}


/**
*
* @author  alanter
*/
class SOMVector extends java.util.Vector {
	
	/** Creates a new instance of VariantVector */
	public SOMVector() {
	}
	
	/** Calculates the distance between this vector and
	 *  v2.  Returns -999 if the vectors so not contain the
	 *  same number of elements, otherwise returns the
	 *  square of the distance.
	 */
	public double euclideanDist(SOMVector v2) {
		if (v2.size() != size())
			return -999;
		
		double summation = 0, temp;
		for (int x=0; x<size(); x++) {
			temp = ((Double)elementAt(x)).doubleValue() -
				   ((Double)v2.elementAt(x)).doubleValue();
			temp *= temp;
			summation += temp;
		}
		
		return summation;
	}
	
}
