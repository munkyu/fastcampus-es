FROM docker.elastic.co/elasticsearch/elasticsearch:8.12.0

COPY analysis-nori-8.12.0.zip /tmp/

RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install file:///tmp/analysis-nori-8.12.0.zip

