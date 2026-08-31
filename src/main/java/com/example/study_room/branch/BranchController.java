package com.example.study_room.branch;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/branches")
public class BranchController {

	private final BranchService branchService;

	public BranchController(BranchService branchService) {
		this.branchService = branchService;
	}

	@GetMapping
	public BranchListResponse getAllBranches() {
		return branchService.getAllBranches();
	}

	@GetMapping("/{branchId}")
	public BranchResponse getBranch(@PathVariable Long branchId) {
		return branchService.getBranch(branchId);
	}
}
