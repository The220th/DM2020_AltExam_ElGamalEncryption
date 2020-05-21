import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import eltech.DM2020.*;

public class Test
{
	private static byte[] P = (new BigInteger("3059948265576697055654798349610816034604773243403863023150578718877729532684562987694453313878251451254283494498789726949726198487680738503410570538673840061074838237828562097741482642255867011465702352542767321887346917300678841123047258048923224244673142929764836199357635920344973852083877916404882114363785252975772502420212752565228142833749030552920866210733085036517974323136561548219958510339609615489690427986347201928110093355890261675208936231441192928362396921036037853750461824689953347443291989148450689440788626055682908121792437405760536324095016905767528384892425057751691127821608654060158624587234899229172972274894920727708428165610055056408921258926727374045455384517073828765227636867520573846350284270575312946488455672201969665194810217940596952487848914745672526688928094556035410117561527975627024144208162641454386784233929329243195346943911147852945575080772276881872266439305682037625291066048242838382853843982270242783995859946569757766242751613482773268355037590205809822230750516349041685444410989408139329748915517013914990825705636436399786144094950925447925212112913022891056376867425777853438325033086050666935160287355750561140897366979311788041683137796754312137382350856750444545769202049604256396500340448694840579500520465791049921195865076726687623329629786518395308080080322177838202242538506345732428718372839422202373657610730895830129848104390660485337116024743984720512029311510211609053290618334471178104289213538234025824478464573862444626679042328656261189700741711101167543670083867325779491907083703414325126357915173028377555675266530232252522717146330663921108479216380359720353278656264430380900243576985325092196172062092904887449402833588758116896920355720482387995700920644643770845128603121496344388833583766522101785062937368542666961702932321151817447829473644209507902622210472382365750501543436846610857115572931254726875013533420080655500445258703038240370981580041788752092497377248866914091232110154678533524779293070778856463226681028683030546280365688672870439605364625095483621973987")).toByteArray();
	private static byte[] A = getAA(P);
	
	public static void main(String[] args)
	{
		ElGamalCipher test = new ElGamalCipher(P, A);
		ElGamalCipher test2 = new ElGamalCipher(test.getP(), test.getA());
		
		System.out.println("X1: " + new BigInteger(test.getX()));
		System.out.println("Y1: " + new BigInteger(test.getY()));
		System.out.println("A1: " + new BigInteger(test.getA()));
		System.out.println("P1: " + new BigInteger(test.getP()));
		
		System.out.println("X2: " + new BigInteger(test2.getX()));
		System.out.println("Y2: " + new BigInteger(test2.getY()));
		System.out.println("A2: " + new BigInteger(test2.getA()));
		System.out.println("P2: " + new BigInteger(test2.getP()) + "\n");
		
		String S = "ttesttesesttestttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest3212532125t3ttest32125esttestttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest3212532125t3ttest32125esttestttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest3212532125t3ttest32125esttestttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest3212532125t3ttest32125esttestttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest3212532125t3ttest32125esttestttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest3212532125t3ttest32125tttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest32125ttest3212532125t3ttest32125ttest321252125\n\n";
		
		byte[] enMsg = test.encrypt(S.getBytes(), test2.getY());
		byte[] signEnMsg = test.signMessage(enMsg);
		
		byte[] verSignMsg = test2.verifyMessage(signEnMsg, test.getY());
		byte[] deMsg = test2.decrypt(verSignMsg);
		System.out.println((new String(deMsg)));
		
		
		S = "I want to sign this";
		byte[] signedMsg =  test.signMessage(S.getBytes());
		String whoWrite = new String(test2.verifyMessage(signedMsg, test.getY()));
		
		System.out.println("\n" + whoWrite + "\n\n\n");
		
		byte[] sgnKey = test.signKey(test.getY(), "It is my (test) public key".getBytes());
		byte[][] verKey = test2.vefifyKey(sgnKey, test.getY());
		
		System.out.println("\n\nKey: " + new String(verKey[0]) + "\nDescriprion: " + new String(verKey[1]) );
		
		
		
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
	
	private static byte[] getAA(byte[] Pbyte)
	{
		BigInteger P = new BigInteger(Pbyte);
		BigInteger A;
		BigInteger q = P.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2));
		//System.out.println("\n\n" + q + "\n");
		
        do
        {
            A = PrimeNum.rndBigInteger(P.divide(BigInteger.valueOf(2)), P);
        }while( A.modPow(q, P).compareTo(BigInteger.ONE) == 0 );
		return A.toByteArray();
	}
}






























