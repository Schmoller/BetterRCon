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

public class AddCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "add";
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
		return label + " <accountName> <password> [isOp]";
	}

	@Override
	public String getDescription()
	{
		return "Adds an account with the specified name. isOp defaults to true by default";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		if(args.length != 2 && args.length != 3)
			return false;
		
		AuthManager manager = BetterRCon.getAuth();
		
		User user = manager.getUser(args[0]);
		
		if(user != null)
		{
			sender.sendMessage(ChatColor.RED + "That user account already exists");
			return true;
		}
		
		char[] password = args[1].toCharArray();
		
		boolean op = true;
		if(args.length == 3)
			op = Boolean.parseBoolean(args[2]);

		try
		{
			user = manager.createUser(args[0]);
			
			user.setOp(op);
			user.setPassword(StoredPassword.generate(password));
			
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
