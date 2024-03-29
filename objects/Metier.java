package objects;

import game.GameServer;
import game.GameThread.GameAction;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.Timer;

import objects.Carte.Case;
import objects.Carte.InteractiveObject;
import objects.Objet.ObjTemplate;
import common.*;

public class Metier {

	public static class StatsMetier
	{
		private int _id;
		private Metier _template;
		private int _lvl;
		private long _xp;
		private ArrayList<JobAction> _posActions = new ArrayList<JobAction>();
		private boolean _isCheap = false;
		private boolean _freeOnFails = false;
		private boolean _noRessource = false;
		private JobAction _curAction;
		private int max_case = 2;
		
		public StatsMetier(int id,Metier tp,int lvl,long xp)
		{
			_id = id;
			_template = tp;
			_lvl = lvl;
			_xp = xp;
			_posActions = Constants.getPosActionsToJob(tp.getId(),lvl);
		}

		public int get_lvl() {
			return _lvl;
		}
		public boolean isCheap() {
			return _isCheap;
		}

		public void setIsCheap(boolean isCheap) {
			_isCheap = isCheap;
		}

		public boolean isFreeOnFails() {
			return _freeOnFails;
		}

		public void setFreeOnFails(boolean freeOnFails) {
			_freeOnFails = freeOnFails;
		}

		public boolean isNoRessource() {
			return _noRessource;
		}

		public void setNoRessource(boolean noRessource) {
			_noRessource = noRessource;
		}

		public void levelUp(Personnage P,boolean send)
		{
			_lvl++;
			_posActions = Constants.getPosActionsToJob(_template.getId(),_lvl);
			
			if(send)
			{
				//on cr�er la listes des statsMetier a envoyer (Seulement celle ci)
				ArrayList<StatsMetier> list = new ArrayList<StatsMetier>();
				list.add(this);
				SocketManager.GAME_SEND_JS_PACKET(P, list);
				SocketManager.GAME_SEND_STATS_PACKET(P);
				SocketManager.GAME_SEND_Ow_PACKET(P);
				SocketManager.GAME_SEND_JN_PACKET(P,_template.getId(),_lvl);
				SocketManager.GAME_SEND_JO_PACKET(P, list);
			}
		}
		public String parseJS()
		{
			StringBuilder str = new StringBuilder();
			str.append("|").append(_template.getId()).append(";");
			boolean first = true;
			for(JobAction JA : _posActions)
			{
				if(!first)str.append(",");
				else first = false;
				str.append(JA.getSkillID()).append("~").append(JA.getMin()).append("~");
				if(JA.isCraft())str.append("0~0~").append(JA.getChance());
				else str.append(JA.getMax()).append("~0~").append(JA.getTime());
			}
			return str.toString();
		}
		public long getXp()
		{
			return _xp;
		}
		
		public void startAction(int id,Personnage P,InteractiveObject IO,GameAction GA,Case cell)
		{
			for(JobAction JA : _posActions)
			{
				if(JA.getSkillID() == id)
				{
					_curAction = JA;
					JA.startAction(P,IO,GA,cell);
					return;
				}
			}
		}
		
		public void endAction(int id,Personnage P,InteractiveObject IO,GameAction GA,Case cell)
		{
			if(_curAction == null)return;
			_curAction.endAction(P,IO,GA,cell);
			addXp(P,_curAction.getXpWin()*Ancestra.XP_METIER);
			//Packet JX
			//on cr�er la listes des statsMetier a envoyer (Seulement celle ci)
			ArrayList<StatsMetier> list = new ArrayList<StatsMetier>();
			list.add(this);
			SocketManager.GAME_SEND_JX_PACKET(P, list);
		}
		
		public void addXp(Personnage P,long xp)
		{
			if(_lvl >99)return;
			int exLvl = _lvl;
			_xp += xp;
			
			//Si l'xp d�passe le pallier du niveau suivant
			while(_xp >= World.getExpLevel(_lvl+1).metier && _lvl <100)
				levelUp(P,false);
			
			//s'il y a eu Up
			if(_lvl > exLvl && P.isOnline())
			{
				//on cr�er la listes des statsMetier a envoyer (Seulement celle ci)
				ArrayList<StatsMetier> list = new ArrayList<StatsMetier>();
				list.add(this);
				//on envoie le packet
				SocketManager.GAME_SEND_JS_PACKET(P, list);
				SocketManager.GAME_SEND_JN_PACKET(P,_template.getId(),_lvl);
				SocketManager.GAME_SEND_STATS_PACKET(P);
				SocketManager.GAME_SEND_Ow_PACKET(P);
				SocketManager.GAME_SEND_JO_PACKET(P, list);
			}
		}
		
		public String getXpString(String s)
		{
			StringBuilder str = new StringBuilder();
			str.append( World.getExpLevel(_lvl).metier).append(s);
			str.append(_xp).append(s);
			str.append(World.getExpLevel((_lvl<100?_lvl+1:_lvl)).metier);
			return str.toString();
		}
		public Metier getTemplate() {
			
			return _template;
		}

		public int getOptBinValue()
		{
			int nbr = 0;
			nbr += (_isCheap?1:0);
			nbr += (_freeOnFails?2:0);
			nbr += (_noRessource?4:0);
			return nbr;
		}
		
		public boolean isValidMapAction(int id)
		{
			for(JobAction JA : _posActions)if(JA.getSkillID() == id) return true;
			return false;
		}
		
		public void setOptBinValue(int bin)
		{
			_isCheap = false;
			_freeOnFails = false;
			_noRessource = false;
			
			if(bin - 4 >=0)
			{
				bin -= 4;
				_isCheap = true;
			}
			if(bin - 2 >=0)
			{
				bin -=2;
				_freeOnFails = true;
			}
			if(bin - 1 >= 0)
			{
				bin -= 1;
				_noRessource = true;
			}
		}

		public int getID()
		{
			return _id;
		}

