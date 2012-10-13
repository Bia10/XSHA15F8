package common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import objects.Fight;

@SuppressWarnings("unused")
public class fightTemporizer {
	private Timer _myTemporizer;
	private temporizeFightTask temporizeFightTask;
	
	public fightTemporizer(Fight f)
	{
		this._myTemporizer = new Timer();
		this.temporizeFightTask = new temporizeFightTask(f);
		_myTemporizer.schedule(temporizeFightTask, 1500, 1500);
	}
	public void stop() {
        temporizeFightTask.stop();
        _myTemporizer.purge();
        _myTemporizer.cancel();
        this._myTemporizer = null;
        this.temporizeFightTask = null;
    }
}

