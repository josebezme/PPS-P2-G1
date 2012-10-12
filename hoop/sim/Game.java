package hoop.sim;

import java.util.Arrays;

public class Game {

	public final String teamA;
	public final String teamB;

	public final int scoreA;
	public final int scoreB;

	private int[] playersA;
	private int[] playersB;

	public enum Action {SCORED, MISSED, STOLEN, PASSED};

	public static class Round {

		public final boolean attacksA;
		public final boolean attacksB;

		private int defenders[];
		private int holders[];
		private Action lastAction;

		Round(int[] defenders, int[] holders, boolean attacksA, Action lastAction)
		{
			this.defenders = defenders;
			this.holders = holders;
			this.lastAction = lastAction;
			this.attacksA = attacksA;
			this.attacksB = !attacksA;
		}

		public int[] defenders()
		{
			return Arrays.copyOf(defenders, defenders.length);
		}

		public int[] holders()
		{
			return Arrays.copyOf(holders, holders.length);
		}

		public Action lastAction()
		{
			return lastAction;
		}
	}

	private Round[] rounds;

	Game(String teamA, String teamB, int scoreA, int scoreB,
	     int[] playersA, int[] playersB, Round[] rounds)
	{
		this.teamA = teamA;
		this.teamB = teamB;
		this.scoreA = scoreA;
		this.scoreB = scoreB;
		this.playersA = playersA;
		this.playersB = playersB;
		this.rounds = rounds;
	}

	public int[] playersA()
	{
		return Arrays.copyOf(playersA, 5);
	}

	public int[] playersB()
	{
		return Arrays.copyOf(playersB, 5);
	}

	public int rounds()
	{
		return rounds.length;
	}

	public Round round(int x)
	{
		return rounds[x];
	}
}
