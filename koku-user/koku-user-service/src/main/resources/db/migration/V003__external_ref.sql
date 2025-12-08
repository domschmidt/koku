alter table koku.user ADD COLUMN external_ref varchar UNIQUE;
alter table koku.user_appointment ADD COLUMN external_ref varchar UNIQUE;