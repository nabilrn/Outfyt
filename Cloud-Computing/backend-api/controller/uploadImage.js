const { Storage } = require('@google-cloud/storage');
const axios = require('axios');  // Tambahkan axios untuk melakukan request ke endpoint /predict/color
const path = require('path');
const { User } = require('../models');  // Sesuaikan dengan model yang kamu gunakan
require("dotenv").config({ path: "../../../.env" });

const storage = new Storage({
  keyFilename: "./bucket-key.json",
});

const bucketName = process.env.BUCKET_NAME;

// Fungsi upload image dan update database
const uploadImage = async (req, res) => {
  try {
    console.log("File yang diupload:", req.file);
    console.log(bucketName);

    if (!req.file) {
      return res.status(400).send('Tidak ada file yang di-upload.');
    }

    // Memastikan gender dan age ada di dalam request body
    const { gender, age } = req.body;
    if (!gender || !age) {
      return res.status(400).send('Gender dan umur harus disertakan.');
    }

    // Menyimpan file di dalam folder 'gambar' di bucket
    const fileName = `image/${Date.now()}_${path.basename(req.file.originalname)}`;
    const blob = storage.bucket(bucketName).file(fileName);
    const blobStream = blob.createWriteStream({
      resumable: false,
      contentType: req.file.mimetype,
    });

    // Error handling di dalam `blobStream`
    blobStream.on('error', (err) => {
      console.error('Error saat meng-upload file ke Cloud Storage:', err);
      res.status(500).send('Gagal meng-upload file');
    });

    blobStream.on('finish', async () => {
      const publicUrl = `https://storage.googleapis.com/${bucketName}/${fileName}`;
      console.log('Upload berhasil, URL:', publicUrl);

      try {
        // Menentukan genderCategory berdasarkan umur dan jenis kelamin
        let genderCategory = '';
        const ageNumber = parseInt(age);

        if (ageNumber < 18) {
          genderCategory = gender === 'male' ? 'boy' : 'girl';
        } else {
          genderCategory = gender === 'male' ? 'man' : 'woman';
        }

        // Kirim request ke /predict/color dengan image_url dan model_url
        const modelUrl = `https://storage.googleapis.com/${bucketName}/model.h5`; // Gantilah ini jika model berada di lokasi lain
        
        const response = await axios.post('http://127.0.0.1:5000/predict/color', {
          image_url: publicUrl,
          model_url: modelUrl,  // Kirim model_url juga
        });

        const predictedClass = response.data.predicted_class;

        // Update data pengguna dengan hasil prediksi dan informasi genderCategory
        const user = await User.findOne({ where: { googleId: req.user.googleId } });

        if (user) {
          // Perbarui kolom colorType, faceImageUrl, gender, genderCategory
          user.colorType = predictedClass;
          user.faceImageUrl = publicUrl;
          user.gender = gender;
          user.genderCategory = genderCategory;  // Menyimpan genderCategory
          await user.save();
          console.log('User data updated successfully');
        } else {
          console.log('User not found');
        }

        res.status(200).send({
          message: 'Upload berhasil dan data diperbarui',
          url: publicUrl,
          predicted_class: predictedClass,
          gender: gender, // Kirim genderCategory dalam response
          age: ageNumber, // Kirim genderCategory dalam response
          genderCategory: genderCategory, // Kirim genderCategory dalam response
        });
      } catch (error) {
        console.error('Error saat mengirim request ke /predict/color:', error);
        res.status(500).send('Terjadi kesalahan saat mendapatkan prediksi');
      }
    });

    // Akhiri stream dengan buffer file yang di-upload
    blobStream.end(req.file.buffer);

  } catch (error) {
    console.error('Terjadi kesalahan pada proses upload:', error);
    res.status(500).send('Terjadi kesalahan saat meng-upload file');
  }
};

module.exports = {
  uploadImage,
};
