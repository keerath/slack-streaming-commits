package com.imaginea.slack;

import com.imaginea.json.connection.SlackInfo;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SlackAuthen {

    private static final String SLACK_RTM_AUTHEN_URL = "https://slack.com/api/rtm.start?token=";

    public SlackInfo tokenAuthen(String token) {
        HttpClient client = new HttpClient();
        GetMethod getMethod = new GetMethod(SLACK_RTM_AUTHEN_URL + token);
        SlackInfo slackInfo = new SlackInfo();

        try {
            int httpStatus = client.executeMethod(getMethod);
            if (httpStatus == HttpStatus.SC_OK) {
                ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                SlackInfo info = mapper.readValue(getMethod.getResponseBodyAsStream(), SlackInfo.class);
                return info;
            } else {
                slackInfo.setError("http_status_" + httpStatus);
                return slackInfo;
            }
        } catch (IOException ex) {
            slackInfo.setError("exception " + ex.getMessage());
            Logger.getLogger(SlackAuthen.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            getMethod.releaseConnection();
        }
        return slackInfo;
    }

}
