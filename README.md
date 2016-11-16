# ChordSim
An implementation of Chord on top of the PeerSim simulator.

http://peersim.sourceforge.net/

In the main branch all remote calls are considered as such and are also asynchronous. Whereas in the local branch, most of the remote calls are done through direct method invocation on the target node. This is very similar to the pseudocode in the Chord paper and it's simple to read, but it's not realistic.


![alt tag](https://raw.githubusercontent.com/gtato/ChordSim/master/chord.png)
