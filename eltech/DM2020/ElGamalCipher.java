package eltech.DM2020;

import java.math.BigInteger;
import eltech.DM2020.PrimeNum;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

import javax.sound.sampled.ReverbType;

public class ElGamalCipher
{
    /**
     * Закрытый ключ
     */
    private BigInteger X;
    /**
     * Открытый ключ
     */
    private BigInteger Y;

    /**
     * Общий параметр A
     */
    private BigInteger A;
    /**
     * Общий параметр P - поле вычитов
     */
    private BigInteger P;

    public ElGamalCipher(byte[] P, byte[] A, byte[] X, byte[] Y)
    {
        this.P = new BigInteger(P);
        this.A = new BigInteger(A);
        this.X = new BigInteger(X);
        this.Y = new BigInteger(Y);
    }

    public ElGamalCipher(byte[] P, byte[] A)
    {
        this.P = new BigInteger(P);
        this.A = new BigInteger(A);
        if(this.P.toString().length() > 100)
            this.X = PrimeNum.rndBigInteger(PrimeNum.getZahl(80), this.P);
        else
            this.X = PrimeNum.rndBigInteger(BigInteger.valueOf(2), this.P);
        
        this.Y = this.A.modPow(this.X, this.P);
    }

    public ElGamalCipher()
    {
        PrimeNum numGen = new PrimeNum(10000);
        BigInteger q = numGen.rndSophieGermainNum(50, 5); // 50 = 300
        this.P = q.multiply(BigInteger.valueOf(2)).add(BigInteger.ONE);
        do
        {
            this.A = PrimeNum.rndBigInteger(PrimeNum.getZahl(10), this.P); // 10 = 150
        }while( this.A.modPow(q, this.P).compareTo(BigInteger.ONE) == 0 );
        
        this.X = PrimeNum.rndBigInteger(PrimeNum.getZahl(10), this.P); // 10 = 100
        
        this.Y = this.A.modPow(this.X, this.P);
    }

    public byte[] getP()
    {
        return this.P.toByteArray();
    }

    public byte[] getA()
    {
        return this.A.toByteArray();
    }

    public byte[] getX()
    {
        return this.X.toByteArray();
    }

    public byte[] getY()
    {
        return this.Y.toByteArray();
    }

    public byte[] encrypt(byte[] msg, byte[] publicKey)
    {
        byte[] res;
        int needBytes = 0;
        BigInteger Y2 = new BigInteger(publicKey);
        int maxLength = this.P.toString().length() - 3;
        BigInteger k;
        int n, i;

        //Разбить сообщение на большие числа меньше, чем P
        System.out.println(new BigInteger(msg) + "\n");
        String[] buff = (new BigInteger(msg)).toString().split("(?<=\\G.{" + maxLength + "})");
        ArrayList<BigInteger> m = new ArrayList<BigInteger>();
        for(i = 0; i < buff.length; i++)
        {
            m.add(new BigInteger(buff[i]));
            System.out.println("m: " + new BigInteger(buff[i]) );
        }
        //System.out.println("\n\n");
        // Найти k
        //PrimeNum primeGen = new PrimeNum(1000);
        do
        {
            if(maxLength > 100)
                k = PrimeNum.rndBigInteger(PrimeNum.getZahl(80), this.P.subtract(BigInteger.ONE));
            else
                k = PrimeNum.rndBigInteger(BigInteger.valueOf(2), this.P.subtract(BigInteger.ONE));
        }while( k.gcd( this.P.subtract(BigInteger.ONE) ).compareTo(BigInteger.ONE) != 0 );
        // Сделать m -> (r, e)
        BigInteger r;
        BigInteger e;
        byte[] eByte;
        byte[] rByte;
        int index;
        n = m.size();
        ArrayList<byte[]> msgNew = new ArrayList<byte[]>();
        for(i = 0; i < n; i++)
        {
            r = this.A.modPow(k, P);
            e = (m.get(i)).multiply(Y2.modPow(k, this.P)).mod(P);
            //System.out.println("r: " + r + "\ne: " + e);
            eByte = e.toByteArray();
            rByte = r.toByteArray();
            index = Integer.BYTES + rByte.length;
            //index Long -> byte[]
            byte[] indexByte = ByteBuffer.allocate(Integer.BYTES).putInt(index).array();
            //Сцепить все байт-массивы
            byte[] allByteArray = new byte[indexByte.length + rByte.length + eByte.length];
            ByteBuffer byteBuffer = ByteBuffer.wrap(allByteArray);
            byteBuffer.put(indexByte);
            byteBuffer.put(rByte);
            byteBuffer.put(eByte);
            // Засунуть в сообщения
            byte[] union3 = byteBuffer.array();
            needBytes += union3.length;
            msgNew.add(union3);
        }

        // Теперь msg1, msg2, ... -> index1 + msg1 + index2 + msg2 + ... 
        n = msgNew.size();
        res = new byte[n * Integer.BYTES + needBytes];
        ByteBuffer resByteBuffer = ByteBuffer.wrap(res);
        for(i = 0; i < n; i++)
        {
            byte[] msgByte = msgNew.get(i);
            index = Integer.BYTES + msgByte.length;
            byte[] indexByte = ByteBuffer.allocate(Integer.BYTES).putInt(index).array();

            resByteBuffer.put(indexByte);
            resByteBuffer.put(msgByte);
        }
        res = resByteBuffer.array();
        return res;
    }