		/* Livre de m�tiers
		 * Par Taparisse
		 * Aid� par Nestrya !
		 */ 
		
		public void set_o(String[] pp) {
			Ancestra.printDebug("pp2 :" + pp[1]);
			Ancestra.printDebug("pp3 :" + pp[2]);
			setOptBinValue(Integer.parseInt(pp[1]));
			int cm = Constants.getTotalCaseByJobLevel(_lvl);
			if(cm <= Integer.parseInt(pp[2]))
				this.max_case = cm;
			max_case = Integer.parseInt(pp[2]);
		}
		
		public String parsetoJO(){
			return _id+"|"+getOptBinValue()+"|"+max_case;
		}
		
		public int getmaxcase() {
			return max_case;
		}
	}
	
	public static class JobAction
	{
		private int _skID;
		private int _min = 1;
		private int _max = 1;
		private boolean _isCraft;
		private int _chan = 100;
		private int _time = 0;
		private int _xpWin = 0;
		private long _startTime;
		private Map<Integer,Integer> _ingredients = new TreeMap<Integer,Integer>();
		private Map<Integer,Integer> _lastCraft = new TreeMap<Integer,Integer>();
		private Timer _craftTimer;
		private Personnage _P;
		
		public JobAction(int sk,int min, int max,boolean craft, int arg,int xpWin)
		{
			_skID = sk;
			_min = min;
			_max = max;
			_isCraft = craft;
			if(craft)_chan = arg;
			else _time = arg;
			_xpWin = xpWin;
			
			_craftTimer = new Timer(100,new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					craft();
					_craftTimer.stop();
				}
			});
		}
		
		public void endAction(Personnage P, InteractiveObject IO, GameAction GA,Case cell)
		{
			if(!_isCraft)
			{
				//Si recue trop tot, on ignore
				if(_startTime - System.currentTimeMillis() > 500)return;
				IO.setState(Constants.IOBJECT_STATE_EMPTY);
				IO.startTimer();
				//Packet GDF (changement d'�tat de l'IO)
				SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.get_curCarte(), cell);
				
				boolean special = Formulas.getRandomValue(0, 99)==0;//Restriction de niveau ou pas ?
				
				//On ajoute X ressources
				int qua = (_max>_min?Formulas.getRandomValue(_min, _max):_min);
				int tID = Constants.getObjectByJobSkill(_skID,special);
								
				ObjTemplate T = World.getObjTemplate(tID);
				if(T == null)return;
				Objet O = T.createNewItem(qua, false);
				//Si retourne true, on l'ajoute au monde
				if(P.addObjet(O, true))
					World.addObjet(O, true);
				SocketManager.GAME_SEND_IQ_PACKET(P,P.get_GUID(),qua);
				SocketManager.GAME_SEND_Ow_PACKET(P);
			}
		}

		public void startAction(Personnage P, InteractiveObject IO, GameAction GA,Case cell)
		{
			_P = P;
			if(!_isCraft)
			{
				IO.setInteractive(false);
				IO.setState(Constants.IOBJECT_STATE_EMPTYING);
				SocketManager.GAME_SEND_GA_PACKET_TO_MAP(P.get_curCarte(),""+GA._id, 501, P.get_GUID()+"", cell.getID()+","+_time);
				SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.get_curCarte(),cell);
				_startTime = System.currentTimeMillis()+_time;//pour eviter le cheat
			}else
			{
				P.set_away(true);
				IO.setState(Constants.IOBJECT_STATE_EMPTYING);//FIXME trouver la bonne valeur
				P.setCurJobAction(this);
				SocketManager.GAME_SEND_ECK_PACKET(P, 3, _min+";"+_skID);//_min => Nbr de Case de l'interface
				SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.get_curCarte(), cell);
			}
		}

		public int getSkillID()
		{
			return _skID;
		}
		public int getMin()
		{
			return _min;
		}
		public int getXpWin()
		{
			return _xpWin;
		}
		public int getMax()
		{
			return _max;
		}
		public int getChance()
		{
			return _chan;
		}
		public int getTime()
		{
			return _time;
		}
		public boolean isCraft()
		{
			return _isCraft;
		}
		
		public void modifIngredient(Personnage P,int guid, int qua)
		{
			//on prend l'ancienne valeur
			int q = _ingredients.get(guid)==null?0:_ingredients.get(guid);
			//on enleve l'entr�e dans la Map
			_ingredients.remove(guid);
			//on ajoute (ou retire, en fct du signe) X objet
			q += qua;
			if(q > 0)
			{
				_ingredients.put(guid,q);
				SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(P,'O', "+", guid+"|"+q);
			}else SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(P,'O', "-", guid+"");
		}

		public void craft() {
			if (!_isCraft) {
				return;
			}
                        if (_P.isOnline())_P.get_compte().openQueue();
			boolean signed = false;//TODO
			
			//Si Forgemagie
			if (_skID == 1
					|| _skID == 113
					|| _skID == 115
					|| _skID == 116
					|| _skID == 117
					|| _skID == 118
					|| _skID == 119
					|| _skID == 120
					|| (_skID >= 163 && _skID <= 169)) {
				doFmCraft();
				return;
			}
                        else
                        {
                            try {
				Thread.sleep(750);
                            } catch (Exception e) {
                            };
                        }

			Map<Integer, Integer> items = new TreeMap<Integer, Integer>();
			//on retire les items mis en ingr�dients
			for (Entry<Integer, Integer> e : _ingredients.entrySet()) {
				//Si le joueur n'a pas l'objet
				if (!_P.hasItemGuid(e.getKey())) {
					SocketManager.GAME_SEND_Ec_PACKET(_P, "EI");
					GameServer.addToLog("/!\\ " + _P.get_name() + " essaye de crafter avec un objet qu'il n'a pas");
					if(_P.isOnline())_P.get_compte().flushQueue(); return;
				}
				//Si l'objet n'existe pas
				Objet obj = World.getObjet(e.getKey());
				if (obj == null) {
					SocketManager.GAME_SEND_Ec_PACKET(_P, "EI");
					GameServer.addToLog("/!\\ " + _P.get_name() + " essaye de crafter avec un objet qui n'existe pas");
					if(_P.isOnline())_P.get_compte().flushQueue(); return;
				}
				//Si la quantit� est trop faible
				if (obj.getQuantity() < e.getValue()) {
					SocketManager.GAME_SEND_Ec_PACKET(_P, "EI");
					GameServer.addToLog("/!\\ " + _P.get_name() + " essaye de crafter avec un objet dont la quantite est trop faible");
					if(_P.isOnline())_P.get_compte().flushQueue(); return;
				}
				//On calcule la nouvelle quantit�
				int newQua = obj.getQuantity() - e.getValue();

				if (newQua < 0) {
					if(_P.isOnline())_P.get_compte().flushQueue(); return;//ne devrais pas arriver
				}
				if (newQua == 0) {
					_P.removeItem(e.getKey());
					World.removeItem(e.getKey());
					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_P, e.getKey());
				} else {
					obj.setQuantity(newQua);
					SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_P, obj);
				}
				//on ajoute le couple tID/qua a la liste des ingr�dients pour la recherche
				items.put(obj.getTemplate().getID(), e.getValue());
			}
			//On retire les items a ignorer pour la recette
			//Rune de signature
			if (items.containsKey(7508)) {
				signed = true;
			}
			items.remove(7508);
			//Fin des items a retirer
			SocketManager.GAME_SEND_Ow_PACKET(_P);

			//On trouve le template corespondant si existant
			StatsMetier SM = _P.getMetierBySkill(_skID);
			int tID = World.getObjectByIngredientForJob(SM.getTemplate().getListBySkill(_skID), items);

			//Recette non existante ou pas adapt� au m�tier
			if (tID == -1 || !SM.getTemplate().canCraft(_skID, tID)) {
				SocketManager.GAME_SEND_Ec_PACKET(_P, "EI");
				SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.get_curCarte(), _P.get_GUID(), "-");
				_ingredients.clear();

				if(_P.isOnline())_P.get_compte().flushQueue(); return;
			}

			int chan = Constants.getChanceByNbrCaseByLvl(SM.get_lvl(), _ingredients.size());
			int jet = Formulas.getRandomValue(1, 100);
			boolean success = chan >= jet;

			if (!success)//Si echec
			{
				SocketManager.GAME_SEND_Ec_PACKET(_P, "EF");
				SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.get_curCarte(), _P.get_GUID(), "-" + tID);
				SocketManager.GAME_SEND_Im_PACKET(_P, "0118");
			} else {
				Objet newObj = World.getObjTemplate(tID).createNewItem(1, false);
				//Si sign� on ajoute la ligne de Stat "Fabriqu� par:"
				if (signed) {
					newObj.addTxtStat(988, _P.get_name());
				}
				boolean add = true;
				int guid = newObj.getGuid();

				for (Entry<Integer, Objet> entry : _P.getItems().entrySet()) {
					Objet obj = entry.getValue();
					if (obj.getTemplate().getID() == newObj.getTemplate().getID()
							&& obj.getStats().isSameStats(newObj.getStats())
							&& obj.getPosition() == Constants.ITEM_POS_NO_EQUIPED)//Si meme Template et Memes Stats et Objet non �quip�
					{
						obj.setQuantity(obj.getQuantity() + newObj.getQuantity());//On ajoute QUA item a la quantit� de l'objet existant
						SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_P, obj);
						add = false;
						guid = obj.getGuid();
					}
				}
				if (add) {
					_P.getItems().put(newObj.getGuid(), newObj);
					SocketManager.GAME_SEND_OAKO_PACKET(_P, newObj);
					World.addObjet(newObj, true);
				}

				//on envoie les Packets
				SocketManager.GAME_SEND_Ow_PACKET(_P);
				SocketManager.GAME_SEND_Em_PACKET(_P, ((new StringBuilder("KO")).append(guid).append("|1|").append(tID).append("|").append(newObj.parseStatsString())).toString().replace(";", "#"));
				SocketManager.GAME_SEND_Ec_PACKET(_P, "K;" + tID);
				SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.get_curCarte(), _P.get_GUID(), "+" + tID);
			}


			//On donne l'xp
			int winXP = Constants.calculXpWinCraft(SM.get_lvl(), _ingredients.size()) * Ancestra.XP_METIER;
			if (success) {
				SM.addXp(_P, winXP);
				ArrayList<StatsMetier> SMs = new ArrayList<StatsMetier>();
				SMs.add(SM);
				SocketManager.GAME_SEND_JX_PACKET(_P, SMs);
			}

			_lastCraft.clear();
			_lastCraft.putAll(_ingredients);
			_ingredients.clear();
                        if(_P.isOnline())_P.get_compte().flushQueue();
			//*/
		}
		
		private void doFmCraft() {
			boolean signed = false;
			Objet obj = null, sign = null, mod = null;// sign = Rune de
														// signature, mod: rune
														// ou Potion, obj :
														// objet modif�
			int isElementChanging = 0, stat = -1, isStatsChanging = 0, add = 0;
			int poid = 0;
			String stats = "-1";
			for (int guid : _ingredients.keySet()) {
				Objet ing = World.getObjet(guid);
				if (!_P.hasItemGuid(guid) || ing == null) {
					SocketManager.GAME_SEND_Ec_PACKET(_P, "EI");
					SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.get_curCarte(),
							_P.get_GUID(), "-");
					_ingredients.clear();
					return;
				}
				int id = ing.getTemplate().getID();
				switch (id) {

				// Potions
				case 1333:// Potion Etincelle
					stat = 99;
					isElementChanging = ing.getTemplate().getLevel();
					mod = ing;
					break;
				case 1335:// Potion crachin
					stat = 96;
					isElementChanging = ing.getTemplate().getLevel();
					mod = ing;
					break;
				case 1337:// Potion de courant d'air
					stat = 98;
					isElementChanging = ing.getTemplate().getLevel();
					mod = ing;
					break;
				case 1338:// Potion de secousse
					stat = 97;
					isElementChanging = ing.getTemplate().getLevel();
					mod = ing;
					break;
				case 1340:// Potion d'eboulement
					stat = 97;
					isElementChanging = ing.getTemplate().getLevel();
					mod = ing;
					break;
				case 1341:// Potion Averse
					stat = 96;
					isElementChanging = ing.getTemplate().getLevel();
					mod = ing;
					break;
				case 1342:// Potion de rafale
					stat = 98;
					isElementChanging = ing.getTemplate().getLevel();
					mod = ing;
					break;
				case 1343:// Potion de Flamb�e
					stat = 99;
					isElementChanging = ing.getTemplate().getLevel();
					mod = ing;
					break;
				case 1345:// Potion Incendie
					stat = 99;
					isElementChanging = ing.getTemplate().getLevel();
					mod = ing;
					break;
				case 1346:// Potion Tsunami
					stat = 96;
					isElementChanging = ing.getTemplate().getLevel();
					mod = ing;
					break;
				case 1347:// Potion Ouragan
					stat = 98;
					isElementChanging = ing.getTemplate().getLevel();
					mod = ing;
					break;
				case 1348:// Potion de seisme
					stat = 97;
					isElementChanging = ing.getTemplate().getLevel();
					mod = ing;
					break;

				// Fin potions
				// Runes
				// Stats : Effect a changer dans les stats
				// add : Valeur a ajouter.
				// poid : Poid qui influs sur la chance.
				/*
				 * RUNES MANQUANTES : Rune Pa Prospe + 3 9
				 * 
				 * Rune Pa Ini + 30 3 Rune Ra Ini + 100 10
				 * 
				 * Rune Pa Do Per + 3 % 6 Rune Ra Do Per + 10 % 20
				 * 
				 * Rune Pa Pi + 3 45 Rune Ra Pi + 10 ?
				 * 
				 * Rune Pa Pi Per + 3 % 6 Rune Ra Pi Per + 10 % 20
				 */

				case 1519:// Force
					mod = ing;
					stats = "76";
					add = 1;
					poid = 1;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1521:// Sagesse
					mod = ing;
					stats = "7c";
					add = 1;
					poid = 3;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1522:// Intel
					mod = ing;
					stats = "7e";
					add = 1;
					poid = 1;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1523:// Vita
					mod = ing;
					stats = "7d";
					add = 3;
					poid = 1;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1524:// Agi
					mod = ing;
					stats = "77";
					add = 1;
					poid = 1;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1525:// Chance
					mod = ing;
					stats = "7b";
					add = 1;
					poid = 1;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1545:// Pa force
					mod = ing;
					stats = "77";
					add = 3;
					poid = 3;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1546:// Pa Sagesse
					mod = ing;
					stats = "7c";
					add = 3;
					poid = 9;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1547:// Pa Intel
					mod = ing;
					stats = "7e";
					add = 3;
					poid = 3;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1548:// Pa VI
					mod = ing;
					stats = "7d";
					add = 10;
					poid = 10;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1549:// Pa age
					mod = ing;
					stats = "77";
					add = 3;
					poid = 3;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1550:// Pa cha
					mod = ing;
					stats = "7b";
					add = 3;
					poid = 10;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1551:// Ra Fo
					mod = ing;
					stats = "76";
					add = 10;
					poid = 10;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1552:// Ra Sa
					mod = ing;
					stats = "7c";
					add = 10;
					poid = 30;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1553:// Ra Ine
					mod = ing;
					stats = "7e";
					add = 10;
					poid = 10;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1554:// Ra Vi
					mod = ing;
					stats = "7d";
					add = 30;
					poid = 10;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1555:// Ra Age
					mod = ing;
					stats = "77";
					add = 10;
					poid = 10;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1556:// Ra cha
					mod = ing;
					stats = "7b";
					add = 10;
					poid = 10;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1557:// Ga PA
					mod = ing;
					stats = "6f";
					add = 1;
					poid = 100;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 1558:// Ga PME
					mod = ing;
					stats = "80";
					add = 1;
					poid = 90;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7433:// Cri
					mod = ing;
					stats = "73";
					add = 1;
					poid = 30;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7434:// Soins
					mod = ing;
					stats = "b2";
					add = 1;
					poid = 20;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7435:// Dommages
					mod = ing;
					stats = "70";
					add = 1;
					poid = 20;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7436:// Domages %
					mod = ing;
					stats = "8a";
					add = 1;
					poid = 2;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7437:// Domage renvoy�
					mod = ing;
					stats = "dc";
					add = 1;
					poid = 2;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7438:// Porter
					mod = ing;
					stats = "75";
					add = 1;
					poid = 50;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7442:// Invoque
					mod = ing;
					stats = "b6";
					add = 1;
					poid = 30;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7443:// Pod
					mod = ing;
					stats = "9e";
					add = 10;
					poid = 1; // ?
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7444:// Pa pod
					mod = ing;
					stats = "9e";
					add = 30;
					poid = 1; // ?
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7445:// Ra pod
					mod = ing;
					stats = "9e";
					add = 100;
					poid = 1; // ?
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7446:// Pi�ge
					mod = ing;
					stats = "e1";
					add = 1;
					poid = 15;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7447:// Pi�ge %
					mod = ing;
					stats = "e2";
					add = 1;
					poid = 2;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7448:// Initiative
					mod = ing;
					stats = "ae";
					add = 10;
					poid = 1;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7449:// Pa Initiative
					mod = ing;
					stats = "ae";
					add = 30;
					poid = 3;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7450:// Ra Initiative
					mod = ing;
					stats = "ae";
					add = 100;
					poid = 10;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7451:// Prospec
					mod = ing;
					stats = "b0";
					add = 1;
					poid = 3;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7452:// R� Feu
					mod = ing;
					stats = "f3";
					add = 1;
					poid = 4;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7453:// R� Air
					mod = ing;
					stats = "f2";
					add = 1;
					poid = 4;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7454:// R� Eau
					mod = ing;
					stats = "f1";
					add = 1;
					poid = 4;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7455:// R� Terre
					mod = ing;
					stats = "f0";
					add = 1;
					poid = 4;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7456:// R� Neutre
					mod = ing;
					stats = "f4";
					add = 1;
					poid = 4;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7457:// R� % Feu
					mod = ing;
					stats = "d5";
					add = 1;
					poid = 5;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7458:// R� % Air
					mod = ing;
					stats = "d4";
					add = 1;
					poid = 5;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7459:// R� % Terre
					mod = ing;
					stats = "d2";
					add = 1;
					poid = 5;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7460:// R� % neutre
					mod = ing;
					stats = "d6";
					add = 1;
					poid = 5;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7560:// R� % Eau
					mod = ing;
					stats = "d3";
					add = 1;
					poid = 5;
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 8379:// Rune Vie
					mod = ing;
					// TODO : N'existe plus.
					isStatsChanging = ing.getTemplate().getLevel();
					break;
				case 7508:// Rune de signature
					signed = true;
					sign = ing;
					break;
				default:// Si pas runes ou popo, et qu'il a un cout en PA, alors
						// c'est une arme (une v�rification du type serait
						// pr�f�rable)
					if (ing.getTemplate().getPACost() > 0)
						obj = ing;
					if (ing.getTemplate().getType() == 1
							|| ing.getTemplate().getType() == 2
							|| ing.getTemplate().getType() == 3
							|| ing.getTemplate().getType() == 4
							|| ing.getTemplate().getType() == 5
							|| ing.getTemplate().getType() == 6
							|| ing.getTemplate().getType() == 7
							|| ing.getTemplate().getType() == 8
							|| ing.getTemplate().getType() == 9
							|| ing.getTemplate().getType() == 10
							|| ing.getTemplate().getType() == 11
							|| ing.getTemplate().getType() == 16
							|| ing.getTemplate().getType() == 17
							|| ing.getTemplate().getType() == 19
							|| ing.getTemplate().getType() == 20
							|| ing.getTemplate().getType() == 21
							|| ing.getTemplate().getType() == 22
							|| ing.getTemplate().getType() == 81
							|| ing.getTemplate().getType() == 102
							|| ing.getTemplate().getType() == 114)
						obj = ing;
					break;
				}
			}
			StatsMetier SM = _P.getMetierBySkill(_skID);

			if (SM == null || obj == null || mod == null) {
				SocketManager.GAME_SEND_Ec_PACKET(_P, "EI");
				SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.get_curCarte(),
						_P.get_GUID(), "-");
				_ingredients.clear();
				return;
			}
			if (((SM._lvl) * 2) < obj.getTemplate().getLevel()) {
				isElementChanging = 0;
				isStatsChanging = 0;
			}

			int chan = 80;
			int nivelOficio = SM.get_lvl();

		/*	if (isElementChanging > 0 && isStatsChanging == 0)// Si changement
																// d'�l�ment
			{
				chan = Formulas.calculElementChangeChance(SM.get_lvl(), obj
						.getTemplate().getLevel(), isElementChanging);
				// Min/max de 5% /95%
				if (chan > 100 - (SM.get_lvl() / 20))
					chan = 100 - (SM.get_lvl() / 20);
				if (chan < (SM.get_lvl() / 20))
					chan = (SM.get_lvl() / 20);
			} else*/ if (isStatsChanging > 0 && isElementChanging == 0)// Si
																		// changement
																		// de
																		// stats
			{
				int poidActual = 1;
				int ActualJet = 1;
				if (!obj.parseStatsString().isEmpty()) {
					poidActual = Objet.getPoidOfActualItem(obj
							.parseStatsString().replace(";", "#"));// Poid de
																	// l'item
																	// actuel
					ActualJet = getActualJet(obj, stats);// Jet actuel de l'item
				}
				int poidBase = Objet.getPoidOfBaseItem(obj.getTemplate()
						.getID());// Poid de base de l'item
				int BaseMaxJet = getBaseMaxJet(obj.getTemplate().getID(), stats);

				if (poidBase <= 0) {
					poidBase = 0;
				}
				if (BaseMaxJet <= 0) {
					BaseMaxJet = 0;
				}
				if (ActualJet <= 0) {
					ActualJet = 0;
				}
				if (poidActual <= 0) {
					poidActual = 0;
				}
				if (poid <= 0) {
					poid = 0;
				}

				double Coef = 1;
				if (ViewBaseStatsItem(obj, stats) == 1
						&& ViewActualStatsItem(obj, stats) == 1
						|| ViewBaseStatsItem(obj, stats) == 1
						&& ViewActualStatsItem(obj, stats) == 0)// Existe sur
																// l'arme de
																// base
				{
					Coef = 1;
				} else if (ViewBaseStatsItem(obj, stats) == 2
						&& ViewActualStatsItem(obj, stats) == 2)// Existe en
																// n�gatif de
																// base &&
																// n�gatif sur
																// l'arme
				{
					Coef = 0.75;
				} else if (ViewBaseStatsItem(obj, stats) == 0
						&& ViewActualStatsItem(obj, stats) == 0
						|| ViewBaseStatsItem(obj, stats) == 0
						&& ViewActualStatsItem(obj, stats) == 1)// N'existe pas
																// sur l'arme de
																// base
				{
					Coef = 0.25;
				}

				// OverMax
				int JetMax = BaseMaxJet
						* (2 - (obj.getTemplate().getLevel() / 100));
				if (JetMax <= 0)
					JetMax = 1;
				// int JetMax = (int)
				// (BaseMaxJet+(BaseMaxJet+(100-(poid*BaseMaxJet)) / (2*poid)));
				Coef = Coef * ((JetMax - (double) (ActualJet)) / 25);
				if (Coef <= 0)
					Coef = 0;
				chan = Formulas.ChanceFM(poidBase, poidActual, ActualJet, poid,
						JetMax, Coef);
				chan = 20 - (int) (Math.sqrt(200 - nivelOficio) * 2D);
				// DEBUG :

				if (chan <= 0)
					chan = 1;
				if (chan >= 100)
					chan = 100;

				SocketManager.GAME_SEND_MESSAGE(_P,
						"Votre chance de succ�s �tait de " + chan + "%",
						"009900");
				SocketManager.GAME_SEND_MESSAGE(_P, "Vous pouvez toujours ajouter "
						+ (BaseMaxJet - (ActualJet + add))
						+ " stats avec une certitude de r�ussir.", "009900");

				// 2 cas : R�ussite Totale
				// ou �chec total : la FM n'a pas r�ussi et les bonus de toutes
				// les caract�ristiques diminuent proportionnellement � la
				// puissance � la puissance de la rune. Utiliser de grosses
				// runes est donc risqu�.
				// chan = chan-(106-SM.get_lvl());
			}

			int jet = Formulas.getRandomValue(1, 100);
			boolean success = chan >= jet;
			int tID = obj.getTemplate().getID();
			if (!success)// Si echec
			{
				String statsnegatif = "";
				if (ViewBaseStatsItem(obj, "98") == 1) {
					if (ViewActualStatsItem(obj, "98") == 0
							&& ViewActualStatsItem(obj, "7b") == 0) {
						statsnegatif += ",98#" + Integer.toHexString(1)
								+ "#0#0#0d0+1";
					}
				}
				if (ViewBaseStatsItem(obj, "9a") == 1) {
					if (ViewActualStatsItem(obj, "9a") == 0
							&& ViewActualStatsItem(obj, "77") == 0) {
						statsnegatif += ",9a#" + Integer.toHexString(1)
								+ "#0#0#0d0+1";
					}
				}
				if (ViewBaseStatsItem(obj, "9b") == 1) {
					if (ViewActualStatsItem(obj, "9b") == 0
							&& ViewActualStatsItem(obj, "7e") == 0) {
						statsnegatif += ",9b#" + Integer.toHexString(1)
								+ "#0#0#0d0+1";
					}
				}
				if (ViewBaseStatsItem(obj, "9d") == 1) {
					if (ViewActualStatsItem(obj, "9d") == 0
							&& ViewActualStatsItem(obj, "76") == 0) {
						statsnegatif += ",9d#" + Integer.toHexString(1)
								+ "#0#0#0d0+1";
					}
				}
				if (ViewBaseStatsItem(obj, "74") == 1) {
					if (ViewActualStatsItem(obj, "74") == 0
							&& ViewActualStatsItem(obj, "75") == 0) {
						statsnegatif += ",74#" + Integer.toHexString(1)
								+ "#0#0#0d0+1";
					}
				}
				if (ViewBaseStatsItem(obj, "99") == 1) {
					if (ViewActualStatsItem(obj, "99") == 0
							&& ViewActualStatsItem(obj, "7d") == 0) {
						statsnegatif += ",99#" + Integer.toHexString(1)
								+ "#0#0#0d0+1";
					}
				}
				if (obj.parseStatsString().isEmpty() && !statsnegatif.isEmpty())// Si
																				// l'item
																				// est
																				// vide
																				// et
																				// que
																				// l'on
																				// a
																				// l'ajout
																				// de
																				// stats
																				// n�gatifs
				{
					obj.setStats(obj.generateNewStatsFromTemplate(
							(statsnegatif.substring(1)), false));
				} else if (!obj.parseStatsString().isEmpty())// Si l'item
																// poss�de d�j�
																// des stats
				{
					obj.setStats(obj.generateNewStatsFromTemplate(
							(obj.parseFMEchecStatsString(obj, poid).replace(
									";", "#") + statsnegatif), false));
				}
				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_P, obj.getGuid());// Supprime
																				// l'ancien
																				// affichage
																				// de
																				// l'item
				SocketManager.GAME_SEND_Ow_PACKET(_P);
				SocketManager.GAME_SEND_OAKO_PACKET(_P, obj);
				SocketManager.GAME_SEND_Em_PACKET(_P,
						"EC+" + obj.getGuid() + "|1|" + tID + "|"
								+ obj.parseStatsString().replace(";", "#"));// On
																			// replace
																			// l'item
																			// dans
																			// l'inventaire
				SocketManager.GAME_SEND_Ec_PACKET(_P, "EF");
				SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.get_curCarte(),
						_P.get_GUID(), "-" + tID);
				SocketManager.GAME_SEND_Im_PACKET(_P, "0183");
				SQLManager.SAVE_ITEM(obj);
			} else {
				int coef = 0;
				if (isElementChanging == 1)
					coef = 50;
				if (isElementChanging == 25)
					coef = 65;
				if (isElementChanging == 50)
					coef = 85;
				// Si sign� on ajoute la ligne de Stat "Modifi� par: "
				if (signed)
					obj.addTxtStat(985, _P.get_name());

				if (isElementChanging > 0 && isStatsChanging == 0)// Si on
																	// modifier
																	// l'�l�ment
				{
					for (SpellEffect SE : obj.getEffects()) {
						// Si pas un effet Dom Neutre, on continue
						if (SE.getEffectID() != 100)
							continue;
						String[] infos = SE.getArgs().split(";");
						try {
							// on calcule les nouvelles stats
							int min = Integer.parseInt(infos[0], 16);
							int max = Integer.parseInt(infos[1], 16);
							int newMin = (int) ((min * coef) / 100);
							int newMax = (int) ((max * coef) / 100);

							if (newMin == 0)
								newMin = 1;
							String newJet = "1d" + (newMax - newMin + 1) + "+"
									+ (newMin - 1);
							String newArgs = Integer.toHexString(newMin) + ";"
									+ Integer.toHexString(newMax) + ";-1;-1;0;"
									+ newJet;

							SE.setArgs(newArgs);// on modifie les propri�t�s du
												// SpellEffect
							SE.setEffectID(stat);// On change l'�lement
													// d'attaque

						} catch (Exception e) {
							e.printStackTrace();
						}
						;
					}
				} else if (isStatsChanging > 0 && isElementChanging == 0)// Si
																			// on
																			// modifier
																			// les
																			// stats
																			// (rune)
				{
					System.out.println("Cambiando los stats");
					System.out.println("Suerte : " + chan);
					System.out.println("Elemento a modificar : " + stats);

					boolean negatif = false;

					if (ViewActualStatsItem(obj, stats) == 2)// Le stats existe
																// actuellement
																// en n�gatif
					{
						// R�duit les stats n�gatifs si r�ussit jusqu'a leur
						// disparitions
						if (stats.compareTo("7b") == 0) {
							stats = "98";
							negatif = true;
						}
						if (stats.compareTo("77") == 0) {
							stats = "9a";
							negatif = true;
						}
						if (stats.compareTo("7e") == 0) {
							stats = "9b";
							negatif = true;
						}
						if (stats.compareTo("76") == 0) {
							stats = "9d";
							negatif = true;
						}
						if (stats.compareTo("75") == 0) {
							stats = "74";
							negatif = true;
						}
						if (stats.compareTo("7d") == 0) {
							stats = "99";
							negatif = true;
						}
						// On change la valeur du stats a modifier
					}

					if (ViewActualStatsItem(obj, stats) == 1
							|| ViewActualStatsItem(obj, stats) == 2)// L'item
																	// poss�de
																	// le stats
																	// n�gatif
																	// ou
																	// positif
					{
						if (Ancestra.CONFIG_DEBUG)
							System.out
									.println("Modification d'un stat existant : "
											+ stats + ". Ajout de " + add);
						obj.setStats(obj.generateNewStatsFromTemplate(obj
								.parseFMStatsString(stats, obj, add, negatif)
								.replace(";", "#"), false));
					} else// L'item ne poss�de pas le stats.
					{
						if (Ancestra.CONFIG_DEBUG)
							System.out.println("Ajout d'un stat inexistant : "
									+ stats + ". Ajout de " + add);
						if (obj.parseStatsString().isEmpty())// Si l'item est
																// vide
						{
							obj.setStats(obj.generateNewStatsFromTemplate(
									(stats + "#" + Integer.toHexString(add)
											+ "#0#0#0d0+" + add), false));
						} else// Si l'item poss�de d�j� des stats
						{
							obj.setStats(obj.generateNewStatsFromTemplate(
									(obj.parseFMStatsString(stats, obj, add,
											negatif).replace(";", "#")
											+ ","
											+ stats
											+ "#"
											+ Integer.toHexString(add)
											+ "#0#0#0d0+" + add), false));
						}

					}
				}
				// On envoie les packets
				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_P, obj.getGuid());// Supprime
																				// l'ancien
																				// affichage
																				// de
																				// l'item
				SocketManager.GAME_SEND_Ow_PACKET(_P);// Pods
				SocketManager.GAME_SEND_OAKO_PACKET(_P, obj);// Nouveau jet
				SocketManager.GAME_SEND_Em_PACKET(_P,
						"KO+" + obj.getGuid() + "|1|" + tID + "|"
								+ obj.parseStatsString().replace(";", "#"));// On
																			// replace
																			// l'item
																			// dans
																			// l'inventaire
				SocketManager.GAME_SEND_Ec_PACKET(_P, "K;" + tID);// R�ussite
				SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.get_curCarte(),
						_P.get_GUID(), "+" + tID);// Icone de r�ussite
				SQLManager.SAVE_ITEM(obj);// On Save
				// TODO : Le repeat ?
			}
			// On consumme les runes
			// Rune de signature si diff de null
			if (sign != null) {
				int newQua = sign.getQuantity() - 1;
				// S'il ne reste rien
				if (newQua <= 0) {
					_P.removeItem(sign.getGuid());
					World.removeItem(sign.getGuid());
					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_P,
							sign.getGuid());
				} else {
					sign.setQuantity(newQua);
					SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_P, sign);
				}
			}
			// Objet modificateur
			if (mod != null) {
				int newQua = mod.getQuantity() - 1;
				// S'il ne reste rien
				if (newQua <= 0) {
					_P.removeItem(mod.getGuid());
					World.removeItem(mod.getGuid());
					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_P,
							mod.getGuid());
				} else {
					mod.setQuantity(newQua);
					SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_P, mod);
				}
			}
			// fin


			// On sauve le dernier craft
			_lastCraft.clear();
			_lastCraft.putAll(_ingredients);
			_ingredients.clear();
		}

		public void repeat(int time, Personnage P) {
			_craftTimer.stop();
			// /!\ Time = Nombre R�el -1
			_lastCraft.clear();
			_lastCraft.putAll(_ingredients);
			for (int a = time; a >= 0; a--) {
				SocketManager.GAME_SEND_EA_PACKET(P, a + "");
				_ingredients.clear();
				_ingredients.putAll(_lastCraft);
				craft();
			}
			SocketManager.GAME_SEND_Ea_PACKET(P, "1");
		}

		public void startCraft(Personnage P)
		{
			//on retarde le lancement du craft en cas de packet EMR (craft auto)
			_craftTimer.start();
		}

		public void putLastCraftIngredients()
		{
			if(_P == null)return;
			if(_lastCraft == null)return;
			if(!_ingredients.isEmpty())return;//OffiLike, mais possible de faire un truc plus propre en enlevant les objets pr�sent et en rajoutant ceux de la recette
			_ingredients.clear();
			_ingredients.putAll(_lastCraft);
			for(Entry<Integer,Integer> e : _ingredients.entrySet())
			{
				if(World.getObjet(e.getKey()) == null)return;
				if(World.getObjet(e.getKey()).getQuantity() < e.getValue())return;
				SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(_P,'O', "+", e.getKey()+"|"+e.getValue());
			}
		}

		public void resetCraft()
		{
			_ingredients.clear();
			_lastCraft.clear();
		}
}
	//Classe Metier
	private int _id;
	private ArrayList<Integer> _tools = new ArrayList<Integer>();
	private Map<Integer,ArrayList<Integer>> _crafts = new TreeMap<Integer,ArrayList<Integer>>();
	
	public Metier(int id,String tools,String crafts)
	{
		_id= id;
		if(!tools.equals(""))
		{
			for(String str : tools.split(","))
			{
				try
				{
					int tool = Integer.parseInt(str);
					_tools.add(tool);
				}catch(Exception e){continue;};
			}
		}
		
		if(!crafts.equals(""))
		{
			for(String str : crafts.split("\\|"))
			{
				try
				{
					int skID = Integer.parseInt(str.split(";")[0]);
					ArrayList<Integer> list = new ArrayList<Integer>();
					for(String str2 : str.split(";")[1].split(","))list.add(Integer.parseInt(str2));
					_crafts.put(skID, list);
				}catch(Exception e){continue;};
			}
		}
	}
	public ArrayList<Integer> getListBySkill(int skID)
	{
		return _crafts.get(skID);
	}
	public boolean canCraft(int skill,int template)
	{
		if(_crafts.get(skill) != null)for(int a : _crafts.get(skill))if(a == template)return true;
		return false;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public boolean isValidTool(int t)
	{
		for(int a : _tools)if(t == a)return true;
		return false;
	}
	public static byte ViewActualStatsItem(Objet obj, String stats)//retourne vrai si le stats est actuellement sur l'item
	{
		if(!obj.parseStatsString().isEmpty())
		{
		for(Entry<Integer,Integer> entry : obj.getStats().getMap().entrySet())
		{
			if(Integer.toHexString(entry.getKey()).compareTo(stats) > 0)//Effets inutiles
			{
				if(Integer.toHexString(entry.getKey()).compareTo("98") == 0 && stats.compareTo("7b") == 0)
				{
					return 2;
				}
				else if(Integer.toHexString(entry.getKey()).compareTo("9a") == 0 && stats.compareTo("77") == 0)
				{
					return 2;
				}
				else if(Integer.toHexString(entry.getKey()).compareTo("9b") == 0 && stats.compareTo("7e") == 0)
				{
					return 2;
				}
				else if(Integer.toHexString(entry.getKey()).compareTo("9d") == 0 && stats.compareTo("76") == 0)
				{
					return 2;
				}
				else if(Integer.toHexString(entry.getKey()).compareTo("74") == 0 && stats.compareTo("75") == 0)
				{
					return 2;
				}
				else if(Integer.toHexString(entry.getKey()).compareTo("99") == 0 && stats.compareTo("7d") == 0)
				{
					return 2;
				}
				else
				{
					continue;
				}
			}
			else if(Integer.toHexString(entry.getKey()).compareTo(stats) == 0)//L'effet existe bien !
			{
				return 1;
			}
		}
			return 0;
		}
		else
		{
			return 0;
		}
	}
	
	public static byte ViewBaseStatsItem(Objet obj, String ItemStats)//retourne vrai si le stats existe de base sur l'item
	{
		
		String[] splitted = obj.getTemplate().getStrTemplate().split(",");
		for(String s : splitted)
		{
			String[] stats = s.split("#");
			if(stats[0].compareTo(ItemStats) > 0)//Effets n'existe pas de base
			{
				if(stats[0].compareTo("98") == 0 && ItemStats.compareTo("7b") == 0)
				{
					return 2;
				}
				else if(stats[0].compareTo("9a") == 0 && ItemStats.compareTo("77") == 0)
				{
					return 2;
				}
				else if(stats[0].compareTo("9b") == 0 && ItemStats.compareTo("7e") == 0)
				{
					return 2;
				}
				else if(stats[0].compareTo("9d") == 0 && ItemStats.compareTo("76") == 0)
				{
					return 2;
				}
				else if(stats[0].compareTo("74") == 0 && ItemStats.compareTo("75") == 0)
				{
					return 2;
				}
				else if(stats[0].compareTo("99") == 0 && ItemStats.compareTo("7d") == 0)
				{
					return 2;
				}
				else
				{
					continue;
				}
			}
			else if(stats[0].compareTo(ItemStats) == 0)//L'effet existe bien !
			{
				return 1;
			}
		}
		return 0;
	}
	
	public static int getBaseMaxJet(int templateID, String statsModif)
	{
		ObjTemplate t = World.getObjTemplate(templateID);
		String[] splitted = t.getStrTemplate().split(",");
		for(String s : splitted)
		{
			String[] stats = s.split("#");
			if(stats[0].compareTo(statsModif) > 0)//Effets n'existe pas de base
			{
				continue;
			}
			else if(stats[0].compareTo(statsModif) == 0)//L'effet existe bien !
			{
				int max = Integer.parseInt(stats[2],16);
				if(max == 0) max = Integer.parseInt(stats[1],16);//Pas de jet maximum on prend le minimum
				return max;
			}
		}
		return 0;
	}
	
	public static int getActualJet(Objet obj, String statsModif)
	{
		for(Entry<Integer,Integer> entry : obj.getStats().getMap().entrySet())
		{
			if(Integer.toHexString(entry.getKey()).compareTo(statsModif) > 0)//Effets inutiles
			{
				continue;
			}
			else if(Integer.toHexString(entry.getKey()).compareTo(statsModif) == 0)//L'effet existe bien !
			{
				int JetActual = entry.getValue();		
				return JetActual;
			}
		}	
		return 0;
	}
	
}
