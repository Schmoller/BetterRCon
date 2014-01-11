package au.com.addstar.rcon.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PacketLogin extends RConPacket
{
	public String username;
	public String password;
	
	public boolean silentMode;
	public boolean noFormat;
	
	public PacketLogin()
	{
		super(1);
	}
	
	public PacketLogin(String username, String password, boolean silent, boolean noFormat)
	{
		super(1);
		this.username = username;
		this.password = password;
		silentMode = silent;
		this.noFormat = noFormat;
	}
	
	@Override
	public void write( DataOutput output ) throws IOException
	{
		super.write(output);
		output.writeUTF(username);
		output.writeUTF(password);
		
		int flags = 0;
		if(silentMode)
			flags |= 1;
		if(noFormat)
			flags |= 2;
		
		output.writeByte(flags);
	}
	
	@Override
	public void read( DataInput input ) throws IOException
	{
		username = input.readUTF();
		password = input.readUTF();
		
		int flags = input.readUnsignedByte();
		
		silentMode = (flags & 1) != 0;
		noFormat = (flags & 2) != 0;
	}

}
