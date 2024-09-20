package com.hypersocket.netty.util;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;

/**
 * 
 * 
 *  The standard GZIP header (10 bytes), which is defined in bytes so the bit-ordering is immaterial
 * 
 * 	1f 8b	Standard GZIP declaration
 *	08	Compression method: 0x08 represents GZIP
 *	08	Flags (see below)
 *	a2 42 b8 4d	Timestamp
 *	00	Extra flags
 *	03	Operating System
 *
 *
 *	The flags byte, byte 4, is interpreted as shown below.
 *
 *	Bit mask (in big-endian format)	Meaning
 *
 *	00000001	Text follows
 *	00000010	Header CRC follows
 *	00000100	"Extra" follows
 *	00001000	Name follows (byte 8 indicates we have a file name)
 *	00010000	Comment follows
 *
 *	In essence, the flags byte indicates that the header can be followed by up to five null- terminated strings, 
 *	which must at least be skipped over before the actual gzipped-proper content appears.
 *
 *	This class modifies the header of Gzip adding flag value 8 for filename and adds random bytes to it
 *	followed by Gzip data.
 *
 *	BREACH vulnerability demands that Gzip response should contain nounce or random string, filename seems appropriate
 *  for adding these random bytes on every request mitigating the vulnerability. 
 *  
 *  Note: This does not eliminates the vulnerability it adds more challenge.
 */
public class GzipBreachHandlerByteArrayOutputStream extends ByteArrayOutputStream {
	
	public static final int DEFAULT_MAX_RANDOM_BYTES = 100;
	
	private SecureRandom secureRandom = new SecureRandom();
	
	private int randomBytes;
	
	public GzipBreachHandlerByteArrayOutputStream() {
		randomBytes = DEFAULT_MAX_RANDOM_BYTES;
	}
	
	public GzipBreachHandlerByteArrayOutputStream(int randomBytes) {
		this.randomBytes = randomBytes;
	}
	
	@Override
	public synchronized byte[] toByteArray() {
		
		
		var randomBytes = new byte[this.randomBytes];
		
		// we cannot have 0 in random bytes that would mean null termination of a segment as per gzip spec 
		// leading to failed inflate operation
		// Note: byte 256 is 0 when interpreted as a byte value (avoid it)
		for (int i = 0; i < randomBytes.length; ++i) {
			randomBytes[i] = randomRangeRandom(1, 255);
		}
		
		// construct data array for header + random bytes + gzip proper data
		// randomBytes.length + 1 (file name as per gzip spec + 1 for null termination)
		// count gives original file (header + proper gzip data)
		// finally we header + random bytes + gzip data
		var dataArray = new byte[randomBytes.length + 1 + count];
		
		// copies all the headers into the data array
		System.arraycopy(buf, 0, dataArray, 0, 10); // 0 - 9
		
		dataArray[3] = (byte) 8; // marks 4th byte as flag value 8 i.e. we are providing a null byte (0) terminated file name (randomBytes)
		
		// copies random bytes into data array from the 11th byte i.e. right after the header
		System.arraycopy(randomBytes, 0, dataArray, 10, randomBytes.length); // 10 - randomBytes length
		
		
		// null terminating the random bytes
		dataArray[10 + randomBytes.length] = (byte)0; // header 10 + random bytes length
		
		// copy proper gzip data into data array
		// count already provides (header + data), we just need to strip header which is fixed at 10
		System.arraycopy(buf, 10, dataArray, 10 + randomBytes.length + 1, count - 10);
		
		// all goes well data array has 
		// modified header (adding value 8 for file name flag)
		// adding random bytes + null termination
		// proper gzip data
		// from [header, data] => to [header (4th byte set to value 8 marking filename is present), random data, null termination, data]
		return dataArray;
	}
	
	private byte randomRangeRandom(int start, int end) {
	    int number = secureRandom.nextInt((end - start) + 1) + start; 
	    return (byte) number;
	}
	
}
