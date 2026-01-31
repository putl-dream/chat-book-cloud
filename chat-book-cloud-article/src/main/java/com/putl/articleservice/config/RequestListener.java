package com.putl.articleservice.config;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServletRequest;


@WebListener
public class RequestListener implements ServletRequestListener {

    @Override
    public void requestDestroyed(ServletRequestEvent servletRequestEvent) {
    }

    @Override
    public void requestInitialized(ServletRequestEvent servletRequestEvent) {
        ((HttpServletRequest)servletRequestEvent.getServletRequest()).getSession();
    }
}