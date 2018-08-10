package com.qik.demo.common;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by cxq on 2018/7/14.
 */
public class RSAUtil {

    private static Map<String, String> keyMap = new HashMap<>();
    private static String public_key = "public_key";
    private static String private_key = "private_key";
    private static KeyPair keyPairGenerator;

    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub

        initKeyPair(1024);

        Scanner scanner = new Scanner(System.in);
        String data = scanner.next();

        //获取公钥，并以base64格式打印出来
        PublicKey publicKey = genPublicKey();
        String publicKeyString = Codec.base64Encoder(publicKey.getEncoded());
        if (!keyMap.containsKey(public_key)) {
            keyMap.put(public_key, publicKeyString);
        }
        System.out.println("公钥：" + publicKeyString);

        //获取私钥，并以base64格式打印出来
        PrivateKey privateKey = genPrivateKey();

        String privateKeyString = Codec.base64Encoder(privateKey.getEncoded());
        if (!keyMap.containsKey(private_key)) {
            keyMap.put(private_key, privateKeyString);
        }
        System.out.println("私钥：" + privateKeyString);

        //公钥加密
        byte[] encryptedBytes = encrypt(data.getBytes(), publicKey);
        System.out.println("加密后：" + new String(encryptedBytes));

        //私钥解密
        byte[] decryptedBytes = decrypt(encryptedBytes, privateKey);
        System.out.println("解密后：" + new String(decryptedBytes));
    }

    //生成密钥对
    public static void initKeyPair(int keyLength) throws Exception {
        if (keyPairGenerator == null) {
            KeyPairGenerator newKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
            newKeyPairGenerator.initialize(keyLength);
            keyPairGenerator = newKeyPairGenerator.generateKeyPair();
        }
    }

    public static PrivateKey genPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (keyMap.containsKey(private_key)) {
            String privateKeyString = keyMap.get(private_key);
            KeyFactory rsa = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Codec.base64Decoder(privateKeyString));

            return rsa.generatePrivate(keySpec);
        }
        return keyPairGenerator.getPrivate();
    }

    public static PublicKey genPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (keyMap.containsKey(public_key)) {
            String publicKeyString = keyMap.get(public_key);
            KeyFactory rsa = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Codec.base64Decoder(publicKeyString));

            return rsa.generatePublic(keySpec);
        }
        return keyPairGenerator.getPublic();
    }
    //公钥加密
    public static byte[] encrypt(byte[] content, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");//java默认"RSA"="RSA/ECB/PKCS1Padding"
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(content);
    }

    //私钥解密
    public static byte[] decrypt(byte[] content, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(content);
    }


}
