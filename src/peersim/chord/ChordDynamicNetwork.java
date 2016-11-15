package peersim.chord;

import java.util.ArrayList;
import java.util.Collections;

import peersim.core.CommonState;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.dynamics.DynamicNetwork;

public class ChordDynamicNetwork extends DynamicNetwork {

	public ChordDynamicNetwork(String prefix) {
		super(prefix);
	}

	protected void remove(int n){
		//super.remove(n);
		ArrayList<Integer> inx = new ArrayList<Integer>();
		for(int i = 0; i < Network.size(); i++){
			inx.add(i);
		}
		Collections.shuffle(inx, CommonState.r);
		for(int i=0; i < n; i++){
			ChordProtocol cp = Utils.getChordFromNode(Network.get(inx.get(i)));
			System.out.println("Node " + cp.chordId + " died");
			Network.remove(inx.get(i));
//			Network.get(inx.get(i)).setFailState(Fallible.DOWN);
		}
	}
}
