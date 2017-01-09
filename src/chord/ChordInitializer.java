package chord;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;

public class ChordInitializer implements NodeInitializer, Control {

	private static final String PAR_PROT = "protocol";
	private static final String PAR_TRANS = "transport";

	int pid = 0;
	int tid = 0;
	

	public ChordInitializer(String prefix) {
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
		tid = Configuration.getPid(prefix + "." + PAR_TRANS);
		Utils.initialize(pid, tid);
	}

	
	public boolean execute() {
		ArrayList<BigInteger> ids = Utils.generateIDs(Network.size());
		
		for (int i = 0; i < Network.size(); i++) {
			Node node = (Node) Network.get(i);
			ChordProtocol cp = Utils.getChordFromNode(node);
			cp.node = node;
			cp.chordId = ids.get(i);
			Utils.NODES.put(cp.chordId, cp);
			cp.fingerTable = new BigInteger[Utils.M];
			cp.successorList = new BigInteger[Utils.SUCC_SIZE];
		}
		NodeComparator nc = new NodeComparator(pid);
		Network.sort(nc);
		myCreateFingerTable();
//		printNeighs();
		return false;
	}
	
	
	
	public void initialize(Node n) {
		ChordProtocol cp = (ChordProtocol) n.getProtocol(pid);
		cp.join(n);
	}

	public ChordProtocol findNodeforId(BigInteger id) {
		for (int i = 0; i < Network.size(); i++) {
			ChordProtocol cp = Utils.getChordFromNode(Network.get(i));
			if(cp.chordId.compareTo(id) >= 0)
				return cp;
		}
		return Utils.getChordFromNode(Network.get(0));
	}
	
	
	
	public void myCreateFingerTable() {
		
		for (int i = 0; i < Network.size(); i++) {
			ChordProtocol cp = Utils.getChordFromNode(Network.get(i));
			for (int a = 0; a < Utils.SUCC_SIZE; a++) 
				cp.successorList[a] = Utils.getChordFromNode(Network.get((a + i + 1)%Network.size())).chordId;
			if (i > 0)
				cp.predecessor =  Utils.getChordFromNode(Network.get(i - 1)).chordId;
			else
				cp.predecessor =  Utils.getChordFromNode(Network.get(Network.size() - 1)).chordId;

			for (int j = 0; j < cp.fingerTable.length; j++) {
				
				long a = (long) (cp.chordId.longValue() + Math.pow(2, j)) %(long)Math.pow(2, Utils.M);
				BigInteger id = new BigInteger(a+"");
				cp.fingerTable[j] = findNodeforId(id).chordId; 
				
			}
		}
		
	}
	
	
	
	public void printNeighs(){
		for (int i = 0; i < Network.size(); i++) {
			Node node = (Node) Network.get(i);
			ChordProtocol cp = (ChordProtocol) node.getProtocol(pid);
			
			System.out.print(cp + "@" +node.getIndex() + ": ");
//			System.out.print((ChordProtocol) cp.predecessor.getProtocol(pid));
//			for(int j =0; j < cp.successorList.length; j++){
//				System.out.print((ChordProtocol) cp.successorList[j].getProtocol(pid) + "@"+cp.successorList[j].getIndex() + " ");
//			}
			for(int j =0; j < cp.fingerTable.length; j++){
				if(cp.fingerTable[j] != null)
					System.out.print(cp.fingerTable[j] + " ");
			}
			
			System.out.println();
		}
	}
	
	
	
	
	class NodeComparator implements Comparator<Node> {
	
		public int pid = 0;
	
		public NodeComparator(int pid) {
			this.pid = pid;
		}
	
		
		@Override
		public int compare(Node arg0, Node arg1) {
			BigInteger one = ((ChordProtocol) ( arg0).getProtocol(pid)).chordId;
			BigInteger two = ((ChordProtocol) ( arg1).getProtocol(pid)).chordId;
			return one.compareTo(two);
		}
	
	}

	
}
