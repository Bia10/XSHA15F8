package common;

import game.GameServer;
import game.GameThread.GameAction;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;


import objects.*;
import objects.Sort.SortStats;
import objects.Carte.Case;
import objects.Fight.*;

public class IA {

	public static class IAThread implements Runnable
	{
		private Fight _fight;
		private Fighter _fighter;
		private static boolean stop = false;
		private Thread _t;
		
		public IAThread(Fighter fighter, Fight fight)
		{
			_fighter = fighter;
			_fight = fight;
			_t = new Thread(this);
			_t.setDaemon(true);
			_t.start();
		}
		  public void run() { 
		      stop = false;
		      if (_fighter.getMob() == null)
		      {
		        if (_fighter.isDouble()) {
		          try {
		            apply_type5(_fighter, _fight); } catch (Exception localException) {
		          }try {
		            Thread.sleep(2000L); } catch (InterruptedException localInterruptedException) {
		          }
		          _fight.endTurn();
		        }
		        else if (_fighter.isPerco())
		        {
		          apply_typePerco(_fighter, _fight);
		          try {
		            Thread.sleep(2000L); } catch (InterruptedException localInterruptedException1) {
		          }
		          _fight.endTurn();
		        }
		        else
		        {
		          try {
		            Thread.sleep(2000L); } catch (InterruptedException localInterruptedException2) {
		          }
		          _fight.endTurn();
		        }
		      }
		      else if (_fighter.getMob().getTemplate() == null)
		      {
		        _fight.endTurn();
		      }
		      else {
		        int IA_Type = _fighter.getMob().getTemplate().getIAType();
		        switch (IA_Type)
		        {
		        case 0:
		          apply_type0(_fighter, _fight);
		          break;
		        case 1:
		          try { apply_type1(_fighter, _fight); } catch (Exception localException1) {
		          }case 2:
		          try {
		            apply_type2(_fighter, _fight); } catch (Exception localException2) {
		          }case 3:
		          try {
		            apply_type3(_fighter, _fight); } catch (Exception localException3) {
		          }case 4:
		          try {
		            apply_type4(_fighter, _fight); } catch (Exception localException4) {
		          }case 5:
		          try {
		            apply_type5(_fighter, _fight); } catch (Exception localException5) {
		          }case 6:
		          try {
		            apply_type6(_fighter, _fight); } catch (Exception localException6) {
		          }
		        }
		        try {
		          if (IA_Type == 0)
		            Thread.sleep(250L);
		          else
		            Thread.sleep(2000L);
		        } catch (InterruptedException localInterruptedException3) {
		        }
		        if (!_fighter.isDead())
		          _fight.endTurn();
		      }
		    }
		
		private static void apply_type0(Fighter F, Fight fight)
		{
			stop = true;
		}

		private static void apply_type1(Fighter F, Fight fight)
		{
			while(!stop && F.canPlay())
			{
				int PDVPER = (F.getPDV()*100)/F.getPDVMAX();
				Fighter T = getNearestEnnemy(fight, F); // Ennemis
				Fighter T2 = getNearestFriend(fight,F); // Amis
				if(T == null)
					return;
				if(PDVPER > 15)
				{
					int attack = attackIfPossible(fight,F);
					if(attack != 0)//Attaque
					{
						if(attack == 5) stop = true;//EC
						if(!moveToAttackIfPossible(fight,F))
						{
							if(!buffIfPossible(fight,F,F))//auto-buff
							{
								if(!HealIfPossible(fight,F, false))//soin allié
								{
									if(!buffIfPossible(fight,F,T2))//buff allié
									{
										if(!moveNearIfPossible(fight,F,T))//avancer
										{
											if(!invocIfPossible(fight,F))//invoquer
											{
												stop = true;
											}
										}
									}
								}
							}
						}
					}
				}
				else
				{
					if(!HealIfPossible(fight,F,true))//auto-soin
					{
						int attack = attackIfPossible(fight,F);
						if(attack != 0)//Attaque
						{
							if(attack == 5) stop = true;//EC
							if(!buffIfPossible(fight,F,F))//auto-buff
							{
								if(!HealIfPossible(fight,F,false))//soin allié
								{
									if(!buffIfPossible(fight,F,T2))//buff allié
									{
										if(!invocIfPossible(fight,F))
										{
											if(!moveFarIfPossible(fight, F))//fuite
											{
												stop = true;
											}
										}
									}
								}
							}
						}
					}				
				}
			}
		}

		private static void apply_type2(Fighter F, Fight fight)
		{
			while(!stop && F.canPlay())
			{
				Fighter T = getNearestFriend(fight,F);
				if(!HealIfPossible(fight,F,false))//soin allié
				{
					if(!buffIfPossible(fight,F,T))//buff allié
					{
						if(!moveNearIfPossible(fight,F,T))//Avancer vers allié
						{
							if(!HealIfPossible(fight,F,true))//auto-soin
							{
								if(!buffIfPossible(fight,F,F))//auto-buff
								{
									if(!invocIfPossible(fight,F))
									{
										T = getNearestEnnemy(fight, F);
										int attack = attackIfPossible(fight,F);
										if(attack != 0)//Attaque
										{
											if(attack == 5) stop = true;//EC
											if(!moveFarIfPossible(fight, F))//fuite
												stop = true;
										}
									}
								}
							}
						}
					}
				}		
			}
		}
		
		private static void apply_type3(Fighter F, Fight fight)
		{
			while(!stop && F.canPlay())
			{
				Fighter T = getNearestFriend(fight,F);
					if(!moveNearIfPossible(fight,F,T))//Avancer vers allié
					{
						if(!HealIfPossible(fight,F,false))//soin allié
						{
							if(!buffIfPossible(fight,F,T))//buff allié
							{
								if(!HealIfPossible(fight,F,true))//auto-soin
								{
									if(!invocIfPossible(fight,F))
									{
										if(!buffIfPossible(fight,F,F))//auto-buff
										{
												stop = true;
										}
									}
							}
						}
					}
				}
			}		
		}
		
		private static void apply_type4(Fighter F, Fight fight) //IA propre La Folle
		{
			while(!stop && F.canPlay())
			{
				Fighter T = getNearestEnnemy(fight, F);
				if(T == null) return;
				int attack = attackIfPossible(fight,F);
				if(attack != 0)//Attaque
				{
					if(attack == 5) stop = true;//EC
					if(!moveFarIfPossible(fight, F))//fuite
					{
							if(!HealIfPossible(fight,F,false))//soin allié
							{
								if(!buffIfPossible(fight,F,T))//buff allié
								{
									if(!HealIfPossible(fight,F,true))//auto-soin
									{
										if(!invocIfPossible(fight,F))
										{
											if(!buffIfPossible(fight,F,F))//auto-buff
											{
													stop = true;
											}
										}
									}
								}
							}
					}
				}
			}
		}
		
