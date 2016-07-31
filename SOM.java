package SOM;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SOM extends HexGrid<SOMNode>{
	
	int m_iNumIterations	= 5000;
	static 	SOMNode[] 	gridWeights;
	
	SOM(int r,int c,int d)
	{
		gridWeights = new SOMNode[r*c];
		for(int i=0;i<r*c;i++)
		{
			gridWeights[i] = new SOMNode(d);
		}
		LoadData(gridWeights);
	}
	
	
	class threadSimData implements Callable<SOMNode>{
		
		SOMData d1;
		
		public threadSimData(SOMData somData) {
			d1 = somData;
		}

		@Override
		public SOMNode call() throws Exception {
			return findBMU(d1);
		}
		
	}
	@SuppressWarnings("unchecked")
	public SimResAr simulate(SOMData[] data) throws InterruptedException, ExecutionException {
		
		ExecutorService pool		= Executors.newFixedThreadPool(3);
		Future<SOMNode>[] futures	= new Future[data.length];
	
		int		i;
		for(i=0;i<data.length;i++)
		{
			futures[i] = pool.submit(new threadSimData(data[i]));
		}
		
		SimResAr  res = new SimResAr();
		
		for(i=0;i<data.length;i++)
		{
			SOMNode d  = (SOMNode) futures[i].get();
			/** group id **/
			int group_id=-1;
			for(int x=0;x<gridWeights.length;x++)
			{
				if(d == gridWeights[x])
				{
					group_id = x;
					break;
				}
			}
			if(res.search(group_id)>1)
				res.addToGroup(group_id, i);
			else res.addNewGroup(group_id, i);
		}		
		
		pool.shutdown();
		try {
			  pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MICROSECONDS);
		} 
		catch (InterruptedException e) {
			  
		}
		return res;
	}
	
	private SOMNode findBMU(SOMData d1) throws InterruptedException, ExecutionException
	{
		int		i;
		int 	threadWidth = 1000;
		int 	indexFu		= 0;
		
		ExecutorService pool		= Executors.newCachedThreadPool();
		@SuppressWarnings("unchecked")
		Future<BMURes>[] futures	= new Future[gridWeights.length/threadWidth+1];
		
		for(i=0;i<gridWeights.length;i+=threadWidth)
		{
			futures[indexFu++] = pool.submit(new threadFindBMU(i, threadWidth, d1));
		}
		
		double d = Double.MAX_VALUE;
		indexFu=0;
		BMURes res; 
		SOMNode BMUNode=null;
		for(i=0;i<gridWeights.length;i+=threadWidth)
		{
			res = (BMURes) futures[indexFu++].get();
			if(res.d<d)
			{
				d= res.d;
				BMUNode = gridWeights[res.k];
			}
		}
		pool.shutdown();
		return BMUNode;
	}

	public void train(SOMData[] trainingData) throws InterruptedException, ExecutionException 
	{
		int m_dMapRadius 				= (row>col?row:col)/2; 							//sigma (0)   
		double m_dTimeConstant 			= m_iNumIterations/Math.log(m_dMapRadius);  // lamda
		double constStartLearningRate 	= 0.1; 										// L(0)
		
		double m_dNeighbourhoodRadius;
		int m_iIterationCount;
		double m_dLearningRate = constStartLearningRate;
		
		for(m_iIterationCount=1;m_iIterationCount<=m_iNumIterations;m_iIterationCount++)// loop variable
		{
			
			SOMData d1 	= trainingData[((int)(Math.random()*1000))%trainingData.length];
			SOMNode	BMU = findBMU(d1);

			m_dNeighbourhoodRadius = Math.round(m_dMapRadius * Math.exp(-(double)m_iIterationCount/m_dTimeConstant)); //sigma(t)
			updateNeighbor(BMU,m_dNeighbourhoodRadius,d1,m_dLearningRate);
			
			m_dLearningRate = constStartLearningRate * Math.exp(-(double)m_iIterationCount/m_iNumIterations);
		}
		return;
	}
	
	private void updateNeighbor(SOMNode BMU, double m_dNeighbourhoodRadius, SOMData d1, double m_dLearningRate) 
	{
		ExecutorService pool		= Executors.newCachedThreadPool();
		
		for(int i = 1;i<=m_dNeighbourhoodRadius;i++)
		{
			pool.execute(new threadUpdateWeight(BMU,i, this, m_dNeighbourhoodRadius,d1,m_dLearningRate));
		}
		
		
		pool.shutdown();
		
		try {
			  pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} 
		catch (InterruptedException e) {
			  
		}
	}
	
	class threadUpdateWeight implements Runnable
	{
		private int 	radious;
		private final 	SOMNode BMU;
		private final   SOM refSOM;
		private final 	double m_dNeighbourhoodRadius;
		private final 	SOMData candadite;
		private final double m_dLearningRate;

		public threadUpdateWeight(SOMNode bmu, int r,SOM ref,double m_dN, SOMData d1, double m_dL)
		{
			BMU = bmu;
			radious = r;
			refSOM = ref;
			m_dNeighbourhoodRadius = m_dN;
			candadite = d1;
			m_dLearningRate = m_dL;
		}
		
		@Override
		public void run() 
		{
			//System.out.println(radious);
			double DistToNodeSq = radious*radious;
			double WidthSq = m_dNeighbourhoodRadius * m_dNeighbourhoodRadius;
			double m_dInfluence = Math.exp(-(DistToNodeSq) / (2*WidthSq));

			ArrayList<SOMNode> l = refSOM.ring(BMU, radious);
			for(int i = 0; i<l.size();i++)
			{	
				l.get(i).adjustWeights(candadite.weights, m_dLearningRate,m_dInfluence);
			}
		}
	}



	
	class BMURes{
		SOMNode a;
		double d;
		int k;
	}
	class threadFindBMU implements Callable<BMURes>
	{
		private int 	start;
		private int 	width;
		private final 	SOMData key;

		public threadFindBMU(int s, int w,SOMData a)
		{
			key   = a;
			start = s;
			width = w;
			
		}
		
		@Override
		public BMURes call() throws Exception {
			BMURes res = new BMURes();
			res.d 		= Double.MAX_VALUE;
			for(int i = start;i<start+width && i< gridWeights.length ;i++)
			{
				double b = gridWeights[i].distance(key);
				if( res.d >b )
				{
					res.d = b;
					res.a = gridWeights[i];
					res.k = i;
				}
			}
			return res;
		}
	}
}
