package hoop.sim;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

class Pairs implements Iterable <Iterable <Pairs.Pair>> {

	private static Random gen = new Random();
	private int[] index;
	private boolean[] idle;
	private boolean[][] done;
	private Vector <int[]> base;

	public static class Pair {

		public final int t1, t2;

		public Pair(int t1, int t2)
		{
			this.t1 = t1;
			this.t2 = t2;
		}
	}

	public Pairs(int n)
	{
		index = new int [n];
		for (int i = 0 ; i != n ; ++i)
			index[i] = i;
	}

	public Iterator <Iterable <Pair>> iterator()
	{
		shuffle();
		compute();
		Vector <Iterable <Pair>> table = new Vector <Iterable <Pair>> ();
		int s = index.length % 2;
		for (int[] p : base) {
			Vector <Pair> v = new Vector <Pair> ();
			for (int i = s ; i != p.length ; i += 2)
				v.add(new Pairs.Pair(p[i], p[i + 1]));
			table.add(v);
		}
		return table.iterator();
	}

	private void swap(int i, int j)
	{
		int x = index[i];
		index[i] = index[j];
		index[j] = x;
	}

	private void shuffle()
	{
		for (int i = 0 ; i != index.length ; ++i)
			swap(i, gen.nextInt(index.length - i) + i);
	}

	private void compute()
	{
		base = new Vector <int[]> ();
		if (index.length % 2 == 1)
			idle = new boolean [index.length];
		done = new boolean [index.length][index.length];
		if (index.length % 2 == 0)
			compute_backtrack(0);
		else for (int i = 0 ; i != index.length ; ++i) {
			swap(0, i);
			compute_backtrack(1);
			swap(0, i);
		}
	}

	private boolean compute_backtrack(int pos)
	{
		if (pos == index.length) {
			base.add(Arrays.copyOf(index, index.length));
			return true;
		}
		for (int i = pos ; i != index.length ; ++i)
			for (int j = i + 1 ; j != index.length ; ++j)
				if (!done[index[i]][index[j]]) {
					swap(pos, i);
					swap(pos + 1, j);
					boolean ok = compute_backtrack(pos + 2);
					swap(pos, i);
					swap(pos + 1, j);
					done[index[i]][index[j]] = ok;
					if (ok && pos != 0)
						return true;
				}
		return false;
	}
}
