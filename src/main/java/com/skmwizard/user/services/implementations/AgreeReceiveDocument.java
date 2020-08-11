package com.skmwizard.user.services.implementations;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author ingu_ko
 * @since 2020-07-24
 */

@Document("user_agree")
@Setter
@Getter
@ToString(doNotUseGetters = true)
class AgreeReceiveDocument {
    @NotNull
    private String code;

    @NotNull
    private LocalDateTime agreedDatetime;

    @NotNull
    private String email;
}
