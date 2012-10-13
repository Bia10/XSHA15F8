package common;

//Commands Console update by Return | Porky
import game.GameServer;
import game.GameThread;
import game.GameServer.SaveThread;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.Map.Entry;

import javax.swing.Timer;

import common.Console.ConsoleColorEnum;
import common.World.ItemSet;

import objects.Action;
import objects.Carte;
import objects.Compte;
import objects.NPC_tmpl;
import objects.Objet;
import objects.Personnage;
import objects.Tickets;
import objects.Carte.MountPark;
import objects.HDV.HdvEntry;
import objects.Metier.StatsMetier;
import objects.Monstre.MobGroup;
import objects.NPC_tmpl.NPC;
import objects.NPC_tmpl.NPC_question;
import objects.NPC_tmpl.NPC_reponse;
import objects.Objet.ObjTemplate;

public class Commands {
	Compte _compte;
	Personnage _perso;
	PrintWriter _out;
	//Sauvegarde
	private boolean _TimerStart = false;
	Timer _timer;
	
	private Timer createTimer(final int time)
	{
	    ActionListener action = new ActionListener ()
	      {
	    	int Time = time;
	        public void actionPerformed (ActionEvent event)
	        {
	        	Time = Time-1;
	        	if(Time == 1)
	        	{
	        		SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;"+Time+" minute");
	        	}else
	        	{
		        	SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;"+Time+" minutes");
	        	}
	        	if(Time <= 0)
	        	{
	        		for(Personnage perso : World.getOnlinePersos())
	        		{
	        			perso.get_compte().getGameThread().kick();
	        		}
	    			System.exit(0);
	        	}
	        }
	      };
	    // Génération du repeat toutes les minutes.
	    return new Timer (60000, action);//60000
	}
	
	public Commands(Personnage perso)
	{
		this._compte = perso.get_compte();
		this._perso = perso;
		this._out = _compte.getGameThread().get_out();
	}
	
	public static GameServer gameServer;
	public void consoleCommand(String packet)
	{

		if(_compte.get_gmLvl() < 1)
		{
			_compte.getGameThread().closeSocket();
			return;
		}
		
		String msg = packet.substring(2);
		String[] infos = msg.split(" ");
		if(infos.length == 0)return;
		String command = infos[0];
		
		if(Ancestra.canLog)
		{
			Ancestra.addToMjLog(msg+" <="+_compte.get_curIP()+" : "+_compte.get_name()+" / "+_perso.get_name());
		}
		
		if(_compte.get_gmLvl() == 1)
		{
			commandGmOne(command, infos, msg);
		}else
		if(_compte.get_gmLvl() == 2)
		{
			commandGmTwo(command, infos, msg);
		}
		else
		if(_compte.get_gmLvl() == 3)
		{
			commandGmThree(command, infos, msg);
		}
		else
		if(_compte.get_gmLvl() == 4)
		{
			commandGmFour(command, infos, msg);
		}
		else
		if(_compte.get_gmLvl() >= 5)
		{
			commandGmFive(command, infos, msg);
		}
	}
	
