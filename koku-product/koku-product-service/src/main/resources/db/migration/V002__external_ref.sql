alter table koku.product_manufacturer ADD COLUMN external_ref varchar UNIQUE;
alter table koku.product ADD COLUMN external_ref varchar UNIQUE;
alter table koku.product_price_history ADD COLUMN external_ref varchar UNIQUE;