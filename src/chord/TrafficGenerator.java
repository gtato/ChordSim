
package chord;

import java.math.BigInteger;


import peersim.core.*;
import peersim.config.Configuration;
import peersim.edsim.EDSimulator;


public class TrafficGenerator implements Control {

	private static final String PAR_PROT = "protocol";
	private final int pid;

	public TrafficGenerator(String prefix) {
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
	}
	
	public boolean execute() {
		int size = Network.size();
		Node sender, target;
		do {
			sender = Network.get(CommonState.r.nextInt(size));
			target = Network.get(CommonState.r.nextInt(size));
		} while (sender == null || sender.isUp() == false || target == null
				|| target.isUp() == false);
			
//		sender = Network.get(49);
//		target = Network.get(1);
		
		ChordProtocol senderCp = Utils.getChordFromNode(sender);
		ChordProtocol targetCp = Utils.getChordFromNode(target);
		System.out.println("(" + CommonState.getTime() + ") sending from " + senderCp.chordId + " to " + targetCp.chordId );
		
		BigInteger trgID = targetCp.chordId;
//		trgID = new BigInteger("11111");
		ChordMessage message = new ChordMessage(ChordMessage.LOOK_UP, trgID);
		message.setSender(senderCp.chordId);
				
		EDSimulator.add(0, message, sender, pid);
		
		return false;
	}

}
