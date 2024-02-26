package com.ur.akka.fsm.springakka.state;

import java.util.List;

import akka.serialization.jackson.JsonSerializable;

public record Invoice (
	String amount,
	List<Tender> tenders,
	List<Accounting> accountings
) implements JsonSerializable {}
