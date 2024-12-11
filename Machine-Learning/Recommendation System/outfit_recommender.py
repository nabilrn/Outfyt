import os
import pandas as pd
import numpy as np
import joblib
import re
import tensorflow as tf
from sklearn.preprocessing import LabelEncoder, MinMaxScaler
from sklearn.model_selection import train_test_split
from collections import defaultdict

class DeepLearningOutfitRecommender:
    def __init__(self, csv_path='styles_new.csv', sep=';', model_path='/content/deep_outfit_recommender.h5'):
        self.skin_color_matches = {
            'Summer': [
                'Navy Blue', 'Pink', 'Lavender',
                'Purple', 'Blue', 'Teal',
                'White', 'Grey', 'Sea Green',
                'Turquoise Blue', 'Green'   
            ],
            'Fall': [
                'Burgundy', 'Brown', 'Olive',
                'Maroon', 'Bronze', 'Copper',
                'Gold', 'Red', 'Orange'
            ],
            'Winter': [
                'Black', 'White', 'Grey',
                'Navy Blue', 'Red', 'Purple',
                'Burgundy', 'Silver', 'Teal'
            ],
            'Spring': [
                'Green', 'Lime Green', 'Yellow',
                'Pink', 'Light Blue', 'Turquoise',
                'Orange', 'Maroon'
            ]
        }

        self.subcategory_likes = defaultdict(lambda: defaultdict(lambda: defaultdict(int)))
        self.user_recommendations = defaultdict(lambda: defaultdict(list))
        self.user_likes = defaultdict(lambda: defaultdict(list))
        
        self.model_path = model_path
        self.label_encoders = {}
        self.scaler = MinMaxScaler()
        
        if os.path.exists(model_path):
            self.load_model()
        else:
            self.data = None
            df = pd.read_csv(csv_path, sep=sep)
            df.dropna(inplace=True)
            
            df['productDisplayName'] = df['productDisplayName'].astype(str)
            df['id'] = df['id'].astype(str)
            df['gender'] = df['gender'].astype(str)
            df['baseColour'] = df['baseColour'].astype(str)
            df['usage'] = df['usage'].astype(str)
            df['season'] = df['season'].astype(str)
            
            self.fit(df)
            self.save_model()

    def build_model(self, input_dim):
        """
        Build the neural network model architecture with matching dimensions
        """
        inputs = tf.keras.Input(shape=(input_dim,))
        
        # Encoder
        x = tf.keras.layers.Dense(256, activation='relu')(inputs)
        x = tf.keras.layers.Dropout(0.3)(x)
        x = tf.keras.layers.Dense(128, activation='relu')(x)
        x = tf.keras.layers.Dropout(0.2)(x)
        
        # Embedding layer (same dimension as input)
        embedding = tf.keras.layers.Dense(input_dim, activation='tanh', name='embedding')(x)
        
        # Decoder
        x = tf.keras.layers.Dense(128, activation='relu')(embedding)
        x = tf.keras.layers.Dropout(0.2)(x)
        x = tf.keras.layers.Dense(256, activation='relu')(x)
        x = tf.keras.layers.Dropout(0.3)(x)
        
        # Output layers
        reconstructed = tf.keras.layers.Dense(input_dim, name='reconstruction')(x)
        similarity = tf.keras.layers.Dense(1, activation='sigmoid', name='similarity')(embedding)
        
        model = tf.keras.Model(inputs=inputs, outputs=[reconstructed, similarity])
        
        model.compile(
            optimizer='adam',
            loss={
                'reconstruction': 'mse',
                'similarity': 'binary_crossentropy'
            },
            loss_weights={
                'reconstruction': 0.5,
                'similarity': 0.5
            }
        )
        
        return model

    def preprocess_text(self, text):
        text = str(text).lower()
        text = re.sub(r'[^a-zA-Z0-9\s]', '', text)
        return text

    def create_feature_vector(self, data):
        categorical_features = ['gender', 'masterCategory', 'subCategory', 'articleType',
                              'baseColour', 'season', 'usage']
        
        encoded_features_list = []
        for feature in categorical_features:
            if feature in data.columns:
                encoded_feature = self.label_encoders[feature].transform(data[feature])
                encoded_features_list.append(encoded_feature.reshape(-1, 1))
        
        encoded_features = np.hstack(encoded_features_list)
        encoded_features_scaled = self.scaler.transform(encoded_features)
        
        return encoded_features_scaled

    def fit(self, data, epochs=10, batch_size=32):
        self.data = data.copy().reset_index(drop=True)
        
        categorical_features = ['gender', 'masterCategory', 'subCategory', 'articleType',
                              'baseColour', 'season', 'usage']
        
        encoded_features_list = []
        for feature in categorical_features:
            if feature in self.data.columns:
                self.label_encoders[feature] = LabelEncoder()
                encoded_feature = self.label_encoders[feature].fit_transform(self.data[feature])
                encoded_features_list.append(encoded_feature.reshape(-1, 1))
        
        encoded_features = np.hstack(encoded_features_list)
        self.input_dim = encoded_features.shape[1]  # Store input dimension
        encoded_features_scaled = self.scaler.fit_transform(encoded_features)
        
        # Create training data
        X = encoded_features_scaled
        y_reconstruction = X  # For autoencoder
        y_similarity = np.ones(len(X))  # For similarity
        
        # Generate negative samples
        negative_samples = np.random.rand(*X.shape)
        X = np.vstack([X, negative_samples])
        y_reconstruction = np.vstack([y_reconstruction, negative_samples])
        y_similarity = np.hstack([y_similarity, np.zeros(len(negative_samples))])
        
        # Split data
        X_train, X_val, y_rec_train, y_rec_val, y_sim_train, y_sim_val = train_test_split(
            X, y_reconstruction, y_similarity, test_size=0.2, random_state=42
        )
        
        # Build and train model
        self.model = self.build_model(self.input_dim)
        
        # Train the model
        self.model.fit(
            X_train,
            {
                'reconstruction': y_rec_train,
                'similarity': y_sim_train.reshape(-1, 1)
            },
            validation_data=(
                X_val,
                {
                    'reconstruction': y_rec_val,
                    'similarity': y_sim_val.reshape(-1, 1)
                }
            ),
            epochs=epochs,
            batch_size=batch_size
        )
        
        # Get embeddings layer model
        self.embedding_model = tf.keras.Model(
            inputs=self.model.input,
            outputs=self.model.get_layer('embedding').output
        )
        
        # Generate embeddings for all items
        self.item_embeddings = self.embedding_model.predict(encoded_features_scaled)
        
        return self

    def save_model(self):
        self.model.save(self.model_path)
        
        components = {
            'data': self.data,
            'label_encoders': self.label_encoders,
            'scaler': self.scaler,
            'item_embeddings': self.item_embeddings,
            'input_dim': self.input_dim
        }
        
        joblib.dump(components, f"{self.model_path}_components.joblib")
        print(f"Model saved to {self.model_path}")

    def load_model(self):
        self.model = tf.keras.models.load_model(self.model_path)
        
        components = joblib.load(f"{self.model_path}_components.joblib")
        
        self.data = components['data']
        self.label_encoders = components['label_encoders']
        self.scaler = components['scaler']
        self.item_embeddings = components['item_embeddings']
        self.input_dim = components['input_dim']
        
        self.embedding_model = tf.keras.Model(
            inputs=self.model.input,
            outputs=self.model.get_layer('embedding').output
        )
        
        print(f"Model loaded from {self.model_path}")

    def get_recommendations_by_subcategory(self, user_id, skin_tone, gender, usage, season, subcategory, n=4):
        recommended_colors = self.skin_color_matches.get(skin_tone, [])
        
        mask = (
            (self.data['subCategory'] == subcategory) &
            (self.data['gender'] == gender) &
            (self.data['usage'] == usage) &
            (self.data['season'] == season) &
            (self.data['baseColour'].isin(recommended_colors))
        )
        
        filtered_data = self.data[mask].reset_index(drop=True)
        
        if len(filtered_data) == 0:
            return pd.DataFrame()
        
        # Get embeddings and similarity scores
        filtered_features = self.create_feature_vector(filtered_data)
        filtered_embeddings = self.embedding_model.predict(filtered_features)
        _, similarity_scores = self.model.predict(filtered_features)
        
        base_scores = similarity_scores.flatten()
        
        like_scores = np.zeros(len(filtered_data))
        for i, row in filtered_data.iterrows():
            input_key = f"{skin_tone}_{gender}_{usage}_{season}"
            likes = self.subcategory_likes[subcategory][input_key][row['id']]
            like_scores[i] = likes
        
        if like_scores.max() > 0:
            like_scores = like_scores / like_scores.max()
        
        embedding_similarities = tf.matmul(
            filtered_embeddings, filtered_embeddings, transpose_b=True
        ).numpy()
        diversity_penalty = embedding_similarities.mean(axis=1)
        
        final_scores = (
            0.6 * base_scores +
            0.3 * like_scores -
            0.1 * diversity_penalty +
            0.1 * np.random.random(len(filtered_data))
        )
        
        n = min(n, len(filtered_data))
        top_indices = np.argsort(final_scores)[::-1][:n]
        
        recommendations = filtered_data.iloc[top_indices][[
            'id', 'productDisplayName', 'gender', 'baseColour', 'usage',
            'season', 'masterCategory', 'subCategory'
        ]].copy()
        
        recommendations['score'] = final_scores[top_indices]
        
        self.user_recommendations[user_id][subcategory].extend(recommendations['id'].tolist())
        
        return recommendations

    def get_complete_outfit(self, user_id, skin_tone, gender, usage, season):
        outfit_recommendations = {
            'Recommended Colors': self.skin_color_matches[skin_tone],
            'Outfit': {},
            'Input': {
                'skin_tone': skin_tone,
                'gender': gender,
                'usage': usage,
                'season': season
            }
        }
        
        subcategories = [
            'Accessories', 'Headwear', 'Bottomwear', 'Topwear',
            'Flip Flops', 'Sandal', 'Shoes', 'Sports Equipment'
        ]
        
        for subcat in subcategories:
            recommendations = self.get_recommendations_by_subcategory(
                user_id, skin_tone, gender, usage, season, subcat
            )
            if not recommendations.empty:
                outfit_recommendations['Outfit'][subcat] = recommendations.to_dict('records')
        
        return outfit_recommendations

    def add_like(self, user_id, product_name, subcategory, skin_tone, gender, usage, season, rating=1):
        input_key = f"{skin_tone}_{gender}_{usage}_{season}"
        
        product = self.data[self.data['productDisplayName'] == product_name]
        if len(product) == 0:
            print(f"Product not found: {product_name}")
            return
        
        item_id = product.iloc[0]['id']
        self.subcategory_likes[subcategory][input_key][item_id] += rating
        self.user_likes[user_id][subcategory].append(item_id)