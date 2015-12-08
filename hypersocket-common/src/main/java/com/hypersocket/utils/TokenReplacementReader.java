package com.hypersocket.utils;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.List;

public class TokenReplacementReader extends Reader {

	  protected PushbackReader pushbackReader   = null;
	  protected List<ITokenResolver> tokenResolvers    = null;
	  protected StringBuilder  tokenNameBuffer  = new StringBuilder();
	  protected String         tokenValue       = null;
	  protected int            tokenValueIndex  = 0;

	  public TokenReplacementReader(Reader source, List<ITokenResolver> resolvers) {
	    this.pushbackReader = new PushbackReader(source, 2);
	    this.tokenResolvers  = resolvers;
	  }

	  public int read(CharBuffer target) throws IOException {
	    throw new RuntimeException("Operation Not Supported");
	  }

	  public int read() throws IOException {
	    if(this.tokenValue != null){
	      if(this.tokenValueIndex < this.tokenValue.length()){
	        return this.tokenValue.charAt(this.tokenValueIndex++);
	      }
	      if(this.tokenValueIndex == this.tokenValue.length()){
	        this.tokenValue = null;
	        this.tokenValueIndex = 0;
	      }
	    }

	    int data = this.pushbackReader.read();
	    if(data != '$') return data;

	    data = this.pushbackReader.read();
	    if(data != '{'){
	      this.pushbackReader.unread(data);
	      return '$';
	    }
	    this.tokenNameBuffer.delete(0, this.tokenNameBuffer.length());

	    data = this.pushbackReader.read();
	    while(data != '}'){
	      this.tokenNameBuffer.append((char) data);
	      data = this.pushbackReader.read();
	    }

	    for(ITokenResolver r : tokenResolvers) {
    		tokenValue = r.resolveToken(this.tokenNameBuffer.toString());
    		if(tokenValue!=null) {
    			break;
    		}
    	}

	    if(this.tokenValue == null){
	      this.tokenValue = "${"+ this.tokenNameBuffer.toString() + "}";
	    }
	    
	    if(this.tokenValue.length()==0) {
	    	return read();
	    } else {
	    	return this.tokenValue.charAt(this.tokenValueIndex++);
	    }

	  }

	  public int read(char cbuf[]) throws IOException {
	    return read(cbuf, 0, cbuf.length);
	  }

	  public int read(char cbuf[], int off, int len) throws IOException {
	    int charsRead = 0;
	    for(int i=0; i<len; i++){
	        int nextChar = read();
	        if(nextChar == -1) {
	            if(charsRead == 0){
	                charsRead = -1;
	            }
	            break;
	        }
	        charsRead = i + 1;
	        cbuf[off + i] = (char) nextChar;
	      }
	    return charsRead;
	  }

	  public void close() throws IOException {
	    this.pushbackReader.close();
	  }

	  public long skip(long n) throws IOException {
	    throw new RuntimeException("Operation Not Supported");
	  }

	  public boolean ready() throws IOException {
	    return this.pushbackReader.ready();
	  }

	  public boolean markSupported() {
	    return false;
	  }

	  public void mark(int readAheadLimit) throws IOException {
	    throw new RuntimeException("Operation Not Supported");
	  }

	  public void reset() throws IOException {
	    throw new RuntimeException("Operation Not Supported");
	  }
	}
