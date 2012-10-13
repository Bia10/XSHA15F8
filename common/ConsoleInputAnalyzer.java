package common;
import game.GameServer.SaveThread;

import java.io.Console;


import common.Ancestra;
import common.SQLManager;


public class ConsoleInputAnalyzer implements Runnable{
	private Thread _t;

	public ConsoleInputAnalyzer()
	{
		this._t = new Thread(this);
		_t.setDaemon(true);
		_t.start();
	}
	
	@Override
	public void run() {
		while (Ancestra.isRunning){
			Console console = System.console();
		    String command = console.readLine();
		    try{
		    evalCommand(command);
		    }catch(Exception e){}
		    finally
		    {
		    	try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
		    }
		}
	}
	public void evalCommand(String command)
	{
		String[] args = command.split(" ");
		String fct =args[0].toUpperCase();
		if(fct.equals("SAVE"))
		{
			Thread t = new Thread(new SaveThread());
			t.start();
		}else
		if(fct.equals("EXIT"))
		{
			SQLManager.LOAD_ACTION();
			System.exit(0);
		}
		else
		if(fct.equals("CLS"))
		{
			Ancestra.ReOnemuStarted();
		}
		else
		if(fct.equals("TOOGLE_DEBUG"))
		{
			Ancestra.CONFIG_DEBUG = !Ancestra.CONFIG_DEBUG;
			if(Ancestra.CONFIG_DEBUG)
			{
				sendInfo("Debug active!");
			}else
			{
				sendInfo("Debug desactive!");
			}
		}
		else
		if(fct.equals("TOOGLE_LOG"))
		{
			Ancestra.canLog = !Ancestra.canLog;
			if(Ancestra.canLog)
			{
				sendInfo("Log active!");
			}else
			{
				sendInfo("Log desactive!");
			}
		}else
		if(fct.equalsIgnoreCase("ANNOUNCE"))
		{	
				String announce = command.substring(9);
				String PrefixConsole = "<b>Serveur</b> : ";
				SocketManager.GAME_SEND_MESSAGE_TO_ALL(PrefixConsole+announce, Ancestra.CONFIG_MOTD_COLOR);
				sendEcho("<Announce:> "+announce);
		}else
		if(fct.equals("?")||command.equals("HELP"))
		{
			sendInfo("------------Commandes:------------");
			sendInfo("- SAVE pour sauvegarder le serveur.");
			sendInfo("- EXIT pour fermer le serveur.");
			sendInfo("- TOOGLE_DEBUG pour activer/desactiver le mode debug.");
			sendInfo("- TOOGLE_LOG pour activer/desactiver le systeme de logs");
			sendInfo("- INFOS pour afficher les informations comme en jeu.");
			sendInfo("- CLS pour nettoyer la console.");
			sendInfo("- ANNOUNCE pour envoyer un message aux joueurs.");
			sendInfo("- Touches CTRL+C pour stop le serveur.");
			sendInfo("- HELP ou ? pour afficher cette liste.");
			sendInfo("----------------------------------");
		}else
		if(fct.equals("INFOS"))
		{
			long uptime = System.currentTimeMillis() - Ancestra.gameServer.getStartTime();
			int jour = (int) (uptime/(1000*3600*24));
			uptime %= (1000*3600*24);
			int hour = (int) (uptime/(1000*3600));
			uptime %= (1000*3600);
			int min = (int) (uptime/(1000*60));
			uptime %= (1000*60);
			int sec = (int) (uptime/(1000));
			
			String mess =	"===========\n"+Ancestra.Version()
					+			"OnEmu by Return\n"
					+			"Uptime: "+jour+"d "+hour+"h "+min+"m "+sec+"s\n"
					+			"Joueurs En Ligne: "+Ancestra.gameServer.getPlayerNumber()+"\n"
					+			"Record de Connexions: "+Ancestra.gameServer.getMaxPlayer()+"\n"
					+			"===========";		
			sendInfo(mess);
		}
		else
		if(fct.equals("ECHO"))
		{
				try{
					String message = command.substring(5);
					sendEcho(message);
				}catch(Exception e)
				{}
		}else
		{
			sendError("Commande non reconnue ou incomplete.");
		}
	}

	public static void sendInfo(String msg)
	{
		common.Console.println(msg, common.Console.ConsoleColorEnum.GREEN);
	}
	public static void sendError(String msg)
	{
		common.Console.println(msg, common.Console.ConsoleColorEnum.RED);
	}
	public static void send(String msg)
	{
		common.Console.println(msg);
	}
	public static void sendDebug(String msg)
	{
		if(Ancestra.CONFIG_DEBUG)common.Console.println(msg, common.Console.ConsoleColorEnum.YELLOW);
	}
	public static void sendEcho(String msg)
	{
		common.Console.println(msg, common.Console.ConsoleColorEnum.BLUE);
	}
	
}
