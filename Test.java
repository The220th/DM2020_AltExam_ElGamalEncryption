import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import eltech.DM2020.*;

public class Test
{
	public static void main(String[] args)
	{
		ElGamalCipher test = new ElGamalCipher();
		ElGamalCipher test2 = new ElGamalCipher(test.getP(), test.getA());
		
		System.out.println("X1: " + new BigInteger(test.getX()));
		System.out.println("Y1: " + new BigInteger(test.getY()));
		System.out.println("A1: " + new BigInteger(test.getA()));
		System.out.println("P1: " + new BigInteger(test.getP()));
		
		System.out.println("X2: " + new BigInteger(test2.getX()));
		System.out.println("Y2: " + new BigInteger(test2.getY()));
		System.out.println("A2: " + new BigInteger(test2.getA()));
		System.out.println("P2: " + new BigInteger(test2.getP()) + "\n");
		
		String S = "ttest3212534232432532 235 23 2141257657857234325";
		
		byte[] enMsg = test.encrypt(S.getBytes(), test2.getY());
		System.out.println("\n\n");
		byte[] deMsg = test2.decrypt(enMsg);
		System.out.println((new String(deMsg)));
		
		/*Long a = 54275682714523523L;
		byte[] aByte = ByteBuffer.allocate(Long.BYTES).putLong(a).array();
		System.out.println(aByte.length);
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES).put(aByte);
		buffer.flip();
		a = buffer.getLong();
		System.out.println(a);
		int i, j;
		
        BigInteger r = new BigInteger("12335213542315252315263264326732476324573457435");
        BigInteger e = new BigInteger("3213457435263246346354746574536543764357653465346435653465345");
        byte[] eByte;
        byte[] rByte;
        int index;
        int n = 1;
        ArrayList<byte[]> msgNew = new ArrayList<byte[]>();
        for(i = 0; i < n; i++)
        {
            eByte = e.toByteArray();
            rByte = r.toByteArray();
            index = Integer.BYTES + rByte.length;
            //index int -> byte[]
            byte[] indexByte = ByteBuffer.allocate(Integer.BYTES).putInt(index).array();
            //Сцепить все байт-массивы
            byte[] allByteArray = new byte[indexByte.length + rByte.length + eByte.length];
            ByteBuffer byteBuffer = ByteBuffer.wrap(allByteArray);
            byteBuffer.put(indexByte);
            byteBuffer.put(rByte);
            byteBuffer.put(eByte);
            // Засунуть в сообщения
            msgNew.add(byteBuffer.array());
			System.out.println("index = " + index + "; len indexByte: " + indexByte.length);
			System.out.println("r: " + rByte.length);
			System.out.println("e: " + eByte.length);
			System.out.println("all: " + msgNew.get(0).length);
        }
		
		System.out.println("\n");
		byte[] arrayM = msgNew.get(0);
		byte[] buffByte = new byte[Integer.BYTES];
		for(i = 0; i < buffByte.length; i++)
			buffByte[i] = arrayM[i];
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES).put(buffByte);
		buffer.flip();
		int next = buffer.getInt();
		System.out.println(next);
		
		buffByte = new byte[next - Integer.BYTES];
		for(j = 0, i = Integer.BYTES; i < next; i++, j++)
			buffByte[j] = arrayM[i];
		r = new BigInteger(buffByte);
		
		buffByte = new byte[arrayM.length - next];
		for(j = 0, i = next; i < arrayM.length; i++, j++)
			buffByte[j] = arrayM[i];
		e = new BigInteger(buffByte);
		System.out.println(r + "\n" + e);*/
	}
}






























