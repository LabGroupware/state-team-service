CREATE TABLE team_user (
        team_user_id VARCHAR(100) PRIMARY KEY,
        team_id VARCHAR(100) NOT NULL,
        user_id VARCHAR(100) NOT NULL,
        version INTEGER DEFAULT 0 NOT NULL,
        created_at date NOT NULL,
        created_by varchar(50) NOT NULL,
        updated_at date DEFAULT NULL,
        updated_by varchar(50) DEFAULT NULL
);

CREATE INDEX team_user_team_id_index ON team_user (team_id);
CREATE INDEX team_user_user_id_index ON team_user (user_id);
CREATE UNIQUE INDEX team_user_team_id_user_id_index ON team_user (team_id, user_id);

ALTER TABLE team_user ADD CONSTRAINT team_user_team_id_fk FOREIGN KEY (team_id) REFERENCES teams (team_id) ON DELETE CASCADE;