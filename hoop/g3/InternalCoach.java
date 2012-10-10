package hoop.g3;

import hoop.sim.Game;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InternalCoach implements Coach {
  
  private int lineupSize;
  private Player[] team1;
  private Player[] team2;
  private boolean coaching1 = true;
  private int turnNumber;
  
  private int holder;
  
  public InternalCoach() {
    
  }
  
  @Override
  public Player[] pickTeam(Player[] myRoster, int teamSize, int lineupSize) {
    this.lineupSize = lineupSize;
    int startIndex;
    if (coaching1) {
      coaching1 = !coaching1;
      if (team1 == null) {
        team1 = new Player[lineupSize];
        team2 = new Player[lineupSize];
        startIndex = 0;
      }
      else
        startIndex = team2[lineupSize-1].id % teamSize;
      
      for (int i = 0; i < lineupSize; i++)
        team1[i] = myRoster[(startIndex + i) % teamSize];
      return team1;
    }
    else {
      coaching1 = !coaching1;
      startIndex = team1[lineupSize-1].id % teamSize;
      for (int i = 0; i < lineupSize; i++)
        team2[i] = myRoster[(startIndex + i) % teamSize];
      return team2;
    }
  }
  
  public void newGame() {
    turnNumber = 0;
  }
  
  @Override
  public int pickAttack(int yourScore, int opponentScore, Game.Round previousRound) {
    turnNumber++;
    holder = (turnNumber % lineupSize) + 1;
    return holder;
  }
  
  @Override
  public int action(int[] defenders) {
    if (holder == (turnNumber % lineupSize) + 1) {
      holder = ((turnNumber + 1) % lineupSize) + 1;
      return holder;
    }
    else
      return 0;
  }
  
  @Override
  public int[] pickDefend(int yourScore, int opponentScore, int ballHolder, Game.Round previousRound) {
	Integer [] myDefenders = new Integer[] {1, 2, 3, 4, 5};
	List<Integer> myTeam = Arrays.asList(myDefenders);
	Collections.shuffle(myTeam);
	//return myTeam.(type[]) collection.toArray(new type[collection.size()])
	
    return toIntArray(myTeam);
  }
  int[] toIntArray(List<Integer> list)  {
	    int[] ret = new int[list.size()];
	    int i = 0;
	    for (Integer e : list)  
	        ret[i++] = e;
	    return ret;
	}

  
  public void setOpposing(Player[] other) {
  }
}
