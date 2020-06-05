package guru.springframework.msscssm.services;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

/**
 * Created by jt on 2019-08-10.
 */
/*
    Questo è il payment service, in pratica creiamo e persistiamo un evento di pagamento, il paymentId
    è l'@Id dell'entità Payment che persistiamo a DB.

    Questa classe in pratica crea, persiste e ripristina una macchina a stati da e verso il DB

    StateMachine è una classe di spring.
 */
public interface PaymentService {

    Payment newPayment(Payment payment);

    StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId);
}
