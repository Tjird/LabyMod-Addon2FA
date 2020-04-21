package me.tjird.mfaaddon;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TOTP {
    private static final int[] DIGITS_POWER = new int[]{1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};

    private TOTP() {
    }

    private static byte[] hmac_sha(String crypto, byte[] keyBytes, byte[] text) {
        try {
            Mac hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        }
        catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }

    private static byte[] hexStr2Bytes(String hex) {
        byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();
        byte[] ret = new byte[bArray.length - 1];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = bArray[i + 1];
        }
        return ret;
    }

    public static String generateTOTP(String key, String time, String returnDigits) {
        return TOTP.generateTOTP(key, time, returnDigits, "HmacSHA1");
    }

    public static String generateTOTP256(String key, String time, String returnDigits) {
        return TOTP.generateTOTP(key, time, returnDigits, "HmacSHA256");
    }

    public static String generateTOTP512(String key, String time, String returnDigits) {
        return TOTP.generateTOTP(key, time, returnDigits, "HmacSHA512");
    }

    public static String generateTOTP(String key, String time, String returnDigits, String crypto) {
        int codeDigits = Integer.decode(returnDigits);
        String result = null;
        while (time.length() < 16) {
            time = "0" + time;
        }
        byte[] msg = TOTP.hexStr2Bytes(time);
        byte[] k = TOTP.hexStr2Bytes(key);
        byte[] hash = TOTP.hmac_sha(crypto, k, msg);
        int offset = hash[hash.length - 1] & 15;
        int binary = (hash[offset] & 127) << 24 | (hash[offset + 1] & 255) << 16 | (hash[offset + 2] & 255) << 8 | hash[offset + 3] & 255;
        int otp = binary % DIGITS_POWER[codeDigits];
        result = Integer.toString(otp);
        while (result.length() < codeDigits) {
            result = "0" + result;
        }
        return result;
    }

    public static void main(String[] args) {
        String seed = "3132333435363738393031323334353637383930";
        String seed32 = "3132333435363738393031323334353637383930313233343536373839303132";
        String seed64 = "31323334353637383930313233343536373839303132333435363738393031323334353637383930313233343536373839303132333435363738393031323334";
        long T0 = 0;
        long X = 30;
        long[] testTime = new long[]{59, 1111111109, 1111111111, 1234567890, 2000000000, 20000000000L};
        String steps = "0";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            System.out.println("+---------------+-----------------------+------------------+--------+--------+");
            System.out.println("|  Time(sec)    |   Time (UTC format)   | Value of T(Hex)  |  TOTP  | Mode   |");
            System.out.println("+---------------+-----------------------+------------------+--------+--------+");
            for (int i = 0; i < testTime.length; ++i) {
                long T = (testTime[i] - T0) / X;
                steps = Long.toHexString(T).toUpperCase();
                while (steps.length() < 16) {
                    steps = "0" + steps;
                }
                String fmtTime = String.format("%1$-11s", testTime[i]);
                String utcTime = df.format(new Date(testTime[i] * 1000));
                System.out.print("|  " + fmtTime + "  |  " + utcTime + "  | " + steps + " |");
                System.out.println(TOTP.generateTOTP(seed, steps, "8", "HmacSHA1") + "| SHA1   |");
                System.out.print("|  " + fmtTime + "  |  " + utcTime + "  | " + steps + " |");
                System.out.println(TOTP.generateTOTP(seed32, steps, "8", "HmacSHA256") + "| SHA256 |");
                System.out.print("|  " + fmtTime + "  |  " + utcTime + "  | " + steps + " |");
                System.out.println(TOTP.generateTOTP(seed64, steps, "8", "HmacSHA512") + "| SHA512 |");
                System.out.println("+---------------+-----------------------+------------------+--------+--------+");
            }
        }
        catch (Exception e) {
            System.out.println("Error : " + e);
        }
    }

    public static String getTOTPCode(String secretKey) {
        String normalizedBase32Key = secretKey.replace(" ", "").toUpperCase();
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(normalizedBase32Key);
        String hexKey = Hex.encodeHexString((byte[])bytes);
        long time = System.currentTimeMillis() / 1000 / 30;
        String hexTime = Long.toHexString(time);
        return TOTP.generateTOTP(hexKey, hexTime, "6");
    }
}
