// routes/userRoutes.js
const express = require('express');
const {auth, getCalendar} = require('./controller.js')
const router = express.Router();


router.post('/auth/google/android', auth );
router.get('/calendar',  getCalendar);
// router.post('/logout', verifyToken, logout);





module.exports = router;
