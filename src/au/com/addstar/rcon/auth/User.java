package au.com.addstar.rcon.auth;

import org.apache.commons.lang.Validate;
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
	
	public StoredPassword getPassword()
	{
		String stored = getConfig().getString("password", null);
		if(stored == null)
			return null;
		
		String[] parts = stored.split(":");
		if(parts.length != 2)
			return null;
		
		return new StoredPassword(parts[0], parts[1]);
	}
	
	public void setPassword(StoredPassword password)
	{
		Validate.notNull(password);
		
		getConfig().set("password", String.format("%s:%s", password.getHash(), password.getSalt()));
	}
	
	@Override
	public int hashCode()
	{
		return getName().hashCode();
	}
	
	@Override
	public boolean equals( Object obj )
	{
		if(!(obj instanceof User))
			return false;
		
		return ((User)obj).getName().equals(getName());
	}
}
