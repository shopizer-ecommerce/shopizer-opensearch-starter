package com.shopizer.search.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("search.opensearch")
public class SearchConfigurationProperties {
	
	/**
	 * This will set basic properties to the module if any are required
	 */
	

	/*
	 * private String uniqueCode="payment.square";
	 * 
	 * public String getUniqueCode() { return uniqueCode; }
	 * 
	 * 
	 * public String getLogo() {
	 * 
	 * String logo = null; try {
	 * 
	 * Resource resource = new ClassPathResource("square.png"); if(resource != null)
	 * { InputStream is = resource.getInputStream(); if(is != null) { byte[] encoded
	 * = IOUtils.toByteArray(is); logo = Base64 .getEncoder()
	 * .encodeToString(encoded); } }
	 * 
	 * 
	 * 
	 * } catch(Exception e) { e.printStackTrace(); }
	 * 
	 * return logo; }
	 * 
	 * public String getModuleConfiguration() {
	 * 
	 * String moduleConfiguration = null; try {
	 * 
	 * Resource resource = new ClassPathResource("configuration.json"); if(resource
	 * != null) { InputStream is = resource.getInputStream(); if(is != null) {
	 * byte[] ba = IOUtils.toByteArray(is); moduleConfiguration = new String(ba); }
	 * }
	 * 
	 * 
	 * 
	 * } catch(Exception e) { e.printStackTrace(); }
	 * 
	 * return moduleConfiguration;
	 * 
	 * }
	 */



}
