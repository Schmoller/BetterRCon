package au.com.addstar.rcon.auth;

import org.bukkit.configuration.ConfigurationSection;

public class Group extends PermissionObject
{
	private AuthManager mManager;
	
	public Group(ConfigurationSection section, AuthManager manager)
	{
		super(section);
		mManager = manager;
	}
	
	public String getParent()
	{
		return getConfig().getString("parent", null);
	}
	
	public Group getParentGroup()
	{
		return mManager.getGroup(getParent());
	}
	
	public void setParent(String parent)
	{
		getConfig().set("parent", parent);
	}
	
	@Override
	public int hashCode()
	{
		return getName().hashCode();
	}
	
	@Override
	public boolean equals( Object obj )
	{
		if(!(obj instanceof Group))
			return false;
		
		return ((Group)obj).getName().equals(getName());
	}
}
