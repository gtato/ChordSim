package peersim.chord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import peersim.core.Node;
import peersim.reports.GraphObserver;



public class LinkObserver extends GraphObserver {

    
    HashMap<Long, double[]> cords = new HashMap<Long, double[]>();
    ArrayList<ChordProtocol> orderedGraph = new ArrayList<ChordProtocol>();
    ArrayList<double[]> coordinates = new ArrayList<double[]>();
    HashMap<Long, Integer> positions = new HashMap<Long, Integer>();
    String filename = "graph.dat";
    
	public LinkObserver(String prefix) {
		super(prefix);		
	}

	
	public boolean execute() {
		updateGraph();
		generateGraph();
		graphToFile();
		return false;
	}

	protected void graphToFile() {
		
		try {
			File file = new File(filename);
			
			
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
            PrintStream ps =  new PrintStream(fos);

            printGraph(ps);
            fos.close();
            ps.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


	private void generateGraph(){
		for (int i = 0; i < g.size(); i++) {
			ChordProtocol each = Utils.getChordFromNode((Node)g.getNode(i));
        	if (each.isUp())
				orderedGraph.add(each);
		}
		
		Collections.sort(orderedGraph);
		
		long lastdc = 0;
		
		int j=0;
		for(ChordProtocol each: orderedGraph){
			long dcid = each.chordId.longValue();
			if(dcid - lastdc > 1)
				for(int i = 1; i < dcid - lastdc; i++)
					getNextCoordinate(lastdc+i);
			
	    	double[] coords = getNextCoordinate(dcid);
	    	coordinates.add(coords);
	    	positions.put(each.chordId.longValue(), j);
			lastdc = dcid;
			j++;
		}
		
		

	}
	
	private double[] getNextCoordinate(long id){
		double radius = 0.5;
		double[] center = {radius, radius};
		double unitangle = 2*Math.PI/ Math.pow(2, Utils.M); // getNrOnlineDC();
		double[] entry = new double[2];
		double angle = 0;
		if(cords.containsKey(id)){
			double[] angleNcount = cords.get(id);
			angle = angleNcount[0];
			radius = radius - angleNcount[1]*0.01; 
			angleNcount[1]++;
			cords.put(id, angleNcount);
		}else{
			angle = unitangle*cords.size();
			cords.put(id, new double[]{angle,1});
		}
		
		entry[0] = Math.cos(angle) * radius + center[0];
		entry[1] = Math.sin(angle) * radius + center[1];
		  
		return entry;
	}

	
	
	protected void printGraph(PrintStream ps) {
		boolean first;
		for (int i = 0; i < orderedGraph.size(); i++) {
			ChordProtocol current = orderedGraph.get(i);
			if(!current.isUp()) continue;
            double x_to = coordinates.get(i)[0];
            double y_to = coordinates.get(i)[1];
            first = true;
            for(ChordProtocol finger : current.fingerTable){
            	if(finger == null || !finger.isUp()) continue;
            	double[] coords = coordinates.get(positions.get(finger.chordId.longValue()));
            	double x_from =  coords[0];
                double y_from =   coords[1];
                String label = first ? current.chordId.intValue()+"": "";
                ps.println(x_from + " " + y_from );
//                if(i==0 || i==20)
                ps.println(x_to + " " + y_to + " " + label);
                ps.println();
                first = false;
            }
            
            
            
        }
		
	}
	

		
	
}
