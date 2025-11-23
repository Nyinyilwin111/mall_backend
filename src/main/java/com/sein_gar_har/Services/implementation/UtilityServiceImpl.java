
package com.sein_gar_har.Services.implementation;

import com.sein_gar_har.RepositoryMain.SpaceRepository;
import com.sein_gar_har.RepositoryMain.UtilityRepository;
import com.sein_gar_har.Services.UtilityService;
import com.sein_gar_har.dto.request.UtilityRequestDTO;
import com.sein_gar_har.dto.request.UtilityUpdateRequestDTO;
import com.sein_gar_har.dto.response.UtilityResponseDTO;
import com.sein_gar_har.entity.Space;
import com.sein_gar_har.entity.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UtilityServiceImpl implements UtilityService {

    @Autowired
    private UtilityRepository utilityRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    private UtilityResponseDTO convertToDTO(Utility utility) {
        UtilityResponseDTO dto = new UtilityResponseDTO();
        dto.setUtilityId(utility.getUtilityId());
        dto.setSpaceId(utility.getSpace().getSpaceId());
        dto.setSpaceCode(utility.getSpace().getSpaceCode());
        dto.setSpaceLocation(utility.getSpace().getLocation());
        dto.setUtilityType(utility.getUtilityType());
        dto.setDescription(utility.getDescription());

        // ADDED: Set amount
        dto.setAmount(utility.getAmount());

        dto.setDueDate(utility.getDueDate());
        dto.setBillingPeriod(utility.getBillingPeriod());
        dto.setUsageUnit(utility.getUsageUnit());
        dto.setPreviousReading(utility.getPreviousReading());
        dto.setCurrentReading(utility.getCurrentReading());
        dto.setUsageAmount(utility.getUsageAmount());
        dto.setRecordStatus(utility.getRecordStatus());
        dto.setCreatedAt(utility.getCreatedAt());
        dto.setUpdatedAt(utility.getUpdatedAt());
        return dto;
    }

    @Override
    @Transactional
    public UtilityResponseDTO createUtility(UtilityRequestDTO utilityRequestDTO) {
        Space space = spaceRepository.findById(utilityRequestDTO.getSpaceId())
                .orElseThrow(() -> new RuntimeException("Space not found with id: " + utilityRequestDTO.getSpaceId()));

        Utility utility = new Utility();
        utility.setSpace(space);
        utility.setUtilityType(utilityRequestDTO.getUtilityType());
        utility.setDescription(utilityRequestDTO.getDescription());
        utility.setDueDate(utilityRequestDTO.getDueDate());
        utility.setBillingPeriod(utilityRequestDTO.getBillingPeriod());
        utility.setUsageUnit(utilityRequestDTO.getUsageUnit());
        utility.setPreviousReading(utilityRequestDTO.getPreviousReading());
        utility.setCurrentReading(utilityRequestDTO.getCurrentReading());

        // ADDED: Set amount
        utility.setAmount(utilityRequestDTO.getAmount());

        // Calculate usage amount only
        utility.calculateUsageAmount();

        Utility savedUtility = utilityRepository.save(utility);
        return convertToDTO(savedUtility);
    }

    @Override
    public List<UtilityResponseDTO> getAllUtilities() {
        return utilityRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UtilityResponseDTO getUtilityById(Long id) {
        Utility utility = utilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utility not found with id: " + id));
        return convertToDTO(utility);
    }

    @Override
    public List<UtilityResponseDTO> getUtilitiesBySpaceId(UUID spaceId) {
        return utilityRepository.findAllBySpaceId(spaceId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UtilityResponseDTO updateUtility(Long id, UtilityUpdateRequestDTO utilityUpdateRequestDTO) {
        Utility existingUtility = utilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utility not found with id: " + id));

        boolean readingsUpdated = false;

        if (utilityUpdateRequestDTO.getUtilityType() != null) {
            existingUtility.setUtilityType(utilityUpdateRequestDTO.getUtilityType());
        }
        if (utilityUpdateRequestDTO.getDescription() != null) {
            existingUtility.setDescription(utilityUpdateRequestDTO.getDescription());
        }

        // ADDED: Update amount
        if (utilityUpdateRequestDTO.getAmount() != null) {
            existingUtility.setAmount(utilityUpdateRequestDTO.getAmount());
        }

        if (utilityUpdateRequestDTO.getDueDate() != null) {
            existingUtility.setDueDate(utilityUpdateRequestDTO.getDueDate());
        }
        if (utilityUpdateRequestDTO.getBillingPeriod() != null) {
            existingUtility.setBillingPeriod(utilityUpdateRequestDTO.getBillingPeriod());
        }
        if (utilityUpdateRequestDTO.getUsageUnit() != null) {
            existingUtility.setUsageUnit(utilityUpdateRequestDTO.getUsageUnit());
        }
        if (utilityUpdateRequestDTO.getPreviousReading() != null) {
            existingUtility.setPreviousReading(utilityUpdateRequestDTO.getPreviousReading());
            readingsUpdated = true;
        }
        if (utilityUpdateRequestDTO.getCurrentReading() != null) {
            existingUtility.setCurrentReading(utilityUpdateRequestDTO.getCurrentReading());
            readingsUpdated = true;
        }

        // Recalculate usage amount if readings were updated
        if (readingsUpdated) {
            existingUtility.calculateUsageAmount();
        }

        existingUtility.preUpdate();
        Utility updatedUtility = utilityRepository.save(existingUtility);
        return convertToDTO(updatedUtility);
    }

    @Override
    @Transactional
    public boolean deleteUtility(Long id) {
        Optional<Utility> utilityOptional = utilityRepository.findById(id);
        if (utilityOptional.isPresent()) {
            Utility utility = utilityOptional.get();
            utility.setRecordStatus("CANCELLED");
            utility.preUpdate();
            utilityRepository.save(utility);
            return true;
        }
        return false;
    }

    // Remove unused payment methods but keep the interface implementation
    @Override
    @Transactional
    public boolean markAsPaid(Long utilityId) {
        return false;
    }

    @Override
    public BigDecimal getTotalPendingAmountBySpaceId(UUID spaceId) {
        // Now we can calculate actual total amount from utilities
        List<Utility> utilities = utilityRepository.findAllBySpaceId(spaceId);
        BigDecimal total = utilities.stream()
                .filter(utility -> utility.getAmount() != null)
                .map(Utility::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total;
    }

    @Override
    public List<UtilityResponseDTO> getPendingUtilitiesBySpaceId(UUID spaceId) {
        // Return utilities that have amount set (you can modify this logic as needed)
        return utilityRepository.findAllBySpaceId(spaceId)
                .stream()
                .filter(utility -> utility.getAmount() != null && utility.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}