package com.sapient.quickbook.services.ews;

import com.sapient.quickbook.configuration.ExchangeProperties;
import com.sapient.quickbook.util.XMLPrettyPrinter;
import lombok.extern.slf4j.Slf4j;
import microsoft.exchange.webservices.data.autodiscover.IAutodiscoverRedirectionUrl;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.misc.TraceFlags;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.misc.ITraceListener;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Component
@Slf4j
public class ExchangeProvider {
    private final List<ExchangeService> exchangeServices;
    private final ExchangeProperties exchangeProperties;

    public ExchangeProvider(ExchangeProperties properties) {
        exchangeServices = new ArrayList<>();
        exchangeProperties = properties;

        synchronized(exchangeServices) {
            log.info("preload 1 connection");
            ExchangeService exchangeService = newExchangeService();
            exchangeServices.add(exchangeService);
        }

    }

    public ExchangeService retainService() {
        ExchangeService exchangeService = null;

        synchronized(exchangeServices) {
            if(exchangeServices.size()>0) {
                exchangeService = exchangeServices.remove(0);
            }
        }

        if(exchangeService == null) {
            log.info("no connection in pool");
            exchangeService = newExchangeService();
        }

        return exchangeService;
    }

    public void releaseService(ExchangeService exchangeService) {
        synchronized(exchangeServices) {
            if(exchangeServices.contains(exchangeService)) {
                log.warn("clients shouldn't release a service they don't own");
            }

            exchangeServices.add(exchangeService);
        }
    }

    public ExchangeService renewService(ExchangeService exchangeService) {
        synchronized(exchangeServices) {
            if(exchangeServices.contains(exchangeService)) {
                log.warn("clients shouldn't renew a service they dont own");
                exchangeServices.remove(exchangeService);
            }
        }

        exchangeService.close();
        return newExchangeService();
    }

    @PreDestroy
    private void dealloc() {
        synchronized(exchangeServices) {
            for (ExchangeService service : exchangeServices) {
                service.close();
            }
        }
    }

    private ExchangeService newExchangeService() {
        log.info("make new service");

	    ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        ExchangeCredentials credentials = new WebCredentials(exchangeProperties.getEmail(), exchangeProperties.getPassword());
        service.setCredentials(credentials);

        //get final url - autodiscover it if needed
        boolean uriSet = false;
		if(exchangeProperties.getUrl() != null) {
			try {
				service.setUrl(new URI(exchangeProperties.getUrl()));
				uriSet = true;
			} catch (URISyntaxException e) {
				log.error("specified url is invalid, trying autodiscovery");
			}
		}
		if(!uriSet) {
			try {
				service.autodiscoverUrl(exchangeProperties.getEmail(), new RedirectionUrlCallback());
			} catch (Exception e) {
                log.error("failed to initialize autodiscovery and no url.");
			}
		}
		//prepare tracing
		if(exchangeProperties.isTracingEnabled()) {
			service.setTraceEnabled(true);
			service.setTraceFlags(EnumSet.allOf(TraceFlags.class)); // can also be restricted
			service.setTraceListener(new ITraceListener() {
				public void trace(String traceType, String traceMessage) {
				    if(traceMessage.startsWith("<Trace")) {
                        //hackish remove <?xml
                        traceMessage = traceMessage.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>","");
                        log.debug(XMLPrettyPrinter.prettyPrintXml(traceMessage));
                    }
                    else {
                        // do some logging-mechanism here
                        log.debug("Type:" + traceType + "\nMessage:" + traceMessage);
                    }
				}
			});
		}

		//all callss after this should be quicker
        service.setTimeout(exchangeProperties.getTimeoutMs());

        return service;
	}

    static class RedirectionUrlCallback implements IAutodiscoverRedirectionUrl {
        public boolean autodiscoverRedirectionUrlValidationCallback(
                String redirectionUrl) {
            return redirectionUrl.toLowerCase().startsWith("https://");
        }
    }
}
