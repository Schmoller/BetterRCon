package au.com.addstar.rcon.commands;

import java.io.IOException;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.addstar.rcon.BetterRCon;
import au.com.addstar.rcon.ICommand;
import au.com.addstar.rcon.auth.StoredPassword;
import au.com.addstar.rcon.auth.User;

public class PasswordCommand implements ICommand
{
	private CommandSender mLastSender;
	private String mLastPassword;
	
	@Override
	public String getName()
	{
		return "password";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "rcon.manage.password";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + ChatColor.GOLD + " <oldpassword> <newpassword>";
	}

	@Override
	public String getDescription()
	{
		return null;
	}

	@Override
	public boolean canBeConsole()
	{
		return true;
	}

	@Override
	public boolean canBeCommandBlock() { return false; }

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		if(args.length != 1 && args.length != 2)
			return false;
		
		User consoleUser = BetterRCon.getAuth().getUser("Console");
		
		if(mLastSender == sender)
		{
			if(args.length == 1)
			{
				if(args[0].equals(mLastPassword))
				{
					consoleUser.setPassword(StoredPassword.generate(args[0].toCharArray()));
					sender.sendMessage("Console's password has been changed");
					BetterRCon.getLog().warning("RCon password was changed by " + sender.getName());
					
					try
					{
						BetterRCon.getAuth().write();
					}
					catch ( IOException e )
					{
						sender.sendMessage("Unable to save changes, an internal error occured.");
						e.printStackTrace();
					}
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "The entered password does not match.");
				}
				
				mLastSender = null;
				mLastPassword = null;
				
				return true;
			}
		}

		
		if(consoleUser.getPassword() == null)
		{
			if(args.length != 1)
			{
				sender.sendMessage(ChatColor.RED + "No existing password exists, just use " + ChatColor.YELLOW + "/rcon password <password>");
				return true;
			}
			
			mLastPassword = args[0];
		}
		else
		{
			if(args.length != 2)
				return false;

			if(!consoleUser.getPassword().matches(args[0].toCharArray()))
			{
				sender.sendMessage(ChatColor.RED + "The specified password does not match the existing password!");
				return true;
			}
			mLastPassword = args[1];
		}
		
		mLastSender = sender;
		sender.sendMessage("Please confirm the new password with /rcon password <newpassword>");
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}
