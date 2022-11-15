package com.shopizer.search.opensearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import modules.commons.search.SearchModule;
import modules.commons.search.configuration.SearchConfiguration;
import modules.commons.search.request.IndexItem;
import modules.commons.search.request.SearchFilter;
import modules.commons.search.request.SearchRequest;
import modules.commons.search.request.SearchResponse;

/**
 * Creates index
 * Indexes an item in product and keywords indexes
 * Search item
 * Search keywords
 * Delete indexx
 * @author carlsamson
 *
 */

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes=SearchProperties.class)
@EnableConfigurationProperties(SearchProperties.class)
public class OpenSearchTest {
	
	
	private final static String KEYWORDS_MAPPING_DEFAULT = "{\"properties\":"
			+ "      {\n\"id\": {\n"
			+ "        \"type\": \"long\"\n"
			+ "      }\n"
			+ "     }\n"
			+ "    }";	
	
	private final static String PRODUCT_MAPPING_DEFAULT = "{\"properties\":"
			+ "      {\n\"id\": {\n"
			+ "        \"type\": \"long\"\n"
			+ "      },\n\"price\": {\n"
			+ "        \"type\": \"float\"\n"
			+ "      },\n\"description\": {\n"
			+ "        \"type\": \"text\"\n"
			+ "      },\n\"name\": {\n"
			+ "        \"type\": \"text\"\n"
			+ "      },\n\"category\": {\n"
			+ "        \"type\": \"keyword\", \n"
			+ "        \"normalizer\": \"custom_normalizer\"\n"
			+ "      },\n\"brand\": {\n"
			+ "        \"type\": \"keyword\" ,\n"
			+ "        \"normalizer\": \"custom_normalizer\"\n"
			+ "      },\n\"attributes\": {\n"
			+ "        \"type\": \"nested\"\n"
			+ "      },\n\"variants\": {\n"
			+ "        \"type\": \"nested\" \n"
			+ "      },\n\"store\": {\n"
			+ "        \"type\": \"keyword\" \n"
			+ "      },\n\"reviews\": {\n"
			+ "        \"type\": \"keyword\" \n"
			+ "      },\n\"image\": {\n"
			+ "        \"type\": \"keyword\"\n"
			+ "      }\n"
			+ "    }\n"
			+ "  }";	
	private final static String SETTING_DEFAULT = "{\"analysis\": {\n"
			+ "      \"normalizer\": {\n"
			+ "        \"custom_normalizer\": {\n"
			+ "          \"type\": \"custom\",\n"
			+ "          \"char_filter\": [],\n"
			+ "          \"filter\": [\"lowercase\", \"asciifolding\"]\n"
			+ "        }\n"
			+ "      }\n"
			+ "    }\n"
			+ "  }";
	private final static String SETTING_en = "{\"analysis\": {\n"
			+ "      \"normalizer\": {\n"
			+ "        \"custom_normalizer\": {\n"
			+ "          \"type\": \"custom\",\n"
			+ "          \"char_filter\": [],\n"
			+ "          \"filter\": [\"lowercase\", \"asciifolding\"]\n"
			+ "        }\n"
			+ "      }\n"
			+ "    }\n"
			+ "  }";



	@Autowired
	private SearchModule searchModule;
	
	@Autowired
	private SearchProperties searchProperties;
	
	@Test
	public void contextLoads() { }
	
