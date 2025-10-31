package com.spring.Services.ServiceImplements;

import com.spring.Entity.Branch;
import com.spring.Repository.BranchRepository;
import com.spring.Services.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BranchServiceImpl implements BranchService {

    @Autowired
    BranchRepository branchRepository;


    @Override
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }
}

