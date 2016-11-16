package chord;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;


public class Utils {
	public static int PID;
	public static int TID;
	public static int M;
	public static int SUCC_SIZE;
	public static HashMap<BigInteger, ChordProtocol> NODES = new HashMap<BigInteger, ChordProtocol>();
	public static ArrayList<ChordMessage> receivedMessages;
	private static boolean initialized = false;
	
	public static int FAILS=0, SUCCESS=0; 
	
	public static void initialize(int pid, int tid){
		if(!initialized){
			PID = pid;
			TID = tid;
			SUCC_SIZE = Configuration.getInt("SUCC_SIZE", 4);
			M = Configuration.getInt("M", 10);
			receivedMessages = new ArrayList<ChordMessage>();
		}
	}
	
	public static ChordProtocol getChordFromNode(Node n){
		return (ChordProtocol) n.getProtocol(PID);
	}
	
	
	public static int distance(BigInteger a, BigInteger b){
		int ia = a.intValue(); 
		int ib = b.intValue();
		if(ib >= ia) return ib-ia;
		return ib+(int)Math.pow(2, M)-ia;
		
	}
	
	public static ArrayList<BigInteger> generateIDs(int nr){
		HashSet<BigInteger> ids = new HashSet<BigInteger>();
		
		while(ids.size() != nr)		
			ids.add(new BigInteger(Utils.M, CommonState.r));
		
		return new ArrayList<BigInteger>(ids);
	}
	
	public static BigInteger generateNewID(){
		BigInteger newId;
		do
			newId= new BigInteger(Utils.M, CommonState.r);
		while(Utils.NODES.containsKey(newId));
		
		return newId;
	}
	
}
