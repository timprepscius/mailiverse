update %s set block = block_conv;
alter table %s drop column block_conv;
alter table %s drop column converted;
