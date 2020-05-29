package eltech.DM2020;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.security.SecureRandom;

/** 
 * Генерирует простые числа
*/
public class PrimeNum
{
	/**
	 * Кол-во раундов на проверку простоты
	 */
	private long k;

	private PrimeNum(){}

	/**
	 * @param k - кол-во раундов для проверки на простое
	 */
	public PrimeNum(long k)
	{
		this.k = k;
	}

	/**
	 * @param a - минимальная вероятность того, что число будет простым
	 */
	public PrimeNum(double a)
	{
		// Надо бы это ускорить)
		//k >= ln(1-a)/ln(4)
		//this.k = (long)(Math.log(1-a) / Math.log(4)) + 1;
		for(this.k = 1; 1 - Math.pow(0.25, this.k) < a; this.k++ );
	}

	/**
	 * Возвращает вероятность того, что число простое. Точнее число простое с вероятностью не меньше, чем [возвращаемое значение]
	 */
	public double getProbabilityPrime()
	{
		return Math.pow(0.25, k);
	}

	/**
	 * Возвращает количество раундов для проверки на простоту
	 */
	public long getAmountRounds()
	{
		return k;
	}
	
	/**
	 * Генерирует простое число p, у которого n цифр, с вероятностью его простоты не меньше чем getProbabilityPrime() такое, что p*2+1 тоже простое с вероятностью не меньше, чем getProbabilityPrime()
	 * Скорость генерации почти не зависит от числа k
	 * 
	 * @param n - кол-во цифр у генерируемого числа
	 * @return p
	 */
	public BigInteger rndSophieGermainNum(long n)
	{
		BigInteger res; // Это и есть p
		long minRounds = 30;
		do
		{
			res = getZahl(n);
		}while( !PrimeNum.millerRabinTest(res, minRounds) || !PrimeNum.millerRabinTest(res.multiply(BigInteger.valueOf(2)).add(BigInteger.ONE), minRounds));

		if( !PrimeNum.millerRabinTest(res, k) || !PrimeNum.millerRabinTest(res.multiply(BigInteger.valueOf(2)).add(BigInteger.ONE), k))
			res = rndSophieGermainNum(n);
		return res;
	}

	/**
	 * Генерирует простое число p, у которого (n ± [0; d]) цифр, с вероятностью его простоты не меньше чем getProbabilityPrime() такое, что p*2+1 тоже простое с вероятностью не меньше, чем getProbabilityPrime()
	 * Скорость генерации почти не зависит от числа раундов k
	 * Если d > n, то будет считаться, что n = d
	 * 
	 * @param n - кол-во цифр у генерируемого числа
	 * @return p
	 */
	public BigInteger rndSophieGermainNum(long n, long d)
	{
		BigInteger res; // Это и есть p
		Random r = new Random();
		long minRounds = 30;
		if(d > n) n = d;
		do
		{
			res = getZahl(n + (r.nextInt()%2==0?1:-1) * ( r.nextInt() % (d+1) ) );
		}while( !PrimeNum.millerRabinTest(res, minRounds) || !PrimeNum.millerRabinTest(res.multiply(BigInteger.valueOf(2)).add(BigInteger.ONE), minRounds));

		if( !PrimeNum.millerRabinTest(res, k) || !PrimeNum.millerRabinTest(res.multiply(BigInteger.valueOf(2)).add(BigInteger.ONE), k))
			res = rndSophieGermainNum(n, d);
		return res;
	}

	/**
	 * Генерирует простое число p, у которого n цифр, с вероятностью его простоты не меньше чем getProbabilityPrime()
	 * Скорость генерации почти не зависит от числа k
	 * 
	 * @param n - кол-во цифр у генерируемого числа
	 * @return p
	 */
	public BigInteger rndPrimeNum(long n)
	{
		BigInteger res; // Это и есть p
		long minRounds = 30;
		do
		{
			res = getZahl(n);
		}while( !PrimeNum.millerRabinTest(res, minRounds) );

		if( !PrimeNum.millerRabinTest(res, k) )
			res = rndPrimeNum(n);
		return res;
	}

	/**
	 * Генерирует простое число p, у которого (n ± [0; d]) цифр, с вероятностью его простоты не меньше чем getProbabilityPrime()
	 * Скорость генерации почти не зависит от числа раундов k
	 * Если d > n, то будет считаться, что n = d
	 * 
	 * @param n - кол-во цифр у генерируемого числа
	 * @return p
	 */
	public BigInteger rndPrimeNum(long n, long d)
	{
		BigInteger res; // Это и есть p
		Random r = new Random();
		long minRounds = 30;
		if(d > n) n = d;
		do
		{
			res = getZahl(n + (r.nextInt()%2==0?1:-1) * ( r.nextInt() % (d+1) ) );
		}while( !PrimeNum.millerRabinTest(res, minRounds) );

		if( !PrimeNum.millerRabinTest(res, k) )
			res = rndPrimeNum(n, d);
		return res;
	}

