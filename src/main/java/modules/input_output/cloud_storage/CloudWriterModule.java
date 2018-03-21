package modules.input_output.cloud_storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.bouncycastle.util.Arrays;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.MultipartPart;
import org.jclouds.blobstore.domain.MultipartUpload;
import org.jclouds.blobstore.options.PutOptions;

import common.parallelization.CallbackReceiver;
import modules.BytePipe;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.NotSupportedException;

public class CloudWriterModule extends ModuleImpl {

	private static final Logger LOGGER = Logger.getLogger(CloudReaderModule.class.getSimpleName());

	// name of the output port
	private final String INPUTID = "input";
	
	// the storag configuration used to access the storage provider
	private StorageConfiguration config;

	public CloudWriterModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// Configure the input port for char and byte input
		InputPort inputPort = new InputPort(INPUTID, "Byte or character input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		inputPort.addSupportedPipe(BytePipe.class);
		super.addInputPort(inputPort);
		
		// setting up properties is done by the storage configuration
		StorageConfiguration.setPropertyDefaultValuesOn(this.getPropertyDefaultValues());
		StorageConfiguration.setPropertyDescriptionsOn(this.getPropertyDescriptions());

		// Add module description and name
		this.setDescription(
				"Writes the input to a remote object provider (e.g. aws-s3, open-stack-swift, ...) via the jClouds BlobStore API.");
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "CloudWriter");
	}

	@Override
	public boolean process() throws Exception {
		boolean result = false;

		// configure the blob store to access the file
		BlobStore blobStore = config.createBlobStore();

		final InputPort inputPort = this.getInputPorts().get(INPUTID);
		Charset charset = Charset.forName(config.encoding);

		// we cannot use the BlobStore/Payload interfaces of jClouds as they will need
		// to have the whole input source present in some way to determine content
		// length, instead we will manually do the multipart upload

		// determine the buffer length
		int uploadChunkSize = 5000000;

		if (uploadChunkSize < blobStore.getMinimumMultipartPartSize()) {
			uploadChunkSize = (new Long(blobStore.getMinimumMultipartPartSize())).intValue();
		}
		if (uploadChunkSize > blobStore.getMaximumMultipartPartSize()) {
			// if the buffer length is already bigger, we may safely cast to long
			// TODO: This might actually not work with char stream uploads..., throw?
			uploadChunkSize = (int) blobStore.getMaximumMultipartPartSize();
		}
		LOGGER.info("Using a multipart upload size of: " + uploadChunkSize);

		// build a blob, mainly used for building the correct metadata
		Blob blob = blobStore.blobBuilder(config.file).build();

		try {
			// Make sure that we will have at least two parts of data to upload
			byte[] part1 = readNextPayload(uploadChunkSize, inputPort, charset);
			byte[] part2 = readNextPayload(uploadChunkSize, inputPort, charset);

			if (part2.length == 0) {
				LOGGER.info("Will do a single upload with size: " + part1.length + " B");
				blob.setPayload(part1);
				blobStore.putBlob(config.container, blob);
				return true;
			}

			// we have at least two chunks, so let's begin a multipart upload;
			MultipartUpload mpu = blobStore.initiateMultipartUpload(config.container, blob.getMetadata(),
					new PutOptions(true));
			if (mpu != null) {
				LOGGER.info("Got mpu: " + mpu);
			} else {
				throw new RuntimeException("Unable to initialise multipart upload");
			}
			// the list of uploaded payload parts
			List<MultipartPart> parts = new ArrayList<>();
			parts.add(uploadChunk(part1, 1, blob, blobStore, mpu));
			parts.add(uploadChunk(part2, 2, blob, blobStore, mpu));

			// read the next input and upload any additional parts
			while (true) {
				if (parts.size() >= blobStore.getMaximumNumberOfParts()) {
					blobStore.abortMultipartUpload(mpu);
					throw new RuntimeException(
							"Aborting upload. Maximum number of parts reached: " + blobStore.getMaximumNumberOfParts());
				}
				byte[] nextPayload = readNextPayload(uploadChunkSize, inputPort, charset);
				if (nextPayload.length == 0) {
					break;
				}
				MultipartPart part = uploadChunk(nextPayload, parts.size() + 1, blob, blobStore, mpu);
				parts.add(part);
			}
			String eTag = blobStore.completeMultipartUpload(mpu, parts);
			LOGGER.info("Finished uploading to: " + blob.getMetadata().getUri() + ", eTag: " + eTag);
			result = true;
		} finally {
			config.close();
		}

		return result;
	}

	private MultipartPart uploadChunk(byte[] nextPayload, int partNr, Blob blob, BlobStore blobStore,
			MultipartUpload mpu) {
		blob.setPayload(nextPayload);
		LOGGER.info("payload: " + blob.getPayload());

		// attempt the multipart upload
		MultipartPart part = blobStore.uploadMultipartPart(mpu, partNr, blob.getPayload());
		String msg = String.format("Uploaded part %s as Nr. %d, length: %d", part.partETag(), part.partNumber(),
				part.partSize());
		LOGGER.info(msg);
		return part;
	}

	/**
	 * Read about n bytes (possibly some more) from the given input port and return
	 * them as an array
	 */
	private byte[] readNextPayload(int n, InputPort port, Charset encoding) throws IOException {
		int chunkSize = 1000;
		byte[] bytes = new byte[n + chunkSize];
		int bytesRead = 0;

		try {
			// handle ports with byte input
			if (port.isConnectedTo(BytePipe.class)) {
				int singleRead = 0;

				while (bytesRead < n) {
					singleRead = port.getInputStream().read(bytes, bytesRead, chunkSize);
					if (singleRead <= 0) {
						break;
					}
					bytesRead += singleRead;
				}
			}
			// handle ports with char input
			if (port.isConnectedTo(CharPipe.class)) {
				int charChunkSize = (chunkSize / 2) - 1;
				int charsInSingleRead = 0;
				char chars[] = new char[charChunkSize];

				while (bytesRead < n) {
					charsInSingleRead = port.getInputReader().read(chars, 0, charChunkSize);
					if (charsInSingleRead <= 0) {
						break;
					}

					ByteBuffer temp = encoding.encode(CharBuffer.wrap(Arrays.copyOf(chars, charsInSingleRead)));
					System.arraycopy(temp.array(), 0, bytes, bytesRead, temp.limit());
					bytesRead += temp.limit();
				}
			}
		}
		// we should never get these as we check for pipe types
		catch (NotSupportedException e) {
			throw new IllegalStateException(e);
		}

		return Arrays.copyOf(bytes, bytesRead);
	}
	
	
	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();
		this.config = StorageConfiguration.fromProperties(this.getProperties());
		super.applyProperties();
	}

}
