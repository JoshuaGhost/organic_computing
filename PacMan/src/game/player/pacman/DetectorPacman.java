package game.player.pacman;

import game.core.G;
import game.core.Game;
import game.core.Game.DM;
import game.core.GameView;
import game.core._G_;
import gui.AbstractPlayer;

import java.awt.Color;
import java.util.Hashtable;

/*
 * Same as NearestPillPacMan but does some visuals to illustrate what can be done.
 * Please note: the visuals are just to highlight different functionalities and may
 * not make sense from a controller's point of view (i.e., they might not be useful)
 * Comment/un-comment code below as desired (drawing all visuals would probably be too much).
 */
public final class DetectorPacman extends AbstractPlayer { // at best 40
	//private static final int MAX_DETECT_STEP = 31;
	private static final int MAX_DETECT_STEP = 61;
	private static final int ghost_dist_threshold = 20;
	private static final int power_pill_max_dist_threshold = 45;
	private static final int power_pill_min_dist_threshold = 13;
	private static final int nearst_ghost_max_threshold = 12;
	private static final int fishing_threshold = 61;
	@Override
	public int getAction(Game game, long timeDue) {
		int current = game.getCurPacManLoc();
		int[] neighbour_grids = game.getPacManNeighbours();

		int[] activePills = game.getPillIndicesActive();
		// int[] targetsArray=new
		// int[activePills.length+activePowerPills.length];

		int[] junctions = game.getJunctionIndices();
		int nearst_junction = game.getTarget(current, junctions, true, DM.PATH);
		int nearst_junction_dist = game.getPathDistance(current, nearst_junction);
		int nearst_junction_dir = game.getNextPacManDir(nearst_junction, true, DM.PATH);

		int nearst_ghost_index = 0;
		int[] ghost_dists = new int[4];
		int[] ghost_locs = new int[4];
		int[] toward_ghost_dirs = new int[4];

		int[] activePowerPills = game.getPowerPillIndicesActive();
		int nearst_power_pill_loc = 0;
		int nearst_power_pill_dist = 0;
		int nearst_power_pill_dir = 0;
		if (activePowerPills.length != 0) {
			nearst_power_pill_loc = game.getTarget(current, activePowerPills, true, DM.PATH);
			nearst_power_pill_dist = game.getPathDistance(current, nearst_power_pill_loc);
			nearst_power_pill_dir = game.getNextPacManDir(nearst_power_pill_loc, true, DM.PATH);
		}

		int start_dir = (int) (Math.random() / 4.0);
		start_dir = 0;
		
		int[] ghost_on_road = new int[4];
		for (int i = 0; i < 4; i++) {
			ghost_on_road[i] = 0;
		}
		for (int i = 0; i < 4; i++) {
			ghost_on_road[toward_ghost_dirs[i]] ++;
		}
		for (int i = 0; i < 4; i++) {
			if (ghost_on_road[(start_dir + i) / 4] == 0 && neighbour_grids[(start_dir + i) / 4] != -1) {
				return i;
			}
		}
		int[] nearst_ghost_index_on_dir = new int[4];
		for (int i = 0; i < 4; i++) {
			nearst_ghost_index_on_dir[i] = -1;
		}
		for (int i = 0; i < 4; i++) {// directions
			for (int j = 0; j < 4; j++) {// ghosts
				if (i == toward_ghost_dirs[j]) {
					if (nearst_ghost_index_on_dir[i] == -1) {
						nearst_ghost_index_on_dir[i] = j;
					} else if (ghost_dists[nearst_ghost_index_on_dir[i]] > ghost_dists[j]) {
						nearst_ghost_index_on_dir[i] = j;
					}
				}
			}
		}
		for (int i = 0; i < 4; i++) {
			ghost_locs[i] = game.getCurGhostLoc(i);
			ghost_dists[i] = game.getPathDistance(current, ghost_locs[i]);
			toward_ghost_dirs[i] = game.getNextPacManDir(ghost_locs[i], true, DM.PATH);
			if (ghost_dists[i] != -1 && ghost_dists[i] < ghost_dists[nearst_ghost_index]) {
				nearst_ghost_index = i;
			}
		}

		int min_ghost_dir_index = 3;
		int min_ghost_dir_num = 4;
		for (int i = 1; i < 4; i++) {
			if (ghost_on_road[i] < min_ghost_dir_num &
				neighbour_grids[i] != -1) {
				min_ghost_dir_index = i;
				min_ghost_dir_num = ghost_on_road[i];
			}
		}
		
		start_dir = min_ghost_dir_index;
		// fishing
		if (game.getNumActivePowerPills() != 0 &&
				nearst_power_pill_dist <= power_pill_min_dist_threshold &&
				!ghostAlertWhileFishing(game, ghost_dists)) {
			//return game.getReverse(game.getCurPacManDir());
		}

		// no need of worrying about ghosts
		if (!ghostAlertWhileWandering(game, ghost_dists)) {
			return game.getNextPacManDir(game.getTarget(current, activePills, true, G.DM.PATH), true, DM.PATH);
		}

		// no power pills any more
		if (game.getNumActivePowerPills() == 0) {
			return detected_dir(game, current);
		}

		// first power pill, then deal with ghosts
		if(nearst_power_pill_dist < power_pill_max_dist_threshold &
				!ghostAlertWhileHeadingtoPP(game, ghost_dists,
										   nearst_power_pill_dist,
										   nearst_power_pill_dir,
										   toward_ghost_dirs)) {
			return nearst_power_pill_dir;
		}

		return detected_dir(game, current);
	}

