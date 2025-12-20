show databases;;;

show processlist;;;

DROP TABLE IF EXISTS xyz_unit_test_table;;;

CREATE TABLE xyz_unit_test_table ( 
fielda INT NOT NULL,
fieldb INT NOT NULL,
PRIMARY KEY (fielda)
) ENGINE=InnoDB;
;;;

INSERT INTO xyz_unit_test_table (fielda, fieldb) VALUES (1, 1000), (2, 2000), (3, 3000), (4, 4000);;;

SELECT * from xyz_unit_test_table;;;

UPDATE xyz_unit_test_table set fieldb = 1500 * fielda;;;

SELECT fielda from xyz_unit_test_table;;; SELECT fieldb
FROM xyz_unit_test_table;;;

DROP TABLE IF EXISTS xyz_unit_test_table;;;
