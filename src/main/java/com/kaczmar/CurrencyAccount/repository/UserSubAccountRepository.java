package com.kaczmar.CurrencyAccount.repository;

import com.kaczmar.CurrencyAccount.model.UserSubAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Repository
public interface UserSubAccountRepository extends JpaRepository<UserSubAccount, Long> {

    List<UserSubAccount> findAllByPesel(String pesel);
//    List<UserSubAccount> findAllByUserMainAccount(Long id);

}
