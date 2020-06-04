package guru.springframework.msscssm.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Created by jt on 2019-07-23.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
/*
    object which represent the payment

    we gonna recover the payment object with the repository after an event come and we gonna update its state
 */
public class Payment {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private PaymentState state;

    private BigDecimal amount;
}
