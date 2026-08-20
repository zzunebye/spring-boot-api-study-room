package com.example.study_room.room;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Entity class from persistent layer.
@Entity
// 이미 엔티티로 선언된 클래스에 대해, 물리 테이블 이름을 지정. 엔티티의 테이블 메타데이터.
@Table(name = "study_rooms")
public class StudyRoom {

    // ddl-auto=validate이므로 컬럼명·타입이 migration과 맞아야 앱이 뜹니다
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private String description;
    private int capacity;
    private String status; // 처음엔 String으로 해도 OK. 나중에 enum으로
    private long version;
    // getter only is enough, and if needed, add setter.

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getStatus() {
        return status;
    }

    public long getVersion() {
        return version;
    }

}
