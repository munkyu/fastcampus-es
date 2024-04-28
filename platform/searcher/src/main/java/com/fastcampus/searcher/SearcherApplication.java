package com.fastcampus.searcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SearcherApplication {
	public static void main(String[] args) {
		SpringApplication.run(SearcherApplication.class, args);
	}

}
