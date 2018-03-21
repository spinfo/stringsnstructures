package modules.experimental;

import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;

// a test class for the cloud reader and writer classes
// TODO: Actually setup properties for the two classes
class StorageConfiguration implements AutoCloseable {

	private static final Logger LOGGER = Logger.getLogger(StorageConfiguration.class.getName());

	private static final String PKEY_CONTEXT_IDENTIFIER = "Context identifier";
	private static final String PKEY_ENDPOINT = "Endpoint";
	private static final String PKEY_IDENTITY = "Identity";
	private static final String PKEY_CREDENTIAL = "Credential";
	private static final String PKEY_SALT = "Salt";
	private static final String PKEY_CONTAINER = "Container";
	private static final String PKEY_FILE = "File";
	private static final String PKEY_ENCODING = "Encoding";

	private static String[] PROPERTY_KEYS = { PKEY_CONTEXT_IDENTIFIER, PKEY_ENDPOINT, PKEY_IDENTITY, PKEY_CREDENTIAL,
			PKEY_SALT, PKEY_CONTAINER, PKEY_FILE, PKEY_ENCODING };

	private static String DEFAULT_VALUE = "";

	String contextIdentifier;
	String url;
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
		result.url = properties.getProperty(PKEY_ENDPOINT, DEFAULT_VALUE);
		result.identity = properties.getProperty(PKEY_IDENTITY, DEFAULT_VALUE);
		result.credential = properties.getProperty(PKEY_CREDENTIAL, DEFAULT_VALUE);
		result.container = properties.getProperty(PKEY_CONTAINER, DEFAULT_VALUE);
		result.file = properties.getProperty(PKEY_FILE, DEFAULT_VALUE);
		result.encoding = properties.getProperty(PKEY_ENCODING, DEFAULT_VALUE);

		return result;
	}

	static void setPropertyDefaultValuesOn(Map<String, String> propDefaults) {
		for (String key : PROPERTY_KEYS) {
			propDefaults.put(key, DEFAULT_VALUE);
		}
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
		ContextBuilder builder = ContextBuilder.newBuilder(contextIdentifier).credentials(identity, credential);
		if (url != null && !url.isEmpty()) {
			builder.endpoint(url);
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

}
