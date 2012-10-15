package hoop.g1.test;

import java.util.Arrays;

public class TestCombination {
	
	private static final int TEAM_SIZE = 5;

	public static void main(String[] args) {
		int[] teamA = new int[] {1,2,3,4,5};
		int[] teamB = new int[] {1,2,3,4,5};
		
		// 12345
		// -51234
		// -45123
		// -...
		// 21345
		// 23415
		// 23451
		// 13245
		// 13425
		// 13452
		
		int[] initialTeam = new int[]{1,2,3,4,5};
		int[] teamCombo = new int[5];
		for(int a = 0; a < TEAM_SIZE; a++) { // All possible orderings
			for(int b = 0; b < TEAM_SIZE; b++) { // New position
				if(b == a) {continue;}
				
				for(int c = 0; c < TEAM_SIZE; c++) { // Shift
					if(c == b || c == a) {continue;}
					
					for(int d = 0; d < TEAM_SIZE; d++) {
						if(d == c || d == b || d == a) {continue;}
						
						for(int e = 0; e < TEAM_SIZE; e++) {
							if(e == d || e == c || e == b || e == a) {continue;}
							
							int[] team = new int[] {
									a + 1,
									b + 1,
									c + 1,
									d + 1,
									e + 1
							};
							System.out.println(Arrays.toString(team));
						}
					}
				}
			}
		}
	}
}
