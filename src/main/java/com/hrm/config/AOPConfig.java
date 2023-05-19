package com.hrm.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Aspect
@Configuration
public class AOPConfig {
    private static final Logger logger = LoggerFactory.getLogger(AOPConfig.class);

    @Before("within(com.hrm.controller..*)")
    public void beforeAdvice(JoinPoint joinPoint) {
        logger.info("LOGGER - enter method: {} () with argument[s] = {} ", joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()));
    }

    @After("within(com.hrm.controller..*)")
    public void afterAdvice(JoinPoint joinPoint) {
        logger.info("LOGGER - Method signature after advice: " + joinPoint.getSignature().getName());
    }

    @AfterThrowing(pointcut = "execution(* com.hrm.service.*.*(..)) || execution(* com.hrm.repository.*.*(..))", throwing = "exception")
    public void afterThrowingAdvice(JoinPoint joinPoint, Exception exception) {
        logger.error("LOGGER - Exception in {}.{}() with cause = {}", joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(), exception.getMessage());
    }
}
