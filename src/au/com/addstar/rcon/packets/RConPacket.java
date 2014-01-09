package au.com.addstar.rcon.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class RConPacket
{
	@SuppressWarnings( "unchecked" )
	private static Class<? extends RConPacket>[] packets = new Class[256];
	
	static
	{
		setPacket(1, PacketLogin.class);
		setPacket(20, PacketCommand.class);
		setPacket(30, PacketMessage.class);
	}
	
	public static void setPacket(int id, Class<? extends RConPacket> packetClass) throws IllegalArgumentException
	{
		packets[id] = packetClass;
		try
		{
			packetClass.newInstance();
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException(packetClass.getName() + " does not have a default constructor, or it is not visible");
		}
	}
	
	private int mId;
	protected RConPacket(int id)
	{
		mId = id;
	}
	
	public void write(DataOutput output) throws IOException
	{
		output.writeByte(mId);
	}
	
	public abstract void read(DataInput input) throws IOException;
	
	public static RConPacket load(DataInput input) throws IOException
	{
		int id = input.readUnsignedByte();
		
		Class<? extends RConPacket> packetClass = packets[id];
		if(packetClass == null)
			throw new IOException("Bad packet id " + id);
		
		try
		{
			RConPacket packet = packetClass.newInstance();
			packet.read(input);
			
			return packet;
		}
		catch(InstantiationException e)
		{
			e.printStackTrace();
		}
		catch(IllegalAccessException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
}
