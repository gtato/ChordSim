package peersim.chord;

import java.math.BigInteger;
import java.util.ArrayList;

import peersim.config.Configuration;
import peersim.core.Node;


public class Utils {
	public static int PID;
	public static int M;
	public static int SUCC_SIZE;
	public static ArrayList<BigInteger> NODE_IDS = new ArrayList<BigInteger>();
	private static boolean initialized = false;
	public static void initialize(int pid){
		if(!initialized){
			PID = pid;
			SUCC_SIZE = Configuration.getInt("SUCC_SIZE", 4);
			M = Configuration.getInt("M", 10);
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
	
}
