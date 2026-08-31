package com.example.study_room.room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA automatically create connections, JPA, and repository bean
 * when interface is annotated with @Repository
 */
public interface RoomRepository extends JpaRepository<StudyRoom, Long> {

        List<StudyRoom> findByStatus(String status);

        // 주어진 상태와 최소 수용 인원을 만족하는 모든 스터디룸을 조회하는 메서드
        List<StudyRoom> findByStatusAndCapacityGreaterThanEqual(String status, int capacity);

        List<StudyRoom> findByCapacityGreaterThanEqual(int minCapacity);

        List<StudyRoom> findByCapacityLessThanEqual(int maxCapacity);

        List<StudyRoom> findByCapacityBetween(int minCapacity, int maxCapacity);

        @Query("""
                        SELECT r FROM StudyRoom r
                        WHERE (:status IS NULL OR r.status = :status)
                        AND (:branchId IS NULL OR r.branchId = :branchId)
                        AND (:minCapacity IS NULL OR r.capacity >= :minCapacity)
                        AND (:maxCapacity IS NULL OR r.capacity <= :maxCapacity)
                        """)
        List<StudyRoom> search(
                        @Param("status") String status,
                        @Param("branchId") Long branchId,
                        @Param("minCapacity") Integer minCapacity,
                        @Param("maxCapacity") Integer maxCapacity);

}
