package hoop.g1;

interface StrategyAdvisor {

	public int[] pickTeam(String opponent);
	public int[] getInitialPasser(String opponent);
	public int[] pickDefenders(int[] attackers, int ballHolder);



}
