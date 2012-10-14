package hoop.g1;

import hoop.g1.Team.Status;
import hoop.sim.Game.Round;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PivotTeamPicker implements TeamPicker {
	
	enum TrainStatus{
		Shooting, Blocking
	}
	private int totalPlayers;
	private int firstPivot;
	private int secondPivot;
	
	private List<Player> players;
	
	private int[] teamA = new int[Team.TEAM_SIZE];
	private int[] teamB = new int[Team.TEAM_SIZE];
	
	private int testingIndex = -1;
	private int changeTester = 0;
	
	private int pickingTeam; // Changes every game twice.
	private int currentPlayer; //
	
	private int pickingDefense; // Changes every turn.
	
	private Logger logger = Team.DEFAULT_LOGGER;
	
	Random gen = new Random();
	
	@Override
	public void printExtraInfo(){
		logger.log("FIRST PIVOT: " + firstPivot);
		logger.log("SECOND PIVOT: " + secondPivot);
	}
	
	@Override
	public List<Player> getPlayers() {
		return players;
	}
	
	@Override
	public void initialize(int totalPlayers) {
		this.totalPlayers = totalPlayers;

		
		firstPivot = gen.nextInt(totalPlayers) + 1;
		secondPivot = firstPivot;
		
		while(secondPivot == firstPivot) {
			secondPivot = gen.nextInt(totalPlayers) + 1;
		}
		
		logger.log("First pivot is: " + firstPivot);
		logger.log("Second pivot is: " + secondPivot);
		currentPlayer = 1;
		while(currentPlayer == firstPivot || currentPlayer == secondPivot) {
			currentPlayer++;
		}
		
		players = new ArrayList<Player>(totalPlayers);
		for(int i = 0; i < totalPlayers; i++) {
			players.add(new Player(i + 1));
		}
	}

	@Override
	public int[] pickTeam() {

		int curPos = currentPlayer;
		
		int[] team = null;
		if(pickingTeam++ % 2 == 0) { //this 
			team = teamA;
			team[0] = firstPivot;
			logger.log("Team A...");
		} else {
			team = teamB;
			team[0] = secondPivot;
			logger.log("Team B...");
		}
		
		for(int i = 1; i < Team.TEAM_SIZE;) {
			if(curPos != firstPivot && curPos != secondPivot) {
				team[i] = curPos;
				i++;
			}
			
			curPos = ++curPos % totalPlayers;
			if(curPos == 0) {
				curPos = totalPlayers;
			}
		}
		
		logger.log("Team: " + Arrays.toString(team));
		
		currentPlayer = curPos;
		
		return team;
	}

	@Override
	public int getBallHolder() {
		
		if(changeTester == 0) {
			testingIndex = ++testingIndex % Team.TEAM_SIZE; //shooter variable is an index !!!!
		}
		
		if(testingIndex == 0) {
			changeTester = ++changeTester % 2;
		} else {
			changeTester = ++changeTester % 4;
		}
		
		int[] players = null;
		if(pickingDefense == 0) {
			players = teamA;
		} else {
			players = teamB;
		}
		pickingDefense = ++pickingDefense % 2;

		int ballHolder = ((testingIndex + 1) % Team.TEAM_SIZE) + 1;
		
		if(ballHolder == 1) {
			ballHolder++;
		}
		
		logger.log(whatTeam("attack") + ": Picker: ballHolder: [playerID]: "+ players[ballHolder-1] + " | [sim#]: " + ballHolder);
		return ballHolder; //we return the position of ball holder here [simulation #]
	}
	
	@Override
	public Move action(int[] defenders, Move lastMove) {
		Move move = null;
		
		int[] positions = null;
		if(pickingDefense == 0) {
			positions = teamB;
		} else {
			positions = teamA;
		}
			
		switch(lastMove.action) {
			case START:
				// do the pass.
				int nextHolder = 0;
				if(changeTester < 2) {
					nextHolder = testingIndex + 1;
				} else {
					nextHolder = 1;
				}
				
				logger.log(whatTeam("attack") + ": Passing to --> playerID " + positions[testingIndex] + " ( [Sim#]: " + nextHolder + ") ");
				move = new Move(lastMove.ourPlayer, nextHolder, Status.PASSING);
				//Log the pass -Jiang
				players.get(positions[lastMove.ourPlayer-1]-1).passAttempted();
				break;
			case PASSING:
				// Shoot
				logger.log("Shooting..." + " from " + whatTeam("attack"));
				move = new Move(lastMove.ourPlayer, 0, Status.SHOOTING);
				//Log the pass -Jiang
				players.get(positions[lastMove.ourPlayer - 1] - 1).passMade();

				break;
			default:
				throw new IllegalArgumentException("Invalid status: " + lastMove.action);
			
		}
		
		return move;
	}

	@Override
	public void reportLastRound(Round previousRound) {
		// Because the first turn doesn't have previous round.
		// previousRound.attacksA = A is attacking
		if(previousRound != null) {
			int[] offTeam = null;
			int[] defTeam = null;
			
			logger.log("AttakcsA: " + previousRound.attacksA);
			if(!previousRound.attacksA) {
				
				// B is attacking A.
				offTeam = teamB;
				defTeam = teamA;
			} else {
				// A is attacking B.
				offTeam = teamA;
				defTeam = teamB;
			}
			
			// crunch da numbers.
			
			// 1 passes to 2
			// and 2 shoots
			// ballholder[] = {1, 2};
			// last action?
			
			int holders[] = previousRound.holders();
			logger.log("holders array: " + Arrays.toString(holders));
			
			int shooter = holders[holders.length - 1];
			int playerId = offTeam[shooter -1];
			int shooterIndex = playerId - 1;
			int blockerPosition = testingIndex;
			int blockerId = defTeam[blockerPosition];
			int blockerIndex = blockerId - 1;

			
			if(changeTester < 2) {
				switch(previousRound.lastAction()) {
					case SCORED:
						players.get(shooterIndex).shotMade();
					case MISSED:
						players.get(shooterIndex).shotAttempted();
						break;
					default:
						// dont care.
				}
			}
			
			if(changeTester >= 2 || shooter == 1){
				switch(previousRound.lastAction()) {
					case MISSED:
						players.get(blockerIndex).blockMade();
					case SCORED:
						players.get(blockerIndex).blockAttempted();
						break;
					default:
					// dont care.
				}
			}
			
		}
	}

	@Override
	public int[] getDefenseMatch() {
		
		int[] match = new int[Team.TEAM_SIZE];
		
		if(changeTester < 2) {
			for(int i = 0; i < Team.TEAM_SIZE; i++) {
				int offPos = ((i + testingIndex) % Team.TEAM_SIZE);
				match[offPos] = i + 1;
			}
		} else {
			for(int i = 0; i < Team.TEAM_SIZE; i++) {
				int offPos = ((i + testingIndex) % Team.TEAM_SIZE);
				match[i] = offPos + 1;
			}
		}

		logger.log("Picker: DefMatch: " + Arrays.toString(match) + " of " + whatTeam("defend"));
		return match;
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public String whatTeam(String attackOrDefend){
		//depending on the state of the game, returns the attacking team 

			if(attackOrDefend.equals("attack"))
				return ( changeTester == 0 )? "Team A" : "Team B" ;
			else
				return ( changeTester == 0 )? "Team B" : "Team A" ;

	}

}