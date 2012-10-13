package common;

import game.GameServer;

import java.util.TimerTask;

import objects.Fight;

public class temporizeFightTask extends TimerTask{
	private Fight f;
	
	public temporizeFightTask(Fight f)
	{
		this.f = f;
	}
	@Override
	public void run() {
		try{
			f.ticMyTimer();
			} catch (Exception e2) {
				//SocketManager.GAME_SEND_cMK_PACKET_TO_ADMIN("@", 0, "DEBUG-TIC-N2", "ERREUR FATALE !!! Inside ticAllFightersTurns().f.ticMyTimer(); " + e2.getMessage());
				//SocketManager.GAME_SEND_cMK_PACKET_TO_ADMIN("@", 0, "DEBUG-TIC-N2", ", mapID: " +f.get_map().get_id()); Osef de ça
				GameServer.addToLog("GameFighter: Bug Timer");
			}
	}
	
	public void stop()
	{
		super.cancel();
		this.f = null;
		try {
			this.finalize();
		} catch (Throwable e) {
		}
	} 

}
