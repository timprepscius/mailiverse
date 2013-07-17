alter table %s change block block_bin blob;
alter table %s change block_b64 block text;

alter table %s drop column block_bin;
alter table %s drop column converted;