	public void commandGmOne(String command, String[] infos, String msg)
	{
	if(command.equalsIgnoreCase("HELP"))
	{
		String mess1 =	"Vous avez actuellement le niveau GM "+_compte.get_gmLvl()+". --- Commandes disponibles:<br />";
		SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess1);
		
		
		if (_compte.get_gmLvl() == 1)
		{
			String mess =	"<u>Rechargement</u>"+" - Recharge les items + personnages non joueurs \n"
				+			"<u>Infos</u>"+" - Affiche l'uptime + informations diverses\n"
				+			"<u>Refreshmobs</u>"+" - Raffraîchis tous les monstres sur la carte\n"
				+			"<u>Mapinfo</u>"+" - Affiche les monstres et PNJ sur la carte\n"
				+			"<u>Who</u>"+" - Affiche la liste des joueurs connectés\n"
				+			"<u>Nameannounce + [TEXTE]</u>"+" - Faire une annonce dans tout le serveur\n"
				+			"<u>Mute + [Pseudo] + [Time en seconde]</u>"+" - Pour muter un joueur\n"
				+			"<u>Unmute + [Pseudo]</u>"+" - Redonne la parole à un joueur\n"
				+			"<u>Demorph + [Pseudo]</u>"+" - Rendre l'apparence par défault\n"
				+			"<u>Join + [Pseudo]</u>"+" - Se téléporter à un joueur\n"
				+			"<u>Kick + [Pseudo]</u>"+" - Kick le joueur du jeu\n"
				+			"<u>Size + [Nombre] + [Pseudo]</u>"+" - Change la taille d'un joueur\n"
				+			"<u>Morph + [MorphID] + [Pseudo]</u>"+" - Transforme un joueur\n"
				+			"<u>Namego + [Pseudo]</u>"+" - Apporte un joueur à vous\n"
				+			"<u>Teleport [mapID] [cellID]</u>"+" - Téléporte le joueur à un endroit défini\n"
				+			"<u>Gomap + [posX] + [posY] + [cellID]</u>"+" - Téléporte à un endroit défini\n"
				+			"<u>Boutique</u>"+" - Distribue les lives_actions (boutique)\n"
				+			"<u>Ticket</u>"+" - Vous téléporte et montre le ticket d'un joueur en besoin d'aide\n"
				+			"<u>Delticket</u>"+" - Suprime votre ticket que vous venez d'effectuer\n"
				+			"<u>TICKETLISTE</u>"+" - Affiche les tickets en cours et en attente\n";
		SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
		return;
		}
		
		if (_compte.get_gmLvl() == 2)
		{
			String mess =	"<u>Rechargement</u>"+" - Recharge les items + personnages non joueurs \n"
				+			"<u>Infos</u>"+" - Affiche l'uptime + informations diverses\n"
				+			"<u>Refreshmobs</u>"+" - Raffraîchis tous les monstres sur la carte\n"
				+			"<u>Mapinfo</u>"+" - Affiche les monstres et PNJ sur la carte\n"
				+			"<u>Who</u>"+" - Affiche la liste des joueurs connectés\n"
				+			"<u>Nameannounce + [TEXTE]</u>"+" - Faire une annonce dans tout le serveur\n"
				+			"<u>Mute + [Pseudo] + [Time en seconde]</u>"+" - Pour muter un joueur\n"
				+			"<u>Unmute + [Pseudo]</u>"+" - Redonne la parole à un joueur\n"
				+			"<u>Demorph + [Pseudo]</u>"+" - Rendre l'apparence par défault\n"
				+			"<u>Join + [Pseudo]</u>"+" - Se téléporter à un joueur\n"
				+			"<u>Kick + [Pseudo]</u>"+" - Kick le joueur du jeu\n"
				+			"<u>Size + [Nombre] + [Pseudo]</u>"+" - Change la taille d'un joueur\n"
				+			"<u>Morph + [MorphID] + [Pseudo]</u>"+" - Transforme un joueur\n"
				+			"<u>Namego + [Pseudo]</u>"+" - Apporte un joueur à vous\n"
				+			"<u>Teleport [mapID] [cellID]</u>"+" - Téléporte le joueur à un endroit défini\n"
				+			"<u>Gomap + [posX] + [posY] + [cellID]</u>"+" - Téléporte à un endroit défini\n"
				+			"<u>Ban + [Pseudo] + [Durée] + [Raison]</u>"+" - Bannir un joueur avec une durée\n"
				+			"<u>Boutique</u>"+" - Distribue les lives_actions (boutique)\n"
				+			"<u>Title + [Pseudo] + [TitleID]</u>"+" - Donner un titre à un joueur \n"
				+			"<u>Ticket</u>"+" - Vous téléporte et montre le ticket d'un joueur en besoin d'aide\n"
				+			"<u>Delticket</u>"+" - Suprime votre ticket que vous venez d'effectuer\n"
				+			"<u>TICKETLISTE</u>"+" - Affiche les tickets en cours et en attente\n"
				+			"<u>Jail [Pseudo]</u> - Ajoute 1 Jail au quota du joueur et l'envoie en prison muté\n"
				+			"<u>Unjail [Pseudo]</u> - Unmute et téléporte le joueur à la zone de départ\n"
				+			"<u>Deljail [Pseudo]</u> - Remets le quota de Jail du joueur à 0\n"
				+			"<u>Viewjail [Pseudo]</u> - Affiche le quota de jail d'un joueur\n";
			
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			return;
		}
		
		if (_compte.get_gmLvl() == 3)
		{
			String mess ="<u>Rechargement</u>"+" - Recharge les items + personnages non joueurs \n"
			+			"<u>Infos</u>"+" - Affiche l'uptime + informations diverses\n"
			+			"<u>Refreshmobs</u>"+" - Raffraîchis tous les monstres sur la carte\n"
			+			"<u>Mapinfo</u>"+" - Affiche les monstres et PNJ sur la carte\n"
			+			"<u>Who</u>"+" - Affiche la liste des joueurs connectés\n"
			+			"<u>Nameannounce + [TEXTE]</u>"+" - Faire une annonce dans tout le serveur\n"
			+			"<u>Mute + [Pseudo] + [Time en seconde]</u>"+" - Pour muter un joueur\n"
			+			"<u>Unmute + [Pseudo]</u>"+" - Redonne la parole à un joueur\n"
			+			"<u>Demorph + [Pseudo]</u>"+" - Rendre l'apparence par défault\n"
			+			"<u>Join + [Pseudo]</u>"+" - Se téléporter à un joueur\n"
			+			"<u>Kick + [Pseudo]</u>"+" - Kick le joueur du jeu\n"
			+			"<u>Size + [Nombre] + [Pseudo]</u>"+" - Change la taille d'un joueur\n"
			+			"<u>Morph + [MorphID] + [Pseudo]</u>"+" - Transforme un joueur\n"
			+			"<u>Namego + [Pseudo]</u>"+" - Apporte un joueur à vous\n"
			+			"<u>Teleport [mapID] [cellID]</u>"+" - Téléporte le joueur à un endroit défini\n"
			+			"<u>Gomap + [posX] + [posY] + [cellID]</u>"+" - Téléporte à un endroit défini\n"
			+			"<u>Ban + [Pseudo] + [Durée] + [Raison]</u>"+" - Bannir un joueur avec une durée\n"
			+			"<u>Boutique</u>"+" - Distribue les lives_actions (boutique)\n"
			+			"<u>Title + [Pseudo] + [TitleID]</u>"+" - Donner un titre à un joueur \n"
			+			"<u>Exit</u>"+" -  Lance une sauvegarde et relance le serveur\n"
			+			"<u>Save</u>"+" -  Lance une sauvegarde du serveur\n"				
			+			"<u>Ticket</u>"+" - Vous téléporte et montre le ticket d'un joueur en besoin d'aide\n"
			+			"<u>Delticket</u>"+" - Suprime votre ticket que vous venez d'effectuer\n"
			+			"<u>TICKETLISTE</u>"+" - Affiche les tickets en cours et en attente\n"
			+			"<u>Vitalite [Nombre] [Pseudo]</u> - Ajoute des points de vitalité au personnage\n"
			+			"<u>Intelligence [Nombre] [Pseudo]</u> - Ajoute des points d'intelligence au personnage\n"
			+			"<u>Agilite [Nombre] [Pseudo]</u> - Ajoute des points d'agilité au personnage\n"
			+			"<u>Force [Nombre] [Pseudo]</u> - Ajoute des points de force au personnage\n"
			+			"<u>Chance [Nombre] [Pseudo]</u> - Ajoute des points de chance au personnage\n"
			+			"<u>Sagesse [Nombre] [Pseudo]</u> - Ajoute des points de sagesse au personnage\n"
			+			"<u>Vitalite [Nombre] [Pseudo]</u> - Ajoute des points de vitalité au personnage\n"
			+			"<u>Pa [Nombre] [Pseudo]</u> - Ajoute de PA jusqu'au prochain reboot au personnage\n"
			+			"<u>Pm [Nombre] [Pseudo]</u> - Ajoute des PM jusqu'au prochain reboot au personnage\n"
			+			"<u>Jail [Pseudo]</u> - Ajoute 1 Jail au quota du joueur et l'envoie en prison muté\n"
			+			"<u>Unjail [Pseudo]</u> - Unmute et téléporte le joueur à la zone de départ\n"
			+			"<u>Deljail [Pseudo]</u> - Remets le quota de Jail du joueur à 0\n"
			+			"<u>Viewjail [Pseudo]</u> - Affiche le quota de jail d'un joueur\n";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
				return;
		}
		
		if (_compte.get_gmLvl() == 4)
		{
			String mess =	"<u>Rechargement</u>"+" - Recharge les items + personnages non joueurs \n"
			+			"<u>Infos</u>"+" - Affiche l'uptime + informations diverses\n"
			+			"<u>Refreshmobs</u>"+" - Raffraîchis tous les monstres sur la carte\n"
			+			"<u>Mapinfo</u>"+" - Affiche les monstres et PNJ sur la carte\n"
			+			"<u>Who</u>"+" - Affiche la liste des joueurs connectés\n"
			+			"<u>Nameannounce + [TEXTE]</u>"+" - Faire une annonce dans tout le serveur\n"
			+			"<u>Mute + [Pseudo] + [Time en seconde]</u>"+" - Pour muter un joueur\n"
			+			"<u>Unmute + [Pseudo]</u>"+" - Redonne la parole à un joueur\n"
			+			"<u>Demorph + [Pseudo]</u>"+" - Rendre l'apparence par défault\n"
			+			"<u>Join + [Pseudo]</u>"+" - Se téléporter à un joueur\n"
			+			"<u>Kick + [Pseudo]</u>"+" - Kick le joueur du jeu\n"
			+			"<u>Size + [Nombre] + [Pseudo]</u>"+" - Change la taille d'un joueur\n"
			+			"<u>Morph + [MorphID] + [Pseudo]</u>"+" - Transforme un joueur\n"
			+			"<u>Namego + [Pseudo]</u>"+" - Apporte un joueur à vous\n"
			+			"<u>Teleport [mapID] [cellID]</u>"+" - Téléporte le joueur à un endroit défini\n"
			+			"<u>Gomap + [posX] + [posY] + [cellID]</u>"+" - Téléporte à un endroit défini\n"
			+			"<u>Ban + [Pseudo] + [Durée] + [Raison]</u>"+" - Bannir un joueur avec une durée\n"
			+			"<u>Title + [Pseudo] + [TitleID]</u>"+" - Donner un titre à un joueur \n"
			+			"<u>Exit</u>"+" -  Lance une sauvegarde et relance le serveur\n"
			+			"<u>Save</u>"+" -  Lance une sauvegarde du serveur\n"
			+			"<u>Level + [Nombre] + [Pseudo]</u>"+" -  Ajoute des levels à un joueur\n"
			+			"<u>Kamas + [Nombre] + [Pseudo]</u>"+" -  \n"
			+			"<u>Boutique</u>"+" - Distribue les lives_actions (boutique)\n"
			+			"<u>Itemset + [PanoplieID] (+MAX?)</u>"+" -  Ajoute une panoplie à un joueur\n"
			+			"<u>Item + [itemID] + [Nombre] (+MAX?)</u>"+" -  Ajoute un objet à un joueur\n"
			+			"<u>Spellpoint + [Nombre] + [Pseudo]</u>"+" -  Ajoute des points de sort à un joueur\n"
			+			"<u>Capital</u>"+" -  Ajoute des points de capital à un joueur\n"
			+			"<u>Learnspell + [Nombre] + [Pseudo]</u>"+" -  Ajoute des points de sort à un joueur\n"
			+			"<u>Setalign + [1,2,3] + [Pseudo]</u>"+" -  Donne un alignement à un joueur\n"
			+			"<u>Honor + [Nombre] + [Pseudo]</u>"+" -  Donne des points d'honneur à un joueur\n"
			+			"<u>Ticket</u>"+" - Vous téléporte et montre le ticket d'un joueur en besoin d'aide\n"
			+			"<u>Delticket</u>"+" - Suprime votre ticket que vous venez d'effectuer\n"
			+			"<u>TICKETLISTE</u>"+" - Affiche les tickets en cours et en attente\n"
			+			"<u>Vitalite [Nombre] [Pseudo]</u> - Ajoute des points de vitalité au personnage\n"
			+			"<u>Intelligence [Nombre] [Pseudo]</u> - Ajoute des points d'intelligence au personnage\n"
			+			"<u>Agilite [Nombre] [Pseudo]</u> - Ajoute des points d'agilité au personnage\n"
			+			"<u>Force [Nombre] [Pseudo]</u> - Ajoute des points de force au personnage\n"
			+			"<u>Chance [Nombre] [Pseudo]</u> - Ajoute des points de chance au personnage\n"
			+			"<u>Sagesse [Nombre] [Pseudo]</u> - Ajoute des points de sagesse au personnage\n"
			+			"<u>Vitalite [Nombre] [Pseudo]</u> - Ajoute des points de vitalité au personnage\n"
			+			"<u>Pa [Nombre] [Pseudo]</u> - Ajoute de PA jusqu'au prochain reboot au personnage\n"
			+			"<u>Pm [Nombre] [Pseudo]</u> - Ajoute des PM jusqu'au prochain reboot au personnage\n"
			+			"<u>Jail [Pseudo]</u> - Ajoute 1 Jail au quota du joueur et l'envoie en prison muté\n"
			+			"<u>Unjail [Pseudo]</u> - Unmute et téléporte le joueur à la zone de départ\n"
			+			"<u>Deljail [Pseudo]</u> - Remets le quota de Jail du joueur à 0\n"
			+			"<u>Viewjail [Pseudo]</u> - Affiche le quota de jail d'un joueur\n";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
				return;
		}
		
		if (_compte.get_gmLvl() >= 5)
		{
			String mess ="<u>Rechargement</u>"+" - Recharge les items + personnages non joueurs \n"
			+			"<u>Infos</u>"+" - Affiche l'uptime + informations diverses\n"
			+			"<u>Refreshmobs</u>"+" - Raffraîchis tous les monstres sur la carte\n"
			+			"<u>Mapinfo</u>"+" - Affiche les monstres et PNJ sur la carte\n"
			+			"<u>Who</u>"+" - Affiche la liste des joueurs connectés\n"
			+			"<u>Nameannounce + [TEXTE]</u>"+" - Faire une annonce dans tout le serveur\n"
			+			"<u>Mute + [Pseudo] + [Time en seconde]</u>"+" - Pour muter un joueur\n"
			+			"<u>Unmute + [Pseudo]</u>"+" - Redonne la parole à un joueur\n"
			+			"<u>Demorph + [Pseudo]</u>"+" - Rendre l'apparence par défault\n"
			+			"<u>Join + [Pseudo]</u>"+" - Se téléporter à un joueur\n"
			+			"<u>Kick + [Pseudo]</u>"+" - Kick le joueur du jeu\n"
			+			"<u>Size + [Nombre] + [Pseudo]</u>"+" - Change la taille d'un joueur\n"
			+			"<u>Morph + [MorphID] + [Pseudo]</u>"+" - Transforme un joueur\n"
			+			"<u>Namego + [Pseudo]</u>"+" - Apporte un joueur à vous\n"
			+			"<u>Teleport [mapID] [cellID]</u>"+" - Téléporte le joueur à un endroit défini\n"
			+			"<u>Gomap + [posX] + [posY] + [cellID]</u>"+" - Téléporte à un endroit défini\n"
			+			"<u>Ban + [Pseudo] + [Durée] + [Raison]</u>"+" - Bannir un joueur avec une durée\n"
			+			"<u>Title + [Pseudo] + [TitleID]</u>"+" - Donner un titre à un joueur \n"
			+			"<u>Exit</u>"+" -  Lance une sauvegarde et relance le serveur\n"
			+			"<u>Save</u>"+" -  Lance une sauvegarde du serveur\n"
			+			"<u>Level + [Nombre] + [Pseudo]</u>"+" -  Ajoute des levels à un joueur\n"
			+			"<u>Kamas + [Nombre] + [Pseudo]</u>"+" -  \n"
			+			"<u>Itemset + [PanoplieID] (+MAX?)</u>"+" -  Ajoute une panoplie à un joueur\n"
			+			"<u>Item + [itemID] + [Nombre] (+MAX?)</u>"+" -  Ajoute un objet à un joueur\n"
			+			"<u>Spellpoint + [Nombre] + [Pseudo]</u>"+" -  Ajoute des points de sort à un joueur\n"
			+			"<u>Capital</u>"+" -  Ajoute des points de capital à un joueur\n"
			+			"<u>Learnspell + [Nombre] + [Pseudo]</u>"+" -  Ajoute des points de sort à un joueur\n"
			+			"<u>Setalign + [1,2,3] + [Pseudo]</u>"+" -  Donne un alignement à un joueur\n"
			+			"<u>Honor + [Nombre] + [Pseudo]</u>"+" -  Donne des points d'honneur à un joueur\n"
			+			"<u>CreateGuild + [Pseudo]</u>"+" -  Permet de créer une guilde\n"
			+			"<u>Toogleaggro + [Pseudo]</u>"+" -  Etre invulnérable aux agressements\n"
			+			"<u>Announce + [TEXTE]</u>"+" -  Faire une announce dans le serveur\n"
			+			"<u>Unban + [Pseudo]</u>"+" -  Rendre accecible le compte à un joueur\n"	
			+			"<u>Setmaxgroup [Nombre]</u>"+" -  Définir le nombre de monstres par groupe\n"
			+			"<u>AddReponseAction + [RepID + [ID] + [Arg]</u>"+" -  Créer une RéponseActionID\n"
			+			"<u>SetInitQuestion + [Type] + [ID] + [Arg]</u>"+" -  Creér une RéponseActionID\n"
			+			"<u>AddEndFightAction + [Type] + [ID] + [Arg]</u>"+" -  Ajouter un EndFight\n"
			+			"<u>Addnpc + [NpcID]</u>"+" -  Ajouter un PNJ sur la map où l'on est\n"
			+			"<u>Delnpc + [NpcGuid]</u>"+" -  Supprimer un PNJ sur la map ou l'on est\n"
			+			"<u>AddNpcItem + [NpcGuid] + [ItemID]</u>"+" -  Ajouter un item à un PNJ\n"
			+			"<u>DelNpcItem + [NpcGuid] + [ItemID]</u>"+" -  Retirer un item à un PNJ\n"
			+			"<u>AddMountPark + [Size] + [Owner] + [Price]</u>"+" -  Créer un enclos SQL\n"
			+			"<u>Pdvper + [???%] + [Pseudo]</u>"+" -  Choisir le pourcentage de points de vie\n"
			+			"<u>LearnJob + [JobID] + [Pseudo]</u>"+" -  Apprendre un métier à un joueur\n"
			+			"<u>AddJobXp + [JobID] + [Nombre] + [Pseudo]</u>"+" -  Ajouter de l'XP à un métier\n"
			+			"<u>SetReponses + [QuestionID] + [RepsID]</u>"+" -  Créer une RéponseID\n"
			+			"<u>ShowReponses + [QuestionID]</u>"+" -  Voir les réponsesID\n"
			+			"<u>MoveNpc + [NpcGuid]</u>"+" -  Supprimer un PNJ de la BDD\n"
			+			"<u>AddFightPos + [0,1]</u>"+" -  Ajoute une cellule de combat sur la carte\n"
			+			"<u>DelFightPos + [0,1]</u>"+" -  Supprime une cellule de combat sur la carte\n"
			+			"<u>AddTrigger + [ActionID] + [Args]</u>"+" -  Ajoute un trigger sur la carte\n"
			+			"<u>DelTrigger + [mapID] + [cellID]</u>"+" -  Supprimer un trigger sur la carte\n"
			+			"<u>Spawn [GroupID] [LevelMin] [LevelMax]</u>"+" -  Créer un groupe de monstre non fixe\n"
			+			"<u>Spawnfix [GroupeID] [LevelMin] [LevelMax]</u>"+" -  Créer un groupe de monstre fixe\n"
			+			"<u>Showfightpos</u>"+" -  Voir les cellules de combat\n"
			+			"<u>Shutdown</u>"+" -  Faire un reboot avec un Timer\n"
			+			"<u>Boutique</u>"+" - Distribue les lives_actions (boutique)\n"
			+			"<u>Setadmin [GmLvl] [Pseudo]</u>"+" -  Modifier le GMLEVEL d'un joueur\n"				
			+			"<u>Ticket</u>"+" - Vous téléporte et montre le ticket d'un joueur en besoin d'aide\n"
			+			"<u>Delticket</u>"+" - Suprime votre ticket que vous venez d'effectuer\n"
			+			"<u>TICKETLISTE</u>"+" - Affiche les tickets en cours et en attente\n"
			+			"<u>Cadeau + [ID] + [Pseudo]</u>"+" - Ajoute un cadeau à la prochaine connexion d'un personnage\n"
			+			"<u>Allcadeau + [ID]</u>"+" - Ajoute un cadeau à la prochaine connexion de tous les personnages\n"
			+			"<u>Vitalite [Nombre] [Pseudo]</u> - Ajoute des points de vitalité au personnage\n"
			+			"<u>Intelligence [Nombre] [Pseudo]</u> - Ajoute des points d'intelligence au personnage\n"
			+			"<u>Agilite [Nombre] [Pseudo]</u> - Ajoute des points d'agilité au personnage\n"
			+			"<u>Force [Nombre] [Pseudo]</u> - Ajoute des points de force au personnage\n"
			+			"<u>Chance [Nombre] [Pseudo]</u> - Ajoute des points de chance au personnage\n"
			+			"<u>Sagesse [Nombre] [Pseudo]</u> - Ajoute des points de sagesse au personnage\n"
			+			"<u>Vitalite [Nombre] [Pseudo]</u> - Ajoute des points de vitalité au personnage\n"
			+			"<u>Pa [Nombre] [Pseudo]</u> - Ajoute de PA jusqu'au prochain reboot au personnage\n"
			+			"<u>Pm [Nombre] [Pseudo]</u> - Ajoute des PM jusqu'au prochain reboot au personnage\n"
			+			"<u>Jail [Pseudo]</u> - Ajoute 1 Jail au quota du joueur et l'envoie en prison muté\n"
			+			"<u>Unjail [Pseudo]</u> - Unmute et téléporte le joueur à la zone de départ\n"
			+			"<u>Deljail [Pseudo]</u> - Remets le quota de Jail du joueur à 0\n"
			+			"<u>Viewjail [Pseudo]</u> - Affiche le quota de jail d'un joueur\n";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
				return;
		}
		
	}else
	
//TODO: Commandes d'accès pour GM 1
		if(_compte.get_gmLvl() < 1)
		{
			_compte.getGameThread().closeSocket();
			return;
		}
	if(command.equalsIgnoreCase("BOUTIQUE"))
	{

		
		SQLManager.LOAD_ACTION();
		SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Items boutiques distribués");
		GameServer.addToLog("Les live actions ont ete appliquees");
		
	}else
	
