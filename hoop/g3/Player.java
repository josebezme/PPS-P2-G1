/* 
 * Player.java
 * Group 2: David Mauskop, Manushi Shah, Shane Chine
 * 
 * Class that represents a basketball player
 * 
 */

package hoop.g3;

import java.util.*;

public class Player implements Comparable<Player> {
  public int team;                  // ID for this Player's team
  public int id;                    // Player ID (Jersey Number)
  
  public int baskets;              // Total shots made by this player
  public int basketsAttempted;     // Total shots attempted
  
  public int blocks;               // Total shots blocked
  public int blocksAttempted;      // Total block opportunities
  
  public int passes;               // Total passes completed
  public int passesAttempted;      // Total passes attempted
  
  public int steals;               // Total passes stolen
  public int stealsAttempted;      // Total steal opportunities
  
  
  /* Public constructor, takes team id and player id */
  public Player (int team, int id) {
    this.team = team;
    this.id = id;
    
    baskets = 0;
    basketsAttempted = 0;
    
    blocks = 0;
    blocksAttempted = 0;
    
    passes = 0;
    passesAttempted = 0;
    
    steals = 0;
    stealsAttempted = 0;
  }
  
  /* Returns estimated shooting score */
  public double shooting() {
    return (double) (1 + baskets)/(2 + basketsAttempted);
  }
  
  /* Returns estimated blocking score */
  public double blocking() {
    double blockPct = (double) (1 + blocks)/(2 + blocksAttempted);
    return 1 - blockPct;
  }
  
  /* Returns estimated passing score */
  public double passing() {
    double passPct = (double) (9 + passes)/(10 + passesAttempted);
    double passScore = (passPct - 0.8)/0.2;
    if (passScore < 0.0)
      return 0.0;
    else
      return passScore;
  }
  
  /* Returns estimated stealing score */
  public double stealing() {
    double stealPct = (double) (1 + steals)/(10 + stealsAttempted);
    double stealScore = 5.0*stealPct;
    if (stealScore > 1.0)
      return 1.0;
    else 
      return stealScore;
  }
  
  /* Updates player counts after a made shot */
  public void shotMade() {
    baskets++;
    basketsAttempted++;
  }
  
  /* Updates player counts after a missed shot */
  public void shotMissed() {
    basketsAttempted++;
  }
  
  /* Updates player counts after a successful block */
  public void blockMade() {
    blocks++;
    blocksAttempted++;
  }
  
  /* Updates player counts after a failed block attempt */
  public void blockMissed() {
    blocksAttempted++;
  }
  
  /* Updates player counts after a successful pass */
  public void passMade() {
    passes++;
    passesAttempted++;
  }
  
  /* Updates player counts after an unsuccessful pass */
  public void passMissed() {
    passesAttempted++;
  }
  
  /* Updates player counts after a steal */
  public void stealMade() {
    steals++;
    stealsAttempted++;
  }
  
  /* Updates player counts after a failes steal attempt */
  public void stealMissed() {
    stealsAttempted++;
  }
  
  /* For ordering players */
  public int compareTo(Player that) {
    double thisScore = this.shooting() - this.blocking();
    double thatScore = that.shooting() - that.blocking();
    
    if (thisScore < thatScore)
      return -1;
    else if (thisScore > thatScore)
      return 1;
    else 
      return 0;
  }
  
  public String toString(){
	  return this.id + " " + this.shooting();
  }
}