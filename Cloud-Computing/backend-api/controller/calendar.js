// controllers/auth.controller.js
const { google } = require('googleapis');
const { Schedule } = require('../models');  
require("dotenv").config({ path: "../../.env" });

const axios = require('axios');
const base_url = process.env.FLASK_BASE_URL;
const bucket_name= process.env.BUCKET_NAME;
const folder_name= process.env.FOLDER_ACTIVITY_MODEL;


// require("dotenv").config();

const getCalendar = async (req, res) => {
  try {
    console.log("ini req. oauth", req.oauth2Client);
    const calendar = google.calendar({ version: 'v3', auth: req.oauth2Client });

    const today = new Date();
    const tenDaysLater = new Date();
    tenDaysLater.setDate(today.getDate() + 10);

    // Mengambil events dari kalender
    const events = await calendar.events.list({
      calendarId: 'primary',
      timeMin: today.toISOString(),
      timeMax: tenDaysLater.toISOString(),
      singleEvents: true,
      orderBy: 'startTime',
    });

    // Iterasi untuk menyimpan atau memperbarui data event
    for (const event of events.data.items) {
      const startDateTime = event.start.dateTime || event.start.date;
      const startTime = startDateTime ? new Date(startDateTime).toISOString() : null;

      // Cari apakah sudah ada entri di database berdasarkan event ID
      const existingSchedule = await Schedule.findOne({
        where: { scheduleId: event.id },
      });

      if (existingSchedule) {
        // Jika sudah ada, perbarui data
        existingSchedule.title = event.summary || '';
        existingSchedule.desc = event.description || '';
        existingSchedule.date = startDateTime ? startDateTime.split('T')[0] : null;
        existingSchedule.time = startTime ? startTime.split('T')[1] : null;
        existingSchedule.updatedAt = new Date();

        // Cek apakah type masih kosong (belum diprediksi)
        if (existingSchedule.type === null) {
          // Kirim ke prediksi
          const predictionResponse = await axios.post(`${base_url}/predict/activity`, {
            folder_url: `https://storage.googleapis.com/${bucket_name}/${folder_name}/`,
            input_text: event.summary,
          });

          const predictedCategory = predictionResponse.data.predicted_category;
          existingSchedule.type = predictedCategory ;
        }

        await existingSchedule.save();
        console.log('Schedule updated:', existingSchedule);
      } else {
        // Jika belum ada, buat entri baru
        const newSchedule = await Schedule.create({
          scheduleId: event.id,
          googleId: req.user.googleId, // Asumsi googleId pengguna sudah tersedia
          title: event.summary || '',
          desc: event.description || '',
          date: startDateTime ? startDateTime.split('T')[0] : null,
          time: startTime ? startTime.split('T')[1] : null,
        // Masukkan "default" sebagai type pertama kali
          createdAt: new Date(),
          updatedAt: new Date(),
        });

        console.log('Schedule saved:', newSchedule);
      }
    }

    res.json({
      success: true,
      events: events.data.items,
    });

  } catch (error) {
    console.error('Calendar fetch error:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to fetch calendar data',
    });
  }
};



module.exports = { getCalendar};