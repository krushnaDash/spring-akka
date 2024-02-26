package com.ur.akka.fsm.springakka.state;

import akka.serialization.jackson.JsonSerializable;

public record Tender (
	String paymentHandle,
	String amount
) implements JsonSerializable{}
