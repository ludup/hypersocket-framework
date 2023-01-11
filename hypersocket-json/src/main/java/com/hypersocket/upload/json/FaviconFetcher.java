package com.hypersocket.upload.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.http.HttpUtilsImpl;

@Component
public class FaviconFetcher {
	
	static Logger log = LoggerFactory.getLogger(FaviconFetcher.class);
	
	private TikaConfig tika;
	
	// https://github.com/element-io/svg-tags/blob/master/lib/svg-tags.json
	static final String SVG_TAGS_CSV = "a,"
			+ "	altGlyph,"
			+ "	altGlyphDef,"
			+ "	altGlyphItem,"
			+ "	animate,"
			+ "	animateColor,"
			+ "	animateMotion,"
			+ "	animateTransform,"
			+ "	circle,"
			+ "	clipPath,"
			+ "	color-profile,"
			+ "	cursor,"
			+ "	defs,"
			+ "	desc,"
			+ "	ellipse,"
			+ "	feBlend,"
			+ "	feColorMatrix,"
			+ "	feComponentTransfer,"
			+ "	feComposite,"
			+ "	feConvolveMatrix,"
			+ "	feDiffuseLighting,"
			+ "	feDisplacementMap,"
			+ "	feDistantLight,"
			+ "	feFlood,"
			+ "	feFuncA,"
			+ "	feFuncB,"
			+ "	feFuncG,"
			+ "	feFuncR,"
			+ "	feGaussianBlur,"
			+ "	feImage,"
			+ "	feMerge,"
			+ "	feMergeNode,"
			+ "	feMorphology,"
			+ "	feOffset,"
			+ "	fePointLight,"
			+ "	feSpecularLighting,"
			+ "	feSpotLight,"
			+ "	feTile,"
			+ "	feTurbulence,"
			+ "	filter,"
			+ "	font,"
			+ "	font-face,"
			+ "	font-face-format,"
			+ "	font-face-name,"
			+ "	font-face-src,"
			+ "	font-face-uri,"
			+ "	foreignObject,"
			+ "	g,"
			+ "	glyph,"
			+ "	glyphRef,"
			+ "	hkern,"
			+ "	image,"
			+ "	line,"
			+ "	linearGradient,"
			+ "	marker,"
			+ "	mask,"
			+ "	metadata,"
			+ "	missing-glyph,"
			+ "	mpath,"
			+ "	path,"
			+ "	pattern,"
			+ "	polygon,"
			+ "	polyline,"
			+ "	radialGradient,"
			+ "	rect,"
			/* + "	script," */ // Forced remove potential XSS
			+ "	set,"
			+ "	stop,"
			+ "	style,"
			+ "	svg,"
			+ "	switch,"
			+ "	symbol,"
			+ "	text,"
			+ "	textPath,"
			+ "	title,"
			+ "	tref,"
			+ "	tspan,"
			+ "	use,"
			+ "	view,"
			+ "	vkern";
	
	private static final Set<String> SVG_TAG_SET = new HashSet<>();
	
	static {
		
		SVG_TAG_SET.addAll(
				Arrays.asList(SVG_TAGS_CSV.split(","))
				.stream()
				.map(t -> t.trim())
				.collect(Collectors.toList())
				);
	}
	
	public static final String MIME_PNG = "image/png";
	public static final String MIME_GIF = "image/gif";
	public static final String MIME_ICO_VND = "image/vnd.microsoft.icon";
	public static final String MIME_ICO = "image/x-icon";
	public static final String MIME_SVG = "image/svg+xml";
	public static final String MIME_WEBP = "image/webp";
	
	@Autowired
	private HttpUtilsImpl httpUtils; 
	
	private BiFunction<Integer, List<FavIconInfo>, Optional<FavIconInfo>> filterOnSize = (size, favIcons) -> {
		// look for svg first then by size
		return favIcons.stream().filter(icon -> (icon.size() == size) || icon.isSVG()).findFirst();
	};
	
