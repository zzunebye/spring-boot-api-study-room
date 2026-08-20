package com.example.study_room;

import org.springframework.boot.SpringApplication;

public class TestStudyRoomApplication {

	public static void main(String[] args) {
		SpringApplication.from(StudyRoomApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
