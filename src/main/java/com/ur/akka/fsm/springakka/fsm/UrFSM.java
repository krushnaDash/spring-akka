package com.ur.akka.fsm.springakka.fsm;

import java.util.ArrayList;

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
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import akka.serialization.jackson.JsonSerializable;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EffectBuilder;

public class UrFSM extends EventSourcedBehavior<UrFSM.UrFSMCommand, UrFSM.UrFSMEvent, URState> {

	

	public static interface UrFSMCommand extends JsonSerializable {}
	public static interface UrFSMResponse extends JsonSerializable {}
	public static interface UrFSMEvent extends JsonSerializable {}

// List of command
	public static record AddInvoice(ActorRef<UrFSMResponse> replyTo, Invoice invoice, UrKey urKey) implements UrFSMCommand {}

	public static record AddAccounting(ActorRef<UrFSMResponse> replyTo, Accounting accounting, UrKey urKey)
			implements UrFSMCommand {}
	public static record AddTender(ActorRef<UrFSMResponse> replyTo, Tender tender, UrKey urKey) implements UrFSMCommand {}
	public static record GetState(ActorRef<UrFSMResponse> replyTo) implements UrFSMCommand {}

//List of Event

	public static record InvoiceAdded(ActorRef<UrFSMResponse> replyTo, Invoice invoice, UrKey urKey) implements UrFSMEvent {}

	public static record AccountingAdded(ActorRef<UrFSMResponse> replyTo, Accounting accounting, UrKey urKey)
			implements UrFSMEvent {}

	public static record TenderAdded(ActorRef<UrFSMResponse> replyTo, Tender tender, UrKey urKey) implements UrFSMEvent {}

//Response from the UR FSM

	public static record EventProcessed() implements UrFSMResponse {}

	public static record CurrentStateResponse(URState state) implements UrFSMResponse {}
	
	public static record InvoicePorcessResponse(URState state) implements UrFSMResponse {}

	public static EntityTypeKey<UrFSMCommand> typeKey = EntityTypeKey.create(UrFSMCommand.class, "URFSM");

	
    public UrFSM(ActorContext<UrFSMCommand> context, String entityId, PersistenceId persistenceId) {
		super(persistenceId);
		context.getLog().info("Starting UrFSM {}", entityId);
		
	}
	
	@Override
	public CommandHandler<UrFSMCommand, UrFSMEvent, URState> commandHandler() {
		return newCommandHandlerBuilder().forAnyState()
				.onCommand(AddInvoice.class, this::addAccounting)
				.build();
	}

	/**
	 * Effect().persist this will persist the event to event source journal
	 * For each Effect().persist the EventHandler will be called for each event 
	 * @param state
	 * @param addInvoice
	 * @return
	 */
	private EffectBuilder<UrFSMEvent, URState> addAccounting(URState state, AddInvoice addInvoice) {
		return Effect().persist(new InvoiceAdded(addInvoice.replyTo, addInvoice.invoice, addInvoice.urKey()))
				.thenRun(newState -> {
					addInvoice.replyTo.tell(new InvoicePorcessResponse(newState));
				});

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
				.onEvent(InvoiceAdded.class, (state, event) -> state.addInovice(event.invoice())).build();

	}

    //initial state
	public static Behavior<UrFSMCommand> create(String entityId, PersistenceId persistenceId) {
		return Behaviors.setup(context -> new UrFSM(context, entityId, persistenceId));

	}




	@Override
	public URState emptyState() {
		return new URState(new URSalesOrder(null, new ArrayList<Invoice>()));
	}

}
