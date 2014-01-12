package au.com.addstar.rcon.commands.accounts;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.command.CommandSender;

import au.com.addstar.rcon.BetterRCon;
import au.com.addstar.rcon.RconConnection;
import au.com.addstar.rcon.auth.User;
import au.com.addstar.rcon.commands.CommandSenderType;

public class PasswordCommand extends au.com.addstar.rcon.commands.PasswordCommand
{
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
		return null;
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		if(sender instanceof RconConnection)
			return label + " [username] [oldpassword] <newpassword>";
		else
			return label + " <username> [oldpassword] <newpassword>";
		
	}

	@Override
	public String getDescription()
	{
		return "Changes your password";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Console, CommandSenderType.Player);
	}

	@Override
	protected String getCommandString()
	{
		return "/rcon account password <newpassword>";
	}
	
	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		User user = null;
		char[] oldPassword = null;
		char[] newPassword = null;
		
		if(sender instanceof RconConnection)
		{
			if(args.length == 0 || args.length > 3)
				return false;
			
			user = ((RconConnection)sender).getUser();
			if(user == null) 
				user = BetterRCon.getAuth().getUser("Console");
			
			if(args.length == 1)
				newPassword = args[0].toCharArray();
			else if(args.length == 3)
			{
				user = BetterRCon.getAuth().getUser(args[0]);
				oldPassword = args[1].toCharArray();
				newPassword = args[2].toCharArray();
			}
			else
			{
				// Could be either of the optional ones
				User alternate = BetterRCon.getAuth().getUser(args[0]);
				
				if(alternate != null && isConfirming(sender, alternate)) // user was specified
					user = alternate;
				else // oldpassword was specified
					oldPassword = args[0].toCharArray();
				
				newPassword = args[1].toCharArray();
			}
		}
		else
		{
			if(args.length != 2 && args.length != 3)
				return false;
			
			user = BetterRCon.getAuth().getUser(args[0]);
			
			if(args.length == 2)
				newPassword = args[1].toCharArray();
			else
			{
				newPassword = args[2].toCharArray();
				oldPassword = args[1].toCharArray();
			}
		}
		
		return handlePasswordChange(sender, user, newPassword, oldPassword);
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}
