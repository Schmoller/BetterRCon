package au.com.addstar.rcon.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PacketLogin extends RConPacket
{
	public String username;
	public char[] password;
	
	public boolean silentMode;
	public boolean noFormat;
	
	public PacketLogin()
	{
		super(1);
	}
	
	public PacketLogin(String username, char[] password, boolean silent, boolean noFormat)
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
		
		output.writeShort(password.length);
		for(int i = 0; i < password.length; ++i)
			output.writeChar(password[i]);
		
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
		
		int passwordLen = input.readShort();
		password = new char[passwordLen];
		for(int i = 0; i < passwordLen; ++i)
			password[i] = input.readChar();
		
		int flags = input.readUnsignedByte();
		
		silentMode = (flags & 1) != 0;
		noFormat = (flags & 2) != 0;
	}

}
