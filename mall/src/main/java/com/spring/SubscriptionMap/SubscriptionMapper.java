package com.spring.SubscriptionMap;

import com.spring.DTO.request.SubscriptionDto;
import com.spring.Entity.SubscriptionEntity;

public class SubscriptionMapper {

    public static SubscriptionEntity toEntity(SubscriptionDto dto) {
        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setEndpoint(dto.getEndpoint());
        entity.setP256dh(dto.getKeys().getP256dh());
        entity.setAuth(dto.getKeys().getAuth());
        return entity;
    }
}
