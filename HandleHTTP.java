package ua.elitasoftware.UzhNU;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HandleHTTP {

    public String makeRequest(String url){
        try {
//            HttpParams params = new BasicHttpParams();
//            HttpConnectionParams.setConnectionTimeout(params, 100);
//            HttpConnectionParams.setSoTimeout(params, 2000);

            DefaultHttpClient httpClient = new DefaultHttpClient();//params);
//            HttpGet httpGet = new HttpGet("http://www.google.com");
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);

            HttpEntity httpEntity = httpResponse.getEntity();
            return EntityUtils.toString(httpEntity);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
