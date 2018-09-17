/**
 * 
 */
package chord;

import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

import java.math.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;


public class ChordProtocol implements EDProtocol, Comparable<ChordProtocol> {

	
	public Node node; 
	
	public BigInteger predecessor;
	public BigInteger[] fingerTable, successorList;
	public BigInteger chordId;

	public int fingerToFix=0;

	public ChordProtocol(String prefix) {
	}


	public void processEvent(Node node, int pid, Object event) {
		
		ChordMessage msg = (ChordMessage) event;
		receive(msg);		
	}
	
	private void receive(ChordMessage msg){
		switch(msg.getType()){
		case ChordMessage.LOOK_UP:
			onRoute(msg);
			break;
		case ChordMessage.SUCCESSOR:
			onRoute(msg);
			break;
		case ChordMessage.SUCCESSOR_FOUND:
			onSuccessorFound(msg);
			break;
		case ChordMessage.FINAL:
			onFinal(msg);
			break;
		case ChordMessage.NOTIFY:
			onNotify(msg);
			break;
		
		}

	}
	
	private void send(ChordMessage msg, BigInteger destID){
		Transport transport = (Transport) node.getProtocol(Utils.TID);
		msg.addToPath(destID);
		ChordProtocol cpDest = Utils.NODES.get(destID);  
		if(cpDest != null && cpDest.isUp())
			transport.send(node, cpDest.node, msg, Utils.PID);
		else
			Utils.FAILS++;
	}
		
	
	public void onRoute(ChordMessage msg){
		BigInteger target = (BigInteger)msg.getContent();
		Object content = msg.isType(ChordMessage.LOOK_UP) ? msg.getPath(): Utils.NODES.get(successorList[0]).clone();
		int type = msg.isType(ChordMessage.LOOK_UP) ? ChordMessage.FINAL : ChordMessage.SUCCESSOR_FOUND;
		if (target.equals(chordId) ||
			(msg.isType(ChordMessage.SUCCESSOR) && inAB(target, chordId, successorList[0]))) {
			
			if(msg.isType(ChordMessage.SUCCESSOR) && target.equals(chordId))
				content = this.clone();
			ChordMessage finalmsg = new ChordMessage(type, content);
			finalmsg.setLabel(msg.getLabel());
			finalmsg.setSender(chordId);
			send(finalmsg, msg.getSender());
		}
		else{
			BigInteger dest = closestPrecedingNode(target);
			if (dest == null) 
				Utils.FAILS++;
			else 
				send(msg, dest);
		}
	}

	public void onFinal(ChordMessage msg){
		Utils.receivedMessages.add(msg);
		Utils.SUCCESS++;
	}
	
	public void onSuccessorFound(ChordMessage msg){
		String label = msg.getLabel();
		ChordProtocol succ = (ChordProtocol)msg.getContent();
		if(label.contains("successor")) //predecessor
		{
			BigInteger pred = succ.predecessor;
			if(label.contains("first") || pred.equals(chordId) || !Utils.isUp(pred)){
				successorList[0] = succ.chordId;
				if(label.contains("first")) predecessor = pred;
				System.arraycopy(succ.successorList,0,successorList,1,successorList.length-1);
			}
			else if(label.contains("stabilize")){
				if (inAB(pred, chordId, succ.chordId)){
					successorList[0] = pred;
					successorList[1] = succ.chordId;
					System.arraycopy(succ.successorList,0,successorList,2,successorList.length-2);
				}
				notify(successorList[0]);
			}
		}
		else if(label.contains("finger")){
			int index = Integer.parseInt(label.split(" ")[1]);
			fingerTable[index] = succ.chordId;
		}
	}
	
	public void onNotify(ChordMessage msg){
		BigInteger nodeId = (BigInteger) msg.getContent();
		if (predecessor == null || 
			(inAB(nodeId, predecessor, this.chordId)
			&& !nodeId.equals(chordId)))
			predecessor = nodeId;
	}
	
