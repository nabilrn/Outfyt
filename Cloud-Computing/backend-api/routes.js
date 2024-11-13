const express = require('express');
const { auth, getCalendar, uploadImage } = require('./controller.js');
const { verifyGoogleToken } = require('./auth.js');
const multer = require('multer');
// const path = require('path');
const router = express.Router();



const upload = multer({
    storage: multer.memoryStorage(), // simpan di memori sementara sebelum di-upload ke Cloud
  });
  
router.post('/auth/google/android', auth);
router.get('/calendar', verifyGoogleToken, getCalendar);
router.post('/upload-image',  verifyGoogleToken, upload.single('image'), uploadImage);


module.exports = router;