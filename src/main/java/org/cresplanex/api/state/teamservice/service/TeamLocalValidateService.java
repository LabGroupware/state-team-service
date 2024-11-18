package org.cresplanex.api.state.teamservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.saga.local.LocalException;
import org.cresplanex.api.state.common.saga.local.team.AlreadyExistTeamNameInOrganizationException;
import org.cresplanex.api.state.common.saga.local.team.NotAllowedOnDefaultTeamException;
import org.cresplanex.api.state.common.saga.local.team.NotFoundTeamException;
import org.cresplanex.api.state.common.saga.local.team.ReservedTeamNameException;
import org.cresplanex.api.state.common.service.BaseService;
import org.cresplanex.api.state.teamservice.constants.ActionOnTeam;
import org.cresplanex.api.state.teamservice.constants.ReservedTeamName;
import org.cresplanex.api.state.teamservice.entity.TeamEntity;
import org.cresplanex.api.state.teamservice.entity.TeamUserEntity;
import org.cresplanex.api.state.teamservice.exception.AlreadyExistTeamUserException;
import org.cresplanex.api.state.teamservice.exception.TeamNotFoundException;
import org.cresplanex.api.state.teamservice.repository.TeamRepository;
import org.cresplanex.api.state.teamservice.repository.TeamUserRepository;
import org.cresplanex.api.state.teamservice.saga.model.team.AddUsersTeamSaga;
import org.cresplanex.api.state.teamservice.saga.model.team.CreateTeamSaga;
import org.cresplanex.api.state.teamservice.saga.state.team.AddUsersTeamSagaState;
import org.cresplanex.api.state.teamservice.saga.state.team.CreateTeamSagaState;
import org.cresplanex.core.saga.orchestration.SagaInstanceFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TeamLocalValidateService extends BaseService {

    private final TeamRepository teamRepository;
    private final TeamUserRepository teamUserRepository;

    public void validateCreatedTeam(String organizationId, String name, String description)
            throws AlreadyExistTeamNameInOrganizationException, ReservedTeamNameException {
        if (Arrays.asList(ReservedTeamName.ALL).contains(name)) {
            throw new ReservedTeamNameException(List.of(name));
        }
        teamRepository.findByOrganizationIdAndName(organizationId, name)
                .ifPresent(organization -> {
                    throw new AlreadyExistTeamNameInOrganizationException(organizationId, List.of(name));
                });
    }

    public TeamEntity validateTeam(String teamId, String actionType)
            throws NotFoundTeamException {
        TeamEntity teamEntity = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundTeamException(List.of(teamId)));
        if (teamEntity.isDefault()){
            switch (actionType) {
                case ActionOnTeam.UPDATE_PROFILE:
                    throw new NotAllowedOnDefaultTeamException(List.of(teamId), ActionOnTeam.UPDATE_PROFILE);
                case ActionOnTeam.DELETE:
                    throw new NotAllowedOnDefaultTeamException(List.of(teamId), ActionOnTeam.DELETE);
                case ActionOnTeam.ADD_USERS:
                    throw new NotAllowedOnDefaultTeamException(List.of(teamId), ActionOnTeam.ADD_USERS);
                case ActionOnTeam.REMOVE_USERS:
                    throw new NotAllowedOnDefaultTeamException(List.of(teamId), ActionOnTeam.REMOVE_USERS);
            }
        }

        return teamEntity;
    }
}