		private static void apply_type5(Fighter F, Fight fight) //IA propre aux énus
		{
			while(!stop && F.canPlay())
			{
				Fighter T = getNearestEnnemy(fight, F);
				if(T == null) return;
				
				if(!moveNearIfPossible(fight,F,T))//Avancer vers enemis
				{
					stop = true;
				}
			}
		}
		
		private static void apply_type6(Fighter F, Fight fight)
		{
			while(!stop && F.canPlay())
			{
				if(!invocIfPossible(fight,F))
				{
					Fighter T = getNearestFriend(fight,F);
					if(!HealIfPossible(fight,F,false))//soin allié
					{
						if(!buffIfPossible(fight,F,T))//buff allié
						{
							if(!buffIfPossible(fight,F,F))//buff allié
							{
								if(!HealIfPossible(fight,F,true))
								{
									int attack = attackIfPossible(fight,F);
									if(attack != 0)//Attaque
									{
										if(attack == 5) stop = true;//EC
										if(!moveFarIfPossible(fight, F))//fuite
											stop = true;
									}
								}
							}
						}
					}	
				}
			}
		}
		
		private static void apply_typePerco(Fight.Fighter F, Fight fight)
	    {
	      try
	      {
	        int noBoucle = 0;
	        do
	        {
	          noBoucle++;
	          if (noBoucle >= 12) stop = true;
	          if (noBoucle > 15) return;
	          Fight.Fighter T = getNearestEnnemy(fight, F);
	          if (T == null) return;
	          int attack = attackIfPossiblePerco(fight, F);
	          if (attack != 0)
	          {
	            if (attack == 5) stop = true;
	            if (!moveFarIfPossible(fight, F))
	            {
	              if (!HealIfPossiblePerco(fight, F, false))
	              {
	                if (!buffIfPossiblePerco(fight, F, T))
	                {
	                  if (!HealIfPossiblePerco(fight, F, true))
	                  {
	                    if (!buffIfPossiblePerco(fight, F, F))
	                    {
	                      stop = true;
	                    }
	                  }
	                }
	              }
	            }
	          }
	          if (stop) break;
	        }while (F.canPlay());
	      }
	      catch (Exception e)
	      {
	        return;
	      }
	    }
		
		private static boolean moveFarIfPossible(Fight fight, Fighter F) 
		{
			int dist[] = {1000,1000,1000,1000,1000,1000,1000,1000,1000,1000}, cell[] = {0,0,0,0,0,0,0,0,0,0};
			for(int i = 0; i < 10 ; i++)
			{
				for(Fighter f : fight.getFighters(3))
				{
					
					if(f.isDead())continue;
					if(f == F || f.getTeam() == F.getTeam())continue;
					int cellf = f.get_fightCell().getID();
					if(cellf == cell[0] || cellf == cell[1] || cellf == cell[2] || cellf == cell[3] || cellf == cell[4] || cellf == cell[5] || cellf == cell[6] || cellf == cell[7] || cellf == cell[8] || cellf == cell[9])continue;					
					int d = 0;
					d = Pathfinding.getDistanceBetween(fight.get_map(), F.get_fightCell().getID(), f.get_fightCell().getID());
					if(d == 0)continue;
					if(d < dist[i])
					{
						dist[i] = d;
						cell[i] = cellf;
					}
					if(dist[i] == 1000)
					{
						dist[i] = 0;
						cell[i] = F.get_fightCell().getID();
					}
				}
			}
			if(dist[0] == 0)return false;
			int dist2[] = {0,0,0,0,0,0,0,0,0,0};
			int PM = F.getCurPM(fight), caseDepart = F.get_fightCell().getID(), destCase = F.get_fightCell().getID();
			for(int i = 0; i <= PM;i++)
			{
				if(destCase > 0)
					caseDepart = destCase;
				int curCase = caseDepart;
				curCase += 15;
				int infl = 0, inflF = 0;
				for(int a = 0; a < 10 && dist[a] != 0; a++)
				{
					dist2[a] = Pathfinding.getDistanceBetween(fight.get_map(), curCase, cell[a]);
					if(dist2[a] > dist[a])
						infl++;
				}
				
				if(infl > inflF && curCase > 0 && curCase < 478 && testCotes(destCase, curCase))
				{
					inflF = infl;
					destCase = curCase;
				}
				
				curCase = caseDepart + 14;
				infl = 0;
				
				for(int a = 0; a < 10 && dist[a] != 0; a++)
				{
					dist2[a] = Pathfinding.getDistanceBetween(fight.get_map(), curCase, cell[a]);
					if(dist2[a] > dist[a])
						infl++;
				}
				
				if(infl > inflF && curCase > 0 && curCase < 478 && testCotes(destCase, curCase))
				{
					inflF = infl;
					destCase = curCase;
				}
				
				curCase = caseDepart -15;
				infl = 0;
				for(int a = 0; a < 10 && dist[a] != 0; a++)
				{
					dist2[a] = Pathfinding.getDistanceBetween(fight.get_map(), curCase, cell[a]);
					if(dist2[a] > dist[a])
						infl++;
				}
				
				if(infl > inflF && curCase > 0 && curCase < 478 && testCotes(destCase, curCase))
				{
					inflF = infl;
					destCase = curCase;
				}
				
				curCase = caseDepart - 14;
				infl = 0;
				for(int a = 0; a < 10 && dist[a] != 0; a++)
				{
					dist2[a] = Pathfinding.getDistanceBetween(fight.get_map(), curCase, cell[a]);
					if(dist2[a] > dist[a])
						infl++;
				}
				
				if(infl > inflF && curCase > 0 && curCase < 478 && testCotes(destCase, curCase))
				{
					inflF = infl;
					destCase = curCase;
				}
			}
			Ancestra.printDebug("Test MOVEFAR : cell = " + destCase);
			if(destCase < 0 || destCase > 478 || destCase == F.get_fightCell().getID() || !fight.get_map().getCase(destCase).isWalkable(false))return false;
			if(F.getPM() <= 0)return false;
			ArrayList<Case> path = Pathfinding.getShortestPathBetween(fight.get_map(),F.get_fightCell().getID(),destCase, 0);
			if(path == null)return false;
			
			// DEBUG PATHFINDING
			/*Ancestra.printDebug("DEBUG PATHFINDING:");
			Ancestra.printDebug("startCell: "+F.get_fightCell().getID());
			Ancestra.printDebug("destinationCell: "+cellID);
			
			for(Case c : path)
			{
				Ancestra.printDebug("Passage par cellID: "+c.getID()+" walk: "+c.isWalkable(true));
			}*/
			
			ArrayList<Case> finalPath = new ArrayList<Case>();
			for(int a = 0; a<F.getPM();a++)
			{
				if(path.size() == a)break;
				finalPath.add(path.get(a));
			}
			String pathstr = "";
			try{
			int curCaseID = F.get_fightCell().getID();
			int curDir = 0;
			for(Case c : finalPath)
			{
				char d = Pathfinding.getDirBetweenTwoCase(curCaseID, c.getID(), fight.get_map(), true);
				if(d == 0)return false;//Ne devrait pas arriver :O
				if(curDir != d)
				{
					if(finalPath.indexOf(c) != 0)
						pathstr += CryptManager.cellID_To_Code(curCaseID);
					pathstr += d;
				}
				curCaseID = c.getID();
			}
			if(curCaseID != F.get_fightCell().getID())
				pathstr += CryptManager.cellID_To_Code(curCaseID);
			}catch(Exception e){e.printStackTrace();};
			//Création d'une GameAction
			GameAction GA = new GameAction(0,1, "");
			GA._args = pathstr;
			boolean result = fight.fighterDeplace(F, GA);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			return result;

		}