    public byte[] decrypt(byte[] msg)
    {
        byte[] res;
        int i, j, n;
        //ArrayList<BigInteger> m = new ArrayList<BigInteger>();
        ArrayList<byte[]> mByte = new ArrayList<byte[]>();
        
        
        j = 0;
        int next = 0;
        do
        {
            byte[] buffByte = new byte[Integer.BYTES];
            for(i = 0; j < next + Integer.BYTES; j++, i++)
                buffByte[i] = msg[j];
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES).put(buffByte);
            buffer.flip();
            next += buffer.getInt();
            
            buffByte = new byte[next-j];
            for(i = 0; j < next; j++, i++)
                buffByte[i] = msg[j];
            mByte.add(buffByte);
        }while(next != msg.length);

        BigInteger r;
        BigInteger e;
        BigInteger m;
        StringBuilder resS = new StringBuilder();
        n = mByte.size();
        ArrayList<byte[]> Debug = new ArrayList<byte[]>();
        for(int li = 0; li < n; li++)
        {
            byte[] arrayM = mByte.get(li);
            byte[] buffByte = new byte[Integer.BYTES];
            for(i = 0; i < buffByte.length; i++)
                buffByte[i] = arrayM[i];
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES).put(buffByte);
            buffer.flip();
            next = buffer.getInt();
            
            buffByte = new byte[next - Integer.BYTES];
            for(j = 0, i = Integer.BYTES; i < next; i++, j++)
                buffByte[j] = arrayM[i];
            r = new BigInteger(buffByte);
            
            buffByte = new byte[arrayM.length - next];
            for(j = 0, i = next; i < arrayM.length; i++, j++)
                buffByte[j] = arrayM[i];
            e = new BigInteger(buffByte);
            m = e.multiply(  r.modPow(  this.P.subtract(BigInteger.ONE).subtract(this.X), this.P  )   ).mod(this.P);
            //System.out.println("r: " + r + "\ne: " + e);
            Debug.add(m.toByteArray());
            
            resS.append(m.toString());
        }
        for(byte[] buff : Debug)
            System.out.println("m: " + (new BigInteger(buff)) + "\n");
        System.out.println( resS.toString() );
        res = (new BigInteger(resS.toString())).toByteArray();
        return res;
    }

    private static byte[] reverse(byte[] array) 
    {
        for (int i = 0; i < array.length / 2; i++) 
        {
            byte temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
        return array;
    }
}