package com.example.study_room.room;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA automatically create connections, JPA, and repository bean
 * when interface is annotated with @Repository
 */
public interface RoomRepository extends JpaRepository<StudyRoom, Long> {

}
