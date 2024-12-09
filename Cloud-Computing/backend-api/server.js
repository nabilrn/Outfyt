const express = require('express');
const marked = require('marked');
const fs = require('fs');
require("dotenv").config({ path: "../../.env" });

const app = express();
const port = process.env.PORT || 8080;
const routes = require('./routes.js');

app.use(express.json());
app.use('/api', routes);

// Middleware to render Markdown documentation
app.get('/', (req, res) => {
  try {
    // Read the Markdown file
    const markdownPath = 'API_Documentation.md';
    const markdownContent = fs.readFileSync(markdownPath, 'utf8');

    // Convert Markdown to HTML
    const htmlContent = marked.parse(markdownContent);

    // HTML template with some basic styling
    const htmlResponse = `
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>API Documentation</title>
      <style>
        body {
          font-family: Arial, sans-serif;
          line-height: 1.6;
          max-width: 800px;
          margin: 0 auto;
          padding: 20px;
          background-color: #f4f4f4;
        }
        pre {
          background-color: #f1f1f1;
          padding: 15px;
          border-radius: 5px;
          overflow-x: auto;
        }
        code {
          background-color: #f1f1f1;
          padding: 2px 4px;
          border-radius: 4px;
        }
        h1, h2 {
          color: #333;
          border-bottom: 1px solid #ccc;
          padding-bottom: 10px;
        }
        hr {
          border: 0;
          height: 1px;
          background: #333;
          margin: 20px 0;
        }
      </style>
    </head>
    <body>
      ${htmlContent}
    </body>
    </html>
    `;

    // Send the HTML response
    res.send(htmlResponse);
  } catch (error) {
    // Error handling if Markdown file can't be read
    res.status(500).send(`
      <html>
        <body>
          <h1>Error Loading Documentation</h1>
          <p>${error.message}</p>
        </body>
      </html>
    `);
  }
});

app.listen(port, () => {
  console.log(`Server sukses berjalan di http://localhost:${port}`);
});

module.exports = app;