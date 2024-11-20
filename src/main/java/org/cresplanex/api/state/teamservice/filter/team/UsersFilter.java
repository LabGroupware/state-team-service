package org.cresplanex.api.state.teamservice.filter.team;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UsersFilter {

    private boolean isValid;
    private boolean any;
    private List<String> userIds;
}
