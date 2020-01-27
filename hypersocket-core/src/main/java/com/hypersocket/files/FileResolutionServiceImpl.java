package com.hypersocket.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.hypersocket.realm.Realm;
import com.hypersocket.utils.FileUtils;

@Service
public class FileResolutionServiceImpl implements FileResolutionService {

	private List<FileResolver> fileSources = new ArrayList<FileResolver>();
	
	@PostConstruct
	private void postConstruct() {
		fileSources.add(new SystemFileSource());
	}
	
	@Override
	public void registerFileSource(FileResolver source) {
		fileSources.add(source);
		Collections.sort(fileSources, new Comparator<FileResolver>() {

			@Override
			public int compare(FileResolver o1, FileResolver o2) {
				return o1.getWeight().compareTo(o2.getWeight());
			}
		});
	}
	
	@Override
	public boolean fileExists(String path, Realm realm, boolean includeSystem) {
		for(FileResolver source : fileSources) {
			if(includeSystem || !source.isSystem()) {
				if(source.fileExists(path, realm)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected FileResolver resolveSource(String path, Realm realm, boolean includeSystem) throws FileNotFoundException {
		for(FileResolver source : fileSources) {
			if(includeSystem || !source.isSystem()) {
				if(source.fileExists(path, realm)) {
					return source;
				}
			}
		}
		throw new FileNotFoundException(String.format("%s is not a valid path", path));
	}
	
	@Override
	public InputStream getInputStream(String path, Realm realm, boolean includeSystem) throws IOException {
		return resolveSource(path, realm, includeSystem).getInputStream(path, realm);
	}
	
	@Override
	public OutputStream getOutputStream(String path, Realm realm, boolean includeSystem) throws IOException {
		return resolveSource(FileUtils.getParentPath(path), realm, includeSystem).getOutputStream(path, realm);
	}


	class SystemFileSource implements FileResolver {
		
		@Override
		public boolean isSystem() {
			return true;
		}
		
		@Override
		public Integer getWeight() {
			return Integer.MAX_VALUE;
		}

		@Override
		public boolean fileExists(String path, Realm realm) {
			return new File(path).exists();
		}
		
		@Override
		public InputStream getInputStream(String path, Realm realm) throws FileNotFoundException {
			return new FileInputStream(path);
		}
		
		@Override
		public OutputStream getOutputStream(String path, Realm realm) throws FileNotFoundException {
			return new FileOutputStream(path);
		}
	}
}
