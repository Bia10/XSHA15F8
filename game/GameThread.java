package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;


import objects.*;
import objects.Carte.*;
import objects.Fight.Fighter;
import objects.Guild.GuildMember;
import objects.HDV.HdvEntry;
import objects.Metier.StatsMetier;
import objects.Carte;
import objects.Dragodinde;
import objects.Objet;
import objects.Personnage;
import objects.Prisma;
import objects.NPC_tmpl.*;
import objects.Objet.ObjTemplate;
import objects.Personnage.Group;
import objects.Sort.SortStats;
import common.*;


public class GameThread implements Runnable
{
	private BufferedReader _in;
	private Thread _t;
	private PrintWriter _out;
	private Socket _s;
	private Compte _compte;
	private Personnage _perso;
	private Map<Integer,GameAction> _actions = new TreeMap<Integer,GameAction>();
	private long _timeLastTradeMsg = 0, _timeLastRecrutmentMsg = 0, _timeLastsave = 0, _timeLastAlignMsg = 0;
	private long _timeLastDebug = 0;
	private long _timeLastIncarnamMsg = 0;
	private Commands command;
	private String ip;

    public String getHostAdress() {
        return ip;
    }
	
	
	public static class GameAction
	{
		public int _id;
		public int _actionID;
		public String _packet;
		public String _args;
		public GameAction(int aId, int aActionId,String aPacket)
		{
			_id = aId;
			_actionID = aActionId;
			_packet = aPacket;
		}
	}
	
	public GameThread(String ip, Socket sock)
	{
		try
		{
			this.ip = ip;
			_s = sock;
			_in = new BufferedReader(new InputStreamReader(_s.getInputStream()));
			_out = new PrintWriter(_s.getOutputStream());
			_t = new Thread(this);
			_t.setDaemon(true);
			_t.start();
		}
		catch(IOException e)
		{
			try {
				GameServer.addToLog(e.getMessage());
				if(!_s.isClosed())_s.close();
			} catch (IOException e1) {e1.printStackTrace();}
		}
	}
	
	public void run()
	{
		try
    	{
			StringBuilder sPacket = new StringBuilder();
			char charCur[] = new char[1];
			SocketManager.GAME_SEND_HELLOGAME_PACKET(_out);
	    	while(_in.read(charCur, 0, 1)!=-1 && Ancestra.isRunning)
	    	{
	    		if (charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r')
		    	{
	    			sPacket.append(charCur[0]);
		    	}else if(sPacket.length() > 0)
		    	{
		    		String packet = CryptManager.toUnicode(sPacket.toString());
		    		GameServer.addToSockLog("Game: Recv << "+packet);
		    		parsePacket(packet);
		    		//Ancestra.printDebug(packet);
		    		packet = null;
		    		sPacket = new StringBuilder();
		    	}
	    	}
    	}catch(IOException e)
    	{
    		try
    		{
    			GameServer.addToLog(e.getMessage());
	    		_in.close();
	    		_out.close();
	    		if(_compte != null)
	    		{
	    			_compte.setCurPerso(null);
	    			_compte.setGameThread(null);
	    			_compte.setRealmThread(null);
	    		}
	    		if(!_s.isClosed())_s.close();
	    	}catch(IOException e1){e1.printStackTrace();};
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    		GameServer.addToLog(e.getMessage());
    	}
    	finally
    	{
    		kick();
    	}
	}

	private void parsePacket(String packet)
	{
		if(_perso != null) {
			_perso.refreshLastPacketTime();
		}
		
		/*if(packet.length()>3 && packet.substring(0,4).equalsIgnoreCase("ping"))
		{
			SocketManager.GAME_SEND_PONG(_out);
			return;
		}
		if(packet.length()>4 && packet.substring(0,5).equalsIgnoreCase("qping"))
		{
			SocketManager.GAME_SEND_QPONG(_out);
			return;
		}*/
		
		switch(packet.charAt(0))
		{
			case 'p': // 'p'
				if(packet.equals("ping"))
				{
					SocketManager.GAME_SEND_PONG(_out);
				}
				break;				
			case 'q': // 'q'
				if(!packet.equals("qping"))
				{
					return;
				}
				if(_perso == null)
				{
					return;
				}
				if(_perso.get_fight() == null)
				{
					return;
				}
				SocketManager.GAME_SEND_QPONG(_out);
				break;
			case 'A':
				parseAccountPacket(packet);
			break;
			case 'B':
				parseBasicsPacket(packet);
			break;
			case 'C': // conquista
				analizar_Conquista(packet);
				break;
			case 'c':
				parseChanelPacket(packet);
			break;
			case 'D':
				parseDialogPacket(packet);
			break;
			case 'E':
				parseExchangePacket(packet);
			break;
			case 'e':
				parse_environementPacket(packet);
			break;
			case 'F':
				parse_friendPacket(packet);
			break;
			case 'f':
				parseFightPacket(packet);
			break;
			case 'G':
				parseGamePacket(packet);
			break;
			case 'g':
				parseGuildPacket(packet);
			break;
			case 'h':
				parseHousePacket(packet);
			break;
			case 'i':
				parse_enemyPacket(packet);
			break;
			case 'K':
				parseHouseKodePacket(packet);
			break;
			case 'O':
				parseObjectPacket(packet);
			break;
			case 'P':
				parseGroupPacket(packet);
			break;
			case 'R':
				parseMountPacket(packet);
			break;
			case 'S':
				parseSpellPacket(packet);
			break;
			case 'W':
				parseWaypointPacket(packet);
			break;
		}
			
	}
	private void analizar_Conquista(String packet) {
		switch (packet.charAt(1)) {

		case 'b':// balance de mundo y area
			SocketManager.ENVIAR_Cb_BALANCE_CONQUISTA(_perso, World.getBalanceMundo(_perso.get_align()) + ";" + World.getBalanceArea(_perso.get_curCarte().getSubArea().get_area(), _perso.get_align()));
			break;

		case 'B':// bonus de alineacion
			double porc = World.getBalanceMundo(_perso.get_align());
			double porcN = Math.rint((_perso.getGrade() / 50.6) + 23.3);
			SocketManager.ENVIAR_CB_BONUS_CONQUISTA(_perso, porc + "," + porc
					+ "," + porc + ";" + porcN + "," + porcN + "," + porcN
					+ ";" + porc + "," + porc + "," + porc);
			break;

		case 'W':// info de mapa sobre los mapas
			conquista_Geoposicion(packet);
			break;

		case 'I':// Modificacion de precio de venta
			conquista_Defensa(packet);
			break;

		case 'F':// Cerrar ventana de compra
			conquista_Unirse_Defensa_Prisma(packet);
			break;

		}
	}

	private void conquista_Geoposicion(String packet) {
		switch (packet.charAt(2)) {
			case 'J':// info de Prismes defensa
				SocketManager.ENVIAR_CW_INFO_MUNDO_CONQUISTA(_perso, World.prismasGeoposicion(_perso.get_align()));
				break;
			case 'V':
				SocketManager.ENVIAR_CIV_CERRAR_INFO_CONQUISTA(_perso);
				break;
		}
	}

	private void conquista_Defensa(String packet) {
		switch (packet.charAt(2)) {
		case 'J':// info de prismas defensa
			String str = _perso.analizarPrismas();
			Prisma prisma = World.getPrisma(_perso.get_curCarte().getSubArea()
					.getPrismaID());
			if (prisma != null) {
				objects.Prisma.analizarAtaque(_perso);
				objects.Prisma.analizarDefensa(_perso);
			}
			SocketManager.ENVIAR_CIJ_INFO_UNIRSE_PRISMA(_perso, str);
			break;
		case 'V':
			SocketManager.ENVIAR_CIV_CERRAR_INFO_CONQUISTA(_perso);
			break;
		}
	}

	private void conquista_Unirse_Defensa_Prisma(String packet) {
		switch (packet.charAt(2)) {
		case 'J':// info de prismas defensa
			int prismaID = _perso.get_curCarte().getSubArea().getPrismaID();
			objects.Prisma prisma = World.getPrisma(prismaID);
			if (prisma == null)
				return;
			int peleaID = -1;
			try {
				peleaID = prisma.getPeleaID();
			} catch (Exception e) {
			}
			short mapaID = -1;
			try {
				mapaID = prisma.getMapa();
			} catch (Exception e) {
			}
			int celdaID = -1;
			try {
				celdaID = prisma.getCelda();
			} catch (Exception e) {
			}
			if (prismaID == -1 || peleaID == -1 || mapaID == -1
					|| celdaID == -1)
				return;
			if (prisma.getAlineacion() != _perso.get_align())
				return;
			if (_perso.get_fight() != null)
				return;
			if (_perso.get_curCarte().get_id() != mapaID) {
				_perso.set_curCarte(_perso.get_curCarte());
				_perso.set_curCell(_perso.get_curCell());
				try {
					Thread.sleep(200);
					_perso.teleport(mapaID, celdaID);
					Thread.sleep(400);
				} catch (Exception e) {
				}
			}
			World.getCarte(mapaID).getFight(peleaID)
					.unirsePeleaPrisma(_perso, _perso.get_GUID(), prismaID);
			for (Personnage z : World.getOnlinePersos()) {
				if (z == null)
					continue;
				if (z.get_align() != _perso.get_align())
					continue;
				Prisma.analizarDefensa(z);
			}
			break;
		}
	}
	
	private void parseHousePacket(String packet)
	{
		switch(packet.charAt(1))
		{
		case 'B'://Acheter la maison
			packet = packet.substring(2);
			House.HouseAchat(_perso);
		break;
		case 'G'://Maison de guilde
			packet = packet.substring(2);
			if(packet.isEmpty()) packet = null;
			House.parseHG(_perso, packet);
		break;
		case 'Q'://Quitter/Expulser de la maison
			packet = packet.substring(2);
			House.Leave(_perso, packet);
		break;
		case 'S'://Modification du prix de vente
			packet = packet.substring(2);
			House.SellPrice(_perso, packet);
		break;
		case 'V'://Fermer fenetre d'achat
			House.closeBuy(_perso);
		break;
		}
	}
	
	private void parseHouseKodePacket(String packet)
	{
		switch(packet.charAt(1))
		{
		case 'V'://Fermer fenetre du code
			House.closeCode(_perso);
		break;
		case 'K'://Envoi du code
			House_code(packet);
		break;
		}
	}
	
	private void House_code(String packet)
	{
		switch(packet.charAt(2))
		{
		case '0'://Envoi du code || Boost
			packet = packet.substring(4);
			if(_perso.get_savestat() >0)
			{
				try{
					int code = 0;
					code = Integer.parseInt(packet);
					if(code<0)return;
					if(_perso.get_capital() < code)code=_perso.get_capital();
			        _perso.boostStatFixedCount(_perso.get_savestat(), code);
				}catch(Exception e){
				}
				finally{
					 _perso.set_savestat(0);
					 SocketManager.GAME_SEND_KODE(_perso, "V");
				}
			}
			else if(_perso.getInTrunk() != null) Trunk.OpenTrunk(_perso, packet, false);
			else House.OpenHouse(_perso, packet, false);
		break;
		case '1'://Changement du code
			packet = packet.substring(4);
			if(_perso.getInTrunk() != null)
				Trunk.LockTrunk(_perso, packet);
			else
			    House.LockHouse(_perso, packet);
		break;
		}
	}
	
	private void parse_enemyPacket(String packet)
	{
		switch(packet.charAt(1))
		{
		case 'A'://Ajouter
			Enemy_add(packet);
		break;
		case 'D'://Delete
			Enemy_delete(packet);
		break;
		case 'L'://Liste
			SocketManager.GAME_SEND_ENEMY_LIST(_perso);
		break;
		}
	}
	
	private void Enemy_add(String packet)
	{
		if(_perso == null)return;
		int guid = -1;
		switch(packet.charAt(2))
		{
			case '%'://Nom de perso
				packet = packet.substring(3);
				Personnage P = World.getPersoByName(packet);
				if(P == null)
				{
					SocketManager.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = P.getAccID();
				
			break;
			case '*'://Pseudo
				packet = packet.substring(3);
				Compte C = World.getCompteByPseudo(packet);
				if(C==null)
				{
					SocketManager.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = C.get_GUID();
			break;
			default:
				packet = packet.substring(2);
				Personnage Pr = World.getPersoByName(packet);
				if(Pr == null?true:!Pr.isOnline())
				{
					SocketManager.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = Pr.get_compte().get_GUID();
			break;
		}
		if(guid == -1)
		{
			SocketManager.GAME_SEND_FD_PACKET(_perso, "Ef");
			return;
		}
		_compte.addEnemy(packet, guid);
	}

	private void Enemy_delete(String packet)
	{
		if(_perso == null)return;
		int guid = -1;
		switch(packet.charAt(2))
		{
			case '%'://Nom de perso
				packet = packet.substring(3);
				Personnage P = World.getPersoByName(packet);
				if(P == null)
				{
					SocketManager.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = P.getAccID();
				
			break;
			case '*'://Pseudo
				packet = packet.substring(3);
				Compte C = World.getCompteByPseudo(packet);
				if(C==null)
				{
					SocketManager.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = C.get_GUID();
			break;
			default:
				packet = packet.substring(2);
				Personnage Pr = World.getPersoByName(packet);
				if(Pr == null?true:!Pr.isOnline())
				{
					SocketManager.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = Pr.get_compte().get_GUID();
			break;
		}
		if(guid == -1 || !_compte.isEnemyWith(guid))
		{
			SocketManager.GAME_SEND_FD_PACKET(_perso, "Ef");
			return;
		}
		_compte.removeEnemy(guid);
	}
	
	private void parseWaypointPacket(String packet)
	{
		switch(packet.charAt(1))
		{
			case 'U'://Use
				Waypoint_use(packet);
			break;
			case 'u'://use zaapi
				Zaapi_use(packet);
			break;
			case 'v'://quitter zaapi
				Zaapi_close();
			break;
			case 'V'://Quitter
				Waypoint_quit();
			break;
			case 'w':
				prisma_Cerrar();
				break;

			case 'p':
				prisma_Usar(packet);
				break;
		}
	}
	private void prisma_Cerrar() {
		_perso.cerrarPrisma();
	}
	private void Zaapi_close()
	{
		_perso.Zaapi_close();
	}
	private void prisma_Usar(String packet) {
		if (_perso.getDeshonor() >= 2) {
			SocketManager.GAME_SEND_Im_PACKET(_perso, "183");
			return;
		}
		_perso.usarPrisma(packet);
	}
	private void Zaapi_use(String packet)
	{
		if(_perso.getDeshonor() >= 2) 
		{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "183");
			return;
		}
		_perso.Zaapi_use(packet);
	}
	
	private void Waypoint_quit()
	{
		_perso.stopZaaping();
	}

	private void Waypoint_use(String packet)
	{
		short id = -1;
		try
		{
			id = Short.parseShort(packet.substring(2));
		}catch(Exception e){};
		if( id == -1)return;
		_perso.useZaap(id);
	}
	private void parseGuildPacket(String packet)
	{
		switch(packet.charAt(1))
		{
			case 'B'://Stats
				if(_perso.get_guild() == null)return;
				Guild G = _perso.get_guild();
				if(!_perso.getGuildMember().canDo(Constants.G_BOOST))return;
				switch(packet.charAt(2))
				{
					case 'p'://Prospec
						if(G.get_Capital() < 1)return;
						if(G.get_Stats(176) >= 500)return;
						G.set_Capital(G.get_Capital()-1);
						G.upgrade_Stats(176, 1);
					break;
					case 'x'://Sagesse
						if(G.get_Capital() < 1)return;
						if(G.get_Stats(124) >= 400)return;
						G.set_Capital(G.get_Capital()-1);
						G.upgrade_Stats(124, 1);
					break;
					case 'o'://Pod
						if(G.get_Capital() < 1)return;
						if(G.get_Stats(158) >= 5000)return;
						G.set_Capital(G.get_Capital()-1);
						G.upgrade_Stats(158, 20);
					break;
					case 'k'://Nb Perco
						if(G.get_Capital() < 10)return;
						if(G.get_nbrPerco() >= 50)return;
						G.set_Capital(G.get_Capital()-10);
						G.set_nbrPerco(G.get_nbrPerco()+1);
					break;
				}
				SQLManager.UPDATE_GUILD(G);
				SocketManager.GAME_SEND_gIB_PACKET(_perso, _perso.get_guild().parsePercotoGuild());
			break;
			case 'b'://Sorts
				if(_perso.get_guild() == null)return;
				Guild G2 = _perso.get_guild();
				if(!_perso.getGuildMember().canDo(Constants.G_BOOST))return;
				int spellID = Integer.parseInt(packet.substring(2));
				if(G2.getSpells().containsKey(spellID))
				{
					if(G2.get_Capital() < 5)return;
					G2.set_Capital(G2.get_Capital()-5);
					G2.boostSpell(spellID);
					SQLManager.UPDATE_GUILD(G2);
					SocketManager.GAME_SEND_gIB_PACKET(_perso, _perso.get_guild().parsePercotoGuild());
				}else
				{
					GameServer.addToLog("[ERROR]Sort "+spellID+" non trouve.");
				}
			break;
			case 'C'://Creation
				guild_create(packet);
			break;
			case 'f'://Téléportation enclo de guilde
				guild_enclo(packet.substring(2));
			break;
			case 'F'://Retirer percepteur
				guild_remove_perco(packet.substring(2));
			break;
			case 'h'://Téléportation maison de guilde
				guild_house(packet.substring(2));
			break;
			case 'H'://Poser un percepteur
				guild_add_perco();
			break;
			case 'I'://Infos
				guild_infos(packet.charAt(2));
			break;
			case 'J'://Join
				guild_join(packet.substring(2));
			break;
			case 'K'://Kick
				guild_kick(packet.substring(2));
			break;
			case 'P'://Promote
				guild_promote(packet.substring(2));
			break;
			case 'T'://attaque sur percepteur
				guild_perco_join_fight(packet.substring(2));
			break;
			case 'V'://Ferme le panneau de création de guilde
				guild_CancelCreate();
			break;
		}
	}
	
	private void guild_perco_join_fight(String packet) 
	{
		switch(packet.charAt(0))
		{
			case 'J'://Rejoindre
				String PercoID = Integer.toString(Integer.parseInt(packet.substring(1)), 36);
				
				int TiD = -1;
				try
				{
					TiD = Integer.parseInt(PercoID);
				}catch(Exception e){};
				
				Percepteur perco = World.getPerco(TiD);
				if(perco == null) return;
				
				int FightID = -1;
				try
				{
					FightID = perco.get_inFightID();
				}catch(Exception e){};
				
				short MapID = -1;
				try
				{
					MapID = World.getCarte((short)perco.get_mapID()).getFight(FightID).get_map().get_id();
				}catch(Exception e){};
				
				int CellID = -1;
				try
				{
					CellID = perco.get_cellID();
				}catch(Exception e){};
				
				if(Ancestra.CONFIG_DEBUG) GameServer.addToLog("[DEBUG] Percepteur INFORMATIONS : TiD:"+TiD+", FightID:"+FightID+", MapID:"+MapID+", CellID"+CellID);
				if(TiD == -1 || FightID == -1 || MapID == -1 || CellID == -1) return;
				if(_perso.get_fight() == null && !_perso.is_away())
				{
					if(_perso.get_curCarte().get_id() != MapID)
					{
						_perso.teleport(MapID, CellID);
					}
					World.getCarte(MapID).getFight(FightID).joinPercepteurFight(_perso,_perso.get_GUID(), TiD);
				}
			break;
		}
	}

	private void guild_remove_perco(String packet) 
	{
		if(_perso.get_guild() == null || _perso.get_fight() != null || _perso.is_away())return;
		if(!_perso.getGuildMember().canDo(Constants.G_POSPERCO))return;//On peut le retirer si on a le droit de le poser
		byte IDPerco = Byte.parseByte(packet);
		Percepteur perco = World.getPerco(IDPerco);
		if(perco == null || perco.get_inFight() > 0) return;
		SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(_perso.get_curCarte(), IDPerco);
		SQLManager.DELETE_PERCO(perco.getGuid());
		perco.DelPerco(perco.getGuid());
		for(Personnage z : _perso.get_guild().getMembers())
		{
			if(z.isOnline())
			{
				SocketManager.GAME_SEND_gITM_PACKET(z, Percepteur.parsetoGuild(z.get_guild().get_id()));
				String str = "";
				str += "R"+perco.get_N1()+","+perco.get_N2()+"|";
				str += perco.get_mapID()+"|";
				str += World.getCarte((short)perco.get_mapID()).getX()+"|"+World.getCarte((short)perco.get_mapID()).getY()+"|"+_perso.get_name();
				SocketManager.GAME_SEND_gT_PACKET(z, str);
			}
		}
	
	
	}
	private void guild_add_perco() 
	{
		if(_perso.get_guild() == null || _perso.get_fight() != null || _perso.is_away())return;
		if(!_perso.getGuildMember().canDo(Constants.G_POSPERCO))return;//Pas le droit de le poser
		if(_perso.get_guild().getMembers().size() < 1)return;//Guilde invalide
		short price = (short)(1000+10*_perso.get_guild().get_lvl());//Calcul du prix du percepteur
		if(_perso.get_kamas() < price)//Kamas insuffisants
		{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "182");
			return;
		}
		if(Percepteur.GetPercoGuildID(_perso.get_curCarte().get_id()) > 0)//La carte possède un perco
		{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "1168;1");
			return;
		}
		if(_perso.get_curCarte().get_placesStr().length() < 5)//La map ne possède pas de "places"
		{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "113");
			return;
		}
		if(Percepteur.CountPercoGuild(_perso.get_guild().get_id()) >= _perso.get_guild().get_nbrPerco()) return;//Limite de percepteur
		short random1 = (short) (Formulas.getRandomValue(1, 39));
		short random2 = (short) (Formulas.getRandomValue(1, 71));
		//Ajout du Perco.
		int id = SQLManager.GetNewIDPercepteur();
		Percepteur perco = new Percepteur(id, _perso.get_curCarte().get_id(), _perso.get_curCell().getID(), (byte)3, _perso.get_guild().get_id(), random1, random2, "", 0, 0);
		World.addPerco(perco);
		SocketManager.GAME_SEND_ADD_PERCO_TO_MAP(_perso.get_curCarte());
		SQLManager.ADD_PERCO_ON_MAP(id, _perso.get_curCarte().get_id(), _perso.get_guild().get_id(), _perso.get_curCell().getID(), 3, random1, random2);
		for(Personnage z : _perso.get_guild().getMembers())
		{
			if(z != null && z.isOnline())
			{
				SocketManager.GAME_SEND_gITM_PACKET(z, Percepteur.parsetoGuild(z.get_guild().get_id()));
				String str = "";
				str += "S"+perco.get_N1()+","+perco.get_N2()+"|";
				str += perco.get_mapID()+"|";
				str += World.getCarte((short)perco.get_mapID()).getX()+"|"+World.getCarte((short)perco.get_mapID()).getY()+"|"+_perso.get_name();
				SocketManager.GAME_SEND_gT_PACKET(z, str);
			}
		}
	}

	private void guild_enclo(String packet)
	{
		if(_perso.get_guild() == null)
		{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "1135");
			return;
		}
		
		if(_perso.get_fight() != null || _perso.is_away())return;
		short MapID = Short.parseShort(packet);
		MountPark MP = World.getCarte(MapID).getMountPark();
		if(MP.get_guild().get_id() != _perso.get_guild().get_id())
		{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "1135");
			return;
		}
		int CellID = World.getEncloCellIdByMapId(MapID);
		if (_perso.hasItemTemplate(9035, 1))
		{
			_perso.removeByTemplateID(9035,1);
			_perso.teleport(MapID, CellID);
		}else
		{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "1159");
			return;
		}
	}
	
	private void guild_house(String packet)
	{
		if(_perso.get_guild() == null)
		{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "1135");
			return;
		}
		
