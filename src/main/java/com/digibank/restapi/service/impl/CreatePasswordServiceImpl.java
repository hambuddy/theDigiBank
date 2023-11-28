package com.digibank.restapi.service.impl;

import com.digibank.restapi.dto.changePassword.ChangePasswordDto;
import com.digibank.restapi.dto.createPassword.CreatePasswordDto;
import com.digibank.restapi.exception.ResponseBadRequestException;
import com.digibank.restapi.exception.ResponseUnauthorizationException;
import com.digibank.restapi.model.entity.User;
import com.digibank.restapi.repository.UserRepository;
import com.digibank.restapi.service.PasswordService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
@AllArgsConstructor
public class CreatePasswordServiceImpl implements PasswordService {
    private final UserRepository userRepository;
    @Override
    public CreatePasswordDto createPassword(Long id_user, CreatePasswordDto request) {
        User user = userRepository.findById(id_user)
                .orElseThrow(() -> new ResponseUnauthorizationException("User tidak ditemukan"));

        String newPassword = request.getPassword();
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        userRepository.save(user);
        return request;
    }

    @Override
    public CreatePasswordDto changePasswordWithValidation(Long id_user, ChangePasswordDto changePasswordDto) {

        User user = userRepository.findById(id_user)
                .orElseThrow(() -> new ResponseUnauthorizationException("User tidak ditemukan"));

        if (!BCrypt.checkpw(changePasswordDto.getOldPassword(), user.getPassword())) {
            throw  new ResponseBadRequestException("Password tidak sesuai");
        }

        if (!Objects.equals(changePasswordDto.getConfirmPassword(), changePasswordDto.getNewPassword())) {
            throw  new ResponseBadRequestException("Password tidak sesuai");
        }

        String hashedPassword = BCrypt.hashpw(changePasswordDto.getNewPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);
        userRepository.save(user);

        return null;
    }
}
