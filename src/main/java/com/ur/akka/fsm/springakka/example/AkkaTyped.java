package com.ur.akka.fsm.springakka.example;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import com.ur.akka.fsm.springakka.example.AkkaTyped.MessageActor.Reply;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.pattern.StatusReply;

public class AkkaTyped {

	/**
	 * Creating a Akka typed Actor | Behaviour Actor
	 * 
	 * @author k0d03gd
	 *
	 */

	static class MessageActor extends AbstractBehavior<MessageActor.Message> {

		static class Message {
			String msg;
			ActorRef<Reply> sender;

			public Message(String msg, ActorRef<Reply> sender) {
				super();
				this.msg = msg;
				this.sender = sender;
			}

		}

		static class Reply {
			String msg;

			public Reply(String msg) {
				super();
				this.msg = msg;
			}

		}

		public MessageActor(ActorContext<Message> context) {
			super(context);
		}

		public static Behavior<Message> create() {
			return Behaviors.setup(MessageActor::new);
		}
		
		public static Behavior<Message>  reply (Message msg) {
			msg.sender.tell(new Reply("Hello from Behaviour by method"));
			return Behaviors.same();
		}
		

		@Override
		public Receive<Message> createReceive() {
			return newReceiveBuilder()
					
			.onMessage(Message.class, MessageActor::reply) // we can do like this also
			
			.onAnyMessage((msg) -> {
				msg.sender.tell(new Reply("Hello from Behaviour"));
				return this;
			})		
			.build();
		}
	}

	public static void main(String[] args) {

		ActorSystem<MessageActor.Message> system = ActorSystem.create(MessageActor.create(), "hello");

		// We are using the ask pattern to talk with a behaviour
		CompletionStage<MessageActor.Reply> result = AskPattern.ask(system,
				(ActorRef<Reply> sender) -> new MessageActor.Message("ask Pattern", sender), Duration.ofSeconds(3),
				system.scheduler());

		result.whenComplete((reply, failure) -> {
			if (reply != null)
				System.out.println("Yay, recived " + reply.msg );
			else if (failure instanceof StatusReply.ErrorMessage)
				System.out.println("No Reply " + failure.getMessage());
			else
				System.out.println("Did not recived the reply" + failure);
		});

	}

}
