package com.dongzy.common.common.io.zip.io;

import java.io.IOException;
import java.io.InputStream;

import com.dongzy.common.common.io.zip.unzip.UnzipEngine;

public abstract class BaseInputStream extends InputStream {

	public int read() throws IOException {
		return 0;
	}
	
	public void seek(long pos) throws IOException {
	}
	
	public int available() throws IOException {
		return 0;
	}
	
	public UnzipEngine getUnzipEngine() {
		return null;
	}

}
