package hoop.g1;

import hoop.g1.Team.Status;

public class Move {	
	int ourPlayer;
	int theirPlayer;
	Status action; 

	public Move(int ourPlayer, int theirPlayer, Status action) {
		this.ourPlayer = ourPlayer;
		this.theirPlayer = theirPlayer;
		this.action = action;
	}
}
