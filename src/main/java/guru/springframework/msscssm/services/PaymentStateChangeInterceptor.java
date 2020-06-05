package guru.springframework.msscssm.services;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Created by jt on 2019-08-10.
 */
@RequiredArgsConstructor
@Component
/*
    Quello che facciamo è reagire agli eventi che arrivano:
    - recuperiamo lo stato dell'oggetto Payment dal DB
    - modifichiamo lo stato con quello che ci passa l'interceptor
    - lo persistiamo nuovamente

    il service poi ogni volta che arriverà una richiesta (pre autorizzazione p.es)
    si limiterà a recuperare lo stato dal db, startare la sm e inviare l'evento relativo al metodo (p.es. pre_auth_event)

    E' possibile legarsi a diversi eventi della macchina a stati, noi abbiamo sovrascritto solo il metodo preStateChange, quindi
    la macchina da sola reagisce all'evento capendo il next state a partire da quanto indicato in StateMachineConfig
    e noi poco prima che vari lo stato facciamo la nostra logica di set nuovo stato
    (ce lo passa l'interceptor che conosce il nuovo stato) e persist nel DB essendoci legato al
    relativo evento di variazione della macchina a stati (che non c'entra nulla con gli eventi di pagamento)
 */
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

    private final PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message,
                               Transition<PaymentState, PaymentEvent> transition, StateMachine<PaymentState, PaymentEvent> stateMachine) {

        Optional.ofNullable(message).ifPresent(msg -> {
            Optional.ofNullable(Long.class.cast(msg.getHeaders().getOrDefault(PaymentServiceImpl.PAYMENT_ID_HEADER, -1L)))
                    .ifPresent(paymentId -> {
                        Payment payment = paymentRepository.getOne(paymentId);
                        payment.setState(state.getId());
                        paymentRepository.save(payment);
                    });
        });
    }
}
