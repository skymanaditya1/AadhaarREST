package in.gov.uidai.auth.aua.httpclient;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public class HttpClientHelper {
	
	public static ClientConfig getClientConfig(String uriScheme) {
		ClientConfig config = new DefaultClientConfig();

		if (uriScheme.equalsIgnoreCase("https")) {
			X509TrustManager xtm = new X509TrustManager() {

				@Override
				public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					return;
				}

				@Override
				public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					return;
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			TrustManager mytm[] = { xtm };

			HostnameVerifier hv = new HostnameVerifier() {

				public boolean verify(String hostname, SSLSession sslSession) {
					return true;
				}
			};

			SSLContext ctx = null;

			try {
				ctx = SSLContext.getInstance("SSL");
				ctx.init(null, mytm, null);
				config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(hv, ctx));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (KeyManagementException e) {
				e.printStackTrace();
			}
		}

		return config;
	}
	
}
