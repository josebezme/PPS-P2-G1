package hoop.g1;
import hoop.g1.Player;
import hoop.g1.OtherTeam;
import hoop.sim.Game;
import hoop.sim.Game.Round;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;

public class HistoryAnalyzer{
	Game[] history;
	Game game;
	LinkedList<Player> playersTeamA;
	LinkedList<Player> playersTeamB;
	Set<Player> seen = new HashSet<Player>();
	// HashMap<String, OtherTeam>  name2TeamObj = new HashMap<String,OtherTeam>();


	/*

		Game object contains

		teamA
		teamB
		scoreA
		scoreB
		playersA()
		playersB()
		rounds()
		roundX()

		//Round has
		attacksA
		attacksB
		defenders();
		holders();
		lastAction();
		
		Observation: the smaller the index of the Game[], the more recent the game is.

	*/
	public HistoryAnalyzer(){
	}
	public void takeHistory(Game[] history){
		this.history = history;
	
		//update

		if(history == null || history.length ==  0) {
			return; //no need to take history
		} 

		this.game = history[0];


		//initiize if not seen
		playersTeamA = new LinkedList<Player>();
		playersTeamB = new LinkedList<Player>();

		Player player;
		for (int pA : game.playersA() ) {
			player = new Player(pA, game.teamA);
			if(seen.contains(player)){
				playersTeamA.add(player);
				// System.out.println("Player Added: " + player);
				
			} else {
				System.out.println("NEW Player Added A: " + player);
				seen.add(player);	
				playersTeamA.add(player);
				
			}
		}

		for (int pB : game.playersB() ) {
			player = new Player(pB, game.teamB);
			if(seen.contains(player)){
				playersTeamB.add(player);
			} else {
				System.out.println("NEW Player Added B: " + player);
				seen.add(player);	
				playersTeamB.add(player);
				
			}
		}
		printHistory();
	}

	public void printHistory(){
		System.out.println("----------------Game History-------------------");
		
		int i=0;
		for (Game g : history) {
			System.out.print("Game: " + i + "\t");
			System.out.print(g.teamA + " vs. " + g.teamB);
			System.out.print(" | score: " + g.scoreA + " : " + g.scoreB);
			System.out.print(" | teamA: " + Arrays.toString(g.playersA()) + " vs. teamB:" + Arrays.toString(g.playersB()));
			System.out.println("| rounds: " + g.rounds());
			for(int j=0; j< g.rounds();j++){
				Round r = g.round(j);
				System.out.print("--> Round [" + j + "] ");
				if(r.attacksA)
					System.out.print("attack A [" + g.teamA + "]");
				else
					System.out.print("attack B [" + g.teamB + "]");

				
				System.out.println(" defenders: " + Arrays.toString(r.defenders())
									+ " | holders:  " + Arrays.toString(r.holders())
									+ " | lastAction: " + r.lastAction()

									);
				
			}
									
			System.out.println();
			
			i++;
		}
		System.out.println("----------------Game History Ends-------------------");
	}

	public void loadTeam(){

	}
	// public void update(){
	// 	for(Round r : game){
	// 		int[] holders = r.holders();
	// 		int[] defenders =	r.defenders();

	// 		if(holders.length > 1){
	// 		//they pass it around
	// 		//let's just assume that pass is done twice at most
	// 		}

	// 		int lastBallHolderIndex = holders.length - 1;
	// 		int position = 0;
	// 		Player lastBallHolder_from_otherTeam = otherTeamPointer.getPlayer(holders[lastBallHolderIndex]);
	// 		Player lastBallHolderDefender_from_ourTeam=ourTeamPointer.getPlayer(defenders[holders[lastBallHolderIndex]-1]);

	// 		// Assume the last ballholder was a passer and penalize in the case
	// 		lastBallHolder_from_otherTeam.passAttempted();
	// 		lastBallHolder_from_otherTeam.passMade();

	// 		//Assume that the last ballholder defender was a passer defender
	// 		lastBallHolderDefender_from_ourTeam.interceptAttempted();
	// 		lastBallHolderDefender_from_ourTeam.interceptMade();

	// 		log("currentPlayingTeam : " + Arrays.toString(currentPlayingTeam));
	// 		log("Previous Rounds Defenders:" + Arrays.toString(previousRound.defenders()));
	// 		log("current Opponent PlayerID: " + Arrays.toString(otherTeamPointer.getCurrentPlayingTeam()));
	// 		switch(previousRound.lastAction()) {
	// 		case MISSED:
	// 		// Shooter Point of view
	// 		lastBallHolder_from_otherTeam.passNullify();
	// 		lastBallHolder_from_otherTeam.shotAttempted();

	// 		// Blocker Point of view
	// 		lastBallHolderDefender_from_ourTeam.interceptNullify();
	// 		lastBallHolderDefender_from_ourTeam.blockAttempted();
	// 		lastBallHolderDefender_from_ourTeam.blockMade();

	// 		log("shot was missed by: " + lastBallHolder_from_otherTeam );
	// 		log("block was succeeded by: " + lastBallHolderDefender_from_ourTeam);

	// 		lastBallHolderIndex--;
	// 		lastBallHolderDefender_from_ourTeam=ourTeamPointer.getPlayer(defenders[holders[lastBallHolderIndex]-1]);
	// 		lastBallHolderDefender_from_ourTeam.blockAttempted();
	// 		break;
	// 		case SCORED:
	// 		//th least ball holder was a shooter
	// 		// Shooter Point of view
	// 		lastBallHolder_from_otherTeam.passNullify();
	// 		lastBallHolder_from_otherTeam.shotAttempted();
	// 		lastBallHolder_from_otherTeam.shotMade();

	// 		// Blocker Point of view
	// 		lastBallHolderDefender_from_ourTeam.interceptNullify();
	// 		lastBallHolderDefender_from_ourTeam.blockAttempted();

	// 		lastBallHolder_from_otherTeam.shootingWeight++;
	// 		teamStats.favoriteShooters.remove(lastBallHolder_from_otherTeam);
	// 		teamStats.favoriteShooters.add(lastBallHolder_from_otherTeam);

	// 		log("shot was made by: " + lastBallHolder_from_otherTeam);
	// 		log("block was failed by: " + lastBallHolderDefender_from_ourTeam);
	// 		// Continue to next case to do passers.
	// 		//Assume that only one passes is made: two ball holders

	// 		lastBallHolderIndex--;
	// 		lastBallHolderDefender_from_ourTeam=ourTeamPointer.getPlayer(defenders[holders[lastBallHolderIndex]-1]);
	// 		lastBallHolderDefender_from_ourTeam.blockAttempted();

	// 		break;
	// 		case STOLEN:
	// 		lastBallHolder_from_otherTeam.passFailed();

	// 		log("pass is stolen from " + lastBallHolder_from_otherTeam);


	// 		for(int i = 0; i < lastBallHolderIndex; i++) {
	// 			position = holders[i];
	// 			lastBallHolder_from_otherTeam = currentOpponentTeam[position - 1]; // it's their index + 1
	// 			lastBallHolder_from_otherTeam.passingWeight++;
	// 			teamStats.favoritePassers.remove(lastBallHolder_from_otherTeam);
	// 			teamStats.favoritePassers.add(lastBallHolder_from_otherTeam);
	// 		}
	// 		break;
	// 		default:
	// 		log("START OF THE GAME?");
	// 		break;
						
	// 		}
	// 	}

}