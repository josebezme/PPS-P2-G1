package hoop.g1;

import hoop.g1.Team.Status;

public class Move {	
	int ourPlayer;
	int toPlayer;
	Status action; 

	public Move(int ourPlayer, Status action) {
		this.ourPlayer = ourPlayer;
		this.action = action;
	}
	
	public Move(int ourPlayer, int toPlayer, Status action) {
		this.ourPlayer = ourPlayer;
		this.toPlayer = toPlayer;
		this.action = action;
	}
}
