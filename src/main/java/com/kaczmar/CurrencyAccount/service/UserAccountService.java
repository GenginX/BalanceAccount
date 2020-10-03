package com.kaczmar.CurrencyAccount.service;

import com.kaczmar.CurrencyAccount.dto.CreateUserAccountDto;
import com.kaczmar.CurrencyAccount.exceptions.PeselAlreadyExistsException;
import com.kaczmar.CurrencyAccount.exceptions.UserAgeIsNotLegalException;
import com.kaczmar.CurrencyAccount.model.UserAccount;
import com.kaczmar.CurrencyAccount.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserAccountService {

    public static final String AGE_LOWER_THAN_18 = "USER AGE IS LESS THAN 18. WE CAN'T CREATE ACCOUNT";
    public static final String PESEL_CANNOT_BE_DUPLICATED = "PROVIDED PESEL HAS BEEN USED FOR ANOTHER ACCOUNT";
    private UserAccountRepository userAccountRepository;

    public UserAccountService(UserAccountRepository userRepository) {
        this.userAccountRepository = userRepository;
    }

//    @Transactional
    public UserAccount createUserAcc(CreateUserAccountDto userDto) throws Exception {
        if(isPeselExistingInDB(userDto.getPesel())){
            throw new PeselAlreadyExistsException(PESEL_CANNOT_BE_DUPLICATED);
        }
        if(!isUserLegalAge(userDto.getPesel())){
            throw new UserAgeIsNotLegalException(AGE_LOWER_THAN_18);
        }

        UserAccount userAccount = createUserAccFromDto(userDto);
        userAccountRepository.save(userAccount);
        return userAccount;
    }

    public UserAccount getUserById(Long id){
        UserAccount one = userAccountRepository.findById(id).get();
        return one;
    }

    public List<UserAccount> getAllUsers(){
        return userAccountRepository.findAll();
    }

    public UserAccount getUserByPesel(String pesel){
        return userAccountRepository.findByPesel(pesel);
    }

    private UserAccount createUserAccFromDto(CreateUserAccountDto userAccDto){
        return UserAccount.builder()
                .name(userAccDto.getName())
                .surname(userAccDto.getSurname())
                .pesel(userAccDto.getPesel())
                .baseAccountAmount(userAccDto.getBaseAccountAmount())
                .build();
    }

    private boolean isPeselExistingInDB(String pesel){
        return userAccountRepository.existsAllByPesel(pesel);
    }

    private boolean isUserLegalAge(String pesel){
        Integer userYearBirth = null;
        String userYearEndBirth = pesel.substring(0,2);
        int userMonthBirth = Integer.parseInt(pesel.substring(2, 4));
        if(userMonthBirth - 20 > 0 && userMonthBirth - 20 <= 12){
            String fullUserYearBirth = "20" + userYearEndBirth;
            userYearBirth = Integer.valueOf(fullUserYearBirth);
        }else{
            String fullUserYearBirth = "19" + userYearEndBirth;
            userYearBirth = Integer.valueOf(fullUserYearBirth);
        }
        Integer currentYear = LocalDateTime.now().getYear();
        if(currentYear - userYearBirth >=18){
            return true;
        }else{
            return false;
        }
    }
}
