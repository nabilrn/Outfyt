// controllers/auth.controller.js
const { google } = require('googleapis');
const { User } = require('./models/index.js');
const { Schedule } = require('./models');  
require("dotenv").config({ path: "../../.env" });

const axios = require('axios');
const cheerio = require('cheerio');

// require("dotenv").config();


const auth = async (req, res) => {
  const { idToken, authCode } = req.body;

  try {
    const client = new google.auth.OAuth2(
      process.env.GOOGLE_CLIENT_ID,
      process.env.GOOGLE_CLIENT_SECRET,
      process.env.REDIRECT_URI
    );

    // Get tokens from authCode
    const { tokens } = await client.getToken(authCode);

    // Verify ID token
    const ticket = await client.verifyIdToken({
      idToken: idToken,
      audience: process.env.GOOGLE_CLIENT_ID,
    });

    const payload = ticket.getPayload();
    const googleId = payload['sub'];

    try {
      let user = await User.findByPk(googleId);

      if (user) {
        const updateData = {
          displayName: payload['name'],
          email: payload['email'],
          photoUrl: payload['picture'],
        };

        if (tokens.refresh_token) {
          updateData.refreshTokenOauth = tokens.refresh_token;
        }

        await user.update(updateData);
      } else {
        user = await User.create({
          googleId: googleId,
          displayName: payload['name'],
          email: payload['email'],
          photoUrl: payload['picture'],
          refreshTokenOauth: tokens.refresh_token,
        });
      }

      // Only send access token to client
      res.json({
        success: true,
        user: {
          googleId: user.googleId,
          displayName: user.displayName,
          email: user.email,
          photoUrl: user.photoUrl
        },
        accessToken: tokens.access_token
      });
    } catch (dbError) {
      console.error('Database error:', dbError);
      res.status(500).json({
        success: false,
        error: 'Failed to save user data'
      });
    }
  } catch (error) {
    console.error('Authentication error:', error);
    res.status(401).json({
      success: false,
      error: 'Authentication failed'
    });
  }
};

const getCalendar = async (req, res) => {
  try {
    const calendar = google.calendar({ version: 'v3', auth: req.oauth2Client });

    const today = new Date();
    // Mendapatkan tanggal 10 hari mendatang
    const tenDaysLater = new Date();
    tenDaysLater.setDate(today.getDate() + 10);

    const events = await calendar.events.list({
      calendarId: 'primary',
      timeMin: today.toISOString(),  // Waktu mulai dari hari ini
      timeMax: tenDaysLater.toISOString(),  // Waktu akhir 10 hari mendatang
      singleEvents: true,
      orderBy: 'startTime',
    });

    // Iterasi atas setiap event dan simpan ke database
    for (const event of events.data.items) {
      const startDateTime = event.start.dateTime || event.start.date;
      const startTime = startDateTime ? new Date(startDateTime).toISOString() : null;

      // Cari apakah ada entri dengan scheduleId yang sama
      const existingSchedule = await Schedule.findOne({
        where: { scheduleId: event.id },
      });

      if (existingSchedule) {
        // Jika data sudah ada, lakukan update
        existingSchedule.title = event.summary || '';
        existingSchedule.desc = event.description || '';
        existingSchedule.date = startDateTime ? startDateTime.split('T')[0] : null;
        existingSchedule.time = startTime ? startTime.split('T')[1] : null;
        existingSchedule.type = event.eventType || 'default';
        existingSchedule.updatedAt = new Date();

        await existingSchedule.save();
        console.log('Schedule updated:', existingSchedule);
      } else {
        // Jika data belum ada, buat entri baru
        const newSchedule = await Schedule.create({
          scheduleId: event.id,
          googleId: req.user.googleId, // Asumsi googleId pengguna sudah tersedia di req.user
          title: event.summary || '',
          desc: event.description || '',
          date: startDateTime ? startDateTime.split('T')[0] : null, // Ambil bagian tanggal dari dateTime
          time: startTime ? startTime.split('T')[1] : null, // Ambil bagian waktu dari dateTime
          type: event.eventType || 'default', // Asumsi ada eventType, atau bisa ganti dengan field lain
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
      error: 'Failed to fetch calendar data'
    });
  }
};


const scrapeNews = async (req,res)=>{
  try {
    // Fetch the webpage content
    const response = await axios.get('https://www.whowhatwear.com/fashion/outfit-ideas');
    const html = response.data;
    const $ = cheerio.load(html);
    
    // Initialize array to store results
    const articles = [];

    // Select all listing items
    $('.listing__item').each((i, element) => {
      const article = {
        title: $(element).find('.listing__title').text().trim(),
        synopsis: $(element).find('.listing__text--synopsis').text().trim(),
        author: $(element).find('.listing__text--byline').text().trim(),
        link: $(element).find('.listing__link').attr('href'),
        imageUrl: $(element).find('picture img').attr('src')
      };
      
      // Only add articles that have content
      if (article.title || article.synopsis) {
        articles.push(article);
      }
    });

    res.json({
      status: 'success',
      count: articles.length,
      data: articles
    });

  } catch (error) {
    console.error('Scraping error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Failed to scrape the website',
      error: error.message
    });
  }

};

const chat = async (req,res)=>{

};
module.exports = {
  auth,
  getCalendar,
  scrapeNews,
  chat
};