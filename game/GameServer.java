package game;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import objects.Compte;
import objects.Personnage;

import common.*;
import common.Console.ConsoleColorEnum;

public class GameServer implements Runnable {
    private ServerSocket _SS;
    private Thread _t;
    private ArrayList<GameThread> _clients = new ArrayList<GameThread>();
    private ArrayList<Compte> _waitings = new ArrayList<Compte>();
    private Timer _saveTimer;
    public static GameServer gameServer;
    private Timer _loadActionTimer;
    private Timer _reloadMobTimer;
    private Timer _lastPacketTimer;
    private long _startTime;
    private int _maxPlayer = 0;
    private Timer _loadSaveTimer;
    private Timer _loadPubTimer; // Pub 
    private Timer _ExiTimer; // Reboot automatique
    private Timer _PopUpTimer; //Pop-up
    private Timer _PopUpTimerFlood; //Popup flood ||  On donne quand même un timer si ça échoue, même si ça ne devrait jamais arriver :)
    private Timer _PopUpTimerFloodKick; //Kick popup flood
    public static ConcurrentHashMap<String, ThreadsGroup> _ThreadsGroups = new ConcurrentHashMap<String, ThreadsGroup>();
    public class ThreadsGroup {

        private String ip;
        private long lastConnexion;
        private int avertissment;
        private ArrayList<GameThread> _threads = new ArrayList<GameThread>();

        public ThreadsGroup(final String ip, final GameThread newR) {
            this.ip = ip;
            this.lastConnexion = System.currentTimeMillis();
            if (newR.getHostAdress().equals(ip)) {
                _threads.add(newR);
            }
        }

        public void updateLastConnexion() {
            this.lastConnexion = System.currentTimeMillis();
        }

        public int getSize() {
            return _threads.size();
        }

        public void addThread(final GameThread t) {
            if (!t.getHostAdress().equals(ip)) {
                return;
            }
            if (!_threads.contains(t)) {
                _threads.add(t);
                _threads.trimToSize();
            }
        }

        public boolean canConnect() {
        	if(ip.equals("127.0.0.1"))return true;
            if (avertissment > 15) {
                return false;
            }
            long diff = System.currentTimeMillis() - lastConnexion;
            boolean can = this.getSize() <= 8 && diff > 500;
            if (diff <= 500) {
                avertissment++;
            }
            if (avertissment > 15) {
                synchronized (_threads) {
                    for (GameThread t : _threads) {
                        if (t == null) {
                            continue;
                        }
                        t.closeSocket();//On deco les bannis
                    }
                    Console.println("(AntiDOS)BANLOG: IP[" + this.ip + "] Banned for current server execution.", ConsoleColorEnum.RED);
                }
                return false;
            }
            this.updateLastConnexion();
            return can;
        }

