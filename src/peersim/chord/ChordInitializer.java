package peersim.chord;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;

public class ChordInitializer implements NodeInitializer, Control {

	private static final String PAR_PROT = "protocol";
	

	int pid = 0;
	

	public ChordInitializer(String prefix) {
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
		Utils.initialize(pid);
	}

	
	public boolean execute() {
		ArrayList<BigInteger> ids = generateIDs(Network.size());
		
		for (int i = 0; i < Network.size(); i++) {
			Node node = (Node) Network.get(i);
			ChordProtocol cp = Utils.getChordFromNode(node);
			cp.node = node;
			cp.chordId = ids.get(i);
			Utils.NODE_IDS.add(cp.chordId);
			cp.fingerTable = new ChordProtocol[Utils.M];
			cp.successorList = new ChordProtocol[Utils.SUCC_SIZE];
		}
		NodeComparator nc = new NodeComparator(pid);
		Network.sort(nc);
		myCreateFingerTable();
		printNeighs();
		return false;
	}
	
	
	
	public void initialize(Node n) {
		join(n);
	}

	public void join(Node myNode) {
		ChordProtocol cp = (ChordProtocol) myNode.getProtocol(pid);
		cp.node = myNode;
		// search a node to join
		Node n;
		do {
			n = Network.get(CommonState.r.nextInt(Network.size()));
		} while (n == null || !n.isUp());
		
		cp.chordId = generateNewID();
		Utils.NODE_IDS.add(cp.chordId);
		ChordProtocol cpRemote = Utils.getChordFromNode(n);

		ChordProtocol successor = cpRemote.findSuccessor(cp.chordId);
		cp.successorList = new ChordProtocol[Utils.SUCC_SIZE];
		cp.successorList[0] = successor;
		cp.predecessor = successor.predecessor;
		cp.fingerTable = new ChordProtocol[Utils.M];
		cp.updateSuccessors();
		for (int i = 0; i < cp.fingerTable.length; i++) {
			long a = (long) (cp.chordId.longValue() + Math.pow(2, i)) %(long)Math.pow(2, Utils.M);
			BigInteger id = new BigInteger(a+"");
			cp.fingerTable[i] = cpRemote.findSuccessor(id); 
		}
		System.out.println("Node " + cp.chordId + " is in da house");
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
				cp.successorList[a] = Utils.getChordFromNode(Network.get((a + i + 1)%Network.size()));
			if (i > 0)
				cp.predecessor =  Utils.getChordFromNode(Network.get(i - 1));
			else
				cp.predecessor =  Utils.getChordFromNode(Network.get(Network.size() - 1));

			for (int j = 0; j < cp.fingerTable.length; j++) {
				
				long a = (long) (cp.chordId.longValue() + Math.pow(2, j)) %(long)Math.pow(2, Utils.M);
				BigInteger id = new BigInteger(a+"");
				cp.fingerTable[j] = findNodeforId(id); 
				
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
	
	private ArrayList<BigInteger> generateIDs(int nr){
		HashSet<BigInteger> ids = new HashSet<BigInteger>();
		
		while(ids.size() != nr)		
			ids.add(new BigInteger(Utils.M, CommonState.r));
		
		return new ArrayList<BigInteger>(ids);
	}
	
	private BigInteger generateNewID(){
		BigInteger newId;
		do
			newId= new BigInteger(Utils.M, CommonState.r);
		while(Utils.NODE_IDS.contains(newId));
		
		return newId;
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
