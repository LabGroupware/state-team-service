package org.cresplanex.api.state.teamservice.event.publisher;

import org.cresplanex.api.state.common.event.EventAggregateType;
import org.cresplanex.api.state.common.event.model.team.TeamDomainEvent;
import org.cresplanex.api.state.common.event.publisher.AggregateDomainEventPublisher;
import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.cresplanex.core.events.publisher.DomainEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class TeamDomainEventPublisher extends AggregateDomainEventPublisher<TeamEntity, TeamDomainEvent> {

    public TeamDomainEventPublisher(DomainEventPublisher eventPublisher) {
        super(eventPublisher, TeamEntity.class, EventAggregateType.TEAM);
    }
}
