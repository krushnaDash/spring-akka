package com.ur.akka.fsm.springakka.example;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;

import com.ur.akka.fsm.springakka.example.AkkaTyped.MessageActor;
import com.ur.akka.fsm.springakka.example.AkkaTyped.MessageActor.Reply;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;

abstract class Buncher {

	static final class Batch {
		public final List<Object> list;

		public Batch(List<Object> list) {
			this.list = list;
		}
	}

	interface Data {

	}

	static final class Todo implements Data {
		public final ActorRef<Batch> target;
		public final List<Object> queue;

		public Todo(ActorRef<Batch> target, List<Object> queue) {
			this.target = target;
			this.queue = queue;
		}

		public Todo addElement(Object message) {
			// TODO Auto-generated method stub
			return null;
		}

		public Todo copy(ArrayList arrayList) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	interface Event {
	}

	static final class SetTarget implements Event {
		public final ActorRef<Batch> ref;

		public SetTarget(ActorRef<Batch> ref) {
			this.ref = ref;
		}
	}

	private enum Timeout implements Event {
		INSTANCE
	}

	public enum Flush implements Event {
		INSTANCE
	}

	static final class Queue implements Event {
		public final Object obj;

		public Queue(Object obj) {
			this.obj = obj;
		}
	}

	// initial state
	public static Behavior<Event> create() {
		return uninitialized();

	}

	private static Behavior<Event> uninitialized() {
		return Behaviors.receive(Event.class)
				.onMessage(SetTarget.class, message -> idle(new Todo(message.ref, Collections.emptyList()))).build();
	}

	private static Behavior<Event> idle(Todo data) {
		return Behaviors.receive(Event.class).onMessage(Queue.class, message -> active(data.addElement(message)))
				.build();
	}

	private static Behavior<Event> active(Todo data) {
		return Behaviors.withTimers(timers -> {
			// State timeouts done with withTimers
			timers.startSingleTimer("Timeout", Timeout.INSTANCE, Duration.ofSeconds(1));
			return Behaviors.receive(Event.class).onMessage(Queue.class, message -> active(data.addElement(message)))
					.onMessage(Flush.class, message -> activeOnFlushOrTimeout(data))
					.onMessage(Timeout.class, message -> activeOnFlushOrTimeout(data)).build();
		});
	}

	private static Behavior<Event> activeOnFlushOrTimeout(Todo data) {
		data.target.tell(new Batch(data.queue));
		return idle(data.copy(new ArrayList<>()));
	}

}

/**
 * 
 * Model states using different behaviors Model storing data at each state by
 * representing the behavior as a method Implement state timeouts
 * 
 * @author k0d03gd
 *
 */
public class BehaviorsFSM {

	public static void main(String[] args) {

		ActorSystem<Buncher.Event> system = ActorSystem.create(Buncher.create(), "hello");

	}

}
