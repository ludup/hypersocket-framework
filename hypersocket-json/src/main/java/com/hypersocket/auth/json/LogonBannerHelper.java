package com.hypersocket.auth.json;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class LogonBannerHelper {

	public static final PolicyFactory HTML_SANITIZE_POLICY = allowCommonInlineFormattingElements()
			.and(allowCommonBlockElements());

	public static PolicyFactory allowCommonInlineFormattingElements() {
		return new HtmlPolicyBuilder()
				.allowElements("b", "i", "u", "sup", "sub", "strong",	"big", "small", "br", "span", "em")
				.toFactory();

	}

	public static PolicyFactory allowCommonBlockElements() {
		return new HtmlPolicyBuilder()
				.allowElements("p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote")
				.toFactory();
	}
}
