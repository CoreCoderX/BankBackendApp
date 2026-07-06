package com.dvein.banking_backend.auth.repository;

import com.dvein.banking_backend.auth.model.Device;
import com.dvein.banking_backend.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceId(String deviceId);

    Optional<Device> findByUserAndDeviceId(User user, String deviceId);

    List<Device> findByUserAndActiveTrue(User user);

    List<Device> findByUser(User user);

    long countByUserAndActiveTrue(User user);

    boolean existsByDeviceId(String deviceId);
}