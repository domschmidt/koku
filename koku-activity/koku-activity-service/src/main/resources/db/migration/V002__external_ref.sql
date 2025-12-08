alter table koku.activity ADD COLUMN external_ref varchar UNIQUE;
alter table koku.activity_price_history ADD COLUMN external_ref varchar UNIQUE;
alter table koku.activity_step ADD COLUMN external_ref varchar UNIQUE;