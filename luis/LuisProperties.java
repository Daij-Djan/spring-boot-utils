package info.pich.chatbottest.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

@SuppressWarnings("serial")
@Component
@ConfigurationProperties("chatbot.luis")
@Data
public class LuisProperties implements Serializable {
    private Map<String, LocalizedLuisApplicationProperties> localized;
    private String predictMask;
    private boolean preview;
    private boolean verbose;
}
