/**
 @file      ProfilerProperties.java
 @author    Dominik Pich
 @date      30/11/2016

Copyright (c) 2016, Sapient GmbH
All rights reserved.

*/
package com.sapient.quickbook.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@SuppressWarnings("serial")
@Component
@ConfigurationProperties("exchange")
@Data
public class ExchangeProperties implements Serializable {
    private String url;
    private String email;
    private String password;
    private int timeoutMs;
    private boolean tracingEnabled;
}
