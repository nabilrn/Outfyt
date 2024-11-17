// server.js
const express = require('express');
require("dotenv").config({ path: "../../.env" });
const app = express();
const port = process.env.PORT || 8080;
const routes = require('./routes.js');

app.use(express.json());
app.use('/api', routes);

app.listen(port, () => {
  console.log(`Server berjalan di http://localhost:${port}`);
});
