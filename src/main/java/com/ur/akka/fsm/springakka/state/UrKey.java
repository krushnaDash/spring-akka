package com.ur.akka.fsm.springakka.state;

import akka.serialization.jackson.JsonSerializable;

public record UrKey(String orderId, String invoiceId, String paymentHandle) implements JsonSerializable {

}
