package com.ur.akka.fsm.springakka.state;

import akka.serialization.jackson.JsonSerializable;

public record Settlement (
	String status,	
	String amount,
	String clientMatcherId

)implements JsonSerializable {}
