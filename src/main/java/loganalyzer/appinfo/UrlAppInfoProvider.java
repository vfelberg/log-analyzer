package loganalyzer.appinfo;

import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.beans.factory.annotation.Required;

import org.apache.commons.io.IOUtils;

public class UrlAppInfoProvider implements AppInfoProvider {
    private String _appInfoUrl;

    @Override
    public String getContentType() {
        return "text/html";
    }

    @Override
    public String getAppInfo() {
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(_appInfoUrl);
        getMethod.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        try {
            int statusCode = httpClient.executeMethod(getMethod);
            if (statusCode == HttpStatus.SC_OK) {
                InputStream in = getMethod.getResponseBodyAsStream();
                return IOUtils.toString(in, "UTF-8");
            } else {
                return String.format("Application info could not be retrieved from %s, status code: %s", _appInfoUrl, statusCode);
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    @Required
    public void setAppInfoUrl(String appInfoUrl) {
        _appInfoUrl = appInfoUrl;
    }
}
