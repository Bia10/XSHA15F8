package realm;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

import common.Ancestra;
import common.Console;
import common.Console.ConsoleColorEnum;

public class RealmServer implements Runnable{

	private ServerSocket _SS;
	private Thread _t;
	
	public static int _totalNonAbo = 0;//Total de connections non abo
	public static int _totalAbo = 0;//Total de connections abo
	public static int _queueID = -1;//Numéro de la queue
	public static int _subscribe = 1;//File des non abonnées (0) ou abonnées (1)
	public static ConcurrentHashMap<String, ThreadsGroup> _ThreadsGroups = new ConcurrentHashMap<String, ThreadsGroup>();

    public class ThreadsGroup {

        private String ip;
        private ArrayList<RealmThread> _threads = new ArrayList<RealmThread>();
        private long lastConnexion;
        private int avertissment;

        public ThreadsGroup(final String ip, final RealmThread newR) {
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

        public void addThread(final RealmThread t) {
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
            boolean can = this.getSize() <= 8 && diff > 750;
            if (diff <= 750) {
                avertissment++;
            }
            if (avertissment > 15) {
                synchronized (_threads) {
                    for (RealmThread t : _threads) {
                        if (t == null) {
                            continue;
                        }
                        t.kick();//On deco les bannis
                    }
                    Console.println("(AntiDOS)BANLOG: IP[" + this.ip + "] Banned for current server execution.", ConsoleColorEnum.RED);
                }
                return false;
            }
            this.updateLastConnexion();
            return can;
        }

        public void delThread(final RealmThread t) {
            if (_threads.contains(t)) {
                _threads.remove(t);
                _threads.trimToSize();
            }
        }
    }
    
	public RealmServer()
	{
		try {
			_SS = new ServerSocket(Ancestra.CONFIG_REALM_PORT);
			_t = new Thread(this);
			_t.setDaemon(true);
			_t.start();
		} catch (IOException e) {
			addToLog("IOException: "+e.getMessage());
			Ancestra.closeServers();
		}
		
	}

	public void run() {
        while (Ancestra.isRunning)//bloque sur _SS.accept()
        {
            try {
                if (_SS.isClosed()) {
                    _SS = new ServerSocket(Ancestra.CONFIG_REALM_PORT);
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
                            TGroup.addThread(new RealmThread(ip, SAccepted));
                        }
                    } else {
                        SAccepted.setTcpNoDelay(true);
                        ThreadsGroup TGroup = new ThreadsGroup(ip, new RealmThread(ip, SAccepted));
                        _ThreadsGroups.put(ip, TGroup);
                    }
                } catch (Exception e) {
                }
            } catch (IOException e) {
                addToLog("IOException: " + e.getMessage());
                try {
                    addToLog("Fermeture du serveur de connexion");
                    if (!_SS.isClosed()) {
                        _SS.close();
                    }
                } catch (IOException e1) {
                    System.exit(0);
                }
            }
        }
    }
	
	public void kickAll()
	{
		try {
			_SS.close();
		} catch (IOException e) {}
	}
	public synchronized static void addToLog(String str)
	{
		System.out.println(str);
		if(Ancestra.canLog)
		{
			try {
				String date = Calendar.HOUR_OF_DAY+":"+Calendar.MINUTE+":"+Calendar.SECOND;
				Ancestra.Log_Realm.write(date+": "+str);
				Ancestra.Log_Realm.newLine();
				Ancestra.Log_Realm.flush();
			} catch (IOException e) {}//ne devrait pas avoir lieu
		}
	}
	
	public synchronized static void addToSockLog(String str)
	{
		if(Ancestra.CONFIG_DEBUG)System.out.println(str);
		if(Ancestra.canLog)
		{
			try {
				String date = Calendar.HOUR_OF_DAY+":"+Calendar.MINUTE+":"+Calendar.SECOND;
				Ancestra.Log_RealmSock.write(date+": "+str);
				Ancestra.Log_RealmSock.newLine();
				Ancestra.Log_RealmSock.flush();
			} catch (IOException e) {}//ne devrait pas avoir lieu
		}
	}

	public Thread getThread()
	{
		return _t;
	}
}
