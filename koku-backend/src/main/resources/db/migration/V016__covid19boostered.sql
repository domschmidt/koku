alter table koku.customer
    add column covid19boostered boolean;
update koku.customer
set covid19boostered = false;
alter table koku.customer
    alter column covid19boostered
SET NOT NULL;
