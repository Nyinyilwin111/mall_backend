//package com.spring.Services;
//
//import java.util.List;
//import java.util.UUID;
//
//import com.spring.DTO.request.LeaseRequest;
//import com.spring.DTO.response.LeaseResponse;
//import com.spring.Entity.Lease;
//
//public interface LeaseService {
//    Lease createLease(LeaseRequest leaseRequest);
//    List<Lease> getLeasesByTenantId(UUID tenantId);
//	List<LeaseResponse> getAllLeases();
//	
//}
package com.spring.Services;

import java.util.List;

import com.spring.DTO.request.LeaseRequest;
import com.spring.DTO.response.LeaseResponse;
import com.spring.Entity.Lease;

public interface LeaseService {
	
	//create lease
    Lease createLease(LeaseRequest leaseRequest);
    
    // lease lists
    List<LeaseResponse> getAllLeases();
    
   LeaseResponse getLeaseById(Long leaseId); 
   
   //update lease
    LeaseResponse updateLease(Long leaseId, LeaseRequest leaseRequest); // NEW
    //delete lease
    void deleteLease(Long leaseId); // NEW
}