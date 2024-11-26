import os
import numpy as np
import requests
from flask import Flask, request, jsonify
from tensorflow.keras.preprocessing.image import load_img, img_to_array
from tensorflow.keras.models import load_model
from PIL import Image
from io import BytesIO

# Initialize the Flask app
app = Flask(__name__)

# Function to load model from a URL
def load_model_from_url(url):
    response = requests.get(url)
    if response.status_code == 200:
        model_file = BytesIO(response.content)
        model = load_model(model_file)
        return model
    else:
        raise Exception("Failed to load model from URL")

# Define image parameters
img_height, img_width = 224, 224
class_labels = ["fall", "spring", "summer", "winter"]

# Endpoint for image URL and prediction
@app.route('/predict/color', methods=['POST'])
def predict():
    try:
        # Get the JSON data from the request
        data = request.get_json()
        
        # Check if the required fields are in the request
        if 'image_url' not in data or 'model_url' not in data:
            return jsonify({"error": "No image URL or model URL provided"}), 400
        
        image_url = data['image_url']
        model_url = data['model_url']
        
        # Load the model from the provided URL
        model = load_model_from_url(model_url)
        
        # Fetch the image from the URL
        response = requests.get(image_url)
        
        # Check if the request was successful
        if response.status_code != 200:
            return jsonify({"error": "Failed to retrieve image from URL"}), 400
        
        # Open the image from the response content
        img = Image.open(BytesIO(response.content))
        img = img.resize((img_height, img_width))  # Resize the image
        img_array = img_to_array(img) / 255.0  # Normalize the image
        img_array = np.expand_dims(img_array, axis=0)  # Add batch dimension
        
        # Predict the class
        prediction = model.predict(img_array)
        predicted_class = np.argmax(prediction, axis=-1)
        predicted_label = class_labels[predicted_class[0]]

        return jsonify({
            "predicted_class": predicted_label,
            "prediction_probability": prediction[0].tolist()
        })
    
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# Run the Flask app
if __name__ == "__main__":
    app.run(debug=True)
