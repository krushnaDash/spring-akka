package com.ur.akka.fsm.springakka.projection;
import java.util.concurrent.CompletionStage;

import com.ur.akka.fsm.springakka.state.Invoice;

import akka.Done;

public interface URProjectionRepository {

	public CompletionStage<Done> saveOrupdate(Invoice invoice, String orderId);
	
}
