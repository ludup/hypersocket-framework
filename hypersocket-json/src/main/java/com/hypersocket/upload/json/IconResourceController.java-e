package com.hypersocket.upload.json;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.i18n.I18NServiceImpl;
import com.hypersocket.json.ResourceList;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.Resource;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.upload.FileUploadService;
import com.hypersocket.utils.HypersocketUtils;
import com.sshtools.icongenerator.AwesomeIcon;
import com.sshtools.icongenerator.Colors;
import com.sshtools.icongenerator.IconBuilder;
import com.sshtools.icongenerator.IconBuilder.IconShape;
import com.sshtools.icongenerator.IconBuilder.TextCase;
import com.sshtools.icongenerator.java2d.Java2DIconCanvas;

public abstract class IconResourceController<T extends Resource> extends ResourceController {

	
	static Logger log = LoggerFactory.getLogger(LogoController.class);
	static MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
	static Map<String, AwesomeIcon> map = new HashMap<String, AwesomeIcon>();
	
	final static ResourceList<IconEntry> ICON_LIST = new ResourceList<IconEntry>(new ArrayList<IconEntry>());
	
	@Autowired
	FileUploadService fileService; 
	
	static {
		for(AwesomeIcon a : AwesomeIcon.values()) {
			IconEntry e = new IconEntry();
			e.setIcon(String.valueOf(a.character()));
			e.setValue(a.name());
			e.setName(a.name().substring(5).replace("_", " ").toLowerCase());
			ICON_LIST.getResources().add(e);
		}
	}
	
	protected void downloadLogo(String url, String type, T resource, 
			HttpServletRequest request, HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException, IOException,
			ResourceNotFoundException {

		if(HypersocketUtils.isUUID(url)) {
			fileService.downloadURIFile(url, request, response, false);
			return;
		} 
		
		url = HypersocketUtils.urlDecode(url.substring(7));
		String name = resource.getName();
		String ext;
		String spec = "spec";
		String reversed = "";
		for (int i = type.length() - 1; i >= 0; i--) {
			reversed = reversed + type.charAt(i);
		}
		if (type.length() < 5)
			type = type + reversed;

		// Extract file extension
		String[] arr = url.split("_+");
		if (arr.length < 4) {
			throw new ResourceNotFoundException(
					I18NServiceImpl.USER_INTERFACE_BUNDLE, "error.notFound");
		}

		String[] tmp = arr[3].split("\\.");
		arr[3] = tmp[0];
		ext = tmp[1];
		
		// Parse spec
		int size = Integer.parseInt(arr[0]);
		IconShape shape = isAuto(arr[1]) ? IconShape.values()[Math
				.abs(isAutoBasedOnName(arr[1]) ? name.hashCode() : type
						.hashCode())
				% IconShape.values().length] : IconShape.valueOf(arr[1]
				.toUpperCase());

		// Color
		String colText = arr[2];
		if (colText.startsWith("#")) {
			colText = colText.substring(1);
		}
		int rgb = isAuto(colText) ? Colors.MATERIAL
				.color(isAutoBasedOnName(colText) ? name : type) : Integer
				.parseInt(colText, 16);

		String iconText = "";
		String text = URLDecoder.decode(arr[3], "UTF-8");
		AwesomeIcon icon = null;

		String[] words = alphaOnly(name).split("\\s+");
		if (text.equals("autoicon") || text.equals("auto")) {
			// Try first getting an icon by matching words from the resource
			// name
			int iconMatches = 0;
			for (AwesomeIcon a : AwesomeIcon.values()) {
				final String ename = a.name().replace("ICON_", "").replace("_", " ");
				String[] iconWords = ename.split("\\s+");
				int matches = 0;
				for (String w : words) {
					for (String iw : iconWords) {
						if (iw.equalsIgnoreCase(w)) {
							matches++;
							break;
						}
					}
				}
				if (matches > iconMatches) {
					icon = a;
					iconMatches = matches;
				}
			}

			// If we found no matches, pick an icon based on the hash code
			if (icon == null && text.equals("autoicon")) {
				icon = AwesomeIcon.values()[Math.abs(name.hashCode())
						% AwesomeIcon.values().length];
			}
		}
		
		if(text.startsWith("icon")) {
			String iconName = "ICON_" + text.substring(4).replace(" ", "_").toUpperCase();
			if(iconName.length()>5) {
				try {
					icon = AwesomeIcon.valueOf(iconName);
				}
				catch(Exception e) {
					if(log.isErrorEnabled()) {
						log.error("Cannot find awesome icon", e);
					}
				}
			}
		}

		if (icon == null && ( text.equalsIgnoreCase("auto") || text.equalsIgnoreCase("autoname"))) {
			// Text is generated from the resource name
			for (String w : words) {
				if (w.length() > 0)
					iconText = iconText.concat(String.valueOf(w.charAt(0)));
			}
			
			if(iconText.length() < 2 && name.length() > 1) {
				iconText = name.substring(0, 2);
			}
		} else {
			// Fixed
			iconText = text;
		}
		if (iconText.length() > 3) {
			iconText = iconText.substring(0, 3);
		}

		// Build icon
		IconBuilder builder = new IconBuilder().width(size).height(size)
				.shape(shape).textCase(TextCase.UPPER).color(rgb)
				.border(0);
		if (shape == IconShape.ROUNDED) {
			builder.roundRect(size / 4);
		}
		if (icon != null) {
			builder.icon(icon);
		} else
			builder.text(iconText);

		// Draw icon
		BufferedImage image = new BufferedImage((int) builder.width(),
				(int) builder.height(), BufferedImage.TYPE_4BYTE_ABGR);
		new Java2DIconCanvas(builder).draw((Graphics2D) image.getGraphics());

		// Respond
		String contentType = mimeTypesMap.getContentType(spec + "." + ext);
		response.setContentType(contentType);

		ImageIO.write(image, ext.toUpperCase(), response.getOutputStream());
		response.flushBuffer();

	}
	

	static boolean isAuto(String value) {
		return value.equalsIgnoreCase("auto")
				|| value.equalsIgnoreCase("autoname")
				|| value.equalsIgnoreCase("autotype");
	}

	static boolean isAutoBasedOnName(String value) {
		return value.equalsIgnoreCase("auto")
				|| value.equalsIgnoreCase("autoname");
	}

	static boolean isAutoBasedOnType(String value) {
		return value.equalsIgnoreCase("auto")
				|| value.equalsIgnoreCase("autotype");
	}

	static String alphaOnly(String s) {
		StringBuilder b = new StringBuilder();
		for (char c : s.toCharArray()) {
			if (Character.isAlphabetic(c) || Character.isDigit(c) || c == ' '
					|| c == '\t')
				b.append(c);
		}
		return b.toString();
	}
}
