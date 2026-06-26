package com.hypersocket.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;

@Service
public class ZipExportService {

	public void streamZipFile(List<FileSpec> files, OutputStream out) throws IOException, AccessDeniedException {
		try (ZipOutputStream zos = new ZipOutputStream(out)) {
			zos.setLevel(Deflater.DEFAULT_COMPRESSION);

			for (FileSpec spec : files) {
				ZipEntry entry = new ZipEntry(spec.fileName);
				zos.putNextEntry(entry);
				// Wrap the ZipOutputStream so generators can't close the underlying stream
				// this is required as some downstream code explicitly close the stream, resulting in "Stream Closed" error.
				NonClosingOutputStream nc = new NonClosingOutputStream(zos);
				spec.generator.generate(nc, spec.realm);
				zos.closeEntry();
			}
		}
	}

	public static class FileSpec {
		public final String fileName;
		public final FileContentGenerator generator;
		public final Realm realm;

		public FileSpec(String fileName, FileContentGenerator generator, Realm realm) {
			this.fileName = fileName;
			this.generator = generator;
			this.realm = realm;
		}
	}

	/**
	 * OutputStream wrapper that ignores close() so callers can safely close
	 * wrappers (e.g. Writers) without closing the underlying ZipOutputStream.
	 * 
	 * This is required as some downstream code explicitly close the stream, resulting in "Stream Closed" error.
	 */
	private static class NonClosingOutputStream extends FilterOutputStream {
		
		public NonClosingOutputStream(OutputStream out) {
			super(out);
		}

		@Override
		public void close() throws IOException {
			// don't close the underlying stream; just flush to ensure data is written
			try {
				flush();
			} catch (IOException e) {
				// if flush fails, propagate to caller
				throw e;
			}
			// deliberately omit super.close();
		}
	}

}