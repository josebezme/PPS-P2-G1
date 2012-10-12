package hoop.g1;
import hoop.g1.Player;
import hoop.g1.OtherTeam;
import hoop.sim.Game;
import hoop.sim.Game.Round;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.HashMap;

public class HistoryAnalyzer{
	Game[] history;
	Game game;
	LinkedList<Player> playersTeamA;
	LinkedList<Player> playersTeamB;
	Set<Player> seen = new HashSet<Player>();
	HashMap<String, OtherTeam>  name2TeamObj = new HashMap<String,OtherTeam>();

	OtherTeam teamAObject;
	OtherTeam teamBObject;

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
	public void takeHistory(Game[] history, int totalPlayers){
		this.history = history;
	
		//update

		if(history == null || history.length ==  0) {
			return; //no need to take history
		} 

		this.game = history[0];

		//TEAM
		//if I've never seen that team, initialize'

		if(!name2TeamObj.containsKey(game.teamA)){
			teamAObject = new OtherTeam(game.teamA, totalPlayers);
			name2TeamObj.put(game.teamA, teamAObject);
			System.out.println("adding new team : " + game.teamA);

		} else {
			teamAObject = name2TeamObj.get(game.teamA);
			
		}
		if(!name2TeamObj.containsKey(game.teamB)){
			teamBObject= new  OtherTeam(game.teamB,totalPlayers);
			name2TeamObj.put(game.teamB, teamBObject);
			System.out.println("adding new team : " + game.teamB);
		} else {
			teamBObject = name2TeamObj.get(game.teamB);
		}

		teamAObject.setCurrentPlayingTeam(game.playersA());
		teamBObject.setCurrentPlayingTeam(game.playersB());
		

		//initiize if not seen
		playersTeamA = new LinkedList<Player>();
		playersTeamB = new LinkedList<Player>();

		Player player;
		for (int pA : game.playersA() ) {
			player = new Player(pA, game.teamA);
			if(seen.contains(player)){
				//This means player has been seen
				//meaning i need to load him up 
				System.out.println("player # : " + pA + " : " + player + " is being loaded up");
				System.out.println("teamAObject" + teamAObject);
				
				
				player = teamAObject.getPlayerById(pA);
				// playersTeamA.add(player);
				// System.out.println("Player Added: " + player);
				
			} else {
				System.out.println("NEW Player Added A: " + player);
				playersTeamA.add(player);
				seen.add(player);	
				
			}
		}

		for (int pB : game.playersB() ) {
			player = new Player(pB, game.teamB);
			if(seen.contains(player)){
				player = teamBObject.getPlayerById(pB);
			} else {
				System.out.println("NEW Player Added B: " + player);
				seen.add(player);	
				playersTeamB.add(player);
				
			}
		}
		//print
		printHistory();
		//do the update
		update();


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
	public void update(){
	
		for (int roundIdx=0; roundIdx < game.rounds() ; roundIdx++ ) {
			Round r = game.round(roundIdx);
			int[] holders = r.holders();
			int[] defenders =r.defenders();

			if(holders.length > 1){
			//they pass it around
			//let's just assume that pass is done twice at most
			}


			//teamA update
			//teamB update

			Player attackBH;
			Player defendBH;

			//Passing Update
			int lastHolder = holders.length - 1;

			if(r.attacksA) {
				//team A is attacking
				attackBH = teamAObject.getPlayer(holders[lastHolder]);
				defendBH = teamBObject.getPlayer(defenders[holders[lastHolder]-1]);
			} else {
				//team B is attacking
				attackBH = teamBObject.getPlayer(holders[lastHolder]);
				defendBH = teamAObject.getPlayer(defenders[holders[lastHolder]-1]);
			}


			// Assume the last ballholder was a passer and penalize in the case
			attackBH.passAttempted();
			attackBH.passMade();

			//Assume that the last ballholder defender was a passer defender
			defendBH.interceptAttempted();
			defendBH.interceptMade();

			switch(r.lastAction()) {
				case MISSED: // implies that pass was successful
					// Shooter Point of view
					attackBH.passNullify();
					defendBH.interceptNullify();

					attackBH.shotAttempted();
					defendBH.blockAttempted();
					defendBH.blockMade();
					// for(int h=0; h < holders.length - 1; h++){

					// }
					break;
				case SCORED: // implies that pass was successful
				//th least ball holder was a shooter
					attackBH.passNullify();
					defendBH.interceptNullify();
					
					// Shooter Point of view
					attackBH.shotAttempted();
					attackBH.shotMade();

					// Blocker Point of view
					defendBH.blockAttempted();
					break;
				case STOLEN:
					attackBH.passFailed();
					break;
			default:
			break;
						
			}
			
		}
	}

	// public void printStat(){
		
	// 	for (name2TeamObj)

	// }

}