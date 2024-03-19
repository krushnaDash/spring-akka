package com.ur.akka.fsm.springakka.projection;

import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ur.akka.fsm.springakka.state.Invoice;
import akka.stream.alpakka.cassandra.javadsl.CassandraSession;

import akka.Done;

/**
 * 
 * @author k0d03gd This can be replace with Spring Data for Apache Cassandra
 * @see https://spring.io/projects/spring-data-cassandra
 */

public class URProjectionRepositoryImpl implements URProjectionRepository {

	CassandraSession session;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public URProjectionRepositoryImpl(CassandraSession session) {
		super();
		this.session = session;
	}

	public static final String Keyspace = "akka_projection";
	public static final String PopularityTable = "ur_order";

	@Override
	public CompletionStage<Done> saveOrupdate(Invoice invoice, String orderId) {
		
		logger.info("Invoice with InvoiceId {} and orderId {} >>>", invoice.inoviceId(), orderId);

		return session.executeWrite(
				String.format("UPDATE %s.%s SET invoice = ? WHERE order_id = ? and invoice_id=?", Keyspace, PopularityTable),
				invoice.amount(),orderId, invoice.inoviceId(),invoice);

	}

}
