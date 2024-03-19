package com.ur.akka.fsm.springakka.projection;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ur.akka.fsm.springakka.fsm.UrFSM;
import com.ur.akka.fsm.springakka.utility.AppFunction;

import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.javadsl.ShardedDaemonProcess;
import akka.persistence.cassandra.query.javadsl.CassandraReadJournal;
import akka.persistence.query.Offset;
import akka.projection.HandlerRecoveryStrategy;
import akka.projection.Projection;
import akka.projection.ProjectionBehavior;
import akka.projection.ProjectionId;
import akka.projection.cassandra.javadsl.CassandraProjection;
import akka.projection.eventsourced.EventEnvelope;
import akka.projection.eventsourced.javadsl.EventSourcedProvider;
import akka.projection.javadsl.AtLeastOnceProjection;
import akka.projection.javadsl.SourceProvider;
import akka.stream.alpakka.cassandra.javadsl.CassandraSession;
import akka.stream.alpakka.cassandra.javadsl.CassandraSessionRegistry;
import jakarta.annotation.PostConstruct;


@Component
public class URStateProcessor {

	@Autowired
	ActorSystem<?> system;
	
	CassandraSession session;
	URProjectionRepository repo;
	
	

	// should it be 100 to group the events and do a batch update 
	int saveOffsetAfterEnvelopes = 1;
	Duration saveOffsetAfterDuration = Duration.ofMillis(500);

	public SourceProvider<Offset, EventEnvelope<UrFSM.UrFSMEvent>> sourceProvider(String tag) {
		return EventSourcedProvider.eventsByTag(system, CassandraReadJournal.Identifier(), tag);
	}

	
	/**
	 * Which projection Offset should be used, considering the huge volume of events
	 * around 3k events per seconds
	 * 
	 * -> Cassandra
	 * -> relational DB with JDBC
	 * -> Offset in a relational DB with 
	 * -> Offset in a relational DB with Slick (only Scala support) ??
	 * @param tag
	 * @return
	 */
	public AtLeastOnceProjection<Offset,EventEnvelope<UrFSM.UrFSMEvent>> projection(String tag) {

		return CassandraProjection
				.atLeastOnce(ProjectionId.of(AppFunction.UR_TAG, tag), sourceProvider(tag), () -> new ProjectionHandeller(repo))
				.withSaveOffset(saveOffsetAfterEnvelopes, saveOffsetAfterDuration)
				.withRecoveryStrategy(HandlerRecoveryStrategy.retryAndFail(1, Duration.ofSeconds(1)))
				.withRestartBackoff(Duration.ofMillis(200), Duration.ofSeconds(5), 0.1);
		// .withRestartBackoff(minBackoff = 200.millis, maxBackoff = 5.seconds, 0.1);
	}

	
	//Once we have the tags, the SourceProvider and the Projection of our choice, 
	//we can glue all the pieces together using the Sharded Daemon Process and let it be distributed across the cluster
	
	
	@PostConstruct
	public void initilize() {
		session = CassandraSessionRegistry.get(system).sessionFor("akka.projection.cassandra.session-config");
		repo = new URProjectionRepositoryImpl(session);
		ShardedDaemonProcess.get(system).init(ProjectionBehavior.Command.class, AppFunction.UR_TAG,
				AppFunction.tags.size(),
				id -> ProjectionBehavior.create(projection(AppFunction.tags.get((Integer) id))),
				ProjectionBehavior.stopMessage());
		
	}

}
