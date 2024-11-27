import os
import tempfile
import requests
import numpy as np
from flask import Flask, request, jsonify
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.image import load_img, img_to_array
from PIL import Image
from io import BytesIO
import pickle

app = Flask(__name__)

# Fungsi untuk memuat model dari URL
def load_model_from_url(url):
    response = requests.get(url)
    if response.status_code == 200:
        # Membuat direktori sementara yang dapat diakses
        temp_dir = tempfile.mkdtemp()
        
        model_path = os.path.join(temp_dir, 'model.h5')
        
        # Menyimpan file model ke direktori sementara
        with open(model_path, 'wb') as f:
            f.write(response.content)
        
        # Memuat model dari file sementara
        model = load_model(model_path)
        return model
    else:
        raise Exception(f"Failed to load model from URL. Status code: {response.status_code}")

# Fungsi untuk memuat file pickle (vectorizer, label encoder) dari URL
def load_pickle_from_url(url):
    response = requests.get(url)
    if response.status_code == 200:
        # Membuat direktori sementara yang dapat diakses
        temp_dir = tempfile.mkdtemp()
        
        pickle_path = os.path.join(temp_dir, 'file.pkl')
        
        # Menyimpan file pickle ke direktori sementara
        with open(pickle_path, 'wb') as f:
            f.write(response.content)
        
        # Memuat vectorizer atau label encoder
        with open(pickle_path, 'rb') as f:
            return pickle.load(f)
    else:
        raise Exception(f"Failed to load file from URL. Status code: {response.status_code}")

# Parameter gambar untuk prediksi warna
img_height, img_width = 224, 224
class_labels = ["fall", "spring", "summer", "winter"]

# Endpoint untuk prediksi aktivitas berdasarkan input teks
@app.route('/predict/activity', methods=['POST'])
def predict_activity():
    try:
        data = request.get_json()

        if 'folder_url' not in data:
            return jsonify({"error": "No folder URL provided"}), 400
        
        folder_url = data['folder_url']

        # Mengonfigurasi URL untuk file model, vectorizer, dan label encoder
        model_url = os.path.join(folder_url, 'nlp.h5')
        vectorizer_url = os.path.join(folder_url, 'vectorizer.pkl')
        label_encoder_url = os.path.join(folder_url, 'label_encoder.pkl')

        # Memuat model, vectorizer, dan label encoder dari URL
        model = load_model_from_url(model_url)
        vectorizer = load_pickle_from_url(vectorizer_url)
        label_encoder = load_pickle_from_url(label_encoder_url)

        # Mendapatkan input teks dari permintaan
        input_text = data.get('input_text', '')
        if not input_text:
            return jsonify({"error": "No input text provided"}), 400
        
        # Mengubah input teks menjadi vektor menggunakan vectorizer
        input_vectorized = vectorizer.transform([input_text])

        # Prediksi kategori menggunakan model
        predictions = model.predict(input_vectorized.toarray())
        predicted_index = predictions.argmax()
        predicted_category = label_encoder.inverse_transform([predicted_index])[0]

        # Mengembalikan hasil prediksi
        return jsonify({
            "predicted_category": predicted_category,
            "prediction_probability": predictions[0].tolist()
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500

# Endpoint untuk prediksi berdasarkan URL gambar dan model
@app.route('/predict/color', methods=['POST'])
def predict_color():
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
if __name__ == "__main__":
    app.run(debug=True)
