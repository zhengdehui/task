CREATE TABLE student (
 id VARCHAR(64) NOT NULL,
 name VARCHAR(32)
) ENGINE=InnoDB CHARSET=utf8;

ALTER TABLE student ADD CONSTRAINT PK_student PRIMARY KEY (id);

CREATE TABLE course (
 id VARCHAR(64) NOT NULL,
 name VARCHAR(32),
 academy VARCHAR(32)
) ENGINE=InnoDB CHARSET=utf8;

ALTER TABLE course ADD CONSTRAINT PK_course PRIMARY KEY (id);

CREATE TABLE score (
 student_id VARCHAR(64),
 course_id VARCHAR(64),
 score int
) ENGINE=InnoDB CHARSET=utf8;

ALTER TABLE score ADD CONSTRAINT FK_score_1 FOREIGN KEY (student_id) REFERENCES student (id) ON DELETE RESTRICT;
ALTER TABLE score ADD CONSTRAINT FK_score_2 FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE RESTRICT;