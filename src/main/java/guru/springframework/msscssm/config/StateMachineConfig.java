package guru.springframework.msscssm.config;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.services.PaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Random;

/**
 * Created by jt on 2019-07-23.
 */

/*
    This is the state machine configuration, with all the involved states, initial, terminal and so on.

    We gonna use factory, but also builder is present

    In pratica forniamo gli stati che la macchina a stati avrà e gli eventi a cui dovrà rispondere (son i due parametri del
    parent parametrizzato)

    Il factory è costituito da stati iniziali, enumerazione degli stati e stati finali.g
 */
@Slf4j
/* annotation fondamentale */
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .initial(PaymentState.NEW)
                .states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.AUTH)
                .end(PaymentState.PRE_AUTH_ERROR)
                .end(PaymentState.AUTH_ERROR);
    }

    /*
        qua in pratica dico alla macchina a stati come effettuare le transizioni in relazione agli eventi che arriveranno.

        p.es.:

        .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTH_APPROVED)

        significa che se sei nello stato PaymentState.NEW e ti arriva un evento PaymentEvent.PRE_AUTH_APPROVED, allora devi andare
        nello stato PaymentState.PRE_AUTH
     */

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions.withExternal().source(PaymentState.NEW).target(PaymentState.NEW).event(PaymentEvent.PRE_AUTHORIZE)
                    .action(preAuthAction())
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTH_APPROVED)
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTH_DECLINED);
    }

    /*
        Aggiungiamo un listener che ci avvisa alla variazione di stato p.es. così possiamo scrivere qualche
        log o tracciare o effettuare azioni
     */
    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>(){
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {

                /*
                *   interessante questo String.format che mi permette di formattare le stringhe con i parametri al fondo
                *
                *   https://dzone.com/articles/java-string-format-examples
                *
                */
                log.info(String.format("stateChanged(from: %s, to: %s)", from, to));
            }
        };

        config.withConfiguration()
                .listener(adapter);
    }

    /*
        questo metodo viene chiamato quando sei in new e ti arriva un evento di PRE_AUTHORIZE, da cui si genererà poi
        un evento di PRE_AUTH_APPROVED e PRE_AUTH_DECLINED a seconda dell'esito a cui poi la macchina a stati
        reagirà nuovamente

        Guarda come usa le lambda, che cazzuto, per tornare un oggetto che implementa un'interfaccia (che è l'unica
        e quindi possiamo usare le lambda)
     */
    public Action<PaymentState, PaymentEvent> preAuthAction(){


        return context -> {
            System.out.println("PreAuth was called!!!");

            if (new Random().nextInt(10) < 8) {
                System.out.println("Approved");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED)
                    .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                    .build());

            } else {
                System.out.println("Declined! No Credit!!!!!!");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());
            }
        };
    }
}
