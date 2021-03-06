package lxz.tutorial.https;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

/**
 * 使用 httpclient4.5 进行 https 通讯，
 * 采用双向认证， 连接池管理connection
 * 
 * @author wangfeihu
 *
 */
public class HttpClientforSSL {

	public static HttpClientConnectionManager CONNECTION_MANAGER = null;

	/**
	 * 初始化 connection manager.
	 * @param keyStoreFile, 客户端证书
	 * @param keyStorePass，客户端证书对应的密码
	 * @param trustStoreFile
	 * @param trustStorePass
	 * @throws Exception
	 */
	public void init(String keyStoreFile, String keyStorePass,
			String trustStoreFile, String trustStorePass) throws Exception {
		System.out.println("init conection pool...");

		InputStream ksis = new FileInputStream(new File(keyStoreFile));
		InputStream tsis = new FileInputStream(new File(trustStoreFile));

		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(ksis, keyStorePass.toCharArray());

		KeyStore ts = KeyStore.getInstance("JKS");
		ts.load(tsis, trustStorePass.toCharArray());

		SSLContext sslContext = SSLContexts.custom()
				.loadKeyMaterial(ks, keyStorePass.toCharArray())
				// 如果有 服务器证书
				.loadTrustMaterial(ts, new TrustSelfSignedStrategy())
				// 如果没有服务器证书，可以采用自定义 信任机制
				// .loadTrustMaterial(null, new TrustStrategy() {
				//
				// // 信任所有
				// public boolean isTrusted(X509Certificate[] arg0,
				// String arg1) throws CertificateException {
				// return true;
				// }
				//
				// })
				.build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				sslContext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		Registry<ConnectionSocketFactory> registry = RegistryBuilder
				.<ConnectionSocketFactory> create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", sslsf).build();
		ksis.close();
		tsis.close();
		CONNECTION_MANAGER = new PoolingHttpClientConnectionManager(registry);

	}

	/**
	 * do post
	 * @param url
	 * @param params
	 * @throws Exception
	 */
	public void post(String url, String params) throws Exception {
		if (CONNECTION_MANAGER == null) {
			return;
		}
		CloseableHttpClient httpClient = HttpClients.custom()
				.setConnectionManager(CONNECTION_MANAGER).build();
		HttpPost httpPost = new HttpPost(url);

		httpPost.setEntity(new StringEntity(params,
				ContentType.APPLICATION_JSON));

		CloseableHttpResponse resp = httpClient.execute(httpPost);
		System.out.println(resp.getStatusLine());
		InputStream respIs = resp.getEntity().getContent();
		String content = convertStreamToString(respIs);
		System.out.println(content);
		EntityUtils.consume(resp.getEntity());
	}

	
	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}


	/**
	 * do get
	 * @param url
	 * @param params
	 * @throws Exception
	 */
	public void get(String url) throws Exception {
		if (CONNECTION_MANAGER == null) {
			return;
		}
		CloseableHttpClient httpClient = HttpClients.custom()
				.setConnectionManager(CONNECTION_MANAGER).build();
		HttpGet get = new HttpGet(url);

		CloseableHttpResponse resp = httpClient.execute(get);
		System.out.println(resp.getStatusLine());
		InputStream respIs = resp.getEntity().getContent();
		String content = convertStreamToString(respIs);
		System.out.println(content);
		EntityUtils.consume(resp.getEntity());
	}
	
	public static void main(String[] args) {
		// 服务地址
		String url = "https://domain.lxz.com/";
		// 私钥证书
		String keyStoreFile = "C:\\CWS\\certificates\\openssl\\client.p12";
		String keyStorePass = "root";

		// 配置信任证书库及密码
		String trustStoreFile = "C:\\CWS\\certificates\\openssl\\mySelfSignedTrustStore";
		String trustStorePass = "changeit";

		HttpClientforSSL obj = new HttpClientforSSL();
		try {
			obj.init(keyStoreFile, keyStorePass, trustStoreFile, trustStorePass);
			obj.get(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}