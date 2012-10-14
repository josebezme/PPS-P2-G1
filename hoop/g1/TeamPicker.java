package hoop.g1;

import hoop.sim.Game.Round;

import java.util.List;

public interface TeamPicker {
	//intitalize the total # of players.
	void initialize(int players);

	//Returns the array of teams
	int[] pickTeam();

	//Returns the Move object based on the defenders & lastMove
	Move action(int[] defenders, Move lastMove);

	//Reports previous Rounds
	void reportLastRound(Round previousRound);
	
	//Returns the array of defense matchup
	int[] getDefenseMatch();

	//Returns who will be the starting player of the game
	int getBallHolder();
	
	List<Player> getPlayers();
	
	//Returns the extra Infomariton
	void printExtraInfo();
	
	//logger
	void setLogger(Logger logger);
}