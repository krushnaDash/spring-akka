package com.ur.akka.fsm.springakka.example;

import java.time.Duration;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;

public class AkkaClassical {

	/**
	 * Creating a Akka classical actor
	 * 
	 * @author k0d03gd
	 *
	 */
	static class TestActor extends AbstractActor {
		@Override
		public Receive createReceive() {
			return ReceiveBuilder.create().matchAny(o -> sender().tell("Hello from Test actor " + o, self())).build();
		}
	}

	
	public static void main(String[] args) {
		System.out.println("Start");
		ActorSystem system = ActorSystem.create("Hello");
		ActorRef testActor = system.actorOf(Props.create(TestActor.class));

		// talking with classical Actor, we can ask pattern
		testActor.tell("anyThing", ActorRef.noSender());
		Patterns.ask(testActor, "anyThing", Duration.ofMillis(3000)).whenComplete((message, failure) -> {
			if (failure == null)
				System.out.println(message);
			else
				System.err.println(failure);
		});

	}
}
