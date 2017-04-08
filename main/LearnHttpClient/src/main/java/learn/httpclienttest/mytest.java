package learn.httpclienttest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
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
        test4();
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
            HttpGet httpget1 = new HttpGet(("http://127.0.0.1:8080/rest/kfc/brands/aaa"));
            httpget1.setConfig(requestConfig);
            CloseableHttpResponse response1 = httpclient.execute(httpget1, context);
            try {
                HttpEntity entity1 = response1.getEntity();
            } finally {
                response1.close();
            }
            HttpGet httpget2 = new HttpGet(("http://127.0.0.1:8080/rest/kfc/brands/bbb"));
            Shop shop = httpclient.execute(httpget2, getRH(Shop.class), context);
            System.out.println(shop);
        }
        finally {
            httpclient.close();
        }
    }

    private static void test4() throws InterruptedException, IOException
    {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        // Increase max total connection to 200
        cm.setMaxTotal(200);
        // Increase default max connection per route to 20
        cm.setDefaultMaxPerRoute(20);
        // Increase max connections for localhost:80 to 50
        HttpHost localhost = new HttpHost("locahost", 80);
        cm.setMaxPerRoute(new HttpRoute(localhost), 50);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();
        try {
            // URIs to perform GETs on
            String[] urisToGet = {
                    "http://127.0.0.1:8080/rest/kfc/brands/aaa",
                    "http://127.0.0.1:8080/rest/kfc/brands/bbb",
                    "http://127.0.0.1:8080/rest/kfc/brands/ccc",
                    "http://127.0.0.1:8080/rest/kfc/brands/ddd"
            };

            // create a thread for each URI
            GetThread[] threads = new GetThread[urisToGet.length];
            for (int i = 0; i < threads.length; i++) {
                HttpGet httpget = new HttpGet(urisToGet[i]);
                threads[i] = new GetThread(httpClient, httpget);
            }

            // start the threads
            for (int j = 0; j < threads.length; j++) {
                threads[j].start();
            }

            // join the threads
            for (int j = 0; j < threads.length; j++) {
                threads[j].join();
            }
        }
        finally {
            httpClient.close();
        }
    }

    static class GetThread extends Thread {
        private final CloseableHttpClient httpClient;
        private final HttpContext context;
        private final HttpGet httpget;

        public GetThread(CloseableHttpClient httpClient, HttpGet httpget) {
            //每个线程共享一个httpclient，因为它是线程安全的
            this.httpClient = httpClient;

            //每个线程都create一个自己的context对象，因为它是非线程安全的
            this.context = HttpClientContext.create();

            this.httpget = httpget;
        }

        @Override
        public void run() {
            try {
                CloseableHttpResponse response = httpClient.execute(
                        httpget, context);
                try {
                    System.out.println("Run httpget for URI: " + httpget.getURI());
                    HttpEntity entity = response.getEntity();
                }
                finally {
                    response.close();
                }
            }
            catch (ClientProtocolException ex) {
                // Handle protocol errors
            }
            catch (IOException ex) {
                // Handle I/O errors
            }
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
