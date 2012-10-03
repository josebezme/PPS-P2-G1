package hoop.g1;

import hoop.sim.Result;


import java.util.Random;

public class Team implements hoop.sim.Team {
	
	private static int versions;
	
	private static final boolean DEBUG = true;
	
	enum Status {
		DEFENDING,
		START,
		PASSING,
		SHOOTING
	}
	
	private void log(String message) {
		if(DEBUG) {
			System.err.println(name() + ": "  + message);			
		}
	}

	public String name()
	{
		return "G1-" + version;
	}

	private final int version = ++versions;
	private int[] last = null;
	private int holder = 0;
	private boolean pass = false;
	private Random gen = new Random();

	private Game game;
	
	private int currentPerspective;
	private Game[] games = new Game[2];

	private Game attackingGame;

	private static boolean in(int[] a, int n, int x)
	{
		for (int i = 0 ; i != n ; ++i)
			if (a[i] == x) return true;
		return false;
	}

	private static void shuffle(int[] a, Random gen)
	{
		for (int i = 0 ; i != a.length ; ++i) {
			int r = gen.nextInt(a.length - i) + i;
			int t = a[i];
			a[i] = a[r];
			a[r] = t;
		}
	}

	public void opponentTeam(int[] opponentPlayers) {
		log("Called opponentTeam()");
		// We're told what players were picked to play us.
		// Keep track of these players for the game Jose
	}

	public int[] pickTeam(String opponent, int totalPlayers, Result[] history) {
		log("Called pickTeam() with opponent: " + opponent + " with tP: " + totalPlayers);
		// Here we find out how many players we have for the game.
		
		
		game = new Game();
		attackingGame = game;
		if(name().equals(opponent)) {
			// We're playing a self game.
			log("Setting game to self game.");
			game.selfGame = true;
			games[currentPerspective] = game;
			currentPerspective = (currentPerspective + 1) % 2;
		}
		
		// First initialize the scores.
		game.ourScore = -1;
		game.theirScore = 0;
		
		if (!opponent.equals(name())) last = null;
		int lastLen = last == null ? 0 : last.length;
		int[] result = new int [5];
		for (int i = 0 ; i != 5 ; ++i) {
			int x = gen.nextInt(totalPlayers) + 1;
			if (in(last, lastLen, x) || in(result, i, x)) i--;
			else result[i] = x;
		}
		last = result;
		return result;
	}

	public int pickAttack(int yourScore, int opponentScore)
	{
		log("Called pickAttack()");
		log("yourScore: " + yourScore + " ourScore: " + game.ourScore);
		if(game.selfGame) {
			game = games[currentPerspective];
			attackingGame = game;
			currentPerspective = (currentPerspective + 1) % 2;
		}
		
		if(game.ourScore == -1) {
			// This is the first turn.
			game.ourScore = 0;
		} else if (game.theirScore == opponentScore) {
			// They failed to make their shot.
			// Or their pass was blocked.
		} else {
			game.theirScore = opponentScore;
			// They made their shot.
		}
		
		
		// The person who is going to pass first.
		// Simple version: who's our best passer?
		holder = gen.nextInt(5) + 1;
		pass = true;
		
		// Set status to holding until action.
		game.lastMove = new Move(holder, 0, Status.START);
		
		return holder;
	}

	/**
	 * Return
	 *  0 - For shoot
	 *  # - Of player to pass to
	 */
	public int action(int[] defenders)
	{
		
		switch(attackingGame.lastMove.action) {
			case START:  
				// then we pass
				
				// Then we should be shooting
				
				// whether or not to pass again ?
				// decide
				// Also who to pass to??
				
				// last move is holder is passing to the new holder. 
				attackingGame.lastMove = new Move(holder, defenders[holder - 1], Status.PASSING);
				
				int newHolder = holder;
				while (newHolder == holder)
					newHolder = gen.nextInt(5) + 1;
				holder = newHolder;
				
				return holder;
				
				
			case PASSING:
				// We want to logs the success of a pass
				// from player x on defending player y
				
				// then we shoot.
				
				//lastMove = shooting move.
				
				attackingGame.lastMove = new Move(holder, defenders[holder - 1], Status.SHOOTING);
				return 0;// return 0 cause we're shooting.
				
			case DEFENDING:
			case SHOOTING:
				// This should never happen.
				throw new IllegalArgumentException("Illegal status on action: " + attackingGame.lastMove.action);
		}
		
		return 0;
	}

	public int[] pickDefend(int yourScore, int oppScore, int holder)
	{
		log("Called pickDefend()");
		log("yourScore: " + yourScore + " ourScore: " + game.ourScore);
		if(game.selfGame) {
			log("Current perspective: " + currentPerspective);
			game = games[currentPerspective];
		}
		
		if(game.ourScore == -1) {
			// This is the first turn.
			game.ourScore = 0;
		} else if (game.ourScore == yourScore) {
			// so either pass is blocked
			// or shot failed.
			
			switch(game.lastMove.action) {
				case PASSING:
					// pass was blocked.
					break;
				case SHOOTING:
					// shot was blocked.
					// shot as blocked by:
					// lastMove.theirPlayer;
					// log that their player has the ability
					// to block our shooter.
					break;
				default:
					throw new IllegalArgumentException("Illegal status for defend:" + game.lastMove.action);
			}
			
		} else {
			// supposedly can only go up.
			// Maybe we made a shot?
			game.ourScore = yourScore;
		}
		
		// We're on defense so our last move is not defending.
		game.lastMove = new Move(0, 0, Status.DEFENDING);
		
		
		
		int[] defenders = new int [] {1,2,3,4,5};
		shuffle(defenders, gen);
		return defenders;
	}

}
