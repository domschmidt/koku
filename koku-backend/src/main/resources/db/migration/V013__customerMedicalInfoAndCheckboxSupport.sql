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