		private static boolean testCotes(int cell1, int cell2)
		{
			if ( cell1 == 15 || cell1 == 44 || cell1 == 73 || cell1 == 102 || cell1 == 131 || cell1 == 160 || cell1 == 189 || cell1 == 218 || cell1 == 247 || cell1 == 276 || cell1 == 305 || cell1 == 334 || cell1 == 363 || cell1 == 392 || cell1 == 421 || cell1 == 450 )
			{
				if( cell2 == cell1 + 14 || cell2 == cell1 - 15 )
					return false;			
			}
			if ( cell1 == 28 || cell1 == 57 || cell1 == 86 || cell1 == 115 || cell1 == 144 || cell1 == 173 || cell1 == 202 || cell1 == 231 || cell1 == 260 || cell1 == 289 || cell1 == 318 || cell1 == 347 || cell1 == 376 || cell1 == 405 || cell1 == 434 || cell1 == 463 )
			{
				if( cell2 == cell1 + 15 || cell2 == cell1 - 14 )
					return false;
			}
			return true;
		}
		
		private static boolean invocIfPossible(Fight fight,Fighter fighter)
		{
			Fighter nearest = getNearestEnnemy(fight, fighter);
			if(nearest == null)
				return false;
			int nearestCell = Pathfinding.getNearestCellAround(fight.get_map(),fighter.get_fightCell().getID(),nearest.get_fightCell().getID(),null);
			if(nearestCell == -1)
				return false;
			SortStats spell = getInvocSpell(fight,fighter,nearestCell);
			if(spell == null)
				return false;
			int invoc = fight.tryCastSpell(fighter, spell, nearestCell);
			if(invoc != 0)return false;
			
			return true;
		}
		
		private static SortStats getInvocSpell(Fight fight,Fighter fighter,int nearestCell)
		{
			if(fighter.getMob() == null)return null;
			for(Entry<Integer, SortStats> SS : fighter.getMob().getSpells().entrySet())
			{
				if(!fight.CanCastSpell(fighter, SS.getValue(), fight.get_map().getCase(nearestCell), -1))
					continue;
				for(SpellEffect SE : SS.getValue().getEffects())
				{
					if(SE.getEffectID() == 181)
						return SS.getValue();		
				}
			}
			return null;
		}
		
		private static boolean HealIfPossible(Fight fight, Fighter f, boolean autoSoin)//boolean pour choisir entre auto-soin ou soin allié
		{
			if(autoSoin && (f.getPDV()*100)/f.getPDVMAX() > 95 )return false;
			Fighter target = null;
			SortStats SS = null;
			if(autoSoin)
			{
				target = f;			
				SS = getHealSpell(fight,f,target);
			}
			else//sélection joueur ayant le moins de pv
			{
				Fighter curF = null;
				int PDVPERmin = 100;
				SortStats curSS = null;
				for(Fighter F : fight.getFighters(3))
				{					
					if(f.isDead())continue;
					if(F == f)continue;
					if(F.getTeam() == f.getTeam())
					{
						int PDVPER = (F.getPDV()*100)/F.getPDVMAX();
						if( PDVPER < PDVPERmin && PDVPER < 95)
						{
							int infl = 0;
							for(Entry<Integer, SortStats> ss : f.getMob().getSpells().entrySet())
							{
								if(infl < calculInfluenceHeal(ss.getValue()) && calculInfluenceHeal(ss.getValue()) != 0 && fight.CanCastSpell(f, ss.getValue(), F.get_fightCell(), -1))//Si le sort est plus interessant
								{
									infl = calculInfluenceHeal(ss.getValue());
									curSS = ss.getValue();
								}
							}
							if(curSS != SS && curSS != null)
							{
								curF = F;
								SS = curSS;
								PDVPERmin = PDVPER;
							}
						}
					}
				}
				target = curF;			
			}
			if(target == null)return false;
			if(SS == null)return false;
			int heal = fight.tryCastSpell(f, SS, target.get_fightCell().getID());
			if(heal != 0)
				return false;
			
			return true;
		}
		
		 @SuppressWarnings("rawtypes")
		private static boolean HealIfPossiblePerco(Fight fight, Fight.Fighter f, boolean autoSoin)
		    {
		      if ((autoSoin) && (f.getPDV() * 100 / f.getPDVMAX() > 95)) return false;
		      Fight.Fighter target = null;
		      SortStats SS = null;
		      if (autoSoin)
		      {
		        target = f;
		        SS = getHealSpell(fight, f, target);
		      }
		      else
		      {
		        Fight.Fighter curF = null;
		        int PDVPERmin = 100;
		        SortStats curSS = null;
		        for (Fight.Fighter F : fight.getFighters(3))
		        {
		          if ((f.isDead()) ||
		            (F == f) ||
		            (F.getTeam() != f.getTeam()))
		            continue;
		          int PDVPER = F.getPDV() * 100 / F.getPDVMAX();
		          if ((PDVPER >= PDVPERmin) || (PDVPER >= 95))
		            continue;
		          int infl = 0;
		          for (Map.Entry ss : World.getGuild(F.getPerco().GetPercoGuildID()).getSpells().entrySet())
		          {
		            if ((ss.getValue() == null) ||
		              (infl >= calculInfluenceHeal((SortStats)ss.getValue())) || (calculInfluenceHeal((SortStats)ss.getValue()) == 0) || (!fight.CanCastSpell3(f, (SortStats)ss.getValue(), F.get_fightCell(), infl)))
		              continue;
		            infl = calculInfluenceHeal((SortStats)ss.getValue());
		            curSS = (SortStats)ss.getValue();
		          }

		          if ((curSS == SS) || (curSS == null))
		            continue;
		          curF = F;
		          SS = curSS;
		          PDVPERmin = PDVPER;
		        }

		        target = curF;
		      }
		      if (target == null) return false;
		      if (SS == null) return false;
		      int heal = fight.tryCastSpell(f, SS, target.get_fightCell().getID());

		      return heal == 0;
		    }
		
