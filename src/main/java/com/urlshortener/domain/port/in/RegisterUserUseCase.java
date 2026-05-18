package com.urlshortener.domain.port.in;

import com.urlshortener.domain.model.User;

/**
 * INPUT PORT — defines what the outside world can ask the application to do.
 *
 * Controllers depend on this interface, never on the concrete service.
 * This keeps the web layer decoupled from business logic.
 */
public interface RegisterUserUseCase {

    User register(RegisterUserCommand command);

    record RegisterUserCommand(String email, String password) {}
}