        public void delThread(final GameThread t) {
            if (_threads.contains(t)) {
                _threads.remove(t);
                _threads.trimToSize();
            }
        }
    }
    
    
    public GameServer(String Ip) {
        try {
            _saveTimer = new Timer();
            _saveTimer.schedule(new TimerTask() {

                public void run() {
                    if (!Ancestra.isSaving) {
                        Thread t = new Thread(new SaveThread());
                        t.start();
                    }
                }
            }, Ancestra.CONFIG_SAVE_TIME, Ancestra.CONFIG_SAVE_TIME);

            // TODO : Systéme de pub
            if (Ancestra.CONFIG_PUB == true) {
                _loadPubTimer = new Timer();
                _loadPubTimer.schedule(new TimerTask() {

                    public void run() {
                        int rand = Formulas.getRandomValue(1, 3);
                        switch (rand) {
                            case 1:
                                SocketManager.GAME_SEND_MESSAGE_TO_ALL(Ancestra.PUB1, Ancestra.CONFIG_COLOR_BLEU);
                                break;
                            case 2:
                                SocketManager.GAME_SEND_MESSAGE_TO_ALL(Ancestra.PUB2, Ancestra.CONFIG_COLOR_BLEU);
                                break;
                            case 3:
                                SocketManager.GAME_SEND_MESSAGE_TO_ALL(Ancestra.PUB3, Ancestra.CONFIG_COLOR_BLEU);
                                break;
                        }


                    }
                }, Ancestra.CONFIG_LOAD_PUB, Ancestra.CONFIG_LOAD_PUB);
            }

            // Système de sauvegarde automatique
            _loadSaveTimer = new Timer();
            _loadSaveTimer.schedule(new TimerTask() {

                public void run() {
                    SocketManager.GAME_SEND_MESSAGE_TO_ALL("Une sauvegarde du serveur est en cours... Vous pouvez continuer de jouer, mais l'accès au serveur est temporairement bloqué. La connexion sera de nouveau disponible d'ici quelques instants. Merci de votre patience.", Ancestra.CONFIG_MOTD_COLOR);
                    World.saveAll(null);
                    SocketManager.GAME_SEND_MESSAGE_TO_ALL("La sauvegarde du serveur est terminée. L'accès au serveur est de nouveau disponible. Merci de votre compréhension.", Ancestra.CONFIG_MOTD_COLOR);
                }
            }, Ancestra.CONFIG_LOAD_SAVE, Ancestra.CONFIG_LOAD_SAVE);

            _loadActionTimer = new Timer();
            _loadActionTimer.schedule(new TimerTask() {

                public void run() {
                    SQLManager.LOAD_ACTION();
                    GameServer.addToLog("Les live actions ont ete appliquees");
                }
            }, Ancestra.CONFIG_LOAD_DELAY, Ancestra.CONFIG_LOAD_DELAY);

            //TODO: Reboot automatique ACTION + PREVIEW by Return
            /**
             * Annoncement*
             */
            _ExiTimer = new Timer();
            _ExiTimer.schedule(new TimerTask() {

                public void run() {
                    Thread t = new Thread(new SaveThread());
                    t.start();
                    SocketManager.PACKET_POPUP_ALERTE("Serveur: </b>Le reboot automatique va avoir lieu dans 1 minute.<br />Sauvegardez votre personnage en .save !");
                    return;
                }
            }, Ancestra.CONFIG_TIME_REBOOT - 60000, Ancestra.CONFIG_TIME_REBOOT - 60000);

            /**
             * Action*
             */
            _ExiTimer = new Timer();
            _ExiTimer.schedule(new TimerTask() {

                public void run() {
                    System.exit(0);
                }
            }, Ancestra.CONFIG_TIME_REBOOT, Ancestra.CONFIG_TIME_REBOOT);
            //TODO: Fin reboot automatique ACTION + PREVIEW
            
            //TODO: PopUp's by Return 
            
            if (Ancestra.CONFIG_ACTIVER_POPUP_VOTE) {
            	if(Ancestra.CONFIG_ACTIVER_POPUP_VOTE_DECO);
             	{
	            	_PopUpTimerFloodKick = new Timer();
	                _PopUpTimerFloodKick.schedule(new TimerTask() {
	                	
	                    public void run() {
	                    	long currTime = System.currentTimeMillis();
	                        for (Personnage P : World.getOnlinePersos()) {
	                        	if (P!=null && P.get_compte() != null && P.isOnline())
	                        	{
		                            if(P.whenKick4nullVote > 0 && P.whenKick4nullVote <= currTime) 
		                            {
		                            	P.whenKick4nullVote = -1;
		                            	P.get_compte().getGameThread().kick();
		                            }
	                        	}
	                        }
	                    }
	                }, 1000 , 1000);
	               
                    _PopUpTimerFlood = new Timer();
                    _PopUpTimerFlood.schedule(new TimerTask() {
                     public void run() {
                        	long currTime = System.currentTimeMillis();
                            for (Personnage P : World.getOnlinePersos()) {
                                		if (P.whenKick4nullVote <= 0 && P.get_compte() != null&&P.get_compte().get_vote() == 0 &&P.isOnline())
                                    	{
                                			SocketManager.PACKET_POPUP_VOTE(P, "Le vote est obligatoire !<br />"
                                					+ "\n Vous n'avez effectué aucun vote"
                                					+ "\n Cliquez <a href='" + Ancestra.CONFIG_LINK_VOTE + "'>ici</a> pour voter<br />"
                                					+ "\n!! Attention !!"
                                					+ "\nDéconnexion dans 10 secondes");
                                			P.whenKick4nullVote = currTime + 10*1000;//IL a recu, on  peut kick dans 10 secs
                                	
                                    	}
                                	
                                	
                                	}
                            
                            }
                     }, 18000, 18000);
             	}
            	_PopUpTimer = new Timer();//Popup chaque Ancestra.CONFIG_TIME_POPUP_VOTE pour demander de voter.
                _PopUpTimer.schedule(new TimerTask() {
                         public void run() {
                             for (Personnage P : World.getOnlinePersos()) {
                             	if (P.isOnline() && P.get_compte().get_vote() > 0)
                             	{
                                 		SocketManager.PACKET_POPUP_VOTE(P, "Le vote est obligatoire chaque "+Ancestra.CONFIG_TIME_POPUP_VOTE+"h00 !<br />"
                                             + "\n Votre nombre de votes: " + P.get_compte().get_vote() + " votes"
                                             + "\n Votre nombre de points: " + P.get_compte().get_points() + " points"
                                             + "\n Cliquez <a href='" + Ancestra.CONFIG_LINK_VOTE + "'>ici</a> pour voter<br />"
                                             + "\n!! Attention !!"
                                             + "\nLes comptes sans avoir voter sont bannis");
                             	}
                             }
                         }
                     }, Ancestra.CONFIG_TIME_POPUP_VOTE*1000*60*60, Ancestra.CONFIG_TIME_POPUP_VOTE*1000*60*60);
            }
                
            _reloadMobTimer = new Timer();
            _reloadMobTimer.schedule(new TimerTask() {

                public void run() {
                    World.RefreshAllMob();
                    GameServer.addToLog("La recharge des mobs est finie");
                }
            }, Ancestra.CONFIG_RELOAD_MOB_DELAY, Ancestra.CONFIG_RELOAD_MOB_DELAY);

            _lastPacketTimer = new Timer();
            _lastPacketTimer.schedule(new TimerTask() {

                public void run() {
                    for (Personnage perso : World.getOnlinePersos()) {
                        if (perso.getLastPacketTime() + Ancestra.CONFIG_MAX_IDLE_TIME < System.currentTimeMillis()) {

                            if (perso != null && perso.get_compte().getGameThread() != null && perso.isOnline()) {
                                GameServer.addToLog("Kick pour inactiviter de : " + perso.get_name());
                                SocketManager.REALM_SEND_MESSAGE(perso.get_compte().getGameThread().get_out(), "01|");
                                perso.get_compte().getGameThread().closeSocket();
                            }
                        }

                    }
                }
            }, 60000, 60000);

            _SS = new ServerSocket(Ancestra.CONFIG_GAME_PORT);
            if (Ancestra.CONFIG_USE_IP) {
                Ancestra.GAMESERVER_IP = CryptManager.CryptIP(Ip) + CryptManager.CryptPort(Ancestra.CONFIG_GAME_PORT);
            }
            _startTime = System.currentTimeMillis();
            _t = new Thread(this);
            _t.start();
        } catch (IOException e) {
            addToLog("IOException: " + e.getMessage());
            Ancestra.closeServers();
        }
    }

