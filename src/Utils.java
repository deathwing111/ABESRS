import com.sun.xml.internal.ws.util.ByteArrayBuffer;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

public class Utils {
    public static Element HashGid(Field f, byte v, byte[] gid){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        digest.update(v);
        digest.update(gid);
        byte[] hash = digest.digest();
        return f.newElementFromHash(hash, 0, hash.length);
    }

    public static Element HashU(Pairing pairing, String u){
        return HashE(u.getBytes(), pairing.getG1());
    }

    public static Element HashE(byte[] m, Field group){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        digest.update(m);
        byte[] hash = digest.digest();
        return group.newElementFromHash(hash, 0, hash.length);

    }

    public static Element HashesE(byte[][] m, Field group){

        ByteArrayBuffer buffer = new ByteArrayBuffer();
        byte[] message = null;
        try{
            for (int i = 0; i < m.length; i++) {
                buffer.write(m[i]);
            }
            buffer.flush();
            message = buffer.getRawData();
            buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return HashE(message, group);

    }

    public static BigInteger HashN(BigInteger n, byte[] bytes){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        digest.update(bytes);
        byte[] hash = digest.digest();
        if(n == null)
            return new BigInteger(hash);
        int d = hash[0] % 2 + 2;
        return BigInteger.valueOf(d).modPow(new BigInteger(hash), n);
    }

    public static BigInteger HashesN(BigInteger n, byte[][] bytes){

        ByteArrayBuffer buffer = new ByteArrayBuffer();
        byte[] message = null;
        try{
            for (int i = 0; i < bytes.length; i++) {
                buffer.write(bytes[i]);
            }
            buffer.flush();
            message = buffer.getRawData();
            buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return HashN(n, message);
    }

    public static BigInteger randomZn(BigInteger n, Random random){
        BigInteger b = new BigInteger(n.bitLength(), random);
        int d = b.mod(BigInteger.valueOf(2)).intValue() + 2;
        return BigInteger.valueOf(d).modPow(b, n);
    }

    public static byte[] AES_KGEN(byte[] seed){
        KeyGenerator kgen = null;
        try {
            kgen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        SecureRandom rand = new SecureRandom();
        rand.setSeed(seed);
        kgen.init(256, rand);
        SecretKey sk = kgen.generateKey();
        return sk.getEncoded();
    }
    public static final String IV  = "1ci5crnda6ojzgtr";
    // 1 / Cipher.ENCRYPT_MODE, 2 / Cipher.DECRYPT_MODE
    public static byte[] AES_ENC(int mod, byte[] key, byte[] input){
        SecretKeySpec k = new SecretKeySpec(AES_KGEN(key), "AES");
        Cipher c = null;
        byte[] res = null;
        try {
            c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ips = new IvParameterSpec(IV.getBytes());
            c.init(mod, k, ips);
            res = c.doFinal(input);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } finally {
            return res;
        }
    }

    public static String extract_theta(String value){
        if(value == null)
            return null;
        if("".equals(value))
            return null;
        if(!value.contains("@"))
            return null;
        int index = value.indexOf('@');
        String r = value.substring(index + 1);
        return r;
    }

    public static BigInteger[] RSAGEN(int lambda, Random random){
        BigInteger p = BigInteger.probablePrime(lambda, random);
        BigInteger q = BigInteger.probablePrime(lambda, random);
        BigInteger n = p.multiply(q);
        BigInteger phi_n = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        BigInteger e;
        while(true){
            e = BigInteger.probablePrime(lambda, random);
            if(e.gcd(phi_n).equals(BigInteger.ONE))
                break;
        }
        // e * d mod phi(n) = e * d mod (p-1)(q-1) = 1
        BigInteger d = e.modInverse(phi_n);

        return new BigInteger[]{
                n, e, d
        };
    }

    public static BigInteger[] RSAGEN(int lambda, Random random, BigInteger e){
        BigInteger p = BigInteger.probablePrime(lambda, random);
        BigInteger q = BigInteger.probablePrime(lambda, random);
        BigInteger n = p.multiply(q);
        BigInteger phi_n = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        // e * d mod phi(n) = e * d mod (p-1)(q-1) = 1
        BigInteger d = e.modInverse(phi_n);

        return new BigInteger[]{
                n, e, d
        };
    }

    public static boolean check_in_group(BigInteger a, BigInteger n){
        if(a.equals(BigInteger.ZERO))
            return false;
        if(a.compareTo(n) >= 0)
            return false;
        if(!a.gcd(n).equals(BigInteger.ONE))
            return false;
        return true;
    }

    public static byte[] long2bytes(long t){
        return new byte[]{
                (byte) (t & 0xFF),
                (byte) ((t >> 8) & 0xFF),
                (byte) ((t >> 16) & 0xFF),
                (byte) ((t >> 24) & 24)
        };
    }

    public static String array2str(Object[] array){
        String res = "[ ";
        for (int i = 0; i < array.length - 1; i++) {
            res += array[i] + " , ";
        }
        res += array[array.length - 1] + " ]";
        return res;
    }
    public static int[][] search(String[] src, String[] filter){
        int[] tmp = new int[src.length];
        Arrays.fill(tmp, -1);
        int[] res1 = new int[src.length];
        int[] res2 = new int[src.length];
        int index = 0;
        int start = 0;
        int temp = 0;
        for(int i = 0; i < filter.length; i++){
            start = 0;

            for(int j = 0; j < src.length; j++){
                if(src[j].equals(filter[i])){
                    tmp[j] = i;
                }
            }

        }
        for (int i = 0; i < src.length; i++) {
            if(tmp[i] >= 0){
                res1[index] = i;
                res2[index] = tmp[i];
                index++;
            }
        }
        if(index == 0)
            return null;
        res1 = Arrays.copyOf(res1, index);
        res2 = Arrays.copyOf(res2, index);
        int[][] res = new int[][]{ res1, res2};
        return res;
    }

    public static void main(String[] args){
        byte[] sed = "Hello".getBytes();
        System.out.println(Arrays.equals(AES_KGEN(sed), AES_KGEN(sed)));
    }
}
