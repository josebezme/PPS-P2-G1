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
import java.util.Iterator;
import java.util.Map;
import java.util.List;

public class HistoryAnalyzer{
	Game[] history;
	Game game;
	LinkedList<Player> playersTeamA;
	LinkedList<Player> playersTeamB;
	Set<Player> seen = new HashSet<Player>();
	Map<String, OtherTeam>  name2TeamObj; 

	OtherTeam teamAObject;
	OtherTeam teamBObject;
	private Team ourTeam;

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
	public HistoryAnalyzer(Team team) {
		this.name2TeamObj = team.name2OtherTeam;
		this.ourTeam = team;
	}
	
	public void takeHistory(Game[] history, int totalPlayers) {
		this.history = history;
	
		//TEAM
		//if I've never seen that team, initialize'
		
		if(history == null || history.length ==  0) {
			return; //no need to take history
		} 

		this.game = history[0];
		
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
			//if self game
			
			//if not self game
			teamBObject = name2TeamObj.get(game.teamB);
		}

		
		System.out.println("teamA " + Arrays.toString(game.playersA()));
		System.out.println("teamB " + Arrays.toString(game.playersB()));
		
		teamAObject.setCurrentPlayingTeam(game.playersA());
		teamBObject.setCurrentPlayingTeam(game.playersB());

		System.out.println(Arrays.toString(teamAObject.getCurrentPlayingTeam()));
		
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

		//print stats
		printStat();


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
			int[] defenders = r.defenders();

			Player attackBH;
			Player defendBH;

			//Passing Update
			int lastHolder = holders.length - 1;

			if(r.attacksA) {
				//team A is attacking
				attackBH = teamAObject.getPlayerByPosition(holders[lastHolder]);
				defendBH = teamBObject.getPlayerByPosition(defenders[holders[lastHolder]-1]);
				
			} else {
				//team B is attacking
				attackBH = teamBObject.getPlayerByPosition(holders[lastHolder]);
				defendBH = teamAObject.getPlayerByPosition(defenders[holders[lastHolder]-1]);
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
					defendBH.blockAttempted();

					attackBH.shotMade();
					// Blocker Point of view
					break;
				case STOLEN:
					attackBH.passFailed();
					break;
			default:
			break;
						
			}
			
		}
	}

	public void printStat(){
		Iterator itr = name2TeamObj.entrySet().iterator();
		OtherTeam teamPointer;
		while(itr.hasNext()){
			Map.Entry pairs = (Map.Entry) itr.next();
			System.out.println("--------------------print Stat-----------------");
			teamPointer = (OtherTeam) pairs.getValue();
			
			List<Player> pList = teamPointer.getPlayerList();
			System.out.println("player\tSM\tSA\tBM\tBA");
			
			for (Player p : pList ) {
				System.out.print("Player:" + p.playerId);
				
				System.out.print("\t"+ p.numShotMade);
				System.out.print("\t"+ p.numShotAttempted);
				System.out.print("\t"+ p.numBlockMade);
				System.out.print("\t"+ p.numBlockAttempted);
				System.out.println();
				
			}

			System.out.println("pairs : " + pairs.getKey() + " | value: " + pairs.getValue());
			System.out.println("--------------------print Stat ENDS-----------------");
			itr.remove();
			
		}

	}

}