    public static class SaveThread implements Runnable {

        public void run() {
            if (!Ancestra.isSaving) {
                SocketManager.GAME_SEND_Im_PACKET_TO_ALL("1164");
                World.saveAll(null);
                SocketManager.GAME_SEND_Im_PACKET_TO_ALL("1165");
            }
        }
    }

    public ArrayList<GameThread> getClients() {
        return _clients;
    }

    public long getStartTime() {
        return _startTime;
    }

    public int getMaxPlayer() {
        return _maxPlayer;
    }

    public int getPlayerNumber() {
        return _clients.size();
    }

    public void run() {
        while (Ancestra.isRunning)//bloque sur _SS.accept()
        {
            try {
                if (_SS.isClosed()) {
                    _SS = new ServerSocket(Ancestra.CONFIG_GAME_PORT);
                }
                Socket SAccepted = _SS.accept();
                try {
                    String ip = SAccepted.getInetAddress().getHostAddress();
                    if (_ThreadsGroups.containsKey(ip)) {
                        ThreadsGroup TGroup = _ThreadsGroups.get(ip);
                        if (!TGroup.canConnect()) {
                            SAccepted.close();
                            continue;
                        } else {
                            SAccepted.setTcpNoDelay(true);
                            GameThread Gt = new GameThread(ip, SAccepted);
                            TGroup.addThread(Gt);
                            _clients.add(Gt);
                            if (_clients.size() > _maxPlayer) {
                                _maxPlayer = _clients.size();
                            }
                        }
                    } else {
                        SAccepted.setTcpNoDelay(true);
                        GameThread Gt = new GameThread(ip, SAccepted);
                        ThreadsGroup TGroup = new ThreadsGroup(ip, Gt);
                        _ThreadsGroups.put(ip, TGroup);
                        _clients.add(Gt);
                        if (_clients.size() > _maxPlayer) {
                            _maxPlayer = _clients.size();
                        }
                    }
                } catch (Exception e) {
                }

            } catch (IOException e) {
                addToLog("IOException: " + e.getMessage());
                try {
                    if (!_SS.isClosed()) {
                        _SS.close();
                    }
                    System.exit(0);
                } catch (IOException e1) {
                }
            }
        }
    }

    public void kickAll() {
        try {
            _SS.close();
        } catch (IOException e) {
        }
        //Copie
        ArrayList<GameThread> c = new ArrayList<GameThread>();
        c.addAll(_clients);
        for (GameThread GT : c) {
            try {
                GT.closeSocket();
            } catch (Exception e) {
            };
        }
    }

