package com.ur.akka.fsm.springakka.fsm;

import akka.actor.DeadLetter;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class DeadLetterMonitorActor {
	public static Behavior<DeadLetter> create() {
		return Behaviors.setup(context -> {
			return Behaviors.receive(DeadLetter.class).onMessage(DeadLetter.class, (DeadLetter d) -> {
				context.getLog().info(
						"Got Dead Letter : Sender: {} Recipient: {} ; Message: {}", d.sender(),d.recipient(),d.message());

				return Behaviors.same();
			}).build();
		});
	}

}
