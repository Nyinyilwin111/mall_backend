package com.spring.Controller;

import com.spring.Entity.Branch;
import com.spring.Services.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    @Autowired
    BranchService branchService;

    // GET all branches
    @GetMapping("/getAll")
    public List<Branch> getAllBranches() {
        return branchService.getAllBranches();
    }
}
