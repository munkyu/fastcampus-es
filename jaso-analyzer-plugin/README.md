# jaso-analyzer-plugin
한글 자소분리 기능을 제공하는 es plugin

### Build & Packaging

* 자바 버전은 17 기준
* 빌드 방법
    ```shell
    $ sh gradlew clean build buildPluginZip
    ```

### Plugin 설치 (in Dockerfile)
```shell
COPY jaso-analyzer-plugin-8.12.0-plugin.zip /tmp/
RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install file:///tmp/jaso-analyzer-plugin-8.12.0-plugin.zip
```
### jaso token filter 테스트
```shell
curl -XGET "http://localhost:9200/_analyze?pretty=true" -H 'Content-Type: application/json' -d'
{
  "tokenizer": "whitespace",
  "filter": [
     {
      "type": "synonym",
      "synonyms": ["스벅 => 스타벅스"]
    },
    {
      "type": "jaso"
    }
  ],
  "text": "스벅 강남1호점"
}'
```