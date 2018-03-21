package modules.experimental;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.Logger;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;

import common.parallelization.CallbackReceiver;
import modules.BytePipe;
import modules.CharPipe;
import modules.ModuleImpl;
import modules.OutputPort;

public class CloudReaderModule extends ModuleImpl {

	private static final Logger LOGGER = Logger.getLogger(CloudReaderModule.class.getSimpleName());

	// name of the output port
	private final String OUTPUTID = "output";

	// the storage configuration used to access the storage provider
	private StorageConfiguration config;

	public CloudReaderModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// Configure the output port for char and byte output
		OutputPort outputPort = new OutputPort(OUTPUTID, "Byte or character output.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		outputPort.addSupportedPipe(BytePipe.class);
		super.addOutputPort(outputPort);

		// setting up properties is done by the storage configuration
		StorageConfiguration.setPropertyDefaultValuesOn(this.getPropertyDefaultValues());
		StorageConfiguration.setPropertyDescriptionsOn(this.getPropertyDescriptions());

		// Add module description and name
		this.setDescription(
				"Reads contents from a remote object provider (e.g. aws-s3, open-stack, ...) via the jClouds BlobStore API.");
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "CloudReader");
	}

	@Override
	public boolean process() throws Exception {
		boolean result = false;

		int bufferLength = 2048;

		BlobStore blobStore = config.createBlobStore();

		// streaming is generally not supported yet with jClouds, so lets just get the
		// blob, if streaming is supported, we may do:
		// blobStore.streamBlob(container, file)
		// TODO: Enable streaming of large resource files
		Blob blob = blobStore.getBlob(config.container, config.file);

		// read the file
		try (InputStream stream = blob.getPayload().openStream();
				InputStreamReader streamReader = new InputStreamReader(stream, config.encoding);) {
			char buffer[] = new char[bufferLength];

			int charsRead = streamReader.read(buffer);
			while (charsRead != -1) {
				if (Thread.interrupted()) {
					throw new InterruptedException("Thread has been interrupted.");
				}

				// output the buffer to listening modules
				// TODO: Handle byte pipes
				this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(buffer, 0, charsRead);

				charsRead = streamReader.read(buffer);
			}
			// if we get this far, all went probably well
			result = true;
			LOGGER.info("Completed reading from: " + blob.getMetadata().getUri());
		} finally {
			// close resources not opened in the try statement
			this.closeAllOutputs();
			config.close();
		}

		return result;
	}

	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();
		this.config = StorageConfiguration.fromProperties(this.getProperties());
		super.applyProperties();
	}

}
