package hoop.sim;

import java.io.*;
import java.util.*;
import javax.tools.*;

public class Hoop {

	private static int gameTurns;
	private static int selfGames;

	public static int gameTurns() { return gameTurns; }
	
	public static int selfGames() { return selfGames; }

	// list files below a certain directory
	// can filter those having a specific extension constraint
	private static List <File> directoryFiles(String path, String extension) {
		List <File> allFiles = new ArrayList <File> ();
		allFiles.add(new File(path));
		int index = 0;
		while (index != allFiles.size()) {
			File currentFile = allFiles.get(index);
			if (currentFile.isDirectory()) {
				allFiles.remove(index);
				for (File newFile : currentFile.listFiles())
					allFiles.add(newFile);
			} else if (!currentFile.getPath().endsWith(extension))
				allFiles.remove(index);
			else index++;
		}
		return allFiles;
	}

	// compile and load players dynamically
	private static Team[] loadTeams(String txtPath) {
		// list of players
		List <Team> playersList = new LinkedList <Team> ();
		try {
			// get file of players
			BufferedReader in = new BufferedReader(new FileReader(new File(txtPath)));
			// get tools
			ClassLoader loader = ClassLoader.getSystemClassLoader();
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
			// load players
			String group;
			while ((group = in.readLine()) != null) {
				System.err.println("Group: " + group);
				// delete all class files
				List <File> classFiles = directoryFiles("hoop/" + group, ".class");
				System.err.print("Deleting " + classFiles.size() + " class files...   ");
				for (File file : classFiles)
					file.delete();
				System.err.println("OK");
				// compile all files
				List <File> javaFiles = directoryFiles("hoop/" + group, ".java");
				System.err.print("Compiling " + javaFiles.size() + " source files...   ");
				Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(javaFiles);
				boolean ok = compiler.getTask(null, fileManager, null, null, null, units).call();
				if (!ok) throw new Exception("compile error");
				System.err.println("OK");
				// load class
				System.err.print("Loading player class...   ");
				Class playerClass = loader.loadClass("hoop." + group + ".Team");
				System.err.println("OK");
				// set name of player and append on list
				Team player = (Team) playerClass.newInstance();
				playersList.add(player);
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
		return playersList.toArray(new Team[0]);
	}

	public static void main(String[] args) throws Exception
	{
		boolean display = true;
		// path with players
		String playerPath = "hoop/players.list";
		if (args.length > 0)
			playerPath = args[0];
		// turns of each game
		gameTurns = 10;
		if (args.length > 1)
			gameTurns = Integer.parseInt(args[1]);
		if (gameTurns < 1) {
			System.err.println("Invalid game turns");
			System.exit(1);
		}
		// games with yourself
		selfGames = 10;
		if (args.length > 2)
			selfGames = Integer.parseInt(args[2]);
		// games with each opponent
		int seasons = 2;
		if (args.length > 3)
			seasons = Integer.parseInt(args[3]);
		// initial number of players
		int playerPool = 12;
		if (args.length > 4)
			playerPool = Integer.parseInt(args[4]);
		// load players
		Team[] teams = loadTeams(playerPath);
		if (teams == null)
			throw new Exception("Invalid player in list");
		// check names
		if (!uniqueNames(teams))
			throw new Exception("Player names are not unique");
		// generate team stats
		Random gen = new Random();
		double[][][] stats = stats(gen, teams.length, playerPool);
		// history of self games
		Game[][] selfResults = new Game [teams.length][selfGames];
		// play training games
		if (display)
			System.err.println("\n### Training games ###");
		Game[] g0 = new Game[0];
		for (int team = 0 ; team != teams.length ; ++team) {
			Vector <Game> oneResults = new Vector <Game> ();
			for (int game = 0 ; game < selfGames ; ++game) {
				Game result = play(teams[team], teams[team],
				                   stats[team], stats[team],
			                       gameTurns, gen,
				                   oneResults.toArray(g0),
				                   g0, g0, display);
				selfResults[team][game] = result;
				oneResults.add(result);
			}
		}
		// display training games result
		System.err.println("\n### Training results ###");
		for (int team = 0 ; team != teams.length ; ++team) {
			System.err.println("  Team: " + teams[team].name());
			for (int game = 0 ; game != selfGames ; ++game) {
				Game result = selfResults[team][game];
				System.err.println("    " + result.scoreA + "-" + result.scoreB);
			}
		}
		// no tournament if only one player
		if (teams.length == 1)
			System.exit(0);
		// max name for aligned team name prints
		int maxName = 0;
		for (Team team : teams)
			if (team.name().length() > maxName)
				maxName = team.name().length();
		// compute and print tournament schedule
		Vector <Iterable <Iterable <Pairs.Pair>>> schedule =
		    new Vector <Iterable <Iterable <Pairs.Pair>>> ();
		for (int season = 0 ; season != seasons ; ++season)
			schedule.add(new Pairs(teams.length));
		System.err.println("\n### Tournament schedule ###");
		int seasonNum = 1;
		for (Iterable <Iterable <Pairs.Pair>> season : schedule) {
			System.err.println("  Season " + seasonNum++);
			int roundNum = 1;
			for (Iterable <Pairs.Pair> round : season) {
				System.err.println("    Round " + roundNum++);
				for (Pairs.Pair match : round) {
					String n1 = teams[match.t1].name();
					String n2 = teams[match.t2].name();
					System.err.println("     " + align(n1, maxName, false) +
					                    " - " + align(n2, maxName, false));
				}
			}
		}
		// tournament history and points
		Vector <Game> history = new Vector <Game> ();
		int[] points = new int [teams.length];
		Arrays.fill(points, 0);
		// play tournament games
		if (display)
			System.err.println("\n### Tournament games ###");
		int rounds = teams.length - 1 + (teams.length % 2);
		int per_round = teams.length / 2;
		int score[][][][] = new int [seasons][rounds][per_round][2];
		int i = 0;
		for (Iterable <Iterable <Pairs.Pair>> season : schedule) {
			int j = 0;
			for (Iterable <Pairs.Pair> round : season) {
				Game[] currentHistory = history.toArray(g0);
				int k = 0;
				for (Pairs.Pair match : round) {
					Game result = play(teams[match.t1], teams[match.t2],
					                   stats[match.t1], stats[match.t2],
						               gameTurns, gen, currentHistory,
					                   selfResults[match.t1],
					                   selfResults[match.t2], display);
					if (result.scoreA == result.scoreB)
						throw new RuntimeException("Same score!");
					score[i][j][k][0] = result.scoreA;
					score[i][j][k][1] = result.scoreB;
					points[result.scoreA > result.scoreB ? match.t1 : match.t2]++;
					history.add(result);
					k++;
				}
				j++;
			}
			i++;
		}
		// print tournament results
		System.err.println("\n### Tournament results ###");
		seasonNum = 1;
		for (Iterable <Iterable <Pairs.Pair>> season : schedule) {
			System.err.println("  Season " + seasonNum++);
			int roundNum = 1;
			for (Iterable <Pairs.Pair> round : season) {
				System.err.println("    Round " + roundNum++);
				int game = 0;
				for (Pairs.Pair match : round) {
					String n1 = teams[match.t1].name();
					String n2 = teams[match.t2].name();
					int s1 = score[seasonNum - 2][roundNum - 2][game][0];
					int s2 = score[seasonNum - 2][roundNum - 2][game][1];
					System.err.println("      " + align(n1, maxName, false) +
					                   " - " + align(n2, maxName, false) +
					                   ":  " + s1 + "-" + s2);
					game++;
				}
			}
		}
		// print final tournament points
		sortByScore(teams, points);
		System.err.println("\n### Tournament points ###");
		for (i = 0 ; i != teams.length ; ++i)
			System.err.println(align("" + points[i], 6, true) + "   " + teams[i].name());
		System.exit(0);
	}

	// play one game between two teams (could be same)
	private static Game play(Team teamA, Team teamB,
	                         double[][] statsA, double[][] statsB,
	                         int turns, Random gen, Game[] history,
	                         Game[] historyA, Game[] historyB,
	                         boolean display) throws Exception {
		int extraTurns = turns / 8;
		if (extraTurns == 0)
			extraTurns = 1;
		// pick players from teams
		int[] playersA = teamA.pickTeam(teamB.name(), statsA.length, append(history, historyA));
		if (!checkTeam(playersA, statsA.length))
			throw new Exception("Invalid lineup for team " + teamA.name());
		int[] playersB = teamB.pickTeam(teamA.name(), statsB.length, append(history, historyB));
		if (!checkTeam(playersB, statsB.length))
			throw new Exception("Invalid lineup for team " + teamB.name());
		if (teamA == teamB && playerTwice(playersA, playersB))
			throw new Exception("Internal game of team " + teamA.name() + " declared player in both lineups");
		teamA.opponentTeam(playersB);
		teamB.opponentTeam(playersA);
		// isolate stats
		double[][] stats = new double[10][4];
		for (int i = 0 ; i != 5 ; ++i)
			for (int j = 0 ; j != 4 ; ++j)
				stats[i][j] = statsA[playersA[i] - 1][j];
		for (int i = 5 ; i != 10 ; ++i)
			for (int j = 0 ; j != 4 ; ++j)
				stats[i][j] = statsB[playersB[i - 5] - 1][j];
		// game state
		int turn = 1;
		int holder = 0;
		boolean changed = true;
		boolean passed = false;
		int[] defenders = null;
		int team = gen.nextInt(2);
		Team[] p = new Team[] {teamA, teamB};
		int[] score = new int[] {0, 0};
		Vector <Game.Round> seasons = new Vector <Game.Round> ();
		Vector <Integer> holders = new Vector <Integer> ();
		Game.Action lastAction = Game.Action.SCORED;
		Game.Round lastRound = null;
		// info
		if (display)
			if (teamA == teamB)
				System.err.println("Training game: " + teamA.name());
			else
				System.err.println("Tournament game: " + teamA.name() + " vs. " + teamB.name());
		for (;;) {
			// attack starts now
			if (holder == 0) {
				// save old season
				if (!holders.isEmpty()) {
					lastRound = new Game.Round(defenders, toIntArray(holders), team == 0, lastAction);
					seasons.add(lastRound);
				}
				// swap teams
				team = 1 - team;
				passed = false;
				// pick ball holder
				holder = p[team].pickAttack(score[team], score[1 - team], lastRound);
				if (holder < 1 || holder > 5)
					throw new Exception("Invalid initial ball holder (please give one of 0,1,2,3,4,5)");
				// pick defenders
				defenders = p[1 - team].pickDefend(score[1 - team], score[team], holder, lastRound);
				checkTeam(defenders, 5);
				defenders = Arrays.copyOf(defenders, 5);
				// display
				if (display) {
					if (team == 0)
						printStartTeamA(holder, defenders, score, changed);
					else
						printStartTeamB(holder, defenders, score, changed);
					System.err.println("  Team " + (team == 0 ? "A" : "B") + " is now attacking  (turn " + turn + ")");
					System.err.println("    Player " + holder + " holds the ball");
				}
				// check end of game (ties are not allowed)
				if (turn++ == turns) {
					if (score[0] != score[1]) break;
					turns += extraTurns;
				}
				changed = false;
				holders.clear();
			}
			holders.add(holder);
			if (display)
				if (team == 0)
					printAttackTeamA(holder, defenders);
				else
					printAttackTeamB(holder, defenders);
			// next action
			int newHolder = p[team].action(defenders);
			int a = holder - 1 + team * 5;
			int d = defenders[holder - 1] - 1 + (1 - team) * 5;
			if (newHolder == 0) {
				// ensure at least one pass
				if (!passed)
					throw new Exception("Cannot shoot without passing at least once");
				// attempt shoot
				double prob = (stats[a][0] + stats[d][1]) / 2.0;
				if (display)
					System.err.print("    Player " + holder + " shoots");
				if (gen.nextDouble() > prob) {
					if (display)
						System.err.println(" and misses.");
					lastAction = Game.Action.MISSED;
				} else {
					if (display)
						System.err.println(" and scores! (" + score[0] + "-" + score[1] + ")");
					score[team]++;
					changed = true;
					lastAction = Game.Action.SCORED;
				}
			} else {
				// check passing destination
				if (newHolder < 1 || newHolder > 5)
					throw new Exception("Invalid player to pass (please give one of 0,1,2,3,4,5)");
				passed = true;
				// try passing
				double prob = (8.0 + stats[a][2] + stats[d][3]) / 10.0;
				if (display)
					System.err.print("    Player " + holder + " passes to player " + newHolder);
				// pass missed
				if (gen.nextDouble() > prob) {
					newHolder = 0;
					lastAction = Game.Action.STOLEN;
					if (display)
						System.err.print(" but the ball is stolen!");
				}
				if (display)
					System.err.println("");
			}
			holder = newHolder;
		}
		if (display)
			System.err.println(teamA.name() + " - " + teamB.name() +
			                   ":   " + score[0] + "-" + score[1]);
		return new Game(teamA.name(), teamB.name(), score[0], score[1],
		                playersA, playersB, seasons.toArray(new Game.Round[0]));
	}

	// generate player stats
	private static double[][][] stats(Random gen, int teams, int players)
	{
		double[][][] stats = new double [teams][players][4];
		for (int t = 0 ; t != teams ; ++t)
			for (int i = 0 ; i != players ; ++i)
				for (int j = 0 ; j != 4 ; ++j)
					stats[t][i][j] = gen.nextInt(1001) * 0.001;
		return stats;
	}

	// check teams
	private static boolean checkTeam(int[] players, int totalPlayers)
	{
		if (players.length != 5)
			return false;
		for (int i = 0 ; i != players.length ; ++i) {
			if (players[i] < 1 || players[i] > totalPlayers)
				return false;
			for (int j = 0 ; j != i ; ++j)
				if (players[i] == players[j])
					return false;
		}
		return true;
	}

	// non conflicting players
	private static boolean playerTwice(int[] team1, int[] team2)
	{
		for (int i = 0 ; i != team1.length ; ++i)
			for (int j = 0 ; j != team2.length ; ++j)
				if (team1[i] == team2[j])
					return true;
		return false;
	}

	// merge history results
	private static Game[] append(Game[] a, Game[] b)
	{
		Game[] c = new Game[a.length + b.length];
		for (int i = 0 ; i != a.length ; ++i)
			c[i] = a[i];
		for (int i = 0 ; i != b.length ; ++i)
			c[i + a.length] = b[i];
		return c;
	}

	// convert to array of primitive ints
	private static int[] toIntArray(Vector <Integer> v)
	{
		int[] r = new int [v.size()];
		for (int i = 0 ; i != r.length ; ++i)
			r[i] = v.get(i).intValue();
		return r;
	}

	// digits of a number
	private static int spaces(int num)
	{
		int spaces = 0;
		if (num <= 0) {
			spaces = 1;
			num = -num;
		}
		while (num != 0) {
			spaces++;
			num /= 10;
		}
		return spaces;
	}

	// number to at 3-character string
	private static String intToString(int num, int len, boolean before)
	{
		int spaces = spaces(num);
		int extra = 0;
		if (spaces < len)
			extra = len - spaces;
		StringBuffer buf = new StringBuffer("");
		if (before)
			while (extra-- != 0)
				buf.append(" ");
		buf.append("" + num);
		if (!before)
			while (extra-- != 0)
				buf.append(" ");
		return buf.toString();
	}

	// print adding spaces to preserve alignment
	private static String align(String str, int len, boolean before)
	{
		if (str.length() >= len)
			return str;
		StringBuffer buf = new StringBuffer("");
		for (int i = 0 ; i != len - str.length() ; ++i)
			buf.append(" ");
		String space = buf.toString();
		return before ? space + str : str + space;
	}

	// print table for team A attacking
	private static void printStartTeamA(int holder, int[] defenders, int score[], boolean changed)
	{
		String h1 = (holder == 1 ? "o" : " ");
		String h2 = (holder == 2 ? "o" : " ");
		String h3 = (holder == 3 ? "o" : " ");
		String h4 = (holder == 4 ? "o" : " ");
		String h5 = (holder == 5 ? "o" : " ");
		String s1 = changed ? intToString(score[0], 3, true) : "   ";
		String s2 = changed ? intToString(score[1], 3, false) : "   ";
		System.err.println("+--------------------------------------------+");
		System.err.println("|                      |                     |");
		System.err.println("|________   1" + h1 + "         |         " + defenders[0] + "   ________|");
		System.err.println("|      /|\\  2" + h2 + "        / \\        " + defenders[1] + "  /|\\      |");
		System.err.println("|O    | | | 3" + h3 + "   " + s1 + " |   | " + s2 + "   " + defenders[2] + " | | |    O|");
		System.err.println("|______\\|/  4" + h4 + "        \\ /        " + defenders[3] + "  \\|/______|");
		System.err.println("|           5" + h5 + "         |         " + defenders[4] + "           |");
		System.err.println("|                      |                     |");
		System.err.println("+--------------------------------------------+");
	}

	// print table for team B attacking
	private static void printStartTeamB(int holder, int[] defenders, int score[], boolean changed)
	{
		String h1 = (holder == 1 ? "o" : " ");
		String h2 = (holder == 2 ? "o" : " ");
		String h3 = (holder == 3 ? "o" : " ");
		String h4 = (holder == 4 ? "o" : " ");
		String h5 = (holder == 5 ? "o" : " ");
		String s1 = changed ? intToString(score[0], 3, true) : "   ";
		String s2 = changed ? intToString(score[1], 3, false) : "   ";
		System.err.println("+--------------------------------------------+");
		System.err.println("|                      |                     |");
		System.err.println("|________   " + defenders[0] + "          |        " + h1 + "1   ________|");
		System.err.println("|      /|\\  " + defenders[1] + "         / \\       " + h2 + "2  /|\\      |");
		System.err.println("|O    | | | " + defenders[2] + "    " + s1 + " |   | " + s2 + "  " + h3 + "3 | | |    O|");
		System.err.println("|______\\|/  " + defenders[3] + "         \\ /       " + h4 + "4  \\|/______|");
		System.err.println("|           " + defenders[4] + "          |        " + h5 + "5           |");
		System.err.println("|                      |                     |");
		System.err.println("+--------------------------------------------+");
	}

	// print table for team A attacking
	private static void printAttackTeamA(int holder, int[] defenders)
	{
		String h1 = (holder == 1 ? "o" : " ") + defenders[0];
		String h2 = (holder == 2 ? "o" : " ") + defenders[1];
		String h3 = (holder == 3 ? "o" : " ") + defenders[2];
		String h4 = (holder == 4 ? "o" : " ") + defenders[3];
		String h5 = (holder == 5 ? "o" : " ") + defenders[4];
		System.err.println("+---------------------------------------------+");
		System.err.println("|                      |                1" + h1 + "  |");
		System.err.println("|________              |          2" + h2 + "________|");
		System.err.println("|      /|\\            / \\           /|\\      |");
		System.err.println("|O    | | |          |   |     3" + h3 + " | | |    O|");
		System.err.println("|______\\|/            \\ /           \\|/______|");
		System.err.println("|                      |          4" + h4 + "        |");
		System.err.println("|                      |                5" + h5 + "  |");
		System.err.println("+--------------------------------------------+");
	}

	// print table for team B attacking
	private static void printAttackTeamB(int holder, int[] defenders)
	{
		String h1 = "" + defenders[0] + (holder == 1 ? "o" : " ");
		String h2 = "" + defenders[1] + (holder == 2 ? "o" : " ");
		String h3 = "" + defenders[2] + (holder == 3 ? "o" : " ");
		String h4 = "" + defenders[3] + (holder == 4 ? "o" : " ");
		String h5 = "" + defenders[4] + (holder == 5 ? "o" : " ");
		System.err.println("+--------------------------------------------+");
		System.err.println("|   " + h1 + "1                |                     |");
		System.err.println("|________ " + h2 + "2          |             ________|");
		System.err.println("|      /|\\            / \\           /|\\      |");
		System.err.println("|O    | | | " + h3 + "3      |   |         | | |    O|");
		System.err.println("|______\\|/            \\ /           \\|/______|");
		System.err.println("|         " + h4 + "4          |                     |");
		System.err.println("|   " + h5 + "5                |                     |");
		System.err.println("+--------------------------------------------+");
	}

	// check unique names
	private static boolean uniqueNames(Team[] teams)
	{
		HashSet <String> set = new HashSet <String> ();
		for (Team team : teams)
			if (!set.add(team.name()))
				return false;
		return true;
	}

	// sort teams by score
	private static void sortByScore(Team[] teams, int[] points)
	{
		for (int i = 0 ; i != teams.length ; ++i) {
			// find max
			int max = i;
			for (int j = i + 1 ; j != teams.length ; ++j)
				if (points[j] > points[max])
					max = j;
			// swap team
			Team tempTeam = teams[i];
			teams[i] = teams[max];
			teams[max] = tempTeam;
			// swap points
			int teamPoints = points[i];
			points[i] = points[max];
			points[max] = teamPoints;
		}
	}
}
