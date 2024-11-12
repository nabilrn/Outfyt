const { google } = require('googleapis');
const { User } = require('./models/index.js');
require("dotenv").config({ path: "../../.env" });
// require("dotenv").config();

const verifyGoogleToken = async (req, res, next) => {
  const accessToken = req.headers['authorization']?.split(' ')[1];
  
  if (!accessToken) {
    return res.status(401).json({
      success: false,
      error: 'Access token tidak ditemukan'
    });
  }

  try {
    const oauth2Client = new google.auth.OAuth2(
      process.env.GOOGLE_CLIENT_ID,
      process.env.GOOGLE_CLIENT_SECRET,
      process.env.REDIRECT_URI
    );

    // Verify the access token and get user info
    const tokenInfo = await oauth2Client.getTokenInfo(accessToken);
    const user = await User.findOne({ where: { googleId: tokenInfo.sub } });

    if (!user) {
      return res.status(401).json({
        success: false,
        error: 'User tidak ditemukan'
      });
    }

    // Set user and oauth client in request for use in route handlers
    req.user = user;
    req.oauth2Client = oauth2Client;
    
    // Set credentials including refresh token from database
    oauth2Client.setCredentials({
      access_token: accessToken,
      refresh_token: user.refreshTokenOauth
    });

    next();
  } catch (error) {
    if (error.response?.status === 401) {
      try {
        // Get user info from the expired token
        const decodedToken = JSON.parse(Buffer.from(accessToken.split('.')[1], 'base64').toString());
        const user = await User.findOne({ 
          where: { googleId: decodedToken.sub } 
        });
        
        if (!user || !user.refreshTokenOauth) {
          return res.status(401).json({
            success: false,
            error: 'Invalid refresh token'
          });
        }

        const oauth2Client = new google.auth.OAuth2(
          process.env.GOOGLE_CLIENT_ID,
          process.env.GOOGLE_CLIENT_SECRET,
          process.env.REDIRECT_URI
        );

        oauth2Client.setCredentials({
          refresh_token: user.refreshTokenOauth
        });

        const { credentials } = await oauth2Client.refreshAccessToken();
        
        // Set refreshed credentials
        req.user = user;
        req.oauth2Client = oauth2Client;
        req.newAccessToken = credentials.access_token;
        
        next();
      } catch (refreshError) {
        console.error('Error refreshing token:', refreshError);
        return res.status(401).json({
          success: false,
          error: 'Failed to refresh access token'
        });
      }
    } else {
      console.error('Auth middleware error:', error);
      return res.status(401).json({
        success: false,
        error: 'Authentication failed'
      });
    }
  }
};

module.exports = { verifyGoogleToken };