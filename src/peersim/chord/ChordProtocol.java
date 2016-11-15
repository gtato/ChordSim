/**
 * 
 */
package peersim.chord;

import peersim.chord.messaging.ChordMessage;
import peersim.config.Configuration;
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

	private static final String PAR_TRANSPORT = "transport";
 
	private int tid;
	public Node node; Transport transport;
	public ArrayList<ChordMessage> receivedMessages;	
	public ChordProtocol predecessor;
	public ChordProtocol[] fingerTable, successorList;
	public BigInteger chordId;

	public int fails=0, success=0, fingerToFix=0;

	public ChordProtocol(String prefix) {
		receivedMessages = new ArrayList<ChordMessage>();
		tid = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
	}


	public void processEvent(Node node, int pid, Object event) {
		transport = (Transport) node.getProtocol(this.tid);
		ChordMessage msg = (ChordMessage) event;
		receive(msg);		
	}
	
	private void receive(ChordMessage msg){
		switch(msg.getType()){
		case ChordMessage.LOOK_UP:
			onRoute(msg);
			break;
		case ChordMessage.FINAL:
			onFinal(msg);
			break;
		
		}

	}
	
	private void send(ChordMessage msg, BigInteger destID){
		msg.addToPath(destID);
		ChordProtocol cpDest = Utils.NODES.get(destID);  
		if(cpDest != null && cpDest.isUp())
			transport.send(node, cpDest.node, msg, Utils.PID);
		
	}
		
	
	public void onRoute(ChordMessage msg){
		BigInteger target = (BigInteger)msg.getContent();
		if (target.compareTo(chordId) == 0) {
			ChordMessage finalmsg = new ChordMessage(ChordMessage.FINAL, msg.getPath());
			finalmsg.setSender(chordId);
			send(finalmsg, msg.getSender());
		}
		else{
			ChordProtocol dest = closestPrecedingNode(target);
			if (dest == null) 
				fails++;
			else 
				send(msg, dest.chordId);
		}
	}

	public void onFinal(ChordMessage msg){
		receivedMessages.add(msg);
		success++;
	}
	
	public void join(Node myNode) {
		node = myNode;
		// search a node to join
		Node n;
		do {
			n = Network.get(CommonState.r.nextInt(Network.size()));
		} while (n == null || !n.isUp());
		
		chordId = Utils.generateNewID();
		Utils.NODES.put(chordId, this);
		ChordProtocol cpRemote = Utils.getChordFromNode(n);

		ChordProtocol successor = cpRemote.findSuccessor(chordId);
		successorList = new ChordProtocol[Utils.SUCC_SIZE];
		successorList[0] = successor;
		predecessor = successor.predecessor;
		fingerTable = new ChordProtocol[Utils.M];
		updateSuccessors();
		for (int i = 0; i < fingerTable.length; i++) {
			long a = (long) (chordId.longValue() + Math.pow(2, i)) %(long)Math.pow(2, Utils.M);
			BigInteger id = new BigInteger(a+"");
			fingerTable[i] = cpRemote.findSuccessor(id); 
		}
		System.out.println("Node " + chordId + " is in da house");
	}

	
	public ChordProtocol findSuccessor(BigInteger id) {
		ChordProtocol tmp = findPredecessor(id);
		return tmp.successorList[0];
	}
	
	public ChordProtocol findPredecessor(BigInteger id) {
		ChordProtocol tmp = this;
		while(!inAB(id, tmp.chordId, tmp.successorList[0].chordId))
			tmp = tmp.closestPrecedingNode(id);
		return tmp;
	}

	private ChordProtocol closestPrecedingNode(BigInteger id) {

		ArrayList<ChordProtocol> fullTable = getFullTable();
		ChordProtocol found = null;
		for (int i = fullTable.size()-1; i >= 0; i--) {
			ChordProtocol entry = fullTable.get(i);
			if (entry != null && entry.isUp() && inAB(entry.chordId, this.chordId, id) ) {
				found = entry;
				break;
			}
		}
		
		//do smth with downFingers
//		if(found == null){
//			stabilize();
//			return successorList[0];
//		}
		return found;
	}

	private ArrayList<ChordProtocol> getFullTable(){
		ArrayList<ChordProtocol> fullTable = new ArrayList<ChordProtocol>();
		HashSet<ChordProtocol> hs = new HashSet<ChordProtocol>();
		hs.addAll(Arrays.asList(fingerTable));
		hs.addAll(Arrays.asList(successorList));
		fullTable.addAll(hs);
		fullTable.sort(new Comparator<ChordProtocol>() {
			@Override
			public int compare(ChordProtocol arg0, ChordProtocol arg1) {
				int dist1 = Utils.distance(chordId, arg0.chordId);
				int dist2 = Utils.distance(chordId, arg1.chordId);
				return dist1 -dist2;
			}
		});
		
		fullTable.add(predecessor);
		return fullTable;
	}
//	// debug function
//	private void printFingers() {
//		for (int i = fingerTable.length - 1; i > 0; i--) {
//			if (fingerTable[i] == null) {
//				System.out.println("Finger " + i + " is null");
//				continue;
//			}
//			if(this.compareTo(fingerTable[i])==0)
//				break;
//			System.out.println("Finger["+ i+ "] = chordId " + fingerTable[i].chordId);
//		}
//	}

	
	public void stabilize() {
		updateSuccessors();
		ChordProtocol node = successorList[0].predecessor;
		if (this.compareTo(node)!=0){ 
			if (node.isUp() && inAB(node.chordId, chordId, successorList[0].chordId))
				successorList[0] = node;
			successorList[0].notify(this);
		}
	}

	

	public void notify(ChordProtocol node){
		if (predecessor == null || inAB(node.chordId, predecessor.chordId, this.chordId))
			predecessor = node;
	}

	
	
	public void updateSuccessors() {
		for(ChordProtocol successor : successorList){
			if(successor != null && successor.isUp()){
				successorList[0] = successor;
				System.arraycopy(successorList[0].successorList,0,successorList,1,successorList.length-1);
				return;
			}
		}
		System.err.println("All successors of node " + this.chordId + " are down!");
		System.exit(1); //something went totally wrong
		
	}
	
	public void fixFingers(){
		if(fingerToFix >= fingerTable.length)
			fingerToFix = 0;
		long id = (long) (chordId.longValue() + Math.pow(2, fingerToFix)) %(long)Math.pow(2, Utils.M);
		fingerTable[fingerToFix] = findSuccessor(new BigInteger(id+""));
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
	

	public void emptyReceivedMessage() {
		this.receivedMessages = new ArrayList<ChordMessage>();
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
