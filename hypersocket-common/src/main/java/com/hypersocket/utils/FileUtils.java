package com.hypersocket.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {

	static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd MMM yyyy HH:mm");

	public static String convertBackslashToForwardSlash(String str) {
		return str.replace('\\', '/');
	}
	
	public static String checkStartsWithSlash(String str) {
		if (str.startsWith("/")) {
			return str;
		} else {
			return "/" + str;
		}
	}

	public static String checkStartsWithNoSlash(String str) {
		if (str.startsWith("/")) {
			return str.substring(1);
		} else {
			return str;
		}
	}

	public static String checkEndsWithSlash(String str) {
		if (str.endsWith("/")) {
			return str;
		} else {
			return str + "/";
		}
	}

	public static String checkEndsWithNoSlash(String str) {
		if (str.endsWith("/")) {
			return str.substring(0, str.length() - 1);
		} else {
			return str;
		}
	}

	public static String stripParentPath(String rootPath, String path)
			throws IOException {
		path = checkEndsWithSlash(path);
		rootPath = checkEndsWithSlash(rootPath);
		if (!path.startsWith(rootPath)) {
			throw new IOException(path + " is not a child path of " + rootPath);
		} else {
			return checkEndsWithNoSlash(path.substring(rootPath.length()));
		}
	}
	
	public static String stripFirstPathElement(String path){
		int idx;
		if ((idx = path.indexOf('/',1)) > -1) {
			return path.substring(idx);
		} else {
			return path;
		}
	}

	public static String stripLastPathElement(String path){
		int idx;
		String thePath = checkEndsWithNoSlash(path);
		if ((idx = thePath.lastIndexOf('/')) > -1) {
			return checkEndsWithSlash(thePath.substring(0, idx));
		} else {
			return path;
		}
	}
	
	public static String firstPathElement(String mountPath) {
		int idx;
		mountPath = FileUtils.checkStartsWithNoSlash(mountPath);
		if ((idx = mountPath.indexOf('/')) > -1) {
			return mountPath.substring(0, idx);
		} else {
			return mountPath;
		}
	}

	public static void closeQuietly(InputStream in) {
		try {
			if (in != null) {
				in.close();
			}
		} catch (IOException e) {
		}
	}

	public static void closeQuietly(OutputStream out) {
		try {
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
		}
	}

	public static void deleteFolder(File folder) {

		if (folder != null) {
			File[] files = folder.listFiles();
			if (files != null) {
				for (File f : files) {
					if (f.isDirectory()) {
						deleteFolder(f);
					} else {
						f.delete();
					}
				}
			}
			folder.delete();
		}
	}

	public static String formatSize(long size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size
				/ Math.pow(1024, digitGroups))
				+ " " + units[digitGroups];
	}

	public static String formatLastModified(long lastModifiedTime) {
		return dateFormat.format(new Date(lastModifiedTime));
	}
	
	public static String getParentPath(String originalFilename) {
		
		originalFilename = checkEndsWithNoSlash(originalFilename);

		int idx;
		if ((idx = originalFilename.lastIndexOf('/')) > -1) {
			originalFilename = originalFilename.substring(0, idx+1);
		} else if ((idx = originalFilename.lastIndexOf('\\')) > -1) {
			originalFilename = originalFilename.substring(0, idx+1);
		}

		return originalFilename;
	}

	public static String stripPath(String originalFilename) {

		originalFilename = checkEndsWithNoSlash(originalFilename);

		int idx;
		if ((idx = originalFilename.lastIndexOf('/')) > -1) {
			originalFilename = originalFilename.substring(idx + 1);
		} else if ((idx = originalFilename.lastIndexOf('\\')) > -1) {
			originalFilename = originalFilename.substring(idx + 1);
		}

		return originalFilename;
	}

	public static String lastPathElement(String originalFilename) {
		int idx;
		originalFilename = checkEndsWithNoSlash(originalFilename);
		if ((idx = originalFilename.lastIndexOf('/')) > -1) {
			return originalFilename.substring(idx+1);
		} else {
			return originalFilename;
		}
	}

	public static boolean hasParents(String sourcePath) {
		return sourcePath.indexOf('/') > -1;
	}

	public static List<String> generatePaths(String fullpath) {
		
		List<String> paths = new ArrayList<String>();
		String tmp = "/";
		for(String p : fullpath.split("/")) {
			if(p.equals("")) {
				continue;
			}
			tmp += p + "/";
			paths.add(tmp);
		}
		return paths;
	}
	
	/**
	 * From http://stackoverflow.com/questions/12090598/parsing-human-readable-filesizes-in-java-to-bytes
	 * @param filesize
	 * @return
	 */
	public static long toBytes(String filesize) {
	    long returnValue = -1;
	    Pattern patt = Pattern.compile("([0-9]+([,.][0-9]+)?)([TGMK]B)", Pattern.CASE_INSENSITIVE);
	    Matcher matcher = patt.matcher(filesize);
	    Map<String, Integer> powerMap = new HashMap<String, Integer>();
	    powerMap.put("TB", 4);
	    powerMap.put("GB", 3);
	    powerMap.put("MB", 2);
	    powerMap.put("KB", 1);
	    if (matcher.find()) {
	      String number = matcher.group(1);
	      String unit = matcher.group(3);
	      int pow = powerMap.get(unit.toUpperCase());
	      BigDecimal bytes = new BigDecimal(number);
	      bytes = bytes.multiply(BigDecimal.valueOf(1024).pow(pow));
	      returnValue = bytes.longValue();
	    }
	    return returnValue;
	}


}
