package com.ur.akka.fsm.springakka.controller;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ur.akka.fsm.springakka.domain.Product;



@RestController
public class ProductController {

	org.slf4j.Logger log = LoggerFactory.getLogger(this.getClass());
	

	@Autowired
	Environment env;
	
	@GetMapping("/getProduct")
	public ResponseEntity<Product> getProduct() {
		log.info("Called /getProduct");
		return ResponseEntity.ok(new Product("Test"));
	}


}