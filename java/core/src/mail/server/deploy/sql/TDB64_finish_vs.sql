alter table user change v v_bin blob;
alter table user change s s_bin blob;

alter table user change v_b64 v text;
alter table user change s_b64 s text;

alter table user drop column v_bin;
alter table user drop column s_bin;
alter table user drop column converted;