	private Function<List<FavIconInfo>, Optional<FavIconInfo>> filterForBestResolution = (favIcons) -> {
		/**
		 * 1. svg
		 * 2. apple
		 * 3. normal 
		 */
		FavIconInfo appleToucIcon = null;
		FavIconInfo normalIcon = null;
		FavIconInfo svgIcon = null;
		
		// try to find high resolution apple icon else normal icon
		for (FavIconInfo icon : favIcons) {
			
			if (svgIcon == null && icon.isSVG()) {
				svgIcon = icon;
				continue;
			}
			
			if (appleToucIcon == null && icon.isApple()) {
				appleToucIcon = icon;
				continue;
			}
			
			if (normalIcon == null && !icon.isApple()) {
				normalIcon = icon;
				continue;
			}
			
			if (normalIcon != null && appleToucIcon != null) {
				break;
			}
		}
		
		return Optional.ofNullable(svgIcon != null ? svgIcon : (appleToucIcon != null ? appleToucIcon : normalIcon));
	};
	
	@PostConstruct
	private void postConstruct() {
		try {
			tika = new TikaConfig();
		} catch (Exception e) {
			log.error("Tika config failed to create, content detection will not work.", e);
		}
	}
	
	public FavIcon favicon(String hostAddress) {
		return favicon(hostAddress, null);
	}
	
	public FavIcon favicon(String hostAddress, Integer size) {
		try {
			
			FavIcon defaultfavicon = exceptionSafeGetDefaultFavicon(hostAddress);
			
			if (defaultfavicon.iconStream.isPresent()) {
				return defaultfavicon;
			}
			
			String url = String.format("https://%s", hostAddress);
			
			log.info("Fetching favicon for {}", url);
			
			Document doc = Jsoup.connect(url).get();
			
			Elements faviconLinks = doc.select("link[rel*=icon]");
			
			List<FavIconInfo> favIcons = faviconLinks.stream().map(link -> {
				String type = link.attr("rel"); // apple-touch-icon or icon
				String sizes = link.attr("sizes"); //  32x32 16x16
				String href = link.attr("href"); // link to favicon
				
				return new FavIconInfo(type, sizes, href);
				
			})
			.sorted() // sort in descending order
			.collect(Collectors.toList());
			
			Optional<FavIconInfo> finalIcon = size != null 
					? filterOnSize.apply(size, favIcons) : 
						filterForBestResolution.apply(favIcons);
			
			if (finalIcon.isPresent()) {
				log.info("Fetching fav icon from {}", finalIcon.get());
				
				String target = finalIcon.get().href;
				
				if (!target.startsWith("https")) {
					target = url + target;
				}
				return checkAndReturnIconBytes(target);
			}
			
			return new FavIcon(Optional.empty(), Optional.empty());
			
		} catch (IOException io) {
			throw new IllegalStateException(io);
		}
	}
	
	public FavIcon getDefaultFavicon(String hostAddress) {
		try {
			String faviconUrl = String.format("https://%s/favicon.ico", hostAddress);
			
			log.info("Fetching default favicon for {}", faviconUrl);
			
			return checkAndReturnIconBytes(faviconUrl);
			
		} catch (IOException io) {
			throw new IllegalStateException(io);
		}
	}