		private static boolean buffIfPossible(Fight fight, Fighter fighter,Fighter target) 
		{		
			if(target == null)return false;
			SortStats SS = getBuffSpell(fight,fighter,target);
			if(SS == null)return false;
			int buff = fight.tryCastSpell(fighter, SS, target.get_fightCell().getID());
			if(buff != 0)return false;			
			
			return true;	
		}
		
		 private static boolean buffIfPossiblePerco(Fight fight, Fight.Fighter fighter, Fight.Fighter target)
		    {
		      if (target == null) return false;
		      SortStats SS = getBuffSpellPerco(fight, fighter, target);
		      if (SS == null) return false;
		      int buff = fight.tryCastSpell(fighter, SS, target.get_fightCell().getID());
		      return buff == 0;
		    }

		private static SortStats getBuffSpell(Fight fight, Fighter F, Fighter T)
		{
			int infl = 0;	
			SortStats ss = null;
			for(Entry<Integer, SortStats> SS : F.getMob().getSpells().entrySet())
			{
				if(infl < calculInfluence(fight, SS.getValue(),F,T) && calculInfluence(fight, SS.getValue(),F,T) > 0 && fight.CanCastSpell(F, SS.getValue(), T.get_fightCell(), -1))//Si le sort est plus interessant
				{
					infl = calculInfluence(fight, SS.getValue(),F,T);
					ss = SS.getValue();
				}
			}
			return ss;				
		}
		
		 @SuppressWarnings("rawtypes")
		private static SortStats getBuffSpellPerco(Fight fight, Fight.Fighter F, Fight.Fighter T)
		    {
		      int infl = 0;
		      SortStats ss = null;
		      for (Map.Entry SS : World.getGuild(F.getPerco().GetPercoGuildID()).getSpells().entrySet())
		      {
		        if ((SS.getValue() == null) ||
		          (infl >= calculInfluence((SortStats)SS.getValue(), F, T)) || (calculInfluence((SortStats)SS.getValue(), F, T) <= 0) || (!fight.CanCastSpell3(F, (SortStats)SS.getValue(), T.get_fightCell(), infl)))
		          continue;
		        infl = calculInfluence((SortStats)SS.getValue(), F, T);
		        ss = (SortStats)SS.getValue();
		      }

		      return ss;
		    }
		
		private static SortStats getHealSpell(Fight fight, Fighter F, Fighter T)
		{
			int infl = 0;	
			SortStats ss = null;
			for(Entry<Integer, SortStats> SS : F.getMob().getSpells().entrySet())
			{
				if(infl < calculInfluenceHeal(SS.getValue()) && calculInfluenceHeal(SS.getValue()) != 0 && fight.CanCastSpell(F, SS.getValue(), T.get_fightCell(), -1))//Si le sort est plus interessant
				{
					infl = calculInfluenceHeal(SS.getValue());
					ss = SS.getValue();
				}
			}
			return ss;
		}
		
		private static boolean moveNearIfPossible(Fight fight, Fighter F, Fighter T)
		{
			if(F.getCurPM(fight) <= 0)
				return false;
			if(Pathfinding.isNextTo(F.get_fightCell().getID(), T.get_fightCell().getID()))
				return false;
			
			if(Ancestra.CONFIG_DEBUG) GameServer.addToLog("Tentative d'approche par "+F.getPacketsName()+" de "+T.getPacketsName());
			
			int cellID = Pathfinding.getNearestCellAround(fight.get_map(),T.get_fightCell().getID(),F.get_fightCell().getID(),null);
			//On demande le chemin plus court
			if(cellID == -1)
			{
				Map<Integer,Fighter> ennemys = getLowHpEnnemyList(fight,F);
				if(ennemys==null)return false;
				if(ennemys.isEmpty())return false;
				for(Entry<Integer, Fighter> target : ennemys.entrySet())
				{
					int cellID2 = Pathfinding.getNearestCellAround(fight.get_map(),target.getValue().get_fightCell().getID(),F.get_fightCell().getID(),null);
					if(cellID2 != -1)
					{
						cellID = cellID2;
						break;
					}
				}
			}
			ArrayList<Case> path = Pathfinding.getShortestPathBetween(fight.get_map(),F.get_fightCell().getID(),cellID,0);
			if(path == null || path.isEmpty())return false;
			// DEBUG PATHFINDING
			/*Ancestra.printDebug("DEBUG PATHFINDING:");
			Ancestra.printDebug("startCell: "+F.get_fightCell().getID());
			Ancestra.printDebug("destinationCell: "+cellID);
			
			for(Case c : path)
			{
				Ancestra.printDebug("Passage par cellID: "+c.getID()+" walk: "+c.isWalkable(true));
			}*/
			
			ArrayList<Case> finalPath = new ArrayList<Case>();
			for(int a = 0; a<F.getCurPM(fight);a++)
			{
				if(path.size() == a)break;
				finalPath.add(path.get(a));
			}
			String pathstr = "";
			try{
			int curCaseID = F.get_fightCell().getID();
			int curDir = 0;
			for(Case c : finalPath)
			{
				char d = Pathfinding.getDirBetweenTwoCase(curCaseID, c.getID(), fight.get_map(), true);
				if(d == 0)return false;//Ne devrait pas arriver :O
				if(curDir != d)
				{
					if(finalPath.indexOf(c) != 0)
						pathstr += CryptManager.cellID_To_Code(curCaseID);
					pathstr += d;
				}
				curCaseID = c.getID();
			}
			if(curCaseID != F.get_fightCell().getID())
				pathstr += CryptManager.cellID_To_Code(curCaseID);
			}catch(Exception e){e.printStackTrace();};
			//Création d'une GameAction
			GameAction GA = new GameAction(0,1, "");
			GA._args = pathstr;
			boolean result = fight.fighterDeplace(F, GA);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			return result;
		}

