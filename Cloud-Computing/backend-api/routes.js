const express = require('express');
const { auth, getCalendar } = require('./controller.js');
const { verifyGoogleToken } = require('./auth.js');
const router = express.Router();

router.post('/auth/google/android', auth);
router.get('/calendar', verifyGoogleToken, getCalendar);

module.exports = router;