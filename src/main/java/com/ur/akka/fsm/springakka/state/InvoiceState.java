package com.ur.akka.fsm.springakka.state;

import akka.serialization.jackson.JsonSerializable;

public enum InvoiceState implements JsonSerializable {
	InitialState,IncompleteState,ReconciledState
}
