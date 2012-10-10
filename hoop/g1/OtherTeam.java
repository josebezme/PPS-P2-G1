package hoop.g1;

import java.util.ArrayList;
import java.util.List;

public class OtherTeam{
	String teamName;
	List<Player> playerList;
	int[] currentPlayingTeam;
	
	public OtherTeam(String teamName, int totalPlayers){
		this.teamName = teamName;
		playerList = new ArrayList(totalPlayers);
	}

	public void setCurrentPlayingTeam(int[] team){
		this.currentPlayingTeam = team;
	}

	public int[] getCurrentPlayingTeam(){
		return currentPlayingTeam;
	}
	public String getName(){
		return teamName;
	}
}
