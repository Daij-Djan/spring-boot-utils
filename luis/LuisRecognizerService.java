package info.pich.chatbottest.luis;

import info.pich.chatbottest.configuration.LocalizedLuisApplicationProperties;
import info.pich.chatbottest.configuration.LuisProperties;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Service
@Slf4j
public class LuisRecognizerService {
    final
    LuisProperties props;

    @Autowired
    public LuisRecognizerService(LuisProperties props) {
        this.props = props;
    }

    public LUISResponse predict(String text, String localeIdentifier) {
        if (text == null)
            throw new IllegalArgumentException("NULL text to predict");
        text = text.trim();
        if (text.isEmpty())
            throw new IllegalArgumentException("Empty text to predict");

        LUISResponse LUISresponse = null;

        //TODO : why can I not use my global exception handler?
        try {
            //prepare
            String urlStr = generatePredictURL(text, localeIdentifier);
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            //do call
            log.debug("Sending Luis 'GET' request to URL : " + url);
            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                log.debug("got message to luis");
                //read all
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
                log.debug("luis response: " + response.toString());
                JSONObject json = new JSONObject(response.toString());
                LUISresponse = new LUISResponse(json);
            } else {
                log.error("failed to get prediction from luis");
            }
        } catch (IOException e) {
            log.error("Ingoring Exception during call to luis: " + e, e);
        }

        return LUISresponse;
    }

    public String generatePredictURL(String text, String localeIdentifier) throws IOException {
        LocalizedLuisApplicationProperties app = props.getLocalized().get(localeIdentifier);

        String LUISURL = app.getUrl();
        String appId = app.getAppId();
        String appKey = app.getAppKey();

        String LUISPredictMask = props.getPredictMask();
        boolean preview = props.isPreview();
        boolean verbose = props.isVerbose();

        String LUISPreviewURL = preview ? "/preview" : "";
        String LUISVerboseURL = verbose ? "&verbose=true" : "";

        String encodedQuery = URLEncoder.encode(text, "UTF-8");
        return String.format(LUISPredictMask, LUISURL, LUISPreviewURL, appId, appKey, LUISVerboseURL, encodedQuery);
    }

}
