package com.ur.akka.fsm.springakka.state;

import java.util.HashSet;

import akka.serialization.jackson.JsonSerializable;

public record URState(URSalesOrder uRSalesOrder) implements JsonSerializable {

	public URState addInovice(Invoice invoice) {
		
		Invoice existingInvoice = uRSalesOrder.invoices().get(invoice.inoviceId());
		if(existingInvoice == null) {
			uRSalesOrder.invoices().put(invoice.inoviceId(), invoice.copy(InvoiceState.InitialState, new HashSet<>(), new HashSet<>(),
					new HashSet<>()));
		}else {
			uRSalesOrder.invoices().put(invoice.inoviceId(),existingInvoice.merge(invoice));
		}
		return this;
	}

	public URState addAccounting(String invoiceId, Accounting accounting) {
		Invoice invoice = uRSalesOrder.invoices().get(invoiceId);
		if (invoice == null) {
			invoice = new Invoice(InvoiceState.InitialState, invoiceId, null, new HashSet<>(), new HashSet<>(),
					new HashSet<>());
			uRSalesOrder.invoices().put(invoiceId, invoice);

		}
		invoice.accountings().add(accounting);

		return this;
	}
	
	public URState addTender(String invoiceId, Tender tender) {
		// @TOdo
		return this;
	}
	
	public URState addSettlement(String invoiceId, Settlement settlement) {
		// @Todo
		return this;
	}

}
