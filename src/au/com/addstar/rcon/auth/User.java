package au.com.addstar.rcon.auth;

import org.bukkit.configuration.ConfigurationSection;

public class User extends PermissionObject
{
	private AuthManager mManager;
	public User(ConfigurationSection section, AuthManager manager)
	{
		super(section);
		mManager = manager;
	}
	
	public String getGroup()
	{
		return getConfig().getString("group");
	}
	
	public Group getGroupObject()
	{
		return mManager.getGroup(getGroup());
	}
	
	public void setGroup(String group)
	{
		getConfig().set("group", group);
	}
}
