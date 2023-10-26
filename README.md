# Auth API Documentation

Base URL: http://digibank/api/v1


## Belum punya akun & Rekening

### Choose Rekening Type / Card Type

Setelah user pilih kartu, data tipe rekening ditampung di fe, nantinya akan dipost ketika user klik button lanjut pada isi data diri/cif

Endpoint : GET/users/cards

Response : 

```json
{
    "status" : 200,
    "data" :[
        {
            "id" : 1,
            "nama" : "Silver",
            "limit_transfer" : "5 Juta"
        },
        {
            "id" : 2,
            "nama" : "Gold",
            "limit_transfer" : "10 Juta"
        },
        {    
            "id" : 3,
            "nama" : "Platinum",
            "limit_transfer" : "15 Juta"
        },
    ]
}
```


### Email Confirmation / OTP Generate


Endpoint : POST/users/otp-generate

Request Body :

```json
{
    "email" : "budi@gmail.com"
}
```

Response Body (succes) :

```json
{
    "status" : 200,
    "id_user" : 1,
    "email"   : "budi@gmail.com" 
}
```

### OTP Verification / OTP Confirmation


Endpoint : POST /users/{id}/otp-verification

Request Body :

```json
{
     "otp" : "1234"
}
```

Response Body (succes) :

```json
{
    "status" : 200,
    "message" : "Success"
}
```

Response Body (failed) :

```json
{
    "status" : 400,
    "message" : "Maaf Kode OTP yang dimasukkan tidak valid. Silahkan coba lagi."
}
```

### Email Confirmation / OTP Generate Resend


Endpoint : POST/users/otp-regenerate

Request Body :

```json
{
    "email" : "budi@gmail.com"
}
```

Response Body (succes) :

```json
{
    "status" : 200,
    "id_user" : 1,
    "email"   : "budi@gmail.com" 
}
```

### CIF


Endpoint : POST /users/cif

Request Body :

```json
{
    "nik" : "123456",
    "alamat" : "Maguwo 12",
    "nama_lengkap" : "Suparman"
    "pekerjaan" : "Pegawai Negeri Sipil (PNS)",
    "penghasilan" : "0 - 5.000.000",
   
}
```

Response Body (success) :

```json
{
    "status" : 200,
    "message" : "success",
    "id_cif" : 1,
    "no_rek" : "12345678"
}
```

### Create Rekening


Endpoint : POST /users/account

Request Body :

```json
{
    "no_rek" : "12345678",
    "id_cif" : 1,
    "id_tipe_rekening" : 3 // post id tipe rekening yang sudah ditampung
}
```

Response Body (success) :

```json
{
    "status" : 200,
    "message" : "success",
}
```



### Create Password


Endpoint : POST /users/{id}/password

Request Body :

```json
{
    "id_cif" : 1
    "password" : "rahasia1"
}
```

Response Body (success) :

```json
{
    "status" : 200,
    "message" : "Kata Sandi Berhasil Disimpan" 
}
```

Response Body (failed) :

```json
{
    "status" : 408,
    "message" : "Maaf! Kata Sandi gagal disimpan. Silakan masukkan ulang Kata Sandi" 
}
```


### Create MPIN


Endpoint : POST /users/{id}pin

Request Body :

```json
{
    "mpin" : "123456"
}
```

Response Body (success) :

```json
{
    "status" : 200,
    "message" : "Selamat! Akun Berhasil dibuat. Silakan Melakukan Login." 
}
```


## Belum punya akun & sudah punya rekening


### Email Confirmation / OTP Generate


Endpoint : POST/users/otp-generate

Request Body :

```json
{
    "email" : "budi@gmail.com"
}
```

Response Body (succes) :

```json
{
    "status" : 200,
    "id_user" : 1,
}
```

### OTP Verification / OTP Confirmation


Endpoint : POST /users/{id}/otp-verification

Request Body :

```json
{
     "otp" : "1234"
}
```

Response Body (succes) :

```json
{
    "status" : 200,
    "message" : "Success"
}
```

Response Body (failed) :

```json
{
    "status" : 400,
    "message" : "Maaf Kode OTP yang dimasukkan tidak valid. Silahkan coba lagi."
}
```

### Konfirmasi Rekening

Endpoint : POST /users/confirm-accounts

Request Body :

```json
{
    "nomor_rekening" : "12345678"
}
```

Response Body (success) :

```json
{
    "status" : 200,
    "message" : "Success"
    "id_cif" : 1
}
```

Response Body (failed) :

```json
{
    "status" : 401,
    "message" : "Nomor Rekening tidak terdaftar"
    "id_cif" : null
}
```


### Konfirmasi CIF

Endpoint : GET /users/{id_cif}/confirm-cif


Response Body (success) :

```json
{
    "nik" : "1234567887654321",
    "nama_lengkap" : "Suparman"
}
```


### Create Password


Endpoint : POST /users/{id}/password

Request Body :

```json
{
    "id_cif" : 1
    "password" : "rahasia1"
}
```

Response Body (success) :

```json
{
    "status" : 200,
    "message" : "Kata Sandi Berhasil Disimpan" 
}
```

Response Body (failed) :

```json
{
    "status" : 408,
    "message" : "Maaf! Kata Sandi gagal disimpan. Silakan masukkan ulang Kata Sandi" 
}
```


### Create MPIN


Endpoint : POST /users/{id}pin

Request Body :

```json
{
    "mpin" : "123456"
}
```

Response Body (success) :

```json
{
    "status" : 200,
    "message" : "Selamat! Akun Berhasil dibuat. Silakan Melakukan Login." 
}
```



### User Login


Endpoint : POST /users/login

Request Body :

```json
{
    "email" : "fahrizalshofyanaziz@gmail.com"
    "password" : "rahasia1"
}
```

Response Body (success) :

```json
{
    "status" : 200,
    "message" : "Login Berhasil!"
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1dWlkIjoiYTQzNjUzYjEtMjk2Ni00NDY1LWE0YjktZjRmYmM0OTE3NzVhIiwiaWF0IjoxNjg2MzIxMzQ0LCJleHAiOjE2ODYzMjE2NDR9.mzHMPKXzlOkHpRFAq3Sol5ALtc5TH0l_o4aN4YZxLMA"
    
}
```

Response Body (failed email&password) :

```json
{
    "status" : 401,
    "message" : "Maaf! Email dan Kata Sandi yang dimasukkan salah. Pastikan Email dan Kata Sandi benar."
}
```



