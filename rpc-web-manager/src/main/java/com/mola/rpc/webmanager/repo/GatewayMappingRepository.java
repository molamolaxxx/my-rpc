package com.mola.rpc.webmanager.repo;

import com.mola.rpc.webmanager.entity.GatewayMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayMappingRepository extends JpaRepository<GatewayMappingEntity, Long> {
}
