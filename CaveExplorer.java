import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Stack;

public class CaveExplorer {
	// Direction order: north, south, east, west
	private static final int[] ROW_STEP = {-1, 1, 0, 0};
	private static final int[] COL_STEP = {0, 0, 1, -1};
	private static final char[] DIR_CHAR = {'n', 's', 'e', 'w'};

	private char[][] cave;
	private String path = "";
	private boolean solved = false;

	// One pending move on the stack: the cell to visit and the exact directions
	// taken from 'S' to get there. Keeping the path with each move means a dead
	// end never contaminates the answer: when we pop a different move, its own
	// path is already correct.
	private static class Move {
		int row;
		int col;
		String path;

		Move(int row, int col, String path) {
			this.row = row;
			this.col = col;
			this.path = path;
		}
	}

	// Step 1: Zero parameter constructor to create the specified cave
	public CaveExplorer() {
		String[] layout = {
			"RRRRRR",
			"R..SRR",
			"R.RRRR",
			"R.MRRR",
			"RRRRRR"
		};
		cave = new char[layout.length][];
		for (int r = 0; r < layout.length; r++) {
			cave[r] = layout[r].toCharArray();
		}
	}

	// Step 2: toString method
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int r = 0; r < cave.length; r++) {
			sb.append(new String(cave[r]));
			sb.append("\n");
		}
		return sb.toString();
	}

	// Step 3: solve method (stack-based search with backtracking)
	// Pop a move, then push every unvisited open neighbor. Branch points push two
	// or three moves; if the explorer hits a dead end, nothing new is pushed and
	// the next pop resumes from the most recent unexplored branch.
	// Neighbors are pushed in reverse so moves are tried in order N, S, E, W.
	public boolean solve() {
		clearMarks();
		path = "";
		solved = false;

		int startRow = -1;
		int startCol = -1;
		for (int r = 0; r < cave.length; r++) {
			for (int c = 0; c < cave[r].length; c++) {
				if (cave[r][c] == 'S') {
					startRow = r;
					startCol = c;
				}
			}
		}
		if (startRow == -1) {
			return false; // no starting point
		}

		boolean[][] visited = new boolean[cave.length][];
		for (int r = 0; r < cave.length; r++) {
			visited[r] = new boolean[cave[r].length];
		}

		Stack<Move> stack = new Stack<Move>();
		visited[startRow][startCol] = true;
		stack.push(new Move(startRow, startCol, ""));

		while (!stack.isEmpty()) {
			Move cur = stack.pop();

			for (int d = 3; d >= 0; d--) {
				int nr = cur.row + ROW_STEP[d];
				int nc = cur.col + COL_STEP[d];

				if (nr < 0 || nr >= cave.length || nc < 0 || nc >= cave[nr].length) {
					continue;
				}
				if (visited[nr][nc]) {
					continue;
				}

				char next = cave[nr][nc];
				if (next == 'M') {
					path = cur.path + DIR_CHAR[d];
					solved = true;
					markPath(startRow, startCol);
					return true;
				}
				if (next == '.') {
					visited[nr][nc] = true; // never queue the same cell twice
					stack.push(new Move(nr, nc, cur.path + DIR_CHAR[d]));
				}
			}
		}
		return false; // stack empty: every reachable cell explored, no mirror pool
	}

	// Walks the finished path from 'S' and marks each step with '*'.
	// The last move lands on 'M', so it is not marked. Dead ends are never marked.
	private void markPath(int r, int c) {
		for (int i = 0; i < path.length() - 1; i++) {
			int d = directionIndex(path.charAt(i));
			r += ROW_STEP[d];
			c += COL_STEP[d];
			cave[r][c] = '*';
		}
	}

	private int directionIndex(char ch) {
		for (int d = 0; d < DIR_CHAR.length; d++) {
			if (DIR_CHAR[d] == ch) {
				return d;
			}
		}
		return -1;
	}

	// Removes path marks left over from an earlier call to solve
	private void clearMarks() {
		for (int r = 0; r < cave.length; r++) {
			for (int c = 0; c < cave[r].length; c++) {
				if (cave[r][c] == '*') {
					cave[r][c] = '.';
				}
			}
		}
	}

	// Step 4: getPath method (exact path only, no dead ends)
	public String getPath() {
		if (solved) {
			return path;
		}
		return "";
	}

	// Step 5: One parameter constructor to read from a file
	public CaveExplorer(String fname) throws Exception {
		Scanner in = new Scanner(new File(fname));

		int rows = in.nextInt();
		int cols = in.nextInt();
		in.nextLine(); // Skip newline character

		// Construct cave and populate with rest of data in the file
		cave = new char[rows][cols];
		for (int r = 0; r < rows; r++) {
			String line = in.nextLine();
			for (int c = 0; c < cols; c++) {
				if (c < line.length()) {
					cave[r][c] = line.charAt(c);
				} else {
					cave[r][c] = 'R'; // pad short lines with rock
				}
			}
		}
		in.close();
	}

	// Prints the starting layout, solves, then prints the final layout and path
	private static void runCave(String title, CaveExplorer ce) {
		System.out.println("--- " + title + " ---");
		System.out.println("Starting layout:");
		System.out.print(ce);

		boolean found = ce.solve();

		System.out.println("Final layout:");
		System.out.print(ce);
		if (found) {
			System.out.println("Path to the mirror pool exists: " + ce.getPath());
		} else {
			System.out.println("There is no path to the mirror pool.");
		}
		System.out.println();
	}

	public static void main(String[] args) {
		System.out.println("Starting CaveExplorer");

		// Test 1: hardcoded cave, no branches
		runCave("Test 1: hardcoded cave (no branches)", new CaveExplorer());

		// Tests 2-4: caves read from files
		String[] fileNames = {"cave_branch.txt", "cave_maze.txt", "cave_nopath.txt"};
		String[] titles = {
			"Test 2: two dead-end branches at the start",
			"Test 3: larger cave, several dead ends",
			"Test 4: no path to the mirror pool"
		};

		for (int i = 0; i < fileNames.length; i++) {
			try {
				CaveExplorer ce = new CaveExplorer(fileNames[i]);
				runCave(titles[i], ce);
			}
			catch (FileNotFoundException e) {
				System.out.println("Can't find file " + fileNames[i]);
			}
			catch (Exception e) {
				System.out.println("Other error: " + e.getMessage());
			}
		}

		System.out.println("Finished CaveExplorer");
	}

}