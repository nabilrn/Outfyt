require("dotenv").config({ path: "../../.env" });
const axios = require('axios');
const cheerio = require('cheerio');

// require("dotenv").config();
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


module.exports = {
  scrapeNews,
};