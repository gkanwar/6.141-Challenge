package rrt;

import java.util.LinkedList;

import core.Config;
import core.StateEstimator;

import map.Map;
import map.Point;
import map.Segment;
import state_machine.StateMachine;

public class PathPlanning {
	private static PathPlanning instance;

	private StateMachine sm;
	private StateEstimator se;
	private Map map;

	LinkedList<Point> path;
	Point nextWaypoint;
	public Point goal;

	public PathPlanning() {
		this.sm = StateMachine.getInstance();
		this.se = StateEstimator.getInstance();
		this.map = Map.getInstance();
	}

	public static PathPlanning getInstance() {
		if (instance == null)
			instance = new PathPlanning();
		return instance;
	}

	public void step() {
		Point curLoc = map.bot.pose;
		
		Point newGoal = sm.getGoal();
		
		if (newGoal == null) {
			nextWaypoint = curLoc;
		}
		
		if (newGoal != goal) {
			goal = newGoal;
			findPath(newGoal);
			nextWaypoint = path.getFirst();
		}
		
		if (map.checkSegment(new Segment(curLoc, nextWaypoint))) {
			findPath(newGoal);
			nextWaypoint = path.getFirst();
		}

		// try to shortcut paths
		for (int i = path.size()-1; i >= 0; i--) {
			if (map.checkSegment(new Segment(curLoc, path.get(i)))) {
				nextWaypoint = path.get(i);
				i--;
				for (; i>= 0; i--)
					path.remove(i);
			}
		}

		// stop when we arrive at goal
		if (Math.abs(curLoc.distance(newGoal)) < .05) {
			nextWaypoint = curLoc;
			path.clear();
			return;
		}
		
		System.out.print("Path: ");
		for (Point p : path)
			System.out.print(p + ", ");
		System.out.println("");
	}

	public Point getNextWaypoint() {
		return nextWaypoint;
	}

	public void findPath(Point goal) {
		Point start = new Point(map.bot.pose.x, map.bot.pose.y);
		TreeNode root = new TreeNode(start);
		Tree rrt = new Tree(root);

		Point p;
		TreeNode closest, newNode, goalNode;
		Segment seg;
		if (map.checkSegment(new Segment(start, goal))) {
			path = new LinkedList<Point>();

			path.add(goal);
			map.path = path;
			nextWaypoint = path.get(0);
		}

		while (true) {
			p = map.randomPoint();

			closest = root;
			for (TreeNode node : rrt.nodes) {
				if (node.loc.distance(p) < closest.loc.distance(p)) {
					closest = node;
				}
			}

			seg = new Segment(closest.loc, p);
			seg = seg.trim(Config.MAXLENGTH);
			p = seg.end;
			if (!map.checkSegment(seg)) {
				continue;
			}

			newNode = new TreeNode(seg.end);
			closest.addChild(newNode);
			seg = new Segment(goal, p);

			if (!map.checkSegment(seg)) {
				continue;
			}

			goalNode = new TreeNode(goal);
			newNode.addChild(goalNode);
			break;
		}

		TreeNode curNode = goalNode;
		Boolean pathcomplete = false;
		path = new LinkedList<Point>();
		while (!pathcomplete) {
			path.add(0, curNode.loc);
			if (curNode == rrt.root) {
				pathcomplete = true;
				break;
			}
			curNode = curNode.parent;
		}
		map.path = path;
		nextWaypoint = path.get(0);
	}
}
