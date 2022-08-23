package com.shopizer.search.opensearch;

import java.util.List;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import modules.commons.search.configuration.Credentials;
import modules.commons.search.configuration.SearchHost;

@Configuration
@ConfigurationProperties(prefix = "search") 
@EnableAutoConfiguration
@ComponentScan({"com.shopizer.search"})
public class SearchProperties {
	
    private String clusterName;
    private Credentials credentials;
    private List<SearchHost> host;
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	public Credentials getCredentials() {
		return credentials;
	}
	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}
	public List<SearchHost> getHost() {
		return host;
	}
	public void setHost(List<SearchHost> host) {
		this.host = host;
	}


}
