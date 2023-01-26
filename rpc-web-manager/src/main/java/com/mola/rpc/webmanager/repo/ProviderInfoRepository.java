package com.mola.rpc.webmanager.repo;

import com.mola.rpc.webmanager.entity.ProviderInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderInfoRepository extends JpaRepository<ProviderInfoEntity, Long> {
    
}
