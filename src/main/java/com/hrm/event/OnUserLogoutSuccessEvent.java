package com.hrm.event;

import com.hrm.model.request.LogOutRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
public class OnUserLogoutSuccessEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    private final String email;
    private final String token;
    private final transient LogOutRequest logOutRequest;
    private final Date eventTime;

    public OnUserLogoutSuccessEvent(String email, String token, LogOutRequest logOutRequest) {
	super(email);
	this.email = email;
	this.token = token;
	this.logOutRequest = logOutRequest;
	this.eventTime = Date.from(Instant.now());
    }
}
