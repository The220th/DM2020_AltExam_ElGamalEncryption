package eltech.DM2020;

import java.math.BigInteger;
import eltech.DM2020.PrimeNum;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException;


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

    /**
     * Конструктор, который требует, чтобы было готовы и P, и A, и X, и Y
     */
    public ElGamalCipher(byte[] P, byte[] A, byte[] X, byte[] Y)
    {
        this.P = new BigInteger(P);
        this.A = new BigInteger(A);
        this.X = new BigInteger(X);
        this.Y = new BigInteger(Y);
    }
    /**
     * Конструктор, которому нужны только A и P, остальное он сгенерирует всё сам. Ключи пожно будет получить с помощью getX() и getY()
     */
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

    /**
     * Конструктор, который всё генерирует сам: и P, и A, и X, и Y. Если использозвать его, то инициализироваться Cipher будет долго, так как на генерацию P и A уходит много времени
     * Генерирует P таким образом, чтобы P=2*q+1, где q - тоже просто число.
     * Считается, что если у P, будет около 300 цифр, то это более чем достаточно. Примерно именно столько будет цифр у P.
     */
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

    /**
     * Метод позволяет получить P
     */
    public byte[] getP()
    {
        return this.P.toByteArray();
    }


    /**
     * Метод позволяет получить A
     */
    public byte[] getA()
    {
        return this.A.toByteArray();
    }

    /**
     * Метод позволяет получить X  - закрытый (секретный) ключ. Никому его не сообщайте)
     */
    public byte[] getX()
    {
        return this.X.toByteArray();
    }

    /**
     * Метод позволяет получить Y - открытый ключ в виде строки, которая представляет число
     */
    public String sGetY()
    {
        return this.Y.toString();
    }


    /**
     * Метод позволяет получить Y - открытый ключ 
     */
    public byte[] getY()
    {
        return this.Y.toByteArray();
    }

    /**
     * Шифрует сообщение в виде строки.
     * 
     * @param msg - сообщение в виде строки
     * @param publicKey - публичный ключ в виде строки
     * @return какую-то кашу в виде строки
     */
    public String sEncrypt(String msg, String publicKey)
    {
        return new String( encrypt( msg.getBytes(), new BigInteger(publicKey).toByteArray() ) );
    }

    /**
     * Дешифрует сообщение в виде строки
     * 
     * @param msg - зашифрованная каша-сообщение в виде строки
     * @return Нормальное сообщение
     */
    public String sDecrypt(String msg)
    {
        return new String( decrypt( msg.getBytes() ) );
    }

    /**
     * Позволяет зашифровать сообщение
     * 
     * @param msg - само сообщение
     * @param publicKey - Публичный ключ того, кому это сообщение будет посылаться
     * @return защифрованное сообщение
     */
    public byte[] encrypt(byte[] msg, byte[] publicKey)
    {
        byte[] res;
        int needBytes = 0;
        BigInteger Y2 = new BigInteger(publicKey);
        int maxLength = this.P.toString().length() - 3;
        BigInteger k;
        int n, i;

        //System.out.println("msg: " + new String(msg));
        //System.out.println("P: " + P);
        //System.out.println("A: " + A);
        //System.out.println("X: " + X);

        //Разбить сообщение на большие числа меньше, чем P
        //System.out.println(new BigInteger(msg) + "\n");//!!!Debug 
        //String[] buff = (new BigInteger(msg)).toString().split("(?<=\\G.{" + maxLength + "})");
		String[] buff = parseCut( (new BigInteger(msg)).toString(), maxLength);
		//for (i = 0; i < buff.length; i++)//!!!Debug 
			//System.out.println(i + ": " + buff[i] + ".");//!!!Debug 
        ArrayList<BigInteger> m = new ArrayList<BigInteger>();
        for(i = 0; i < buff.length; i++)
        {
			if( !buff[i].equals("") ) // Николай блин, почему из parseCut() последний элемент "" ?!?!?!
			{
				m.add(new BigInteger(buff[i]));
				//System.out.println("m: " + new BigInteger(buff[i]) );//!!!Debug 
			}
        }
        //System.out.println("\n\n");
        // Найти k
        //PrimeNum primeGen = new PrimeNum(1000);
        do
        {
            k = PrimeNum.rndBigInteger(this.P.divide(BigInteger.valueOf(2)), this.P.subtract(BigInteger.ONE));
            //if(maxLength > 100)
                //k = PrimeNum.rndBigInteger(PrimeNum.getZahl(80), this.P.subtract(BigInteger.ONE));
            //else
                //k = PrimeNum.rndBigInteger(BigInteger.valueOf(2), this.P.subtract(BigInteger.ONE));
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

        //System.out.println("DeMsg: " + new String( decrypt(res, (new BigInteger("920256062478")).toByteArray() )   ));
        //System.out.println("EnMsg (res): " + AES256.ByteArrToStr(res) );
        //System.out.println("EnMsg (res): " + AES256.ByteArrToStr(res) );
        //System.out.println("EnMsg (1): " + AES256.ByteArrToStr((new BigInteger(1, res)).toByteArray() ));
        //System.out.println("EnMsg (2): " + AES256.ByteArrToStr( (new BigInteger(1,  (new BigInteger(1, res)).toByteArray())).toByteArray()  ));
        //System.out.println("DeMsg: " + new String( decrypt( (new BigInteger((new BigInteger(res)).toString())).toByteArray()  , (new BigInteger("920256062478")).toByteArray() )   ) );

        return res;
    }

    public byte[] decrypt(byte[] msg, byte[] keyX)
    {
        byte[] res;
        int i, j, n;
        //ArrayList<BigInteger> m = new ArrayList<BigInteger>();
        ArrayList<byte[]> mByte = new ArrayList<byte[]>();
        BigInteger BigX = new BigInteger(keyX); // Типа закрытый ключ
        
        
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
        //ArrayList<byte[]> Debug = new ArrayList<byte[]>();
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
            m = e.multiply(  r.modPow(  this.P.subtract(BigInteger.ONE).subtract(BigX), this.P  )   ).mod(this.P);
            //System.out.println("r: " + r + "\ne: " + e);
            //Debug.add(m.toByteArray());
            
            resS.append(m.toString());
        }
        //for(byte[] buff : Debug)//!!!Debug 
            //System.out.println("m: " + (new BigInteger(buff)) + "\n");//!!!Debug 
        //System.out.println( resS.toString() );//!!!Debug
        res = (new BigInteger(resS.toString())).toByteArray();
        return res;
    }

    /**
     * Дешифрует сообщение при условии, что оно было зашифровано с помощью вашего публичного ключа
     * @param msg - зашифрованное сообщение
     * @return расшифрованное сообщение
     */
    public byte[] decrypt(byte[] msg)
    {
        return decrypt(msg, X.toByteArray());
        /*byte[] res;
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
        return res;*/
    }

	/**
	* Нужна для функции encrypt. Разбивает большое число, записанное в виде строки на числа, у которых не более N цифр, так, чтобы они не начинались с нуля/
	*
	* @param num - само число
	* @param N - максимальное кол-во цифр после разбиения
	* @return массив разбиений
	*/
    private static String[] parseCut(String num, int N)
    {
        ArrayList<String> res = new ArrayList<String>();
        String regex = "\\d{0,"+Integer.toString(N)+"}(?!0)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(num);
        while (matcher.find())
            res.add(matcher.group());

        return res.toArray(new String[res.size()]);
    }

    /**
     * Разворачивает массив байт. Изменяет параметр!!!
     */
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

    /**
     * Подписывает сообщение, чтобы можно было точно сказать, что оно отправлено вами
     * 
     * @param msg - сообщение, которое подписывается
     * @return подписанное сообщение
     */
    public byte[] signMessage(byte[] msg)
    {
        byte[] res;
        BigInteger k;
        byte[] hashMsg = getSHA256(msg);
        BigInteger m = new BigInteger(hashMsg);
        BigInteger r;
        BigInteger e;

        // Найти k
        do
        {
            k = PrimeNum.rndBigInteger(this.P.divide(BigInteger.valueOf(2)), this.P.subtract(BigInteger.ONE));
        }while( k.gcd( this.P.subtract(BigInteger.ONE) ).compareTo(BigInteger.ONE) != 0 );

        // Найти r и e
        r = this.A.modPow(k, P);
        e = m.subtract( this.X.multiply(r) ).multiply( k.modInverse(P.subtract(BigInteger.ONE)) ).mod( P.subtract(BigInteger.ONE) );
        
        // Сделать так: r, e, msg -> [index1 to e][index2 to msg][r bytes][e bytes][msg bytes]
        byte[] eByte = e.toByteArray();
        byte[] rByte = r.toByteArray();
        int index1 = Integer.BYTES + Integer.BYTES + rByte.length;
        int index2 = index1 + eByte.length;

        byte[] index1Byte = ByteBuffer.allocate(Integer.BYTES).putInt(index1).array();
        byte[] index2Byte = ByteBuffer.allocate(Integer.BYTES).putInt(index2).array();
        byte[] allByteArray = new byte[index1Byte.length + index2Byte.length + rByte.length + eByte.length + msg.length];
        ByteBuffer byteBuffer = ByteBuffer.wrap(allByteArray);
        byteBuffer.put(index1Byte);
        byteBuffer.put(index2Byte);
        byteBuffer.put(rByte);
        byteBuffer.put(eByte);
        byteBuffer.put(msg);
        res = byteBuffer.array();
        return res;
    }

    /**
     * Подписывает чужой публичный ключ. Также можно вставить сообщение об этом ключе.
     * 
     * @param key - ключ, который вы подписываете
     * @param msg - сообщение, которое прикрипится к этому ключу. Например, если msg = "It is Mike`s key. I trust Mike. He can sign other keys", то после проверки этого ключа будет: "It is Mike`s Key. I trust Mike. He can sign other keys;;;[его публичный ключ]"
     * @return Подписанный ключ
     */
    public byte[] signKey(byte[] key, byte[] msg)
    {
        String S = ( new String(msg) ) + ";;;" + (new BigInteger(key)).toString();
        return signMessage(S.getBytes());
    }

    /**
     * Смотрит подписанное кем-нибудь сообщение
     * 
     * @param msg - подписанное кем-то сообщение
     * @param publicKey - публичный ключ, того, кто подписал сообщение msg
     * @return исходное сообщение, которое подписал этот кто-то
     */
    public byte[] verifyMessage(byte[] msg, byte[] publicKey)
    {
        byte[] res;
        int i, j;
        BigInteger BigY = new BigInteger(publicKey); // Типа открытый ключ
        
        BigInteger r;
        BigInteger e;
        
        int index1;
        int index2;
        byte[] rByte;
        byte[] eByte;
        byte[] msgByte;
        
        // Получить index1
        byte[] buffByte = new byte[Integer.BYTES];
        for(i = 0, j = 0; i < Integer.BYTES; i++, j++)
            buffByte[i] = msg[j];
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES).put(buffByte);
        buffer.flip();
        index1 = buffer.getInt();

        // Получить index2
        for(i = 0; i < Integer.BYTES; i++, j++)
            buffByte[i] = msg[j];
        buffer = ByteBuffer.allocate(Integer.BYTES).put(buffByte);
        buffer.flip();
        index2 = buffer.getInt();

        // Получить r
        rByte = new byte[index1 - j];
        for(i = 0; j < index1; i++, j++)
            rByte[i] = msg[j];
        r = new BigInteger(rByte);

        // Получить e
        eByte = new byte[index2 - j];
        for(i = 0; j < index2; i++, j++)
            eByte[i] = msg[j];
        e = new BigInteger(eByte);

        // Получить msgByte
        msgByte = new byte[msg.length - j];
        for(i = 0; j < msg.length; i++, j++)
            msgByte[i] = msg[j];
        
        byte[] hashMsg = getSHA256(msgByte);

        BigInteger left = BigY.modPow(r, this.P).multiply( r.modPow(e, this.P) ).mod(this.P);
        BigInteger right = this.A.modPow(new BigInteger(hashMsg), this.P ).mod(this.P);
        if(left.compareTo(right) == 0)
            res = msgByte;
        else
            res = "There is problem with verify msg. MB it is fake".getBytes();
        return res;
    }

    /**
     * Смотрит подписанное кем-нибудь сообщение. Если подпись окажется подделайнной, то вернёт соответствующее сообщение.
     * 
     * @param msg - подписанное кем-то сообщение
     * @param publicKey - публичный ключ того, кто подписывал это сообщение
     * @param P1 - поле вычитов P того, кто подписывал сообщение msg
     * @param A1 - параметр A того, кто подписывал сообщение msg
     * @return если проверка пройдена успешно, то вернёт исходное сообщение, иначе строку, где говорится что что-то не так с подписью.
     */
    public static byte[] verifyMessage(byte[] msg, byte[] publicKey, byte[] P1, byte[] A1)
    {
        byte[] res;
        int i, j;
        BigInteger BigY = new BigInteger(publicKey); // Типа открытый ключ
        BigInteger BigP = new BigInteger(P1);
        BigInteger BigA = new BigInteger(A1);
        
        BigInteger r;
        BigInteger e;
        
        int index1;
        int index2;
        byte[] rByte;
        byte[] eByte;
        byte[] msgByte;
        
        // Получить index1
        byte[] buffByte = new byte[Integer.BYTES];
        for(i = 0, j = 0; i < Integer.BYTES; i++, j++)
            buffByte[i] = msg[j];
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES).put(buffByte);
        buffer.flip();
        index1 = buffer.getInt();

        // Получить index2
        for(i = 0; i < Integer.BYTES; i++, j++)
            buffByte[i] = msg[j];
        buffer = ByteBuffer.allocate(Integer.BYTES).put(buffByte);
        buffer.flip();
        index2 = buffer.getInt();

        // Получить r
        rByte = new byte[index1 - j];
        for(i = 0; j < index1; i++, j++)
            rByte[i] = msg[j];
        r = new BigInteger(rByte);

        // Получить e
        eByte = new byte[index2 - j];
        for(i = 0; j < index2; i++, j++)
            eByte[i] = msg[j];
        e = new BigInteger(eByte);

        // Получить msgByte
        msgByte = new byte[msg.length - j];
        for(i = 0; j < msg.length; i++, j++)
            msgByte[i] = msg[j];
        
        byte[] hashMsg = getSHA256(msgByte);

        BigInteger left = BigY.modPow(r, BigP).multiply( r.modPow(e, BigP) ).mod(BigP);
        BigInteger right = BigA.modPow(new BigInteger(hashMsg), BigP ).mod(BigP);
        if(left.compareTo(right) == 0)
            res = msgByte;
        else
            res = "There is problem with verify msg. MB it is fake".getBytes();
        return res;
    }

    /**
     * Позволяет проверть подписанный ключ
     * 
     * @param SignedKey - подписанный ключ
     * @param KeeperPublicKey - публичный ключ того, кто подписывал
     * @param KeeperP - поле вычитов P того, кто подписывал
     * @param KeeperA - параметр A того, кто подписывал
     * @return res[0] - ключ (или 123, если что-то не так), res[1] - описание этого ключа (или "There is no key", если что-то пошло не так)
     */
    public static byte[][] verifyKey(byte[] SignedKey, byte[] KeeperPublicKey, byte[] KeeperP, byte[] KeeperA)
    {
        byte[][] res = new byte[2][];
        String[] S = new String( verifyMessage(SignedKey, KeeperPublicKey, KeeperP, KeeperA) ).split(";;;");
        

        if(S.length == 2)
        {
            res[0] = S[S.length-1].getBytes();
            res[1] = S[0].getBytes();
        }
        else
        {
            res[0] = "123".getBytes();
            res[1] = "There is no key".getBytes();
        }
        return res;
    }
    
    /**
     * Позволяет проверть подписанный ключ
     * 
     * @param SignedKey - подписанный ключ
     * @param KeeperPublicKey - публичный ключ того, кто подписывал
     * @return res[0] - ключ (или 123, если что-то не так), res[1] - описание этого ключа (или "There is no key", если что-то пошло не так)
     */
    public byte[][] vefifyKey(byte[] SignedKey, byte[] KeeperPublicKey)
    {
        byte[][] res = new byte[2][];
        String[] S = new String( verifyMessage(SignedKey, KeeperPublicKey) ).split(";;;");
        

        if(S.length == 2)
        {
            res[0] = S[1].getBytes();
            res[1] = S[0].getBytes();
        }
        else
        {
            res[0] = "123".getBytes();
            res[1] = "There is no key".getBytes();
        }
        return res;
    }

    /**
     * Метод, который вычисляет Хеш-функцию SHA-256 от msg
     * 
     * @param msg - сообщение, от которого вычисляется Хеш-функция
     * @return Хеш-функцию от msg
     */
    public static byte[] getSHA256(byte[] msg)
    {  
        MessageDigest md = null;
        byte[] res = null;
        try
        {
            md = MessageDigest.getInstance("SHA-256");
            res = md.digest(msg);
            
        }
        catch(Exception e)
        {
            System.out.println("WOK in getSHA256\n ");
            e.printStackTrace();
        }
        return res;
    }
    
    
}