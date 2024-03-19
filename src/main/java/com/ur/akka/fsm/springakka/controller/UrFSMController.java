package com.ur.akka.fsm.springakka.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletionStage;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ur.akka.fsm.springakka.fsm.UrFSM;
import com.ur.akka.fsm.springakka.fsm.UrFSM.UrFSMResponse;
import com.ur.akka.fsm.springakka.state.Accounting;
import com.ur.akka.fsm.springakka.state.Invoice;
import com.ur.akka.fsm.springakka.state.Tender;
import com.ur.akka.fsm.springakka.state.URState;
import com.ur.akka.fsm.springakka.state.UrKey;

import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.pattern.StatusReply;

@RestController
public class UrFSMController {
	
org.slf4j.Logger log = LoggerFactory.getLogger(this.getClass());
private final Duration askTimeout = Duration.ofSeconds(60);
	

	@Autowired
	Environment env;
	
	
	@Autowired
	ClusterSharding sharding; 
	
	
	
	@GetMapping("/currentState")
	public CompletionStage<UrFSM.UrFSMResponse> getState(@RequestBody UrKey urkey) {
		log.info("Called /currentState");
		
		EntityRef<UrFSM.UrFSMCommand> entityRef = sharding.entityRefFor(UrFSM.typeKey, urkey.orderId());

		CompletionStage<UrFSM.UrFSMResponse> result =
				entityRef.ask(replyTo -> new UrFSM.GetState(replyTo, urkey), askTimeout);
		
		return result;
		
	}
	
	@PutMapping("/order/{orderId}")
	public CompletionStage<UrFSM.UrFSMResponse> addInvoice(@RequestBody Invoice invoice, @PathVariable String orderId) {
		log.info("Called add Invoice order-id {} with invoice-id {}", orderId, invoice.inoviceId());

		EntityRef<UrFSM.UrFSMCommand> entityRef = sharding.entityRefFor(UrFSM.typeKey, orderId);

		CompletionStage<UrFSM.UrFSMResponse> result = entityRef.ask(
				replyTo -> new UrFSM.AddInvoice(replyTo, invoice, new UrKey(orderId, invoice.inoviceId(), null)), askTimeout);

		return result;

	}
	
	@PutMapping("/order/{orderId}/invoice/{invoiceId}")
	public CompletionStage<UrFSM.UrFSMResponse> addAccounting(@RequestBody Accounting accounting,
			@PathVariable String orderId, @PathVariable String invoiceId) {
		log.info("Called add Invoice order-id {} with invoice-id {}", orderId, invoiceId);

		EntityRef<UrFSM.UrFSMCommand> entityRef = sharding.entityRefFor(UrFSM.typeKey, orderId);

		CompletionStage<UrFSM.UrFSMResponse> result = entityRef.ask(replyTo -> new UrFSM.AddAccounting(replyTo,
				accounting, new UrKey(orderId, invoiceId, accounting.paymentHandle())), askTimeout);

		return result;

	}
	

}
