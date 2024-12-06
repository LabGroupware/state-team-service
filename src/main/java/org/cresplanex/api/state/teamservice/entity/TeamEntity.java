package org.cresplanex.api.state.teamservice.entity;

import jakarta.persistence.*;
import org.cresplanex.api.state.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.cresplanex.api.state.common.utils.OriginalAutoGenerate;
import org.hibernate.Hibernate;

import java.util.List;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "teams",
    indexes = {@Index(name = "teams_organization_id_index", columnList = "organization_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "teams_organization_id_name_unique", columnNames = {"organization_id", "name"})
})
public class TeamEntity extends BaseEntity<TeamEntity> {

    @Override
    public void setId(String id) {
        this.teamId = id;
    }

    @Override
    public String getId() {
        return this.teamId;
    }

    @Id
    @OriginalAutoGenerate
    @Column(name = "team_id", length = 100, nullable = false, unique = true)
    private String teamId;

    @Column(name = "organization_id", length = 100)
    private String organizationId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamUserEntity> teamUsers;

    @Override
    public TeamEntity clone() {
        TeamEntity cloned = super.clone();

        // FetchされているもしくはすでにSetされている場合のみクローンを作成する
        if (this.teamUsers != null && Hibernate.isInitialized(this.teamUsers)) {
            cloned.teamUsers = this.teamUsers.stream()
                    .map(TeamUserEntity::clone)
                    .toList();
        }

        return cloned;
    }
}
