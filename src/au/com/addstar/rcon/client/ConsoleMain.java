package au.com.addstar.rcon.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.Ansi.Attribute;

import jline.ConsoleReader;

public class ConsoleMain
{
	private static final Map<Character, String> mColors = new HashMap<Character, String>();
	private static String mColorChar = "§";
	
	static
	{
		mColors.put('0', Ansi.ansi().fg(Ansi.Color.BLACK).toString());
        mColors.put('1', Ansi.ansi().fg(Ansi.Color.BLUE).toString());
        mColors.put('2', Ansi.ansi().fg(Ansi.Color.GREEN).toString());
        mColors.put('3', Ansi.ansi().fg(Ansi.Color.CYAN).toString());
        mColors.put('4', Ansi.ansi().fg(Ansi.Color.RED).toString());
        mColors.put('5', Ansi.ansi().fg(Ansi.Color.MAGENTA).toString());
        mColors.put('6', Ansi.ansi().fg(Ansi.Color.YELLOW).bold().toString());
        mColors.put('7', Ansi.ansi().fg(Ansi.Color.WHITE).toString());
        mColors.put('8', Ansi.ansi().fg(Ansi.Color.BLACK).bold().toString());
        mColors.put('9', Ansi.ansi().fg(Ansi.Color.BLUE).bold().toString());
        mColors.put('a', Ansi.ansi().fg(Ansi.Color.GREEN).bold().toString());
        mColors.put('b', Ansi.ansi().fg(Ansi.Color.CYAN).bold().toString());
        mColors.put('c', Ansi.ansi().fg(Ansi.Color.RED).bold().toString());
        mColors.put('d', Ansi.ansi().fg(Ansi.Color.MAGENTA).bold().toString());
        mColors.put('e', Ansi.ansi().fg(Ansi.Color.YELLOW).bold().toString());
        mColors.put('f', Ansi.ansi().fg(Ansi.Color.WHITE).bold().toString());
        mColors.put('k', Ansi.ansi().a(Attribute.BLINK_SLOW).toString());
        mColors.put('l', Ansi.ansi().a(Attribute.UNDERLINE_DOUBLE).toString());
        mColors.put('m', Ansi.ansi().a(Attribute.STRIKETHROUGH_ON).toString());
        mColors.put('n', Ansi.ansi().a(Attribute.UNDERLINE).toString());
        mColors.put('o', Ansi.ansi().a(Attribute.ITALIC).toString());
        mColors.put('r', Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.DEFAULT).toString());
	}
	
	private static ConsoleReader mConsole;
	
	public static void main(String[] args)
	{
		try
		{
			mConsole = new ConsoleReader();
			mConsole.setBellEnabled(false);
		}
		catch(IOException e)
		{
			System.out.println("Cannot initialize console. Exiting");
			return;
		}
		
		if(args.length < 1)
		{
			printUsage();
			return;
		}
		
		String host = args[0];
		
		int port = 8000;
		String username = null;
		String password = null;
		
		if(args.length > 1)
		{
			for(int i = 1; i < args.length - 1; i+= 2)
			{
				if(!args[i].startsWith("-") || args[i].length() == 1)
				{
					System.out.println("Unknown option: " + args[i]);
					printUsage();
					return;
				}
				
				char opt = args[i].charAt(1);
				
				switch(opt)
				{
				case 'p':
					try
					{
						port = Integer.parseInt(args[i+1]);
						if(port <= 0 || port > 65535)
						{
							printUsage();
							return;
						}
					}
					catch(NumberFormatException e)
					{
						printUsage();
					}
					break;
				case 'P':
					password = args[i+1];
					break;
				case 'U':
					username = args[i+1];
					break;
				default:
					System.out.println("Unknown option: " + args[i]);
					printUsage();
					return;
				}
			}
		}
		
		ConsoleMain con = new ConsoleMain(host, port, username, password);
		con.run();
	}
	
	private static void printUsage()
	{
		System.err.println("Better Remote Console version 1.0");
		System.err.println();
		System.err.println("Usage: <host> [options]");
		System.err.println();
		System.err.println("-p <port> Specifies port number. Defaults to 8000");
		System.err.println("-U <username> Specifies your username.");
		System.err.println("-P <password> Specifies password to use");
	}
	
	public static void printString(String string)
	{
		try
		{
			string = string.replaceAll("Â", "");
            for (Character color : mColors.keySet()) 
                string = string.replaceAll(mColorChar + color, mColors.get(color));
			
            AnsiConsole.out.println(ConsoleReader.RESET_LINE + string + Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.DEFAULT));
			
			mConsole.redrawLine();
			mConsole.flushConsole();
		}
		catch(IOException e)
		{
		}
	}
	
	
	private String mHost;
	private int mPort;
	
	private String mUsername;
	private String mPassword;
	
	private Connection mConnection;
	
	public ConsoleMain(String host, int port, String username, String password)
	{
		mHost = host;
		mPort = port;
		
		if(username == null)
			mUsername = "Console";
		else
			mUsername = username;
		
		if(password == null)
			mPassword = "";
		else
			mPassword = password;
	}
	
	public void run()
	{
		try
		{
			mConnection = new Connection(mHost, mPort);
		}
		catch(IOException e)
		{
			printString(String.format("Unable to connect to host (%s:%s)", mHost, mPort));
			printString(e.getMessage());
			return;
		}
		catch(IllegalArgumentException e)
		{
			printString(e.getMessage());
			return;
		}
		
		mConsole.addCompletor(new TabCompleter(mConnection));

		try
		{
			if(!mConnection.login(mUsername, mPassword, false, false))
			{
				mConnection.close();
				return;
			}
			
			while(true)
			{
				if(!mConnection.isOpen())
					break;
				
				String command = mConsole.readLine(">");
				
				if(command != null) // Handle commands
				{
					if(command.equals("quit"))
						break;
					
					mConnection.sendCommand(command);
				}
			}

		}
		catch(IOException e)
		{
		}
		mConnection.close();
	}
}
