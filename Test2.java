import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import eltech.DM2020.*;

public class Test2
{

	public static void main(String[] args)
	{
		String S = "kurwa";
		System.out.println(S);
		
		AES256 aes = new AES256();
		byte[] key = aes.getKey();
		
		byte[] src = S.getBytes();
		System.out.println( new String(src) );
		
		byte[] enmsg = aes.makeAES256(src, AES256.ifENCRYPT);
		
		byte[] demsg = aes.makeAES256(enmsg, AES256.ifDECRYPT);
		System.out.println( new String(demsg) );
	}
}






























