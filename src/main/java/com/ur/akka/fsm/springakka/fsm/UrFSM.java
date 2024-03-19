package com.ur.akka.fsm.springakka.fsm;

import static com.ur.akka.fsm.springakka.utility.AppFunction.getInvState;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import com.ur.akka.fsm.springakka.state.Accounting;
import com.ur.akka.fsm.springakka.state.Invoice;
import com.ur.akka.fsm.springakka.state.Tender;
import com.ur.akka.fsm.springakka.state.URSalesOrder;
import com.ur.akka.fsm.springakka.state.URState;
import com.ur.akka.fsm.springakka.state.UrKey;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EffectBuilder;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import akka.persistence.typed.javadsl.RetentionCriteria;
import akka.serialization.jackson.JsonSerializable;
import static com.ur.akka.fsm.springakka.utility.AppFunction.tags;

public class UrFSM extends EventSourcedBehavior<UrFSM.UrFSMCommand, UrFSM.UrFSMEvent, URState> {

	

	public static interface UrFSMCommand extends JsonSerializable {}
	public static interface UrFSMResponse extends JsonSerializable {}
	public static interface UrFSMEvent extends JsonSerializable {}

// List of command
	public static record AddInvoice(ActorRef<UrFSMResponse> replyTo, Invoice invoice, UrKey urKey) implements UrFSMCommand {}

	public static record AddAccounting(ActorRef<UrFSMResponse> replyTo, Accounting accounting, UrKey urKey)
			implements UrFSMCommand {}
	public static record AddTender(ActorRef<UrFSMResponse> replyTo, Tender tender, UrKey urKey) implements UrFSMCommand {}
	public static record GetState(ActorRef<UrFSMResponse> replyTo, UrKey urKey) implements UrFSMCommand {}

//List of Event

	public static record InvoiceAdded(ActorRef<UrFSMResponse> replyTo, Invoice invoice, UrKey urKey) implements UrFSMEvent {}

	public static record AccountingAdded(ActorRef<UrFSMResponse> replyTo, Accounting accounting, UrKey urKey)
			implements UrFSMEvent {}

	public static record TenderAdded(ActorRef<UrFSMResponse> replyTo, Tender tender, UrKey urKey) implements UrFSMEvent {}

//Response from the UR FSM

	public static record EventProcessed() implements UrFSMResponse {}

	public static record CurrentStateResponse(URState state) implements UrFSMResponse {}
	public static record CurrentOrderStateResponse(URSalesOrder salesOrder) implements UrFSMResponse {}
	public static record CurrentInvoiceStateResponse(Invoice invoice) implements UrFSMResponse {}
	
	public static record InvoicePorcessResponse(URState state) implements UrFSMResponse {}
	public static record SuccessPorcessResponse(URState state) implements UrFSMResponse {}

	public static EntityTypeKey<UrFSMCommand> typeKey = EntityTypeKey.create(UrFSMCommand.class, "URFSM");

	
    public UrFSM(ActorContext<UrFSMCommand> context, String entityId, PersistenceId persistenceId) {
		super(persistenceId);
		context.getLog().info("Starting UrFSM {}", entityId);
		
	}
	
	@Override
	public CommandHandler<UrFSMCommand, UrFSMEvent, URState> commandHandler() {
		return newCommandHandlerBuilder()
				
				.forAnyState()
				.onCommand(AddInvoice.class, this::addInvoice)
				.onCommand(AddAccounting.class, this::addAccounting)
				.onCommand(AddTender.class, this::addTender)
				.onCommand(GetState.class, this::getState)
				.build();
	}
	
	
	/**
	 * When an event has been persisted successfully the new state is created by
	 * applying the event to the current state with the eventHandler. In the case of
	 * multiple persisted events, the eventHandler is called with each event in the
	 * same order as they were passed to Effect().persist(..).
	 * 
	 * The event handler must only update the state and never perform side effects,
	 * as those would also be executed during recovery of the persistent actor. Side
	 * effects should be performed in thenRun from the command handler after
	 * persisting the event or from the RecoveryCompleted after Recovery.
	 * 
	 */
	@Override
	public EventHandler<URState, UrFSMEvent> eventHandler() {
		return newEventHandlerBuilder().forAnyState()
				.onEvent(InvoiceAdded.class, this::processEventInvoiceAdded)
				.onEvent(AccountingAdded.class, this::processEventAccoutntingAdded)
				.onEvent(TenderAdded.class, this::processEventTenderAdded)
				.build();

	}
	
	
	
	/**
	 * Reply with current state based on the key, If invoice id is passed then return the specific invoice
	 * @param state
	 * @param getState
	 * @return
	 */
	private EffectBuilder<UrFSMEvent, URState> getState(URState state, GetState getState) {
		return Effect().none()
				.thenRun (newState -> {
					if(getState.urKey.invoiceId()!=null)
						getState.replyTo.tell(new CurrentInvoiceStateResponse(
								newState.uRSalesOrder().invoices().get(getState.urKey.invoiceId())));
					else
					  getState.replyTo.tell(new CurrentOrderStateResponse(newState.uRSalesOrder()));
				});

	}
	

