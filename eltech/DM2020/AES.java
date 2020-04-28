import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
/**
 * JOPA! B rotEb*lETO!
 * HADO EwE DO6ABuTb COJlb!
 * А ты скопировал вопрос или ответ?!
 */
public class AES 
{
	private SecretKey secretKey;

	public static void main(String[] args) 
	{
		AES aes256 = new AES();
		String mes = "Hello";
		for (int i = 0; i < 3; i++) 
		{
			byte[] shifr = aes256.makeAes(mes.getBytes(), Cipher.ENCRYPT_MODE);
			System.out.println(new String(shifr));
			byte[] src = aes256.makeAes(shifr, Cipher.DECRYPT_MODE);
			System.out.println(new String(src));
		}
	}

	public AES() 
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
	public byte[] makeAes(byte[] rawMessage, int cipherMode)
	{
		try 
		{
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init( cipherMode, this.secretKey);
			byte [] output = cipher.doFinal(rawMessage);
			
			//byte[] encodedKey = Base64.decode(stringKey);
			byte[] buffBytes = this.secretKey.getEncoded();
			SecretKey buffKey = new SecretKeySpec(buffBytes, 0, buffBytes.length, "AES");
            cipher.init( cipherMode, this.secretKey);
			byte [] buffOutput = cipher.doFinal(rawMessage);
			System.out.println("buff: " + new String(buffOutput) );
			//===============
            return output;
		} catch (Exception e)
		{
            e.printStackTrace();
            return null;
        }
	}
	
    private static String fillBlock(String text) {
        int spaceNum = text.getBytes().length%16==0?0:16-text.getBytes().length%16;
        for (int i = 0; i<spaceNum; i++) text += " ";
        return text;
    }
}