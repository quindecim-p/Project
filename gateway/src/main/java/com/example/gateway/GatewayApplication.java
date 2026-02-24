package com.example.gateway;

import com.example.gateway.config.CustomLoadBalancerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;

@SpringBootApplication
@LoadBalancerClients({
		@LoadBalancerClient(name = "BUYER-SERVICE", configuration = CustomLoadBalancerConfiguration.class),
		@LoadBalancerClient(name = "HOUSE-SERVICE", configuration = CustomLoadBalancerConfiguration.class)
})
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

}
