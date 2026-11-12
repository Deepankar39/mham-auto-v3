package com.mhamauto.db;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public final class DbPoolLifecycle implements ServletContextListener {
    @Override public void contextDestroyed(ServletContextEvent event) { DbPool.shutdown(); }
}
