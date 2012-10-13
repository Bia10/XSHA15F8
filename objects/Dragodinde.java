package objects;

import game.GameServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import common.Constants;
import common.Formulas;
import common.SQLManager;
import common.SocketManager;
import common.World;

import objects.Personnage.Stats;

public class Dragodinde {

	private int _id;
	private int _color;
	private int _sexe;
	private int _amour;
	private int _endurance;
	private int _level;
	private long _exp;
	private String _nom;
	private int _fatigue;
	private int _energie;
	private int _reprod;
	private int _maturite;
	private int _serenite;
	private Stats _stats = new Stats();
	private String _ancetres = ",,,,,,,,,,,,,";
	private Map<Integer,Objet> _items = new TreeMap<Integer,Objet>();
	private List<Integer> capacite = new ArrayList<Integer>();
	private String _ability = ",";
	
	public Dragodinde(int color)
	{
		_id = World.getNextIdForMount();
		_color = color;
		_sexe = Formulas.getRandomValue(0, 1);
		_level = 1;
		_exp = 0;
		_nom = "SansNom";
		_fatigue = 0;
		_energie = getMaxEnergie();
		_reprod = 0;
		_maturite = getMaxMatu();
		_serenite = 0;
		_stats = Constants.getStats(_color,_level);
		_ancetres = ",,,,,,,,,,,,,";
		_ability = ""+Formulas.getChanceCapa(1, 4, 9)+"";
		
		World.addDragodinde(this);
		SQLManager.CREATE_MOUNT(this);
	}
	
	public Dragodinde(int id, int color, int sexe, int amour, int endurance,
			int level, long exp, String nom, int fatigue,
			int energie, int reprod, int maturite, int serenite,String items,String anc, String ability)
	{
		_id = id;
		_color = color;
		_sexe = sexe;
		_amour = amour;
		_endurance = endurance;
		_level = level;
		_exp = exp;
		_nom = nom;
		_fatigue = fatigue;
		_energie = energie;
		_reprod = reprod;
		_maturite = maturite;
		_serenite = serenite;
		_ancetres = anc;
		_stats = Constants.getStats(_color,_level);
		_ability = ability;
		for (String s : ability.split(",", 2))
			if (s != null) {
				int a = Integer.parseInt(s);
				try {
					this.capacite.add(Integer.valueOf(a));
				} catch (Exception localException) {}
			}
		for(String item : items.split("\\|"))
		{
			if(item.equals(""))continue;
			String[] infos = item.split(":");
			int guid = Integer.parseInt(infos[0]);

			Objet obj = World.getObjet(guid);
			if( obj == null)continue;
			_items.put(obj.getGuid(), obj);
		}
	}

	public int get_id() {
		return _id;
	}

	public int get_color() {
		return _color;
	}
	
	public String get_color(String a)
	{
		String b = "";
		if (capacite.contains(Integer.valueOf(9))) 
			b = b + "," + a;
		return _color + b;
	}

	public int get_sexe() {
		return _sexe;
	}

	public int get_amour() {
		return _amour;
	}

	public String get_ancetres() {
		return _ancetres;
	}

	public int get_endurance() {
		return _endurance;
	}
	public int get_level() {
		return _level;
	}

	public long get_exp() {
		return _exp;
	}

	public String get_nom() {
		return _nom;
	}

	public int get_fatigue() {
		return _fatigue;
	}

	public int get_energie() {
		return _energie;
	}

	public int get_reprod() {
		return _reprod;
	}

	public int get_maturite() {
		return _maturite;
	}

	public int get_serenite() {
		return _serenite;
	}

	public Stats get_stats() {
		return _stats;
	}

	public Map<Integer, Objet> get_items()
	{
		return _items;
	}
	
	public void CastrerDinde() {
		_reprod = -1;
	}
	
