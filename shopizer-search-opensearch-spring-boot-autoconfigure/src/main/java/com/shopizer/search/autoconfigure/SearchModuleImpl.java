package com.shopizer.search.autoconfigure;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.lucene.search.join.ScoreMode;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.delete.DeleteResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.TermQueryBuilder;
import org.opensearch.rest.RestStatus;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;

import modules.commons.search.SearchModule;
import modules.commons.search.configuration.SearchConfiguration;
import modules.commons.search.request.IndexItem;
import modules.commons.search.request.SearchFilter;
import modules.commons.search.request.SearchRequest;
import modules.commons.search.request.SearchResponse;

public class SearchModuleImpl implements SearchModule {

	

	private String uniqueCode = "opensearch";
	private SearchClient searchClient = null;
	
	private final static String PRODUCTS_INDEX = "products_";
	private final static String KEYWORDS_INDEX = "keywords_";



	@Override
	public void configure(SearchConfiguration configuration) throws Exception {
		
		searchClient = SearchClient.getInstance(configuration);

		
	}


	@Override
	public String getUniqueCode() {

		return uniqueCode;
	}
	


	@Override
	public void index(IndexItem item) throws Exception {
		
		
		Validate.notNull(item, "Item must not be null");
		Validate.notNull(item.getLanguage(),"Languge must not be null");
		
		
		
		if(searchClient == null) {
			throw new Exception("OpenSearch client has not been initialized. Please run configure(SearchConfiguration) before trying to index.");
		}
		

		//index to product
        IndexRequest request = new IndexRequest(new StringBuilder().append(PRODUCTS_INDEX).append(item.getLanguage()).toString());
        request.id(String.valueOf(item.getId()));
        Map<String, Object> product = this.parameters(item);
        request.source(product);
        
        IndexResponse indexResponse = searchClient.getClient().index(request, RequestOptions.DEFAULT);
        System.out.println("Adding product document:");
        System.out.println(indexResponse);
        
        //index to keyword
        
        KeywordIndex k = new KeywordIndex();
        k.setName(item.getName());
        
        request = new IndexRequest(new StringBuilder().append(KEYWORDS_INDEX).append(item.getLanguage()).toString());
        request.id(String.valueOf(item.getId()));
        //Map<String, Object> keyword = this.parameters(k);
        Map<String, Object> map = new HashMap<>();
        map.put("name", item.getName());
        map.put("store", item.getStore());
        request.source(map);
        
        indexResponse = searchClient.getClient().index(request, RequestOptions.DEFAULT);
        System.out.println("Adding keyword document:");
        System.out.println(indexResponse);
		
	}
	
    private Map<String, Object> parameters(Object obj) {
        Map<String, Object> map = new HashMap<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try { map.put(field.getName(), field.get(obj)); } catch (Exception e) { }
        }
        return map;
    }
    
    class KeywordIndex {
    	
    	private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}


    	
    }

	@Override
	public void index(List<IndexItem> item) throws Exception {
		item.stream().forEach(i -> {
			try {
				this.index(i);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		});
		
	}


	@Override
	public void delete(List<String> languages, Long id) throws Exception {
		
		if(searchClient == null) {
			throw new Exception("OpenSearch client has not been initialized. Please run configure(SearchConfiguration) before trying to index.");
		}
		
		Validate.notNull(languages, "languages cannot be null");
		Validate.notEmpty(languages, "Languages cannot be empry");
		
		languages.stream().forEach(l -> {
			try {
				this.deleteDocument(l.toLowerCase(), id);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});


		
		
	}
	
	private void deleteDocument(String language, Long documentId) throws Exception {
		
		StringBuilder productsIndex = new StringBuilder().append(PRODUCTS_INDEX).append(language);
		StringBuilder keywordIndex = new StringBuilder().append(KEYWORDS_INDEX).append(language);
		
		
		
        DeleteRequest deleteDocumentRequest = new DeleteRequest(productsIndex.toString(), String.valueOf(documentId));
        DeleteResponse deleteResponse = searchClient.getClient().delete(deleteDocumentRequest, RequestOptions.DEFAULT);
        
        deleteDocumentRequest = new DeleteRequest(keywordIndex.toString(), String.valueOf(documentId));
        deleteResponse = searchClient.getClient().delete(deleteDocumentRequest, RequestOptions.DEFAULT);
		
	}


	@Override
	public SearchResponse searchKeywords(SearchRequest searchRequest) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public SearchResponse searchProducts(SearchRequest searchRequest) throws Exception {
		
		
		Validate.notNull(searchRequest, "SearchRequest must not be null");
		Validate.notNull(searchRequest.getLanguage(), "SearchRequest.language must not be null");
		Validate.notNull(searchRequest.getStore(), "SearchRequest.stoe must not be null");
		
		BoolQueryBuilder builder = QueryBuilders.boolQuery();
		builder.must(//TODO Boost
				QueryBuilders.multiMatchQuery(searchRequest.getSearchString(), new String[]{"name", "description"})
					);
		builder.filter(QueryBuilders.termQuery("store", searchRequest.getStore()));
		
		
		if(!CollectionUtils.isEmpty(searchRequest.getFilters())) {
			searchRequest.getFilters().stream().forEach(f -> this.buildFilter(f, builder));
		}

		
		//aggregations
		
		
		org.opensearch.action.search.SearchRequest search = new org.opensearch.action.search.SearchRequest( new StringBuilder().append(this.PRODUCTS_INDEX).append(searchRequest.getLanguage()).toString());
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(builder);
		search.source(searchSourceBuilder);
		
		org.opensearch.action.search.SearchResponse searchResponse = searchClient.getClient().search(search,RequestOptions.DEFAULT);
		RestStatus status = searchResponse.status();
		
		//check status
		
		SearchHits hits = searchResponse.getHits();
		
		for (SearchHit hit : hits) {
			Map<String, Object> sourceAsMap = hit.getSourceAsMap();
			System.out.println(hit.getId());
			/**
			 *                 String index = hit.getIndex();
                String type = hit.getType();
                String id = hit.getId();
                float score = hit.getScore();
                // end::search-hits-singleHit-properties
                // tag::search-hits-singleHit-source
                String sourceAsString = hit.getSourceAsString();
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                String documentTitle = (String) sourceAsMap.get("title");
                List<Object> users = (List<Object>) sourceAsMap.get("user");
                Map<String, Object> innerObject = (Map<String, Object>) sourceAsMap.get("innerObject");
			 */
		}
		
		return null;
	}
	
	private void buildFilter(SearchFilter filter, BoolQueryBuilder builder) {
		
		TermQueryBuilder b = QueryBuilders.termQuery(filter.getField(),  filter.getValue());
		
		if(filter.isVariant()) {
			builder.filter(
			QueryBuilders
		    		.nestedQuery("variants", b, ScoreMode.None)
		    );

		} else {
			builder.filter(b);
		}
		
	}

    

    

}

