package chord;

import java.util.ArrayList;
import java.util.Collections;

import peersim.core.Control;
import peersim.core.Network;

public class ResultObserver implements Control {
	
	public ResultObserver(String prefix) {}

	
	public boolean execute() {
		
		ArrayList<Integer> hopCounters = new ArrayList<Integer>(); 
	
		int max = 0;
		int min = Integer.MAX_VALUE;
			
			
		for (int j = 0; j < Utils.receivedMessages.size(); j++){
			@SuppressWarnings("unchecked")
			int hops = ((ArrayList<String>)Utils.receivedMessages.get(j).getContent()).size()-1;
			if (hops > max)
				max = hops;
			if (hops< min)
				min = hops;
			hopCounters.add(hops);
		}
				
		
		double mean = meanCalculator(hopCounters);
		
		System.out.println("Mean:  " + mean + " Max Value: " + max+ " Min Value: " + min);
		System.out.println("Failures: " + Utils.FAILS+ " Success: " + Utils.SUCCESS);
		System.out.println("Final system size: " + Network.size());
		return false;
	}

	private double meanCalculator(ArrayList<Integer> list) {
		
		int lenght = list.size();
		if (lenght == 0)
			return 0;
		int sum = 0;
		for (int i = 0; i < lenght; i++) {
			sum = sum + ((Integer) list.get(i)).intValue();
		}
		double mean = (double) sum / lenght;
		return mean;
	}

}