		private static Fighter getNearestFriend(Fight fight, Fighter fighter)
		{
			int dist = 1000;
			Fighter curF = null;
			for(Fighter f : fight.getFighters(3))
			{
				if(f.isDead())continue;
				if(f == fighter)continue;
				if(f.getTeam2() == fighter.getTeam2())//Si c'est un ami
				{
					int d = Pathfinding.getDistanceBetween(fight.get_map(), fighter.get_fightCell().getID(), f.get_fightCell().getID());
					if( d < dist)
					{
						dist = d;
						curF = f;
					}
				}
			}
			return curF;
		}
		
		private static Fighter getNearestEnnemy(Fight fight, Fighter fighter)
		{
			int dist = 1000;
			Fighter curF = null;
			for(Fighter f : fight.getFighters(3))
			{
				if(f.isDead())continue;
				if(f.getTeam2() != fighter.getTeam2())//Si c'est un ennemis
				{
					int d = Pathfinding.getDistanceBetween(fight.get_map(), fighter.get_fightCell().getID(), f.get_fightCell().getID());
					if( d < dist)
					{
						dist = d;
						curF = f;
					}
				}
			}
			return curF;
		}
		
		private static Map<Integer,Fighter> getLowHpEnnemyList(Fight fight,Fighter fighter)
		{
			Map<Integer,Fighter> list = new TreeMap<Integer,Fighter>();
			Map<Integer,Fighter> ennemy = new TreeMap<Integer,Fighter>();
			for(Fighter f : fight.getFighters(3))
			{
				if(f==null)continue;
				if(f.isDead())continue;
				if(f == fighter)continue;
				if(f.getTeam2() != fighter.getTeam2())
				{
					ennemy.put(f.getPDV(), f);
				}
			}
			int i = 0, i2 = ennemy.size();
			int curHP = 1000000000;
			while ( i < i2)
			{
				try{
				curHP = 1000000000;
				for(Entry<Integer, Fighter> t : ennemy.entrySet())
				{
					if(t.getValue()==null)continue;
					if (t.getValue().getPDV() < curHP)
						curHP = t.getValue().getPDV();
				//TODO: J'ai cherché, j'ai rien trouver à faire :o
					}
				Fighter test = ennemy.get(curHP);
				if(test==null)break;
				list.put(test.getPDV(), test);
				ennemy.remove(curHP);
				i++;
				}catch(NullPointerException e){break;}//Avec mon calcul on arriverait à cette ligne...
			}
			return list;
		}
		
		
		private static int attackIfPossible(Fight fight, Fighter fighter)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
		{	
			try{
			if(fight==null||fighter==null)return 0;
			Map<Integer,Fighter> ennemyList = getLowHpEnnemyList(fight,fighter);
			if(ennemyList==null)return 0;
			if(ennemyList.isEmpty())return 0;
			SortStats SS = null;
			Fighter target = null;
			for(Entry<Integer, Fighter> t : ennemyList.entrySet()) // pour chaque ennemi on cherche le meilleur sort
			{
				SS = getBestSpellForTarget(fight,fighter,t.getValue());
				if(SS != null) // s'il existe un sort pour un ennemi, on le prend pour cible
				{
					target = t.getValue();
					break;
				}
			}
			int curTarget = 0,cell = 0;
			SortStats SS2 = null;
			for(Entry<Integer, SortStats> S : fighter.getMob().getSpells().entrySet()) // pour chaque sort du mob
			{
				int targetVal = getBestTargetZone(fight,fighter,S.getValue(),fighter.get_fightCell().getID()); // on détermine le meilleur
				if(targetVal == -1 || targetVal == 0) // endroit pour lancer le sort de zone (ou non)
					continue;
				int nbTarget = targetVal / 1000;
				int cellID = targetVal - nbTarget * 1000;
				if(nbTarget > curTarget)
				{
					curTarget = nbTarget;
					cell = cellID;
					SS2 = S.getValue();
				}
			}
			if(curTarget > 0 && cell > 0 && cell < 480 && SS2 != null) // si la case sélectionnée est valide et qu'il y a au moins une cible
			{
				int attack = fight.tryCastSpell(fighter, SS2, cell);
				if(attack != 0)
					return attack;
			}
			else
			{
				if(target == null || SS == null)
					return 666;
				int attack = fight.tryCastSpell(fighter, SS, target.get_fightCell().getID());
				if(attack != 0)
					return attack;			
			}
			return 0;
			}catch(NullPointerException e){Ancestra.printDebug("Connardj'suislà");return 0;}
		}
		