	private int detected_dir(Game game, int current) {
		// run run run
		Hashtable<Integer, Integer> visited = new Hashtable<Integer, Integer>();
		visited.put(current, 0);
		long maxWeight = Long.MIN_VALUE;
		int direction = 0;

		for(int dir = 0; dir < 4; dir++) {
			int detectLoc = game.getNeighbour(current, dir);
			if (detectLoc != -1) {
				long tempWeight = detect(game, detectLoc, 1, visited);
				//System.out.println("to direction: "+String.valueOf(dir));
				//System.out.println("tmpWeight = "+String.valueOf(tempWeight));
				if (tempWeight > maxWeight) {
					maxWeight = tempWeight;
					direction = dir;
				}
			}
		} 
		//System.out.println("----------------------");
		//System.out.println("maxWeight = "+String.valueOf(maxWeight));
		//System.out.println("direction = "+String.valueOf(direction));
		//System.out.println("=======================");
		return direction;
	}

	private boolean ghostAlertWhileFishing(Game game, int[] ghost_dists) {
		boolean ghost_alert = false;
		for (int ghost_num = 0; ghost_num < 4; ghost_num++){
			if (ghost_dists[ghost_num] != -1 &
					ghost_dists[ghost_num] < ghost_dist_threshold &
					game.getEdibleTime(ghost_num) < ghost_dists[ghost_num] + 4) {
				ghost_alert = true;
				break;
			}
		}
		return ghost_alert;
	}

	private boolean ghostAlertWhileWandering(Game game, int[] ghost_dists){
		boolean ghost_alert = false;
		for (int ghost_num = 0; ghost_num < 4; ghost_num++){
			if (ghost_dists[ghost_num] != -1 &
					ghost_dists[ghost_num] < ghost_dist_threshold &
					game.getEdibleTime(ghost_num) < ghost_dist_threshold / 2 + 4) {
				ghost_alert = true;
				break;
			}
		}
		return ghost_alert;
	}
	
	private boolean ghostAlertWhileHeadingtoPP(Game game, int[] ghost_dists, 
											   int nearst_power_pill_dist, 
											   int nearst_power_pill_dir,
											   int[] toward_ghost_dirs){
		boolean ghost_alert = false;
		for (int ghost_num = 0; ghost_num < 4; ghost_num++){
			if (ghost_dists[ghost_num] != -1 &
					ghost_dists[ghost_num] < ghost_dist_threshold &
					game.getEdibleTime(ghost_num) < ghost_dists[ghost_num] + 4 &
					nearst_power_pill_dist > ghost_dists[ghost_num] &
					toward_ghost_dirs[ghost_num] == nearst_power_pill_dir) {
				ghost_alert = true;
				break;
			}
		}
		return ghost_alert;
	}

