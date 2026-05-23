-- liquibase formatted sql

-- changeset atimoshkov:1
CREATE TABLE IF NOT EXISTS notification_task (
    id BIGINT PRIMARY KEY,
    chat_id BIGINT,
    message TEXT,
    date_time TIMESTAMP
)