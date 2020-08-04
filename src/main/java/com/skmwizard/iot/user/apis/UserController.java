package com.skmwizard.iot.user.apis;

import com.skmwizard.iot.user.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author ingu_ko
 * @since 2020-07-30
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final UserResourceConverter converter;

    @RequestMapping("/users/{email}")
    public Mono<Void> check(@PathVariable("email") String email) {
        return userService.exists(email).then();
    }
}
