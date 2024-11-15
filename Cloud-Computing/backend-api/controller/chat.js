const { GoogleGenerativeAI } = require('@google/generative-ai');
require("dotenv").config({ path: "../../../.env" });


// Inisialisasi Gemini
const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

// Fungsi untuk mendapatkan model chat
const getGeminiChat = async () => {
  const model = genAI.getGenerativeModel({ model: "gemini-pro" });
  const chat = model.startChat({
    history: [],
    generationConfig: {
      maxOutputTokens: 2048,
      temperature: 0.9,
      topP: 0.1,
      topK: 16,
    },
  });
  return chat;
};

// Route untuk memulai chat baru
const start= async (req, res) => {
  try {
    const chat = await getGeminiChat();
    res.json({
      status: 'success',
      message: 'Chat session started successfully'
    });
  } catch (error) {
    console.error('Error starting chat:', error);
    res.status(500).json({
      status: 'error',
      message: 'Failed to start chat session',
      error: error.message
    });
  }
};

// Route untuk mengirim pesan dan mendapatkan respons
const send = async (req, res) => {
  try {
    const { message } = req.body;

    if (!message) {
      return res.status(400).json({
        status: 'error',
        message: 'Message is required'
      });
    }

    const chat = await getGeminiChat();
    const result = await chat.sendMessage(message);
    const response = await result.response;
    
    res.json({
      status: 'success',
      data: {
        reply: response.text(),
        promptFeedback: response.promptFeedback
      }
    });

  } catch (error) {
    console.error('Error sending message:', error);
    res.status(500).json({
      status: 'error',
      message: 'Failed to process message',
      error: error.message
    });
  }
};

// Route untuk stream chat (real-time response)
const stream = async (req, res) => {
  try {
    const { message } = req.body;

    if (!message) {
      return res.status(400).json({
        status: 'error',
        message: 'Message is required'
      });
    }

    // Set headers for SSE
    res.setHeader('Content-Type', 'text/event-stream');
    res.setHeader('Cache-Control', 'no-cache');
    res.setHeader('Connection', 'keep-alive');

    const chat = await getGeminiChat();
    const result = await chat.sendMessage(message);
    const response = await result.response;
    
    // Send the response in chunks
    const chunks = response.text().split(' ');
    for (const chunk of chunks) {
      res.write(`data: ${chunk}\n\n`);
      await new Promise(resolve => setTimeout(resolve, 100)); // Delay between chunks
    }
    
    res.write('event: end\ndata: END\n\n');
    res.end();

  } catch (error) {
    console.error('Error in stream:', error);
    res.write(`event: error\ndata: ${error.message}\n\n`);
    res.end();
  }
};

module.exports={
    start,
    send,
    stream,

}