	public String parse()
	{
		StringBuilder str = new StringBuilder();
		str.append(_id).append(":");
		str.append(_color).append(":");
		str.append(_ancetres).append(":");
		str.append(",,").append(_ability).append(":");//FIXME capacités
		str.append(_nom).append(":");
		str.append(_sexe).append(":");
		str.append(parseXpString()).append(":");
		str.append(_level).append(":");
		str.append("1").append(":");//FIXME
		str.append(getTotalPod()).append(":");
		str.append("0").append(":");//FIXME podActuel?
		str.append(_endurance).append(",10000:");
		str.append(_maturite).append(",").append(getMaxMatu()).append(":");
		str.append(_energie).append(",").append(getMaxEnergie()).append(":");
		str.append(_serenite).append(",-10000,10000:");
		str.append(_amour).append(",10000:");
		str.append("-1").append(":");//FIXME
		str.append("0").append(":");//FIXME
		str.append(parseStats()).append(":");
		str.append(_fatigue).append(",240:");
		str.append(_reprod).append(",20:");
		return str.toString();
	}

	private String parseStats()
	{
		String stats = "";
		for(Entry<Integer,Integer> entry : _stats.getMap().entrySet())
		{
			if(entry.getValue() <= 0)continue;
			if(stats.length() >0)stats += ",";
			stats += Integer.toHexString(entry.getKey())+"#"+Integer.toHexString(entry.getValue())+"#0#0";
		}
		return stats;
	}

	private int getMaxEnergie()
	{
		if (isInfatiguable() == true)
		return 2000 + _level * 130;
		return 1000 + _level * 90;
	}

	private int getMaxMatu()
	{
		int matu = 1000;
		return matu;
	}

	private int getTotalPod()
	{
		if(isPorteuse() == true)
			return 500 + _level * 25;
		return 500 + _level * 15;
	}
	
	public int getMaxPod() {
		return getTotalPod();
	}

	private String parseXpString()
	{
		return _exp+","+World.getExpLevel(_level).dinde+","+World.getExpLevel(_level+1).dinde;
	}

	public boolean isMountable()
	{
		if(_energie <10
		|| _maturite < getMaxMatu()
		|| _fatigue == 240)return false;
		return true;
	}

	public void setName(String packet)
	{
		_nom = packet;
		SQLManager.UPDATE_MOUNT_INFOS(this);
	}
	
	public void addXp(long amount)
	{
		if(isSage() == true) {
		_exp += amount * 2;
		}
		_exp += amount;
		while(_exp >= World.getExpLevel(_level+1).dinde && _level<100)
			levelUp();
		
	}
	
	public void levelUp()
	{
		_level++;
		_stats = Constants.getStats(_color,_level);
		if(isInfatiguable() == true) {
			_energie = _energie + 130;
			if(_energie > getMaxEnergie()) _energie = getMaxEnergie();
		} else {
			_energie = _energie + 90;
			if(_energie > getMaxEnergie()) _energie = getMaxEnergie();
		}
	}
	
	public String get_ability() {
		return _ability;
	}
	
	public boolean addCapacity(String capacites) {
		int c = 0;
		for (String s : capacites.split(",", 2)) {
			if (capacite.size() >= 2) 
				return false; 
			try
			{
				c = Integer.parseInt(s); 
			} catch (Exception localException) {}
			
			if (c != 0)
				capacite.add(Integer.valueOf(c));
			
			if (capacite.size() == 1)
				_ability = (capacite.get(0) + ",");
			else
				_ability = (capacite.get(0) + "," + this.capacite.get(1));
		}
		return true;
	}
	
	public boolean isInfatiguable() {
		return capacite.contains(Integer.valueOf(1));
	}
	
	public boolean isPorteuse() {
		return capacite.contains(Integer.valueOf(2));
	}
	
	public boolean isSage() {
		return capacite.contains(Integer.valueOf(4));
	}

	public boolean isCameleone() {
		return capacite.contains(Integer.valueOf(9));
	}
	
	public void setEnergie(int energie){
        _energie = energie; 
        if(_energie > getMaxEnergie())
        	_energie = getMaxEnergie();
    }
	
