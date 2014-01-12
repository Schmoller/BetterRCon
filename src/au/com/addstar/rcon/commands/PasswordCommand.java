package au.com.addstar.rcon.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.WeakHashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.addstar.rcon.BetterRCon;
import au.com.addstar.rcon.auth.StoredPassword;
import au.com.addstar.rcon.auth.User;

public class PasswordCommand implements ICommand
{
	private WeakHashMap<CommandSender, User> mLastUser = new WeakHashMap<CommandSender, User>();
	private WeakHashMap<CommandSender, char[]> mLastPasswords = new WeakHashMap<CommandSender, char[]>();
	
	@Override
	public String getName()
	{
		return "rootpassword";
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
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}
	
	protected final boolean handlePasswordChange(CommandSender sender, User user, char[] newPassword, char[] oldPassword)
	{
		char[] lastPassword = mLastPasswords.remove(sender);
		User lastUser = mLastUser.remove(sender);
		
		if(lastPassword != null && user.equals(lastUser))
		{
			if(oldPassword == null)
			{
				if(Arrays.equals(lastPassword, newPassword))
				{
					user.setPassword(StoredPassword.generate(newPassword));
					sender.sendMessage(user.getName() + "'s password has been changed");
					BetterRCon.getLog().warning(user.getName() + "'s RCon password was changed by " + sender.getName());
					
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
				
				return true;
			}
		}

		
		if(user.getPassword() == null)
		{
			if(oldPassword != null)
			{
				sender.sendMessage(ChatColor.RED + "No existing password exists, just use " + ChatColor.YELLOW + "/rcon password <password>");
				return true;
			}
			
			mLastPasswords.put(sender, newPassword);
			mLastUser.put(sender, user);
		}
		else
		{
			if(oldPassword == null)
				return false;

			if(!user.getPassword().matches(oldPassword))
			{
				sender.sendMessage(ChatColor.RED + "The specified password does not match the existing password!");
				BetterRCon.getLog().warning(String.format("%s tried to change %s's RCon password", sender.getName(), user.getName()));
				return true;
			}
			
			mLastPasswords.put(sender, newPassword);
			mLastUser.put(sender, user);
		}
		
		sender.sendMessage("Please confirm the new password with " + getCommandString());
		return true;
	}
	
	protected String getCommandString()
	{
		return "/rcon password <newpassword>";
	}
	
	protected final boolean isConfirming(CommandSender sender, User user)
	{
		return mLastPasswords.containsKey(sender) && user.equals(mLastUser.get(sender));
	}
	
	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		if(args.length != 1 && args.length != 2)
			return false;
		
		User consoleUser = BetterRCon.getAuth().getUser("Console");
		
		if(args.length == 1)
			return handlePasswordChange(sender, consoleUser, args[0].toCharArray(), null);
		else
			return handlePasswordChange(sender, consoleUser, args[1].toCharArray(), args[0].toCharArray());
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}
