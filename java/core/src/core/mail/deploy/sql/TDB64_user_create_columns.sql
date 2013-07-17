alter table user add column v_b64 text after s;
alter table user add column s_b64 text after v_b64;
alter table user add column converted boolean default false after s_b64