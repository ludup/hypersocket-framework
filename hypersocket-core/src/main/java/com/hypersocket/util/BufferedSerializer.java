package com.hypersocket.util;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class BufferedSerializer<S> implements Closeable {

	private final List<S> buffer = Collections.synchronizedList(new LinkedList<>());
	private final File bufferFile;

	private int bufferSize = 100;
	private long items;
	private ObjectOutputStream oos;

	public BufferedSerializer(File bufferFile) throws IOException {
		/*
		 * TODO: If we are keeping these, there may be some scope for replaying them if
		 * the server crashes so events are not lost.
		 */
		bufferFile.deleteOnExit();

		this.bufferFile = bufferFile;
		oos = new ObjectOutputStream(new FileOutputStream(bufferFile));
	}

	public void add(S item) {
		synchronized (buffer) {
			if (oos == null)
				throw new IllegalStateException("Cannot add once iterated.");
			buffer.add(item);
			if (buffer.size() >= bufferSize) {
				flush();
			}
		}
	}

	@Override
	public void close() throws IOException {
		if (oos != null) {
			oos.close();
		}
		buffer.clear();
		items = 0;
		bufferFile.delete();
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public long getSize() {
		return items + buffer.size();
	}

	public Iterator<S> iterator() {
		if (oos != null) {
			try {
				oos.close();
			} catch (IOException ioe) {
				throw new IllegalStateException("Failed to close cache stream.", ioe);
			}
			oos = null;
		}
		return new Iterator<S>() {

			private ObjectInputStream in;
			private File inFile;
			private Iterator<S> memoryIt;
			private S next;

			@Override
			public boolean hasNext() {
				checkNext();
				return next != null;
			}

			@Override
			public S next() {
				try {
					checkNext();
					if (next == null)
						throw new NoSuchElementException();
					return next;
				} finally {
					next = null;
				}
			}

			@SuppressWarnings("unchecked")
			void checkNext() {
				if (next == null) {
					if (inFile == null) {
						inFile = bufferFile;
						if (inFile.exists()) {
							try {
								in = new ObjectInputStream(new FileInputStream(inFile));
							} catch (EOFException eof) {
								in = null;
							} catch (IOException e) {
								throw new IllegalStateException("Failed to read cache file.", e);
							}
						}
					}

					if (in != null) {
						try {
							next = (S) in.readObject();
							if (next != null)
								return;
							/* No more in the cache */

							in.close();
						} catch (EOFException eof) {
							try {
								in.close();
							} catch (IOException e) {
							}
							next = null;
							return;
						} catch (ClassNotFoundException | IOException e) {
							throw new IllegalStateException("Failed to read cache file.", e);
						}
					}

					if (memoryIt == null) {
						memoryIt = buffer.iterator();
					}

					if (memoryIt != null) {
						if (memoryIt.hasNext())
							next = memoryIt.next();
					}
				}
			}

		};
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
		if (bufferSize < buffer.size())
			flush();
	}

	protected void flush() {
		try {
			synchronized (buffer) {
				for (S s : buffer) {
					oos.writeObject(s);
				}
				oos.flush();
				items += buffer.size();
				buffer.clear();
			}
		} catch (IOException ioe) {
			throw new IllegalStateException(String.format("Failed to flush to %s", bufferFile));
		}
	}

}
