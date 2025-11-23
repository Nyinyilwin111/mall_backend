package com.sein_gar_har.subscriptionMap;


import com.sein_gar_har.dto.request.SubscriptionDto;
import com.sein_gar_har.entity.SubscriptionEntity;

public class SubscriptionMapper {

    public static SubscriptionEntity toEntity(SubscriptionDto dto) {
        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setEndpoint(dto.getEndpoint());
        entity.setP256dh(dto.getKeys().getP256dh());
        entity.setAuth(dto.getKeys().getAuth());
        return entity;
    }
}
