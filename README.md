# DownStream using Kafka

Spring application which downstreams the data from Elasticsearch index to Kafka using Kafka's Streams API 

Here, Kafka acts as a data pipeline where source can be ingested by [this](https://github.com/GaneshJayaram97/UpStreamApp) Spring application

All the components required for installing / running this application has been packaged in a docker-compose file and details on achieving the same is present in Usage Section

## Pre-requisites

### Docker

   Docker should be installed in the host machine
   ```
   https://docs.docker.com/get-docker/
   ```

### Maven
Maven should be installed in the host machine to build the application jar. 

Recommended version for this application is >= 3.6.3

It is recommended to add the maven binaries to $PATH environment variable so that maven commands can be executed without specifying their full path

## Kafka Containers

This application downstreams the data produced by [this](https://github.com/GaneshJayaram97/UpStreamApp) application into Kafka container. The definition and packages of kafka container is present in [this](https://github.com/GaneshJayaram97/UpStreamApp) repository and kafka container(s) should be started before running this application

## Usage

  Run below commands from root directory of the project 

1. Clone this repository in local / target machine 


2. Build the application jar
    ```
    mvn clean install
    ```

3. Build and Run the containers 
   
   a. Start the Kafka containers either in Standalone or Cluster mode as mentioned [here](https://github.com/GaneshJayaram97/UpStreamApp#upstream-using-kafka) 

   b. Start Elasticsearch and Spring application containers 
   ```
   docker-compose up
   ``` 

4. To stop and remove the containers 
    
   ```
   docker-compose -f <compose-file> down
   ```
   ```
   docker container rm <container-id>
   ```

5. To update the configurations and re-deploy the containers 
   
   a. Make the required configuration changes in the application.properties
   
   b. Build the jar
      ```
      mvn clean install
      ```
   
   c. Re-Deploy the containers
      ```
      docker-compose -f <compose-file> down
      ```
      ```
      docker container rm <container-id>
      ```
      ```
      docker-compose -f <compose-file> build
      ```
      ```
      docker-compose -f <compose-file> up
      ```
      

6. To run multiple instances of spring application for consumer scaling 

   ```
   docker-compose up  --scale downstreamapp=<integer-number-of-instances>
   ```
    For Eg,
    ```
    docker-compose up  --scale downstreamapp=3
    ```


## Notes

 1. Parameters such as elasticsearch index, host, port, kafka bootstrap servers, topic, partition, replicas are configured in application.properties under src/main/resources directory. Update them based on the need and build the jar and re-deploy the containers as needed. 
 2. All containers are configured to run under a common network configuration and its definition is present in kafka-docker-compose.yml/kafka-cluster-docker-compose.yml files. Hence, start the kafka containers before starting any other containers during the first time. 
 3. Running multiple instances of downstreamapp container would add them into a single consumer group resulting the quicker processing of data streams. Assignment of topic's partition to consumer will be handled by kafka group co-ordinator. This option could be used for achieving higher throughput from the consumer end. Also this option could work well under kafka cluster where the data is being distributed among the brokers.  

## Contributing
This is a bootstrap application for streaming the data from kafka topic into elasticsearch index. Contributions to further enhance this application are always welcomed.


## License
