package org.cresplanex.api.state.teamservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cresplanex.api.state.common.entity.BaseEntity;
import org.cresplanex.api.state.common.utils.OriginalAutoGenerate;
import org.hibernate.Hibernate;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "team_user", indexes = {
        @Index(name = "team_user_team_id_index", columnList = "team_id"),
        @Index(name = "team_user_user_id_index", columnList = "user_id"),
        @Index(name = "team_user_team_id_user_id_index", columnList = "team_id, user_id", unique = true)
})
public class TeamUserEntity extends BaseEntity<TeamUserEntity> {

    @Override
    public void setId(String id) {
        this.teamUserId = id;
    }

    @Override
    public String getId() {
        return this.teamUserId;
    }

    @Id
    @OriginalAutoGenerate
    @Column(name = "team_user_id", length = 100, nullable = false, unique = true)
    private String teamUserId;

    @Column(name = "team_id", length = 100, nullable = false,
            insertable = false, updatable = false)
    private String teamId;

    @Column(name = "user_id", length = 100, nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    @Override
    public TeamUserEntity clone() {
        TeamUserEntity cloned = super.clone();
        // FetchされているもしくはすでにSetされている場合のみクローンを作成する
        if (this.team != null && Hibernate.isInitialized(this.team)) {
            cloned.team = this.team.clone();
        }

        return cloned;
    }
}
