package com.hypersocket.export;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.cache.Cache;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;

@Service
public class ExportServiceImpl implements ExportService {

	@Autowired
	private I18NService i18nService;

	@Override
	public void downloadCSV(Realm realm, ExportDataProvider provider, boolean outputHeaders,
			String delimiter, CommonEndOfLineEnum terminate, String wrap, String escapex, String attributes,
			OutputStream out, Locale locale)
			throws AccessDeniedException, UnsupportedEncodingException {
		Set<String> headers = new LinkedHashSet<String>(provider.getHeaders());
		Cache<String, String> i18n = i18nService.getResourceMap(locale);
		Set<String> includeAttributes = new LinkedHashSet<String>();
		if(StringUtils.isNotBlank(attributes))
			includeAttributes.addAll(Arrays.asList(attributes.split(",")));
		for (String attributeName : includeAttributes) {
			headers.add(attributeName);
		}
		ICsvMapWriter mapWriter = null;
		Writer writer = new OutputStreamWriter(out, "UTF-8");
		try {
			String terminateCharacter = terminate.getCharacter();

			final CsvPreference preferences = new CsvPreference.Builder(wrap.charAt(0), delimiter.charAt(0),
					terminateCharacter).surroundingSpacesNeedQuotes(true).build();
			mapWriter = new CsvMapWriter(writer, preferences);

			final CellProcessor[] processors = getProcessors(headers.size());
			if (outputHeaders) {
				List<String> i18nHeaders = new ArrayList<String>();
				for (String header : headers) {
					String txt = i18n.get(header);
					if (StringUtils.isNotBlank(txt)) {
						i18nHeaders.add(txt);
					} else {
						i18nHeaders.add(header);
					}

				}
				mapWriter.writeHeader(i18nHeaders.toArray(new String[0]));
			}
			while (provider.hasNext()) {
				mapWriter.write(provider.next(), headers.toArray(new String[0]), processors);
			}
			mapWriter.flush();
		} catch (IOException e) {
			log.error("Error generating CSV", e);
			throw new IllegalStateException(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(mapWriter);
			IOUtils.closeQuietly(writer);
		}
	}

	private static CellProcessor[] getProcessors(int size) {

		final CellProcessor[] processors = new CellProcessor[size];
		for (int i = 0; i < size; i++) {
			processors[i] = new Optional();

		}
		return processors;
	}

}
