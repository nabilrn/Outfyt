from flask import Flask, request, jsonify
import joblib
from outfit_recommender import LikableOutfitRecommender

app = Flask(__name__)

# Muat model dari file .joblib
model_path = 'https://storage.googleapis.com/outfyt-image-bucket/model-recommendation/outfit_recommender_model.joblib'
recommender = LikableOutfitRecommender(model_path)

@app.route('/')
def home():
    return "Outfit Recommender API is running! "

# Endpoint untuk mendapatkan rekomendasi outfit
@app.route('/get_recommendation', methods=['POST'])
def get_recommendation():
    data = request.json
    user_id = data.get('user_id')
    input_params = data.get('input_params', {})
    
    try:
        outfit = recommender.get_complete_outfit(user_id, **input_params)
        return jsonify({
            "user_id": user_id,
            "recommendations": outfit
        })
    except AttributeError:
        return jsonify({"error": "Model tidak memiliki metode 'get_complete_outfit'"}), 500
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# Endpoint untuk menambahkan "like"
@app.route('/add_like', methods=['POST'])
def add_like():
    data = request.json
    user_id = data.get('user_id')
    product_name = data.get('product_name')
    subcategory = data.get('subcategory')
    input_params = data.get('input_params', {})
    
    try:
        recommender.add_like(
            user_id=user_id,
            product_name=product_name,
            subcategory=subcategory,
            **input_params
        )
        return jsonify({"message": "Produk berhasil ditambahkan ke daftar kesukaan."})
    except AttributeError:
        return jsonify({"error": "Model tidak memiliki metode 'add_like'"}), 500
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# Menjalankan aplikasi
if __name__ == '__main__':
    app.run(debug=True)
