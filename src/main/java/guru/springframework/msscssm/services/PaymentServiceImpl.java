package guru.springframework.msscssm.services;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

/**
 * Created by jt on 2019-08-10.
 */
@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    /*
        state machine factory, classe di spring per la creazione di state machine
     */
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;

    /*
        p.es. qua restituiamo un oggetto Payment con uno stato NEW e lo salviamo a DB.
     */
    @Override
    public Payment newPayment(Payment payment) {
        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        return null;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
        return null;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
        return null;
    }
}
