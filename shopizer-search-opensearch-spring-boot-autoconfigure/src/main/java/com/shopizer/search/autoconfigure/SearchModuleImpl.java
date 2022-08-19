package com.shopizer.search.autoconfigure;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import modules.commons.search.SearchModule;
import modules.commons.search.configuration.SearchConfiguration;
import modules.commons.search.request.Aggregation;
import modules.commons.search.request.IndexItem;
import modules.commons.search.request.SearchFilter;
import modules.commons.search.request.SearchItem;
import modules.commons.search.request.SearchRequest;
import modules.commons.search.request.SearchResponse;

public class SearchModuleImpl implements SearchModule {

	

	private String uniqueCode = "opensearch";
	private SearchClient searchClient = null;
	
	private final static String PRODUCTS_INDEX = "products_";
	private final static String KEYWORDS_INDEX = "keywords_";
	private final static String DEFAULT_AGGREGATION = "aggregations";



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
        map.put("category", item.getCategory());
        map.put("brand", item.getBrand());
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
		Validate.notNull(searchRequest, "SearchRequest must not be null");
		Validate.notNull(searchRequest.getLanguage(), "SearchRequest.language must not be null");
		Validate.notNull(searchRequest.getStore(), "SearchRequest.stoe must not be null");
		
		BoolQueryBuilder builder = QueryBuilders.boolQuery();
		builder.must(//TODO Boost TODO suggest
				QueryBuilders.multiMatchQuery(searchRequest.getSearchString(), new String[]{"name", "brand", "category"})
					);
		builder.filter(QueryBuilders.termQuery("store", searchRequest.getStore()));
		
		org.opensearch.action.search.SearchRequest search = new org.opensearch.action.search.SearchRequest( new StringBuilder().append(KEYWORDS_INDEX).append(searchRequest.getLanguage()).toString());
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(builder);
		
		org.opensearch.action.search.SearchResponse searchResponse = searchClient.getClient().search(search,RequestOptions.DEFAULT);
		RestStatus status = searchResponse.status();
		
		//check status
		if(status.getStatus() != 200) {
			throw new Exception("Search Response failed [" + status.getStatus() + "]");
		}
		
		SearchHits hits = searchResponse.getHits();
		
		
		return null;
	}


	@Override
	public SearchResponse searchProducts(SearchRequest searchRequest) throws Exception {
		
		
		Validate.notNull(searchRequest, "SearchRequest must not be null");
		Validate.notNull(searchRequest.getLanguage(), "SearchRequest.language must not be null");
		Validate.notNull(searchRequest.getStore(), "SearchRequest.stoe must not be null");
		
		BoolQueryBuilder builder = QueryBuilders.boolQuery();
		builder.must(//TODO Boost
				QueryBuilders.multiMatchQuery(searchRequest.getSearchString(), new String[]{"name", "description", "brand", "category"})
					);
		builder.filter(QueryBuilders.termQuery("store", searchRequest.getStore()));
		
		
		if(!CollectionUtils.isEmpty(searchRequest.getFilters())) {
			searchRequest.getFilters().stream().forEach(f -> this.buildFilter(f, builder));
		}

		TermsAggregationBuilder aggregation = null;
		
		//aggregations
		if(!CollectionUtils.isEmpty(searchRequest.getAggregations())) {
			aggregation = AggregationBuilders.terms(DEFAULT_AGGREGATION);
			for(String agg : searchRequest.getAggregations()) {
				aggregation.field(agg);
			}
		}
        //TermsAggregationBuilder aggregation = AggregationBuilders.terms("aggregations")
        //        .field("company.keyword").field("");
		
		
		org.opensearch.action.search.SearchRequest search = new org.opensearch.action.search.SearchRequest( new StringBuilder().append(PRODUCTS_INDEX).append(searchRequest.getLanguage()).toString());
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(builder);
		if(aggregation != null) {
			searchSourceBuilder.aggregation(aggregation);
		}
		search.source(searchSourceBuilder);
		

		
		org.opensearch.action.search.SearchResponse searchResponse = searchClient.getClient().search(search,RequestOptions.DEFAULT);
		RestStatus status = searchResponse.status();
		
		//check status
		if(status.getStatus() != 200) {
			throw new Exception("Search Response failed [" + status.getStatus() + "]");
		}
		
		SearchHits hits = searchResponse.getHits();
		

		
		SearchResponse serviceResponse = new SearchResponse();
		serviceResponse.setCount(hits.getTotalHits().value);
		
		for (SearchHit hit : hits) {
			Map<String, Object> sourceAsMap = hit.getSourceAsMap();
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			
			SearchItem item = mapper.convertValue(sourceAsMap, SearchItem.class);
			
			serviceResponse.getItems().add(item);
			
		}
		
		Aggregations aggregations = searchResponse.getAggregations();

		
		Terms aggregate = aggregations.get(DEFAULT_AGGREGATION);

		//get count per aggregations
		for(Bucket bucket : aggregate.getBuckets()) {
			Aggregation agg = new Aggregation();
			agg.setCount(bucket.getDocCount());
			agg.setName(bucket.getKeyAsString());
			serviceResponse.getAggregations().add(agg);
		}
		

		return serviceResponse;
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


	@Override
	public Object getConnection() {
		// TODO Auto-generated method stub
		try {
			return this.searchClient.getClient();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}

    

    

}

