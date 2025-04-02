--liquibase formatted sql
--changeset Yakovlev:create_table
--preconditions onfail:mark_ran onerror:halt

CREATE TABLE notification_task (
    id BIGSERIAL NOT NULL,
    chat_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    notification_date_time TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);