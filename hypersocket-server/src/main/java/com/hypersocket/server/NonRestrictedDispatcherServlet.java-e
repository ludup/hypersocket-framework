package com.hypersocket.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class NonRestrictedDispatcherServlet extends DispatcherServlet {

	private static final long serialVersionUID = -3511464246012229321L;
	
	public NonRestrictedDispatcherServlet(WebApplicationContext webappContext) {
		super(webappContext);
	}
	/**
     * Override of the default implementation to enable webdav methods
     *
     * @param req
     * @param resp
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doService(req, resp);
        } catch(ServletException e) {
            throw e;
        } catch(IOException e) {
            throw e;
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
}
