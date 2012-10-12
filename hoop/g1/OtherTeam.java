package hoop.g1;

import java.util.ArrayList;
import java.util.List;

public class OtherTeam{
	String teamName;
	static int totalPlayers;
	List<Player> playerList;
	int[] currentPlayingTeam;

	
	public OtherTeam(String teamName, int totalPlayers){
		this.teamName = teamName;
		this.totalPlayers=totalPlayers;
		playerList = new ArrayList<Player>(totalPlayers);
		for (int p=0; p < totalPlayers; p++ ) {
			Player player = new Player(p+1, teamName);
			playerList.add(p,player);
		}
	}


	public void setPlayerList(List<Player> playerList){
		this.playerList = playerList;
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

	public Player getPlayer(int playerPosition){
		return playerList.get(currentPlayingTeam[playerPosition -1] -1) ;
	}

	public Player getPlayerById(int id){
		return playerList.get(id - 1 );
	}

	public List<Player> getPlayerList(){
		return playerList;
	}

	public String toString(){
		return "teamname : " + teamName + " | " + " players :" + playerList;
	}

}
