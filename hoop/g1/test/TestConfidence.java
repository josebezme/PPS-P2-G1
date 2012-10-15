package hoop.g1.test;

import hoop.g1.Player;
import hoop.g1.Team;

public class TestConfidence {
	public static void main(String[] args) {
		Player offPlayer = new Player(1);
		Player defPlayer = new Player(2);
		// Shooting: 50	100	
		// Blocking: 25	50
		

		offPlayer.setShotsAttempted(50);
		offPlayer.setShotsMade(25);
		
		defPlayer.setBlocksAttempted(2);
		defPlayer.setBlocksMade(1);
		
		
		double result = Team.getConfidence(offPlayer, defPlayer);
		
		System.out.println("result: " + result);
	}
}
