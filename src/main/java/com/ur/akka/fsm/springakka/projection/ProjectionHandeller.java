package com.ur.akka.fsm.springakka.projection;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.ur.akka.fsm.springakka.fsm.UrFSM;
import com.ur.akka.fsm.springakka.fsm.UrFSM.UrFSMEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.Done;
import akka.projection.eventsourced.EventEnvelope;
import akka.projection.javadsl.Handler;

/**
 * @author k0d03gd
 * 
 *         Cassandra projection support at-least-once/at-most-once processing
 *         semantics. This means that if the projection is restarted from a
 *         previously stored offset some elements may be processed more than
 *         once. Therefore, the Handler code must be idempotent.
 *
 */
public class ProjectionHandeller extends Handler<EventEnvelope<UrFSM.UrFSMEvent>> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private URProjectionRepository urProjectionRepository;

	public ProjectionHandeller(URProjectionRepository urProjectionRepository) {
		super();
		logger.info("UR ProjectionHandeller created");
		this.urProjectionRepository = urProjectionRepository;
	}

	@Override
	public CompletionStage<Done> process(EventEnvelope<UrFSMEvent> envelope) throws Exception {
		logger.info("Projection Process method called >>>>");
		CompletionStage<Done> dbEffect = null;

		UrFSM.UrFSMEvent event = envelope.event();

		if (event instanceof UrFSM.InvoiceAdded) {
			UrFSM.InvoiceAdded invoiceAddedevent = (UrFSM.InvoiceAdded) event;
			logger.info("Projection for the invoice added event");
			dbEffect = urProjectionRepository.saveOrupdate(invoiceAddedevent.invoice(),
					invoiceAddedevent.urKey().orderId());
		} else {
			// skip all other events, such as `getState`
			dbEffect = CompletableFuture.completedFuture(Done.getInstance());
		}

		return dbEffect;
	}

}
