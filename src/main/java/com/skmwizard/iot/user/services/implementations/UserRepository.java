package com.skmwizard.iot.user.services.implementations;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

interface UserRepository extends ReactiveCrudRepository<UserDocument, String> {
}
