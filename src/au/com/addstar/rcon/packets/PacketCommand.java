package au.com.addstar.rcon.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PacketCommand extends RConPacket
{
	public String command;
	
	public PacketCommand()
	{
		super(20);
	}
	
	public PacketCommand(String command)
	{
		super(20);
		this.command = command;
	}
	
	@Override
	public void write( DataOutput output ) throws IOException
	{
		super.write(output);
		output.writeUTF(command);
	}
	
	@Override
	public void read( DataInput input ) throws IOException
	{
		command = input.readUTF();
	}

}
