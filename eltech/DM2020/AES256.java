import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.io.ByteArrayOutputStream;

/*
 * JOPA! B rotEb*lETO!
 * HADO EwE DO6ABuTb COJlb!
 * А ты скопировал вопрос или ответ?!
 */

/**
 * Штука, которая умеет шифровать с помощью AES 256 бит
 */
public class AES256
{
	/**
	 * Тут хранится сам ключ шифрования
	 */
	private SecretKey secretKey;

	/**
	 * Нужно для параметра mode метода makeAES256. Показывает, что надо шифровать
	 */
	public static final int ifENCRYPT = Cipher.ENCRYPT_MODE;
	/**
	 * Нужно для параметра mode метода makeAES256. Показывает, что надо дешифровать
	 */
	public static final int ifDECRYPT = Cipher.DECRYPT_MODE;

	public static void main(String[] args) 
	{
		AES256 aes256 = new AES256();
		//String mes = "Hello";
		String mes = "Hello HuKuTA. BAw KJlol4 = 6548421365892736145983564983216549872639875463298175462139785463289714659213548712649872136589213684596321854761282281337";
		for (int i = 0; i < 3; i++) 
		{
			byte[] shifr = aes256.makeAES256(mes.getBytes(), AES256.ifENCRYPT);
			System.out.println(new String(shifr));
			byte[] src = aes256.makeAES256(shifr, AES256.ifDECRYPT);
			System.out.println(new String(src));
		}
		System.out.println("============");
		for (int i = 0; i < 3; i++) 
		{
			byte[] shifr = aes256.makeAES256_withSalt(mes.getBytes(), AES256.ifENCRYPT);
			System.out.println(new String(shifr));
			byte[] src = aes256.makeAES256_withSalt(shifr, AES256.ifDECRYPT);
			System.out.println(new String(src));
		}
	}

	/**
	 * Конструктор, который генерирует ключ secretKey самостоятельно
	 */
	public AES256() 
	{
		try 
		{
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            this.secretKey = keyGenerator.generateKey();
		}
		catch (NoSuchAlgorithmException e)
		{
            e.printStackTrace();
        }
	}
	/**
	 * Конструктор, который использует в качестве secretKey уже готовый ключ
	 * 
	 * @param key
	 */
	public AES256(byte[] key) 
	{
		try 
		{
			this.secretKey = new SecretKeySpec(key, 0, key.length, "AES");
		} 
		catch (Exception e)
		{
			e.printStackTrace();
        }
	}
	/**
	 * Метод, который возвращает ключ secretKey
	 * 
	 * @return secretKey
	 */
	public byte[] getKey()
	{
		return this.secretKey.getEncoded();
	}

	/**
	 * Генерирует рандомный ключ для шифрования AES 256 бит
	 * @return key 256 бит
	 */
	public static byte[] getRndKey256()
	{
		//ByteArrayOutputStream outStream;
		KeyGenerator keyGenerator;
		byte[] key256 = null;
		try 
		{
			//outStream = new ByteArrayOutputStream();
			keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(256);
			//outStream.write(keyGenerator.generateKey().getEncoded());
			//key256 = outStream.toByteArray();
			key256 = keyGenerator.generateKey().getEncoded();
		} 
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return key256;
	}

	/**
	 * Вот этот метод и шифрует или дешифрует rawMessage в зависимости от cipherMode. Лучше использовать makeAES256_withSalt
	 * 
	 * @param rawMessage - сообщение, которое надо зашифровать или расшифровать, представленное в байтах
	 * @param Mode - если AES256.ifENCRYPT (или Cipher.ENCRYPT_MODE==1), то шифрует, если AES256.ifDECRYPT (или Cipher.DECRYPT_MODE==2), то дешифрует
	 * @return зашифрованное или расшифрованное сообщение, представленное в байтах
	 */
	public byte[] makeAES256(byte[] rawMessage, int mode)
	{
		try
		{
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init( mode, this.secretKey );
			byte [] output = cipher.doFinal(rawMessage);
			
			/*byte[] encodedKey = Base64.decode(stringKey);
			byte[] buffBytes = this.secretKey.getEncoded();
			SecretKey buffKey = new SecretKeySpec(buffBytes, 0, buffBytes.length, "AES");
            cipher.init( cipherMode, this.secretKey);
			byte [] buffOutput = cipher.doFinal(rawMessage);
			System.out.println("buff: " + new String(buffOutput) );
			*/
            return output;
		} 
		catch (Exception e)
		{
            e.printStackTrace();
            return null;
        }
	}

