<<<<<<< HEAD
//// UtilityService.java
//package com.sein_gar_har.Services;
//
//import com.sein_gar_har.dto.request.UtilityRequestDTO;
//import com.sein_gar_har.dto.request.UtilityUpdateRequestDTO;
//import com.sein_gar_har.dto.response.UtilityResponseDTO;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.UUID;
//
//public interface UtilityService {
//    UtilityResponseDTO createUtility(UtilityRequestDTO utilityRequestDTO);
//    List<UtilityResponseDTO> getAllUtilities();
//    UtilityResponseDTO getUtilityById(Long id);
//    List<UtilityResponseDTO> getUtilitiesBySpaceId(UUID spaceId);
//    UtilityResponseDTO updateUtility(Long id, UtilityUpdateRequestDTO utilityUpdateRequestDTO);
//    boolean deleteUtility(Long id);
//
//    // Keep these for interface compatibility but they can return empty/default values
//    boolean markAsPaid(Long utilityId);
//    BigDecimal getTotalPendingAmountBySpaceId(UUID spaceId);
//    List<UtilityResponseDTO> getPendingUtilitiesBySpaceId(UUID spaceId);
//}

=======
>>>>>>> 7849d7fed339778c291f082b6b5ba33d53d04c80
package com.sein_gar_har.Services;

import com.sein_gar_har.dto.request.UtilityRequestDTO;
import com.sein_gar_har.dto.request.UtilityUpdateRequestDTO;
import com.sein_gar_har.dto.response.UtilityResponseDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface UtilityService {
    UtilityResponseDTO createUtility(UtilityRequestDTO utilityRequestDTO);
    List<UtilityResponseDTO> getAllUtilities();
    UtilityResponseDTO getUtilityById(Long id);
    List<UtilityResponseDTO> getUtilitiesBySpaceId(UUID spaceId);

    // ✅ ADDED: Get utilities by tenant ID
    List<UtilityResponseDTO> getUtilitiesByTenantId(UUID tenantId);

    UtilityResponseDTO updateUtility(Long id, UtilityUpdateRequestDTO utilityUpdateRequestDTO);
    boolean deleteUtility(Long id);

    // Keep these for interface compatibility but they can return empty/default values
    boolean markAsPaid(Long utilityId);
    BigDecimal getTotalPendingAmountBySpaceId(UUID spaceId);
    List<UtilityResponseDTO> getPendingUtilitiesBySpaceId(UUID spaceId);

    // ✅ ADDED: Get total pending amount by tenant ID
    BigDecimal getTotalPendingAmountByTenantId(UUID tenantId);
}