	private FavIcon checkAndReturnIconBytes(String faviconUrl) throws IOException {
		CloseableHttpResponse response = httpUtils.execute(new HttpGet(faviconUrl), true);
		InputStream iconStream = null;
		
		try {
			
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				log.error("Problem in fetching icon, status not OK");
				return new FavIcon(Optional.empty(), Optional.empty());
			}
			
			HttpEntity entity = response.getEntity();
			
			iconStream = entity.getContent();
			
			byte[] iconBytes = IOUtils.toByteArray(iconStream);
			
			ParseResult parseResult = parseIcoFile(iconBytes);
		
			if (!parseResult.result) {
				throw new IllegalStateException(String.format("Not an ico file for url => %s", faviconUrl));
			}
			
			return new FavIcon(Optional.of(new ByteArrayInputStream(iconBytes)), Optional.of(parseResult.type));

		} finally {
			if (iconStream != null) {
				iconStream.close();
			}
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
			}
		}

	}
	
	private FavIcon exceptionSafeGetDefaultFavicon(String hostAddress) {
		try {
			return getDefaultFavicon(hostAddress);
		} catch (Exception e) {
			log.error("Problem in fetching favicon for host {}", hostAddress, e);
			return new FavIcon(Optional.empty(), Optional.empty());
		}
		
	}
	

	/**
	 * ICO - image/vnd.microsoft.icon image/x-icon
	 * PNG - image/png
	 * GIF - image/gif
	 * 
	 * @param file
	 * @return
	 */
	public ParseResult parseIcoFile(byte[] file) {
		
		InputStream fileStream = null;
		
		try {
			if (tika == null) {
				throw new IllegalStateException("Tika config is null.");
			}
			
			fileStream = new ByteArrayInputStream(file);
			
			MediaType mimetype = tika.getDetector().detect(
			        TikaInputStream.get(fileStream), new Metadata());
			
			   
			String type = mimetype.toString();
			
			if (MIME_PNG.equals(type)) {
				return new ParseResult(true, MIME_PNG);	
			} else if (MIME_GIF.equals(type)) {
				return new ParseResult(true, MIME_GIF);
			} else if (MIME_ICO_VND.equals(type)) {
				return new ParseResult(true, MIME_ICO_VND);
			} else if (MIME_ICO.equals(type)) {
				return new ParseResult(true, MIME_ICO);
			} else if (MIME_WEBP.equals(type)) {
				return new ParseResult(true, MIME_WEBP);
			} else if (isSvgFile(file)) { 
				return new ParseResult(true, MIME_SVG);
			} else {
				return new ParseResult(false, null);
			}

		} catch (Exception e) {
			log.error("Problem in content detection.", e);
			return new ParseResult(false, null);
		} finally {
			if (fileStream != null) {
				try {
					fileStream.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}
	
	/**
	 * We try to parse and find known svg elements and if found
	 * it is good chance it is indeed svg file. 
	 * 
	 * As SVG is an xml file somebody can still play dirty.
	 * 
	 * @param file
	 * @return
	 */
	private boolean isSvgFile(byte[] file) {
		try {
			Document document = Jsoup.parse(new String(file));
			
			Elements allElements = document.body().getAllElements();
			
			var found = new HashSet<Element>();
			
			for (Element element : allElements) {
				
				// jsoup thing wraps everything in a html -> body
				if ("body".equals(element.nodeName())) {
					continue;
				}
				
				if (SVG_TAG_SET.contains(element.nodeName())) {
					found.add(element);
				} else {
					log.error("Found tag {} not in white list.", element.nodeName());
				}
			}
			
			return !found.isEmpty();
			
		} catch (Exception e) {
			log.error("Problem in parsing SVG", e);
			return false;
		}
	}
	
	public static class FavIcon {
		final Optional<InputStream> iconStream;
		final Optional<String> mime;
		
		public FavIcon(Optional<InputStream> iconStream, Optional<String> mime) {
			super();
			this.iconStream = iconStream;
			this.mime = mime;
		}

		public Optional<InputStream> getIconStream() {
			return iconStream;
		}

		public Optional<String> getMime() {
			return mime;
		}

		@Override
		public String toString() {
			return "FavIcon [mime=" + mime + "]";
		}
		
		
	}
	
	public static class ParseResult {
		
		final boolean result;
		final String type;
		
		ParseResult(boolean result, String type) {
			super();
			this.result = result;
			this.type = type;
		}
		
		public boolean isICO() {
			return MIME_ICO.equals(type) || MIME_ICO_VND.equals(type);
		}
		
		public boolean isSVG() {
			return MIME_SVG.equals(type);
		}
		
		public boolean isPNG() {
			return MIME_PNG.equals(type);
		}
		
		public boolean isGIF() {
			return MIME_GIF.equals(type);
		}
		
		public boolean isWebP() {
			return MIME_WEBP.equals(type);
		}
		
	}
	
	private static class FavIconInfo implements Comparable<FavIconInfo>{
		final String type;
		final String sizes;
		final String href;
		
		FavIconInfo(String type, String sizes, String href) {
			this.type = type;
			this.sizes = sizes;
			this.href = href;
		}
		
		boolean isApple() {
			return type != null && type.contains("apple-touch-icon");
		}
		
		boolean isSVG() {
			return (type != null && type.contains(MIME_SVG)) || (href != null & href.endsWith(".svg"));
		}
		
		Integer size() {
			
			if (sizes != null && sizes.contains("x")) {
				String size = sizes.trim().substring(0, sizes.indexOf("x"));
				
				if (NumberUtils.isCreatable(size)) {
					return NumberUtils.createInteger(size);
				}
			}
			
			return 0;
		}

		@Override
		public int compareTo(FavIconInfo other) {
			return other.size().compareTo(this.size());
		}
		
		@Override
		public String toString() {
			return "FavIconInfo [type=" + type + ", sizes=" + sizes + ", href=" + href + "]";
		}
	}

}
