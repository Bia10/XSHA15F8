package common;

import game.GameServer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;

import common.Console.ConsoleColorEnum;

import realm.RealmServer;

public class Ancestra {
	
	private static final String CONFIG_FILE = "OnEmuR.ini";
	public static String IP = "127.0.0.1";
	public static boolean isInit = false;
	public static String DB_HOST;
	public static String DB_USER;
	public static String DB_PASS;
	public static String STATIC_DB_NAME;
	public static String OTHER_DB_NAME;
	public static long FLOOD_TIME = 60000;
	public static String GAMESERVER_IP;
	public static String CONFIG_MOTD = "";
	public static String accept = "dcba";
	public static String CONFIG_MOTD_COLOR = "";
	public static boolean CONFIG_DEBUG = false;
	public static PrintStream PS;
	public static boolean CONFIG_POLICY = false;
	public static int CONFIG_REALM_PORT = 443;
	public static int CONFIG_GAME_PORT 	= 5555;
	public static int CONFIG_MAX_PERSOS = 5;
	public static short CONFIG_START_MAP = 10111;
	public static int CONFIG_START_CELL = 314;
	public static boolean CONFIG_ALLOW_MULTI = false;
	public static int CONFIG_START_LEVEL = 1;
	public static int CONFIG_START_KAMAS = 0;
	public static int CONFIG_SAVE_TIME = 10*60*10000;
	public static int CONFIG_DROP = 1;
	public static boolean CONFIG_ZAAP = false;
	public static int CONFIG_LOAD_DELAY = 60000;
	public static int CONFIG_RELOAD_MOB_DELAY = 18000000;
	public static int CONFIG_PLAYER_LIMIT = 30;
	public static boolean CONFIG_IP_LOOPBACK = true;
	public static int XP_PVP = 1;
	public static int LVL_PVP = 15;
	public static boolean ALLOW_MULE_PVP = false;
	public static int XP_PVM = 1;
	public static int KAMAS = 1;
	public static int HONOR = 1;
	public static int XP_METIER = 1;
	public static boolean CONFIG_CUSTOM_STARTMAP;
	public static boolean CONFIG_USE_MOBS = false;
	public static boolean CONFIG_USE_IP = false;
	public static GameServer gameServer;
	public static RealmServer realmServer;
	public static boolean isRunning = false;
	public static BufferedWriter Log_GameSock;
	public static BufferedWriter Log_Game;
	public static BufferedWriter Log_Realm;
	public static BufferedWriter Log_MJ;
	public static BufferedWriter Log_RealmSock;
	public static BufferedWriter Log_Shop;
	public static boolean canLog;
	public static boolean isSaving = false;
	public static boolean AURA_SYSTEM = false;
	public static long FLOOD_TIME_ALL = 10000L;
	public static ArrayList<Integer> arenaMap = new ArrayList<Integer>(8);
	public static int CONFIG_ARENA_TIMER = 10*60*1000;// 10 minutes
	public static int CONFIG_DB_COMMIT = 30*1000;
	public static int CONFIG_MAX_IDLE_TIME = 1800000;//En millisecondes
	public static ArrayList<Integer> NOTINHDV = new ArrayList<Integer>();
	public static boolean CONFIG_SOCKET_USE_COMPACT_DATA = false;
	public static int CONFIG_SOCKET_TIME_COMPACT_DATA = 200;
	public static long CONFIG_MS_PER_TURN = 30000;//Maintenant
	public static long CONFIG_MS_PER_TURN_MOB = 30000;//On laisse pour le moment
	public static long CONFIG_MS_FOR_START_FIGHT = 45000;
	public static boolean CONFIG_TAILLE_VAR = false;
	public static int CONFIG_LOAD_SAVE = 6800;
	public static int CONFIG_TIME_REBOOT = 10800000; //3h00 par d�fault en millisecondes. 3 600 000 = 1H
	public static String CONFIG_MESSAGE_BIENVENUE = "";
	
	
	//TODO: Variables ajout�s |By Return alias Porky|
	//Teleportation dans la faction de l'ennemi si aggression
	public static String killedByFaction_BONTA = "mapid,cellID";
	public static String killedByFaction_BRAK = "mapid,cellID";
	public static String killedByFaction_SERIANE = "mapid,cellID";
	public static boolean ALLOW_TELEPORT_ENEMY_FACTION;
	
