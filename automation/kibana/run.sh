docker run --name es01 --net elastic -p 9200:9200 --restart=on-failure --memory 4G -d docker.elastic.co/elasticsearch/elasticsearch:8.5.3
docker run --name kib-01 --net elastic -p 5601:5601 --restart=on-failure -d docker.elastic.co/kibana/kibana:8.5.3

