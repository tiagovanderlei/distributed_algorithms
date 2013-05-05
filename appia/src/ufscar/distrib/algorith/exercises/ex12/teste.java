package ufscar.distrib.algorith.exercises.ex12;

public class teste {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EpochUtil ep = new EpochUtil();
		ep.createEpoch(1, 0);
		System.out.println("A epoca 1 foi encontrada? "+ep.existsEpoch(1));
		System.out.println("Conteudo da epoca 1: "+ep.readEpoch(1));
		System.out.println("Incrementando ...");
		ep.increaseEpoch(1);
		System.out.println("Conteudo da epoca 1: "+ep.readEpoch(1));
	}

}
