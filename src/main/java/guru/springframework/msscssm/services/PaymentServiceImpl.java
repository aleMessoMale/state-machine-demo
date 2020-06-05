package guru.springframework.msscssm.services;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jt on 2019-08-10.
 */
@RequiredArgsConstructor
@Service
/*
    ognuno di questi metodi quello che fa è:
        - recuperare lo stato della macchina a stati
        - inviarle un evento

    li mette in transazionalità per evitare situazioni inconsistenti

    L'invio del messaggio avviene tramite messaging, spring messaging, quindi con il builder già fatto ad hoc.

    Tu hai scelto di utilizzare Redis e hai scritto qualche classe architetturale da te (ma a quanto ho visto
    Redis non supporta header...)
 */
public class PaymentServiceImpl implements PaymentService {
    public static final String PAYMENT_ID_HEADER = "payment_id";

    private final PaymentRepository paymentRepository;
    /*
        state machine factory, classe di spring per la creazione di state machine
     */
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
    /*
        interceptor della macchina a stati
     */
    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

    /*
        p.es. qua restituiamo un oggetto Payment con uno stato NEW e lo salviamo a DB.
     */
    @Override
    public Payment newPayment(Payment payment) {
        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId, sm, PaymentEvent.PRE_AUTHORIZE);

        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId, sm, PaymentEvent.AUTH_APPROVED);

        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

        sendEvent(paymentId, sm, PaymentEvent.AUTH_DECLINED);

        return sm;
    }


    private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent event){

        Message msg = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_ID_HEADER, paymentId)
                .build();

        sm.sendEvent(msg);
    }



    /*
        ciò che fa questo metodo è semplicemente:
            -   recuperare l'entità Payment dal repository
            -   ricreare un'entità StateMachine con il factory
            -   stoppare la macchina a stati
            -   impostare la macchina a stati tramite l'accessor allo stato che era presente a DB
            -   ristartare la macchina a stati
            -   ritornarla

        questo metodo è utilizzato in tutti gli altri metodi poi...

     */
    private StateMachine<PaymentState, PaymentEvent> build(Long paymentId){
        Payment payment = paymentRepository.getOne(paymentId);

        StateMachine<PaymentState, PaymentEvent> sm = stateMachineFactory.getStateMachine(Long.toString(payment.getId()));

        sm.stop();

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(payment.getState(), null, null, null));
                });

        sm.start();

        return sm;
    }
}
