package enums;

public class Direction {
	public static final int NO_DIRECTION = 0;
	public static final int UP = 4;
	public static final int RIGHT = 1;
	public static final int DOWN = 3;
	public static final int LEFT = 2;

	public static String toString(int direction) {
		switch (direction) {
		case UP:
			return "UP";
		case RIGHT:
			return "RIGHT";
		case DOWN:
			return "DOWN";
		case LEFT:
			return "LEFT";
		default:
			return "NO_DIRECTION";
		}
	}
}
