package hoop.sim;

public interface Team {

	/* name of the team (unique per team) */
	public String name();

	/* return 5 players in 1,2,3,4,...,P where P total players */
	public int[] pickTeam(String opponent, int totalPlayers, Game[] history);

	/* get players of opponent team */
	public void opponentTeam(int[] opponentPlayers);

	/* return one of 1,2,3,4,5 to pick initial ball holder */
	public int pickAttack(int yourScore, int opponentScore,
	                      Game.Round previousRound);

	/* if 0 then shoot, otherwise pass to 1,2,3,4,5 */
	public int action(int[] defenders);

	/* pick defenders, use 1,2,3,4,5 for players */
	public int[] pickDefend(int yourScore, int opponentScore, int ballHolder,
	                        Game.Round previousRound);
}
