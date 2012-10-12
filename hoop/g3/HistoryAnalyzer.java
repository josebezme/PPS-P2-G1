package hoop.g3;
import hoop.sim.Game;
import hoop.sim.Game.Round;
import java.util.Arrays;
public class HistoryAnalyzer{
	Game[] history;

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

}