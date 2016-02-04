package game.player.pacman;

import game.core.G;
import game.core.Game;
import game.core.Game.DM;
import game.core.GameView;
import gui.AbstractPlayer;

import java.awt.Color;

/*
 * Same as NearestPillPacMan but does some visuals to illustrate what can be done.
 * Please note: the visuals are just to highlight different functionalities and may
 * not make sense from a controller's point of view (i.e., they might not be useful)
 * Comment/un-comment code below as desired (drawing all visuals would probably be too much).
 */
public final class NearestPillPacManVS3 extends AbstractPlayer
{	
	@Override
	public int getAction(Game game,long timeDue){		
		final int ghost_dist_threshold = 15;
		final int power_pill_dist_threshold = 10;
		int current=game.getCurPacManLoc();
		int [] neighbour_grids = game.getPacManNeighbours();

		int[] activePills=game.getPillIndicesActive();
		//int[] targetsArray=new int[activePills.length+activePowerPills.length];

		int [] junctions = game.getJunctionIndices();
		int nearst_junction = game.getTarget(current, junctions, true, DM.PATH);
		int nearst_junction_dist = game.getPathDistance(current, nearst_junction);
		int nearst_junction_dir = game.getNextPacManDir(nearst_junction, true, DM.PATH);

		int nearst_ghost_index = 0;
		int [] ghost_dists = new int[4];
		int [] ghost_locs  = new int[4];
		int [] toward_ghost_dirs  = new int[4];

		for (int i = 0; i < 4; i++) {
			ghost_locs[i]  = game.getCurGhostLoc(i);
			ghost_dists[i] = game.getPathDistance(current, ghost_locs[i]);
			toward_ghost_dirs[i] = game.getNextPacManDir(ghost_locs[i], true, DM.PATH);
			if (ghost_dists[i] != -1 && ghost_dists[i] < ghost_dists[nearst_ghost_index]) {
				nearst_ghost_index = i;
			}
		}

		//no need of worrying about ghosts
		//System.out.println(ghost_dists[nearst_ghost_index]);
		if (ghost_dists[nearst_ghost_index] == -1 ||
			ghost_dists[nearst_ghost_index] > ghost_dist_threshold || 
			game.getEdibleTime(nearst_ghost_index) > ghost_dist_threshold/2) {
			//System.out.println("no need to worry about ghosts");
			return game.getNextPacManDir(game.getTarget(current,activePills,true,G.DM.PATH), 
										 true, 
										 DM.PATH);
		} 

		//no power pills any more
		if (game.getNumActivePowerPills() == 0) {
			boolean [] ghost_on_road = new boolean[4];
			for (int i = 0; i < 4; i++) {
				ghost_on_road[i] = false;
			}
			for (int i = 0; i < 4; i++) {
				ghost_on_road[toward_ghost_dirs[i]] = true;
			}
			for (int i = 0; i < 4; i++) {
				if (!ghost_on_road[i] && neighbour_grids[i] != -1) {
					return i;
				}
			}
			int [] nearst_ghost_index_on_dir = new int[4];
			for (int i = 0; i < 4; i++) {
				nearst_ghost_index_on_dir[i] = -1;
			}
			for (int i = 0; i < 4; i++) {//directions
				for (int j = 0; j < 4; j++) {//ghosts
					if (i == toward_ghost_dirs[j]) {
						if (nearst_ghost_index_on_dir[i] == -1) {
							nearst_ghost_index_on_dir[i] = j;
						} else if (ghost_dists[nearst_ghost_index_on_dir[i]] > ghost_dists[j]) {
							nearst_ghost_index_on_dir[i] = j;
						}
					}
				}
			}
			
			int compromise_dir = 0;
			while (neighbour_grids[compromise_dir] == -1) {
				compromise_dir++;
			}
			for (int i = compromise_dir+1; i < 4; i++) {
				if (neighbour_grids[i] != -1 &&
					ghost_dists[nearst_ghost_index_on_dir[i]] > ghost_dists[nearst_ghost_index_on_dir[compromise_dir]]) {
					compromise_dir = i;
				}
			}
			return compromise_dir;
		}

		int[] activePowerPills=game.getPowerPillIndicesActive();
		int nearst_power_pill_loc = game.getTarget(current, activePowerPills, true, DM.PATH);
		int nearst_power_pill_dist = game.getPathDistance(current, nearst_power_pill_loc);
		int nearst_power_pill_dir = game.getNextPacManDir(nearst_power_pill_loc, true, DM.PATH);

		//first power pill, then deal with ghosts
		if (nearst_power_pill_dist < power_pill_dist_threshold &&
			(nearst_power_pill_dist < ghost_dists[nearst_ghost_index] ||
			toward_ghost_dirs[nearst_ghost_index] != nearst_power_pill_dir)) {
			return nearst_power_pill_dir;
		}

		//trouble comes
//		System.out.println("==============================");
//		System.out.println("Ghost Alert!");
//		System.out.println("on direction "+ghost_locs[nearst_ghost_index]);
//		System.out.println("pacman: ("+game.getX(current)+", "+game.getY(current)+")");
//		System.out.println("ghost : ("+game.getX(game.getCurGhostLoc(nearst_ghost_index))+", "+game.getY(game.getCurGhostLoc(nearst_ghost_index))+")");
		boolean [] ghost_on_road = new boolean[4];
		for (int i = 0; i < 4; i++) {
			ghost_on_road[i] = false;
		}
		for (int i = 0; i < 4; i++) {
			ghost_on_road[toward_ghost_dirs[i]] = true;
		}
		for (int i = 0; i < 4; i++) {
			if (!ghost_on_road[i] && neighbour_grids[i] != -1) {
				return i;
			}
		}
		
		int [] nearst_ghost_index_on_dir = new int[4];
		for (int i = 0; i < 4; i++) {
			nearst_ghost_index_on_dir[i] = -1;
		}
		for (int i = 0; i < 4; i++) {//directions
			for (int j = 0; j < 4; j++) {//ghosts
				if (i == toward_ghost_dirs[j]) {
					if (nearst_ghost_index_on_dir[i] == -1) {
						nearst_ghost_index_on_dir[i] = j;
					} else if (ghost_dists[nearst_ghost_index_on_dir[i]] > ghost_dists[j]) {
						nearst_ghost_index_on_dir[i] = j;
					}
				}
			}
		}
		
		int compromise_dir = 0;
		while (neighbour_grids[compromise_dir] == -1) {
			compromise_dir++;
		}
		for (int i = compromise_dir+1; i < 4; i++) {
			if (neighbour_grids[i] != -1 &&
				ghost_dists[nearst_ghost_index_on_dir[i]] > ghost_dists[nearst_ghost_index_on_dir[compromise_dir]]) {
				compromise_dir = i;
			}
		}
		return compromise_dir;
		/*
		if (neighbour_grids[game.getReverse(nearst_ghost_dir)] != -1) {
			return game.getReverse(nearst_ghost_dir);
		}
		for (int i = 0; i < 4; i++) {
			if (i != nearst_ghost_dir && neighbour_grids[i] != -1) {
				return i;
			}
		}

		if (game.isJunction(current)) {
			for (int i = 0; i < 4; i++) {
				if (i != nearst_ghost_dir &&
					i != game.getReverse(game.getCurPacManDir()) &&
					neighbour_grids[i] != -1){
					return i;
				}
			}
		}
		if (nearst_junction_dir != nearst_ghost_dir ||
			nearst_junction_dist < nearst_ghost_dist) {
			return nearst_junction_dir;
		}

		int temp_dist_to_ghost = 10000;
		if (game.getPowerPillIndicesActive().length != 0){
			int [] route_to_nearst_power_pill = game.getPath(current, nearst_power_pill_loc);
			for (int cur_pos : route_to_nearst_power_pill) {
				int cur_pos_to_ghost_dist = game.getPathDistance(cur_pos, nearst_ghost_loc);
				if (cur_pos_to_ghost_dist > temp_dist_to_ghost) {
					return nearst_power_pill_dir;
				}
				temp_dist_to_ghost = cur_pos_to_ghost_dist;
			}
		}
		return nearst_ghost_dir;
		 */
	}

	@Override
	public String getGroupName() {
		return "ANearest Pill PacMan Visuals Modified 2";
	}
}