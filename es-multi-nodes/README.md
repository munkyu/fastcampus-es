# elasticsearch 실습 세팅 (multi nodes)

### docker build
```shell
docker build -t fastcampus-es-multi-nodes . 
```

### docker run
```shell
docker-compose up -d
```

### docker stop
```shell
docker-compose down
```

### elasticsearch 접속 url
* http://localhost:9200

### kibana 접속 url. id, password는 .env 파일 참조
* http://localhost:5601