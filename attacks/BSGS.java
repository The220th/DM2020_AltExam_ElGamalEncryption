/**
 * Алгоритм Baby step giant step для решения h = g^x(mod p)
 */

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class BSGS {

    public static void main(String[] args) throws IOException {

        File file = new File("data.txt");
        Scanner sc = new Scanner(file);

        ArrayList<BigInteger> data = new ArrayList<>();
        while (sc.hasNextBigInteger()) {
            try {
                BigInteger bigint = sc.nextBigInteger();
                data.add(bigint);
            } catch (NumberFormatException ex) {
                System.out.println(ex.toString());
            }
        }

        BigInteger h = data.get(0);
        BigInteger g = data.get(1);
        BigInteger p = data.get(2);

        Long startTime = System.nanoTime(); // для замера скорости

        /* Вычисляем m */
        BigInteger[] mp = p.sqrtAndRemainder();
        BigInteger m = (mp[1].intValue() >= 0)? mp[0].add(BigInteger.ONE) : mp[0];
        BigInteger q = BigInteger.ZERO, r = BigInteger.ZERO;

        HashMap<BigInteger, BigInteger> gs = new HashMap<>();

        /* Вычисляем g^0, g^1 ... g^(m-1) mod p */
        for (BigInteger i = BigInteger.ZERO; i.compareTo(m) == -1; i = i.add(BigInteger.ONE))
        {
            BigInteger key = SquareAndMultiply(g, i, p);
            gs.put(key, i);
        }

        /* Вычисляем g^-m mod p */
        BigInteger gm = SquareAndMultiply(g.modInverse(p), m, p);

        /* Вычисляем h(g^-m)^0, h(g^-m)^1 ... h(g^-m)^q где q = 0,1,2 ... */
        for (BigInteger j = BigInteger.ZERO; j.compareTo(m) == -1; j = j.add(BigInteger.ONE)) {
            BigInteger hi = gm.pow(j.intValue());
            BigInteger hj = h.multiply(hi);
            BigInteger hs = hj.mod(p);

            BigInteger collision = gs.get(hs);
            if ( collision != null) {
                r = collision;
                q = j;
                break;
            }
        }

        Long stopTime = System.nanoTime();
        System.out.println(m.multiply(q).add(r));

        System.out.println("It took " + ((stopTime - startTime) / 1000000000.0) + " seconds.");
    }
    public static BigInteger SquareAndMultiply(BigInteger base, BigInteger exponent, BigInteger mod) {
        if (exponent.compareTo(BigInteger.ZERO) == 0) {
            return BigInteger.ONE;
        }
        if (exponent.compareTo(BigInteger.ONE) <= 0) {
            return base;
        }

        BigInteger ret;

        String bits = exponent.toString(2);
        ret = base.mod(mod);

        for (int i = 1; i < bits.length(); ++i)
        {
            ret = ret.multiply(ret).mod(mod);
            if (bits.charAt(i) == '1')
                ret = ret.multiply(base).mod(mod);
        }

        return ret;
    }
}