		 @SuppressWarnings("rawtypes")
		private static int attackIfPossiblePerco(Fight fight, Fight.Fighter fighter)
		    {
		    	Map<Integer,Fighter> ennemyList = getLowHpEnnemyList(fight,fighter);
				SortStats SS = null;
				Fighter target = null;
				for(Entry<Integer, Fighter> t : ennemyList.entrySet())
		      {
		        SS = getBestSpellForTargetPerco(fight, fighter, (Fight.Fighter)t.getValue());
		        if (SS == null)
		          continue;
		        target = (Fight.Fighter)t.getValue();
		        break;
		      }

		      int curTarget = 0; int cell = 0;
		      SortStats SS2 = null;
		      for (Map.Entry S : World.getGuild(fighter.getPerco().GetPercoGuildID()).getSpells().entrySet())
		      {
		        if (S.getValue() != null) {
		          int targetVal = getBestTargetZone(fight, fighter, (SortStats)S.getValue(), fighter.get_fightCell().getID());
		          if ((targetVal == -1) || (targetVal == 0))
		            continue;
		          int nbTarget = targetVal / 1000;
		          int cellID = targetVal - nbTarget * 1000;
		          if (nbTarget <= curTarget)
		            continue;
		          curTarget = nbTarget;
		          cell = cellID;
		          SS2 = (SortStats)S.getValue();
		        }
		      }
		      if ((curTarget > 0) && (cell > 0) && (cell < 480) && (SS2 != null))
		      {
		        int attack = fight.tryCastSpell(fighter, SS2, cell);
		        if (attack != 0)
		          return attack;
		      }
		      else
		      {
		        if ((target == null) || (SS == null))
		          return 666;
		        int attack = fight.tryCastSpell(fighter, SS, target.get_fightCell().getID());
		        if (attack != 0)
		          return attack;
		      }
		      return 0;
		    }
		
		
		private static boolean moveToAttackIfPossible(Fight fight,Fighter fighter)
		{
			ArrayList<Integer> cells = Pathfinding.getListCaseFromFighter(fight,fighter);
			if(cells == null)
				return false;
			int distMin = Pathfinding.getDistanceBetween(fight.get_map(), fighter.get_fightCell().getID(), getNearestEnnemy(fight,fighter).get_fightCell().getID());
			ArrayList <SortStats> sorts = getLaunchableSort(fighter,fight,distMin);
			if(sorts == null)
				return false;
			ArrayList <Fighter> targets = getPotentialTarget(fight,fighter,sorts);
			if(targets == null)
				return false;
			
			int CellDest = 0;
			boolean found = false;
			for(int i : cells)
			{
				for(SortStats S : sorts)
				{
					for(Fighter T : targets)
					{
						if(fight.CanCastSpell(fighter,S,T.get_fightCell(),i))
						{
							CellDest = i;
							found = true;
						}
						int targetVal = getBestTargetZone(fight,fighter,S,i);
						if(targetVal > 0)
						{
							int nbTarget = targetVal / 1000;
							int cellID = targetVal - nbTarget * 1000;
							if(fight.get_map().getCase(cellID) != null)
							{
								if(fight.CanCastSpell(fighter,S,fight.get_map().getCase(cellID),i))
								{
									CellDest = i;
									found = true;
								}
							}
						}
						if(found)
							break;
					}
					if(found)
						break;
				}
				if(found)
					break;
			}
			if(CellDest == 0)
				return false;
			ArrayList<Case> path = Pathfinding.getShortestPathBetween(fight.get_map(),fighter.get_fightCell().getID(),CellDest, 0);
			if(path == null)return false;
			String pathstr = "";
			try{
			int curCaseID = fighter.get_fightCell().getID();
			int curDir = 0;
			for(Case c : path)
			{
				char d = Pathfinding.getDirBetweenTwoCase(curCaseID, c.getID(), fight.get_map(), true);
				if(d == 0)return false;//Ne devrait pas arriver :O
				if(curDir != d)
				{
					if(path.indexOf(c) != 0)
						pathstr += CryptManager.cellID_To_Code(curCaseID);
					pathstr += d;
				}
				curCaseID = c.getID();
			}
			if(curCaseID != fighter.get_fightCell().getID())
				pathstr += CryptManager.cellID_To_Code(curCaseID);
			}catch(Exception e){e.printStackTrace();};
			//Création d'une GameAction
			GameAction GA = new GameAction(0,1, "");
			GA._args = pathstr;
			boolean result = fight.fighterDeplace(fighter, GA);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			return result;
			
		}
		
		private static ArrayList <SortStats> getLaunchableSort(Fighter fighter,Fight fight,int distMin)
		{
			ArrayList <SortStats> sorts = new ArrayList <SortStats>();
			if(fighter.getMob() == null)
				return null;
			for(Entry<Integer, SortStats> S : fighter.getMob().getSpells().entrySet())
			{
				if(S.getValue().getPACost() > fighter.getCurPA(fight))//si PA insuffisant
					continue;
				//if(S.getValue().getMaxPO() + fighter.getCurPM(fight) < distMin && S.getValue().getMaxPO() != 0)// si po max trop petite
					//continue;
				if(!LaunchedSort.coolDownGood(fighter, S.getValue().getSpellID()))// si cooldown ok
					continue;
				if(S.getValue().getMaxLaunchbyTurn() - LaunchedSort.getNbLaunch(fighter, S.getValue().getSpellID()) <= 0 && S.getValue().getMaxLaunchbyTurn() > 0)// si nb tours ok
					continue;
				if(calculInfluence(fight, S.getValue(),fighter,fighter) >= 0)// si sort pas d'attaque
					continue;
				sorts.add(S.getValue());
			}
			ArrayList <SortStats> finalS = TriInfluenceSorts(fight, fighter,sorts);
			
			return finalS;
		}
		
		private static ArrayList <SortStats> TriInfluenceSorts(Fight fight, Fighter fighter, ArrayList <SortStats> sorts)
		{
			if(sorts == null)
				return null;
			
			ArrayList <SortStats> finalSorts = new ArrayList <SortStats>();
			Map <Integer,SortStats> copie = new TreeMap <Integer,SortStats>();
			for(SortStats S : sorts)
			{
				copie.put(S.getSpellID(), S);
			}
			
			int curInfl = 0;
			int curID = 0;
			
			while ( copie.size() > 0)
			{
				curInfl = 0;
				curID = 0;
				for(Entry<Integer, SortStats> S : copie.entrySet())
				{
					int infl = -calculInfluence(fight, S.getValue(),fighter,fighter);
					if (infl > curInfl)
					{
						curID = S.getValue().getSpellID();
						curInfl = infl;
					}
				}
				if(curID == 0 || curInfl == 0)
					break;
				finalSorts.add(copie.get(curID));
				copie.remove(curID);
			}
			
			return finalSorts;
		}
		
		private static ArrayList <Fighter> getPotentialTarget(Fight fight,Fighter fighter,ArrayList<SortStats> sorts)
		{
			try{
			ArrayList <Fighter> targets = new ArrayList <Fighter>();
			int distMax = 0;
			for(SortStats S : sorts)
			{
				if(S.getMaxPO() > distMax)
					distMax = S.getMaxPO();
			}
			distMax += fighter.getCurPM(fight);
			Map<Integer,Fighter> potentialsT = getLowHpEnnemyList(fight,fighter);
			if(potentialsT==null || potentialsT.isEmpty())return new ArrayList<Fighter>();
			for(Entry<Integer,Fighter> T : potentialsT.entrySet())
			{
				int dist = Pathfinding.getDistanceBetween(fight.get_map(), fighter.get_fightCell().getID(), T.getValue().get_fightCell().getID());
				if(dist < distMax)
					targets.add(T.getValue());
			}
			
			return targets;
			}catch(Exception e){return new ArrayList<Fighter>();}
		}
		