	private EffectBuilder<UrFSMEvent, URState> addTender(URState state, AddTender addTender) {
		return Effect().persist(new TenderAdded(addTender.replyTo, addTender.tender(), addTender.urKey))
				.thenRun (newState -> {
					addTender.replyTo.tell(new SuccessPorcessResponse(newState));
				});

	}
	
	/**
	 * Effect().persist this will persist the event to event source journal
	 * For each Effect().persist the EventHandler will be called for each event 
	 * @param state
	 * @param addInvoice
	 * @return
	 */
	private EffectBuilder<UrFSMEvent, URState> addAccounting(URState state, AddAccounting addAccounting) {
		return Effect().persist(new AccountingAdded(addAccounting.replyTo, addAccounting.accounting(), addAccounting.urKey()))
				.thenRun (newState -> {
					addAccounting.replyTo.tell(new InvoicePorcessResponse(newState));
				});

	}

	private EffectBuilder<UrFSMEvent, URState> addInvoice(URState state, AddInvoice addInvoice) {
		return Effect().persist(new InvoiceAdded(addInvoice.replyTo, addInvoice.invoice, addInvoice.urKey()))
				.thenRun (newState -> {
					addInvoice.replyTo.tell(new SuccessPorcessResponse(newState));
				});

	}
	
	
	
	/**
	 * The original state is tracked at Invoice level, hence we need the event to identify the Invoice 
	 * which state needs to check for event for Event handle
	 * @param state
	 * @param event
	 * @return
	 */
	public URState processEventInvoiceAdded(URState state, InvoiceAdded event) {

		switch (getInvState(state, event.urKey().invoiceId())) {
		case InitialState: {
			state.addInovice(event.invoice());
			return state;
		}
		case IncompleteState: {
			// add logic
			return state;
		}
		case ReconciledState: {
			// Add logic
			return state;
		}
		default:
			return state;
		}

	}
	
	private URState processEventTenderAdded(URState state, TenderAdded event){
		switch (getInvState(state, event.urKey().invoiceId())) {

		case InitialState: {
			state.addTender(event.urKey.invoiceId(), event.tender);
			return state;
		}
		case IncompleteState: {
			// add logic
			return state;
		}
		case ReconciledState: {
			// Add logic
			return state;
		}

		default:
			return state;
		}
	}
	
	
	public URState processEventAccoutntingAdded(URState state, AccountingAdded event) {
		switch (getInvState(state, event.urKey().invoiceId())) {

		case InitialState: {
			state.addAccounting(event.urKey.invoiceId(), event.accounting());
			return state;
		}
		case IncompleteState: {
			// add logic
			return state;
		}
		case ReconciledState: {
			// Add logic
			return state;
		}

		default:
			return state;
		}

	}


    //initial state
	public static Behavior<UrFSMCommand> create(String entityId, PersistenceId persistenceId) {
		return Behaviors.setup(context -> new UrFSM(context, entityId, persistenceId));

	}
	

	@Override
	public URState emptyState() {
		return new URState(new URSalesOrder(persistenceId().entityId(), new HashMap<>()));
	}
	
	@Override // override retentionCriteria in EventSourcedBehavior 
	//https://doc.akka.io/docs/akka/current/typed/persistence-snapshot.html
	public RetentionCriteria retentionCriteria() {
	  // for test we have make it 10, it should be 100 at least
		return RetentionCriteria.snapshotEvery(10, 2).withDeleteEventsOnSnapshot();
	}

	/**
	 * Each entity instance will tag its events using one of those tags. The tag is
	 * selected based on the modulo of the entity id’s hash code (stable identifier)
	 * and the total number of tags. As a matter of fact, this will create a journal
	 * sliced with different tags (ie: from URTag-1 to URTag-50).
	 * 
	 * 
	 * When using Akka Persistence Cassandra plugin you should not use too many tags for each event. 
	 * Each tag will result in a copy of the event in a separate table and that can impact write performance. 
	 * Typically, you would use 1 tag per event as done below. Additional filtering of events can be done in the Projection handler 
	 * if it doesn’t have to act on certain events. The JDBC plugin don’t have this constraint.
	 */
	
	@Override
	public Set<String> tagsFor(UrFSMEvent event) {
		String entityId=persistenceId().entityId();
		int n = Math.abs(entityId.hashCode() % tags.size());
		  String selectedTag = tags.get(n);
		  return Collections.singleton(selectedTag);
	}
	
	/**
	 * We will add code for this
	 */

	@Override
	public Set<String> tagsFor(URState state, UrFSMEvent event) {
		String entityId = persistenceId().entityId();
		int n = Math.abs(entityId.hashCode() % tags.size());
		String selectedTag = tags.get(n);
		return Collections.singleton(selectedTag);
	}
	
	
	

}
