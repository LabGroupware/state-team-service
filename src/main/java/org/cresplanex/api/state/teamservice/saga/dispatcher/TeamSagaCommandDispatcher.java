package org.cresplanex.api.state.teamservice.saga.dispatcher;

import lombok.AllArgsConstructor;
import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.dispatcher.BaseSagaCommandDispatcher;
import org.cresplanex.api.state.teamservice.saga.handler.TeamSagaCommandHandlers;
import org.cresplanex.core.saga.participant.SagaCommandDispatcher;
import org.cresplanex.core.saga.participant.SagaCommandDispatcherFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AllArgsConstructor
@Configuration
public class TeamSagaCommandDispatcher extends BaseSagaCommandDispatcher {

    @Bean
    public SagaCommandDispatcher organizationSagaCommandHandlersDispatcher(
            TeamSagaCommandHandlers teamSagaCommandHandlers,
            SagaCommandDispatcherFactory sagaCommandDispatcherFactory) {
        return sagaCommandDispatcherFactory.make(
                this.getDispatcherId(SagaCommandChannel.TEAM),
                teamSagaCommandHandlers.commandHandlers());
    }
}