		private static SortStats getBestSpellForTarget(Fight fight, Fighter F,Fighter T)
		{
			int inflMax = 0;
			SortStats ss = null;
			for(Entry<Integer, SortStats> SS : F.getMob().getSpells().entrySet())
			{
				int curInfl = 0, Infl1 = 0, Infl2 = 0;
				int PA = F.getMob().getPA();
				int usedPA[] = {0,0};
				if(!fight.CanCastSpell(F, SS.getValue(), T.get_fightCell(), -1))continue;
				curInfl = calculInfluence(fight, SS.getValue(),F,T);
				if(curInfl == 0)continue;
				if(curInfl > inflMax)
				{
					ss = SS.getValue();
					usedPA[0] = ss.getPACost();
					Infl1 = curInfl;
					inflMax = Infl1;
				}
				
				for(Entry<Integer, SortStats> SS2 : F.getMob().getSpells().entrySet())
				{
					if( (PA - usedPA[0]) < SS2.getValue().getPACost())continue;
					if(!fight.CanCastSpell(F, SS2.getValue(), T.get_fightCell(), -1))continue;
					curInfl = calculInfluence(fight, SS2.getValue(),F,T);
					if(curInfl == 0)continue;
					if((Infl1 + curInfl) > inflMax)
					{
						ss = SS.getValue();
						usedPA[1] = SS2.getValue().getPACost();
						Infl2 = curInfl;
						inflMax = Infl1 + Infl2;
					}
					for(Entry<Integer, SortStats> SS3 : F.getMob().getSpells().entrySet())
					{
						if( (PA - usedPA[0] - usedPA[1]) < SS3.getValue().getPACost())continue;
						if(!fight.CanCastSpell(F, SS3.getValue(), T.get_fightCell(), -1))continue;
						curInfl = calculInfluence(fight, SS3.getValue(),F,T);
						if(curInfl == 0)continue;
						if((curInfl+Infl1+Infl2) > inflMax)
						{
							ss = SS.getValue();
							inflMax = curInfl + Infl1 + Infl2;
						}
					}				
				}			
			}
			return ss;
		}
		
		@SuppressWarnings("rawtypes")
		private static SortStats getBestSpellForTargetPerco(Fight fight, Fight.Fighter F, Fight.Fighter T)
	    {
	      int inflMax = 0;
	      SortStats ss = null;
	      Ancestra.printDebug("SIZE SPELL : " + World.getGuild(F.getPerco().GetPercoGuildID()).getSpells().size());
	      for (Map.Entry SS : World.getGuild(F.getPerco().GetPercoGuildID()).getSpells().entrySet())
	      {
	        if (SS.getValue() != null) {
	          int curInfl = 0; int Infl1 = 0; int Infl2 = 0;
	          int PA = 6;
	          int[] usedPA = new int[2];
	          if (fight.CanCastSpell2(F, (SortStats)SS.getValue(), F.get_fightCell(), T.get_fightCell().getID())) {
	            curInfl = calculInfluence((SortStats)SS.getValue(), F, T);
	            if (curInfl != 0) {
	              if (curInfl > inflMax)
	              {
	                ss = (SortStats)SS.getValue();
	                usedPA[0] = ss.getPACost();
	                Infl1 = curInfl;
	                inflMax = Infl1;
	              }

	              for (Map.Entry SS2 : World.getGuild(F.getPerco().GetPercoGuildID()).getSpells().entrySet())
	              {
	                if ((PA - usedPA[0] < ((SortStats)SS2.getValue()).getPACost()) ||
	                  (!fight.CanCastSpell2(F, (SortStats)SS2.getValue(), F.get_fightCell(), T.get_fightCell().getID()))) continue;
	                curInfl = calculInfluence((SortStats)SS2.getValue(), F, T);
	                if (curInfl != 0) {
	                  if (Infl1 + curInfl > inflMax)
	                  {
	                    ss = (SortStats)SS.getValue();
	                    usedPA[1] = ((SortStats)SS2.getValue()).getPACost();
	                    Infl2 = curInfl;
	                    inflMax = Infl1 + Infl2;
	                  }
	                  for (Map.Entry SS3 : World.getGuild(F.getPerco().GetPercoGuildID()).getSpells().entrySet())
	                  {
	                    if ((PA - usedPA[0] - usedPA[1] < ((SortStats)SS3.getValue()).getPACost()) ||
	                      (!fight.CanCastSpell2(F, (SortStats)SS3.getValue(), F.get_fightCell(), T.get_fightCell().getID()))) continue;
	                    curInfl = calculInfluence((SortStats)SS3.getValue(), F, T);
	                    if ((curInfl == 0) ||
	                      (curInfl + Infl1 + Infl2 <= inflMax))
	                      continue;
	                    ss = (SortStats)SS.getValue();
	                    inflMax = curInfl + Infl1 + Infl2;
	                  }
	                }
	              }
	            }
	          }
	        }
	      }
	      return ss;
	    }

		private static int getBestTargetZone(Fight fight,Fighter fighter,SortStats spell,int launchCell)
		{
			if(spell.getPorteeType().isEmpty() || (spell.getPorteeType().charAt(0) == 'P' && spell.getPorteeType().charAt(1) == 'a'))
			{
				return 0;
			}
			ArrayList<Case> possibleLaunch = new ArrayList<Case>();
			int CellF = -1;
			if(spell.getMaxPO() != 0)
			{
				char arg1 = 'a';
				if(spell.isLineLaunch())
				{	
					arg1 = 'X';
				}
				else
				{
					arg1 = 'C';
				}
				char[] table = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v'};
				char arg2 = 'a';
				if(spell.getMaxPO() > 20)
				{
					arg2 = 'u';
				}
				else
				{
					arg2 = table[spell.getMaxPO()];
				}
				String args = Character.toString(arg1) + Character.toString(arg2);
				possibleLaunch = Pathfinding.getCellListFromAreaString(fight.get_map(),launchCell,launchCell,args,0,false);
			}
			else
			{
				possibleLaunch.add(fight.get_map().getCase(launchCell));
			}
			
			if(possibleLaunch == null)
			{
				return -1;
			}
			int nbTarget = 0;	
			for(Case cell : possibleLaunch)
			{
				try{
					if(!fight.CanCastSpell(fighter, spell, cell, launchCell))
						continue;
					int num = 0;
					int curTarget = 0;
					ArrayList<SpellEffect> test = new ArrayList<SpellEffect>();
					test.addAll(spell.getEffects());
					
					for(SpellEffect SE : test)
					{
						try{
							if(SE == null)
								continue;
							if(SE.getValue() == -1)
								continue;
							int POnum = num *2;
							ArrayList<Case> cells = Pathfinding.getCellListFromAreaString(fight.get_map(),cell.getID(),launchCell,spell.getPorteeType(),POnum,false);
							for(Case c : cells)
							{
								if(c.getFirstFighter() == null)
									continue;
								if(c.getFirstFighter().getTeam2() != fighter.getTeam2())
									curTarget++;
							}
						}catch(Exception e){};
						num++;
					}
					if(curTarget > nbTarget)
					{
						nbTarget = curTarget;
						CellF = cell.getID();
					}
				}
				catch(Exception E){}
			}
			if(nbTarget > 0 && CellF != -1)	
				return CellF + nbTarget * 1000;
			else
				return 0;
		}
		
