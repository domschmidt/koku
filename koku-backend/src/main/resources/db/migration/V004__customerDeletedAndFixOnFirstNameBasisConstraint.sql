alter table koku.customer add column deleted boolean not null default false;
alter table koku.customer alter column on_first_name_basis SET DEFAULT false;
alter table koku.customer alter column on_first_name_basis SET not null;
