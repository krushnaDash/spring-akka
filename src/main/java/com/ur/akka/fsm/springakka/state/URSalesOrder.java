package com.ur.akka.fsm.springakka.state;

import java.util.Map;

import akka.serialization.jackson.JsonSerializable;

public record URSalesOrder(	String orderId, Map<String,Invoice> invoices) implements JsonSerializable{

}