	/*
	 * Генерирует какое-то число на интервале [min; max). Это число не обязательно простое, скорее наоборот
	 * 
	 * @param min - минимальное число для генерации
	 * @param max - максимальное число для генерации
	 * @return сгенерированное какое-то число
	 /
	public static BigInteger rndBigInteger(BigInteger min, BigInteger max)
	{
		long nMin = min.toString().length();
		long nMax = max.toString().length() + 1;
		BigInteger buff;
		SecureRandom r = new SecureRandom();
		do
		{
			buff = getZahl( r.nextLong() % (nMax-nMin) + nMin );
		}while( buff.compareTo(min) < 0 || buff.compareTo(max) >= 0 );
		return buff;
	}*/
	
	/**
	 * Генерирует какое-то число на интервале [min; max). Это число не обязательно простое, скорее наоборот
	 * 
	 * @param min - минимальное число для генерации
	 * @param max - максимальное число для генерации
	 * @return сгенерированное какое-то число
	 */
	public static BigInteger rndBigInteger(BigInteger min, BigInteger max)
	{
		// Николай, почему так грустно?
		SecureRandom r = new SecureRandom();
		int MaxBits = max.toByteArray().length * 10;
		MaxBits = MaxBits*(r.nextInt() % 50 + 3);
		BigInteger g = new BigInteger(MaxBits > 0?MaxBits:MaxBits*-1, r);
		g = g.add( max.multiply( BigInteger.valueOf(r.nextInt() + 3) ) );
		BigInteger res = min.add( g.mod(max.subtract(min)) ); // аля res = min + rnd() % (max-min)
		/*BigInteger m;
		if(max.getLowestSetBit() != 0)
			m = max.add(BigInteger.ONE);
		else
			m = max.add(BigInteger.ZERO);
		long buff = r.nextLong();
		if (buff < 500)
			buff += 500;
		BigInteger g = BigInteger.valueOf( buff );
		BigInteger res = min.add( m.modPow(g, max.subtract(min)) );*/
		return res;
	}
	
	/**
	 * Генерирует какое-то число, у которого n цифр. Это число не обязательно простое, скорее наоборот
	 * 
	 * @param n - кол-во цифр этого числа
	 * @return сгенерированное число
	 */
    public static BigInteger getZahl(long n)
	{
		SecureRandom r = new SecureRandom();
		StringBuilder s = new StringBuilder();
		s.append(String.valueOf(1 + r.nextInt(9)));
		for (long i = 1 ; i < n ; i++)
			s.append(String.valueOf(r.nextInt(10)));
        
		return new BigInteger(s.toString());
	}
	
	/**
	 * Тест Миллера — Рабина на простоту числа
	 * Производится k раундов проверки числа n на простоту
	 * Из теоремы Рабина следует, что если k случайно выбранных чисел оказались свидетелями простоты числа n, то вероятность того, что n составное, не превосходит 4^(-k) (или число составное с вероятностью 0.25^k)
	 * 
	 * @param n - число, которое проверяется на простоту
	 * 
	 * @return true = вероятно простое
	*/
	public boolean millerRabinTest(BigInteger n)
	{
		BigInteger TWO = BigInteger.valueOf(2);
		BigInteger n_MINUS_ONE = n.subtract(BigInteger.ONE);
		// если n == 2 или n == 3 - эти числа простые, возвращаем true
		if (n.compareTo(TWO) == 0 || n.compareTo(BigInteger.valueOf(3)) == 0)
			return true;
	 
		// если n < 2 или n четное - возвращаем false
		if (n.compareTo(TWO) < 0 || n.getLowestSetBit() != 0)
			return false;
	 
		// представим n − 1 в виде (2^s)·t, где t нечётно, это можно сделать последовательным делением n - 1 на 2
		
		BigInteger t = n.subtract( BigInteger.ONE );
	 
		long s = 0;
		while (t.getLowestSetBit() != 0)
		{
			t = t.divide(TWO);
			s += 1;
		}
	 
		// повторить k раз
		for (long i = 0; i < k; i++)
		{
			// выберем случайное целое число a в отрезке [2, n − 2]
	 
			BigInteger a = PrimeNum.rndBigInteger(TWO, n_MINUS_ONE);
	 
			// x ← a^t mod n, вычислим с помощью возведения в степень по модулю
			BigInteger x = a.modPow(t, n);
	 
			// если x == 1 или x == n − 1, то перейти на следующую итерацию цикла
			if (x.compareTo(BigInteger.ONE) == 0 || x.compareTo(n_MINUS_ONE) == 0)
				continue;
	 
			// повторить s − 1 раз
			for (long r = 1; r < s; r++)
			{
				// x ← x^2 mod n
				x = x.modPow(TWO, n);
	 
				// если x == 1, то вернуть "составное"
				if (x.compareTo(BigInteger.ONE) == 0)
					return false;
	 
				// если x == n − 1, то перейти на следующую итерацию внешнего цикла
				if (x.compareTo( n_MINUS_ONE ) == 0)
					break;
			}
			
			if (x.compareTo( n_MINUS_ONE ) != 0)
				return false;
		}
	 
		// вернуть "вероятно простое"
		return true;
	}

