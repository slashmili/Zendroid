package com.github.slashmili.Zendroid.utils
import java.io.IOException;
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.conn.scheme.PlainSocketFactory
import org.apache.http.params.BasicHttpParams
import org.apache.http.conn.params.{ConnManagerPNames, ConnPerRouteBean}
import org.apache.http.params.HttpProtocolParams
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.{HttpException, HttpRequest, HttpRequestInterceptor, HttpVersion}
import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.conn.scheme.Scheme
import org.apache.http.protocol.HttpContext
import com.byarger.exchangeit._

object UntrustedHttpsClient {
  def createHttpClient : DefaultHttpClient = {
          val schemeRegistry = new SchemeRegistry();
          // http scheme
          schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
          // https scheme
          schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));

          val params = new BasicHttpParams();
          params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
          params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
                  new ConnPerRouteBean(30));
          HttpProtocolParams.setUseExpectContinue(params, true);
          HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

          val cm = new ThreadSafeClientConnManager(params,
                  schemeRegistry);

          val client = new DefaultHttpClient(cm, params);

  /*        client.addRequestInterceptor(new HttpRequestInterceptor() {
              @throws(classOf[IOException],classOf[HttpException])
              def process(request: HttpRequest, context: HttpContext )= {
                  request.addHeader("User-Agent", "Mozilla/5.0");
              }
          });*/

          return client;
  }
}


// vim: set ts=2 sw=2 et:
