package hoop.g3;

import java.util.Arrays;
import java.util.ArrayList;
import hoop.sim.Game;

public class ExternalCoach implements Coach {
  
  private int lineupSize;
  private Player[] myTeam;
  private Player[] otherTeam;
  private int turnNumber;
  
  private Player initHolder;
  private Player currentHolder;
  
  private int turn;
  private boolean hasPassed;
  
  public ExternalCoach() {
    myTeam = new Player[5];
    otherTeam = new Player[5];
    turn = 0;
    hasPassed = false;
  }
  
  @Override
  public Player[] pickTeam(Player[] myRoster, int teamSize, int lineupSize) {
    this.lineupSize = lineupSize;
    Player[] sorted = Arrays.copyOf(myRoster, teamSize);
    Arrays.sort(sorted);
    
    //This is sorted from worst to best
    for (int i = 0; i < lineupSize; i++)
      myTeam[i] = myRoster[sorted[teamSize -i -1].id-1];
    
    int bestShooter  = getSharpShooter(myRoster);
    int bestDefender = getLockdownDefender(myRoster);

    boolean hasBestShooter = false;
    boolean hasBestDefender = false;
    for (int i = 0; i < lineupSize; i++){
    	if(myTeam[i].id == bestShooter+1)
        	hasBestShooter = true;
    	if(myTeam[i].id == bestDefender+1)
    		hasBestDefender = true;
    }
        
    
    //fourth worst player is kicked off for the best shooter
    if(!hasBestShooter){    
    	myTeam[3] = myRoster[bestShooter];
    }
    if(bestDefender != bestShooter && !hasBestDefender 
    		//don't replace best defender for best shooter
    		&& myTeam[4].id != bestShooter+1){
    	myTeam[4] = myRoster[bestDefender];
    }
    	    
    
    
    return myTeam;
  }
  
  public void setOpposing(Player[] otherTeam) {
    this.otherTeam = otherTeam;
  }
  
  public void newGame() {
  }
  
  @Override
  public int pickAttack(int yourScore, int opponentScore, Game.Round previousRound) {
//	if(!hasPassed){
	//	hasPassed = true;
		return getBestPasser();
	//}
//	else{		
		//If we get in here my understanding of the sim is flawed.
	//	System.out.println("This is a test.");
		//return 1;
	//}
  }
  
  //[TODO] move these methods to a team class
  //get our teams best shoter
  public int getSharpShooter(Player[] myRoster){
	  double maxPassing = Double.NEGATIVE_INFINITY;
	    int bestPasser = -1;
	    for (int i = 0; i < myRoster.length; i++) {
	      if (myRoster[i].shooting() > maxPassing) {
	        maxPassing = myRoster[i].shooting();
	        bestPasser = i;
	      }
	    }
	    return (bestPasser);
  }
  
  //[TODO] move these methods to a team class
  //get our teams best shoter
  public int getLockdownDefender(Player[] myRoster){
	  double maxPassing = Double.POSITIVE_INFINITY;
	    int bestPasser = -1;
	    for (int i = 0; i < myRoster.length; i++) {
	      if (myRoster[i].blocking() < maxPassing) {
	        maxPassing = myRoster[i].blocking();
	        bestPasser = i;
	      }
	    }
	    return (bestPasser);
  }
  
  
  //[TODO] move these methods to a team class
  public int getBestPasser(){
	  double maxPassing = Double.NEGATIVE_INFINITY;
	    int bestPasser = -1;
	    for (int i = 0; i < 5; i++) {
	      if (myTeam[i].passing() > maxPassing) {
	        maxPassing = myTeam[i].passing();
	        bestPasser = i;
	      }
	    }
	    initHolder = myTeam[bestPasser];
	    currentHolder = initHolder;
	    return (bestPasser + 1);
  }
  
  //[TODO] move these methods to a team class
  public int getBestShotBlocker(){
		double maxDefending = Double.POSITIVE_INFINITY;
		int bestDefender = -1;
		for (int i = 0; i < 5; i++) {
		if (myTeam[i].blocking() < maxDefending) {
			maxDefending = myTeam[i].blocking();
			bestDefender = i;
		}
		}
		return (bestDefender + 1);
}
  
  //[TODO] move these methods to a team class & refactor to get best
  //of a stat
  public int getBestStealer(){
		double maxStealing = Double.POSITIVE_INFINITY;
		int bestDefender = -1;
		for (int i = 0; i < 5; i++) {
		if (myTeam[i].stealing() < maxStealing) {
			maxStealing = myTeam[i].stealing();
			bestDefender = i;
		}
		}
		return (bestDefender + 1);
}
  
  @Override
  public int action(int[] defenders) {
    // Later update to check for pass back
    if (currentHolder != initHolder)
      return 0;
    
    Player[] defense = new Player[5];
    for (int i = 0; i < 5; i++) 
      defense[i] = otherTeam[defenders[i]-1];
    
    double biggestMismatch = Double.NEGATIVE_INFINITY;
    int shooter = -1;
    for (int i = 0; i < 5; i++) {
      double mismatch = myTeam[i].shooting() + defense[i].blocking();
      if ((mismatch > biggestMismatch) && (myTeam[i] != currentHolder)){
        biggestMismatch = mismatch;
        shooter = i;
      }
    }
    currentHolder = myTeam[shooter];
    return (shooter + 1);
  }
  
  @Override
  public int[] pickDefend(int yourScore, int opponentScore, int ballHolder, Game.Round previousRound) {
	hasPassed = false;
	boolean []used = new boolean[5];
	
	int myBestBlocker = getBestShotBlocker();
	int myBestStealer = getBestStealer();
	if (myBestBlocker == myBestStealer){
		myBestStealer=myBestBlocker +1;
		if (6== myBestStealer){
			myBestStealer = 1;
		}
	}
	used[myBestBlocker-1] = true;
	used[myBestStealer-1] = true;
	
	int[] defenderArray = new int [5];
	int myTeamMember = 0; 
	int otherTeamsBestShooter = getBestShooter();
	if (otherTeamsBestShooter == ballHolder){
		otherTeamsBestShooter=ballHolder +1;
		if (6== otherTeamsBestShooter){
			otherTeamsBestShooter = 1;
		}
	}
	for (int i = 0; i < otherTeam.length; i++) {

		if(i == ballHolder-1){
			myTeamMember = myBestStealer;
		}
		else if(i == otherTeamsBestShooter-1){
			myTeamMember = myBestBlocker;
		}
		else{
			myTeamMember = 1;
			while(used[myTeamMember-1]){
				myTeamMember++;
			}			
		}
		defenderArray[i]= myTeamMember;
		used[myTeamMember-1]=true;				
	}	
	return defenderArray;
    //return new int[] {1, 2, 3, 4, 5};
  }
  /* Get's the opposing teams best shooters id */
  public int getBestShooter(){
		double maxShooting = Double.NEGATIVE_INFINITY;
		int bestShooter = -1;
		for (int i = 0; i < 5; i++) {
			if (otherTeam[i].shooting() > maxShooting) {
				maxShooting = otherTeam[i].shooting();
				bestShooter = i;
			}
		}
		return bestShooter+1;
  }
}
