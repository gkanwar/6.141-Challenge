package core;

import java.io.IOException;
import java.text.ParseException;

import logging.Log;
import logging.RobotGraph;
import map.Map;
import map.ParseMap;
import control.Control;
import data_collection.DataCollection;
import orc.Orc;
import rrt.PathPlanning;
import state_machine.StateMachine;
import uORCInterface.OrcController;

public class Overlord extends Thread {

	OrcController orcControl;
	Orc orc;

	DataCollection dc;
	StateEstimator se;
	StateMachine sm;
	PathPlanning pp;
	Control c;

	Log l;
	private RobotGraph f;
	private long startTime;

	public Overlord() {
		try {
			Map m = Map.getInstance();

			m.setMap(ParseMap.parseFile("construction_map_2013.txt"));

			orcControl = new OrcController(new int[] { 0, 1 });
			orc = Orc.makeOrc();
			dc = DataCollection.getInstance();
			se = StateEstimator.getInstance();
			se.numBlocksLeft = m.getBlocks().size();
			sm = StateMachine.getInstance();

			pp = PathPlanning.getInstance();
			c = Control.getInstance();

			f = new RobotGraph(m);

			l = Log.getInstance(f);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		while (true) {
			System.out.println("justed updated");
			startTime = System.currentTimeMillis();
			dc.step();

			dc.log();
			se.step();
			l.updatePose();

			sm.step();

			pp.step();
			c.step();

			f.repaint();
			
			try {Thread.sleep(50-(System.currentTimeMillis()-startTime));} catch (Exception e){};
		}
	}

	public static void main(String[] args) {
		System.out.println("Main");

		Overlord me = new Overlord();
		System.out.println("About to start");
		me.start();
	}
}
