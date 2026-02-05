package com.base.weather;

import com.base.common.util.HttpUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.*;
import java.security.interfaces.EdECPrivateKey;
import java.security.interfaces.EdECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@SpringBootTest
class WeatherRestApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(1);
    }

    @Test
    void testPost() {
        String url = "http://localhost/v1/chat-messages";
        HttpUtils http = new HttpUtils();

    }

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // 创建Ed25519密钥对生成器
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("Ed25519");

        // 生成密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // 获取私钥和公钥
        EdECPrivateKey privateKey = (EdECPrivateKey) keyPair.getPrivate();
        EdECPublicKey publicKey = (EdECPublicKey) keyPair.getPublic();

        // 获取密钥的编码格式
        byte[] privateKeyBytes = privateKey.getEncoded();
        byte[] publicKeyBytes = publicKey.getEncoded();

        // 转换为Base64编码以便显示和存储
        String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKeyBytes);
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKeyBytes);

        // 打印密钥信息
        System.out.println("=== Ed25519 密钥对生成成功 ===");
        System.out.println("算法: " + privateKey.getAlgorithm());
        System.out.println("格式: " + privateKey.getFormat());
        System.out.println("\n私钥 (Base64):");
        System.out.println(privateKeyBase64);
        System.out.println("\n公钥 (Base64):");
        System.out.println(publicKeyBase64);
        System.out.println("\n私钥长度: " + privateKeyBytes.length + " 字节");
        System.out.println("公钥长度: " + publicKeyBytes.length + " 字节");

        // 演示如何从编码恢复密钥
        KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");

        // 从私钥编码恢复私钥
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        PrivateKey restoredPrivateKey = keyFactory.generatePrivate(privateKeySpec);

        // 从公钥编码恢复公钥
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey restoredPublicKey = keyFactory.generatePublic(publicKeySpec);

        System.out.println("\n=== 密钥恢复验证 ===");
        System.out.println("私钥恢复成功: " + restoredPrivateKey.equals(privateKey));
        System.out.println("公钥恢复成功: " + restoredPublicKey.equals(publicKey));
    }

    @Test
    void generateEd25519KeyPair() throws NoSuchAlgorithmException, InvalidKeySpecException {

    }


}
