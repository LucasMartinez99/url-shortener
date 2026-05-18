package com.urlshortener.domain.port.in;

import com.urlshortener.domain.model.User;

public interface AuthenticateUserUseCase {

    User authenticate(AuthenticateUserCommand command);

    record AuthenticateUserCommand(String email, String password) {}
}
