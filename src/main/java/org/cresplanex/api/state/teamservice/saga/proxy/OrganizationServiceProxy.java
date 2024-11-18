package org.cresplanex.api.state.teamservice.saga.proxy;

import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.validate.organization.OrganizationAndOrganizationUserExistValidateCommand;
import org.cresplanex.core.saga.simpledsl.CommandEndpoint;
import org.cresplanex.core.saga.simpledsl.CommandEndpointBuilder;
import org.springframework.stereotype.Component;

@Component
public class OrganizationServiceProxy {

    public final CommandEndpoint<OrganizationAndOrganizationUserExistValidateCommand> organizationUserExistValidateCommand
            = CommandEndpointBuilder
            .forCommand(OrganizationAndOrganizationUserExistValidateCommand.class)
            .withChannel(SagaCommandChannel.ORGANIZATION)
            .withCommandType(OrganizationAndOrganizationUserExistValidateCommand.TYPE)
            .build();
}
