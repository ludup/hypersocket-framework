// ========================================================================
// Copyright (c) 1997-2009 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses.
// ========================================================================

package com.hypersocket.netty.log;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.Principal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.netty.HttpRequestServletWrapper;
import com.hypersocket.netty.HttpResponseServletWrapper;

/**
 * This {@link RequestLog} implementation outputs logs in the pseudo-standard
 * NCSA common log format. Configuration options allow a choice between the
 * standard Common Log Format (as used in the 3 log format) and the Combined Log
 * Format (single log format). This log format can be output by most web
 * servers, and almost all web log analysis software can understand these
 * formats.
 *
 * @org.apache.xbean.XBean element="ncsaLog"
 */

/* ------------------------------------------------------------ */
/**
 */
public class NCSARequestLog implements Closeable
{
    public static final String __LINE_SEPARATOR = System.lineSeparator();

    public final static String REFERER= "Referer";
    public final static String USER_AGENT= "User-Agent";
    public final static String X_FORWARDED_FOR = "X-Forwarded-For";
    
    		
    private static final Logger LOG = LoggerFactory.getLogger(NCSARequestLog.class);

    private String _filename;
    private boolean _extended;
    private boolean _append;
    private int _retainDays;
    private boolean _closeOut;
    private boolean _preferProxiedForAddress;
    private String _logDateFormat = "dd/MMM/yyyy:HH:mm:ss Z";
    private String _filenameDateFormat = null;
    private Locale _logLocale = Locale.getDefault();
    private String _logTimeZone = "GMT";
    private String[] _ignorePaths;
    private boolean _logLatency = false;
    private boolean _logCookies = false;
    private boolean _logServer = false;
    private boolean _logDispatch = false;

    private transient OutputStream _out;
    private transient OutputStream _fileOut;
    private transient DateCache _logDateCache;
    private transient Map<String, String> _ignorePathMap;
    private transient Writer _writer;

    /* ------------------------------------------------------------ */
    /**
     * Create request log object with default settings.
     */
    public NCSARequestLog()
    {
        _extended = true;
        _append = true;
        _retainDays = 31;
    }

    /* ------------------------------------------------------------ */
    /**
     * Create request log object with specified output file name.
     * 
     * @param filename the file name for the request log.
     *                 This may be in the format expected
     *                 by {@link RolloverFileOutputStream}
     */
    public NCSARequestLog(String filename)
    {
        _extended = true;
        _append = true;
        _retainDays = 31;
        setFilename(filename);
    }

    /* ------------------------------------------------------------ */
    /**
     * Set the output file name of the request log.
     * The file name may be in the format expected by
     * {@link RolloverFileOutputStream}.
     * 
     * @param filename file name of the request log
     *                
     */
    public void setFilename(String filename)
    {
        if (filename != null)
        {
            filename = filename.trim();
            if (filename.length() == 0)
                filename = null;
        }
        _filename = filename;
    }

    /* ------------------------------------------------------------ */
    /**
     * Retrieve the output file name of the request log.
     * 
     * @return file name of the request log
     */
    public String getFilename()
    {
        return _filename;
    }

    /* ------------------------------------------------------------ */
    /**
     * Retrieve the file name of the request log with the expanded
     * date wildcard if the output is written to the disk using
     * {@link RolloverFileOutputStream}.
     * 
     * @return file name of the request log, or null if not applicable
     */
    public String getDatedFilename()
    {
        if (_fileOut instanceof RolloverFileOutputStream)
            return ((RolloverFileOutputStream)_fileOut).getDatedFilename();
        return null;
    }

    /* ------------------------------------------------------------ */
    /**
     * Set the timestamp format for request log entries in the file.
     * If this is not set, the pre-formated request timestamp is used.
     * 
     * @param format timestamp format string 
     */
    public void setLogDateFormat(String format)
    {
        _logDateFormat = format;
    }

    /* ------------------------------------------------------------ */
    /**
     * Retrieve the timestamp format string for request log entries.
     * 
     * @return timestamp format string.
     */
    public String getLogDateFormat()
    {
        return _logDateFormat;
    }

