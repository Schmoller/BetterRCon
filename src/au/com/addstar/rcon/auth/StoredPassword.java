package au.com.addstar.rcon.auth;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class StoredPassword
{
	private String mHash;
	private String mSalt;
	
	public StoredPassword(String hash, String salt)
	{
		mHash = hash;
		mSalt = salt;
	}
	
	public String getHash()
	{
		return mHash;
	}
	
	public String getSalt()
	{
		return mSalt;
	}
	
	public boolean matches(char[] password)
	{
		StoredPassword other = generate(password, mSalt);
		return mHash.equals(other.mHash);
	}
	
	@Override
	public boolean equals( Object obj )
	{
		if(!(obj instanceof StoredPassword))
			return false;
		
		StoredPassword other = (StoredPassword)obj;
		
		return mHash.equals(other.mHash) && mSalt.equals(other.mSalt);
	}
	
	@Override
	public int hashCode()
	{
		return mHash.hashCode() ^ mSalt.hashCode();
	}
	
	public static StoredPassword generate(char[] password)
	{
		return generate(password, null);
	}
	
	private static StoredPassword generate(char[] password, String salt)
	{
		try
		{
			if(salt == null)
				salt = genSalt();
			
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(salt.getBytes());
			byte[] result = md.digest(new String(password).getBytes("UTF-8"));
			
			return new StoredPassword(bytesToHex(result), salt);
		}
		catch(NoSuchAlgorithmException e)
		{
			throw new UnsupportedOperationException();
		}
		catch ( UnsupportedEncodingException e )
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private static String genSalt() throws NoSuchAlgorithmException
	{
		SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[16];
		rand.nextBytes(salt);
		return bytesToHex(salt);
	}
	
	private static String bytesToHex(byte[] data)
	{
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < data.length; ++i)
			b.append(Integer.toHexString(data[i] + 0x100).substring(1));
		
		return b.toString();
	}
}
