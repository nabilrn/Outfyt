# Gunakan Node versi yang dibutuhkan
FROM node:22

# Atur direktori kerja
WORKDIR /app

# Salin file yang dibutuhkan
COPY package*.json ./
COPY . .

# Instal dependencies
RUN npm install

# Ekspos port yang akan digunakan
EXPOSE 3000

# Jalankan aplikasi
CMD ["npm", "start"]
