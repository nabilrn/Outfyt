const { google } = require("googleapis");

require('dotenv').config({ path: '../../.env' });


const auth = async (req, res) => {
  const { idToken, authCode } = req.body;

  console.log("Received idToken: ", idToken);
  console.log("Received authCode: ", authCode);

  try {
    // Tukarkan authCode dengan access token menggunakan OAuth2
    const client = new google.auth.OAuth2(
      process.env.GOOGLE_CLIENT_ID,
      process.env.GOOGLE_CLIENT_SECRET,
      process.env.REDIRECT_URI
    );

    const { tokens } = await client.getToken(authCode); // Tukarkan authCode dengan access token

    client.getToken(authCode).catch((error) => {
      console.log("Error exchanging auth code for tokens:", error);
    });

    // Verifikasi ID token
    const ticket = await client.verifyIdToken({
      idToken: idToken,
      audience: process.env.GOOGLE_CLIENT_ID,
    });

    const payload = ticket.getPayload();

    // Kirim response ke Android dengan informasi pengguna
    res.json({
      success: true,
      user: {
        googleId: payload["sub"],
        displayName: payload["name"],
        email: payload["email"],
        photoUrl: payload["picture"],
      },
      accessToken: tokens.access_token, // Kirim accessToken yang didapat dari authCode
      refreshToken: tokens.refresh_token, // Kirim refreshToken yang didapat dari authCode
    });

  } catch (error) {
    console.error("Error dalam autentikasi:", error);
    res.status(401).json({
      success: false,
      error: "Autentikasi gagal",
    });
  }
};
const getCalendar = async (req, res) => {
  const { accessToken, refreshToken } = req.headers;

  try {
    // Buat OAuth2 client dengan accessToken dan refreshToken
    const oauth2Client = new google.auth.OAuth2(
      process.env.GOOGLE_CLIENT_ID,
      process.env.GOOGLE_CLIENT_SECRET,
      process.env.REDIRECT_URI
    );
    oauth2Client.setCredentials({
      access_token: accessToken,
      refresh_token: refreshToken,
    });

    // Panggil Google Calendar API
    const calendar = google.calendar({ version: "v3", auth: oauth2Client });

    // Ambil daftar acara dari kalender utama pengguna
    const events = await calendar.events.list({
      calendarId: "primary",
      timeMin: new Date().toISOString(),
      maxResults: 10,
      singleEvents: true,
      orderBy: "startTime",
    });

    // Kirim respons ke klien dengan daftar acara
    res.json({
      success: true,
      events: events.data.items,
    });
  } catch (error) {
    console.error("Error dalam mengambil data kalender:", error);

    // Periksa apakah error karena akses token kedaluwarsa
    if (error.response && error.response.status === 401) {
      try {
        // Gunakan refresh token untuk mendapatkan access token baru
        const newTokens = await oauth2Client.refreshAccessToken();
        oauth2Client.setCredentials(newTokens.credentials);

        // Panggil ulang Google Calendar API dengan token baru
        const calendar = google.calendar({ version: "v3", auth: oauth2Client });
        const events = await calendar.events.list({
          calendarId: "primary",
          timeMin: new Date().toISOString(),
          maxResults: 10,
          singleEvents: true,
          orderBy: "startTime",
        });

        // Kirim respons dengan data acara dan access token baru
        res.json({
          success: true,
          events: events.data.items,
          newAccessToken: newTokens.credentials.access_token,
        });
      } catch (refreshError) {
        console.error("Error dalam memperbarui access token:", refreshError);
        res.status(500).json({
          success: false,
          error: "Gagal memperbarui access token",
        });
      }
    } else {
      res.status(500).json({
        success: false,
        error: "Gagal mengambil data kalender",
      });
    }
  }
}
  
module.exports ={
    auth,
    getCalendar,

}