	public void join(Node myNode) {
		node = myNode;
		// search a bootstrap node to join  
		Node n;
		do {
			n = Network.get(CommonState.r.nextInt(Network.size()));
		} while (n == null || !n.isUp());
		
		chordId = Utils.generateNewID();
		Utils.NODES.put(chordId, this);
		ChordProtocol cpRemote = Utils.getChordFromNode(n);
		successorList = new BigInteger[Utils.SUCC_SIZE];
		fingerTable = new BigInteger[Utils.M];

		findSuccessor(cpRemote.chordId, chordId, "successor first");
		for (int i = 0; i < fingerTable.length; i++) {
			long a = (long) (chordId.longValue() + Math.pow(2, i)) %(long)Math.pow(2, Utils.M);
			BigInteger id = new BigInteger(a+"");
			findSuccessor(cpRemote.chordId, id, "finger " + i);
		}
		System.out.println("Node " + chordId + " is in da house");
	}

	public void findSuccessor(BigInteger nodeToAsk, BigInteger id, String label){
		ChordMessage predmsg = new ChordMessage(ChordMessage.SUCCESSOR, id);
		predmsg.setLabel(label);
		predmsg.setSender(chordId);
		send(predmsg, nodeToAsk);
	}
	
	
	private BigInteger closestPrecedingNode(BigInteger id) {

		ArrayList<BigInteger> fullTable = getFullTable();
		BigInteger found = null;
		for (int i = fullTable.size()-1; i >= 0; i--) {
			BigInteger entry = fullTable.get(i);
			if (entry != null && Utils.isUp(entry) && inAB(entry, this.chordId, id) ) {
				found = entry;
				break;
			}
		}
		
		return found;
	}

	private ArrayList<BigInteger> getFullTable(){
		ArrayList<BigInteger> fullTable = new ArrayList<BigInteger>();
		HashSet<BigInteger> hs = new HashSet<BigInteger>();
		hs.addAll(Arrays.asList(fingerTable));
		hs.addAll(Arrays.asList(successorList));
		fullTable.addAll(hs);
		fullTable.sort(new Comparator<BigInteger>() {
			@Override
			public int compare(BigInteger arg0, BigInteger arg1) {
				int dist1 = Utils.distance(chordId, arg0);
				int dist2 = Utils.distance(chordId, arg1);
				return dist1 -dist2;
			}
		});
		
		fullTable.add(predecessor);
		return fullTable;
	}

	public void notify(BigInteger nodeId){
		ChordMessage notifyMsg = new ChordMessage(ChordMessage.NOTIFY, chordId);
		notifyMsg.setSender(chordId);
		send(notifyMsg, nodeId);
	}
	
	public void stabilize() {
		for(BigInteger succ: successorList){
			if(succ != null && Utils.isUp(succ)){
				successorList[0] = succ;
				findSuccessor(succ, succ, "successor stabilize");
				return;
			}
		}
		System.err.println("All successors of node " + this.chordId + " are down!");
		System.exit(1); //something went totally wrong
		
	}
	
	public void fixFingers(){
		if(fingerToFix >= fingerTable.length)
			fingerToFix = 0;
		long a = (long) (chordId.longValue() + Math.pow(2, fingerToFix)) %(long)Math.pow(2, Utils.M);
		BigInteger id = new BigInteger(a+"");
		findSuccessor(chordId, id, "finger " + fingerToFix);
		fingerToFix++;
	}

	
	private boolean inAB(BigInteger bid, BigInteger ba, BigInteger bb){
		long id = bid.longValue();
		long a = ba.longValue();
		long b = bb.longValue();
		if (id == a || id == b) return true;
		
		if(id > a && id < b)
			return true;
		if(id < a && a > b && id < b)
			return true;
		
		if(id > b && a > b && id > a)
			return true;
		
		
		return false;
	}
	

	
	@Override
	public String toString(){
		return this.chordId.toString();
	}


	public boolean equals(Object arg0){
		if(arg0 == null || !arg0.getClass().equals(this.getClass()))
			return false;
		return this.compareTo((ChordProtocol)arg0) == 0;
		
	}
	
	@Override
	public int compareTo(ChordProtocol arg0) {
		if (arg0 == null) return 100; 
		return this.chordId.compareTo(arg0.chordId);
	}

	public Object clone() {
		ChordProtocol inp = null;
        try {
            inp = (ChordProtocol) super.clone();
            
        } catch (CloneNotSupportedException e) {
        } // never happens
        return inp;
    }

	
	public boolean isUp(){
		return node.isUp();
	}
}
