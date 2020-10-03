package com.kaczmar.CurrencyAccount.repository;

import com.kaczmar.CurrencyAccount.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    boolean existsAllByPesel(String pesel);
    List<UserAccount> findAllByPesel(String pesel);
    UserAccount findByPesel(String pesel);

}
