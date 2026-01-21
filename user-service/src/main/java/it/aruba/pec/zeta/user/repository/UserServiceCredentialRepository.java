package it.aruba.pec.zeta.user.repository;

import it.aruba.pec.zeta.user.entity.User;
import it.aruba.pec.zeta.user.entity.UserServiceCredential;
import it.aruba.pec.zeta.user.entity.UserServiceCredential.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserServiceCredentialRepository extends JpaRepository<UserServiceCredential, Long> {

    List<UserServiceCredential> findByUser(User user);

    List<UserServiceCredential> findByUserAndActiveTrue(User user);

    Optional<UserServiceCredential> findByUserAndServiceType(User user, ServiceType serviceType);

    boolean existsByUserAndServiceType(User user, ServiceType serviceType);

    void deleteByUserAndServiceType(User user, ServiceType serviceType);
}