package modularization;

import java.io.File;
import java.io.FileInputStream;
import java.io.PipedOutputStream;

public class FileReaderModule extends ModuleImpl {

	public FileReaderModule() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean process() throws Exception {
		FileInputStream is = new FileInputStream(new File("/tmp/test.txt"));
		PipedOutputStream os = new PipedOutputStream();
		byte[] buffer = new byte[1024];
		int readbytes = is.read(buffer);
		while (readbytes>0){
			os.write(buffer);
			readbytes = is.read(buffer);
		}
		is.close();
		os.close();
		return true;
	}

}
