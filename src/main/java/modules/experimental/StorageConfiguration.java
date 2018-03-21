package modules.experimental;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.Properties;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.shiro.codec.Hex;
import org.apache.shiro.crypto.AesCipherService;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;

import base.web.Server;

// a test class for the cloud reader and writer classes
// TODO: Actually setup properties for the two classes
class StorageConfiguration implements AutoCloseable {

	private static final String PKEY_CONTEXT_IDENTIFIER = "Context identifier";
	private static final String PKEY_ENDPOINT = "Endpoint";
	private static final String PKEY_IDENTITY = "Identity";
	private static final String PKEY_CREDENTIAL = "Credential";
	private static final String PKEY_SALT = "Salt";
	private static final String PKEY_CONTAINER = "Container";
	private static final String PKEY_FILE = "File";
	private static final String PKEY_ENCODING = "Encoding";

	private static String[] PKEYS_WITH_EMPTY_DEFAULT = { PKEY_CONTEXT_IDENTIFIER, PKEY_ENDPOINT, PKEY_IDENTITY,
			PKEY_CREDENTIAL, PKEY_SALT, PKEY_CONTAINER, PKEY_FILE };

	private static String DEFAULT_VALUE = "";

	String contextIdentifier;
	String endpoint;
	String identity;
	String credential;
	String salt;
	String container;
	String file;
	String encoding;

	// the actual context build from the above values and used to interact with the
	// storage provider
	BlobStoreContext context = null;

	private StorageConfiguration() {
	};

	static StorageConfiguration fromProperties(Properties properties) {
		StorageConfiguration result = new StorageConfiguration();

		result.contextIdentifier = properties.getProperty(PKEY_CONTEXT_IDENTIFIER, DEFAULT_VALUE);
		result.endpoint = properties.getProperty(PKEY_ENDPOINT, DEFAULT_VALUE);
		result.identity = properties.getProperty(PKEY_IDENTITY, DEFAULT_VALUE);
		result.credential = properties.getProperty(PKEY_CREDENTIAL, DEFAULT_VALUE);
		result.salt = properties.getProperty(PKEY_SALT, DEFAULT_VALUE);
		result.container = properties.getProperty(PKEY_CONTAINER, DEFAULT_VALUE);
		result.file = properties.getProperty(PKEY_FILE, DEFAULT_VALUE);
		result.encoding = properties.getProperty(PKEY_ENCODING, DEFAULT_VALUE);

		return result;
	}

	static void setPropertyDefaultValuesOn(Map<String, String> propDefaults) {
		for (String key : PKEYS_WITH_EMPTY_DEFAULT) {
			propDefaults.put(key, DEFAULT_VALUE);
		}
		propDefaults.put(PKEY_ENCODING, "UTF-8");
	}

	static void setPropertyDescriptionsOn(Map<String, String> props) {
		props.put(PKEY_CONTEXT_IDENTIFIER,
				"A context identifier supported by jClouds, tested for: 'aws-s3', 'b2', 'openstack-swift'.");
		props.put(PKEY_ENDPOINT, "An endpoint to contact the Storage provider on. Needed for 'openstack-swift'.");
		props.put(PKEY_IDENTITY, "The identity to connect as, e.g. a username.");
		props.put(PKEY_CREDENTIAL, "The credential used to authenticate the identity.");
		props.put(PKEY_SALT, "A salt to use in decryption when the credential is given in encrypted form.");
		props.put(PKEY_CONTAINER, "The container name to use, e.g. an s3 bucket name.");
		props.put(PKEY_FILE, "The file to read from or write to.");
		props.put(PKEY_ENCODING, "An encoding to use when reading/writing the file.");
	}

	protected BlobStore createBlobStore() {
		// if a salt value is provided we assume the credential to be encrypted
		String credentialToUse = credential;
		if (!salt.isEmpty()) {
			credentialToUse = decrypt(credential, salt);
		}

		ContextBuilder builder = ContextBuilder.newBuilder(contextIdentifier).credentials(identity, credentialToUse);
		if (endpoint != null && !endpoint.isEmpty()) {
			builder.endpoint(endpoint);
		}
		context = builder.buildView(BlobStoreContext.class);
		return context.getBlobStore();
	}

	@Override
	public void close() throws Exception {
		if (context != null) {
			context.close();
		}
	}

	private static String decrypt(String encrypted, String salt) {
		if (!Server.sharedSecretIsAvailable()) {
			throw new RuntimeException("No shared secret is available to decrypt the credential.");
		}

		// Generate a key using the credentials salt
		byte[] saltBytes = Hex.decode(salt);
		PBEKeySpec pbe = new PBEKeySpec(Server.readSharedSecret().toCharArray(), saltBytes, 60000, 256);
		Key key;
		try {
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			key = keyFactory.generateSecret(pbe);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException("Unexpected error during key generation: " + e.getMessage());
		}

		// actually decrypt using the generated key
		AesCipherService aes = new AesCipherService();
		aes.setKeySize(256);
		return new String(aes.decrypt(Hex.decode(encrypted), key.getEncoded()).getBytes());
	}

}
