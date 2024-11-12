// controllers/auth.controller.js
const { google } = require('googleapis');
const { User } = require('./models/index.js');
require("dotenv").config({ path: "../../.env" });
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

    const events = await calendar.events.list({
      calendarId: 'primary',
      timeMin: new Date().toISOString(),
      maxResults: 10,
      singleEvents: true,
      orderBy: 'startTime',
    });

    const response = {
      success: true,
      events: events.data.items,
    };

    // If token was refreshed, send new access token
    if (req.newAccessToken) {
      response.newAccessToken = req.newAccessToken;
    }

    res.json(response);
  } catch (error) {
    console.error('Calendar fetch error:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to fetch calendar data'
    });
  }
};

module.exports = {
  auth,
  getCalendar,
};