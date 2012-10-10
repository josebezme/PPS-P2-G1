package hoop.g3;

import hoop.sim.Game;

public interface Coach {
 
	/* return 5 players in 1,2,3,4,...,P where P total players */
	public Player[] pickTeam(Player[] myRoster, int teamSize, int lineupSize); 

	/* return one of 1,2,3,4,5 to pick initial ball holder */
	public int pickAttack(int yourScore, int opponentScore, Game.Round previousRound);

	/* if 0 then shoot, otherwise pass to 1,2,3,4,5 */
	public int action(int[] defenders);

	/* pick defenders, use 1,2,3,4,5 for players */
	public int[] pickDefend(int yourScore, int opponentScore, int ballHolder, Game.Round previousRound);
 
	public void setOpposing(Player[] otherTeam);
 
	public void newGame();
 
}