		private static int calculInfluenceHeal(SortStats ss)
		{
			int inf = 0;
			for(SpellEffect SE : ss.getEffects())
			{
				if(SE.getEffectID() != 108)return 0;			
				inf += 100 * Formulas.getMiddleJet(SE.getJet());
			}
			
			return inf;
		}
		
		private static int calculInfluence(Fight fight, SortStats ss,Fighter C,Fighter T)
		{		
			//FIXME TODO
			int infTot = 0;
			double fact = 1;
			int num = 0, POnum = 0;
			int allies = 0, ennemies = 0;
			for(SpellEffect SE : ss.getEffects())
			{
				allies = 0;
				ennemies = 0;
				POnum = 2*num;
				/**Détermine à qui s'applique l'effet**/
				ArrayList<Case> cells = Pathfinding.getCellListFromAreaString(fight.get_map(),T.get_fightCell().getID(),C.get_fightCell().getID(),ss.getPorteeType(),POnum,false);
				ArrayList<Case> finalCells = new ArrayList<Case>();
				int TE = 0;
				Sort S = ss.getSpell();
				//on prend le targetFlag corespondant au num de l'effet
				//si on peut
				if(S!= null?S.getEffectTargets().size()>num:false)
					TE = S.getEffectTargets().get(num);
				
				for(Case C1 : cells)
				{
					if(C1 == null)continue;
					Fighter F = C1.getFirstFighter();
					if(F == null)continue;
					//Ne touche pas les alliés
					if(((TE & 1) == 1) && (F.getTeam() == C.getTeam()))continue;
					//Ne touche pas le lanceur
					if((((TE>>1) & 1) == 1) && (F.getGUID() == C.getGUID()))continue;
					//Ne touche pas les ennemies
					if((((TE>>2) & 1) == 1) && (F.getTeam() != C.getTeam()))continue;
					//Ne touche pas les combatants (seulement invocations)
					if((((TE>>3) & 1) == 1) && (!F.isInvocation()))continue;
					//Ne touche pas les invocations
					if((((TE>>4) & 1) == 1) && (F.isInvocation()))continue;
					//N'affecte que le lanceur
					if((((TE>>5) & 1) == 1) && (F.getGUID() != C.getGUID()))continue;
					//Si pas encore eu de continue, on ajoute la case
					finalCells.add(C1);
				}
				//Si le sort n'affecte que le lanceur et que le lanceur n'est pas dans la zone
				if(((TE>>5) & 1) == 1)
					if(!finalCells.contains(C.get_fightCell()))
						finalCells.add(C.get_fightCell());
				ArrayList<Fighter> cibles = SpellEffect.getTargets(SE,fight,finalCells);
				for (Fighter fighter : cibles)
				{
					if (fighter.getTeam() == C.getTeam())
						allies++;
					else ennemies++;
				}
				num++;
				/**Fin de la détermination**/
				//Ancestra.printDebug("SpellEffect : "+SE.getEffectID()+"   Nbr ennemis : "+ennemies+"   Nbr alliés : "+allies);
				int inf = 0;
				switch(SE.getEffectID())
				{
					case 5 ://repousse de X cases
						inf = 100;
						fact += 0.3 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 89://dommages % vie neutre
						inf = 500 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 91://Vol de Vie Eau
						inf = 200 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 92://Vol de Vie Terre
						inf = 200 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 93://Vol de Vie Air
						inf = 200 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 94://Vol de Vie feu
						inf = 200 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 95://Vol de Vie neutre
						inf = 200 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 96://Dommage Eau
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 97://Dommage Terre
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 98://Dommage Air
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 99://Dommage feu
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 100://Dommage neutre
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 101://retrait PA
						inf = 5000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 127://retrait PM
						inf = 3000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 84://vol PA
						inf = 8000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 77://vol PM
						inf = 4000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 108:// soin
						inf = -100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 111://+ PA
						inf = -5000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 128://+ PM
						inf = -3000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 121://+ Dom
						inf = -500 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 131://poison X pdv par PA
						inf = 300 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 132://désenvoute
						inf = 6000;
					break;
					case 138://+ %Dom
						inf = -150 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 150://invisibilité
						inf = -5000;
					break;
					case 168://retrait PA non esquivable
						inf = 10000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 169://retrait PM non esquivable
						inf = 8000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 210://résistance
						inf = -100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 211://résistance
						inf = -100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 212://résistance
						inf = -100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 213://résistance
						inf = -100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 214://résistance
						inf = -100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 215://faiblesse
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 216://faiblesse
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 217://faiblesse
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 218://faiblesse
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 219://faiblesse
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 265://réduction dommage
						inf = -250 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 765://sacrifice
						inf = -50000;
					break;
						
					
				}
				if(C.getTeam() == T.getTeam())//Si Amis
					infTot -= inf*(allies-ennemies);
				else//Si ennemis
					infTot += inf*(ennemies-allies);
			}
			return (int) (((double)infTot)*fact);
		}
	
private static int calculInfluence(SortStats ss, Fight.Fighter C, Fight.Fighter T)
    {
      int infTot = 0;
      for (SpellEffect SE : ss.getEffects())
      {
        int inf = 0;
        switch (SE.getEffectID())
        {
        case 5:
          inf = 500 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 89:
          inf = 200 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 91:
          inf = 150 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 92:
          inf = 150 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 93:
          inf = 150 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 94:
          inf = 150 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 95:
          inf = 150 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 96:
          inf = 100 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 97:
          inf = 100 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 98:
          inf = 100 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 99:
          inf = 100 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 100:
          inf = 100 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 101:
          inf = 1000 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 127:
          inf = 1000 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 84:
          inf = 1500 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 77:
          inf = 1500 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 111:
          inf = -1000 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 128:
          inf = -1000 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 121:
          inf = -100 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 131:
          inf = 300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 132:
          inf = 2000;
          break;
        case 138:
          inf = -50 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 150:
          inf = -2000;
          break;
        case 168:
          inf = 1000 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 169:
          inf = 1000 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 210:
          inf = -300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 211:
          inf = -300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 212:
          inf = -300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 213:
          inf = -300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 214:
          inf = -300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 215:
          inf = 300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 216:
          inf = 300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 217:
          inf = 300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 218:
          inf = 300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 219:
          inf = 300 * Formulas.getMiddleJet(SE.getJet());
          break;
        case 265:
          inf = -250 * Formulas.getMiddleJet(SE.getJet());
        }

        if (C.getTeam() == T.getTeam())
          infTot -= inf;
        else
          infTot += inf;
      }
      return infTot;
    }
}

}

