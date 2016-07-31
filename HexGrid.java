package SOM;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

public class HexGrid <T>{
    int row,col;
    int cx,cy;
    T[][] grid;
    
    public HexGrid(){}

    public HexGrid(T[] data){
    	LoadData(data);
    }       
    public HexGrid(int r,T[] data){
        this(r,(int)(Math.ceil(data.length/r)),data);
    }
    public HexGrid(int r){
        this(r,r,null);
    }
    public HexGrid(int r, int c){
        this(r,c,null);
    }
    public HexGrid(int r,int c,T[] data) {
        LoadData(r, c, data);           
    }

    public void LoadData(T[] data)
    {
        int n = data.length;
        int r = (int) Math.ceil(Math.sqrt(n));
        LoadData(r, r, data);
    }
    @SuppressWarnings("unchecked")
    public void LoadData(int r, int c, T[] data){
        row     = r;
        col     = c;
        cx 		= row/2;
        cy 		= (col+(int)(Math.floor(row/2.0)))/2;
        
        grid    = (T[][]) Array.newInstance(data.getClass().getComponentType(),r, c+(int) Math.floor(r/2.0));
        if(data!=null)
            AssignValues(data);
    }
    
    public T safeItem(int x, int y)
    {
    	T obj = null;
    	//r, c+(int) Math.floor(r/2.0)
    	if(x>=0 && x < row && y >=(int)Math.ceil(x/2.0) && y < col+(int) Math.floor(x/2.0) )
    		obj = grid[x][y];
    	return obj;
    }

    
    public void search(T obj, int []res)
    {
    	boolean found = false;
    	for(int i = 0;i<row && !found;i++)
    	{
    		for(int j=(int)Math.ceil(i/2.0);j<col+(int) Math.floor(i/2.0);j++)
    		{
    			if(grid[i][j]==obj)
    			{
    				res[0] = i;
    				res[1] = j;
    				found = true;
    				break;
    			}
    		}
    	}
    }

    private void AssignValues(T[] data) {
        int dbug=0;
        for(int i=0; i<row; i++)
        {
        	int j;
        	
        	for(j=0;j<(int)(Math.ceil(i/2.0));j++)
        		grid[i][j] = null;
        	
            for(;j<col+(int)(Math.floor(i/2.0)); j++)
            {
                
                if(dbug<data.length)
                     grid[i][j] = data[dbug++];
                else
                	grid[i][j] = null;
            }
            
            for(;j<col+(int)(Math.floor(row/2.0)); j++)
            	grid[i][j] = null;
        }
        //System.out.printf("row=%d col=%d and Total = %d%n",row,col,dbug);
    }
    
    public ArrayList<T> slice(){
    	return slice(-1,-1);
    }
    
    public ArrayList<T>slice(int slice){
    	return slice(slice,-1);
    }
    
    /**
     * ## distance > 0 (-1 indicates full distance)
     * @param slice
     * @param distance
     * @return
     */
    public ArrayList<T> slice(int slice,int distance)
    {
    	ArrayList<T> list = new ArrayList<T>();
    	int r;
    	if(distance==-1)
    		r = (cx>cy?cx:cy); 
    	else r=distance;
    	
    	switch(slice){

    	case -1:
    	case 1:
    		list.add(grid[cx][cy]);
    		for(int i=cx, k =0;i>=0;i--,k++)
    		{
    			for(int j=(int)Math.ceil(i/2.0);j<cy-k;j++)
    			{
    				list.add(grid[i][j]);
    			}
    		}

    		if(slice!=-1)
    			break;
    	case 2:
    		for(int i=cx, k =0;i>=0;i--,k++) // will be same as slice #1
    		{
    			for(int j=cy-k;j<cy;j++) // starts from where slice #1 finish and continue till the center
    			{
    				list.add(grid[i][j]);
    			}
    		}
    		if(slice!=-1)
    			break;
    	case 3:
    		for(int i=cx, k =0;i>=0;i--,k++) // will be same as slice #1 and slice #2
    		{
    			for(int j=cy;j<col+(int) Math.floor(i/2.0);j++) // starts from where slice #1 finish and continue till the center
    			{
    				if(i==cx && j ==cy)
    					continue;
    				list.add(grid[i][j]);
    			}
    		}
    		if(slice!=-1)
    			break;
    	case 4:
    		for(int i=cx+1, k =1;i<row;i++,k++) 
    		{
    			for(int j=cy+k;j<col+(int) Math.floor(i/2.0);j++)
    			{
    				list.add(grid[i][j]);
    			}
    		}
    		if(slice!=-1)
    			break;
    		
    	case 5:
    		for(int i=cx+1, k =1;i<row;i++,k++) // same as slice #4 
    		{
    			for(int j = cy ; j<cy+k;j++)
    			{
    				list.add(grid[i][j]);
    			}
    		}
    		if(slice!=-1)
    			break;
    	case 6:
    		for(int i=cx+1, k =1;i<row;i++,k++) // same as slice #4 and slie #5 
    		{
    			for(int j = (int)Math.ceil(i/2.0) ; j<cy;j++) // col start same as slice #1
    			{
    				list.add(grid[i][j]);
    			}
    		}
    		if(slice!=-1)
    			break;
    	}
    	
    	return list;
    	
    }
        
    public ArrayList<T> ring(int radius) // center of the grid is the center of the ring
    {
    	return ring(cx,cy,radius);
    }

    public ArrayList<T> ring(T obj) // min(cx,cy) will be the radius 
    {
    	int[] res = new int[2];    	
    	int r = (cx<cy?cx:cy);
    	
    	search(obj, res);
    	
    	return ring(res[0],res[1],r);
    }
    
    public ArrayList<T> ring(int x, int y) // min(cx,cy) will be the radius 
    {
    	int r = (cx<cy?cx:cy);
    	return ring(x,y,r);
    }
    
    
    public ArrayList<T> ring(T obj,int radius)
    {
    	int[] res = new int[2];    	
    	search(obj, res);
    	
    	return ring(res[0],res[1],radius);
    }
    
    public ArrayList<T> ring(int x, int y,int radius) // x,y is the center
    {
    	ArrayList<T> list = new ArrayList<T>();
    	
    	if(radius != 0)
    	{
	    	for(int i=0; i <radius;i++)
	    	{
	    		list.add(safeItem(x-i		 ,y-radius		));
	    		list.add(safeItem(x-radius	 ,y-radius+i	));
	    		list.add(safeItem(x-radius+i ,y+i			));
	    		list.add(safeItem(x+i		 ,y+radius		));
	    		list.add(safeItem(x+radius	 ,y+radius-i	));
	    		list.add(safeItem(x+radius-i ,y-i			));
	    	}
    	}
    	else
    		list.add(safeItem(x,y));

    	list.removeAll(Collections.singleton(null));  
    	return list;
    }
    
    public Object testMore(int i, int j)
	{
		return grid[i][j];
	}

}