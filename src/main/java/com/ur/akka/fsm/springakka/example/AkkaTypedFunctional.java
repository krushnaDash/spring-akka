package com.ur.akka.fsm.springakka.example;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import com.ur.akka.fsm.springakka.example.AkkaTypedFunctional.MessageActor.Reply;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.pattern.StatusReply;

public class AkkaTypedFunctional {

	/**
	 * Creating a Akka typed Actor | Behaviour Actor
	 * 
	 * An actor An actor `Behavior` can be implemented by extending the class
	 * AbstractBehavior and impelemting the method createReceive or by using the
	 * factory method of the Behaviors.receiveMessage If the Behavior actor do not
	 * need to have instance variable we can use the functional style
	 * 
	 * @author k0d03gd
	 *
	 */

	static class MessageActor {

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

		public static Behavior<Message> create() {
			return Behaviors.receive(Message.class).onMessage(Message.class, MessageActor::reply).build();
		}
		
	

		public static Behavior<Message> reply(Message msg) {
			msg.sender.tell(new Reply(msg.msg +" Hello from Behaviour by functional style"));
			return Behaviors.same();
		}

	}

	public static void main(String[] args) {

		ActorSystem<MessageActor.Message> system = ActorSystem.create(MessageActor.create(), "hello");
		
		SendMessageToActor(system, new MessageActor.Message("Fisrt Message", null));
		SendMessageToActor(system, new MessageActor.Message("Second Message", null));

	}

	public static void SendMessageToActor(ActorSystem<MessageActor.Message> system, MessageActor.Message msg) {
		// We are using the ask pattern to talk with a behaviour
		CompletionStage<MessageActor.Reply> result = AskPattern.ask(system, (ActorRef<Reply> sender) -> {
			msg.sender = sender;
			return msg;
		}, Duration.ofSeconds(3), system.scheduler());

		result.whenComplete((reply, failure) -> {
			if (reply != null)
				System.out.println("Yay, recived " + reply.msg);
			else if (failure instanceof StatusReply.ErrorMessage)
				System.out.println("No Reply " + failure.getMessage());
			else
				System.out.println("Did not recived the reply" + failure);
		});
	}

}
