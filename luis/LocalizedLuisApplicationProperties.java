package info.pich.chatbottest.configuration;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@SuppressWarnings("serial")
@Component()
public class LocalizedLuisApplicationProperties implements Serializable {
    private String url;
    private String appId;
    private String appKey;
}

