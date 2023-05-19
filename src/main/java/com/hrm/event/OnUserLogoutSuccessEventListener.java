package com.hrm.event;

import com.hrm.cache.LoggedOutJwtTokenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class OnUserLogoutSuccessEventListener implements ApplicationListener<OnUserLogoutSuccessEvent> {

    private static final Logger logger = LoggerFactory.getLogger(OnUserLogoutSuccessEventListener.class);

    private final LoggedOutJwtTokenCache tokenCache;

    @Autowired
    public OnUserLogoutSuccessEventListener(LoggedOutJwtTokenCache tokenCache) {
        this.tokenCache = tokenCache;
    }

    @Override
    public void onApplicationEvent(OnUserLogoutSuccessEvent event) {
        logger.info("Log out success event received for user {}", event.getEmail());
        tokenCache.markLogoutEventForToken(event);
    }
}
