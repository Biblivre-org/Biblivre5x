--SQL: SELECT R.*, trim(substr(S.phrase, ignore_chars_count + 1)) as sort FROM (SELECT * FROM biblio_records WHERE database = ? ORDER BY id DESC LIMIT ? ) R LEFT JOIN biblio_idx_sort S ON S.holding_id = R.id AND S.indexing_group_id = ? ORDER BY sort NULLS LAST, R.id ASC OFFSET ? LIMIT ?;



ALTER TABLE dolorbarreira.reservations ADD COLUMN holding_id integer;

ALTER TABLE dolorbarreira.reservations DROP COLUMN record_id;

--ALTER TABLE dolorbarreira.biblio_idx_sort ADD COLUMN holding_id integer;

Administration.reindex.confirm('authorities')



Revisar todos os DAOs de reservations com o do Biblivre5-master
inserção e remoção


