package ufscar.distrib.algorith.exercises.ex12;

import net.sf.appia.core.Event;

public class StartTimerEvent extends Event {
	private int novoDelay;
	/**
	 * Retorna o novo delay (em segundos)
	 **/
	public int getNovoDelay() {
		return novoDelay;
	}
	/**
	 * Define o novo delay
	 * @param novoDelay novo delay (em segundos)
	 **/
	public void setNovoDelay(int novoDelay) {
		this.novoDelay = novoDelay*1000; //converte para milissegundos
	}
}