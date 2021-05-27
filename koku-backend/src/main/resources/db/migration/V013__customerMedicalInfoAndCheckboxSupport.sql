alter table koku.customer add column hay_fever boolean;
update koku.customer set hay_fever = false;
alter table koku.customer alter column hay_fever SET NOT NULL;

alter table koku.customer add column glasses boolean;
update koku.customer set glasses = false;
alter table koku.customer alter column glasses SET NOT NULL;

alter table koku.customer add column epilepsy boolean;
update koku.customer set epilepsy = false;
alter table koku.customer alter column epilepsy SET NOT NULL;

alter table koku.customer add column dry_eyes boolean;
update koku.customer set dry_eyes = false;
alter table koku.customer alter column dry_eyes SET NOT NULL;

alter table koku.customer add column diabetes boolean;
update koku.customer set diabetes = false;
alter table koku.customer alter column diabetes SET NOT NULL;

alter table koku.customer add column cyanoacrylate_allergy boolean;
update koku.customer set cyanoacrylate_allergy = false;
alter table koku.customer alter column cyanoacrylate_allergy SET NOT NULL;

alter table koku.customer add column contacts boolean;
update koku.customer set contacts = false;
alter table koku.customer alter column contacts SET NOT NULL;

alter table koku.customer add column claustrophobia boolean;
update koku.customer set claustrophobia = false;
alter table koku.customer alter column claustrophobia SET NOT NULL;

alter table koku.customer add column circulation_problems boolean;
update koku.customer set circulation_problems = false;
alter table koku.customer alter column circulation_problems SET NOT NULL;

alter table koku.customer add column asthma boolean;
update koku.customer set asthma = false;
alter table koku.customer alter column asthma SET NOT NULL;

alter table koku.document_field drop column read_only;

alter table koku.field_definition_text add column read_only boolean;
update koku.field_definition_text set read_only = true;
alter table koku.field_definition_text alter column read_only SET NOT NULL;

create table koku.field_definition_checkbox (
    context varchar(255),
    font_size varchar(255),
    label varchar(255),
    read_only boolean not null,
    value boolean not null,
    id int8 not null,
    primary key (id)
);

alter table koku.field_definition_checkbox add constraint FK86e8betoik76e5afqmqnwge3s foreign key (id) references koku.field_definition_type;