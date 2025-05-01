package com.example.auth.repository;

import com.example.auth.entity.ApiEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiEndpointRepository extends JpaRepository<ApiEndpoint, Long> {
    
    Optional<ApiEndpoint> findByPathAndMethod(String path, String method);
    
    List<ApiEndpoint> findAllByOrderByPathAsc();
    
    @Query("SELECT DISTINCT a.controllerName FROM ApiEndpoint a ORDER BY a.controllerName")
    List<String> findAllControllerNames();
    
    List<ApiEndpoint> findByControllerNameOrderByPathAsc(String controllerName);
} 