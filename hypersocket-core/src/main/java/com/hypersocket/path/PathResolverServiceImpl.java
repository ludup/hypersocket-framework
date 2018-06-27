package com.hypersocket.path;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class PathResolverServiceImpl implements PathResolverService {

	
	List<PathResolver> resolvers = new ArrayList<PathResolver>();
	
	
	@Override
	public void registerResolver(PathResolver resolver) {
		resolvers.add(resolver);
	}
	
	public InputStream resolvePath(String path, boolean enableSystem) throws IOException {
		
		for(PathResolver resolver : resolvers) {
			if(!enableSystem && resolver.isSystem()) {
				continue;
			}
			try {
				return resolver.resolvePath(path);
			} catch(FileNotFoundException e) {
			
			}
		}
		
		throw new FileNotFoundException(String.format("%s could not be resolved", path));
	}
}