	/**
	 * Тест Миллера — Рабина на простоту числа
	 * Производится rounds раундов проверки числа n на простоту
	 * Из теоремы Рабина следует, что если rounds случайно выбранных чисел оказались свидетелями простоты числа n, то вероятность того, что n составное, не превосходит 4^(-rounds) (или число составное с вероятностью 0.25^rounds)
	 * 
	 * @param n - число, которое проверяется на простоту
	 * @param rounds - кол-во раундов
	 * 
	 * @return true = вероятно простое
	*/
	public static boolean millerRabinTest(BigInteger n, long rounds)
	{
		BigInteger TWO = BigInteger.valueOf(2);
		BigInteger n_MINUS_ONE = n.subtract(BigInteger.ONE);
		// если n == 2 или n == 3 - эти числа простые, возвращаем true
		if (n.compareTo(TWO) == 0 || n.compareTo(BigInteger.valueOf(3)) == 0)
			return true;
	 
		// если n < 2 или n четное - возвращаем false
		if (n.compareTo(TWO) < 0 || n.getLowestSetBit() != 0)
			return false;
	 
		// представим n − 1 в виде (2^s)·t, где t нечётно, это можно сделать последовательным делением n - 1 на 2
		
		BigInteger t = n.subtract( BigInteger.ONE );
	 
		long s = 0;
		while (t.getLowestSetBit() != 0)
		{
			t = t.divide(TWO);
			s += 1;
		}
	 
		// повторить k раз
		for (long i = 0; i < rounds; i++)
		{
			// выберем случайное целое число a в отрезке [2, n − 2]
	 
			BigInteger a = PrimeNum.rndBigInteger(TWO, n_MINUS_ONE);
	 
			// x ← a^t mod n, вычислим с помощью возведения в степень по модулю
			BigInteger x = a.modPow(t, n);
	 
			// если x == 1 или x == n − 1, то перейти на следующую итерацию цикла
			if (x.compareTo(BigInteger.ONE) == 0 || x.compareTo(n_MINUS_ONE) == 0)
				continue;
	 
			// повторить s − 1 раз
			for (long r = 1; r < s; r++)
			{
				// x ← x^2 mod n
				x = x.modPow(TWO, n);
	 
				// если x == 1, то вернуть "составное"
				if (x.compareTo(BigInteger.ONE) == 0)
					return false;
	 
				// если x == n − 1, то перейти на следующую итерацию внешнего цикла
				if (x.compareTo( n_MINUS_ONE ) == 0)
					break;
			}
			
			if (x.compareTo( n_MINUS_ONE ) != 0)
				return false;
		}
	 
		// вернуть "вероятно простое"
		return true;
	}

	/**
	 * Метод, который позволит единственным образом вредставить байты в виде большого числа
	 * 
	 * @param b - набор байт, который необходимо представить в виде большого числа
	 * @return большое число, которо представляет единственным образом b
	 */
	public static BigInteger BytesToNum(byte[] b)
	{
		byte[] res = new byte[b.length + 1];
		
		for(int i = 0; i < b.length; i++)
			res[i+1] = b[i];
		res[0] = 127;
		return new BigInteger(res);
	}

	/**
	 * Метод, который позволяет представить единственным образом большое число в виде байт
	 * 
	 * @param a - число, которое необходимо представить в виде байт
	 * @return байты, которые представляют число есдинственным образом
	 */
	public static byte[] NumToBytes(BigInteger a)
	{
		byte[] b = a.toByteArray();
		byte[] res = new byte[b.length-1];
		for(int i = 0; i < res.length; i++)
			res[i] = b[i+1];
		return res;
	}

	public static void main(String[] args)
	{
		String S = "Ky2142151";
		byte[] b = S.getBytes();
		BigInteger a = BytesToNum(b);
		System.out.println(a);
		System.out.println( new String( NumToBytes(a) )  );
	}
}