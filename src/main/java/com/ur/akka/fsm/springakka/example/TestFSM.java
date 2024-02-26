package com.ur.akka.fsm.springakka.example;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletionStage;

import com.ur.akka.fsm.springakka.fsm.UrFSM;
import com.ur.akka.fsm.springakka.state.Accounting;
import com.ur.akka.fsm.springakka.state.Invoice;
import com.ur.akka.fsm.springakka.state.Tender;
import com.ur.akka.fsm.springakka.state.UrKey;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.pattern.StatusReply;
import akka.persistence.typed.PersistenceId;


public class TestFSM {

	private static final Duration askTimeout = Duration.ofSeconds(60);

	public static void main(String[] args) {
		System.out.println("Hello");
		ActorSystem.create(create(), "URFSM");
		
	}

	public static Behavior<Void> create() {
		return Behaviors.setup(context -> {
			TalkWithFsm(context.getSystem());
			return Behaviors.empty();
		});
	}
	

	public static void TalkWithFsm(ActorSystem<?> system) {

		ClusterSharding sharding = ClusterSharding.get(system);

		sharding.init(Entity.of(UrFSM.typeKey, entityContext -> UrFSM.create(entityContext.getEntityId(),
				PersistenceId.of(entityContext.getEntityTypeKey().name(), entityContext.getEntityId()))));
		
			
		
		 // AkkaManagement(system).start();
		  //ClusterBootstrap(system).start();
		
		
		

		EntityRef<UrFSM.UrFSMCommand> entityRef = sharding.entityRefFor(UrFSM.typeKey, "1001");

		Invoice inovce = new Invoice("12.0", new ArrayList<Tender>(), new ArrayList<Accounting>());
		UrKey urKey = new UrKey("OR-1001", "1001", null);

		CompletionStage<UrFSM.UrFSMResponse> result =

				entityRef.ask(replyTo -> new UrFSM.AddInvoice(replyTo, inovce, urKey), askTimeout);

		result.whenComplete((reply, failure) -> {
			if (reply != null)
				System.out.println("Yay, recived " + reply);
			else if (failure instanceof StatusReply.ErrorMessage)
				System.out.println("No Reply " + failure.getMessage());
			else
				System.out.println("Did not recived the reply" + failure);
		});

		// result.thenApply(greeting -> greeting.numberOfPeople);
	}

}
