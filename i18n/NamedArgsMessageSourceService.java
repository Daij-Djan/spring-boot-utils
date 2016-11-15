package info.pich.chatbottest.i18n;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class NamedArgsMessageSourceService {
    private final MessageSource messageSource;

    @Autowired
    public NamedArgsMessageSourceService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String get(String key, Map<String, Object> args, String localeIdentifier) {
        String str;

        try {
            Locale locale = new Locale(localeIdentifier);
            str = messageSource.getMessage(key, null, locale);

            if (args != null) {
                StringBuilder builder = new StringBuilder(str);

                for (String argName : args.keySet()) {
                    String from = "${" + argName + "}";
                    String to = args.get(argName).toString();

                    int index = builder.indexOf(from);
                    while (index != -1) {
                        builder.replace(index, index + from.length(), to);
                        index += to.length(); // Move to the end of the replacement
                        index = builder.indexOf(from, index);
                    }
                }

                str = builder.toString();
            }
        } catch (Exception e) {
            log.error("Ignoring exception " + e + ": " + e.getStackTrace() + " returning key!");
            str = key;
        }

        return str;
    }
}