	private long detect(Game game, int current, int step, Hashtable<Integer, Integer> visited) {
		visited.put(current, step);
		long minDistAdvance = Long.MAX_VALUE;
		int distFromPacman = game.getPathDistance(game.getCurPacManLoc(), current);
		//System.out.println("++++++++++++++++++++++++");
		for (int ghostNum = 0; ghostNum < 4; ghostNum++) {
			int ghostLoc = game.getCurGhostLoc(ghostNum);
			int distFromGhost = game.getPathDistance(ghostLoc, current);
			int distFromGhosttoPacman = game.getPathDistance(ghostLoc, current);
			//int distFromGhost = game.getGhostPathDistance(ghostNum, current);
			//int distFromGhosttoPacman = game.getGhostPathDistance(ghostNum, game.getCurPacManLoc());
			int distAdvance = distFromGhost - distFromPacman;
			//System.out.println("Ghost num: "+String.valueOf(ghostNum));
			//System.out.println("at location");
			//System.out.println("Ghost with step advance = "+String.valueOf(distAdvance));
			if (game.getPathDistance(current, ghostLoc) < 6 &
				game.getEdibleTime(ghostNum) < distFromGhost * 2 &
				distFromGhosttoPacman < 2 * ghost_dist_threshold) {
				//GameView.addPoints(game, Color.RED, current);
				distAdvance -= (MAX_DETECT_STEP - step);
			}
			if (distAdvance >= -3 & distAdvance <= 3 &//Math.min(MAX_DETECT_STEP - step - 1, 0) & 
				game.getEdibleTime(ghostNum) < distFromGhost * 2 &
				distFromGhosttoPacman < 2 * ghost_dist_threshold) {
				//GameView.addPoints(game, Color.YELLOW, current);
				distAdvance -= (MAX_DETECT_STEP - step);
			} 
			if (distAdvance < minDistAdvance) {
				minDistAdvance = distAdvance;
			}
		}

		long dirWeight = minDistAdvance;
		
		if (step == MAX_DETECT_STEP) {
			//GameView.addPoints(game, Color.CYAN, current);
			return dirWeight;
		}
		
		long maxNeighbourhood = Long.MIN_VALUE;
		for (int dir = 0; dir < 4; dir++) {
			int detectLoc = game.getNeighbour(current, dir);
			if (detectLoc != -1) {
				if (visited.get(detectLoc) == null) {
					int distFromPacmanToDet = game.getPathDistance(game.getCurPacManLoc(), detectLoc);
					if (distFromPacmanToDet != -1) {
						//recursive search neighborhoods
						Hashtable<Integer, Integer> v = (Hashtable<Integer, Integer>) visited.clone();
						long distAdvanceWeight = detect(game, detectLoc, step + 1, v);
						if (maxNeighbourhood < distAdvanceWeight) {
							maxNeighbourhood = distAdvanceWeight;
						}
						//increase weight when pill on the road
						int pillIndex = game.getPillIndex(detectLoc);
						if (pillIndex != -1) {
							if (game.checkPill(pillIndex)) {
								dirWeight += Math.max(MAX_DETECT_STEP / 2 - step, 0);
							}
						}
					}
				} else {
					if (step - visited.get(detectLoc) > 1) {//loop
						//System.out.println("==================");
						//System.out.println("step - visited = "+String.valueOf(step-visited.get(detectLoc)));
						
					dirWeight -= (visited.get(detectLoc) - step) * (visited.get(detectLoc) - step) / 4;
				}
					}
			}
		}
		//GameView.addPoints(game, Color.CYAN, current);
		return dirWeight + maxNeighbourhood;
	}

	@Override
	public String getGroupName() {
		return "Detector Pacman";
	}
}