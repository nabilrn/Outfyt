const { google } = require("googleapis");
const { User } = require("./models/index.js");
require("dotenv").config({ path: "../../.env" });
// require("dotenv").config();

const auth = async (req, res) => {
  const { idToken, authCode } = req.body;

  console.log("Received idToken: ", idToken);
  console.log("Received authCode: ", authCode);

  try {
    // Setup OAuth2 client
    const client = new google.auth.OAuth2(
      process.env.GOOGLE_CLIENT_ID,
      process.env.GOOGLE_CLIENT_SECRET,
      process.env.REDIRECT_URI
    );

    // Dapatkan tokens dari authCode
    const { tokens } = await client.getToken(authCode);

    console.log("Received tokens: ", tokens);

    // Verifikasi ID token
    const ticket = await client.verifyIdToken({
      idToken: idToken,
      audience: process.env.GOOGLE_CLIENT_ID,
    });

    const payload = ticket.getPayload();
    const googleId = payload["sub"];

    try {
      // Cek apakah user sudah ada di database
      let user = await User.findByPk(googleId);

      if (user) {
        // User sudah ada, update data
        const updateData = {
          displayName: payload["name"],
          email: payload["email"],
          photoUrl: payload["picture"],
        };

        // Jika dapat refresh token baru, update di database
        if (tokens.refresh_token) {
          updateData.refreshTokenOauth = tokens.refresh_token;
        }

        await user.update(updateData);
      } else {
        // User belum ada, buat user baru
        user = await User.create({
          googleId: googleId,
          displayName: payload["name"],
          email: payload["email"],
          photoUrl: payload["picture"],
          refreshTokenOauth: tokens.refresh_token || null,
        });
      }

      // Siapkan response tokens
      let responseTokens = {
        access_token: tokens.access_token,
        refresh_token: tokens.refresh_token,
      };

      // Jika tidak dapat refresh token baru, gunakan yang ada di database
      if (!tokens.refresh_token && user.refreshTokenOauth) {
        responseTokens.refresh_token = user.refreshTokenOauth;
      }

      console.log(
        "Response  tokens setelah cek db: ",
        responseTokens.refresh_token
      );
      console.log("Response  tokens asli: ", tokens.refresh_token);

      // Kirim response ke client
      res.json({
        success: true,
        user: {
          googleId: user.googleId,
          displayName: user.displayName,
          email: user.email,
          photoUrl: user.photoUrl,
        },
        accessToken: responseTokens.access_token,
        refreshToken: responseTokens.refresh_token,
      });
    } catch (dbError) {
      console.error("Database error:", dbError);
      res.status(500).json({
        success: false,
        error: "Gagal menyimpan data user",
      });
    }
  } catch (error) {
    console.error("Error dalam autentikasi:", error);
    res.status(401).json({
      success: false,
      error: "Autentikasi gagal",
    });
  }
};

const getCalendar = async (req, res) => {
  console.log("Header", req.headers);
  const accessToken = req.headers["accesstoken"]; // Periksa akses ke header 'accesstoken'
  const refreshToken = req.headers["refreshtoken"]; // Periksa akses ke header 'refreshtoken'

  if (!accessToken || !refreshToken) {
    return res.status(400).json({
      success: false,
      error: "Access token atau refresh token tidak ditemukan di header",
    });
  }

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
};

module.exports = {
  auth,
  getCalendar,
};