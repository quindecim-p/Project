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
// 2) Компенсирующие транзакции / сага через оркес. (+)
// 3) Функц. интерф., стримы (+)
// 4) Многопоточ: виды пулов потоков, виртуал. потоки / 2 парал. запроса, ошибка в любом из них прерыв. остальные запросы
// 5) WebFlux
