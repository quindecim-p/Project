package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BuyerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BuyerServiceApplication.class, args);
	}

}

// 1) CRUD (+)
// 2) настройки load balancer (+)
// 3) компенсирующие транзакции (сага через оркес.)
// 4) WebFlux
// 5) многопоточ., 2 парал. запроса, ошибка в любом из них прерыв. остальные запросы, виды пулов потоков, виртуал. потоки
// 6) функц. интерф. (можно сделать парочку своих), стримы