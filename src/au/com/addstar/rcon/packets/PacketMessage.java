package au.com.addstar.rcon.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PacketMessage extends RConPacket
{
	public String message;
	
	public PacketMessage()
	{
		super(30);
	}
	
	public PacketMessage(String message)
	{
		super(30);
		this.message = message;
	}
	
	@Override
	public void write( DataOutput output ) throws IOException
	{
		super.write(output);
		output.writeUTF(message);
	}
	@Override
	public void read( DataInput input ) throws IOException
	{
		message = input.readUTF();
	}

}
