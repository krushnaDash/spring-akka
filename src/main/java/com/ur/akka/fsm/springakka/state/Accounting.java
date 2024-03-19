package com.ur.akka.fsm.springakka.state;

import akka.serialization.jackson.JsonSerializable;

public record Accounting (

	String receivableName,
	String postingDate,
	String paymentHandle,
	String amount

)implements JsonSerializable {}
