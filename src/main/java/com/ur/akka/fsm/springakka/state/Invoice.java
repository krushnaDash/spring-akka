package com.ur.akka.fsm.springakka.state;

import java.util.Set;

import akka.serialization.jackson.JsonSerializable;

public record Invoice(InvoiceState state, String inoviceId, String amount, Set<Tender> tenders,
		Set<Accounting> accountings, Set<Settlement> settlements) implements JsonSerializable {

	public Invoice copy(InvoiceState invstate, Set<Tender> invTenders, Set<Accounting> invAccountings,
			Set<Settlement> invSettlements) {
		return new Invoice(invstate, inoviceId, amount, invTenders, invAccountings, invSettlements);
	}
	
	public Invoice copy(InvoiceState invstate) {
		return new Invoice(invstate, inoviceId, amount, tenders, accountings, settlements);
	}
	
	
	public Invoice merge(Invoice newInvoice) {
		return new Invoice(state, inoviceId, newInvoice.amount, tenders, accountings, settlements);
	}
}
