package com.ur.akka.fsm.springakka.utility;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.ur.akka.fsm.springakka.state.Invoice;
import com.ur.akka.fsm.springakka.state.InvoiceState;
import com.ur.akka.fsm.springakka.state.URState;

public class AppFunction {
	
	public static final String UR_TAG="URTag";
	
	
	/**
	 * As a rule of thumb, the number of tags should be a factor of ten greater than
	 * the planned maximum number of cluster nodes. It doesn’t have to be exact
	 * for local test it is set to 3, it should be 50 for prod
	 */
	public static final List<String> tags = Collections
			.unmodifiableList(IntStream.rangeClosed(1, 3).mapToObj(i -> UR_TAG+"-" + i).collect(Collectors.toList()));
	


	/**
	 * @param state
	 * @param event
	 * @return the Invoice state with null check
	 */
	public static InvoiceState getInvState(URState state, String invoiceId) {
		Invoice existingInvoice = state.uRSalesOrder().invoices().get(invoiceId);
		return existingInvoice == null ? InvoiceState.InitialState : existingInvoice.state();

	}
	
	

}
