import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class CaveExplorer {
	// Direction order: north, south, east, west
	private static final int[] ROW_STEP = {-1, 1, 0, 0};
	private static final int[] COL_STEP = {0, 0, 1, -1};
	private static final char[] DIR_CHAR = {'n', 's', 'e', 'w'};

	private char[][] cave;
	private StringBuilder path = new StringBuilder();
	private boolean solved = false;

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

	// Step 3: solve method
	// Depth-first search from 'S'. Cells on the current path are marked with '*',
	// and the mark is removed when we back out of a dead end, so the final layout
	// shows only the successful path. 'S' and 'M' are never overwritten.
	public boolean solve() {
		clearMarks();
		path = new StringBuilder();
		solved = false;

		for (int r = 0; r < cave.length; r++) {
			for (int c = 0; c < cave[r].length; c++) {
				if (cave[r][c] == 'S') {
					solved = explore(r, c);
					if (!solved) {
						path = new StringBuilder();
					}
					return solved;
				}
			}
		}
		return false; // no starting point
	}

	// Tries all four directions from (r, c). Returns true if the mirror pool is reached.
	private boolean explore(int r, int c) {
		for (int d = 0; d < 4; d++) {
			int nr = r + ROW_STEP[d];
			int nc = c + COL_STEP[d];

			if (nr < 0 || nr >= cave.length || nc < 0 || nc >= cave[nr].length) {
				continue;
			}

			char next = cave[nr][nc];
			if (next == 'M') {
				path.append(DIR_CHAR[d]);
				return true;
			}
			if (next == '.') {
				cave[nr][nc] = '*';          // mark visited so we never loop
				path.append(DIR_CHAR[d]);
				if (explore(nr, nc)) {
					return true;
				}
				path.deleteCharAt(path.length() - 1); // dead end: back up
				cave[nr][nc] = '.';
			}
		}
		return false;
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

	// Step 4: getPath method
	public String getPath() {
		if (solved) {
			return path.toString();
		}
		return "";
	}

	// Step 5: One parameter constructor to read from a file
	public CaveExplorer(String fname) throws Exception {
		Scanner in = new Scanner(new File(fname));

		int rows = in.nextInt();
		int cols = in.nextInt();
		String s = in.nextLine(); // Skip newline character

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

		// Create a CaveExplorer object, print the starting layout, solve, print results
		CaveExplorer ce1 = new CaveExplorer();
		runCave("Hardcoded cave", ce1);

		// Step 5/6: Repeat for a different CaveExplorer object read from a file
		String fileName = "testdat.txt";
		try {
			CaveExplorer ce2 = new CaveExplorer(fileName);
			runCave("Cave from " + fileName, ce2);
		}
		catch (FileNotFoundException e) {
			System.out.println("Can't find file " + fileName);
		}
		catch (Exception e) {
			System.out.println("Other error: " + e.getMessage());
		}

		System.out.println("Finished CaveExplorer");
	}

}