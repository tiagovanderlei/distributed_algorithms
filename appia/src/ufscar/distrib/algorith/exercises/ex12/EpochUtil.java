package ufscar.distrib.algorith.exercises.ex12;

import irdp.protocols.tutorialDA.utils.SampleProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class EpochUtil {
	final String _dir = "/home/tiago/workspace/appia/src/ufscar/distrib/algorith/exercises/ex12/epochs/";
	public int createEpoch(int processId, int value){
		if(!existsEpoch(processId)){
			try {
		          File file = new File(_dir+String.valueOf(processId));
		          BufferedWriter output = new BufferedWriter(new FileWriter(file));
		          output.write(String.valueOf(value));
		          output.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return value;
		}else{
			return -1;
		}
	}
	public int readEpoch(int processId){
		if(existsEpoch(processId)){
			try {
				BufferedReader reader = new BufferedReader(new FileReader(_dir+String.valueOf(processId)));
				return Integer.parseInt(reader.readLine().trim());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			}
		}
		return -1;
	}
	public int increaseEpoch(int processId){
		int n_epoch=0;
		try {
			if(!existsEpoch(processId)){
				//Caso não possua epoch, cria arquivo com epoch 0
				createEpoch(processId, n_epoch);
			}else{
				n_epoch = readEpoch(processId)+1;
				updateEpoch(processId, n_epoch);
				
			}
			return n_epoch;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	public boolean existsEpoch(int processId){
		File f = new File(_dir+String.valueOf(processId));
		return f.exists();
	}
	public void updateEpoch(int processId, int value){
		FileWriter fstream;
		try {
			fstream = new FileWriter(_dir+String.valueOf(processId));
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(String.valueOf(value));
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int maxrank(SampleProcess[] processos){
		int maxrank=0;
		for(int i=0;i<processos.length;i++){
			if (processos[i].getProcessNumber() > maxrank)
				maxrank = processos[i].getProcessNumber();
		}
		return maxrank;
	}
}
