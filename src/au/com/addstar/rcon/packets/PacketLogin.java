package au.com.addstar.rcon.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PacketLogin extends RConPacket
{
	public String username;
	public long passwordHash;
	
	public PacketLogin()
	{
		super(1);
	}
	
	public PacketLogin(String username, long passwordHash)
	{
		super(1);
		this.username = username;
		this.passwordHash = passwordHash;
	}
	
	@Override
	public void write( DataOutput output ) throws IOException
	{
		super.write(output);
		output.writeUTF(username);
		output.writeLong(passwordHash);
	}
	
	@Override
	public void read( DataInput input ) throws IOException
	{
		username = input.readUTF();
		passwordHash = input.readLong();
	}

}
