const { Storage } = require('@google-cloud/storage');
require("dotenv").config({ path: "../../../.env" });
const path = require('path');
const storage = new Storage({
  keyFilename:  "./bucket-key.json",
});

const bucketName = process.env.BUCKET_NAME;

// Improved model verification function
const uploadImage=  async (req, res) => {
  try {
    console.log("File yang diupload:", req.file);
    console.log(bucketName);

    if (!req.file) {
      return res.status(400).send('Tidak ada file yang di-upload.');
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

    blobStream.on('finish', () => {
      const publicUrl = `https://storage.googleapis.com/${bucketName}/${fileName}`;
      console.log('Upload berhasil, URL:', publicUrl);

     
      res.status(200).send({
        message: 'Upload berhasil',
        url: publicUrl
      });
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