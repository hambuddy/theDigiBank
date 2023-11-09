package com.digibank.restapi.service.impl;

import com.digibank.restapi.dto.otp.OtpDto;
import com.digibank.restapi.dto.otp.OtpRegenerateDto;
import com.digibank.restapi.dto.otp.OtpResponseDto;
import com.digibank.restapi.dto.otp.OtpVerificationDto;
import com.digibank.restapi.exception.OtpException.FailedException;
import com.digibank.restapi.mapper.UserMapper;
import com.digibank.restapi.mapper.UserOtpMapper;
import com.digibank.restapi.model.entity.User;
import com.digibank.restapi.model.entity.UserBank;
import com.digibank.restapi.model.entity.UserOTP;
import com.digibank.restapi.repository.UserOtpRepository;
import com.digibank.restapi.repository.UserRepository;
import com.digibank.restapi.service.OtpService;
import com.digibank.restapi.utils.EmailUtil;
import com.digibank.restapi.utils.OtpUtil;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
//@RequiredArgsConstructor
@AllArgsConstructor
public class OtpServiceImpl implements OtpService {

    private OtpUtil otpUtil;
    private EmailUtil emailUtil;
    private UserRepository userRepository;
    private UserOtpRepository userOtpRepository;

    @Override
    public OtpResponseDto register(OtpDto otpDto) {
        // Cari apakah pengguna dengan email tersebut sudah ada
        User existingUser = userRepository.findByEmail(otpDto.getEmail()).orElse(null);
        if (existingUser != null) {
            throw new FailedException("Email Sudah Terdaftar");
        }

        // Generate OTP
        String otp = otpUtil.generateOtp();

        try {
            // Kirim email OTP
            emailUtil.sendOtpEmail(otpDto.getEmail(), otp);
        } catch (MessagingException e) {
            throw new FailedException("Unable to send OTP. Please try again.");
        }

        // Simpan pengguna ke dalam database
        User user = UserMapper.MAPPER.mapToUser(otpDto);
        User savedUser = userRepository.save(user);

        // Simpan OTP ke dalam tabel UserOtp
        UserOTP userOtp = UserOtpMapper.MAPPER.mapToUserOtp(otpDto);
        userOtp.setOtp(otp);
        userOtp.setIdUser(savedUser);
        userOtp.setCreatedAt(new Date()); // Set createdAt ke waktu sekarang
        userOtpRepository.save(userOtp);

        // Mengembalikan data DTO
        OtpResponseDto responseDto = UserMapper.MAPPER.mapToOtpDto(savedUser);
        responseDto.setIdUser(savedUser.getIdUser());

        return responseDto;
    }

    @Override
    public OtpVerificationDto verifyOtp(User idUser, OtpDto otpDto) {
        // Cari UserOtp berdasarkan id_otp
//        UserOTP userOtp = userOtpRepository.findById(id_otp)
//                .orElseThrow(() -> new FailedException("Kode OTP yang dimasukkan tidak valid"));

        // Pastikan id_user cocok dengan yang ditemukan
        Optional<UserOTP> userOTP = Optional.ofNullable(userOtpRepository.findByIdUser(idUser)
                .orElseThrow(() -> new FailedException("User tidak ditemukkan")));;

        // Periksa apakah waktu OTP masih berlaku (2 menit)
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime createdAt = userOTP.get().getCreatedAt();
        long diffInSeconds = Duration.between(createdAt, currentTime).getSeconds();
        if (diffInSeconds < (2 * 60)) {
            User user = userOTP.get().getIdUser();
            Boolean active = user.getActive();

            if (active != null && active) {
                // Akun sudah terverifikasi, kirim respons kesalahan
                throw new FailedException("Akun sudah terverifikasi.");
            }

            // Tandai akun sebagai terverifikasi
            if (active == null) {
                active = true;
            }
            user.setActive(active);
            userRepository.save(user);

            // Hapus kolom id_otp dari entitas UserOtp
            userOtpRepository.delete(userOTP.get());

            // Mengembalikan respons sukses
            return new OtpVerificationDto("OTP Terverifikasi");
        } else {
            // Verifikasi gagal, hapus entitas UserOtp
            userOtpRepository.delete(userOTP.get());

            // Mengembalikan respons dengan status 400 dan pesan error
            throw new FailedException("Kode OTP yang dimasukkan tidak valid");
        }
    }

    // Metode ini akan dijalankan setiap 2 menit
    @Transactional
    @Scheduled(fixedRate = 120000)
    public void deleteExpiredUserOtp() {
        LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
        userOtpRepository.deleteByCreatedAtBefore(twoMinutesAgo);
    }

    @Override
    public String regenerateOtp(OtpRegenerateDto otpRegenerateDto, User idUser) {

        Optional<UserOTP> userOtp = Optional.ofNullable(userOtpRepository.findByIdUser(idUser)
                .orElseThrow(() -> new FailedException("ID user tidak ditemukan")));


        userRepository.findByEmail(otpRegenerateDto.getEmail())
                .orElseThrow(() -> new FailedException("Email tidak dapat ditemukan"));

        String otp = otpUtil.generateOtp();
        try {
            emailUtil.sendOtpEmail(otpRegenerateDto.getEmail(), otp);
        } catch (MessagingException e) {
            throw new FailedException("Tidak dapat mengirim otp, silakan coba lagi");
        }
        userOtp.get().setOtp(otp);
        userOtp.get().setCreatedAt(new Date());

        userOtpRepository.save(userOtp.get());

        return "OTP Terkirim Kembali";
    }

}