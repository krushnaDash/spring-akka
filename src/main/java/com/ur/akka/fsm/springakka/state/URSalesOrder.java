package com.ur.akka.fsm.springakka.state;

import java.util.List;

import akka.serialization.jackson.JsonSerializable;

public record URSalesOrder(	String orderId, List<Invoice> invoices) implements JsonSerializable{

}
