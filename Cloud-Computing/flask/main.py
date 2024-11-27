import os
import numpy as np
import requests
from flask import Flask, request, jsonify
from tensorflow.keras.preprocessing.image import load_img, img_to_array
from tensorflow.keras.models import load_model
from PIL import Image
from io import BytesIO
import tempfile  # Untuk file sementara

# Inisialisasi aplikasi Flask
app = Flask(_name_)

# Fungsi untuk memuat model dari URL
def load_model_from_url(url):
    response = requests.get(url)
    if response.status_code == 200:
        with tempfile.NamedTemporaryFile(suffix=".h5", delete=True) as temp_model_file:
            temp_model_file.write(response.content)
            temp_model_file.flush()  # Pastikan semua data tersimpan ke disk
            model = load_model(temp_model_file.name)
            return model
    else:
        raise Exception("Failed to load model from URL. Status code: " + str(response.status_code))

# Parameter gambar
img_height, img_width = 224, 224
class_labels = ["fall", "spring", "summer", "winter"]

# Endpoint untuk prediksi berdasarkan URL gambar dan model
@app.route('/predict/color', methods=['POST'])
def predict():
    try:
        # Mendapatkan data JSON dari permintaan
        data = request.get_json()
        
        # Memeriksa apakah 'image_url' dan 'model_url' ada dalam permintaan
        if 'image_url' not in data or 'model_url' not in data:
            return jsonify({"error": "No image URL or model URL provided"}), 400
        
        image_url = data['image_url']
        model_url = data['model_url']
        
        # Memuat model dari URL
        model = load_model_from_url(model_url)
        
        # Mengunduh gambar dari URL
        response = requests.get(image_url)
        if response.status_code != 200:
            return jsonify({"error": "Failed to retrieve image from URL. Status code: " + str(response.status_code)}), 400
        
        # Membuka dan memproses gambar
        img = Image.open(BytesIO(response.content))
        img = img.resize((img_height, img_width))  # Mengubah ukuran gambar
        img_array = img_to_array(img) / 255.0  # Normalisasi piksel
        img_array = np.expand_dims(img_array, axis=0)  # Menambahkan dimensi batch
        
        # Prediksi kelas
        prediction = model.predict(img_array)
        predicted_class = np.argmax(prediction, axis=-1)
        predicted_label = class_labels[predicted_class[0]]

        # Mengembalikan hasil prediksi
        return jsonify({
            "predicted_class": predicted_label,
            "prediction_probability": prediction[0].tolist()
        })
    
    except Exception as e:
        # Menangkap error dan mengembalikan pesan error
        return jsonify({"error": str(e)}), 500

# Menjalankan aplikasi Flask
if _name_ == "_main_":
    app.run(debug=True)