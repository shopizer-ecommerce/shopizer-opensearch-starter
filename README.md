# OpenSearch starter module for Shopizer

Spring Boot starter for OpenSearch 2.X and Shopizer

  - Java Rest High Level client connect with OpenSearch 2.X cluster
  - Automatic index creation for product search and Keywords autocomplete
  - Index and delete documents
  - Search products and search as you type
  
 ## OpenSearch configuration
 
This is a quick summary configuration guide for OpenSearch based on docker-compose. It is not recommended to use this guide for a production cluster. All OpenSearch configurations  are documented here: https://opensearch.org/docs/latest/opensearch/install/index/

Quick Guide for running OpenSearch 2.x cluster (non tls) with Docker Compose

Requirements: Docker settings must have 4GB of RAM available

run ``` docker compose up -f docker-compose.yaml```

The OpenSearch cluster will start with 2 nodes on port 9200 and the administration UI

Once fully tested open a browser on port 5601 (http://localhost:560/)

Username: admin

Password: admin

Cluster Name: opensearch-cluster

## Run using the unit test

A unit test allows to verify the configuration of the cluster com.shopizer.search.opensearch.OpenSearchTest




