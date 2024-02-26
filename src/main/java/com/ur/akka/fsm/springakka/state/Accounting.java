package com.ur.akka.fsm.springakka.state;

import akka.serialization.jackson.JsonSerializable;

public record Accounting (

	String accountingReceivableName,
	String PostingDate,
	String paymentHandle,
	String amount

)implements JsonSerializable {}