    public synchronized static void addToLog(String str) {
        Ancestra.printDebug(str);
        if (Ancestra.canLog) {
            try {
                String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(+Calendar.MINUTE) + ":" + Calendar.getInstance().get(Calendar.SECOND);
                Ancestra.Log_Game.write(date + ": " + str);
                Ancestra.Log_Game.newLine();
                Ancestra.Log_Game.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }//ne devrait pas avoir lieu
        }
    }

    public synchronized static void addToSockLog(String str) {
        if (Ancestra.CONFIG_DEBUG) {
            Ancestra.printDebug(str);
        }
        if (Ancestra.canLog) {
            try {
                String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(+Calendar.MINUTE) + ":" + Calendar.getInstance().get(Calendar.SECOND);
                Ancestra.Log_GameSock.write(date + ": " + str);
                Ancestra.Log_GameSock.newLine();
                Ancestra.Log_GameSock.flush();
            } catch (IOException e) {
            }//ne devrait pas avoir lieu
        }
    }

    public void delClient(GameThread gameThread) {
    	if (_clients.contains(gameThread)) {
            _clients.remove(gameThread);
        }
        if (_clients.size() > _maxPlayer) {
            _maxPlayer = _clients.size();
        }
        if (_ThreadsGroups.containsKey(gameThread.getHostAdress())) {
            _ThreadsGroups.get(gameThread.getHostAdress()).delThread(gameThread);
        }
        Ancestra.refreshTitle();
    }

    public synchronized Compte getWaitingCompte(int guid) {
        for (int i = 0; i < _waitings.size(); i++) {
            if (_waitings.get(i).get_GUID() == guid) {
                return _waitings.get(i);
            }
        }
        return null;
    }

    public synchronized void delWaitingCompte(Compte _compte) {
        _waitings.remove(_compte);
    }

    public synchronized void addWaitingCompte(Compte _compte) {
        _waitings.add(_compte);
    }

    public static String getServerTime() {
        Date actDate = new Date();
        return "BT" + (actDate.getTime() + 3600000);
    }

    public static String getServerDate() {
        Date actDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd");
        String jour = Integer.parseInt(dateFormat.format(actDate)) + "";
        while (jour.length() < 2) {
            jour = "0" + jour;
        }
        dateFormat = new SimpleDateFormat("MM");
        String mois = (Integer.parseInt(dateFormat.format(actDate)) - 1) + "";
        while (mois.length() < 2) {
            mois = "0" + mois;
        }
        dateFormat = new SimpleDateFormat("yyyy");
        String annee = (Integer.parseInt(dateFormat.format(actDate)) - 1370) + "";
        return "BD" + annee + "|" + mois + "|" + jour;
    }

    public Thread getThread() {
        return _t;
    }
    /*
     * public static class AllFightsTurns implements Runnable { Timer
     * _allFightsTurns; long _lastFightsTurns;
     *
     * public void run() { try { _allFightsTurns = new Timer();
     * _allFightsTurns.scheduleAtFixedRate(new TimerTask() { public void run() {
     * try { /*if (System.currentTimeMillis() - _lastFightsTurns > 3500L) {
     * SocketManager.GAME_SEND_cMK_PACKET_TO_ADMIN("@", 0, "DEBUG-TIC", "ERREUR
     * TIMER-LAG Dans: _allFightsTurns; " + (System.currentTimeMillis() -
     * _lastFightsTurns)); } //long t = System.currentTimeMillis();
     *
     * try { World.ticAllFightersTurns(); } catch (Exception e) {
     * SocketManager.GAME_SEND_cMK_PACKET_TO_ADMIN("@", 0, "DEBUG-TIC", "ERREUR
     * FATAL ------ No2 ---- (rar) Dans: ticAllFightersTurns(); " +
     * e.getMessage()); }
     *
     * /*if (System.currentTimeMillis() - t > 5000L) {
     * SocketManager.GAME_SEND_cMK_PACKET_TO_ADMIN("@", 0, "DEBUG-TIC", "LAG:
     * ticAllFightersTurns(); " + (System.currentTimeMillis() - t)); }
     *
     * // Ancestra.printDebug("---- Tic! " + (System.currentTimeMillis() -
     * _lastFightsTurns)); //_lastFightsTurns = System.currentTimeMillis(); }
     * catch (Exception e) { Ancestra.printDebug("--------------- ERROR! " +
     * e.getMessage()); return; } } } , 1500L, 1500L); } catch (Exception
     * localException) { } }
  }
     */
}
