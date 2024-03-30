FROM docker.elastic.co/elasticsearch/elasticsearch:8.12.0


COPY analysis-nori-8.12.0.zip /tmp/

# custom nori 용
#COPY analysis-nori-8.12.0-SNAPSHOT.zip /tmp/

# jaso analyzer 복사
COPY jaso-analyzer-plugin-8.12.0-plugin.zip /tmp/

RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install file:///tmp/analysis-nori-8.12.0.zip

# custom nori 용
#RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install file:///tmp/analysis-nori-8.12.0-SNAPSHOT.zip

RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install file:///tmp/jaso-analyzer-plugin-8.12.0-plugin.zip