	//TODO: Debug de l'inventaire des dindes
	public int get_podsActuels() {
		int pods = 0;
		for(Objet value : _items.values())
		{
			pods = value.getTemplate().getPod() * value.getQuantity(); 
		}
		return pods;
	}
	
	public void addInDinde(int guid, int qua, Personnage P)
	{	
		Objet PersoObj = World.getObjet(guid);
		if(PersoObj == null) return;
		//Si le joueur n'a pas l'item dans son sac ...
		if(P.getItems().get(guid) == null)
		{
			GameServer.addToLog("Le joueur "+P.get_name()+" a tenter d'ajouter un objet dans sa dragodinde qu'il n'avait pas.");
			return;
		}
		
		String str = "";
		
		//Si c'est un item équipé ...
		if(PersoObj.getPosition() != Constants.ITEM_POS_NO_EQUIPED)return;
		
		Objet TrunkObj = getSimilarDindeItem(PersoObj);
		int newQua = PersoObj.getQuantity() - qua;
		if(TrunkObj == null)//S'il n'y pas d'item du meme Template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0)
			{
				//On enleve l'objet du sac du joueur
				P.removeItem(PersoObj.getGuid());
				//On met l'objet du sac dans le coffre, avec la meme quantité
				_items.put(PersoObj.getGuid() ,PersoObj);
				str = "O+"+PersoObj.getGuid()+"|"+PersoObj.getQuantity()+"|"+PersoObj.getTemplate().getID()+"|"+PersoObj.parseStatsString();
				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(P, guid);
				
			}
			else//S'il reste des objets au joueur
			{
				//on modifie la quantité d'item du sac
				PersoObj.setQuantity(newQua);
				//On ajoute l'objet au coffre et au monde
				TrunkObj = Objet.getCloneObjet(PersoObj, qua);
				World.addObjet(TrunkObj, true);
				_items.put(TrunkObj.getGuid() ,TrunkObj);
				
				//Envoie des packets
				str = "O+"+TrunkObj.getGuid()+"|"+TrunkObj.getQuantity()+"|"+TrunkObj.getTemplate().getID()+"|"+TrunkObj.parseStatsString();
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(P, PersoObj);
				
			}
		}else // S'il y avait un item du meme template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0)
			{
				//On enleve l'objet du sac du joueur
				P.removeItem(PersoObj.getGuid());
				//On enleve l'objet du monde
				World.removeItem(PersoObj.getGuid());
				//On ajoute la quantité a l'objet dans le coffre
				TrunkObj.setQuantity(TrunkObj.getQuantity() + PersoObj.getQuantity());
				//on envoie l'ajout au coffre de l'objet
			    str = "O+"+TrunkObj.getGuid()+"|"+TrunkObj.getQuantity()+"|"+TrunkObj.getTemplate().getID()+"|"+TrunkObj.parseStatsString();
				//on envoie la supression de l'objet du sac au joueur
				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(P, guid);
				
			}else //S'il restait des objets
			{
				//on modifie la quantité d'item du sac
				PersoObj.setQuantity(newQua);
				TrunkObj.setQuantity(TrunkObj.getQuantity() + qua);
				str = "O+"+TrunkObj.getGuid()+"|"+TrunkObj.getQuantity()+"|"+TrunkObj.getTemplate().getID()+"|"+TrunkObj.parseStatsString();
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(P, PersoObj);
				
			}
		}
		
		for(Personnage perso : P.get_curCarte().getPersos())
		{
			if(perso.getInTrunk() != null && get_id() == perso.getInTrunk().get_id())
			{
				SocketManager.GAME_SEND_EsK_PACKET(perso, str);
			}
		}
		
		SocketManager.GAME_SEND_Ow_PACKET(P);
		SocketManager.GAME_SEND_Ew_PACKET(P, get_podsActuels(), getTotalPod());
		SQLManager.UPDATE_MOUNT_INFOS(this);
	}
	
	public void removeFromDinde(int guid, int qua, Personnage P)
	{
		if(P.getInTrunk().get_id() != get_id()) return;
		
		Objet TrunkObj = World.getObjet(guid);
		//Si le joueur n'a pas l'item dans son coffre
		if(_items.get(guid) == null)
		{
			GameServer.addToLog("Le joueur "+P.get_name()+" a tenter de retirer un objet dans sa dragodinde qu'il n'avait pas.");
			return;
		}
		
		Objet PersoObj = P.getSimilarItem(TrunkObj);
		
		String str = "";
		
		int newQua = TrunkObj.getQuantity() - qua;
		
		if(PersoObj == null)//Si le joueur n'avait aucun item similaire
		{
			//S'il ne reste rien dans le coffre
			if(newQua <= 0)
			{
				//On retire l'item du coffre
				_items.remove(guid);
				//On l'ajoute au joueur
				P.getItems().put(guid, TrunkObj);
				
				//On envoie les packets
				SocketManager.GAME_SEND_OAKO_PACKET(P,TrunkObj);
				str = "O-"+guid;
				
			}else //S'il reste des objets dans le coffre
			{
				//On crée une copy de l'item dans le coffre
				PersoObj = Objet.getCloneObjet(TrunkObj, qua);
				//On l'ajoute au monde
				World.addObjet(PersoObj, true);
				//On retire X objet du coffre
				TrunkObj.setQuantity(newQua);
				//On l'ajoute au joueur
				P.getItems().put(PersoObj.getGuid(), PersoObj);
				
				//On envoie les packets
				SocketManager.GAME_SEND_OAKO_PACKET(P,PersoObj);
				str = "O+"+TrunkObj.getGuid()+"|"+TrunkObj.getQuantity()+"|"+TrunkObj.getTemplate().getID()+"|"+TrunkObj.parseStatsString();
				
			}
		}
		else
		{
			//S'il ne reste rien dans le coffre
			if(newQua <= 0)
			{
				//On retire l'item du coffre
				_items.remove(TrunkObj.getGuid());
				World.removeItem(TrunkObj.getGuid());
				//On Modifie la quantité de l'item du sac du joueur
				PersoObj.setQuantity(PersoObj.getQuantity() + TrunkObj.getQuantity());
				
				//On envoie les packets
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(P, PersoObj);
				str = "O-"+guid;
				
			}
			else//S'il reste des objets dans le coffre
			{
				//On retire X objet du coffre
				TrunkObj.setQuantity(newQua);
				//On ajoute X objets au joueurs
				PersoObj.setQuantity(PersoObj.getQuantity() + qua);
				
				//On envoie les packets
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(P,PersoObj);
				str = "O+"+TrunkObj.getGuid()+"|"+TrunkObj.getQuantity()+"|"+TrunkObj.getTemplate().getID()+"|"+TrunkObj.parseStatsString();
			}
		}
				
		SocketManager.GAME_SEND_EsK_PACKET(P, str);
		SocketManager.GAME_SEND_Ow_PACKET(P);
	}
	
	private Objet getSimilarDindeItem(Objet obj)
	{
		for(Objet value : _items.values())
		{
			if(value.getTemplate().getType() == 85)
				continue;
			if(value.getTemplate().getID() == obj.getTemplate().getID() && value.getStats().isSameStats(obj.getStats()))
				return value;
		}
		return null;
	}
	
	public String parseDindeObjetsToDB()
	{
		StringBuilder str = new StringBuilder();
		for(Entry<Integer,Objet> entry : _items.entrySet())
		{
			Objet obj = entry.getValue();
			str.append(obj.getGuid()).append("|");
		}
		return str.toString();
	}
	
	public String parseToDindePacket()
	{
		StringBuilder packet = new StringBuilder();
		for(Objet obj : _items.values())
			packet.append("O").append(obj.parseItem()).append(";");
		return packet.toString();
	}
}
