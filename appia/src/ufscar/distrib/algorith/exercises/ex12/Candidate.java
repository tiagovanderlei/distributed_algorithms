package ufscar.distrib.algorith.exercises.ex12;

public class Candidate {
	private int processID;
	private int epoch;
	public Candidate(int processID, int epoch){
		this.processID = processID;
		this.epoch = epoch;
	}
	public int getProcessID() {
		return processID;
	}
	public void setProcessID(int processID) {
		this.processID = processID;
	}
	public int getEpoch() {
		return epoch;
	}
	public void setEpoch(int epoch) {
		this.epoch = epoch;
	}
	public boolean equals(int processID, int epoch){
		return (this.processID==processID && this.epoch == epoch);
	}
}