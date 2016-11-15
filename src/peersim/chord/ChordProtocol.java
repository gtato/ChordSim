/**
 * 
 */
package peersim.chord;

import peersim.chord.messaging.FinalMessage;
import peersim.chord.messaging.LookUpMessage;
import peersim.config.Configuration;
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
	public Node node; 
	public ArrayList<Integer> lookupMessage;	
	public ChordProtocol predecessor;
	public ChordProtocol[] fingerTable, successorList;
	public BigInteger chordId;

	public int stabilizations=0, fails=0, success=0, fingerToFix=0;

	public ChordProtocol(String prefix) {
		lookupMessage = new ArrayList<Integer>();
		tid = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
	}


	public void processEvent(Node node, int pid, Object event) {
		if (event.getClass() == LookUpMessage.class) {
			LookUpMessage message = (LookUpMessage) event;
			message.increaseHopCounter();
			BigInteger target = message.getTarget();
			Transport t = (Transport) node.getProtocol(this.tid);
			Node n = message.getSender();
			if (target.compareTo(chordId) == 0) {
				t.send(node, n, new FinalMessage(message.getHopCounter(), message.getPath().toString()), pid);
			}
			else{
				ChordProtocol dest = closestPrecedingNode(target);
				if (dest == null) 
					fails++;
				else {
					message.addToPath(dest.chordId);
					t.send(message.getSender(), dest.node, message, pid);
				}
			}
		}
		if (event.getClass() == FinalMessage.class) {
			FinalMessage message = (FinalMessage) event;
			lookupMessage.add(message.getHopCounter());
			success++;
		}
	}
	

	public void stabilize() {
		updateSuccessors();
		ChordProtocol node = successorList[0].predecessor;
		if (this.compareTo(node)!=0){ 
			if (inAB(node.chordId, chordId, successorList[0].chordId))
				successorList[0] = node;
			successorList[0].notify(this);
		}
	}

	

	public void notify(ChordProtocol node){
		if (predecessor == null || inAB(node.chordId, predecessor.chordId, this.chordId))
			predecessor = node;
	}

	
	
	public void updateSuccessors() {
//		if(!firstTime && successorList[0] != null && successorList[0].isUp()) return;
		
		for(ChordProtocol successor : successorList){
			if(successor != null && successor.isUp()){
				successorList[0] = successor;
				System.arraycopy(successorList[0].successorList,0,successorList,1,successorList.length-1);
				return;
			}
		}
		System.exit(1); //something went totally wrong
		
	}

	public ChordProtocol findSuccessor(BigInteger id) {
		ChordProtocol tmp = findPredecessor(id);
		return tmp.successorList[0];
	}
	
	public ChordProtocol findPredecessor(BigInteger id) {
		
//		updateSuccessor();
		ChordProtocol tmp = this;
		while(!inAB(id, tmp.chordId, tmp.successorList[0].chordId))
			tmp = tmp.closestPrecedingNode(id);
		
		return tmp;
	}

	private ChordProtocol closestPrecedingNode(BigInteger id) {
//		ArrayList<ChordProtocol> downFingers = new ArrayList<ChordProtocol>();
		ArrayList<ChordProtocol> fullTable = getFullTable();
		ChordProtocol found = null;
		for (int i = fullTable.size()-1; i >= 0; i--) {
			ChordProtocol entry = fullTable.get(i);
			if (entry == null || !entry.isUp()) {
//				downFingers.add(entry);
				continue;
			}
			
			if (found == null && inAB(entry.chordId, this.chordId, id)) 
				found = entry;
		}
		
		//do smth with downFingers
		if(found == null){
			stabilize();
			return successorList[0];
		}
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
	

	public void emptyLookupMessage() {
		this.lookupMessage = new ArrayList<Integer>();
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
