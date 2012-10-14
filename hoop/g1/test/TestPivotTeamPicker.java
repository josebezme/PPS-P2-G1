package hoop.g1.test;

import java.util.Arrays;

import hoop.g1.Move;
import hoop.g1.Team;
import hoop.g1.Team.PivotTeamPicker;
import hoop.g1.Team.TeamPicker;

public class TestPivotTeamPicker {

	public static void main(String[] args) {
		TeamPicker picker = new PivotTeamPicker();
		
		picker.initialize(12);
		
		for(int i = 0; i < 1; i++) {// Games
			
			
			int[] teamA = picker.pickTeam();
			System.out.println("Team A: " + Arrays.toString(teamA));
			
			int[] teamB = picker.pickTeam();
			System.out.println("Team B: " + Arrays.toString(teamB));
			
			
			for(int j = 0; j < 10; j++) { // Turns
				picker.getBallHolder();
				picker.getDefenseMatch();
				
				Move lastMove = picker.action(new int[]{1,2,3,4,5}, new Move(1, 0, Team.Status.START));
				picker.action(new int[]{1,2,3,4,5}, lastMove);
			}
		}
	}
}