	@Test
	public void testSearch() {
		
		
		try {
			
			/**
			 * Init - connect, creates index
			 */
			
			searchModule.configure(config());
			
			Thread.sleep(500);
			
			/**
			 * Test index product
			 */

			
			List<IndexItem> items = new ArrayList<IndexItem>();
			
			IndexItem item = new IndexItem();
			item.setId(1L);
			item.setLanguage("en");
			item.setBrand("Nike");
			item.setCategory("Shoes");
			
			item.setName("Nike Zoom Fly 5");
			item.setDescription("Bridge the gap between your weekend training run and race day in a durable design that can be deployed not just at the starting line of your favourite race but in the days and months after your conquest.");
			item.setPrice("109.00");
			item.setStore("default");
			
			
			//option size
			Map<String,String> smallWhite = new HashMap<String,String>();
			smallWhite.put("size", "S");
			smallWhite.put("color", "white");
			
			Map<String,String> mediumWhite = new HashMap<String,String>();
			mediumWhite.put("size", "M");
			mediumWhite.put("color", "white");
			
			Map<String,String> largeWhite = new HashMap<String,String>();
			largeWhite.put("size", "L");
			largeWhite.put("color", "white");
			
			List<Map<String,String>> variants = new ArrayList<Map<String,String>>();
			variants.add(smallWhite);
			variants.add(mediumWhite);
			variants.add(largeWhite);

			item.setVariants(variants);
			
			items.add(item);
			
			try {
				searchModule.index(items);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Thread.sleep(500);
			
			/**
			 * Test search product
			 */
			SearchRequest request = new SearchRequest();
			request.setSearchString("Nike");
			request.setStore("default");
			request.setLanguage("en");
			
			List<SearchFilter> filters = new ArrayList<SearchFilter>();
			
			SearchFilter searchFilter = new SearchFilter();
			searchFilter.setField("variants.size.keyword");
			searchFilter.setValue("L");
			searchFilter.setVariant(true);
			
			filters.add(searchFilter);
			
			request.setFilters(filters);
			
			List<String> aggregations = new ArrayList<String>();
			aggregations.add("brand");
			
			request.setAggregations(aggregations);
			
			SearchResponse searchResponse = searchModule.searchProducts(request);
			
			System.out.println("Search results **************");
			
	        ObjectMapper mapper = new ObjectMapper();
	        String jsonResult = mapper.writerWithDefaultPrettyPrinter()
	          .writeValueAsString(searchResponse);
	        System.out.println(jsonResult);

			searchResponse.getItems();
			searchResponse.getAggregations();
			
			
			/**
			 * Test search keywords
			 */
			
			request = new SearchRequest();
			request.setSearchString("Fly");
			request.setStore("default");
			request.setLanguage("en");

			
			searchResponse = searchModule.searchKeywords(request);
			
			System.out.println("Keyword results **************");
			
	        mapper = new ObjectMapper();
	        jsonResult = mapper.writerWithDefaultPrettyPrinter()
	          .writeValueAsString(searchResponse);
	        System.out.println(jsonResult);

					
			
			/**
			 * Test delete document
			 */
			searchResponse.getItems().stream().forEach(i -> {
				try {
					searchModule.delete(List.of("en"), i.getId());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});

			
			/**
			 * Delete index
			 */
			DeleteIndexRequest deleteIndex = new DeleteIndexRequest("products_en");
			((RestHighLevelClient)(searchModule.getConnection())).indices().delete(deleteIndex, RequestOptions.DEFAULT);
			
			deleteIndex = new DeleteIndexRequest("keywords_en");
			((RestHighLevelClient)(searchModule.getConnection())).indices().delete(deleteIndex, RequestOptions.DEFAULT);

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	
	private SearchConfiguration config() {
		
		
		SearchConfiguration config = new SearchConfiguration();
		config.setClusterName(searchProperties.getClusterName());
		config.setHosts(searchProperties.getHost());
		config.setCredentials(searchProperties.getCredentials());
		
		List<String> langs = new ArrayList<String>();
		langs.add("en");
		
		config.setLanguages(langs);
		
		config.getProductMappings().put("en", PRODUCT_MAPPING_DEFAULT);
		config.getKeywordsMappings().put("en", KEYWORDS_MAPPING_DEFAULT);
		config.getSettings().put("en", SETTING_DEFAULT);
		
		/**
		config.getProductMappings().put("variants", "nested");
		config.getProductMappings().put("brand", "keyword");
		config.getProductMappings().put("store", "keyword");
		config.getProductMappings().put("category", "text");
		config.getProductMappings().put("name", "text");
		config.getProductMappings().put("description", "text");
		config.getProductMappings().put("price", "float");
		config.getProductMappings().put("id", "long");
		
		config.getKeywordsMappings().put("store", "keyword");
		**/
		
		/**
		 * Suggested mapping
		 * 		  "variants" : {
			        "type": "nested"
			      },
			      "brand" : {
			        "type": "keyword"
			      },
			      "store" : {
			        "type": "keyword"
			      },
			      "category" : {
			        "type": "text"
			      },
			      "name" : {
			        "type": "text"
			      },
			      "description" : {
			        "type": "text"
			      },
			      "price": {
          			"type": "float"
        		  },
        		"id": {
          			"type": "long"
        		}
		 */
		
		return config;
		
	}
	

	

}