    /* ------------------------------------------------------------ */
    /**
     * Set the locale of the request log.
     * 
     * @param logLocale locale object
     */
    public void setLogLocale(Locale logLocale)
    {
        _logLocale = logLocale;
    }

    /* ------------------------------------------------------------ */
    /**
     * Retrieve the locale of the request log.
     * 
     * @return locale object
     */
    public Locale getLogLocale()
    {
        return _logLocale;
    }

    /* ------------------------------------------------------------ */
    /**
     * Set the timezone of the request log.
     * 
     * @param tz timezone string
     */
    public void setLogTimeZone(String tz)
    {
        _logTimeZone = tz;
    }

    /* ------------------------------------------------------------ */
    /**
     * Retrieve the timezone of the request log.
     * 
     * @return timezone string
     */
    public String getLogTimeZone()
    {
        return _logTimeZone;
    }

    /* ------------------------------------------------------------ */
    /**
     * Set the number of days before rotated log files are deleted.
     * 
     * @param retainDays number of days to keep a log file
     */
    public void setRetainDays(int retainDays)
    {
        _retainDays = retainDays;
    }

    /* ------------------------------------------------------------ */
    /**
     * Retrieve the number of days before rotated log files are deleted.
     * 
     * @return number of days to keep a log file
     */
    public int getRetainDays()
    {
        return _retainDays;
    }

    /* ------------------------------------------------------------ */
    /**
     * Set the extended request log format flag.
     * 
     * @param extended true - log the extended request information,
     *                 false - do not log the extended request information
     */
    public void setExtended(boolean extended)
    {
        _extended = extended;
    }

    /* ------------------------------------------------------------ */
    /**
     * Retrieve the extended request log format flag.
     * 
     * @return value of the flag
     */
    public boolean isExtended()
    {
        return _extended;
    }

    /* ------------------------------------------------------------ */
    /**
     * Set append to log flag.
     * 
     * @param append true - request log file will be appended after restart,
     *               false - request log file will be overwritten after restart
     */
    public void setAppend(boolean append)
    {
        _append = append;
    }

    /* ------------------------------------------------------------ */
    /**
     * Retrieve append to log flag.
     * 
     * @return value of the flag
     */
    public boolean isAppend()
    {
        return _append;
    }

    /* ------------------------------------------------------------ */
    /**
     * Set request paths that will not be logged.
     * 
     * @param ignorePaths array of request paths
     */
    public void setIgnorePaths(String[] ignorePaths)
    {
        _ignorePaths = ignorePaths;
    }

    /* ------------------------------------------------------------ */
    /**
     * Retrieve the request paths that will not be logged.
     * 
     * @return array of request paths
     */
    public String[] getIgnorePaths()
    {
        return _ignorePaths;
    }

    /* ------------------------------------------------------------ */
    /**
     * Controls logging of the request cookies.
     * 
     * @param logCookies true - values of request cookies will be logged,
     *                   false - values of request cookies will not be logged
     */
    public void setLogCookies(boolean logCookies)
    {
        _logCookies = logCookies;
    }

    /* ------------------------------------------------------------ */
    /**
     * Retrieve log cookies flag
     * 
     * @return value of the flag
     */
    public boolean getLogCookies()
    {
        return _logCookies;
    }

    /* ------------------------------------------------------------ */
    /**
     * Controls logging of the request hostname.
     * 
     * @param logServer true - request hostname will be logged,
     *                  false - request hostname will not be logged
     */
    public void setLogServer(boolean logServer)
    {
        _logServer = logServer;
    }

    /* ------------------------------------------------------------ */
    /**
     * Retrieve log hostname flag.
     * 
     * @return value of the flag
     */
    public boolean getLogServer()
    {
        return _logServer;
    }

    /* ------------------------------------------------------------ */
    /**
     * Controls logging of request processing time.
     * 
     * @param logLatency true - request processing time will be logged
     *                   false - request processing time will not be logged
     */
    public void setLogLatency(boolean logLatency)
    {
        _logLatency = logLatency;
    }

