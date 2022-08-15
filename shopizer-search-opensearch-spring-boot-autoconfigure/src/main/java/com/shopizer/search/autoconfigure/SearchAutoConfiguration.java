package com.shopizer.search.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(SearchModuleImpl.class)
@EnableConfigurationProperties(SearchConfigurationProperties.class)
public class SearchAutoConfiguration {
	
	
    private SearchConfigurationProperties properties;

    public SearchAutoConfiguration(SearchConfigurationProperties properties) {
        this.properties = properties;
    }

    @Bean
    public SearchModuleImpl searchModule(){
    	
    	
    	//singleton configuration that creates connectivity with server
    	//creates settings and mapping through the api
    	//check if settings exist
    	//create an index by store and language
    	
    	//index event after
    	/**
    	 * - save
    	 * - update
    	 * - delete product
    	 * 
    	 * event to populate a SearchProduct
    	 * Module - index
    	 */
    	
    	
    	
    	//opensearch module
    	SearchModuleImpl module = new SearchModuleImpl();

    	
    	/**
    	 * Any specific configuration ?
    	 */
    	
    	
    	/**
    	
    	
    	((ModuleStarter)module).setUniqueCode(this.properties.getUniqueCode());
    	((ModuleStarter)module).setModuleType(ModuleType.PAYMENT);
    	((ModuleStarter)module).setLogo(properties.getLogo());
    	((ModuleStarter)module).setSupportedCountry(properties.getSupportedCountry());
    	((ModuleStarter)module).setConfigurable(properties.getModuleConfiguration());
    	
    	**/
    	
    	
        return module;
    }

}
