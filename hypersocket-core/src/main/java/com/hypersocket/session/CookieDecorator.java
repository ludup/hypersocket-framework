package com.hypersocket.session;

import javax.servlet.http.Cookie;

public interface CookieDecorator {

	Cookie decorate(Cookie cookie);
}
