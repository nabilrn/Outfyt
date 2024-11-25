const tf = require('@tensorflow/tfjs-node'); // Changed from tfjs to tfjs-node
const { Storage } = require('@google-cloud/storage');
require("dotenv").config({ path: "../../.env" });

const storage = new Storage({
  credentials: JSON.parse(process.env.BUCKET_KEY), // Menggunakan JSON yang langsung di-decode dari environment variable
});


const bucketName = process.env.BUCKET_NAME;

// Improved model verification function
async function checkModelExists(modelPath) {
  try {
    const response = await fetch(modelPath);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const modelJson = await response.json();
    if (!modelJson.modelTopology) {
      throw new Error('Invalid model format: missing modelTopology');
    }
    console.log('Model metadata verified');
    return true;
  } catch (error) {
    console.error('Error checking model:', error);
    return false;
  }
}

// Updated preprocessing function
async function preprocessImage(buffer) {
  try {
    // Create tensor from buffer
    const tensor = tf.node.decodeImage(buffer);
    
    // Ensure 3 channels (RGB)
    const rgb = tensor.slice([0, 0, 0], [-1, -1, 3]);
    
    // Resize to model input size (224x224)
    const resized = tf.image.resizeBilinear(rgb, [224, 224]);
    
    // Normalize to [0,1]
    const normalized = tf.div(resized, 255.0);
    
    // Add batch dimension
    const batched = tf.expandDims(normalized, 0);
    
    // Cleanup intermediate tensors
    tensor.dispose();
    rgb.dispose();
    resized.dispose();
    normalized.dispose();
    
    return batched;
  } catch (error) {
    console.error('Error in preprocessing:', error);
    throw error;
  }
}

const uploadImage = async (req, res) => {
  try {
    console.log("File upload received:", req.file?.originalname);

    if (!req.file) {
      return res.status(400).json({
        message: 'No file uploaded'
      });
    }

    const fileName = `image/${Date.now()}_${req.file.originalname}`;
    const blob = storage.bucket(bucketName).file(fileName);
    const blobStream = blob.createWriteStream({
      resumable: false,
      contentType: req.file.mimetype,
    });

    blobStream.on('error', (err) => {
      console.error('Cloud Storage upload error:', err);
      res.status(500).json({
        message: 'Failed to upload file',
        error: err.message
      });
    });

    blobStream.on('finish', async () => {
      const publicUrl = `https://storage.googleapis.com/${bucketName}/${fileName}`;
      console.log('Upload successful, URL:', publicUrl);

      try {
        const modelPath = `https://storage.googleapis.com/${bucketName}/model-color/model.json`;
        console.log('Loading model from:', modelPath);

        // Verify model existence
        const modelExists = await checkModelExists(modelPath);
        if (!modelExists) {
          throw new Error('Model not found or inaccessible');
        }

        // Load model with proper handler
        const model = await tf.loadGraphModel(modelPath, {
          onProgress: (fraction) => {
            console.log(`Model loading: ${(fraction * 100).toFixed(2)}%`);
          }
        });

        console.log('Model loaded successfully');
        
        // Process image
        const inputTensor = await preprocessImage(req.file.buffer);
        console.log('Image preprocessed, shape:', inputTensor.shape);

        // Run prediction
        const prediction = model.predict(inputTensor);
        const predictionData = await prediction.data();

        // Clean up tensors
        inputTensor.dispose();
        prediction.dispose();
        model.dispose();

        console.log('Prediction results:', predictionData);

        res.status(200).json({
          message: 'Upload and prediction successful',
          url: publicUrl,
          prediction: Array.from(predictionData),
        });

      } catch (error) {
        console.error('Prediction error:', error);
        res.status(500).json({
          message: 'Failed to process prediction',
          error: error.message
        });
      }
    });

    blobStream.end(req.file.buffer);

  } catch (error) {
    console.error('Upload process error:', error);
    res.status(500).json({
      message: 'Error during file upload',
      error: error.message
    });
  }
};

module.exports = {
  uploadImage,
};