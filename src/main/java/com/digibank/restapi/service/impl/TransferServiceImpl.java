package com.digibank.restapi.service.impl;

import com.digibank.restapi.dto.transfer.RekeningNameDto;
import com.digibank.restapi.dto.transfer.TransaksiDto;
import com.digibank.restapi.dto.transfer.RequestRekeningNameDto;
import com.digibank.restapi.dto.transfer.TransferDto;
import com.digibank.restapi.exception.AccountNotFoundException;
import com.digibank.restapi.exception.PinFailedException;
import com.digibank.restapi.exception.TransferFailedException;
import com.digibank.restapi.model.entity.Bank;
import com.digibank.restapi.model.entity.Rekening;
import com.digibank.restapi.model.entity.Transaksi;
import com.digibank.restapi.model.enums.AccountStatus;
import com.digibank.restapi.model.enums.JenisTransaksi;
import com.digibank.restapi.model.enums.TipeTransaksi;
import com.digibank.restapi.repository.TransaksiRepository;
import com.digibank.restapi.repository.TransferRepository;
import com.digibank.restapi.repository.UserRepository;
import com.digibank.restapi.service.TransferService;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class TransferServiceImpl implements TransferService {

    private TransferRepository transferRepository;
    private UserRepository userRepository;
    private TransaksiRepository transaksiRepository;


    @Override
    public TransaksiDto createTransfer(TransferDto transferDto) {
        Optional<Rekening> rekeningAsal = transferRepository.findByNoRekening(transferDto.getNoRekeningSumber());
        String mpinRekeningRequest = transferDto.getMpin();

        if (rekeningAsal.isEmpty()) {
            throw new TransferFailedException("Rekening Asal Tidak Ditemukan");
        }

        Integer getCountMpinWrong = rekeningAsal.get().getIdCif().getIdUsers().getCountBlockedMpin();


        AccountStatus accountStatus = rekeningAsal.get().getIdCif().getIdUsers().getStatusUser();
        if (accountStatus.equals(AccountStatus.TERBLOKIR)) {
            throw new TransferFailedException("Akun anda Sedang diblokir");
        }

        Optional<Rekening> rekeningTujuan = transferRepository.findByNoRekening(transferDto.getNoRekeningTujuan());
        if (rekeningTujuan.isEmpty()) {
            throw new TransferFailedException("Rekening Tujuan Tidak Ditemukan");
        }

        if(rekeningAsal.equals(rekeningTujuan)){
            throw new TransferFailedException("Rekening Tujuan Tidak Boleh Sama Dengan Rekening Asal");
        }

        AccountStatus accountStatusTujuan = rekeningTujuan.get().getIdCif().getIdUsers().getStatusUser();
        if (accountStatusTujuan.equals(AccountStatus.TERBLOKIR)) {
            throw new TransferFailedException("Maaf! Nomor Rekening yang dituju terblokir");
        }

        String matcherRekeningAsal = rekeningAsal.get().getIdCif().getIdUsers().getMpin();

        if (mpinRekeningRequest.equals(matcherRekeningAsal)) {

            if (transferDto.getNominal() < 10000) {
                throw new TransferFailedException("Minimal transfer Rp. 10.000");
            }

            long getTypeRekeningAsal = rekeningAsal.get().getTipeRekening().getIdTipe();
            if (getTypeRekeningAsal == 1) {
                if (transferDto.getNominal() > 5000000) {
                    throw new TransferFailedException("Maksimal transfer untuk SILVER Rp. 5.000.000");
                }
            } else if (getTypeRekeningAsal == 2) {
                if (transferDto.getNominal() > 10000000) {
                    throw new TransferFailedException("Maksimal transfer untuk GOLD Rp. 10.000.000");
                }
            } else if (getTypeRekeningAsal == 3) {
                if (transferDto.getNominal() > 15000000) {
                    throw new TransferFailedException("Maksimal transfer untuk PLATINUM Rp. 15.000.000");
                }
            }

            if (rekeningAsal.get().getSaldo() < transferDto.getNominal()) {
                throw new TransferFailedException("Saldo anda tidak mencukupi");
            }

            rekeningAsal.get().setSaldo(rekeningAsal.get().getSaldo() - transferDto.getNominal());
            rekeningTujuan.get().setSaldo(rekeningTujuan.get().getSaldo() + transferDto.getNominal());
            transferRepository.save(rekeningAsal.get());
            transferRepository.save(rekeningTujuan.get());

            rekeningAsal.get().getIdCif().getIdUsers().setCountBlockedMpin(0);
            rekeningAsal.get().getIdCif().getIdUsers().setStatusUser(AccountStatus.ACTIVE);
            userRepository.save(rekeningAsal.get().getIdCif().getIdUsers());
        } else {
            if (getCountMpinWrong == 1){
                rekeningAsal.get().getIdCif().getIdUsers().setCountBlockedMpin(getCountMpinWrong + 1);
                userRepository.save(rekeningAsal.get().getIdCif().getIdUsers());
                throw new PinFailedException("1");
            } else if (getCountMpinWrong >= 2) {
                rekeningAsal.get().getIdCif().getIdUsers().setStatusUser(AccountStatus.TERBLOKIR);
                rekeningAsal.get().getIdCif().getIdUsers().setCountBlockedMpin(0);
                userRepository.save(rekeningAsal.get().getIdCif().getIdUsers());
                throw new TransferFailedException("Maaf! Akun ini telah terblokir karena salah memasukkan MPIN 3 kali, Silahkan hubungi call center terdekat untuk membuka blokir akun");
            } else {
                rekeningAsal.get().getIdCif().getIdUsers().setCountBlockedMpin(getCountMpinWrong + 1);
                userRepository.save(rekeningAsal.get().getIdCif().getIdUsers());
                throw new PinFailedException("2");
            }
        }

        Bank bank = new Bank();
        bank.setKodeBank(1);

        Transaksi transaksi = new Transaksi();
        transaksi.setRekeningTujuan(rekeningTujuan.get());
        transaksi.setRekeningAsal(rekeningAsal.get());
        transaksi.setJenisTransaksi(JenisTransaksi.PINDAHBUKU);
        transaksi.setNominal(transferDto.getNominal());
        transaksi.setCatatan(transferDto.getCatatan());
        transaksi.setTipeTransaksi(TipeTransaksi.KREDIT);
        transaksi.setBank(bank);
        transaksi.setTotalTransaksi(transferDto.getNominal());
        transaksiRepository.save(transaksi);

        Transaksi transaksiTujuan = new Transaksi();
        transaksiTujuan.setRekeningTujuan(rekeningTujuan.get());
        transaksiTujuan.setRekeningAsal(rekeningAsal.get());
        transaksiTujuan.setJenisTransaksi(JenisTransaksi.PINDAHBUKU);
        transaksiTujuan.setNominal(transferDto.getNominal());
        transaksiTujuan.setCatatan(transferDto.getCatatan());
        transaksiTujuan.setTipeTransaksi(TipeTransaksi.DEBIT);
        transaksiTujuan.setBank(bank);
        transaksiTujuan.setTotalTransaksi(transferDto.getNominal());
        transaksiRepository.save(transaksiTujuan);


        TransaksiDto transaksiDto = new TransaksiDto();
        transaksiDto.setId(transaksi.getKodeTransaksi());
        transaksiDto.setTimeTransaksi(transaksi.getWaktuTransaksi());
        transaksiDto.setBiayaAdmin(String.valueOf(0L));
        transaksiDto.setTotalTransaksi(String.format("%.0f",transaksi.getTotalTransaksi()));
        transaksiDto.setCatatan(transaksi.getCatatan());
        transaksiDto.setJenisTransaksi(transaksi.getJenisTransaksi());

        return transaksiDto;
    }

    @Override
    public Object getAccountRekening(RequestRekeningNameDto requestRekeningNameDto) {
        Optional<Rekening> getAccountTujuan = transferRepository.findByNoRekening(Long.parseLong(requestRekeningNameDto.getNoRekeningTujuan()));
        Optional<Rekening> getAccountAsal = transferRepository.findByNoRekening(Long.parseLong(requestRekeningNameDto.getNoRekeningAsal()));

        RekeningNameDto rekeningNameDto = new RekeningNameDto();
        if (getAccountTujuan.isPresent()) {

            if (getAccountAsal.equals(getAccountTujuan)){
                throw new TransferFailedException("Rekening Tujuan Tidak Boleh Sama Dengan Rekening Asal");
            }
            return getObject(getAccountTujuan, rekeningNameDto);

        } else {
            throw new AccountNotFoundException("Maaf! Nomor Rekening yang dituju" +
                    "tidak terdaftar. Pastikan memasukkan" +
                    "Nomor Rekening yang benar ");
        }
    }

    @Override
    public Object getAccountRekening(long noRekening) {
        Optional<Rekening> getAccountTujuan = transferRepository.findByNoRekening(noRekening);

        RekeningNameDto rekeningNameDto = new RekeningNameDto();
        if (getAccountTujuan.isPresent()) {

            return getObject(getAccountTujuan, rekeningNameDto);

        } else {
            throw new AccountNotFoundException("Maaf! Nomor Rekening yang dituju" +
                    "tidak terdaftar. Pastikan memasukkan" +
                    "Nomor Rekening yang benar ");
        }
    }

    @NotNull
    private Object getObject(Optional<Rekening> getAccountTujuan, RekeningNameDto rekeningNameDto) {
        AccountStatus getUser = getAccountTujuan.get().getIdCif().getIdUsers().getStatusUser();
        if (getUser.equals(AccountStatus.TERBLOKIR)) {
            throw new TransferFailedException("Maaf! Nomor Rekening yang dituju terblokir");
        }

        String numberRekening = String.valueOf(getAccountTujuan.get().getNoRekening());

        rekeningNameDto.setNoRekening(numberRekening);
        rekeningNameDto.setNama(getAccountTujuan.get().getIdCif().getNamaLengkap());
        rekeningNameDto.setNamaBank("DigiBank");
        return rekeningNameDto;
    }


}
