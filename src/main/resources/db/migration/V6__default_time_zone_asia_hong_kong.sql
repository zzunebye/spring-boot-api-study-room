ALTER TABLE study_rooms
ALTER COLUMN time_zone
SET DEFAULT 'Asia/Hong_Kong';
UPDATE study_rooms
SET time_zone = 'Asia/Hong_Kong';