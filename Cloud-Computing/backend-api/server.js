// server.js
const express = require('express');
const app = express();
const port = 3000;
const routes = require('./routes.js');

app.use(express.json());
app.use('/', routes);

app.listen(port, () => {
  console.log(`Server berjalan di http://localhost:${port}`);
});
