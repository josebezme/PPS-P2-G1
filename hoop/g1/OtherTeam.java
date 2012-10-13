package hoop.g1;

import java.util.ArrayList;
import java.util.List;

public class OtherTeam{
	String teamName;
	List<Player> playerList;

	
	public OtherTeam(String teamName, int totalPlayers){
		this.teamName = teamName;
		playerList = new ArrayList<Player>(totalPlayers);
		for (int p=0; p < totalPlayers; p++ ) {
			Player player = new Player(p+1, teamName);
			playerList.add(p,player);
		}
	}


	public void setPlayerList(List<Player> playerList){
		this.playerList = playerList;
	}

	public String getName(){
		return teamName;
	}

	public Player getPlayer(int playerId) {
		return playerList.get(playerId - 1) ;
	}

	public Player getPlayerById(int id){
		return playerList.get(id - 1 );
	}

	public List<Player> getPlayerList(){
		return playerList;
	}

	public String toString(){
		return "<" + teamName + ">" + " players :" + playerList;
		// return "<" + teamName + ">";
	}

}
