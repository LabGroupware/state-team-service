CREATE TABLE teams (
        team_id VARCHAR(100) PRIMARY KEY,
        organization_id VARCHAR(100),
        version INTEGER DEFAULT 0 NOT NULL,
        name VARCHAR(255) NOT NULL,
        description TEXT,
        is_default BOOLEAN DEFAULT FALSE NOT NULL,
        created_at date NOT NULL,
        created_by varchar(50) NOT NULL,
        updated_at date DEFAULT NULL,
        updated_by varchar(50) DEFAULT NULL
);

CREATE INDEX teams_organization_id_index ON teams (organization_id);

CREATE UNIQUE INDEX teams_organization_id_name_index ON teams (organization_id, name);