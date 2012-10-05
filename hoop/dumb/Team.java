package hoop.dumb;

import hoop.sim.Game;
import java.util.Arrays;
import java.util.Random;

public class Team implements hoop.sim.Team {

	public String name()
	{
		return "New York Dumbers" + (version == 1 ? "" : " v" + version);
	}

	private static int versions;
	private final int version = ++versions;
	private int[] last = null;
	private int holder = 0;
	private boolean pass = false;
	private Random gen = new Random();

	private static boolean in(int[] a, int n, int x)
	{
		for (int i = 0 ; i != n ; ++i)
			if (a[i] == x) return true;
		return false;
	}

	private static void shuffle(int[] a, Random gen)
	{
		for (int i = 0 ; i != a.length ; ++i) {
			int r = gen.nextInt(a.length - i) + i;
			int t = a[i];
			a[i] = a[r];
			a[r] = t;
		}
	}

	public void opponentTeam(int[] opponentPlayers) {}

	public int[] pickTeam(String opponent, int totalPlayers, Game[] history)
	{
		if (!opponent.equals(name())) last = null;
		int lastLen = last == null ? 0 : last.length;
		int[] result = new int [5];
		for (int i = 0 ; i != 5 ; ++i) {
			int x = gen.nextInt(totalPlayers) + 1;
			if (in(last, lastLen, x) || in(result, i, x)) i--;
			else result[i] = x;
		}
		last = result;
		return result;
	}

	public int pickAttack(int yourScore, int opponentScore, Game.Round previousRound)
	{
		holder = gen.nextInt(5) + 1;
		pass = true;
		return holder;
	}

	public int action(int[] defenders)
	{
		if (!pass) return 0;
		int newHolder = holder;
		while (newHolder == holder)
			newHolder = gen.nextInt(5) + 1;
		holder = newHolder;
		pass = false;
		return holder;
	}

	public int[] pickDefend(int yourScore, int oppScore, int holder, Game.Round previousRound)
	{
		int[] defenders = new int [] {1,2,3,4,5};
		shuffle(defenders, gen);
		return defenders;
	}
}
