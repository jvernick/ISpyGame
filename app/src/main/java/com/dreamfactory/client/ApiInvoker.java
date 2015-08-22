package com.dreamfactory.client;


import android.util.Log;

import com.dreamfactory.model.FileRequest;
import com.fasterxml.jackson.databind.JavaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ApiInvoker {
	private static ApiInvoker INSTANCE = new ApiInvoker();
	private Map<String, String> defaultHeaderMap = new HashMap<String, String>();

	private HttpClient client = null;

	//TODO added by Brunel
	private static final String TAG = "ApiInvoker";

	private boolean ignoreSSLCertificates = false;

	private ClientConnectionManager ignoreSSLConnectionManager;

	public ApiInvoker() {
		initConnectionManager();
	}

	public static ApiInvoker getInstance() {
		return INSTANCE;
	}

	public void ignoreSSLCertificates(boolean ignoreSSLCertificates) {
		this.ignoreSSLCertificates = ignoreSSLCertificates;
	}

	public void addDefaultHeader(String key, String value) {
		defaultHeaderMap. put(key, value);
	}

	public String escapeString(String str) {
		return str;
	}

	//TODO should this be here?
	/**
	 * Added by Brunel F. on 7/22/15
	 *
	 ensures that the string parameters are encoded to be used in the url
	 */
	public String urlEncodeString (String input) {

		try {
			return String.valueOf(URLEncoder.encode(input, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(); //TODO find a better way of handling this exception
			return null;
		}

	}

	public static Object deserialize(String json, String containerType, Class cls) throws ApiException {
		try{
			if("List".equals(containerType)) {
				JavaType typeInfo = JsonUtil.getJsonMapper().getTypeFactory().constructCollectionType(List.class, cls);
				List response = (List<?>) JsonUtil.getJsonMapper().readValue(json, typeInfo);
				return response;
			}
			else if(String.class.equals(cls)) {
				if(json != null && json.startsWith("\"") && json.endsWith("\"") && json.length() > 1)
					return json.substring(1, json.length() - 2);
				else 
					return json;
			}
			else {
				return JsonUtil.getJsonMapper().readValue(json, cls);
			}
		}
		catch (IOException e) {
			throw new ApiException(500, e.getMessage());
		}
	}

	public static String serialize(Object obj) throws ApiException {
		try {
			if (obj != null) {
				//TODO for debugging, remove
				//Log.d("ApiInvoker",JsonUtil.getJsonMapper().writeValueAsString(obj));
				return JsonUtil.getJsonMapper().writeValueAsString(obj);
			} else
				return null;
		}
		catch (Exception e) {
			throw new ApiException(500, e.getMessage());
		}
	}

	public String invokeAPI(String host, String path, String method, Map<String, String> queryParams, String body, Map<String, String> headerParams, String contentType) throws ApiException {
//		return invokeAPI(host, path, method, queryParams, body, headerParams, contentType, null);
		return invokeAPIInternal(host, path, method, queryParams, body, headerParams, contentType, null);
	}
	
	public String invokeAPI(String host, String path, String method, Map<String, String> queryParams, Object body, Map<String, String> headerParams, String contentType) throws ApiException {
		return invokeAPI(host, path, method, queryParams, body, headerParams, contentType, null);
	}

	public String invokeAPI(String host, String path, String method, Map<String, String> queryParams, Object body, Map<String, String> headerParams, String contentType, FileRequest fileRequest) throws ApiException {
		return invokeAPIInternal(host, path, method, queryParams, serialize(body), headerParams, contentType, fileRequest);
	}
	
	private String invokeAPIInternal(String host, String path, String method, Map<String, String> queryParams, String body, Map<String, String> headerParams, String contentType, FileRequest fileRequest) throws ApiException {
		HttpClient client = getClient(host);

		StringBuilder b = new StringBuilder();
		for(String key : queryParams.keySet()) {
			String value = queryParams.get(key);
			if (value != null){
				if(b.toString().length() == 0)
					b.append("?");
				else
					b.append("&");
				//TODO confiirm change
				//new
				b.append(escapeString(key)).append("=").append(urlEncodeString(value));
				//old
				//b.append(escapeString(key)).append("=").append(escapeString(value));
			}
		}
		String url = host + path + b.toString();

		HashMap<String, String> headers = new HashMap<String, String>();

		for(String key : headerParams.keySet()) {
			headers.put(key, headerParams.get(key));
		}

		for(String key : defaultHeaderMap.keySet()) {
			if(!headerParams.containsKey(key)) {
				headers.put(key, defaultHeaderMap.get(key));
			}
		}
		headers.put("Accept", "application/json");

		HttpResponse response = null;
		try{
			if("GET".equals(method)) {
				HttpGet get = new HttpGet(url);
				get.addHeader("Accept", "application/json");
				for(String key : headers.keySet()) {
					get.setHeader(key, headers.get(key));
				}
				response = client.execute(get);
			}
			else if ("POST".equals(method)) {
				HttpPost post = new HttpPost(url);
				post.setHeader("Content-Type", contentType);
				//TODO added by Brunel
				Log.d(TAG, "Post created and header set");
				if (fileRequest != null){
					File file = new File(fileRequest.getPath());
					FileEntity reqEntity = new FileEntity(file, contentType);
					post.setEntity(reqEntity);
				}
				else if (body != null) {
					post.setEntity(new StringEntity(body, "UTF-8"));
				}
				for(String key : headers.keySet()) {
					post.setHeader(key, headers.get(key));
				}
				//TODO added by Brunel
				Log.d(TAG, "All parameters set up");
				response = client.execute(post);
				//TODO added by Brunel
				Log.d(TAG, "Response: " + response.toString());

			}
			else if ("PUT".equals(method)) {
				HttpPut put = new HttpPut(url);
				if(body != null) {
					put.setHeader("Content-Type", contentType);
					put.setEntity(new StringEntity(body, "UTF-8"));
				}
				for(String key : headers.keySet()) {
					put.setHeader(key, headers.get(key));
				}
				response = client.execute(put);
			}
			else if ("DELETE".equals(method)) {
				HttpDelete delete = new HttpDelete(url);
				for(String key : headers.keySet()) {
					delete.setHeader(key, headers.get(key));
				}
			}
			/* 		TODO
					Added by Brunel on 7-23-15
					added to enable a body in delete requests
				 */
			else if ("DELETE_BODY".equals(method)) {
				HttpDeleteWithBody delete = new HttpDeleteWithBody(url);
				for(String key : headers.keySet()) {
					delete.setHeader(key, headers.get(key));
				}
				//TODO Veriify
				if (body != null) {
					delete.setHeader("Content-Type", contentType);
					delete.setEntity(new StringEntity(body, "UTF-8"));
				}
				response = client.execute(delete);
			}
			else if ("PATCH".equals(method)) {
				HttpPatch patch = new HttpPatch(url);

				if (body != null) {
					patch.setHeader("Content-Type", contentType);
					patch.setEntity(new StringEntity(body, "UTF-8"));
				}
				for(String key : headers.keySet()) {
					patch.setHeader(key, headers.get(key));
				}
				response = client.execute(patch);
			}

			int code = response.getStatusLine().getStatusCode();
			String responseString = null;
			if(code == 204) 
				responseString = "";
			else if(code >= 200 && code < 300) {
				if(response.getEntity() != null) {
					HttpEntity resEntity = response.getEntity();
					responseString = EntityUtils.toString(resEntity);
				}
			}
			else {
				if(response.getEntity() != null) {
					HttpEntity resEntity = response.getEntity();
					responseString = EntityUtils.toString(resEntity);
				}
				else {
					responseString = "no data";
				}
				throw new ApiException(code, responseString);
			}
			return responseString;
		}
		catch(IOException e) {
			Log.v(TAG, e.getClass().toString());
			Log.v(TAG, e.getLocalizedMessage());
			Log.v(TAG, e.getStackTrace().toString());
			throw new ApiException(500, e.getMessage());
		}
	}

	//@NotThreadSafe
	class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
		public static final String METHOD_NAME = "DELETE";
		public String getMethod() { return METHOD_NAME; }

		public HttpDeleteWithBody(final String uri) {
			super();
			setURI(URI.create(uri));
		}
		public HttpDeleteWithBody(final URI uri) {
			super();
			setURI(uri);
		}
		public HttpDeleteWithBody() { super(); }
	}

	//TODO Confirm modification below to add a timeout when there is no connection
	private HttpClient getClient(String host) {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 10000); // 10 second timeout

		if (client == null) {
			if (ignoreSSLCertificates && ignoreSSLConnectionManager != null) {
				// Trust self signed certificates
				client = new DefaultHttpClient(ignoreSSLConnectionManager, httpParams);
			} else {
				client = new DefaultHttpClient(httpParams);
			}
		}
		return client;
	}

	private void initConnectionManager() {
		try {
			final SSLContext sslContext = SSLContext.getInstance("SSL");

			// set up a TrustManager that trusts everything
			TrustManager[] trustManagers = new TrustManager[] {
					new X509TrustManager() {
						public X509Certificate[] getAcceptedIssuers() {
							return null;
						}
						public void checkClientTrusted(X509Certificate[] certs, String authType) {}
						public void checkServerTrusted(X509Certificate[] certs, String authType) {}
					}};

			sslContext.init(null, trustManagers, new SecureRandom());

			SSLSocketFactory sf = new SSLSocketFactory((KeyStore)null) {
				private javax.net.ssl.SSLSocketFactory sslFactory = sslContext.getSocketFactory();

				public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
						throws IOException, UnknownHostException {
					return sslFactory.createSocket(socket, host, port, autoClose);
				}

				public Socket createSocket() throws IOException {
					return sslFactory.createSocket();
				}
			};

			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			Scheme httpsScheme = new Scheme("https", sf, 443);
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(httpsScheme);
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

			ignoreSSLConnectionManager = new SingleClientConnManager(new BasicHttpParams(), schemeRegistry);
		} catch (NoSuchAlgorithmException e) {
			// This will only be thrown if SSL isn't available for some reason.
		} catch (KeyManagementException e) {
			// This might be thrown when passing a key into init(), but no key is being passed.
		} catch (GeneralSecurityException e) {
			// This catches anything else that might go wrong.
			// If anything goes wrong we default to the standard connection manager.
		}
	}
}