    /* ------------------------------------------------------------ */
    /**
     * Retrieve log request processing time flag.
     * 
     * @return value of the flag
     */
    public boolean getLogLatency()
    {
        return _logLatency;
    }

    /* ------------------------------------------------------------ */
    /**
     * Controls whether the actual IP address of the connection or
     * the IP address from the X-Forwarded-For header will be logged.
     * 
     * @param preferProxiedForAddress true - IP address from header will be logged,
     *                                false - IP address from the connection will be logged
     */
    public void setPreferProxiedForAddress(boolean preferProxiedForAddress)
    {
        _preferProxiedForAddress = preferProxiedForAddress;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * Retrieved log X-Forwarded-For IP address flag.
     * 
     * @return value of the flag
     */
    public boolean getPreferProxiedForAddress()
    {
        return _preferProxiedForAddress;
    }

    /* ------------------------------------------------------------ */
    /**
     * Set the log file name date format.
     * @see RolloverFileOutputStream#RolloverFileOutputStream(String, boolean, int, TimeZone, String, String)
     * 
     * @param logFileDateFormat format string that is passed to {@link RolloverFileOutputStream}
     */
    public void setFilenameDateFormat(String logFileDateFormat)
    {
        _filenameDateFormat = logFileDateFormat;
    }

    /* ------------------------------------------------------------ */
    /**
     * Retrieve the file name date format string.
     * 
     * @return the log File Date Format
     */
    public String getFilenameDateFormat()
    {
        return _filenameDateFormat;
    }

    /* ------------------------------------------------------------ */
    /** 
     * Controls logging of the request dispatch time
     * 
     * @param value true - request dispatch time will be logged
     *              false - request dispatch time will not be logged
     */
    public void setLogDispatch(boolean value)
    {
        _logDispatch = value;
    }

    /* ------------------------------------------------------------ */
    /**
     * Retrieve request dispatch time logging flag
     * 
     * @return value of the flag
     */
    public boolean isLogDispatch()
    {
        return _logDispatch;
    }

    /* ------------------------------------------------------------ */
    /**
     * Writes the request and response information to the output stream.
     */
    public void log(HttpRequestServletWrapper request, HttpResponseServletWrapper response)
    {
        try
        {
            if (_ignorePathMap != null && _ignorePathMap.get(request.getRequestURI()) != null)
                return;

            if (_fileOut == null)
                return;

            StringBuilder buf= new StringBuilder(256);

            if (_logServer)
            {
                buf.append(request.getServerName());
                buf.append(' ');
            }

            String addr = null;
            if (_preferProxiedForAddress)
            {
                addr = request.getHeader(X_FORWARDED_FOR);
            }

            if (addr == null)
                addr = request.getRemoteAddr();

            buf.append(addr);
            buf.append(" - ");
            Principal authentication=request.getUserPrincipal();
            if (authentication != null)
                buf.append(authentication.getName());
            else
                buf.append(" - ");

            buf.append(" [");
            if (_logDateCache != null)
                buf.append(_logDateCache.format(request.getTimestamp()));
            else
                buf.append(request.getTimestamp().toString());

            buf.append("] \"");
            buf.append(request.getMethod());
            buf.append(' ');
            buf.append(request.getRequestURI());
            buf.append(' ');
            buf.append(request.getProtocol());
            buf.append("\" ");
//            if (response.is.getAsyncContinuation().isInitial())
//            {
                int status = response.getStatus();
                if (status <= 0)
                    status = 404;
                buf.append((char)('0' + ((status / 100) % 10)));
                buf.append((char)('0' + ((status / 10) % 10)));
                buf.append((char)('0' + (status % 10)));
//            }
//            else
//                buf.append("Async");

            String responseLengthStr = response.getHeader("Content-Length");
			long responseLength = responseLengthStr == null ? -1 : Long.parseLong(responseLengthStr);
            if (responseLength >= 0)
            {
                buf.append(' ');
                if (responseLength > 99999)
                    buf.append(responseLength);
                else
                {
                    if (responseLength > 9999)
                        buf.append((char)('0' + ((responseLength / 10000) % 10)));
                    if (responseLength > 999)
                        buf.append((char)('0' + ((responseLength / 1000) % 10)));
                    if (responseLength > 99)
                        buf.append((char)('0' + ((responseLength / 100) % 10)));
                    if (responseLength > 9)
                        buf.append((char)('0' + ((responseLength / 10) % 10)));
                    buf.append((char)('0' + (responseLength) % 10));
                }
                buf.append(' ');
            }
            else
                buf.append(" - ");

            
            if (_extended)
                logExtended(request, response, buf);

            if (_logCookies)
            {
                Cookie[] cookies = request.getCookies();
                if (cookies == null || cookies.length == 0)
                    buf.append(" -");
                else
                {
                    buf.append(" \"");
                    for (int i = 0; i < cookies.length; i++)
                    {
                        if (i != 0)
                            buf.append(';');
                        buf.append(cookies[i].getName());
                        buf.append('=');
                        buf.append(cookies[i].getValue());
                    }
                    buf.append('\"');
                }
            }

            if (_logDispatch || _logLatency)
            {
                long now = System.currentTimeMillis();

                if (_logDispatch)
                {   
                    long d = request.getTimestamp().getTime();
                    buf.append(' ');
                    buf.append(now - (d==0 ? request.getTimestamp().getTime():d));
                }

                if (_logLatency)
                {
                    buf.append(' ');
                    buf.append(now - response.getTimestamp().getTime());
                }
            }

            buf.append(__LINE_SEPARATOR);
            String log = buf.toString();
            synchronized(this)
            {
                if (_writer==null)
                    return;
                _writer.write(log);
                _writer.flush();
            }
        }
        catch (IOException e)
        {
            LOG.warn("Failed to log.", e);
        }

    }

    /* ------------------------------------------------------------ */
    /**
     * Writes extended request and response information to the output stream.
     * 
     * @param request request object
     * @param response response object
     * @param b StringBuilder to write to
     * @throws IOException
     */
    protected void logExtended(HttpRequestServletWrapper request,
                               HttpResponseServletWrapper response,
                               StringBuilder b) throws IOException
    {
        String referer = request.getHeader(REFERER);
        if (referer == null)
            b.append("\"-\" ");
        else
        {
            b.append('"');
            b.append(referer);
            b.append("\" ");
        }

        String agent = request.getHeader(USER_AGENT);
        if (agent == null)
            b.append("\"-\" ");
        else
        {
            b.append('"');
            b.append(agent);
            b.append('"');
        }
    }

    /* ------------------------------------------------------------ */
    /**
     * Set up request logging and open log file.
     */
    public void start() throws Exception
    {
        if (_logDateFormat != null)
        {
            _logDateCache = new DateCache(_logDateFormat,_logLocale, _logTimeZone);
        }

        if (_filename != null)
        {
            _fileOut = new RolloverFileOutputStream(_filename,_append,_retainDays,TimeZone.getTimeZone(_logTimeZone),_filenameDateFormat,null);
            _closeOut = true;
            LOG.info("Opened " + getDatedFilename());
        }
        else
            _fileOut = System.err;

        _out = _fileOut;

        if (_ignorePaths != null && _ignorePaths.length > 0)
        {
            _ignorePathMap = new HashMap<>();
            for (int i = 0; i < _ignorePaths.length; i++)
                _ignorePathMap.put(_ignorePaths[i],_ignorePaths[i]);
        }
        else
            _ignorePathMap = null;

        _writer = new OutputStreamWriter(_out);
    }

    /* ------------------------------------------------------------ */
    /**
     * Close the log file and perform cleanup.
     */
    @Override
    public void close() throws IOException
    {
        synchronized (this)
        {
            try
            {
                if (_writer != null)
                    _writer.flush();
            }
            catch (IOException e)
            {
                LOG.trace("Failed to flush on close.", e);
            }
            if (_out != null && _closeOut)
                try
                {
                    _out.close();
                }
                catch (IOException e)
                {
                    LOG.trace("Failed to close.", e);
                }

            _out = null;
            _fileOut = null;
            _closeOut = false;
            _logDateCache = null;
            _writer = null;
        }
    }
}