		if(_perso.get_fight() != null || _perso.is_away())return;
		int HouseID = Integer.parseInt(packet);
		House h = World.getHouses().get(HouseID);
		if(h == null) return;
		if(_perso.get_guild().get_id() != h.get_guild_id()) 
		{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "1135");
			return;
		}
		if(!h.canDo(Constants.H_GTELE))
		{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "1136");
			return;
		}
		if (_perso.hasItemTemplate(8883, 1))
		{
			_perso.removeByTemplateID(8883,1);
			_perso.teleport((short)h.get_mapid(), h.get_caseid());
		}else
		{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "1137");
			return;
		}
	}

	private void guild_promote(String packet)
	{
		if(_perso.get_guild() == null)return;	//Si le personnage envoyeur n'a même pas de guilde
		
		String[] infos = packet.split("\\|");
		
		int guid = Integer.parseInt(infos[0]);
		int rank = Integer.parseInt(infos[1]);
		byte xpGive = Byte.parseByte(infos[2]);
		int right = Integer.parseInt(infos[3]);
		
		Personnage p = World.getPersonnage(guid);	//Cherche le personnage a qui l'on change les droits dans la mémoire
		GuildMember toChange;
		GuildMember changer = _perso.getGuildMember();
		
		//Récupération du personnage à changer, et verification de quelques conditions de base
		if(p == null)	//Arrive lorsque le personnage n'est pas chargé dans la mémoire
		{
			int guildId = SQLManager.isPersoInGuild(guid);	//Récupère l'id de la guilde du personnage qui n'est pas dans la mémoire
			
			if(guildId < 0)return;	//Si le personnage à qui les droits doivent être modifié n'existe pas ou n'a pas de guilde
			
			
			if(guildId != _perso.get_guild().get_id())					//Si ils ne sont pas dans la même guilde
			{
				SocketManager.GAME_SEND_gK_PACKET(_perso, "Ed");
				return;
			}
			toChange = World.getGuild(guildId).getMember(guid);
		}
		else
		{
			if(p.get_guild() == null)return;	//Si la personne à qui changer les droits n'a pas de guilde
			if(_perso.get_guild().get_id() != p.get_guild().get_id())	//Si ils ne sont pas de la meme guilde
			{
				SocketManager.GAME_SEND_gK_PACKET(_perso, "Ea");
				return;
			}
			
			toChange = p.getGuildMember();
		}
		
		//Vérifie ce que le personnage changeur à le droit de faire
		
		if(changer.getRank() == 1)	//Si c'est le meneur
		{
			if(changer.getGuid() == toChange.getGuid())	//Si il se modifie lui même, reset tout sauf l'XP
			{
				rank = -1;
				right = -1;
			}
			else //Si il modifie un autre membre
			{
				if(rank == 1) //Si il met un autre membre "Meneur"
				{
					changer.setAllRights(2, (byte) -1, 29694);	//Met le meneur "Bras droit" avec tout les droits
					
					//Défini les droits à mettre au nouveau meneur
					rank = 1;
					xpGive = -1;
					right = 1;
				}
			}
		}
		else	//Sinon, c'est un membre normal
		{
			if(toChange.getRank() == 1)	//S'il veut changer le meneur, reset tout sauf l'XP
			{
				rank = -1;
				right = -1;
			}
			else	//Sinon il veut changer un membre normal
			{
				if(!changer.canDo(Constants.G_RANK) || rank == 1)	//S'il ne peut changer les rang ou qu'il veut mettre meneur
					rank = -1; 	//"Reset" le rang
				
				if(!changer.canDo(Constants.G_RIGHT) || right == 1)	//S'il ne peut changer les droits ou qu'il veut mettre les droits de meneur
					right = -1;	//"Reset" les droits
				
				if(!changer.canDo(Constants.G_HISXP) && !changer.canDo(Constants.G_ALLXP) && changer.getGuid() == toChange.getGuid())	//S'il ne peut changer l'XP de personne et qu'il est la cible
					xpGive = -1; //"Reset" l'XP
			}
			
			if(!changer.canDo(Constants.G_ALLXP) && !changer.equals(toChange))	//S'il n'a pas le droit de changer l'XP des autres et qu'il n'est pas la cible
				xpGive = -1; //"Reset" L'XP
		}

		toChange.setAllRights(rank,xpGive,right);
		
		SocketManager.GAME_SEND_gS_PACKET(_perso,_perso.getGuildMember());
		
		if(p != null && p.get_GUID() != _perso.get_GUID())
			SocketManager.GAME_SEND_gS_PACKET(p,p.getGuildMember());
	}
	
	private void guild_CancelCreate()
	{
		SocketManager.GAME_SEND_gV_PACKET(_perso);
	}

	private void guild_kick(String name)
	{
		if(_perso.get_guild() == null)return;
		Personnage P = World.getPersoByName(name);
		int guid = -1,guildId = -1;
		Guild toRemGuild;
		GuildMember toRemMember;
		if(P == null)
		{
			int infos[] = SQLManager.isPersoInGuild(name);
			guid = infos[0];
			guildId = infos[1];
			if(guildId < 0 || guid < 0)return;
			toRemGuild = World.getGuild(guildId);
			toRemMember = toRemGuild.getMember(guid);
		}
		else
		{
			toRemGuild = P.get_guild();
			if(toRemGuild == null)//La guilde du personnage n'est pas charger ?
			{
					toRemGuild = World.getGuild(_perso.get_guild().get_id());//On prend la guilde du perso qui l'éjecte
			}
			toRemMember = toRemGuild.getMember(P.get_GUID());
			if(toRemMember == null) return;//Si le membre n'est pas dans la guilde.
			if(toRemMember.getGuild().get_id() != _perso.get_guild().get_id()) return;//Si guilde différente
		}
		//si pas la meme guilde
		if(toRemGuild.get_id() != _perso.get_guild().get_id())
		{
			SocketManager.GAME_SEND_gK_PACKET(_perso, "Ea");
			return;
		}
		//S'il n'a pas le droit de kick, et que ce n'est pas lui même la cible
		if(!_perso.getGuildMember().canDo(Constants.G_BAN) && _perso.getGuildMember().getGuid() != toRemMember.getGuid())
		{
			SocketManager.GAME_SEND_gK_PACKET(_perso, "Ed");
			return;
		}
		//Si différent : Kick
		if(_perso.getGuildMember().getGuid() != toRemMember.getGuid())
		{
			if(toRemMember.getRank() == 1) //S'il veut kicker le meneur
				return;
			
			toRemGuild.removeMember(toRemMember.getPerso());
			if(P != null)
				P.setGuildMember(null);
			
			SocketManager.GAME_SEND_gK_PACKET(_perso, "K"+_perso.get_name()+"|"+name);
			if(P != null)
				SocketManager.GAME_SEND_gK_PACKET(P, "K"+_perso.get_name());
		}else//si quitter
		{
			Guild G = _perso.get_guild();
			if(_perso.getGuildMember().getRank() == 1 && G.getMembers().size() > 1)	//Si le meneur veut quitter la guilde mais qu'il reste d'autre joueurs
			{
				//TODO : Envoyer le message qu'il doit mettre un autre membre meneur (Pas vraiment....)
				return;
			}
			G.removeMember(_perso);
			_perso.setGuildMember(null);
			//S'il n'y a plus personne
			if(G.getMembers().isEmpty())World.removeGuild(G.get_id());
			SocketManager.GAME_SEND_gK_PACKET(_perso, "K"+name+"|"+name);
		}
	}
	
	private void guild_join(String packet)
	{
		switch(packet.charAt(0))
		{
		case 'R'://Nom perso
			Personnage P = World.getPersoByName(packet.substring(1));
			if(P == null || _perso.get_guild() == null)
			{
				SocketManager.GAME_SEND_gJ_PACKET(_perso, "Eu");
				return;
			}
			if(!P.isOnline())
			{
				SocketManager.GAME_SEND_gJ_PACKET(_perso, "Eu");
				return;
			}
			if(P.is_away())
			{
				SocketManager.GAME_SEND_gJ_PACKET(_perso, "Eo");
				return;
			}
			if(P.get_guild() != null)
			{
				SocketManager.GAME_SEND_gJ_PACKET(_perso, "Ea");
				return;
			}
			if(!_perso.getGuildMember().canDo(Constants.G_INVITE))
			{
				SocketManager.GAME_SEND_gJ_PACKET(_perso, "Ed");
				return;
			}
			if(_perso.get_guild().getMembers().size() >= (40+_perso.get_guild().get_lvl()))//Limite membres max
			{
				SocketManager.GAME_SEND_Im_PACKET(_perso, "155;"+(40+_perso.get_guild().get_lvl()));
				return;
			}
			
			_perso.setInvitation(P.get_GUID());
			P.setInvitation(_perso.get_GUID());

			SocketManager.GAME_SEND_gJ_PACKET(_perso,"R"+packet.substring(1));
			SocketManager.GAME_SEND_gJ_PACKET(P,"r"+_perso.get_GUID()+"|"+_perso.get_name()+"|"+_perso.get_guild().get_name());
		break;
		case 'E'://ou Refus
			if(packet.substring(1).equalsIgnoreCase(_perso.getInvitation()+""))
			{
				Personnage p = World.getPersonnage(_perso.getInvitation());
				if(p == null)return;//Pas censé arriver
				SocketManager.GAME_SEND_gJ_PACKET(p,"Ec");
			}
		break;
		case 'K'://Accepte
			if(packet.substring(1).equalsIgnoreCase(_perso.getInvitation()+""))
			{
				Personnage p = World.getPersonnage(_perso.getInvitation());
				if(p == null)return;//Pas censé arriver
				Guild G = p.get_guild();
				GuildMember GM = G.addNewMember(_perso);
				SQLManager.UPDATE_GUILDMEMBER(GM);
				_perso.setGuildMember(GM);
				_perso.setInvitation(-1);
				p.setInvitation(-1);
				//Packet
				SocketManager.GAME_SEND_gJ_PACKET(p,"Ka"+_perso.get_name());
				SocketManager.GAME_SEND_gS_PACKET(_perso, GM);
				SocketManager.GAME_SEND_gJ_PACKET(_perso,"Kj");
			}
		break;
		}
	}

	private void guild_infos(char c)
	{
		switch(c)
		{
		case 'B'://Perco
			SocketManager.GAME_SEND_gIB_PACKET(_perso, _perso.get_guild().parsePercotoGuild());
		break;
		case 'F'://Enclos
			SocketManager.GAME_SEND_gIF_PACKET(_perso, World.parseMPtoGuild(_perso.get_guild().get_id()));
		break;
		case 'G'://General
			SocketManager.GAME_SEND_gIG_PACKET(_perso, _perso.get_guild());
		break;
		case 'H'://House
			SocketManager.GAME_SEND_gIH_PACKET(_perso, House.parseHouseToGuild(_perso));
		break;
		case 'M'://Members
			SocketManager.GAME_SEND_gIM_PACKET(_perso, _perso.get_guild(),'+');
		break;
		case 'T'://Perco
			SocketManager.GAME_SEND_gITM_PACKET(_perso, Percepteur.parsetoGuild(_perso.get_guild().get_id()));
			Percepteur.parseAttaque(_perso, _perso.get_guild().get_id());
			Percepteur.parseDefense(_perso, _perso.get_guild().get_id());
		break;
		}
	}

	private void guild_create(String packet)
	{
		if(_perso == null)return;
		if(_perso.get_guild() != null || _perso.getGuildMember() != null)
		{
			SocketManager.GAME_SEND_gC_PACKET(_perso, "Ea");
			return;
		}
		if(_perso.get_fight() != null || _perso.is_away())return;
		try
		{
			String[] infos = packet.substring(2).split("\\|");
			//base 10 => 36
			String bgID = Integer.toString(Integer.parseInt(infos[0]),36);
			String bgCol = Integer.toString(Integer.parseInt(infos[1]),36);
			String embID =  Integer.toString(Integer.parseInt(infos[2]),36);
			String embCol =  Integer.toString(Integer.parseInt(infos[3]),36);
			String name = infos[4];
			if(World.guildNameIsUsed(name))
			{
				SocketManager.GAME_SEND_gC_PACKET(_perso, "Ean");
				return;
			}
			
			//Validation du nom de la guilde
			String tempName = name.toLowerCase();
			boolean isValid = true;
			//Vérifie d'abord si il contient des termes définit
			if(tempName.length() > 20
					|| tempName.contains("mj")
					|| tempName.contains("modo")
					|| tempName.contains("admin"))
			{
				isValid = false;
			}
			//Si le nom passe le test, on vérifie que les caractère entré sont correct.
			if(isValid)
			{
				int tiretCount = 0;
				for(char curLetter : tempName.toCharArray())
				{
					if(!(	(curLetter >= 'a' && curLetter <= 'z')
							|| curLetter == '-'))
					{
						isValid = false;
						break;
					}
					if(curLetter == '-')
					{
						if(tiretCount >= 2)
						{
							isValid = false;
							break;
						}
						else
						{
							tiretCount++;
						}
					}
				}
			}
			//Si le nom est invalide
			if(!isValid)
			{
				SocketManager.GAME_SEND_gC_PACKET(_perso, "Ean");
				return;
			}
			//FIN de la validation
			String emblem = bgID+","+bgCol+","+embID+","+embCol;//9,6o5nc,2c,0;
			if(World.guildEmblemIsUsed(emblem))
			{
				SocketManager.GAME_SEND_gC_PACKET(_perso, "Eae");
				return;
			}
			if(_perso.get_curCarte().get_id() == 2196)//Temple de création de guilde
			{
				if(!_perso.hasItemTemplate(1575,1))//Guildalogemme
				{
					SocketManager.GAME_SEND_Im_PACKET(_perso, "14");
					return;
				}
				_perso.removeByTemplateID(1575, 1);
			}
			Guild G = new Guild(_perso,name,emblem);
			GuildMember gm = G.addNewMember(_perso);
			gm.setAllRights(1,(byte) 0,1);//1 => Meneur (Tous droits)
			_perso.setGuildMember(gm);//On ajoute le meneur
			World.addGuild(G, true);
			SQLManager.UPDATE_GUILDMEMBER(gm);
			//Packets
			SocketManager.GAME_SEND_gS_PACKET(_perso, gm);
			SocketManager.GAME_SEND_gC_PACKET(_perso,"K");
			SocketManager.GAME_SEND_gV_PACKET(_perso);
		}catch(Exception e){return;};
	}

	private void parseChanelPacket(String packet)
	{
		switch(packet.charAt(1))
		{
			case 'C'://Changement des Canaux
				Chanels_change(packet);
			break;
		}
	}

	private void Chanels_change(String packet)
	{
		String chan = packet.charAt(3)+"";
		switch(packet.charAt(2))
		{
			case '+'://Ajout du Canal
				_perso.addChanel(chan);
			break;
			case '-'://Desactivation du canal
				_perso.removeChanel(chan);
			break;
		}
		SQLManager.SAVE_PERSONNAGE(_perso, false);
	}

	private void parseMountPacket(String packet)
	{
		switch(packet.charAt(1))
		{
		case 'b'://Achat d'un enclos
			SocketManager.GAME_SEND_R_PACKET(_perso, "v");//Fermeture du panneau
			MountPark MP = _perso.get_curCarte().getMountPark();
			Personnage Seller = null;
			if(MP.get_owner() != 0)
			{
			Seller = World.getPersonnage(MP.get_owner());
			}
			if(MP.get_price() == 0)
			{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "197");
			return;
			}
			if(_perso.get_guild() == null)
			{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "1135");
			return;
			}
			if(_perso.getGuildMember().getRank() != 1)
			{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "198"); 
			return;
			}
			if(Seller != null)
			{
			byte enclosMax = (byte)Math.floor(_perso.get_guild().get_lvl()/10);
			byte TotalEncloGuild = (byte)World.totalMPGuild(_perso.get_guild().get_id());
			if(TotalEncloGuild >= enclosMax)
			{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "1103");
			return;
			}
			if(_perso.get_kamas() < MP.get_price())
			{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "182");
			return;
			}
			long NewKamas = _perso.get_kamas()-MP.get_price();
			_perso.set_kamas(NewKamas);
			long NewSellerBankKamas = Seller.getBankKamas()+MP.get_price();
			Seller.setBankKamas(NewSellerBankKamas);
			if(Seller.isOnline())
			{
			SocketManager.GAME_SEND_MESSAGE(Seller, "Vous avez vendu une enclos : " + MP.get_map().getX() + "," + MP.get_map().getY(), Ancestra.CONFIG_MOTD_COLOR);
			}
			MP.set_price(0);//On vide le prix
			MP.set_owner(_perso.get_GUID());
			MP.set_guild(_perso.get_guild());
			SQLManager.SAVE_MOUNTPARK(MP);
			SQLManager.SAVE_PERSONNAGE(_perso, true);
			//On rafraichit l'enclo
			for(Personnage z:_perso.get_curCarte().getPersos())
			{
			SocketManager.GAME_SEND_Rp_PACKET(z, MP);
			}
			}else
			{
			long NewKamas = _perso.get_kamas()-MP.get_price();
			_perso.set_kamas(NewKamas);
			MP.set_price(0);//On vide le prix
			MP.set_owner(_perso.get_GUID());
			MP.set_guild(_perso.get_guild());
			SQLManager.SAVE_MOUNTPARK(MP);
			SQLManager.SAVE_PERSONNAGE(_perso, true);
			//On rafraichit l'enclo
			for(Personnage z:_perso.get_curCarte().getPersos())
			{
			SocketManager.GAME_SEND_Rp_PACKET(z, MP);
			}
			}
			break;
		
			case 'd'://Demande Description
				Mount_description(packet);
			break;
			
			case 'n'://Change le nom
				Mount_name(packet.substring(2));
			break;
			
			case 'r'://Monter sur la dinde
				Mount_ride();
			break;
			case 's'://Vendre l'enclo
				SocketManager.GAME_SEND_R_PACKET(_perso, "v");//Fermeture du panneau
				int price = Integer.parseInt(packet.substring(2));
				MountPark MP1 = _perso.get_curCarte().getMountPark();
				if(!MP1.getData().isEmpty())
				{
					SocketManager.GAME_SEND_MESSAGE(_perso, "[ENCLO] Impossible de vendre un enclo plein.", Ancestra.CONFIG_MOTD_COLOR);
					return;
				}
				if(MP1.get_owner() == -1)
				{
					SocketManager.GAME_SEND_Im_PACKET(_perso, "194");
					return;
				}
				if(MP1.get_owner() != _perso.get_GUID())
				{
					SocketManager.GAME_SEND_Im_PACKET(_perso, "195");
					return;
				}
				MP1.set_price(price);
				SQLManager.SAVE_MOUNTPARK(MP1);
				SQLManager.SAVE_PERSONNAGE(_perso, true);
				//On rafraichit l'enclo
				for(Personnage z:_perso.get_curCarte().getPersos())
				{
					SocketManager.GAME_SEND_Rp_PACKET(z, MP1);
				}
			break;
			case 'v'://Fermeture panneau d'achat
				SocketManager.GAME_SEND_R_PACKET(_perso, "v");
			break;
			case 'x'://Change l'xp donner a la dinde
				Mount_changeXpGive(packet);
			break;
			case 'c'://Castrer la dinde
				Mount_castrer();
			break;	
		}
	}

	private void Mount_changeXpGive(String packet)
	{
		try
		{
			int xp = Integer.parseInt(packet.substring(2));
			if(xp <0)xp = 0;
			if(xp >90)xp = 90;
			_perso.setMountGiveXp(xp);
			SocketManager.GAME_SEND_Rx_PACKET(_perso);
		}catch(Exception e){};
	}

	private void Mount_name(String name)
	{
		if(_perso.getMount() == null)return;
		_perso.getMount().setName(name);
		SocketManager.GAME_SEND_Rn_PACKET(_perso, name);
	}
	
	private void Mount_ride()
	{
		if(_perso.get_lvl()<60 || _perso.getMount() == null || !_perso.getMount().isMountable() || _perso._isGhosts)
		{
			SocketManager.GAME_SEND_Re_PACKET(_perso,"Er", null);
			return;
		}
		_perso.toogleOnMount();
	}
	
	private void Mount_description(String packet)
	{
		int DDid = -1;
		try
		{
			DDid = Integer.parseInt(packet.substring(2).split("\\|")[0]);
			//on ignore le temps?
		}catch(Exception e){};
		if(DDid == -1)return;
		Dragodinde DD = World.getDragoByID(DDid);
		if(DD == null)return;
		SocketManager.GAME_SEND_MOUNT_DESCRIPTION_PACKET(_perso,DD);
	}
	
	private void Mount_castrer() {
		if (_perso.getMount() == null) {
			SocketManager.GAME_SEND_Re_PACKET(_perso, "Er", null);
			return;
		}
		_perso.getMount().CastrerDinde();
		SocketManager.GAME_SEND_Re_PACKET(_perso, "+", _perso.getMount());
	}

	private void parse_friendPacket(String packet)
	{
		switch(packet.charAt(1))
		{
			case 'A'://Ajouter
				Friend_add(packet);
			break;
			case 'D'://Effacer un ami
				Friend_delete(packet);
			break;
			case 'L'://Liste
				SocketManager.GAME_SEND_FRIENDLIST_PACKET(_perso);
			break;
			case 'O':
				switch(packet.charAt(2))
				{
				case '-':
					 _perso.SetSeeFriendOnline(false);
					 SocketManager.GAME_SEND_BN(_perso);
					 break;
				 case'+':
					 _perso.SetSeeFriendOnline(true);
					 SocketManager.GAME_SEND_BN(_perso);
					 break;
				}
			break;
			case 'J': //Wife
				FriendLove(packet);
			break;
		}
	}

	private void FriendLove(String packet)
	{
		Personnage Wife = World.getPersonnage(_perso.getWife());
		if(Wife == null) return;
		_perso.RejoindeWife(Wife); // Correcion téléportation mariage
		if(!Wife.isOnline())
		{
			if(Wife.get_sexe() == 0) SocketManager.GAME_SEND_Im_PACKET(_perso, "140");
			else SocketManager.GAME_SEND_Im_PACKET(_perso, "139");
			
			SocketManager.GAME_SEND_FRIENDLIST_PACKET(_perso);
			return;
		}
		switch(packet.charAt(2))
		{
			case 'S'://Teleportation
				if(_perso.get_fight() != null)
					return;
				else
					_perso.meetWife(Wife);
			break;
			case 'C'://Suivre le deplacement
				if(packet.charAt(3) == '+'){//Si lancement de la traque
					if(_perso._Follows != null)
					{
						_perso._Follows._Follower.remove(_perso.get_GUID());
					}
					SocketManager.GAME_SEND_FLAG_PACKET(_perso, Wife);
					_perso._Follows = Wife;
					Wife._Follower.put(_perso.get_GUID(), _perso);
				}else{//On arrete de suivre
					SocketManager.GAME_SEND_DELETE_FLAG_PACKET(_perso);
					_perso._Follows = null;
					Wife._Follower.remove(_perso.get_GUID());
				}
			break;
		}
	} 
	
	private void Friend_delete(String packet) {
		if(_perso == null)return;
		int guid = -1;
		switch(packet.charAt(2))
		{
			case '%'://Nom de perso
				packet = packet.substring(3);
				Personnage P = World.getPersoByName(packet);
				if(P == null)//Si P est nul, ou si P est nonNul et P offline
				{
					SocketManager.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = P.getAccID();
				
			break;
			case '*'://Pseudo
				packet = packet.substring(3);
				Compte C = World.getCompteByPseudo(packet);
				if(C==null)
				{
					SocketManager.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = C.get_GUID();
			break;
			default:
				packet = packet.substring(2);
				Personnage Pr = World.getPersoByName(packet);
				if(Pr == null?true:!Pr.isOnline())//Si P est nul, ou si P est nonNul et P offline
				{
					SocketManager.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = Pr.get_compte().get_GUID();
			break;
		}
		if(guid == -1 || !_compte.isFriendWith(guid))
		{
			SocketManager.GAME_SEND_FD_PACKET(_perso, "Ef");
			return;
		}
		_compte.removeFriend(guid);
	}

	private void Friend_add(String packet)
	{
		if(_perso == null)return;
		int guid = -1;
		switch(packet.charAt(2))
		{
			case '%'://Nom de perso
				packet = packet.substring(3);
				Personnage P = World.getPersoByName(packet);
				if(P == null?true:!P.isOnline())//Si P est nul, ou si P est nonNul et P offline
				{
					SocketManager.GAME_SEND_FA_PACKET(_perso, "Ef");
					return;
				}
				guid = P.getAccID();
			break;
			case '*'://Pseudo
				packet = packet.substring(3);
				Compte C = World.getCompteByPseudo(packet);
				if(C==null?true:!C.isOnline())
				{
					SocketManager.GAME_SEND_FA_PACKET(_perso, "Ef");
					return;
				}
				guid = C.get_GUID();
			break;
			default:
				packet = packet.substring(2);
				Personnage Pr = World.getPersoByName(packet);
				if(Pr == null?true:!Pr.isOnline())//Si P est nul, ou si P est nonNul et P offline
				{
					SocketManager.GAME_SEND_FA_PACKET(_perso, "Ef");
					return;
				}
				guid = Pr.get_compte().get_GUID();
			break;
		}
		if(guid == -1)
		{
			SocketManager.GAME_SEND_FA_PACKET(_perso, "Ef");
			return;
		}
		_compte.addFriend(guid);
	}

	private void parseGroupPacket(String packet)
	{
		switch(packet.charAt(1))
		{
			case 'A'://Accepter invitation
				group_accept(packet);
			break;
			
			case 'F'://Suivre membre du groupe PF+GUID
				Group g = _perso.getGroup();
				if(g == null)return;
				
				int pGuid = -1;
				try
				{
					pGuid = Integer.parseInt(packet.substring(3));
				}catch(NumberFormatException e){return;};
				
				if(pGuid == -1) return;
				
				Personnage P = World.getPersonnage(pGuid);
				
				if(P == null || !P.isOnline()) return;
				
				if(packet.charAt(2) == '+')//Suivre
				{
					if(_perso._Follows != null)
					{
						_perso._Follows._Follower.remove(_perso.get_GUID());
					}
					SocketManager.GAME_SEND_FLAG_PACKET(_perso, P);
					SocketManager.GAME_SEND_PF(_perso, "+"+P.get_GUID());
					_perso._Follows = P;
					P._Follower.put(_perso.get_GUID(), _perso);
				}
				else if(packet.charAt(2) == '-')//Ne plus suivre
				{
					SocketManager.GAME_SEND_DELETE_FLAG_PACKET(_perso);
					SocketManager.GAME_SEND_PF(_perso, "-");
					_perso._Follows = null;
					P._Follower.remove(_perso.get_GUID());
				}
			break;
			case 'G'://Suivez le tous PG+GUID
				Group g2 = _perso.getGroup();
				if(g2 == null)return;
				
				int pGuid2 = -1;
				try
				{
					pGuid2 = Integer.parseInt(packet.substring(3));
				}catch(NumberFormatException e){return;};
				
				if(pGuid2 == -1) return;
				
				Personnage P2 = World.getPersonnage(pGuid2);
				
				if(P2 == null || !P2.isOnline()) return;
				
				if(packet.charAt(2) == '+')//Suivre
				{
					for(Personnage T : g2.getPersos())
					{
						if(T.get_GUID() == P2.get_GUID()) continue;
						if(T._Follows != null)
						{
							T._Follows._Follower.remove(_perso.get_GUID());
						}
						SocketManager.GAME_SEND_FLAG_PACKET(T, P2);
						SocketManager.GAME_SEND_PF(T, "+"+P2.get_GUID());
						T._Follows = P2;
						P2._Follower.put(T.get_GUID(), T);
					}
				}
				else if(packet.charAt(2) == '-')//Ne plus suivre
				{
					for(Personnage T : g2.getPersos())
					{
						if(T.get_GUID() == P2.get_GUID()) continue;
						SocketManager.GAME_SEND_DELETE_FLAG_PACKET(T);
						SocketManager.GAME_SEND_PF(T, "-");
						T._Follows = null;
						P2._Follower.remove(T.get_GUID());
					}
				}
			break;
			
			case 'I'://inviation
				group_invite(packet);
			break;
			
			case 'R'://Refuse
				group_refuse();
			break;
			
			case 'V'://Quitter
				group_quit(packet);
			break;
			case 'W'://Localisation du groupe
				group_locate();
			break;
		}
	}
	
	private void group_locate()
	{
		if(_perso == null)return;
		Group g = _perso.getGroup();
		if(g == null)return;
		String str = "";
		boolean isFirst = true;
		for(Personnage GroupP : _perso.getGroup().getPersos())
		{
			if(!isFirst) str += "|";
			str += GroupP.get_curCarte().getX()+";"+GroupP.get_curCarte().getY()+";"+GroupP.get_curCarte().get_id()+";2;"+GroupP.get_GUID()+";"+GroupP.get_name();
			isFirst = false;
		}
		SocketManager.GAME_SEND_IH_PACKET(_perso, str);
	}
	
	private void group_quit(String packet)
	{
		if(_perso == null)return;
		Group g = _perso.getGroup();
		if(g == null)return;
		if(packet.length() == 2)//Si aucun guid est spécifié, alors c'est que le joueur quitte
		{
			 g.leave(_perso);
			 SocketManager.GAME_SEND_PV_PACKET(_out,"");
			SocketManager.GAME_SEND_IH_PACKET(_perso, "");
		}else if(g.isChief(_perso.get_GUID()))//Sinon, c'est qu'il kick un joueur du groupe
		{
			int guid = -1;
			try
			{
				guid = Integer.parseInt(packet.substring(2));
			}catch(NumberFormatException e){return;};
			if(guid == -1)return;
			Personnage t = World.getPersonnage(guid);
			g.leave(t);
			SocketManager.GAME_SEND_PV_PACKET(t.get_compte().getGameThread().get_out(),""+_perso.get_GUID());
			SocketManager.GAME_SEND_IH_PACKET(t, "");
		}
	}

	private void group_invite(String packet)
	{
		if(_perso == null)return;
		String name = packet.substring(2);
		Personnage target = World.getPersoByName(name);
		if(target == null)return;
		if(!target.isOnline())
		{
			SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(_out,"n"+name);
			return;
		}
		if(target.getGroup() != null)
		{
			SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(_out, "a"+name);
			return;
		}
		if(_perso.getGroup() != null && _perso.getGroup().getPersosNumber() == 8)
		{
			SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(_out, "f");
			return;
		}
		target.setInvitation(_perso.get_GUID());	
		_perso.setInvitation(target.get_GUID());
		SocketManager.GAME_SEND_GROUP_INVITATION(_out,_perso.get_name(),name);
		SocketManager.GAME_SEND_GROUP_INVITATION(target.get_compte().getGameThread().get_out(),_perso.get_name(),name);
	}

	private void group_refuse()
	{
		if(_perso == null)return;
		if(_perso.getInvitation() == 0)return;
		_perso.setInvitation(0);
		SocketManager.GAME_SEND_BN(_out);
		Personnage t = World.getPersonnage(_perso.getInvitation());
		if(t == null) return;
		t.setInvitation(0);
		SocketManager.GAME_SEND_PR_PACKET(t);
	}

	private void group_accept(String packet)
	{
		if(_perso == null)return;
		if(_perso.getInvitation() == 0)return;
		Personnage t = World.getPersonnage(_perso.getInvitation());
		if(t == null) return;
		Group g = t.getGroup();
		if(g == null)
		{
			g = new Group(t,_perso);
			SocketManager.GAME_SEND_GROUP_CREATE(_out,g);
			SocketManager.GAME_SEND_PL_PACKET(_out,g);
			SocketManager.GAME_SEND_GROUP_CREATE(t.get_compte().getGameThread().get_out(),g);
			SocketManager.GAME_SEND_PL_PACKET(t.get_compte().getGameThread().get_out(),g);
			t.setGroup(g);
			SocketManager.GAME_SEND_ALL_PM_ADD_PACKET(t.get_compte().getGameThread().get_out(),g);
		}
		else
		{
			SocketManager.GAME_SEND_GROUP_CREATE(_out,g);
			SocketManager.GAME_SEND_PL_PACKET(_out,g);
			SocketManager.GAME_SEND_PM_ADD_PACKET_TO_GROUP(g, _perso);
			g.addPerso(_perso);
		}
		_perso.setGroup(g);
		SocketManager.GAME_SEND_ALL_PM_ADD_PACKET(_out,g);
		SocketManager.GAME_SEND_PR_PACKET(t);
	}

	private void parseObjectPacket(String packet)
	{
		switch(packet.charAt(1))
		{
			case 'd'://Supression d'un objet
				Object_delete(packet);
				break;
			case 'D'://Depose l'objet au sol
				Object_drop(packet);
				break;
			case 'M'://Bouger un objet (Equiper/déséquiper) // Associer obvijevan
				Object_move(packet);
				break;
			case 'U'://Utiliser un objet (potions)
				Object_use(packet);
				break;
			case 'x':
			    Object_obvijevan_desassocier(packet);
			    break;
			case 'f':
				Object_obvijevan_feed(packet);
				break;
			case 's':
				Object_obvijevan_changeApparence(packet);
		}
	}

	private void Object_drop(String packet)
	{
		int guid = -1;
		int qua = -1;
		try
		{
			guid = Integer.parseInt(packet.substring(2).split("\\|")[0]);
			qua = Integer.parseInt(packet.split("\\|")[1]);
		}catch(Exception e){};
		if(guid == -1 || qua <= 0 || !_perso.hasItemGuid(guid) || _perso.get_fight() != null || _perso.is_away())return;
		Objet obj = World.getObjet(guid);
		
		_perso.set_curCell(_perso.get_curCell());
		int cellPosition = Constants.getNearCellidUnused(_perso);
		if(cellPosition < 0)
		{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "1145");
			return;
		}
		if(obj.getPosition() != Constants.ITEM_POS_NO_EQUIPED)
		{
			obj.setPosition(Constants.ITEM_POS_NO_EQUIPED);
			SocketManager.GAME_SEND_OBJET_MOVE_PACKET(_perso,obj);
			if(obj.getPosition() == Constants.ITEM_POS_ARME 		||
				obj.getPosition() == Constants.ITEM_POS_COIFFE 		||
				obj.getPosition() == Constants.ITEM_POS_FAMILIER 	||
				obj.getPosition() == Constants.ITEM_POS_CAPE		||
				obj.getPosition() == Constants.ITEM_POS_BOUCLIER	||
				obj.getPosition() == Constants.ITEM_POS_NO_EQUIPED)
					SocketManager.GAME_SEND_ON_EQUIP_ITEM(_perso.get_curCarte(), _perso);
		}
		if(qua >= obj.getQuantity())
		{
			_perso.removeItem(guid);
			_perso.get_curCarte().getCase(_perso.get_curCell().getID()+cellPosition).addDroppedItem(obj);
			obj.setPosition(Constants.ITEM_POS_NO_EQUIPED);
			SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_perso, guid);
		}else
		{
			obj.setQuantity(obj.getQuantity() - qua);
			Objet obj2 = Objet.getCloneObjet(obj, qua);
			obj2.setPosition(Constants.ITEM_POS_NO_EQUIPED);
			_perso.get_curCarte().getCase(_perso.get_curCell().getID()+cellPosition).addDroppedItem(obj2);
			SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso, obj);
		}
		SocketManager.GAME_SEND_Ow_PACKET(_perso);
		SocketManager.GAME_SEND_GDO_PACKET_TO_MAP(_perso.get_curCarte(),'+',_perso.get_curCarte().getCase(_perso.get_curCell().getID()+cellPosition).getID(),obj.getTemplate().getID(),0);
		SocketManager.GAME_SEND_STATS_PACKET(_perso);
	}

	private void Object_use(String packet)
	{
		int guid = -1;
		int targetGuid = -1;
		short cellID = -1;
		Personnage Target = null;
		try
		{
			String[] infos = packet.substring(2).split("\\|");
			guid = Integer.parseInt(infos[0]);
			try
			{
				targetGuid = Integer.parseInt(infos[1]);
			}catch(Exception e){targetGuid = -1;};
			try
			{
				cellID = Short.parseShort(infos[2]);
			}catch(Exception e){cellID = -1;};
		}catch(Exception e){return;};
		//Si le joueur n'a pas l'objet
		if(World.getPersonnage(targetGuid) != null)
		{
			Target = World.getPersonnage(targetGuid);
		}
		if(!_perso.hasItemGuid(guid) || _perso.get_fight() != null || _perso.is_away())return;
		if(Target != null && (Target.get_fight() != null || Target.is_away()))return;
		Objet obj = World.getObjet(guid);
		if(obj == null) return;
		ObjTemplate T = obj.getTemplate();
		if(!obj.getTemplate().getConditions().equalsIgnoreCase("") && !ConditionParser.validConditions(_perso,obj.getTemplate().getConditions()))
		{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "119|43");
			return;
		}
		T.applyAction(_perso, Target, guid, cellID);
	}
	
	private synchronized void Object_move(String packet)
	{
		String[] infos = packet.substring(2).split(""+(char)0x0A)[0].split("\\|");
		try
		{
			int qua;
			int guid = Integer.parseInt(infos[0]);
			int pos = Integer.parseInt(infos[1]);
			try
			{
				qua = Integer.parseInt(infos[2]);
			}catch(Exception e)
			{
				qua = 1;
			}
			Objet obj = World.getObjet(guid);
			// LES VERIFS
			if(!_perso.hasItemGuid(guid) || obj == null) // item n'existe pas ou perso n'a pas l'item
				return;
			if(_perso.get_fight() != null) // si en combat démarré
				if(_perso.get_fight().get_state() > Constants.FIGHT_STATE_ACTIVE)
					return;
			if(!Constants.isValidPlaceForItem(obj.getTemplate(),pos) && pos != Constants.ITEM_POS_NO_EQUIPED) // si mauvaise place
				return;
			if(!obj.getTemplate().getConditions().equalsIgnoreCase("") && !ConditionParser.validConditions(_perso,obj.getTemplate().getConditions())) {
				SocketManager.GAME_SEND_Im_PACKET(_perso, "119|43"); // si le perso ne vérifie pas les conditions diverses
				return;
			}
			if(obj.getTemplate().getLevel() > _perso.get_lvl())  {// si le perso n'a pas le level
				SocketManager.GAME_SEND_OAEL_PACKET(_out);
				return;
			}
			//On ne peut équiper 2 items de panoplies identiques, ou 2 Dofus identiques
			if(pos != Constants.ITEM_POS_NO_EQUIPED && (obj.getTemplate().getPanopID() != -1 || obj.getTemplate().getType() == Constants.ITEM_TYPE_DOFUS )&& _perso.hasEquiped(obj.getTemplate().getID()))
				return;
			// FIN DES VERIFS
			
			
			Objet exObj = _perso.getObjetByPos(pos);//Objet a l'ancienne position
		    int objGUID = obj.getTemplate().getID();
		    // CODE OBVI
			if ((objGUID == 9234) || (objGUID == 9233) || (objGUID == 9255) || (objGUID == 9256))
			{
				// LES VERFIS
				if (exObj == null) 	{// si on place l'obvi sur un emplacement vide
					SocketManager.send(_perso, "Im1161");
					return;	
				}
				if (exObj.getObvijevanPos() != 0) {// si il y a déjà un obvi
					SocketManager.GAME_SEND_BN(_perso);
					return;
				}
				// FIN DES VERIFS
		        		
				exObj.setObvijevanPos(obj.getObvijevanPos()); // L'objet qui était en place a maintenant un obvi
					
				_perso.removeItem(obj.getGuid(), 1, false, false); // on enlève l'existance de l'obvi en lui-même
				SocketManager.send(_perso, "OR" + obj.getGuid()); // on le précise au client
					
				StringBuilder cibleNewStats = new StringBuilder();
				cibleNewStats.append(obj.parseStatsStringSansUserObvi()).append(",").append(exObj.parseStatsStringSansUserObvi());
				cibleNewStats.append(",3ca#").append(Integer.toHexString(objGUID)).append("#0#0#0d0+").append(objGUID);
				exObj.clearStats();
				exObj.parseStringToStats(cibleNewStats.toString());
					
				SocketManager.send(_perso, exObj.obvijevanOCO_Packet(pos));
					
				if ((objGUID == 9233) || (objGUID == 9234)) 
					SocketManager.GAME_SEND_ON_EQUIP_ITEM(_perso.get_curCarte(), _perso); // Si l'obvi était cape ou coiffe : packet au client
				// S'il y avait plusieurs objets
				if(obj.getQuantity() > 1)
				{
					if(qua > obj.getQuantity())
						qua = obj.getQuantity();
					
					if(obj.getQuantity() - qua > 0)//Si il en reste
					{
						int newItemQua = obj.getQuantity()-qua;
						Objet newItem = Objet.getCloneObjet(obj,newItemQua);
						_perso.addObjet(newItem,false);
						World.addObjet(newItem,true);
						obj.setQuantity(qua);
						SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso, obj);
					}
				}
				
				return; // on s'arrête là pour l'obvi
			} // FIN DU CODE OBVI
			
			if(exObj != null)//S'il y avait déja un objet sur cette place on déséquipe
			{
				Objet obj2;
				if((obj2 = _perso.getSimilarItem(exObj)) != null)//On le possède deja
				{
					obj2.setQuantity(obj2.getQuantity()+exObj.getQuantity());
					SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso, obj2);
					World.removeItem(exObj.getGuid());
					_perso.removeItem(exObj.getGuid());
					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_perso, exObj.getGuid());
				}
				else//On ne le possède pas
				{
					exObj.setPosition(Constants.ITEM_POS_NO_EQUIPED);
					SocketManager.GAME_SEND_OBJET_MOVE_PACKET(_perso,exObj);
				}
				if(_perso.getObjetByPos(Constants.ITEM_POS_ARME) == null)
					SocketManager.GAME_SEND_OT_PACKET(_out, -1);
				
				//Si objet de panoplie
				if(exObj.getTemplate().getPanopID() > 0)
					SocketManager.GAME_SEND_OS_PACKET(_perso,exObj.getTemplate().getPanopID());
			}else//getNumbEquipedItemOfPanoplie(exObj.getTemplate().getPanopID()
			{
				Objet obj2;
				//On a un objet similaire
				if((obj2 = _perso.getSimilarItem(obj)) != null)
				{
					if(qua > obj.getQuantity()) qua = 
							obj.getQuantity();
					
					obj2.setQuantity(obj2.getQuantity()+qua);
					SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso, obj2);
					
					if(obj.getQuantity() - qua > 0)//Si il en reste
					{
						obj.setQuantity(obj.getQuantity()-qua);
						SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso, obj);
					}else//Sinon on supprime
					{
						World.removeItem(obj.getGuid());
						_perso.removeItem(obj.getGuid());
						SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_perso, obj.getGuid());
					}
				}
				else//Pas d'objets similaires
				{
					obj.setPosition(pos);
					SocketManager.GAME_SEND_OBJET_MOVE_PACKET(_perso,obj);
					if(obj.getQuantity() > 1)
					{
						if(qua > obj.getQuantity()) qua = obj.getQuantity();
						
						if(obj.getQuantity() - qua > 0)//Si il en reste
						{
							int newItemQua = obj.getQuantity()-qua;
							Objet newItem = Objet.getCloneObjet(obj,newItemQua);
							_perso.addObjet(newItem,false);
							World.addObjet(newItem,true);
							obj.setQuantity(qua);
							SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso, obj);
						}
					}
				}
			}
			SocketManager.GAME_SEND_Ow_PACKET(_perso);
			_perso.refreshStats();
			if(_perso.getGroup() != null)
			{
				SocketManager.GAME_SEND_PM_MOD_PACKET_TO_GROUP(_perso.getGroup(),_perso);
			}
			SocketManager.GAME_SEND_STATS_PACKET(_perso);
			if( pos == Constants.ITEM_POS_ARME 		||
				pos == Constants.ITEM_POS_COIFFE 	||
				pos == Constants.ITEM_POS_FAMILIER 	||
				pos == Constants.ITEM_POS_CAPE		||
				pos == Constants.ITEM_POS_BOUCLIER	||
				pos == Constants.ITEM_POS_NO_EQUIPED)
				SocketManager.GAME_SEND_ON_EQUIP_ITEM(_perso.get_curCarte(), _perso);
		
			//Si familier
			if(pos == Constants.ITEM_POS_FAMILIER && _perso.isOnMount())_perso.toogleOnMount();
			//Nourir dragodinde
			Dragodinde DD = _perso.getMount();
			if(pos == Constants.ITEM_POS_DRAGODINDE) {
				if(_perso.getMount() == null) {
					SocketManager.GAME_SEND_MESSAGE(_perso, "Votre personnage ne possède pas de dragodinde sur lui, il ne peut donc en nourir ...", Ancestra.CONFIG_MOTD_COLOR);
				} else {
					if (obj.getTemplate().getType() == 41 || obj.getTemplate().getType() == 63) {
						int totalwin = qua * 10;
						if(DD.isInfatiguable() == true) totalwin = qua * 10 * 2;
						int winEnergie = DD.get_energie()+totalwin;
						DD.setEnergie(winEnergie);
						SocketManager.GAME_SEND_Re_PACKET(_perso, "+", DD);
						_perso.deleteItem(guid);
						World.removeItem(guid);
						SocketManager.ENVIAR_OR_ELIMINAR_OBJETO(_perso, guid);
						SocketManager.GAME_SEND_MESSAGE(_perso, "Votre dragodinde a gagné "+totalwin+" en énergie.", Ancestra.CONFIG_MOTD_COLOR);
					} else {
						SocketManager.GAME_SEND_MESSAGE(_perso, "Nourriture pour dragodinde incomestible !", Ancestra.CONFIG_MOTD_COLOR);
					}
				}
			}
			//Verif pour les outils de métier
			if(pos == Constants.ITEM_POS_NO_EQUIPED && _perso.getObjetByPos(Constants.ITEM_POS_ARME) == null)
				SocketManager.GAME_SEND_OT_PACKET(_out, -1);
			
			if(pos == Constants.ITEM_POS_ARME && _perso.getObjetByPos(Constants.ITEM_POS_ARME) != null)
			{
				int ID = _perso.getObjetByPos(Constants.ITEM_POS_ARME).getTemplate().getID();
				for(Entry<Integer,StatsMetier> e : _perso.getMetiers().entrySet())
				{
					if(e.getValue().getTemplate().isValidTool(ID))
						SocketManager.GAME_SEND_OT_PACKET(_out,e.getValue().getTemplate().getId());
				}
			}
			//Si objet de panoplie
			if(obj.getTemplate().getPanopID() > 0)SocketManager.GAME_SEND_OS_PACKET(_perso,obj.getTemplate().getPanopID());
			//Si en combat
			if(_perso.get_fight() != null)
			{
				SocketManager.GAME_SEND_ON_EQUIP_ITEM_FIGHT(_perso, _perso.get_fight().getFighterByPerso(_perso), _perso.get_fight());
			}
		}catch(Exception e)
		{
			e.printStackTrace();
			SocketManager.GAME_SEND_DELETE_OBJECT_FAILED_PACKET(_out);
		}
	}

	private void Object_delete(String packet)
	{
		String[] infos = packet.substring(2).split("\\|");
		try
		{
			int guid = Integer.parseInt(infos[0]);
			int qua = 1;
			try
			{
				qua = Integer.parseInt(infos[1]);
			}catch(Exception e){};
			Objet obj = World.getObjet(guid);
			if(obj == null || !_perso.hasItemGuid(guid) || qua <= 0 || _perso.get_fight() != null || _perso.is_away())
			{
				SocketManager.GAME_SEND_DELETE_OBJECT_FAILED_PACKET(_out);
				return;
			}
			int newQua = obj.getQuantity()-qua;
			if(newQua <=0)
			{
				_perso.removeItem(guid);
				World.removeItem(guid);
				SQLManager.DELETE_ITEM(guid);
				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_perso, guid);
			}else
			{
				obj.setQuantity(newQua);
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso, obj);
			}
			SocketManager.GAME_SEND_STATS_PACKET(_perso);
			SocketManager.GAME_SEND_Ow_PACKET(_perso);
		}catch(Exception e)
		{
			SocketManager.GAME_SEND_DELETE_OBJECT_FAILED_PACKET(_out);
		}
	}

	private void parseDialogPacket(String packet)
	{
		switch(packet.charAt(1))
		{
			case 'C'://Demande de l'initQuestion
				Dialog_start(packet);
			break;
			
			case 'R'://Réponse du joueur
				Dialog_response(packet);
			break;
			
			case 'V'://Fin du dialog
				Dialog_end();
			break;
		}
	}

	private void Dialog_response(String packet)
	{
		String[] infos = packet.substring(2).split("\\|");
		try
		{
			int qID = Integer.parseInt(infos[0]);
			int rID = Integer.parseInt(infos[1]);
			NPC_question quest = World.getNPCQuestion(qID);
			NPC_reponse rep = World.getNPCreponse(rID);
			if(quest == null || rep == null || !rep.isAnotherDialog())
			{
				SocketManager.GAME_SEND_END_DIALOG_PACKET(_out);
				_perso.set_isTalkingWith(0);
			}
			rep.apply(_perso);
		}catch(Exception e)
		{
			SocketManager.GAME_SEND_END_DIALOG_PACKET(_out);
		}
	}

	private void Dialog_end()
	{
		SocketManager.GAME_SEND_END_DIALOG_PACKET(_out);
		if(_perso.get_isTalkingWith() != 0)
			_perso.set_isTalkingWith(0);
	}

	private void Dialog_start(String packet)
	{
		try
		{
			int npcID = Integer.parseInt(packet.substring(2).split((char)0x0A+"")[0]);
			NPC npc = _perso.get_curCarte().getNPC(npcID);
			if( npc == null)return;
			SocketManager.GAME_SEND_DCK_PACKET(_out,npcID);
			int qID = npc.get_template().get_initQuestionID();
			NPC_question quest = World.getNPCQuestion(qID);
			if(quest == null)
			{
				SocketManager.GAME_SEND_END_DIALOG_PACKET(_out);
				return;
			}
			SocketManager.GAME_SEND_QUESTION_PACKET(_out,quest.parseToDQPacket(_perso));
			_perso.set_isTalkingWith(npcID);
		}catch(NumberFormatException e){};
	}

	private void parseExchangePacket(String packet)
	{	
		switch(packet.charAt(1))
		{
			case 'A'://Accepter demande d'échange
				Exchange_accept();
			break;
			case 'B'://Achat
				Exchange_onBuyItem(packet);
			break;
			
			case 'H'://Demande prix moyen + catégorie
				Exchange_HDV(packet);
			break;
			
			case 'K'://Ok
				Exchange_isOK();
			break;
			case 'L'://jobAction : Refaire le craft précedent
				Exchange_doAgain();
			break;
			
			case 'M'://Move (Ajouter//retirer un objet a l'échange)
				Exchange_onMoveItem(packet);
			break;
			
			case 'q'://Mode marchand
				if(_perso.get_isTradingWith() > 0 || _perso.get_fight() != null || _perso.is_away())return;
		        if (_perso.get_curCarte().getStoreCount() == 5)
		        {
		        	SocketManager.GAME_SEND_Im_PACKET(_perso, "125;5");
		        	return;
		        }
		        if (_perso.parseStoreItemsList().isEmpty())
		        {
		        	SocketManager.GAME_SEND_Im_PACKET(_perso, "123");
		        	return;
		        }
		        int orientation = Formulas.getRandomValue(1, 3);
		        _perso.set_orientation(orientation);
		        Carte map = _perso.get_curCarte();
		        _perso.set_showSeller(true);
		        World.addSeller(_perso);
		        kick();
		        for(Personnage z : map.getPersos())
		        {
		        	if(z != null && z.isOnline())
		        		SocketManager.GAME_SEND_MERCHANT_LIST(z, z.get_curCarte().get_id());
		        }
			break;
			case 'r'://Rides => Monture
				Exchange_mountPark(packet);
			break;
			
			case 'R'://liste d'achat NPC
				Exchange_start(packet);
			break;
			case 'S'://Vente
				Exchange_onSellItem(packet);
			break;
			
			case 'V'://Fin de l'échange
				Exchange_finish_buy();
			break;
			case 'J'://Livre de métiers
				Book_open(packet.substring(3));
			break;
		}
	}
	
	private void Book_open(String packet) {
		int v = Integer.parseInt(packet);
		if(!_perso.get_curCarte().hasatelierfor(v))return;
		switch(v){
		case 2:
			for(Entry<Integer,StatsMetier> al : World.upB.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 2,i , al.getValue());
			}
			break;
		case 11:
			for(Entry<Integer,StatsMetier> al : World.upFE.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 11,i , al.getValue());
			}
			break;
		case 13:
			for(Entry<Integer,StatsMetier> al : World.upSA.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 13,i , al.getValue());
			}
			break;
		case 14:
			for(Entry<Integer,StatsMetier> al : World.upFM.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 14,i , al.getValue());
			}
			break;
		case 15:
			for(Entry<Integer,StatsMetier> al : World.upCo.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 15,i , al.getValue());
			}
			break;
		case 16:
			for(Entry<Integer,StatsMetier> al : World.upBi.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 16,i , al.getValue());
			}
			break;
		case 17:
			for(Entry<Integer,StatsMetier> al : World.upFD.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 17,i , al.getValue());
			}
			break;
		case 18:
			for(Entry<Integer,StatsMetier> al : World.upSB.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 18,i , al.getValue());
			}
			break;
		case 19:
			for(Entry<Integer,StatsMetier> al : World.upSBg.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 19,i , al.getValue());
			}
			break;
		case 20:
			for(Entry<Integer,StatsMetier> al : World.upFP.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 20,i , al.getValue());
			}
			break;
		case 24:
			for(Entry<Integer,StatsMetier> al : World.upM.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 24,i , al.getValue());
			}
			break;
		case 25:
			for(Entry<Integer,StatsMetier> al : World.upBou.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 25,i , al.getValue());
			}
			break;
		case 26:
			for(Entry<Integer,StatsMetier> al : World.upAlchi.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 26,i , al.getValue());
			}
			break;
		case 27:
			for(Entry<Integer,StatsMetier> al : World.upT.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 27,i , al.getValue());
			}
			break;
		case 28:
			for(Entry<Integer,StatsMetier> al : World.upP.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 28,i , al.getValue());
			}
			break;
		case 31:
			for(Entry<Integer,StatsMetier> al : World.upFH.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 31,i , al.getValue());
			}
			break;
		case 36:
			for(Entry<Integer,StatsMetier> al : World.upFPc.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 36,i , al.getValue());
			}
			break;
		case 41:
			for(Entry<Integer,StatsMetier> al : World.upC.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 41,i , al.getValue());
			}
			break;
		case 43:
			for(Entry<Integer,StatsMetier> al : World.upFMD.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 43,i , al.getValue());
			}
			break;
		case 44:
			for(Entry<Integer,StatsMetier> al : World.upFME.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 44,i , al.getValue());
			}
			break;
		case 45:
			for(Entry<Integer,StatsMetier> al : World.upFMM.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 45,i , al.getValue());
			}
			break;
		case 46:
			for(Entry<Integer,StatsMetier> al : World.upFMP.entrySet()){
				int i = al.getKey();
				SocketManager.GAME_SEND_EJ_PACKET(_perso, 46,i , al.getValue());
			}
			break;
		case 47:
				for(Entry<Integer,StatsMetier> al : World.upFMH.entrySet()){
					int i = al.getKey();
					SocketManager.GAME_SEND_EJ_PACKET(_perso, 47,i , al.getValue());
				}
				break;
			case 48:
				for(Entry<Integer,StatsMetier> al : World.upSMA.entrySet()){
					int i = al.getKey();
					SocketManager.GAME_SEND_EJ_PACKET(_perso, 48,i , al.getValue());
				}
				break;
			case 49:
				for(Entry<Integer,StatsMetier> al : World.upSMB.entrySet()){
					int i = al.getKey();
					SocketManager.GAME_SEND_EJ_PACKET(_perso, 49,i , al.getValue());
				}
				break;
			case 50:
				for(Entry<Integer,StatsMetier> al : World.upSMBg.entrySet()){
					int i = al.getKey();
					SocketManager.GAME_SEND_EJ_PACKET(_perso, 50,i , al.getValue());
				}
				break;
			case 56:
				for(Entry<Integer,StatsMetier> al : World.upBouc.entrySet()){
					int i = al.getKey();
					SocketManager.GAME_SEND_EJ_PACKET(_perso, 56,i , al.getValue());
				}
				break;
			case 58:
				for(Entry<Integer,StatsMetier> al : World.upPO.entrySet()){
					int i = al.getKey();
					SocketManager.GAME_SEND_EJ_PACKET(_perso, 58,i , al.getValue());
				}
				break;
			case 60:
				for(Entry<Integer,StatsMetier> al : World.upFBou.entrySet()){
					int i = al.getKey();
					SocketManager.GAME_SEND_EJ_PACKET(_perso, 60,i , al.getValue());
				}
				break;
			case 63:
				for(Entry<Integer,StatsMetier> al : World.upJM.entrySet()){
					int i = al.getKey();
					SocketManager.GAME_SEND_EJ_PACKET(_perso, 62,i , al.getValue());
				}
				break;
			case 64:
				for(Entry<Integer,StatsMetier> al : World.upCRM.entrySet()){
					int i = al.getKey();
					SocketManager.GAME_SEND_EJ_PACKET(_perso, 64,i , al.getValue());
				}
				break;
			case 65:
				for(Entry<Integer,StatsMetier> al : World.upBrico.entrySet()){
					int i = al.getKey();
					SocketManager.GAME_SEND_EJ_PACKET(_perso, 65,i , al.getValue());
				}
				break;
		default:
			return;
		}
	}
	
	private void Exchange_HDV(String packet)
	{
		if(_perso.get_isTradingWith() > 0 || _perso.get_fight() != null || _perso.is_away())return;
		int templateID;
		switch(packet.charAt(2))
		{
			case 'B': //Confirmation d'achat
				String[] info = packet.substring(3).split("\\|");//ligneID|amount|price
				
				HDV curHdv = World.getHdv(Math.abs(_perso.get_isTradingWith()));
				
				int ligneID = Integer.parseInt(info[0]);
				byte amount = Byte.parseByte(info[1]);
				
				if(curHdv.buyItem(ligneID,amount,Integer.parseInt(info[2]),_perso))
				{
					SocketManager.GAME_SEND_EHm_PACKET(_perso,"-",ligneID+"");//Enleve la ligne
					if(curHdv.getLigne(ligneID) != null && !curHdv.getLigne(ligneID).isEmpty())
						SocketManager.GAME_SEND_EHm_PACKET(_perso, "+", curHdv.getLigne(ligneID).parseToEHm());//Réajoute la ligne si elle n'est pas vide
					
					/*if(curHdv.getLigne(ligneID) != null)
					{
						String str = curHdv.getLigne(ligneID).parseToEHm();
						SocketManager.GAME_SEND_EHm_PACKET(_perso,"+",str);
					}*/
					
					
					_perso.refreshStats();
					SocketManager.GAME_SEND_Ow_PACKET(_perso);
					SocketManager.GAME_SEND_Im_PACKET(_perso,"068");//Envoie le message "Lot acheté"
				}
				else
				{
					SocketManager.GAME_SEND_Im_PACKET(_perso,"172");//Envoie un message d'erreur d'achat
				}
			break;
			case 'l'://Demande listage d'un template (les prix)
				templateID = Integer.parseInt(packet.substring(3));
				try
				{
					SocketManager.GAME_SEND_EHl(_perso,World.getHdv(Math.abs(_perso.get_isTradingWith())),templateID);
				}catch(NullPointerException e)//Si erreur il y a, retire le template de la liste chez le client
				{
					SocketManager.GAME_SEND_EHM_PACKET(_perso,"-",templateID+"");
				}
				
			break;
			case 'P'://Demande des prix moyen
				templateID = Integer.parseInt(packet.substring(3));
				SocketManager.GAME_SEND_EHP_PACKET(_perso,templateID);
			break;			
			case 'T'://Demande des template de la catégorie
				int categ = Integer.parseInt(packet.substring(3));
				String allTemplate = World.getHdv(Math.abs(_perso.get_isTradingWith())).parseTemplate(categ);
				SocketManager.GAME_SEND_EHL_PACKET(_perso,categ,allTemplate);
			break;			
		}
	}
	
	private void Exchange_mountPark(String packet)
	{
		//Si dans un enclos
		if(_perso.getInMountPark() != null)
		{
			MountPark MP = _perso.getInMountPark();
			if(_perso.get_isTradingWith() > 0 || _perso.get_fight() != null)return;
			char c = packet.charAt(2);
			packet = packet.substring(3);
			int guid = -1;
			try
			{
				guid = Integer.parseInt(packet);
			}catch(Exception e){};
			switch(c)
			{
				case 'C'://Parcho => Etable (Stocker)
					if(guid == -1 || !_perso.hasItemGuid(guid))return;
					if(MP.get_size() <= MP.MountParkDATASize())
					{
						SocketManager.GAME_SEND_Im_PACKET(_perso, "1145");
						return;
					}
					Objet obj = World.getObjet(guid);
					//on prend la DD demandée
					int DDid = obj.getStats().getEffect(995);
					Dragodinde DD = World.getDragoByID(DDid);
					//FIXME mettre return au if pour ne pas créer des nouvelles dindes
					if(DD == null)
					{
						int color = Constants.getMountColorByParchoTemplate(obj.getTemplate().getID());
						if(color <1)return;
						DD = new Dragodinde(color);
					}
					//On enleve l'objet du Monde et du Perso
					_perso.removeItem(guid);
					World.removeItem(guid);
					//on ajoute la dinde a l'étable
					MP.addData(DD.get_id(), _perso.get_GUID());
					SQLManager.UPDATE_MOUNTPARK(MP);
					//On envoie les packet
					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_perso,obj.getGuid());
					SocketManager.GAME_SEND_Ee_PACKET(_perso, '+', DD.parse());
				break;
				case 'c'://Etable => Parcho(Echanger)
					Dragodinde DD1 = World.getDragoByID(guid);
					//S'il n'a pas la dinde
					if(DD1 == null || !MP.getData().containsKey(DD1.get_id()))return;
					if(MP.getData().get(DD1.get_id()) != _perso.get_GUID() && 
						World.getPersonnage(MP.getData().get(DD1.get_id())).get_guild() != _perso.get_guild())
					{
						//Pas la même guilde, pas le même perso
						return;
					}
					if(MP.getData().get(DD1.get_id()) != _perso.get_GUID() && 
							World.getPersonnage(MP.getData().get(DD1.get_id())).get_guild() == _perso.get_guild() &&
							!_perso.getGuildMember().canDo(Constants.G_OTHDINDE))
					{
						//Même guilde, pas le droit
						SocketManager.GAME_SEND_Im_PACKET(_perso, "1101");
						return;
					}
					//on retire la dinde de l'étable
					MP.removeData(DD1.get_id());
					SQLManager.UPDATE_MOUNTPARK(MP);
					//On créer le parcho
					ObjTemplate T = World.getMountScroll(DD1.get_color());
					Objet obj1 = T.createNewItem(1, false);
					//On efface les stats
					obj1.clearStats();
					//on ajoute la possibilité de voir la dinde
					obj1.getStats().addOneStat(995, DD1.get_id());
					obj1.addTxtStat(996, _perso.get_name());
					obj1.addTxtStat(997, DD1.get_nom());
					
					//On ajoute l'objet au joueur
					World.addObjet(obj1, true);
					_perso.addObjet(obj1, false);//Ne seras jamais identique de toute
					
					//Packets
					SocketManager.GAME_SEND_Ow_PACKET(_perso);
					SocketManager.GAME_SEND_Ee_PACKET(_perso,'-',DD1.get_id()+"");
				break;
				case 'g'://Equiper
					Dragodinde DD3 = World.getDragoByID(guid);
					//S'il n'a pas la dinde
					if(DD3 == null || !MP.getData().containsKey(DD3.get_id()) || _perso.getMount() != null)return;
					
					if(MP.getData().get(DD3.get_id()) != _perso.get_GUID() && 
							World.getPersonnage(MP.getData().get(DD3.get_id())).get_guild() != _perso.get_guild())
					{
						//Pas la même guilde, pas le même perso
						return;
					}
					if(MP.getData().get(DD3.get_id()) != _perso.get_GUID() && 
							World.getPersonnage(MP.getData().get(DD3.get_id())).get_guild() == _perso.get_guild() &&
							!_perso.getGuildMember().canDo(Constants.G_OTHDINDE))
					{
						//Même guilde, pas le droit
						SocketManager.GAME_SEND_Im_PACKET(_perso, "1101");
						return;
					}
					
					MP.removeData(DD3.get_id());
					SQLManager.UPDATE_MOUNTPARK(MP);
					_perso.setMount(DD3);
					
					//Packets
					SocketManager.GAME_SEND_Re_PACKET(_perso, "+", DD3);
					SocketManager.GAME_SEND_Ee_PACKET(_perso,'-',DD3.get_id()+"");
					SocketManager.GAME_SEND_Rx_PACKET(_perso);
				break;
				case 'p'://Equipé => Stocker
					//Si c'est la dinde équipé
					if(_perso.getMount()!=null?_perso.getMount().get_id() == guid:false)
					{
						//Si le perso est sur la monture on le fait descendre
						if(_perso.isOnMount())_perso.toogleOnMount();
						//Si ca n'a pas réussie, on s'arrete là (Items dans le sac ?)
						if(_perso.isOnMount())return;
						
						Dragodinde DD2 = _perso.getMount();
						MP.addData(DD2.get_id(), _perso.get_GUID());
						SQLManager.UPDATE_MOUNTPARK(MP);
						_perso.setMount(null);
						
						//Packets
						SocketManager.GAME_SEND_Ee_PACKET(_perso,'+',DD2.parse());
						SocketManager.GAME_SEND_Re_PACKET(_perso, "-", null);
						SocketManager.GAME_SEND_Rx_PACKET(_perso);
					}else//Sinon...
					{
						
					}
				break;
			}
		}
	}
	/*private void Exchange_mountPark(String packet) {
		//Si dans un enclos
		if (_perso.getInMountPark() != null) {
			char c = packet.charAt(2);
			packet = packet.substring(3);
			int guid = -1;
			try {
				guid = Integer.parseInt(packet);
			} catch (Exception e) {
			}
			;
			switch (c) {
				case 'C'://Parcho => Etable (Stocker)
					if (guid == -1 || !_perso.hasItemGuid(guid)) {
						return;
					}
					Objet obj = World.getObjet(guid);

					//on prend la DD demandï¿½e
					int DDid = obj.getStats().getEffect(995);
					Dragodinde DD = World.getDragoByID(DDid);
					//FIXME mettre return au if pour ne pas crï¿½er des nouvelles dindes
					if (DD == null) {
						int color = Constants.getMountColorByParchoTemplate(obj.getTemplate().getID());
						if (color < 1) {
							return;
						}
						DD = new Dragodinde(color);
					}

					//On enleve l'objet du Monde et du Perso
					_perso.removeItem(guid);
					World.removeItem(guid);
					//on ajoute la dinde a l'ï¿½table
					_compte.getStable().add(DD);

					//On envoie les packet
					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_perso, obj.getGuid());
					SocketManager.GAME_SEND_Ee_PACKET(_perso, '+', DD.parse());
					break;
				case 'c'://Etable => Parcho(Echanger)
					Dragodinde DD1 = World.getDragoByID(guid);
					//S'il n'a pas la dinde
					if (!_compte.getStable().contains(DD1) || DD1 == null) {
						return;
					}
					//on retire la dinde de l'ï¿½table
					_compte.getStable().remove(DD1);

					//On crï¿½er le parcho
					ObjTemplate T = World.getMountScroll(DD1.get_color());
					Objet obj1 = T.createNewItem(1, false);
					//On efface les stats
					obj1.clearStats();
					//on ajoute la possibilitï¿½ de voir la dinde
					obj1.getStats().addOneStat(995, DD1.get_id());
					obj1.addTxtStat(996, _perso.get_name());
					obj1.addTxtStat(997, DD1.get_nom());

					//On ajoute l'objet au joueur
					World.addObjet(obj1, true);
					_perso.addObjet(obj1, false);//Ne seras jamais identique de toute

					//Packets
					SocketManager.GAME_SEND_Ow_PACKET(_perso);
					SocketManager.GAME_SEND_Ee_PACKET(_perso, '-', DD1.get_id() + "");
					break;
				case 'g'://Equiper
					Dragodinde DD3 = World.getDragoByID(guid);
					//S'il n'a pas la dinde
					if (!_compte.getStable().contains(DD3) || DD3 == null || _perso.getMount() != null) {
						return;
					}

					_compte.getStable().remove(DD3);
					_perso.setMount(DD3);

					//Packets
					SocketManager.GAME_SEND_Re_PACKET(_perso, "+", DD3);
					SocketManager.GAME_SEND_Ee_PACKET(_perso, '-', DD3.get_id() + "");
					SocketManager.GAME_SEND_Rx_PACKET(_perso);
					break;
				case 'p'://Equipï¿½ => Stocker
					//Si c'est la dinde ï¿½quipï¿½
					if (_perso.getMount() != null ? _perso.getMount().get_id() == guid : false) {
						//Si le perso est sur la monture on le fait descendre
						if (_perso.isOnMount()) {
							_perso.toogleOnMount();
						}
						//Si ca n'a pas rï¿½ussie, on s'arrete lï¿½ (Items dans le sac ?)
						if (_perso.isOnMount()) {
							return;
						}

						Dragodinde DD2 = _perso.getMount();
						_compte.getStable().add(DD2);
						_perso.setMount(null);

						//Packets
						SocketManager.GAME_SEND_Ee_PACKET(_perso, '+', DD2.parse());
						SocketManager.GAME_SEND_Re_PACKET(_perso, "-", null);
						SocketManager.GAME_SEND_Rx_PACKET(_perso);
					} else//Sinon...
					{
						//TODO
					}
					break;
			}
		}
	}*/

	private void Exchange_doAgain()
	{
		if(_perso.getCurJobAction() != null)
			_perso.getCurJobAction().putLastCraftIngredients();
	}

	private void Exchange_isOK()
	{
		if(_perso.getCurJobAction() != null)
		{
			//Si pas action de craft, on s'arrete la
			if(!_perso.getCurJobAction().isCraft())return;
			_perso.getCurJobAction().startCraft(_perso);
		}
		if(_perso.get_curExchange() == null)return;
		_perso.get_curExchange().toogleOK(_perso.get_GUID());
	}

	private void Exchange_onMoveItem(String packet)
	{
		//Store
		if(_perso.get_isTradingWith() == _perso.get_GUID())
		{
			switch(packet.charAt(2))
			{
			case 'O'://Objets
				if(packet.charAt(3) == '+')
				{
					String[] infos = packet.substring(4).split("\\|");
					try
					{
						
						int guid = Integer.parseInt(infos[0]);
						int qua  = Integer.parseInt(infos[1]);
						int price  = Integer.parseInt(infos[2]);
						
						Objet obj = World.getObjet(guid);
						if(obj == null)return;
						
						if(qua > obj.getQuantity())
							qua = obj.getQuantity();
						
						_perso.addinStore(obj.getGuid(), price, qua);
						
					}catch(NumberFormatException e){};
				}else
				{
					String[] infos = packet.substring(4).split("\\|");
					try
					{
						int guid = Integer.parseInt(infos[0]);
						int qua  = Integer.parseInt(infos[1]);
						
						if(qua <= 0)return;
						
						Objet obj = World.getObjet(guid);
						if(obj == null)return;
						if(qua > obj.getQuantity())return;
						if(qua < obj.getQuantity()) qua = obj.getQuantity();
						
						_perso.removeFromStore(obj.getGuid(), qua);
					}catch(NumberFormatException e){};
				}
			break;
			}
			return;
		}
		//Percepteur
		if(_perso.get_isOnPercepteurID() != 0)
		{
			Percepteur perco = World.getPerco(_perso.get_isOnPercepteurID());
			if(perco == null || perco.get_inFight() > 0)return;
			switch(packet.charAt(2))
			{
			case 'G'://Kamas
				if(packet.charAt(3) == '-') //On retire
				{
					long P_Kamas = Integer.parseInt(packet.substring(4));
					long P_Retrait = perco.getKamas()-P_Kamas;
					if(P_Retrait < 0)
					{
						P_Retrait = 0;
						P_Kamas = perco.getKamas();
					}
					perco.setKamas(P_Retrait);
					_perso.addKamas(P_Kamas);
					SocketManager.GAME_SEND_STATS_PACKET(_perso);
					SocketManager.GAME_SEND_EsK_PACKET(_perso,"G"+perco.getKamas());
				}
			break;
			case 'O'://Objets
				if(packet.charAt(3) == '-') //On retire
				{
					String[] infos = packet.substring(4).split("\\|");
					int guid = 0;
					int qua = 0;
					try
					{
						guid = Integer.parseInt(infos[0]);
						qua  = Integer.parseInt(infos[1]);
					}catch(NumberFormatException e){};
					
					if(guid <= 0 || qua <= 0) return;
					
					Objet obj = World.getObjet(guid);
					if(obj == null)return;

					if(perco.HaveObjet(guid))
					{
						perco.removeFromPercepteur(_perso, guid, qua);
					}
					perco.LogObjetDrop(guid, obj);
				}
			break;
			}
			_perso.get_guild().addXp(perco.getXp());
			perco.LogXpDrop(perco.getXp());
			perco.setXp(0);
			SQLManager.UPDATE_GUILD(_perso.get_guild());
			return;
		}
		//HDV
		if(_perso.get_isTradingWith() < 0)
		{
			switch(packet.charAt(3))
			{
				case '-'://Retirer un objet de l'HDV
					int cheapestID = Integer.parseInt(packet.substring(4).split("\\|")[0]);
					int count = Integer.parseInt(packet.substring(4).split("\\|")[1]);
					if(count <= 0)return;
					
					_perso.get_compte().recoverItem(cheapestID,count);//Retire l'objet de la liste de vente du compte
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(_out,'-',"",cheapestID+"");
				break;
				case '+'://Mettre un objet en vente
					int itmID = Integer.parseInt(packet.substring(4).split("\\|")[0]);
					byte amount = Byte.parseByte(packet.substring(4).split("\\|")[1]);
					int price = Integer.parseInt(packet.substring(4).split("\\|")[2]);
					if(amount <= 0 || price <= 0)return;
					
					HDV curHdv = World.getHdv(Math.abs(_perso.get_isTradingWith()));
					int taxe = (int)(price * (curHdv.getTaxe()/100));
					
					
					if(!_perso.hasItemGuid(itmID))//Vérifie si le personnage a bien l'item spécifié et l'argent pour payer la taxe
						return;
					if(_perso.get_compte().countHdvItems(curHdv.getHdvID()) >= curHdv.getMaxItemCompte())
					{
						SocketManager.GAME_SEND_Im_PACKET(_perso, "058");
						return;
					}
					if(_perso.get_kamas() < taxe)
					{
						SocketManager.GAME_SEND_Im_PACKET(_perso, "176");
						return;
					}
					
					_perso.addKamas(taxe *-1);//Retire le montant de la taxe au personnage
					
					SocketManager.GAME_SEND_STATS_PACKET(_perso);//Met a jour les kamas du client
					
					Objet obj = World.getObjet(itmID);//Récupère l'item
					if(amount > obj.getQuantity())//S'il veut mettre plus de cette objet en vente que ce qu'il possède
						return;
					
					int rAmount = (int)(Math.pow(10,amount)/10);
					int newQua = (obj.getQuantity()-rAmount);
					
					if(newQua <= 0)//Si c'est plusieurs objets ensemble enleve seulement la quantité de mise en vente
					{
						_perso.removeItem(itmID);//Enlève l'item de l'inventaire du personnage
						SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_perso,itmID);//Envoie un packet au client pour retirer l'item de son inventaire
					}
					else
					{
						obj.setQuantity(obj.getQuantity() - rAmount);
						SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso,obj);
						
						Objet newObj = Objet.getCloneObjet(obj, rAmount);
						World.addObjet(newObj, true);
						obj = newObj;
					}
					
					HdvEntry toAdd = new HdvEntry(price,amount,_perso.get_compte().get_GUID(),obj);
					curHdv.addEntry(toAdd);	//Ajoute l'entry dans l'HDV
					
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(_out,'+',"",toAdd.parseToEmK());	//Envoie un packet pour ajouter l'item dans la fenetre de l'HDV du client
				break;
			}
			return;
		}
		else
		//Metier
		if(_perso.getCurJobAction() != null)
		{
			//Si pas action de craft, on s'arrete la
			if(!_perso.getCurJobAction().isCraft())return;
			if(packet.charAt(2) == 'O')//Ajout d'objet
			{
				if(packet.charAt(3) == '+')
				{
					//FIXME gerer les packets du genre  EMO+173|5+171|5+172|5 (split sur '+' ?:/)
					String[] infos = packet.substring(4).split("\\|");
					try
					{
						int guid = Integer.parseInt(infos[0]);
						int qua  = Integer.parseInt(infos[1]);
						if(qua <= 0)return;
						if(!_perso.hasItemGuid(guid))return;
						Objet obj = World.getObjet(guid);
						if(obj == null)return;
						if(obj.getQuantity()<qua)
							qua = obj.getQuantity();
							_perso.getCurJobAction().modifIngredient(_perso,guid,qua);
					}catch(NumberFormatException e){};
				}else
				{
					String[] infos = packet.substring(4).split("\\|");
					try
					{
						int guid = Integer.parseInt(infos[0]);
						int qua  = Integer.parseInt(infos[1]);
						if(qua <= 0)return;
						Objet obj = World.getObjet(guid);
						if(obj == null)return;
						_perso.getCurJobAction().modifIngredient(_perso,guid,-qua);
					}catch(NumberFormatException e){};
				}
				
			}else
			if(packet.charAt(2) == 'R')
			{
				try
				{
					int c = Integer.parseInt(packet.substring(3));
					_perso.getCurJobAction().repeat(c,_perso);
				}catch(Exception e){};
			}
			return;
		}
		//Banque
		if(_perso.isInBank())
		{
			if(_perso.get_curExchange() != null)return;
			switch(packet.charAt(2))
			{
				case 'G'://Kamas
					long kamas = 0;
					try
					{
							kamas = Integer.parseInt(packet.substring(3));
					}catch(Exception e){};
					if(kamas == 0)return;
					
					if(kamas > 0)//Si On ajoute des kamas a la banque
					{
						if(_perso.get_kamas() < kamas)kamas = _perso.get_kamas();
						_perso.setBankKamas(_perso.getBankKamas()+kamas);//On ajoute les kamas a la banque
						_perso.set_kamas(_perso.get_kamas()-kamas);//On retire les kamas du personnage
						SocketManager.GAME_SEND_STATS_PACKET(_perso);
						SocketManager.GAME_SEND_EsK_PACKET(_perso,"G"+_perso.getBankKamas());
					}else
					{
						kamas = -kamas;//On repasse en positif
						if(_perso.getBankKamas() < kamas)kamas = _perso.getBankKamas();
						_perso.setBankKamas(_perso.getBankKamas()-kamas);//On retire les kamas de la banque
						_perso.set_kamas(_perso.get_kamas()+kamas);//On ajoute les kamas du personnage
						SocketManager.GAME_SEND_STATS_PACKET(_perso);
						SocketManager.GAME_SEND_EsK_PACKET(_perso,"G"+_perso.getBankKamas());
					}
				break;
				
				case 'O'://Objet
					int guid = 0;
					int qua = 0;
					try
					{
						guid = Integer.parseInt(packet.substring(4).split("\\|")[0]);
						qua = Integer.parseInt(packet.substring(4).split("\\|")[1]);
					}catch(Exception e){};
					if(guid == 0 || qua <= 0)return;
					
					switch(packet.charAt(3))
					{
						case '+'://Ajouter a la banque
							_perso.addInBank(guid,qua);
						break;
						
						case '-'://Retirer de la banque
							_perso.removeFromBank(guid,qua);
						break;
					}
				break;
			}
			return;
		}
		//Dragodinde (inventaire)
			if (_perso.isInDinde()) {
			Dragodinde drago = _perso.getMount();
			if (drago == null) {
				return;
			}
			switch (packet.charAt(2)) {
				case 'O':// Objet
					int id = 0;
					int cant = 0;
					try {
						id = Integer.parseInt(packet.substring(4).split("\\|")[0]);
						cant = Integer.parseInt(packet.substring(4).split("\\|")[1]);
					} catch (Exception e) {}
					if (id == 0 || cant <= 0)
						return;
					if (World.getObjet(id) == null) {
						SocketManager.GAME_SEND_MESSAGE(_perso, "Erreur 1 d'inventaire de dragodinde : l'objet n'existe pas !", Ancestra.CONFIG_MOTD_COLOR);
						return;
					}
					switch (packet.charAt(3)) {
						case '+':
							drago.addInDinde(id, cant, _perso);
							break;
						case '-':
							drago.removeFromDinde(id, cant, _perso);
							break;
					}
					break;
			}
			return;
		}
		//Coffre
	    if(_perso.getInTrunk() != null)
        {
                if(_perso.get_curExchange() != null)return;
                Trunk t = _perso.getInTrunk();
                if(t == null) return;
               
                switch(packet.charAt(2))
                {
                	case 'G'://Kamas
                    	long kamas = 0;
                    	try
                    	{
                    		kamas = Integer.parseInt(packet.substring(3));
                        }catch(Exception e){};
                        if(kamas == 0)return;
                               
                        if(kamas > 0)//Si On ajoute des kamas au coffre
                        {
                            if(_perso.get_kamas() < kamas)kamas = _perso.get_kamas();
                            t.set_kamas(t.get_kamas() + kamas);//On ajoute les kamas au coffre
                            _perso.set_kamas(_perso.get_kamas()-kamas);//On retire les kamas du personnage
                            SocketManager.GAME_SEND_STATS_PACKET(_perso);
                        }else // On retire des kamas au coffre
                        {
                        	kamas = -kamas;//On repasse en positif
                        	if(t.get_kamas() < kamas)kamas = t.get_kamas();
                        	t.set_kamas(t.get_kamas()-kamas);//On retire les kamas de la banque
                         	_perso.set_kamas(_perso.get_kamas()+kamas);//On ajoute les kamas du personnage
                         	SocketManager.GAME_SEND_STATS_PACKET(_perso);
                        }
                        for(Personnage P : World.getOnlinePersos())
                        {
                        	if(P.getInTrunk() != null && _perso.getInTrunk().get_id() == P.getInTrunk().get_id())
                            {
                        		SocketManager.GAME_SEND_EsK_PACKET(P,"G"+t.get_kamas());
                         	}
                        }
                        SQLManager.UPDATE_TRUNK(t);
                    break;
              	
                	case 'O'://Objet
                		int guid = 0;
                		int qua = 0;
                		try
                		{
                			guid = Integer.parseInt(packet.substring(4).split("\\|")[0]);
                			qua = Integer.parseInt(packet.substring(4).split("\\|")[1]);
                		}catch(Exception e){};
                		if(guid == 0 || qua <= 0)return;
                               
                		switch(packet.charAt(3))
                		{
                			case '+'://Ajouter a la banque
                				t.addInTrunk(guid, qua, _perso);
                			break;
                                       
                			case '-'://Retirer de la banque
                				t.removeFromTrunk(guid,qua, _perso);
                			break;
                		}
                	break;
                }
                return;
        }
		if(_perso.get_curExchange() == null)return;
		switch(packet.charAt(2))
		{
			case 'O'://Objet ?
				if(packet.charAt(3) == '+')
				{
					String[] infos = packet.substring(4).split("\\|");
					try
					{
						
						int guid = Integer.parseInt(infos[0]);
						int qua  = Integer.parseInt(infos[1]);
						int quaInExch = _perso.get_curExchange().getQuaItem(guid, _perso.get_GUID());
						
						if(!_perso.hasItemGuid(guid))return;
						Objet obj = World.getObjet(guid);
						if(obj == null)return;
						
						if(qua > obj.getQuantity()-quaInExch)

							qua = obj.getQuantity()-quaInExch;
						if(qua <= 0)return;
						
						_perso.get_curExchange().addItem(guid,qua,_perso.get_GUID());
					}catch(NumberFormatException e){};
				}else
				{
					String[] infos = packet.substring(4).split("\\|");
					try
					{
						int guid = Integer.parseInt(infos[0]);
						int qua  = Integer.parseInt(infos[1]);
						
						if(qua <= 0)return;
						if(!_perso.hasItemGuid(guid))return;
						
						Objet obj = World.getObjet(guid);
						if(obj == null)return;
						if(qua > _perso.get_curExchange().getQuaItem(guid, _perso.get_GUID()))return;
						
						_perso.get_curExchange().removeItem(guid,qua,_perso.get_GUID());
					}catch(NumberFormatException e){};
				}
			break;
			case 'G'://Kamas
				try
				{
					long numb = Integer.parseInt(packet.substring(3));
					if(_perso.get_kamas() < numb)
						numb = _perso.get_kamas();
					_perso.get_curExchange().setKamas(_perso.get_GUID(), numb);
				}catch(NumberFormatException e){};
			break;
		}
	}

	private void Exchange_accept()
	{
		if(_perso.get_isTradingWith() == 0)return;
		Personnage target = World.getPersonnage(_perso.get_isTradingWith());
		if(target == null)return;
		SocketManager.GAME_SEND_EXCHANGE_CONFIRM_OK(_out,1);
		SocketManager.GAME_SEND_EXCHANGE_CONFIRM_OK(target.get_compte().getGameThread().get_out(),1);
		World.Exchange echg = new World.Exchange(target,_perso);
		_perso.setCurExchange(echg);
		_perso.set_isTradingWith(target.get_GUID());
		target.setCurExchange(echg);
		target.set_isTradingWith(_perso.get_GUID());
	}

	private void Exchange_onSellItem(String packet)
	{
		try
		{
			String[] infos = packet.substring(2).split("\\|");
			int guid = Integer.parseInt(infos[0]);
			int qua = Integer.parseInt(infos[1]);
			if(!_perso.hasItemGuid(guid))
			{
				SocketManager.GAME_SEND_SELL_ERROR_PACKET(_out);
				return;
			}
			_perso.sellItem(guid, qua);
		}catch(Exception e)
		{
			SocketManager.GAME_SEND_SELL_ERROR_PACKET(_out);
		}
	}

	private void Exchange_onBuyItem(String packet)
	{
		String[] infos = packet.substring(2).split("\\|");
		
        if (_perso.get_isTradingWith() > 0) 
        {
            Personnage seller = World.getPersonnage(_perso.get_isTradingWith());
            if (seller != null) 
            {
            	int itemID = 0;
            	int qua = 0;
            	int price = 0;
            	try
        		{
            		itemID = Integer.valueOf(infos[0]);
            		qua = Integer.valueOf(infos[1]);
        		}catch(Exception e){return;}
        		
                if (!seller.getStoreItems().containsKey(itemID) || qua <= 0) 
                {
                    SocketManager.GAME_SEND_BUY_ERROR_PACKET(_out);
                    return;
                }
                price = seller.getStoreItems().get(itemID);
                Objet itemStore = World.getObjet(itemID);
                if(itemStore == null) return;
                
                if(qua > itemStore.getQuantity()) qua = itemStore.getQuantity();
                if(qua == itemStore.getQuantity())
                {
                	seller.getStoreItems().remove(itemStore.getGuid());
                	_perso.addObjet(itemStore, true);
                }else // si l'échange peut se faire
                {
                	seller.getStoreItems().remove(itemStore.getGuid()); // on enlève entièrement l'objet en vente
                	itemStore.setQuantity(itemStore.getQuantity()-qua); // on modifie la quantité dans le magasin
                	SQLManager.SAVE_ITEM(itemStore);					// on sauvegarde le magasin
                	seller.addStoreItem(itemStore.getGuid(), price);	// on remet dans le magasin
                	
                	Objet clone = Objet.getCloneObjet(itemStore, qua);	// on clone l'objet acheté
                    SQLManager.SAVE_NEW_ITEM(clone);					// on sauvegarde celui-ci
                    _perso.addObjet(clone, true);						// et on le donne au joueur
                }
	            //remove kamas
	            _perso.addKamas(-price * qua);
	            //add seller kamas
	            seller.addKamas(price * qua);
	            SQLManager.SAVE_PERSONNAGE(seller, true);
	            SQLManager.SAVE_PERSONNAGE(this._perso, true);
	            //send packets
	            SocketManager.GAME_SEND_STATS_PACKET(_perso);
	            SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(seller, _perso);
	            SocketManager.GAME_SEND_BUY_OK_PACKET(_out);
	            if(seller.getStoreItems().isEmpty())
	            {
	            	if(World.getSeller(seller.get_curCarte().get_id()) != null && World.getSeller(seller.get_curCarte().get_id()).contains(seller.get_GUID()))
	        		{
	        			World.removeSeller(seller.get_GUID(), seller.get_curCarte().get_id());
	        			SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(seller.get_curCarte(), seller.get_GUID());
	        			Exchange_finish_buy();
	        		}
	            }
            }
            return;
        }
        
		try
		{
			int tempID = Integer.parseInt(infos[0]);
			int qua = Integer.parseInt(infos[1]);
			
			if(qua <= 0) return;
			
			ObjTemplate template = World.getObjTemplate(tempID);
			if(template == null)//Si l'objet demandé n'existe pas(ne devrait pas arrivé)
			{
				GameServer.addToLog(_perso.get_name()+" tente d'acheter l'itemTemplate "+tempID+" qui est inexistant");
				SocketManager.GAME_SEND_BUY_ERROR_PACKET(_out);
				return;
			}
			if(!_perso.get_curCarte().getNPC(_perso.get_isTradingWith()).get_template().haveItem(tempID))//Si le PNJ ne vend pas l'objet voulue
			{
				GameServer.addToLog(_perso.get_name()+" tente d'acheter l'itemTemplate "+tempID+" que le present PNJ ne vend pas");
				SocketManager.GAME_SEND_BUY_ERROR_PACKET(_out);
				return;
			}
			int prix = template.getPrix() * qua;
			if(_perso.get_kamas()<prix)//Si le joueur n'a pas assez de kamas
			{
				GameServer.addToLog(_perso.get_name()+" tente d'acheter l'itemTemplate "+tempID+" mais n'a pas l'argent necessaire");
				SocketManager.GAME_SEND_BUY_ERROR_PACKET(_out);
				return;
			}
			Objet newObj = template.createNewItem(qua,false);
			long newKamas = _perso.get_kamas() - prix;
			_perso.set_kamas(newKamas);
			if(_perso.addObjet(newObj,true))//Return TRUE si c'est un nouvel item
				World.addObjet(newObj,true);
			SocketManager.GAME_SEND_BUY_OK_PACKET(_out);
			SocketManager.GAME_SEND_STATS_PACKET(_perso);
			SocketManager.GAME_SEND_Ow_PACKET(_perso);
		}catch(Exception e)
		{
			e.printStackTrace();
			SocketManager.GAME_SEND_BUY_ERROR_PACKET(_out);
			return;
		};
	}

	private void Exchange_finish_buy()
	{
		if(_perso.get_isTradingWith() == 0 &&
		   _perso.get_curExchange() == null &&
		   _perso.getCurJobAction() == null &&
		   _perso.getInMountPark() == null &&
		   !_perso.isInBank() &&
		   _perso.get_isOnPercepteurID() == 0 &&
		   _perso.getInTrunk() == null)
			return;
		
		//Si échange avec un personnage
		if(_perso.get_curExchange() != null)
		{
			_perso.get_curExchange().cancel();
			_perso.set_isTradingWith(0);
			_perso.set_away(false);
			return;
		}
	
		//Si métier
		if(_perso.getCurJobAction() != null)
		{
			_perso.getCurJobAction().resetCraft();
		}
		//Si dans un enclos
		if(_perso.getInMountPark() != null)_perso.leftMountPark();
		//prop d'echange avec un joueur
		if(_perso.get_isTradingWith() > 0)
		{
			Personnage p = World.getPersonnage(_perso.get_isTradingWith());
			if(p != null)
			{
				if(p.isOnline())
				{
					PrintWriter out = p.get_compte().getGameThread().get_out();
					SocketManager.GAME_SEND_EV_PACKET(out);
					p.set_isTradingWith(0);
				}
			}
		}
		//Si perco
		if(_perso.get_isOnPercepteurID() != 0)
		{
			Percepteur perco = World.getPerco(_perso.get_isOnPercepteurID());
			if(perco == null) return;
			for(Personnage z : World.getGuild(perco.get_guildID()).getMembers())
			{
				if(z.isOnline())
				{
					SocketManager.GAME_SEND_gITM_PACKET(z, Percepteur.parsetoGuild(z.get_guild().get_id()));
					String str = "";
					str += "G"+perco.get_N1()+","+perco.get_N2();
					str += "|.|"+World.getCarte((short)perco.get_mapID()).getX()+"|"+World.getCarte((short)perco.get_mapID()).getY()+"|";
					str += _perso.get_name()+"|";
					str += perco.get_LogXp();
					str += perco.get_LogItems();
					SocketManager.GAME_SEND_gT_PACKET(z, str);
				}
			}
			_perso.get_curCarte().RemoveNPC(perco.getGuid());
			SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(_perso.get_curCarte(), perco.getGuid());
			perco.DelPerco(perco.getGuid());
			SQLManager.DELETE_PERCO(perco.getGuid());
			_perso.set_isOnPercepteurID(0);
		}
		
		SQLManager.SAVE_PERSONNAGE(_perso,true);
		SocketManager.GAME_SEND_EV_PACKET(_out);
		_perso.set_isTradingWith(0);
		_perso.set_away(false);
		_perso.setInBank(false);
		_perso.setInTrunk(null);
	}

	private void Exchange_start(String packet)
	{
		if(packet.substring(2,4).equals("11"))//Ouverture HDV achat
		{
			if(_perso.get_isTradingWith() < 0)//Si déjà ouvert
				SocketManager.GAME_SEND_EV_PACKET(_out);
			
			if(_perso.getDeshonor() >= 5) 
			{
				SocketManager.GAME_SEND_Im_PACKET(_perso, "183");
				return;
			}
			
			HDV toOpen = World.getHdv(_perso.get_curCarte().get_id());
			
			if(toOpen == null) return;
			
			String info = "1,10,100;"+
						toOpen.getStrCategories()+
						";"+toOpen.parseTaxe()+
						";"+toOpen.getLvlMax()+
						";"+toOpen.getMaxItemCompte()+
						";-1;"+
						toOpen.getSellTime();
			SocketManager.GAME_SEND_ECK_PACKET(_perso,11,info);
			_perso.set_isTradingWith(0 - _perso.get_curCarte().get_id());	//Récupère l'ID de la map et rend cette valeur négative
			return;
		}
		else if(packet.substring(2,4).equals("10"))//Ouverture HDV vente
		{
			if(_perso.get_isTradingWith() < 0)//Si déjà ouvert
				SocketManager.GAME_SEND_EV_PACKET(_out);
			
			if(_perso.getDeshonor() >= 5) 
			{
				SocketManager.GAME_SEND_Im_PACKET(_perso, "183");
				return;
			}
			
			HDV toOpen = World.getHdv(_perso.get_curCarte().get_id());
			
			if(toOpen == null) return;
			
			String info = "1,10,100;"+
						toOpen.getStrCategories()+
						";"+toOpen.parseTaxe()+
						";"+toOpen.getLvlMax()+
						";"+toOpen.getMaxItemCompte()+
						";-1;"+
						toOpen.getSellTime();
			SocketManager.GAME_SEND_ECK_PACKET(_perso,10,info);
			_perso.set_isTradingWith(0 - _perso.get_curCarte().get_id());	//Récupère l'ID de la map et rend cette valeur négative
			
			SocketManager.GAME_SEND_HDVITEM_SELLING(_perso);
			return;
		} else if (packet.substring(2, 4).equals("15")) {//Dinde (inventaire)
			try {
				Dragodinde monture = _perso.getMount();
				int idMontura = monture.get_id();
				SocketManager.GAME_SEND_ECK_PACKET(_out, 15, _perso.getMount().get_id() + "");
				SocketManager.GAME_SEND_EL_MOUNT_PACKET(_out, monture);
				SocketManager.GAME_SEND_Ew_PACKET(_perso, monture.get_podsActuels(), monture.getMaxPod());
				_perso.set_isTradingWith(idMontura);
				_perso.setInDinde(true);
				_perso.set_away(true);
			} catch (Exception e) {}
			return;
		}
		switch(packet.charAt(2))
		{
			case '0'://Si NPC
				try
				{
					int npcID = Integer.parseInt(packet.substring(4));
					NPC_tmpl.NPC npc = _perso.get_curCarte().getNPC(npcID);
					if(npc == null)return;
					SocketManager.GAME_SEND_ECK_PACKET(_out, 0, npcID+"");
					SocketManager.GAME_SEND_ITEM_VENDOR_LIST_PACKET(_out,npc);
					_perso.set_isTradingWith(npcID);
				}catch(NumberFormatException e){};
			break;
			case '1'://Si joueur
				try
				{
				int guidTarget = Integer.parseInt(packet.substring(4));
				Personnage target = World.getPersonnage(guidTarget);
				if(target == null )
				{
					SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(_out,'E');
					return;
				}
				if(target.get_curCarte()!= _perso.get_curCarte() || !target.isOnline())//Si les persos ne sont pas sur la meme map
				{
					SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(_out,'E');
					return;
				}
				if(target.is_away() || _perso.is_away() || target.get_isTradingWith() != 0)
				{
					SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(_out,'O');
					return;
				}
				SocketManager.GAME_SEND_EXCHANGE_REQUEST_OK(_out, _perso.get_GUID(), guidTarget,1);
				SocketManager.GAME_SEND_EXCHANGE_REQUEST_OK(target.get_compte().getGameThread().get_out(),_perso.get_GUID(), guidTarget,1);
				_perso.set_isTradingWith(guidTarget);
				target.set_isTradingWith(_perso.get_GUID());
			}catch(NumberFormatException e){}
			break;
            case '4'://StorePlayer
            	int pID = 0;
            	//int cellID = 0;//Inutile
            	try
				{
            		pID = Integer.valueOf(packet.split("\\|")[1]);
            		//cellID = Integer.valueOf(packet.split("\\|")[2]);
				}catch(NumberFormatException e){return;};
				if(_perso.get_isTradingWith() > 0 || _perso.get_fight() != null || _perso.is_away())return;
				Personnage seller = World.getPersonnage(pID);
				if(seller == null) return;
				_perso.set_isTradingWith(pID);
				SocketManager.GAME_SEND_ECK_PACKET(_perso, 4, seller.get_GUID()+"");
				SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(seller, _perso);
            break;
			case '6'://StoreItems
				if(_perso.get_isTradingWith() > 0 || _perso.get_fight() != null || _perso.is_away())return;
                _perso.set_isTradingWith(_perso.get_GUID());
                SocketManager.GAME_SEND_ECK_PACKET(_perso, 6, "");
                SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(_perso, _perso);
			break;
			case '8'://Si Percepteur
				try
				{
					int PercepteurID = Integer.parseInt(packet.substring(4));
					Percepteur perco = World.getPerco(PercepteurID);
					if(perco == null || perco.get_inFight() > 0 || perco.get_Exchange())return;
					perco.set_Exchange(true);
					SocketManager.GAME_SEND_ECK_PACKET(_out, 8, perco.getGuid()+"");
					SocketManager.GAME_SEND_ITEM_LIST_PACKET_PERCEPTEUR(_out, perco);
					_perso.set_isTradingWith(perco.getGuid());
					_perso.set_isOnPercepteurID(perco.getGuid());
				}catch(NumberFormatException e){};
			break;
		}
	}

	private void parse_environementPacket(String packet)
	{
		switch(packet.charAt(1))
		{
			case 'D'://Change direction
				Environement_change_direction(packet);
			break;
			
			case 'U'://Emote
				Environement_emote(packet);
			break;
		}
	}

	private void Environement_emote(String packet)
	{
		int emote = -1;
		try
		{
			emote = Integer.parseInt(packet.substring(2));
		}catch(Exception e){};
		if(emote == -1)return;
		if(_perso == null)return;
		if(_perso.get_fight() != null)return;//Pas d'émote en combat
		
		switch(emote)//effets spéciaux des émotes
		{
			case 19://s'allonger 
			case 1:// s'asseoir
				_perso.setSitted(!_perso.isSitted());
			break;
		}
		if(_perso.emoteActive() == emote)_perso.setEmoteActive(0);
		else _perso.setEmoteActive(emote);
		
		Ancestra.printDebug("Set Emote "+_perso.emoteActive());
		Ancestra.printDebug("Is sitted "+_perso.isSitted());
		
		SocketManager.GAME_SEND_eUK_PACKET_TO_MAP(_perso.get_curCarte(), _perso.get_GUID(), _perso.emoteActive());
	}

	private void Environement_change_direction(String packet)
	{
		try
		{
			if(_perso.get_fight() != null)return;
			int dir = Integer.parseInt(packet.substring(2));
			_perso.set_orientation(dir);
			SocketManager.GAME_SEND_eD_PACKET_TO_MAP(_perso.get_curCarte(),_perso.get_GUID(),dir);
		}catch(NumberFormatException e){return;};
	}

	private void parseSpellPacket(String packet)
	{
		switch(packet.charAt(1))
		{
			case 'B':
				boostSort(packet);
			break;
			case 'F'://Oublie de sort
				forgetSpell(packet);
			break;
			case'M':
				addToSpellBook(packet);
			break;
		}
	}

	private void addToSpellBook(String packet)
	{
		try
		{
			int SpellID = Integer.parseInt(packet.substring(2).split("\\|")[0]);
			int Position = Integer.parseInt(packet.substring(2).split("\\|")[1]);
			SortStats Spell = _perso.getSortStatBySortIfHas(SpellID);
			
			if(Spell != null)
			{
				_perso.set_SpellPlace(SpellID, CryptManager.getHashedValueByInt(Position));
			}
				
			SocketManager.GAME_SEND_BN(_out);
		}catch(Exception e){};
	}

	private void boostSort(String packet)
	{
		try
		{
			int id = Integer.parseInt(packet.substring(2));
			GameServer.addToLog("Info: "+_perso.get_name()+": Tente BOOST sort id="+id);
			if(_perso.boostSpell(id))
			{
				GameServer.addToLog("Info: "+_perso.get_name()+": OK pour BOOST sort id="+id);
				SocketManager.GAME_SEND_SPELL_UPGRADE_SUCCED(_out, id, _perso.getSortStatBySortIfHas(id).getLevel());
				SocketManager.GAME_SEND_STATS_PACKET(_perso);
			}else
			{
				GameServer.addToLog("Info: "+_perso.get_name()+": Echec BOOST sort id="+id);
				SocketManager.GAME_SEND_SPELL_UPGRADE_FAILED(_out);
				return;
			}
		}catch(NumberFormatException e){SocketManager.GAME_SEND_SPELL_UPGRADE_FAILED(_out);return;};
	}

	private void forgetSpell(String packet)
	{
		if(!_perso.isForgetingSpell())return;
		
		int id = Integer.parseInt(packet.substring(2));
		
		if(Ancestra.CONFIG_DEBUG) GameServer.addToLog("Info: "+_perso.get_name()+": Tente Oublie sort id="+id);
		
		if(_perso.forgetSpell(id))
		{
			if(Ancestra.CONFIG_DEBUG) GameServer.addToLog("Info: "+_perso.get_name()+": OK pour Oublie sort id="+id);
			SocketManager.GAME_SEND_SPELL_UPGRADE_SUCCED(_out, id, _perso.getSortStatBySortIfHas(id).getLevel());
			SocketManager.GAME_SEND_STATS_PACKET(_perso);
			_perso.setisForgetingSpell(false);
		}
	}

	private void parseFightPacket(String packet)
	{
		try
		{
			switch(packet.charAt(1))
			{
				case 'D'://Détails d'un combat (liste des combats)
					int key = -1;
					try
					{
						key = Integer.parseInt(packet.substring(2).replace(((int)0x0)+"", ""));
					}catch(Exception e){};
					if(key == -1)return;
					SocketManager.GAME_SEND_FIGHT_DETAILS(_out,_perso.get_curCarte().get_fights().get(key));
				break;
				
				case 'H'://Aide
					if(_perso.get_fight() == null)return;
					_perso.get_fight().toggleHelp(_perso.get_GUID());
				break;
				
				case 'L'://Lister les combats
					SocketManager.GAME_SEND_FIGHT_LIST_PACKET(_out, _perso.get_curCarte());
				break;
				case 'N'://Bloquer le combat
					if(_perso.get_fight() == null)return;
					_perso.get_fight().toggleLockTeam(_perso.get_GUID());
				break;
				case 'P'://Seulement le groupe
					if(_perso.get_fight() == null || _perso.getGroup() == null)return;
					_perso.get_fight().toggleOnlyGroup(_perso.get_GUID());
				break;
				case 'S'://Bloquer les specs
					if(_perso.get_fight() == null)return;
					_perso.get_fight().toggleLockSpec(_perso.get_GUID());
				break;
				
			}
		}catch(Exception e){e.printStackTrace();};
	}

	private void parseBasicsPacket(String packet)
	{
		switch(packet.charAt(1))
		{
			case 'A'://Console
				Basic_console(packet);
			break;
			case 'D':
				Basic_send_Date_Hour();
			break;
			case 'M':
				Basic_chatMessage(packet);
			break;
			case 'W':
				Basic_infosmessage(packet);
			break;
			case 'S':
				_perso.emoticone(packet.substring(2));
			break;
			case 'Y':
				Basic_state(packet);
			break;
		}
	}
	public void Basic_state(String packet)
	{
		switch(packet.charAt(2))
		{
			case 'A': //Absent
				if(_perso._isAbsent)
				{

					SocketManager.GAME_SEND_Im_PACKET(_perso, "038");

					_perso._isAbsent = false;
				}
				else

				{
					SocketManager.GAME_SEND_Im_PACKET(_perso, "037");
					_perso._isAbsent = true;
				}
			break;
			case 'I': //Invisible
				if(_perso._isInvisible)
				{
					SocketManager.GAME_SEND_Im_PACKET(_perso, "051");
					_perso._isInvisible = false;
				}
				else
				{
					SocketManager.GAME_SEND_Im_PACKET(_perso, "050");
					_perso._isInvisible = true;
				}
			break;
		}
	}
	
	public Personnage getPerso()
	{
		return _perso;
	}
	  
	private void Basic_console(String packet)
	{
		if(command == null) command = new Commands(_perso);
		command.consoleCommand(packet);
	}

	public void closeSocket()
	{
		try {
			this._s.close();
		} catch (IOException e) {}
	}

	private void Basic_chatMessage(String packet)
	{
		String msg = "";
		if(_perso.isMuted())
		{
			SocketManager.GAME_SEND_Im_PACKET(_perso, "1124;"+_perso.get_compte()._muteTimer.getInitialDelay());//FIXME
			return;
		}
		packet = packet.replace("<", "");
		packet = packet.replace(">", "");
		if(packet.length() == 3)return;
		switch(packet.charAt(2))
		{
			case '*'://Canal noir
				if(!_perso.get_canaux().contains(packet.charAt(2)+""))return;
				msg = packet.split("\\|",2)[1];
				
				//On annonce la pré-commande
				if(msg.charAt(0) == '.')
				{
					//Début des commandes d'informations |By Return Alias Porky|
					//
					//
					//
					//
					//
					//
					//
					//
					//
					//
					//
					//
					//
					//
					//
					//
					
					
					//TODO: Commande d'affichage des commandes
					
					if(msg.length() > 7 && msg.substring(1, 8).equalsIgnoreCase("command")) // Si le joueur écrit .command
					{
						if (Ancestra.CONFIG_ACTIV_FUNCOMMANDS == false && Ancestra.CONFIG_ACTIV_COMANDESEMIFUN == false && Ancestra.CONFIG_ACTIV_ENERGIE == false)
							
						
					    
					{
						
						SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Commandes disponibles:</u>\n" + //Le socket affiche les informations ci-contre
					     ".<b>infos</b> - Permet d'avoir des informations sur le serveur\n" +
					     ".<b>save</b> - Permet de sauvegarder son personnage\n" +
					     ".<b>start</b> - Permet de se téléporter à la zone de départ\n" +
					     ".<b>staff</b> - Permet de visualiser le staff en ligne\n" +
					     ".<b>guilde</b> - Permet d'afficher le panel de création de guildes\n" +
					     ".<b>mj</b> - Permet de rejoindre la salle des MJ\n" +
					     ".<b>ticket</b> - Permet contacter un modérateur\n" +
					     ".<b>phoenix</b> - Permet de se téléporter au phoenix\n" +
					     ".<b>prisme</b> - Permet d'avoir des prismes dans son sac\n" +
					     ".<b>Spellmax</b> - Permet d'avoir tout ces sorts niveaux 6\n" +
					     ".<b>Boss</b> - Permet de se téléporter à la zone du boss Asterion\n" +
						 ".<b>vipcommand</b> - Permet de visualiser commandes V.I.P", Ancestra.COLOR_VERT); 
						return; 
						
					    }
						//ALLER, LOUYA !!
						else if (Ancestra.CONFIG_ACTIV_FUNCOMMANDS == true && Ancestra.CONFIG_ACTIV_COMANDESEMIFUN == false && Ancestra.CONFIG_ACTIV_ENERGIE == false)
						{
							SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Commandes disponibles:</u>\n" + //Le socket affiche les informations ci-contre
								     ".<b>infos</b> - Permet d'avoir des informations sur le serveur\n" +
								     ".<b>save</b> - Permet de sauvegarder son personnage\n" +
								     ".<b>start</b> - Permet de se téléporter à la zone de départ\n" +
								     ".<b>staff</b> - Permet de visualiser le staff en ligne\n" +
								     ".<b>guilde</b> - Permet d'afficher le panel de création de guildes\n" +
								     ".<b>mj</b> - Permet de rejoindre la salle des MJ\n" +
								     ".<b>shop</b> - Permet de se téléporter à la zone marchande \n" +
								     ".<b>pvp</b> - Permet de se téléporter à la zone pvp\n" +
								     ".<b>pvm</b> - Permet de se téléporter à la zone pvm\n" +
								     ".<b>enclos</b> - Permet de se téléporter à l'enclos\n" +
								     ".<b>event</b> - Permet de se téléporter à la map event\n" +
								     ".<b>fmcac</b> - Permet de (Fm) son arme\n" +
								     ".<b>parcho</b> - Permet d'auguementer ses caractéristiques\n" +
								     ".<b>vie</b> - Permet de regénérer ses points de vie au maximum\n" +
								     ".<b>ticket</b> - Permet contacter un modérateur\n" +
								     ".<b>phoenix</b> - Permet de se téléporter au phoenix\n" +
								     ".<b>prisme</b> - Permet d'avoir des prismes dans son sac\n" +
								     ".<b>Spellmax</b> - Permet d'avoir tout ces sorts niveaux 6\n" +
								     ".<b>Boss</b> - Permet de se téléporter à la zone du boss Asterion\n" +
									 ".<b>vipcommand</b> - Permet de visualiser les commandes V.I.P", Ancestra.COLOR_VERT); 
									
									 
							return; 
						}
						else if (Ancestra.CONFIG_ACTIV_FUNCOMMANDS == false && Ancestra.CONFIG_ACTIV_COMANDESEMIFUN == false && Ancestra.CONFIG_ACTIV_ENERGIE == true)
							{
								SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Commandes disponibles:</u>\n" + //Le socket affiche les informations ci-contre
										 ".<b>infos</b> - Permet d'avoir des informations sur le serveur\n" +
									     ".<b>save</b> - Permet de sauvegarder son personnage\n" +
									     ".<b>start</b> - Permet de se téléporter à la zone de départ\n" +
									     ".<b>staff</b> - Permet de visualiser le staff en ligne\n" +
									     ".<b>guilde</b> - Permet d'afficher le panel de création de guildes\n" +
									     ".<b>mj</b> - Permet de rejoindre la salle des MJ\n" +
									     ".<b>energie</b> - Permet d'afficher les M.A.J de l'émulateur\n" +
									     ".<b>ticket</b> - Permet contacter un modérateur\n" +
									     ".<b>phoenix</b> - Permet de se téléporter au phoenix\n" +
									     ".<b>prisme</b> - Permet d'avoir des prismes dans son sac\n" +
									     ".<b>Spellmax</b> - Permet d'avoir tout ces sorts niveaux 6\n" +
									     ".<b>Boss</b> - Permet de se téléporter à la zone du boss Asterion\n" +
										 ".<b>vipcommand</b> - Permet de visualiser les commandes V.I.P", Ancestra.COLOR_VERT); 
										
										 
								return; 
							}
						else if (Ancestra.CONFIG_ACTIV_FUNCOMMANDS == false && Ancestra.CONFIG_ACTIV_COMANDESEMIFUN == true && Ancestra.CONFIG_ACTIV_ENERGIE == false)
						{
							SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Commandes disponibles:</u>\n" + //Le socket affiche les informations ci-contre
									 ".<b>infos</b> - Permet d'avoir des informations sur le serveur\n" +
								     ".<b>save</b> - Permet de sauvegarder son personnage\n" +
								     ".<b>start</b> - Permet de se téléporter à la zone de départ\n" +
								     ".<b>staff</b> - Permet de visualiser le staff en ligne\n" +
								     ".<b>guilde</b> - Permet d'afficher le panel de création de guildes\n" +
								     ".<b>mj</b> - Permet de rejoindre la salle des MJ\n" +
								     ".<b>bontarien</b> - Permet de devenir bontarien\n" +
								     ".<b>brakmarien</b> - Permet de devenir brakmarien\n" +
								     ".<b>ticket</b> - Permet contacter un modérateur\n" +
								     ".<b>phoenix</b> - Permet de se téléporter au phoenix\n" +
								     ".<b>serianne</b> - Permet de devenir serianne\n" +
								     ".<b>prisme</b> - Permet d'avoir des prismes dans son sac\n" +
								     ".<b>Spellmax</b> - Permet d'avoir tout ces sorts niveaux 6\n" +
								     ".<b>Boss</b> - Permet de se téléporter à la zone du boss Asterion\n" +
									 ".<b>vipcommand</b> - Permet de visualiser les commandes V.I.P", Ancestra.COLOR_VERT); 
									
									 
							return; 
						}
						else if (Ancestra.CONFIG_ACTIV_FUNCOMMANDS == true && Ancestra.CONFIG_ACTIV_COMANDESEMIFUN == true && Ancestra.CONFIG_ACTIV_ENERGIE == true)
							{
									SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Commandes disponibles:</u>\n" + //Le socket affiche les informations ci-contre
										     ".<b>infos</b> - Permet d'avoir des informations sur le serveur\n" +
										     ".<b>save</b> - Permet de sauvegarder son personnage\n" +
										     ".<b>start</b> - Permet de se téléporter à la zone de départ\n" +
										     ".<b>staff</b> - Permet de visualiser le staff en ligne\n" +
										     ".<b>guilde</b> - Permet d'afficher le panel de création de guildes\n" +
										     ".<b>mj</b> - Permet de rejoindre la salle des MJ\n" +
										     ".<b>shop</b> - Permet de se téléporter à la zone marchande \n" +
										     ".<b>pvp</b> - Permet de se téléporter à la zone pvp\n" +
										     ".<b>pvm</b> - Permet de se téléporter à la zone pvm\n" +
										     ".<b>enclos</b> - Permet de se téléporter à l'enclos\n" +
										     ".<b>event</b> - Permet de se téléporter à la map event\n" +
										     ".<b>fmcac</b> - Permet de (Fm) son arme\n" +
										     ".<b>parcho</b> - Permet d'auguementer ses caractéristiques\n" +
										     ".<b>vie</b> - Permet de regénérer ses points de vie au maximum\n" +
										     ".<b>energie</b> - Permet de regénérer ses points d'énergie\n" +
										     ".<b>bontarien</b> - Permet de devenir bontarien\n" +
										     ".<b>brakmarien</b> - Permet de devenir brakmarien\n" +
										     ".<b>serianne</b> - Permet de devenir serianne\n" +
										     ".<b>ticket</b> - Permet contacter un modérateur\n" +
										     ".<b>phoenix</b> - Permet de se téléporter au phoenix\n" +
										     ".<b>Spellmax</b> - Permet d'avoir tout ces sorts niveaux 6\n" +
										     ".<b>Boss</b> - Permet de se téléporter à la zone du boss Asterion\n" +
											 ".<b>vipcommand</b> - Permet de visualiser les commandes V.I.P", Ancestra.COLOR_VERT); 
											
											 
									return; 
							}
									else if (Ancestra.CONFIG_ACTIV_FUNCOMMANDS == true && Ancestra.CONFIG_ACTIV_COMANDESEMIFUN == true && Ancestra.CONFIG_ACTIV_ENERGIE == false)
									{
											SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Commandes disponibles:</u>\n" + //Le socket affiche les informations ci-contre
												     ".<b>infos</b> - Permet d'avoir des informations sur le serveur\n" +
												     ".<b>save</b> - Permet de sauvegarder son personnage\n" +
												     ".<b>start</b> - Permet de se téléporter à la zone de départ\n" +
												     ".<b>staff</b> - Permet de visualiser le staff en ligne\n" +
												     ".<b>guilde</b> - Permet d'afficher le panel de création de guildes\n" +
												     ".<b>mj</b> - Permet de rejoindre la salle des MJ\n" +
												     ".<b>shop</b> - Permet de se téléporter à la zone marchande \n" +
												     ".<b>pvp</b> - Permet de se téléporter à la zone pvp\n" +
												     ".<b>pvm</b> - Permet de se téléporter à la zone pvm\n" +
												     ".<b>enclos</b> - Permet de se téléporter à l'enclos\n" +
												     ".<b>event</b> - Permet de se téléporter à la map event\n" +
												     ".<b>fmcac</b> - Permet de (Fm) son arme\n" +
												     ".<b>parcho</b> - Permet d'auguementer ses caractéristiques\n" +
												     ".<b>vie</b> - Permet de regénérer ses points de vie au maximum\n" +
												     ".<b>bontarien</b> - Permet de devenir bontarien\n" +
												     ".<b>brakmarien</b> - Permet de devenir brakmarien\n" +
												     ".<b>serianne</b> - Permet de devenir serianne\n" +
												     ".<b>ticket</b> - Permet contacter un modérateur\n" +
												     ".<b>phoenix</b> - Permet de se téléporter au phoenix\n" +
												     ".<b>Spellmax</b> - Permet d'avoir tout ces sorts niveaux 6\n" +
												     ".<b>Boss</b> - Permet de se téléporter à la zone du boss Asterion\n" +
													 ".<b>vipcommand</b> - Permet de visualiser les commandes V.I.P", Ancestra.COLOR_VERT); 
													
													 
											return; 
									}
									else if (Ancestra.CONFIG_ACTIV_FUNCOMMANDS == true && Ancestra.CONFIG_ACTIV_COMANDESEMIFUN == false && Ancestra.CONFIG_ACTIV_ENERGIE == true)
									{
											SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Commandes disponibles:</u>\n" + //Le socket affiche les informations ci-contre
												     ".<b>infos</b> - Permet d'avoir des informations sur le serveur\n" +
												     ".<b>save</b> - Permet de sauvegarder son personnage\n" +
												     ".<b>start</b> - Permet de se téléporter à la zone de départ\n" +
												     ".<b>staff</b> - Permet de visualiser le staff en ligne\n" +
												     ".<b>guilde</b> - Permet d'afficher le panel de création de guildes\n" +
												     ".<b>mj</b> - Permet de rejoindre la salle des MJ\n" +
												     ".<b>shop</b> - Permet de se téléporter à la zone marchande \n" +
												     ".<b>pvp</b> - Permet de se téléporter à la zone pvp\n" +
												     ".<b>pvm</b> - Permet de se téléporter à la zone pvm\n" +
												     ".<b>enclos</b> - Permet de se téléporter à l'enclos\n" +
												     ".<b>event</b> - Permet de se téléporter à la map event\n" +
												     ".<b>fmcac</b> - Permet de (Fm) son arme\n" +
												     ".<b>bontarien</b> - Permet de devenir bontarien\n" +
												     ".<b>brakmarien</b> - Permet de devenir brakmarien\n" +
												     ".<b>serianne</b> - Permet de devenir serianne\n" +
												     ".<b>parcho</b> - Permet d'auguementer ses caractéristiques\n" +
												     ".<b>vie</b> - Permet de regénérer ses points de vie au maximum\n" +
												     ".<b>energie</b> - Permet d'afficher les M.A.J de l'émulateur\n" +
												     ".<b>ticket</b> - Permet contacter un modérateur\n" +
												     ".<b>phoenix</b> - Permet de se téléporter au phoenix\n" +
												     ".<b>Spellmax</b> - Permet d'avoir tout ces sorts niveaux 6\n" +
												     ".<b>Boss</b> - Permet de se téléporter à la zone du boss Asterion\n" +
													 ".<b>vipcommand</b> - Permet de visualiser les commandes V.I.P", Ancestra.COLOR_VERT); 
													
													 
											return; 
									}
									else if (Ancestra.CONFIG_ACTIV_FUNCOMMANDS == false && Ancestra.CONFIG_ACTIV_COMANDESEMIFUN == true && Ancestra.CONFIG_ACTIV_ENERGIE == true)
									{
											SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Commandes disponibles:</u>\n" + //Le socket affiche les informations ci-contre
													".<b>infos</b> - Permet d'avoir des informations sur le serveur\n" +
												     ".<b>save</b> - Permet de sauvegarder son personnage\n" +
												     ".<b>start</b> - Permet de se téléporter à la zone de départ\n" +
												     ".<b>staff</b> - Permet de visualiser le staff en ligne\n" +
												     ".<b>guilde</b> - Permet d'afficher le panel de création de guildes\n" +
												     ".<b>mj</b> - Permet de rejoindre la salle des MJ\n" +
												     ".<b>bontarien</b> - Permet de devenir bontarien\n" +
												     ".<b>brakmarien</b> - Permet de devenir brakmarien\n" +
												     ".<b>serianne</b> - Permet de devenir serianne\n" +
												     ".<b>energie</b> - Permet de récupérer ses points d'énergie\n" +
												     ".<b>ticket</b> - Permet contacter un modérateur\n" +
												     ".<b>phoenix</b> - Permet de se téléporter au phoenix\n" +
												     ".<b>Spellmax</b> - Permet d'avoir tout ces sorts niveaux 6\n" +
												     ".<b>Boss</b> - Permet de se téléporter à la zone du boss Asterion\n" +
													 ".<b>vipcommand</b> - Permet de visualiser les commandes V.I.P", Ancestra.COLOR_VERT); 
													
													 
											return; 
											
							
						}
					}
					
					//TODO: Commande d'affichage des commandes V.I.P
					if (Ancestra.CONFIG_ACTIV_VIPCOMMANDS == true)
					{
					if(msg.length() > 10 && msg.substring(1, 11).equalsIgnoreCase("vipcommand")) //Si le joueur écrit .vipcommand
					{
					if (_compte.get_vip() == 1) // Si le compte est V.I.P
					
					{
						
						SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Commandes disponibles:</u>\n" + //Le socket affiche les informations ci-contre
					     ".<b>normal</b> - Permet de redevenir normal\n" +
					     ".<b>invisible</b> - Permet de devenir invisible\n" +
					     ".<b>skin</b> - Permet de se transformer en skin V.I.P\n" +
					     ".<b>refresh</b> - Permet de rafraîchir les monstres sur la map\n" +
					     ".<b>vip</b> - Permet d'accéder à la zone V.I.P\n" +
					     ".<b>monde</b> - Permet de parler en canal commun sans limite\n" +
					     ".<b>prisme</b> - Permet d'avoir des prismes dans son sac\n" +
					     ".<b>speed</b> - Active ou désactive le speed x3\n" +
					     ".<b>zobal</b> - Permet de devenir un zobal mais attention, tout tes niveaux seront perdus. Mettre ses items à la banque est nécessaire.\n", Ancestra.COLOR_VERT); 
						return;
					}
					}
					}
					
						
						
					//TODO: Commande d'affichage d'informations sur le serveur
					if(msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("infos"))//Si le joueur écrit .infos
					{
						long uptime = System.currentTimeMillis() - Ancestra.gameServer.getStartTime();//Calcul des TIMES
						int jour = (int) (uptime/(1000*3600*24));
						uptime %= (1000*3600*24);
						int hour = (int) (uptime/(1000*3600));
						uptime %= (1000*3600);
						int min = (int) (uptime/(1000*60));
						uptime %= (1000*60);
						SocketManager.PACKET_POPUP_DEPART(_perso, "<b>OnEmuR</b> - Version 2.3.0 by Return<br />"
						+         "\nServeur en ligne depuis : <b>"+jour+"</b>j <b>"+hour+"</b>h <b>"+min+"</b>m\n"
						+         "Joueurs en ligne : <b>"+Ancestra.gameServer.getPlayerNumber()+"</b>\n"
						+         "Reccord de connexion: "+Ancestra.gameServer.getMaxPlayer()+"\n");
						return;
					}
					else if(msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("payer"))
					{
						_perso.function_leaveEnnemyFactionAndPay();
						return;
					}
					else if(msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("sortir"))
					{
						_perso.function_leaveEnnemyFaction();
						return;
					}
				
						else 
							
							
					/**TODO: Tutoriel image 2.0 |For Asterion|                          //Beaucoup trop floo for l'image ;D
					if(msg.length() > 3 && msg.substring(1, 6).equalsIgnoreCase("2.0"))
					{
						
						SocketManager.PACKET_MESSAGE_BIENVENUE_INFOS(_perso, "<b>Bienvenue sur Asterion !:</b><br />"
						+ "\n<img src='fuckk*_*' title='Tutoriel'/>");
						return;
						
						
					}**/
						
					
							
							
					//TODO: Commande d'affichage des membres du staff en ligne
							
							if(msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("staff")) // Si le joueur écrit .staff
							{
							String staff = "Membres du staff connectés :\n"; //Assignation d'une  valeur à staff (C'est un string)
							boolean allOffline = true; //On fait une boucle pour vérifier si les GM sont hors ligne
							for(int i = 0; i < World.getOnlinePersos().size(); i++) //Pareil mais pour voir si ils sont en ligne
							{
							if(World.getOnlinePersos().get(i).get_compte().get_gmLvl() > 0 && (World.getOnlinePersos().get(i).get_compte().get_gmLvl() < 6)) //Si dans le ALL_GAME, un joueur a un grade GM
							{
							staff += "- " + World.getOnlinePersos().get(i).get_name() + " ("; //Début des préfix
							if(World.getOnlinePersos().get(i).get_compte().get_gmLvl() == 1) //Si il est égal à 1
							staff += "Animateur)";//Le préfix animateur s'ajoute
							else if(World.getOnlinePersos().get(i).get_compte().get_gmLvl() == 2)//Si il est à 2
							staff += "Superviseur)";//Le préfix MJ s'ajoute
							else if(World.getOnlinePersos().get(i).get_compte().get_gmLvl() == 3)//Si il est à 3
							staff += "Développeur)";//Le préfix Chef-MJ s'ajoute
							else if(World.getOnlinePersos().get(i).get_compte().get_gmLvl() == 4)//Si il est à 4
							staff += "Ressources Humaines)";//Le préfix Co-Amdministrateur s'ajoute
							else if(World.getOnlinePersos().get(i).get_compte().get_gmLvl() == 5)//Si il est à 5
							staff += "Directeur)";//Le préfix Créateur s'ajoute
							else //Et encore
							staff += "Staff";//Rajouter Staff
							staff += "\n";//Aller à la ligne
							allOffline = false; //Si ils sont tous hors ligne, on attribue False à AllOFFLINE
							}
							}
							if(!staff.isEmpty() && !allOffline) //Si le staff est entièrement connecté
							{
								SocketManager.GAME_SEND_MESSAGE(_perso, staff, Ancestra.CONFIG_MOTD_COLOR); //Afficher le string Staff (Tout le code ci dessus)
							}
							else if (allOffline) //Si le staff est hors ligne
							{
							SocketManager.GAME_SEND_MESSAGE(_perso, "Aucun membre du staff est présent !", Ancestra.CONFIG_MOTD_COLOR);//Le socket affiche le message suivant
							}
							return;
							}
					
					//TODO: Commande d'affichage des releases de l'émulateur
					
					if(msg.length() > 7 && msg.substring(1, 8).equalsIgnoreCase("release")) //Si le joueur écrit .maj
		
					{
						
						SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Mises à jour effectués:</u>\n" + //Le socket affiche les informations ci-contre
					     ".<b>maj1</b> - Révision 1.0 \n" +
					     ".<b>maj2</b> - Révision 1.1\n" +
					     ".<b>maj3</b> - Révision 1.2\n" +
					     ".<b>maj4</b> - Révision 1.3\n" +
					     ".<b>maj5</b> - Révision 1.4\n" +
					     ".<b>maj6</b> - Révision 1.5\n" +
					     ".<b>maj7</b> - Révision 1.6\n", Ancestra.COLOR_VERT); 
						return;
					}
					
					
					
						
						//
						//
						//
						//
						//
						//
						//
						//
						//
						//
						//
						//Fin des commandes d'informations |By Return Alias Porky|
					

					
						
						//Début des commandes V.I.P et GM |By Return Alias Porky|
						//
						//
						//
						//
						//
						//
						//
						//
						//
						//
						//
						//
						//
						//
						//
						//
						
					
					if (Ancestra.CONFIG_ACTIV_VIPCOMMANDS == true) //Si les commandes VIP sont activés
					{
				
					//TODO: Commande pour redevenir visible (Si le joueur a été tranformé)
					if(msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("normal")) //Si le joueur écrit .visible
					{
					if(_compte.get_vip() == 1) //Si le compte est V.I.P
					{
							Personnage target = _perso; //On désigne la variable
							int morphID = target.get_classe()*10 + target.get_sexe(); // Effectue le calcul de la classe du personnage
							target.set_gfxID(morphID); //Transforme le personnage avec le résultat du calcul (C'est à dire la morph par défault du personnage, sa classe)
							SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.get_curCarte(), target.get_GUID()); //Effectue les changements niveau visuel sur la map
							SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(target.get_curCarte(), target);// Système de rafrâichissement automatique (Evite le reboot du serveur)
							SocketManager.GAME_SEND_MESSAGE(_perso, "Vous avez été rendu de nouveau normal avec succès !", Ancestra.CONFIG_MOTD_COLOR);// Message d'information
							return;
					}	
						
					else //Si le joueur n'est pas V.I.P
					{
							 SocketManager.GAME_SEND_MESSAGE(_perso, "Tu n'es pas V.I.P ! Pour profiter de cette commande, accède au site et achette le rang necessaire", Ancestra.CONFIG_MOTD_COLOR);//Le socket affiche le message ci-contre
								return;
					}
					}
				
					
					
					//TODO: Commande pour devenir invisible (Nécessite une déconnexion)
					
					if(msg.length() > 9 && msg.substring(1, 10).equalsIgnoreCase("invisible"))//Si le joueur écrit .invisible
					{
						if (_compte.get_vip() == 1)//Si le compte est V.I.P
					{
							Personnage target = _perso; 
							int morphID = 9999; 
							target.set_gfxID(morphID);
							SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.get_curCarte(), target.get_GUID());
							SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(target.get_curCarte(), target);SocketManager.GAME_SEND_MESSAGE(_perso, "Vous avez été rendu invisible avec succès !", Ancestra.CONFIG_MOTD_COLOR);// Message d'information
							return; 	
			       
					}
						else //Si le compte n'est pas V.I.P
					{
							 SocketManager.GAME_SEND_MESSAGE(_perso, "Tu n'es pas V.I.P ! Pour profiter de cette commande, accède au site et achette le rang necessaire", Ancestra.CONFIG_MOTD_COLOR);//Le socket affiche le message ci-contre
								return;
					}
					}
					
					//TODO: Commande pour devenir un skin V.I.P
					
					if(msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("skin"))//Si le joueur écrit .invisible
					{
						if (_compte.get_vip() == 1)//Si le compte est V.I.P
					
							{
								Personnage target = _perso; 
								int morphID = Ancestra.CONFIG_MORPHID_SKIN; 
								target.set_gfxID(morphID);
								SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.get_curCarte(), target.get_GUID());
								SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(target.get_curCarte(), target);SocketManager.GAME_SEND_MESSAGE(_perso, "Vous avez été transformé avec succès !", Ancestra.CONFIG_MOTD_COLOR);// Message d'information
								return;
						}
			          	
			         
					
						else //Si le compte n'est pas V.I.P
					{
							 SocketManager.GAME_SEND_MESSAGE(_perso, "Tu n'es pas V.I.P ! Pour profiter de cette commande, accède au site et achette le rang necessaire", Ancestra.CONFIG_MOTD_COLOR);//Le socket affiche le message ci-contre
								return;
					}
					}
					
					
					//TODO: Commande de téléportation à la zone V.I.P
					
					if(msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("vip"))//Si le joueur écrit .vip
					{
						if (_compte.get_vip() == 1)// Si le compte est V.I.P
					{
					_perso.teleport(Ancestra.CONFIG_MAP_VIP, Ancestra.CONFIG_CELL_VIP); //Téléporter le joueur aux coordonnées suivantes: CONFIG_MAP_VIP (Dans ancestra) | CONFIG_MAP_CELL
					return;
					}
						else //Si le compte n'est pas V.I.P
					{
							 SocketManager.GAME_SEND_MESSAGE(_perso, "Tu n'es pas V.I.P ! Pour profiter de cette commande, accède au site et achette le rang necessaire", Ancestra.CONFIG_MOTD_COLOR);//Le socket affiche le message ci-contre
						return;
					}
					}
					
					
					
					//TODO: Commande de rafraîchissement de monstres sur la carte
					
					if(msg.length() > 7 && msg.substring(1, 8).equalsIgnoreCase("refresh"))//Si le joueur écrit .refresh
					{
						if(_compte.get_vip() >= 1) //Si le compte est V.I.P
					{
						_perso.get_curCarte().refreshSpawns(); //Rafrâichir sur la carte tous les spawns de monstres
						return;
					}
						else //Si le compte n'est pas V.I.P
					{
							 SocketManager.GAME_SEND_MESSAGE(_perso, "Tu n'es pas V.I.P ! Pour profiter de cette commande, accède au site et achette le rang necessaire", Ancestra.CONFIG_MOTD_COLOR);//Le socket affiche le message ci-contre
						return;
					
					}
					}
					
					// TODO : Commande de communiquation dans tout le serveur sans limite
					
						
					if(msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("monde")) //Si le personnage écrit .monde
					{
					if(_compte.get_vip() >= 1)//Si le compte est V.I.P
							
					{
					
						long l;
						String[] infos1 = msg.split(" ",100);
						
						
						/*if(infos1[1]=="")
						{
							infos1[1]="";
						}*/
						if(infos1[1]==null)return;
						
					
						
						
						if((l = System.currentTimeMillis() - _timeLastTradeMsg) < Ancestra.FLOOD_TIME_ALL && _compte.get_gmLvl()==1)
							
						{	
							l = (Ancestra.FLOOD_TIME_ALL  - l)/1000;//On calcul la différence en secondes
							SocketManager.GAME_SEND_Im_PACKET(_perso, "0115;"+((int)Math.ceil(l)+1));
							return;
						}
						
						_timeLastTradeMsg = System.currentTimeMillis();
						String prefix = _perso.get_name();
						String infos[] = msg.split(" ", 2);
						String clicker_name = "<a href='asfunction:onHref,ShowPlayerPopupMenu,"+_perso.get_name()+"'>"+prefix+"</a>";
						SocketManager.GAME_SEND_MESSAGE_TO_ALL((new StringBuilder("(VIP)<b> ")).append(clicker_name).append("</b> : ").append(infos[1]).toString(), "000099");
						
						
						return;
					}
					
						else if(_compte.get_vip() == 0)
					     {
					     SocketManager.GAME_SEND_MESSAGE(_perso, "Tu n'es pas V.I.P ! Pour profiter cette commande, accède au site et achette le rang necessaire", Ancestra.CONFIG_MOTD_COLOR);
					     return;
					     }
					}
					}
					
					//TODO:  Commande de téléportation pour le Maître de Jeu au bureau

					if(msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("gomj"))//Si le joueur écrit .gomj
					{
						if(_compte.get_gmLvl() == 0)//Si le compte n'est pas GM
					{
							SocketManager.GAME_SEND_cMK_PACKET_TO_ADMIN("@", 0, "FAST", "(<b>" + _perso.get_name() + "</b>) a tenter d'utiliser la command .<b>gomj</b>");//Le socket ADMIN affiche dans le panel le message ci-contre
					}
						if(_compte.get_gmLvl() >= 1)//Si le compte est GM
					{
						_perso.teleport((short) 1674, 226);//Téléporter le personnage à la map 1674, cell 226
						SocketManager.GAME_SEND_MESSAGE_TO_ALL("Un maitre du jeu est disponible dans le bureau mj ! faites .<b>mj</b>", Ancestra.COLOR_VERT);//Le socket affiche le message ci-contre
						return;
					}
					}
					
					
						
					
					
					
					//
					//
					//
					//
					//
					//
					//
					//
					//
					//
					//
					//Fin des commandes V.I.P et GM |By Return Alias Porky|
					
					
					
					
					//Début des commandes joueur sans privilège |By Return Alias Porky|
					//
					//
					//
					//
					//
					//
					//
					//
					//
					//
					//
					//
					//
					//
					//
					//
					
					
					
					
					//TODO: Commande innédite Yeah :D
					if (Ancestra.ACTIVER_COMMANDE_SPELLMAX == true)
					{
						if(msg.length() > 8 && msg.substring(1, 9).equalsIgnoreCase("spellmax"))
						{
							int lvlMax = _perso.get_lvl() > 99? (6):(5);
							boolean changed = false;
							for(SortStats sort : _perso.getSorts())
							{
								  if(sort.getLevel() == lvlMax) continue;
	                              _perso.learnSpell(sort.getSpellID(),lvlMax,false,false); 
	                              changed = true;
							}
							if(changed)
							{
								SocketManager.GAME_SEND_SPELL_LIST(_perso);
								SQLManager.SAVE_PERSONNAGE(_perso, false);
								//Message "tous vos sorts ont été fixés au niveau maximum..."
								SocketManager.GAME_SEND_MESSAGE(_perso,"Vous avez boosté vos sorts au niveaux <b>"+lvlMax+"</b>", Ancestra.COLOR_BLEU2);
							}
							return;
						}
					}
					//TODO: Commande de téléportation au bureau des maîtres de jeu
					
					if (Ancestra.CONFIG_ACTIV_MJ == true)
					{
					if(msg.length() > 2 && msg.substring(1, 3).equalsIgnoreCase("mj"))//Si le joueur écrit .mj
				
							
						{
							_perso.teleport((short) 1674, 354); //Le personnage est téléporté à la MapID 1674, CellID 354
							SocketManager.GAME_SEND_MESSAGE(_perso, "<b>Flooder sur cette map est passible de ban !</b>", Ancestra.COLOR_VERT);//Le socket affiche le message ci-contre
							return;
						}
					}
					//TODO: Commande de téléportation à la zone de départ
					
					if(msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("start"))//Si le joueur écrit .start
				
							
						{
						_perso.teleport(Ancestra.CONFIG_START_MAP, Ancestra.CONFIG_START_CELL); //Téléporter le joueur aux coordonnées écrites.
						SocketManager.GAME_SEND_MESSAGE(_perso, "<b>Vous avez été téléporté à la zone de départ</b>", Ancestra.COLOR_VERT);//Le socket affiche le message ci-contre
							return;
						}
					
					//TODO: Commande de téléportation au Phoenix
					
					if(msg.length() > 7 && msg.substring(1, 8).equalsIgnoreCase("phoenix"))//Si le joueur écrit .start
				
							
						{
						_perso.teleport((short) 8534, 297);//Téléporter le personnage à la map et celle inscrite.
						SocketManager.GAME_SEND_MESSAGE(_perso, "<b>Vous avez été téléporté au Phoenixt</b>", Ancestra.COLOR_VERT);//Le socket affiche le message ci-contre
							return;
						}
					
					
					
					//TODO: Commande d'affichage du pannel de création de guilde
					
						if (Ancestra.CONFIG_ACTIV_COMMAND_GUILD == true)
						{
						if(msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("guilde"))//Si le joueur écrit .guilde
						{
						Personnage perso = _perso; //On assigne Personnage perso à la variable _perso
						if(perso == null) //Si le personnage n'est pas sélectionné
							
						{
						String mess = "Le personnage n'existe pas."; //Le socket ADMIN affiche le message ci-contre
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);// Disconnect Socket
						return;
						}
						
						
					
						if(!perso.isOnline()) //Si le personnage n'est pas en ligne
							
						{
						String mess = (new StringBuilder("Le personnage ")).append(perso.get_name()).append(" n'etait pas connecte").toString(); //Le socket ADMIN affiche  le message ci-contre (Le personnage [NOM_DU_JOUEUR] n'était pas connecté)
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);//Disconnect Socket
						return;
						}
						
						
						if(perso.get_guild() != null || perso.getGuildMember() != null) //Si le personnage a déjà une guilde
						{
						String mess = (new StringBuilder("Le personnage ")).append(perso.get_name()).append(" a deja une guilde").toString();//Le socket ADMIN affiche le message ci-contre (Le personnage [NOM_DU_JOUEUR] a déjà une guilde
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);//Disconnect Socket
						return;
						} 
						
						else //Si aucune erreur n'a été trouvé
							
						{
						SocketManager.GAME_SEND_gn_PACKET(perso); //On ouvre le panel de création de guildes
						String mess = (new StringBuilder(String.valueOf(perso.get_name()))).append(": Panneau de creation de guilde ouvert").toString(); //Le socket ADMIN affiche le message ci-contre
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);//Disconnect Socket
						return;
						}
						}
						}
						
							
					//TODO: Commande de sauvegarde du personnage + DEBUG
						
						if(msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("save"))//Si le joueur écrit .save
						{
							if((System.currentTimeMillis() - _timeLastsave) < 360000) //Si le temps du dernier save est inférieur à 360 000 secondes
							{
								SocketManager.GAME_SEND_MESSAGE(_perso, "Vous avez déjà débloqué votre personnage il y a moins d'1h00", Ancestra.CONFIG_MOTD_COLOR);
								return;
							}
							_timeLastsave = System.currentTimeMillis(); //On donne les informations
							if(_perso.get_fight() != null)return; //Si le joueur est en combat
							SQLManager.SAVE_PERSONNAGE(_perso,true);//Sauvegarde du personnage
							SocketManager.GAME_SEND_MESSAGE(_perso,  "Votre personnage: <b>"+_perso.get_name()+"</b> a été sauvegardé", Ancestra.COLOR_BLEU2);//Le socket affiche le message ci-contre
							return;
						}
						if(msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("debug"))//Si le joueur écrit .debug
						{
							if((System.currentTimeMillis() - _timeLastDebug) < 3600000) //Si le temps du dernier debug est inférieur à 360 000 secondes
							{
								SocketManager.GAME_SEND_MESSAGE(_perso, "Vous avez déjà débloqué votre personnage il y a moins d'1h00", Ancestra.CONFIG_MOTD_COLOR);
								return;
							}
							_timeLastDebug = System.currentTimeMillis(); //On donne les informations
							Personnage perso = _perso;
			                SocketManager.GAME_SEND_GV_PACKET(perso);
			                perso.set_duelID(-1);
			                perso.set_ready(false);
			                perso.fullPDV();
			                perso.set_fight(null);
			                SocketManager.GAME_SEND_GV_PACKET(perso);
			                SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(perso._curCarte, perso);
			                perso.get_curCell().addPerso(perso);
			                perso.teleport(Ancestra.CONFIG_START_MAP, Ancestra.CONFIG_START_CELL);
							SQLManager.SAVE_PERSONNAGE(_perso,true);//Sauvegarde du personnage
							SocketManager.GAME_SEND_MESSAGE(_perso,  "Votre personnage: <b>"+_perso.get_name()+"</b> a été débug", Ancestra.COLOR_BLEU2);//Le socket affiche le message ci-contre
							return;
						}
						//TODO: Commande de message Modérateur/Joeur
						if(msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("ticket"))
						{		
							
							if (msg.length() > 15)
							{
									String infos[] = msg.split(" ", 2);
									
									int verif = SQLManager.Verifticket(_perso.get_name());
									
									if (verif == 0)
									{
										SQLManager.addticket(_perso.get_name(),infos[1]);
										SocketManager.GAME_SEND_MESSAGE(_perso, "Votre ticket à bien été pris en compte , il sera traité lorsque qu'un maitre de jeu sera disponible", Ancestra.COLOR_VERT);
										return;
									}
									else
									{
										SocketManager.GAME_SEND_MESSAGE(_perso, "Vous avez déjà  un ticket en cours . Merci de patienter", Ancestra.COLOR_BLEU);
										return;
									}
							}
							else
							{
								SocketManager.GAME_SEND_MESSAGE(_perso, "Vous n'avez saisie aucun message", Ancestra.COLOR_BLEU);
								return;
							}
						}
				
				
				
				//TODO: Commande spéciale pour Sadidom très propre *_*
				if (Ancestra.CONFIG_ACTIV_ENERGIE == true)
				{
					if(msg.length() > 7 && msg.substring(1, 8).equalsIgnoreCase("energie"))
					{
					if(_perso.get_fight() != null)
					{
					SocketManager.GAME_SEND_MESSAGE(_perso, "Vous ne pouvez pas effectuer cette action en combat !", Ancestra.COLOR_VERT);
					return;
					}
					else
					{
					if(_perso.get_energy() >= 5001)
					{
					String mess = "Vous avez déjà plus de 5000 poins d'énergie";
					SocketManager.GAME_SEND_MESSAGE(_perso, mess, Ancestra.COLOR_VERT);
					return;
					}
					else
					{
					int EnergyNbr = 5000;
					int EnergyWin = _perso.get_energy() + EnergyNbr;
					_perso.set_energy(EnergyWin);
					SocketManager.GAME_SEND_STATS_PACKET(_perso);
					return;
					}
					}
					}
				}
		
				
				
				/////////Commandes fun activables/désactivables via config |By Return Alias Porky| \\\\\\\\\\
				
				if (Ancestra.CONFIG_ACTIV_FUNCOMMANDS == true) //Si les commandes FUN sont activés
				{
				//TODO: Commande de téléportation à la zone SHOP
				
				if(msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("shop"))//Si le joueur écrit .shop
				{
				_perso.teleport(Ancestra.CONFIG_MAP_SHOP, Ancestra.CONFIG_CELL_SHOP); //Téléporter le joueur aux coordonnées suivantes: CONFIG_MAP_SHOP (Dans ancestra) | CONFIG_CELL_SHOP
				return;
				}
				
				//TODO: Commande de téléportation à la zone PVP
				
				if(msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("pvp"))//Si le joueur écrit .pvp
				{
				_perso.teleport(Ancestra.CONFIG_MAP_PVP, Ancestra.CONFIG_CELL_PVP); //Téléporter le joueur aux coordonnées suivantes: CONFIG_MAP_PVP (Dans ancestra) | CONFIG_CELL_PVP
				return;
				}
				
				//TODO: Commande de téléportation à la zone PVM
				
				if(msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("pvm"))//Si le joueur écrit .pvm
				{
				_perso.teleport(Ancestra.CONFIG_MAP_PVM, Ancestra.CONFIG_CELL_PVM); //Téléporter le joueur aux coordonnées suivantes: CONFIG_MAP_PVM (Dans ancestra) | CONFIG_CELL_PVM
				return;
				}
				
				//TODO: Commande de téléportation à l'enclos
				
				if(msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("enclos"))//Si le joueur écrit .enclos
				{
				_perso.teleport(Ancestra.CONFIG_MAP_ENCLOS, Ancestra.CONFIG_CELL_ENCLOS); //Téléporter le joueur aux coordonnées suivantes: CONFIG_MAP_ENCLOS (Dans ancestra) | CONFIG_CELL_ENCLOS
				return;
				}
				
				//TODO: Commande de téléportation à la zone EVENT
				
				if(msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("event"))//Si le joueur écrit .event
				{
				_perso.teleport(Ancestra.CONFIG_MAP_EVENT, Ancestra.CONFIG_CELL_EVENT); //Téléporter le joueur aux coordonnées suivantes: CONFIG_MAP_EVENT (Dans ancestra) | CONFIG_CELL_EVENT
				return;
				}
				
				//TODO: Commandes d'fm via tchat
				
				if(msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("fmcac"))
				{
				Objet obj = _perso.getObjetByPos(Constants.ITEM_POS_ARME);

				if(_perso.get_kamas() < Ancestra.CONFIG_PRIX_FMCAC)
				{
				SocketManager.GAME_SEND_MESSAGE(_perso, "Action impossible : vous avez moins de "+Ancestra.CONFIG_PRIX_FMCAC+" k", Ancestra.CONFIG_MOTD_COLOR);
				return;
				}

				else if(_perso.get_fight() != null)
				{
				SocketManager.GAME_SEND_MESSAGE(_perso, "Action impossible : vous ne devez pas être en combat", Ancestra.CONFIG_MOTD_COLOR);
				return;
				}

				else if(obj == null)
				{
				SocketManager.GAME_SEND_MESSAGE(_perso, "Action impossible : vous ne portez pas d'arme", Ancestra.CONFIG_MOTD_COLOR);
				return;
				}

				boolean containNeutre = false;
				for(SpellEffect effect : obj.getEffects())
				{
				if(effect.getEffectID() == 100 || effect.getEffectID() == 95)
				{
				containNeutre = true;
				}
				}
				if(!containNeutre)
				{
				SocketManager.GAME_SEND_MESSAGE(_perso, "Action impossible : votre arme n'a pas de dégats neutre", Ancestra.CONFIG_MOTD_COLOR);
				return;
				}

				String answer;

				try
				{
				answer = msg.substring(7, msg.length() - 1);
				}
				catch(Exception e)
				{
				SocketManager.GAME_SEND_MESSAGE(_perso, "Action impossible : vous n'avez pas spécifié l'élément (air, feu, terre, eau) qui remplacera les dégats/vols de vies neutres", Ancestra.CONFIG_MOTD_COLOR);
				return;
				}

				if(!answer.equalsIgnoreCase("air") && !answer.equalsIgnoreCase("terre") && !answer.equalsIgnoreCase("feu") && !answer.equalsIgnoreCase("eau"))
				{
				SocketManager.GAME_SEND_MESSAGE(_perso, "Action impossible : l'élément " + answer + " n'existe pas ! (dispo : air, feu, terre, eau)", Ancestra.CONFIG_MOTD_COLOR);
				return;
				}

				for(int i = 0; i < obj.getEffects().size(); i++)
				{
				if(obj.getEffects().get(i).getEffectID() == 100)
				{
				if(answer.equalsIgnoreCase("air"))
				{
				obj.getEffects().get(i).setEffectID(98);
				}
				if(answer.equalsIgnoreCase("feu"))
				{
				obj.getEffects().get(i).setEffectID(99);
				}
				if(answer.equalsIgnoreCase("terre"))
				{
				obj.getEffects().get(i).setEffectID(97);
				}
				if(answer.equalsIgnoreCase("eau"))
				{
				obj.getEffects().get(i).setEffectID(96);
				}
				}

				if(obj.getEffects().get(i).getEffectID() == 95)
				{
				if(answer.equalsIgnoreCase("air"))
				{
				obj.getEffects().get(i).setEffectID(93);
				}
				if(answer.equalsIgnoreCase("feu"))
				{
				obj.getEffects().get(i).setEffectID(94);
				}
				if(answer.equalsIgnoreCase("terre"))
				{
				obj.getEffects().get(i).setEffectID(92);
				}
				if(answer.equalsIgnoreCase("eau"))
				{
				obj.getEffects().get(i).setEffectID(91);
				}
				}
				}

				long new_kamas = _perso.get_kamas() - Ancestra.CONFIG_PRIX_FMCAC ;
				if(new_kamas < 0) //Ne devrait pas arriver...
				new_kamas = 0;
				_perso.set_kamas(new_kamas);

				SocketManager.GAME_SEND_STATS_PACKET(_perso);

				SocketManager.GAME_SEND_MESSAGE(_perso, "Votre objet : " + obj.getTemplate().getName() + " a été FM avec succès en " + answer, Ancestra.CONFIG_MOTD_COLOR);
				SocketManager.GAME_SEND_MESSAGE(_perso, " Penser à vous deco/reco pour voir les changement !", Ancestra.CONFIG_MOTD_COLOR);
				return;
				}
				
				//TODO: Commande de parchotage via tchat
				
				if(msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("parcho")) //Commande .parcho
				{
				if(_perso.get_fight() != null)
				return;

				String element = "";
				int nbreElement = 0;
				if(_perso.get_baseStats().getEffect(125) < 101)
				{
				_perso.get_baseStats().addOneStat(125, 101 - _perso.get_baseStats().getEffect(125));
				element += "vitalité";
				nbreElement++;
				}

				if(_perso.get_baseStats().getEffect(124) < 101)
				{
				_perso.get_baseStats().addOneStat(124, 101 - _perso.get_baseStats().getEffect(124));
				if(nbreElement == 0)
				element += "sagesse";
				else
				element += ", sagesse";
				nbreElement++;
				}

				if(_perso.get_baseStats().getEffect(118) < 101)
				{
				_perso.get_baseStats().addOneStat(118, 101 - _perso.get_baseStats().getEffect(118));
				if(nbreElement == 0)
				element += "force";
				else
				element += ", force";
				nbreElement++;
				}

				if(_perso.get_baseStats().getEffect(126) < 101)
				{
				_perso.get_baseStats().addOneStat(126, 101 - _perso.get_baseStats().getEffect(126));
				if(nbreElement == 0)
				element += "intelligence";
				else
				element += ", intelligence";
				nbreElement++;
				}

				if(_perso.get_baseStats().getEffect(119) < 101)
				{
				_perso.get_baseStats().addOneStat(119, 101 - _perso.get_baseStats().getEffect(119));
				if(nbreElement == 0)
				element += "agilité";
				else
				element += ", agilité";
				nbreElement++;
				}

				if(_perso.get_baseStats().getEffect(123) < 101)
				{
				_perso.get_baseStats().addOneStat(123, 101 - _perso.get_baseStats().getEffect(123));
				if(nbreElement == 0)
				element += "chance";
				else
				element += ", chance";
				nbreElement++;
				}

				if(nbreElement == 0)
				{
				SocketManager.GAME_SEND_Im_PACKET(_perso, "116;<i>Serveur:</i>Vous avez déjà plus de 100 partout !");
				}
				else
				{
				SocketManager.GAME_SEND_STATS_PACKET(_perso);
				SocketManager.GAME_SEND_Im_PACKET(_perso, "116;<i>Serveur:</i>Vous êtes parcho 101 en " + element + " !");
				}
				return;
				}
				
				//TODO: Commande de regénération de vitalité
				
				if(msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("vie"))//Commande vie
				{
				int count = 100;
				Personnage perso = _perso;
				int newPDV = (perso.get_PDVMAX() * count) / 100;
				perso.set_PDV(newPDV);
				if(perso.isOnline())
				{
				SocketManager.GAME_SEND_STATS_PACKET(perso);
				}
				SocketManager.GAME_SEND_MESSAGE(_perso, "Vos points de vie sont au maximum.", Ancestra.COLOR_BLEU);
				return;
				}
				}
		//Fin des commandes fun activables/désactivables |By Return Alias Porky|
				
				
				
				/////////Début des commandes Semi-Fun activables/désactivables\\\\\\\\\\\
				
				
				if (Ancestra.CONFIG_ACTIV_SEMIFUNCOMMANDS == true) //Si les commandes SEMI-FUNS sont activés
				{
				//TODO: Commande d'alignement brakmarien
				
				if(msg.length() > 10 && msg.substring(1, 11).equalsIgnoreCase("brakmarien")) 
				{
				byte align = 2;
				Personnage target = _perso;
				target.modifAlignement(align);
				if(target.isOnline())
				SocketManager.GAME_SEND_STATS_PACKET(target);
				SocketManager.GAME_SEND_MESSAGE(_perso, "Tu es désormais Brakmarien", Ancestra.CONFIG_MOTD_COLOR);
				return;
				} 
				
				//TODO: Commande d'alignement bontarien
				
				if(msg.length() > 9 && msg.substring(1, 10).equalsIgnoreCase("bontarien")) 
				{
				byte align = 1;
				Personnage target = _perso;
				target.modifAlignement(align);
				if(target.isOnline())
				SocketManager.GAME_SEND_STATS_PACKET(target);
				SocketManager.GAME_SEND_MESSAGE(_perso, "Tu es désormais Bontarien", Ancestra.CONFIG_MOTD_COLOR);
				return;
				}
				
				//TODO: Commande d'alignement serianne
				
				if(msg.length() > 8 && msg.substring(1, 9).equalsIgnoreCase("serianne")) 
				{
				byte align = 3;
				Personnage target = _perso;
				target.modifAlignement(align);
				if(target.isOnline())
				SocketManager.GAME_SEND_STATS_PACKET(target);
				SocketManager.GAME_SEND_MESSAGE(_perso, "Tu es désormais Serianne", Ancestra.CONFIG_MOTD_COLOR);
				return;
				}
				
				//TODO: Commande d'alignement neutre
				
				if(msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("neutre")) 
				{
				byte align = 0;
				Personnage target = _perso;
				target.modifAlignement(align);
				if(target.isOnline())
				SocketManager.GAME_SEND_STATS_PACKET(target);
				SocketManager.GAME_SEND_MESSAGE(_perso, "Tu es désormais Neutre", Ancestra.CONFIG_MOTD_COLOR);
				return;
				}
				}//Fin des commandes semi-fun activables/désactivables |By Return alias Porky|
				
				//
				//
				//
				//
				//
				//
				//
				//
				//
				//
				//
				//Fin des commandes joueur sans privilège |By Return Alias Porky|
				
				
				
				//Début des commandes M.A.J |By Return Alias Porky|
				//
				//
				//
				//
				//
				//
				//
				//
				//
				//
				//
				//
				//
				//
				//
				//
				
				//TODO: Mise à jour 1.0
				
				if (Ancestra.CONFIG_ACTIV_MAJ == true)
				{
				if(msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("maj1")) //Si le joueur écrit .maj1
					
				{
					
					SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Changelogs:</u>\n" + //Le socket affiche les informations ci-contre
				     "-Refonte totale des commandes (Plus en détails dessous)\n" +
				     "-Revue de la structure (Le code est moins sale)\n" +
				     "-Revue partiel des restrictions (La condition V.I.P buggué)\n" +
				     "-Supression de lignes innutiles (Tel que les commentaires et fonctions innutiles)\n" +
				     "-Ajouts de plusieurs COLORS pour les commandes diverses\n", Ancestra.COLOR_VERT); 
					return;
				}
				
				//TODO: Mise à jour 1.1
				
				if(msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("maj2")) //Si le joueur écrit .maj2
					
				{
					
					SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Changelogs:</u>\n" + //Le socket affiche les informations ci-contre
				     "-Supression du système d'étoile (L'expérience en % était trop élevé)\n" +
				     "-Mise en place finale des restrictions (La condition V.I.P est fonctionnelle à 100%)\n" +
				     "-Création d'une commande spéciale V.I.P .invisible (En bêtâ TEST)\n" +
				     "-Création d'une seconde commande spéciale V.I.P .visible (Permet de redevenir visible)\n" +
				     "-Création d'une troisième commande spéciale V.I.P .refresh (En BêTâ TEST)\n", Ancestra.COLOR_VERT); 
					return;
				}
				
				//TODO: Mise à jour 1.2
				
				if(msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("maj3")) //Si le joueur écrit .maj3
					
				{
					
					SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Changelogs:</u>\n" + //Le socket affiche les informations ci-contre
				     "-Correction des commandes. La restriction V.I.P à été mise en place et à 100%\n" +
				     "-La commande .monde est désormais réservé aux V.I.P\n" +
				     "-La commande .refresh devient stable (100% Permet de rafraîchir le groupe de monstre sur une map)\n" +
				     "-La commande .invisible devient stable (100% Permet de devenir invisible mais nécessite une déconnexion)\n" +
				     "-Supression de commandes pour Anka'Like (.shop | .pvp | .fmcac | .parcho)\n", Ancestra.COLOR_VERT); 
					return;
				}
				
				//TODO: Mise à jour 1.3
				
				if(msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("maj4")) //Si le joueur écrit .maj4
					
				{
					
					SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Changelogs:</u>\n" + //Le socket affiche les informations ci-contre
				     "-Ajout d'une commande .mj (Et .gomj pour le maître de jeu)\n" +
				     "-La commande |.monde| a été revue. Désormais nom cliquable et modification de sa couleur (Bleu)\n" +
				     "-Tentative de rectification du XP (Avancement 10% |Attendre la prochaine M.A.J)\n" +
				     "-Stabilité des commandes, l'émulateur sort dans sa version stable... A suivre.\n", Ancestra.COLOR_VERT); 
					return;
				}
				
				//TODO: Mise à jour 1.4
				
				if(msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("maj5")) //Si le joueur écrit .maj5
				
				{
					
					SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Changelogs:</u>\n" + //Le socket affiche les informations ci-contre
				     "-Nouveau nom à l'émulateur: OnEmu\n" +
				     "-Supression de commandes pour Anka'Like (.bontarien | .brakmarien | .vie)\n" +
				     "-Ajout d'un système Anti-DoSS et Anti Cheat\n" +
				     "-Rectification partielle du XP (Avancement 25% |Attendre la prochaine M.A.J)\n", Ancestra.COLOR_VERT); 
					return;
				}
				
				//TODO: Mise à jour 1.5
				
				if(msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("maj6")) //Si le joueur écrit .maj6
				
				{
					
					SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Changelogs:</u>\n" + //Le socket affiche les informations ci-contre
				     "-Revue complète de GameThread niveau commandes. (Restructuration, mise au propre et commentaires ajoutés pour les débutants en JAVA. 6h00)\n" +
				     "-Supression des commandes désactivés (Economie du CPU)\n" +
				     "-Réorganisation du CryptManager (Economie du CPU)\n" +
				     "-L'anti Cheat a une nouvelle fonction (Anti Vip_Mod)\n" +
				     "-Création de diverses commandes (.maj | .maj1 | .maj2 | .maj3 | .maj4 | .maj5 | .maj5) ect...\n" +
				     "-Rectification partielle du XP (Avancement 40% |Attendre la prochaine M.A.J)\n", Ancestra.COLOR_VERT); 
					return;
				}
				
				//TODO: Mise à jour 1.6
				
				if(msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("maj7")) //Si le joueur écrit .maj7
				
				{
					
					SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Changelogs:</u>\n" + //Le socket affiche les informations ci-contre
				     "-Création de deux nouvelles montures (JAVA): Montilier & Hurledent\n" +
				     "-Mise en place des variables ID Montilier | Hurledent | Dragodinde Squelette\n" +
				     "-Ajout de stats anka'like aux montures créées/modifiées: Hurledent: +80PP | Montilier: +50% dommages | Dragodinde Squelette: +600 vitalité\n" +
				     "-Ajout de diverses variables Options: ACTIVER_COMMANDE_GUILDE | ACTIVER_COMMANDES_VIP | ACTIVER_COMMANDES_MAJ\n" +
				     "-Rectification des variables MAP_VIP & CELL_VIP. (Elles marchent désormais)\n" +
				     "-Rectification partielle du XP (Avancement 50% |Attendre la prochaine M.A.J) (Peu de temps en semaine, voilà pourquoi seulement 5%)\n", Ancestra.COLOR_VERT); 
					return;
				}
				
				//TODO: Mise à jour 1.7
				
				if(msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("maj8")) //Si le joueur écrit .maj7
				
				{
					
					SocketManager.GAME_SEND_MESSAGE(_perso,"<u>Changelogs:</u>\n" + //Le socket affiche les informations ci-contre
				     "-Ajout de l'xp en défie\n" +
				     "-Correction du BUG TCHAT (Dans 1.6): Désormais, on peut reparler.\n" +
				     "-Remise en place des commandes Fun(s) activables/désactivables: SHOP|PVP|PVM|ENCLOS|EVENT|FMCAC|PARCHO|VIE\n" +
				     "-Mise en place de variables OPTIONS: ACTIVER_PANOPLIE_BIENVENUE = true | ACTIVER_XP_DEFI = false | ACTIVIER_COMMANDES_ALIGNEMENT = false |" +
				     "-Ajout des updates SWF & SQL (Pour les montiliers)" +
				     "-Ajout de variables pour STATS des montiliers/hurledent/Drago_Squelette:  HURLEDENT_STATS_TYPE = XXX | HURLEDENT_STATS = XXX " +
				     "-Ajout de tutoriels dans le LISEZ-MOI pour les Updates & Nouvelles montures" +
				     "-Rectification partielle du XP (Avancement 60% |Attendre la prochaine M.A.J) (Peu de temps en semaine, voilà pourquoi seulement 5%)\n", Ancestra.COLOR_VERT); 
					return;
				}
				}
				
				
				
				//Fin des commandes M.A.J
				
				//Début commande zobal |By Return alias Porky|
				//
				//
				//
				//
				//
				//
				//
				//
				//
				//
				//
				
				if (Ancestra.CONFIG_ACTIV_ZOBAL == true)
				{
				if(msg.length() >5 && msg.substring(1,6).equalsIgnoreCase("zobal"))
				{
				if (_compte.get_vip() == 1 && _perso.get_classe() < 13 && _perso.get_lvl() >= Ancestra.CONFIG_LEVEL_REQUIERT_ZOBAL)
				{
				
			
						
				    _perso.remove();
				    _perso.get_compte().getGameThread().kick();
				    Personnage.CREATE_PERSONNAGE(_perso.get_name(), _perso.get_sexe(), 14, -1, -1, -1, _perso.get_compte());
			        return;
			        
			        
				}
				else if (_compte.get_vip() == 1 && _perso.get_classe() == 14 && _perso.get_gfxID() == 141)
				
				{
					Personnage target = _perso; 
					int morphID = 140; 
					target.set_gfxID(morphID);
					SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.get_curCarte(), target.get_GUID());
					SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(target.get_curCarte(), target);SocketManager.GAME_SEND_MESSAGE(_perso, "Optimisation du zobal accepté. De nouveau, refaites .zobal", Ancestra.COLOR_BLEU2);// Message d'information
					return;
		    	}
				else if (_compte.get_vip() == 1 && _perso.get_classe() == 14 && _perso.get_gfxID() == 140)
				{
					_perso.learnSpell(6000, 1, true, true);
		            _perso.learnSpell(6001, 1, true, true);
		            _perso.learnSpell(6002, 1, true, true);
		            _perso.learnSpell(6003, 1, true, true);
		            _perso.learnSpell(6004, 1, true, true);
		            _perso.learnSpell(6005, 1, true, true);
		            _perso.learnSpell(6006, 1, true, true);
		            _perso.learnSpell(6007, 1, true, true);
		            _perso.learnSpell(6008, 1, true, true);
		            _perso.learnSpell(6009, 1, true, true);
		            _perso.learnSpell(6010, 1, true, true);
		            _perso.learnSpell(6011, 1, true, true);
		            _perso.learnSpell(6012, 1, true, true);
		            _perso.learnSpell(6013, 1, true, true);
		            _perso.learnSpell(6014, 1, true, true);
		            _perso.learnSpell(6015, 1, true, true);
					Personnage target = _perso; //On désigne la variable
					int morphID = 142; // Effectue le calcul de la classe du personnage
					target.set_gfxID(morphID); //Transforme le personnage avec le résultat du calcul (C'est à dire la morph par défault du personnage, sa classe)
					SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.get_curCarte(), target.get_GUID()); //Effectue les changements niveau visuel sur la map
					SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(target.get_curCarte(), target);// Système de rafrâichissement automatique (Evite le reboot du serveur)
					SocketManager.GAME_SEND_MESSAGE(_perso, "Vous êtes désormais un zobal à part entière !", Ancestra.COLOR_BLEU2);
		       	 	return;
		       	 	
				}
		       	 	else if (_perso.get_classe() == 14)
		       	{
		       	 	SocketManager.GAME_SEND_MESSAGE(_perso,"Commande innacessible: tu es déjà un zobal !", Ancestra.COLOR_BLEU2);
					return;
				
		        }
		       	 	
				
				
				else if (_compte.get_vip() == 0)
				{
					
					SocketManager.GAME_SEND_MESSAGE(_perso,"Tu n'es pas V.I.P ! Pour profiter de cette commande, accède au site et achette le rang necessaire", Ancestra.COLOR_BLEU2);
					return;
				}
				else if (_compte.get_vip() == 1 && _perso.get_lvl() < Ancestra.CONFIG_LEVEL_REQUIERT_ZOBAL)
				
				{
						
						SocketManager.GAME_SEND_MESSAGE(_perso,"Tu n'as pas encore atteint le niveau "+Ancestra.CONFIG_LEVEL_REQUIERT_ZOBAL+" !", Ancestra.COLOR_BLEU2);
						return;
				}
				else if (_compte.get_vip() == 1 && _perso.get_classe() == 14 && _perso.get_gfxID() == 142)
					
				{
							
							SocketManager.GAME_SEND_MESSAGE(_perso,"Commande innacessible: tu es déjà un zobal !", Ancestra.COLOR_BLEU2);
							return;
						
						
				}
				
				else
				{
				
				SocketManager.GAME_SEND_MESSAGE(_perso, "Erreur système, veuillez contacter l'administrateur.", Ancestra.COLOR_BLEU2);
				return;
				}
				}
				}
				
				/** Commandes spéciales pour Asterions **/
				
			if (Ancestra.ACTIVER_COMMANDE_PRISME == true)
			{
				if(msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("prisme"))//Si le joueur écrit .event
				{
					if (Ancestra.COMMANDE_PRISME_VIP == true)
					{
						if (_compte.get_vip() == 1 && _perso.get_lvl() >= Ancestra.LEVEL_REQUIS_COMMANDE_PRISME)
							
						{
									int tID = 8990;
									ObjTemplate t = World.getObjTemplate(tID);
									Objet obj =t.createNewItem(Ancestra.NOMBRE_COMMANDE_PRISME,true);
									_perso.addObjet(obj, true);
									
									SocketManager.GAME_SEND_MESSAGE(_perso, "<b>Vous venez de recevoir "+Ancestra.NOMBRE_COMMANDE_PRISME+" prismes</b>", Ancestra.COLOR_BLEU2);
									return;
						}
								
						else if (_perso.get_lvl() <= Ancestra.LEVEL_REQUIS_COMMANDE_PRISME)
							
						{
							
									SocketManager.GAME_SEND_MESSAGE(_perso, "<b>Action impossible: le level "+Ancestra.LEVEL_REQUIS_COMMANDE_PRISME+" est requis !</b>", Ancestra.COLOR_BLEU2);
										
						}
								
						else
								
						{
									SocketManager.GAME_SEND_MESSAGE(_perso, "<b>Tu n'es pas V.I.P ! Pour profiter de cette commande, accède au site et achette le rang necessaire</b>", Ancestra.COLOR_BLEU2);
									
						}
					
					}
					
					else if (Ancestra.COMMANDE_PRISME_VIP == false)
						
					{
						if (_perso.get_lvl() >= Ancestra.LEVEL_REQUIS_COMMANDE_PRISME)
							
						{
									ObjTemplate t = World.getObjTemplate(8990);
									Objet obj = t.createNewItem(Ancestra.NOMBRE_COMMANDE_PRISME,true);
									if(_perso.addObjet(obj, true))
									World.addObjet(obj,true);SocketManager.GAME_SEND_MESSAGE(_perso, "<b>Vous venez de recevoir "+Ancestra.NOMBRE_COMMANDE_PRISME+" prismes</b>", Ancestra.COLOR_BLEU2);
									return;
						}
								
						else if (_perso.get_lvl() <= Ancestra.LEVEL_REQUIS_COMMANDE_PRISME)
							
						{
							
									SocketManager.GAME_SEND_MESSAGE(_perso, "<b>Action impossible: le level "+Ancestra.LEVEL_REQUIS_COMMANDE_PRISME+" est requis !</b>", Ancestra.COLOR_BLEU2);
										
						}
					
					}
				}
			}
			
			if(msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("sorts")) 
			{
				_perso.learnSpell(15000, 1, true, true);
	            _perso.learnSpell(15001, 1, true, true);
	            _perso.learnSpell(15002, 1, true, true);
	            _perso.learnSpell(15003, 1, true, true);
	            _perso.learnSpell(15004, 1, true, true);
	            _perso.learnSpell(15005, 1, true, true);
	            _perso.learnSpell(15006, 1, true, true);
	            _perso.learnSpell(15007, 1, true, true);
	            _perso.learnSpell(15008, 1, true, true);
	            _perso.learnSpell(15009, 1, true, true);
	            SocketManager.GAME_SEND_MESSAGE(_perso, "<b>Vous venez d'apprendre les nouveaux sorts</b>", Ancestra.COLOR_BLEU);
	            return;
			}
			
			if (msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("boss"))
			{
				_perso.teleport(Ancestra.MAP_BOSS, Ancestra.CELL_BOSS);
				SocketManager.GAME_SEND_MESSAGE(_perso, "<b>Vous avez été téléporté au boss Asterion</b>", Ancestra.COLOR_BLEU2);
				return;
			}
			//set_Speed(-40);
			if(msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("speed")) 
				
			{
				if (_compte.get_vip() >= 1)
				{
					if (_perso.get_Speed() < 200)
					{
					Personnage target = _perso;
					_perso.set_Speed(23);
					SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.get_curCarte(), target.get_GUID()); //Effectue les changements niveau visuel sur la map
					SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(target.get_curCarte(), target);// Système de rafrâichissement automatique (Evite le reboot du serveur)
					SocketManager.GAME_SEND_MESSAGE(_perso, "Votre vitesse a été auguementé avec succès.", Ancestra.COLOR_BLEU2);
					
					}
					else
					{
					Personnage target = _perso;
					_perso.set_Speed(23);
					SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.get_curCarte(), target.get_GUID()); //Effectue les changements niveau visuel sur la map
					SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(target.get_curCarte(), target);// Système de rafrâichissement automatique 
					SocketManager.GAME_SEND_MESSAGE(_perso, "Votre vitesse a été remise à la normale avec succès.", Ancestra.COLOR_BLEU2);
					
					}
					return;
				}
				else
				{
					SocketManager.GAME_SEND_MESSAGE(_perso, "Tu n'es pas V.I.P ! Pour profiter de cette commande, accède au site et achette le rang necessaire", Ancestra.CONFIG_MOTD_COLOR);//Le socket affiche le message ci-contre
					return;
				}
			}
			//TODO: Commande d'achat d'items EVENT (A terminé)
			
			if (msg.length() > 5 && msg.substring(1,6).equalsIgnoreCase("cadeau"))
				
			{
				if (_perso.get_compte().getEventPoints() >= 1)
				{
					SocketManager.GAME_SEND_MESSAGE(_perso, "Vous avez actuellement 1+ point event", Ancestra.COLOR_BLEU2);
					//_perso.addObjet(184, true);
					return;
				}
				
			}
			
			
			/** Fin des commandes spéciales pour Asterion **/
				else
				{
					SocketManager.GAME_SEND_MESSAGE(_perso, "<b>Commande innexistante ou désactivée</b>", Ancestra.COLOR_BLEU2);//Le socket affiche le message ci-contre
					return;
				}
				}//Fin de la mise en place des pré-commande (.)
				
				
		
				//
				//
				//
				//
				//
				//
				//
				//
				//
				//Fin de la commande zobal |By Return alias Porky|
				
				
				
				
				if(_perso.get_fight() == null)
					SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(_perso.get_curCarte(), "", _perso.get_GUID(), _perso.get_name(), msg);
				else
					SocketManager.GAME_SEND_cMK_PACKET_TO_FIGHT(_perso.get_fight(), 7, "", _perso.get_GUID(), _perso.get_name(), msg);
			break;
			
		case '$'://Canal groupe
				if(!_perso.get_canaux().contains(packet.charAt(2)+""))return;
				if(_perso.getGroup() == null)break;
				msg = packet.split("\\|",2)[1];
				SocketManager.GAME_SEND_cMK_PACKET_TO_GROUP(_perso.getGroup(), "$", _perso.get_GUID(), _perso.get_name(), msg);
				
				
				break;
			
			case ':'://Canal commerce
				if(!_perso.get_canaux().contains(packet.charAt(2)+""))return;
				long l;
				if((l = System.currentTimeMillis() - _timeLastTradeMsg) < Ancestra.FLOOD_TIME)
				{
					l = (Ancestra.FLOOD_TIME  - l)/1000;//On calcul la différence en secondes
					SocketManager.GAME_SEND_Im_PACKET(_perso, "0115;"+((int)Math.ceil(l)+1));
					return;
				}
				_timeLastTradeMsg = System.currentTimeMillis();
				msg = packet.split("\\|",2)[1];
				SocketManager.GAME_SEND_cMK_PACKET_TO_ALL(":", _perso.get_GUID(), _perso.get_name(), msg);
				
			
			break;
			case '@'://Canal Admin
				if(_perso.get_compte().get_gmLvl() ==0)return;
				msg = packet.split("\\|",2)[1];
				SocketManager.GAME_SEND_cMK_PACKET_TO_ADMIN("@", _perso.get_GUID(), _perso.get_name(), msg);
			break;
			case '?'://Canal recrutement
				if(!_perso.get_canaux().contains(packet.charAt(2)+""))return;
				long j;
				if((j = System.currentTimeMillis() - _timeLastRecrutmentMsg) < Ancestra.FLOOD_TIME)
				{
					j = (Ancestra.FLOOD_TIME  - j)/1000;//On calcul la différence en secondes
					SocketManager.GAME_SEND_Im_PACKET(_perso, "0115;"+((int)Math.ceil(j)+1));
					return;
				}
				_timeLastRecrutmentMsg = System.currentTimeMillis();
				msg = packet.split("\\|",2)[1];
				SocketManager.GAME_SEND_cMK_PACKET_TO_ALL("?", _perso.get_GUID(), _perso.get_name(), msg);
			break;
			case '#'://Canal Equipe
				if(!_perso.get_canaux().contains(packet.charAt(2)+""))return;//Return a cote et execution en bas... Chépa o_o
				{
					msg = packet.split("\\|",2)[1];
					int team = _perso.get_fight().getTeamID(_perso.get_GUID());
					if(team == -1)return;
					SocketManager.GAME_SEND_cMK_PACKET_TO_FIGHT(_perso.get_fight(), team, "#", _perso.get_GUID(), _perso.get_name(), msg);
				
				}
					break;
			case '%'://Canal guilde
				if(!_perso.get_canaux().contains(packet.charAt(2)+""))return;
				if(_perso.get_guild() == null)return;
				msg = packet.split("\\|",2)[1];
				SocketManager.GAME_SEND_cMK_PACKET_TO_GUILD(_perso.get_guild(), "%", _perso.get_GUID(), _perso.get_name(), msg);
			break;
			case 0xC2://Canal 
			break;
			case '!'://Alignement
				if(!_perso.get_canaux().contains(packet.charAt(2)+""))return;
				if(_perso.get_align() == 0) return;
				if(_perso.getDeshonor() >= 1) 
				{
					SocketManager.GAME_SEND_Im_PACKET(_perso, "183");
					return;
				}
				long k;
				if((k = System.currentTimeMillis() - _timeLastAlignMsg) < Ancestra.FLOOD_TIME)
				{
					k = (Ancestra.FLOOD_TIME  - k)/1000;//On calcul la différence en secondes
					SocketManager.GAME_SEND_Im_PACKET(_perso, "0115;"+((int)Math.ceil(k)+1));
					return;
				}
				_timeLastAlignMsg = System.currentTimeMillis();
				msg = packet.split("\\|",2)[1];
				SocketManager.GAME_SEND_cMK_PACKET_TO_ALIGN("!", _perso.get_GUID(), _perso.get_name(), msg, _perso);
			break;
			case '^':// Canal Incarnam 
				msg = packet.split("\\|", 2)[1]; 
				long x; 
				if((x = System.currentTimeMillis() - _timeLastIncarnamMsg) < Ancestra.FLOOD_TIME) 
				{
					x = (Ancestra.FLOOD_TIME - x)/1000;//Calculamos a diferença em segundos 
					SocketManager.GAME_SEND_Im_PACKET(_perso, "0115;"+((int)Math.ceil(x)+1)); 
					return; 
				} 
				_timeLastIncarnamMsg = System.currentTimeMillis();
				msg = packet.split("\\|",2)[1];
				SocketManager.GAME_SEND_cMK_PACKET_INCARNAM_CHAT(_perso, "^", _perso.get_GUID(), _perso.get_name(), msg); 
			break;
			default:
				String nom = packet.substring(2).split("\\|")[0];
				msg = packet.split("\\|",2)[1];
				if(nom.length() <= 1)
					GameServer.addToLog("ChatHandler: Chanel non gere : "+nom);
				else
				{
					Personnage target = World.getPersoByName(nom);
					if(target == null)//si le personnage n'existe pas
					{
						SocketManager.GAME_SEND_CHAT_ERROR_PACKET(_out, nom);
						return;
					}
					if(target.get_compte() == null)
					{
						SocketManager.GAME_SEND_CHAT_ERROR_PACKET(_out, nom);
						return;
					}
					if(target.get_compte().getGameThread() == null)//si le perso n'est pas co
					{
						SocketManager.GAME_SEND_CHAT_ERROR_PACKET(_out, nom);
						return;
					}
					if(target.get_compte().isEnemyWith(_perso.get_compte().get_GUID()) == true || !target.isDispo(_perso))
					{
						SocketManager.GAME_SEND_Im_PACKET(_perso, "114;"+target.get_name());
						return;
					}
					SocketManager.GAME_SEND_cMK_PACKET(target, "F", _perso.get_GUID(), _perso.get_name(), msg);
					SocketManager.GAME_SEND_cMK_PACKET(_perso, "T", target.get_GUID(), target.get_name(), msg);
				}
			break;
		}
	}

	private void Basic_send_Date_Hour()
	{
		SocketManager.GAME_SEND_SERVER_DATE(_out);
		SocketManager.GAME_SEND_SERVER_HOUR(_out);
	}
	
	private void Basic_infosmessage(String packet)
	{
			packet = packet.substring(2);
			Personnage T = World.getPersoByName(packet);
			if(T == null) return;
			SocketManager.GAME_SEND_BWK(_perso, T.get_compte().get_pseudo()+"|1|"+T.get_name()+"|-1");
	}

	private void parseGamePacket(String packet)
	{
		switch(packet.charAt(1))
		{
			case 'A':
				if(_perso == null)
					return;
				parseGameActionPacket(packet);
			break;
			case 'C':
				if(_perso == null)
					return;
				_perso.sendGameCreate();
			break;
			case 'd': // demande de reciblage challenge
				Game_on_Gdi_packet(packet);
			case 'f':
				Game_on_showCase(packet);
			case 'I':
				Game_on_GI_packet();
			break;
			case 'K':
				Game_on_GK_packet(packet);
			break;
			case 'P'://PvP Toogle
				_perso.toggleWings(packet.charAt(2));
			break;
			case 'p':
				Game_on_ChangePlace_packet(packet);
			break;
			case 'Q':
				Game_onLeftFight(packet);
			break;
			case 'R':
				Game_on_Ready(packet);
			break;
			case 't':
				if(_perso.get_fight() == null)return;
				_perso.get_fight().playerPass(_perso);
			break;
		}
	}


	 
	
	private void Game_onLeftFight(String packet)
	{
		int targetID = -1;
		if(!packet.substring(2).isEmpty())
		{
			try
			{
				targetID = Integer.parseInt(packet.substring(2));
			}catch(Exception e){};
		}
		if(_perso.get_fight() == null)return;
		if(targetID > 0)//Expulsion d'un joueurs autre que soi-meme
		{
			Personnage target = World.getPersonnage(targetID);
			//On ne quitte pas un joueur qui : est null, ne combat pas, n'est pas de ça team.
			if(target == null || target.get_fight() == null || target.get_fight().getTeamID(target.get_GUID()) != _perso.get_fight().getTeamID(_perso.get_GUID()))return;
			_perso.get_fight().leftFight(_perso, target);
			
		}else
		{
			_perso.get_fight().leftFight(_perso, null);
		}
	}

	private void Game_on_showCase(String packet)
	{
		if(_perso == null)return;
		if(_perso.get_fight() == null)return;
		if(_perso.get_fight().get_state() != Constants.FIGHT_STATE_ACTIVE)return;
		int cellID = -1;
		try
		{
			cellID = Integer.parseInt(packet.substring(2));
		}catch(Exception e){};
		if(cellID == -1)return;
		_perso.get_fight().showCaseToTeam(_perso.get_GUID(),cellID);
	}

	private void Game_on_Ready(String packet)
	{
		if(_perso.get_fight() == null)return;
		_perso.get_fight().ticMyTimer();
		if(_perso.get_fight().get_state() != Constants.FIGHT_STATE_PLACE)return;
		_perso.set_ready(packet.substring(2).equalsIgnoreCase("1"));
		_perso.get_fight().verifIfAllReady();
		SocketManager.GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(_perso.get_fight(),3,_perso.get_GUID(),packet.substring(2).equalsIgnoreCase("1"));
	}

	private void Game_on_ChangePlace_packet(String packet)
	{
		if(_perso.get_fight() == null)return;
		_perso.get_fight().ticMyTimer();
		try
		{
			int cell = Integer.parseInt(packet.substring(2));
			_perso.get_fight().changePlace( _perso, cell);
		}catch(NumberFormatException e){return;};
	}
	
	private void Game_on_Gdi_packet(String packet)
	{
		int chalID = 0;
		chalID = Integer.parseInt(packet.split("i")[1]);
		if(chalID != 0 && _perso.get_fight() != null) {
			 Fight fight = _perso.get_fight();
			 if(fight.get_challenges().containsKey(chalID))
				 fight.get_challenges().get(chalID).show_cibleToPerso(_perso);
		}
			
	}

	private void Game_on_GK_packet(String packet)
	{	
		int GameActionId = -1;
		String[] infos = packet.substring(3).split("\\|");
		try
		{
			GameActionId = Integer.parseInt(infos[0]);
		}catch(Exception e){return;};
		if(GameActionId == -1)return;
		GameAction GA = _actions.get(GameActionId);
		if(GA == null)return;
		boolean isOk = packet.charAt(2) == 'K';
		
		switch(GA._actionID)
		{
			case 1://Deplacement
				if(isOk)
				{
					//Hors Combat
					if(_perso.get_fight() == null)
					{
						_perso.get_curCell().removePlayer(_perso.get_GUID());
						SocketManager.GAME_SEND_BN(_out);
						String path = GA._args;
						//On prend la case ciblée
						Case nextCell = _perso.get_curCarte().getCase(CryptManager.cellCode_To_ID(path.substring(path.length()-2)));
						Case targetCell = _perso.get_curCarte().getCase(CryptManager.cellCode_To_ID(GA._packet.substring(GA._packet.length()-2)));
						
						//On définie la case et on ajoute le personnage sur la case
						_perso.set_curCell(nextCell);
						_perso.set_orientation(CryptManager.getIntByHashedValue(path.charAt(path.length()-3)));
						_perso.get_curCell().addPerso(_perso);
						if(!_perso._isGhosts) _perso.set_away(false);
						
						if(targetCell.getObject() != null)
						{
							//Si c'est une "borne" comme Emotes, ou Création guilde
							if(targetCell.getObject().getID() == 1324)
							{
								Constants.applyPlotIOAction(_perso,_perso.get_curCarte().get_id(),targetCell.getID());
							}
							//Statues phoenix
							else if(targetCell.getObject().getID() == 542)
							{
								if(_perso._isGhosts) _perso.set_Alive();
							}
						}
						_perso.get_curCarte().onPlayerArriveOnCell(_perso,_perso.get_curCell().getID());
					}
					else//En combat
					{
						_perso.get_fight().onGK(_perso);
						return;
					}
					
				}
				else
				{
					//Si le joueur s'arrete sur une case
					int newCellID = -1;
					try
					{
						newCellID = Integer.parseInt(infos[1]);
					}catch(Exception e){return;};
					if(newCellID == -1)return;
					String path = GA._args;
					_perso.get_curCell().removePlayer(_perso.get_GUID());
					_perso.set_curCell(_perso.get_curCarte().getCase(newCellID));
					_perso.set_orientation(CryptManager.getIntByHashedValue(path.charAt(path.length()-3)));
					_perso.get_curCell().addPerso(_perso);
					SocketManager.GAME_SEND_BN(_out);
				}
			break;
			
			case 500://Action Sur Map
				_perso.finishActionOnCell(GA);
			break;

		}
		removeAction(GA);
	}

	private void Game_on_GI_packet() 
	{
		Carte mapa = _perso.get_curCarte();
		if (_perso.get_fight() != null) {
			// Only percepteur
			SocketManager.GAME_SEND_MAP_GMS_PACKETS(mapa, _perso);// GM|+
			SocketManager.GAME_SEND_GDK_PACKET(_out);// GDK
			return;
   }
	
		if(_perso.get_fight() != null)
		{
			//Only percepteur
			SocketManager.GAME_SEND_MAP_GMS_PACKETS(_perso.get_curCarte(), _perso);
			SocketManager.GAME_SEND_GDK_PACKET(_out);
			return;
		}
		//Enclos
		SocketManager.GAME_SEND_Rp_PACKET(_perso, _perso.get_curCarte().getMountPark());
		//Maisons
		House.LoadHouse(_perso, _perso.get_curCarte().get_id());
		//Objets sur la carte
		SocketManager.GAME_SEND_MAP_GMS_PACKETS(_perso.get_curCarte(), _perso);
		SocketManager.GAME_SEND_MAP_MOBS_GMS_PACKETS(_perso.get_compte().getGameThread().get_out(), _perso.get_curCarte());
		SocketManager.GAME_SEND_MAP_NPCS_GMS_PACKETS(_perso,_perso.get_curCarte());
		SocketManager.GAME_SEND_MAP_PERCO_GMS_PACKETS(_out,_perso.get_curCarte());
		SocketManager.GAME_SEND_MAP_OBJECTS_GDS_PACKETS(_out,_perso.get_curCarte());
		SocketManager.GAME_SEND_GDK_PACKET(_out);
		SocketManager.GAME_SEND_MAP_FIGHT_COUNT(_out, _perso.get_curCarte());
		SocketManager.ENVIAR_GM_PRISMAS_EN_MAPA(_out, mapa);
		SocketManager.GAME_SEND_MERCHANT_LIST(_perso, _perso.get_curCarte().get_id());
		//Les drapeau de combats
		Fight.FightStateAddFlag(_perso.get_curCarte(), _perso);
		//items au sol
		_perso.get_curCarte().sendFloorItems(_perso);
	}

	private void parseGameActionPacket(String packet)
	{
		int actionID;
		try
		{
			actionID = Integer.parseInt(packet.substring(2,5));
		}catch(NumberFormatException e){return;};
		
		int nextGameActionID = 0;
		if(_actions.size() > 0)
		{
			//On prend le plus haut GameActionID + 1
			nextGameActionID = (Integer)(_actions.keySet().toArray()[_actions.size()-1])+1;
		}
		GameAction GA = new GameAction(nextGameActionID,actionID,packet);
		
		switch(actionID)
		{
			case 1://Deplacement
				game_parseDeplacementPacket(GA);
			break;
			
			case 300://Sort
				game_tryCastSpell(packet);
			break;
			
			case 303://Attaque CaC
				game_tryCac(packet);
			break;
			
			case 500://Action Sur Map
				game_action(GA);
			break;
			
			case 507://Panneau intérieur de la maison
				house_action(packet);
			break;
			
			case 512:// usar prisma
				_perso.abrirMenuPrisma();
				break;
			
			case 618://Mariage oui
				_perso.setisOK(Integer.parseInt(packet.substring(5,6)));
				SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(_perso.get_curCarte(), "", _perso.get_GUID(), _perso.get_name(), "Oui");
				if(World.getMarried(0).getisOK() > 0 && World.getMarried(1).getisOK() > 0)
				{
					World.Wedding(World.getMarried(0), World.getMarried(1), 1);
				}
				if(World.getMarried(0) != null && World.getMarried(1) != null)
				{
					World.PriestRequest((World.getMarried(0)==_perso?World.getMarried(1):World.getMarried(0)), (World.getMarried(0)==_perso?World.getMarried(1).get_curCarte():World.getMarried(0).get_curCarte()), _perso.get_isTalkingWith());
				}
			break;
			case 619://Mariage non
				_perso.setisOK(0);
				SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(_perso.get_curCarte(), "", _perso.get_GUID(), _perso.get_name(), "Non");
				World.Wedding(World.getMarried(0), World.getMarried(1), 0);
			break;
			
			case 900://Demande Defie
				game_ask_duel(packet);
			break;
			case 901://Accepter Defie
				game_accept_duel(packet);
			break;
			case 902://Refus/Anuler Defie
				game_cancel_duel(packet);
			break;
			case 903://Rejoindre combat
				game_join_fight(packet);
			break;
			case 906://Agresser
				game_aggro(packet);
			break;
			case 909://Perco
				game_perco(packet);
			break;
		}	
	}

	private void house_action(String packet)
	{
		int actionID = Integer.parseInt(packet.substring(5));
		House h = _perso.getInHouse();
		if(h == null) return;
		switch(actionID)
		{
			case 81://Vérouiller maison
				h.Lock(_perso);
			break;
			case 97://Acheter maison
				h.BuyIt(_perso);
			break;
			case 98://Vendre
			case 108://Modifier prix de vente
				h.SellIt(_perso);
			break;
		}
	}
	
	
	private void game_perco(String packet)
	{
		try
		{
			if(_perso == null)return;
			if(_perso.get_fight() != null)return;
			if(_perso.get_isTalkingWith() != 0 ||
			   _perso.get_isTradingWith() != 0 ||
			   _perso.getCurJobAction() != null ||
			   _perso.get_curExchange() != null ||
			   _perso.is_away())
					{
						return;
					}
			int id = Integer.parseInt(packet.substring(5));
			Percepteur target = World.getPerco(id);
			if(target == null || target.get_inFight() > 0) return;
			if(target.get_Exchange())
			{
				
				SocketManager.GAME_SEND_Im_PACKET(_perso, "1180");
				return;
			}
			SocketManager.GAME_SEND_GA_PACKET_TO_MAP(_perso.get_curCarte(),"", 909, _perso.get_GUID()+"", id+"");
			_perso.get_curCarte().startFigthVersusPercepteur(_perso, target);
		}catch(Exception e){};
	}
	
	private void game_aggro(String packet)
	{
		try
		{
			if(_perso == null)return;
			if(_perso.get_fight() != null)return;
			int id = Integer.parseInt(packet.substring(5));
			Personnage target = World.getPersonnage(id);
			if(target == null || !target.isOnline() || target.get_fight() != null
			|| target.get_curCarte().get_id() != _perso.get_curCarte().get_id()
			|| target.get_align() == _perso.get_align()
			|| _perso.get_curCarte().get_placesStr().equalsIgnoreCase("|")
			|| !target.canAggro())
				return;
			if(target.whenILeaveFaction != -1)
			{
				if(System.currentTimeMillis() - (target.whenILeaveFaction+1000*60*10) < 0)
				{
					SocketManager.PACKET_POPUP_DEPART(_perso, "Vous ne pouvez pas aggresser un joueur sortant de prison");
					//Message à _perso comme quoi il peut pas l'aggro
					return;
				}else
				{
					target.whenILeaveFaction=-1;//on reinitialise
				}
			}
			if(target.get_align() == 0) 
			{
				_perso.setDeshonor(_perso.getDeshonor()+1);
				SocketManager.GAME_SEND_Im_PACKET(_perso, "084;1");
			}

			_perso.toggleWings('+');
			SocketManager.GAME_SEND_GA_PACKET_TO_MAP(_perso.get_curCarte(),"", 906, _perso.get_GUID()+"", id+"");
			_perso.get_curCarte().newFight(_perso, target, Constants.FIGHT_TYPE_AGRESSION);
		}catch(Exception e){};
	}

	private void game_action(GameAction GA)
	{
		String packet = GA._packet.substring(5);
		int cellID = -1;
		int actionID = -1;
		
		try
		{
			cellID = Integer.parseInt(packet.split(";")[0]);
			actionID = Integer.parseInt(packet.split(";")[1]);
		}catch(Exception e){}
		//Si packet invalide, ou cellule introuvable
		if(cellID == -1 || actionID == -1 || _perso == null || _perso.get_curCarte() == null ||
				_perso.get_curCarte().getCase(cellID) == null)
			return;
		GA._args = cellID+";"+actionID;
		_perso.get_compte().getGameThread().addAction(GA);
		_perso.startActionOnCell(GA);
	}

	private void game_tryCac(String packet)
	{
		try
		{
			if(_perso.get_fight() == null || _perso.get_fight().getFighterByPerso(_perso) == null)return;
		    //if(!_perso.get_fight().tryStopTurnAndPass(_perso))return;
			int cellID = -1;
			try
			{
				cellID = Integer.parseInt(packet.substring(5));
			}catch(Exception e){return;};
			
			_perso.get_fight().tryCaC(_perso,cellID);
		}catch(Exception e){};
	}

	private void game_tryCastSpell(String packet)
	{
		if(_perso.get_fight() == null || _perso.get_fight().getFighterByPerso(_perso) == null)return;
		//if(!_perso.get_fight().tryStopTurnAndPass(_perso))return;
		try
		{
			String[] splt = packet.split(";");
			int spellID = Integer.parseInt(splt[0].substring(5));
			int caseID = Integer.parseInt(splt[1]);
			if(_perso.get_fight() != null && _perso.get_fight().getFighterByPerso(_perso) != null && _perso.get_fight().getFighterByPerso(_perso).canPlay())
			{
				SortStats SS = _perso.getSortStatBySortIfHas(spellID);
				if(SS == null)return;
				_perso.get_fight().tryCastSpell(_perso.get_fight().getFighterByPerso(_perso),SS,caseID);
			}
		}catch(NumberFormatException e){return;};
	}

	private void game_join_fight(String packet)
    {
            if(_perso.get_fight() != null)
                    return;
            
            String[] infos = packet.substring(5).split(";");
            if(infos.length == 1)
            {
                    try
                    {
                            Fight F = _perso.get_curCarte().getFight(Integer.parseInt(infos[0]));
                            F.joinAsSpect(_perso);
                    }catch(Exception e){return;};
            }
            else
            {
                    try
                    {
                            int guid = Integer.parseInt(infos[1]);
                            if(_perso.is_away())
                            {
                                    SocketManager.GAME_SEND_GA903_ERROR_PACKET(_out,'o',guid);
                                    return;
                            }
                            if(World.getPersonnage(guid) == null)return;
                            World.getPersonnage(guid).get_fight().joinFight(_perso,guid);
                    }catch(Exception e){return;};
            }
    }

	private void game_accept_duel(String packet)
	{
		int guid = -1;
		try{guid = Integer.parseInt(packet.substring(5));}catch(NumberFormatException e){return;};
		if(_perso.get_duelID() != guid || _perso.get_duelID() == -1)return;
		SocketManager.GAME_SEND_MAP_START_DUEL_TO_MAP(_perso.get_curCarte(),_perso.get_duelID(),_perso.get_GUID());
		Fight fight = _perso.get_curCarte().newFight(World.getPersonnage(_perso.get_duelID()),_perso,Constants.FIGHT_TYPE_CHALLENGE);
		_perso.set_fight(fight);
		World.getPersonnage(_perso.get_duelID()).set_fight(fight);
		
	}

	private void game_cancel_duel(String packet)
	{
		try
		{
			if(_perso.get_duelID() == -1)return;
			SocketManager.GAME_SEND_CANCEL_DUEL_TO_MAP(_perso.get_curCarte(),_perso.get_duelID(),_perso.get_GUID());
			World.getPersonnage(_perso.get_duelID()).set_away(false);
			World.getPersonnage(_perso.get_duelID()).set_duelID(-1);
			_perso.set_away(false);
			_perso.set_duelID(-1);	
		}catch(NumberFormatException e){return;};
	}

	private void game_ask_duel(String packet)
	{
		if(_perso.get_curCarte().get_placesStr().equalsIgnoreCase("|"))
		{
			SocketManager.GAME_SEND_DUEL_Y_AWAY(_out, _perso.get_GUID());
			return;
		}
		try
		{
			int guid = Integer.parseInt(packet.substring(5));
			if(_perso.is_away() || _perso.get_fight() != null){SocketManager.GAME_SEND_DUEL_Y_AWAY(_out, _perso.get_GUID());return;}
			Personnage Target = World.getPersonnage(guid);
			if(Target == null) return;
			if(Target.is_away() || Target.get_fight() != null || Target.get_curCarte().get_id() != _perso.get_curCarte().get_id()){SocketManager.GAME_SEND_DUEL_E_AWAY(_out, _perso.get_GUID());return;}
			_perso.set_duelID(guid);
			_perso.set_away(true);
			World.getPersonnage(guid).set_duelID(_perso.get_GUID());
			World.getPersonnage(guid).set_away(true);
			SocketManager.GAME_SEND_MAP_NEW_DUEL_TO_MAP(_perso.get_curCarte(),_perso.get_GUID(),guid);
		}catch(NumberFormatException e){return;}
	}

	private void game_parseDeplacementPacket(GameAction GA)
	{
		String path = GA._packet.substring(5);
		if(_perso.get_fight() == null)
		{
			if(_perso.getPodUsed() > _perso.getMaxPod())
			{
				SocketManager.GAME_SEND_Im_PACKET(_perso, "112");
				SocketManager.GAME_SEND_GA_PACKET(_out, "", "0", "", "");
				removeAction(GA);
				return;
			}
			AtomicReference<String> pathRef = new AtomicReference<String>(path);
			int result = Pathfinding.isValidPath(_perso.get_curCarte(),_perso.get_curCell().getID(),pathRef, null);
			
			//Si déplacement inutile
			if(result == 0)
			{
				SocketManager.GAME_SEND_GA_PACKET(_out, "", "0", "", "");
				removeAction(GA);
				return;
			}
			if(result != -1000 && result < 0)result = -result;
			
			//On prend en compte le nouveau path
			path = pathRef.get();
			//Si le path est invalide
			if(result == -1000)
			{
				GameServer.addToLog(_perso.get_name()+"("+_perso.get_GUID()+") Tentative de  deplacement avec un path invalide");
				path = CryptManager.getHashedValueByInt(_perso.get_orientation())+CryptManager.cellID_To_Code(_perso.get_curCell().getID());	
			}
			//On sauvegarde le path dans la variable
			GA._args = path;
			
			SocketManager.GAME_SEND_GA_PACKET_TO_MAP(_perso.get_curCarte(), ""+GA._id, 1, _perso.get_GUID()+"", "a"+CryptManager.cellID_To_Code(_perso.get_curCell().getID())+path);
			addAction(GA);
			if(_perso.isSitted())_perso.setSitted(false);
			_perso.set_away(true);
		}else
		{
			if(_perso.get_fight() == null || _perso.get_fight().getFighterByPerso(_perso) == null)return;
			//if(!_perso.get_fight().tryStopTurnAndPass(_perso))return;
			Fighter F = _perso.get_fight().getFighterByPerso(_perso);
			if(F == null||!F.canPlay())return;
			GA._args = path;
			_perso.get_fight().fighterDeplace(F,GA);
		}
	}

	public PrintWriter get_out() {
		return _out;
	}
	
	public void kick() {
        try {
            Ancestra.gameServer.delClient(this);
            if (_compte != null) {
                _compte.deconnexion();
            }
            if (!_s.isClosed()) {
                _s.close();
            }
            _in.close();
            _out.close();
            _t.interrupt();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        ;
        try {
            this.finalize();
        } catch (Throwable ex) {
        }
    }

	private void parseAccountPacket(String packet)
	{
		switch(packet.charAt(1))
		{
			case 'A':
				String[] infos = packet.substring(2).split("\\|");
				if(SQLManager.persoExist(infos[0]))
				{
					SocketManager.GAME_SEND_NAME_ALREADY_EXIST(_out);
					return;
				}
				//Validation du nom du personnage
				boolean isValid = true;
				String name = infos[0].toLowerCase();
				//Vérifie d'abord si il contient des termes définit
				if(name.length() > 20
						|| name.contains("mj")
						|| name.contains("modo")
						|| name.contains("admin"))
				{
					isValid = false;
				}
				//Si le nom passe le test, on vérifie que les caractère entré sont correct.
				if(isValid)
				{
					int tiretCount = 0;
					char exLetterA = ' ';
					char exLetterB = ' ';
					for(char curLetter : name.toCharArray())
					{
						if(!((curLetter >= 'a' && curLetter <= 'z') || curLetter == '-'))
						{
							isValid = false;
							break;
						}
						if(curLetter == exLetterA && curLetter == exLetterB)
						{
							isValid = false;
							break;
						}
						if(curLetter >= 'a' && curLetter <= 'z')
						{
							exLetterA = exLetterB;
							exLetterB = curLetter;
						}
						if(curLetter == '-')
						{
							if(tiretCount >= 1)
							{
								isValid = false;
								break;
							}
							else
							{
								tiretCount++;
							}
						}
					}
				}
				//Si le nom est invalide
				if(!isValid)
				{
					SocketManager.GAME_SEND_NAME_ALREADY_EXIST(_out);
					return;
				}
				if(_compte.GET_PERSO_NUMBER() >= Ancestra.CONFIG_MAX_PERSOS)
				{
					SocketManager.GAME_SEND_CREATE_PERSO_FULL(_out);
					return;
				}
				if(_compte.createPerso(infos[0], Integer.parseInt(infos[2]), Integer.parseInt(infos[1]), Integer.parseInt(infos[3]),Integer.parseInt(infos[4]), Integer.parseInt(infos[5])))
				{
					SocketManager.GAME_SEND_CREATE_OK(_out);
					SocketManager.GAME_SEND_PERSO_LIST(_out, _compte.get_persos());
				}else
				{
					SocketManager.GAME_SEND_CREATE_FAILED(_out);
				}
				
			break;
			
			case 'B':
				//TODO: Caractéristiques 2.0 améliorés by Return
				if (Ancestra.CONFIG_ACTIVER_STATS_2)
				{
					int stat = -1;
		            try
		            {
		                stat = Integer.parseInt(packet.substring(2).split("/u000A")[0]); _perso.set_savestat(stat); //Rectification du bootstat avec Condition ||Variables||
		                SocketManager.GAME_SEND_KODE(_perso, "CK0|5"); //Sauvegarde du personnage une seule fois après stats |By Return|
		            }
		            catch(Exception  e)
		            {
		            	
		                return;
		            }
				}
				else
				{
					int stat = -1;
					try
					{
						stat = Integer.parseInt(packet.substring(2).split("/u000A")[0]);
						_perso.boostStat(stat);
					}catch(NumberFormatException e){return;};
				break;
				}
	            break;
			case 'g'://Cadeaux à la connexion
				int regalo = _compte.getCadeau();
				if (regalo != 0) {
					String idModObjeto = Integer.toString(regalo, 16);
					String efectos = World.getObjTemplate(regalo).getStrTemplate();
					SocketManager.GAME_SEND_Ag_PACKET(_out, regalo, "1~" + idModObjeto + "~1~~" + efectos);
				}
				break;
			case 'G':
				cuenta_Entregar_Regalo(packet.substring(2));
				break;
			case 'D':
				String[] split = packet.substring(2).split("\\|");
				int GUID = Integer.parseInt(split[0]);
				String reponse = split.length>1?split[1]:"";
				
				if(_compte.get_persos().containsKey(GUID))
				{
					if(_compte.get_persos().get(GUID).get_lvl() <20 ||(_compte.get_persos().get(GUID).get_lvl() >=20 && reponse.equals(_compte.get_reponse())))
					{
						_compte.deletePerso(GUID);
						SocketManager.GAME_SEND_PERSO_LIST(_out, _compte.get_persos());
					}
					else
						SocketManager.GAME_SEND_DELETE_PERSO_FAILED(_out);
				}else
					SocketManager.GAME_SEND_DELETE_PERSO_FAILED(_out);
			break;
			
			case 'f':
				int queueID = 1;
				int position = 1;
				SocketManager.MULTI_SEND_Af_PACKET(_out,position,1,1,""+1,queueID);
			break;
			
			case 'i':
				_compte.setClientKey(packet.substring(2));
			break;
			
			case 'L':
				SocketManager.GAME_SEND_PERSO_LIST(_out, _compte.get_persos());
				//SocketManager.GAME_SEND_HIDE_GENERATE_NAME(_out);
			break;
			
			case 'S':
				int charID = Integer.parseInt(packet.substring(2));
				if(_compte.get_persos().get(charID) != null)
				{
					_compte.setGameThread(this);
					_perso = _compte.get_persos().get(charID);
					if(_perso != null)
					{
						_perso.OnJoinGame();
						return;
					}
				}
				SocketManager.GAME_SEND_PERSO_SELECTION_FAILED(_out);
			break;
				
			case 'T':
				int guid = Integer.parseInt(packet.substring(2));
				_compte = Ancestra.gameServer.getWaitingCompte(guid);
				if(_compte != null)
				{
					String ip = _s.getInetAddress().getHostAddress();
					
					_compte.setGameThread(this);
					_compte.setCurIP(ip);
					Ancestra.gameServer.delWaitingCompte(_compte);
					SocketManager.GAME_SEND_ATTRIBUTE_SUCCESS(_out);
				}else
				{
					SocketManager.GAME_SEND_ATTRIBUTE_FAILED(_out);
				}
			break;
			
			case 'V':
				SocketManager.GAME_SEND_AV0(_out);
			break;
			
			case 'P':
				SocketManager.REALM_SEND_REQUIRED_APK(_out);
				break;
		}
	}
	
	private void cuenta_Entregar_Regalo(String packet) {
		String[] info = packet.split("\\|");
		int idObjeto = Integer.parseInt(info[0]);
		int idPj = Integer.parseInt(info[1]);
		Personnage pj = null;
		Objet objeto = null;
		try {
			pj = World.getPersonnage(idPj);
			objeto = World.getObjTemplate(idObjeto).createNewItem(1, true);
		} catch (Exception e) {}
		if (pj == null || objeto == null) {
			return;
		}
		pj.addObjet(objeto, false);
		World.addObjet(objeto, true);
		_compte.setCadeau();
		SQLManager.ACTUALIZAR_REGALO(_compte);
		SocketManager.GAME_SEND_AGK_PACKET(_out);
	}

	public Thread getThread()
	{
		return _t;
	}

	public void removeAction(GameAction GA)
	{
		//* DEBUG
		Ancestra.printDebug("Supression de la GameAction id = "+GA._id);
		//*/
		_actions.remove(GA._id);
	}
	
	public void addAction(GameAction GA)
	{
		_actions.put(GA._id, GA);
		//* DEBUG
		Ancestra.printDebug("Ajout de la GameAction id = "+GA._id);
		Ancestra.printDebug("Packet: "+GA._packet);
		//*/
	}
	
	private void Object_obvijevan_changeApparence(String packet)
	{
		int guid = -1;
		int pos = -1;
		int val = -1;
		try
		{
			guid = Integer.parseInt(packet.substring(2).split("\\|")[0]);
			pos = Integer.parseInt(packet.split("\\|")[1]);
			val = Integer.parseInt(packet.split("\\|")[2]); } catch (Exception e) {
				return;
			}if ((guid == -1) || (!_perso.hasItemGuid(guid))) return;
			Objet obj = World.getObjet(guid);
			if ((val >= 21) || (val <= 0)) return;
			
			obj.obvijevanChangeStat(972, val);
			SocketManager.send(_perso, obj.obvijevanOCO_Packet(pos));
			if (pos != -1) SocketManager.GAME_SEND_ON_EQUIP_ITEM(_perso.get_curCarte(), _perso);
	}
	private void Object_obvijevan_feed(String packet)
	{
		int guid = -1;
		int pos = -1;
		int victime = -1;
		try
		{
			guid = Integer.parseInt(packet.substring(2).split("\\|")[0]);
			pos = Integer.parseInt(packet.split("\\|")[1]);
			victime = Integer.parseInt(packet.split("\\|")[2]);
		} catch (Exception e) {return;}
		
		if ((guid == -1) || (!_perso.hasItemGuid(guid)))
			return;
		Objet obj = World.getObjet(guid);
		Objet objVictime = World.getObjet(victime);
		obj.obvijevanNourir(objVictime);
		
		int qua = objVictime.getQuantity();
		if (qua <= 1)
		{
			_perso.removeItem(objVictime.getGuid());
			SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_perso, objVictime.getGuid());
		} else {
			objVictime.setQuantity(qua - 1);
			SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso, objVictime);
		}
		SocketManager.send(_perso, obj.obvijevanOCO_Packet(pos));
	}
	
	private void Object_obvijevan_desassocier(String packet)
	{
		int guid = -1;
		int pos = -1;
		try
		{
			guid = Integer.parseInt(packet.substring(2).split("\\|")[0]);
			pos = Integer.parseInt(packet.split("\\|")[1]); } catch (Exception e) {
				return;
			}if ((guid == -1) || (!_perso.hasItemGuid(guid))) return;
			Objet obj = World.getObjet(guid);
			int idOBVI = 0;
			
			switch (obj.getTemplate().getType())
			{
			case 1:
				idOBVI = 9255;
				break;
			case 9:
				idOBVI = 9256;
				break;
			case 16:
				idOBVI = 9234;
				break;
			case 17:
				idOBVI = 9233;
				break;
			default:
				SocketManager.GAME_SEND_MESSAGE(_perso, "Erreur d'obvijevan numero: 4. Merci de nous le signaler si le probleme est grave.", "000000");
				return;
			}
			Objet.ObjTemplate t = World.getObjTemplate(idOBVI);
			Objet obV = t.createNewItem(1, true);
			String obviStats = obj.getObvijevanStatsOnly();
			if (obviStats == "") {
				SocketManager.GAME_SEND_MESSAGE(_perso, "Erreur d'obvijevan numero: 3. Merci de nous le signaler si le probleme est grave.", "000000");
				return;
			}
			obV.clearStats();
			obV.parseStringToStats(obviStats);
			if (_perso.addObjet(obV, true)) {
				World.addObjet(obV, true);
			}
			obj.removeAllObvijevanStats();
			SocketManager.send(_perso, obj.obvijevanOCO_Packet(pos));
			SocketManager.GAME_SEND_ON_EQUIP_ITEM(_perso.get_curCarte(), _perso);
	}
}
	/*
	private void Object_dissociate(String packet) {
		String[] infos = packet.substring(2).split("" + (char) 0x0A)[0].split("\\|");
		try {
			int guid = Integer.parseInt(infos[0]);
			Objet Obj = World.getObjet(guid);

			if (Obj.is_linked()) {
				Speaking Obv = Obj.get_linkedItem();
				Obj.set_unlinkedItem();
				Obv.set_unlinkedItem();
				_perso.addObjet(Obv);

				SQLManager.SAVE_PERSONNAGE(_perso, false);
				SQLManager.UPDATE_SPEAKING(Obv);

				SocketManager.GAME_SEND_OAKO_PACKET(_perso, Obv);
				SocketManager.GAME_SEND_OCO_PACKET(_perso, Obj);
				SocketManager.GAME_SEND_Ow_PACKET(_perso);

				_perso.refreshStats();
				if (_perso.getGroup() != null) {
					SocketManager.GAME_SEND_PM_MOD_PACKET_TO_GROUP(_perso.getGroup(), _perso);
				}
                                 _perso.resetAS();
				SocketManager.GAME_SEND_STATS_PACKET(_perso);
				SocketManager.GAME_SEND_ON_EQUIP_ITEM(_perso.get_curCarte(), _perso);

				//Si objet de panoplie
				if (Obj.getTemplate().getPanopID() > 0) {
					SocketManager.GAME_SEND_OS_PACKET(_perso, Obj.getTemplate().getPanopID());
				}
			}
		} catch (Exception e) {
			SocketManager.GAME_SEND_BN(_perso);
			return;
		}
	}

	private void Object_eat(String packet) {
		String[] infos = packet.substring(2).split("" + (char) 0x0A)[0].split("\\|");
		try {
			int guid = Integer.parseInt(infos[0]);
			int foodID = Integer.parseInt(infos[2]);
			Item Obj = World.getObjet(guid);
			Item Food = World.getObjet(foodID);

			Speaking Obv = Obj.get_linkedItem();

			if (Obv == null) {
				if (Ancestrar.CONFIG_DEBUG) {
					Ancestrar.printIn("Target Object null", true);
				}
				SocketManager.GAME_SEND_BN(_perso);
				return;
			}
			if (Food == null) {
				if (Ancestrar.CONFIG_DEBUG) {
					Ancestrar.printIn("Nourriture Object null", true);
				}
				SocketManager.GAME_SEND_BN(_perso);
				return;
			}
			if (Obj.getTemplate().getType() != Obv.getTemplate().get_obviType()) {
				if (Ancestrar.CONFIG_DEBUG) {
					Ancestrar.printIn("Mauvaise nourriture", true);
				}
				SocketManager.GAME_SEND_BN(_perso);
				return;
			}
			if (Obv.eatItem(_perso, Food)) {
				SQLManager.UPDATE_SPEAKING(Obv);//on save

				SocketManager.GAME_SEND_OCO_PACKET(_perso, Obj);//Update affichage (liï¿½ obligatoirement pour nourrir)

				//On envoit le reste (apparence)
				SocketManager.GAME_SEND_Ow_PACKET(_perso);
				_perso.refreshStats();
				if (_perso.getGroup() != null) {
					SocketManager.GAME_SEND_PM_MOD_PACKET_TO_GROUP(_perso.getGroup(), _perso);
				}
                                 _perso.resetAS();
				SocketManager.GAME_SEND_STATS_PACKET(_perso);
				SocketManager.GAME_SEND_ON_EQUIP_ITEM(_perso.get_curCarte(), _perso);

				//Si objet de panoplie
				if (Obj.getTemplate().getPanopID() > 0) {
					SocketManager.GAME_SEND_OS_PACKET(_perso, Obj.getTemplate().getPanopID());
				}
				SocketManager.GAME_SEND_BN(_perso);
			} else {
				if (Ancestrar.CONFIG_DEBUG) {
					Ancestrar.printIn("Ne peut pas ï¿½tre nourri", true);
				}
				SocketManager.GAME_SEND_BN(_perso);
				return;
			}
		} catch (Exception e) {
			SocketManager.GAME_SEND_BN(_perso);
			if (Ancestrar.CONFIG_DEBUG) {
				Ancestrar.printIn("Erreur globale: " + e.getMessage() + "& \n" + e.getCause(), true);
			}
			return;
		}
	}

	private void Object_ChangeSkin(String packet) {
		String[] infos = packet.substring(2).split("" + (char) 0x0A)[0].split("\\|");
		try {
			int guid = Integer.parseInt(infos[0]);
			int skinTarget = Integer.parseInt(infos[2]);
			Item Obj = World.getObjet(guid);
			Speaking Obv = null;
			if (Obj.isSpeaking()) {
				Obv = Speaking.toSpeaking(Obj);
			} else if (Obj.is_linked()) {
				Obv = Obj.get_linkedItem();
			}
			if (skinTarget < 0 || skinTarget > 20 || Obv == null || skinTarget > Obv.get_lvl() || skinTarget == Obv.get_selectedLevel()) {
				SocketManager.GAME_SEND_BN(_perso);
				return;
			}

			Obv.set_selectedLevel(skinTarget);
			SQLManager.UPDATE_SPEAKING(Obv);//on save

			if (Obj.is_linked()) {
				SocketManager.GAME_SEND_OCO_PACKET(_perso, Obj);//Update affichage si liï¿½
			} else {
				SocketManager.GAME_SEND_OCO_PACKET(_perso, Obv);//Update affichage si seul
			}
			//On envoit le reste (apparence)
			SocketManager.GAME_SEND_Ow_PACKET(_perso);
			_perso.refreshStats();
			if (_perso.getGroup() != null) {
				SocketManager.GAME_SEND_PM_MOD_PACKET_TO_GROUP(_perso.getGroup(), _perso);
			}
                         _perso.resetAS();
			SocketManager.GAME_SEND_STATS_PACKET(_perso);
			SocketManager.GAME_SEND_ON_EQUIP_ITEM(_perso.get_curCarte(), _perso);

			//Si objet de panoplie
			if (Obj.getTemplate().getPanopID() > 0) {
				SocketManager.GAME_SEND_OS_PACKET(_perso, Obj.getTemplate().getPanopID());
			}
			SocketManager.GAME_SEND_BN(_perso);
		} catch (Exception e) {
			SocketManager.GAME_SEND_BN(_perso);
			return;
		}
	}*/


	

