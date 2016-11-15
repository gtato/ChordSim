package peersim.chord;

import java.util.ArrayList;
import java.util.Collections;

import peersim.core.Control;
import peersim.core.Network;

public class ResultObserver implements Control {
	
	public ResultObserver(String prefix) {}

	
	public boolean execute() {
		
		
		int totFails = 0;
		int totSuccess = 0;
		ArrayList<Integer> hopCounters = new ArrayList<Integer>(); 
		
		hopCounters.clear();
		int max = 0;
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < Network.size(); i++) {
			ChordProtocol cp = Utils.getChordFromNode(Network.get(i));
			totFails += cp.fails;
			totSuccess += cp.success;
			
			if (cp.receivedMessages.size() > 0) {
				for (int j = 0; j < cp.receivedMessages.size(); j++){
					
					@SuppressWarnings("unchecked")
					int hops = ((ArrayList<String>)cp.receivedMessages.get(j).getContent()).size()-1;
					if (hops > max)
						max = hops;
					if (hops< min)
						min = hops;
					hopCounters.add(hops);
				}
				
			}
			cp.emptyReceivedMessage();
		}
		double mean = meanCalculator(hopCounters);
		
		System.out.println("Mean:  " + mean + " Max Value: " + max+ " Min Value: " + min);
		System.out.println("Failures: " + totFails+ " Success: " + totSuccess);
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
