import eltech.DM2020.GenerateNum.*;
import java.lang.*;
import java.util.*;
import java.math.*;

public class Main
{
	public static void main(String[] args)
	{
		Scanner in = new Scanner(System.in);
		
		System.out.println("How ~digits?");
		long c = in.nextLong();
		
		PrimeNum rndPrime = new PrimeNum(10000);
		BigInteger a = rndPrime.rndSophieGermainNum(c, 5);
		BigInteger second = a.multiply(BigInteger.valueOf(2)).add(BigInteger.ONE);
		System.out.println(a + " isPrime " + rndPrime.millerRabinTest(a));
		System.out.println(second + " isPrime " + rndPrime.millerRabinTest(second));
		System.out.println(a + " \nand \n" + second);
	}
}