package org.cresplanex.api.state.teamservice.config;

import org.cresplanex.core.events.publisher.CoreEventPublisherConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    // 実装
    CoreEventPublisherConfiguration.class
})
public class EventPublisherConfiguration {
}
