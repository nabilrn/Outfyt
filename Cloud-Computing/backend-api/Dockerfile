# Gunakan Node versi yang dibutuhkan
FROM node:18

# Atur direktori kerja
WORKDIR /app

# Salin file package terlebih dahulu
COPY package*.json ./

# Instal dependencies
RUN npm install

# Salin seluruh kode aplikasi
COPY . .

# Ekspos port yang akan digunakan
EXPOSE 8080

# Pastikan mendengarkan pada semua antarmuka
CMD ["npm", "start", "--host", "0.0.0.0", "--port", "8080"]