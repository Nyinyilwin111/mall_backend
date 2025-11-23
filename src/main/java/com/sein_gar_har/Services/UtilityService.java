// UtilityService.java
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
    UtilityResponseDTO updateUtility(Long id, UtilityUpdateRequestDTO utilityUpdateRequestDTO);
    boolean deleteUtility(Long id);

    // Keep these for interface compatibility but they can return empty/default values
    boolean markAsPaid(Long utilityId);
    BigDecimal getTotalPendingAmountBySpaceId(UUID spaceId);
    List<UtilityResponseDTO> getPendingUtilitiesBySpaceId(UUID spaceId);
}