	/**
	 * Вот этот метод и шифрует или дешифрует rawMessage в зависимости от cipherMode + ещё используется соль
	 * 
	 * @param rawMessage - сообщение, которое надо зашифровать или расшифровать, представленное в байтах
	 * @param Mode - если AES256.ifENCRYPT (или Cipher.ENCRYPT_MODE==1), то шифрует, если AES256.ifDECRYPT (или Cipher.DECRYPT_MODE==2), то дешифрует
	 * @return зашифрованное или расшифрованное сообщение, представленное в байтах
	 */
	public byte[] makeAES256_withSalt(byte[] rawMessage, int mode) throws IllegalArgumentException
	{
		int i, j, k;
		byte[] output;
		byte[] msg;
		byte[] buff;
		Cipher cipher;
		SecureRandom secRND = new SecureRandom();
		if(rawMessage == null || rawMessage.length == 0)
			throw new IllegalArgumentException("rawMessage must be init and len > 0\n");
		try
		{
            cipher = Cipher.getInstance("AES");
			cipher.init( mode, this.secretKey );
			if(mode == AES256.ifENCRYPT)
			{
				msg = new byte[ rawMessage.length + rawMessage.length + 1 ]; // S 0 S 0 S 0 S 0 S 0 S 0 S 0 S 0 S, где S - соль, а 0 - исходные байты
				buff = new byte[rawMessage.length + 1];
				secRND.nextBytes(buff);

				for(i = 0, j = 0, k = 0; i < msg.length; i++)
				{
					if(i % 2 == 0)
					{
						msg[i] = buff[j];
						j++;
					}
					else
					{
						msg[i] = rawMessage[k];
						k++;
					}
				}
				output = cipher.doFinal(msg);
			}
			else if (mode == AES256.ifDECRYPT)
			{
				output = cipher.doFinal(rawMessage); // S 0 S 0 S 0 S 0 S 0 S 0 S 0 S 0 S, где S - соль, а 0 - исходные байты
				buff = new byte[(rawMessage.length - 1)/2];

				for(i = 0, j = 0; i < output.length; i++)
					if(i % 2 == 1)
					{
						buff[j] = output[i];
						j++;
					}
				output = buff;
			}
			else
			{
				System.out.println("There is no a such mode: " + mode);
				output = null;
			}
            return output;
		} 
		catch (Exception e)
		{
            e.printStackTrace();
            return null;
        }
	}
	
	/**
	 * Дополняет строку пробелами так, чтобы ещё длина в битах была кратна 128 битам
	 * 
	 * @param text - строка, которая преобразуется
	 * @return дополненную строку пробелами
	 */
	public static String fillBlock128(String text) 
	{
        int spaceNum = text.getBytes().length%16==0?0:16-text.getBytes().length%16;
        for (int i = 0; i<spaceNum; i++) text += " ";
        return text;
	}

	/**
	 * Метод, который переводит байты в строку, чтобы можно было хоть как-то вывести массив байт
	 * 
	 * @param Arr - массив байт
	 * @return строка, которая представляет массив байтов
	 */
	public static String ByteArrToStr(byte[] Arr)
	{
		int i;
		StringBuilder builder = new StringBuilder();
		for (i = 0; i < Arr.length; i++)
			builder.append( Byte.valueOf( Arr[i] ).toString() + ( i < Arr.length-1 ?"_":"" ) );
		return builder.toString();
	}

	/**
	 * Метод, который переводит строку в байты; делает обратное действие метода ByteArrToStr
	 * 
	 * @param S - строка, которая будет представлена в виде массива байтов
	 * @return массив байтов, который представляет строку S
	 */
	public static byte[] StrToByteArr(String S)
	{
		int i;
		String[] splitted = S.split("_");
		byte[] res = new byte[ splitted.length ];
		for (i = 0; i < res.length; i++)
			res[i] = Byte.valueOf(splitted[i]);
		return res;
	}
}