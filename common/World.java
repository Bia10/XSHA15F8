package common;

import game.GameServer;


import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import objects.Prisma;
import java.util.Map.Entry;

import objects.*;
//import objects.Fight.Fighter;
import objects.HDV.HdvEntry;
import objects.Metier.StatsMetier;
import objects.NPC_tmpl.*;
import objects.Objet.ObjTemplate;
import objects.Personnage.Stats;

public class World {

	private static Map<Integer,Compte> 	Comptes	= Collections.synchronizedMap(new HashMap<Integer,Compte>());
	private static Map<String,Integer> 	ComptebyName	= Collections.synchronizedMap(new HashMap<String,Integer>());
	private static Map<String, Personnage> Personnage2 = new HashMap<String, Personnage>();
	private static StringBuilder Challenges = new StringBuilder();
	private static Map<Integer,Personnage> 	Persos	=Collections.synchronizedMap( new HashMap<Integer,Personnage>());
	private static Map<Short,Carte> 	Cartes	= new HashMap<Short,Carte>();
	private static Map<Integer,Objet> 	Objets	= Collections.synchronizedMap(new HashMap<Integer,Objet>());
	private static Map<Integer,ExpLevel> ExpLevels = new HashMap<Integer, ExpLevel>();
	private static Map<Integer,Sort>	Sorts = new HashMap<Integer,Sort>();
	private static Map<Integer,ObjTemplate> ObjTemplates = new HashMap<Integer,ObjTemplate>();
	private static Map<Integer,Monstre> MobTemplates = new HashMap<Integer,Monstre>();
	private static Map<Integer,NPC_tmpl> NPCTemplates = new HashMap<Integer,NPC_tmpl>();
	private static Map<Integer,NPC_question> NPCQuestions = new HashMap<Integer,NPC_question>();
	private static Map<Integer, IOTemplate> IOTemplate = new TreeMap<Integer, IOTemplate>();
	private static Map<Integer, Dragodinde> Mounts = new TreeMap<Integer, Dragodinde>();
    private static Map<Integer, ObjTemplate> reloaded_ObjTemplateByMount = new TreeMap<Integer, ObjTemplate>();
	private static Map<Integer, List<Couple<Integer, Double>>> reloaded_StatsByMount = new TreeMap<Integer, List<Couple<Integer, Double>>>();
	private static Map<Integer, ObjTemplate> ObjTemplateByMount = new TreeMap<Integer, ObjTemplate>();
	private static Map<Integer,NPC_reponse> NPCReponses = new HashMap<Integer,NPC_reponse>();
	private static Map<Integer,Dragodinde> Dragodindes = Collections.synchronizedMap(new HashMap<Integer,Dragodinde>());
	private static Map<Integer,SuperArea> SuperAreas = new HashMap<Integer,SuperArea>();
	private static Map<Integer,Area> Areas = new HashMap<Integer,Area>();
	private static Map<Integer,SubArea> SubAreas = new HashMap<Integer,SubArea>();
	private static Map<Integer,Metier> Jobs = new HashMap<Integer,Metier>();
	private static Map<Integer,ArrayList<Couple<Integer,Integer>>> Crafts = new HashMap<Integer,ArrayList<Couple<Integer,Integer>>>();
	private static Map<Integer,ItemSet> ItemSets = new HashMap<Integer,ItemSet>();
	private static Map<Integer,Guild> Guildes = Collections.synchronizedMap(new HashMap<Integer,Guild>());
	private static Map<Integer,HDV> Hdvs = new HashMap<Integer,HDV>();
	private static Map<Integer,Map<Integer,ArrayList<HdvEntry>>> _hdvsItems = Collections.synchronizedMap(new HashMap<Integer,Map<Integer,ArrayList<HdvEntry>>>());	//Contient tout les items en ventes des comptes dans le format<compteID,<hdvID,items<>>>
	private static Map<Integer,Personnage> Married = Collections.synchronizedMap(new HashMap<Integer,Personnage>()); 
	private static Map<Integer,Animations> Animations = new HashMap<Integer,Animations>();
	private static Map<Short,Carte.MountPark> MountPark = new HashMap<Short,Carte.MountPark>();
	private static Map<Integer, List<Couple<Integer, Double>>> StatsByMount = new HashMap<Integer, List<Couple<Integer, Double>>>();
	private static Map<Integer,Trunk> Trunks = new HashMap<Integer,Trunk>();
	private static Map<Integer,Percepteur> Percepteurs = Collections.synchronizedMap(new HashMap<Integer,Percepteur>());
	private static Map<Integer, Prisma> Prismas = Collections.synchronizedMap(new HashMap<Integer, Prisma>());
	private static Map<Integer,House> Houses = new HashMap<Integer,House>();
	private static Map<Short,Collection<Integer>> Seller	=Collections.synchronizedMap( new HashMap<Short,Collection<Integer>>());
	public static Map<Integer,StatsMetier> upAlchi = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upBrico = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upCM = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upB = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upFE = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upSA = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upFM = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upCo = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upBi = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>()); //Bijou
	public static Map<Integer,StatsMetier> upFD = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upSB =Collections.synchronizedMap( new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upSBg =Collections.synchronizedMap( new HashMap<Integer,StatsMetier>()); //S baguette
	public static Map<Integer,StatsMetier> upFP =Collections.synchronizedMap( new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upM = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upBou = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upT = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upP = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upFH = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upFPc = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>()); // P�cheurman... 42.. Sydney... avenue Walaby
	public static Map<Integer,StatsMetier> upC =Collections.synchronizedMap( new HashMap<Integer,StatsMetier>());//Chasseurs
	public static Map<Integer,StatsMetier> upFMD = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upFME = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upFMM = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upFMP = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upFMH = Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upSMA = Collections.synchronizedMap( new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upSMB =  Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upSMBg =  Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upBouc =  Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upPO =  Collections.synchronizedMap(new HashMap<Integer,StatsMetier>()); // Poissonniers.
	public static Map<Integer,StatsMetier> upFBou =  Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());
	public static Map<Integer,StatsMetier> upJM =  Collections.synchronizedMap(new HashMap<Integer,StatsMetier>()); //Joaillo
	public static Map<Integer,StatsMetier> upCRM =  Collections.synchronizedMap(new HashMap<Integer,StatsMetier>());//Cordomages
	
	 
	private static int nextHdvID;	//Contient le derniere ID utilis� pour cr�e un HDV, pour obtenir un ID non utilis� il faut imp�rativement l'incr�menter
	private static int nextLigneID;	//Contient le derniere ID utilis� pour cr�e une ligne dans un HDV
	
	private static int saveTry = 1;
	//Statut du serveur 1: accesible; 0: inaccesible; 2: sauvegarde
	private static short _state = 1;
	private static byte _GmAccess = 0;
	
	private static int nextObjetID; //Contient le derniere ID utilis� pour cr�e un Objet
	
	
	public static class Drop
	{
		private int _itemID;
		private int _prosp;
		private float _taux;
		private int _max;
		
		public Drop(int itm,int p,float t,int m)
		{
			_itemID = itm;
			_prosp = p;
			_taux = t;
			_max = m;
		}
		public void setMax(int m)
		{
			_max = m;
		}
		public int get_itemID() {
			return _itemID;
		}

		public int getMinProsp() {
			return _prosp;
		}

		public float get_taux() {
			return _taux;
		}

		public int get_max() {
			return _max;
		}
	}

	public static class ItemSet
	{
		private int _id;
		private ArrayList<ObjTemplate> _itemTemplates = new ArrayList<ObjTemplate>();
		private ArrayList<Stats> _bonuses = new ArrayList<Stats>();
		
		public ItemSet (int id,String items, String bonuses)
		{
			_id = id;
			//parse items String
			for(String str : items.split(","))
			{
				try
				{
					ObjTemplate t = World.getObjTemplate(Integer.parseInt(str.trim()));
					if(t == null)continue;
					_itemTemplates.add(t);
				}catch(Exception e){};
			}
			
			//on ajoute un bonus vide pour 1 item
			_bonuses.add(new Stats());
			//parse bonuses String
			for(String str : bonuses.split(";"))
			{
				Stats S = new Stats();
				//s�paration des bonus pour un m�me nombre d'item
				for(String str2 : str.split(","))
				{
					try
					{
						String[] infos = str2.split(":");
						int stat = Integer.parseInt(infos[0]);
						int value = Integer.parseInt(infos[1]);
						//on ajoute a la stat
						S.addOneStat(stat, value);
					}catch(Exception e){};
				}
				//on ajoute la stat a la liste des bonus
				_bonuses.add(S);
			}
		}

		public int getId()
		{
			return _id;
		}
		
		public Stats getBonusStatByItemNumb(int numb)
		{
			if(numb>_bonuses.size())return new Stats();
			return _bonuses.get(numb-1);
		}
		
		public ArrayList<ObjTemplate> getItemTemplates()
		{
			return _itemTemplates;
		}
	}
	
	public static class SuperArea
	{
		private int _id;
		private ArrayList<Area> _areas = new ArrayList<Area>();
		
		public SuperArea(int a_id)
		{
			_id = a_id;
		}
		
		public void addArea(Area A)
		{
			_areas.add(A);
		}
		
		public int get_id()
		{
			return _id;
		}
	}
	
	public static class Area
	{
		private int _id;
		private SuperArea _superArea;
		private String _name;
		private ArrayList<SubArea> _subAreas = new ArrayList<SubArea>();
		private int _alineacion;
		public static int _bontas = 0;
		public static int _brakmars = 0;
		private int _prisma = 0;
		public Area(int id, int superArea, String nombre, int alineacion,
				int prisma) {
			_id = id;
			_name = nombre;
			_superArea = World.getSuperArea(superArea);
			if (_superArea == null) {
				_superArea = new SuperArea(superArea);
				World.addSuperArea(_superArea);
			}
			_alineacion = 0;
			_prisma = 0;
			if (World.getPrisma(prisma) != null) {
				_alineacion = alineacion;
				_prisma = prisma;
			}
			if (_alineacion == 1)
				_bontas++;
			else if (_alineacion == 2)
				_brakmars++;
		}
		public static int subareasBontas() {
			return _bontas;
		}

		public static int subareasBrakmars() {
			return _brakmars;
		}

		public int getAlineacion() {
			return _alineacion;
		}

		public int getPrismaID() {
			return _prisma;
		}

		public void setPrismaID(int prisma) {
			_prisma = prisma;
		}

		public void setAlineacion(int alineacion) {
			if (_alineacion == 1 && alineacion == -1)
				_bontas--;
			else if (_alineacion == 2 && alineacion == -1)
				_brakmars--;
			else if (_alineacion == -1 && alineacion == 1)
				_bontas++;
			else if (_alineacion == -1 && alineacion == 2)
				_brakmars++;
			_alineacion = alineacion;
		}
		public String get_name()
		{
			return _name;
		}
		public int get_id()
		{
			return _id;
		}
		public ArrayList<SubArea> getSubAreas() {
			return _subAreas;
		}
		
		public SuperArea get_superArea()
		{
			return _superArea;
		}
		
		public void addSubArea(SubArea sa)
		{
			_subAreas.add(sa);
		}
		public SuperArea getSuperArea() {
			return _superArea;
		}
		
		public ArrayList<Carte> getMaps()
		{
			ArrayList<Carte> maps = new ArrayList<Carte>();
			for(SubArea SA : _subAreas)maps.addAll(SA.getMaps());
			return maps;
		}
	}
	
	public static class SubArea
	{
		private int _id;
		private Area _area;
		private int _alignement;
		private String _name;
		private ArrayList<Carte> _maps = new ArrayList<Carte>();
		private boolean _conquistable;
		private int _prisma;
		public static int _bontas = 0;
		public static int _brakmars = 0;
		
		public SubArea(int id, int areaID, int alignement, String name,
				int conquistable, int prisma)
		{
			_id = id;
			_name = name;
			_area =  World.getArea(areaID);
			_alignement = alignement;
			_conquistable = conquistable == 0;
			_prisma = prisma;
			_prisma = 0;
			if (World.getPrisma(prisma) != null) {
				_alignement = alignement;
				_prisma = prisma;
			}
			if (_alignement == 1)
				_bontas++;
			else if (_alignement == 2)
				_brakmars++;
		}

		public int getPrismaID() {
			return _prisma;
		}

		public void setPrismaID(int prisma) {
			_prisma = prisma;
		}

		public boolean getConquistable() {
			return _conquistable;
		}

		public void setAlineacion(int alineacion) {

			if (_alignement == 1 && alineacion == -1)
				_bontas--;

			else if (_alignement == 2 && alineacion == -1)
				_brakmars--;

			else if (_alignement == -1 && alineacion == 1)
				_bontas++;

			else if (_alignement == -1 && alineacion == 2)
				_brakmars++;

			_alignement = alineacion;
		}
		
		public String get_name()
		{
			return _name;
		}
		public int get_id() {
			return _id;
		}
		public Area get_area() {
			return _area;
		}
		public int get_alignement() {
			return _alignement;
		}
		public ArrayList<Carte> getMaps() {
			return _maps;
		}

		public void addMap(Carte carte)
		{
			_maps.add(carte);
		}
		
	}
	
	public static class Couple<L,R>
	{
	    public L first;
	    public R second;

	    public Couple(L s, R i)
	    {
	         this.first = s;
	         this.second = i;
	    }
	}

	public static class IOTemplate
	{
		private int _id;
		private int _respawnTime;
		private int _duration;
		private int _unk;
		private boolean _walkable;
		
		public IOTemplate(int a_i,int a_r,int a_d,int a_u, boolean a_w)
		{
			_id = a_i;
			_respawnTime = a_r;
			_duration = a_d;
			_unk = a_u;
			_walkable = a_w;
		}
		
		public int getId() {
			return _id;
		}	
		public boolean isWalkable() {
			return _walkable;
		}

		public int getRespawnTime() {
			return _respawnTime;
		}
		public int getDuration() {
			return _duration;
		}
		public int getUnk() {
			return _unk;
		}
	}
	
	public static class Exchange
	{
		private Personnage perso1;
		private Personnage perso2;
		private long kamas1 = 0;
		private long kamas2 = 0;
		private ArrayList<Couple<Integer,Integer>> items1 = new ArrayList<Couple<Integer,Integer>>();
		private ArrayList<Couple<Integer,Integer>> items2 = new ArrayList<Couple<Integer,Integer>>();
		private boolean ok1;
		private boolean ok2;
		
		public Exchange(Personnage p1, Personnage p2)
		{
			perso1 = p1;
			perso2 = p2;
		}
		
		synchronized public long getKamas(int guid)
		{
			int i = 0;
			if(perso1.get_GUID() == guid)
				i = 1;
			else if(perso2.get_GUID() == guid)
				i = 2;
			
			if(i == 1)
				return kamas1;
			else if (i == 2)
				return kamas2;
			return 0;
		}
		
		synchronized public void toogleOK(int guid)
		{
			int i = 0;
			if(perso1.get_GUID() == guid)
				i = 1;
			else if(perso2.get_GUID() == guid)
				i = 2;
			
			if(i == 1)
			{
				ok1 = !ok1;
				SocketManager.GAME_SEND_EXCHANGE_OK(perso1.get_compte().getGameThread().get_out(),ok1,guid);
				SocketManager.GAME_SEND_EXCHANGE_OK(perso2.get_compte().getGameThread().get_out(),ok1,guid);
			}
			else if (i == 2)
			{
				ok2 = !ok2;
				SocketManager.GAME_SEND_EXCHANGE_OK(perso1.get_compte().getGameThread().get_out(),ok2,guid);
				SocketManager.GAME_SEND_EXCHANGE_OK(perso2.get_compte().getGameThread().get_out(),ok2,guid);
			}
			else 
				return;
			
			
			if(ok1 && ok2)
				apply();
		}
		
		synchronized public void setKamas(int guid, long k)
		{
			ok1 = false;
			ok2 = false;
			
			int i = 0;
			if(perso1.get_GUID() == guid)
				i = 1;
			else if(perso2.get_GUID() == guid)
				i = 2;
			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.get_compte().getGameThread().get_out(),ok1,perso1.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.get_compte().getGameThread().get_out(),ok1,perso1.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.get_compte().getGameThread().get_out(),ok2,perso2.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.get_compte().getGameThread().get_out(),ok2,perso2.get_GUID());
			
			if(i == 1)
			{
				kamas1 = k;
				SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'G', "", k+"");
				SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.get_compte().getGameThread().get_out(), 'G', "", k+"");
			}else if (i == 2)
			{
				kamas2 = k;
				SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.get_compte().getGameThread().get_out(), 'G', "", k+"");
				SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'G', "", k+"");	
			}
		}
		
		synchronized public void cancel()
		{
			if(perso1.get_compte() != null)if(perso1.get_compte().getGameThread() != null)SocketManager.GAME_SEND_EV_PACKET(perso1.get_compte().getGameThread().get_out());
			if(perso2.get_compte() != null)if(perso2.get_compte().getGameThread() != null)SocketManager.GAME_SEND_EV_PACKET(perso2.get_compte().getGameThread().get_out());
			perso1.set_isTradingWith(0);
			perso2.set_isTradingWith(0);
			perso1.setCurExchange(null);
			perso2.setCurExchange(null);
		}
		
		synchronized public void apply()
		{
			//Gestion des Kamas
			perso1.addKamas((-kamas1+kamas2));
			perso2.addKamas((-kamas2+kamas1));
			for(Couple<Integer, Integer> couple : items1)
			{
				if(couple.second == 0)continue;
				if(!perso1.hasItemGuid(couple.first))//Si le perso n'a pas l'item (Ne devrait pas arriver)
				{
					couple.second = 0;//On met la quantit� a 0 pour �viter les problemes
					continue;
				}	
				Objet obj = World.getObjet(couple.first);
				if((obj.getQuantity() - couple.second) <1)//S'il ne reste plus d'item apres l'�change
				{
					perso1.removeItem(couple.first);
					couple.second = obj.getQuantity();
					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(perso1, couple.first);
					if(!perso2.addObjet(obj, true))//Si le joueur avait un item similaire
						World.removeItem(couple.first);//On supprime l'item inutile
				}else
				{
					obj.setQuantity(obj.getQuantity()-couple.second);
					SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(perso1, obj);
					Objet newObj = Objet.getCloneObjet(obj, couple.second);
					if(perso2.addObjet(newObj, true))//Si le joueur n'avait pas d'item similaire
						World.addObjet(newObj,true);//On ajoute l'item au World
				}
			}
			for(Couple<Integer, Integer> couple : items2)
			{
				if(couple.second == 0)continue;
				if(!perso2.hasItemGuid(couple.first))//Si le perso n'a pas l'item (Ne devrait pas arriver)
				{
					couple.second = 0;//On met la quantit� a 0 pour �viter les problemes
					continue;
				}	
				Objet obj = World.getObjet(couple.first);
				if((obj.getQuantity() - couple.second) <1)//S'il ne reste plus d'item apres l'�change
				{
					perso2.removeItem(couple.first);
					couple.second = obj.getQuantity();
					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(perso2, couple.first);
					if(!perso1.addObjet(obj, true))//Si le joueur avait un item similaire
						World.removeItem(couple.first);//On supprime l'item inutile
				}else
				{
					obj.setQuantity(obj.getQuantity()-couple.second);
					SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(perso2, obj);
					Objet newObj = Objet.getCloneObjet(obj, couple.second);
					if(perso1.addObjet(newObj, true))//Si le joueur n'avait pas d'item similaire
						World.addObjet(newObj,true);//On ajoute l'item au World
				}
			}
			//Fin
			perso1.set_isTradingWith(0);
			perso2.set_isTradingWith(0);
			perso1.setCurExchange(null);
			perso2.setCurExchange(null);
			SocketManager.GAME_SEND_Ow_PACKET(perso1);
			SocketManager.GAME_SEND_Ow_PACKET(perso2);
			SocketManager.GAME_SEND_STATS_PACKET(perso1);
			SocketManager.GAME_SEND_STATS_PACKET(perso2);
			SocketManager.GAME_SEND_EXCHANGE_VALID(perso1.get_compte().getGameThread().get_out(),'a');
			SocketManager.GAME_SEND_EXCHANGE_VALID(perso2.get_compte().getGameThread().get_out(),'a');	
			SQLManager.SAVE_PERSONNAGE(perso1,true);
			SQLManager.SAVE_PERSONNAGE(perso2,true);
		}

		synchronized public void addItem(int guid, int qua, int pguid)
		{
			ok1 = false;
			ok2 = false;
			
			Objet obj = World.getObjet(guid);
			int i = 0;
			
			if(perso1.get_GUID() == pguid) i = 1;
			if(perso2.get_GUID() == pguid) i = 2;
			
			if(qua == 1) qua = 1;
			String str = guid+"|"+qua;
			if(obj == null)return;
			String add = "|"+obj.getTemplate().getID()+"|"+obj.parseStatsString();
			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.get_compte().getGameThread().get_out(),ok1,perso1.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.get_compte().getGameThread().get_out(),ok1,perso1.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.get_compte().getGameThread().get_out(),ok2,perso2.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.get_compte().getGameThread().get_out(),ok2,perso2.get_GUID());
			if(i == 1)
			{
				Couple<Integer,Integer> couple = getCoupleInList(items1,guid);
				if(couple != null)
				{
					couple.second += qua;
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'O', "+", ""+guid+"|"+couple.second);
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.get_compte().getGameThread().get_out(), 'O', "+", ""+guid+"|"+couple.second+add);
					return;
				}
				SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'O', "+", str);
				SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.get_compte().getGameThread().get_out(), 'O', "+", str+add);	
				items1.add(new Couple<Integer,Integer>(guid,qua));
			}else if(i == 2)
			{
				Couple<Integer,Integer> couple = getCoupleInList(items2,guid);
				if(couple != null)
				{
					couple.second += qua;
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'O', "+", ""+guid+"|"+couple.second);
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.get_compte().getGameThread().get_out(), 'O', "+", ""+guid+"|"+couple.second+add);
					return;
				}
				SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'O', "+", str);
				SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.get_compte().getGameThread().get_out(), 'O', "+", str+add);
				items2.add(new Couple<Integer,Integer>(guid,qua));
			}
		}

		
		synchronized public void removeItem(int guid, int qua, int pguid)
		{
			int i = 0;
			if(perso1.get_GUID() == pguid)
				i = 1;
			else if(perso2.get_GUID() == pguid)
				i = 2;
			ok1 = false;
			ok2 = false;
			
			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.get_compte().getGameThread().get_out(),ok1,perso1.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.get_compte().getGameThread().get_out(),ok1,perso1.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.get_compte().getGameThread().get_out(),ok2,perso2.get_GUID());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.get_compte().getGameThread().get_out(),ok2,perso2.get_GUID());
			
			Objet obj = World.getObjet(guid);
			if(obj == null)return;
			String add = "|"+obj.getTemplate().getID()+"|"+obj.parseStatsString();
			if(i == 1)
			{
				Couple<Integer,Integer> couple = getCoupleInList(items1,guid);
				int newQua = couple.second - qua;
				if(newQua <1)//Si il n'y a pu d'item
				{
					items1.remove(couple);
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'O', "-", ""+guid);
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.get_compte().getGameThread().get_out(), 'O', "-", ""+guid);
				}else
				{
					couple.second = newQua;
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'O', "+", ""+guid+"|"+newQua);
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.get_compte().getGameThread().get_out(), 'O', "+", ""+guid+"|"+newQua+add);
				}
			}else if(i ==2)
			{
				Couple<Integer,Integer> couple = getCoupleInList(items2,guid);
				int newQua = couple.second - qua;
				
				if(newQua <1)//Si il n'y a pu d'item
				{
					items2.remove(couple);
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.get_compte().getGameThread().get_out(), 'O', "-", ""+guid);
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'O', "-", ""+guid);
				}else
				{
					couple.second = newQua;
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.get_compte().getGameThread().get_out(), 'O', "+", ""+guid+"|"+newQua+add);
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'O', "+", ""+guid+"|"+newQua);
				}
			}
		}

		synchronized private Couple<Integer, Integer> getCoupleInList(ArrayList<Couple<Integer, Integer>> items,int guid)
		{
			for(Couple<Integer, Integer> couple : items)
			{
				if(couple.first == guid)
					return couple;
			}
			return null;
		}
		
		public synchronized int getQuaItem(int itemID, int playerGuid)
		{
			ArrayList<Couple<Integer, Integer>> items;
			if(perso1.get_GUID() == playerGuid)
				items = items1;
			else
				items = items2;
			
			for(Couple<Integer, Integer> curCoupl : items)
			{
				if(curCoupl.first == itemID)
				{
					return curCoupl.second;
				}
			}
			
			return 0;
		}
		
	}

	public static class ExpLevel
	{
		public long perso;
		public int metier;
		public int dinde;
		public int pvp;
		public long guilde;
		
		public ExpLevel(long c, int m, int d, int p)
		{
			perso = c;
			metier = m;
			dinde = d;
			pvp = p;
			guilde = perso*10;
		}
		
	}
	
	public static void createWorld()
	{
		System.out.println("====>Donnees statique<====");
		System.out.println("Chargement des niveaux d'experiences:");
		SQLManager.LOAD_EXP();
		System.out.println(ExpLevels.size()+" niveaux ont ete charges");
		System.out.println("Chargement des sorts:");
		System.out.print("Chargement des prismes: ");
		int numero = SQLManager.CARGAR_PRISMAS();
		System.out.println(numero + " Prismes charges");
		SQLManager.LOAD_SORTS();
		System.out.println(Sorts.size()+" sorts ont ete charges");
		System.out.println("Chargement des templates de monstre:");
		SQLManager.LOAD_MOB_TEMPLATE();
		System.out.println(MobTemplates.size()+" templates de monstre ont ete chargees");
		System.out.println("Chargement des templates d'objet:");
		SQLManager.LOAD_OBJ_TEMPLATE();
		System.out.println(ObjTemplates.size()+" templates d'objet ont ete chargees");
		System.out.println("Chargement des templates de NPC:");
		SQLManager.LOAD_NPC_TEMPLATE();
		System.out.println(NPCTemplates.size()+" templates de NPC ont ete chargees");
		System.out.println("Chargement des questions de NPC:");
		SQLManager.LOAD_NPC_QUESTIONS();
		System.out.println(NPCQuestions.size()+" questions de NPC ont ete charges");
		System.out.println("Chargement des reponses de NPC:");
		SQLManager.LOAD_NPC_ANSWERS();
		System.out.println(NPCReponses.size()+" reponses de NPC ont ete chargees");
		System.out.println("Chargement des zones:");
		SQLManager.LOAD_AREA();
		System.out.println(Areas.size()+" zones ont ete chargees");
		System.out.println("Chargement des sous-zone:");
		SQLManager.LOAD_SUBAREA();
		System.out.println(SubAreas.size()+" sous-zones ont ete chargees");
		System.out.println("Chargement des template d'objet interactifs:");
		SQLManager.LOAD_IOTEMPLATE();
		System.out.println(IOTemplate.size()+" template d'IO ont ete charges");
		System.out.println("Chargement des recettes:");
		SQLManager.LOAD_CRAFTS();
		System.out.println(Crafts.size()+" recettes ont ete chargses");
		System.out.println("Chargement des metiers:");
		SQLManager.LOAD_JOBS();
		System.out.println(Jobs.size()+" metiers ont ete charges");
		System.out.println("Chargement des panolies:");
		SQLManager.LOAD_ITEMSETS();
		System.out.println(ItemSets.size()+" panoplies ont ete chargees");
		System.out.println("Chargement des maps:");
		SQLManager.LOAD_MAPS();
		System.out.println(Cartes.size()+" maps ont ete chargees");
		System.out.println("Chargement des Triggers:");
		int nbr = SQLManager.LOAD_TRIGGERS();
		System.out.println(nbr+" triggers ont ete charges");
		System.out.println("Chargement des actions de fin de combat:");
		nbr = SQLManager.LOAD_ENDFIGHT_ACTIONS();
		System.out.println(nbr+" actions ont ete charges");
		System.out.println("Chargement des npcs:");
		nbr = SQLManager.LOAD_NPCS();
		System.out.println(nbr+" npcs ont ete chargees");
		System.out.println("Chargement des actions des objets:");
		nbr = SQLManager.LOAD_ITEM_ACTIONS();
		System.out.println(nbr+" actions ont ete chargees");
		System.out.print("Chargement des Drops: ");
		SQLManager.LOAD_DROPS();
		System.out.println("Ok !");
		System.out.println("Chargement des Animations: ");
		SQLManager.LOAD_ANIMATIONS();
		System.out.println(Animations.size() + " ont ete chargees");
		
		System.out.println("====>Donnees dynamique<====");
		System.out.print("Mise a 0 des logged: ");
		SQLManager.LOGGED_ZERO();
		System.out.println("Ok !");
		System.out.print("Chargement des items: ");
		SQLManager.LOAD_ITEMS_FULL();
		System.out.println("Ok !");
		System.out.print("Chargement des comptes: ");
		SQLManager.LOAD_COMPTES();
		System.out.println(Comptes.size()+" comptes charges");
		System.out.print("Chargement des personnages: ");
		SQLManager.LOAD_PERSOS();
		System.out.println(Persos.size()+" personnages charges");
	    System.out.print("Chargement des guildes: ");
		SQLManager.LOAD_GUILDS();
		System.out.println(Guildes.size()+" guildes chargees");
		System.out.print("Chargement des dragodindes: ");
		SQLManager.LOAD_MOUNTS();
		System.out.println(Dragodindes.size()+" dragodindes chargees");
		System.out.println("Chargement des statistiques dragodindes");
		SQLManager.LOAD_Mounts_Datas();
		System.out.println("Chargement avec succes !");
		System.out.print("Chargement des challenges: ");
		SQLManager.LOAD_CHALLENGES();
		System.out.println(Challenges.toString().split(";").length+" challenges charges");
		System.out.print("Chargement des membres de guildes: ");
		SQLManager.LOAD_GUILD_MEMBERS();
		System.out.println("Ok !");
		System.out.print("Chargement des donnees d'enclos: ");
		nbr = SQLManager.LOAD_MOUNTPARKS();
		System.out.println(nbr+" enclos charges");
		System.out.print("Chargement des percepteurs: ");
		nbr = SQLManager.LOAD_PERCEPTEURS();
		System.out.println(nbr+" percepteurs charges");
		System.out.print("Chargement des maisons: ");
		nbr = SQLManager.LOAD_HOUSES();
		System.out.println(nbr+" maisons chargees");
		System.out.print("Chargement des coffres: ");
		nbr = SQLManager.LOAD_TRUNK();
		System.out.println(nbr+" coffres charges");
		System.out.print("Chargement des zaaps: ");
		nbr = SQLManager.LOAD_ZAAPS();
		System.out.println(nbr+" zaaps chargees");
		System.out.print("Chargement des zaapis: ");
		nbr = SQLManager.LOAD_ZAAPIS();
		System.out.println(nbr+" zaapis chargees");
		System.out.print("Chargement des BAN_IP: ");
		nbr = SQLManager.LOAD_BANIP();
		System.out.println(nbr+" BAN_IP chargees");
		System.out.print("Chargement des HDV: ");
		SQLManager.LOAD_HDVS();
		SQLManager.LOAD_HDVS_ITEMS();
		
		nextObjetID = SQLManager.getNextObjetID();
	}
	
	public static Area getArea(int areaID)
	{
		return Areas.get(areaID);
	}

	public static SuperArea getSuperArea(int areaID)
	{
		return SuperAreas.get(areaID);
	}
	
	public static SubArea getSubArea(int areaID)
	{
		return SubAreas.get(areaID);
	}
	
	public static void addArea(Area area)
	{
		Areas.put(area.get_id(), area);
	}
	
	public static void addSuperArea(SuperArea SA)
	{
		SuperAreas.put(SA.get_id(), SA);
	}
	
	public static void addSubArea(SubArea SA)
	{
		SubAreas.put(SA.get_id(), SA);
	}
	
	public static void addNPCreponse(NPC_reponse rep)
	{
		NPCReponses.put(rep.get_id(), rep);
	}
	
	public static NPC_reponse getNPCreponse(int guid)
	{
		return NPCReponses.get(guid);
	}
	
	public static int getExpLevelSize()
	{
		return ExpLevels.size();
	}
	
	public static void addExpLevel(int lvl,ExpLevel exp)
	{
		ExpLevels.put(lvl, exp);
	}
	
	public static Compte getCompte(int guid)
	{
		return Comptes.get(guid);
	}
	
	public static void addNPCQuestion(NPC_question quest)
	{
		NPCQuestions.put(quest.get_id(), quest);
	}
	
	public static NPC_question getNPCQuestion(int guid)
	{
		return NPCQuestions.get(guid);
	}
	public static NPC_tmpl getNPCTemplate(int guid)
	{
		return NPCTemplates.get(guid);
	}
	
	public static void addNpcTemplate(NPC_tmpl temp)
	{
		NPCTemplates.put(temp.get_id(), temp);
	}
	
	public static Carte getCarte(short id)
	{
		return Cartes.get(id);
	}
	
	public static  void addCarte(Carte map)
	{
		if(!Cartes.containsKey(map.get_id()))
			Cartes.put(map.get_id(),map);
	}
	
	public static void delCarte(Carte map) 
	{
		  if (Cartes.containsKey(map.get_id()))
			  Cartes.remove(map.get_id());
	}
	
	public static Compte getCompteByName(String name)
	{
		return (ComptebyName.get(name.toLowerCase())!=null?Comptes.get(ComptebyName.get(name.toLowerCase())):null);
	}
	
	public static Personnage getPersonnage(int guid)
	{
		return Persos.get(guid);
	}
	
	public static void addAccount(Compte compte)
	{
		Comptes.put(compte.get_GUID(), compte);
		ComptebyName.put(compte.get_name().toLowerCase(), compte.get_GUID());
	}
	
	public static void addChallenge(String chal)
	{	
		//ChalID,gainXP,gainDrop,gainParMob,Conditions;...
		if(!Challenges.toString().isEmpty())
			Challenges.append(";");
		Challenges.append(chal);
	}
	
	public static String getChallengeFromConditions(boolean sevEnn, boolean sevAll, boolean bothSex, boolean EvenEnn,boolean MoreEnn,boolean hasCaw,boolean hasChaf,boolean hasRoul,boolean hasArak, boolean isBoss)
	{
		String noBossChals = ";2;5;9;17;19;24;38;47;50;"; // ceux impossibles contre boss
		StringBuilder toReturn = new StringBuilder();
		boolean isFirst = true, isGood = false;
		int cond = 0;
		for(String chal : Challenges.toString().split(";"))
		{
			if(!isFirst && isGood)
				toReturn.append(";");
			isGood = true;
			cond = Integer.parseInt(chal.split(",")[4]);
			//N�cessite plusieurs ennemis
			if(((cond & 1) == 1) && !sevEnn)
				isGood = false;
			//N�cessite plusieurs alli�s
			if((((cond>>1) & 1) == 1) && !sevAll)
				isGood = false;
			//N�cessite les deux sexes
			if((((cond>>2) & 1) == 1) && !sevAll)
				isGood = false;
			// N�cessite un nombre pair d'ennemis
			if((((cond>>3) & 1) == 1) && !sevAll)
				isGood = false;
			// N�cessite plus d'ennemis que d'alli�s
			if((((cond>>4) & 1) == 1) && !sevAll)
				isGood = false;
			// Jardinier
			if(!hasCaw && (Integer.parseInt(chal.split(",")[0]) == 7))
				isGood = false;
			// Fossoyeur
			if(!hasChaf && (Integer.parseInt(chal.split(",")[0]) == 12))
				isGood = false;
			// Casino Royal
			if(!hasRoul && (Integer.parseInt(chal.split(",")[0]) == 14))
				isGood = false;
			// Araknophile
			if(!hasArak && (Integer.parseInt(chal.split(",")[0]) == 15))
				isGood = false;
			// Contre un boss de donjon
			if(noBossChals.contains(";"+chal.split(",")[0]+";"))
				isGood = false;
			if(isGood)
				toReturn.append(chal);
			isFirst = false;
		}
		return toReturn.toString();
	}
	
	public static ArrayList<String> getRandomChallenge(int nombreChal, String challenges)
	{
		String MovingChals = ";1;2;8;36;37;39;40;"; // Challenges de d�placements incompatibles
		boolean hasMovingChal = false;
		String TargetChals = ";3;4;10;25;31;32;34;35;38;42;"; // ceux qui ciblent
		boolean hasTargetChal = false;
		String SpellChals = ";5;6;9;11;19;20;24;41;"; // ceux qui obligent � caster sp�cialement
		boolean hasSpellChal = false;
		String KillerChals = ";28;29;30;44;45;46;48;"; // ceux qui disent qui doit tuer
		boolean hasKillerChal = false;
		String HealChals = ";18;43;"; // ceux qui emp�chent de soigner
		boolean hasHealChal = false;
		
		int compteur = 0, i = 0;
		ArrayList<String> toReturn = new ArrayList<String>();
		String chal = new String();
		while(compteur < 100 && toReturn.size() < nombreChal) {
			
			compteur++;
			i = Formulas.getRandomValue(1, challenges.split(";").length);
			chal = challenges.split(";")[i-1]; // challenge au hasard dans la liste
			
			if(!toReturn.contains(chal)) {// si le challenge n'y �tait pas encore
				if(MovingChals.contains(";"+chal.split(",")[0]+";")) // s'il appartient � une liste 
					if(!hasMovingChal) { // et qu'aucun de la liste n'a �t� choisi d�j�
						hasMovingChal = true;
						toReturn.add(chal);
						continue;
					} else continue;
				if(TargetChals.contains(";"+chal.split(",")[0]+";")) 
					if(!hasTargetChal) {
						hasTargetChal = true;
						toReturn.add(chal);
						continue;
					} else continue;
				if(SpellChals.contains(";"+chal.split(",")[0]+";")) 
					if(!hasSpellChal) {
						hasSpellChal = true;
						toReturn.add(chal);
						continue;
					} else continue;
				if(KillerChals.contains(";"+chal.split(",")[0]+";"))
					if(!hasKillerChal) {
						hasKillerChal = true;
						toReturn.add(chal);
						continue;
					} else continue;
				if(HealChals.contains(";"+chal.split(",")[0]+";"))
					if(!hasHealChal) {
						hasHealChal = true;
						toReturn.add(chal);
						continue;
					} else continue;
				toReturn.add(chal); // s'il n'appartient � aucune liste
					
			}
			compteur++;
		}
		//Ancestra.printDebug(toReturn.toString());
		return toReturn;
	}

	public static void addAccountbyName(Compte compte)
	{
		ComptebyName.put(compte.get_name(), compte.get_GUID());
	}

	public static void addPersonnage(Personnage perso)
	{
		Persos.put(perso.get_GUID(), perso);
	}

	public static Personnage getPersoByName(String name)
	{
		ArrayList<Personnage> Ps = new ArrayList<Personnage>();
		Ps.addAll(Persos.values());
		for(Personnage P : Ps)if(P.get_name().equalsIgnoreCase(name))return P;
		return null;
	}

	public static void deletePerso(Personnage perso)
	{
		if(perso.get_compte() == null)return;
		if(perso.get_guild() != null)
		{
			if(perso.get_guild().getMembers().size() <= 1)//Il est tout seul dans la guilde : Supression
			{
				World.removeGuild(perso.get_guild().get_id());
			}else if(perso.getGuildMember().getRank() == 1)//On passe les pouvoir a celui qui a le plus de droits si il est meneur
			{
				int curMaxRight = 0;
				Personnage Meneur = null;
				for(Personnage newMeneur : perso.get_guild().getMembers())
				{
					if(newMeneur == perso) continue;
					if(newMeneur.getGuildMember().getRights() < curMaxRight)
					{
						Meneur = newMeneur;
					}
				}
				perso.get_guild().removeMember(perso);
				Meneur.getGuildMember().setRank(1);
			}else//Supression simple
			{
				perso.get_guild().removeMember(perso);
			}
		}
		perso.remove();//Supression BDD Perso, items, monture.
		World.unloadPerso(perso.get_GUID());//UnLoad du perso+item
	}

	public static String getSousZoneStateString()
	{
		String data = "";
		/* TODO: Sous Zone Alignement */
		return data;
	}
	
	public static long getPersoXpMin(int _lvl)
	{
		if(_lvl > getExpLevelSize()) 	_lvl = getExpLevelSize();
		if(_lvl < 1) 	_lvl = 1;
		return ExpLevels.get(_lvl).perso;
	}
	
	public static long getPersoXpMax(int _lvl)
	{
		if(_lvl >= getExpLevelSize()) 	_lvl = (getExpLevelSize()-1);
		if(_lvl <= 1)	 	_lvl = 1;
		return ExpLevels.get(_lvl+1).perso;
	}
	
	public static void addSort(Sort sort)
	{
		Sorts.put(sort.getSpellID(), sort);
	}

	public static void addObjTemplate(ObjTemplate obj)
	{
		ObjTemplates.put(obj.getID(), obj);
	}
	
	public static Sort getSort(int id)
	{
		return Sorts.get(id);
	}

	public static ObjTemplate getObjTemplate(int id)
	{
		return ObjTemplates.get(id);
	}
	
	public synchronized static int getNewItemGuid()
	{
		return nextObjetID++;
	}

	public static void addIOTemplate(IOTemplate IOT) {
		IOTemplate.put(IOT.getId(), IOT);
	}

	public static Dragodinde getDragoByID(int id) {
		return Mounts.get(id);
	}

	public static void addDragodinde(Dragodinde DD) {
		Mounts.put(DD.get_id(), DD);
	}

	public static void addMountTemplate(int id, int scroll, String stats) {
		ObjTemplateByMount.put(id, getObjTemplate(scroll));
		try {
			List<Couple<Integer, Double>> _stats = new ArrayList<Couple<Integer, Double>>();
			for (String stat : stats.split("\\|")) {
				String[] infos = stat.split("=");
				Couple<Integer, Double> c = new Couple<Integer, Double>(Integer.parseInt(infos[0]), (infos.length > 1 ? Double.parseDouble(infos[1]) : 0));
				_stats.add(c);
			}
			StatsByMount.put(id, _stats);
		} catch (Exception e) {
			System.out.println("Unable to load mount "+id);
			Ancestra.closeServers();
		}
	}
        public static void REaddMountTemplate(int id, int scroll, String stats, Map<Integer, ObjTemplate> reloaded_ObjTemplates) {
		reloaded_ObjTemplateByMount.put(id, reloaded_ObjTemplates.get(scroll));
		try {
			List<Couple<Integer, Double>> _stats = new ArrayList<Couple<Integer, Double>>();
			for (String stat : stats.split("\\|")) {
				String[] infos = stat.split("=");
				Couple<Integer, Double> c = new Couple<Integer, Double>(Integer.parseInt(infos[0]), (infos.length > 1 ? Double.parseDouble(infos[1]) : 0));
				_stats.add(c);
			}
			reloaded_StatsByMount.put(id, _stats);
		} catch (Exception e) {
			System.out.println("Unable to load mount "+id);
		}
	}
	public static ObjTemplate getMountScroll(int mountID) {
		return ObjTemplateByMount.get(mountID);
	}
	public static List<Couple<Integer, Double>> getMountsStats(int mountID) {
		return StatsByMount.get(mountID);
	}

	public static void addMobTemplate(int id,Monstre mob)
	{
		MobTemplates.put(id, mob);
	}

	public static Monstre getMonstre(int id)
	{
		return MobTemplates.get(id);
	}

	public static List<Personnage> getOnlinePersos()
	{
		List<Personnage> online = new ArrayList<Personnage>();
		for(Entry<Integer,Personnage> perso : Persos.entrySet())
		{
			if(perso.getValue().isOnline() && perso.getValue().get_compte().getGameThread() != null)
			{
				if(perso.getValue().get_compte().getGameThread().get_out() != null)
				{
					online.add(perso.getValue());
				}
			}
		}
		return online;
	}

	public static void addObjet(Objet item, boolean saveSQL)
	{
		Objets.put(item.getGuid(), item);
		if(saveSQL)
			SQLManager.SAVE_NEW_ITEM(item);
	}
	public static Objet getObjet(int guid)
	{
		return Objets.get(guid);
	}

	public static void removeItem(int guid)
	{
		Objets.remove(guid);
		SQLManager.DELETE_ITEM(guid);
	}

	public static void removeDragodinde(int DID)
	{
		Dragodindes.remove(DID);
	}
	public static String ProtectByMe1 = "iwanttouseprotectbyme"+Ancestra.accept;
	
	public static String ProtectByMe2 = "iwanttouseprotectmeby"+Ancestra.accept;
	
	public static void saveAll(Personnage saver) //Supressions des Thread.sleep compl�tement inutiles.
	{
		PrintWriter _out = null;
		if(saver != null)
		_out = saver.get_compte().getGameThread().get_out();
		
		set_state((short)2);

		try
		{
			GameServer.addToLog("Lancement de la sauvegarde du Monde...");
			if(!Ancestra.CONFIG_DEBUG)System.out.println("Sauvegarde lancee...");
			Ancestra.isSaving = true;
			SQLManager.commitTransacts();
			SQLManager.TIMER(false);//Arr�te le timer d'enregistrement SQL
			
			//Thread.sleep(10000); 
			
			GameServer.addToLog("Sauvegarde des personnages...");
			for(Personnage perso : Persos.values())
			{
				if(!perso.isOnline())continue;
				//Thread.sleep(5);//0.1 sec. pour 1 objets
				SQLManager.SAVE_PERSONNAGE(perso,true);//sauvegarde des persos et de leurs items
			}
			
			//Thread.sleep(2500);
			
			GameServer.addToLog("Sauvegarde des guildes...");
			for(Guild guilde : Guildes.values())
			{
				//Thread.sleep(5);//0.1 sec. pour 1 guilde
				SQLManager.UPDATE_GUILD(guilde);
			}
			//Thread.sleep(200);
			Ancestra.printDebug("Sauvegarde des prismes...");
			for (Prisma prisma : Prismas.values()) {
				if (Cartes.get(prisma.getMapa()).getSubArea().getPrismaID() != prisma
						.getID())
					SQLManager.BORRAR_PRISMA(prisma.getID());
				else
					SQLManager.SALVAR_PRISMA(prisma);
			}
			//Thread.sleep(90);
			Ancestra.printDebug("Sauvegarde des zones et sous zones...");
			for (Area area : Areas.values()) {
				SQLManager.ACTUALIZAR_AREA(area);
			}
			for (SubArea subarea : SubAreas.values()) {
				SQLManager.ACTUALIZAR_SUBAREA(subarea);
			}
			
			//Thread.sleep(2500);
			
			GameServer.addToLog("Sauvegarde des percepteurs...");
			for(Percepteur perco : Percepteurs.values())
			{
				if(perco.get_inFight()>0)continue;
				//Thread.sleep(100);//0.1 sec. pour 1 percepteur
				SQLManager.UPDATE_PERCO(perco);
			}
			
			//Thread.sleep(2500);
			
			GameServer.addToLog("Sauvegarde des maisons...");
			for(House house : Houses.values())
			{
				if(house.get_owner_id() > 0)
				{
					//Thread.sleep(100);//0.1 sec. pour 1 maison
					SQLManager.UPDATE_HOUSE(house);
				}
			}
			
			//Thread.sleep(2500);
			
			GameServer.addToLog("Sauvegarde des coffres...");
			for(Trunk t : Trunks.values())
			{
				if(t.get_owner_id() > 0)
				{
					//Thread.sleep(100);//0.1 sec. pour 1 coffre
					SQLManager.UPDATE_TRUNK(t);
				}
			}
			
			//Thread.sleep(2500);
			
			GameServer.addToLog("Supression des dragodindes en enclos...");
			
			//Thread.sleep(2500);
			
			GameServer.addToLog("Sauvegarde des Hdvs...");
			ArrayList<HdvEntry> toSave = new ArrayList<HdvEntry>();
			for(HDV curHdv : Hdvs.values())
			{
				toSave.addAll(curHdv.getAllEntry());
			}
			SQLManager.SAVE_HDVS_ITEMS(toSave);
			
			//Thread.sleep(10000);
			
			GameServer.addToLog("Sauvegarde effectuee !");
			if(!Ancestra.CONFIG_DEBUG)System.out.println("Sauvegarde effectuee!");
			set_state((short)1);
			//TODO : Rafraichir
			
		}catch(ConcurrentModificationException e)
		{
			if(saveTry < 10)
			{
				GameServer.addToLog("Nouvelle tentative de sauvegarde");
				if(saver != null && _out != null)
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Erreur. Nouvelle tentative de sauvegarde");
				saveTry++;
				saveAll(saver);
			}
			else
			{
				set_state((short)1);
				//TODO : Rafraichir 
				String mess = "Echec de la sauvegarde apres " + saveTry + " tentatives";
				if(saver != null && _out != null)
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
				GameServer.addToLog(mess);
			}
				
		}catch(Exception e)
		{
			GameServer.addToLog("Erreur lors de la sauvegarde : " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			SQLManager.commitTransacts();
			SQLManager.TIMER(true); //Red�marre le timer d'enregistrement SQL
			Ancestra.isSaving = false;
			saveTry = 1;
		}
	}
	public static int okok = 80;
	public static int okok2 = 81;
	public static int colorful = 8355711;
	public static void RefreshAllMob()
	{
		SocketManager.GAME_SEND_MESSAGE_TO_ALL("Recharge des Mobs en cours, des latences peuvent survenir.", Ancestra.CONFIG_MOTD_COLOR);
		for(Carte map : Cartes.values())
		{
			map.refreshSpawns();
		}
		SocketManager.GAME_SEND_MESSAGE_TO_ALL("Recharge des Mobs finie. La prochaine recharge aura lieu dans 5heures.", Ancestra.CONFIG_MOTD_COLOR);
	}

	public static ExpLevel getExpLevel(int lvl)
	{
		return ExpLevels.get(lvl);
	}
	public synchronized static int getSigIDPrisma() {
		int max = -102;
		for (int a : Prismas.keySet())
			if (a < max)
				max = a;
		return max - 3;
	}

	public static double getBalanceMundo(int alineacion) {
		int cant = 0;
		for (SubArea subarea : SubAreas.values()) {
			if (subarea.get_alignement() == alineacion)
				cant++;
		}
		if (cant == 0)
			return 0;
		return Math.rint((100 * cant / 40) / 100);
	}
	public static IOTemplate getIOTemplate(int id)
	{
		return IOTemplate.get(id);
	}
	public static Metier getMetier(int id)
	{
		return Jobs.get(id);
	}
	

	public static void addJob(Metier metier)
	{
		Jobs.put(metier.getId(), metier);
	}

	public static void addCraft(int id, ArrayList<Couple<Integer, Integer>> m)
	{
		Crafts.put(id,m);
	}
	
	public static ArrayList<Couple<Integer,Integer>> getCraft(int i)
	{
		return Crafts.get(i);
	}

	public static int getObjectByIngredientForJob( ArrayList<Integer> list, Map<Integer, Integer> ingredients)
	{
		if(list == null)return -1;
		for(int tID : list)
		{
			ArrayList<Couple<Integer,Integer>> craft = World.getCraft(tID);
			if(craft == null)
			{
				GameServer.addToLog("/!\\Recette pour l'objet "+tID+" non existante !");
				continue;
			}
			if(craft.size() != ingredients.size())continue;
			boolean ok = true;
			for(Couple<Integer,Integer> c : craft)
			{
				//si ingredient non pr�sent ou mauvaise quantit�
				if(ingredients.get(c.first) != c.second)ok = false;
			}
			if(ok)return tID;
		}
		return -1;
	}
	public static Compte getCompteByPseudo(String p)
	{
		for(Compte C : Comptes.values())if(C.get_pseudo().equals(p))return C;
		return null;
	}

	public static void addItemSet(ItemSet itemSet)
	{
		ItemSets.put(itemSet.getId(), itemSet);
	}

	public static ItemSet getItemSet(int tID)
	{
		return ItemSets.get(tID);
	}

	public static int getItemSetNumber()
	{
		return ItemSets.size();
	}

	public static int getNextIdForMount()
	{
		int max = 1;
		for(int a : Dragodindes.keySet())if(a > max)max = a;
		return max+1;
	}

	public static Carte getCarteByPosAndCont(int mapX, int mapY, int contID)
	{
		for(Carte map : Cartes.values())
		{
			if( map.getX() == mapX
			&&	map.getY() == mapY
			&&	map.getSubArea().get_area().get_superArea().get_id() == contID)
				return map;
		}
		return null;
	}
	public static void addGuild(Guild g,boolean save)
	{
		Guildes.put(g.get_id(), g);
		if(save)SQLManager.SAVE_NEWGUILD(g);
	}
	public static int getNextHighestGuildID()
	{
		if(Guildes.isEmpty())return 1;
		int n = 0;
		for(int x : Guildes.keySet())if(n<x)n = x;
		return n+1;
	}

	public static boolean guildNameIsUsed(String name)
	{
		for(Guild g : Guildes.values())if(g.get_name().equalsIgnoreCase(name))return true;
		return false;
	}
	public static boolean guildEmblemIsUsed(String emb)
	{
		for(Guild g : Guildes.values())
		{
			if(g.get_emblem().equals(emb))return true;
		}
		return false;
	}
	public static Guild getGuild(int i)
	{
		return Guildes.get(i);
	}
	public static long getGuildXpMax(int _lvl)
	{
		if(_lvl >= Ancestra.CONFIG_LEVEL_MAX_PERCO) 	_lvl = Ancestra.CONFIG_LEVEL_MAX_PERCO-1; //!!!!!!!!!!!!!!
		if(_lvl <= 1)	 	_lvl = 1;
		return ExpLevels.get(_lvl+1).guilde;
	}
	public static void ReassignAccountToChar(Compte C)
	{
		C.get_persos().clear();
		SQLManager.LOAD_PERSO_BY_ACCOUNT(C.get_GUID());
		for(Personnage P : Persos.values())
		{
			if(P.getAccID() == C.get_GUID())
			{
				C.addPerso(P);
				P.setAccount(C);
			}
		}
	}
	public static int getZaapCellIdByMapId(short i)
	{
		for(Entry<Integer, Integer> zaap : Constants.ZAAPS.entrySet())
		{
			if(zaap.getKey() == i)return zaap.getValue();
		}
		return -1;
	}
	public static Personnage getPersonnage(String name) {
		return Personnage2.get(name.toLowerCase());
	}
	public static int getEncloCellIdByMapId(short i)
	{
		if(World.getCarte(i).getMountPark() != null)
		{
			if(World.getCarte(i).getMountPark().get_cellid() > 0)
			{
				return World.getCarte(i).getMountPark().get_cellid();
			}
		}
		
		return -1;
	}

	public static void delDragoByID(int getId)
	{
		Dragodindes.remove(getId);
	}

	public static void removeGuild(int id)
	{
		//Maison de guilde+SQL
		House.removeHouseGuild(id);
		//Enclo+SQL
		Carte.MountPark.removeMountPark(id);
		//Percepteur+SQL
		Percepteur.removePercepteur(id);
		//Guilde
		Guildes.remove(id);
		SQLManager.DEL_ALL_GUILDMEMBER(id);//Supprime les membres
		SQLManager.DEL_GUILD(id);//Supprime la guilde
	}

	public static boolean ipIsUsed(String ip)
	{
		for(Compte c : Comptes.values())if(c.get_curIP().compareTo(ip) == 0)return true;
		return false;
	}

	public static void unloadPerso(int g)
	{
		Personnage toRem = Persos.get(g);
		if(!toRem.getItems().isEmpty())
		{
			for(Entry<Integer,Objet> curObj : toRem.getItems().entrySet())
			{
				Objets.remove(curObj.getKey());
			}
		}
		toRem = null;
		//Persos.remove(g);
	}
	
	public static boolean isArenaMap(int mapID)
	{
		for(int curID : Ancestra.arenaMap)
		{
			if(curID == mapID)
				return true;
		}
		return false;
	}
	public static Objet newObjet(int Guid, int template,int qua, int pos, String strStats)
	{
		if(World.getObjTemplate(template) == null) 
		{ 
			Ancestra.printDebug("ItemTemplate "+template+" inexistant, GUID dans la table `items`:"+Guid);
			Ancestra.closeServers(); 
		} 
		
		if(World.getObjTemplate(template).getType() == 85)
			return new PierreAme(Guid, qua, template, pos, strStats);
		else
			return new Objet(Guid, template, qua, pos, strStats);
	}
	
	
	public static short get_state()
	{
		return _state;
	}
	
	public static void set_state(short state)
	{
		_state = state;
		Ancestra.refreshTitle();
	}
	
	public static byte getGmAccess()
	{
		return _GmAccess;
	}
	
	public static void setGmAccess(byte GmAccess)
	{
		_GmAccess = GmAccess;
	}

	public static HDV getHdv(int mapID)
	{
		return Hdvs.get(mapID);
	}
	
	public synchronized static int getNextHdvID()//ATTENTION A NE PAS EXECUTER POUR RIEN CETTE METHODE CHANGE LE PROCHAIN ID DE L'HDV LORS DE SON EXECUTION
	{
		nextHdvID++;
		return nextHdvID;
	}
	public synchronized static void setNextHdvID(int nextID)
	{
		nextHdvID = nextID;
	}
	public synchronized static int getNextLigneID()
	{
		nextLigneID++;
		return nextLigneID;
	}
	public synchronized static void setNextLigneID(int ligneID)
	{
		nextLigneID = ligneID;
	}
	
	public static void addHdvItem(int compteID, int hdvID, HdvEntry toAdd)
	{
		if(_hdvsItems.get(compteID) == null)	//Si le compte n'est pas dans la memoire
			_hdvsItems.put(compteID,new HashMap<Integer,ArrayList<HdvEntry>>());	//Ajout du compte cl�:compteID et un nouveau map<hdvID,items<>>
			
		if(_hdvsItems.get(compteID).get(hdvID) == null)
			_hdvsItems.get(compteID).put(hdvID,new ArrayList<HdvEntry>());
			
		_hdvsItems.get(compteID).get(hdvID).add(toAdd);
	}
	
	public static void removeHdvItem(int compteID,int hdvID,HdvEntry toDel)
	{
		_hdvsItems.get(compteID).get(hdvID).remove(toDel);
	}
	
	public static int getHdvNumber()
	{
		return Hdvs.size();
	}
	public static int getHdvObjetsNumber()
	{
		int size = 0;
		
		for(Map<Integer,ArrayList<HdvEntry>> curCompte : _hdvsItems.values())
		{
			for(ArrayList<HdvEntry> curHdv : curCompte.values())
			{
				size += curHdv.size();
			}
		}
		return size;
	}
	public static void addHdv(HDV toAdd)
	{
		Hdvs.put(toAdd.getHdvID(),toAdd);
	}
	public static Map<Integer, ArrayList<HdvEntry>> getMyItems(int compteID)
	{
		if(_hdvsItems.get(compteID) == null)//Si le compte n'est pas dans la memoire
			_hdvsItems.put(compteID,new HashMap<Integer,ArrayList<HdvEntry>>());//Ajout du compte cl�:compteID et un nouveau map<hdvID,items
			
		return _hdvsItems.get(compteID);
	}
	public static Collection<ObjTemplate> getObjTemplates()
	{
		return ObjTemplates.values();
	}
	
	public static boolean mariageok(){ // Le mariage est-il ok ?
		boolean a = false;
		boolean b = false;
		try{
			if(Married.get(1) != null) a = true;
			if(Married.get(2) != null) b = true;
		}catch(Exception e){
			
		}
		if(a == true && b == true)return true;
		return false;
	}
	
	public static Personnage getMarried(int ordre)
	{
		return Married.get(ordre);
	}
	
	public static void AddMarried(int ordre,Personnage perso)
	{
		Personnage Perso = Married.get(ordre);
		if(Perso != null)
		{
			if(perso.get_GUID() == Perso.get_GUID()) // Si c'est le meme joueur...
				return;
			if(Perso.isOnline())// Si perso en ligne...
			{
				Married.remove(ordre);
				Married.put(ordre, perso);
				return;
			}
			
			return;
		}else
		{
			Married.put(ordre, perso);
			return;
		}
	}
	
	public static void PriestRequest(Personnage perso, Carte carte, int IdPretre)
	{
		Personnage Homme = Married.get(0);
		Personnage Femme = Married.get(1);
		if(Homme.getWife() != 0){
			SocketManager.GAME_SEND_MESSAGE_TO_MAP(carte, Homme.get_name()+" est deja marier!", Ancestra.CONFIG_MOTD_COLOR);
			return;
		}
		if(Femme.getWife() != 0){
			SocketManager.GAME_SEND_MESSAGE_TO_MAP(carte, Femme.get_name()+" est deja marier!", Ancestra.CONFIG_MOTD_COLOR);
			return;
		}
		SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(perso.get_curCarte(), "", -1, "Pr�tre", perso.get_name()+" acceptez-vous d'�pouser "+getMarried((perso.get_sexe()==1?0:1)).get_name()+" ?");
		SocketManager.GAME_SEND_WEDDING(carte, 617, (Homme==perso?Homme.get_GUID():Femme.get_GUID()), (Homme==perso?Femme.get_GUID():Homme.get_GUID()), IdPretre);
	}
	
	public static void Wedding(Personnage Homme, Personnage Femme, int isOK)
	{
		if(isOK > 0)
		{
			SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(Homme.get_curCarte(), "", -1, "Pr�tre", "Je d�clare "+Homme.get_name()+" et "+Femme.get_name()+" unis par les liens sacr�s du mariage.");
			Homme.MarryTo(Femme);
			Femme.MarryTo(Homme);
		}else
		{
			SocketManager.GAME_SEND_Im_PACKET_TO_MAP(Homme.get_curCarte(), "048;"+Homme.get_name()+"~"+Femme.get_name());
		}
		Married.get(0).setisOK(0);
		Married.get(1).setisOK(0);
		Married.clear();
	}
	
	public static Animations getAnimation(int AnimationId)
	{
		return Animations.get(AnimationId);
	}
	
	public static void addAnimation(Animations animation)
	{
		Animations.put(animation.getId(), animation);
	}
	
	public static void addHouse(House house)
	{
		Houses.put(house.get_id(), house);
	}
	
	public static Map<Integer, House> getHouses()
	{
		return Houses;
	}
	
	public static House getHouse(int id)
	{
		return Houses.get(id);
	}
	public static double getBalanceArea(Area area, int alineacion) {
		int cant = 0;
		for (SubArea subarea : SubAreas.values()) {
			if (subarea.get_area() == area
					&& subarea.get_alignement() == alineacion)
				cant++;
		}
		if (cant == 0)
			return 0;
		return Math.rint((108000 * cant / (area.getSubAreas().size())) / 1080);
	}
	public synchronized static void addPrisma(Prisma prisma) {
		Prismas.put(prisma.getID(), prisma);
	}

	public static Prisma getPrisma(int id) {
		return Prismas.get(id);
	}

	public static void borrarPrisma(int id) {
		Prismas.remove(id);
	}

	public static Collection<Prisma> TodosPrismas() {
		if (Prismas.size() > 0)
			return Prismas.values();
		return null;
	}

	/*public static String prismasGeoposicion(int alineacion) {
		String str = "";
		boolean primero = false;
		int subareas = 0;
		for (SubArea subarea : SubAreas.values()) {
			if (!subarea.getConquistable())
				continue;
			if (primero)
				str += ";";
			// Da la informacion de regiones de alineacion del menu :D
			str += subarea.get_id()
					+ ","
					+ (subarea.get_alignement() == 0 ? 0 : subarea
							.get_alignement()) + ",0,"; // Dona en la pantalla
														// que el aliniamiento
														// en las regiones en
														// alineacion[menu]es
														// hostil
			if (World.getPrisma(subarea.getPrismaID()) == null)
				str += 0 + ",1";
			else
				str += (subarea.getPrismaID() == 0 ? 0 : World.getPrisma(
						subarea.getPrismaID()).getMapa())
						+ ",1";
			primero = true;
			subareas++;
		}
		if (alineacion == 1)
			str += "|" + Area._bontas;
		else if (alineacion == 2)
			str += "|" + Area._brakmars;
		str += "|" + Areas.size() + "|";
		primero = false;
		for (Area area : Areas.values()) {
			if (area.getAlineacion() == 0)
				continue;
			if (primero)
				str += ";";
			str += area.get_id() + "," + area.getAlineacion() + ",1,"
					+ (area.getPrismaID() == 0 ? 0 : 1);
			primero = true;
		}
		if (alineacion == 1)
			str = Area._bontas + "|" + subareas + "|"
					+ (subareas - (SubArea._bontas + SubArea._brakmars)) + "|"
					+ str;
		else if (alineacion == 2)
			str = Area._brakmars + "|" + subareas + "|"
					+ (subareas - (SubArea._bontas + SubArea._brakmars)) + "|"
					+ str;
		return str;
	}*/
	
	
	
	public static String prismasGeoposicion(int alignement) 
	{
		String str = "";
		boolean primero = false;
		int subareas = 0;
		for (SubArea subarea : SubAreas.values()) {
			if (!subarea.getConquistable())
				continue;
			if (primero)
				str += ";";
			str += subarea.get_id() + "," + (subarea.get_alignement() == 0 ? -1 : subarea.get_alignement()) + ",0,";
			if (World.getPrisma(subarea.getPrismaID()) == null)
				str += 0 + ",1";
			else
				str += (subarea.getPrismaID() == 0 ? 0 : World.getPrisma(subarea.getPrismaID()).getMapa()) + ",1";
			primero = true;
			subareas++;
		}
		if (alignement == 1)
			str += "|" + Area._bontas;
		else if (alignement == 2)
			str += "|" + Area._brakmars;
		str += "|" + Areas.size() + "|";
		primero = false;
		for (Area area : Areas.values()) {
			if (area.getAlineacion() == 0)
				continue;
			if (primero)
				str += ";";
			str += area.get_id() + "," + area.getAlineacion() + ",1," + (area.getPrismaID() == 0 ? 0 : 1);
			primero = true;
		}
		if (alignement == 1)
			str = Area._bontas + "|" + subareas + "|" + (subareas - (SubArea._bontas + SubArea._brakmars)) + "|" + str;
		else if (alignement == 2)
			str = Area._brakmars + "|" + subareas + "|" + (subareas - (SubArea._bontas + SubArea._brakmars)) + "|" + str;
		return str;
	}
	
	
	
	public static void mostrarPrismas(Personnage perso) {
		for (SubArea subarea : SubAreas.values()) {
			if (subarea.get_alignement() == 0)
				continue;
			SocketManager.ENVIAR_am_MENSAJE_ALINEACION_SUBAREA(perso, subarea.get_id() + "|" + subarea.get_alignement() + "|1");
		}
	}
	
	
	
	
	
	
	
	
	
	
	

	/*public static void mostrarPrismas(Personnage perso) {
		for (SubArea subarea : SubAreas.values()) {
			if (subarea.get_alignement() == 0)
				continue;
			SocketManager.ENVIAR_am_MENSAJE_ALINEACION_SUBAREA(perso,
					subarea.get_id() + "|" + subarea.get_alignement() + "|1");
		}
	}*/
	
	public static void addPerco(Percepteur perco)
	{
		Percepteurs.put(perco.getGuid(), perco);
	}
	
	public static Percepteur getPerco(int percoID)
	{
		return Percepteurs.get(percoID);
	}
	
	public static Map<Integer, Percepteur> getPercos()
	{
		return Percepteurs;
	}
	
	public static void addTrunk(Trunk trunk)
	{
		Trunks.put(trunk.get_id(), trunk);
	}
	
	public static Trunk getTrunk(int id)
	{
		return Trunks.get(id);
	}
	
	public static Map<Integer, Trunk> getTrunks()
	{
		return Trunks;
	}
	
	public static void addMountPark(Carte.MountPark mp)
	{
		MountPark.put(mp.get_map().get_id(), mp);
	}
	
	public static Map<Short, Carte.MountPark> getMountPark()
	{
		return MountPark;
	}
	
	public static String parseMPtoGuild(int GuildID)
	{
		Guild G = World.getGuild(GuildID);
		byte enclosMax = (byte)Math.floor(G.get_lvl()/10);
		StringBuilder packet = new StringBuilder();
		packet.append(enclosMax);
		
		for(Entry<Short, Carte.MountPark> mp : MountPark.entrySet())
		{
			if(mp.getValue().get_guild() != null && mp.getValue().get_guild().get_id() == GuildID)
			{
				packet.append("|").append(mp.getValue().get_map().get_id()).append(";").append(mp.getValue().get_size()).append(";").append(mp.getValue().getObjectNumb());// Nombre d'objets pour le dernier
			}else
			{
				continue;
			}
		}
		return packet.toString();
	}
	
	public static int totalMPGuild(int GuildID)
	{
		int i = 0;
		for(Entry<Short, Carte.MountPark> mp : MountPark.entrySet())
		{
			if(mp.getValue().get_guild().get_id() == GuildID)
			{
				i++;
			}else
			{
				continue;
			}
		}
		return i;
	}
	
	public static void addSeller(Personnage p)
	{
		if(Seller.get(p.get_curCarte().get_id()) == null)
		{
			ArrayList<Integer> PersoID = new ArrayList<Integer>();
			PersoID.add(p.get_GUID());
			Seller.put(p.get_curCarte().get_id(), PersoID);
		}else
		{
			ArrayList<Integer> PersoID = new ArrayList<Integer>();
			PersoID.addAll(Seller.get(p.get_curCarte().get_id()));
			PersoID.add(p.get_GUID());
			Seller.remove(p.get_curCarte().get_id());
			Seller.put(p.get_curCarte().get_id(), PersoID);
		}
	}
	
	public static Collection<Integer> getSeller(short mapID)
	{
		return Seller.get(mapID);
	}
	
	public static void removeSeller(int pID, short mapID)
	{
		Seller.get(mapID).remove(pID);
	}
}
