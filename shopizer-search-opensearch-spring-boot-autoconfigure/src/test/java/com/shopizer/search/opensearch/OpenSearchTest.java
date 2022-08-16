package com.shopizer.search.opensearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import modules.commons.search.SearchModule;
import modules.commons.search.configuration.SearchConfiguration;
import modules.commons.search.request.IndexItem;
import modules.commons.search.request.SearchFilter;
import modules.commons.search.request.SearchRequest;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes=SearchProperties.class)
@EnableConfigurationProperties(SearchProperties.class)
public class OpenSearchTest {


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
			 * Init
			 */
			
			searchModule.configure(config());
			
			/**
			 * Test index product
			 */

			
			List<IndexItem> items = new ArrayList<IndexItem>();
			
			IndexItem item = new IndexItem();
			item.setId(1L);
			item.setLanguage("en");
			item.setBrand("nike");
			item.setCategory("shoes");
			
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
			
			searchModule.searchProducts(request);
			
			System.out.println("TBD");
			
			
			/**
			 * Test search keywords
			 */
			
			/**
			 * Test delete document
			 */
			
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
		
		config.getProductMappings().put("variants", "nested");
		config.getProductMappings().put("brand", "keyword");
		config.getProductMappings().put("store", "keyword");
		config.getProductMappings().put("category", "text");
		config.getProductMappings().put("name", "text");
		config.getProductMappings().put("description", "text");
		config.getProductMappings().put("price", "float");
		config.getProductMappings().put("id", "long");
		
		config.getKeywordsMappings().put("name", "text");
		config.getKeywordsMappings().put("store", "keyword");
		config.getKeywordsMappings().put("id", "long");
		
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
	

	
	/**
	 * Test search keywords
	 */
	
	/**
	 * Test search product
	 */
	
	/**
	 * Test delete document
	 */

}
