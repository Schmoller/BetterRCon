package au.com.addstar.rcon.commands.accounts;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.addstar.rcon.BetterRCon;
import au.com.addstar.rcon.auth.AuthManager;
import au.com.addstar.rcon.auth.StoredPassword;
import au.com.addstar.rcon.auth.User;
import au.com.addstar.rcon.commands.CommandSenderType;
import au.com.addstar.rcon.commands.ICommand;

public class CopyCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "copy";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "rcon.account.manage.add";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <existingaccount> <newaccount> <password>";
	}

	@Override
	public String getDescription()
	{
		return "Copy an existing account's settings to a new account";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Console, CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		if(args.length != 3)
			return false;
		
		AuthManager manager = BetterRCon.getAuth();
		
		User existing = manager.getUser(args[0]);
		
		if(existing == null)
		{
			sender.sendMessage(ChatColor.RED + "The user account " + args[0] + " doesnt exist");
			return true;
		}
		
		if(manager.getUser(args[1]) == null)
		{
			sender.sendMessage(ChatColor.RED + "The user account " + args[1] + " already exists.");
			return true;
		}
		
		char[] password = args[2].toCharArray();
		
		try
		{
			User user = manager.createUser(args[1]);
			
			user.setOp(existing.isOp());
			user.setPassword(StoredPassword.generate(password));
			user.setGroup(existing.getGroup());
			for(String perm : existing.getDefinedPermissions())
			{
				if(perm.startsWith("-"))
					user.addPerm(perm.substring(1), true);
				else
					user.addPerm(perm, false);
			}
			
			sender.sendMessage(ChatColor.GREEN + "Account " + user.getName() + " was successfully created.");
			manager.write();
		}
		catch(IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}
		catch ( IOException e )
		{
			sender.sendMessage(ChatColor.RED + "An error occured while saving the new account.");
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}
