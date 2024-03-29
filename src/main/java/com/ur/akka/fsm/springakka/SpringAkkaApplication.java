package com.ur.akka.fsm.springakka;

import java.time.Duration;
import java.util.HashSet;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.ur.akka.fsm.springakka.fsm.DeadLetterMonitorActor;
import com.ur.akka.fsm.springakka.fsm.UrFSM;
import com.ur.akka.fsm.springakka.state.Accounting;
import com.ur.akka.fsm.springakka.state.Invoice;
import com.ur.akka.fsm.springakka.state.InvoiceState;
import com.ur.akka.fsm.springakka.state.Tender;
import com.ur.akka.fsm.springakka.state.UrKey;

import akka.actor.DeadLetter;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.eventstream.EventStream;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.pattern.StatusReply;
import akka.persistence.typed.PersistenceId;

@SpringBootApplication
public class SpringAkkaApplication {

	static final Logger log = LoggerFactory.getLogger(SpringAkkaApplication.class);
	private static final Duration askTimeout = Duration.ofSeconds(20);

	public static void main(String[] args) {
		loadConfigOverrides(args);
		SpringApplication.run(SpringAkkaApplication.class, args);
		//initializeActorSystem();

	}

	@Bean
	public ActorSystem<?> initializeActorSystem(Behavior<Void> emptyBehavior) {
		return ActorSystem.create(emptyBehavior, "URFSM");
	}

	@Bean
	public static Behavior<Void> create() {
		return Behaviors.setup(context -> {

			ActorRef<DeadLetter> deadLetterMonitorActor = context.spawn(DeadLetterMonitorActor.create(),
					"DeadLetterMonitorActor");
			
			context.getSystem().eventStream()
					.tell(new EventStream.Subscribe<DeadLetter>(DeadLetter.class, deadLetterMonitorActor));

			return Behaviors.empty();
		});
	}

	@Bean
	public static ClusterSharding  initializedSharding(ActorSystem<?> system) {
		ClusterSharding sharding = ClusterSharding.get(system);
		sharding.init(Entity.of(UrFSM.typeKey, entityContext -> UrFSM.create(entityContext.getEntityId(),
				PersistenceId.of(entityContext.getEntityTypeKey().name(), entityContext.getEntityId()))));
		
		return sharding;

	}
	
	
	
	
	public static void sendMessageTOFSM(ClusterSharding sharding) {
		EntityRef<UrFSM.UrFSMCommand> entityRef = sharding.entityRefFor(UrFSM.typeKey, "1001");

		Invoice inovce = new Invoice(InvoiceState.InitialState,"I101", "12.0", new HashSet<Tender>(), new HashSet<Accounting>(), new HashSet<>());
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
	}
	

	private static void loadConfigOverrides(String[] args) {
		String regex = "-D(\\S+)=(\\S+)";
		Pattern pattern = Pattern.compile(regex);

		for (String arg : args) {
			Matcher matcher = pattern.matcher(arg);

			while (matcher.find()) {
				String key = matcher.group(1);
				String value = matcher.group(2);
				log.info("Config Override: " + key + " = " + value);
				System.setProperty(key, value);
			}
		}
	}

}
