package peersim.chord;

import java.util.ArrayList;
import java.util.Collections;

import peersim.core.Control;
import peersim.core.Network;

public class MessageCounterObserver implements Control {
	
	public MessageCounterObserver(String prefix) {}

	
	public boolean execute() {
		int size = Network.size();
		int totalStab = 0;
		int totFails = 0;
		int totSuccess = 0;
		ArrayList<Integer> hopCounters = new ArrayList<Integer>(); 
		
		hopCounters.clear();
		int max = 0;
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < size; i++) {
			ChordProtocol cp = Utils.getChordFromNode(Network.get(i));
			totalStab += cp.stabilizations;
			totFails += cp.fails;
			totSuccess += cp.success;
			cp.stabilizations = 0;
			cp.fails = 0;
			
			if (cp.lookupMessage.size() > 0) {
			
				int maxNew = Collections.max(cp.lookupMessage); // maxArray(counters, cp.index);
				if (maxNew > max)
					max = maxNew;
			
				for (int j = 0; j < cp.lookupMessage.size(); j++)
					hopCounters.add(cp.lookupMessage.get(j));
				int minNew = Collections.min(cp.lookupMessage);// minArray(counters, cp.index);
				if (minNew < min)
					min = minNew;
			}
			cp.emptyLookupMessage();
		}
		double media = meanCalculator(hopCounters);
		if (media > 0)
			System.out.println("Mean:  " + media + " Max Value: " + max
					+ " Min Value: " + min + " # Observations: "
					+ hopCounters.size());
		System.out.println("	 # Stabilizations: " + totalStab + " # Failures: " + totFails+ " # Success: " + totSuccess);
		System.out.println("");
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
