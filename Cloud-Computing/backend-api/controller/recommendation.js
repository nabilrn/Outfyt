const { promisify } = require('util');
const axios = require('axios');
const cheerio = require('cheerio');
const NodeCache = require('node-cache');

const { User, Schedule, Recommendation } = require('../models');

// Konfigurasi environment dan konstanta
require('dotenv').config({ path: '../../.env' });
const BASE_URL = process.env.FLASK_BASE_URL;
const AMAZON_BASE_URL = 'https://www.amazon.com/s?k=';

// Cache untuk menyimpan hasil scraping
const amazonCache = new NodeCache({ 
  stdTTL: 3600, // Cache berlaku selama 1 jam
  checkperiod: 600 // Periksa expired cache setiap 10 menit
});

/**
 * Capitalize first letter of a string
 * @param {string} str - Input string
 * @returns {string} Capitalized string
 */
const capitalizeFirstLetter = (str) => {
  if (!str) return '';
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
};

/**
 * Scrape Amazon untuk mendapatkan detail produk
 * @param {string} query - Kueri pencarian produk
 * @returns {Promise<Object|null>} Detail produk atau null
 */
const scrapeAmazon = async (query) => {
  // Cek cache terlebih dahulu
  const cachedResult = amazonCache.get(query);
  if (cachedResult) return cachedResult;

  try {
    const response = await axios.get(AMAZON_BASE_URL + encodeURIComponent(query), {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36',
        'Accept-Language': 'en-US,en;q=0.9'
      },
      timeout: 10000 // Timeout 10 detik
    });

    const $ = cheerio.load(response.data);
    const firstResult = $('div[data-component-type="s-search-result"]').first();

    const productDetails = {
      imageUrl: firstResult.find('img.s-image').attr('src'),
      productLink: 'https://www.amazon.com' + firstResult.find('a.a-link-normal.s-underline-text').attr('href'),
      productName: firstResult.find('span.a-size-base-plus.a-color-base').text().trim(),
      productPrice: `$${firstResult.find('span.a-price-whole').text().trim()}.${firstResult.find('span.a-price-fraction').text().trim()}`
    };

    if (productDetails.imageUrl && productDetails.productLink) {
      // Simpan ke cache
      amazonCache.set(query, productDetails);
      return productDetails;
    }

    return null;
  } catch (error) {
    console.error(`Amazon scraping error for "${query}":`, error.message);
    return null;
  }
};

/**
 * Dapatkan rekomendasi produk untuk jadwal tertentu
 * @param {Object} req - Request objek Express
 * @param {Object} res - Response objek Express
 */
const recommendation = async (req, res) => {
  try {
    const { scheduleId } = req.body;

    // Validasi input
    if (!scheduleId) {
      return res.status(400).json({ error: 'Schedule ID is required' });
    }

    const user = await User.findByPk(req.user.googleId);
    const schedule = await Schedule.findByPk(scheduleId);

    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    if (!schedule) {
      return res.status(404).json({ error: 'Schedule not found' });
    }

    // Tentukan musim
    const scheduleDate = new Date(schedule.date);
    const month = scheduleDate.getMonth() + 1;
    const season = month >= 11 || month <= 3 ? 'Winter' : 'Summer';

    // Persiapkan parameter
    const recommendationParams = {
      user_id: user.googleId,
      input_params: {
        skin_tone: capitalizeFirstLetter(user.colorType),
        gender: capitalizeFirstLetter(user.genderCategory),
        usage: capitalizeFirstLetter(schedule.type),
        season: season
      }
    };

    // Ambil rekomendasi dari API
    const apiResponse = await axios.post(`${BASE_URL}/get_recommendation`, recommendationParams);
    const recommendations = apiResponse.data.recommendations.Outfit;

    // Hapus rekomendasi sebelumnya
    await Recommendation.destroy({
      where: { scheduleId: scheduleId }
    });

    // Scraping produk secara paralel
    const scrapingPromises = Object.entries(recommendations).map(async ([category, products]) => {
      const categoryResults = await Promise.all(
        products.map(async (product) => {
          const { productDisplayName } = product;
          
          // Simpan rekomendasi ke database
          const recommendation = await Recommendation.create({
            scheduleId: scheduleId,
            displayName: productDisplayName,
            subCategory: category
          });

          // Lakukan scraping
          const scrappedData = await scrapeAmazon(productDisplayName);

          return scrappedData ? {
            recommendationId: recommendation.recommendationId,
            name: productDisplayName,
            imageUrl: scrappedData.imageUrl,
            productLink: scrappedData.productLink
          } : null;
        })
      );

      return { 
        category, 
        results: categoryResults.filter(result => result !== null) 
      };
    });

    // Jalankan semua promise scraping
    const scrappedResults = await Promise.all(scrapingPromises);

    // Konversi array ke objek
    const formattedResults = scrappedResults.reduce((acc, { category, results }) => {
      acc[category] = results;
      return acc;
    }, {});

    res.json(formattedResults);

  } catch (error) {
    console.error('Recommendation error:', error);
    res.status(500).json({ 
      error: 'An error occurred while processing the recommendation',
      details: error.message 
    });
  }
};


/**
 * Dapatkan rekomendasi produk untuk jadwal tertentu
 * @param {Object} req - Request objek Express
 * @param {Object} res - Response objek Express
 */
const addLike = async (req, res) => {
  try {
    const { recommendationId } = req.body;

    // Validasi input
    if (!recommendationId) {
      return res.status(400).json({ error: 'Recommendation ID is required' });
    }

    // Cari user yang sedang login
    const user = await User.findByPk(req.user.googleId);
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    // Cari detail rekomendasi
    const recommendation = await Recommendation.findByPk(recommendationId, {
      include: [{ 
        model: Schedule, 
        attributes: ['type', 'date'] 
      }]
    });

    if (!recommendation) {
      return res.status(404).json({ error: 'Recommendation not found' });
    }

    // Tentukan musim
    const scheduleDate = new Date(recommendation.Schedule.date);
    const month = scheduleDate.getMonth() + 1;
    const season = month >= 11 || month <= 3 ? 'Winter' : 'Summer';

    // Persiapkan payload untuk API like
    const likePayload = {
      user_id: user.googleId,
      product_name: recommendation.displayName,
      subcategory: recommendation.subCategory,
      input_params: {
        skin_tone: capitalizeFirstLetter(user.colorType),
        gender: capitalizeFirstLetter(user.genderCategory),
        usage: capitalizeFirstLetter(recommendation.Schedule.type),
        season: season
      }
    };

    // Kirim like ke API
    const likeResponse = await axios.post(`${BASE_URL}/add_like`, likePayload, {
      headers: {
        'Content-Type': 'application/json'
      },
      timeout: 10000 // 10 detik timeout
    });

    // Berikan respons
    res.status(200).json({
      message: 'Like added successfullyy',
      data: likeResponse.data
    });

  } catch (error) {
    console.error('Add Like Error:', error);

    // Tangani error dari axios
    if (error.response) {
      return res.status(error.response.status).json({
        error: 'Error from recommendation API',
        details: error.response.data
      });
    }

    // Error umum
    res.status(500).json({ 
      error: 'An error occurred while processing the like',
      details: error.message 
    });
  }
};

module.exports = {
  recommendation,
  addLike
};