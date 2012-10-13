package common;
import java.io.PrintStream;

import org.fusesource.jansi.AnsiConsole;
public class Console {
	public enum ConsoleColorEnum
	{
		//FONT
		BOLD(1),
		UNDERLINE(4),
		BLINK(5),
		HIDDEN(8),
		BLACK(30),
		RED(31),
		GREEN(32),
		YELLOW(33),
		BLUE(34),
		MAGENTA(35),
		CYAN(36),
		WHITE(37),
		//BG
		BG_BLACK(40),
		BG_RED(41),
		BG_GREEN(42),
		BG_YELLOW(43),
		BG_BLUE(44),
		BG_MAGENTA(45),
		BG_CYAN(46),
		BG_WHITE(47),
		//SPECIAL
		BLACK_AND_BG_WHITE(7),
		RESET(0);
		private int color;
		private ConsoleColorEnum(int color)
		{
			this.color = color;
		}
		public int get()
		{
			return color;
		}
	}
	public static void clear() {
        AnsiConsole.out.print("\033[H\033[2J");
    }
    
    public static void setTitle(String title) {
        AnsiConsole.out.append("\033]0;").append(title).append("\007");
    }
    public static void print(String message)
    {
    	AnsiConsole.out.print(message);
    }
    public static void println(String message)
    {
    	AnsiConsole.out.println(message);
    }
    public static PrintStream out()
    {
    	return AnsiConsole.out;
    }
    
    public static void println(String msg, ConsoleColorEnum color)
    {
    	AnsiConsole.out.println(new StringBuilder().append("\033[").append(color.get()).append("m").append(msg).append("\033[").append(ConsoleColorEnum.RESET.get()).append("m").toString());
    }
    public static void print(String msg, ConsoleColorEnum color)
    {
    	AnsiConsole.out.print(new StringBuilder().append("\033[").append(color.get()).append("m").append(msg).append("\033[").append(ConsoleColorEnum.RESET.get()).append("m").toString());
    }
    public static void print(String msg, ConsoleColorEnum color, ConsoleColorEnum Background)
    {
    	AnsiConsole.out.print(new StringBuilder().append("\033[").append(color.get()).append("m").append("\033[").append(Background.get()).append("m").append(msg).append("\033[").append(ConsoleColorEnum.RESET.get()).append("m").toString());
    }
    public static void println(String msg, ConsoleColorEnum color, ConsoleColorEnum Background)
    {
    	AnsiConsole.out.println(new StringBuilder().append("\033[").append(color.get()).append("m").append("\033[").append(Background.get()).append("m").append(msg).append("\033[").append(ConsoleColorEnum.RESET.get()).append("m").toString());
    }
}
