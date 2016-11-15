package peersim.chord;

import peersim.core.Control;
import peersim.core.Network;

public class ChordMaintainer implements Control{

	public ChordMaintainer(String prefix){
		
	}
	
	@Override
	public boolean execute() {
		for(int i = 0; i < Network.size(); i++){
			ChordProtocol cp = Utils.getChordFromNode(Network.get(i));
			cp.stabilize();
			cp.fixFingers();
		}
		return false;
	}

}
