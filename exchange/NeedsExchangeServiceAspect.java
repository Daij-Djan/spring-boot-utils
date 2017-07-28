/**
 @file      Profiler.java
 @author    Dominik Pich
 @date      30/11/2016

Copyright (c) 2016, Sapient GmbH
All rights reserved.

*/
package com.sapient.quickbook.services.ews;

import com.sapient.quickbook.configuration.ProfilerProperties;
import lombok.extern.slf4j.Slf4j;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.exception.service.remote.ServiceRequestException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Aspect
@Slf4j
public class NeedsExchangeServiceAspect {
    @Autowired
    ExchangeProvider exchangeProvider;

    @Around("@annotation(NeedsExchangeService) && execution(* *(..))")
    public Object perform(ProceedingJoinPoint pjp) throws Throwable {
        List<Object> args = new ArrayList<>(Arrays.asList(pjp.getArgs()));
        Object output = null;
        boolean retrying = false;
        boolean ownService = false;
        ExchangeService exchangeService = null;

        if(args.get(args.size()-1) == null) {
            exchangeService = exchangeProvider.retainService();
            args.remove(args.size()-1);
            args.add(exchangeService);
            ownService = true;
        }
        else {
            log.warn("user provided own ExchangeService instance to call " + pjp + " so we wont use the retain/release or retry and renew");
        }

        if(ownService) {
            do {
                try {
                    output = pjp.proceed(args.toArray());
                    retrying = false;
                } catch (ServiceRequestException sre) {
                    //likely a timeout
                    if (!retrying) {
                        log.warn("timeout error performing EWS Method: " + sre + ", method is: " + pjp);
                        log.warn("getting a new EWS connection and retrying");

                        args.remove(exchangeService);
                        exchangeService = exchangeProvider.renewService(exchangeService);
                        args.add(exchangeService);

                        retrying = true;
                    } else {
                        log.error("2nd and final timeout error performing EWS Method: " + sre + ", method is: " + pjp);
                        retrying = false;
                    }
                } finally {
                    if(!retrying) {
                        exchangeProvider.releaseService(exchangeService);
                    }
                }
            }
            while (retrying);
        }
        else {
            pjp.proceed(args.toArray());
        }

        return output;
    }
}