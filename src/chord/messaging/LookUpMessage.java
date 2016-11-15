//package peersim.chord.messaging;
//
//import java.math.*;
//import java.util.ArrayList;
//
//import peersim.core.*;
//
//public class LookUpMessage implements ChordMessage {
//
//	private Node sender;
//
//	private BigInteger targetId;
//	private ArrayList<String> path = new ArrayList<String>();
//	private int hopCounter = -1;
//
//	public LookUpMessage(Node sender, BigInteger targetId) {
//		this.sender = sender;
//		this.targetId = targetId;
//	}
//
//	public void increaseHopCounter() {
//		hopCounter++;
//	}
//
//	/**
//	 * @return the senderId
//	 */
//	public Node getSender() {
//		return sender;
//	}
//
//	/**
//	 * @return the target
//	 */
//	public BigInteger getTarget() {
//		return targetId;
//	}
//
//	/**
//	 * @return the hopCounter
//	 */
//	public int getHopCounter() {
//		return hopCounter;
//	}
//	
//	public void addToPath(BigInteger chordId){
//		path.add(chordId.toString());
//	}
//	public ArrayList<String> getPath(){
//		return path;
//	}
//
//}
