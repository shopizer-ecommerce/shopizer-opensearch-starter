package com.shopizer.search.autoconfigure;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.CreateIndexResponse;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.xcontent.XContentType;

import modules.commons.search.configuration.SearchConfiguration;
import modules.commons.search.configuration.SearchHost;

public class SearchClient {
	
	private final static String PRODUCTS_INDEX = "products_";
	private final static String KEYWORDS_INDEX = "keywords_";
	
	private static SearchClient client = null;
	private RestHighLevelClient searchClient = null; 
	
	private SearchClient(SearchConfiguration configuration) throws Exception {
		
		Validate.notNull(configuration,"SearchConfiguration cannot be null");

		
		if(searchClient != null) {
			searchClient.close();
			searchClient = null;
		}

		
		List<HttpHost> hostList = configuration.getHosts()
				.stream()
				.map(h -> this.host(h))
				.collect(Collectors.toList());

        RestClientBuilder builder = RestClient
        		.builder(hostList.toArray(new HttpHost[hostList.size()]));
        
        /**
         * spring boot issue documented here
         * https://github.com/elastic/elasticsearch/issues/59261
         */
        builder.setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder.setKeepAliveStrategy((response, context) -> 3600000/* 1hour */));

        
        if(configuration.getCredentials()!=null) {

				final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(configuration.getCredentials().getUserName(), configuration.getCredentials().getPassword()));
			
	        
		        builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
		        	  
			              public HttpAsyncClientBuilder customizeHttpClient(
			            		  
			            		  
			            	  //with or without ssl
			            	  		  
			                  final HttpAsyncClientBuilder httpAsyncClientBuilder) {
			            	  
			            	    httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
			            	  	try {
			                    
			                      
			          			/** only if a test certificate is used **/
			            	  		
			            		/**
			            		* To be able to connect to the node you should have a valid or root certificate on opensearch
			            		* Docker version uses a self signed certificate and required the use of a java jks that has open search certificate
			            		* a test jks working with OS decker version can be downloaded here https://github.com/opensearch-project/OpenSearch/tree/main/client/rest-high-level
			            		*/				            	  		
			          			if(!StringUtils.isBlank(configuration.getJksAbsolutePath())) {
			          		

			          		        Path trustStorePath = Paths.get(configuration.getJksAbsolutePath());
			          		        KeyStore truststore = KeyStore.getInstance("jks");
			          		        try (InputStream is = Files.newInputStream(trustStorePath)) {
			          		            truststore.load(is, "instaclustr".toCharArray());
			          		        } catch (CertificateException e) {
			          		            e.printStackTrace();
			          		        }
			          		        SSLContextBuilder sslBuilder = SSLContexts.custom().loadTrustMaterial(truststore, null);
			          		        final SSLContext sslContext = sslBuilder.build();
			          		        
			          		        httpAsyncClientBuilder.setSSLContext(sslContext);

			          			
			          			}
			                      
			                      
			                     
			            	  	} catch(Exception e) {
			            	  		throw new RuntimeException(e);
			            	  	}
			            	  	
			            	  	 return httpAsyncClientBuilder;

			                  }
			          	}
		          
		        );
	        
        
        }
 
        searchClient = new RestHighLevelClient(builder);


        List<String> languages = configuration.getLanguages();

        
        if(CollectionUtils.isEmpty(languages)) {
        	languages = new ArrayList<String>();
        	languages.add("en");
        }
        
        final List<String> indexLanguages = new ArrayList<String>(languages);
        
        /**
         * check if index already exist
         */

        
        /**
         * Create indexes
         */
        
        indexLanguages.stream().forEach(l -> {
			try {
				this.createIndex(PRODUCTS_INDEX, configuration.getProductMappings().get(l), configuration.getSettings().get(l), false, l);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
        indexLanguages.stream().forEach(l -> {
			try {
				this.createIndex(KEYWORDS_INDEX, configuration.getKeywordsMappings().get(l), configuration.getSettings().get(l),true, l);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
        
		
	} 
	
	protected static SearchClient getInstance(SearchConfiguration config) throws Exception {
		if(client == null) {
			client = new SearchClient( config);
		}
		
		return client;
	}
	
	
	private HttpHost host(SearchHost host) {
		return new HttpHost(host.getHost(),host.getPort(),host.getScheme());
	}

	
	private void createIndex(String indexPrefix, String mappings, String settings, boolean hasMappings, String language) throws Exception {
		StringBuilder indexBuilder = new StringBuilder();
		indexBuilder.append(indexPrefix).append(language.toLowerCase());
		CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexBuilder.toString());
		
		//test if index exists
		GetIndexRequest request = new GetIndexRequest(indexBuilder.toString());
		boolean exists = searchClient.indices().exists(request, RequestOptions.DEFAULT);
		if(exists) {
			return;
		}
		

		createIndexRequest.settings(
		        settings
		        , XContentType.JSON);
        

		createIndexRequest.mapping(mappings, XContentType.JSON);
		
		System.out.println(mappings);
        
        CreateIndexResponse createIndexResponse = searchClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        System.out.println("Creating index: " + indexBuilder.toString());
        System.out.println("Is client acknowledged?" + ((createIndexResponse.isAcknowledged())? " Yes" : " No"));
 
        
	}
	
	protected RestHighLevelClient getClient() throws Exception {
		
		return searchClient;
	}

    


}
