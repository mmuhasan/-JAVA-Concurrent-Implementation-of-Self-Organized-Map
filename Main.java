package SOM;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Main {
	
	private static SOMData[] trainingData;
	private static SOMData[] testData;
	private static int dataDimension=5;
	private static SOM mapHex;
	
	public static void main(String[] args) {
		System .out.println("Initializing SOM");
		mapHex = new SOM(10,10,dataDimension);
		System .out.println("Creating Training Data");
		loadTrainingData(5000);
		try {
			System .out.println("Training");
			mapHex.train(trainingData);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		testData = trainingData;
		loadTrainingData(2000);
		
		SimResAr res = new SimResAr();
		try {
			System .out.println("Simulating");
			res = mapHex.simulate(trainingData);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		printResult(res);
		
	}
	
	private static void printResult(SimResAr res) 
	{
		for(int i =0; i<res.groups.size();i++)
		{
			System.out.printf("Group %d: (",i+1);
			SOMNode s= SOM.gridWeights[res.groups.get(i).group_id];
			
			for(int j=0;j<dataDimension;j++)
				System.out.printf("%.4f ",s.weights.get(j));
			
			System.out.printf(")\nGroup Members (%d):\n",res.groups.get(i).members.size());
			for(int x =0; x<res.groups.get(i).members.size();x++)
			{
				int id_member = res.groups.get(i).members.get(x);
				System.out.printf("%d (",id_member);
				for(int j=0;j<dataDimension;j++)
					System.out.printf("%.0f ",trainingData[id_member].weights.get(j));
				System.out.printf(") ");
			}
			System.out.printf("\n\n");
		}
	}

	private static void loadTrainingData(int n)
	{
		
		trainingData = new SOMData[n];
		for(int i = 0; i<n;i++)
		{
			trainingData[i] = new SOMData(dataDimension);
		}
		
	}
}

class SimResAr{
	ArrayList<SimRes> groups = new ArrayList<SimRes>();
	Lock l = new ReentrantLock();

	int search(int id_group)
	{
		int res = -1;
		for(int i=0;i<groups.size();i++)
		{
			if(groups.get(i).group_id==id_group)
			{
				res = i;
			}
		}			
		return res;
	}
	
	boolean addToGroup(int id_group, int id)
	{
		boolean res=false;
		for(int i=0;i<groups.size();i++)
		{
			if(groups.get(i).group_id==id_group)
			{
				groups.get(i).addData(id);
				res = true;
			}
		}			
		if(!res)
			addNewGroup(id_group, id);
		return true;
	}
	
	boolean addNewGroup(int id_group, int i)
	{
		l.lock();
		try{
			/** search again because by the the thread acquire the lock some one else may add the same group */
			int k = search(id_group);
			if(0>k)
			{
				groups.add(new SimRes(id_group,i));
				return true;
			}
			else
			{
				groups.get(k).addData(i);
				return true;
			}
		}
		finally{
			l.unlock();
		}
	}
	
	
	class SimRes
	{
		int group_id;
		ArrayList<Integer> members;
		Lock l;	
		
		public SimRes(int id_group, int i) {
			l = new ReentrantLock();
			group_id = id_group;
			members = new ArrayList<Integer>();
			members.add(i);
		}

		public void addData(Integer i)
		{
			l.lock();
			try{
				members.add(i);
			}
			finally
			{
				l.unlock();
			}
		}
	}
}


class SOMData
{	
	SOMVector weights;
	
	public SOMData()
	{
		weights = new SOMVector();	
	}
	
	@SuppressWarnings("unchecked")
	public SOMData(int n,int i)
	{
		double[][] P = { {100,100}, {200,203},  {500,550}, {1000,1120}, {340,340}, {620,655}, {733,1000}, {1950,5000}, {2100,7000}, {50,50}, {462,1000}, {755,76}};;
		
		weights = new SOMVector();
		for (int x=0; x<n; x++) 
		{
			weights.addElement(P[i][x]);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public SOMData(int n)
	{
		int[] constrain={5000,50000,1000,1000,50000,5000};
		int[] mul	   ={10000,100000,10000,10000,100000,10000};
		

		weights = new SOMVector();
		for (int x=0; x<n; x++) 
		{
			weights.addElement(new Double( (int)(Math.random()*mul[x])%constrain[x]));
		}
	}
	
	public SOMData(SOMVector a)
	{
		weights = a;
	}
}
