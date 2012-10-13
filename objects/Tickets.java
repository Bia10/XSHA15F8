package objects;

public class Tickets {
	
	private int id;
	private String message;
	private String joueur;
	private String maitrejeu;
	private int valid;
	
	public Tickets (int id , String message , String joueur,String maitrejeu, int valid)
	{
		this.id = id;
		this.message = message;
		this.joueur = joueur;
		this.maitrejeu = maitrejeu;
		this.valid = valid;
	}
	
	public int getID()
	{
		return this.id;
	}
	public String getMessage()
	{
		return this.message;
	}
	public String getJoueur()
	{
		return this.joueur;
	}
	public String getMaitrejeu()
	{
		return this.maitrejeu;
	}
	
	public int getValid()
	{ 
		return this.valid;
	}
	
	public void setID(int id)
	{
		this.id = id;
	}
	public void setMessage(String message)
	{
		this.message = message;
	}
	public void setJoueur(String joueur)
	{
		this.joueur = joueur;
	}
	public void setMaitrejeu(String maitrejeu)
	{
		this.maitrejeu = maitrejeu;
	}
	public void setValid(int valid)
	{
		this.valid = valid;
	}
	
	public String getTicket()
	{
		String info = "";
		
		info += "Ticket écrit par : "+this.joueur+" - Message : \n"+this.message;
		
		return info;
	
	}
}
