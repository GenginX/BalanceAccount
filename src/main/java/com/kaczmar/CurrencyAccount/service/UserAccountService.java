package com.kaczmar.CurrencyAccount.service;

import com.kaczmar.CurrencyAccount.dto.CreateUserAccountDto;
import com.kaczmar.CurrencyAccount.exceptions.*;
import com.kaczmar.CurrencyAccount.model.UserAccount;
import com.kaczmar.CurrencyAccount.repository.UserAccountRepository;
import org.hibernate.validator.internal.constraintvalidators.hv.pl.PESELValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserAccountService {

    public static final String AGE_LOWER_THAN_18 = "USER AGE IS LESS THAN 18. WE CAN'T CREATE ACCOUNT";
    public static final String PESEL_CANNOT_BE_DUPLICATED = "PROVIDED PESEL HAS BEEN USED FOR ANOTHER ACCOUNT";
    public static final String ID_NOT_FOUND = "USER WITH THIS ID NOT FOUND";
    public static final String PESEL_IS_NOT_CORRECT = "PROVIDED PESEL IS NOT CORRECT";
    private UserAccountRepository userAccountRepository;

    public UserAccountService(UserAccountRepository userRepository) {
        this.userAccountRepository = userRepository;
    }

    public UserAccount createUserAcc(CreateUserAccountDto userDto) throws Exception {
        ownPeselValidator(userDto);

        UserAccount userAccount = createUserAccFromDto(userDto);
        userAccountRepository.save(userAccount);
        return userAccount;
    }

    private void ownPeselValidator(CreateUserAccountDto userDto) throws PeselAlreadyExistsException, UserAgeIsNotLegalException, PeselIsNotValid {
        if (isPeselExistingInDB(userDto.getPesel())) {
            throw new PeselAlreadyExistsException(PESEL_CANNOT_BE_DUPLICATED);
        }
        if (!isUserLegalAge(userDto.getPesel())) {
            throw new UserAgeIsNotLegalException(AGE_LOWER_THAN_18);
        }
        if(! isPeselValid(userDto.getPesel())){
            throw new PeselIsNotValid(PESEL_IS_NOT_CORRECT);
        }
    }

    public UserAccount getUserById(Long id) throws NoUserFoundForGivenIdException {
        return userAccountRepository.findById(id).orElseThrow(() -> new NoUserFoundForGivenIdException(ID_NOT_FOUND));

    }

    public List<UserAccount> getAllUsers() {
        return userAccountRepository.findAll();
    }

    public UserAccount getUserByPesel(String pesel) throws NoUserFoundForGivenPeselException {
        return userAccountRepository.findByPesel(pesel).orElseThrow(() -> new NoUserFoundForGivenPeselException("USER WITH THIS PESEL NOT FOUND"));
    }

    private UserAccount createUserAccFromDto(CreateUserAccountDto userAccDto) {
        return UserAccount.builder()
                .name(userAccDto.getName())
                .surname(userAccDto.getSurname())
                .pesel(userAccDto.getPesel())
                .currentAccountBalance(userAccDto.getBaseAccountAmount())
                .currencyCode("PLN")
                .build();
    }

    private boolean isPeselExistingInDB(String pesel) {
        return userAccountRepository.existsAllByPesel(pesel);
    }

    private boolean isUserLegalAge(String pesel) {
        Integer userYearBirth = null;
        String userYearEndBirth = pesel.substring(0, 2);
        int userMonthBirth = Integer.parseInt(pesel.substring(2, 4));
        if (userMonthBirth - 20 > 0 && userMonthBirth - 20 <= 12) {
            String fullUserYearBirth = "20" + userYearEndBirth;
            userYearBirth = Integer.valueOf(fullUserYearBirth);
        } else {
            String fullUserYearBirth = "19" + userYearEndBirth;
            userYearBirth = Integer.valueOf(fullUserYearBirth);
        }
        Integer currentYear = LocalDateTime.now().getYear();
        if (currentYear - userYearBirth >= 18) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isPeselValid(String pesel){
        int[] weights = {1, 3, 7, 9, 1, 3, 7 ,9 ,1 ,3};
        int sum = 0;
        if(pesel.length() != 11){
            return false;
        }
        for(int i = 0; i < 10; i++){
            sum += Integer.parseInt(pesel.substring(i, i+1)) * weights[i];
        }
        int controlNumber = Integer.parseInt(pesel.substring(10,11));

        sum %= 10;
        sum = 10 - sum;
        sum %= 10;

        return sum == controlNumber;
    }
}
