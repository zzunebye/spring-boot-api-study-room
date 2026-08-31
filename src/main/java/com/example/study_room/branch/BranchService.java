package com.example.study_room.branch;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.study_room.common.exception.BusinessException;
import com.example.study_room.common.exception.ErrorCode;

@Service
public class BranchService {

	private final BranchRepository branchRepository;

	public BranchService(BranchRepository branchRepository) {
		this.branchRepository = branchRepository;
	}

	public BranchListResponse getAllBranches() {
		List<BranchResponse> items = branchRepository
				.findAll()
				.stream()
				.map(BranchResponse::from)
				.toList();
		return new BranchListResponse(items);
	}

	public BranchResponse getBranch(Long id) {
		return branchRepository
				.findById(id)
				.map(BranchResponse::from)
				.orElseThrow(() -> new BusinessException(ErrorCode.BRANCH_NOT_FOUND));
	}
}
