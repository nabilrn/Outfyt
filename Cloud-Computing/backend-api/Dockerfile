# Gunakan Node versi yang dibutuhkan
FROM node:21.6.2

# Atur direktori kerja
WORKDIR /server

# Salin file yang dibutuhkan
COPY package*.json ./
COPY . .

# Instal dependencies
RUN npm install

# Ekspos port yang akan digunakan
EXPOSE 8080

# Jalankan aplikasi
CMD ["npm", "start"]
