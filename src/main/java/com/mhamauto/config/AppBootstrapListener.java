package com.mhamauto.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/** Fail startup safely if the minimal academic configuration is invalid. */
@WebListener
public final class AppBootstrapListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        event.getServletContext().setAttribute(AppSettings.CONTEXT_ATTRIBUTE, AppSettings.load());
    }
}
