-- Cloud backup configuration rows for Biblivre.
-- Execute inside the target schema, for example:
--   SET search_path TO global;
--
-- If OAuth succeeds in the application, the refresh token is saved automatically.
-- If the application cannot write to dolorbarreira.configurations, replace the empty values below
-- and run this script manually in the same schema used by the library.

BEGIN;

UPDATE dolorbarreira.configurations SET value = 'true', type = 'boolean', required = false, modified = now(), modified_by = 0
WHERE "key" = 'administration.backup.google_drive.enabled';
INSERT INTO dolorbarreira.configurations ("key", value, type, required, modified, modified_by)
SELECT 'administration.backup.google_drive.enabled', 'true', 'boolean', false, now(), 0
WHERE NOT EXISTS (SELECT 1 FROM dolorbarreira.configurations WHERE "key" = 'administration.backup.google_drive.enabled');

UPDATE dolorbarreira.configurations SET type = 'string', required = false, modified = now(), modified_by = 0
WHERE "key" = 'administration.backup.google_drive.client_id';
INSERT INTO dolorbarreira.configurations ("key", value, type, required, modified, modified_by)
SELECT 'administration.backup.google_drive.client_id', '', 'string', false, now(), 0
WHERE NOT EXISTS (SELECT 1 FROM dolorbarreira.configurations WHERE "key" = 'administration.backup.google_drive.client_id');

UPDATE dolorbarreira.configurations SET type = 'string', required = false, modified = now(), modified_by = 0
WHERE "key" = 'administration.backup.google_drive.client_secret';
INSERT INTO dolorbarreira.configurations ("key", value, type, required, modified, modified_by)
SELECT 'administration.backup.google_drive.client_secret', '', 'string', false, now(), 0
WHERE NOT EXISTS (SELECT 1 FROM dolorbarreira.configurations WHERE "key" = 'administration.backup.google_drive.client_secret');

UPDATE dolorbarreira.configurations SET type = 'string', required = false, modified = now(), modified_by = 0
WHERE "key" = 'administration.backup.google_drive.refresh_token';
INSERT INTO dolorbarreira.configurations ("key", value, type, required, modified, modified_by)
SELECT 'administration.backup.google_drive.refresh_token', '', 'string', false, now(), 0
WHERE NOT EXISTS (SELECT 1 FROM dolorbarreira.configurations WHERE "key" = 'administration.backup.google_drive.refresh_token');

UPDATE dolorbarreira.configurations SET type = 'string', required = false, modified = now(), modified_by = 0
WHERE "key" = 'administration.backup.google_drive.account_email';
INSERT INTO dolorbarreira.configurations ("key", value, type, required, modified, modified_by)
SELECT 'administration.backup.google_drive.account_email', '', 'string', false, now(), 0
WHERE NOT EXISTS (SELECT 1 FROM dolorbarreira.configurations WHERE "key" = 'administration.backup.google_drive.account_email');

UPDATE dolorbarreira.configurations SET type = 'boolean', required = false, modified = now(), modified_by = 0
WHERE "key" = 'administration.backup.dropbox.enabled';
INSERT INTO dolorbarreira.configurations ("key", value, type, required, modified, modified_by)
SELECT 'administration.backup.dropbox.enabled', 'false', 'boolean', false, now(), 0
WHERE NOT EXISTS (SELECT 1 FROM dolorbarreira.configurations WHERE "key" = 'administration.backup.dropbox.enabled');

UPDATE dolorbarreira.configurations SET type = 'string', required = false, modified = now(), modified_by = 0
WHERE "key" = 'administration.backup.dropbox.app_key';
INSERT INTO dolorbarreira.configurations ("key", value, type, required, modified, modified_by)
SELECT 'administration.backup.dropbox.app_key', '', 'string', false, now(), 0
WHERE NOT EXISTS (SELECT 1 FROM dolorbarreira.configurations WHERE "key" = 'administration.backup.dropbox.app_key');

UPDATE dolorbarreira.configurations SET type = 'string', required = false, modified = now(), modified_by = 0
WHERE "key" = 'administration.backup.dropbox.app_secret';
INSERT INTO dolorbarreira.configurations ("key", value, type, required, modified, modified_by)
SELECT 'administration.backup.dropbox.app_secret', '', 'string', false, now(), 0
WHERE NOT EXISTS (SELECT 1 FROM dolorbarreira.configurations WHERE "key" = 'administration.backup.dropbox.app_secret');

UPDATE dolorbarreira.configurations SET type = 'string', required = false, modified = now(), modified_by = 0
WHERE "key" = 'administration.backup.dropbox.access_token';
INSERT INTO dolorbarreira.configurations ("key", value, type, required, modified, modified_by)
SELECT 'administration.backup.dropbox.access_token', '', 'string', false, now(), 0
WHERE NOT EXISTS (SELECT 1 FROM dolorbarreira.configurations WHERE "key" = 'administration.backup.dropbox.access_token');

UPDATE dolorbarreira.configurations SET type = 'string', required = false, modified = now(), modified_by = 0
WHERE "key" = 'administration.backup.dropbox.refresh_token';
INSERT INTO dolorbarreira.configurations ("key", value, type, required, modified, modified_by)
SELECT 'administration.backup.dropbox.refresh_token', '', 'string', false, now(), 0
WHERE NOT EXISTS (SELECT 1 FROM dolorbarreira.configurations WHERE "key" = 'administration.backup.dropbox.refresh_token');

UPDATE dolorbarreira.configurations SET type = 'string', required = false, modified = now(), modified_by = 0
WHERE "key" = 'administration.backup.dropbox.access_token_expires_at';
INSERT INTO dolorbarreira.configurations ("key", value, type, required, modified, modified_by)
SELECT 'administration.backup.dropbox.access_token_expires_at', '', 'string', false, now(), 0
WHERE NOT EXISTS (SELECT 1 FROM dolorbarreira.configurations WHERE "key" = 'administration.backup.dropbox.access_token_expires_at');

UPDATE dolorbarreira.configurations SET type = 'string', required = false, modified = now(), modified_by = 0
WHERE "key" = 'administration.backup.dropbox.account_email';
INSERT INTO dolorbarreira.configurations ("key", value, type, required, modified, modified_by)
SELECT 'administration.backup.dropbox.account_email', '', 'string', false, now(), 0
WHERE NOT EXISTS (SELECT 1 FROM dolorbarreira.configurations WHERE "key" = 'administration.backup.dropbox.account_email');

COMMIT;