	//Commandes joueur 
	public static short MAP_BOSS = 12300;
	public static int CELL_BOSS = 404;
	public static short CONFIG_MAP_VIP = 15054;
	public static int CONFIG_CELL_VIP = 543;
	public static short CONFIG_MAP_SHOP = 10298;
	public static int CONFIG_CELL_SHOP = 314;
	public static short CONFIG_MAP_PVP = 10298;
	public static int CONFIG_CELL_PVP = 314;
	public static short CONFIG_MAP_PVM = 10298;
	public static int CONFIG_CELL_PVM = 314;
	public static short CONFIG_MAP_ENCLOS = 10298;
	public static int CONFIG_CELL_ENCLOS = 314;
	public static short CONFIG_MAP_EVENT = 10298;
	public static int CONFIG_CELL_EVENT = 314;
	//Options activables/d�sactivables 
	public static boolean CONFIG_ACTIV_COMMAND_GUILD = false;
	public static boolean CONFIG_ACTIV_COMANDESEMIFUN = false;
	public static boolean CONFIG_ACTIV_ENERGIE = false;
	public static boolean CONFIG_ACTIV_ETOILE = false;
	public static boolean CONFIG_ACTIV_MAJ = false;
	public static boolean CONFIG_ACTIV_MJ = false;
	public static boolean CONFIG_ACTIV_VIPCOMMANDS = false;
	public static boolean CONFIG_ACTIV_FUNCOMMANDS = false;
	public static boolean CONFIG_ACTIV_SEMIFUNCOMMANDS = false;
	public static boolean CONFIG_ACTIVER_NOUVEAUX_SORTS = false;
	public static boolean ACTIVER_COMMANDE_SPELLMAX = false;
	public static boolean ACTIVER_CINEMATIQUE = false;
	//Variables STATS des montures sp�ciales
	public static int CONFIG_HURLEDENT_STATS_TYPE = 10;
	public static double CONFIG_HURLEDENT_STATS = 1;
	public static int CONFIG_MONTILIER_STATS_TYPE = 138;
	public static double CONFIG_MONTILIER_STATS = 2;
	public static int CONFIG_SQUELETTE_STATS_TYPE = 125;
	public static double CONFIG_SQUELETTE_STATS = 6;
	public static boolean CONFIG_ACTIV_HURLEDENT_STATS2 = false;
	public static int CONFIG_HURLEDENT_STATS_TYPE2 = 10;
	public static double CONFIG_HURLEDENT_STATS2 = 1.25;
	public static boolean CONFIG_ACTIV_HURLEDENT_STATS3 = false;
	public static int CONFIG_HURLEDENT_STATS_TYPE3 = 10;
	public static int CONFIG_HURLEDENT_STATS3 = 1;
	public static boolean CONFIG_ACTIV_MONTILIER_STATS2 = false;
	public static int CONFIG_MONTILIER_STATS_TYPE2 = 10;
	public static double CONFIG_MONTILIER_STATS2 = 1.25;
	public static boolean CONFIG_ACTIV_MONTILIER_STATS3 = false;
	public static int CONFIG_MONTILIER_STATS_TYPE3 = 10;
	public static int CONFIG_MONTILIER_STATS3 = 1;
	public static boolean CONFIG_ACTIV_SQUELETTE_STATS2 = false;
	public static int CONFIG_SQUELETTE_STATS_TYPE2 = 10;
	public static double CONFIG_SQUELETTE_STATS2 = 1.25;
	public static boolean CONFIG_ACTIV_SQUELETTE_STATS3 = false;
	public static int CONFIG_SQUELETTE_STATS_TYPE3 = 10;
	public static int CONFIG_SQUELETTE_STATS3 = 1;
	public static boolean CONFIG_ACTIV_MONTURE_STATS = false;
	public static int CONFIG_MONTURE_STATS_TYPE = 10;
	public static int CONFIG_MONTURE_STATS = 1;
	public static boolean CONFIG_ACTIV_MONTURE_STATS2 = false;
	public static int CONFIG_MONTURE_STATS_TYPE2 = 10;
	public static int CONFIG_MONTURE_STATS2 = 1;
	public static boolean CONFIG_ACTIV_MONTURE_STATS3 = false;
	public static int CONFIG_MONTURE_STATS_TYPE3 = 10;
	public static int CONFIG_MONTURE_STATS3 = 1;
	//Percepteurs
	public static int MORPH_PERCEPTEUR = 6000;
	public static boolean ACTIVER_VIE_PAR_LEVEL = false;
	public static int VIE_PERCEPTEUR = 100;
	public static int CONFIG_LEVEL_MAX_PERCO = 200;
	//Autres
	public static int CONFIG_PRIX_FMCAC = 500000;
	public static int CONFIG_PM_DEPART = 3;
	public static int CONFIG_PA_DEPART = 6;
	public static int CONFIG_PA_DEBUG = 7;
	public static int CONFIG_INITIATIVE_DEPART = 1000;
	public static int CONFIG_INVOCATION_DEPART = 1;
	public static int CONFIG_PODS_DEPART = 1000;
	public static int CONFIG_DEPART_PP_ENU = 120;
	public static int CONFIG_CARACTERISTIQUES_PAR_LEVEL = 5;
	public static int CONFIG_POINTS_SORT_PAR_LEVEL = 1;
	public static int CONFIG_VITALITE_PAR_LEVEL = 5;
	public static boolean CONFIG_ACTIV_GAGNER_PA = false;
	public static int CONFIG_LEVEL_POUR_GAGNER_PA = 100;
	public static int CONFIG_NOMBRE_PA = 1;
	public static boolean CONFIG_ACTIV_GAGNER_PA2 = false;
	public static int CONFIG_LEVEL_POUR_GAGNER_PA2 = 200;
	public static int CONFIG_NOMBRE_PA2 = 1;
	public static boolean CONFIG_ACTIV_GAGNER_PM = false;
	public static int CONFIG_LEVEL_POUR_GAGNER_PM = 100;
	public static int CONFIG_NOMBRE_PM = 1;
	public static boolean CONFIG_ACTIV_GAGNER_PM2 = false;
	public static int CONFIG_LEVEL_POUR_GAGNER_PM2 = 200;
	public static int CONFIG_NOMBRE_PM2 = 1;
	public static boolean CONFIG_ACTIV_NOUVEAU_SORT = false;
	public static int CONFIG_LEVEL_NOUVEAU_SORT = 200;
	public static int CONFIG_ID_NOUVEAU_SORT = 415;
	public static int CONFIG_SECONDS_FOR_BONUS = 60; 
	public static int CONFIG_BONUS_MAX = 400;
	public static int CONFIG_MORPHID_SKIN = 300;
	//Variables des traques 
	public static int CONFIG_XPTRAQUE_50 = 10000;
	public static int CONFIG_XPTRAQUE_60 = 65000;
	public static int CONFIG_XPTRAQUE_70 = 90000;
	public static int CONFIG_XPTRAQUE_80 = 120000;
	public static int CONFIG_XPTRAQUE_90 = 160000;
	public static int CONFIG_XPTRAQUE_100 = 210000;
	public static int CONFIG_XPTRAQUE_110 = 270000;
	public static int CONFIG_XPTRAQUE_120 = 350000;
	public static int CONFIG_XPTRAQUE_130 = 440000;
	public static int CONFIG_XPTRAQUE_140 = 540000;
	public static int CONFIG_XPTRAQUE_150 = 650000;
	public static int CONFIG_XPTRAQUE_155 = 760000;
	public static int CONFIG_XPTRAQUE_160 = 880000;
	public static int CONFIG_XPTRAQUE_165 = 1000000;
	public static int CONFIG_XPTRAQUE_170 = 1130000;
	public static int CONFIG_XPTRAQUE_175 = 1300000;
	public static int CONFIG_XPTRAQUE_180 = 1500000;
	public static int CONFIG_XPTRAQUE_185 = 1700000;
	public static int CONFIG_XPTRAQUE_190 = 2000000;
	public static int CONFIG_XPTRAQUE_195 = 2500000;
	public static int CONFIG_XPTRAQUE_200 = 3000000;
	public static int CONFIG_XPTRAQUE_10000 = 5000000;
	//Couleurs
	public static String CONFIG_COLOR_GLOBAL = "FF0000";
	public static String COLOR_BLEU = "3366FF";
	public static String COLOR_BLEU2 = "0561dd";
	public static String COLOR_VERT = "0d8b0d";
	public static String COLOR_RED = "FF0000";
	//Pubs
	public static int CONFIG_LOAD_PUB = 60000;
    public static String PUB1 = "";
	public static String PUB2 = "";
	public static String PUB3 = "";
	public static String CONFIG_COLOR_BLEU = "3366FF";
	public static boolean CONFIG_PUB = false;
	//Nouvelles montures
	public static int CONFIG_MONTILIER_ID = 80000;
	public static int CONFIG_HURLEDENT_ID = 81000;
	public static int CONFIG_MONTURE_ID = 82000;
	public static int CONFIG_SQUELETTE_ID = 7865;
	//Variables classe zobale ++ Caract�ristiques 2.0
	public static int CONFIG_LEVEL_REQUIERT_ZOBAL = 100;
	public static boolean CONFIG_ACTIV_ZOBAL = false;
	public static boolean CONFIG_ACTIVER_STATS_2 = false;
	//Variables des m�tiers
	public static boolean CONFIG_ACTIVER_METIER_REQUIS_FM = false;
	public static int CONFIG_LEVEL_REQUIS_FM = 65;
	//Xp en d�fi
	public static boolean CONFIG_XP_DEFI = false;
	public static int CONFIG_KAMASMIN = 101;
	public static int CONFIG_KAMASMAX = 10000;
	//Objets � la connexion du personnage (Par d�fault panoplie aventurier)
	public static boolean START_AVEC_PANO_BIENVENUE = false;
	public static int CONFIG_ITEM_BIENVENUE = 2473;
	public static int CONFIG_ITEM_BIENVENUE2 = 2474;
	public static int CONFIG_ITEM_BIENVENUE3 = 2475;
	public static int CONFIG_ITEM_BIENVENUE4 = 2476;
	public static int CONFIG_ITEM_BIENVENUE5 = 2477;
	public static int CONFIG_ITEM_BIENVENUE6 = 2478;
	public static int CONFIG_ITEM_BIENVENUE7 = 0;
	//Heroic Mode + Prismes & rates FM
	public static boolean MODE_HEROIC = false;
	public static ArrayList<Integer> mapasNoPrismas = new ArrayList<Integer>();
	public static boolean ACTIVER_COMMANDE_PRISME = false;
	public static boolean COMMANDE_PRISME_VIP = false;
	public static int LEVEL_REQUIS_COMMANDE_PRISME = 10000;
	public static int NOMBRE_COMMANDE_PRISME = 10;
	public static int PORC_FM = 100;
	//POPUP
	public static boolean CONFIG_ACTIVER_POPUP_VOTE = false;
	public static boolean CONFIG_ACTIVER_POPUP_VOTE_DECO = false;
	public static int CONFIG_TIME_POPUP_VOTE = 2; //En heures
	public static String CONFIG_LINK_VOTE = "http://asterionserveurs.ca/site/?p=vote";
	
	
	
	
	
