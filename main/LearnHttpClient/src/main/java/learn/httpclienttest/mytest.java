package learn.httpclienttest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

public class mytest {

    public static void main(String[] args) throws Exception {
        test3();
    }

    // For http get
    private static void test1() throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet("http://127.0.0.1:8080/rest/kfc/brands/name");
            Shop myjson = httpclient.execute(httpget, getRH(Shop.class));
            System.out.println(myjson.toString());
        } finally {
            httpclient.close();
        }
    }

    // For http post
    private static void test2() throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost("http://127.0.0.1:8080/rest/kfc/brands/onecar");
            httpPost.setHeader("Content-Type", "application/json");

            Car car = new Car();
            car.setColor("123");
            car.setMiles(12);
            car.setVIN("3123123");

            Type type = new TypeToken<Car>(){}.getType();
            String postConent = new GsonBuilder().create().toJson(car, type);
            httpPost.setEntity(new StringEntity(postConent, ContentType.APPLICATION_JSON));
            Car myjson = httpclient.execute(httpPost, getRH(Car.class));
            System.out.println(myjson.toString());
        } finally {
            httpclient.close();
        }
    }

    //
    private static void test3() throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpContext context = new BasicHttpContext();
        try
        {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(1000)
                    .setConnectTimeout(1000)
                    .build();
            HttpGet httpget1 = new HttpGet(("http://127.0.0.1:8080/SpringMVC/rest/kfc/brands/aaa"));
            httpget1.setConfig(requestConfig);
            CloseableHttpResponse response1 = httpclient.execute(httpget1, context);
            try {
                HttpEntity entity1 = response1.getEntity();
            } finally {
                response1.close();
            }
            HttpGet httpget2 = new HttpGet(("http://127.0.0.1:8080/SpringMVC/rest/kfc/brands/bbb"));
            Shop shop = httpclient.execute(httpget2, getRH(Shop.class), context);
            System.out.println(shop);
        }
        finally {
            httpclient.close();
        }
    }

    private static <T> ResponseHandler<T> getRH(final Class<T> clazz)
    {
        ResponseHandler<T> rh = new ResponseHandler<T>() {
            public T handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                StatusLine statusLine = response.getStatusLine();
                HttpEntity entity = response.getEntity();
                if (statusLine.getStatusCode() >= 300) {
                    throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
                }
                if (entity == null) {
                    throw new ClientProtocolException("Response contains no content");
                }
                Gson gson = new GsonBuilder().create();
                ContentType contentType = ContentType.getOrDefault(entity);
                Charset charset = contentType.getCharset();
                Reader reader = new InputStreamReader(entity.getContent(), charset);
                return gson.fromJson(reader, clazz);
            }
        };
        return rh;
    }

}
