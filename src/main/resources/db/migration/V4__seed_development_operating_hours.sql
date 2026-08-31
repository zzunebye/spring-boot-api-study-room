INSERT INTO room_operating_hours (room_id, day_of_week, open_time, close_time)
SELECT r.id,
    d.day,
    d.open_time,
    d.close_time
FROM study_rooms r
    CROSS JOIN (
        VALUES ('MONDAY', TIME '09:00', TIME '22:00'),
            ('TUESDAY', TIME '09:00', TIME '22:00'),
            ('WEDNESDAY', TIME '09:00', TIME '22:00'),
            ('THURSDAY', TIME '09:00', TIME '22:00'),
            ('FRIDAY', TIME '09:00', TIME '22:00'),
            ('SATURDAY', TIME '10:00', TIME '18:00')
    ) AS d(day, open_time, close_time);