	//Fin des variables |By Return alias Porky|
	
	public static void main(String[] args)
	{
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				Ancestra.closeServers();
			}
		}
		);
		Console.clear();//On vide le precedent.
		PrintStream ps;
        try {
            ps = new PrintStream(System.out, true, "IBM850");
            System.setOut(ps);
        } catch (Exception e) {
            System.out.println("Erreur de conversion du format des caracteres de la console DOS.");
        }
		Console.setTitle("Onemu Remake - Version 2.3.0 by Return - Chargement...");
		System.out.println("---------------------------------------\n\n");
		System.out.println(Version());
		System.out.println("---------------------------------------\n");
		System.out.println("Chargement du fichier de configuration :");
		loadConfiguration();
		isInit = true;
		System.out.println("Les donnee ont ete charges !");
		System.out.println("Connexion a la base de donnee :");
		if(SQLManager.setUpConnexion()) System.out.println("Connexion accepte !");
		else
		{
			System.out.println("Connexion invalide");
			Ancestra.closeServers();
			System.exit(0);
		}
		System.out.println("Creation du Monde :");
		long startTime = System.currentTimeMillis();
		World.createWorld();
		long endTime = System.currentTimeMillis();
		long differenceTime = (endTime - startTime)/1000;
		System.out.println("Monde termine ! en : "+differenceTime+" s");
		isRunning = true;
		System.out.println("Le serveur se lance sur le port "+CONFIG_GAME_PORT);
		String Ip = "";
		try
		{
			Ip = InetAddress.getLocalHost().getHostAddress();
		}catch(Exception e)
		{
			System.out.println(e.getMessage());
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {}
			System.exit(1);
		}
		Ip = IP;
		gameServer = new GameServer(Ip);
		Console.println("Lancement du serveur de Connexion sur le port : "+CONFIG_REALM_PORT, ConsoleColorEnum.BLUE);
		realmServer = new RealmServer();
		if(CONFIG_USE_IP)
			System.out.println("Ip du serveur "+IP+" crypt "+GAMESERVER_IP);
		Console.println("OnEmu en operation! En attente de connexion....", ConsoleColorEnum.GREEN);
		if(CONFIG_SOCKET_USE_COMPACT_DATA)
		{
			System.out.println("Lancement du FlushTimer");
			SendManager.FlushTimer().start();
			System.out.println("FlushTimer : Ok !");
		}
		refreshTitle();
		onOnemuStarted();
	}
	
	private static void loadConfiguration()
	{
		boolean log = false;
		try {
			BufferedReader config = new BufferedReader(new FileReader(CONFIG_FILE));
			String line = "";
			while ((line=config.readLine())!=null)
			{
				if(line.split("=").length == 1) continue ;
				String param = line.split("=")[0].trim();
				String value = line.split("=")[1].trim();
				if(param.equalsIgnoreCase("DEBUG"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						Ancestra.CONFIG_DEBUG = true;
						System.out.println("Mode Debug: On");
					}
				}else if(param.equalsIgnoreCase("SEND_POLICY"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						Ancestra.CONFIG_POLICY = true;
					}
				
				}else if(param.equalsIgnoreCase("SECONDS_PER_TURN"))
				{
					Ancestra.CONFIG_MS_PER_TURN = Integer.parseInt(value);
					if(Ancestra.CONFIG_MS_PER_TURN < 1 )
						Ancestra.CONFIG_MS_PER_TURN = 1;
					if(Ancestra.CONFIG_MS_PER_TURN > 300)
						Ancestra.CONFIG_MS_PER_TURN = 300;
					Ancestra.CONFIG_MS_PER_TURN *= 1000;
				}else if(param.equalsIgnoreCase("INDUNGEON_CHALLENGE"))
				{
					Ancestra.CONFIG_MS_FOR_START_FIGHT = Integer.parseInt(value);
					if(Ancestra.CONFIG_MS_FOR_START_FIGHT < 1 )
						Ancestra.CONFIG_MS_FOR_START_FIGHT = 1;
					if(Ancestra.CONFIG_MS_FOR_START_FIGHT > 300)
						Ancestra.CONFIG_MS_FOR_START_FIGHT = 300;
					Ancestra.CONFIG_MS_FOR_START_FIGHT *= 1000;
				}
					
				else if(param.equalsIgnoreCase("LOG"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						log = true;
					}
				}else if(param.equalsIgnoreCase("PERCO_TAILLE_VAR"))
				{
					if(value.equalsIgnoreCase("false"))
					{
						CONFIG_TAILLE_VAR = false;
					}
				}
				else if(param.equalsIgnoreCase("USE_CUSTOM_START"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						Ancestra.CONFIG_CUSTOM_STARTMAP = true;
					}
				}else if(param.equalsIgnoreCase("START_KAMAs"))
				{
					Ancestra.CONFIG_START_KAMAS = Integer.parseInt(value);
					if(Ancestra.CONFIG_START_KAMAS < 0 )
						Ancestra.CONFIG_START_KAMAS = 0;
					if(Ancestra.CONFIG_START_KAMAS > 1000000000)
						Ancestra.CONFIG_START_KAMAS = 1000000000;
				}else if(param.equalsIgnoreCase("START_LEVEL"))
				{
					Ancestra.CONFIG_START_LEVEL = Integer.parseInt(value);
					if(Ancestra.CONFIG_START_LEVEL < 1 )
						Ancestra.CONFIG_START_LEVEL = 1;
					if(Ancestra.CONFIG_START_LEVEL > 200)
						Ancestra.CONFIG_START_LEVEL = 200;
				}else if(param.equalsIgnoreCase("START_MAP"))
				{
					Ancestra.CONFIG_START_MAP = Short.parseShort(value);
				}else if(param.equalsIgnoreCase("START_CELL"))
				{
					Ancestra.CONFIG_START_CELL = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("KAMAS"))
				{
					Ancestra.KAMAS = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("HONOR"))
				{
					Ancestra.HONOR = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("SAVE_TIME"))
				{
					Ancestra.CONFIG_SAVE_TIME = Integer.parseInt(value)*60*1000000000;
				}
					else if(param.equalsIgnoreCase("TIME_POPUP_VOTE"))
					{
						Ancestra.CONFIG_TIME_POPUP_VOTE = Integer.parseInt(value);
					}else
					
				if(param.equalsIgnoreCase("XP_PVM"))
				{
					Ancestra.XP_PVM = Integer.parseInt(value);
				}
				else if(param.equalsIgnoreCase("XP_PVP"))
				{
					Ancestra.XP_PVP = Integer.parseInt(value);
				}
				else if(param.equalsIgnoreCase("LVL_PVP"))
				{
					Ancestra.LVL_PVP = Integer.parseInt(value);
			    }else if (param.equalsIgnoreCase("LOAD_SAVE_DELAY"))
	            {
	            Ancestra.CONFIG_LOAD_SAVE =(Integer.parseInt(value) * 1000);
	            }
				else if(param.equalsIgnoreCase("DROP"))
				{
					Ancestra.CONFIG_DROP = Integer.parseInt(value);
				}
				else if(param.equalsIgnoreCase("LOCALIP_LOOPBACK"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						Ancestra.CONFIG_IP_LOOPBACK = true;
					}
				}else if(param.equalsIgnoreCase("ZAAP"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						Ancestra.CONFIG_ZAAP = true;
					}
				}else if(param.equalsIgnoreCase("ACTIVER_XP_DEFI"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						Ancestra.CONFIG_XP_DEFI = true;
					}
				}else if(param.equalsIgnoreCase("USE_IP"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						Ancestra.CONFIG_USE_IP = true;
					}
				}else if (param.equalsIgnoreCase("MESSAGE_DE_BIENVENUE"))
		         {
					Ancestra.CONFIG_MOTD = line.split("=",2)[1];
				}else if(param.equalsIgnoreCase("MOTD_COLOR"))
				{
					Ancestra.CONFIG_MOTD_COLOR = value;
				}else if(param.equalsIgnoreCase("XP_METIER"))
				{
					Ancestra.XP_METIER = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("GAME_PORT"))
				{
					Ancestra.CONFIG_GAME_PORT = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("REALM_PORT"))
				{
					Ancestra.CONFIG_REALM_PORT = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("FLOODER_TIME"))
				{
					Ancestra.FLOOD_TIME = Integer.parseInt(value);
				}
				else if(param.equalsIgnoreCase("HOST_IP"))
				{
					Ancestra.IP = value;
				}
				else if(param.equalsIgnoreCase("DB_HOST"))
				{
					Ancestra.DB_HOST = value;
				}else if(param.equalsIgnoreCase("DB_USER"))
				{
					Ancestra.DB_USER = value;
				}else if(param.equalsIgnoreCase("DB_PASS"))
				{
					if(value == null) value = "";
					Ancestra.DB_PASS = value;
				}else if(param.equalsIgnoreCase("STATIC_DB_NAME"))
				{
					Ancestra.STATIC_DB_NAME = value;
				}else if(param.equalsIgnoreCase("OTHER_DB_NAME"))
				{
					Ancestra.OTHER_DB_NAME = value;
				}else if(param.equalsIgnoreCase("MAX_PERSO_PAR_COMPTE"))
				{
					Ancestra.CONFIG_MAX_PERSOS = Integer.parseInt(value);
				}else if (param.equalsIgnoreCase("USE_MOBS"))
				{
					Ancestra.CONFIG_USE_MOBS = value.equalsIgnoreCase("true");
				}else if (param.equalsIgnoreCase("ALLOW_MULTI_ACCOUNT"))
				{
					Ancestra.CONFIG_ALLOW_MULTI = value.equalsIgnoreCase("true");
				}else if (param.equalsIgnoreCase("LOAD_ACTION_DELAY"))
				{
					Ancestra.CONFIG_LOAD_DELAY = (Integer.parseInt(value) * 1000);
				}else if (param.equalsIgnoreCase("PLAYER_LIMIT"))
				{
					Ancestra.CONFIG_PLAYER_LIMIT = Integer.parseInt(value);
				}else if (param.equalsIgnoreCase("ARENA_MAP"))
				{
					for(String curID : value.split(","))
					{
						Ancestra.arenaMap.add(Integer.parseInt(curID));
					}
				}
				else if(param.equalsIgnoreCase("ALLOW_TELEPORT_ENEMY_FACTION"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						ALLOW_TELEPORT_ENEMY_FACTION = true;
					}
				}
				else if(param.equalsIgnoreCase("KILLED_FACTION_BONTA"))
				{
					killedByFaction_BONTA = value;
				}
				else if(param.equalsIgnoreCase("KILLED_FACTION_BRAK"))
				{
					killedByFaction_BRAK = value;
				}
				else if(param.equalsIgnoreCase("KILLED_FACTION_SERIANE"))
				{
					killedByFaction_SERIANE = value;
				}
					else if (param.equalsIgnoreCase("ARENA_TIMER"))
				{
					Ancestra.CONFIG_ARENA_TIMER = Integer.parseInt(value);
				}else if (param.equalsIgnoreCase("AURA_SYSTEM"))
				{
					Ancestra.AURA_SYSTEM = value.equalsIgnoreCase("true");
				}else if (param.equalsIgnoreCase("ALLOW_MULE_PVP"))
				{
					Ancestra.ALLOW_MULE_PVP = value.equalsIgnoreCase("true");
				}else if (param.equalsIgnoreCase("MAX_IDLE_TIME"))
				{
					Ancestra.CONFIG_MAX_IDLE_TIME = (Integer.parseInt(value)*60000);
				}else if (param.equalsIgnoreCase("NOT_IN_HDV"))
				{
					for(String curID : value.split(","))
					{
						Ancestra.NOTINHDV.add(Integer.parseInt(curID));
					}
				}
				else if(param.equalsIgnoreCase("ACTIVER_PANOPLIE_BIENVENUE"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						Ancestra.START_AVEC_PANO_BIENVENUE = true;
					}
				}
				else if (param.equalsIgnoreCase("LOAD_PUB_DELAY"))
		          {
		              Ancestra.CONFIG_LOAD_PUB=(Integer.parseInt(value) * 1000);
		          }
		        else if (param.equalsIgnoreCase("PUB1"))
		          {
		                  Ancestra.PUB1 = value;
		        
		          }
		        else if (param.equalsIgnoreCase("PUB2"))
		          {
		                  Ancestra.PUB2 = value;
		          }
		        else if (param.equalsIgnoreCase("PUB3"))
		          {
		                  Ancestra.PUB3 = value;
		          }
		        else if(param.equalsIgnoreCase("PUB_ACTIVEE"))
					{
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_PUB = true;
						}       
				
					Ancestra.CONFIG_MESSAGE_BIENVENUE = line.split("=",2)[1];
				}
				//TODO: Mise en place des variables pour la configuration |By Return alias Porky|
				
		        else if(param.equalsIgnoreCase("SECONDS_FOR_BONUS"))
				  {
				     Ancestra.CONFIG_SECONDS_FOR_BONUS = Integer.parseInt(value);
				     if(Ancestra.CONFIG_SECONDS_FOR_BONUS < 1 )
				     Ancestra.CONFIG_SECONDS_FOR_BONUS = 1;
				     if(Ancestra.CONFIG_SECONDS_FOR_BONUS > 3600)
				     Ancestra.CONFIG_SECONDS_FOR_BONUS = 3600;
				     
				  }
				
		          else if(param.equalsIgnoreCase("BONUS_MAX"))
				  {
						Ancestra.CONFIG_BONUS_MAX = Integer.parseInt(value);
						if(Ancestra.CONFIG_BONUS_MAX < 0 )
					    Ancestra.CONFIG_BONUS_MAX = 0;
						if(Ancestra.CONFIG_BONUS_MAX > 1000)
					    Ancestra.CONFIG_BONUS_MAX = 1000;
				  }
		          else if (param.equalsIgnoreCase("MAPAS_NO_PRISMAS")) {
						for (String curID : value.split(",")) {
							mapasNoPrismas.add(Integer.parseInt(curID));
						}
					}
		          else if(param.equalsIgnoreCase("ACTIVER_ETOILES"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_ETOILE = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_NOUVEAUX_SORTS"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIVER_NOUVEAUX_SORTS = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_VIE_PAR_LEVEL"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.ACTIVER_VIE_PAR_LEVEL = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_COMMANDE_SPELLMAX"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.ACTIVER_COMMANDE_SPELLMAX = true;
						}
				  }
				
		          else if(param.equalsIgnoreCase("ACTIVER_COMMANDES_FUN"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_FUNCOMMANDS = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_COMMANDE_GOMJ"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_MJ = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_COMMANDE_GUILDE"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_COMMAND_GUILD = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_COMMANDES_VIP"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_VIPCOMMANDS = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_CARACTERISTIQUES_2"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIVER_STATS_2 = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_COMMANDE_ENERGIE"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_ENERGIE = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_COMMANDES_SEMIFUN"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_COMANDESEMIFUN = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_COMMANDES_MAJ"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_MAJ = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_COMMANDES_ALIGNEMENT"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_SEMIFUNCOMMANDS = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_POPUP_VOTE"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIVER_POPUP_VOTE = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_POPUP_VOTE_DECO"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIVER_POPUP_VOTE_DECO = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_LEVEL_POUR_GAGNER_PA"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_GAGNER_PA = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_LEVEL_POUR_GAGNER_PA2"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_GAGNER_PA2 = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_LEVEL_POUR_GAGNER_PM"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_GAGNER_PM = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_LEVEL_POUR_GAGNER_PM2"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_GAGNER_PM2 = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_METIER_BASE_REQUIS_POUR_FM"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIVER_METIER_REQUIS_FM = true;
						}
				  }
				
		          else if (param.equalsIgnoreCase("LINK_VOTE"))
			        {
			        	Ancestra.CONFIG_LINK_VOTE = value;
			        }
		          else if(param.equalsIgnoreCase("ACTIVER_CINEMATIQUE"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.ACTIVER_CINEMATIQUE = true;
						}
				  }
		          
		          else if(param.equalsIgnoreCase("ACTIVER_NOUVEAU_SORT"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_NOUVEAU_SORT = true;
						}
				  }
		          else if (param.equalsIgnoreCase("RATES_FM")) {
						Ancestra.PORC_FM = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("PRIX_FMCAC"))
					{
						Ancestra.CONFIG_PRIX_FMCAC = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("PM_DEPART"))
					{
						Ancestra.CONFIG_PM_DEPART = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("PA_DEPART"))
					{
						Ancestra.CONFIG_PA_DEPART = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("INITIATIVE_DEPART"))
					{
						Ancestra.CONFIG_INITIATIVE_DEPART = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("INVOCATION_DEPART"))
					{
						Ancestra.CONFIG_INVOCATION_DEPART = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("COMMANDE_SKIN_MORPHID"))
					{
						Ancestra.CONFIG_MORPHID_SKIN = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("PODS_DEPART"))
					{
						Ancestra.CONFIG_PODS_DEPART = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("PROSPECTION_ENUTROF_DEPART"))
					{
						Ancestra.CONFIG_DEPART_PP_ENU = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("POINTS_CARACTERISTIQUE_PAR_LEVEL"))
					{
						Ancestra.CONFIG_CARACTERISTIQUES_PAR_LEVEL = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("POINTS_DE_SORT_PAR_LEVEL"))
					{
						Ancestra.CONFIG_POINTS_SORT_PAR_LEVEL = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("VITALITE_PAR_LEVEL"))
					{
						Ancestra.CONFIG_VITALITE_PAR_LEVEL = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("LEVEL_POUR_GAGNER_PA"))
					{
						Ancestra.CONFIG_LEVEL_POUR_GAGNER_PA = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("NOMBRE_DE_PA"))
					{
						Ancestra.CONFIG_NOMBRE_PA = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("LEVEL_POUR_GAGNER_PA2"))
					{
						Ancestra.CONFIG_LEVEL_POUR_GAGNER_PA2 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("NOMBRE_DE_PA2"))
					{
						Ancestra.CONFIG_NOMBRE_PA2 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("LEVEL_POUR_GAGNER_PM"))
					{
						Ancestra.CONFIG_LEVEL_POUR_GAGNER_PM = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("LEVEL_MAX_PERCO"))
		          {
		        	  Ancestra.CONFIG_LEVEL_MAX_PERCO = Integer.parseInt(value);
		          }
		          else if(param.equalsIgnoreCase("NOMBRE_DE_PM"))
					{
						Ancestra.CONFIG_NOMBRE_PM = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("LEVEL_POUR_GAGNER_PM2"))
					{
						Ancestra.CONFIG_LEVEL_POUR_GAGNER_PM2 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("NOMBRE_DE_PM2"))
					{
						Ancestra.CONFIG_NOMBRE_PM2 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("LEVEL_NOUVEAU_SORT"))
					{
						Ancestra.CONFIG_LEVEL_NOUVEAU_SORT = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("ID_NOUVEAU_SORT"))
					{
						Ancestra.CONFIG_ID_NOUVEAU_SORT = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("LEVEL_REQUIS_METIER_BASE_POUR_FM"))
					{
						Ancestra.CONFIG_LEVEL_REQUIS_FM = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MAP_VIP"))
					{
						Ancestra.CONFIG_MAP_VIP = Short.parseShort(value);
					}
		          else if(param.equalsIgnoreCase("CELL_VIP"))
					{
						Ancestra.CONFIG_CELL_VIP = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MAP_SHOP"))
					{
						Ancestra.CONFIG_MAP_SHOP = Short.parseShort(value);
					}
		          else if(param.equalsIgnoreCase("CELL_SHOP"))
					{
						Ancestra.CONFIG_CELL_SHOP = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MAP_PVP"))
					{
						Ancestra.CONFIG_MAP_PVP = Short.parseShort(value);
					}
		          else if(param.equalsIgnoreCase("CELL_PVP"))
					{
						Ancestra.CONFIG_CELL_PVP = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MAP_BOSS"))
		          {
		        	  	Ancestra.MAP_BOSS = Short.parseShort(value);
		          }
		          else if(param.equalsIgnoreCase("CELL_BOSS"))
		        	  {
		        	  	Ancestra.CELL_BOSS = Integer.parseInt(value);
		        	  }
		          else if(param.equalsIgnoreCase("MAP_PVM"))
					{
						Ancestra.CONFIG_MAP_PVM = Short.parseShort(value);
					}
		          else if(param.equalsIgnoreCase("CELL_PVM"))
					{
						Ancestra.CONFIG_CELL_PVM = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MAP_ENCLOS"))
					{
						Ancestra.CONFIG_MAP_ENCLOS = Short.parseShort(value);
					}
		          else if(param.equalsIgnoreCase("CELL_ENCLOS"))
					{
						Ancestra.CONFIG_CELL_ENCLOS = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("TIME_REBOOT"))
		          {
		        	  Ancestra.CONFIG_TIME_REBOOT = Integer.parseInt(value);
		          }
		          else if(param.equalsIgnoreCase("MAP_EVENT"))
					{
						Ancestra.CONFIG_MAP_EVENT = Short.parseShort(value);
					}
		          else if(param.equalsIgnoreCase("CELL_EVENT"))
					{
						Ancestra.CONFIG_CELL_EVENT = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MONTILIER_ID"))
					{
						Ancestra.CONFIG_MONTILIER_ID = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("HURLEDENT_ID"))
					{
						Ancestra.CONFIG_HURLEDENT_ID = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MONTURE_ID"))
					{
						Ancestra.CONFIG_MONTURE_ID = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("DRAGODINDE_SQUELETTE_ID"))
					{
						Ancestra.CONFIG_SQUELETTE_ID = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("DROP_KAMAS_MINIMUM"))
					{
						Ancestra.CONFIG_KAMASMIN = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("DROP_KAMAS_MAXIMUM"))
					{
						Ancestra.CONFIG_KAMASMAX = Integer.parseInt(value);
					}
				//D�but des variables panoplie de bienvenue
		          else if (param.equalsIgnoreCase("ITEM_BIENVENUE1"))
		          {
		        	  Ancestra.CONFIG_ITEM_BIENVENUE = Integer.parseInt(value);
		          }
		          else if (param.equalsIgnoreCase("ITEM_BIENVENUE2"))
		          {
		        	  Ancestra.CONFIG_ITEM_BIENVENUE2 = Integer.parseInt(value);
		          }
		          else if (param.equalsIgnoreCase("ITEM_BIENVENUE3"))
		          {
		        	  Ancestra.CONFIG_ITEM_BIENVENUE3 = Integer.parseInt(value);
		          }
		          else if (param.equalsIgnoreCase("ITEM_BIENVENUE4"))
		          {
		        	  Ancestra.CONFIG_ITEM_BIENVENUE4 = Integer.parseInt(value);
		          }
		          else if (param.equalsIgnoreCase("ITEM_BIENVENUE5"))
		          {
		        	  Ancestra.CONFIG_ITEM_BIENVENUE5 = Integer.parseInt(value);
		          }
		          else if (param.equalsIgnoreCase("ITEM_BIENVENUE6"))
		          {
		        	  Ancestra.CONFIG_ITEM_BIENVENUE6 = Integer.parseInt(value);
		          }
				//Fin des variables panoplie de bienvenue
				//Fin de la mise en place des variables `|By Return alias Porky|
				//D�but des variables traques |By Return alias Porky|
				
		          else if(param.equalsIgnoreCase("XP_TRAQUE_50"))
					{
						Ancestra.CONFIG_XPTRAQUE_50 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_60"))
					{
						Ancestra.CONFIG_XPTRAQUE_60 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_70"))
					{
						Ancestra.CONFIG_XPTRAQUE_70 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_80"))
					{
						Ancestra.CONFIG_XPTRAQUE_80 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_90"))
					{
						Ancestra.CONFIG_XPTRAQUE_90 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_100"))
					{
						Ancestra.CONFIG_XPTRAQUE_100 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_110"))
					{
						Ancestra.CONFIG_XPTRAQUE_110 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_120"))
					{
						Ancestra.CONFIG_XPTRAQUE_120 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_130"))
					{
						Ancestra.CONFIG_XPTRAQUE_130 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_140"))
					{
						Ancestra.CONFIG_XPTRAQUE_140 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_150"))
					{
						Ancestra.CONFIG_XPTRAQUE_150 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_155"))
					{
						Ancestra.CONFIG_XPTRAQUE_155 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_160"))
					{
						Ancestra.CONFIG_XPTRAQUE_160 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_165"))
					{
						Ancestra.CONFIG_XPTRAQUE_165 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_170"))
					{
						Ancestra.CONFIG_XPTRAQUE_170 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_175"))
					{
						Ancestra.CONFIG_XPTRAQUE_175 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_180"))
					{
						Ancestra.CONFIG_XPTRAQUE_180 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_185"))
					{
						Ancestra.CONFIG_XPTRAQUE_185 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_190"))
					{
						Ancestra.CONFIG_XPTRAQUE_190 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_195"))
					{
						Ancestra.CONFIG_XPTRAQUE_195 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_200"))
					{
						Ancestra.CONFIG_XPTRAQUE_200 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("XP_TRAQUE_1000"))
					{
						Ancestra.CONFIG_XPTRAQUE_10000 = Integer.parseInt(value);
					}
				
		          
		          //Fin des variables traques |By Return alias Porky|
				  //D�but des variables montures sp�ciales |By Return alias Porky|
				
		          else if(param.equalsIgnoreCase("SQUELETTE_STATS_TYPE"))
					{
						Ancestra.CONFIG_SQUELETTE_STATS_TYPE = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("SQUELETTE_STATS"))
					{
						Ancestra.CONFIG_SQUELETTE_STATS = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("SQUELETTE_STATS_TYPE2"))
					{
						Ancestra.CONFIG_SQUELETTE_STATS_TYPE2 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("SQUELETTE_STATS2"))
					{
						Ancestra.CONFIG_SQUELETTE_STATS2 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("SQUELETTE_STATS_TYPE3"))
					{
						Ancestra.CONFIG_SQUELETTE_STATS_TYPE3 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("SQUELETTE_STATS3"))
					{
						Ancestra.CONFIG_SQUELETTE_STATS3 = Integer.parseInt(value);
					}
				
		          else if(param.equalsIgnoreCase("MONTILIER_STATS_TYPE"))
					{
						Ancestra.CONFIG_MONTILIER_STATS_TYPE = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MONTILIER_STATS"))
					{
						Ancestra.CONFIG_MONTILIER_STATS = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MONTILIER_STATS_TYPE2"))
					{
						Ancestra.CONFIG_MONTILIER_STATS_TYPE2 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MONTILIER_STATS2"))
					{
						Ancestra.CONFIG_MONTILIER_STATS2 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MONTILIER_STATS_TYPE3"))
					{
						Ancestra.CONFIG_MONTILIER_STATS_TYPE3 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MONTILIER_STATS3"))
					{
						Ancestra.CONFIG_MONTILIER_STATS3 = Integer.parseInt(value);
					}
				
		          else if(param.equalsIgnoreCase("HURLEDENT_STATS_TYPE"))
					{
						Ancestra.CONFIG_HURLEDENT_STATS_TYPE = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("HURLEDENT_STATS"))
					{
						Ancestra.CONFIG_HURLEDENT_STATS = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("HURLEDENT_STATS_TYPE2"))
					{
						Ancestra.CONFIG_HURLEDENT_STATS_TYPE2 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("HURLEDENT_STATS2"))
					{
						Ancestra.CONFIG_HURLEDENT_STATS2 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("HURLEDENT_STATS_TYPE3"))
					{
						Ancestra.CONFIG_HURLEDENT_STATS_TYPE3 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("HURLEDENT_STATS3"))
					{
						Ancestra.CONFIG_HURLEDENT_STATS3 = Integer.parseInt(value);
					}
				else if(param.equalsIgnoreCase("MONTURE_STATS_TYPE"))
					{
						Ancestra.CONFIG_MONTURE_STATS_TYPE = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MONTURE_STATS"))
					{
						Ancestra.CONFIG_MONTURE_STATS = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MONTURE_STATS_TYPE2"))
					{
						Ancestra.CONFIG_MONTURE_STATS_TYPE2 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MONTURE_STATS2"))
					{
						Ancestra.CONFIG_MONTURE_STATS2 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MONTURE_STATS_TYPE3"))
					{
						Ancestra.CONFIG_MONTURE_STATS_TYPE3 = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MONTURE_STATS3"))
					{
						Ancestra.CONFIG_MONTURE_STATS3 = Integer.parseInt(value);
					}
				
		          else if(param.equalsIgnoreCase("ACTIVER_HURLEDENT_STATS_TYPE2"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_HURLEDENT_STATS2 = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_HURLEDENT_STATS_TYPE3"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_HURLEDENT_STATS3 = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_MONTILIER_STATS_TYPE2"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_MONTILIER_STATS2 = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_MONTILIER_STATS_TYPE3"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_MONTILIER_STATS3 = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_SQUELETTE_STATS_TYPE2"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_SQUELETTE_STATS2 = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_SQUELETTE_STATS_TYPE3"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_SQUELETTE_STATS3 = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_MONTURE_STATS_TYPE2"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_MONTURE_STATS2 = true;
						}
				  }
		          else if(param.equalsIgnoreCase("ACTIVER_MONTURE_STATS_TYPE3"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_MONTURE_STATS3 = true;
						}
				  } 
				
				//Fin des variables monture sp�ciales |By Return alias Porky|
				
				//D�but des variables zobals |By Return alias Porky|
				
		          else if(param.equalsIgnoreCase("ACTIVER_COMMANDE_ZOBAL"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.CONFIG_ACTIV_ZOBAL = true;
						}
				  }
		          else if(param.equalsIgnoreCase("LEVEL_REQUIS_ZOBAL"))
					{
						Ancestra.CONFIG_LEVEL_REQUIERT_ZOBAL = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("MODE_HEROIC"))
					{
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.MODE_HEROIC = true;
						} else {
							Ancestra.MODE_HEROIC = false;
						}
					}
				/** Variables pour Asterions **/
	
		          else if(param.equalsIgnoreCase("ACTIVER_COMMANDE_PRISME"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.ACTIVER_COMMANDE_PRISME = true;
						}
				  }
		          else if(param.equalsIgnoreCase("COMMANDE_PRISME_VIP_REQUIS"))
		          {
						if(value.equalsIgnoreCase("true"))
						{
							Ancestra.COMMANDE_PRISME_VIP = true;
						}
				  }
		          else if(param.equalsIgnoreCase("LEVEL_REQUIS_COMMANDE_PRISME"))
					{
						Ancestra.LEVEL_REQUIS_COMMANDE_PRISME = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("NOMBRE_DE_PRISMES_PAR_COMMANDE"))
					{
						Ancestra.NOMBRE_COMMANDE_PRISME = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("VIE_PERCEPTEUR"))
					{
						Ancestra.VIE_PERCEPTEUR = Integer.parseInt(value);
					}
		          else if(param.equalsIgnoreCase("GFXID_PERCEPTEUR"))
					{
						Ancestra.MORPH_PERCEPTEUR = Integer.parseInt(value);
					}
				
		          else if (param.equalsIgnoreCase("USE_COMPACT_DATA"))
				{
					Ancestra.CONFIG_SOCKET_USE_COMPACT_DATA = value.equalsIgnoreCase("true");
				}else if (param.equalsIgnoreCase("TIME_COMPACT_DATA"))
					Ancestra.CONFIG_SOCKET_TIME_COMPACT_DATA = Integer.parseInt(value);
				}
		
		
			
			if(STATIC_DB_NAME == null || OTHER_DB_NAME == null || DB_HOST == null || DB_PASS == null || DB_USER == null)
			{
				throw new Exception();
			}
		} catch (Exception e) {
            System.out.println(e.getMessage());
			System.out.println("Fichier de configuration non existant ou illisible");
			System.out.println("Fermeture du serveur");
			System.exit(1);
		}
		if(CONFIG_DEBUG)Constants.DEBUG_MAP_LIMIT = 20000;
		try
		{
			String date = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"-"+(Calendar.getInstance().get(Calendar.MONTH)+1)+"-"+Calendar.getInstance().get(Calendar.YEAR);
			if(log)
			{
				Log_GameSock = new BufferedWriter(new FileWriter("Game_logs/"+date+"_packets.txt", true));
				Log_Game = new BufferedWriter(new FileWriter("Game_logs/"+date+".txt", true));
				Log_Realm = new BufferedWriter(new FileWriter("Realm_logs/"+date+".txt", true));
				Log_RealmSock = new BufferedWriter(new FileWriter("Realm_logs/"+date+"_packets.txt", true));
				Log_Shop = new BufferedWriter(new FileWriter("Shop_logs/"+date+".txt", true));
				PS = new PrintStream(new File("Error_logs/"+date+"_error.txt"));
				PS.append("Lancement du serveur..\n");
				PS.flush();
				System.setErr(PS);
				Log_MJ = new BufferedWriter(new FileWriter("Gms_logs/"+date+"_GM.txt",true));
				canLog = true;
				String str = "Lancement du serveur...\n";
				Log_GameSock.write(str);
				Log_Game.write(str);
				Log_MJ.write(str);
				Log_Realm.write(str);
				Log_RealmSock.write(str);
				Log_Shop.write(str);
				Log_GameSock.flush();
				Log_Game.flush();
				Log_MJ.flush();
				Log_Realm.flush();
				Log_RealmSock.flush();
				Log_Shop.flush();
			}
		}catch(IOException e)
		{
			/*On cr�er les dossiers*/
			System.out.println("Les fichiers de logs n'ont pas pu etre creer");
			System.out.println("Creation des dossiers");
			new File("Shop_logs").mkdir(); 
			new File("Game_logs").mkdir(); 
			new File("Realm_logs").mkdir(); 
			new File("Gms_logs").mkdir(); 
			new File("Error_logs").mkdir();
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
	
	public static void closeServers()
	{
		Console.println("Arret du serveur demande ...", ConsoleColorEnum.RED);
		StringBuilder title = new StringBuilder();
		title.append("Onemu - RealmPort: ").append(CONFIG_REALM_PORT).append(" GamePort: ").append(CONFIG_GAME_PORT);
		title.append(" Connectes: ").append(gameServer.getPlayerNumber()).append(" Statut: ");
	    title.append("Arret...");
		Console.setTitle(title.toString());
		if(isRunning)
		{
			isRunning = false;
			Ancestra.gameServer.kickAll();
			World.saveAll(null);
			SQLManager.closeCons();
		}
		title = new StringBuilder();
		title.append("Onemu - RealmPort: ").append(CONFIG_REALM_PORT).append(" GamePort: ").append(CONFIG_GAME_PORT);
		title.append(" Connectes: ").append(gameServer.getPlayerNumber()).append(" Statut: ");
	    title.append("Offline");
		Console.setTitle(title.toString());
		Console.println("Arret du serveur : Ok.", ConsoleColorEnum.RED);
		isRunning = false;
	}
	public static void addToMjLog(String str)
	{
		if(!canLog)return;
		String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(+Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND);
		try {
			Log_MJ.write(str+"  ["+date+"]");
			Log_MJ.newLine();
			Log_MJ.flush();
		} catch (IOException e) {}
	}
	
	public static void addToShopLog(String str)
	{
		if(!canLog)return;
		String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(+Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND);
		try {
			Log_Shop.write("["+date+"]"+str);
			Log_Shop.newLine();
			Log_Shop.flush();
		} catch (IOException e) {}
	}
	public static void loadMountData()
    {
        try {
		BufferedReader config = new BufferedReader(new FileReader("Mount.txt"));
		String line = "";
		while ((line = config.readLine()) != null) {
                            String[] args = line.split(" ");
                            int id = Integer.parseInt(args[0]);
                            int scrollID = Integer.parseInt(args[1]);
                            String stats = "";
                            try{ stats = args[2]; }catch (Exception e){}
                            SQLManager.ADD_MOUNT_DATA(id, scrollID, stats);
                            System.out.println(id+" mount added");
                       }
        }catch(Exception e){}
    }
	
	public static String Version()
	{
		StringBuilder mess = new StringBuilder();
		mess.append("OnEmu Remake - For Asterion by Return");
		return mess.toString();
	}
	public static void onOnemuStarted()
	{
		Console.clear();
		System.out.println("---------------------------------------\n\n");
		System.out.println(Version());
		System.out.println("---------------------------------------\n");
		System.out.print("Chargement du serveur...");
		for(int i=0; i<40; i++)
		{
			System.out.print(".");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		System.out.println(". Ok.");
		Console.println("OnEmu pret ! En attente de connexion....", ConsoleColorEnum.GREEN);
		Console.println("Vous pouvez ecrire des commandes dans cette console (Help ou ? pour la liste).", ConsoleColorEnum.YELLOW);
		new ConsoleInputAnalyzer();
	}
	public static void ReOnemuStarted()
	{
		Console.clear();
		System.out.println("---------------------------------------\n\n");
		System.out.println(Version());
		System.out.println("---------------------------------------\n");
		System.out.print("Chargement du serveur...");
		System.out.print("...............................");
		System.out.println(". Ok");
		Console.println("OnEmu pret! En attente de connexion....", ConsoleColorEnum.GREEN);
		Console.println("Vous pouvez ecrire des commandes dans cette console (Help ou ? pour la liste).", ConsoleColorEnum.YELLOW);
		/*try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}*/
		new ConsoleInputAnalyzer();
	}
	public static void refreshTitle()
	{
		if(!isRunning)return;
		StringBuilder title = new StringBuilder();
		title.append("Onemu - RealmPort: ").append(CONFIG_REALM_PORT).append(" GamePort: ").append(CONFIG_GAME_PORT);
		title.append(" En ligne: ").append(gameServer.getPlayerNumber()).append(" Statut: ");
	    switch(World.get_state())
	    {
	    case (short)1:title.append("Online");break;
	    case (short)2:title.append("Save");break;
	    default:title.append("Indisponnible");break;
	    }
		Console.setTitle(title.toString());
	}
	
	public static void printDebug(String debugInfo)
	{
		if(Ancestra.CONFIG_DEBUG)
		{
			Console.println(debugInfo, ConsoleColorEnum.YELLOW);
		}
	}
	public static void printDebug(int debugInfo)
	{
		if(Ancestra.CONFIG_DEBUG)
		{
			Console.println(Integer.toString(debugInfo), ConsoleColorEnum.YELLOW);
		}
	}
	public static void printDebug()
	{
		if(Ancestra.CONFIG_DEBUG)
		{
			Console.println("", ConsoleColorEnum.YELLOW);
		}
	}
	public static void printError(String error)
	{
			if(Ancestra.CONFIG_DEBUG)
			{
				Console.println(error, ConsoleColorEnum.RED);
			}
	}
}
