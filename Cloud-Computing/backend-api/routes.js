const express = require("express");

const { auth, verifyAccessToken, refreshAccessToken  } = require("./controller/auth.js");
const { scrapeNews } = require("./controller/news.js");
const { getCalendar } = require("./controller/calendar.js");
const { start, send, stream } = require("./controller/chat.js");
const {uploadImage}= require("./controller/uploadImage.js")
const {personalColor}= require("./controller/personalColor.js")
const multer = require("multer");
const router = express.Router();

const upload = multer({
    storage: multer.memoryStorage(),
});
router.get("/test", async (req, res) => {
    const respon = {
        message: "Awesome, Server running successfully",
    };
    res.json(respon);
});


router.post("/auth", auth);
router.post("/refresh-token", refreshAccessToken);
router.get("/calendar", verifyAccessToken,  getCalendar);
router.post(
    "/upload-image",
    verifyAccessToken,
    upload.single("image"),
    uploadImage
);
router.get("/personal-color", verifyAccessToken, personalColor);
router.get("/news",verifyAccessToken, scrapeNews);
router.post("/chat/start",verifyAccessToken, start);
router.post("/chat/send",verifyAccessToken, send);
router.post("/chat/stream",verifyAccessToken, stream);
router.post("/recommendation",verifyAccessToken, recommendation);
router.post("/recommendation/like",verifyAccessToken, addLike);
router.post("/rekomendasi",verifyAccessToken, addLike);


module.exports = router;