	if(command.equalsIgnoreCase("DEMORPH"))
	{
		Personnage target = _perso;
		if(infos.length > 1)//Si un nom de perso est spécifié
		{
			target = World.getPersoByName(infos[1]);
			if(target == null)
			{
				String str = "Le personnage n'a pas ete trouve";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
		}
		int morphID = target.get_classe()*10 + target.get_sexe();
		target.set_gfxID(morphID);
		SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.get_curCarte(), target.get_GUID());
		SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(target.get_curCarte(), target);
		String str = "Le joueur a ete transforme";
		SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
	}
	else
	if(command.equalsIgnoreCase("TICKET"))
    {
        if(_compte.get_gmLvl() < 1)
        {
       	
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Vous n'avez pas le niveau MJ requis");
            return;
        }
        	Tickets ticket = SQLManager.SelectTicket();
        	if(ticket.getID() != 0)
        	{
	            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, ticket.getTicket());
	            Personnage persoj = World.getPersoByName(ticket.getJoueur());
		        SQLManager.updateticketencour(ticket.getJoueur(), _perso.get_name());
		        SocketManager.GAME_SEND_MESSAGE(persoj,"Votre ticket à été  assigné au Maitre de jeu : "+_perso.get_name(), Ancestra.COLOR_BLEU2);
		        _perso.teleport(persoj.get_curCarte().get_id(), persoj.get_curCell().getID());
		        return;
        	}
        	else
        	{
        		SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Aucun ticket en cours");
        		return;
        	}
    }else
	if(command.equalsIgnoreCase("DELTICKET"))
	{
        if(_compte.get_gmLvl() < 1)
        {
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Vous n'avez pas le niveau MJ requis");
            return;
        } 
        else
        {
            SQLManager.updateticketfini(_perso.get_name());
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Vous venez de validé le ticket en cour");
            return;
        }
	}else
	if(command.equalsIgnoreCase("TICKETLISTE"))
        if(_compte.get_gmLvl() < 1)
        {
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Vous n'avez pas le niveau MJ requis");
            return;
        } 
        else
        {
            String ticket = SQLManager.listeticket();
            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, ticket);
            return;
        }
		 if(command.equalsIgnoreCase("Rechargement"))
         {
                 try
                 {
                         SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Début du chargement :");
                         SQLManager.LOAD_ITEMS_FULL();
                         SQLManager.LOAD_NPC_TEMPLATE();
                         SQLManager.LOAD_NPC_QUESTIONS();
                         SQLManager.LOAD_NPC_ANSWERS();
                         SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Chargement terminé");
                       
                         
                 }
                 catch(Exception exception) { }
                 return;
         }

		if(command.equalsIgnoreCase("INFOS"))
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
					+			"OnEmuR by Return\n"
					+			"Uptime: "+jour+"d "+hour+"h "+min+"m "+sec+"s\n"
					+			"Joueurs En Ligne: "+Ancestra.gameServer.getPlayerNumber()+"\n"
					+			"Record de Connexions: "+Ancestra.gameServer.getMaxPlayer()+"\n"
					+			"===========";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			return;
		}else
		if(command.equalsIgnoreCase("REFRESHMOBS"))
		{
			_perso.get_curCarte().refreshSpawns();
			String mess = "Mob Spawn refreshed!";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			return;
		}if(command.equalsIgnoreCase("MAPINFO"))
		{
			String mess = 	"==========\n"
						+	"Liste des Npcs de la carte:";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			Carte map = _perso.get_curCarte();
			for(Entry<Integer,NPC> entry : map.get_npcs().entrySet())
			{
				mess = entry.getKey()+" "+entry.getValue().get_template().get_id()+" "+entry.getValue().get_cellID()+" "+entry.getValue().get_template().get_initQuestionID();
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			}
			mess = "Liste des groupes de monstres:";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			for(Entry<Integer,MobGroup> entry : map.getMobGroups().entrySet())
			{
				mess = entry.getKey()+" "+entry.getValue().getCellID()+" "+entry.getValue().getAlignement()+" "+entry.getValue().getSize();
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			}
			mess = "==========";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			return;
		}else
		if(command.equalsIgnoreCase("WHO"))
		{
			String mess = 	"==========\n"
				+			"Liste des joueurs en ligne:";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			int diff = Ancestra.gameServer.getClients().size() -  30;
			for(byte b = 0; b < 30; b++)
			{
				if(b == Ancestra.gameServer.getClients().size())break;
				GameThread GT = Ancestra.gameServer.getClients().get(b);
				Personnage P = GT.getPerso();
				if(P == null)continue;
				mess = P.get_name()+"("+P.get_GUID()+") ";
				
				switch(P.get_classe())
				{
					case Constants.CLASS_FECA:
						mess += "Fec";
					break;
					case Constants.CLASS_OSAMODAS:
						mess += "Osa";
					break;
					case Constants.CLASS_ENUTROF:
						mess += "Enu";
					break;
					case Constants.CLASS_SRAM:
						mess += "Sra";
					break;
					case Constants.CLASS_XELOR:
						mess += "Xel";
					break;
					case Constants.CLASS_ECAFLIP:
						mess += "Eca";
					break;
					case Constants.CLASS_ENIRIPSA:
						mess += "Eni";
					break;
					case Constants.CLASS_IOP:
						mess += "Iop";
					break;
					case Constants.CLASS_CRA:
						mess += "Cra";
					break;
					case Constants.CLASS_SADIDA:
						mess += "Sad";
					break;
					case Constants.CLASS_SACRIEUR:
						mess += "Sac";
					break;
					case Constants.CLASS_PANDAWA:
						mess += "Pan";
					break;
					default:
						mess += "Unk";
				}
				mess += " ";
				mess += (P.get_sexe()==0?"M":"F")+" ";
				mess += P.get_lvl()+" ";
				mess += P.get_curCarte().get_id()+"("+P.get_curCarte().getX()+"/"+P.get_curCarte().getY()+") ";
				mess += P.get_fight()==null?"":"Combat ";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			}
			if(diff >0)
			{
				mess = 	"Et "+diff+" autres personnages";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			}
			mess = 	"==========\n";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			return;
		}else
		
		
		if(command.equalsIgnoreCase("GONAME") || command.equalsIgnoreCase("JOIN"))
		{
			Personnage P = World.getPersoByName(infos[1]);
			if(P == null)
			{
				String str = "Le personnage n'existe pas";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			short mapID = P.get_curCarte().get_id();
			int cellID = P.get_curCell().getID();
			
			Personnage target = _perso;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[2]);
				if(target == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
				if(target.get_fight() != null)
				{
					String str = "La cible est en combat";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			target.teleport(mapID, cellID);
			String str = "Le joueur a ete teleporte";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
		if(command.equalsIgnoreCase("NAMEGO"))
		{
			Personnage target = World.getPersoByName(infos[1]);
			if(target == null)
			{
				String str = "Le personnage n'existe pas";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			if(target.get_fight() != null)
			{
				String str = "La cible est en combat";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			Personnage P = _perso;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				P = World.getPersoByName(infos[2]);
				if(P == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			if(P.isOnline())
			{
				short mapID = P.get_curCarte().get_id();
				int cellID = P.get_curCell().getID();
				target.teleport(mapID, cellID);
				String str = "Le joueur a ete teleporte";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			}else
			{
				String str = "Le joueur n'est pas en ligne";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			}
		}else
		if(command.equalsIgnoreCase("NAMEANNOUNCE"))
		{
			infos = msg.split(" ",2);
			String prefix = "["+_perso.get_name()+"]";
			SocketManager.GAME_SEND_MESSAGE_TO_ALL(prefix+infos[1], Ancestra.CONFIG_MOTD_COLOR);
			return;
		}else
		if(command.equalsIgnoreCase("TELEPORT"))
		{
			short mapID = -1;
			int cellID = -1;
			try
			{
				mapID = Short.parseShort(infos[1]);
				cellID = Integer.parseInt(infos[2]);
			}catch(Exception e){};
			if(mapID == -1 || cellID == -1 || World.getCarte(mapID) == null)
			{
				String str = "MapID ou cellID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			if(World.getCarte(mapID).getCase(cellID) == null)
			{
				String str = "MapID ou cellID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			Personnage target = _perso;
			if(infos.length > 3)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[3]);
				if(target == null  || target.get_fight() != null)
				{
					String str = "Le personnage n'a pas ete trouve ou est en combat";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			target.teleport(mapID, cellID);
			String str = "Le joueur a ete teleporte";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
		if(command.equalsIgnoreCase("GOMAP"))
		{
			int mapX = 0;
			int mapY = 0;
			int cellID = 311;
			int contID = 0;//Par défaut Amakna
			try
			{
				mapX = Integer.parseInt(infos[1]);
				mapY = Integer.parseInt(infos[2]);
				cellID = Integer.parseInt(infos[3]);
				contID = Integer.parseInt(infos[4]);
			}catch(Exception e){};
			Carte map = World.getCarteByPosAndCont(mapX,mapY,contID);
			if(map == null)
			{
				String str = "Position ou continent invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			if(map.getCase(cellID) == null)
			{
				String str = "CellID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			Personnage target = _perso;
			if(infos.length > 5)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[5]);
				if(target == null || target.get_fight() != null)
				{
					String str = "Le personnage n'a pas ete trouve ou est en combat";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
				if(target.get_fight() != null)
				{
					String str = "La cible est en combat";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			target.teleport(map.get_id(), cellID);
			String str = "Le joueur a ete teleporte";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
			
			if (command.equalsIgnoreCase("JAIL"))
				
			{
				Personnage P = World.getPersoByName(infos[1]);
				if(P == null)
				{
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Personnage non trouve");
					return;
				}
				if(P.get_compte() == null)SQLManager.LOAD_ACCOUNT_BY_GUID(P.getAccID());
				if(P.get_compte() == null)
				{
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Erreur");
					return;
				}
				Compte Compte = P.get_compte();
				int nrb = Compte.getQuoteJail()+1;
				if (Compte.getQuoteJail() <2)
				{
				//int nrb = Compte.getQuoteJail()+1;
				short mapID = 7601;
				P.get_compte().setQuoteJail(nrb);
				P.get_compte().mute(true,86400);
				P.teleport(mapID, 199);
				SQLManager.UPDATE_ACCOUNT_DATA(P.get_compte());
				Console.println(P.get_name() + " emprisonne par " + _perso.get_name()+ ".", ConsoleColorEnum.RED);
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Vous avez JAIL pour 24h00 le personnage "+P.get_name());
				SocketManager.GAME_SEND_MESSAGE_TO_ALL("Le joueur <b>"+P.get_name()+"</b> a été jail pendant 24h par "+_perso.get_name(), Ancestra.COLOR_RED);//Le socket affiche le message ci-contre
				if (Compte.getQuoteJail() == 1)
				{
				SocketManager.GAME_SEND_MESSAGE(P, "Vous venez de recevoir votre premier Jail pour 24h. Au bout du troisième, vous serez définitivement bannis. ", Ancestra.COLOR_BLEU2);
				}
				if (Compte.getQuoteJail() == 2)
				{
				SocketManager.GAME_SEND_MESSAGE(P, "Vous venez de recevoir votre deuxième Jail pour 24h. Encore un jail et vous serez définitivement bannis. ", Ancestra.COLOR_BLEU2);
					
				}
				}
				else if (Compte.getQuoteJail() == 2)
				{
					P.get_compte().setQuoteJail(nrb);
					P.get_compte().setBanned(true);
					if(P.get_compte().getGameThread() != null)P.get_compte().getGameThread().kick();
					SQLManager.UPDATE_ACCOUNT_DATA(P.get_compte());
					SocketManager.GAME_SEND_MESSAGE_TO_ALL("Le joueur <b>"+P.get_name()+"</b> est banni par "+_perso.get_name()+" suite à 3 jails.", Ancestra.COLOR_RED);//Le socket affiche le message ci-contre
					return;
				}
			}
		if (command.equalsIgnoreCase("ADDEVENTPOINT"))
			
		{
			Personnage P = World.getPersoByName(infos[1]);
			if(P == null)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Personnage non trouve");
				return;
			}
			if(P.get_compte() == null)SQLManager.LOAD_ACCOUNT_BY_GUID(P.getAccID());
			if(P.get_compte() == null)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Erreur");
				return;
			}
			Compte Compte = P.get_compte();
			int nrb = Compte.getEventPoints()+1;
			P.get_compte().setEventPoints(nrb);
			SQLManager.UPDATE_ACCOUNT_DATA(P.get_compte());
			Console.println(P.get_name() + " Don d'un point Event par " + _perso.get_name()+ ".", ConsoleColorEnum.RED);
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Vous avez offert 1 point event au joueur "+P.get_name());
			SocketManager.GAME_SEND_MESSAGE_TO_ALL("Le joueur <b>"+P.get_name()+"</b> a gagné l'Event organisé par "+_perso.get_name()+" et remporte 1 point Event !", Ancestra.COLOR_RED);//Le socket affiche le message ci-contre
			SocketManager.GAME_SEND_MESSAGE(P, "Vous venez de recevoir votre premier point Event ! Faites .cadeau pour avoir le cadeau au choix.", Ancestra.COLOR_BLEU2);
			//SocketManager.PACKET_POPUP_ALERTE("Vous venez de vous attribuer 1 point EVENT !");
			return;
		}
			else
				if (command.equalsIgnoreCase("UNJAIL"))
				{
					Personnage P = World.getPersoByName(infos[1]);
					if(P == null)
					{
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Personnage non trouve");
						return;
					}
					if(P.get_compte() == null)SQLManager.LOAD_ACCOUNT_BY_GUID(P.getAccID());
					if(P.get_compte() == null)
					{
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Erreur");
						return;
					}

					P.get_compte().mute(false,0);
					P.teleport(Ancestra.CONFIG_START_MAP, Ancestra.CONFIG_START_CELL);
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Vous avez UNJAIL pour 24h00 le personnage "+P.get_name());
					SocketManager.GAME_SEND_MESSAGE_TO_ALL("Le joueur <b>"+P.get_name()+"</b> a été unjail par "+_perso.get_name(), Ancestra.COLOR_RED);//Le socket affiche le message ci-contre
					SocketManager.GAME_SEND_MESSAGE(P, "Le maître de jeu "+_perso.get_name()+" vous a Unjail. ", Ancestra.COLOR_BLEU2);
					
			}else
				if (command.equalsIgnoreCase("DELJAIL"))
				{
					Personnage P = World.getPersoByName(infos[1]);
					if(P == null)
					{
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Personnage non trouve");
						return;
					}
					if(P.get_compte() == null)SQLManager.LOAD_ACCOUNT_BY_GUID(P.getAccID());
					if(P.get_compte() == null)
					{
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Erreur");
						return;
					}	
					
					P.get_compte().setQuoteJail(0);
					SQLManager.UPDATE_ACCOUNT_DATA(P.get_compte());
					SocketManager.GAME_SEND_MESSAGE_TO_ALL("Le quota de Jail du joueur <b>"+P.get_name()+"</b> a été remis à 0 par "+_perso.get_name(), Ancestra.COLOR_RED);//Le socket affiche le message ci-contre
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Vous avez remis à 0 le quota de jail du joueur "+P.get_name());
					SocketManager.GAME_SEND_MESSAGE(P, "Votre quota de Jail a été remis à 0 par "+_perso.get_name(), Ancestra.COLOR_BLEU2);
					return;
					
					}else
						if (command.equalsIgnoreCase("VIEWJAIL"))
						{
							Personnage P = World.getPersoByName(infos[1]);
							if(P == null)
							{
								SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Personnage non trouve");
								return;
							}
							if(P.get_compte() == null)SQLManager.LOAD_ACCOUNT_BY_GUID(P.getAccID());
							if(P.get_compte() == null)
							{
								SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Erreur");
								return;
							}
							
							SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Le quota est de: <b>+"+P.get_compte().getQuoteJail()+"</b> jail(s) pour le personnage "+P.get_name());
							return;
							
							}else
				
			if(command.equalsIgnoreCase("MUTE"))
			{
				Personnage perso = _perso;
				String name = null;
				try
				{
					name = infos[1];
				}catch(Exception e){};
				int time = 0;
				try
				{
					time = Integer.parseInt(infos[2]);
				}catch(Exception e){};
				
				perso = World.getPersoByName(name);
				if(perso == null || time < 0)
				{
					String mess = "Le personnage n'existe pas ou la duree est invalide.";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
					return;
				}
				String mess = "Vous avez mute "+perso.get_name()+" pour "+time+" secondes";
				if(perso.get_compte() == null)
				{
					mess = "(Le personnage "+perso.get_name()+" n'etait pas connecte)";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
					return;
				}
				perso.get_compte().mute(true,time);
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				SocketManager.GAME_SEND_MESSAGE_TO_ALL("Le joueur <b>"+perso.get_name()+"</b> a été mute"+" pendant "+time+" secondes par "+_perso.get_name(), Ancestra.COLOR_RED);//Le socket affiche le message ci-contre
				
				
				if(!perso.isOnline())
				{
					mess = "(Le personnage "+perso.get_name()+" n'etait pas connecte)";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				}else
				{
					SocketManager.GAME_SEND_Im_PACKET(perso, "1124;"+time);
				}
				return;
			}else
				if(command.equalsIgnoreCase("UNMUTE"))
				{
					Personnage perso = _perso;
					
					String name = null;
					try
					{
						name = infos[1];
					}catch(Exception e){};
					
					perso = World.getPersoByName(name);
					if(perso == null)
					{
						String mess = "Le personnage n'existe pas.";
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
						return;
					}
					
					perso.get_compte().mute(false,0);
					String mess = "Vous avez unmute "+perso.get_name();
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
					
					if(!perso.isOnline())
					{
						mess = "(Le personnage "+perso.get_name()+" n'etait pas connecte)";
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
					}
				}else
					if(command.equalsIgnoreCase("SIZE"))
					{
						int size = -1;
						try
						{
							size = Integer.parseInt(infos[1]);
						}catch(Exception e){};
						if(size == -1)
						{
							String str = "Taille invalide";
							SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
							return;
						}
						Personnage target = _perso;
						if(infos.length > 2)//Si un nom de perso est spécifié
						{
							target = World.getPersoByName(infos[2]);
							if(target == null)
							{
								String str = "Le personnage n'a pas ete trouve";
								SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
								return;
							}
						}
						target.set_size(size);
						SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.get_curCarte(), target.get_GUID());
						SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(target.get_curCarte(), target);
						String str = "La taille du joueur a ete modifiee";
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					}else
						if(command.equalsIgnoreCase("MORPH"))
						{
							int morphID = -1;
							try
							{
								morphID = Integer.parseInt(infos[1]);
							}catch(Exception e){};
							if(morphID == -1)
							{
								String str = "MorphID invalide";
								SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
								return;
							}
							Personnage target = _perso;
							if(infos.length > 2)//Si un nom de perso est spécifié
							{
								target = World.getPersoByName(infos[2]);
								if(target == null)
								{
									String str = "Le personnage n'a pas ete trouve";
									SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
									return;
								}
							}
							target.set_gfxID(morphID);
							SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.get_curCarte(), target.get_GUID());
							SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(target.get_curCarte(), target);
							String str = "Le joueur a ete transforme";
							SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
						}else
							if(command.equalsIgnoreCase("KICK"))
							{
								Personnage perso = _perso;
								String name = null;
								try
								{
									name = infos[1];
								}catch(Exception e){};
								perso = World.getPersoByName(name);
								if(perso == null)
								{
									String mess = "Le personnage n'existe pas.";
									SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
									return;
								}
								if(perso.isOnline())
								{
									perso.get_compte().getGameThread().kick();
									String mess = "Vous avez kick "+perso.get_name();
									SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
									SocketManager.GAME_SEND_MESSAGE_TO_ALL("Le joueur <b>"+perso.get_name()+"</b> a été kické"+" par "+_perso.get_name(), Ancestra.COLOR_RED);//Le socket affiche le message ci-contre
									
								}
								else
								{
									String mess = "Le personnage "+perso.get_name()+" n'est pas connecte";
									SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
								}
							}else
		if(command.equalsIgnoreCase("DOACTION"))
		{
			//DOACTION NAME TYPE ARGS COND
			if(infos.length < 4)
			{
				String mess = "Nombre d'argument de la commande incorect !";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				return;
			}
			int type = -100;
			String args = "",cond = "";
			Personnage perso = _perso;
			try
			{
				perso = World.getPersoByName(infos[1]);
				if(perso == null)perso = _perso;
				type = Integer.parseInt(infos[2]);
				args = infos[3];
				if(infos.length >4)
				cond = infos[4];
			}catch(Exception e)
			{
				String mess = "Arguments de la commande incorect !";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				return;
			}
			(new Action(type,args,cond)).apply(perso, null, -1, -1);
			String mess = "Action effectuee !";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
		}else
		{
			String mess = "Commande non reconnue";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
		}
	}
	
	public void commandGmTwo(String command, String[] infos, String msg)
	{
//TODO: Commandes d'accès pour GM 2
		if(_compte.get_gmLvl() < 2)
		{
			_compte.getGameThread().closeSocket();
			return;
		}else

			if(command.equalsIgnoreCase("BAN"))
			{
				Personnage P = null;
							int endban_hours = -1;
	                        String reason = "";
	                        
				try
				{
					P = World.getPersoByName(infos[1]);
									
	                                try{endban_hours = Integer.parseInt(infos[2]);}catch(Exception e){endban_hours = -1;}
	                                try{ reason = "\n"+msg.split(infos[1]+" "+infos[2] + " ")[1];}catch(Exception e){reason = "";}
	                                
				}catch(Exception e){}
				if(P == null)
				{
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Personnage non trouve");
					return;
				}
				try
				{
	                        long endban = System.currentTimeMillis()+(endban_hours*1000*60*60);
				if(P.get_compte() != null)
				{
	                                P.get_compte().setBanned(true);
					 if(endban_hours < 0)
	                                {
	                                     P.get_compte().setEndBan(-1);
	                                }
	                                 else P.get_compte().setEndBan(endban);
				}
	                        if(endban_hours < 0) endban = -1;
				SQLManager.BAN_ACCOUNT_DATA(P.get_compte().get_GUID(), 1, endban);
				if(P.get_compte().getGameThread() != null)
	                        {
	                             String messageDECO =  _perso.get_name()+":"+" Vous avez été banni pour la raison suivante :\n"+reason;
	                             messageDECO       += "\n <u>Durée :</u> " + (endban_hours<0?("infini."):(endban_hours + " heures."));
	                             SocketManager.SEND_MESSAGE_DECO(P, 18, messageDECO);
	                             P.get_compte().getGameThread().kick();
	                             SocketManager.GAME_SEND_MESSAGE_TO_ALL("Le joueur <b>" + P.get_name() + "</b> a été banni du serveur par <b>" + _perso.get_name() + "</b> pour " +(endban_hours<0?("toujours."):(endban_hours + " heures.")), Ancestra.CONFIG_MOTD_COLOR);
	                        }
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Vous avez banni "+P.get_name());
				return;
				}
				catch(Exception e){}
				if(infos[2] == null)
				{
					String mess = "Il manque des arguements: Ban + Joueur + Durée + Raison";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
					return;
				}
			
		}else
		if (command.equalsIgnoreCase("TITLE"))
		{
			Personnage target = null; 
			byte TitleID = 0;
			try
			{
				target = World.getPersoByName(infos[1]);
				TitleID = Byte.parseByte(infos[2]);
			}catch(Exception e){};
			
			if(target == null)
			{
				String str = "Le personnage n'a pas ete trouve";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			
			target.set_title(TitleID);
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Titre mis en place.");
			SQLManager.SAVE_PERSONNAGE(target, false);
			if(target.get_fight() == null) SocketManager.GAME_SEND_ALTER_GM_PACKET(target.get_curCarte(), target);
		}else
		{
			this.commandGmOne(command, infos, msg);
		}
	}
	
	public void commandGmThree(String command, String[] infos, String msg)
	{
//TODO: Commandes d'accès pour GM 3
		if(_compte.get_gmLvl() < 3)
		{
			_compte.getGameThread().closeSocket();
			return;
		}
		
		if(command.equalsIgnoreCase("EXIT"))
		{
		//	SQLManager.LOAD_ACTION(); //On load les items boutiques
			System.exit(0); //On arrête le serveur
		} else
		/*if (command.equalsIgnoreCase("TICALLFIGHTERSTURNS"))
		{
	        World.ticAllFightersTurns();
	        SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "isAlive= " + Ancestra._passerTours.isAlive() + ", SDATA= " + Ancestra._passerTours.toString());
		 else }*/
			
		/*if (command.equalsIgnoreCase("NEWTICALLFIGHTERSTURNS"))
		{
			Ancestra._passerTours = new Thread(new GameServer.AllFightsTurns());
			Ancestra._passerTours.start();
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Ok.");
		} else   */   
		if(command.equalsIgnoreCase("SAVE") && !Ancestra.isSaving)
		{
			Thread t = new Thread(new SaveThread());
			t.start();
			String mess = "Sauvegarde lancee!";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			return;
		}else
		
		if(command.equalsIgnoreCase("ALERTETOALL"))
		{
			infos = msg.split(" ",2);
			SocketManager.PACKET_POPUP_ALERTE(infos[1]);
			return;
		}
		if(command.equalsIgnoreCase("ALERTE"))
				{
			infos = msg.split(" ",2);
			Personnage target = _perso;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[2]);
				if(target == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			SocketManager.PACKET_POPUP_DEPART(target, msg);
			return;
				}
		if(command.equalsIgnoreCase("GETCOORD"))
		{
			int cell = _perso.get_curCell().getID();
			String mess = "["+Pathfinding.getCellXCoord(_perso.get_curCarte(), cell)+","+Pathfinding.getCellYCoord(_perso.get_curCarte(), cell)+"]";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			return;
		}else
		
		if (command.equalsIgnoreCase("SPAWNFIX"))
	          {
	            String groupData = infos[1];

	            _perso.get_curCarte().addStaticGroup(_perso.get_curCell().getID(), groupData);
	            String str = "Le grouppe a ete fixe";

	            if (SQLManager.SAVE_NEW_FIXGROUP(_perso.get_curCarte().get_id(), _perso.get_curCell().getID(), groupData))
	              str = str + " et a ete sauvegarde dans la BDD";
	            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, str);
	            return;
	          }
		
		else
	        	 
		
		
            if(command.equalsIgnoreCase("DEBUG"))
            {
                Personnage perso = _perso;
                try
                {
                    perso = World.getPersoByName(infos[1]);
                }
                catch(Exception e)
                {
                    perso = _perso;
                }
                SocketManager.GAME_SEND_GV_PACKET(perso);
                perso.set_duelID(-1);
                perso.set_ready(false);
                perso.fullPDV();
                perso.set_fight(null);
                SocketManager.GAME_SEND_GV_PACKET(perso);
                SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(perso._curCarte, perso);
                perso.get_curCell().addPerso(perso);
                return;
            }
		if(command.equalsIgnoreCase("DELTRIGGER"))
		{
			int cellID = -1;
			try
			{
				cellID = Integer.parseInt(infos[1]);
			}catch(Exception e){};
			if(cellID == -1 || _perso.get_curCarte().getCase(cellID) == null)
			{
				String str = "CellID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			
			_perso.get_curCarte().getCase(cellID).clearOnCellAction();
			boolean success = SQLManager.REMOVE_TRIGGER(_perso.get_curCarte().get_id(),cellID);
			String str = "";
			if(success)	str = "Le trigger a ete retire";
			else 		str = "Le trigger n'a pas ete retire";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
			if(command.equalsIgnoreCase("LEARNSPELL2"))
			{
				int spell = -1;
				try
				{
					spell = Integer.parseInt(infos[1]);
				}catch(Exception e){};
				if(spell == -1)
				{
					String str = "Valeur invalide";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
				Personnage target = _perso;
				if(infos.length > 2)//Si un nom de perso est spécifié
				{
					target = World.getPersoByName(infos[2]);
					if(target == null)
					{
						String str = "Le personnage n'a pas ete trouve";
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
						return;
					}
				}
				
				target.learnSpell(spell, 1, true,true);
				
				String str = "Le sort a ete appris";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			}else
		if(command.equalsIgnoreCase("ADDTRIGGER"))
		{
			int actionID = -1;
			String args = "",cond = "";
			try
			{
				actionID = Integer.parseInt(infos[1]);
				args = infos[2];
				cond = infos[3];
			}catch(Exception e){};
			if(args.equals("") || actionID <= -3)
			{
				String str = "Valeur invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			
			_perso.get_curCell().addOnCellStopAction(actionID,args, cond);
			boolean success = SQLManager.SAVE_TRIGGER(_perso.get_curCarte().get_id(),_perso.get_curCell().getID(),actionID,1,args,cond);
			String str = "";
			if(success)	str = "Le trigger a ete ajoute";
			else 		str = "Le trigger n'a pas ete ajoute";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
			
			
			/** Commandes spéciales Asterions by Return **/
			
			
			
			if(command.equalsIgnoreCase("vitalite"))
			{
				int pts = -1;
				try
				{
					pts = Integer.parseInt(infos[1]);
				}catch(Exception e){};
				if(pts == -1)
				{
					String str = "Valeur invalide";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
				Personnage target = _perso;
				if(infos.length > 2)//Si un nom de perso est spécifié
				{
					target = World.getPersoByName(infos[2]);
					if(target == null)
					{
						String str = "Le personnage n'a pas ete trouve";
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
						return;
					}
				}
				
				target.get_baseStats().addOneStat(125, pts);
				SocketManager.GAME_SEND_STATS_PACKET(target);
				String str = "Vous avez ajouter "+pts+" points de vitalité au personnage.";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			}
			else
				if(command.equalsIgnoreCase("intelligence"))
				{
					int pts = -1;
					try
					{
						pts = Integer.parseInt(infos[1]);
					}catch(Exception e){};
					if(pts == -1)
					{
						String str = "Valeur invalide";
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
						return;
					}
					Personnage target = _perso;
					if(infos.length > 2)//Si un nom de perso est spécifié
					{
						target = World.getPersoByName(infos[2]);
						if(target == null)
						{
							String str = "Le personnage n'a pas ete trouve";
							SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
							return;
						}
					}
					
					target.get_baseStats().addOneStat(126, pts);
					SocketManager.GAME_SEND_STATS_PACKET(target);
					String str = "Vous avez ajouter "+pts+" points d'intelligences au personnage.";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				}
				else
					if(command.equalsIgnoreCase("agilite"))
					{
						int pts = -1;
						try
						{
							pts = Integer.parseInt(infos[1]);
						}catch(Exception e){};
						if(pts == -1)
						{
							String str = "Valeur invalide";
							SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
							return;
						}
						Personnage target = _perso;
						if(infos.length > 2)//Si un nom de perso est spécifié
						{
							target = World.getPersoByName(infos[2]);
							if(target == null)
							{
								String str = "Le personnage n'a pas ete trouve";
								SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
								return;
							}
						}
						
						target.get_baseStats().addOneStat(119, pts);
						SocketManager.GAME_SEND_STATS_PACKET(target);
						String str = "Vous avez ajouter "+pts+" points d'agilité au personnage.";
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					}
					else
						if(command.equalsIgnoreCase("chance"))
						{
							int pts = -1;
							try
							{
								pts = Integer.parseInt(infos[1]);
							}catch(Exception e){};
							if(pts == -1)
							{
								String str = "Valeur invalide";
								SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
								return;
							}
							Personnage target = _perso;
							if(infos.length > 2)//Si un nom de perso est spécifié
							{
								target = World.getPersoByName(infos[2]);
								if(target == null)
								{
									String str = "Le personnage n'a pas ete trouve";
									SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
									return;
								}
							}
							
							target.get_baseStats().addOneStat(123, pts);
							SocketManager.GAME_SEND_STATS_PACKET(target);
							String str = "Vous avez ajouter "+pts+" points de chance au personnage.";
							SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
						}
						else
							if(command.equalsIgnoreCase("force"))
							{
								int pts = -1;
								try
								{
									pts = Integer.parseInt(infos[1]);
								}catch(Exception e){};
								if(pts == -1)
								{
									String str = "Valeur invalide";
									SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
									return;
								}
								Personnage target = _perso;
								if(infos.length > 2)//Si un nom de perso est spécifié
								{
									target = World.getPersoByName(infos[2]);
									if(target == null)
									{
										String str = "Le personnage n'a pas ete trouve";
										SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
										return;
									}
								}
								
								target.get_baseStats().addOneStat(118, pts);
								SocketManager.GAME_SEND_STATS_PACKET(target);
								String str = "Vous avez ajouter "+pts+" points de force au personnage.";
								SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
							}
							else
								if(command.equalsIgnoreCase("sagesse"))
								{
									int pts = -1;
									try
									{
										pts = Integer.parseInt(infos[1]);
									}catch(Exception e){};
									if(pts == -1)
									{
										String str = "Valeur invalide";
										SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
										return;
									}
									Personnage target = _perso;
									if(infos.length > 2)//Si un nom de perso est spécifié
									{
										target = World.getPersoByName(infos[2]);
										if(target == null)
										{
											String str = "Le personnage n'a pas ete trouve";
											SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
											return;
										}
									}
									
									target.get_baseStats().addOneStat(124, pts);
									SocketManager.GAME_SEND_STATS_PACKET(target);
									String str = "Vous avez ajouter "+pts+" points de sagesse au personnage.";
									SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
								}
								else
									if(command.equalsIgnoreCase("initiative"))
									{
										int pts = -1;
										try
										{
											pts = Integer.parseInt(infos[1]);
										}catch(Exception e){};
										if(pts == -1)
										{
											String str = "Valeur invalide";
											SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
											return;
										}
										Personnage target = _perso;
										if(infos.length > 2)//Si un nom de perso est spécifié
										{
											target = World.getPersoByName(infos[2]);
											if(target == null)
											{
												String str = "Le personnage n'a pas ete trouve";
												SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
												return;
											}
										}
										
										target.get_baseStats().addOneStat(174, pts);
										SocketManager.GAME_SEND_STATS_PACKET(target);
										String str = "Vous avez ajouter "+pts+" points d'initiative au personnage.";
										SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
									}
									else
										if(command.equalsIgnoreCase("pa"))
										{
											int pts = -1;
											try
											{
												pts = Integer.parseInt(infos[1]);
											}catch(Exception e){};
											if(pts == -1)
											{
												String str = "Valeur invalide";
												SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
												return;
											}
											Personnage target = _perso;
											if(infos.length > 2)//Si un nom de perso est spécifié
											{
												target = World.getPersoByName(infos[2]);
												if(target == null)
												{
													String str = "Le personnage n'a pas ete trouve";
													SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
													return;
												}
											}
											
											target.get_baseStats().addOneStat(111, pts);
											SocketManager.GAME_SEND_STATS_PACKET(target);
											String str = "Vous avez ajouter "+pts+" pa au personnage.";
											SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
										}else
		if(command.equalsIgnoreCase("pm"))
		{
			int pts = -1;
			try
			{
				pts = Integer.parseInt(infos[1]);
			}catch(Exception e){};
			if(pts == -1)
			{
				String str = "Valeur invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			Personnage target = _perso;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[2]);
				if(target == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			
			target.get_baseStats().addOneStat(128, pts);
			SocketManager.GAME_SEND_STATS_PACKET(target);
			String str = "Vous avez ajouter "+pts+" pm au personnage.";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}
		else
			/** Fin des commandes pour Asterions by Return **/
			if(command.equalsIgnoreCase("ITEM") || command.equalsIgnoreCase("!getitem"))
			{
				boolean isOffiCmd = command.equalsIgnoreCase("!getitem");
				if(_compte.get_gmLvl() < 2)
				{
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Vous n'avez pas le niveau MJ requis");
					return;
				}
				int tID = 0;
				try
				{
					tID = Integer.parseInt(infos[1]);
				}catch(Exception e){};
				if(tID == 0)
				{
					String mess = "Le template "+tID+" n'existe pas ";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
					return;
				}
				int qua = 1;
				if(infos.length == 3)//Si une quantité est spécifiée
				{
					try
					{
						qua = Integer.parseInt(infos[2]);
					}catch(Exception e){};
				}
				boolean useMax = false;
				if(infos.length == 4 && !isOffiCmd)//Si un jet est spécifié
				{
					if(infos[3].equalsIgnoreCase("MAX"))useMax = true;
				}
				ObjTemplate t = World.getObjTemplate(tID);
				if(t == null)
				{
					String mess = "Le template "+tID+" n'existe pas ";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
					return;
				}
				if(qua <1)qua =1;
				Objet obj = t.createNewItem(qua,useMax);
				if(_perso.addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
					World.addObjet(obj,true);
				String str = "Creation de l'item "+tID+" reussie";
				if(useMax) str += " avec des stats maximums";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				SocketManager.GAME_SEND_Ow_PACKET(_perso);
			}else 
				if(command.equalsIgnoreCase("LEVEL"))
				{
					int count = 0;
					try
					{
						count = Integer.parseInt(infos[1]);
						if(count < 1)	count = 1;
						if(count > World.getExpLevelSize())	count = World.getExpLevelSize();
						Personnage perso = _perso;
						if(infos.length == 3)//Si le nom du perso est spécifié
						{
							String name = infos[2];
							perso = World.getPersoByName(name);
							if(perso == null)
								perso = _perso;
						}
						if(perso.get_lvl() < count)
						{
							while(perso.get_lvl() < count)
							{
								perso.levelUp(false,true);
							}
							if(perso.isOnline())
							{
								SocketManager.GAME_SEND_SPELL_LIST(perso);
								SocketManager.GAME_SEND_NEW_LVL_PACKET(perso.get_compte().getGameThread().get_out(),perso.get_lvl());
								SocketManager.GAME_SEND_STATS_PACKET(perso);
							}
						}
						String mess = "Vous avez fixer le niveau de "+perso.get_name()+" a "+count;
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
					}catch(Exception e)
					{
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Valeur incorecte");
						return;
					};
				}else
		if (command.equalsIgnoreCase("SHUTDOWN"))
		{
			int time = 30, OffOn = 0;
			try
			{
				OffOn = Integer.parseInt(infos[1]);
				time = Integer.parseInt(infos[2]);
			}catch(Exception e){};
			
			if(OffOn == 1 && _TimerStart)// demande de démarer le reboot
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Un shutdown est deja programmer.");
			}else if(OffOn == 1 && !_TimerStart)
			{
				_timer = createTimer(time);
				_timer.start();
				_TimerStart = true;
				String timeMSG = "minutes";
				if(time <= 1)
				{
					timeMSG = "minute";
				}
				SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;"+time+" "+timeMSG);
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Shutdown lance.");
			}else if(OffOn == 0 && _TimerStart)
			{
				_timer.stop();
				_TimerStart = false;
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Shutdown arrete.");
			}else if(OffOn == 0 && !_TimerStart)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Aucun shutdown n'est lance.");
			}
		}else
		{
			this.commandGmTwo(command, infos, msg);
		}
	}
	
	public void commandGmFour(String command, String[] infos, String msg)
	{
//TODO: Commandes d'accès pour GM 4
		if(_compte.get_gmLvl() < 4)
		{
			_compte.getGameThread().closeSocket();
			return;
		}
		
		
			if(command.equalsIgnoreCase("HONOR"))
			{
				int honor = 0;
				try
				{
					honor = Integer.parseInt(infos[1]);
				}catch(Exception e){};
				Personnage target = _perso;
				if(infos.length > 2)//Si un nom de perso est spécifié
				{
					target = World.getPersoByName(infos[2]);
					if(target == null)
					{
						String str = "Le personnage n'a pas ete trouve";
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
						return;
					}
				}
				String str = "Vous avez ajouter "+honor+" honneur a "+target.get_name();
				if(target.get_align() == Constants.ALIGNEMENT_NEUTRE)
				{
					str = "Le joueur est neutre ...";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
				target.addHonor(honor);
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				
			}else
			if(command.equalsIgnoreCase("SETALIGN"))
			{
				byte align = -1;
				try
				{
					align = Byte.parseByte(infos[1]);
				}catch(Exception e){};
				if(align < Constants.ALIGNEMENT_NEUTRE || align >Constants.ALIGNEMENT_MERCENAIRE)
				{
					String str = "Valeur invalide";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
				Personnage target = _perso;
				if(infos.length > 2)//Si un nom de perso est spécifié
				{
					target = World.getPersoByName(infos[2]);
					if(target == null)
					{
						String str = "Le personnage n'a pas ete trouve";
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
						return;
					}
				}
				
				target.modifAlignement(align);
				
				String str = "L'alignement du joueur a ete modifie";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			}else
			if(command.equalsIgnoreCase("LEARNSPELL"))
			{
				int spell = -1;
				try
				{
					spell = Integer.parseInt(infos[1]);
				}catch(Exception e){};
				if(spell == -1)
				{
					String str = "Valeur invalide";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
				Personnage target = _perso;
				if(infos.length > 2)//Si un nom de perso est spécifié
				{
					target = World.getPersoByName(infos[2]);
					if(target == null)
					{
						String str = "Le personnage n'a pas ete trouve";
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
						return;
					}
				}
				
				target.learnSpell(spell, 1, true,true);
				
				String str = "Le sort a ete appris";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			}else
				//perso.get_baseStats().addOneStat(action, nombre);
				
										
			if(command.equalsIgnoreCase("CAPITAL"))
			{
				int pts = -1;
				try
				{
					pts = Integer.parseInt(infos[1]);
				}catch(Exception e){};
				if(pts == -1)
				{
					String str = "Valeur invalide";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
				Personnage target = _perso;
				if(infos.length > 2)//Si un nom de perso est spécifié
				{
					target = World.getPersoByName(infos[2]);
					if(target == null)
					{
						String str = "Le personnage n'a pas ete trouve";
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
						return;
					}
				}
				target.addCapital(pts);
				SocketManager.GAME_SEND_STATS_PACKET(target);
				String str = "Le capital a ete modifiee";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			}
			else
			if(command.equalsIgnoreCase("SPELLPOINT"))
			{
				int pts = -1;
				try
				{
					pts = Integer.parseInt(infos[1]);
				}catch(Exception e){};
				if(pts == -1)
				{
					String str = "Valeur invalide";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
				Personnage target = _perso;
				if(infos.length > 2)//Si un nom de perso est spécifié
				{
					target = World.getPersoByName(infos[2]);
					if(target == null)
					{
						String str = "Le personnage n'a pas ete trouve";
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
						return;
					}
				}
				target.addSpellPoint(pts);
				SocketManager.GAME_SEND_STATS_PACKET(target);
				String str = "Le nombre de point de sort a ete modifiee";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			}else
			
			if(command.equalsIgnoreCase("ITEMSET"))
			{
				int tID = 0;
				String nom = null;
				try
				{
					if(infos.length > 3)
						nom = infos[3];
					else if(infos.length > 1)
						tID = Integer.parseInt(infos[1]);
					
				}catch(Exception e){};
				ItemSet IS = World.getItemSet(tID);
				if(tID == 0 || IS == null)
				{
					String mess = "La panoplie "+tID+" n'existe pas ";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
					return;
				}
				boolean useMax = false;
				if(infos.length > 2)
					useMax = infos[2].equals("MAX");//Si un jet est spécifié

				Personnage perso = _perso;
				if(nom != null)
					try {
						perso = World.getPersoByName(nom);
					} catch(Exception e) {}
				for(ObjTemplate t : IS.getItemTemplates())
				{
					Objet obj = t.createNewItem(1,useMax);
					if(perso != null) {
						if(perso.addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
							World.addObjet(obj,true);
					} else if(_perso.addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
						World.addObjet(obj,true);					
				}
				String str = "Creation de la panoplie "+tID+" reussie";
				if(useMax) str += " avec des stats maximums";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			}else
			if(command.equalsIgnoreCase("KAMAS"))
			{
				int count = 0;
				try
				{
					count = Integer.parseInt(infos[1]);
				}catch(Exception e)
				{
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Valeur incorecte");
					return;
				};
				if(count == 0)return;
				
				Personnage perso = _perso;
				if(infos.length == 3)//Si le nom du perso est spécifié
				{
					String name = infos[2];
					perso = World.getPersoByName(name);
					if(perso == null)
						perso = _perso;
				}
				long curKamas = perso.get_kamas();
				long newKamas = curKamas + count;
				if(newKamas <0) newKamas = 0;
				if(newKamas > 1000000000) newKamas = 1000000000;
				perso.set_kamas(newKamas);
				if(perso.isOnline())
					SocketManager.GAME_SEND_STATS_PACKET(perso);
				String mess = "Vous avez ";
				mess += (count<0?"retirer":"ajouter")+" ";
				mess += Math.abs(count)+" kamas a "+perso.get_name();
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
			}else
			
		if(command.equalsIgnoreCase("LOCK"))
		{
			byte LockValue = 1;//Accessible
			try
			{
				LockValue = Byte.parseByte(infos[1]);
			}catch(Exception e){};
			
			if(LockValue > 2) LockValue = 2;
			if(LockValue < 0) LockValue = 0;
			World.set_state((short)LockValue);
			if(LockValue == 1)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Serveur accessible.");
			}else if(LockValue == 0)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Serveur inaccessible.");
			}else if(LockValue == 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Serveur en sauvegarde.");
			}
		}else
		if(command.equalsIgnoreCase("BLOCK"))
		{
			byte GmAccess = 0;
			byte KickPlayer = 0;
			try
			{
				GmAccess = Byte.parseByte(infos[1]);
				KickPlayer = Byte.parseByte(infos[2]);
			}catch(Exception e){};
			
			World.setGmAccess(GmAccess);
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Serveur bloque au GmLevel : "+GmAccess);
			if(KickPlayer > 0)
			{
				for(Personnage z : World.getOnlinePersos()) 
				{
					if(z.get_compte().get_gmLvl() < GmAccess)
						z.get_compte().getGameThread().closeSocket();
				}
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Les joueurs de GmLevel inferieur a "+GmAccess+" ont ete kicks.");
			}
		}else
		if(command.equalsIgnoreCase("BANIP"))
		{
			Personnage P = null;
			try
			{
				P = World.getPersoByName(infos[1]);
			}catch(Exception e){};
			if(P == null || !P.isOnline())
			{
				String str = "Le personnage n'a pas ete trouve.";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			
			if(!Constants.IPcompareToBanIP(P.get_compte().get_curIP()))
			{
				Constants.BAN_IP += ","+P.get_compte().get_curIP();
				P.get_compte().setBanned(true);
				Console.println(P.get_compte().get_curIP()+" bannie par " + _perso.get_name() + ".", ConsoleColorEnum.RED);
				if(SQLManager.ADD_BANIP(P.get_compte().get_curIP()))
				{
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "L'IP a ete banni.");
				}
				if(P.isOnline()){
					P.get_compte().getGameThread().kick();
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Le joueur a ete kick.");
				}
			}else
			{
				String str = "L'IP existe deja.";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			
		}else
		if(command.equalsIgnoreCase("FULLHDV"))
		{
			int numb = 1;
			try
			{
				numb = Integer.parseInt(infos[1]);
			}catch(Exception e){};
			fullHdv(numb);
		}else
		{
			this.commandGmThree(command, infos, msg);
		}
		
	}
//TODO: Commandes d'accès pour GM 5
	public void commandGmFive(String command, String[] infos, String msg)
	{
		if(_compte.get_gmLvl() < 5)
		{
			_compte.getGameThread().closeSocket();
			return;
		}
		if(command.equalsIgnoreCase("RELOADCONFIG"))
		{
			if(_compte.get_gmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			
			//Ancestra.loadConfiguration();
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Fichier de configuration rechargé!");
			return;
			
		}else
			/** Pour peanuthero <3 **/
			if (command.equalsIgnoreCase("RELOADWORLD")){
				try {
					
					SQLManager.SAVE_PERSONNAGE(_perso, true);
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,
							"Rechargement des données en cours...");
					SQLManager.LOAD_ITEMS_FULL();
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,
							"Chargement des items : Ok");
					SQLManager.LOAD_MOB_TEMPLATE();
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,
							"Chargement des mobs : Ok");
					SQLManager.LOAD_NPC_TEMPLATE();
					SQLManager.LOAD_NPC_QUESTIONS();
					SQLManager.LOAD_NPC_ANSWERS();
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,
							"Chargement des NPC complets : Ok");
					SQLManager.LOAD_COMPTES();
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,
							"Chargement des comptes : Ok");
					SQLManager.LOAD_MOUNTPARKS();
					SQLManager.LOAD_MOUNTS();
					SQLManager.LOAD_Mounts_Datas();
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,
							"Chargement des montures et enclos : Ok");
					SQLManager.LOAD_TRIGGERS();
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,
							"Chargement des triggers : Ok");
					SQLManager.LOAD_OBJ_TEMPLATE();
					SQLManager.LOAD_ITEM_ACTIONS();
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,
							"Chargement des item actions : Ok");
					SQLManager.LOAD_SORTS();
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,
							"Chargement des sorts : Ok");
					SQLManager.CARGAR_PRISMAS();
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,
							"Chargement des prismes : Ok");
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,
							"Chargement des données terminée, Good :)");
				} catch (Exception exception) {
				}
				return;
			}else

		if (command.equalsIgnoreCase("CADEAU")) {
			int regalo = 0;
			try {
				regalo = Integer.parseInt(infos[1]);
			} catch (Exception e) {}
			Personnage objetivo = _perso;
			if (infos.length > 2) {
				objetivo = World.getPersoByName(infos[2]);
				if (objetivo == null) {
					String str = "Le personnage n'est pas reconnu.";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, str);
					return;
				}
			}
			objetivo.get_compte().setCadeau(regalo);
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Don de " + regalo + " à " + objetivo.get_name());
		} else if (command.equalsIgnoreCase("ALLCADEAU")) {
			int regalo = 0;
			try {
				regalo = Integer.parseInt(infos[1]);
			} catch (Exception e) {}
			for (Personnage pj : World.getOnlinePersos()) {
				pj.get_compte().setCadeau(regalo);
			}
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Don de " + regalo + " à tous les joueurs en ligne.");
		} else
		if(command.equalsIgnoreCase("SETADMIN"))
		{
			int gmLvl = -100;
			try
			{
				gmLvl = Integer.parseInt(infos[1]);
			}catch(Exception e){};
			if(gmLvl == -100)
			{
				String str = "Valeur incorrecte";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			Personnage target = _perso;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[2]);
				if(target == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			target.get_compte().setGmLvl(gmLvl);
			SQLManager.UPDATE_ACCOUNT_DATA(target.get_compte());
			String str = "Le niveau GM du joueur a ete modifie";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
			
			if(command.equalsIgnoreCase("CREATEGUILD"))
			{
				Personnage perso = _perso;
				if(infos.length >1)
				{
					perso = World.getPersoByName(infos[1]);
				}
				if(perso == null)
				{
					String mess = "Le personnage n'existe pas.";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
					return;
				}
				
				if(!perso.isOnline())
				{
					String mess = "Le personnage "+perso.get_name()+" n'etait pas connecte";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
					return;
				}
				if(perso.get_guild() != null || perso.getGuildMember() != null)
				{
					String mess = "Le personnage "+perso.get_name()+" a deja une guilde";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
					return;
				}
				SocketManager.GAME_SEND_gn_PACKET(perso);
				String mess = perso.get_name()+": Panneau de creation de guilde ouvert";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				return;
			}else
				if(command.equalsIgnoreCase("TOOGLEAGGRO"))
				{
					Personnage perso = _perso;
					
					String name = null;
					try
					{
						name = infos[1];
					}catch(Exception e){};
					
					perso = World.getPersoByName(name);
					
					if(perso == null)
					{
						String mess = "Le personnage n'existe pas.";
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
						return;
					}
					
					perso.set_canAggro(!perso.canAggro());
					String mess = perso.get_name();
					if(perso.canAggro()) mess += " peut maintenant etre aggresser";
					else mess += " ne peut plus etre agresser";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
					
					if(!perso.isOnline())
					{
						mess = "(Le personnage "+perso.get_name()+" n'etait pas connecte)";
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
					}
				}else
					
					if(command.equalsIgnoreCase("ANNOUNCE"))
					{
						infos = msg.split(" ",2);
						SocketManager.GAME_SEND_MESSAGE_TO_ALL(infos[1], Ancestra.CONFIG_MOTD_COLOR);
						return;
					}else
						if(command.equalsIgnoreCase("UNBAN"))
						{
							Personnage P = World.getPersoByName(infos[1]);
							if(P == null)
							{
								SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Personnage non trouve");
								return;
							}
				                        if(P.get_compte() != null)
							{
								 P.get_compte().setEndBan(0);
				                                 P.get_compte().setBanned(false);
							}
							SQLManager.BAN_ACCOUNT_DATA(P.getAccID(), 0, 0);
							SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Vous avez debanni "+P.get_name());
							return;
						}else
							if(command.equalsIgnoreCase("SETMAXGROUP"))
							{
								infos = msg.split(" ",4);
								byte id = -1;
								try
								{
									id = Byte.parseByte(infos[1]);
								}catch(Exception e){};
								if(id == -1)
								{
									String str = "Valeur invalide";
									SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
									return;
								}
								String mess = "Le nombre de groupe a ete fixe";
								_perso.get_curCarte().setMaxGroup(id);
								boolean ok = SQLManager.SAVE_MAP_DATA(_perso.get_curCarte());
								if(ok)mess += " et a ete sauvegarder a la BDD";
								SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
							}else
								if(command.equalsIgnoreCase("ADDREPONSEACTION"))
								{
									infos = msg.split(" ",4);
									int id = -30;
									int repID = 0;
									String args = infos[3];
									try
									{
										repID = Integer.parseInt(infos[1]);
										id = Integer.parseInt(infos[2]);
									}catch(Exception e){};
									NPC_reponse rep = World.getNPCreponse(repID);
									if(id == -30 || rep == null)
									{
										String str = "Au moins une des valeur est invalide";
										SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
										return;
									}
									String mess = "L'action a ete ajoute";
									
									rep.addAction(new Action(id,args,""));
									boolean ok = SQLManager.ADD_REPONSEACTION(repID,id,args);
									if(ok)mess += " et ajoute a la BDD";
									SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
								}else
									if(command.equalsIgnoreCase("SETINITQUESTION"))
									{
										infos = msg.split(" ",4);
										int id = -30;
										int q = 0;
										try
										{
											q = Integer.parseInt(infos[2]);
											id = Integer.parseInt(infos[1]);
										}catch(Exception e){};
										if(id == -30)
										{
											String str = "NpcID invalide";
											SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
											return;
										}
										String mess = "L'action a ete ajoute";
										NPC_tmpl npc = World.getNPCTemplate(id);
										
										npc.setInitQuestion(q);
										boolean ok = SQLManager.UPDATE_INITQUESTION(id,q);
										if(ok)mess += " et ajoute a la BDD";
										SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
									}else
										if(command.equalsIgnoreCase("ADDENDFIGHTACTION"))
										{
											infos = msg.split(" ",4);
											int id = -30;
											int type = 0;
											String args = infos[3];
											String cond = infos[4];
											try
											{
												type = Integer.parseInt(infos[1]);
												id = Integer.parseInt(infos[2]);
												
											}catch(Exception e){};
											if(id == -30)
											{
												String str = "Au moins une des valeur est invalide";
												SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
												return;
											}
											String mess = "L'action a ete ajoute";
											_perso.get_curCarte().addEndFightAction(type, new Action(id,args,cond));
											boolean ok = SQLManager.ADD_ENDFIGHTACTION(_perso.get_curCarte().get_id(),type,id,args,cond);
											if(ok)mess += " et ajoute a la BDD";
											SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
											return;
										}else
											if(command.equalsIgnoreCase("ADDNPC"))
											{
												int id = 0;
												try
												{
													id = Integer.parseInt(infos[1]);
												}catch(Exception e){};
												if(id == 0 || World.getNPCTemplate(id) == null)
												{
													String str = "NpcID invalide";
													SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
													return;
												}
												NPC npc = _perso.get_curCarte().addNpc(id, _perso.get_curCell().getID(), _perso.get_orientation());
												SocketManager.GAME_SEND_ADD_NPC_TO_MAP(_perso.get_curCarte(), npc);
												String str = "Le PNJ a ete ajoute";
												if(_perso.get_orientation() == 0
														|| _perso.get_orientation() == 2
														|| _perso.get_orientation() == 4
														|| _perso.get_orientation() == 6)
															str += " mais est invisible (orientation diagonale invalide).";
												
												if(SQLManager.ADD_NPC_ON_MAP(_perso.get_curCarte().get_id(), id, _perso.get_curCell().getID(), _perso.get_orientation()))
													SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
												else
													SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"Erreur au moment de sauvegarder la position");
											}else
												if(command.equalsIgnoreCase("DELNPC"))
												{
													int id = 0;
													try
													{
														id = Integer.parseInt(infos[1]);
													}catch(Exception e){};
													NPC npc = _perso.get_curCarte().getNPC(id);
													if(id == 0 || npc == null)
													{
														String str = "Npc GUID invalide";
														SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
														return;
													}
													int exC = npc.get_cellID();
													//on l'efface de la map
													SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(_perso.get_curCarte(), id);
													_perso.get_curCarte().removeNpcOrMobGroup(id);
													
													String str = "Le PNJ a ete supprime";
													if(SQLManager.DELETE_NPC_ON_MAP(_perso.get_curCarte().get_id(),exC))
														SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
													else
														SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"Erreur au moment de sauvegarder la position");
												}else
													if(command.equalsIgnoreCase("DELNPCITEM"))
													{
														if(_compte.get_gmLvl() <3)return;
														int npcGUID = 0;
														int itmID = -1;
														try
														{
															npcGUID = Integer.parseInt(infos[1]);
															itmID = Integer.parseInt(infos[2]);
														}catch(Exception e){};
														NPC_tmpl npc =  _perso.get_curCarte().getNPC(npcGUID).get_template();
														if(npcGUID == 0 || itmID == -1 || npc == null)
														{
															String str = "NpcGUID ou itmID invalide";
															SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
															return;
														}
														
														
														String str = "";
														if(npc.delItemVendor(itmID))str = "L'objet a ete retire";
														else str = "L'objet n'a pas ete retire";
														SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
													}else
														if(command.equalsIgnoreCase("ADDNPCITEM"))
														{
															if(_compte.get_gmLvl() <3)return;
															int npcGUID = 0;
															int itmID = -1;
															try
															{
																npcGUID = Integer.parseInt(infos[1]);
																itmID = Integer.parseInt(infos[2]);
															}catch(Exception e){};
															NPC_tmpl npc =  _perso.get_curCarte().getNPC(npcGUID).get_template();
															ObjTemplate item =  World.getObjTemplate(itmID);
															if(npcGUID == 0 || itmID == -1 || npc == null || item == null)
															{
																String str = "NpcGUID ou itmID invalide";
																SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																return;
															}
															
															
															String str = "";
															if(npc.addItemVendor(item))str = "L'objet a ete rajoute";
															else str = "L'objet n'a pas ete rajoute";
															SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
														}else
														if(command.equalsIgnoreCase("ADDMOUNTPARK"))
														{
															int size = -1;
															int owner = -2;
															int price = -1;
															try
															{
																size = Integer.parseInt(infos[1]);
																owner = Integer.parseInt(infos[2]);
																price = Integer.parseInt(infos[3]);
																if(price > 20000000)price = 20000000;
																if(price <0)price = 0;
															}catch(Exception e){};
															if(size == -1 || owner == -2 || price == -1 || _perso.get_curCarte().getMountPark() != null)
															{
																String str = "Infos invalides ou map deja config.";
																SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																return;
															}
															MountPark MP = new MountPark(owner, _perso.get_curCarte(), _perso.get_curCell().getID(), size, "", -1, price);
															_perso.get_curCarte().setMountPark(MP);
															SQLManager.SAVE_MOUNTPARK(MP);
															String str = "L'enclos a ete config. avec succes";
															SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
														}else 
															if(command.equalsIgnoreCase("PDVPER"))
															{
																int count = 0;
																try
																{
																	count = Integer.parseInt(infos[1]);
																	if(count < 0)	count = 0;
																	if(count > 100)	count = 100;
																	Personnage perso = _perso;
																	if(infos.length == 3)//Si le nom du perso est spécifié
																	{
																		String name = infos[2];
																		perso = World.getPersoByName(name);
																		if(perso == null)
																			perso = _perso;
																	}
																	int newPDV = perso.get_PDVMAX() * count / 100;
																	perso.set_PDV(newPDV);
																	if(perso.isOnline())
																		SocketManager.GAME_SEND_STATS_PACKET(perso);
																	String mess = "Vous avez fixer le pourcentage de pdv de "+perso.get_name()+" a "+count;
																	SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
																}catch(Exception e)
																{
																	SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Valeur incorecte");
																	return;
																};
															}else
																if(command.equalsIgnoreCase("LEARNJOB"))
																{
																	int job = -1;
																	try
																	{
																		job = Integer.parseInt(infos[1]);
																	}catch(Exception e){};
																	if(job == -1 || World.getMetier(job) == null)
																	{
																		String str = "Valeur invalide";
																		SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																		return;
																	}
																	Personnage target = _perso;
																	if(infos.length > 2)//Si un nom de perso est spécifié
																	{
																		target = World.getPersoByName(infos[2]);
																		if(target == null)
																		{
																			String str = "Le personnage n'a pas ete trouve";
																			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																			return;
																		}
																	}
																	
																	target.learnJob(World.getMetier(job));
																	
																	String str = "Le metier a ete appris";
																	SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																}else
																
																	if(command.equalsIgnoreCase("MOVENPC"))
																{
																	int id = 0;
																	try
																	{
																		id = Integer.parseInt(infos[1]);
																	}catch(Exception e){};
																	NPC npc = _perso.get_curCarte().getNPC(id);
																	if(id == 0 || npc == null)
																	{
																		String str = "Npc GUID invalide";
																		SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																		return;
																	}
																	int exC = npc.get_cellID();
																	//on l'efface de la map
																	SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(_perso.get_curCarte(), id);
																	//on change sa position/orientation
																	npc.setCellID(_perso.get_curCell().getID());
																	npc.setOrientation((byte)_perso.get_orientation());
																	//on envoie la modif
																	SocketManager.GAME_SEND_ADD_NPC_TO_MAP(_perso.get_curCarte(),npc);
																	String str = "Le PNJ a ete deplace";
																	if(_perso.get_orientation() == 0
																	|| _perso.get_orientation() == 2
																	|| _perso.get_orientation() == 4
																	|| _perso.get_orientation() == 6)
																		str += " mais est devenu invisible (orientation diagonale invalide).";
																	if(SQLManager.DELETE_NPC_ON_MAP(_perso.get_curCarte().get_id(),exC)
																	&& SQLManager.ADD_NPC_ON_MAP(_perso.get_curCarte().get_id(),npc.get_template().get_id(),_perso.get_curCell().getID(),_perso.get_orientation()))
																		SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																	else
																		SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"Erreur au moment de sauvegarder la position");
																}else	
																	
																if (command.equalsIgnoreCase("SPAWN"))
																{			
																	String Mob = null;
																	try {
																		Mob = infos[1];
																	} catch (Exception e) {
																	}
																	;
																	if (Mob == null)
																		return;
																	_perso.get_curCarte().spawnGroupOnCommand(
																			_perso.get_curCell().getID(), Mob);
																}else
																	if(command.equalsIgnoreCase("ADDJOBXP"))
																	{
																		int job = -1;
																		int xp = -1;
																		try
																		{
																			job = Integer.parseInt(infos[1]);
																			xp = Integer.parseInt(infos[2]);
																		}catch(Exception e){};
																		if(job == -1 || xp < 0)
																		{
																			String str = "Valeurs invalides";
																			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																			return;
																		}
																			Personnage target = _perso;
																		if(infos.length > 3)//Si un nom de perso est spécifié
																		{
																			target = World.getPersoByName(infos[3]);
																			if(target == null)
																			{
																				String str = "Le personnage n'a pas ete trouve";
																				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																				return;
																			}
																		}
																		StatsMetier SM = target.getMetierByID(job);
																		if(SM== null)
																		{
																			String str = "Le joueur ne connais pas le metier demande";
																			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																			return;
																		}
																			
																		SM.addXp(target, xp);
																		
																		String str = "Le metier a ete experimenter";
																		SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																	}else
																		if(command.equalsIgnoreCase("SETREPONSES"))
																		{
																			if(infos.length <3)
																			{
																				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"Il manque un/des arguments");
																				return;
																			}
																			int id = 0;
																			try
																			{
																				id = Integer.parseInt(infos[1]);
																			}catch(Exception e){};
																			String reps = infos[2];
																			NPC_question Q = World.getNPCQuestion(id);
																			String str = "";
																			if(id == 0 || Q == null)
																			{
																				str = "QuestionID invalide";
																				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																				return;
																			}
																			Q.setReponses(reps);
																			boolean a= SQLManager.UPDATE_NPCREPONSES(id,reps);
																			str = "Liste des reponses pour la question "+id+": "+Q.getReponses();
																			if(a)str += "(sauvegarde dans la BDD)";
																			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																			return;
																		}else
																		if(command.equalsIgnoreCase("SHOWREPONSES"))
																		{
																			int id = 0;
																			try
																			{
																				id = Integer.parseInt(infos[1]);
																			}catch(Exception e){};
																			NPC_question Q = World.getNPCQuestion(id);
																			String str = "";
																			if(id == 0 || Q == null)
																			{
																				str = "QuestionID invalide";
																				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																				return;
																			}
																			str = "Liste des reponses pour la question "+id+": "+Q.getReponses();
																			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																			return;
																		}else
																			if(command.equalsIgnoreCase("DELFIGHTPOS"))
																			{
																				int cell = -1;
																				try
																				{
																					cell = Integer.parseInt(infos[2]);
																				}catch(Exception e){};
																				if(cell < 0 || _perso.get_curCarte().getCase(cell) == null)
																				{
																					cell = _perso.get_curCell().getID();
																				}
																				String places = _perso.get_curCarte().get_placesStr();
																				String[] p = places.split("\\|");
																				String newPlaces = "";
																				String team0 = "",team1 = "";
																				try
																				{
																					team0 = p[0];
																				}catch(Exception e){};
																				try
																				{
																					team1 = p[1];
																				}catch(Exception e){};
																				
																				for(int a = 0;a<=team0.length()-2;a+=2)
																				{
																					String c = p[0].substring(a,a+2);
																					if(cell == CryptManager.cellCode_To_ID(c))continue;
																					newPlaces += c;
																				}
																				newPlaces += "|";
																				for(int a = 0;a<=team1.length()-2;a+=2)
																				{
																					String c = p[1].substring(a,a+2);
																					if(cell == CryptManager.cellCode_To_ID(c))continue;
																					newPlaces += c;
																				}
																				_perso.get_curCarte().setPlaces(newPlaces);
																				if(!SQLManager.SAVE_MAP_DATA(_perso.get_curCarte()))return;
																				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"Les places ont ete modifiees ("+newPlaces+")");
																				return;
																			}else
																				
																		
																			if(command.equalsIgnoreCase("ADDFIGHTPOS"))
																			{
																				int team = -1;
																				int cell = -1;
																				try
																				{
																					team = Integer.parseInt(infos[1]);
																					cell = Integer.parseInt(infos[2]);
																				}catch(Exception e){};
																				if( team < 0 || team>1)
																				{
																					String str = "Team ou cellID incorects";
																					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																					return;
																				}
																				if(cell <0 || _perso.get_curCarte().getCase(cell) == null || !_perso.get_curCarte().getCase(cell).isWalkable(true))
																				{
																					cell = _perso.get_curCell().getID();
																				}
																				String places = _perso.get_curCarte().get_placesStr();
																				String[] p = places.split("\\|");
																				boolean already = false;
																				String team0 = "",team1 = "";
																				try
																				{
																					team0 = p[0];
																				}catch(Exception e){};
																				try
																				{
																					team1 = p[1];
																				}catch(Exception e){};
																				
																				//Si case déjà utilisée
																				Ancestra.printDebug("0 => "+team0+"\n1 =>"+team1+"\nCell: "+CryptManager.cellID_To_Code(cell));
																				for(int a = 0; a <= team0.length()-2;a+=2)if(cell == CryptManager.cellCode_To_ID(team0.substring(a,a+2)))already = true;
																				for(int a = 0; a <= team1.length()-2;a+=2)if(cell == CryptManager.cellCode_To_ID(team1.substring(a,a+2)))already = true;
																				if(already)
																				{
																					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"La case est deja dans la liste");
																					return;
																				}
																				if(team == 0)team0 += CryptManager.cellID_To_Code(cell);
																				else if(team == 1)team1 += CryptManager.cellID_To_Code(cell);
																				
																				String newPlaces = team0+"|"+team1;
																				
																				_perso.get_curCarte().setPlaces(newPlaces);
																				if(!SQLManager.SAVE_MAP_DATA(_perso.get_curCarte()))return;
																				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"Les places ont ete modifiees ("+newPlaces+")");
																				return;
																			}else
																				if (command.equalsIgnoreCase("SPAWNFIX"))
																		          {
																		            String groupData = infos[1];

																		            _perso.get_curCarte().addStaticGroup(_perso.get_curCell().getID(), groupData);
																		            String str = "Le grouppe a ete fixe";

																		            if (SQLManager.SAVE_NEW_FIXGROUP(_perso.get_curCarte().get_id(), _perso.get_curCell().getID(), groupData))
																		              str = str + " et a ete sauvegarde dans la BDD";
																		            SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, str);
																		            return;
																		          }
																			
																			else
																		        	 
																			
																			
																			if(command.equalsIgnoreCase("DELTRIGGER"))
																			{
																				int cellID = -1;
																				try
																				{
																					cellID = Integer.parseInt(infos[1]);
																				}catch(Exception e){};
																				if(cellID == -1 || _perso.get_curCarte().getCase(cellID) == null)
																				{
																					String str = "CellID invalide";
																					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																					return;
																				}
																				
																				_perso.get_curCarte().getCase(cellID).clearOnCellAction();
																				boolean success = SQLManager.REMOVE_TRIGGER(_perso.get_curCarte().get_id(),cellID);
																				String str = "";
																				if(success)	str = "Le trigger a ete retire";
																				else 		str = "Le trigger n'a pas ete retire";
																				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																			}else
																			if(command.equalsIgnoreCase("ADDTRIGGER"))
																			{
																				int actionID = -1;
																				String args = "",cond = "";
																				try
																				{
																					actionID = Integer.parseInt(infos[1]);
																					args = infos[2];
																					cond = infos[3];
																				}catch(Exception e){};
																				if(args.equals("") || actionID <= -3)
																				{
																					String str = "Valeur invalide";
																					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																					return;
																				}
																				
																				_perso.get_curCell().addOnCellStopAction(actionID,args, cond);
																				boolean success = SQLManager.SAVE_TRIGGER(_perso.get_curCarte().get_id(),_perso.get_curCell().getID(),actionID,1,args,cond);
																				String str = "";
																				if(success)	str = "Le trigger a ete ajoute";
																				else 		str = "Le trigger n'a pas ete ajoute";
																				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
																			}else
																			
																			
																			if (command.equalsIgnoreCase("SHUTDOWN"))
																			{
																				int time = 30, OffOn = 0;
																				try
																				{
																					OffOn = Integer.parseInt(infos[1]);
																					time = Integer.parseInt(infos[2]);
																				}catch(Exception e){};
																				
																				if(OffOn == 1 && _TimerStart)// demande de démarer le reboot
																				{
																					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Un shutdown est deja programmer.");
																				}else if(OffOn == 1 && !_TimerStart)
																				{
																					_timer = createTimer(time);
																					_timer.start();
																					_TimerStart = true;
																					String timeMSG = "minutes";
																					if(time <= 1)
																					{
																						timeMSG = "minute";
																					}
																					SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;"+time+" "+timeMSG);
																					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Shutdown lance.");
																				}else if(OffOn == 0 && _TimerStart)
																				{
																					_timer.stop();
																					_TimerStart = false;
																					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Shutdown arrete.");
																				}else if(OffOn == 0 && !_TimerStart)
																				{
																					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Aucun shutdown n'est lance.");
																				}
																			}
																			else
																			{
																				this.commandGmFour(command, infos, msg);
																			}
																			
																		
																		
																		
																				
																	
	}
	private void fullHdv(int ofEachTemplate)
	{
		SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"Démarrage du remplissage!");
		
		Objet objet = null;
		HdvEntry entry = null;
		byte amount = 0;
		int hdv = 0;
		
		int lastSend = 0;
		long time1 = System.currentTimeMillis();//TIME
		for (ObjTemplate curTemp : World.getObjTemplates())//Boucler dans les template
		{
			try
			{
				if(Ancestra.NOTINHDV.contains(curTemp.getID())) continue;
				for (int j = 0; j < ofEachTemplate; j++)//Ajouter plusieur fois le template
				{
					if(curTemp.getType() == 85) break;
					
					objet = curTemp.createNewItem(1, false);
					hdv = getHdv(objet.getTemplate().getType());
					
					if(hdv < 0) break;
						
					amount = (byte) Formulas.getRandomValue(1, 3);
					
					
					entry = new HdvEntry(calculPrice(objet,amount), amount, -1, objet);
					objet.setQuantity(entry.getAmount(true));
					
					
					World.getHdv(hdv).addEntry(entry);
					World.addObjet(objet, false);
				}
			}catch (Exception e)
			{
				continue;
			}
			
			if((System.currentTimeMillis() - time1)/1000 != lastSend
				&& (System.currentTimeMillis() - time1)/1000 % 3 == 0)
			{
				lastSend = (int) ((System.currentTimeMillis() - time1)/1000);
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,(System.currentTimeMillis() - time1)/1000 + "sec Template: "+curTemp.getID());
			}
		}
		SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"Remplissage fini en "+(System.currentTimeMillis() - time1) + "ms");
		World.saveAll(null);
		SocketManager.GAME_SEND_MESSAGE_TO_ALL("HDV remplis!",Ancestra.CONFIG_MOTD_COLOR);
	}
	private int getHdv(int type)
	{
		int rand = Formulas.getRandomValue(1, 4);
		int map = -1;
		
		switch(type)
		{
			case 12:
			case 14: 
			case 26: 
			case 43: 
			case 44: 
			case 45: 
			case 66: 
			case 70: 
			case 71: 
			case 86:
				if(rand == 1)
				{
					map = 4271;
				}else
				if(rand == 2)
				{
					map = 4607;
				}else
				{
					map = 7516;
				}
				return map;
			case 1:
			case 9:
				if(rand == 1)
				{
					map = 4216;
				}else
				if(rand == 2)
				{
					map = 4622;
				}else
				{
					map = 7514;
				}
				return map;
			case 18: 
			case 72: 
			case 77: 
			case 90: 
			case 97: 
			case 113: 
			case 116:
				if(rand == 1)
				{
					map = 8759;
				}else
				{
					map = 8753;
				}
				return map;
			case 63:
			case 64:
			case 69:
				if(rand == 1)
				{
					map = 4287;
				}else
				if(rand == 2)
				{
					map = 4595;
				}else
				if(rand == 3)
				{
					map = 7515;
				}else
				{
					map = 7350;
				}
				return map;
			case 33:
			case 42:
				if(rand == 1)
				{
					map = 2221;
				}else
				if(rand == 2)
				{
					map = 4630;
				}else
				{
					map = 7510;
				}
				return map;
			case 84: 
			case 93: 
			case 112: 
			case 114:
				if(rand == 1)
				{
					map = 4232;
				}else
				if(rand == 2)
				{
					map = 4627;
				}else
				{
					map = 12262;
				}
				return map;
			case 38: 
			case 95: 
			case 96: 
			case 98: 
			case 108:
				if(rand == 1)
				{
					map = 4178;
				}else
				if(rand == 2)
				{
					map = 5112;
				}else
				{
					map = 7289;
				}
				return map;
			case 10:
			case 11:
				if(rand == 1)
				{
					map = 4183;
				}else
				if(rand == 2)
				{
					map = 4562;
				}else
				{
					map = 7602;
				}
				return map;
			case 13: 
			case 25: 
			case 73: 
			case 75: 
			case 76:
				if(rand == 1)
				{
					map = 8760;
				}else
				{
					map = 8754;
				}
				return map;
			case 5: 
			case 6: 
			case 7: 
			case 8: 
			case 19: 
			case 20: 
			case 21: 
			case 22:
				if(rand == 1)
				{
					map = 4098;
				}else
				if(rand == 2)
				{
					map = 5317;
				}else
				{
					map = 7511;
				}
				return map;
			case 39: 
			case 40: 
			case 50: 
			case 51: 
			case 88:
				if(rand == 1)
				{
					map = 4179;
				}else
				if(rand == 2)
				{
					map = 5311;
				}else
				{
					map = 7443;
				}
				return map;
			case 87:
				if(rand == 1)
				{
					map = 6159;
				}else
				{
					map = 6167;
				}
				return map;
			case 34:
			case 52:
			case 60:
				if(rand == 1)
				{
					map = 4299;
				}else
				if(rand == 2)
				{
					map = 4629;
				}else
				{
					map = 7397;
				}
				return map;
			case 41:
			case 49:
			case 62:
				if(rand == 1)
				{
					map = 4247;
				}else
				if(rand == 2)
				{
					map = 4615;
				}else
				if(rand == 3)
				{
					map = 7501;
				}else
				{
					map = 7348;
				}
				return map;
			case 15: 
			case 35: 
			case 36: 
			case 46: 
			case 47: 
			case 48: 
			case 53: 
			case 54: 
			case 55: 
			case 56: 
			case 57: 
			case 58: 
			case 59: 
			case 65: 
			case 68: 
			case 103: 
			case 104: 
			case 105: 
			case 106: 
			case 107: 
			case 109: 
			case 110: 
			case 111:
				if(rand == 1)
				{
					map = 4262;
				}else
				if(rand == 2)
				{
					map = 4646;
				}else
				{
					map = 7413;
				}
				return map;
			case 78:
				if(rand == 1)
				{
					map = 8757;
				}else
				{
					map = 8756;
				}
				return map;
			case 2:
			case 3:
			case 4:
				if(rand == 1)
				{
					map = 4174;
				}else
				if(rand == 2)
				{
					map = 4618;
				}else
				{
					map = 7512;
				}
				return map;
			case 16:
			case 17:
			case 81:
				if(rand == 1)
				{
					map = 4172;
				}else
				if(rand == 2)
				{
					map = 4588;
				}else
				{
					map = 7513;
				}
				return map;
			case 83:
				if(rand == 1)
				{
					map = 10129;
				}else
				{
					map = 8482;
				}
				return map;
			case 82:
				return 8039;
			default:
				return -1;
		}
	}
	private int calculPrice(Objet obj, int logAmount)
	{
		int amount = (byte)(Math.pow(10,(double)logAmount) / 10);
		int stats = 0;
		
		for(int curStat : obj.getStats().getMap().values())
		{
			stats += curStat;
		}
		if(stats > 0)
			return (int) (((Math.cbrt(stats) * Math.pow(obj.getTemplate().getLevel(), 2)) * 10 + Formulas.getRandomValue(1, obj.getTemplate().getLevel()*100)) * amount);
		else
			return (int) ((Math.pow(obj.getTemplate().getLevel(),2) * 10 + Formulas.getRandomValue(1, obj.getTemplate().getLevel()*100))*amount);
	}
}