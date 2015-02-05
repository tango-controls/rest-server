package org.tango.web.server.js;

import com.sun.corba.se.spi.protocol.RequestDispatcherDefault;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.RequestWrapper;
import java.io.IOException;
import java.util.*;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 05.02.2015
 */
public class EntryPoint extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().replaceAll("js-entry-point/", "");
        RequestDispatcher dispatcher = request.getRequestDispatcher(path);
        if(dispatcher == null) throw new ServletException("Unknown path: " + path);

        ServletRequestWrapper requestWrapper = new ServletRequestWrapper(request);

//        request.setAttribute("Authorization", decToken);
        requestWrapper.addHeader("Authorization", "xxx");

        dispatcher.forward(requestWrapper,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    //TODO add authentication
    private static class ServletRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper{
        private final Map<String,String> headerMap;
        public ServletRequestWrapper(HttpServletRequest request) {
            super(request);
            headerMap = new HashMap<>();
        }

        public void addHeader(String name, String value) {
            headerMap.put(name, value);
        }

        public Enumeration<String> getHeaderNames() {
            HttpServletRequest request = (HttpServletRequest) getRequest();
            List<String> list = new ArrayList<>();
            for (Enumeration e = request.getHeaderNames(); e.hasMoreElements();) {
                list.add(e.nextElement().toString());
            }

            for (String s : headerMap.keySet()) {
                list.add(s);
            }
            return Collections.enumeration(list);
        }

        public String getHeader(String name) {
            Object value;
            if ((value = headerMap.get("" + name)) != null)
                return value.toString();
            else
                return ((HttpServletRequest) getRequest()).getHeader(name);
        }
    }
}
