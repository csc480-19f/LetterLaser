package edu.oswego.websocket;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.Base64;

public class JSDecryptor {

	private String pubKey;
	private String privKey;
	private Cipher decryptor;

	public JSDecryptor() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(1024);
		KeyPair keys = generator.generateKeyPair();

		PublicKey pub = keys.getPublic();
		PrivateKey priv = keys.getPrivate();

		pubKey = Base64.getEncoder().encodeToString(pub.getEncoded());
		privKey = Base64.getEncoder().encodeToString(priv.getEncoded());

		decryptor = Cipher.getInstance("RSA");
		decryptor.init(Cipher.DECRYPT_MODE, priv);
	}

	public String getPublic() {
		return pubKey;
	}

	public String decrypt(String m)
			throws BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
		byte[] cipherText = decryptor.doFinal(Base64.getDecoder().decode(m));
		m = new String(cipherText, "UTF-8");
		return m;
	}

}
