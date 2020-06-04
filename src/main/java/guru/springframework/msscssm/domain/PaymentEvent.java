package guru.springframework.msscssm.domain;

/**
 * Created by jt on 2019-07-23.
 */
/*
    payment event to which the state machine will react
 */
public enum PaymentEvent {
    PRE_AUTHORIZE, PRE_AUTH_APPROVED, PRE_AUTH_DECLINED, AUTHORIZE, AUTH_APPROVED, AUTH_DECLINED
}
