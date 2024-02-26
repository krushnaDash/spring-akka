package com.ur.akka.fsm.springakka.state;

import akka.serialization.jackson.JsonSerializable;

public record URState(
		URSalesOrder uRSalesOrder
) implements JsonSerializable{
	
	public URState addInovice(Invoice invoice) {
		uRSalesOrder.invoices().add(invoice);
		return this;
	}
	
}
