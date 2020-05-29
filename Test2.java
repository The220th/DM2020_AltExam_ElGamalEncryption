import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Scanner;

import eltech.DM2020.*;

public class Test2
{

	public static void main(String[] args)
	{
		Scanner in = new Scanner(System.in);
		int min = 0;
		int max = 110;
		int rounds = 100000;
		BigInteger buffMin = BigInteger.valueOf(min);
		BigInteger buffMax = BigInteger.valueOf(max);
		//BigInteger buffMin = new BigInteger("0");
		//BigInteger buffMax = new BigInteger("10");
		int[] a = new int[max - min];
		//for(int i = 0; i < a.length; i++)
			//a[i] = 0;
		for(int y = 0; y < rounds; y++)
		{
			int j = PrimeNum.rndBigInteger(buffMin, buffMax).intValue();
			//System.out.println("[" + min + "; " + max + "), rnd = " + j);
			a[j]++;
		}
		System.out.println("avg = " + 1.0/(max-min));
		for(int i = 0; i < a.length; i++)
			System.out.println(i + ": " + a[i] + "(" + a[i]/((double)rounds) + ")");
		
		/*String S = "kurwa";
		System.out.println(S);
		
		AES256 aes = new AES256();
		byte[] key = aes.getKey();
		
		byte[] src = S.getBytes();
		System.out.println( new String(src) );
		
		byte[] enmsg = aes.makeAES256(src, AES256.ifENCRYPT);
		
		byte[] demsg = aes.makeAES256(enmsg, AES256.ifDECRYPT);
		System.out.println( new String(demsg) );*/
	}
}






























