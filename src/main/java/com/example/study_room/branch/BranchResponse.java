package com.example.study_room.branch;

import java.time.ZoneId;

public record BranchResponse(
		Long id,
		String name,
		String location,
		ZoneId timeZone) {

	public static BranchResponse from(Branch branch) {
		return new BranchResponse(
				branch.getId(),
				branch.getName(),
				branch.getLocation(),
				branch.getTimeZone());
	}
}
